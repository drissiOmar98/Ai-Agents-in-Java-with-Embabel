package com.omar.home_energy_tracker;

import com.omar.home_energy_tracker.config.WattWiseProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Entry point for the WattWise application.
 *
 * <p>WattWise is an Embabel-powered AI agent that turns a household's
 * appliance list and electricity tariff into a real, arithmetic-grounded
 * energy cost breakdown, a ranked savings plan with computed payback
 * periods, a plain-language bill explanation, and an off-peak usage
 * schedule.</p>
 *
 * <p>Given a free-text description of a household's appliances and its
 * electricity rate plan, WattWise:</p>
 * <ol>
 *   <li>extracts the household's appliance profile and tariff structure,</li>
 *   <li>calculates a per-appliance monthly cost breakdown deterministically
 *       (real arithmetic, not an LLM estimate),</li>
 *   <li>identifies inefficient usage patterns (phantom loads, peak-hour
 *       usage, oversized appliances),</li>
 *   <li>proposes a ranked savings plan with payback periods computed in
 *       plain Java,</li>
 *   <li>explains the bill in plain language, and</li>
 *   <li>schedules flexible appliances into off-peak hours.</li>
 * </ol>
 *
 * <p>Configuration is bound from {@code application.yml} via
 * {@link WattWiseProperties} and enabled here through
 * {@link EnableConfigurationProperties}.</p>
 */
@SpringBootApplication
@EnableConfigurationProperties(WattWiseProperties.class)
public class HomeEnergyTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(HomeEnergyTrackerApplication.class, args);
	}

}
