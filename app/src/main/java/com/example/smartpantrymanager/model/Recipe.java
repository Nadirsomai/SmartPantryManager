package com.example.smartpantrymanager.model;

public class Recipe {

    private final long id;
    private final String name;
    private final String description;
    private final String preparation;
    private final String instructions;

    public Recipe(long id, String name, String description, String preparation,
                  String instructions) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.preparation = preparation;
        this.instructions = instructions;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getPreparation() {
        return preparation;
    }

    public String getInstructions() {
        return instructions;
    }
}
