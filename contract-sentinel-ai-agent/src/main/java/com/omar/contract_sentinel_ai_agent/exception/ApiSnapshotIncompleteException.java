package com.omar.contract_sentinel_ai_agent.exception;

/**
 * Thrown when the agent cannot extract any endpoints at all for either the
 * previous or current API version from the user's input.
 *
 * <p>Used in place of a generic assertion failure so callers get a
 * meaningful, domain-specific error rather than silently proceeding to
 * diff two empty contracts and reporting "no changes found."</p>
 */
public class ApiSnapshotIncompleteException extends RuntimeException {

    /**
     * @param message a description of which snapshot (previous/current) could not be extracted
     */
    public ApiSnapshotIncompleteException(String message) {
        super(message);
    }
}
