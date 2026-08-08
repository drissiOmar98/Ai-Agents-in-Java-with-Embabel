package com.omar.trip_planner.model;

import java.util.List;

/**
 * The traveler's full trip budget, allocated across spending categories.
 *
 * <p>This is a goal output of
 * {@link com.omar.trip_planner.agent.BudgetPlannerAgent#allocateBudget}. The
 * dollar amounts themselves are computed deterministically by
 * {@link com.omar.trip_planner.tool.BudgetAllocatorTool}; {@code notes} is
 * the only part the LLM contributes, adjusting the guidance for
 * destination-specific cost-of-living context without changing the
 * underlying arithmetic.</p>
 *
 * @param categories the budget split by category
 * @param notes      destination-aware guidance on how to use the allocation
 *                   (e.g. leaning toward the higher end of a category in an
 *                   expensive city)
 */
public record BudgetAllocation(List<BudgetCategoryAllocation> categories, String notes) {
}
