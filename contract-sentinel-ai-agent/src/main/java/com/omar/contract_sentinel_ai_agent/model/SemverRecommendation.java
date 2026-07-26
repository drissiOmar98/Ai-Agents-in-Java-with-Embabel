package com.omar.contract_sentinel_ai_agent.model;

/**
 * The recommended semantic version bump for the new API release, computed
 * by a deterministic rule rather than left to guesswork.
 *
 * <p>Produced by
 * {@link com.omar.contract_sentinel_ai_agent.agent.VersioningAgent#recommendSemverBump},
 * which delegates the actual classification to
 * {@link com.omar.contract_sentinel_ai_agent.tool.SemverRuleTool} — a plain rule engine,
 * not an LLM call — and only uses the LLM afterward to phrase the
 * rationale in plain language.</p>
 *
 * @param bumpType  one of {@code "MAJOR"}, {@code "MINOR"}, or {@code "PATCH"}
 * @param rationale a short, human-readable explanation of why this bump is correct,
 *                  grounded in the actual counts of breaking/added/other changes
 */
public record SemverRecommendation(String bumpType, String rationale) {
}
