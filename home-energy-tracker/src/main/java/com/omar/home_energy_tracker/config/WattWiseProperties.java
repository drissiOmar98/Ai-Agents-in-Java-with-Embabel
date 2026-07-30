package com.omar.home_energy_tracker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Type-safe configuration properties for WattWise, bound from the
 * {@code watt-wise} prefix in {@code application.yml}.
 *
 * <pre>{@code
 * watt-wise:
 *   max-savings-actions: 8
 *   max-schedule-recommendations: 6
 * }</pre>
 *
 * @param maxSavingsActions            the maximum number of ranked savings actions to
 *                                     generate; defaults to {@code 8} when zero or negative
 * @param maxScheduleRecommendations   the maximum number of appliance scheduling
 *                                     recommendations to generate; defaults to {@code 6}
 *                                     when zero or negative
 */
@ConfigurationProperties(prefix = "watt-wise")
public record WattWiseProperties(int maxSavingsActions, int maxScheduleRecommendations) {

    /**
     * Compact constructor applying sensible defaults when values are
     * missing or invalid, so the application never fails to start due to
     * incomplete configuration.
     */
    public WattWiseProperties {
        if (maxSavingsActions <= 0) {
            maxSavingsActions = 8;
        }
        if (maxScheduleRecommendations <= 0) {
            maxScheduleRecommendations = 6;
        }
    }
}
