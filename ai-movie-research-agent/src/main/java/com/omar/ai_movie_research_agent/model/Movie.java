package com.omar.ai_movie_research_agent.model;

import java.time.LocalDate;
import java.util.List;

/**
 * A complete movie profile assembled from every prior pipeline stage:
 * {@link MovieBasicInfo}, {@link MovieDirector}, {@link MovieActors},
 * {@link MovieGenres}, and {@link MoviePlotSummary}.
 *
 * <p>This is the primary goal output of
 * {@link com.omar.ai_movie_research_agent.model.agent.MovieInfoAgent#getMovieInfo} and the input to
 * the secondary {@link com.omar.ai_movie_research_agent.model.agent.MovieInfoAgent#getSimilarMovies}
 * goal.</p>
 *
 * @param name        the movie's title
 * @param releaseDate the movie's original release date
 * @param director    the movie's director
 * @param actors      the movie's principal cast
 * @param genres      the movie's genre classification(s)
 * @param plotSummary a short, spoiler-free synopsis
 */
public record Movie(
        String name,
        LocalDate releaseDate,
        String director,
        List<String> actors,
        List<String> genres,
        String plotSummary
) {

    /**
     * Assembles a {@code Movie} from the individual pipeline stage outputs.
     *
     * <p>Kept as a secondary compact-style constructor so
     * {@link com.omar.ai_movie_research_agent.model.agent.MovieInfoAgent} can combine the stage
     * records directly without manually unpacking each field at the call
     * site.</p>
     *
     * @param info     the movie's basic identity
     * @param director the movie's director
     * @param actors   the movie's principal cast
     * @param genres   the movie's genre classification(s)
     * @param plot     a short, spoiler-free synopsis
     */
    public Movie(MovieBasicInfo info, MovieDirector director, MovieActors actors,
                 MovieGenres genres, MoviePlotSummary plot) {
        this(info.name(), info.releaseDate(), director.name(), actors.actors(),
                genres.genres(), plot.summary());
    }
}
