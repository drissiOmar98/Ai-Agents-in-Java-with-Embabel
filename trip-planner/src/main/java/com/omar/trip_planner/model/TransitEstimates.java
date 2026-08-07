package com.omar.trip_planner.model;

import java.util.List;

/**
 * Every inter-city transit leg the itinerary requires, with realistic
 * time estimates computed deterministically.
 *
 * <p>Produced by
 * {@link com.omar.trip_planner.agent.TripPlanningAgent#estimateTransitTimes}
 * using {@link com.omar.trip_planner.tool.TransitTimeEstimatorTool} for the
 * actual time calculation, so each leg's duration reflects a real formula
 * — including fixed overhead like airport check-in or station transfers —
 * rather than an LLM's bare guess at "how long that probably takes."</p>
 *
 * @param legs             every transit leg the itinerary requires, in trip order
 * @param totalTransitHours the sum of every leg's estimated duration
 */
public record TransitEstimates(List<TransitLeg> legs, double totalTransitHours) {
}
