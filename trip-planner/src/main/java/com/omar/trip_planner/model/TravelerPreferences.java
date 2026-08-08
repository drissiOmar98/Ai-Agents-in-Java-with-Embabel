package com.omar.trip_planner.model;

import java.util.List;

/**
 * The traveler's personal preferences and constraints, extracted from
 * their free-text description.
 *
 * @param interests      what the traveler wants to prioritize, e.g.
 *                       {@code "museums"}, {@code "food"}, {@code "hiking"}
 * @param pace           one of {@code "RELAXED"}, {@code "MODERATE"}, or {@code "PACKED"} —
 *                       how much sightseeing the traveler wants to fit into each day
 * @param totalBudgetUsd the traveler's total trip budget in USD
 * @param travelStyle    a short descriptor, e.g. {@code "backpacking"}, {@code "comfort"},
 *                       {@code "luxury"}
 */
public record TravelerPreferences(List<String> interests, String pace, double totalBudgetUsd, String travelStyle) {
}
