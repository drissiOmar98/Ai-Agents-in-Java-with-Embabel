package com.omar.contract_sentinel_ai_agent.model;

import java.util.List;

/**
 * The API's contract as it exists in the new, not-yet-released version,
 * extracted from the user's input.
 *
 * @param endpoints every endpoint defined in the new version
 */
public record CurrentApiSnapshot(List<ApiEndpoint> endpoints) {
}
