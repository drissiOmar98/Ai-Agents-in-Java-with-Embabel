package com.omar.trip_planner.model;

import java.util.List;

/**
 * A packing list spanning every destination on the trip, accounting for
 * possible climate differences between cities.
 *
 * <p>This is a goal output of
 * {@link com.omar.trip_planner.agent.PackingAdvisorAgent#generatePackingList}.</p>
 *
 * @param essentials       non-negotiable items (documents, chargers, medication, etc.)
 * @param climateSpecificItems items driven by the range of climates across every
 *                          destination on the trip, not just one city
 * @param comfortItems      quality-of-life items experienced multi-city travelers pack
 */
public record PackingList(List<String> essentials, List<String> climateSpecificItems, List<String> comfortItems) {
}
