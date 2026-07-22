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


}
