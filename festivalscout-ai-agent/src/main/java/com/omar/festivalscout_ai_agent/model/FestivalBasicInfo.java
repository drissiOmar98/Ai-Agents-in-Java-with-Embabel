package com.omar.festivalscout_ai_agent.model;

import java.time.LocalDate;

/**
 * The core identity of the festival being planned for, extracted directly
 * from the user's free-text request.
 *
 * <p>This is the anchor fact for the pipeline: lineup research, packing,
 * budget, and safety guidance all depend on knowing what kind of event and
 * environment the attendee is heading into (e.g. a multi-day camping
 * festival like Tomorrowland vs. a downtown multi-venue event like Ultra
 * Miami calls for very different packing and budget advice).</p>
 *
 * @param festivalName the festival's name, e.g. {@code "Tomorrowland"}, {@code "Ultra Music Festival"}
 * @param location     the festival's city/country
 * @param startDate    the festival's first day
 * @param endDate      the festival's last day
 * @param festivalType a short descriptor of the event format, e.g.
 *                     {@code "multi-day camping EDM festival"} or
 *                     {@code "downtown multi-stage festival, no camping"}
 */
public record FestivalBasicInfo(
        String festivalName,
        String location,
        LocalDate startDate,
        LocalDate endDate,
        String festivalType
) {
}
