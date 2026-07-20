package com.omar.careerlens_ai_agent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Type-safe configuration properties for CareerForge, bound from the
 * {@code career-forge} prefix in {@code application.yml}.
 *
 * <pre>{@code
 * career-forge:
 *   max-resume-bullets: 5
 *   max-interview-questions: 6
 *   target-ats-match-percent: 70
 * }</pre>
 *
 * @param maxResumeBullets        the maximum number of tailored resume bullet points
 *                                to generate; defaults to {@code 5} when zero or negative
 * @param maxInterviewQuestions   the maximum number of interview questions to generate
 *                                per category (technical/behavioral); defaults to
 *                                {@code 6} when zero or negative
 * @param targetAtsMatchPercent   the ATS keyword match percentage the cover letter step
 *                                treats as a "good" score when deciding how much emphasis
 *                                to place on working in missing keywords; defaults to
 *                                {@code 70} when zero, negative, or over 100
 */
@ConfigurationProperties(prefix = "career-forge")
public record CareerForgeProperties(int maxResumeBullets, int maxInterviewQuestions, int targetAtsMatchPercent) {

    /**
     * Compact constructor applying sensible defaults when values are
     * missing or invalid, so the application never fails to start due to
     * incomplete configuration.
     */
    public CareerForgeProperties {
        if (maxResumeBullets <= 0) {
            maxResumeBullets = 5;
        }
        if (maxInterviewQuestions <= 0) {
            maxInterviewQuestions = 6;
        }
        if (targetAtsMatchPercent <= 0 || targetAtsMatchPercent > 100) {
            targetAtsMatchPercent = 70;
        }
    }
}
