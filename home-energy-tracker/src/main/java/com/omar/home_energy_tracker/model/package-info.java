/**
 * Immutable data carriers that flow through the WattWise pipeline, from raw
 * household and tariff descriptions to a ranked savings plan and its
 * companion reports.
 *
 * <p>{@link com.omar.home_energy_tracker.model.HouseholdProfile} and
 * {@link com.omar.home_energy_tracker.model.TariffPlan} are extracted independently from
 * the user's input and combined into a
 * {@link com.omar.home_energy_tracker.model.CostBreakdownReport} (computed
 * deterministically). That report drives an
 * {@link com.omar.home_energy_tracker.model.EfficiencyReport}, which in turn drives the
 * primary {@link com.omar.home_energy_tracker.model.SavingsPlan} goal.
 * {@link com.omar.home_energy_tracker.model.BillBreakdownReport} and
 * {@link com.omar.home_energy_tracker.model.ApplianceSchedule} are independent downstream
 * goals derived from the same underlying facts.</p>
 */
package com.omar.home_energy_tracker.model;
