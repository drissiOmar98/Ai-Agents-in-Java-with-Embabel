package com.omar.contract_sentinel_ai_agent.agent;

import com.omar.contract_sentinel_ai_agent.config.ContractSentinelProperties;
import com.omar.contract_sentinel_ai_agent.exception.ApiSnapshotIncompleteException;
import com.omar.contract_sentinel_ai_agent.model.*;
import com.omar.contract_sentinel_ai_agent.persona.Personas;
import com.omar.contract_sentinel_ai_agent.tool.ApiDiffTool;
import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.OperationContext;
import com.embabel.agent.domain.io.UserInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Embabel agent that solves a recurring, expensive problem for API-driven
 * teams: a new API version shipping with unnoticed breaking changes,
 * because nobody diffed the contract carefully enough before release.
 *
 * <p>This class owns the core extraction-through-migration-guide spine.
 * {@link VersioningAgent} contributes an independent, rule-based semver
 * recommendation that feeds into the migration guide, and
 * {@link ChangelogAgent} contributes a downstream changelog goal reachable
 * from the same diff and classification data.</p>
 *
 * <p>Pipeline shape:</p>
 *
 * <pre>
 * UserInput
 *   ├──&gt; extractPreviousSnapshot -&gt; PreviousApiSnapshot
 *   └──&gt; extractCurrentSnapshot  -&gt; CurrentApiSnapshot
 *            │
 *            ▼
 *   compareSnapshots (deterministic diff) -&gt; ApiDiffReport
 *            │
 *            ▼
 *   classifyBreakingChanges -&gt; BreakingChangeReport
 *            │
 *            ▼
 *   [VersioningAgent#recommendSemverBump -&gt; SemverRecommendation]
 *            │
 *            ▼
 *   generateMigrationGuide  🎯 GOAL  -&gt; MigrationGuide
 *
 *   (ApiDiffReport + BreakingChangeReport + SemverRecommendation also feed
 *    ChangelogAgent#generateChangelogEntry  🎯 GOAL)
 * </pre>
 */
@Agent(
        name = "api-contract-sentinel",
        description = "Diffs two API contract versions and flags breaking changes before they ship",
        version = "1.0.0",
        beanName = "apiDiffAgent"
)
public class ApiDiffAgent {

    private static final Logger log = LoggerFactory.getLogger(ApiDiffAgent.class);

    private final ContractSentinelProperties properties;
    private final ApiDiffTool apiDiffTool;

    /**
     * @param properties  bound {@code contract-sentinel.*} configuration (max migration steps)
     * @param apiDiffTool tool exposed to the LLM for computing a deterministic structural
     *                    diff in {@link #compareSnapshots}
     */
    public ApiDiffAgent(ContractSentinelProperties properties, ApiDiffTool apiDiffTool) {
        this.properties = properties;
        this.apiDiffTool = apiDiffTool;
    }


}
