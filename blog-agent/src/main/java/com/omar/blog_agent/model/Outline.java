package com.omar.blog_agent.model;

import java.util.List;

/**
 * A structured plan for the post, generated from {@link ResearchedTopic}
 * before any prose is written. Drafting "blind" tends to wander; asking the
 * LLM to commit to an angle and section list first keeps the eventual draft
 * focused.
 *
 * @param topic            the original topic, carried through for convenience
 * @param angle            the specific thesis or point of view the post will take
 *                         (e.g. "why X is a bad default, and when to reach for it anyway")
 * @param sections         an ordered list of section headings the draft should follow
 * @param researchSummary  the research findings the outline was built from, carried
 *                         forward so downstream steps don't need to re-fetch
 *                         {@link ResearchedTopic}
 */
public record Outline(String topic, String angle, List<String> sections, String researchSummary) {
}
