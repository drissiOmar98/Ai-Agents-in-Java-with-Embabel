package com.omar.home_energy_tracker.model;

import java.util.List;

/**
 * Recommended timing shifts for flexible appliances, aimed at moving usage
 * out of peak pricing hours where the household's tariff plan has one.
 *
 * <p>This is a goal output of
 * {@link com.omar.home_energy_tracker.agent.SchedulingAgent#generateApplianceSchedule}.</p>
 *
 * @param scheduleRecommendations concrete timing suggestions per flexible appliance,
 *                                capped at {@link com.omar.home_energy_tracker.config.WattWiseProperties#maxScheduleRecommendations()}
 */
public record ApplianceSchedule(List<String> scheduleRecommendations) {
}
