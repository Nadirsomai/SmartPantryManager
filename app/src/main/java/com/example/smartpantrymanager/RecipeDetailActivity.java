package com.example.smartpantrymanager;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.adapter.RecipeIngredientAdapter;
import com.example.smartpantrymanager.data.PantryDatabaseHelper;
import com.example.smartpantrymanager.model.Recipe;

public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "recipe_id";

    private PantryDatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        WindowInsetsControllerCompat systemBarController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        systemBarController.setAppearanceLightStatusBars(false);
        systemBarController.setAppearanceLightNavigationBars(false);

        setContentView(R.layout.activity_recipe_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.recipe_detail_root),
                (view, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    view.setPadding(systemBars.left, systemBars.top,
                            systemBars.right, systemBars.bottom);
                    return insets;
                });

        databaseHelper = new PantryDatabaseHelper(getApplicationContext());
        long recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);
        Recipe recipe = databaseHelper.getRecipe(recipeId);
        if (recipe == null) {
            Toast.makeText(this, R.string.recipe_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TextView recipeName = findViewById(R.id.text_recipe_detail_name);
        TextView recipeDescription = findViewById(R.id.text_recipe_detail_description);
        TextView recipeInstructions = findViewById(R.id.text_recipe_instructions);
        RecyclerView ingredientList = findViewById(R.id.list_recipe_ingredients);

        recipeName.setText(recipe.getName());
        recipeDescription.setText(recipe.getDescription());
        recipeInstructions.setText(recipe.getInstructions());
        ingredientList.setLayoutManager(new LinearLayoutManager(this));
        ingredientList.setAdapter(new RecipeIngredientAdapter(
                databaseHelper.getRecipeIngredients(recipe.getId())));
        ingredientList.setNestedScrollingEnabled(false);
    }

    @Override
    protected void onDestroy() {
        databaseHelper.close();
        super.onDestroy();
    }
}
