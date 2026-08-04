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


}
