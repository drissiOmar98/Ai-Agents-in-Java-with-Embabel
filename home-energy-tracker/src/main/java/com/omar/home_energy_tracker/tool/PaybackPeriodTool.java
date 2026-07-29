package com.omar.home_energy_tracker.tool;

import org.springframework.stereotype.Component;

/**
 * A plain payback-period calculator.
 *
 * <p>Deliberately <strong>not</strong> annotated as an {@code @LlmTool}.
 * By the time {@link com.omar.home_energy_tracker.agent.EnergyAnalysisAgent#generateSavingsPlan}
 * computes a payback period, it already holds the exact upfront cost and
 * monthly savings figures as typed numbers — the calculation is a single
 * division with exactly one correct answer, not something worth asking an
 * LLM to get right across a list of several savings actions. This class is
 * called directly as a normal Spring bean, after the model has proposed
 * the underlying action and its estimated figures.</p>
 */
@Component
public class PaybackPeriodTool {

    /**
     * Computes how long an upfront cost takes to pay back given a monthly
     * savings amount.
     *
     * @param upfrontCostUsd    the one-time cost required, or {@code 0} if none
     * @param monthlySavingsUsd the estimated monthly savings the action produces
     * @return {@code "N/A"} if there's no upfront cost or no savings to offset it;
     *         otherwise a duration string like {@code "2.3 years"}
     */
    public String calculatePaybackPeriod(double upfrontCostUsd, double monthlySavingsUsd) {
        if (upfrontCostUsd <= 0) {
            return "N/A";
        }
        if (monthlySavingsUsd <= 0) {
            return "N/A (no savings to offset the cost)";
        }

        double months = upfrontCostUsd / monthlySavingsUsd;
        double years = months / 12.0;
        return String.format("%.1f years", years);
    }
}
