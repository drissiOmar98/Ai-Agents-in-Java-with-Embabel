package com.omar.home_energy_tracker.agent;

import com.omar.home_energy_tracker.model.BillBreakdownReport;
import com.omar.home_energy_tracker.model.CostBreakdownReport;
import com.omar.home_energy_tracker.model.HouseholdProfile;
import com.omar.home_energy_tracker.persona.Personas;
import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.OperationContext;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Downstream agent contributing a plain-language bill explanation goal to
 * the {@link EnergyAnalysisAgent} pipeline.
 *
 * <p>Kept separate from {@link EnergyAnalysisAgent} because explaining a
 * bill and proposing a savings plan are distinct deliverables — one is for
 * understanding where the money already went, the other for what to do
 * about it. This agent depends only on {@link CostBreakdownReport} and
 * {@link HouseholdProfile}, so it's reachable independently of
 * {@link EnergyAnalysisAgent#generateSavingsPlan}.</p>
 */
@Agent(description = "Explains a household's estimated energy bill in plain language")
public class ReportingAgent {

    /**
     * Writes a plain-language summary of where the household's estimated
     * monthly bill goes, using the {@link Personas#HOME_ENERGY_COACH}
     * persona so the result reads like a helpful explanation rather than a
     * raw data dump.
     *
     * <p>Marked as its own {@link AchievesGoal} since a bill explanation is
     * useful on its own, without needing a savings plan to exist first.</p>
     *
     * @param costBreakdownReport the output of {@link EnergyAnalysisAgent#calculateCostBreakdown}
     * @param householdProfile    the output of {@link EnergyAnalysisAgent#extractHouseholdProfile}
     * @param context             Embabel's operation context, providing access to the LLM
     * @return a plain-language summary plus a few skimmable category breakdown lines
     */
    @AchievesGoal(description = "A plain-language explanation of where the household's energy bill goes")
    @Action(description = "Explain the household's estimated bill in plain language")
    public BillBreakdownReport generateBillBreakdownReport(CostBreakdownReport costBreakdownReport,
                                                             HouseholdProfile householdProfile, OperationContext context) {
        String breakdownSummary = costBreakdownReport.breakdown().stream()
                .map(item -> "- %s: $%.2f/month".formatted(item.applianceName(), item.estimatedCostPerMonthUsd()))
                .collect(Collectors.joining("\n"));

        return context.ai()
                .withDefaultLlm()
                .withPromptContributors(List.of(Personas.HOME_ENERGY_COACH))
                .createObjectIfPossible(
                        """
                        Household: %d people, located in %s
                        Total estimated monthly bill: $%.2f

                        Per-appliance costs:
                        %s

                        Write a short, plain-language summary a non-technical homeowner
                        would immediately understand - what's driving the bill, in terms
                        of real appliances and habits, not raw kWh figures. Then give a
                        few grouped, skimmable lines (e.g. category and rough share of
                        the bill).
                        Create a BillBreakdownReport from this summary and breakdown.
                        """.formatted(
                                householdProfile.householdSize(),
                                householdProfile.location(),
                                costBreakdownReport.totalEstimatedMonthlyCostUsd(),
                                breakdownSummary
                        ),
                        BillBreakdownReport.class
                );
    }
}
