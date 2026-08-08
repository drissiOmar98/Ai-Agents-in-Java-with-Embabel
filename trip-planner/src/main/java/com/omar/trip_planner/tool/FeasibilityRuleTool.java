package com.omar.trip_planner.tool;

import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * A plain trip-feasibility rule engine.
 *
 * <p>Deliberately <strong>not</strong> annotated as an {@code @LlmTool}.
 * By the time {@link com.omar.trip_planner.agent.TripPlanningAgent#checkFeasibility}
 * runs, it already holds the exact total transit hours and trip length as
 * typed numbers — whether that transit load is reasonable for a given pace
 * is a ratio against a fixed tolerance threshold, not a judgment call.
 * This class is called directly as a normal Spring bean; the itinerary
 * step downstream is where genuine judgment (how to restructure an
 * infeasible plan) belongs.</p>
 */
@Component
public class FeasibilityRuleTool {

    private record PaceProfile(double usableHoursPerDay, double maxTransitRatio) {
    }

    // usableHoursPerDay: how many hours a day this pace assumes for
    // sightseeing/activities. maxTransitRatio: the maximum share of total
    // usable hours that can reasonably go to transit before the plan is
    // considered overpacked for this pace.
    private static final PaceProfile RELAXED = new PaceProfile(6.0, 0.30);
    private static final PaceProfile MODERATE = new PaceProfile(8.0, 0.20);
    private static final PaceProfile PACKED = new PaceProfile(10.0, 0.15);

    /**
     * Computes the ratio of total transit hours to total usable trip hours,
     * and whether that ratio is reasonable for the traveler's stated pace.
     *
     * @param totalTripDays     the total number of days for the trip
     * @param totalTransitHours the sum of every inter-city leg's estimated duration
     * @param pace              one of {@code "RELAXED"}, {@code "MODERATE"}, or {@code "PACKED"}
     * @return the computed transit-to-usable-hours ratio
     */
    public double calculateTransitRatio(int totalTripDays, double totalTransitHours, String pace) {
        PaceProfile profile = resolveProfile(pace);
        double totalUsableHours = totalTripDays * profile.usableHoursPerDay();
        if (totalUsableHours <= 0) {
            return 1.0; // no usable time at all is trivially "fully consumed"
        }
        return totalTransitHours / totalUsableHours;
    }

    /**
     * Determines whether a computed transit ratio is within a reasonable
     * tolerance for the given pace.
     *
     * @param transitRatio the ratio computed by {@link #calculateTransitRatio}
     * @param pace         one of {@code "RELAXED"}, {@code "MODERATE"}, or {@code "PACKED"}
     * @return {@code true} if the ratio is within this pace's tolerance
     */
    public boolean isFeasible(double transitRatio, String pace) {
        return transitRatio <= resolveProfile(pace).maxTransitRatio();
    }

    private PaceProfile resolveProfile(String pace) {
        return switch (pace == null ? "" : pace.toUpperCase(Locale.ROOT)) {
            case "RELAXED" -> RELAXED;
            case "PACKED" -> PACKED;
            default -> MODERATE; // reasonable default for an unrecognized or missing pace
        };
    }
}
