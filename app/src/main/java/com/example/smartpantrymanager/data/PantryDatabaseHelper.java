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
    public static final int DATABASE_VERSION = 2;

    public static final String TABLE_PANTRY_ITEMS = "pantry_items";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_QUANTITY = "quantity";
    public static final String COLUMN_UNIT = "unit";
    public static final String COLUMN_EXPIRY_DATE = "expiry_date";

    public static final String TABLE_RECIPES = "recipes";
    public static final String COLUMN_RECIPE_NAME = "recipe_name";
    public static final String COLUMN_DESCRIPTION = "description";
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

        addRecipe(database, "Durban Mutton Curry",
                "A fragrant KwaZulu-Natal curry with tender mutton and potatoes.",
                "Brown the mutton. Fry onion with curry powder, add tomato and simmer. " +
                        "Add potatoes and cook until the meat is tender.",
                ingredient("mutton", 500, "g"), ingredient("potato", 3, "item"),
                ingredient("onion", 1, "item"), ingredient("tomato", 2, "item"),
                ingredient("curry powder", 2, "tbsp"));
        addRecipe(database, "Mutton Bunny Chow",
                "Durban curry served inside a hollowed loaf of bread.",
                "Cook the mutton curry until rich and tender. Hollow the bread, spoon in the " +
                        "curry and serve with the bread centre.",
                ingredient("bread", 1, "item"), ingredient("mutton", 500, "g"),
                ingredient("potato", 2, "item"), ingredient("onion", 1, "item"),
                ingredient("curry powder", 2, "tbsp"));
        addRecipe(database, "Bean Bunny Chow",
                "A meat-free Durban bunny filled with curried sugar beans.",
                "Fry onion and curry powder, add tomato and beans, then simmer until thick. " +
                        "Serve inside hollowed bread.",
                ingredient("bread", 1, "item"), ingredient("sugar beans", 2, "cup"),
                ingredient("onion", 1, "item"), ingredient("tomato", 2, "item"),
                ingredient("curry powder", 1, "tbsp"));
        addRecipe(database, "Cape Malay Fish Curry",
                "A gently spiced Cape curry with fish and coconut milk.",
                "Soften the onion with curry powder. Add tomato and coconut milk, simmer, " +
                        "then add fish and cook gently until flaky.",
                ingredient("fish", 500, "g"), ingredient("coconut milk", 1, "tin"),
                ingredient("onion", 1, "item"), ingredient("tomato", 2, "item"),
                ingredient("curry powder", 1, "tbsp"));
        addRecipe(database, "Chicken Breyani",
                "Layered spiced chicken and rice in the South African Indian style.",
                "Marinate and brown the chicken. Layer with rice and potatoes, cover and " +
                        "steam gently until cooked through.",
                ingredient("chicken", 500, "g"), ingredient("rice", 2, "cup"),
                ingredient("potato", 3, "item"), ingredient("onion", 2, "item"),
                ingredient("breyani spice", 2, "tbsp"));
        addRecipe(database, "Bobotie",
                "Cape spiced mince baked beneath a savoury egg custard.",
                "Cook mince with onion and curry powder. Place in a dish, cover with beaten " +
                        "egg and milk, then bake until golden.",
                ingredient("beef mince", 500, "g"), ingredient("onion", 1, "item"),
                ingredient("egg", 2, "item"), ingredient("milk", 1, "cup"),
                ingredient("curry powder", 1, "tbsp"));
        addRecipe(database, "Tomato Bredie",
                "A slow-cooked Cape stew of mutton, tomato and potato.",
                "Brown the mutton and onion. Add tomato and simmer slowly, adding potatoes " +
                        "near the end until tender.",
                ingredient("mutton", 500, "g"), ingredient("tomato", 4, "item"),
                ingredient("potato", 3, "item"), ingredient("onion", 1, "item"));
        addRecipe(database, "Chakalaka",
                "A spicy vegetable relish enjoyed across South Africa.",
                "Fry onion and curry powder. Add carrots, peppers, tomato and beans, then " +
                        "simmer until the vegetables are tender.",
                ingredient("onion", 1, "item"), ingredient("carrot", 3, "item"),
                ingredient("green pepper", 1, "item"), ingredient("tomato", 3, "item"),
                ingredient("baked beans", 1, "tin"));
        addRecipe(database, "Pap and Tomato Relish",
                "Creamy maize meal served with a simple tomato and onion relish.",
                "Cook mealie meal with water until smooth. Fry onion and tomato separately " +
                        "and serve the relish over the pap.",
                ingredient("mealie meal", 2, "cup"), ingredient("tomato", 3, "item"),
                ingredient("onion", 1, "item"));
        addRecipe(database, "Samp and Beans",
                "A comforting traditional combination of samp and sugar beans.",
                "Soak the samp and beans, then simmer together until soft and creamy. " +
                        "Season and serve warm.",
                ingredient("samp", 2, "cup"), ingredient("sugar beans", 1, "cup"),
                ingredient("onion", 1, "item"));
        addRecipe(database, "Boerewors and Pap",
                "Grilled boerewors with pap and tomato relish.",
                "Grill the boerewors. Cook the mealie meal into pap and prepare an onion and " +
                        "tomato relish to serve alongside.",
                ingredient("boerewors", 500, "g"), ingredient("mealie meal", 2, "cup"),
                ingredient("tomato", 3, "item"), ingredient("onion", 1, "item"));
        addRecipe(database, "Vetkoek and Curried Mince",
                "Golden fried dough filled with gently spiced mince.",
                "Mix and prove the dough. Cook mince with onion and curry powder. Fry dough " +
                        "portions until golden and fill with mince.",
                ingredient("flour", 4, "cup"), ingredient("yeast", 1, "packet"),
                ingredient("beef mince", 500, "g"), ingredient("onion", 1, "item"),
                ingredient("curry powder", 1, "tbsp"));
        addRecipe(database, "Milk Tart",
                "A classic cinnamon-dusted South African custard tart.",
                "Bake the pastry shell. Heat milk, thicken with flour, sugar and egg, pour " +
                        "into the shell and chill before serving.",
                ingredient("milk", 1, "L"), ingredient("flour", 1, "cup"),
                ingredient("sugar", 1, "cup"), ingredient("egg", 2, "item"),
                ingredient("cinnamon", 1, "tsp"));
        addRecipe(database, "Koeksisters",
                "Braided fried pastries soaked in cold spiced syrup.",
                "Prepare and chill the syrup. Mix and braid the dough, fry until golden and " +
                        "dip immediately into the cold syrup.",
                ingredient("flour", 4, "cup"), ingredient("sugar", 3, "cup"),
                ingredient("cinnamon", 1, "tsp"), ingredient("oil", 1, "L"));
        addRecipe(database, "Masala Beans Curry",
                "A homestyle Durban bean curry finished with fresh coriander.",
                "Fry onion and curry powder, add tomato and beans, then simmer until thick. " +
                        "Finish with coriander.",
                ingredient("sugar beans", 2, "cup"), ingredient("onion", 1, "item"),
                ingredient("tomato", 2, "item"), ingredient("curry powder", 1, "tbsp"),
                ingredient("coriander", 1, "bunch"));
    }

    private void addRecipe(SQLiteDatabase database, String name, String description,
                           String instructions, RecipeSeedIngredient... ingredients) {
        ContentValues recipeValues = new ContentValues();
        recipeValues.put(COLUMN_RECIPE_NAME, name);
        recipeValues.put(COLUMN_DESCRIPTION, description);
        recipeValues.put(COLUMN_INSTRUCTIONS, instructions);
        long recipeId = database.insertOrThrow(TABLE_RECIPES, null, recipeValues);

        // Store each required ingredient with the new recipe ID so it can be retrieved
        // as part of that recipe and removed automatically if the recipe is deleted.
        for (RecipeSeedIngredient ingredient : ingredients) {
            ContentValues ingredientValues = new ContentValues();
            ingredientValues.put(COLUMN_RECIPE_ID, recipeId);
            ingredientValues.put(COLUMN_INGREDIENT_NAME, ingredient.name);
            ingredientValues.put(COLUMN_QUANTITY, ingredient.quantity);
            ingredientValues.put(COLUMN_UNIT, ingredient.unit);
            database.insertOrThrow(TABLE_RECIPE_INGREDIENTS, null, ingredientValues);
        }
    }

    private RecipeSeedIngredient ingredient(String name, double quantity, String unit) {
        return new RecipeSeedIngredient(name, quantity, unit);
    }

    private static class RecipeSeedIngredient {
        private final String name;
        private final double quantity;
        private final String unit;

        private RecipeSeedIngredient(String name, double quantity, String unit) {
            this.name = name;
            this.quantity = quantity;
            this.unit = unit;
        }
    }
}
