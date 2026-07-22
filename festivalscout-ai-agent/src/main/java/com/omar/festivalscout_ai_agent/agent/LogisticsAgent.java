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


}
