package com.example.foodrescue.model;

// The kind of food being donated, used to group the rescue statistics
public enum FoodCategory {
    BAKERY("Bread and bakery"),
    FRUIT_VEG("Fruit and vegetables"),
    DAIRY("Dairy and eggs"),
    COOKED_MEAL("Cooked meals"),
    PANTRY("Pantry / non-perishable"),
    OTHER("Other");

    private final String label;

    FoodCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
