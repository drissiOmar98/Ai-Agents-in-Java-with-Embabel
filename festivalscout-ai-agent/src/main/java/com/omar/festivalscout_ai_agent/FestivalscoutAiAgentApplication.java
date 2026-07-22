package com.omar.festivalscout_ai_agent;

import com.omar.festivalscout_ai_agent.config.FestivalScoutProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Entry point for the FestivalScout application.
 *
 * <p>FestivalScout is an Embabel-powered AI agent that turns a free-text
 * request (e.g. {@code "I'm going to Tomorrowland, I love techno and
 * melodic house, traveling from Casablanca on a mid-range budget"}) into a
 * complete festival plan: a researched lineup, a personalized must-see
 * schedule with conflicts flagged, a day-by-day itinerary, a packing list,
 * a budget estimate, and safety tips.</p>
 *
 * <p>Configuration is bound from {@code application.yml} via
 * {@link FestivalScoutProperties} and enabled here through
 * {@link EnableConfigurationProperties}.</p>
 */
@SpringBootApplication
@EnableConfigurationProperties(FestivalScoutProperties.class)
public class FestivalscoutAiAgentApplication {

	public static void main(String[] args) {
		SpringApplication.run(FestivalscoutAiAgentApplication.class, args);
	}

}
