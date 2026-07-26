package com.omar.contract_sentinel_ai_agent.agent;


import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.OperationContext;
import com.omar.contract_sentinel_ai_agent.model.ApiDiffReport;
import com.omar.contract_sentinel_ai_agent.model.BreakingChangeReport;
import com.omar.contract_sentinel_ai_agent.model.ChangelogEntry;
import com.omar.contract_sentinel_ai_agent.model.SemverRecommendation;
import com.omar.contract_sentinel_ai_agent.persona.Personas;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Downstream agent contributing a changelog-generation goal to the
 * {@link ApiDiffAgent} pipeline.
 *
 * <p>Kept separate from {@link ApiDiffAgent} because writing a changelog
 * entry is a distinct deliverable from the migration guide — one is
 * written for a consuming team that needs to act, the other for anyone
 * skimming release history. Both are derivable from the same diff and
 * classification data, so this agent depends only on
 * {@link ApiDiffReport}, {@link BreakingChangeReport}, and
 * {@link SemverRecommendation}, making it reachable independently of
 * {@link ApiDiffAgent#generateMigrationGuide}.</p>
 */
@Agent(description = "Generates a Keep-a-Changelog-style entry for an API release")
public class ChangelogAgent {

    /**
     * Writes a changelog entry in the standard Added/Changed/Removed/Deprecated
     * structure, using the {@link Personas#TECHNICAL_WRITER} persona so the
     * result reads like a real release note rather than a raw diff dump.
     *
     * <p>Marked as its own {@link AchievesGoal} since a changelog entry is a
     * useful deliverable on its own, reachable once the diff and
     * classification exist, without ever generating a migration guide.</p>
     *
     * @param diffReport           the output of {@link ApiDiffAgent#compareSnapshots}
     * @param breakingChangeReport the output of {@link ApiDiffAgent#classifyBreakingChanges}
     * @param semverRecommendation the output of {@link VersioningAgent#recommendSemverBump}
     * @param context              Embabel's operation context, providing access to the LLM
     * @return the changelog entry, categorized into added/changed/removed/deprecated
     */
    @AchievesGoal(description = "A Keep-a-Changelog-style entry summarizing this API release")
    @Action(description = "Generate a changelog entry for this API release")
    public ChangelogEntry generateChangelogEntry(ApiDiffReport diffReport, BreakingChangeReport breakingChangeReport,
                                                 SemverRecommendation semverRecommendation, OperationContext context) {
        String classifiedChanges = breakingChangeReport.changes().stream()
                .map(change -> "- [%s] %s".formatted(change.severity(), change.change()))
                .collect(Collectors.joining("\n"));

        return context.ai()
                .withDefaultLlm()
                .withPromptContributors(List.of(Personas.TECHNICAL_WRITER))
                .createObjectIfPossible(
                        """
                        Version bump: %s

                        Added endpoints: %s

                        Classified changes:
                        %s

                        Write a changelog entry in the Keep a Changelog style:
                        - "added": new endpoints/capabilities, one line each
                        - "changed": NON_BREAKING field changes to existing endpoints
                        - "removed": endpoints/fields removed entirely (BREAKING removals)
                        - "deprecated": anything classified as DEPRECATION

                        Each line should be a single, skimmable sentence a consuming
                        team could read in passing and immediately understand.
                        Create a ChangelogEntry from these four categories.
                        """.formatted(
                                semverRecommendation.bumpType(),
                                diffReport.addedEndpoints().isEmpty() ? "(none)" : String.join(", ", diffReport.addedEndpoints()),
                                classifiedChanges.isBlank() ? "(none)" : classifiedChanges
                        ),
                        ChangelogEntry.class
                );
    }
}
