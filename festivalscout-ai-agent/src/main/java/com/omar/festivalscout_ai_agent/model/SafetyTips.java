package com.omar.festivalscout_ai_agent.model;

import java.util.List;

/**
 * Practical safety and wellbeing guidance for the festival, tailored to
 * its format and environment (e.g. multi-day camping heat safety vs. a
 * dense downtown crowd-navigation concern).
 *
 * <p>This is a goal output of
 * {@link com.omar.festivalscout_ai_agent.agent.LogisticsAgent#generateSafetyTips}.</p>
 *
 * @param tips             practical, actionable safety and wellbeing tips
 * @param emergencyAdvice  brief guidance on what to do in an emergency or if separated from a group
 */
public record SafetyTips(List<String> tips, String emergencyAdvice) {
}
