package com.omar.careerlens_ai_agent.agent;


import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.OperationContext;
import com.omar.careerlens_ai_agent.config.CareerForgeProperties;
import com.omar.careerlens_ai_agent.model.CandidateMatch;
import com.omar.careerlens_ai_agent.model.InterviewPrep;
import com.omar.careerlens_ai_agent.model.JobRequirements;
import com.omar.careerlens_ai_agent.persona.Personas;

import java.util.List;

/**
 * Downstream agent contributing an interview-preparation goal to the
 * {@link ApplicationAgent} pipeline.
 *
 * <p>Kept separate from {@link ApplicationAgent} because interview prep is
 * about the candidate's next step after applying, not part of assembling
 * the application materials themselves. It depends only on
 * {@link JobRequirements} and {@link CandidateMatch}, so it's reachable
 * without needing the resume or cover letter to exist first.</p>
 */
@Agent(description = "Generates interview preparation questions tailored to a specific job and candidate")
public class InterviewPrepAgent {

    private final CareerForgeProperties properties;

    /**
     * @param properties bound {@code career-forge.*} configuration (max questions per category)
     */
    public InterviewPrepAgent(CareerForgeProperties properties) {
        this.properties = properties;
    }

    /**
     * Generates likely technical and behavioral interview questions for
     * this specific role, plus targeted prep tips addressing the
     * candidate's identified skill gaps.
     *
     * <p>Marked as its own {@link AchievesGoal} since interview prep is a
     * useful deliverable independent of the resume/cover letter branch of
     * the pipeline &mdash; a caller can request just this, and Embabel will
     * pull in {@link JobRequirements} and {@link CandidateMatch} as needed
     * without generating a resume or cover letter at all.</p>
     *
     * @param jobRequirements the output of {@link ApplicationAgent#extractJobRequirements}
     * @param candidateMatch  the output of {@link ApplicationAgent#matchCandidateProfile}
     * @param context         Embabel's operation context, providing access to the LLM
     * @return technical questions, behavioral questions, and prep tips, each capped
     *         per category at {@link CareerForgeProperties#maxInterviewQuestions()}
     */
    @AchievesGoal(description = "Interview preparation questions and tips tailored to the job and candidate")
    @Action(description = "Generate likely interview questions and preparation tips")
    public InterviewPrep generateInterviewQuestions(JobRequirements jobRequirements,
                                                    CandidateMatch candidateMatch, OperationContext context) {
        return context.ai()
                .withDefaultLlm()
                .withPromptContributors(List.of(Personas.HIRING_MANAGER))
                .createObjectIfPossible(
                        """
                        Role: %s (%s seniority) at %s
                        Required skills: %s
                        Candidate's skill gaps: %s

                        Generate up to %d likely technical questions covering the required
                        skills (weight them toward areas the candidate has gaps in, since
                        those are most likely to be probed), and up to %d likely behavioral
                        or situational questions appropriate for this seniority level.

                        Then give brief, actionable prep tips specifically addressing how
                        the candidate should prepare for their weakest areas.
                        Create an InterviewPrep from these questions and tips.
                        """.formatted(
                                jobRequirements.jobTitle(),
                                jobRequirements.seniorityLevel(),
                                jobRequirements.company(),
                                String.join(", ", jobRequirements.requiredSkills()),
                                candidateMatch.missingSkills().isEmpty()
                                        ? "none identified"
                                        : String.join(", ", candidateMatch.missingSkills()),
                                properties.maxInterviewQuestions(),
                                properties.maxInterviewQuestions()
                        ),
                        InterviewPrep.class
                );
    }
}
