package com.omar.skyclaim;

import com.omar.skyclaim.config.SkyClaimProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Entry point for the SkyClaim application.
 *
 * <p>SkyClaim is an Embabel-powered AI agent that helps travelers
 * understand what they may be owed after a flight delay or cancellation,
 * and takes the tedium out of doing something about it.</p>
 *
 * <p>Given a free-text description of a flight disruption, SkyClaim:</p>
 * <ol>
 *   <li>extracts the disruption details and the traveler's context,</li>
 *   <li>calculates the exact compensation amount EU Regulation 261/2004
 *       provides for, by rule rather than by guess,</li>
 *   <li>assesses eligibility (jurisdiction, and whether the airline's
 *       stated reason plausibly counts as an "extraordinary
 *       circumstance"),</li>
 *   <li>drafts a compensation claim letter,</li>
 *   <li>summarizes immediate care entitlements (meals, hotel, communication)
 *       that apply regardless of the compensation outcome, and</li>
 *   <li>gives practical rebooking and next-step advice.</li>
 * </ol>
 *
 * <p><strong>SkyClaim provides informational guidance, not legal advice.</strong>
 * Every eligibility assessment it produces should be treated as a starting
 * point, not a final determination — actual outcomes depend on facts
 * SkyClaim cannot verify, and travelers should confirm with the airline or
 * their national enforcement body before relying on it.</p>
 *
 * <p>Configuration is bound from {@code application.yml} via
 * {@link SkyClaimProperties} and enabled here through
 * {@link EnableConfigurationProperties}.</p>
 */
@SpringBootApplication
@EnableConfigurationProperties(SkyClaimProperties.class)
public class SkyClaimApplication {

	public static void main(String[] args) {
		SpringApplication.run(SkyClaimApplication.class, args);
	}

}
