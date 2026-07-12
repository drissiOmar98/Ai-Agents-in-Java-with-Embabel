package com.omar.blog_agent.tool;

import com.embabel.agent.api.annotation.LlmTool;
import org.springframework.stereotype.Component;

/**
 * Exposes a simple word-count / reading-time calculation as an LLM tool.
 *
 * <p>Registered with the {@code blog-post-front-matter} action in
 * {@link com.omar.blog_agent.agent.BlogWriterAgent#addFrontMatter} so the
 * model can compute an accurate {@code readTime} instead of guessing.</p>
 */
@Component
public class ReadingStatsTool {

    /** Assumed average adult reading speed, used to estimate read time. */
    private static final int WORDS_PER_MINUTE = 200;

    /**
     * Calculates word count and estimated reading time for the given text.
     *
     * @param text the full text of the blog post to analyze
     * @return a human-readable summary, e.g. {@code "842 words, 5 min read"};
     *         returns {@code "0 words, 0 min read"} for null or blank input
     */
    @LlmTool(description = "Calculate word count and estimated reading time (in minutes) for a piece of text. " +
            "Reading speed is assumed to be 200 words per minute.")
    public String calculateReadingStats(
            @LlmTool.Param(description = "The full text of the blog post to analyze") String text
    ) {
        if (text == null || text.isBlank()) {
            return "0 words, 0 min read";
        }
        int words = text.trim().split("\\s+").length;
        int minutes = Math.max(1, (int) Math.ceil(words / (double) WORDS_PER_MINUTE));
        return String.format("%d words, %d min read", words, minutes);
    }
}
