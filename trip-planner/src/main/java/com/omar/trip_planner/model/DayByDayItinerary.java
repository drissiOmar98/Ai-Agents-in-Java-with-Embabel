package com.omar.trip_planner.model;

import java.util.List;

/**
 * The final day-by-day trip itinerary.
 *
 * <p>This is the primary goal output of
 * {@link com.omar.trip_planner.agent.TripPlanningAgent#generateItinerary}.</p>
 *
 * @param dailyPlans              one narrative entry per trip day, covering sightseeing,
 *                                transit, and rest as appropriate
 * @param wasAdjustedForFeasibility whether the original stop/day allocation had to be
 *                                restructured because it wasn't feasible as originally planned
 */
public record DayByDayItinerary(List<String> dailyPlans, boolean wasAdjustedForFeasibility) {
}
