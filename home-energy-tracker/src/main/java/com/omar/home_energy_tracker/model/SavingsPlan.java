package com.omar.home_energy_tracker.model;

import java.util.List;

/**
 * The household's ranked, cost-grounded energy savings plan.
 *
 * <p>This is the primary goal output of
 * {@link com.omar.home_energy_tracker.agent.EnergyAnalysisAgent#generateSavingsPlan}.</p>
 *
 * @param actions                        ranked savings actions, capped at
 *                                       {@link com.wattwise.config.WattWiseProperties#maxSavingsActions()}
 * @param totalEstimatedMonthlySavingsUsd the sum of every action's estimated monthly savings
 */
public record SavingsPlan(List<SavingsAction> actions, double totalEstimatedMonthlySavingsUsd) {
}
