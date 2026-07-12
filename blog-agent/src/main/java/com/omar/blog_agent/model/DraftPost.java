package com.omar.blog_agent.model;

/**
 * The first draft of the blog post, written from {@link ResearchedTopic}
 * research and not yet reviewed for accuracy or style.
 *
 * @param title   the draft's working title
 * @param content the draft's Markdown content
 */
public record DraftPost(String title, String content) implements BlogPost {
}
