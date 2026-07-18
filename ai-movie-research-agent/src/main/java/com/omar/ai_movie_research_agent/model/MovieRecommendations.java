package com.omar.ai_movie_research_agent.model;

import java.util.List;

/**
 * Recommendations for other movies a viewer might enjoy, based on a
 * previously assembled {@link Movie} profile.
 *
 * @param similarMovies titles recommended as similar, most relevant first
 * @param reason        a brief explanation of what ties the recommendations
 *                       to the original movie (theme, director, genre, tone, etc.)
 */
public record MovieRecommendations(List<String> similarMovies, String reason) {
}
