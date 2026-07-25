package com.omar.contract_sentinel_ai_agent.model;

import java.util.List;

/**
 * The API's contract as it existed in the previous (currently shipped)
 * version, extracted from the user's input.
 *
 * @param endpoints every endpoint defined in the previous version
 */
public record PreviousApiSnapshot(List<ApiEndpoint> endpoints) {
}
