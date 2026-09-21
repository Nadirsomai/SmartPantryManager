# Smart Pantry Manager

Smart Pantry Manager is an Android application that I created for my Mobile App Development 700 assignment.

The purpose of the app is to help a user keep track of the ingredients they have at home. The app can then check those ingredients against a collection of recipes and show which meals can be made.

I chose a Local SA food theme because I wanted the application to feel more personal and connected to the food I know. The recipes include meals such as Durban mutton curry, bunny chow, chicken breyani, bobotie, chakalaka, milk tart and koeksisters.

## What the app can do

- Add pantry ingredients with a name, quantity, unit and expiry date.
- Suggest ingredient names while the user is typing.
- Save pantry items locally using SQLite.
- View, edit and delete saved ingredients.
- Warn the user when an ingredient is close to expiring.
- Allow the expiry warning period to be changed in Settings.
- Match the pantry contents against 15 stored South African recipes.
- Check both the ingredient quantity and measurement unit when matching a recipe.
- Display the ingredients, preparation and cooking instructions for a matched recipe.

## Design

The app uses a black, white and gold colour theme. I used personalised chef illustrations throughout the screens to keep the design consistent and make the application feel like my own.

The main sections are:

1. Welcome screen
2. My Pantry
3. Add or Edit Ingredient
4. Suggested Recipes
5. Recipe Details
6. Settings

## How it was built

The application was developed in Android Studio using:

- Java
- XML layouts
- Activities and Intents
- SQLiteOpenHelper
- RecyclerView and custom adapters
- SharedPreferences for the expiry settings
- JUnit tests for expiry checks and recipe matching

I chose SQLite because the pantry needs to work offline and keep the user's ingredients after the app is closed. It stores the data directly on the device, so the application does not need a separate server or user account.

The minimum supported Android version is Android 7.0 (API 25).

## How to run the project

1. Open Android Studio.
2. Select **Open** and choose the `SmartPantryManager` project folder.
3. Allow Gradle to finish syncing.
4. Start an Android emulator or connect an Android device.
5. Select the `app` run configuration.
6. Click **Run**.

No internet connection or user account is required because the pantry and recipe information is stored locally on the device.

## Testing

I tested the main user journey by adding, editing and deleting ingredients, restarting the app to confirm that items remained stored, changing the expiry-warning settings and checking the suggested recipes.

The project also contains unit tests for:

- Ingredient quantities and unit conversions used for recipe matching.
- Ingredient-name normalisation.
- Expired ingredients and upcoming expiry dates.

## Author

Nadir Somai

Mobile App Development 700
