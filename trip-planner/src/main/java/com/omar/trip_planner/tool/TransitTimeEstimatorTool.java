package com.omar.trip_planner.tool;

import com.embabel.agent.api.annotation.LlmTool;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Exposes a deterministic inter-city transit time calculation as an LLM
 * tool.
 *
 * <p>Registered with
 * {@link com.omar.trip_planner.agent.TripPlanningAgent#estimateTransitTimes} so
 * each leg's duration reflects a real formula — average speed for the mode
 * plus realistic fixed overhead (airport processes, station transfers) —
 * rather than an LLM's bare impression of "that's probably a few hours."
 * The LLM's job is estimating distance and the likely transport mode from
 * its geographic knowledge; converting that into a time estimate is
 * arithmetic with one correct answer per mode, handled here.</p>
 */
@Component
public class TransitTimeEstimatorTool {

    private static final String ENTRY_SEPARATOR = ";";
    private static final String FIELD_SEPARATOR = "\\|";

    // Average effective speed (km/h) and fixed overhead (hours) per mode.
    // Overhead covers realistic non-travel time: airport check-in/security,
    // station arrival buffers, etc. - not just point-to-point movement.
    private record ModeProfile(double avgSpeedKmh, double overheadHours) {
    }

    private static final ModeProfile FLIGHT = new ModeProfile(700.0, 2.5);
    private static final ModeProfile TRAIN = new ModeProfile(120.0, 0.5);
    private static final ModeProfile CAR = new ModeProfile(80.0, 0.25);
    private static final ModeProfile BUS = new ModeProfile(70.0, 0.5);

    /**
     * Calculates realistic total transit time (travel time plus overhead)
     * for each leg.
     *
     * @param legs semicolon-separated entries, each formatted as
     *             {@code fromCity|toCity|distanceKm|mode}, where mode is one of
     *             {@code FLIGHT}, {@code TRAIN}, {@code CAR}, or {@code BUS}
     * @return one line per leg formatted as
     *         {@code "fromCity -> toCity :: X.X hours"}, followed by a final
     *         {@code "TOTAL :: Y.Y hours"} line; malformed entries are skipped
     */
    @LlmTool(description = "Calculate realistic total transit time (travel time plus fixed overhead like " +
            "airport or station processes) for a list of inter-city legs (fromCity|toCity|distanceKm|mode, " +
            "separated by semicolons; mode is FLIGHT, TRAIN, CAR, or BUS).")
    public String calculateTransitTimes(
            @LlmTool.Param(description = "Semicolon-separated entries: fromCity|toCity|distanceKm|mode")
            String legs
    ) {
        if (legs == null || legs.isBlank()) {
            return "TOTAL :: 0.0 hours";
        }

        List<String> lines = new ArrayList<>();
        double total = 0.0;

        for (String rawEntry : legs.split(ENTRY_SEPARATOR)) {
            String entry = rawEntry.trim();
            if (entry.isEmpty()) {
                continue;
            }
            String[] fields = entry.split(FIELD_SEPARATOR);
            if (fields.length != 4) {
                continue; // skip malformed entries rather than failing the whole calculation
            }

            try {
                String fromCity = fields[0].trim();
                String toCity = fields[1].trim();
                double distanceKm = Double.parseDouble(fields[2].trim());
                ModeProfile profile = resolveProfile(fields[3].trim());

                double hours = profile.overheadHours() + (distanceKm / profile.avgSpeedKmh());
                lines.add(String.format("%s -> %s :: %.1f hours", fromCity, toCity, hours));
                total += hours;
            } catch (NumberFormatException e) {
                // skip malformed numeric values rather than failing the whole calculation
            }
        }

        lines.add(String.format("TOTAL :: %.1f hours", total));
        return String.join("\n", lines);
    }

    private ModeProfile resolveProfile(String mode) {
        return switch (mode.toUpperCase(Locale.ROOT)) {
            case "FLIGHT" -> FLIGHT;
            case "TRAIN" -> TRAIN;
            case "CAR" -> CAR;
            case "BUS" -> BUS;
            default -> TRAIN; // reasonable default for an unrecognized mode
        };
    }
}
