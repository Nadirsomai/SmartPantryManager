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

        // Combine matching pantry entries after converting their quantities to a common
        // unit. This handles cases where the same ingredient was added more than once.
        Map<String, Double> pantryQuantities = new HashMap<>();
        for (PantryItem pantryItem : pantryItems) {
            ConvertedQuantity convertedQuantity = convertQuantity(
                    pantryItem.getQuantity(), pantryItem.getUnit());
            String key = ingredientKey(pantryItem.getName(), convertedQuantity.unitGroup);
            double currentQuantity = pantryQuantities.containsKey(key)
                    ? pantryQuantities.get(key)
                    : 0;
            pantryQuantities.put(key, currentQuantity + convertedQuantity.quantity);
        }

        // A recipe qualifies only when every required ingredient is present in a
        // compatible unit and the total available quantity is sufficient.
        for (RecipeIngredient requiredIngredient : requiredIngredients) {
            ConvertedQuantity requiredQuantity = convertQuantity(
                    requiredIngredient.getQuantity(), requiredIngredient.getUnit());
            String key = ingredientKey(requiredIngredient.getName(), requiredQuantity.unitGroup);
            Double availableQuantity = pantryQuantities.get(key);
            if (availableQuantity == null
                    || availableQuantity < requiredQuantity.quantity) {
                return false;
            }
        }

        return true;
    }

    private static String ingredientKey(String name, String unitGroup) {
        return normaliseIngredientName(name) + "|" + unitGroup;
    }

    // Convert compatible measurements to shared base units before comparing them:
    // grams for mass, millilitres for volume, and teaspoons for spoon measurements.
    private static ConvertedQuantity convertQuantity(double quantity, String unit) {
        String normalisedUnit = normalise(unit);
        switch (normalisedUnit) {
            case "kg":
                return new ConvertedQuantity(quantity * 1000, "mass");
            case "g":
                return new ConvertedQuantity(quantity, "mass");
            case "l":
                return new ConvertedQuantity(quantity * 1000, "volume");
            case "ml":
                return new ConvertedQuantity(quantity, "volume");
            case "cup":
                return new ConvertedQuantity(quantity * 48, "spoon");
            case "tbsp":
                return new ConvertedQuantity(quantity * 3, "spoon");
            case "tsp":
                return new ConvertedQuantity(quantity, "spoon");
            default:
                return new ConvertedQuantity(quantity, normalisedUnit);
        }
    }

    private static String normalise(String value) {
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    // Normalise simple plural forms so everyday variations such as "tomato" and
    // "tomatoes" are treated as the same ingredient during recipe matching.
    private static String normaliseIngredientName(String ingredientName) {
        String normalisedName = normalise(ingredientName);
        int lastSpace = normalisedName.lastIndexOf(' ');
        String prefix = lastSpace == -1 ? "" : normalisedName.substring(0, lastSpace + 1);
        String finalWord = lastSpace == -1
                ? normalisedName
                : normalisedName.substring(lastSpace + 1);

        if ("leaves".equals(finalWord)) {
            finalWord = "leaf";
        } else if (finalWord.endsWith("ies") && finalWord.length() > 3) {
            finalWord = finalWord.substring(0, finalWord.length() - 3) + "y";
        } else if (finalWord.endsWith("oes") && finalWord.length() > 3) {
            finalWord = finalWord.substring(0, finalWord.length() - 2);
        } else if (finalWord.endsWith("s") && !finalWord.endsWith("ss")
                && finalWord.length() > 1) {
            finalWord = finalWord.substring(0, finalWord.length() - 1);
        }

        return prefix + finalWord;
    }

    private static class ConvertedQuantity {
        private final double quantity;
        private final String unitGroup;

        private ConvertedQuantity(double quantity, String unitGroup) {
            this.quantity = quantity;
            this.unitGroup = unitGroup;
        }
    }
}
