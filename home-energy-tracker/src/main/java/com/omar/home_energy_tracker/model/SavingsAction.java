package com.omar.home_energy_tracker.model;

/**
 * A single recommended savings action, with a payback period computed
 * deterministically rather than estimated by the model.
 *
 * <p>{@code paybackPeriod} is intentionally left for
 * {@link com.omar.home_energy_tracker.agent.EnergyAnalysisAgent#generateSavingsPlan} to
 * compute in plain Java from {@code estimatedUpfrontCostUsd} and
 * {@code estimatedMonthlySavingsUsd} — a straightforward division with
 * exactly one correct answer, not something worth asking an LLM to get
 * right across a list of several actions.</p>
 *
 * @param action                     a concrete, specific action to take
 * @param estimatedMonthlySavingsUsd the estimated monthly savings in USD
 * @param estimatedUpfrontCostUsd    any upfront cost required (e.g. buying a
 *                                   replacement appliance), or {@code 0} if the
 *                                   action requires no purchase
 * @param effortLevel                a short descriptor, e.g. {@code "no cost, just a habit change"},
 *                                   {@code "requires a one-time purchase"}
 * @param paybackPeriod              computed in Java: {@code "N/A"} if there's no upfront
 *                                   cost, otherwise a duration like {@code "2.3 years"}
 */
public record SavingsAction(
        String action,
        double estimatedMonthlySavingsUsd,
        double estimatedUpfrontCostUsd,
        String effortLevel,
        String paybackPeriod
) {
}
