package com.omar.skyclaim.model;

import java.util.List;

/**
 * Practical next steps for a traveler dealing with a disrupted flight,
 * beyond the compensation claim itself.
 *
 * <p>This is a goal output of
 * {@link com.omar.skyclaim.agent.RebookingAdvisorAgent#generateRebookingAdvice}.</p>
 *
 * @param steps           concrete, ordered next steps, capped at
 *                        {@link com.skyclaim.config.SkyClaimProperties#maxRebookingSteps()}
 * @param evidenceToKeep  what documentation the traveler should preserve
 *                        (boarding passes, receipts, screenshots of the disruption notice)
 *                        in case the claim is disputed
 */
public record RebookingAdvice(List<String> steps, String evidenceToKeep) {
}
