package com.example.foodrescue.service;

import com.example.foodrescue.dto.CategoryStat;
import com.example.foodrescue.dto.NetworkStats;

// Spreadsheet-friendly export: one row per metric, then one row per category.
public class CsvStatsExporter extends StatsExporter {

    @Override
    public String contentType() {
        return "text/csv";
    }

    @Override
    public String fileExtension() {
        return "csv";
    }

    @Override
    public String export(NetworkStats stats) {
        StringBuilder csv = new StringBuilder();
        csv.append("metric,value\n");
        csv.append("kgRescued,").append(stats.getKgRescued()).append('\n');
        csv.append("kgLost,").append(stats.getKgLost()).append('\n');
        csv.append("rescueRatePercent,").append(stats.getRescueRatePercent()).append('\n');
        csv.append("itemsAvailable,").append(stats.getItemsAvailable()).append('\n');
        csv.append("itemsClaimed,").append(stats.getItemsClaimed()).append('\n');
        csv.append("itemsExpired,").append(stats.getItemsExpired()).append('\n');
        csv.append("activeFridges,").append(stats.getActiveFridges()).append('\n');
        csv.append("totalFridges,").append(stats.getTotalFridges()).append('\n');

        csv.append("\ncategory,kgRescued,sharePercent\n");
        for (CategoryStat row : stats.getByCategory()) {
            csv.append(row.getCategory()).append(',')
               .append(row.getKgRescued()).append(',')
               .append(row.getSharePercent()).append('\n');
        }
        return csv.toString();
    }
}
