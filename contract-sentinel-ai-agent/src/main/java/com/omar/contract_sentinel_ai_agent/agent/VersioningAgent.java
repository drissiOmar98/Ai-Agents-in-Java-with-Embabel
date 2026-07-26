package com.omar.contract_sentinel_ai_agent.agent;


import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.OperationContext;
import com.omar.contract_sentinel_ai_agent.model.ApiDiffReport;
import com.omar.contract_sentinel_ai_agent.model.BreakingChange;
import com.omar.contract_sentinel_ai_agent.model.BreakingChangeReport;
import com.omar.contract_sentinel_ai_agent.model.SemverRecommendation;
import com.omar.contract_sentinel_ai_agent.tool.SemverRuleTool;

/**
 * Agent contributing an independent, rule-based semantic version
 * recommendation to the {@link ApiDiffAgent} pipeline.
 *
 * <p>Kept separate from {@link ApiDiffAgent} because this step is a
 * deterministic classification, not a generative one: given the counts of
 * breaking and added changes, there is exactly one correct answer per
 * <a href="https://semver.org">semver.org</a>, computed by
 * {@link SemverRuleTool} in plain Java rather than asked of an LLM. The
 * LLM's only job here is turning the already-decided bump type into a
 * short, readable rationale — a deliberately narrow use of the model,
 * grounded entirely in numbers this code already has in hand.</p>
 */
@Agent(description = "Computes the correct semantic version bump for an API change set")
public class VersioningAgent {

    private final SemverRuleTool semverRuleTool;

    /**
     * @param semverRuleTool the plain rule engine used to decide the bump type;
     *                       called directly in Java, not exposed to the LLM as a tool
     */
    public VersioningAgent(SemverRuleTool semverRuleTool) {
        this.semverRuleTool = semverRuleTool;
    }

    /**
     * Computes the correct semver bump for this change set and asks the
     * LLM only to phrase a short rationale for it.
     *
     * @param breakingChangeReport the output of {@link ApiDiffAgent#classifyBreakingChanges}
     * @param diffReport           the output of {@link ApiDiffAgent#compareSnapshots}
     * @param context              Embabel's operation context, providing access to the LLM
     * @return the computed bump type ({@code MAJOR}/{@code MINOR}/{@code PATCH})
     *         and a plain-language rationale for it
     */
    @Action(description = "Recommend the correct semver bump for this set of API changes")
    public SemverRecommendation recommendSemverBump(BreakingChangeReport breakingChangeReport,
                                                    ApiDiffReport diffReport, OperationContext context) {
        long breakingCount = breakingChangeReport.changes().stream()
                .map(BreakingChange::severity)
                .filter("BREAKING"::equalsIgnoreCase)
                .count();
        int addedCount = diffReport.addedEndpoints().size();

        // The classification itself is deterministic - decided here, in plain
        // Java, before the LLM is ever invoked for this step.
        String bumpType = semverRuleTool.recommendBump((int) breakingCount, addedCount);

        String rationale = context.ai()
                .withDefaultLlm()
                .createObjectIfPossible(
                        """
                        A semantic version bump of %s was computed for this API change set:
                        - %d breaking change(s)
                        - %d newly added endpoint(s)

                        Write one short sentence explaining why this bump type is correct,
                        referencing the actual counts above. Do not suggest a different
                        bump type - the decision is already made; just explain it.
                        """.formatted(bumpType, breakingCount, addedCount),
                        String.class
                );

        return new SemverRecommendation(bumpType, rationale);
    }
}
