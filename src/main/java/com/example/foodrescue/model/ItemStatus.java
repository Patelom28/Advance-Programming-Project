package com.example.foodrescue.model;

// The life cycle of a donated food item
public enum ItemStatus {
    AVAILABLE("Available"),
    CLAIMED("Claimed"),
    EXPIRED("Expired"),
    REMOVED("Removed");

    private final String label;

    ItemStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
