package com.omar.trip_planner.agent;

import com.omar.trip_planner.model.BudgetAllocation;
import com.omar.trip_planner.model.BudgetCategoryAllocation;
import com.omar.trip_planner.model.DestinationStop;
import com.omar.trip_planner.model.ItineraryOutline;
import com.omar.trip_planner.model.TravelerPreferences;
import com.omar.trip_planner.persona.Personas;
import com.omar.trip_planner.tool.BudgetAllocatorTool;
import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.OperationContext;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Downstream agent contributing a budget-allocation goal to the
 * {@link TripPlanningAgent} pipeline.
 *
 * <p>Kept separate from {@link TripPlanningAgent} because budgeting is a
 * distinct concern from itinerary feasibility. Splitting a known total
 * budget into categories by percentage is decided by
 * {@link BudgetAllocatorTool} directly in Java — the arithmetic has one
 * correct answer per travel style. The LLM's only role here is adding
 * destination-cost-of-living context, without touching the computed
 * amounts.</p>
 */
@Agent(description = "Allocates a trip budget across spending categories with destination-aware guidance")
public class BudgetPlannerAgent {

    private final BudgetAllocatorTool budgetAllocatorTool;

    /**
     * @param budgetAllocatorTool the plain allocator used directly in Java to compute
     *                            category amounts; not exposed to the LLM as a tool
     */
    public BudgetPlannerAgent(BudgetAllocatorTool budgetAllocatorTool) {
        this.budgetAllocatorTool = budgetAllocatorTool;
    }

    /**
     * Allocates the traveler's total budget across spending categories,
     * then asks the LLM only to add destination-aware guidance on top of
     * the already-computed amounts.
     *
     * <p>Marked as its own {@link AchievesGoal}, reachable directly from
     * {@link ItineraryOutline}/{@link TravelerPreferences}, independently
     * of the itinerary or packing goals.</p>
     *
     * @param itineraryOutline    the output of {@link TripPlanningAgent#extractItineraryOutline}
     * @param travelerPreferences the output of {@link TripPlanningAgent#extractTravelerPreferences}
     * @param context             Embabel's operation context, providing access to the LLM
     * @return the computed category allocations plus destination-aware guidance notes
     */
    @AchievesGoal(description = "A trip budget allocated across categories with destination-aware guidance")
    @Action(description = "Allocate the trip budget across spending categories")
    public BudgetAllocation allocateBudget(ItineraryOutline itineraryOutline, TravelerPreferences travelerPreferences,
                                             OperationContext context) {
        // Category amounts are decided here, in plain Java, before the LLM
        // is ever invoked for this step - there's one correct split per
        // the traveler's stated travel style.
        List<BudgetCategoryAllocation> allocations = budgetAllocatorTool.allocate(
                travelerPreferences.totalBudgetUsd(), travelerPreferences.travelStyle());

        String allocationSummary = allocations.stream()
                .map(entry -> "- %s: $%.2f (%.0f%%)".formatted(entry.category(), entry.amountUsd(), entry.percentOfTotal()))
                .collect(Collectors.joining("\n"));

        String destinations = itineraryOutline.stops().stream()
                .map(DestinationStop::city)
                .collect(Collectors.joining(", "));

        String notes = context.ai()
                .withDefaultLlm()
                .withPromptContributors(List.of(Personas.BUDGET_ADVISOR))
                .createObjectIfPossible(
                        """
                        Trip destinations: %s
                        Travel style: %s

                        This budget has already been split by category (do not propose
                        different amounts):
                        %s

                        Write brief, destination-aware guidance on how to actually use
                        this allocation - flag any destinations known to be notably more
                        or less expensive than average, and suggest where the traveler
                        might lean toward the higher or lower end within a category
                        given the specific cities on this trip.
                        """.formatted(destinations, travelerPreferences.travelStyle(), allocationSummary),
                        String.class
                );

        return new BudgetAllocation(allocations, notes);
    }
}
