package com.example.foodrescue.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// What the client sends when taking a food item home
public class ClaimRequest {

    @NotBlank(message = "Please enter your name")
    @Size(max = 80)
    private String claimedBy;

    public String getClaimedBy() {
        return claimedBy;
    }

    public void setClaimedBy(String claimedBy) {
        this.claimedBy = claimedBy;
    }
}
