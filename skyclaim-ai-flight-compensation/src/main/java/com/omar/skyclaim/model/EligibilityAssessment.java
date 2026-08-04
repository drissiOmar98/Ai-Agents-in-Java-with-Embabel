package com.omar.skyclaim.model;

/**
 * A judgment-based assessment of whether the traveler is likely eligible
 * for the compensation amount computed by
 * {@link com.omar.skyclaim.agent.DisruptionAnalysisAgent#calculateCompensation}.
 *
 * <p>Unlike the compensation amount itself, eligibility involves genuine
 * judgment calls the regulation doesn't reduce to a formula &mdash;
 * whether the flight falls under EU261 jurisdiction at all, and whether
 * the airline's stated reason plausibly qualifies as an "extraordinary
 * circumstance" that exempts it from paying (case law here is genuinely
 * nuanced: a mechanical fault from routine maintenance typically does
 * <em>not</em> qualify, while a bird strike or an air traffic control
 * strike typically does). This is exactly the kind of assessment where an
 * LLM's judgment is useful and a hardcoded rule would be wrong.</p>
 *
 * <p><strong>This is informational guidance, not legal advice.</strong>
 * {@code assessmentDisclaimer} should always be surfaced to the
 * traveler alongside the assessment.</p>
 *
 * @param likelyEligible          the assessment's best-effort judgment on eligibility
 * @param jurisdictionBasis       why (or why not) this flight likely falls under EU261
 * @param extraordinaryCircumstanceAssessment whether the stated reason plausibly
 *                                exempts the airline, and why
 * @param assessmentDisclaimer    a clear statement that this is not legal advice and
 *                                the traveler should confirm with the airline or their
 *                                national enforcement body before relying on it
 */
public record EligibilityAssessment(
        boolean likelyEligible,
        String jurisdictionBasis,
        String extraordinaryCircumstanceAssessment,
        String assessmentDisclaimer
) {
}
