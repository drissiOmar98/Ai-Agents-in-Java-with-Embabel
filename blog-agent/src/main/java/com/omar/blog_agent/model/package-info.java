/**
 * Immutable data carriers that flow through the {@code BlogWriterAgent}
 * pipeline, from raw research to a fully published post.
 *
 * <p>{@link com.omar.blog_agent.model.BlogPost} is the sealed root of the
 * stages that share a title/content shape; see its Javadoc for the full
 * pipeline order.</p>
 */
package com.omar.blog_agent.model;
