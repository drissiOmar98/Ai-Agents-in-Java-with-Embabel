package com.omar.blog_agent.agent;


import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.omar.blog_agent.model.FinalPost;
import com.omar.blog_agent.model.PublishedPost;
import com.omar.blog_agent.model.SocialPosts;
import com.omar.blog_agent.model.ThumbnailPrompt;
import com.omar.blog_agent.persona.Personas;

import java.util.List;

/**
 * Promotion-focused agent contributing two independent, downstream actions
 * to the {@link BlogWriterAgent} pipeline: a hero image prompt and social
 * media promotion copy.
 *
 * <p>Kept separate from {@link BlogWriterAgent} because these steps are
 * about distribution, not authoring the article itself. Neither step
 * modifies the post's own content or title.</p>
 */
@Agent(description = "Generate a hero image prompt and social media promotion copy for a blog post")
public class PromotionAgent {


}
