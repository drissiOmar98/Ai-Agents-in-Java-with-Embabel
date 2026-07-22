package com.omar.festivalscout_ai_agent.model;

import java.util.List;

/**
 * The attendee's personalized set of must-see artists, matched against
 * their stated preferences and the researched lineup.
 *
 * <p>Capped at
 * {@link com.omar.festivalscout_ai_agent.config.FestivalScoutProperties#maxMustSeeArtists()}
 * to keep the list to genuinely prioritized picks rather than "see
 * everyone." Checked for scheduling conflicts by
 * {@link com.omar.festivalscout_ai_agent.agent.ScheduleQualityAgent#checkScheduleConflicts}
 * before being turned into a day-by-day itinerary.</p>
 *
 * @param picks the recommended sets, ordered by priority
 */
public record MustSeePicks(List<ArtistSetPick> picks) {
}
