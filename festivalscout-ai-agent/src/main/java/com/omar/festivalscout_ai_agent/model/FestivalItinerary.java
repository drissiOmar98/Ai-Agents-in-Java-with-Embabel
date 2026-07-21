package com.omar.festivalscout_ai_agent.model;

import java.util.List;

/**
 * The final, conflict-free day-by-day festival itinerary.
 *
 * <p>This is the primary goal output of
 * {@link com.omar.festivalscout_ai_agent.agent.FestivalPlannerAgent#buildItinerary}.</p>
 *
 * @param dailyPlans one narrative entry per festival day, covering the
 *                   recommended set schedule plus meal/rest breaks
 */
public record FestivalItinerary(List<String> dailyPlans) {
}
