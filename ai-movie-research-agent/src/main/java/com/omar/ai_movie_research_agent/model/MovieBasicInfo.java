package com.omar.ai_movie_research_agent.model;

import java.time.LocalDate;

/**
 * The core identity of a movie, extracted directly from the user's free-text
 * request. This is the first fact established in the pipeline and anchors
 * every subsequent lookup (actors, director, genres, plot).
 *
 * @param name        the movie's title, as identified from user input
 * @param releaseDate the movie's original release date
 */
public record MovieBasicInfo(String name, LocalDate releaseDate) {
}
