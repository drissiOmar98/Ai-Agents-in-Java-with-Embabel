package com.omar.festivalscout_ai_agent.model;

/**
 * A single recommended artist set to attend, with enough scheduling detail
 * to be checked for time conflicts against the rest of the attendee's
 * picks.
 *
 * @param artist    the artist's name
 * @param day       the festival day, e.g. {@code "Day 1"}, {@code "Friday"}
 * @param stage     the stage/venue name the set takes place on
 * @param startTime the set's start time in 24-hour {@code HH:mm} format
 * @param endTime   the set's end time in 24-hour {@code HH:mm} format
 * @param reason    why this set was picked for this attendee specifically
 */
public record ArtistSetPick(String artist, String day, String stage, String startTime, String endTime, String reason) {
}
