package com.omar.careerlens_ai_agent.model;

import java.util.List;

/**
 * The structured requirements extracted from a raw job posting.
 *
 * <p>This is the first fact established in the pipeline and anchors every
 * downstream step: matching, resume tailoring, ATS scoring, the cover
 * letter, and interview question generation all reference it.</p>
 *
 * @param jobTitle         the position's title as stated in the posting
 * @param company          the hiring company's name, or {@code "the company"}
 *                         if not stated in the posting
 * @param requiredSkills   skills/technologies explicitly stated as required
 * @param niceToHaveSkills skills/technologies stated as preferred or a plus, not mandatory
 * @param seniorityLevel   the apparent seniority level, e.g. {@code "Mid-level"}, {@code "Senior"}
 */
public record JobRequirements(
        String jobTitle,
        String company,
        List<String> requiredSkills,
        List<String> niceToHaveSkills,
        String seniorityLevel
) {
}
