package com.omar.skyclaim.model;

/**
 * A draft compensation claim letter addressed to the airline.
 *
 * <p>This is the primary goal output of
 * {@link com.omar.skyclaim.agent.DisruptionAnalysisAgent#draftClaimLetter}. It's
 * a draft meant for the traveler to review, personalize, and send — not a
 * final legal document.</p>
 *
 * @param subject a concise subject line referencing the flight and claim
 * @param body    the complete letter body, ready for the traveler to review and send
 */
public record ClaimLetter(String subject, String body) {
}
