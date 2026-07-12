package com.omar.blog_agent.model;

/**
 * The terminal state of the pipeline: a {@link FinalPost} with YAML front
 * matter prepended, ready to be published and already written to disk by
 * {@link com.omar.blog_agent.agent.BlogWriterAgent}.
 *
 * @param title    the post title
 * @param content  the complete Markdown document, including front matter
 * @param feedback the reviewer feedback, carried through unchanged
 */
public record PublishedPost(String title, String content, String feedback) implements BlogPost {
}
