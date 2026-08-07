package com.omar.trip_planner.tool;

import com.omar.trip_planner.model.BudgetCategoryAllocation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

/**
 * A plain proportional budget allocator.
 *
 * <p>Deliberately <strong>not</strong> annotated as an {@code @LlmTool}.
 * Splitting a known total budget into category amounts by a fixed
 * percentage table is arithmetic with one correct answer per travel style
 * — there's nothing for an LLM to judge in the multiplication itself. This
 * class is called directly as a normal Spring bean by
 * {@link com.omar.trip_planner.agent.BudgetPlannerAgent#allocateBudget}; the LLM's
 * role is limited to adding destination-cost-of-living context in
 * {@code notes}, without touching the computed amounts.</p>
 */
@Component
public class BudgetAllocatorTool {

    private record CategorySplit(String category, double percent) {
    }

    private static final List<CategorySplit> STANDARD_SPLIT = List.of(
            new CategorySplit("Lodging", 0.40),
            new CategorySplit("Food", 0.25),
            new CategorySplit("Transit", 0.15),
            new CategorySplit("Activities", 0.15),
            new CategorySplit("Miscellaneous", 0.05)
    );

    private static final List<CategorySplit> BACKPACKING_SPLIT = List.of(
            new CategorySplit("Lodging", 0.30),
            new CategorySplit("Food", 0.30),
            new CategorySplit("Transit", 0.20),
            new CategorySplit("Activities", 0.15),
            new CategorySplit("Miscellaneous", 0.05)
    );

    private static final List<CategorySplit> LUXURY_SPLIT = List.of(
            new CategorySplit("Lodging", 0.50),
            new CategorySplit("Food", 0.20),
            new CategorySplit("Transit", 0.10),
            new CategorySplit("Activities", 0.15),
            new CategorySplit("Miscellaneous", 0.05)
    );

    /**
     * Splits a total budget into category amounts using a fixed percentage
     * table selected by travel style.
     *
     * @param totalBudgetUsd the total trip budget in USD
     * @param travelStyle    a style descriptor; {@code "backpacking"} and
     *                       {@code "luxury"} (case-insensitive, substring match) select
     *                       adjusted splits, anything else uses the standard split
     * @return each category's allocated amount and percentage of the total
     */
    public List<BudgetCategoryAllocation> allocate(double totalBudgetUsd, String travelStyle) {
        List<CategorySplit> split = resolveSplit(travelStyle);
        return split.stream()
                .map(entry -> new BudgetCategoryAllocation(
                        entry.category(),
                        Math.round(totalBudgetUsd * entry.percent() * 100.0) / 100.0,
                        entry.percent() * 100.0
                ))
                .toList();
    }

    private List<CategorySplit> resolveSplit(String travelStyle) {
        String style = travelStyle == null ? "" : travelStyle.toLowerCase(Locale.ROOT);
        if (style.contains("backpack")) {
            return BACKPACKING_SPLIT;
        }
        if (style.contains("luxury")) {
            return LUXURY_SPLIT;
        }
        return STANDARD_SPLIT;
    }
}
