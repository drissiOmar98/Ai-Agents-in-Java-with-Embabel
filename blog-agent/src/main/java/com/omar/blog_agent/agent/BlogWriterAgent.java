package com.omar.blog_agent.agent;

import com.omar.blog_agent.config.BlogAgentProperties;
import com.omar.blog_agent.model.*;
import com.omar.blog_agent.persona.Personas;
import com.omar.blog_agent.tool.ReadingStatsTool;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.common.ai.model.LlmOptions;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.core.CoreToolGroups;
import com.omar.blog_agent.model.DraftPost;
import com.omar.blog_agent.util.Slugs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Embabel agent that turns a topic into a published, front-matter-tagged
 * Markdown blog post.
 *
 * <p>The pipeline is expressed as a linear sequence of {@link Action}
 * methods, each consuming the previous step's output type and producing the
 * next. Embabel resolves this chain automatically from the method
 * signatures:</p>
 *
 * <pre>
 * UserInput
 *   -&gt; researchTopic   -&gt; ResearchedTopic
 *   -&gt; writeDraft       -&gt; DraftPost
 *   -&gt; reviewDraft      -&gt; ReviewedPost
 *   -&gt; addTldr          -&gt; FinalPost
 *   -&gt; addFrontMatter   -&gt; PublishedPost   (goal, and writes the file to disk)
 * </pre>
 */
@Agent(description = "Write and review a blog post about a given topic")
public class BlogWriterAgent {

    private static final Logger log = LoggerFactory.getLogger(BlogWriterAgent.class);

    private final BlogAgentProperties properties;
    private final ReadingStatsTool readingStatsTool;

    /**
     * @param properties       bound {@code blog-agent.*} configuration (output directory, keyword count)
     * @param readingStatsTool tool exposed to the LLM for computing read time in {@link #addFrontMatter}
     */
    public BlogWriterAgent(BlogAgentProperties properties, ReadingStatsTool readingStatsTool) {
        this.properties = properties;
        this.readingStatsTool = readingStatsTool;
    }

    /**
     * Step 1: researches the user's topic using web search tools.
     *
     * <p>Search usage is intentionally capped in the prompt to avoid rate
     * limiting on the underlying search provider.</p>
     *
     * @param userInput the raw topic supplied by the user
     * @param ai        Embabel's fluent LLM access point
     * @return the topic paired with a research summary
     */
    @Action(description = "Research the topic using web search")
    public ResearchedTopic researchTopic(UserInput userInput, Ai ai) {
        return ai
                .withDefaultLlm()
                .withToolGroup(CoreToolGroups.WEB)
                .withId("blog-topic-researcher")
                .creating(ResearchedTopic.class)
                .fromPrompt("""
                        Research the following topic using web search tools.
                        Find current, relevant, and accurate information.
                        Limit yourself to no more than 3 web searches to avoid rate limiting.

                        Topic: %s

                        Provide the original topic and a concise summary
                        of your findings that would be useful for writing a blog post.
                        """.formatted(userInput.getContent())
                );
    }

    /**
     * Step 2: turns raw research into a structured outline &mdash; an angle
     * plus an ordered list of sections &mdash; before any prose is written.
     *
     * <p>Committing to structure first keeps the eventual draft on-topic
     * instead of wandering, and gives {@link #generateTitleOptions} and
     * {@link #writeHook} a concrete plan to react to.</p>
     *
     * @param research the output of {@link #researchTopic}
     * @param ai       Embabel's fluent LLM access point
     * @return the post's angle and section structure
     */
    @Action(description = "Create a structured outline before drafting")
    public Outline createOutline(ResearchedTopic research, Ai ai) {
        return ai
                .withDefaultLlm()
                .withId("blog-outline-writer")
                .withPromptContributors(List.of(Personas.STRATEGIST, Personas.JSON_OUTPUT))
                .creating(Outline.class)
                .fromPrompt("""
                        Plan a blog post about: %s

                        Research findings:
                        %s

                        Decide on a specific angle or thesis for the post (not just the
                        general topic), and break it into an ordered list of section
                        headings that a reader could skim to understand the post's flow.
                        Carry the research summary forward unchanged in researchSummary.
                        """.formatted(research.topic(), research.research())
                );
    }

    /**
     * Step 3: generates several candidate titles and selects the strongest
     * one, with a short rationale.
     *
     * @param outline the output of {@link #createOutline}
     * @param ai      Embabel's fluent LLM access point
     * @return the candidate titles, the selected title, and why it was chosen
     */
    @Action(description = "Write a catchy title: generate multiple options and pick the strongest one")
    public TitleOptions generateTitleOptions(Outline outline, Ai ai) {
        return ai
                .withDefaultLlm()
                .withId("blog-title-writer")
                .withPromptContributors(List.of(Personas.STRATEGIST, Personas.JSON_OUTPUT))
                .creating(TitleOptions.class)
                .fromPrompt("""
                        Generate 5 distinct title options for a blog post with this angle
                        and structure. Vary the style across options (e.g. direct/how-to,
                        curiosity-driven, number-based, problem/solution).

                        Angle: %s
                        Sections: %s

                        Then pick the single strongest title and explain briefly why it
                        beats the others for a technical developer audience.
                        """.formatted(outline.angle(), String.join(", ", outline.sections()))
                );
    }

    /**
     * Step 4: writes the opening paragraph, treated as its own focused task
     * rather than left to chance inside the full draft prompt.
     *
     * @param outline      the output of {@link #createOutline}
     * @param titleOptions the output of {@link #generateTitleOptions}
     * @param ai           Embabel's fluent LLM access point
     * @return the post's opening paragraph
     */
    @Action(description = "Write a hook: an engaging opening paragraph that pulls readers in")
    public Hook writeHook(Outline outline, TitleOptions titleOptions, Ai ai) {
        return ai
                .withDefaultLlm()
                .withId("blog-hook-writer")
                .withPromptContributors(List.of(Personas.WRITER, Personas.JSON_OUTPUT))
                .creating(Hook.class)
                .fromPrompt("""
                        Title: %s
                        Angle: %s

                        Write a single opening paragraph (2-4 sentences) that hooks a
                        developer reader immediately: lead with a relatable problem,
                        a surprising fact, or a sharp claim tied to the angle above.
                        Do not summarize the whole post here, just earn the next sentence.
                        No heading, just the paragraph text.
                        """.formatted(titleOptions.selectedTitle(), outline.angle())
                );
    }


    /**
     * Step 5: writes the full first draft, continuing from the pre-written
     * hook and following the outline's section structure, using the
     * already-selected title.
     *
     * @param outline      the output of {@link #createOutline}
     * @param titleOptions the output of {@link #generateTitleOptions}
     * @param hook         the output of {@link #writeHook}
     * @param ai           Embabel's fluent LLM access point
     * @return the first draft, title and Markdown content
     */
    @Action(description = "Write a first draft of the blog post")
    public DraftPost writeDraft(Outline outline, TitleOptions titleOptions, Hook hook, Ai ai) {
        DraftPost draft = ai
                .withLlm(LlmOptions.withDefaults().withMaxTokens(16384))
                .withId("blog-post-draft-writer")
                .withPromptContributors(List.of(Personas.WRITER, Personas.JSON_OUTPUT))
                .creating(DraftPost.class)
                .fromPrompt("""
                        Write the body of a blog post that continues on from this opening
                        paragraph, covering each section in order:

                        Title: %s
                        Opening paragraph (already written, repeat it verbatim as the start
                        of content, then continue): %s
                        Sections to cover: %s

                        Research to draw on:
                        %s

                        Keep it practical and beginner friendly.
                        Use short sentences and plain language.
                        Include code examples but keep them short and simple.
                        Write the content in Markdown, using the sections as headings.
                        """.formatted(
                        titleOptions.selectedTitle(),
                        hook.openingParagraph(),
                        String.join(", ", outline.sections()),
                        outline.researchSummary()
                ));

        // The title was already decided in generateTitleOptions; keep it authoritative
        // rather than trusting the drafting call to repeat it verbatim.
        return new DraftPost(titleOptions.selectedTitle(), draft.content());
    }

    /**
     * Step 6: reviews the draft for technical accuracy and tightens the
     * prose, taking the fact-check findings from
     * {@link ContentQualityAgent#factCheckDraft} into account so flagged
     * claims get corrected rather than just noted.
     *
     * <p>Uses the {@code reviewer} LLM role (configured separately from the
     * default model in {@code application.yml}) so review quality can be
     * tuned independently of drafting.</p>
     *
     * @param draft           the output of {@link #writeDraft}
     * @param factCheckReport the output of {@link ContentQualityAgent#factCheckDraft}
     * @param ai              Embabel's fluent LLM access point
     * @return the revised title and content plus a summary of the changes made
     */
    @Action(description = "Review and improve the draft, correcting any fact-check issues")
    public ReviewedPost reviewDraft(DraftPost draft, FactCheckReport factCheckReport, Ai ai) {
        String issues = factCheckReport.findings().stream()
                .filter(finding -> !"VERIFIED".equalsIgnoreCase(finding.verdict()))
                .map(finding -> "- [%s] %s — %s".formatted(finding.verdict(), finding.claim(), finding.explanation()))
                .collect(Collectors.joining("\n"));

        return ai
                .withLlm(LlmOptions.withLlmForRole("reviewer").withMaxTokens(16384))
                .withId("blog-post-reviewer")
                .withPromptContributors(List.of(Personas.REVIEWER, Personas.JSON_OUTPUT))
                .creating(ReviewedPost.class)
                .fromPrompt("""
                        Title: %s
                        Content:
                        %s

                        Fact-check findings to address (empty means nothing flagged):
                        %s

                        Fix any technical errors, including anything flagged above.
                        Tighten the writing.
                        Provide the revised title, revised content, and a brief
                        summary of the changes you made as feedback.
                        """.formatted(
                        draft.title(),
                        draft.content(),
                        issues.isBlank() ? "(none)" : issues
                ));
    }

    /**
     * Step 7: generates a one- or two-sentence TLDR and prepends it as a
     * Markdown blockquote above the reviewed content.
     *
     * @param post the output of {@link #reviewDraft}
     * @param ai   Embabel's fluent LLM access point
     * @return the post with a {@code > **TLDR:**} block prepended to its content
     */
    @Action(description = "Add a TLDR summary to the top of the blog post")
    public FinalPost addTldr(ReviewedPost post, Ai ai) {
        String tldr = ai
                .withDefaultLlm()
                .withId("blog-post-tldr")
                .creating(String.class)
                .fromPrompt("""
                        Write a one or two sentence TLDR summary for this blog post.
                        Return only the summary text, nothing else.

                        Title: %s
                        Content:
                        %s
                        """.formatted(post.title(), post.content())
                );

        String contentWithTldr = "> **TLDR:** " + tldr + "\n\n" + post.content();
        return new FinalPost(post.title(), contentWithTldr, post.feedback());
    }

    /**
     * Step 8 (goal): generates YAML front matter (including the hero image
     * prompt and reading level surfaced by the side agents), prepends it to
     * the post, writes the finished Markdown file to disk, and returns the
     * published post. This is the action Embabel treats as achieving the
     * agent's primary goal.
     *
     * <p>The LLM is given the {@link ReadingStatsTool} so it computes an
     * accurate read time rather than estimating one itself.</p>
     *
     * @param post              the output of {@link #addTldr}
     * @param thumbnailPrompt   the output of {@link PromotionAgent#generateThumbnailPrompt}
     * @param readabilityReport the output of {@link ContentQualityAgent#scoreReadability}
     * @param ai                Embabel's fluent LLM access point
     * @return the fully published post, including front matter
     */
    @AchievesGoal(description = "A reviewed and polished blog post with front matter")
    @Action(description = "Add front matter to the top of the blog post")
    public PublishedPost addFrontMatter(FinalPost post, ThumbnailPrompt thumbnailPrompt,
                                        ReadabilityReport readabilityReport, Ai ai) {
        FrontMatter frontMatter = generateFrontMatter(post, ai);
        String frontMatterBlock = renderFrontMatterBlock(post, frontMatter, thumbnailPrompt, readabilityReport);

        String contentWithFrontMatter = frontMatterBlock + "\n" + post.content();
        PublishedPost publishedPost = new PublishedPost(post.title(), contentWithFrontMatter, post.feedback());

        writeToFile(publishedPost);
        return publishedPost;
    }


    /**
     * Asks the LLM for front matter metadata (description, tags, keywords,
     * read time), using {@link ReadingStatsTool} for an accurate read time.
     *
     * @param post the post to generate metadata for
     * @param ai   Embabel's fluent LLM access point
     * @return the generated front matter metadata
     */
    private FrontMatter generateFrontMatter(FinalPost post, Ai ai) {
        return ai
                .withDefaultLlm()
                .withToolObject(readingStatsTool)
                .withId("blog-post-front-matter")
                .withPromptContributors(List.of(Personas.JSON_OUTPUT))
                .creating(FrontMatter.class)
                .fromPrompt("""
                        Generate front matter metadata for this blog post.
                        Provide a concise description (1-2 sentences), relevant tags, and up to %d keywords.

                        Use the calculateReadingStats tool on the post content below to compute
                        the read time. Put the tool's exact return string into the readTime field.

                        Title: %s
                        Content:
                        %s
                        """.formatted(properties.numberOfKeywords(), post.title(), post.content())
                );
    }

    /**
     * Renders the YAML front matter block for a post, from its title,
     * generated metadata, hero image details, and readability score.
     *
     * @param post              the post being published, used for its title
     * @param frontMatter       the generated metadata to render
     * @param thumbnailPrompt   hero image prompt/alt text to include
     * @param readabilityReport readability score/level to include
     * @return the complete {@code ---}-delimited YAML front matter block
     */
    private String renderFrontMatterBlock(FinalPost post, FrontMatter frontMatter,
                                          ThumbnailPrompt thumbnailPrompt, ReadabilityReport readabilityReport) {
        String slug = Slugs.slugify(post.title());

        String tags = frontMatter.tags().stream()
                .map(tag -> "  - " + tag)
                .collect(Collectors.joining("\n"));

        String keywords = frontMatter.keywords().stream()
                .map(keyword -> "  - " + keyword)
                .collect(Collectors.joining("\n"));

        return """
                ---
                title: "%s"
                slug: %s
                date: "%sT08:00:00.000Z"
                published: true
                description: "%s"
                author: "Dan Vega"
                readTime: "%s"
                readingLevel: "%s (Flesch %d)"
                heroImagePrompt: "%s"
                heroImageAlt: "%s"
                tags:
                %s
                keywords:
                %s
                ---
                """.formatted(
                post.title(),
                slug,
                LocalDate.now(),
                frontMatter.description(),
                frontMatter.readTime(),
                readabilityReport.readingLevel(),
                readabilityReport.fleschReadingEase(),
                thumbnailPrompt.imagePrompt().replace("\"", "\\\""),
                thumbnailPrompt.altText().replace("\"", "\\\""),
                tags,
                keywords
        );
    }


    /**
     * Writes a post's content to {@code <output-dir>/<slugified-title>.md},
     * creating the output directory if it does not yet exist.
     *
     * <p>Failures are logged rather than propagated so a filesystem issue
     * does not surface as an opaque agent failure to the caller; the
     * {@link PublishedPost} is still returned in memory either way.</p>
     *
     * @param post the fully assembled post to persist
     */
    private void writeToFile(BlogPost post) {
        String filename = Slugs.slugify(post.title()) + ".md";

        Path outputDir = Path.of(properties.outputDir());
        Path filePath = outputDir.resolve(filename);

        try {
            Files.createDirectories(outputDir);
            Files.writeString(filePath, post.content());
            log.info("Blog post written to {}", filePath.toAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to write blog post to {}: {}", filePath, e.getMessage());
        }
    }
}
