package com.example.smartpantrymanager.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.smartpantrymanager.model.PantryItem;
import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.List;

public class PantryDatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "smart_pantry.db";
    public static final int DATABASE_VERSION = 3;

    public static final String TABLE_PANTRY_ITEMS = "pantry_items";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_QUANTITY = "quantity";
    public static final String COLUMN_UNIT = "unit";
    public static final String COLUMN_EXPIRY_DATE = "expiry_date";

    public static final String TABLE_RECIPES = "recipes";
    public static final String COLUMN_RECIPE_NAME = "recipe_name";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_PREPARATION = "preparation";
    public static final String COLUMN_INSTRUCTIONS = "instructions";

    public static final String TABLE_RECIPE_INGREDIENTS = "recipe_ingredients";
    public static final String COLUMN_RECIPE_ID = "recipe_id";
    public static final String COLUMN_INGREDIENT_NAME = "ingredient_name";

    // Pantry items are stored separately from recipes. Recipe ingredients use the
    // recipe ID as a foreign key so each recipe can contain multiple ingredients.
    private static final String CREATE_PANTRY_TABLE =
            "CREATE TABLE " + TABLE_PANTRY_ITEMS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " TEXT NOT NULL COLLATE NOCASE, " +
                    COLUMN_QUANTITY + " REAL NOT NULL CHECK (" + COLUMN_QUANTITY + " > 0), " +
                    COLUMN_UNIT + " TEXT NOT NULL, " +
                    COLUMN_EXPIRY_DATE + " TEXT NOT NULL)";

    private static final String CREATE_RECIPE_TABLE =
            "CREATE TABLE " + TABLE_RECIPES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_RECIPE_NAME + " TEXT NOT NULL UNIQUE COLLATE NOCASE, " +
                    COLUMN_DESCRIPTION + " TEXT NOT NULL, " +
                    COLUMN_PREPARATION + " TEXT NOT NULL, " +
                    COLUMN_INSTRUCTIONS + " TEXT NOT NULL)";

    private static final String CREATE_RECIPE_INGREDIENT_TABLE =
            "CREATE TABLE " + TABLE_RECIPE_INGREDIENTS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_RECIPE_ID + " INTEGER NOT NULL, " +
                    COLUMN_INGREDIENT_NAME + " TEXT NOT NULL COLLATE NOCASE, " +
                    COLUMN_QUANTITY + " REAL NOT NULL CHECK (" + COLUMN_QUANTITY + " > 0), " +
                    COLUMN_UNIT + " TEXT NOT NULL, " +
                    "FOREIGN KEY (" + COLUMN_RECIPE_ID + ") REFERENCES " +
                    TABLE_RECIPES + "(" + COLUMN_ID + ") ON DELETE CASCADE)";

    public PantryDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public long addPantryItem(PantryItem pantryItem) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, pantryItem.getName());
        values.put(COLUMN_QUANTITY, pantryItem.getQuantity());
        values.put(COLUMN_UNIT, pantryItem.getUnit());
        values.put(COLUMN_EXPIRY_DATE, pantryItem.getExpiryDate());

        long newItemId = getWritableDatabase().insert(TABLE_PANTRY_ITEMS, null, values);
        pantryItem.setId(newItemId);
        return newItemId;
    }

    public PantryItem getPantryItem(long itemId) {
        String[] columns = {
                COLUMN_ID,
                COLUMN_NAME,
                COLUMN_QUANTITY,
                COLUMN_UNIT,
                COLUMN_EXPIRY_DATE
        };

        try (Cursor cursor = getReadableDatabase().query(
                TABLE_PANTRY_ITEMS,
                columns,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(itemId)},
                null,
                null,
                null)) {
            if (cursor.moveToFirst()) {
                return readPantryItem(cursor);
            }
        }

        return null;
    }

    public int updatePantryItem(PantryItem pantryItem) {
        ContentValues values = createPantryValues(pantryItem);
        return getWritableDatabase().update(
                TABLE_PANTRY_ITEMS,
                values,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(pantryItem.getId())});
    }

    public int deletePantryItem(long itemId) {
        return getWritableDatabase().delete(
                TABLE_PANTRY_ITEMS,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(itemId)});
    }

    public List<PantryItem> getAllPantryItems() {
        List<PantryItem> pantryItems = new ArrayList<>();
        String[] columns = {
                COLUMN_ID,
                COLUMN_NAME,
                COLUMN_QUANTITY,
                COLUMN_UNIT,
                COLUMN_EXPIRY_DATE
        };

        try (Cursor cursor = getReadableDatabase().query(
                TABLE_PANTRY_ITEMS,
                columns,
                null,
                null,
                null,
                null,
                COLUMN_NAME + " COLLATE NOCASE ASC")) {

            int idIndex = cursor.getColumnIndexOrThrow(COLUMN_ID);
            int nameIndex = cursor.getColumnIndexOrThrow(COLUMN_NAME);
            int quantityIndex = cursor.getColumnIndexOrThrow(COLUMN_QUANTITY);
            int unitIndex = cursor.getColumnIndexOrThrow(COLUMN_UNIT);
            int expiryIndex = cursor.getColumnIndexOrThrow(COLUMN_EXPIRY_DATE);

            while (cursor.moveToNext()) {
                pantryItems.add(new PantryItem(
                        cursor.getLong(idIndex),
                        cursor.getString(nameIndex),
                        cursor.getDouble(quantityIndex),
                        cursor.getString(unitIndex),
                        cursor.getString(expiryIndex)));
            }
        }

        return pantryItems;
    }

    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        String[] columns = {
                COLUMN_ID,
                COLUMN_RECIPE_NAME,
                COLUMN_DESCRIPTION,
                COLUMN_PREPARATION,
                COLUMN_INSTRUCTIONS
        };

        try (Cursor cursor = getReadableDatabase().query(
                TABLE_RECIPES,
                columns,
                null,
                null,
                null,
                null,
                COLUMN_RECIPE_NAME + " COLLATE NOCASE ASC")) {
            while (cursor.moveToNext()) {
                recipes.add(new Recipe(
                        cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PREPARATION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INSTRUCTIONS))));
            }
        }

        return recipes;
    }

    public Recipe getRecipe(long recipeId) {
        String[] columns = {
                COLUMN_ID,
                COLUMN_RECIPE_NAME,
                COLUMN_DESCRIPTION,
                COLUMN_PREPARATION,
                COLUMN_INSTRUCTIONS
        };

        try (Cursor cursor = getReadableDatabase().query(
                TABLE_RECIPES,
                columns,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(recipeId)},
                null,
                null,
                null)) {
            if (cursor.moveToFirst()) {
                return new Recipe(
                        cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PREPARATION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INSTRUCTIONS)));
            }
        }

        return null;
    }

    public List<RecipeIngredient> getRecipeIngredients(long recipeId) {
        List<RecipeIngredient> ingredients = new ArrayList<>();
        String[] columns = {
                COLUMN_RECIPE_ID,
                COLUMN_INGREDIENT_NAME,
                COLUMN_QUANTITY,
                COLUMN_UNIT
        };

        try (Cursor cursor = getReadableDatabase().query(
                TABLE_RECIPE_INGREDIENTS,
                columns,
                COLUMN_RECIPE_ID + " = ?",
                new String[]{String.valueOf(recipeId)},
                null,
                null,
                COLUMN_ID + " ASC")) {
            while (cursor.moveToNext()) {
                ingredients.add(new RecipeIngredient(
                        cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INGREDIENT_NAME)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_QUANTITY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UNIT))));
            }
        }

        return ingredients;
    }

    public List<String> getRecipeIngredientNames() {
        List<String> ingredientNames = new ArrayList<>();
        String[] columns = {COLUMN_INGREDIENT_NAME};

        try (Cursor cursor = getReadableDatabase().query(
                true,
                TABLE_RECIPE_INGREDIENTS,
                columns,
                null,
                null,
                null,
                null,
                COLUMN_INGREDIENT_NAME + " COLLATE NOCASE ASC",
                null)) {
            int nameIndex = cursor.getColumnIndexOrThrow(COLUMN_INGREDIENT_NAME);
            while (cursor.moveToNext()) {
                ingredientNames.add(cursor.getString(nameIndex));
            }
        }

        return ingredientNames;
    }

    private ContentValues createPantryValues(PantryItem pantryItem) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, pantryItem.getName());
        values.put(COLUMN_QUANTITY, pantryItem.getQuantity());
        values.put(COLUMN_UNIT, pantryItem.getUnit());
        values.put(COLUMN_EXPIRY_DATE, pantryItem.getExpiryDate());
        return values;
    }

    private PantryItem readPantryItem(Cursor cursor) {
        return new PantryItem(
                cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_QUANTITY)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UNIT)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXPIRY_DATE)));
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(CREATE_PANTRY_TABLE);
        createAndSeedRecipeTables(database);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            createAndSeedRecipeTables(database);
        }
        if (oldVersion < 3) {
            if (oldVersion >= 2) {
                database.execSQL("ALTER TABLE " + TABLE_RECIPES + " ADD COLUMN " +
                        COLUMN_PREPARATION + " TEXT NOT NULL DEFAULT ''");
            }
            database.delete(TABLE_RECIPE_INGREDIENTS, null, null);
            database.delete(TABLE_RECIPES, null, null);
            RecipeSeeder.seedRecipes(database);
        }
    }

    @Override
    public void onConfigure(SQLiteDatabase database) {
        super.onConfigure(database);
        database.setForeignKeyConstraintsEnabled(true);
    }

    // The recipe collection is added when the database is first created or upgraded.
    // This gives every user the same starting recipes without requiring manual entry.
    private void createAndSeedRecipeTables(SQLiteDatabase database) {
        database.execSQL(CREATE_RECIPE_TABLE);
        database.execSQL(CREATE_RECIPE_INGREDIENT_TABLE);
        RecipeSeeder.seedRecipes(database);
    }
}
