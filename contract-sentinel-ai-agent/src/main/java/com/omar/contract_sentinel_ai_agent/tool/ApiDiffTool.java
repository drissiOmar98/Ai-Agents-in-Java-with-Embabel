package com.omar.contract_sentinel_ai_agent.tool;

import com.embabel.agent.api.annotation.LlmTool;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Exposes a deterministic, structural API-endpoint diff as an LLM tool.
 *
 * <p>Registered with
 * {@link com.omar.contract_sentinel_ai_agent.agent.ApiDiffAgent#compareSnapshots}. Comparing
 * two lists of endpoints for additions, removals, and field-level changes
 * is exactly the kind of multi-item, exact-matching comparison LLMs are
 * unreliable at once the list gets past a handful of items — so the actual
 * set comparison happens here, in plain Java, and the LLM's job is only to
 * serialize the two snapshots into the tool's input format and interpret
 * its output.</p>
 */
@Component
public class ApiDiffTool {

    private static final String ENTRY_SEPARATOR = ";";
    private static final String FIELD_SEPARATOR = "\\|";
    private static final String LIST_SEPARATOR = ",";

    /**
     * Structurally compares two sets of API endpoints and reports what was
     * added, removed, and changed.
     *
     * @param previousEndpoints semicolon-separated entries, each formatted as
     *                          {@code method|path|requestFields|responseFields}, with
     *                          fields comma-separated (e.g. {@code "email:String,name:String"})
     * @param currentEndpoints  the same format, for the new version
     * @return a summary string with {@code ADDED:}, {@code REMOVED:}, and {@code CHANGED:}
     *         sections, each listing the relevant endpoints/field differences;
     *         a section is omitted entirely if there's nothing to report
     */
    @LlmTool(description = "Structurally compare two sets of API endpoints (method|path|requestFields|" +
            "responseFields entries, semicolon-separated, fields comma-separated) and report exactly what " +
            "endpoints were added, removed, or had their request/response fields changed.")
    public String compareEndpoints(
            @LlmTool.Param(description = "Previous version endpoints: method|path|requestFields|responseFields, separated by ;")
            String previousEndpoints,
            @LlmTool.Param(description = "Current version endpoints: method|path|requestFields|responseFields, separated by ;")
            String currentEndpoints
    ) {
        Map<String, Endpoint> previous = parse(previousEndpoints);
        Map<String, Endpoint> current = parse(currentEndpoints);

        Set<String> added = new LinkedHashSet<>(current.keySet());
        added.removeAll(previous.keySet());

        Set<String> removed = new LinkedHashSet<>(previous.keySet());
        removed.removeAll(current.keySet());

        StringBuilder changed = new StringBuilder();
        for (String key : previous.keySet()) {
            if (!current.containsKey(key)) {
                continue;
            }
            String fieldDiff = diffFields(key, previous.get(key), current.get(key));
            if (!fieldDiff.isBlank()) {
                if (!changed.isEmpty()) {
                    changed.append(" | ");
                }
                changed.append(fieldDiff);
            }
        }

        StringBuilder result = new StringBuilder();
        if (!added.isEmpty()) {
            result.append("ADDED: ").append(String.join(", ", added));
        }
        if (!removed.isEmpty()) {
            if (!result.isEmpty()) {
                result.append(" || ");
            }
            result.append("REMOVED: ").append(String.join(", ", removed));
        }
        if (!changed.isEmpty()) {
            if (!result.isEmpty()) {
                result.append(" || ");
            }
            result.append("CHANGED: ").append(changed);
        }

        return result.isEmpty() ? "No differences detected." : result.toString();
    }

    private String diffFields(String endpointKey, Endpoint before, Endpoint after) {
        Set<String> removedRequestFields = fieldNames(before.requestFields);
        removedRequestFields.removeAll(fieldNames(after.requestFields));

        Set<String> addedRequestFields = fieldNames(after.requestFields);
        addedRequestFields.removeAll(fieldNames(before.requestFields));

        Set<String> removedResponseFields = fieldNames(before.responseFields);
        removedResponseFields.removeAll(fieldNames(after.responseFields));

        Set<String> addedResponseFields = fieldNames(after.responseFields);
        addedResponseFields.removeAll(fieldNames(before.responseFields));

        if (removedRequestFields.isEmpty() && addedRequestFields.isEmpty()
                && removedResponseFields.isEmpty() && addedResponseFields.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder(endpointKey).append(" [");
        appendIfPresent(sb, "request fields removed", removedRequestFields);
        appendIfPresent(sb, "request fields added", addedRequestFields);
        appendIfPresent(sb, "response fields removed", removedResponseFields);
        appendIfPresent(sb, "response fields added", addedResponseFields);
        sb.append("]");
        return sb.toString();
    }

    private void appendIfPresent(StringBuilder sb, String label, Set<String> fields) {
        if (fields.isEmpty()) {
            return;
        }
        if (sb.charAt(sb.length() - 1) != '[') {
            sb.append("; ");
        }
        sb.append(label).append("=").append(fields);
    }

    private Set<String> fieldNames(Set<String> fieldEntries) {
        // Field entries are "name:type"; compare by name only so a type-only
        // change is still visible in the raw entry set but doesn't hide a
        // pure rename/removal under a mismatched type string.
        return fieldEntries.stream()
                .map(entry -> entry.split(":", 2)[0].trim())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Map<String, Endpoint> parse(String raw) {
        Map<String, Endpoint> endpoints = new LinkedHashMap<>();
        if (raw == null || raw.isBlank()) {
            return endpoints;
        }
        for (String entry : raw.split(ENTRY_SEPARATOR)) {
            String trimmed = entry.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String[] fields = trimmed.split(FIELD_SEPARATOR, -1);
            if (fields.length < 2) {
                continue; // skip malformed entries rather than failing the whole comparison
            }
            String method = fields[0].trim().toUpperCase();
            String path = fields[1].trim();
            Set<String> requestFields = fields.length > 2 ? splitList(fields[2]) : new LinkedHashSet<>();
            Set<String> responseFields = fields.length > 3 ? splitList(fields[3]) : new LinkedHashSet<>();

            endpoints.put(method + " " + path, new Endpoint(requestFields, responseFields));
        }
        return endpoints;
    }

    private Set<String> splitList(String value) {
        Set<String> result = new LinkedHashSet<>();
        if (value == null || value.isBlank()) {
            return result;
        }
        for (String item : value.split(LIST_SEPARATOR)) {
            String trimmed = item.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }

    private record Endpoint(Set<String> requestFields, Set<String> responseFields) {
    }
}
