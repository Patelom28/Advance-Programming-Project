package com.example.foodrescue.controller;

import com.example.foodrescue.dto.NetworkStats;
import com.example.foodrescue.service.StatsExporter;
import com.example.foodrescue.service.StatsService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// The impact numbers shown on the dashboard
@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    // GET /api/stats
    @GetMapping
    public NetworkStats stats() {
        return statsService.buildStats();
    }

    // GET /api/stats/export?format=csv|text - StatsExporter.forFormat() returns
    // the abstract type, so this method never needs to know which subclass it got.
    @GetMapping("/export")
    public ResponseEntity<String> export(@RequestParam(defaultValue = "csv") String format) {
        StatsExporter exporter = StatsExporter.forFormat(format);
        String body = exporter.export(statsService.buildStats());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(exporter.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + exporter.fileName() + "\"")
                .body(body);
    }
}
