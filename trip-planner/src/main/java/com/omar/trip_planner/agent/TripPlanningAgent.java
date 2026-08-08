package com.omar.trip_planner.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.OperationContext;
import com.embabel.agent.domain.io.UserInput;
import com.omar.trip_planner.tool.TransitTimeEstimatorTool;
import com.omar.trip_planner.exception.TripDetailsIncompleteException;
import com.omar.trip_planner.model.*;
import com.omar.trip_planner.persona.Personas;
import com.omar.trip_planner.tool.FeasibilityRuleTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Embabel agent that solves the most common multi-city trip planning
 * mistake: an itinerary that looks reasonable on paper but doesn't
 * actually account for how much real transit time eats out of the plan.
 *
 * <p>This class owns the core extraction-through-itinerary spine.
 * {@link BudgetPlannerAgent} and {@link PackingAdvisorAgent} contribute
 * independent downstream goals reachable from the same outline and
 * preferences.</p>
 *
 * <p>Pipeline shape:</p>
 *
 * <pre>
 * UserInput
 *   ├──&gt; extractItineraryOutline  -&gt; ItineraryOutline
 *   └──&gt; extractTravelerPreferences -&gt; TravelerPreferences
 *            │
 *            ▼
 *   estimateTransitTimes (deterministic formula) -&gt; TransitEstimates
 *            │
 *            ▼
 *   checkFeasibility (deterministic rule) -&gt; FeasibilityReport
 *            │
 *            ▼
 *   generateItinerary  🎯 GOAL  -&gt; DayByDayItinerary
 *
 *   (ItineraryOutline + TravelerPreferences also feed
 *    BudgetPlannerAgent#allocateBudget      🎯 GOAL
 *    PackingAdvisorAgent#generatePackingList 🎯 GOAL)
 * </pre>
 */
@Agent(
        name = "trip-compass",
        description = "Checks a multi-city trip's feasibility and builds a realistic day-by-day itinerary",
        version = "1.0.0",
        beanName = "tripPlanningAgent"
)
public class TripPlanningAgent {


    private static final Logger log = LoggerFactory.getLogger(TripPlanningAgent.class);

    private final TransitTimeEstimatorTool transitTimeEstimatorTool;
    private final FeasibilityRuleTool feasibilityRuleTool;

    /**
     * @param transitTimeEstimatorTool tool exposed to the LLM for computing deterministic
     *                                 transit times in {@link #estimateTransitTimes}
     * @param feasibilityRuleTool      plain rule engine used directly in Java to decide
     *                                 feasibility in {@link #checkFeasibility}
     */
    public TripPlanningAgent(TransitTimeEstimatorTool transitTimeEstimatorTool, FeasibilityRuleTool feasibilityRuleTool) {
        this.transitTimeEstimatorTool = transitTimeEstimatorTool;
        this.feasibilityRuleTool = feasibilityRuleTool;
    }

    /**
     * Extracts the trip's planned stops from the traveler's free-text
     * description.
     *
     * @param userInput free-text input describing the multi-city trip and the
     *                  traveler's preferences
     * @param context   Embabel's operation context, providing access to the LLM
     * @return the planned stops, in visiting order, and the total trip length
     * @throws TripDetailsIncompleteException if no destinations could be identified at all
     */
    @Action
    public ItineraryOutline extractItineraryOutline(UserInput userInput, OperationContext context) {
        ItineraryOutline outline = context.ai()
                .withDefaultLlm()
                .createObjectIfPossible(
                        """
                        The following text describes a multi-city trip. Extract the
                        planned stops, in visiting order:
                        %s

                        For each stop, identify the city and how many days the traveler
                        currently plans to spend there. Also identify the total trip
                        length in days.
                        Create an ItineraryOutline from these details.
                        """.formatted(userInput.getContent()),
                        ItineraryOutline.class
                );

        if (outline == null || outline.stops() == null || outline.stops().isEmpty()) {
            throw new TripDetailsIncompleteException(
                    "Could not identify any planned destinations from the submitted trip description");
        }
        return outline;
    }


}
