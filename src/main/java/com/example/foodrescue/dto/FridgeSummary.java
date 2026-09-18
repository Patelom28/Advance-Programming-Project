package com.example.foodrescue.dto;

// A fridge together with its current filling level, used by the frontend
public class FridgeSummary {

    private final Long id;
    private final String name;
    private final String address;
    private final String district;
    private final int capacity;
    private final boolean active;
    private final long itemsInside;
    private final double kgInside;

    public FridgeSummary(Long id, String name, String address, String district,
                          int capacity, boolean active, long itemsInside, double kgInside) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.district = district;
        this.capacity = capacity;
        this.active = active;
        this.itemsInside = itemsInside;
        this.kgInside = kgInside;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getDistrict() {
        return district;
    }

    public int getCapacity() {
        return capacity;
    }

    public boolean isActive() {
        return active;
    }

    public long getItemsInside() {
        return itemsInside;
    }

    public double getKgInside() {
        return kgInside;
    }
}
