package com.omar.ai_movie_research_agent.model;

/**
 * The director of a movie.
 *
 * <p>Looked up last in the pipeline, once the movie's identity is already
 * confirmed via {@link MovieBasicInfo}, to reduce the chance of the model
 * attributing the wrong director to an ambiguously named film.</p>
 *
 * @param name the director's full name
 */
public record MovieDirector(String name) {
}
