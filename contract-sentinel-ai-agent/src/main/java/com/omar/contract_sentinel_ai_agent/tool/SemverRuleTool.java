package com.omar.contract_sentinel_ai_agent.tool;

import org.springframework.stereotype.Component;

/**
 * A plain semantic-versioning rule engine.
 *
 * <p>Deliberately <strong>not</strong> annotated as an {@code @LlmTool}.
 * By the time {@link com.omar.contract_sentinel_ai_agent.agent.VersioningAgent#recommendSemverBump}
 * runs, the calling Java code already holds the exact counts this decision
 * needs as typed data ({@code BreakingChangeReport}, {@code ApiDiffReport}) —
 * there's nothing for an LLM to extract or interpret. Routing a decision
 * like that through an LLM call would add latency, cost, and a sliver of
 * non-determinism to a choice that has one objectively correct answer per
 * <a href="https://semver.org">semver.org</a>'s rules. This class is called
 * directly as a normal Spring bean; the LLM is only used afterward, to turn
 * the already-decided bump type into a readable rationale sentence.</p>
 */
@Component
public class SemverRuleTool {

    /**
     * Determines the correct semantic version bump per semver.org's rules:
     * any breaking change forces a major bump; otherwise any added
     * capability (new endpoint) is a minor bump; anything else (pure fixes
     * or non-breaking field additions) is a patch bump.
     *
     * @param breakingChangeCount the number of changes classified as breaking
     * @param addedEndpointCount  the number of newly added endpoints
     * @return {@code "MAJOR"}, {@code "MINOR"}, or {@code "PATCH"}
     */
    public String recommendBump(int breakingChangeCount, int addedEndpointCount) {
        if (breakingChangeCount > 0) {
            return "MAJOR";
        }
        if (addedEndpointCount > 0) {
            return "MINOR";
        }
        return "PATCH";
    }
}
