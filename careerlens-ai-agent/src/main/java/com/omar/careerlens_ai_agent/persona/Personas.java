package com.omar.careerlens_ai_agent.persona;

import com.embabel.agent.prompt.persona.RoleGoalBackstory;

/**
 * Reusable {@code PromptContributor}s injected into LLM calls made by
 * {@link com.omar.careerlens_ai_agent.agent.ApplicationAgent} and
 * {@link com.omar.careerlens_ai_agent.agent.InterviewPrepAgent}.
 *
 * <p>Kept as a single, non-instantiable holder class so persona definitions
 * live in one place, separate from each agent's control flow.</p>
 */
public final class Personas {

    private Personas() {
        // Non-instantiable: constants only.
    }

    /**
     * Persona applied when tailoring resume highlights and writing the
     * cover letter: an experienced career coach who emphasizes concrete,
     * achievement-oriented language over generic filler.
     */
    public static final RoleGoalBackstory CAREER_COACH = new RoleGoalBackstory(
            "Career Coach",
            "Present a candidate's real experience in the strongest, most relevant light for a specific role",
            "Career coach who has helped hundreds of engineers land roles by turning vague " +
                    "resume bullets into specific, achievement-oriented statements — never invents " +
                    "experience the candidate doesn't have"
    );

    /**
     * Persona applied when generating interview questions: an experienced
     * technical hiring manager who tailors questions to the role's actual
     * requirements and the candidate's specific gaps.
     */
    public static final RoleGoalBackstory HIRING_MANAGER = new RoleGoalBackstory(
            "Technical Hiring Manager",
            "Prepare a candidate for the specific interview they are about to have",
            "Engineering hiring manager who has run hundreds of interview loops and knows " +
                    "which questions a panel will actually ask for a given role and seniority level"
    );
}
