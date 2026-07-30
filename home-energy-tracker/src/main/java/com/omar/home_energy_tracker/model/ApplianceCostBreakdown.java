package com.omar.home_energy_tracker.model;

/**
 * A single appliance's calculated monthly energy consumption and cost.
 *
 * @param applianceName          the appliance this line item covers
 * @param estimatedKwhPerMonth   the calculated monthly energy consumption in kWh
 * @param estimatedCostPerMonthUsd the calculated monthly cost in USD
 */
public record ApplianceCostBreakdown(String applianceName, double estimatedKwhPerMonth, double estimatedCostPerMonthUsd) {
}
