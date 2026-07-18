package com.omar.ai_movie_research_agent.model;

import java.util.List;

/**
 * The principal cast of a movie.
 *
 * <p>The number of actors returned is capped by
 * {@link com.omar.ai_movie_research_agent.config.CineScoutProperties#maxActors()} to keep the
 * result focused on the most notable names rather than a full credits
 * list.</p>
 *
 * @param actors the movie's principal cast members, most notable first
 */
public record MovieActors(List<String> actors) {
}
