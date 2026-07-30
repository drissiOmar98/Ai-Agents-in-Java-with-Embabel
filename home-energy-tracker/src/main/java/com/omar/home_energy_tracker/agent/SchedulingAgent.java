package com.omar.home_energy_tracker.agent;

import com.omar.home_energy_tracker.config.WattWiseProperties;
import com.omar.home_energy_tracker.model.ApplianceSchedule;
import com.omar.home_energy_tracker.model.CostBreakdownReport;
import com.omar.home_energy_tracker.model.HouseholdProfile;
import com.omar.home_energy_tracker.model.TariffPlan;
import com.omar.home_energy_tracker.persona.Personas;
import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.OperationContext;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Downstream agent contributing an appliance-scheduling goal to the
 * {@link EnergyAnalysisAgent} pipeline.
 *
 * <p>Kept separate from {@link EnergyAnalysisAgent} because scheduling is
 * about timing existing usage differently, not changing what's owned or
 * how it's used — a distinct lever from the savings plan's recommendations.
 * If the household's {@link TariffPlan} has no peak/off-peak split, this
 * goal has nothing meaningful to add and can be skipped by a caller who
 * only wants the savings plan.</p>
 */
@Agent(description = "Recommends timing shifts for flexible appliances to avoid peak electricity pricing")
public class SchedulingAgent {

    private final WattWiseProperties properties;

    /**
     * @param properties bound {@code watt-wise.*} configuration (max schedule recommendations)
     */
    public SchedulingAgent(WattWiseProperties properties) {
        this.properties = properties;
    }

    /**
     * Recommends timing shifts for flexible appliances (e.g. laundry,
     * dishwashers, EV charging) to move usage into off-peak pricing hours,
     * using the {@link Personas#HOME_ENERGY_COACH} persona so
     * recommendations read as practical, everyday advice.
     *
     * <p>Marked as its own {@link AchievesGoal}, reachable independently
     * of {@link EnergyAnalysisAgent#generateSavingsPlan}.</p>
     *
     * @param householdProfile    the output of {@link EnergyAnalysisAgent#extractHouseholdProfile}
     * @param tariffPlan          the output of {@link EnergyAnalysisAgent#extractTariffPlan}
     * @param costBreakdownReport the output of {@link EnergyAnalysisAgent#calculateCostBreakdown}
     * @param context             Embabel's operation context, providing access to the LLM
     * @return concrete timing recommendations, capped at
     *         {@link WattWiseProperties#maxScheduleRecommendations()}
     */
    @AchievesGoal(description = "A schedule for shifting flexible appliance usage into off-peak hours")
    @Action(description = "Recommend appliance timing shifts to avoid peak electricity pricing")
    public ApplianceSchedule generateApplianceSchedule(HouseholdProfile householdProfile, TariffPlan tariffPlan,
                                                          CostBreakdownReport costBreakdownReport, OperationContext context) {
        String appliancesSummary = householdProfile.appliances().stream()
                .map(appliance -> "- %s (%s)".formatted(appliance.applianceName(), appliance.usagePattern()))
                .collect(Collectors.joining("\n"));

        return context.ai()
                .withDefaultLlm()
                .withPromptContributors(List.of(Personas.HOME_ENERGY_COACH))
                .createObjectIfPossible(
                        """
                        Peak hours window: %s
                        Off-peak price per kWh: $%.4f, Peak price per kWh: $%.4f

                        Household appliances:
                        %s

                        If there's no meaningful peak/off-peak price difference or no
                        peak window is defined, say so plainly and don't force
                        recommendations. Otherwise, recommend up to %d specific timing
                        shifts for flexible appliances (laundry, dishwashers, EV
                        charging, etc. - not appliances that must run continuously like
                        refrigerators) to move usage into off-peak hours, with a rough
                        sense of the saving.
                        Create an ApplianceSchedule from these recommendations.
                        """.formatted(
                                tariffPlan.peakHoursWindow(),
                                tariffPlan.offPeakPricePerKwh(),
                                tariffPlan.peakPricePerKwh(),
                                appliancesSummary,
                                properties.maxScheduleRecommendations()
                        ),
                        ApplianceSchedule.class
                );
    }
}
