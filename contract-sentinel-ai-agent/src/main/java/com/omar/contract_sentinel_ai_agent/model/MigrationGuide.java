package com.omar.contract_sentinel_ai_agent.model;

import java.util.List;

/**
 * A step-by-step guide for existing API consumers to migrate to the new
 * contract version safely.
 *
 * <p>This is the primary goal output of
 * {@link com.omar.contract_sentinel_ai_agent.agent.ApiDiffAgent#generateMigrationGuide}.</p>
 *
 * @param steps ordered, concrete migration steps, capped at
 *              {@link com.omar.contract_sentinel_ai_agent.config.ContractSentinelProperties#maxMigrationSteps()}
 */
public record MigrationGuide(List<String> steps) {
}
