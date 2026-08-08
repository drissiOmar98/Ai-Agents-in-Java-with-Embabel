package com.omar.trip_planner.model;

/**
 * A single city on the trip, with the number of days the traveler
 * currently plans to spend there.
 *
 * @param city        the destination city's name
 * @param plannedDays how many days the traveler currently intends to spend here
 */
public record DestinationStop(String city, int plannedDays) {
}
