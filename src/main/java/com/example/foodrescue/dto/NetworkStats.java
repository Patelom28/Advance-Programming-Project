package com.example.foodrescue.dto;

import java.util.List;

// The impact numbers shown on the dashboard
public class NetworkStats {

    private final long totalFridges;
    private final long activeFridges;
    private final long itemsAvailable;
    private final long itemsClaimed;
    private final long itemsExpired;
    private final double kgRescued;
    private final double kgLost;
    private final List<CategoryStat> byCategory;

    public NetworkStats(long totalFridges, long activeFridges, long itemsAvailable,
                         long itemsClaimed, long itemsExpired, double kgRescued,
                         double kgLost, List<CategoryStat> byCategory) {
        this.totalFridges = totalFridges;
        this.activeFridges = activeFridges;
        this.itemsAvailable = itemsAvailable;
        this.itemsClaimed = itemsClaimed;
        this.itemsExpired = itemsExpired;
        this.kgRescued = kgRescued;
        this.kgLost = kgLost;
        this.byCategory = byCategory;
    }

    public long getTotalFridges() {
        return totalFridges;
    }

    public long getActiveFridges() {
        return activeFridges;
    }

    public long getItemsAvailable() {
        return itemsAvailable;
    }

    public long getItemsClaimed() {
        return itemsClaimed;
    }

    public long getItemsExpired() {
        return itemsExpired;
    }

    public double getKgRescued() {
        return kgRescued;
    }

    public double getKgLost() {
        return kgLost;
    }

    /** Rescue rate as a whole percentage, 0 when nothing has happened yet. */
    public int getRescueRatePercent() {
        double total = kgRescued + kgLost;
        return total > 0 ? (int) Math.round(kgRescued * 100.0 / total) : 0;
    }

    public List<CategoryStat> getByCategory() {
        return byCategory;
    }
}
