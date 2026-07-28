package com.omar.home_energy_tracker.model;

/**
 * A single appliance's power draw and usage pattern, as extracted from the
 * user's free-text description.
 *
 * @param applianceName the appliance's name, e.g. {@code "Refrigerator"}, {@code "Washing machine"}
 * @param wattage       the appliance's power draw in watts while running
 * @param hoursPerDay   the estimated average hours per day the appliance runs or draws power
 * @param usagePattern  a short descriptor of how it's used, e.g. {@code "always-on"},
 *                      {@code "daily"}, {@code "few times a week"}
 */
public record ApplianceUsage(String applianceName, double wattage, double hoursPerDay, String usagePattern) {
}
