package com.omar.festivalscout_ai_agent.exception;

/**
 * Thrown when the agent cannot resolve any lineup information for the
 * requested festival &mdash; e.g. web search turns up nothing usable, or
 * the festival name/edition couldn't be identified.
 *
 * <p>Used in place of a generic assertion failure so callers get a
 * meaningful, domain-specific error rather than silently proceeding to
 * generate artist picks with no real lineup to match against.</p>
 */
public class LineupUnavailableException extends RuntimeException {

    /**
     * @param message a description of which festival's lineup could not be resolved
     */
    public LineupUnavailableException(String message) {
        super(message);
    }
}
