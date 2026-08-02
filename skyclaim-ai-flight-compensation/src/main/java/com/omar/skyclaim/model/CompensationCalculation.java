package com.omar.skyclaim.model;

/**
 * The compensation amount EU Regulation 261/2004 provides for, computed by
 * rule rather than estimated.
 *
 * <p>Produced by
 * {@link com.omar.skyclaim.agent.DisruptionAnalysisAgent#calculateCompensation}
 * using {@link com.omar.skyclaim.tool.Eu261CompensationRuleTool} for the actual
 * classification &mdash; the compensation tier for a given distance and
 * delay is fixed by the regulation itself, not a judgment call, so it's
 * decided in plain Java before the LLM is ever involved. The LLM's only
 * job afterward is phrasing a short explanation of the already-decided
 * amount.</p>
 *
 * <p>This amount reflects only the distance/delay rule. It does
 * <strong>not</strong> account for extraordinary-circumstance exemptions
 * &mdash; that judgment happens separately in
 * {@link com.omar.skyclaim.agent.DisruptionAnalysisAgent#assessEligibility}.</p>
 *
 * @param amountEur   the compensation amount in EUR the distance/delay rule provides for,
 *                    or {@code 0} if the delay falls below the regulation's threshold
 * @param ruleApplied which rule tier applied, e.g. {@code "short-haul, delay >= 3h"}
 * @param explanation a short, plain-language explanation of the computed amount
 */
public record CompensationCalculation(double amountEur, String ruleApplied, String explanation) {
}
