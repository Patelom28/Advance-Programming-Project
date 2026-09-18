package com.example.foodrescue.service;

import com.example.foodrescue.dto.CategoryStat;
import com.example.foodrescue.dto.NetworkStats;

// Human-readable report, e.g. to paste into an email or print for a district office.
public class TextStatsExporter extends StatsExporter {

    @Override
    public String contentType() {
        return "text/plain";
    }

    @Override
    public String fileExtension() {
        return "txt";
    }

    @Override
    public String export(NetworkStats stats) {
        StringBuilder text = new StringBuilder();
        text.append("FOOD RESCUE BERLIN - IMPACT REPORT\n");
        text.append("===================================\n\n");
        text.append("Rescued: ").append(stats.getKgRescued()).append(" kg\n");
        text.append("Lost:    ").append(stats.getKgLost()).append(" kg\n");
        text.append("Rescue rate: ").append(stats.getRescueRatePercent()).append("%\n\n");
        text.append("Items available: ").append(stats.getItemsAvailable()).append('\n');
        text.append("Items claimed:   ").append(stats.getItemsClaimed()).append('\n');
        text.append("Items expired:   ").append(stats.getItemsExpired()).append('\n');
        text.append("Fridges in service: ").append(stats.getActiveFridges())
            .append(" / ").append(stats.getTotalFridges()).append("\n\n");

        text.append("Rescued by category:\n");
        for (CategoryStat row : stats.getByCategory()) {
            text.append("  - ").append(row.getCategory()).append(": ")
                .append(row.getKgRescued()).append(" kg (")
                .append(row.getSharePercent()).append("%)\n");
        }
        return text.toString();
    }
}
