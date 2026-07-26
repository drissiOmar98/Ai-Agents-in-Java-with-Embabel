package com.omar.contract_sentinel_ai_agent.model;

/**
 * A single structural change from an {@link ApiDiffReport}, judged for its
 * real-world impact on API consumers.
 *
 * @param change         the underlying structural change, e.g.
 *                       {@code "DELETE /users/{id}"} or
 *                       {@code "GET /users/{id} response field 'email' removed"}
 * @param severity       one of {@code "BREAKING"}, {@code "NON_BREAKING"}, or {@code "DEPRECATION"}
 * @param consumerImpact a concrete explanation of what happens to an existing
 *                       consumer if this change ships as-is
 */
public record BreakingChange(String change, String severity, String consumerImpact) {
}
