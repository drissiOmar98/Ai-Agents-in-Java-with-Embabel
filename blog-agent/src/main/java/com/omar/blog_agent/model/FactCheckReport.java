package com.omar.blog_agent.model;

import java.util.List;

/**
 * The output of verifying a draft's technical claims against web sources.
 *
 * <p>Fed into
 * {@link com.omar.blog_agent.agent.BlogWriterAgent#reviewDraft} so the
 * reviewer step corrects anything flagged here, rather than trusting the
 * draft's claims at face value.</p>
 *
 * @param findings  one entry per claim that was checked
 * @param hasIssues {@code true} if any finding has a verdict other than
 *                  {@code VERIFIED}, provided as a convenience so callers
 *                  don't need to scan {@code findings} themselves
 */
public record FactCheckReport(List<FactCheckFinding> findings, boolean hasIssues) {
}
