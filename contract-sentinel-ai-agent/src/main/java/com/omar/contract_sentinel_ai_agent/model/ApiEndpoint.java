package com.omar.contract_sentinel_ai_agent.model;

import java.util.List;

/**
 * A single API endpoint's shape at a point in time: its method, path, and
 * the fields it accepts and returns.
 *
 * <p>Used identically inside both {@link PreviousApiSnapshot} and
 * {@link CurrentApiSnapshot} &mdash; the two snapshot types exist
 * separately (rather than one {@code ApiSnapshot} reused twice) purely so
 * Embabel's planner can bind "the old contract" and "the new contract" to
 * distinct parameters in {@link com.omar.contract_sentinel_ai_agent.agent.ApiDiffAgent#compareSnapshots}.</p>
 *
 * @param method         the HTTP method, e.g. {@code "GET"}, {@code "POST"}
 * @param path           the endpoint path, e.g. {@code "/users/{id}"}
 * @param requestFields  request fields as {@code "name:type"} entries, e.g. {@code "email:String"}
 * @param responseFields response fields as {@code "name:type"} entries
 */
public record ApiEndpoint(String method, String path, List<String> requestFields, List<String> responseFields) {
}
