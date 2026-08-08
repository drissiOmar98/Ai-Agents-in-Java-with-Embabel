/**
 * Immutable data carriers that flow through the TripCompass pipeline, from
 * a raw multi-city trip description to a feasibility-checked itinerary and
 * its companion budget and packing guidance.
 *
 * <p>{@link com.omar.trip_planner.model.ItineraryOutline} and
 * {@link com.omar.trip_planner.model.TravelerPreferences} are extracted
 * independently from the user's input. The outline drives a
 * deterministically computed {@link com.omar.trip_planner.model.TransitEstimates},
 * which together with preferences drives a
 * {@link com.omar.trip_planner.model.FeasibilityReport}. That report shapes the
 * primary {@link com.omar.trip_planner.model.DayByDayItinerary} goal.
 * {@link com.omar.trip_planner.model.BudgetAllocation} and
 * {@link com.omar.trip_planner.model.PackingList} are independent downstream
 * goals derived from the same underlying facts.</p>
 */
package com.omar.trip_planner.model;
