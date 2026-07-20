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

    /**
     * Extracts structured requirements from the job posting portion of the
     * user's input.
     *
     * @param userInput free-text input expected to contain both the job
     *                  posting and the candidate's background
     * @param context   Embabel's operation context, providing access to the LLM
     * @return the job's title, company, required/nice-to-have skills, and seniority level
     */
    @Action
    public JobRequirements extractJobRequirements(UserInput userInput, OperationContext context) {
        return context.ai()
                .withDefaultLlm()
                .createObjectIfPossible(
                        """
                        The following text contains a job posting, possibly alongside a
                        candidate's background. Extract only the job posting details:

                        %s

                        Identify the job title, company (use "the company" if not named),
                        explicitly required skills/technologies, nice-to-have/preferred
                        skills, and the apparent seniority level.
                        Create a JobRequirements from these details.
                        """.formatted(userInput.getContent()),
                        JobRequirements.class
                );
    }

    /**
     * Extracts the candidate's profile from the candidate-background
     * portion of the user's input.
     *
     * @param userInput free-text input expected to contain both the job
     *                  posting and the candidate's background
     * @param context   Embabel's operation context, providing access to the LLM
     * @return the candidate's skills, experience level, and background summary
     * @throws CandidateProfileIncompleteException if no candidate skills could be identified at all
     */
    @Action
    public CandidateProfile extractCandidateProfile(UserInput userInput, OperationContext context) {
        CandidateProfile profile = context.ai()
                .withDefaultLlm()
                .createObjectIfPossible(
                        """
                        The following text contains a candidate's background, possibly
                        alongside a job posting. Extract only the candidate's details:

                        %s

                        Identify their skills/technologies, a short free-text description
                        of their experience level, and a concise summary of their
                        professional background.
                        Create a CandidateProfile from these details.
                        """.formatted(userInput.getContent()),
                        CandidateProfile.class
                );

        if (profile == null || profile.skills() == null || profile.skills().isEmpty()) {
            throw new CandidateProfileIncompleteException(
                    "Could not identify any candidate skills from the submitted background text");
        }
        return profile;
    }

    /**
     * Compares the candidate's profile against the job's requirements to
     * produce a skill match/gap analysis and an overall fit score.
     *
     * @param jobRequirements  the output of {@link #extractJobRequirements}
     * @param candidateProfile the output of {@link #extractCandidateProfile}
     * @param context          Embabel's operation context, providing access to the LLM
     * @return matching skills, missing skills, a fit score, and a brief assessment
     */
    @Action
    public CandidateMatch matchCandidateProfile(JobRequirements jobRequirements,
                                                  CandidateProfile candidateProfile, OperationContext context) {
        return context.ai()
                .withDefaultLlm()
                .createObjectIfPossible(
                        """
                        Job requires: %s (required), %s (nice-to-have), seniority: %s
                        Candidate has: %s (%s)

                        Identify which candidate skills satisfy a required or nice-to-have
                        skill, which required/nice-to-have skills the candidate does not
                        demonstrate, an honest overall fit score from 0-100, and a brief
                        assessment explaining that score.
                        Create a CandidateMatch from this analysis.
                        """.formatted(
                                String.join(", ", jobRequirements.requiredSkills()),
                                String.join(", ", jobRequirements.niceToHaveSkills()),
                                jobRequirements.seniorityLevel(),
                                String.join(", ", candidateProfile.skills()),
                                candidateProfile.yearsOfExperience()
                        ),
                        CandidateMatch.class
                );
    }


}
