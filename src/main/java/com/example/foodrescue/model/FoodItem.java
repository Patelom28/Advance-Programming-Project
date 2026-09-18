package com.example.foodrescue.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

// One donated portion of food lying in a community fridge
@Entity
@Table(name = "food_items")
public class FoodItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FoodCategory category = FoodCategory.OTHER;

    // Weight in kilograms, used to calculate how much food was rescued
    @Column(nullable = false)
    private double quantityKg;

    @Column(nullable = false)
    private LocalDate expiryDate;

    @Column(length = 80)
    private String donorName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ItemStatus status = ItemStatus.AVAILABLE;

    @Column(length = 80)
    private String claimedBy;

    private LocalDateTime claimedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // EAGER so the REST layer can show the fridge without an open database session
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "fridge_id", nullable = false)
    private Fridge fridge;

    public FoodItem() {
        // required by JPA
    }

    public FoodItem(String name, FoodCategory category, double quantityKg,
                    LocalDate expiryDate, String donorName, Fridge fridge) {
        this.name = name;
        this.category = category;
        this.quantityKg = quantityKg;
        this.expiryDate = expiryDate;
        this.donorName = donorName;
        this.fridge = fridge;
    }

    // True when the best-before date already passed
    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    // Days left until the best-before date (negative when already passed)
    public long getDaysLeft() {
        if (expiryDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
    }

    // True when the item should be taken soon (today or tomorrow)
    public boolean isUrgent() {
        return status == ItemStatus.AVAILABLE && getDaysLeft() <= 1;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FoodCategory getCategory() {
        return category;
    }

    public void setCategory(FoodCategory category) {
        this.category = category;
    }

    public double getQuantityKg() {
        return quantityKg;
    }

    public void setQuantityKg(double quantityKg) {
        this.quantityKg = quantityKg;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getDonorName() {
        return donorName;
    }

    public void setDonorName(String donorName) {
        this.donorName = donorName;
    }

    public ItemStatus getStatus() {
        return status;
    }

    public void setStatus(ItemStatus status) {
        this.status = status;
    }

    public String getClaimedBy() {
        return claimedBy;
    }

    public void setClaimedBy(String claimedBy) {
        this.claimedBy = claimedBy;
    }

    public LocalDateTime getClaimedAt() {
        return claimedAt;
    }

    public void setClaimedAt(LocalDateTime claimedAt) {
        this.claimedAt = claimedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Fridge getFridge() {
        return fridge;
    }

    public void setFridge(Fridge fridge) {
        this.fridge = fridge;
    }

    @Override
    public String toString() {
        return "FoodItem{id=" + id + ", name='" + name + "', status=" + status + "}";
    }
}
