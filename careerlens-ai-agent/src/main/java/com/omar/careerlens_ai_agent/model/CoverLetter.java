package com.omar.careerlens_ai_agent.model;

/**
 * The finished, tailored cover letter for a specific job application.
 *
 * <p>This is the primary goal output of
 * {@link com.omar.careerlens_ai_agent.agent.ApplicationAgent#writeCoverLetter}.</p>
 *
 * @param content the complete cover letter text, ready to send
 */
public record CoverLetter(String content) {
}
