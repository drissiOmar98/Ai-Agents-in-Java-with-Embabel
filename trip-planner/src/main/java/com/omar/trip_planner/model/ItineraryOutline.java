package com.omar.trip_planner.model;

import java.util.List;

/**
 * The trip's planned stops, in visiting order, extracted from the
 * traveler's free-text description.
 *
 * <p>This is the anchor fact for the pipeline: transit estimation,
 * feasibility checking, and the final itinerary all work from this list.</p>
 *
 * @param stops         the planned destinations, in visiting order
 * @param totalTripDays the total number of days for the whole trip
 */
public record ItineraryOutline(List<DestinationStop> stops, int totalTripDays) {
}
