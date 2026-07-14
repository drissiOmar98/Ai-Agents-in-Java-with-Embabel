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

    /**
     * Writes an image-generation prompt (plus accessibility alt text) for
     * the post's hero/thumbnail image.
     *
     * <p>Runs on {@link FinalPost} (before front matter is added) so
     * {@link BlogWriterAgent#addFrontMatter} can embed the result directly
     * into the front matter block.</p>
     *
     * @param post the output of {@link BlogWriterAgent#addTldr}
     * @param ai   Embabel's fluent LLM access point
     * @return a detailed image-generation prompt and matching alt text
     */
    @Action(description = "Write an image generation prompt for the post's hero image")
    public ThumbnailPrompt generateThumbnailPrompt(FinalPost post, Ai ai) {
        return ai
                .withDefaultLlm()
                .withId("blog-post-thumbnail-writer")
                .withPromptContributors(List.of(Personas.JSON_OUTPUT))
                .creating(ThumbnailPrompt.class)
                .fromPrompt("""
                        Write a detailed image-generation prompt for a hero/thumbnail
                        image for this blog post. Describe composition, style (e.g. flat
                        vector illustration, isometric, minimalist), color palette, and
                        any symbolic elements tied to the topic. Avoid any embedded text
                        in the image and avoid depicting real, named people.

                        Also write concise accessibility alt text (under 125 characters)
                        describing the intended image.

                        Title: %s
                        Content:
                        %s
                        """.formatted(post.title(), post.content())
                );
    }

    /**
     * Generates promotional copy for sharing the published post on
     * Twitter/X and LinkedIn.
     *
     * <p>Marked as its own {@link AchievesGoal} since promotion is a
     * useful deliverable independent of publishing &mdash; Embabel can
     * satisfy this goal once {@link BlogWriterAgent#addFrontMatter} has
     * produced a {@link PublishedPost}, without every run needing to
     * request social copy explicitly.</p>
     *
     * @param post the output of {@link BlogWriterAgent#addFrontMatter}
     * @param ai   Embabel's fluent LLM access point
     * @return a Twitter/X post and a LinkedIn post promoting the article
     */
    @AchievesGoal(description = "Social media promotion snippets for a published post")
    @Action(description = "Write social media posts to promote the article")
    public SocialPosts generateSocialPosts(PublishedPost post, Ai ai) {
        return ai
                .withDefaultLlm()
                .withId("blog-post-social-writer")
                .withPromptContributors(List.of(Personas.SOCIAL_MEDIA_MANAGER, Personas.JSON_OUTPUT))
                .creating(SocialPosts.class)
                .fromPrompt("""
                        Write two promotional posts for this article, aimed at developers.

                        Twitter/X post: under 280 characters, punchy, one concrete hook or
                        takeaway, no more than 2 hashtags.

                        LinkedIn post: 3-5 short paragraphs, more context and a professional
                        tone, ending with a question or call to action to read the full post.

                        Title: %s
                        Content:
                        %s
                        """.formatted(post.title(), post.content())
                );
    }
}
