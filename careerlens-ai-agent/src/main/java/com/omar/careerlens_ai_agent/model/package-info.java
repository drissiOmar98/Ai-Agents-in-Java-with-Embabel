/**
 * Immutable data carriers that flow through the CareerForge pipeline, from
 * raw job posting and candidate text to a tailored application package.
 *
 * <p>{@link com.omar.careerlens_ai_agent.model.JobRequirements} and
 * {@link com.omar.careerlens_ai_agent.model.CandidateProfile} are extracted independently
 * from the user's input and combined into a
 * {@link com.omar.careerlens_ai_agent.model.CandidateMatch}. That match feeds two
 * downstream branches: resume/cover-letter generation
 * ({@link com.omar.careerlens_ai_agent.model.ResumeHighlights},
 * {@link com.omar.careerlens_ai_agent.model.AtsScoreReport},
 * {@link com.omar.careerlens_ai_agent.model.CoverLetter}) and interview preparation
 * ({@link com.omar.careerlens_ai_agent.model.InterviewPrep}).</p>
 */
package com.omar.careerlens_ai_agent.model;
