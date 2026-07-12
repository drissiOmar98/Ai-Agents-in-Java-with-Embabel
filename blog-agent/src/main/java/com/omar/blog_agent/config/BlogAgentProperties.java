package com.omar.blog_agent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Type-safe configuration properties for the blog agent, bound from the
 * {@code blog-agent} prefix in {@code application.yml}.
 *
 * <pre>{@code
 * blog-agent:
 *   output-dir: blog-posts
 *   number-of-keywords: 5
 * }</pre>
 *
 * @param outputDir         the directory published posts are written to; defaults to
 *                           {@code "blog-posts"} when blank or not provided
 * @param numberOfKeywords  the maximum number of SEO keywords to generate for the
 *                           front matter; defaults to {@code 5} when zero or negative
 */
@ConfigurationProperties(prefix = "blog-agent")
public record BlogAgentProperties(String outputDir, int numberOfKeywords) {

    /**
     * Compact constructor applying sensible defaults when values are
     * missing or invalid, so the application never fails to start due to
     * incomplete configuration.
     */
    public BlogAgentProperties {
        if (outputDir == null || outputDir.isBlank()) {
            outputDir = "blog-posts";
        }
        if (numberOfKeywords <= 0) {
            numberOfKeywords = 5;
        }
    }
}
