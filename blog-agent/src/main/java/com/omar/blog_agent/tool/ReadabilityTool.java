package com.omar.blog_agent.tool;


import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.embabel.agent.api.annotation.LlmTool;

/**
 * Exposes a deterministic Flesch Reading Ease calculation as an LLM tool.
 *
 * <p>Registered with
 * {@link com.omar.blog_agent.agent.ContentQualityAgent#scoreReadability}
 * so the model grounds its "reading level" label and simplification
 * suggestions in an actual computed score rather than an eyeballed guess.</p>
 */
@Component
public class ReadabilityTool {

    private static final Pattern SENTENCE_BOUNDARY = Pattern.compile("[.!?]+");
    private static final Pattern WORD = Pattern.compile("[A-Za-z]+");
    private static final Pattern VOWEL_GROUP = Pattern.compile("[aeiouyAEIOUY]+");

    /**
     * Computes the Flesch Reading Ease score for a piece of text, along
     * with a human-readable level label.
     *
     * <p>Formula: {@code 206.835 - 1.015 * (words / sentences) - 84.6 * (syllables / words)}.
     * Syllables are approximated by counting vowel groups per word, which
     * is the standard approximation used by most readability tools and is
     * accurate enough to guide editorial suggestions.</p>
     *
     * @param text the full text of the blog post to analyze (Markdown is fine;
     *             code fences are not stripped, so heavily code-heavy posts
     *             will skew scores lower than their prose actually reads)
     * @return a summary string, e.g. {@code "Score: 62.3 (Standard)"};
     *         returns {@code "Score: 0.0 (Unknown)"} for null, blank, or
     *         otherwise unscorable input
     */
    @LlmTool(description = "Calculate the Flesch Reading Ease score for a piece of text and return a " +
            "human-readable reading-level label alongside the numeric score.")
    public String calculateReadability(
            @LlmTool.Param(description = "The full text of the blog post to analyze") String text
    ) {
        if (text == null || text.isBlank()) {
            return "Score: 0.0 (Unknown)";
        }

        int sentences = Math.max(1, countMatches(SENTENCE_BOUNDARY, text));
        int words = Math.max(1, countMatches(WORD, text));
        int syllables = Math.max(words, countSyllables(text));

        double score = 206.835
                - 1.015 * ((double) words / sentences)
                - 84.6 * ((double) syllables / words);

        return String.format("Score: %.1f (%s)", score, levelFor(score));
    }

    private int countMatches(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    private int countSyllables(String text) {
        Matcher words = WORD.matcher(text);
        int syllables = 0;
        while (words.find()) {
            syllables += Math.max(1, countMatches(VOWEL_GROUP, words.group()));
        }
        return syllables;
    }

    private String levelFor(double score) {
        if (score >= 90) return "Very Easy";
        if (score >= 80) return "Easy";
        if (score >= 70) return "Fairly Easy";
        if (score >= 60) return "Standard";
        if (score >= 50) return "Fairly Difficult";
        if (score >= 30) return "Difficult";
        return "Very Difficult";
    }
}
