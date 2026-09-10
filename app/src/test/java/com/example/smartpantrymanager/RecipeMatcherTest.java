package com.example.smartpantrymanager;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.smartpantrymanager.logic.RecipeMatcher;
import com.example.smartpantrymanager.model.PantryItem;
import com.example.smartpantrymanager.model.RecipeIngredient;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RecipeMatcherTest {

    private static final long RECIPE_ID = 1;

    @Test
    public void completePantryMatchesRecipe() {
        List<PantryItem> pantry = Arrays.asList(
                new PantryItem("Mutton", 600, "g", "31/12/2027"),
                new PantryItem("Potato", 3, "item", "31/12/2027"));
        List<RecipeIngredient> recipe = Arrays.asList(
                new RecipeIngredient(RECIPE_ID, "mutton", 500, "g"),
                new RecipeIngredient(RECIPE_ID, "potato", 3, "item"));

        assertTrue(RecipeMatcher.canMakeRecipe(pantry, recipe));
    }

    @Test
    public void missingIngredientRejectsRecipe() {
        List<PantryItem> pantry = Collections.singletonList(
                new PantryItem("Mutton", 600, "g", "31/12/2027"));
        List<RecipeIngredient> recipe = Arrays.asList(
                new RecipeIngredient(RECIPE_ID, "mutton", 500, "g"),
                new RecipeIngredient(RECIPE_ID, "potato", 3, "item"));

        assertFalse(RecipeMatcher.canMakeRecipe(pantry, recipe));
    }

    @Test
    public void insufficientQuantityRejectsRecipe() {
        List<PantryItem> pantry = Collections.singletonList(
                new PantryItem("Mutton", 250, "g", "31/12/2027"));
        List<RecipeIngredient> recipe = Collections.singletonList(
                new RecipeIngredient(RECIPE_ID, "mutton", 500, "g"));

        assertFalse(RecipeMatcher.canMakeRecipe(pantry, recipe));
    }

    @Test
    public void duplicatePantryRowsAreCombined() {
        List<PantryItem> pantry = Arrays.asList(
                new PantryItem("Sugar Beans", 1, "cup", "31/12/2027"),
                new PantryItem(" sugar   beans ", 1, "CUP", "31/12/2027"));
        List<RecipeIngredient> recipe = Collections.singletonList(
                new RecipeIngredient(RECIPE_ID, "sugar beans", 2, "cup"));

        assertTrue(RecipeMatcher.canMakeRecipe(pantry, recipe));
    }

    @Test
    public void emptyRecipeDoesNotMatch() {
        assertFalse(RecipeMatcher.canMakeRecipe(
                Collections.singletonList(
                        new PantryItem("Mutton", 500, "g", "31/12/2027")),
                Collections.emptyList()));
    }

    @Test
    public void kilogramsCanSatisfyGramRequirement() {
        List<PantryItem> pantry = Collections.singletonList(
                new PantryItem("Mutton", 1, "kg", "31/12/2027"));
        List<RecipeIngredient> recipe = Collections.singletonList(
                new RecipeIngredient(RECIPE_ID, "mutton", 500, "g"));

        assertTrue(RecipeMatcher.canMakeRecipe(pantry, recipe));
    }

    @Test
    public void litresCanSatisfyMillilitreRequirement() {
        List<PantryItem> pantry = Collections.singletonList(
                new PantryItem("Oil", 1, "L", "31/12/2027"));
        List<RecipeIngredient> recipe = Collections.singletonList(
                new RecipeIngredient(RECIPE_ID, "oil", 750, "ml"));

        assertTrue(RecipeMatcher.canMakeRecipe(pantry, recipe));
    }

    @Test
    public void incompatibleUnitsStillRejectRecipe() {
        List<PantryItem> pantry = Collections.singletonList(
                new PantryItem("Mutton", 2, "item", "31/12/2027"));
        List<RecipeIngredient> recipe = Collections.singletonList(
                new RecipeIngredient(RECIPE_ID, "mutton", 500, "g"));

        assertFalse(RecipeMatcher.canMakeRecipe(pantry, recipe));
    }

    @Test
    public void commonPluralNamesMatchSingularRecipeNames() {
        List<PantryItem> pantry = Arrays.asList(
                new PantryItem("Tomatoes", 4, "item", "31/12/2027"),
                new PantryItem("Potatoes", 3, "item", "31/12/2027"),
                new PantryItem("Curry Leaves", 1, "bunch", "31/12/2027"));
        List<RecipeIngredient> recipe = Arrays.asList(
                new RecipeIngredient(RECIPE_ID, "tomato", 3, "item"),
                new RecipeIngredient(RECIPE_ID, "potato", 2, "item"),
                new RecipeIngredient(RECIPE_ID, "curry leaf", 1, "bunch"));

        assertTrue(RecipeMatcher.canMakeRecipe(pantry, recipe));
    }

    @Test
    public void pluralMatchingStillChecksQuantity() {
        List<PantryItem> pantry = Collections.singletonList(
                new PantryItem("Tomatoes", 1, "item", "31/12/2027"));
        List<RecipeIngredient> recipe = Collections.singletonList(
                new RecipeIngredient(RECIPE_ID, "tomato", 2, "item"));

        assertFalse(RecipeMatcher.canMakeRecipe(pantry, recipe));
    }
}
