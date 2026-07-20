package com.omar.careerlens_ai_agent;

import com.omar.careerlens_ai_agent.config.CareerForgeProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Entry point for the CareerForge application.
 *
 * <p>CareerForge is an Embabel-powered AI agent that takes a job posting and
 * a candidate's background as free-text input and produces a complete,
 * tailored application package: matched/missing skills, resume highlights,
 * an ATS-scored cover letter, and interview preparation questions.</p>
 *
 * <p>Configuration is bound from {@code application.yml} via
 * {@link CareerForgeProperties} and enabled here through
 * {@link EnableConfigurationProperties}.</p>
 */
@SpringBootApplication
@EnableConfigurationProperties(CareerForgeProperties.class)
public class CareerlensAiAgentApplication {

	public static void main(String[] args) {
		SpringApplication.run(CareerlensAiAgentApplication.class, args);
	}

}
