package com.omar.home_energy_tracker.model;

/**
 * A single identified inefficiency in the household's energy usage.
 *
 * @param appliance      the appliance the finding concerns
 * @param issue          what's inefficient about its usage, e.g.
 *                       {@code "runs 24/7 despite being an old, high-draw model"},
 *                       {@code "typically used during peak pricing hours"}
 * @param recommendation a concrete suggestion addressing the issue
 */
public record EfficiencyFinding(String appliance, String issue, String recommendation) {
}
