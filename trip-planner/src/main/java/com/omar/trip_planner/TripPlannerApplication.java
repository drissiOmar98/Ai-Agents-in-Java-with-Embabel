package com.omar.trip_planner;

import com.omar.trip_planner.config.TripCompassProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Entry point for the TripCompass application.
 *
 * <p>TripCompass is an Embabel-powered AI agent that solves the single most
 * common multi-city trip planning mistake: an itinerary that looks
 * reasonable on paper ("2 days Paris, 1 day Brussels, 2 days Amsterdam")
 * but doesn't actually account for how much of each transition day gets
 * consumed by real inter-city transit.</p>
 *
 * <p>Given a free-text description of a multi-city trip, TripCompass:</p>
 * <ol>
 *   <li>extracts the planned stops and the traveler's preferences,</li>
 *   <li>estimates realistic inter-city transit time by mode and distance,
 *       using real overhead figures rather than a bare distance guess,</li>
 *   <li>deterministically checks whether the plan is actually feasible
 *       given the traveler's pace,</li>
 *   <li>builds a day-by-day itinerary — restructured if the original plan
 *       wasn't feasible,</li>
 *   <li>allocates the travel budget across categories, and</li>
 *   <li>generates a packing list spanning every destination's climate.</li>
 * </ol>
 *
 * <p>Configuration is bound from {@code application.yml} via
 * {@link TripCompassProperties} and enabled here through
 * {@link EnableConfigurationProperties}.</p>
 */
@SpringBootApplication
@EnableConfigurationProperties(TripCompassProperties.class)
public class TripPlannerApplication {

	public static void main(String[] args) {
		SpringApplication.run(TripPlannerApplication.class, args);
	}

}
