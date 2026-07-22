/**
 * Immutable data carriers that flow through the FestivalScout pipeline,
 * from raw user input to a complete, conflict-free festival plan.
 *
 * <p>{@link com.omar.festivalscout_ai_agent.model.FestivalBasicInfo} and
 * {@link com.omar.festivalscout_ai_agent.model.AttendeePreferences} are extracted
 * independently from the user's input. Together with
 * {@link com.omar.festivalscout_ai_agent.model.LineupHighlights} (researched via web
 * search) they produce {@link com.festivalscout.model.MustSeePicks}, which
 * is checked for overlaps ({@link com.festivalscout.model.ScheduleConflictReport})
 * before becoming a {@link com.festivalscout.model.FestivalItinerary}.
 * {@link com.omar.festivalscout_ai_agent.model.PackingList},
 * {@link com.omar.festivalscout_ai_agent.model.BudgetEstimate}, and
 * {@link com.omar.festivalscout_ai_agent.SafetyTips} are independent downstream
 * goals derived from the basic info and preferences.</p>
 */
package com.omar.festivalscout_ai_agent.model;
