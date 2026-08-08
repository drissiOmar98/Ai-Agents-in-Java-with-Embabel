package com.omar.trip_planner.agent;

import com.omar.trip_planner.config.TripCompassProperties;
import com.omar.trip_planner.model.DestinationStop;
import com.omar.trip_planner.model.ItineraryOutline;
import com.omar.trip_planner.model.PackingList;
import com.omar.trip_planner.model.TravelerPreferences;
import com.omar.trip_planner.persona.Personas;
import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.OperationContext;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Downstream agent contributing a packing-list goal to the
 * {@link TripPlanningAgent} pipeline.
 *
 * <p>Kept separate from {@link TripPlanningAgent} because packing is a
 * distinct concern from itinerary feasibility, and — unlike a single-city
 * or single-event packing list — needs to account for potentially several
 * different climates across the trip's destinations, not just one
 * location's weather.</p>
 */
@Agent(description = "Generates a packing list spanning every destination on a multi-city trip")
public class PackingAdvisorAgent {

    private final TripCompassProperties properties;

    /**
     * @param properties bound {@code trip-compass.*} configuration (max packing items per category)
     */
    public PackingAdvisorAgent(TripCompassProperties properties) {
        this.properties = properties;
    }

    /**
     * Generates a packing list covering every destination's likely
     * climate, using the {@link Personas#SEASONED_TRAVELER} persona so
     * the result reflects genuine multi-city packing experience rather
     * than a generic single-destination list.
     *
     * <p>Marked as its own {@link AchievesGoal}, reachable directly from
     * {@link ItineraryOutline}/{@link TravelerPreferences}, independently
     * of the itinerary or budget goals.</p>
     *
     * @param itineraryOutline    the output of {@link TripPlanningAgent#extractItineraryOutline}
     * @param travelerPreferences the output of {@link TripPlanningAgent#extractTravelerPreferences}
     * @param context             Embabel's operation context, providing access to the LLM
     * @return essentials, climate-specific items, and comfort items, each capped at
     *         {@link TripCompassProperties#maxPackingItemsPerCategory()}
     */
    @AchievesGoal(description = "A packing list covering every destination's climate on the trip")
    @Action(description = "Generate a packing list spanning every destination on the trip")
    public PackingList generatePackingList(ItineraryOutline itineraryOutline, TravelerPreferences travelerPreferences,
                                             OperationContext context) {
        String destinations = itineraryOutline.stops().stream()
                .map(DestinationStop::city)
                .collect(Collectors.joining(", "));

        return context.ai()
                .withDefaultLlm()
                .withPromptContributors(List.of(Personas.SEASONED_TRAVELER))
                .createObjectIfPossible(
                        """
                        Trip destinations, in order: %s
                        Travel style: %s
                        Interests: %s

                        List up to %d essentials (documents, chargers, medication, etc.),
                        up to %d items driven by the range of climates likely across ALL
                        of these destinations (not just one city - note if layering makes
                        sense for climate variation across the trip), and up to %d
                        comfort items experienced multi-city travelers bring.
                        Create a PackingList from these three categories.
                        """.formatted(
                                destinations,
                                travelerPreferences.travelStyle(),
                                String.join(", ", travelerPreferences.interests()),
                                properties.maxPackingItemsPerCategory(),
                                properties.maxPackingItemsPerCategory(),
                                properties.maxPackingItemsPerCategory()
                        ),
                        PackingList.class
                );
    }
}
