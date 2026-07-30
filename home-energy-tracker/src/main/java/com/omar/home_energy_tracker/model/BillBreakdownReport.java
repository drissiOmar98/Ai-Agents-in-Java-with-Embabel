package com.omar.home_energy_tracker.model;

import java.util.List;

/**
 * A plain-language explanation of the household's estimated monthly bill,
 * suitable for someone who doesn't want to read a raw kWh-by-appliance
 * table.
 *
 * <p>This is a goal output of
 * {@link com.omar.home_energy_tracker.agent.ReportingAgent#generateBillBreakdownReport}.</p>
 *
 * @param summary            a short, plain-language overview of where the money goes
 * @param categoryBreakdown  a few grouped, skimmable lines (e.g. "Heating/cooling: ~40% of your bill")
 */
public record BillBreakdownReport(String summary, List<String> categoryBreakdown) {
}
