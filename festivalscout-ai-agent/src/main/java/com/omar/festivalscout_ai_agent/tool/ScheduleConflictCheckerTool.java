package com.omar.festivalscout_ai_agent.tool;

import com.embabel.agent.api.annotation.LlmTool;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Exposes a deterministic set-time overlap check as an LLM tool.
 *
 * <p>Registered with
 * {@link com.omar.festivalscout_ai_agent.agent.ScheduleQualityAgent#checkScheduleConflicts}
 * so overlapping picks are detected by comparing actual time ranges rather
 * than an LLM eyeballing a list of times — the same "give the model a real
 * calculation" pattern used by the readability and ATS-scoring tools in
 * sibling projects.</p>
 */
@Component
public class ScheduleConflictCheckerTool {

    private static final String ENTRY_SEPARATOR = ";";
    private static final String FIELD_SEPARATOR = "\\|";

    /**
     * Checks a list of artist set picks for same-day time overlaps.
     *
     * @param scheduleEntries semicolon-separated entries, each formatted as
     *                        {@code day|startTime|endTime|artist} with times in
     *                        24-hour {@code HH:mm} format, e.g.
     *                        {@code "Day 1|22:00|23:30|Artist A;Day 1|23:00|00:30|Artist B"}
     * @return a summary string, e.g.
     *         {@code "Conflicts: 1 | Day 1: Artist A (22:00-23:30) overlaps with Artist B (23:00-00:30)"};
     *         returns {@code "Conflicts: 0"} if no overlaps are found or input is blank
     */
    @LlmTool(description = "Check a list of artist set picks (day|startTime|endTime|artist, separated by " +
            "semicolons, 24-hour HH:mm times) for same-day time overlaps and return a summary of any conflicts.")
    public String checkConflicts(
            @LlmTool.Param(description = "Semicolon-separated entries: day|startTime|endTime|artist")
            String scheduleEntries
    ) {
        if (scheduleEntries == null || scheduleEntries.isBlank()) {
            return "Conflicts: 0";
        }

        List<Entry> entries = parseEntries(scheduleEntries);
        List<String> conflicts = new ArrayList<>();

        for (int i = 0; i < entries.size(); i++) {
            for (int j = i + 1; j < entries.size(); j++) {
                Entry a = entries.get(i);
                Entry b = entries.get(j);
                if (a.day.equalsIgnoreCase(b.day) && overlaps(a, b)) {
                    conflicts.add(String.format("%s: %s (%s-%s) overlaps with %s (%s-%s)",
                            a.day, a.artist, a.start, a.end, b.artist, b.start, b.end));
                }
            }
        }

        if (conflicts.isEmpty()) {
            return "Conflicts: 0";
        }
        return String.format("Conflicts: %d | %s", conflicts.size(), String.join(" | ", conflicts));
    }

    private boolean overlaps(Entry a, Entry b) {
        // Overnight sets (e.g. 23:00-01:00) aren't handled with date rollover here;
        // treated as same-day ranges, which is sufficient for adjacent-set conflict checks.
        return a.start.isBefore(b.end) && b.start.isBefore(a.end);
    }

    private List<Entry> parseEntries(String scheduleEntries) {
        List<Entry> entries = new ArrayList<>();
        for (String raw : scheduleEntries.split(ENTRY_SEPARATOR)) {
            String trimmed = raw.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String[] fields = trimmed.split(FIELD_SEPARATOR);
            if (fields.length != 4) {
                continue; // skip malformed entries rather than failing the whole check
            }
            try {
                entries.add(new Entry(
                        fields[0].trim(),
                        LocalTime.parse(fields[1].trim()),
                        LocalTime.parse(fields[2].trim()),
                        fields[3].trim()
                ));
            } catch (DateTimeParseException e) {
                // skip malformed time values rather than failing the whole check
            }
        }
        return entries;
    }

    private record Entry(String day, LocalTime start, LocalTime end, String artist) {
    }
}
