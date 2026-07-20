package com.omar.careerlens_ai_agent.agent;


import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.OperationContext;
import com.omar.careerlens_ai_agent.model.AtsScoreReport;
import com.omar.careerlens_ai_agent.model.JobRequirements;
import com.omar.careerlens_ai_agent.model.ResumeHighlights;
import com.omar.careerlens_ai_agent.tool.AtsKeywordScoreTool;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Quality-gate agent contributing an independent ATS (Applicant Tracking
 * System) keyword check to the {@link ApplicationAgent} pipeline.
 *
 * <p>Kept separate from {@link ApplicationAgent} because this step is
 * verification/analysis, not content generation &mdash; it reads the
 * tailored resume highlights and reports keyword coverage against the
 * job's required skills, rather than producing the next stage of the
 * application itself. Its output feeds into
 * {@link ApplicationAgent#writeCoverLetter} so under-represented keywords
 * can be naturally addressed before the letter is finalized.</p>
 */
@Agent(description = "Scores ATS keyword coverage for tailored resume content against a job's required skills")
public class ApplicationQualityAgent {

    private final AtsKeywordScoreTool atsKeywordScoreTool;

    /**
     * @param atsKeywordScoreTool tool exposed to the LLM for computing a deterministic
     *                            keyword match percentage in {@link #scoreAtsMatch}
     */
    public ApplicationQualityAgent(AtsKeywordScoreTool atsKeywordScoreTool) {
        this.atsKeywordScoreTool = atsKeywordScoreTool;
    }

    /**
     * Scores how well the tailored resume highlights cover the job's
     * required skill keywords, using a deterministic substring match rather
     * than an LLM estimate.
     *
     * @param resumeHighlights the output of {@link ApplicationAgent#tailorResumeHighlights}
     * @param jobRequirements  the output of {@link ApplicationAgent#extractJobRequirements}
     * @param context          Embabel's operation context, providing access to the LLM
     * @return the keyword match percentage and the list of required keywords still missing
     */
    @Action(description = "Score ATS keyword coverage against the job's required skills")
    public AtsScoreReport scoreAtsMatch(ResumeHighlights resumeHighlights,
                                        JobRequirements jobRequirements, OperationContext context) {
        return context.ai()
                .withDefaultLlm()
                .withToolObject(atsKeywordScoreTool)
                .createObjectIfPossible(
                        """
                        Use the calculateAtsScore tool with:
                        - resumeText: the joined resume highlights below
                        - requiredKeywordsCsv: "%s"

                        Resume highlights:
                        %s

                        Put the tool's exact match percentage into matchPercent and its
                        exact missing-keyword list into missingKeywords.
                        Create an AtsScoreReport from the tool's result.
                        """.formatted(
                                String.join(", ", jobRequirements.requiredSkills()),
                                joinBullets(resumeHighlights)
                        ),
                        AtsScoreReport.class
                );
    }

    private String joinBullets(ResumeHighlights resumeHighlights) {
        return resumeHighlights.bullets().stream().collect(Collectors.joining("\n"));
    }
}
