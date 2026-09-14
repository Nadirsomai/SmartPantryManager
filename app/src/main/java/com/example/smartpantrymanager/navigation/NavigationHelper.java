package com.example.smartpantrymanager.navigation;

import android.app.Activity;
import android.content.Intent;

import com.example.smartpantrymanager.MainActivity;
import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.SettingsActivity;
import com.example.smartpantrymanager.SuggestedRecipesActivity;
import com.google.android.material.appbar.MaterialToolbar;

public final class NavigationHelper {

    private NavigationHelper() {
        // Utility class.
    }

    public static void startActivityWithFade(Activity activity, Intent intent) {
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    public static void setupToolbar(Activity activity, MaterialToolbar toolbar,
                                    int currentDestination) {
        toolbar.inflateMenu(R.menu.main_navigation_menu);
        toolbar.getMenu().findItem(currentDestination).setEnabled(false);
        toolbar.setOnMenuItemClickListener(item -> {
            int selectedItem = item.getItemId();
            if (selectedItem == currentDestination) {
                return true;
            }

            Class<?> destination;
            if (selectedItem == R.id.navigation_pantry) {
                destination = MainActivity.class;
            } else if (selectedItem == R.id.navigation_recipes) {
                destination = SuggestedRecipesActivity.class;
            } else if (selectedItem == R.id.navigation_settings) {
                destination = SettingsActivity.class;
            } else {
                return false;
            }

            Intent navigationIntent = new Intent(activity, destination);
            navigationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivityWithFade(activity, navigationIntent);
            return true;
        });
    }
}
