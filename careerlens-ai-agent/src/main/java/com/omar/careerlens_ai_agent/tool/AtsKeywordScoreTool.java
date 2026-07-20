package com.omar.careerlens_ai_agent.tool;

import com.embabel.agent.api.annotation.LlmTool;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Exposes a deterministic ATS (Applicant Tracking System) keyword-matching
 * calculation as an LLM tool.
 *
 * <p>Registered with
 * {@link com.omar.careerlens_ai_agent.agent.ApplicationQualityAgent#scoreAtsMatch} so the
 * match percentage and missing-keyword list are grounded in an actual
 * case-insensitive substring match rather than an LLM's estimate — the same
 * "give the model a real number" pattern used by
 * {@code ReadabilityTool}/{@code ReadingStatsTool} in sibling projects.</p>
 */
@Component
public class AtsKeywordScoreTool {

    /**
     * Calculates what percentage of the given required keywords appear
     * (case-insensitively, as substrings) in the given resume text, and
     * lists which ones do not.
     *
     * @param resumeText      the resume/highlight text to search within
     * @param requiredKeywordsCsv a comma-separated list of required keywords to look for
     * @return a summary string, e.g. {@code "Match: 75% (3/4 found) | Missing: Kubernetes"};
     *         returns {@code "Match: 0% (no keywords provided)"} if the keyword list is blank
     */
    @LlmTool(description = "Calculate the percentage of required keywords (comma-separated) that appear in a " +
            "piece of resume text, and list which required keywords are missing.")
    public String calculateAtsScore(
            @LlmTool.Param(description = "The resume or highlight text to check") String resumeText,
            @LlmTool.Param(description = "Comma-separated list of required keywords") String requiredKeywordsCsv
    ) {
        if (requiredKeywordsCsv == null || requiredKeywordsCsv.isBlank()) {
            return "Match: 0% (no keywords provided)";
        }

        String haystack = resumeText == null ? "" : resumeText.toLowerCase(Locale.ROOT);
        String[] keywords = requiredKeywordsCsv.split(",");

        List<String> missing = new ArrayList<>();
        int found = 0;
        for (String rawKeyword : keywords) {
            String keyword = rawKeyword.trim();
            if (keyword.isEmpty()) {
                continue;
            }
            if (haystack.contains(keyword.toLowerCase(Locale.ROOT))) {
                found++;
            } else {
                missing.add(keyword);
            }
        }

        int total = keywords.length;
        int percent = total == 0 ? 0 : (int) Math.round((found / (double) total) * 100);

        String missingSummary = missing.isEmpty() ? "none" : String.join(", ", missing);
        return String.format("Match: %d%% (%d/%d found) | Missing: %s", percent, found, total, missingSummary);
    }
}
