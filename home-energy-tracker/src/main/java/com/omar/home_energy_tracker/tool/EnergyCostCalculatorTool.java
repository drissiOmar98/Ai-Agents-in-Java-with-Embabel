package com.omar.home_energy_tracker.tool;

import com.embabel.agent.api.annotation.LlmTool;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Exposes a deterministic monthly energy cost calculation as an LLM tool.
 *
 * <p>Registered with
 * {@link com.omar.home_energy_tracker.agent.EnergyAnalysisAgent#calculateCostBreakdown} so
 * every appliance's kWh and cost figures are computed by real arithmetic
 * rather than an LLM estimating (and likely mis-adding) a dozen line items
 * at once &mdash; exactly the kind of repeated, exact arithmetic LLMs are
 * unreliable at, and exactly the kind of number a homeowner needs to
 * actually trust.</p>
 */
@Component
public class EnergyCostCalculatorTool {

    private static final double DAYS_PER_MONTH = 30.0;
    private static final String ENTRY_SEPARATOR = ";";
    private static final String FIELD_SEPARATOR = "\\|";

    /**
     * Calculates monthly kWh consumption and cost for each appliance entry.
     *
     * @param applianceEntries semicolon-separated entries, each formatted as
     *                         {@code name|wattage|hoursPerDay|pricePerKwh}, e.g.
     *                         {@code "Refrigerator|150|24|0.15;Washing machine|500|1|0.15"}
     * @return one line per appliance formatted as
     *         {@code "name :: X.XX kWh/month, $Y.YY/month"}, followed by a final
     *         {@code "TOTAL :: $Z.ZZ/month"} line; returns {@code "TOTAL :: $0.00/month"}
     *         if no valid entries are found
     */
    @LlmTool(description = "Calculate monthly kWh consumption and cost for a list of appliances " +
            "(name|wattage|hoursPerDay|pricePerKwh entries, separated by semicolons).")
    public String calculateMonthlyCosts(
            @LlmTool.Param(description = "Semicolon-separated entries: name|wattage|hoursPerDay|pricePerKwh")
            String applianceEntries
    ) {
        if (applianceEntries == null || applianceEntries.isBlank()) {
            return "TOTAL :: $0.00/month";
        }

        List<String> lines = new ArrayList<>();
        double total = 0.0;

        for (String rawEntry : applianceEntries.split(ENTRY_SEPARATOR)) {
            String entry = rawEntry.trim();
            if (entry.isEmpty()) {
                continue;
            }
            String[] fields = entry.split(FIELD_SEPARATOR);
            if (fields.length != 4) {
                continue; // skip malformed entries rather than failing the whole calculation
            }

            try {
                String name = fields[0].trim();
                double wattage = Double.parseDouble(fields[1].trim());
                double hoursPerDay = Double.parseDouble(fields[2].trim());
                double pricePerKwh = Double.parseDouble(fields[3].trim());

                double kwhPerMonth = (wattage / 1000.0) * hoursPerDay * DAYS_PER_MONTH;
                double costPerMonth = kwhPerMonth * pricePerKwh;

                lines.add(String.format("%s :: %.2f kWh/month, $%.2f/month", name, kwhPerMonth, costPerMonth));
                total += costPerMonth;
            } catch (NumberFormatException e) {
                // skip malformed numeric values rather than failing the whole calculation
            }
        }

        lines.add(String.format("TOTAL :: $%.2f/month", total));
        return String.join("\n", lines);
    }
}
