package com.omar.trip_planner.exception;

/**
 * Thrown when the agent cannot identify any planned destinations at all
 * from the user's input.
 *
 * <p>Used in place of a generic assertion failure so callers get a
 * meaningful, domain-specific error rather than silently proceeding to
 * estimate transit for an empty trip and reporting it as perfectly
 * feasible.</p>
 */
public class TripDetailsIncompleteException extends RuntimeException {

    /**
     * @param message a description of why the trip's destinations could not be identified
     */
    public TripDetailsIncompleteException(String message) {
        super(message);
    }
}
