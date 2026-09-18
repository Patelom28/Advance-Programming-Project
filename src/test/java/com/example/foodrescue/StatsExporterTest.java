package com.example.foodrescue;

import com.example.foodrescue.dto.NetworkStats;
import com.example.foodrescue.service.StatsExporter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// A plain unit test, no Spring context needed - just exercises the
// StatsExporter abstract class and its two polymorphic subclasses.
class StatsExporterTest {

    private NetworkStats sampleStats() {
        return new NetworkStats(4, 3, 9, 5, 2, 6.7, 1.2, List.of());
    }

    @Test
    void formatFactoryReturnsCsvExporterByDefault() {
        StatsExporter exporter = StatsExporter.forFormat("csv");
        assertEquals("csv", exporter.fileExtension());
        assertTrue(exporter.export(sampleStats()).contains("kgRescued,6.7"));
    }

    @Test
    void formatFactoryReturnsTextExporterForTextFormat() {
        StatsExporter exporter = StatsExporter.forFormat("text");
        assertEquals("txt", exporter.fileExtension());
        assertTrue(exporter.export(sampleStats()).contains("Rescued: 6.7 kg"));
    }

    @Test
    void unknownFormatFallsBackToCsv() {
        StatsExporter exporter = StatsExporter.forFormat("bogus");
        assertEquals("csv", exporter.fileExtension());
    }
}
