package com.omar.skyclaim.agent;

import com.omar.skyclaim.config.SkyClaimProperties;
import com.omar.skyclaim.model.CareEntitlementsSummary;
import com.omar.skyclaim.model.FlightDisruption;
import com.omar.skyclaim.persona.Personas;
import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.OperationContext;

import java.util.List;

/**
 * Downstream agent contributing an immediate care-entitlements goal to the
 * {@link DisruptionAnalysisAgent} pipeline.
 *
 * <p>Kept separate from {@link DisruptionAnalysisAgent} because EU261's
 * "duty of care" obligations (Article 9: meals, accommodation,
 * communication) are legally distinct from the cash compensation
 * question — they apply during a long delay or cancellation regardless of
 * whether the airline can later claim extraordinary circumstances to avoid
 * paying compensation. Depending only on {@link FlightDisruption}, this
 * goal is reachable without needing eligibility to be assessed at all,
 * which matters because these entitlements are useful to know about
 * immediately, at the airport, before any compensation question is even
 * resolved.</p>
 */
@Agent(description = "Summarizes a traveler's immediate care entitlements during a flight disruption")
public class CareEntitlementsAgent {

    private final SkyClaimProperties properties;

    /**
     * @param properties bound {@code sky-claim.*} configuration (max care entitlements)
     */
    public CareEntitlementsAgent(SkyClaimProperties properties) {
        this.properties = properties;
    }

    /**
     * Summarizes what the traveler can reasonably ask the airline for
     * right now, using the {@link Personas#TRAVEL_SUPPORT_AGENT} persona
     * so guidance reads as practical, immediate advice rather than legal
     * text.
     *
     * <p>Marked as its own {@link AchievesGoal}, reachable directly from
     * {@link FlightDisruption} — a traveler at the gate needs this
     * regardless of how the separate compensation question turns out.</p>
     *
     * @param disruption the output of {@link DisruptionAnalysisAgent#extractFlightDisruption}
     * @param context    Embabel's operation context, providing access to the LLM
     * @return practical care entitlements, capped at
     *         {@link SkyClaimProperties#maxCareEntitlements()}, plus a clarifying note
     */
    @AchievesGoal(description = "The traveler's immediate care entitlements (meals, hotel, communication)")
    @Action(description = "Summarize immediate care entitlements during the disruption")
    public CareEntitlementsSummary summarizeCareEntitlements(FlightDisruption disruption, OperationContext context) {
        return context.ai()
                .withDefaultLlm()
                .withPromptContributors(List.of(Personas.TRAVEL_SUPPORT_AGENT))
                .createObjectIfPossible(
                        """
                        Disruption: %s on %s %s, %.1f hours late (or cancelled).

                        List up to %d practical things the traveler can reasonably ask the
                        airline for right now, appropriate to this delay length (meals and
                        refreshments for shorter waits; hotel and transport if it involves
                        an overnight stay; free communication/calls). Be concrete about
                        what to ask for, not just "you may be entitled to assistance".

                        Include a brief note clarifying that these care entitlements apply
                        independently of the separate question of cash compensation
                        eligibility - the airline owes care regardless of whether they
                        later claim extraordinary circumstances for compensation purposes.
                        Create a CareEntitlementsSummary from these entitlements and note.
                        """.formatted(
                                disruption.disruptionType(), disruption.airline(), disruption.flightNumber(),
                                disruption.delayHoursAtArrival(), properties.maxCareEntitlements()
                        ),
                        CareEntitlementsSummary.class
                );
    }
}
