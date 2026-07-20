package com.omar.careerlens_ai_agent.exception;

/**
 * Thrown when the agent cannot extract a usable candidate profile from the
 * user's input &mdash; specifically, when no skills could be identified at
 * all, which would make every downstream matching and generation step
 * meaningless.
 *
 * <p>Used in place of a generic assertion failure so callers get a
 * meaningful, domain-specific error explaining exactly what was missing
 * from the submitted background text.</p>
 */
public class CandidateProfileIncompleteException extends RuntimeException {

    /**
     * @param message a description of what candidate information was missing
     */
    public CandidateProfileIncompleteException(String message) {
        super(message);
    }
}
