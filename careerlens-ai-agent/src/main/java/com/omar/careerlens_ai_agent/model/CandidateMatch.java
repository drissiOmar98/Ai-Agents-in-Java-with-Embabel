package com.omar.careerlens_ai_agent.model;

import java.util.List;

/**
 * The result of comparing a {@link CandidateProfile} against a job's
 * {@link JobRequirements}: what lines up, what's missing, and an overall
 * fit assessment.
 *
 * <p>Feeds directly into
 * {@link com.omar.careerlens_ai_agent.agent.ApplicationAgent#tailorResumeHighlights} and
 * {@link com.omar.careerlens_ai_agent.agent.InterviewPrepAgent#generateInterviewQuestions},
 * so both the resume and the interview prep can specifically address gaps
 * rather than ignoring them.</p>
 *
 * @param matchingSkills candidate skills that directly satisfy a required or
 *                       nice-to-have skill
 * @param missingSkills  required or nice-to-have skills the candidate's profile
 *                       does not demonstrate
 * @param fitScorePercent an overall estimated fit, 0-100
 * @param assessment      a brief, honest explanation of the fit score
 */
public record CandidateMatch(
        List<String> matchingSkills,
        List<String> missingSkills,
        int fitScorePercent,
        String assessment
) {
}
