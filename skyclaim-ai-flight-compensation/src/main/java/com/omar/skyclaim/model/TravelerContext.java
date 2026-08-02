package com.omar.skyclaim.model;

/**
 * Context about the traveler and booking, used to assess jurisdiction and
 * populate the claim letter.
 *
 * @param travelerName            the traveler's name, for the claim letter
 * @param departureCountry        the country the flight departed from
 * @param arrivalCountry          the country the flight arrived in (or was scheduled to)
 * @param airlineCountry          the airline's country of registration, if known
 * @param bookingReference        the booking/PNR reference, if the traveler provided one
 * @param ticketPriceUsd          the price paid for the ticket, if stated
 */
public record TravelerContext(
        String travelerName,
        String departureCountry,
        String arrivalCountry,
        String airlineCountry,
        String bookingReference,
        double ticketPriceUsd
) {
}
