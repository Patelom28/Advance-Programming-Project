package com.example.foodrescue.dto;

import com.example.foodrescue.model.FoodCategory;

// Rescued kilograms for one food category, and its share of the total
public class CategoryStat {

    private final FoodCategory category;
    private final double kgRescued;
    private final int sharePercent;

    public CategoryStat(FoodCategory category, double kgRescued, int sharePercent) {
        this.category = category;
        this.kgRescued = kgRescued;
        this.sharePercent = sharePercent;
    }

    public FoodCategory getCategory() {
        return category;
    }

    public double getKgRescued() {
        return kgRescued;
    }

    public int getSharePercent() {
        return sharePercent;
    }
}
