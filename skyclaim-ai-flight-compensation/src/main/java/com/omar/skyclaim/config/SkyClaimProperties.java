package com.omar.skyclaim.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Type-safe configuration properties for SkyClaim, bound from the
 * {@code sky-claim} prefix in {@code application.yml}.
 *
 * <pre>{@code
 * sky-claim:
 *   max-rebooking-steps: 8
 *   max-care-entitlements: 6
 * }</pre>
 *
 * @param maxRebookingSteps     the maximum number of rebooking/next-step recommendations
 *                              to generate; defaults to {@code 8} when zero or negative
 * @param maxCareEntitlements   the maximum number of care entitlement items to list;
 *                              defaults to {@code 6} when zero or negative
 */
@ConfigurationProperties(prefix = "sky-claim")
public record SkyClaimProperties(int maxRebookingSteps, int maxCareEntitlements) {

    /**
     * Compact constructor applying sensible defaults when values are
     * missing or invalid, so the application never fails to start due to
     * incomplete configuration.
     */
    public SkyClaimProperties {
        if (maxRebookingSteps <= 0) {
            maxRebookingSteps = 8;
        }
        if (maxCareEntitlements <= 0) {
            maxCareEntitlements = 6;
        }
    }
}
