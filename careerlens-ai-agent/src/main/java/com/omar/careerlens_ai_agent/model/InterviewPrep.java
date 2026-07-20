package com.omar.careerlens_ai_agent.model;

import java.util.List;

/**
 * Interview preparation material for a specific job application, generated
 * from the job's requirements and the candidate's match/gap analysis.
 *
 * <p>This is the secondary goal output of
 * {@link com.omar.careerlens_ai_agent.agent.InterviewPrepAgent#generateInterviewQuestions}.</p>
 *
 * @param technicalQuestions  likely technical questions, informed by the job's
 *                            required skills and the candidate's skill gaps
 * @param behavioralQuestions likely behavioral/situational questions appropriate
 *                            to the role's seniority level
 * @param prepTips            brief, actionable advice for addressing the
 *                            candidate's weakest points before the interview
 */
public record InterviewPrep(
        List<String> technicalQuestions,
        List<String> behavioralQuestions,
        String prepTips
) {
}
