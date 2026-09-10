package com.example.smartpantrymanager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.example.smartpantrymanager.navigation.NavigationHelper;

public class SettingsActivity extends AppCompatActivity {

    public static final String PREFERENCES_NAME = "pantry_settings";
    public static final String KEY_EXPIRY_ALERTS = "expiry_alerts";
    public static final String KEY_EXPIRY_WARNING_DAYS = "expiry_warning_days";
    public static final boolean DEFAULT_EXPIRY_ALERTS = true;
    public static final int DEFAULT_EXPIRY_WARNING_DAYS = 7;

    private SwitchMaterial expiryAlertsSwitch;
    private Spinner warningDaysSpinner;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        WindowInsetsControllerCompat systemBarController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        systemBarController.setAppearanceLightStatusBars(false);
        systemBarController.setAppearanceLightNavigationBars(false);

        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.settings_root),
                (view, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    view.setPadding(systemBars.left, systemBars.top,
                            systemBars.right, systemBars.bottom);
                    return insets;
                });

        expiryAlertsSwitch = findViewById(R.id.switch_expiry_alerts);
        warningDaysSpinner = findViewById(R.id.spinner_expiry_warning_days);
        preferences = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);

        MaterialToolbar navigationToolbar = findViewById(R.id.toolbar_navigation);
        NavigationHelper.setupToolbar(this, navigationToolbar, R.id.navigation_settings);

        loadSettings();
        expiryAlertsSwitch.setOnCheckedChangeListener((button, checked) ->
                warningDaysSpinner.setEnabled(checked));

        MaterialButton saveButton = findViewById(R.id.button_save_settings);
        saveButton.setOnClickListener(view -> saveSettings());
    }

    private void loadSettings() {
        boolean alertsEnabled = preferences.getBoolean(
                KEY_EXPIRY_ALERTS, DEFAULT_EXPIRY_ALERTS);
        int warningDays = preferences.getInt(
                KEY_EXPIRY_WARNING_DAYS, DEFAULT_EXPIRY_WARNING_DAYS);

        expiryAlertsSwitch.setChecked(alertsEnabled);
        warningDaysSpinner.setSelection(findWarningDaysPosition(warningDays));
        warningDaysSpinner.setEnabled(alertsEnabled);
    }

    private int findWarningDaysPosition(int savedDays) {
        int[] warningDayValues = getResources().getIntArray(R.array.expiry_warning_day_values);
        for (int position = 0; position < warningDayValues.length; position++) {
            if (warningDayValues[position] == savedDays) {
                return position;
            }
        }
        return 1;
    }

    private void saveSettings() {
        int[] warningDayValues = getResources().getIntArray(R.array.expiry_warning_day_values);
        int selectedDays = warningDayValues[warningDaysSpinner.getSelectedItemPosition()];

        preferences.edit()
                .putBoolean(KEY_EXPIRY_ALERTS, expiryAlertsSwitch.isChecked())
                .putInt(KEY_EXPIRY_WARNING_DAYS, selectedDays)
                .apply();

        Toast.makeText(this, R.string.settings_saved, Toast.LENGTH_SHORT).show();
        finish();
    }
}
