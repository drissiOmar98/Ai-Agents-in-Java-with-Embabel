package com.omar.trip_planner.model;

/**
 * A deterministic verdict on whether the planned itinerary is actually
 * feasible, given the total transit time it requires against the
 * traveler's stated pace.
 *
 * <p>Produced by
 * {@link com.omar.trip_planner.agent.TripPlanningAgent#checkFeasibility} using
 * {@link com.omar.trip_planner.tool.FeasibilityRuleTool} for the actual
 * calculation — whether transit time eats an unreasonable share of the
 * trip's usable hours is a ratio with one correct answer given the
 * traveler's pace tolerance, not a judgment call. Consumed by
 * {@link com.omar.trip_planner.agent.TripPlanningAgent#generateItinerary} so an
 * infeasible plan gets restructured rather than presented as-is.</p>
 *
 * @param isFeasible           whether the plan fits within a reasonable
 *                             transit-to-sightseeing-time ratio for the stated pace
 * @param transitToUsableRatio the computed ratio of total transit hours to total
 *                             usable trip hours
 * @param verdict              a plain-language explanation of the computed ratio and verdict
 */
public record FeasibilityReport(boolean isFeasible, double transitToUsableRatio, String verdict) {
}
