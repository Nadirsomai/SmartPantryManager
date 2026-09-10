package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.adapter.RecipeAdapter;
import com.example.smartpantrymanager.data.PantryDatabaseHelper;
import com.example.smartpantrymanager.logic.RecipeMatcher;
import com.example.smartpantrymanager.model.PantryItem;
import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.navigation.NavigationHelper;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

public class SuggestedRecipesActivity extends AppCompatActivity {

    private PantryDatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        WindowInsetsControllerCompat systemBarController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        systemBarController.setAppearanceLightStatusBars(false);
        systemBarController.setAppearanceLightNavigationBars(false);

        setContentView(R.layout.activity_suggested_recipes);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.suggested_recipes_root),
                (view, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    view.setPadding(systemBars.left, systemBars.top,
                            systemBars.right, systemBars.bottom);
                    return insets;
                });

        databaseHelper = new PantryDatabaseHelper(getApplicationContext());
        MaterialToolbar navigationToolbar = findViewById(R.id.toolbar_navigation);
        NavigationHelper.setupToolbar(this, navigationToolbar, R.id.navigation_recipes);
        displaySuggestedRecipes();
    }

    private void displaySuggestedRecipes() {
        List<PantryItem> pantryItems = databaseHelper.getAllPantryItems();
        List<Recipe> matchingRecipes = new ArrayList<>();

        for (Recipe recipe : databaseHelper.getAllRecipes()) {
            if (RecipeMatcher.canMakeRecipe(
                    pantryItems,
                    databaseHelper.getRecipeIngredients(recipe.getId()))) {
                matchingRecipes.add(recipe);
            }
        }

        RecyclerView recipeList = findViewById(R.id.list_suggested_recipes);
        View emptyState = findViewById(R.id.suggested_recipes_empty_state);
        TextView resultCount = findViewById(R.id.text_recipe_result_count);

        recipeList.setLayoutManager(new LinearLayoutManager(this));
        recipeList.setAdapter(new RecipeAdapter(matchingRecipes, this::openRecipeDetails));

        boolean hasMatches = !matchingRecipes.isEmpty();
        recipeList.setVisibility(hasMatches ? View.VISIBLE : View.GONE);
        emptyState.setVisibility(hasMatches ? View.GONE : View.VISIBLE);
        resultCount.setText(getResources().getQuantityString(
                R.plurals.recipe_result_count,
                matchingRecipes.size(),
                matchingRecipes.size()));
    }

    private void openRecipeDetails(Recipe recipe) {
        Intent recipeDetailsIntent = new Intent(this, RecipeDetailActivity.class);
        recipeDetailsIntent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
        startActivity(recipeDetailsIntent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    protected void onDestroy() {
        databaseHelper.close();
        super.onDestroy();
    }
}
