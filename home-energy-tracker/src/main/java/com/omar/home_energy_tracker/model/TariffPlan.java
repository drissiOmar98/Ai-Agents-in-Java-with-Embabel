package com.omar.home_energy_tracker.model;

/**
 * The household's electricity rate structure, extracted from the user's
 * input.
 *
 * <p>For a flat-rate plan with no peak/off-peak split, {@code peakPricePerKwh}
 * should equal {@code offPeakPricePerKwh} and {@code peakHoursWindow} should
 * be {@code "none"}.</p>
 *
 * @param offPeakPricePerKwh   the price per kWh during off-peak (or flat-rate) hours
 * @param peakPricePerKwh      the price per kWh during peak hours
 * @param peakHoursWindow      a description of when peak pricing applies, e.g.
 *                             {@code "weekdays 2pm-8pm"}, or {@code "none"} for flat-rate plans
 * @param fixedMonthlyChargeUsd a fixed monthly charge independent of usage, if any
 */
public record TariffPlan(double offPeakPricePerKwh, double peakPricePerKwh, String peakHoursWindow,
                          double fixedMonthlyChargeUsd) {
}
