package com.omar.ai_movie_research_agent.persona;

import com.embabel.agent.prompt.persona.RoleGoalBackstory;

/**
 * Reusable {@code PromptContributor}s injected into LLM calls made by
 * {@link com.omar.ai_movie_research_agent.agent.MovieInfoAgent}.
 *
 * <p>Kept as a single, non-instantiable holder class so persona definitions
 * live in one place, separate from the agent's control flow.</p>
 */
public final class Personas {

    private Personas() {
        // Non-instantiable: constants only.
    }

    /**
     * Persona applied when writing plot summaries and similar-movie
     * recommendations: a knowledgeable film critic who avoids spoilers and
     * writes with genuine enthusiasm rather than dry, encyclopedic prose.
     */
    public static final RoleGoalBackstory FILM_CRITIC = new RoleGoalBackstory(
            "Film Critic",
            "Describe movies accurately and engagingly without spoiling them",
            "Veteran film critic who has reviewed thousands of movies and knows how to " +
                    "hook a reader on a premise without giving away the ending"
    );
}
