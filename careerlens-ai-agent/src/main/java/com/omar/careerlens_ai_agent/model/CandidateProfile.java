package com.omar.careerlens_ai_agent.model;

import java.util.List;

/**
 * The candidate's background, extracted from their free-text input
 * (resume summary, LinkedIn-about-style paragraph, or similar).
 *
 * @param skills            the candidate's stated skills/technologies
 * @param yearsOfExperience a short free-text description of experience level,
 *                          e.g. {@code "5 years, mostly backend Java"}
 * @param background        a concise summary of the candidate's professional background
 */
public record CandidateProfile(List<String> skills, String yearsOfExperience, String background) {
}
