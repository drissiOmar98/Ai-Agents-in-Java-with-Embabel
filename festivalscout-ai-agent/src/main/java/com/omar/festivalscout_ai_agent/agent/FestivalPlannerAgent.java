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

    /**
     * Researches the festival's lineup via web search, grounding the result
     * in current, real information rather than the model's general
     * knowledge (which may be stale for a specific year's edition).
     *
     * @param festivalInfo the output of {@link #extractFestivalInfo}
     * @param context      Embabel's operation context, providing access to the LLM
     * @return the festival's headliners, rising artists, and represented genres
     * @throws LineupUnavailableException if no headliners could be identified at all
     */
    @Action(description = "Research the festival's lineup using web search")
    public LineupHighlights researchLineup(FestivalBasicInfo festivalInfo, OperationContext context) {
        LineupHighlights lineup = context.ai()
                .withDefaultLlm()
                .withToolGroup(CoreToolGroups.WEB)
                .createObjectIfPossible(
                        """
                        Research the lineup for %s (%s, %s to %s) using web search.
                        Limit yourself to no more than 3 searches to avoid rate limiting.

                        Identify the headlining artists, notable rising/smaller artists
                        worth discovering, and the genres represented across the lineup.
                        Create a LineupHighlights from these findings.
                        """.formatted(
                                festivalInfo.festivalName(),
                                festivalInfo.location(),
                                festivalInfo.startDate(),
                                festivalInfo.endDate()
                        ),
                        LineupHighlights.class
                );

        if (lineup == null || lineup.headliners() == null || lineup.headliners().isEmpty()) {
            throw new LineupUnavailableException(
                    "Could not resolve lineup information for: " + festivalInfo.festivalName());
        }
        return lineup;
    }

    /**
     * Matches the attendee's preferences against the researched lineup to
     * produce a personalized, prioritized set of must-see artists with
     * concrete scheduling details.
     *
     * <p>Uses the {@link Personas#MUSIC_CURATOR} persona so picks read like
     * genuine recommendations rather than a generic "here are the
     * headliners" list.</p>
     *
     * @param lineup      the output of {@link #researchLineup}
     * @param preferences the output of {@link #extractAttendeePreferences}
     * @param context     Embabel's operation context, providing access to the LLM
     * @return prioritized artist picks with day/stage/time details, capped at
     *         {@link FestivalScoutProperties#maxMustSeeArtists()}
     */
    @Action
    public MustSeePicks pickMustSeeArtists(LineupHighlights lineup, AttendeePreferences preferences,
                                           OperationContext context) {
        return context.ai()
                .withDefaultLlm()
                .withPromptContributors(List.of(Personas.MUSIC_CURATOR))
                .createObjectIfPossible(
                        """
                        Lineup headliners: %s
                        Rising artists: %s
                        Genres on the lineup: %s

                        Attendee favorite genres: %s
                        Attendee favorite artists: %s

                        Pick up to %d must-see sets that best match this attendee's taste,
                        mixing headliners they'd genuinely enjoy with rising artists suited
                        to their genre preferences. For each pick, assign a plausible day,
                        stage, and 24-hour start/end time (estimate reasonably if the exact
                        schedule isn't known), and briefly explain why it fits this
                        attendee.
                        Create a MustSeePicks from these recommendations.
                        """.formatted(
                                String.join(", ", lineup.headliners()),
                                String.join(", ", lineup.risingArtists()),
                                String.join(", ", lineup.genres()),
                                String.join(", ", preferences.favoriteGenres()),
                                String.join(", ", preferences.favoriteArtists()),
                                properties.maxMustSeeArtists()
                        ),
                        MustSeePicks.class
                );
    }


}
