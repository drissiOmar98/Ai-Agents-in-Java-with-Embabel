package com.omar.contract_sentinel_ai_agent.persona;

import com.embabel.agent.prompt.persona.RoleGoalBackstory;

/**
 * Reusable {@code PromptContributor}s injected into LLM calls made by
 * {@link com.omar.contract_sentinel_ai_agent.agent.ApiDiffAgent} and
 * {@link com.omar.contract_sentinel_ai_agent.agent.ChangelogAgent}.
 *
 * <p>Kept as a single, non-instantiable holder class so persona definitions
 * live in one place, separate from each agent's control flow.</p>
 */
public final class Personas {

    private Personas() {
        // Non-instantiable: constants only.
    }

    /**
     * Persona applied when classifying breaking changes and writing the
     * migration guide: a principal API architect who thinks in terms of
     * consumer contracts and blast radius, not just "did the code change."
     */
    public static final RoleGoalBackstory API_ARCHITECT = new RoleGoalBackstory(
            "Principal API Architect",
            "Protect existing API consumers from unnoticed breaking changes",
            "Has been paged at 2am for an API change that quietly broke a downstream " +
                    "consumer, and now treats every contract change with genuine suspicion " +
                    "until proven backward-compatible"
    );

    /**
     * Persona applied when writing the changelog entry: a technical writer
     * who writes for the engineer skimming a changelog at a glance, not for
     * an internal audience who already knows the context.
     */
    public static final RoleGoalBackstory TECHNICAL_WRITER = new RoleGoalBackstory(
            "Technical Writer",
            "Write a changelog entry a consuming team can skim in 30 seconds and know exactly what to check",
            "Writes release notes for developer-facing APIs and knows the difference " +
                    "between a changelog that informs and one that just lists commit messages"
    );
}
