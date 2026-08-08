package com.omar.trip_planner.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Type-safe configuration properties for TripCompass, bound from the
 * {@code trip-compass} prefix in {@code application.yml}.
 *
 * <pre>{@code
 * trip-compass:
 *   max-packing-items-per-category: 6
 * }</pre>
 *
 * @param maxPackingItemsPerCategory the maximum number of items generated per packing
 *                                   list category; defaults to {@code 6} when zero or negative
 */
@ConfigurationProperties(prefix = "trip-compass")
public record TripCompassProperties(int maxPackingItemsPerCategory) {

    /**
     * Compact constructor applying a sensible default when the value is
     * missing or invalid, so the application never fails to start due to
     * incomplete configuration.
     */
    public TripCompassProperties {
        if (maxPackingItemsPerCategory <= 0) {
            maxPackingItemsPerCategory = 6;
        }
    }
}
