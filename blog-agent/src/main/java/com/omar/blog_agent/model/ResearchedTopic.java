package com.omar.blog_agent.model;

/**
 * The output of the research step: the original topic paired with a
 * concise, web-sourced summary that seeds the first draft.
 *
 * @param topic    the original topic supplied by the user
 * @param research a concise summary of findings relevant to writing a blog post
 */
public record ResearchedTopic(String topic, String research) {
}
