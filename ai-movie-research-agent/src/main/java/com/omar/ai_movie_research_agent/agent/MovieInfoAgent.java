package com.omar.ai_movie_research_agent.agent;


import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.OperationContext;
import com.embabel.agent.domain.io.UserInput;
import com.omar.ai_movie_research_agent.config.CineScoutProperties;
import com.omar.ai_movie_research_agent.exception.MovieDataUnavailableException;
import com.omar.ai_movie_research_agent.model.*;
import com.omar.ai_movie_research_agent.persona.Personas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Embabel agent that turns a free-text movie request into a fully assembled
 * {@link Movie} profile, and can go on to recommend similar movies.
 *
 * <p>Each lookup (basic info, cast, genres, plot, director) is its own
 * independent {@link Action}, run against the same {@link UserInput} or
 * previously resolved facts. Embabel's planner resolves the dependency
 * graph from each method's parameter types and executes them in the
 * order needed to reach the requested goal &mdash; there is no manually
 * sequenced control flow here.</p>
 *
 * <p>Pipeline shape:</p>
 *
 * <pre>
 * UserInput
 *   ├──&gt; getMovieBasicInfo   -&gt; MovieBasicInfo
 *   ├──&gt; getMovieActors      -&gt; MovieActors
 *   ├──&gt; getMovieGenres      -&gt; MovieGenres
 *   └──&gt; getPlotSummary      -&gt; MoviePlotSummary
 *            │
 *            ▼
 *   getMovieInfo (+ director lookup)  🎯 GOAL  -&gt; Movie
 *            │
 *            ▼
 *   getSimilarMovies                  🎯 GOAL  -&gt; MovieRecommendations
 * </pre>
 */
@Agent(
        name = "movie-info-provider",
        description = "Researches a movie and provides a complete profile, including similar-movie recommendations",
        version = "2.0.0",
        beanName = "movieInfoProviderAgent"
)
public class MovieInfoAgent {

    private static final Logger log = LoggerFactory.getLogger(MovieInfoAgent.class);

    private final CineScoutProperties properties;

    /**
     * @param properties bound {@code cinescout.*} configuration (max actors, max recommendations)
     */
    public MovieInfoAgent(CineScoutProperties properties) {
        this.properties = properties;
    }

    /**
     * Extracts the movie's core identity (title and release date) from the
     * user's free-text request.
     *
     * <p>This is the anchor fact for the rest of the pipeline: every other
     * lookup either re-reads the raw {@link UserInput} directly (cast,
     * genres, plot) or depends on this resolved identity (director, via
     * {@link #getMovieInfo}) to avoid ambiguity with similarly named
     * films.</p>
     *
     * @param userInput the user's free-text request, e.g. {@code "Tell me about Inception"}
     * @param context   Embabel's operation context, providing access to the LLM
     * @return the movie's title and release date
     */
    @Action
    public MovieBasicInfo getMovieBasicInfo(UserInput userInput, OperationContext context) {
        log.debug("Resolving basic info for input: {}", userInput.getContent());
        return context.ai()
                .withDefaultLlm()
                .createObjectIfPossible(
                        """
                        Create a MovieBasicInfo from this user input, extracting their details:
                        %s""".formatted(userInput.getContent()),
                        MovieBasicInfo.class
                );
    }

    /**
     * Identifies the movie's principal cast, capped at
     * {@link CineScoutProperties#maxActors()} to keep the result focused on
     * the most notable names rather than a full credits list.
     *
     * @param userInput the user's free-text request
     * @param context   Embabel's operation context, providing access to the LLM
     * @return the movie's principal cast, most notable first
     */
    @Action
    public MovieActors getMovieActors(UserInput userInput, OperationContext context) {
        return context.ai()
                .withDefaultLlm()
                .createObjectIfPossible(
                        """
                        Extract the movie name from this user input: %s
                        Then identify its %d most notable principal cast members,
                        most notable first.
                        Create a MovieActors from that cast list.
                        """.formatted(userInput.getContent(), properties.maxActors()),
                        MovieActors.class
                );
    }

    /**
     * Identifies the movie's genre classification(s).
     *
     * @param userInput the user's free-text request
     * @param context   Embabel's operation context, providing access to the LLM
     * @return the movie's genres, primary genre first
     */
    @Action
    public MovieGenres getMovieGenres(UserInput userInput, OperationContext context) {
        return context.ai()
                .withDefaultLlm()
                .createObjectIfPossible(
                        """
                        Extract the movie name from this user input: %s
                        Then identify its genre classification(s), primary genre first.
                        Create a MovieGenres from that list.
                        """.formatted(userInput.getContent()),
                        MovieGenres.class
                );
    }

    /**
     * Writes a short, spoiler-free plot synopsis.
     *
     * <p>Uses the {@link Personas#FILM_CRITIC} persona so the summary reads
     * like an engaging critic's blurb rather than a dry plot dump, and is
     * explicitly instructed to stop at the premise rather than describe the
     * ending.</p>
     *
     * @param userInput the user's free-text request
     * @param context   Embabel's operation context, providing access to the LLM
     * @return a 2-3 sentence, spoiler-free synopsis
     */
    @Action(description = "Write a short, spoiler-free plot summary")
    public MoviePlotSummary getPlotSummary(UserInput userInput, OperationContext context) {
        return context.ai()
                .withDefaultLlm()
                .withPromptContributors(List.of(Personas.FILM_CRITIC))
                .createObjectIfPossible(
                        """
                        Extract the movie name from this user input: %s
                        Then write a 2-3 sentence synopsis of its premise.
                        Cover only the setup - do not reveal plot twists, the ending,
                        or any major turning points.
                        Create a MoviePlotSummary from that synopsis.
                        """.formatted(userInput.getContent()),
                        MoviePlotSummary.class
                );
    }


}
