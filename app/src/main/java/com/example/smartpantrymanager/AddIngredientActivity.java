package com.example.smartpantrymanager;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.smartpantrymanager.data.PantryDatabaseHelper;
import com.example.smartpantrymanager.model.PantryItem;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class AddIngredientActivity extends AppCompatActivity {

    public static final String EXTRA_PANTRY_ITEM_ID = "pantry_item_id";

    private TextInputLayout nameLayout;
    private TextInputLayout quantityLayout;
    private TextInputLayout expiryLayout;
    private AutoCompleteTextView nameInput;
    private TextInputEditText quantityInput;
    private TextInputEditText expiryInput;
    private Spinner unitInput;
    private TextView unitError;
    private PantryDatabaseHelper databaseHelper;
    private PantryItem pantryItemBeingEdited;
    private boolean isFormattingExpiryDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        WindowInsetsControllerCompat systemBarController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        systemBarController.setAppearanceLightStatusBars(false);
        systemBarController.setAppearanceLightNavigationBars(false);

        setContentView(R.layout.activity_add_ingredient);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.add_ingredient_root),
                (view, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    view.setPadding(systemBars.left, systemBars.top,
                            systemBars.right, systemBars.bottom);
                    return insets;
                });

        nameLayout = findViewById(R.id.layout_ingredient_name);
        quantityLayout = findViewById(R.id.layout_ingredient_quantity);
        expiryLayout = findViewById(R.id.layout_ingredient_expiry);
        nameInput = findViewById(R.id.input_ingredient_name);
        quantityInput = findViewById(R.id.input_ingredient_quantity);
        expiryInput = findViewById(R.id.input_ingredient_expiry);
        unitInput = findViewById(R.id.input_ingredient_unit);
        unitError = findViewById(R.id.text_unit_error);

        databaseHelper = new PantryDatabaseHelper(getApplicationContext());
        setUpIngredientSuggestions();

        long pantryItemId = getIntent().getLongExtra(EXTRA_PANTRY_ITEM_ID, PantryItem.NO_ID);
        if (pantryItemId != PantryItem.NO_ID) {
            loadPantryItem(pantryItemId);
        }

        expiryInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {
                // No action is needed before the text changes.
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                // Formatting is applied after the text changes.
            }

            @Override
            public void afterTextChanged(Editable expiryText) {
                formatExpiryDate(expiryText);
            }
        });

        MaterialButton saveIngredientButton = findViewById(R.id.button_save_ingredient);
        saveIngredientButton.setOnClickListener(view -> saveIngredient());

        MaterialButton deleteIngredientButton = findViewById(R.id.button_delete_ingredient);
        if (pantryItemBeingEdited != null) {
            saveIngredientButton.setText(R.string.update_ingredient);
            deleteIngredientButton.setVisibility(View.VISIBLE);
            deleteIngredientButton.setOnClickListener(view -> confirmDeleteIngredient());
        }
    }

    private void setUpIngredientSuggestions() {
        ArrayAdapter<String> suggestionAdapter = new ArrayAdapter<>(
                this,
                R.layout.item_ingredient_suggestion,
                databaseHelper.getRecipeIngredientNames());
        nameInput.setAdapter(suggestionAdapter);
        nameInput.setThreshold(1);
    }

    private void loadPantryItem(long pantryItemId) {
        pantryItemBeingEdited = databaseHelper.getPantryItem(pantryItemId);
        if (pantryItemBeingEdited == null) {
            Toast.makeText(this, R.string.ingredient_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        nameInput.setText(pantryItemBeingEdited.getName(), false);
        quantityInput.setText(String.valueOf(pantryItemBeingEdited.getQuantity()));
        expiryInput.setText(pantryItemBeingEdited.getExpiryDate());
        selectUnit(pantryItemBeingEdited.getUnit());
    }

    private void selectUnit(String unit) {
        for (int position = 0; position < unitInput.getCount(); position++) {
            if (unit.equals(unitInput.getItemAtPosition(position).toString())) {
                unitInput.setSelection(position);
                return;
            }
        }
    }

    private void formatExpiryDate(Editable expiryText) {
        if (isFormattingExpiryDate) {
            return;
        }

        isFormattingExpiryDate = true;
        String digits = expiryText.toString().replaceAll("\\D", "");
        if (digits.length() > 8) {
            digits = digits.substring(0, 8);
        }

        StringBuilder formattedDate = new StringBuilder();
        int dayLength = Math.min(digits.length(), 2);
        formattedDate.append(digits, 0, dayLength);

        if (digits.length() >= 2) {
            formattedDate.append('/');
        }
        if (digits.length() > 2) {
            int monthLength = Math.min(digits.length(), 4);
            formattedDate.append(digits, 2, monthLength);
        }
        if (digits.length() >= 4) {
            formattedDate.append('/');
        }
        if (digits.length() > 4) {
            formattedDate.append(digits.substring(4));
        }

        String formattedText = formattedDate.toString();
        if (!formattedText.equals(expiryText.toString())) {
            expiryText.replace(0, expiryText.length(), formattedText);
        }
        isFormattingExpiryDate = false;
    }

    private void saveIngredient() {
        clearValidationErrors();

        String name = readText(nameInput).replaceAll("\\s+", " ");
        String quantityText = readText(quantityInput);
        String expiryDate = readText(expiryInput);
        String unit = unitInput.getSelectedItem().toString();
        boolean isValid = true;

        if (name.isEmpty()) {
            nameLayout.setError(getString(R.string.ingredient_name_required));
            isValid = false;
        }

        double quantity = 0;
        try {
            quantity = Double.parseDouble(quantityText);
            if (!Double.isFinite(quantity) || quantity <= 0) {
                quantityLayout.setError(getString(R.string.ingredient_quantity_positive));
                isValid = false;
            }
        } catch (NumberFormatException exception) {
            quantityLayout.setError(getString(R.string.ingredient_quantity_required));
            isValid = false;
        }

        if (unitInput.getSelectedItemPosition() == 0) {
            unitError.setVisibility(View.VISIBLE);
            isValid = false;
        }

        if (expiryDate.isEmpty()) {
            expiryLayout.setError(getString(R.string.ingredient_expiry_required));
            isValid = false;
        } else if (!isValidExpiryDate(expiryDate)) {
            expiryLayout.setError(getString(R.string.ingredient_expiry_invalid));
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        PantryItem pantryItem = pantryItemBeingEdited == null
                ? new PantryItem(name, quantity, unit, expiryDate)
                : new PantryItem(pantryItemBeingEdited.getId(), name, quantity, unit, expiryDate);
        boolean saved = pantryItemBeingEdited == null
                ? databaseHelper.addPantryItem(pantryItem) != -1
                : databaseHelper.updatePantryItem(pantryItem) == 1;

        if (!saved) {
            Toast.makeText(this, R.string.ingredient_save_failed, Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(this,
                getString(pantryItemBeingEdited == null
                                ? R.string.ingredient_saved
                                : R.string.ingredient_updated,
                        pantryItem.getName()),
                Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    private void confirmDeleteIngredient() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_ingredient_title)
                .setMessage(getString(R.string.delete_ingredient_message,
                        pantryItemBeingEdited.getName()))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) -> deleteIngredient())
                .show();
    }

    private void deleteIngredient() {
        int deletedRows = databaseHelper.deletePantryItem(pantryItemBeingEdited.getId());
        if (deletedRows != 1) {
            Toast.makeText(this, R.string.ingredient_delete_failed, Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(this,
                getString(R.string.ingredient_deleted, pantryItemBeingEdited.getName()),
                Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    private boolean isValidExpiryDate(String expiryDate) {
        if (!expiryDate.matches("\\d{2}/\\d{2}/\\d{4}")) {
            return false;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(expiryDate);
            return true;
        } catch (ParseException exception) {
            return false;
        }
    }

    private String readText(TextView input) {
        CharSequence text = input.getText();
        return text == null ? "" : text.toString().trim();
    }

    private void clearValidationErrors() {
        nameLayout.setError(null);
        quantityLayout.setError(null);
        expiryLayout.setError(null);
        unitError.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        databaseHelper.close();
        super.onDestroy();
    }
}
