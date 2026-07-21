package com.omar.festivalscout_ai_agent.model;

import java.util.List;

/**
 * A cost estimate for attending the festival, broken down by line item.
 *
 * <p>Produced by
 * {@link com.omar.festivalscout_ai_agent.agent.LogisticsAgent#estimateBudget} using
 * {@link com.omar.festivalscout_ai_agent.tool.BudgetCalculatorTool} to sum the line items
 * deterministically, so the total is arithmetic rather than an LLM's
 * mental math.</p>
 *
 * @param estimatedTotalUsd the estimated total cost in USD
 * @param lineItems         individual cost line items, e.g.
 *                          {@code "Ticket: $400"}, {@code "Flights: $650"}
 */
public record BudgetEstimate(int estimatedTotalUsd, List<String> lineItems) {
}
