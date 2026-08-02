package com.example.skyclaim_ai_flight_compensation.model;

/**
 * The core facts of a flight disruption, extracted from the traveler's
 * free-text description.
 *
 * <p>This is the anchor fact for the pipeline: compensation calculation,
 * eligibility assessment, and care entitlements all depend on these
 * details.</p>
 *
 * @param airline                   the operating airline's name
 * @param flightNumber              the flight number, e.g. {@code "AF1234"}
 * @param departureAirport          the departure airport code or name
 * @param arrivalAirport            the arrival airport code or name
 * @param distanceKm                the great-circle distance between the two airports, in kilometers
 * @param disruptionType            one of {@code "DELAY"} or {@code "CANCELLATION"}
 * @param delayHoursAtArrival       how many hours late the flight actually arrived
 *                                  (or would have, for a cancellation with a known replacement)
 * @param reasonStatedByAirline     the reason the airline gave for the disruption, verbatim
 *                                  where possible, e.g. {@code "technical issue"}, {@code "weather"},
 *                                  {@code "air traffic control restrictions"}
 * @param noticeDaysBeforeDeparture for a cancellation, how many days before the scheduled
 *                                  departure the traveler was notified; {@code -1} if not stated
 */
public record FlightDisruption(
        String airline,
        String flightNumber,
        String departureAirport,
        String arrivalAirport,
        double distanceKm,
        String disruptionType,
        double delayHoursAtArrival,
        String reasonStatedByAirline,
        int noticeDaysBeforeDeparture
) {
}
