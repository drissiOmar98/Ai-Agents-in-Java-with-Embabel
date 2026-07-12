package com.omar.blog_agent.model;

/**
 * Represents a blog post at some stage of the Embabel agent pipeline.
 *
 * <p>This is a sealed hierarchy so the compiler can verify exhaustiveness
 * wherever a {@code BlogPost} is pattern-matched. Each stage of the pipeline
 * ({@link com.omar.blog_agent.agent.BlogWriterAgent}) produces the next
 * permitted type, forming a linear progression:</p>
 *
 * <pre>
 * ResearchedTopic -&gt; DraftPost -&gt; ReviewedPost -&gt; FinalPost -&gt; PublishedPost
 * </pre>
 *
 * <p>Note that {@link ResearchedTopic} is not part of this sealed hierarchy
 * since it does not yet have a title/content shape; it is the raw research
 * output that seeds the first draft.</p>
 */
public sealed interface BlogPost permits DraftPost, ReviewedPost, FinalPost, PublishedPost {

    /** @return the current title of the post */
    String title();

    /** @return the current Markdown content of the post */
    String content();
}
