package com.omar.ai_movie_research_agent.model;

/**
 * A short, spoiler-free synopsis of a movie's premise.
 *
 * <p>Deliberately scoped to the setup rather than the full plot, so it's
 * safe to show someone deciding whether to watch the film.</p>
 *
 * @param summary a 2-3 sentence, spoiler-free synopsis
 */
public record MoviePlotSummary(String summary) {
}
