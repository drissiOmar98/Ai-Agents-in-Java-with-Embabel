package com.omar.home_energy_tracker.model;

import java.util.List;

/**
 * The household's full appliance profile, extracted from the user's input.
 *
 * <p>This is the anchor fact for the pipeline: cost calculation, efficiency
 * analysis, and scheduling all work appliance-by-appliance from this list.</p>
 *
 * @param appliances    every appliance the household described
 * @param location      the household's general location, used to contextualize
 *                       climate-driven usage (heating/cooling)
 * @param householdSize the number of people living in the household
 */
public record HouseholdProfile(List<ApplianceUsage> appliances, String location, int householdSize) {
}
