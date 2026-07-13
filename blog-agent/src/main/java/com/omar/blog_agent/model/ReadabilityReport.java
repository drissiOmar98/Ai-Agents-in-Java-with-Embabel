package com.omar.blog_agent.model;

import java.util.List;

/**
 * A readability assessment for the reviewed post, combining a
 * deterministically computed Flesch Reading Ease score with LLM-generated
 * simplification suggestions.
 *
 * <p>Produced by
 * {@link com.omar.blog_agent.agent.ContentQualityAgent#scoreReadability}.
 * This is advisory: suggestions are surfaced (and recorded in front matter)
 * rather than automatically applied to the post's content, so the
 * publishing pipeline stays predictable.</p>
 *
 * @param readingLevel      a human label for the score, e.g. {@code "Standard"},
 *                          {@code "Fairly Difficult"}
 * @param fleschReadingEase the raw Flesch Reading Ease score (roughly 0-100;
 *                          higher is easier to read)
 * @param suggestions       specific, actionable simplification suggestions
 */
public record ReadabilityReport(String readingLevel, int fleschReadingEase, List<String> suggestions) {
}
