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

    /**
     * Extracts the traveler's preferences and constraints from their
     * free-text description.
     *
     * @param userInput free-text input describing the multi-city trip and the
     *                  traveler's preferences
     * @param context   Embabel's operation context, providing access to the LLM
     * @return the traveler's interests, pace, budget, and travel style
     */
    @Action
    public TravelerPreferences extractTravelerPreferences(UserInput userInput, OperationContext context) {
        return context.ai()
                .withDefaultLlm()
                .createObjectIfPossible(
                        """
                        The following text describes a multi-city trip, possibly
                        alongside the traveler's preferences: %s

                        Identify their main interests, their preferred pace (RELAXED,
                        MODERATE, or PACKED - infer from context if not explicit), their
                        total budget in USD, and their travel style (e.g. backpacking,
                        comfort, luxury).
                        Create a TravelerPreferences from these details.
                        """.formatted(userInput.getContent()),
                        TravelerPreferences.class
                );
    }

    /**
     * Estimates realistic inter-city transit time for every leg the
     * itinerary requires, using {@link TransitTimeEstimatorTool} so each
     * duration reflects real overhead and average speed by mode, not a
     * bare guess.
     *
     * @param itineraryOutline the output of {@link #extractItineraryOutline}
     * @param context          Embabel's operation context, providing access to the LLM
     * @return every transit leg with its estimated duration, and the total transit time
     */
    @Action(description = "Estimate realistic inter-city transit times")
    public TransitEstimates estimateTransitTimes(ItineraryOutline itineraryOutline, OperationContext context) {
        return context.ai()
                .withDefaultLlm()
                .withToolObject(transitTimeEstimatorTool)
                .createObjectIfPossible(
                        """
                        Trip stops in order: %s

                        For each consecutive pair of stops, estimate the approximate
                        distance in kilometers and the most likely transport mode
                        (FLIGHT, TRAIN, CAR, or BUS) based on your geographic knowledge.
                        Then use the calculateTransitTimes tool with all legs formatted
                        as fromCity|toCity|distanceKm|mode, separated by semicolons.

                        Put the tool's exact per-leg results into legs and its exact
                        TOTAL figure into totalTransitHours.
                        Create a TransitEstimates from the tool's result.
                        """.formatted(
                                itineraryOutline.stops().stream()
                                        .map(DestinationStop::city)
                                        .collect(Collectors.joining(" -> "))
                        ),
                        TransitEstimates.class
                );
    }

    /**
     * Checks whether the planned itinerary is actually feasible, using
     * {@link FeasibilityRuleTool} to compute the transit-to-usable-hours
     * ratio deterministically rather than trusting an LLM's sense of
     * "does this seem like a lot."
     *
     * @param itineraryOutline     the output of {@link #extractItineraryOutline}
     * @param transitEstimates     the output of {@link #estimateTransitTimes}
     * @param travelerPreferences  the output of {@link #extractTravelerPreferences}
     * @param context              Embabel's operation context, providing access to the LLM
     * @return the computed feasibility verdict and ratio
     */
    @Action(description = "Deterministically check whether the itinerary's transit load is feasible")
    public FeasibilityReport checkFeasibility(ItineraryOutline itineraryOutline, TransitEstimates transitEstimates,
                                                TravelerPreferences travelerPreferences, OperationContext context) {
        // The ratio and feasibility verdict are decided here, in plain Java,
        // before the LLM is ever invoked for this step.
        double ratio = feasibilityRuleTool.calculateTransitRatio(
                itineraryOutline.totalTripDays(), transitEstimates.totalTransitHours(), travelerPreferences.pace());
        boolean feasible = feasibilityRuleTool.isFeasible(ratio, travelerPreferences.pace());

        String verdictText = context.ai()
                .withDefaultLlm()
                .createObjectIfPossible(
                        """
                        A trip transit-to-usable-time ratio of %.0f%% was computed for a
                        %d-day, %s-paced trip with %.1f total transit hours. This plan is
                        considered %s for this pace.

                        Write one short sentence explaining this verdict in plain
                        language. Do not suggest a different verdict - it's already
                        decided; just explain it.
                        """.formatted(
                                ratio * 100, itineraryOutline.totalTripDays(), travelerPreferences.pace(),
                                transitEstimates.totalTransitHours(), feasible ? "feasible" : "overpacked"
                        ),
                        String.class
                );

        return new FeasibilityReport(feasible, ratio, verdictText);
    }

    /**
     * Builds the final day-by-day itinerary, restructuring the original
     * stop/day allocation if {@link #checkFeasibility} found it wasn't
     * realistic for the traveler's pace.
     *
     * <p>This is the pipeline's primary goal.</p>
     *
     * @param itineraryOutline    the output of {@link #extractItineraryOutline}
     * @param transitEstimates    the output of {@link #estimateTransitTimes}
     * @param feasibilityReport   the output of {@link #checkFeasibility}
     * @param travelerPreferences the output of {@link #extractTravelerPreferences}
     * @param context             Embabel's operation context, providing access to the LLM
     * @return the final, realistic day-by-day itinerary
     */
    @AchievesGoal(description = "A realistic day-by-day itinerary, restructured if the original plan wasn't feasible")
    @Action
    public DayByDayItinerary generateItinerary(ItineraryOutline itineraryOutline, TransitEstimates transitEstimates,
                                                 FeasibilityReport feasibilityReport, TravelerPreferences travelerPreferences,
                                                 OperationContext context) {
        String stopsSummary = itineraryOutline.stops().stream()
                .map(stop -> "- %s: %d planned day(s)".formatted(stop.city(), stop.plannedDays()))
                .collect(Collectors.joining("\n"));

        String legsSummary = transitEstimates.legs().stream()
                .map(leg -> "- %s to %s: %.1fh (%s)".formatted(leg.fromCity(), leg.toCity(), leg.estimatedHours(), leg.mode()))
                .collect(Collectors.joining("\n"));

        DayByDayItinerary itinerary = context.ai()
                .withDefaultLlm()
                .withPromptContributors(List.of(Personas.SEASONED_TRAVELER))
                .createObjectIfPossible(
                        """
                        Planned stops:
                        %s

                        Estimated transit legs:
                        %s

                        Feasibility verdict: %s (%s)
                        Traveler interests: %s

                        If the plan was found NOT feasible, restructure it - this may mean
                        cutting a city, combining two nearby stops, or extending total trip
                        length - and clearly say so. If it was feasible, build the day-by-day
                        plan as originally allocated. Account for transit legs explicitly:
                        a travel day with a multi-hour transit leg should have a lighter
                        sightseeing plan than a full day in one city. Tailor activities to
                        the stated interests.
                        Create a DayByDayItinerary with one entry per day and note whether
                        it was adjusted for feasibility.
                        """.formatted(
                                stopsSummary,
                                legsSummary.isBlank() ? "(no transit required)" : legsSummary,
                                feasibilityReport.isFeasible() ? "FEASIBLE" : "NOT FEASIBLE",
                                feasibilityReport.verdict(),
                                String.join(", ", travelerPreferences.interests())
                        ),
                        DayByDayItinerary.class
                );

        log.info("Itinerary generated: {} days, adjusted for feasibility: {}",
                itinerary != null ? itinerary.dailyPlans().size() : 0,
                itinerary != null && itinerary.wasAdjustedForFeasibility());
        return itinerary;
    }
}
