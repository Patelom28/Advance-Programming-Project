package com.example.foodrescue.service;

import com.example.foodrescue.dto.NetworkStats;

// Shared template for turning dashboard stats into a downloadable file.
// fileName() is concrete and identical for every format; export(), contentType()
// and fileExtension() are abstract because each format builds its output differently.
public abstract class StatsExporter {

    public abstract String export(NetworkStats stats);

    public abstract String contentType();

    public abstract String fileExtension();

    public String fileName() {
        return "food-rescue-stats." + fileExtension();
    }

    // Picks the right exporter for a format name - callers only ever see the abstract type.
    public static StatsExporter forFormat(String format) {
        if ("text".equalsIgnoreCase(format)) {
            return new TextStatsExporter();
        }
        return new CsvStatsExporter();
    }
}
