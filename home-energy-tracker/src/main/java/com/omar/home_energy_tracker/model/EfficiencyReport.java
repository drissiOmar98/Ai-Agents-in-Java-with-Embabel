package com.omar.home_energy_tracker.model;

import java.util.List;

/**
 * Every inefficiency identified across the household's appliance usage,
 * judged against the computed cost breakdown.
 *
 * <p>Feeds into
 * {@link com.omar.home_energy_tracker.agent.EnergyAnalysisAgent#generateSavingsPlan} so
 * recommendations address real, cost-grounded issues rather than generic
 * energy-saving advice.</p>
 *
 * @param findings every identified inefficiency
 */
public record EfficiencyReport(List<EfficiencyFinding> findings) {
}
