package com.example.smartpantrymanager.model;

public class RecipeIngredient {

    private final long recipeId;
    private final String name;
    private final double quantity;
    private final String unit;

    public RecipeIngredient(long recipeId, String name, double quantity, String unit) {
        this.recipeId = recipeId;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }

    public long getRecipeId() {
        return recipeId;
    }

    public String getName() {
        return name;
    }

    public double getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }
}
