package com.omar.blog_agent.agent;


import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.core.CoreToolGroups;
import com.omar.blog_agent.model.DraftPost;
import com.omar.blog_agent.model.FactCheckReport;
import com.omar.blog_agent.model.ReadabilityReport;
import com.omar.blog_agent.model.ReviewedPost;
import com.omar.blog_agent.persona.Personas;
import com.omar.blog_agent.tool.ReadabilityTool;

import java.util.List;

/**
 * Quality-gate agent contributing two independent checks to the
 * {@link BlogWriterAgent} pipeline: technical fact-checking and readability
 * scoring.
 *
 * <p>Kept separate from {@link BlogWriterAgent} because these steps are
 * verification/analysis, not content creation &mdash; they read a post and
 * produce a report, rather than producing the next stage of the post
 * itself. Their outputs ({@link FactCheckReport}, {@link ReadabilityReport})
 * feed back into {@code BlogWriterAgent}'s
 * {@link BlogWriterAgent#reviewDraft} and
 * {@link BlogWriterAgent#addFrontMatter} steps respectively.</p>
 */
@Agent(description = "Fact-check technical claims and score the readability of a blog post")
public class ContentQualityAgent {

    private final ReadabilityTool readabilityTool;

    /**
     * @param readabilityTool tool exposed to the LLM for computing a deterministic
     *                        Flesch Reading Ease score in {@link #scoreReadability}
     */
    public ContentQualityAgent(ReadabilityTool readabilityTool) {
        this.readabilityTool = readabilityTool;
    }

    /**
     * Verifies the technical claims made in a draft against web sources.
     *
     * <p>Deliberately checks the draft (before review) rather than the
     * final post, so anything flagged here can still be corrected in
     * {@link BlogWriterAgent#reviewDraft} before publication.</p>
     *
     * @param draft the output of {@link BlogWriterAgent#writeDraft}
     * @param ai    Embabel's fluent LLM access point
     * @return one verdict per checked claim, plus a convenience flag if any need attention
     */
    @Action(description = "Verify technical claims in the draft against known sources")
    public FactCheckReport factCheckDraft(DraftPost draft, Ai ai) {
        FactCheckReport report = ai
                .withDefaultLlm()
                .withToolGroup(CoreToolGroups.WEB)
                .withId("blog-post-fact-checker")
                .withPromptContributors(List.of(Personas.FACT_CHECKER, Personas.JSON_OUTPUT))
                .creating(FactCheckReport.class)
                .fromPrompt("""
                        Identify the specific technical or factual claims in this draft
                        (version numbers, API behavior, benchmarks, historical/attribution
                        claims, etc. — skip pure opinion or style statements).

                        For each claim, use web search to verify it, then record a verdict
                        of VERIFIED, UNVERIFIED, or INCORRECT with a short explanation.
                        Limit yourself to at most 5 of the most significant claims and no
                        more than 3 web searches total to avoid rate limiting.

                        Title: %s
                        Content:
                        %s
                        """.formatted(draft.title(), draft.content())
                );

        boolean hasIssues = report.findings().stream()
                .anyMatch(finding -> !"VERIFIED".equalsIgnoreCase(finding.verdict()));

        return new FactCheckReport(report.findings(), hasIssues);
    }

    /**
     * Scores the reviewed post's readability and suggests concrete
     * simplifications, grounded in a deterministically computed Flesch
     * Reading Ease score rather than an LLM guess.
     *
     * @param post the output of {@link BlogWriterAgent#reviewDraft}
     * @param ai   Embabel's fluent LLM access point
     * @return the reading level, numeric score, and simplification suggestions
     */
    @Action(description = "Evaluate reading level and suggest simplifications")
    public ReadabilityReport scoreReadability(ReviewedPost post, Ai ai) {
        return ai
                .withDefaultLlm()
                .withToolObject(readabilityTool)
                .withId("blog-post-readability-scorer")
                .withPromptContributors(List.of(Personas.READABILITY_EDITOR, Personas.JSON_OUTPUT))
                .creating(ReadabilityReport.class)
                .fromPrompt("""
                        Use the calculateReadability tool on the content below to compute
                        its Flesch Reading Ease score and level label. Put the exact numeric
                        score into fleschReadingEase and the exact level label into readingLevel.

                        Then suggest up to 5 concrete simplifications (shorter sentences,
                        simpler words, breaking up dense paragraphs) targeted at the
                        densest or most jargon-heavy parts of the post. Be specific about
                        which part of the post each suggestion applies to.

                        Content:
                        %s
                        """.formatted(post.content())
                );
    }
}
