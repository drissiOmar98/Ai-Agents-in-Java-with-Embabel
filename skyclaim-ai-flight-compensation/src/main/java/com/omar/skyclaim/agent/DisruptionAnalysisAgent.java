package com.omar.skyclaim.agent;

import com.omar.skyclaim.exception.DisruptionDetailsIncompleteException;
import com.omar.skyclaim.model.*;
import com.omar.skyclaim.persona.Personas;
import com.omar.skyclaim.tool.Eu261CompensationRuleTool;
import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.OperationContext;
import com.embabel.agent.domain.io.UserInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Embabel agent that helps a traveler understand what a flight delay or
 * cancellation may entitle them to, and drafts a compensation claim.
 *
 * <p>This class owns the core extraction-through-claim-letter spine.
 * {@link CareEntitlementsAgent} and {@link RebookingAdvisorAgent}
 * contribute independent downstream goals reachable from the same
 * disruption facts.</p>
 *
 * <p><strong>Informational guidance, not legal advice</strong> — every
 * eligibility judgment this agent produces is a starting point, not a
 * final determination; actual outcomes depend on facts it cannot verify,
 * and travelers should confirm with the airline or their national
 * enforcement body before relying on it. See {@link EligibilityAssessment}
 * for how this is surfaced.</p>
 *
 * <p>Pipeline shape:</p>
 *
 * <pre>
 * UserInput
 *   ├──&gt; extractFlightDisruption -&gt; FlightDisruption
 *   └──&gt; extractTravelerContext  -&gt; TravelerContext
 *            │
 *            ▼
 *   calculateCompensation (deterministic rule) -&gt; CompensationCalculation
 *            │
 *            ▼
 *   assessEligibility (judgment call) -&gt; EligibilityAssessment
 *            │
 *            ▼
 *   draftClaimLetter  🎯 GOAL  -&gt; ClaimLetter
 *
 *   (FlightDisruption also feeds
 *    CareEntitlementsAgent#summarizeCareEntitlements  🎯 GOAL
 *    RebookingAdvisorAgent#generateRebookingAdvice     🎯 GOAL)
 * </pre>
 */
@Agent(
        name = "sky-claim",
        description = "Assesses flight disruption compensation eligibility and drafts a claim letter",
        version = "1.0.0",
        beanName = "disruptionAnalysisAgent"
)
public class DisruptionAnalysisAgent {

    private static final Logger log = LoggerFactory.getLogger(DisruptionAnalysisAgent.class);

    private final Eu261CompensationRuleTool eu261CompensationRuleTool;

    /**
     * @param eu261CompensationRuleTool the plain rule engine used to decide the compensation
     *                                  amount; called directly in Java, not exposed to the LLM as a tool
     */
    public DisruptionAnalysisAgent(Eu261CompensationRuleTool eu261CompensationRuleTool) {
        this.eu261CompensationRuleTool = eu261CompensationRuleTool;
    }

    /**
     * Extracts the core facts of the flight disruption from the
     * traveler's free-text description.
     *
     * @param userInput free-text input describing the flight disruption and, optionally,
     *                  the traveler's own context
     * @param context   Embabel's operation context, providing access to the LLM
     * @return the disruption's route, distance, delay, and stated reason
     * @throws DisruptionDetailsIncompleteException if no disruption (delay/cancellation
     *         type and duration) could be identified
     */
    @Action
    public FlightDisruption extractFlightDisruption(UserInput userInput, OperationContext context) {
        FlightDisruption disruption = context.ai()
                .withDefaultLlm()
                .createObjectIfPossible(
                        """
                        The following text describes a flight disruption. Extract the
                        disruption details:
                        %s

                        Identify the airline, flight number, departure/arrival airports,
                        approximate distance in kilometers between them, whether this was
                        a DELAY or CANCELLATION, how many hours late the flight arrived (or
                        would have, for a cancellation with a known replacement), the
                        reason the airline gave (verbatim where possible), and for a
                        cancellation, how many days' notice was given (-1 if not stated).
                        Create a FlightDisruption from these details.
                        """.formatted(userInput.getContent()),
                        FlightDisruption.class
                );

        if (disruption == null || disruption.disruptionType() == null || disruption.disruptionType().isBlank()) {
            throw new DisruptionDetailsIncompleteException(
                    "Could not identify the flight disruption (delay/cancellation and duration) from the input");
        }
        return disruption;
    }

    /**
     * Extracts context about the traveler and booking from the input.
     *
     * @param userInput free-text input describing the flight disruption and, optionally,
     *                  the traveler's own context
     * @param context   Embabel's operation context, providing access to the LLM
     * @return the traveler's name, route countries, and booking details, where stated
     */
    @Action
    public TravelerContext extractTravelerContext(UserInput userInput, OperationContext context) {
        return context.ai()
                .withDefaultLlm()
                .createObjectIfPossible(
                        """
                        The following text describes a flight disruption, possibly
                        alongside details about the traveler: %s

                        Identify the traveler's name, the departure and arrival countries,
                        the airline's country of registration if known, any booking
                        reference mentioned, and the ticket price if stated. Leave fields
                        blank/zero rather than guessing if not stated.
                        Create a TravelerContext from these details.
                        """.formatted(userInput.getContent()),
                        TravelerContext.class
                );
    }

    /**
     * Calculates the EU261 Article 7 compensation amount using
     * {@link Eu261CompensationRuleTool}, so the figure quoted to the
     * traveler is the amount the regulation actually specifies, not an
     * LLM's approximation of it.
     *
     * @param disruption the output of {@link #extractFlightDisruption}
     * @param context    Embabel's operation context, providing access to the LLM
     * @return the computed compensation amount, rule tier, and a plain-language explanation
     */
    @Action(description = "Calculate the EU261 compensation amount by rule")
    public CompensationCalculation calculateCompensation(FlightDisruption disruption, OperationContext context) {
        // The amount is decided here, in plain Java, before the LLM is ever
        // invoked for this step - there is exactly one correct figure per
        // the regulation's distance/delay table.
        double amount = eu261CompensationRuleTool.calculateCompensation(
                disruption.distanceKm(), disruption.delayHoursAtArrival());
        String ruleTier = eu261CompensationRuleTool.describeRuleTier(
                disruption.distanceKm(), disruption.delayHoursAtArrival());

        String explanation = context.ai()
                .withDefaultLlm()
                .createObjectIfPossible(
                        """
                        A compensation amount of €%.0f was computed for a flight of
                        approximately %.0f km that arrived %.1f hours late, under this
                        rule tier: %s

                        Write one short sentence explaining this amount in plain language,
                        referencing the actual distance and delay. Do not suggest a
                        different amount - the calculation is already correct; just
                        explain it.
                        """.formatted(amount, disruption.distanceKm(), disruption.delayHoursAtArrival(), ruleTier),
                        String.class
                );

        return new CompensationCalculation(amount, ruleTier, explanation);
    }

    /**
     * Assesses whether the traveler is likely eligible for the computed
     * compensation amount &mdash; a genuine judgment call the regulation
     * doesn't reduce to a formula, covering jurisdiction and whether the
     * airline's stated reason plausibly qualifies as an extraordinary
     * circumstance.
     *
     * <p>Uses the {@link Personas#CONSUMER_RIGHTS_ADVOCATE} persona, which
     * is explicitly instructed to include a clear non-legal-advice
     * disclaimer.</p>
     *
     * @param disruption              the output of {@link #extractFlightDisruption}
     * @param travelerContext         the output of {@link #extractTravelerContext}
     * @param compensationCalculation the output of {@link #calculateCompensation}
     * @param context                 Embabel's operation context, providing access to the LLM
     * @return the eligibility judgment, jurisdiction basis, extraordinary-circumstance
     *         assessment, and a required disclaimer
     */
    @Action
    public EligibilityAssessment assessEligibility(FlightDisruption disruption, TravelerContext travelerContext,
                                                      CompensationCalculation compensationCalculation, OperationContext context) {
        return context.ai()
                .withDefaultLlm()
                .withPromptContributors(List.of(Personas.CONSUMER_RIGHTS_ADVOCATE))
                .createObjectIfPossible(
                        """
                        Flight: %s %s, %s to %s
                        Departure country: %s, Arrival country: %s, Airline country: %s
                        Disruption: %s, %.1f hours late, reason given: "%s"
                        Computed compensation if eligible: €%.0f (%s)

                        Assess:
                        1. Jurisdiction: does this flight plausibly fall under EU261
                           (departing an EU/EEA airport, or arriving one on an EU/EEA
                           carrier)? Explain briefly based on what's known.
                        2. Extraordinary circumstances: does the airline's stated reason
                           plausibly qualify as an extraordinary circumstance that would
                           exempt them from paying (e.g. weather, air traffic control
                           restrictions, security threats, strikes outside the airline's
                           control typically qualify; technical/mechanical issues from
                           routine maintenance typically do NOT)?
                        3. Give an overall likelyEligible judgment.

                        Always include a clear assessmentDisclaimer stating this is
                        informational guidance, not legal advice, and the traveler should
                        confirm with the airline or their national enforcement body.
                        Create an EligibilityAssessment from this analysis.
                        """.formatted(
                                disruption.airline(), disruption.flightNumber(),
                                disruption.departureAirport(), disruption.arrivalAirport(),
                                travelerContext.departureCountry(), travelerContext.arrivalCountry(),
                                travelerContext.airlineCountry(),
                                disruption.disruptionType(), disruption.delayHoursAtArrival(),
                                disruption.reasonStatedByAirline(),
                                compensationCalculation.amountEur(), compensationCalculation.ruleApplied()
                        ),
                        EligibilityAssessment.class
                );
    }


}
