package com.omar.skyclaim.model;

import java.util.List;

/**
 * The traveler's immediate "duty of care" entitlements &mdash; meals,
 * accommodation, and communication the airline owes during a long delay or
 * cancellation.
 *
 * <p>This is a goal output of
 * {@link com.omar.skyclaim.agent.CareEntitlementsAgent#summarizeCareEntitlements}.
 * Critically, these entitlements under EU261 apply regardless of whether
 * the airline can claim "extraordinary circumstances" to avoid paying cash
 * compensation &mdash; care obligations and compensation eligibility are
 * legally separate, and this summary is written to make that distinction
 * clear so a traveler doesn't assume "the airline said it wasn't their
 * fault" means they're owed nothing at all right now.</p>
 *
 * @param entitlements what the traveler can reasonably ask for at the airport right now,
 *                     capped at {@link com.omar.skyclaim.config.SkyClaimProperties#maxCareEntitlements()}
 * @param note         a brief clarification that these apply independently of the
 *                     separate cash compensation eligibility question
 */
public record CareEntitlementsSummary(List<String> entitlements, String note) {
}
