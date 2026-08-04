/**
 * Immutable data carriers that flow through the SkyClaim pipeline, from a
 * raw disruption description to a claim letter and its companion guidance.
 *
 * <p>{@link com.omar.skyclaim.model.FlightDisruption} and
 * {@link com.omar.skyclaim.model.TravelerContext} are extracted independently
 * from the user's input. The disruption drives a deterministically
 * computed {@link com.omar.skyclaim.model.CompensationCalculation}, which
 * together with the traveler context feeds a judgment-based
 * {@link com.omar.skyclaim.model.EligibilityAssessment}. That assessment drives
 * the primary {@link com.omar.skyclaim.model.ClaimLetter} goal.
 * {@link com.omar.skyclaim.model.CareEntitlementsSummary} and
 * {@link com.omar.skyclaim.model.RebookingAdvice} are independent downstream
 * goals derived from the same underlying facts.</p>
 */
package com.omar.skyclaim.model;
