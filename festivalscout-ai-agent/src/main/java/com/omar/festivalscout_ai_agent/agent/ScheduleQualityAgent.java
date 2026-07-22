package com.omar.festivalscout_ai_agent.agent;


import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.OperationContext;
import com.omar.festivalscout_ai_agent.model.ArtistSetPick;
import com.omar.festivalscout_ai_agent.model.MustSeePicks;
import com.omar.festivalscout_ai_agent.model.ScheduleConflictReport;
import com.omar.festivalscout_ai_agent.tool.ScheduleConflictCheckerTool;

/**
 * Quality-gate agent contributing an independent schedule-conflict check to
 * the {@link FestivalPlannerAgent} pipeline.
 *
 * <p>Kept separate from {@link FestivalPlannerAgent} because this step is
 * verification, not content generation &mdash; it reads the attendee's
 * must-see picks and reports any time overlaps, rather than producing the
 * next stage of the plan itself. Its output feeds into
 * {@link FestivalPlannerAgent#buildItinerary} so conflicts get resolved
 * before the final plan is written, not discovered on-site.</p>
 */
@Agent(description = "Checks an attendee's must-see artist picks for same-day time conflicts")
public class ScheduleQualityAgent {

    private final ScheduleConflictCheckerTool scheduleConflictCheckerTool;

    /**
     * @param scheduleConflictCheckerTool tool exposed to the LLM for computing deterministic
     *                                    time-overlap checks in {@link #checkScheduleConflicts}
     */
    public ScheduleQualityAgent(ScheduleConflictCheckerTool scheduleConflictCheckerTool) {
        this.scheduleConflictCheckerTool = scheduleConflictCheckerTool;
    }

    /**
     * Checks the attendee's must-see picks for same-day, overlapping set
     * times, using a deterministic time-range comparison rather than an
     * LLM estimate.
     *
     * @param mustSeePicks the output of {@link FestivalPlannerAgent#pickMustSeeArtists}
     * @param context      Embabel's operation context, providing access to the LLM
     * @return a human-readable list of detected conflicts, plus a convenience flag
     */
    @Action(description = "Check must-see picks for same-day scheduling conflicts")
    public ScheduleConflictReport checkScheduleConflicts(MustSeePicks mustSeePicks, OperationContext context) {
        return context.ai()
                .withDefaultLlm()
                .withToolObject(scheduleConflictCheckerTool)
                .createObjectIfPossible(
                        """
                        Use the checkConflicts tool with this scheduleEntries value:
                        %s

                        Put the tool's exact conflict descriptions into conflicts (empty
                        list if none) and set hasConflicts to true only if conflicts were
                        found.
                        Create a ScheduleConflictReport from the tool's result.
                        """.formatted(formatEntries(mustSeePicks)),
                        ScheduleConflictReport.class
                );
    }

    private String formatEntries(MustSeePicks mustSeePicks) {
        StringBuilder sb = new StringBuilder();
        for (ArtistSetPick pick : mustSeePicks.picks()) {
            if (sb.length() > 0) {
                sb.append(";");
            }
            sb.append(pick.day()).append("|")
                    .append(pick.startTime()).append("|")
                    .append(pick.endTime()).append("|")
                    .append(pick.artist());
        }
        return sb.toString();
    }
}
