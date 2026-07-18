package com.omar.ai_movie_research_agent.exception;

/**
 * Thrown when the agent cannot obtain a required piece of movie data from
 * the LLM (e.g. the model returns no result for a director lookup).
 *
 * <p>Used in place of a generic assertion failure so callers get a
 * meaningful, domain-specific error rather than a bare
 * {@code IllegalArgumentException} with no context about which lookup
 * failed.</p>
 */
public class MovieDataUnavailableException extends RuntimeException {

    /**
     * @param message a description of what data was missing and for which movie
     */
    public MovieDataUnavailableException(String message) {
        super(message);
    }
}
