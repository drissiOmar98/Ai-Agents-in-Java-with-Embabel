package com.omar.blog_agent.model;

/**
 * A {@link DraftPost} after technical review: errors fixed, writing
 * tightened, with reviewer feedback captured for transparency.
 *
 * @param title    the revised title
 * @param content  the revised Markdown content
 * @param feedback a brief summary of the changes the reviewer made
 */
public record ReviewedPost(String title, String content, String feedback) implements BlogPost {
}
