package com.omar.skyclaim.exception;

/**
 * Thrown when the agent cannot identify the core facts of a flight
 * disruption (whether it was delayed or cancelled, and by how long) from
 * the user's input.
 *
 * <p>Used in place of a generic assertion failure so callers get a
 * meaningful, domain-specific error rather than silently proceeding to
 * calculate compensation for a disruption that was never actually
 * described.</p>
 */
public class DisruptionDetailsIncompleteException extends RuntimeException {

    /**
     * @param message a description of which disruption details could not be identified
     */
    public DisruptionDetailsIncompleteException(String message) {
        super(message);
    }
}
