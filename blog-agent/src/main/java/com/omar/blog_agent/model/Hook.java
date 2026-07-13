package com.omar.blog_agent.model;

/**
 * The post's opening paragraph, written to pull a reader in before the
 * body of the draft is generated.
 *
 * <p>Writing the hook as its own step &mdash; separate from the rest of the
 * draft &mdash; gives it focused attention on a single job: earning the
 * next sentence. {@link com.omar.blog_agent.agent.BlogWriterAgent#writeDraft}
 * then continues from it rather than generating an opening as an
 * afterthought buried in a long prompt.</p>
 *
 * @param openingParagraph the hook text, meant to be the first paragraph of the draft
 */
public record Hook(String openingParagraph) {
}
