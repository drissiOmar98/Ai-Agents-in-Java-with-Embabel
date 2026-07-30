package com.omar.home_energy_tracker.model;

import java.util.List;

/**
 * The household's full monthly energy cost breakdown, computed
 * deterministically per appliance.
 *
 * <p>Produced by
 * {@link com.omar.home_energy_tracker.agent.EnergyAnalysisAgent#calculateCostBreakdown}
 * using {@link com.omar.home_energy_tracker.tool.EnergyCostCalculatorTool} for the actual
 * kWh and cost arithmetic, so the numbers reflect real calculations rather
 * than an LLM's estimate across potentially a dozen appliances at once —
 * exactly the kind of repeated arithmetic LLMs get unreliable at.</p>
 *
 * @param breakdown               per-appliance monthly kWh and cost
 * @param totalEstimatedMonthlyCostUsd the sum of every appliance's cost, plus any
 *                                 fixed monthly charge
 */
public record CostBreakdownReport(List<ApplianceCostBreakdown> breakdown, double totalEstimatedMonthlyCostUsd) {
}
