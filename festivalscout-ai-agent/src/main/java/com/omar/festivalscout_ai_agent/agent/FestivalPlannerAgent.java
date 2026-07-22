package com.omar.festivalscout_ai_agent.agent;


import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.OperationContext;
import com.embabel.agent.core.CoreToolGroups;
import com.embabel.agent.domain.io.UserInput;
import com.omar.festivalscout_ai_agent.config.FestivalScoutProperties;
import com.omar.festivalscout_ai_agent.exception.LineupUnavailableException;
import com.omar.festivalscout_ai_agent.model.*;
import com.omar.festivalscout_ai_agent.persona.Personas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Embabel agent that turns a free-text festival request into a
 * personalized, conflict-free festival itinerary.
 *
 * <p>This class owns the core research-through-itinerary spine.
 * {@link ScheduleQualityAgent} contributes an independent time-conflict
 * check that feeds back into {@link #buildItinerary}, and
 * {@link LogisticsAgent} contributes downstream packing, budget, and safety
 * goals reachable from the same {@link FestivalBasicInfo}/
 * {@link AttendeePreferences} facts.</p>
 *
 * <p>Pipeline shape:</p>
 *
 * <pre>
 * UserInput
 *   ├──&gt; extractFestivalInfo        -&gt; FestivalBasicInfo
 *   └──&gt; extractAttendeePreferences -&gt; AttendeePreferences
 *            │
 *            ▼
 *   researchLineup (web search) -&gt; LineupHighlights
 *            │
 *            ▼
 *   pickMustSeeArtists -&gt; MustSeePicks
 *            │
 *            ▼
 *   [ScheduleQualityAgent#checkScheduleConflicts -&gt; ScheduleConflictReport]
 *            │
 *            ▼
 *   buildItinerary  🎯 GOAL  -&gt; FestivalItinerary
 *
 *   (FestivalBasicInfo + AttendeePreferences also feed
 *    LogisticsAgent#generatePackingList  🎯 GOAL
 *    LogisticsAgent#estimateBudget       🎯 GOAL
 *    LogisticsAgent#generateSafetyTips   🎯 GOAL)
 * </pre>
 */
@Agent(
        name = "festival-planner",
        description = "Researches a music festival's lineup and builds a personalized itinerary",
        version = "1.0.0",
        beanName = "festivalPlannerAgent"
)
public class FestivalPlannerAgent {

    private static final Logger log = LoggerFactory.getLogger(FestivalPlannerAgent.class);

    private final FestivalScoutProperties properties;

    /**
     * @param properties bound {@code festival-scout.*} configuration
     */
    public FestivalPlannerAgent(FestivalScoutProperties properties) {
        this.properties = properties;
    }

    /**
     * Extracts the festival's core identity (name, location, dates, format)
     * from the user's free-text request.
     *
     * @param userInput the user's free-text request, e.g.
     *                  {@code "I'm going to Tomorrowland this year"}
     * @param context   Embabel's operation context, providing access to the LLM
     * @return the festival's name, location, dates, and format descriptor
     */
    @Action
    public FestivalBasicInfo extractFestivalInfo(UserInput userInput, OperationContext context) {
        return context.ai()
                .withDefaultLlm()
                .withToolGroup(CoreToolGroups.WEB)
                .createObjectIfPossible(
                        """
                        Identify the music festival mentioned in this request, using web
                        search if needed to confirm its location, dates, and format:
                        %s

                        Create a FestivalBasicInfo with the festival's name, location,
                        start/end dates, and a short descriptor of its format (e.g.
                        "multi-day camping EDM festival" or "downtown multi-stage
                        festival, no camping").
                        """.formatted(userInput.getContent()),
                        FestivalBasicInfo.class
                );
    }

    /**
     * Extracts the attendee's personal preferences and constraints from
     * their free-text request.
     *
     * @param userInput the user's free-text request
     * @param context   Embabel's operation context, providing access to the LLM
     * @return the attendee's favorite genres/artists, budget level, and departure location
     */
    @Action
    public AttendeePreferences extractAttendeePreferences(UserInput userInput, OperationContext context) {
        return context.ai()
                .withDefaultLlm()
                .createObjectIfPossible(
                        """
                        Extract the attendee's preferences from this request: %s

                        Identify their favorite music genres, any specific artists they
                        mentioned wanting to see, their budget comfort level (e.g. budget,
                        mid-range, no limit), and where they're traveling from.
                        Create an AttendeePreferences from these details.
                        """.formatted(userInput.getContent()),
                        AttendeePreferences.class
                );
    }


}
