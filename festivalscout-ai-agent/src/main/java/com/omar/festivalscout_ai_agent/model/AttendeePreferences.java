package com.omar.festivalscout_ai_agent.model;

import java.util.List;

/**
 * The attendee's personal preferences and constraints, extracted from
 * their free-text request.
 *
 * @param favoriteGenres  music genres the attendee wants to prioritize,
 *                        e.g. {@code "techno"}, {@code "melodic house"}
 * @param favoriteArtists specific artists the attendee explicitly mentioned wanting to see
 * @param budgetLevel     a short descriptor of budget comfort, e.g.
 *                        {@code "budget"}, {@code "mid-range"}, {@code "no limit"}
 * @param travelingFrom   the attendee's departure city/country, used for travel cost estimates
 */
public record AttendeePreferences(
        List<String> favoriteGenres,
        List<String> favoriteArtists,
        String budgetLevel,
        String travelingFrom
) {
}
