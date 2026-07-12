package com.omar.blog_agent.persona;

import com.embabel.agent.prompt.persona.RoleGoalBackstory;
import com.embabel.common.ai.prompt.PromptContributor;

/**
 * Reusable {@code PromptContributor}s injected into LLM calls made by
 * {@link com.omar.blog_agent.agent.BlogWriterAgent}.
 *
 * <p>Kept as a single, non-instantiable holder class so persona definitions
 * live in one place and stay easy to tune independently of the agent's
 * control flow.</p>
 */
public abstract class Personas {

    private Personas() {
        // Non-instantiable: constants only.
    }

    /**
     * Instructs the LLM that its response will be parsed as JSON and that
     * embedded double quotes must be escaped. Attached to every action
     * that requests a structured (record) response.
     */
    public static final PromptContributor JSON_OUTPUT = PromptContributor.fixed("""
            IMPORTANT: Your response will be parsed as JSON.
            You MUST escape all double quotes inside string values with a backslash.
            For example: "content": "She said \\"hello\\""
            """);

    /**
     * Persona applied when generating the first draft: an experienced
     * developer/educator writing practical, beginner-friendly content.
     */
    public static final RoleGoalBackstory WRITER = new RoleGoalBackstory(
            "Software Developer and Educator",
            "Write practical, beginner-friendly blog posts",
            "Experienced developer who loves teaching through clear, simple writing"
    );

    /**
     * Persona applied during the review step: a technical editor focused
     * on accuracy and tightening prose.
     */
    public static final RoleGoalBackstory REVIEWER = new RoleGoalBackstory(
            "Technical Editor",
            "Review and polish technical blog posts",
            "Seasoned editor focused on clarity, accuracy, and tight writing"
    );
}
