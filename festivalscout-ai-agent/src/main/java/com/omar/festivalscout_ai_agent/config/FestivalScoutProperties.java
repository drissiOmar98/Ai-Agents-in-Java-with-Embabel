package com.omar.festivalscout_ai_agent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Type-safe configuration properties for FestivalScout, bound from the
 * {@code festival-scout} prefix in {@code application.yml}.
 *
 * <pre>{@code
 * festival-scout:
 *   max-must-see-artists: 8
 *   max-packing-items-per-category: 6
 * }</pre>
 *
 * @param maxMustSeeArtists             the maximum number of must-see artist picks to
 *                                      generate; defaults to {@code 8} when zero or negative
 * @param maxPackingItemsPerCategory    the maximum number of items generated per packing
 *                                      list category (essentials, weather-specific, comfort);
 *                                      defaults to {@code 6} when zero or negative
 */
@ConfigurationProperties(prefix = "festival-scout")
public record FestivalScoutProperties(int maxMustSeeArtists, int maxPackingItemsPerCategory) {

    /**
     * Compact constructor applying sensible defaults when values are
     * missing or invalid, so the application never fails to start due to
     * incomplete configuration.
     */
    public FestivalScoutProperties {
        if (maxMustSeeArtists <= 0) {
            maxMustSeeArtists = 8;
        }
        if (maxPackingItemsPerCategory <= 0) {
            maxPackingItemsPerCategory = 6;
        }
    }
}
