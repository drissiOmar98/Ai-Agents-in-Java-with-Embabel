package com.omar.festivalscout_ai_agent.agent;


import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.OperationContext;
import com.omar.festivalscout_ai_agent.config.FestivalScoutProperties;
import com.omar.festivalscout_ai_agent.model.*;
import com.omar.festivalscout_ai_agent.persona.Personas;
import com.omar.festivalscout_ai_agent.tool.BudgetCalculatorTool;

import java.util.List;

/**
 * Logistics agent contributing three independent, downstream goals to the
 * {@link FestivalPlannerAgent} pipeline: a packing list, a budget estimate,
 * and safety guidance.
 *
 * <p>Kept separate from {@link FestivalPlannerAgent} because these steps
 * are about preparing for and staying safe at the event, not about the
 * lineup/schedule itself. Each depends only on
 * {@link FestivalBasicInfo}/{@link AttendeePreferences}, so any of the
 * three can be requested independently of the itinerary branch of the
 * pipeline.</p>
 */
@Agent(description = "Generates a packing list, budget estimate, and safety guidance for a festival trip")
public class LogisticsAgent {

    private final FestivalScoutProperties properties;
    private final BudgetCalculatorTool budgetCalculatorTool;

    /**
     * @param properties           bound {@code festival-scout.*} configuration (max packing items per category)
     * @param budgetCalculatorTool tool exposed to the LLM for deterministic cost summation
     *                             in {@link #estimateBudget}
     */
    public LogisticsAgent(FestivalScoutProperties properties, BudgetCalculatorTool budgetCalculatorTool) {
        this.properties = properties;
        this.budgetCalculatorTool = budgetCalculatorTool;
    }

    /**
     * Generates a packing list tailored to the festival's format, location,
     * and season.
     *
     * <p>Marked as its own {@link AchievesGoal} since a packing list is
     * useful on its own &mdash; reachable from just
     * {@link FestivalBasicInfo}, without needing a lineup or itinerary to
     * exist first.</p>
     *
     * @param festivalInfo the output of {@link FestivalPlannerAgent#extractFestivalInfo}
     * @param context      Embabel's operation context, providing access to the LLM
     * @return essentials, weather-specific items, and comfort items, each capped at
     *         {@link FestivalScoutProperties#maxPackingItemsPerCategory()}
     */
    @AchievesGoal(description = "A packing list tailored to the festival's format, location, and season")
    @Action(description = "Generate a packing list for the festival")
    public PackingList generatePackingList(FestivalBasicInfo festivalInfo, OperationContext context) {
        return context.ai()
                .withDefaultLlm()
                .withPromptContributors(List.of(Personas.FESTIVAL_VETERAN))
                .createObjectIfPossible(
                        """
                        Festival: %s in %s (%s to %s), format: %s

                        List up to %d essentials (non-negotiable items), up to %d
                        weather/season-specific items given this location and these
                        dates, and up to %d comfort items experienced attendees bring
                        that first-timers often forget.
                        Create a PackingList from these three categories.
                        """.formatted(
                                festivalInfo.festivalName(),
                                festivalInfo.location(),
                                festivalInfo.startDate(),
                                festivalInfo.endDate(),
                                festivalInfo.festivalType(),
                                properties.maxPackingItemsPerCategory(),
                                properties.maxPackingItemsPerCategory(),
                                properties.maxPackingItemsPerCategory()
                        ),
                        PackingList.class
                );
    }

    /**
     * Estimates the total cost of attending, using
     * {@link BudgetCalculatorTool} to sum the individual line items
     * deterministically rather than trusting the model's arithmetic.
     *
     * <p>Marked as its own {@link AchievesGoal}, reachable from just
     * {@link FestivalBasicInfo}/{@link AttendeePreferences}.</p>
     *
     * @param festivalInfo the output of {@link FestivalPlannerAgent#extractFestivalInfo}
     * @param preferences  the output of {@link FestivalPlannerAgent#extractAttendeePreferences}
     * @param context      Embabel's operation context, providing access to the LLM
     * @return the estimated total cost in USD and its line-item breakdown
     */
    @AchievesGoal(description = "A cost estimate for attending the festival, broken down by line item")
    @Action(description = "Estimate the total cost of attending the festival")
    public BudgetEstimate estimateBudget(FestivalBasicInfo festivalInfo, AttendeePreferences preferences,
                                         OperationContext context) {
        return context.ai()
                .withDefaultLlm()
                .withToolObject(budgetCalculatorTool)
                .createObjectIfPossible(
                        """
                        Festival: %s in %s, format: %s
                        Attendee's budget level: %s, traveling from: %s

                        Estimate realistic cost line items (e.g. ticket, flights/travel,
                        lodging or camping gear, food/drink, miscellaneous) appropriate to
                        the attendee's stated budget level.

                        Use the calculateTotal tool with a comma-separated label:amount
                        string built from your estimated line items, and put the tool's
                        exact total into estimatedTotalUsd. List the same line items,
                        formatted as "Label: $amount", into lineItems.
                        Create a BudgetEstimate from the tool's result and your line items.
                        """.formatted(
                                festivalInfo.festivalName(),
                                festivalInfo.location(),
                                festivalInfo.festivalType(),
                                preferences.budgetLevel(),
                                preferences.travelingFrom()
                        ),
                        BudgetEstimate.class
                );
    }

    /**
     * Generates practical safety and wellbeing guidance tailored to the
     * festival's format and environment.
     *
     * <p>Marked as its own {@link AchievesGoal}, reachable from just
     * {@link FestivalBasicInfo}.</p>
     *
     * @param festivalInfo the output of {@link FestivalPlannerAgent#extractFestivalInfo}
     * @param context      Embabel's operation context, providing access to the LLM
     * @return practical safety tips and emergency guidance
     */
    @AchievesGoal(description = "Practical safety and wellbeing guidance for the festival")
    @Action(description = "Generate safety and wellbeing tips for the festival")
    public SafetyTips generateSafetyTips(FestivalBasicInfo festivalInfo, OperationContext context) {
        return context.ai()
                .withDefaultLlm()
                .withPromptContributors(List.of(Personas.FESTIVAL_VETERAN))
                .createObjectIfPossible(
                        """
                        Festival: %s in %s (%s to %s), format: %s

                        Give practical, actionable safety and wellbeing tips specific to
                        this festival's format and environment (heat/hydration for
                        outdoor/camping events, crowd navigation for dense venues, hearing
                        protection, staying with a group, etc.).

                        Also give brief guidance on what to do in an emergency or if
                        separated from a group at this specific type of event.
                        Create a SafetyTips from these tips and guidance.
                        """.formatted(
                                festivalInfo.festivalName(),
                                festivalInfo.location(),
                                festivalInfo.startDate(),
                                festivalInfo.endDate(),
                                festivalInfo.festivalType()
                        ),
                        SafetyTips.class
                );
    }
}
