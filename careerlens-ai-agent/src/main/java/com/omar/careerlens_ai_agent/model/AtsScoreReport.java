package com.omar.careerlens_ai_agent.model;

import java.util.List;

/**
 * A deterministic ATS (Applicant Tracking System) keyword match report,
 * comparing tailored resume content against a job's required keywords.
 *
 * <p>Produced by
 * {@link com.omar.careerlens_ai_agent.agent.ApplicationQualityAgent#scoreAtsMatch} using
 * {@link com.omar.careerlens_ai_agent.tool.AtsKeywordScoreTool} for the underlying
 * calculation, so the score reflects an actual keyword-overlap count rather
 * than an LLM estimate. Consumed by
 * {@link com.careerforge.agent.ApplicationAgent#writeCoverLetter} so the
 * cover letter can naturally work in keywords the resume is currently
 * missing.</p>
 *
 * @param matchPercent    the percentage of required keywords found in the resume content, 0-100
 * @param missingKeywords required keywords not found in the resume content
 */
public record AtsScoreReport(int matchPercent, List<String> missingKeywords) {
}
