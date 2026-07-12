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

}
