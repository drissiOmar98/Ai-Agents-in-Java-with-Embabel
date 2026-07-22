package com.omar.festivalscout_ai_agent.tool;

import com.embabel.agent.api.annotation.LlmTool;
import org.springframework.stereotype.Component;

/**
 * Exposes a deterministic line-item summation as an LLM tool.
 *
 * <p>Registered with
 * {@link com.festivalscout.agent.LogisticsAgent#estimateBudget} so the
 * final total is real arithmetic rather than an LLM's mental math over a
 * list of estimated costs.</p>
 */
@Component
public class BudgetCalculatorTool {

    /**
     * Sums a list of cost line items.
     *
     * @param lineItemsCsv comma-separated {@code label:amount} pairs, e.g.
     *                     {@code "Ticket:400,Flights:650,Camping gear:120"}
     * @return a summary string, e.g. {@code "Total: $1170 | Items: 3"};
     *         returns {@code "Total: $0 | Items: 0"} if the input is blank or unparseable
     */
    @LlmTool(description = "Sum a comma-separated list of label:amount cost line items and return the total.")
    public String calculateTotal(
            @LlmTool.Param(description = "Comma-separated label:amount pairs, e.g. Ticket:400,Flights:650")
            String lineItemsCsv
    ) {
        if (lineItemsCsv == null || lineItemsCsv.isBlank()) {
            return "Total: $0 | Items: 0";
        }

        int total = 0;
        int count = 0;
        for (String rawItem : lineItemsCsv.split(",")) {
            String item = rawItem.trim();
            int separatorIndex = item.lastIndexOf(':');
            if (separatorIndex < 0) {
                continue; // skip malformed entries rather than failing the whole calculation
            }
            try {
                total += Integer.parseInt(item.substring(separatorIndex + 1).trim());
                count++;
            } catch (NumberFormatException e) {
                // skip malformed amounts rather than failing the whole calculation
            }
        }

        return String.format("Total: $%d | Items: %d", total, count);
    }
}
