package com.omar.skyclaim.tool;

import org.springframework.stereotype.Component;

/**
 * A plain EU Regulation 261/2004 compensation rule engine.
 *
 * <p>Deliberately <strong>not</strong> annotated as an {@code @LlmTool}.
 * The compensation tier for a given flight distance and arrival delay is
 * fixed by the regulation itself — there is exactly one correct amount per
 * the published rule, the same way a semver bump has exactly one correct
 * answer per semver.org. Asking an LLM to "calculate" this risks it citing
 * a plausible-sounding but wrong figure. So the classification happens
 * here, in plain Java, before the LLM is ever invoked for this step; the
 * model's only job afterward is phrasing a short explanation of an amount
 * that's already been decided.</p>
 *
 * <p>This encodes only the distance/delay rule under Article 7. It does
 * <strong>not</strong> decide extraordinary-circumstance exemptions —
 * that remains a genuine judgment call, made separately by the LLM in
 * {@link com.omar.skyclaim.agent.DisruptionAnalysisAgent#assessEligibility}.</p>
 */
@Component
public class Eu261CompensationRuleTool {

    private static final double SHORT_HAUL_KM = 1500.0;
    private static final double MEDIUM_HAUL_KM = 3500.0;

    private static final double SHORT_HAUL_COMPENSATION = 250.0;
    private static final double MEDIUM_HAUL_COMPENSATION = 400.0;
    private static final double LONG_HAUL_COMPENSATION = 600.0;
    private static final double LONG_HAUL_REDUCED_COMPENSATION = 300.0;

    /**
     * Computes the EU261 Article 7 compensation amount for a given flight
     * distance and arrival delay.
     *
     * @param distanceKm          the flight's great-circle distance in kilometers
     * @param delayHoursAtArrival how many hours late the flight arrived
     * @return the compensation amount in EUR the regulation provides for at this
     *         distance/delay combination, or {@code 0} if the delay is below the
     *         regulation's minimum threshold
     */
    public double calculateCompensation(double distanceKm, double delayHoursAtArrival) {
        if (distanceKm <= SHORT_HAUL_KM) {
            return delayHoursAtArrival >= 3.0 ? SHORT_HAUL_COMPENSATION : 0.0;
        }
        if (distanceKm <= MEDIUM_HAUL_KM) {
            return delayHoursAtArrival >= 3.0 ? MEDIUM_HAUL_COMPENSATION : 0.0;
        }
        // Long-haul: full amount at 4h+, reduced amount for a 3-4h delay.
        if (delayHoursAtArrival >= 4.0) {
            return LONG_HAUL_COMPENSATION;
        }
        if (delayHoursAtArrival >= 3.0) {
            return LONG_HAUL_REDUCED_COMPENSATION;
        }
        return 0.0;
    }

    /**
     * Describes which rule tier applied for a given distance/delay
     * combination, for use alongside {@link #calculateCompensation}.
     *
     * @param distanceKm          the flight's great-circle distance in kilometers
     * @param delayHoursAtArrival how many hours late the flight arrived
     * @return a short label identifying the applicable tier
     */
    public String describeRuleTier(double distanceKm, double delayHoursAtArrival) {
        if (delayHoursAtArrival < 3.0) {
            return "below 3-hour minimum delay threshold - no compensation under Article 7";
        }
        if (distanceKm <= SHORT_HAUL_KM) {
            return "short-haul (<=1500km), delay >= 3h";
        }
        if (distanceKm <= MEDIUM_HAUL_KM) {
            return "medium-haul (1500-3500km), delay >= 3h";
        }
        return delayHoursAtArrival >= 4.0
                ? "long-haul (>3500km), delay >= 4h"
                : "long-haul (>3500km), delay 3-4h (reduced compensation tier)";
    }
}
