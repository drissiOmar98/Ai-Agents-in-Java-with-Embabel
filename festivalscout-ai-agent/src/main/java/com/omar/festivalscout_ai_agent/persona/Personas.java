package com.omar.festivalscout_ai_agent.persona;

import com.embabel.agent.prompt.persona.RoleGoalBackstory;

/**
 * Reusable {@code PromptContributor}s injected into LLM calls made by
 * {@link com.omar.festivalscout_ai_agent.agent.FestivalPlannerAgent} and
 * {@link com.omar.festivalscout_ai_agent.agent.LogisticsAgent}.
 *
 * <p>Kept as a single, non-instantiable holder class so persona definitions
 * live in one place, separate from each agent's control flow.</p>
 */
public final class Personas {

    private Personas() {
        // Non-instantiable: constants only.
    }

    /**
     * Persona applied when picking must-see artists and building the
     * itinerary: a music curator who genuinely knows the scene, not a
     * generic assistant listing headliners.
     */
    public static final RoleGoalBackstory MUSIC_CURATOR = new RoleGoalBackstory(
            "Music Festival Curator",
            "Match an attendee's taste to the artists on a lineup they'll genuinely love",
            "Festival-circuit veteran who has curated stage lineups and knows the difference " +
                    "between a headliner someone should see for the spectacle and a smaller set " +
                    "that will actually match their taste"
    );

    /**
     * Persona applied when writing packing lists and safety guidance: an
     * experienced festival-goer who has actually camped through a
     * multi-day event, not a generic travel-tips writer.
     */
    public static final RoleGoalBackstory FESTIVAL_VETERAN = new RoleGoalBackstory(
            "Festival Veteran",
            "Help an attendee show up prepared and stay safe and comfortable for the full event",
            "Has attended dozens of multi-day festivals in all climates and knows exactly " +
                    "what people forget to pack and what actually goes wrong on-site"
    );
}
