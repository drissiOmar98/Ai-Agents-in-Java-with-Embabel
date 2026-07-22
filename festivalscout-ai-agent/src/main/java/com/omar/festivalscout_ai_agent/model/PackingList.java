package com.omar.festivalscout_ai_agent.model;

import java.util.List;

/**
 * A festival packing list, split into categories so essentials aren't
 * buried among nice-to-haves.
 *
 * <p>This is a goal output of
 * {@link com.omar.festivalscout_ai_agent.agent.LogisticsAgent#generatePackingList}, tailored
 * to the specific festival's type, location, and season.</p>
 *
 * @param essentials           non-negotiable items (ID, tickets, medication, etc.)
 * @param weatherSpecificItems items driven by the festival's expected climate/season
 * @param comfortItems         quality-of-life items experienced attendees pack
 *                             (earplugs, portable charger, electrolyte packets, etc.)
 */
public record PackingList(List<String> essentials, List<String> weatherSpecificItems, List<String> comfortItems) {
}
