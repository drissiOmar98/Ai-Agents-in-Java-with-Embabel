package com.omar.festivalscout_ai_agent.model;

import java.util.List;

/**
 * A researched summary of the festival's lineup, gathered via web search.
 *
 * <p>Feeds into
 * {@link com.omar.festivalscout_ai_agent.agent.FestivalPlannerAgent#pickMustSeeArtists}
 * so recommendations are grounded in the festival's actual announced
 * artists rather than the model's general knowledge, which may be stale
 * for a specific year's edition.</p>
 *
 * @param headliners    the festival's top-billed, marquee artists
 * @param risingArtists notable but less prominent artists worth discovering
 * @param genres        the genres represented across the lineup
 */
public record LineupHighlights(List<String> headliners, List<String> risingArtists, List<String> genres) {
}
