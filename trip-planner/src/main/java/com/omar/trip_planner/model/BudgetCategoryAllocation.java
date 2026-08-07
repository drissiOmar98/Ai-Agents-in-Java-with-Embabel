package com.omar.trip_planner.model;

/**
 * A single spending category's share of the total trip budget.
 *
 * @param category      the spending category, e.g. {@code "Lodging"}, {@code "Food"},
 *                      {@code "Transit"}, {@code "Activities"}, {@code "Miscellaneous"}
 * @param amountUsd     the allocated amount in USD, computed deterministically from the total budget
 * @param percentOfTotal the percentage of the total budget this category represents
 */
public record BudgetCategoryAllocation(String category, double amountUsd, double percentOfTotal) {
}
