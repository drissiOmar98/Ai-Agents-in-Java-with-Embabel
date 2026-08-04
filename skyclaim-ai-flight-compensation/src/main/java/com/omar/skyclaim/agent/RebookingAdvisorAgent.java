package com.omar.skyclaim.agent;

import com.omar.skyclaim.config.SkyClaimProperties;
import com.omar.skyclaim.model.EligibilityAssessment;
import com.omar.skyclaim.model.FlightDisruption;
import com.omar.skyclaim.model.RebookingAdvice;
import com.omar.skyclaim.persona.Personas;
import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.OperationContext;

import java.util.List;

/**
 * Downstream agent contributing a rebooking-and-next-steps goal to the
 * {@link DisruptionAnalysisAgent} pipeline.
 *
 * <p>Kept separate from {@link DisruptionAnalysisAgent} because practical
 * "what do I do right now" advice is a distinct concern from the
 * compensation claim itself — a traveler stuck at an airport needs this
 * immediately, and it's useful independently of whether a claim letter is
 * ever drafted.</p>
 */
@Agent(description = "Gives practical rebooking and next-step advice for a disrupted flight")
public class RebookingAdvisorAgent {

    private final SkyClaimProperties properties;

    /**
     * @param properties bound {@code sky-claim.*} configuration (max rebooking steps)
     */
    public RebookingAdvisorAgent(SkyClaimProperties properties) {
        this.properties = properties;
    }

    /**
     * Generates practical next steps for the traveler, using the
     * {@link Personas#TRAVEL_SUPPORT_AGENT} persona so advice reads as
     * calm, concrete guidance rather than legal boilerplate.
     *
     * <p>Marked as its own {@link AchievesGoal}, reachable once
     * {@link FlightDisruption} and {@link EligibilityAssessment} exist,
     * independently of whether a claim letter is ever generated.</p>
     *
     * @param disruption            the output of {@link DisruptionAnalysisAgent#extractFlightDisruption}
     * @param eligibilityAssessment the output of {@link DisruptionAnalysisAgent#assessEligibility}
     * @param context               Embabel's operation context, providing access to the LLM
     * @return ordered next steps, capped at {@link SkyClaimProperties#maxRebookingSteps()},
     *         plus guidance on what evidence to preserve
     */
    @AchievesGoal(description = "Practical rebooking and next-step advice for the traveler")
    @Action(description = "Give practical rebooking and next-step advice")
    public RebookingAdvice generateRebookingAdvice(FlightDisruption disruption,
                                                     EligibilityAssessment eligibilityAssessment, OperationContext context) {
        return context.ai()
                .withDefaultLlm()
                .withPromptContributors(List.of(Personas.TRAVEL_SUPPORT_AGENT))
                .createObjectIfPossible(
                        """
                        Disruption: %s on %s %s, %.1f hours late.
                        Eligibility outlook: %s

                        Give up to %d concrete, ordered next steps for the traveler right
                        now - covering rebooking options (same airline, alternate
                        routing, refund instead of rebooking), how to raise the
                        compensation claim if eligibility looks favorable, and a realistic
                        timeline to expect a response.

                        Also list what evidence the traveler should keep in case the claim
                        is disputed (boarding pass, any written notice of the delay/
                        cancellation, receipts for anything paid out of pocket).
                        Create a RebookingAdvice from these steps and evidence guidance.
                        """.formatted(
                                disruption.disruptionType(), disruption.airline(), disruption.flightNumber(),
                                disruption.delayHoursAtArrival(),
                                eligibilityAssessment.likelyEligible() ? "likely eligible" : "uncertain/unlikely",
                                properties.maxRebookingSteps()
                        ),
                        RebookingAdvice.class
                );
    }
}
