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

    /**
     * Extracts the previous (currently shipped) version's endpoints from
     * the user's input.
     *
     * @param userInput free-text input expected to describe both the previous
     *                  and current version of the API contract
     * @param context   Embabel's operation context, providing access to the LLM
     * @return every endpoint defined in the previous version
     * @throws ApiSnapshotIncompleteException if no previous-version endpoints could be identified
     */
    @Action
    public PreviousApiSnapshot extractPreviousSnapshot(UserInput userInput, OperationContext context) {
        PreviousApiSnapshot snapshot = context.ai()
                .withDefaultLlm()
                .createObjectIfPossible(
                        """
                        The following text describes two versions of an API contract.
                        Extract only the PREVIOUS (currently shipped) version's endpoints:
                        %s

                        For each endpoint, identify its HTTP method, path, request fields
                        (as "name:type"), and response fields (as "name:type").
                        Create a PreviousApiSnapshot from these endpoints.
                        """.formatted(userInput.getContent()),
                        PreviousApiSnapshot.class
                );

        if (snapshot == null || snapshot.endpoints() == null || snapshot.endpoints().isEmpty()) {
            throw new ApiSnapshotIncompleteException(
                    "Could not identify any endpoints for the previous API version");
        }
        return snapshot;
    }

    /**
     * Extracts the new, not-yet-released version's endpoints from the
     * user's input.
     *
     * @param userInput free-text input expected to describe both the previous
     *                  and current version of the API contract
     * @param context   Embabel's operation context, providing access to the LLM
     * @return every endpoint defined in the current version
     * @throws ApiSnapshotIncompleteException if no current-version endpoints could be identified
     */
    @Action
    public CurrentApiSnapshot extractCurrentSnapshot(UserInput userInput, OperationContext context) {
        CurrentApiSnapshot snapshot = context.ai()
                .withDefaultLlm()
                .createObjectIfPossible(
                        """
                        The following text describes two versions of an API contract.
                        Extract only the CURRENT (new, not yet released) version's endpoints:
                        %s

                        For each endpoint, identify its HTTP method, path, request fields
                        (as "name:type"), and response fields (as "name:type").
                        Create a CurrentApiSnapshot from these endpoints.
                        """.formatted(userInput.getContent()),
                        CurrentApiSnapshot.class
                );

        if (snapshot == null || snapshot.endpoints() == null || snapshot.endpoints().isEmpty()) {
            throw new ApiSnapshotIncompleteException(
                    "Could not identify any endpoints for the current API version");
        }
        return snapshot;
    }

    /**
     * Mechanically diffs the two snapshots using {@link ApiDiffTool}, so
     * additions, removals, and field-level changes are computed by exact
     * set comparison rather than an LLM comparing two lists by eye.
     *
     * @param previous the output of {@link #extractPreviousSnapshot}
     * @param current  the output of {@link #extractCurrentSnapshot}
     * @param context  Embabel's operation context, providing access to the LLM
     * @return the structural diff: what was added, removed, and changed
     */
    @Action(description = "Structurally diff the previous and current API contracts")
    public ApiDiffReport compareSnapshots(PreviousApiSnapshot previous, CurrentApiSnapshot current,
                                            OperationContext context) {
        return context.ai()
                .withDefaultLlm()
                .withToolObject(apiDiffTool)
                .createObjectIfPossible(
                        """
                        Use the compareEndpoints tool with:
                        - previousEndpoints: %s
                        - currentEndpoints: %s

                        Put the tool's exact ADDED entries into addedEndpoints, its exact
                        REMOVED entries into removedEndpoints, and its exact CHANGED
                        entries into changedEndpoints. Each is a list, so split the tool's
                        comma/semicolon-separated groups into individual list entries.
                        If a section wasn't in the tool's output, leave that list empty.
                        Create an ApiDiffReport from the tool's result.
                        """.formatted(serialize(previous), serialize(current)),
                        ApiDiffReport.class
                );
    }

    /**
     * Judges the real-world consumer impact of each structural change from
     * {@link #compareSnapshots}, using the {@link Personas#API_ARCHITECT}
     * persona so classification reflects genuine consumer-contract
     * thinking rather than a surface reading of "did this field change."
     *
     * @param diffReport the output of {@link #compareSnapshots}
     * @param context    Embabel's operation context, providing access to the LLM
     * @return every change with a severity classification and consumer impact explanation
     */
    @Action
    public BreakingChangeReport classifyBreakingChanges(ApiDiffReport diffReport, OperationContext context) {
        BreakingChangeReport report = context.ai()
                .withDefaultLlm()
                .withPromptContributors(List.of(Personas.API_ARCHITECT))
                .createObjectIfPossible(
                        """
                        Added endpoints: %s
                        Removed endpoints: %s
                        Changed endpoints: %s

                        For every removed endpoint and every changed endpoint, classify it as:
                        - BREAKING: an existing consumer's current integration will fail
                        - NON_BREAKING: existing consumers are unaffected (e.g. a purely
                          additive optional field)
                        - DEPRECATION: still works today but consumers should migrate away

                        Added endpoints are always NON_BREAKING (nothing existing depended
                        on them). For each, explain the concrete consumer impact.
                        Create a BreakingChangeReport listing every change classified this way.
                        """.formatted(
                                diffReport.addedEndpoints().isEmpty() ? "(none)" : String.join(", ", diffReport.addedEndpoints()),
                                diffReport.removedEndpoints().isEmpty() ? "(none)" : String.join(", ", diffReport.removedEndpoints()),
                                diffReport.changedEndpoints().isEmpty() ? "(none)" : String.join(", ", diffReport.changedEndpoints())
                        ),
                        BreakingChangeReport.class
                );

        boolean hasBreaking = report.changes().stream()
                .anyMatch(change -> "BREAKING".equalsIgnoreCase(change.severity()));
        return new BreakingChangeReport(report.changes(), hasBreaking);
    }

    /**
     * Writes a concrete, step-by-step guide for existing consumers to
     * migrate safely to the new contract version, informed by both the
     * classified changes and the computed semver bump.
     *
     * <p>This is the pipeline's primary goal.</p>
     *
     * @param breakingChangeReport the output of {@link #classifyBreakingChanges}
     * @param semverRecommendation the output of {@link VersioningAgent#recommendSemverBump}
     * @param context              Embabel's operation context, providing access to the LLM
     * @return ordered migration steps, capped at
     *         {@link ContractSentinelProperties#maxMigrationSteps()}
     */
    @AchievesGoal(description = "A step-by-step migration guide for existing API consumers")
    @Action
    public MigrationGuide generateMigrationGuide(BreakingChangeReport breakingChangeReport,
                                                   SemverRecommendation semverRecommendation, OperationContext context) {
        String changesSummary = breakingChangeReport.changes().stream()
                .map(change -> "- [%s] %s — %s".formatted(change.severity(), change.change(), change.consumerImpact()))
                .collect(Collectors.joining("\n"));

        MigrationGuide guide = context.ai()
                .withDefaultLlm()
                .withPromptContributors(List.of(Personas.API_ARCHITECT))
                .createObjectIfPossible(
                        """
                        Recommended version bump: %s (%s)

                        Classified changes:
                        %s

                        Write up to %d concrete, ordered migration steps for an existing
                        consumer team to follow before this version ships. Address every
                        BREAKING change explicitly - name what field/endpoint they must
                        update and how. Skip steps for NON_BREAKING changes unless there's
                        a genuine reason a consumer should still act on them.
                        Create a MigrationGuide from these steps.
                        """.formatted(
                                semverRecommendation.bumpType(),
                                semverRecommendation.rationale(),
                                changesSummary.isBlank() ? "(no changes classified)" : changesSummary,
                                properties.maxMigrationSteps()
                        ),
                        MigrationGuide.class
                );

        log.info("Migration guide generated: {} steps, bump type {}",
                guide != null ? guide.steps().size() : 0, semverRecommendation.bumpType());
        return guide;
    }

    private String serialize(PreviousApiSnapshot snapshot) {
        return snapshot.endpoints().stream().map(this::serializeEndpoint).collect(Collectors.joining(";"));
    }

    private String serialize(CurrentApiSnapshot snapshot) {
        return snapshot.endpoints().stream().map(this::serializeEndpoint).collect(Collectors.joining(";"));
    }

    private String serializeEndpoint(ApiEndpoint endpoint) {
        return "%s|%s|%s|%s".formatted(
                endpoint.method(),
                endpoint.path(),
                String.join(",", endpoint.requestFields()),
                String.join(",", endpoint.responseFields())
        );
    }
}
