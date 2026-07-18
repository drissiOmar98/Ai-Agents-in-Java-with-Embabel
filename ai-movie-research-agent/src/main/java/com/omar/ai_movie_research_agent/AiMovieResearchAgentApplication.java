package com.omar.ai_movie_research_agent;

import com.omar.ai_movie_research_agent.config.CineScoutProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;


/**
 * Entry point for the CineScout application.
 *
 * <p>CineScout is an Embabel-powered AI agent that turns a free-text user
 * request (e.g. {@code "Tell me about Inception"}) into a fully assembled
 * {@link com.omar.ai_movie_research_agent.model.Movie} profile — basic info, director, cast,
 * genres, and a spoiler-free plot summary — and can go on to suggest
 * similar movies.</p>
 *
 * <p>Configuration is bound from {@code application.yml} via
 * {@link CineScoutProperties} and enabled here through
 * {@link EnableConfigurationProperties}.</p>
 */
@SpringBootApplication
@EnableConfigurationProperties(CineScoutProperties.class)
public class AiMovieResearchAgentApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiMovieResearchAgentApplication.class, args);
	}

}
