package com.omar.contract_sentinel_ai_agent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Type-safe configuration properties for ContractSentinel, bound from the
 * {@code contract-sentinel} prefix in {@code application.yml}.
 *
 * <pre>{@code
 * contract-sentinel:
 *   max-migration-steps: 10
 * }</pre>
 *
 * @param maxMigrationSteps the maximum number of steps generated in the consumer
 *                          migration guide; defaults to {@code 10} when zero or negative
 */
@ConfigurationProperties(prefix = "contract-sentinel")
public record ContractSentinelProperties(int maxMigrationSteps) {

    /**
     * Compact constructor applying a sensible default when the value is
     * missing or invalid, so the application never fails to start due to
     * incomplete configuration.
     */
    public ContractSentinelProperties {
        if (maxMigrationSteps <= 0) {
            maxMigrationSteps = 10;
        }
    }
}
