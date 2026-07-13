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

    /**
     * Persona applied when outlining and generating title options: a
     * content strategist thinking about structure and reader appeal before
     * any prose is written.
     */
    public static final RoleGoalBackstory STRATEGIST = new RoleGoalBackstory(
            "Content Strategist",
            "Plan a clear, compelling structure and angle before drafting",
            "Editorial strategist who has shipped hundreds of technical articles and knows what makes readers click and keep reading"
    );


    /**
     * Persona applied when fact-checking a draft's technical claims
     * against web sources.
     */
    public static final RoleGoalBackstory FACT_CHECKER = new RoleGoalBackstory(
            "Technical Fact Checker",
            "Verify technical claims in the draft are accurate and current",
            "Meticulous researcher who cross-checks every claim against authoritative sources before signing off"
    );

    /**
     * Persona applied when scoring readability and suggesting simplifications.
     */
    public static final RoleGoalBackstory READABILITY_EDITOR = new RoleGoalBackstory(
            "Readability Editor",
            "Evaluate reading level and suggest concrete simplifications",
            "Plain-language advocate who helps technical writers reach a broader audience without dumbing content down"
    );

    /**
     * Persona applied when writing social media promotion copy.
     */
    public static final RoleGoalBackstory SOCIAL_MEDIA_MANAGER = new RoleGoalBackstory(
            "Social Media Manager",
            "Turn a published article into scroll-stopping promotional copy",
            "Developer-relations social media manager who knows how technical audiences behave on Twitter/X and LinkedIn"
    );


}
