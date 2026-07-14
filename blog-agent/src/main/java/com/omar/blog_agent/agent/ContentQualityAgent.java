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


}
