package com.omar.careerlens_ai_agent.model;

import java.util.List;

/**
 * Resume bullet points tailored to emphasize the candidate's skills that
 * match the target job, generated from a {@link CandidateMatch}.
 *
 * <p>Capped at
 * {@link com.omar.careerlens_ai_agent.config.CareerForgeProperties#maxResumeBullets()}
 * so the output stays focused on the strongest points rather than restating
 * the candidate's entire background.</p>
 *
 * @param bullets tailored, achievement-oriented resume bullet points,
 *                strongest match first
 */
public record ResumeHighlights(List<String> bullets) {
}
