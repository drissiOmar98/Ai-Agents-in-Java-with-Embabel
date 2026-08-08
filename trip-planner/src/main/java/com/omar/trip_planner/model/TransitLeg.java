package com.omar.trip_planner.model;

/**
 * A single inter-city transit leg, with its estimated real-world duration.
 *
 * @param fromCity        the departure city
 * @param toCity          the arrival city
 * @param distanceKm      the approximate distance between the two cities in kilometers
 * @param mode            the assumed transport mode: {@code "FLIGHT"}, {@code "TRAIN"},
 *                        {@code "CAR"}, or {@code "BUS"}
 * @param estimatedHours  the total estimated time for this leg, including realistic
 *                        overhead (airport processes, station transfers, etc.), not
 *                        just raw travel time
 */
public record TransitLeg(String fromCity, String toCity, double distanceKm, String mode, double estimatedHours) {
}
