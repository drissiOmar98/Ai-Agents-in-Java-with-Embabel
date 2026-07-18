package com.omar.ai_movie_research_agent.model;

import java.util.List;

/**
 * The genre classification(s) of a movie (e.g. {@code "Sci-Fi"},
 * {@code "Thriller"}).
 *
 * @param genres the movie's genres, primary genre first
 */
public record MovieGenres(List<String> genres) {
}
