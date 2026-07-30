package com.omar.home_energy_tracker.agent;

import com.omar.home_energy_tracker.config.WattWiseProperties;
import com.omar.home_energy_tracker.exception.HouseholdProfileIncompleteException;
import com.omar.home_energy_tracker.model.*;
import com.omar.home_energy_tracker.persona.Personas;
import com.omar.home_energy_tracker.tool.EnergyCostCalculatorTool;
import com.omar.home_energy_tracker.tool.PaybackPeriodTool;
import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.OperationContext;
import com.embabel.agent.domain.io.UserInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Embabel agent that turns a household's appliance list and electricity
 * tariff into a grounded cost breakdown and a ranked, cost-justified
 * savings plan.
 *
 * <p>This class owns the core extraction-through-savings-plan spine.
 * {@link ReportingAgent} and {@link SchedulingAgent} contribute independent
 * downstream goals &mdash; a plain-language bill explanation and an
 * off-peak usage schedule &mdash; both reachable from the same cost
 * breakdown and tariff facts.</p>
 *
 * <p>Pipeline shape:</p>
 *
 * <pre>
 * UserInput
 *   ├──&gt; extractHouseholdProfile -&gt; HouseholdProfile
 *   └──&gt; extractTariffPlan       -&gt; TariffPlan
 *            │
 *            ▼
 *   calculateCostBreakdown (deterministic) -&gt; CostBreakdownReport
 *            │
 *            ▼
 *   identifyEfficiencyIssues -&gt; EfficiencyReport
 *            │
 *            ▼
 *   generateSavingsPlan  🎯 GOAL  -&gt; SavingsPlan
 *
 *   (HouseholdProfile + TariffPlan + CostBreakdownReport also feed
 *    ReportingAgent#generateBillBreakdownReport  🎯 GOAL
 *    SchedulingAgent#generateApplianceSchedule   🎯 GOAL)
 * </pre>
 */
@Agent(
        name = "home-energy-tracker",
        description = "Analyzes household energy usage and proposes a cost-justified savings plan",
        version = "1.0.0",
        beanName = "energyAnalysisAgent"
)
public class EnergyAnalysisAgent {

    private static final Logger log = LoggerFactory.getLogger(EnergyAnalysisAgent.class);

    private final WattWiseProperties properties;
    private final EnergyCostCalculatorTool energyCostCalculatorTool;
    private final PaybackPeriodTool paybackPeriodTool;

    /**
     * @param properties               bound {@code watt-wise.*} configuration (max savings actions)
     * @param energyCostCalculatorTool tool exposed to the LLM for computing a deterministic
     *                                 per-appliance cost breakdown in {@link #calculateCostBreakdown}
     * @param paybackPeriodTool        plain calculator used directly in Java to compute payback
     *                                 periods in {@link #generateSavingsPlan}
     */
    public EnergyAnalysisAgent(WattWiseProperties properties, EnergyCostCalculatorTool energyCostCalculatorTool,
                                PaybackPeriodTool paybackPeriodTool) {
        this.properties = properties;
        this.energyCostCalculatorTool = energyCostCalculatorTool;
        this.paybackPeriodTool = paybackPeriodTool;
    }

    /**
     * Extracts the household's appliance profile from the user's input.
     *
     * @param userInput free-text input expected to describe the household's
     *                  appliances and its electricity tariff
     * @param context   Embabel's operation context, providing access to the LLM
     * @return the household's appliances, location, and size
     * @throws HouseholdProfileIncompleteException if no appliances could be identified at all
     */
    @Action
    public HouseholdProfile extractHouseholdProfile(UserInput userInput, OperationContext context) {
        HouseholdProfile profile = context.ai()
                .withDefaultLlm()
                .createObjectIfPossible(
                        """
                        The following text describes a household's appliances and
                        electricity tariff. Extract only the appliance and household
                        details:
                        %s

                        For each appliance, identify its name, approximate wattage while
                        running, estimated hours per day it runs or draws power, and its
                        usage pattern (e.g. always-on, daily, a few times a week). Also
                        identify the household's general location and how many people
                        live there.
                        Create a HouseholdProfile from these details.
                        """.formatted(userInput.getContent()),
                        HouseholdProfile.class
                );

        if (profile == null || profile.appliances() == null || profile.appliances().isEmpty()) {
            throw new HouseholdProfileIncompleteException(
                    "Could not identify any appliances from the submitted household description");
        }
        return profile;
    }

    /**
     * Extracts the household's electricity rate structure from the user's
     * input.
     *
     * @param userInput free-text input expected to describe the household's
     *                  appliances and its electricity tariff
     * @param context   Embabel's operation context, providing access to the LLM
     * @return the household's peak/off-peak pricing and fixed monthly charge
     */
    @Action
    public TariffPlan extractTariffPlan(UserInput userInput, OperationContext context) {
        return context.ai()
                .withDefaultLlm()
                .createObjectIfPossible(
                        """
                        The following text describes a household's appliances and
                        electricity tariff. Extract only the tariff/rate details:
                        %s

                        Identify the price per kWh (off-peak or flat rate), the peak-hour
                        price per kWh if a time-of-use plan is mentioned (otherwise equal
                        to the off-peak price), the peak hours window if any (otherwise
                        "none"), and any fixed monthly charge.
                        Create a TariffPlan from these details.
                        """.formatted(userInput.getContent()),
                        TariffPlan.class
                );
    }


}
