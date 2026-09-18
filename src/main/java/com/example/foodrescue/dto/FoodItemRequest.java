package com.example.foodrescue.dto;

import com.example.foodrescue.model.FoodCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

// What the client sends to donate or edit a food item
public class FoodItemRequest {

    @NotBlank(message = "Please give the food a name")
    @Size(max = 100)
    private String name;

    @NotNull(message = "Please choose a category")
    private FoodCategory category;

    @NotNull(message = "Please enter the weight in kilograms")
    @DecimalMin(value = "0.1", message = "Weight must be at least 0.1 kg")
    private Double quantityKg;

    @NotNull(message = "Please enter the best-before date")
    private LocalDate expiryDate;

    @Size(max = 80)
    private String donorName;

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

    public Double getQuantityKg() {
        return quantityKg;
    }

    public void setQuantityKg(Double quantityKg) {
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
}
