package com.omar.contract_sentinel_ai_agent.model;

import java.util.List;

/**
 * A structural, mechanically-computed diff between a {@link PreviousApiSnapshot}
 * and a {@link CurrentApiSnapshot}.
 *
 * <p>Produced by
 * {@link com.omar.contract_sentinel_ai_agent.agent.ApiDiffAgent#compareSnapshots} using
 * {@link com.omar.contract_sentinel_ai_agent.tool.ApiDiffTool} for the actual comparison,
 * so the diff reflects a real structural comparison rather than an LLM's
 * side-by-side reading of two lists (which is exactly the kind of
 * multi-item comparison LLMs are unreliable at).</p>
 *
 * <p>This report is purely mechanical: it says <em>what</em> changed, not
 * whether a change is safe. Severity judgment happens next, in
 * {@link com.omar.contract_sentinel_ai_agentl.agent.ApiDiffAgent#classifyBreakingChanges}.</p>
 *
 * @param addedEndpoints   endpoints present in the current version but not the previous one
 * @param removedEndpoints endpoints present in the previous version but not the current one
 * @param changedEndpoints endpoints present in both versions whose request or
 *                         response fields differ, described field-by-field
 */
public record ApiDiffReport(List<String> addedEndpoints, List<String> removedEndpoints, List<String> changedEndpoints) {
}
