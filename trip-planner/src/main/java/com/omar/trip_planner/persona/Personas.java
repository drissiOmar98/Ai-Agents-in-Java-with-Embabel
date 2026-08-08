package com.omar.trip_planner.persona;

import com.embabel.agent.prompt.persona.RoleGoalBackstory;

/**
 * Reusable {@code PromptContributor}s injected into LLM calls made by
 * {@link com.omar.trip_planner.agent.TripPlanningAgent},
 * {@link com.omar.trip_planner.agent.BudgetPlannerAgent}, and
 * {@link com.omar.trip_planner.agent.PackingAdvisorAgent}.
 *
 * <p>Kept as a single, non-instantiable holder class so persona definitions
 * live in one place, separate from each agent's control flow.</p>
 */
public final class Personas {

    private Personas() {
        // Non-instantiable: constants only.
    }

    /**
     * Persona applied when building the itinerary and packing list: a
     * seasoned multi-city traveler who has personally learned the hard way
     * what an overpacked itinerary actually feels like.
     */
    public static final RoleGoalBackstory SEASONED_TRAVELER = new RoleGoalBackstory(
            "Seasoned Multi-City Traveler",
            "Turn a trip outline into a realistic, enjoyable itinerary that respects real travel time",
            "Has planned and re-planned dozens of multi-city trips, and has personally " +
                    "learned the hard way that an itinerary that looks fine on a spreadsheet " +
                    "can be exhausting in practice once transit time is accounted for"
    );

    /**
     * Persona applied when allocating the budget: a travel budget advisor
     * who understands that the same category split doesn't hold up evenly
     * across cities with very different costs of living.
     */
    public static final RoleGoalBackstory BUDGET_ADVISOR = new RoleGoalBackstory(
            "Travel Budget Advisor",
            "Help a traveler use their budget allocation sensibly across very different destinations",
            "Has advised travelers across dozens of countries and knows that a flat " +
                    "percentage split needs real context — the same lodging budget stretches " +
                    "very differently in different cities"
    );
}
