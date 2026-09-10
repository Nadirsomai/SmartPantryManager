package com.example.smartpantrymanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.adapter.PantryItemAdapter;
import com.example.smartpantrymanager.data.PantryDatabaseHelper;
import com.example.smartpantrymanager.model.PantryItem;
import com.example.smartpantrymanager.navigation.NavigationHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private PantryDatabaseHelper databaseHelper;
    private PantryItemAdapter pantryItemAdapter;
    private RecyclerView pantryList;
    private View pantryEmptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        WindowInsetsControllerCompat systemBarController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        systemBarController.setAppearanceLightStatusBars(false);
        systemBarController.setAppearanceLightNavigationBars(false);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseHelper = new PantryDatabaseHelper(getApplicationContext());
        databaseHelper.getWritableDatabase();

        pantryList = findViewById(R.id.list_pantry_items);
        pantryEmptyState = findViewById(R.id.pantry_empty_state);
        pantryItemAdapter = new PantryItemAdapter(this::openIngredientForEditing);
        pantryList.setLayoutManager(new LinearLayoutManager(this));
        pantryList.setAdapter(pantryItemAdapter);

        MaterialToolbar navigationToolbar = findViewById(R.id.toolbar_navigation);
        NavigationHelper.setupToolbar(this, navigationToolbar, R.id.navigation_pantry);

        MaterialButton addIngredientButton = findViewById(R.id.button_add_ingredient);
        addIngredientButton.setOnClickListener(view -> {
            Intent addIngredientIntent =
                    new Intent(MainActivity.this, AddIngredientActivity.class);
            startActivity(addIngredientIntent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

    }

    private void openIngredientForEditing(PantryItem pantryItem) {
        Intent editIngredientIntent = new Intent(this, AddIngredientActivity.class);
        editIngredientIntent.putExtra(AddIngredientActivity.EXTRA_PANTRY_ITEM_ID,
                pantryItem.getId());
        startActivity(editIngredientIntent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    protected void onResume() {
        super.onResume();
        displayPantryItems();
    }

    private void displayPantryItems() {
        SharedPreferences settings = getSharedPreferences(
                SettingsActivity.PREFERENCES_NAME, MODE_PRIVATE);
        pantryItemAdapter.setExpiryWarningSettings(
                settings.getBoolean(SettingsActivity.KEY_EXPIRY_ALERTS,
                        SettingsActivity.DEFAULT_EXPIRY_ALERTS),
                settings.getInt(SettingsActivity.KEY_EXPIRY_WARNING_DAYS,
                        SettingsActivity.DEFAULT_EXPIRY_WARNING_DAYS));

        List<PantryItem> pantryItems = databaseHelper.getAllPantryItems();
        pantryItemAdapter.setPantryItems(pantryItems);

        boolean pantryIsEmpty = pantryItems.isEmpty();
        pantryEmptyState.setVisibility(pantryIsEmpty ? View.VISIBLE : View.GONE);
        pantryList.setVisibility(pantryIsEmpty ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        databaseHelper.close();
        super.onDestroy();
    }
}
