package com.omar.contract_sentinel_ai_agent;

import com.omar.contract_sentinel_ai_agent.config.ContractSentinelProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Entry point for the ContractSentinel application.
 *
 * <p>ContractSentinel is an Embabel-powered AI agent that solves a
 * recurring, expensive problem in API-driven teams: breaking changes
 * slipping into a new API version unnoticed, semver bumps chosen by gut
 * feeling instead of by rule, and changelogs written as an afterthought
 * (or not written at all).</p>
 *
 * <p>Given the previous and current version of an API's contract (endpoints,
 * request/response fields) as free-text input, ContractSentinel:</p>
 * <ol>
 *   <li>deterministically diffs the two contracts,</li>
 *   <li>classifies each change as breaking, non-breaking, or a deprecation,</li>
 *   <li>computes the semantically-correct semver bump with a rule engine
 *       (not an LLM guess),</li>
 *   <li>writes a migration guide for API consumers, and</li>
 *   <li>generates a Keep-a-Changelog-style changelog entry.</li>
 * </ol>
 *
 * <p>Configuration is bound from {@code application.yml} via
 * {@link ContractSentinelProperties} and enabled here through
 * {@link EnableConfigurationProperties}.</p>
 */
@SpringBootApplication
@EnableConfigurationProperties(ContractSentinelProperties.class)
public class ContractSentinelAiAgentApplication {

	public static void main(String[] args) {
		SpringApplication.run(ContractSentinelAiAgentApplication.class, args);
	}

}
