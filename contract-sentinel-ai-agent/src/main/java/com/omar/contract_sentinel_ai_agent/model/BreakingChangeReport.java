package com.omar.contract_sentinel_ai_agent.model;

import java.util.List;

/**
 * Every change from an {@link ApiDiffReport}, each classified with its real
 * consumer impact.
 *
 * <p>Feeds into {@link com.omar.contract_sentinel_ai_agent.agent.VersioningAgent#recommendSemverBump}
 * (to compute the correct semver bump) and
 * {@link com.omar.contract_sentinel_ai_agent.agent.ApiDiffAgent#generateMigrationGuide}
 * (to explain what consumers need to change).</p>
 *
 * @param changes             every diffed change with its severity and consumer impact
 * @param hasBreakingChanges  convenience flag, {@code true} if any change has
 *                            {@code severity} equal to {@code "BREAKING"}
 */
public record BreakingChangeReport(List<BreakingChange> changes, boolean hasBreakingChanges) {
}
