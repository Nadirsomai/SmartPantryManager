package com.example.smartpantrymanager.logic;

import com.example.smartpantrymanager.model.PantryItem;
import com.example.smartpantrymanager.model.RecipeIngredient;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class RecipeMatcher {

    private RecipeMatcher() {
        // Utility class.
    }

    public static boolean canMakeRecipe(List<PantryItem> pantryItems,
                                        List<RecipeIngredient> requiredIngredients) {
        if (requiredIngredients.isEmpty()) {
            return false;
        }

        Map<String, Double> pantryQuantities = new HashMap<>();
        for (PantryItem pantryItem : pantryItems) {
            String key = ingredientKey(pantryItem.getName(), pantryItem.getUnit());
            double currentQuantity = pantryQuantities.containsKey(key)
                    ? pantryQuantities.get(key)
                    : 0;
            pantryQuantities.put(key, currentQuantity + pantryItem.getQuantity());
        }

        for (RecipeIngredient requiredIngredient : requiredIngredients) {
            String key = ingredientKey(requiredIngredient.getName(), requiredIngredient.getUnit());
            Double availableQuantity = pantryQuantities.get(key);
            if (availableQuantity == null
                    || availableQuantity < requiredIngredient.getQuantity()) {
                return false;
            }
        }

        return true;
    }

    private static String ingredientKey(String name, String unit) {
        return normalise(name) + "|" + normalise(unit);
    }

    private static String normalise(String value) {
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
