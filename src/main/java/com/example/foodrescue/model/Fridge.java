package com.example.foodrescue.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// A public community fridge; one fridge holds many food items
@Entity
@Table(name = "fridges")
public class Fridge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Please give the fridge a name")
    @Size(max = 80, message = "The name may not be longer than 80 characters")
    @Column(nullable = false, length = 80)
    private String name;

    @NotBlank(message = "Please enter the street address")
    @Size(max = 160)
    @Column(nullable = false, length = 160)
    private String address;

    @NotBlank(message = "Please enter the district")
    @Size(max = 60)
    @Column(nullable = false, length = 60)
    private String district;

    // How many items may be stored at the same time
    @Min(value = 1, message = "Capacity must be at least 1")
    @Max(value = 200, message = "Capacity may not be larger than 200")
    @Column(nullable = false)
    private int capacity = 20;

    // A fridge that is switched off (repair, cleaning) accepts no donations
    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // @JsonIgnore stops Fridge -> Item -> Fridge turning into an endless loop in JSON
    @JsonIgnore
    @OneToMany(mappedBy = "fridge", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    private List<FoodItem> items = new ArrayList<>();

    public Fridge() {
        // required by JPA
    }

    public Fridge(String name, String address, String district, int capacity) {
        this.name = name;
        this.address = address;
        this.district = district;
        this.capacity = capacity;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<FoodItem> getItems() {
        return items;
    }

    public void setItems(List<FoodItem> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "Fridge{id=" + id + ", name='" + name + "', district='" + district + "'}";
    }
}
