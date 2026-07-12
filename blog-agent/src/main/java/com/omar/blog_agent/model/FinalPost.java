package com.omar.blog_agent.model;

/**
 * A {@link ReviewedPost} with a generated TLDR summary prepended to its
 * content. This is the last step before front matter is added and the
 * post is written to disk.
 *
 * @param title    the post title, unchanged from the reviewed stage
 * @param content  the Markdown content with a {@code > **TLDR:**} block prepended
 * @param feedback the reviewer feedback, carried through unchanged
 */
public record FinalPost(String title, String content, String feedback) implements BlogPost {
}
