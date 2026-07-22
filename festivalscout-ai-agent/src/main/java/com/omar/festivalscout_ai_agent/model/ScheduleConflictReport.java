package com.omar.festivalscout_ai_agent.model;

import java.util.List;

/**
 * A deterministic time-conflict analysis of a {@link MustSeePicks} list —
 * an attendee can't be in two places at once, so any overlapping sets on
 * the same day need to be flagged before building the final itinerary.
 *
 * <p>Produced by
 * {@link com.omar.festivalscout_ai_agent.agent.ScheduleQualityAgent#checkScheduleConflicts}
 * using {@link com.omar.festivalscout_ai_agent.tool.ScheduleConflictCheckerTool} for the
 * underlying time-range comparison, so conflicts are computed rather than
 * eyeballed. Consumed by
 * {@link com.omar.festivalscout_ai_agent.agent.FestivalPlannerAgent#buildItinerary} so
 * flagged overlaps can be resolved (by dropping the lower-priority pick or
 * splitting a set) rather than silently double-booked.</p>
 *
 * @param conflicts   a human-readable description of each detected overlap
 * @param hasConflicts convenience flag, {@code true} if {@code conflicts} is non-empty
 */
public record ScheduleConflictReport(List<String> conflicts, boolean hasConflicts) {
}
