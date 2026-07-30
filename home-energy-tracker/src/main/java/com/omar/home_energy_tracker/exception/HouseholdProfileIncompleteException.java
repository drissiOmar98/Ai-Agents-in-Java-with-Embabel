package com.omar.home_energy_tracker.exception;

/**
 * Thrown when the agent cannot identify any appliances at all from the
 * user's input.
 *
 * <p>Used in place of a generic assertion failure so callers get a
 * meaningful, domain-specific error rather than silently proceeding to
 * calculate a cost breakdown over an empty appliance list and reporting a
 * $0 bill.</p>
 */
public class HouseholdProfileIncompleteException extends RuntimeException {

    /**
     * @param message a description of why the household's appliance profile could not be extracted
     */
    public HouseholdProfileIncompleteException(String message) {
        super(message);
    }
}
