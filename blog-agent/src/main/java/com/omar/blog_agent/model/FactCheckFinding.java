package com.omar.blog_agent.model;

/**
 * The verdict on a single factual or technical claim extracted from a
 * draft, as assessed by
 * {@link com.omar.blog_agent.agent.ContentQualityAgent#factCheckDraft}.
 *
 * @param claim       the specific claim as it appears (or is paraphrased) in the draft
 * @param verdict     one of {@code VERIFIED}, {@code UNVERIFIED}, or {@code INCORRECT}
 * @param explanation why the verdict was reached, including a source when one was found
 */
public record FactCheckFinding(String claim, String verdict, String explanation) {
}
