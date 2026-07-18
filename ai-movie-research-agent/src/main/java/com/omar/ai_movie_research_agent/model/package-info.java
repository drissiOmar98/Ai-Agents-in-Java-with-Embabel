/**
 * Immutable data carriers that flow through the {@code MovieInfoAgent}
 * pipeline, from raw user input to a fully assembled movie profile and its
 * downstream recommendations.
 *
 * <p>{@link com.omar.ai_movie_research_agent.model.Movie} is the pipeline's central goal
 * output, assembled from {@link com.omar.ai_movie_research_agent.model.MovieBasicInfo},
 * {@link com.omar.ai_movie_research_agent.model.MovieDirector},
 * {@link com.omar.ai_movie_research_agent.model.MovieActors},
 * {@link com.omar.ai_movie_research_agent.model.MovieGenres}, and
 * {@link com.omar.ai_movie_research_agent.model.MoviePlotSummary}.
 * {@link com.omar.ai_movie_research_agent.model.MovieRecommendations} is a secondary output
 * derived from a completed {@code Movie}.</p>
 */
package com.omar.ai_movie_research_agent.model;
