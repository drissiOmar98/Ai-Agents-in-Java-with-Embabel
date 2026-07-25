/**
 * Immutable data carriers that flow through the ContractSentinel pipeline,
 * from two raw API contract descriptions to a migration guide and
 * changelog.
 *
 * <p>{@link com.omar.contract_sentinel_ai_agent.model.PreviousApiSnapshot} and
 * {@link com.omar.contract_sentinel_ai_agent.model.CurrentApiSnapshot} are extracted
 * independently, mechanically diffed into an
 * {@link com.omar.contract_sentinel_ai_agent.model.ApiDiffReport}, then judged for
 * real-world impact as a {@link com.omar.contract_sentinel_ai_agent.model.BreakingChangeReport}.
 * That report drives both a
 * {@link com.omar.contract_sentinel_ai_agent.model.SemverRecommendation} (computed by rule,
 * not guessed) and, together with it, a
 * {@link com.omar.contract_sentinel_ai_agent.model.MigrationGuide} and
 * {@link com.omar.contract_sentinel_ai_agent.model.ChangelogEntry}.</p>
 */
package com.omar.contract_sentinel_ai_agent.model;
