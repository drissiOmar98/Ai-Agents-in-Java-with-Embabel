package com.omar.careerlens_ai_agent.agent;


import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.OperationContext;
import com.embabel.agent.domain.io.UserInput;
import com.omar.careerlens_ai_agent.config.CareerForgeProperties;
import com.omar.careerlens_ai_agent.exception.CandidateProfileIncompleteException;
import com.omar.careerlens_ai_agent.model.*;
import com.omar.careerlens_ai_agent.persona.Personas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Embabel agent that turns a job posting and a candidate's background
 * (submitted together as free-text input) into a tailored resume highlight
 * set and an ATS-aware cover letter.
 *
 * <p>This class owns the core extraction-through-cover-letter spine.
 * {@link ApplicationQualityAgent} contributes an independent ATS scoring
 * check that feeds back into {@link #writeCoverLetter}, and
 * {@link InterviewPrepAgent} contributes a downstream interview-prep goal
 * reachable from the same {@link CandidateMatch}.</p>
 *
 * <p>Pipeline shape:</p>
 *
 * <pre>
 * UserInput
 *   ├──&gt; extractJobRequirements  -&gt; JobRequirements
 *   └──&gt; extractCandidateProfile -&gt; CandidateProfile
 *            │
 *            ▼
 *   matchCandidateProfile -&gt; CandidateMatch
 *            │
 *            ▼
 *   tailorResumeHighlights -&gt; ResumeHighlights
 *            │
 *            ▼
 *   [ApplicationQualityAgent#scoreAtsMatch -&gt; AtsScoreReport]
 *            │
 *            ▼
 *   writeCoverLetter  🎯 GOAL  -&gt; CoverLetter
 *
 *   (CandidateMatch + JobRequirements also feed
 *    InterviewPrepAgent#generateInterviewQuestions  🎯 GOAL)
 * </pre>
 */
@Agent(
        name = "job-application-assistant",
        description = "Tailors a resume and cover letter to a specific job posting",
        version = "1.0.0",
        beanName = "applicationAgent"
)
public class ApplicationAgent {

    private static final Logger log = LoggerFactory.getLogger(ApplicationAgent.class);

    private final CareerForgeProperties properties;

    /**
     * @param properties bound {@code career-forge.*} configuration
     */
    public ApplicationAgent(CareerForgeProperties properties) {
        this.properties = properties;
    }


}
