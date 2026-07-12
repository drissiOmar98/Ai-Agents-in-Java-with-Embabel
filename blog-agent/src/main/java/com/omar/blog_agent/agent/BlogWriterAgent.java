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
     * Step 2: writes a beginner-friendly first draft from the research.
     *
     * @param research the output of {@link #researchTopic}
     * @param ai       Embabel's fluent LLM access point
     * @return the first draft, title and Markdown content
     */
    @Action(description = "Write a first draft of the blog post")
    public DraftPost writeDraft(ResearchedTopic research, Ai ai) {
        return ai
                .withLlm(LlmOptions.withDefaults().withMaxTokens(16384))
                .withId("blog-post-draft-writer")
                .withPromptContributors(List.of(Personas.WRITER, Personas.JSON_OUTPUT))
                .creating(DraftPost.class)
                .fromPrompt("""
                        Write a blog post about: %s

                        Use the following research to inform your writing:
                        %s

                        Keep it practical and beginner friendly.
                        Use short sentences and plain language.
                        Include code examples but keep them short and simple.
                        Write the content in Markdown.
                        """.formatted(research.topic(), research.research())
                );
    }

    /**
     * Step 3: reviews the draft for technical accuracy and tightens the prose.
     *
     * <p>Uses the {@code reviewer} LLM role (configured separately from the
     * default model in {@code application.yml}) so review quality can be
     * tuned independently of drafting.</p>
     *
     * @param draft the output of {@link #writeDraft}
     * @param ai    Embabel's fluent LLM access point
     * @return the revised title and content plus a summary of the changes made
     */
    @Action(description = "Review and improve the draft")
    public ReviewedPost reviewDraft(DraftPost draft, Ai ai) {
        return ai
                .withLlm(LlmOptions.withLlmForRole("reviewer").withMaxTokens(16384))
                .withId("blog-post-reviewer")
                .withPromptContributors(List.of(Personas.REVIEWER, Personas.JSON_OUTPUT))
                .creating(ReviewedPost.class)
                .fromPrompt("""
                        Title: %s
                        Content:
                        %s

                        Fix any technical errors. Tighten the writing.
                        Provide the revised title, revised content, and a brief
                        summary of the changes you made as feedback.
                        """.formatted(draft.title(), draft.content())
                );
    }

    /**
     * Step 4: generates a one- or two-sentence TLDR and prepends it as a
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
     * Step 5 (goal): generates YAML front matter, prepends it to the post,
     * writes the finished Markdown file to disk, and returns the published
     * post. This is the action Embabel treats as achieving the agent's goal.
     *
     * <p>The LLM is given the {@link ReadingStatsTool} so it computes an
     * accurate read time rather than estimating one itself.</p>
     *
     * @param post the output of {@link #addTldr}
     * @param ai   Embabel's fluent LLM access point
     * @return the fully published post, including front matter
     */
    @AchievesGoal(description = "A reviewed and polished blog post with front matter")
    @Action(description = "Add front matter to the top of the blog post")
    public PublishedPost addFrontMatter(FinalPost post, Ai ai) {
        FrontMatter frontMatter = generateFrontMatter(post, ai);
        String frontMatterBlock = renderFrontMatterBlock(post, frontMatter);

        String contentWithFrontMatter = frontMatterBlock + "\n" + post.content();
        PublishedPost publishedPost = new PublishedPost(post.title(), contentWithFrontMatter, post.feedback());

        writeToFile(publishedPost);
        return publishedPost;
    }


}
