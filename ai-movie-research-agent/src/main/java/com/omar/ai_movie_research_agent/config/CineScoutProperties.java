package com.omar.ai_movie_research_agent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Type-safe configuration properties for CineScout, bound from the
 * {@code cinescout} prefix in {@code application.yml}.
 *
 * <pre>{@code
 * cinescout:
 *   max-actors: 5
 *   max-similar-movies: 3
 * }</pre>
 *
 * @param maxActors         the maximum number of cast members to return per movie;
 *                          defaults to {@code 5} when zero or negative
 * @param maxSimilarMovies  the maximum number of similar-movie recommendations to
 *                          generate; defaults to {@code 3} when zero or negative
 */
@ConfigurationProperties(prefix = "cinescout")
public record CineScoutProperties(int maxActors, int maxSimilarMovies) {

    /**
     * Compact constructor applying sensible defaults when values are
     * missing or invalid, so the application never fails to start due to
     * incomplete configuration.
     */
    public CineScoutProperties {
        if (maxActors <= 0) {
            maxActors = 5;
        }
        if (maxSimilarMovies <= 0) {
            maxSimilarMovies = 3;
        }
    }
}
