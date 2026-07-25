package com.omar.contract_sentinel_ai_agent.model;

import java.util.List;

/**
 * A release changelog entry in the widely-used
 * <a href="https://keepachangelog.com">Keep a Changelog</a> category
 * structure.
 *
 * <p>This is the secondary goal output of
 * {@link com.omar.contract_sentinel_ai_agent.agent.ChangelogAgent#generateChangelogEntry}.</p>
 *
 * @param added      new capabilities, e.g. new endpoints
 * @param changed    modifications to existing behavior
 * @param removed    endpoints or fields removed entirely
 * @param deprecated things still present but scheduled for future removal
 */
public record ChangelogEntry(List<String> added, List<String> changed, List<String> removed, List<String> deprecated) {
}
