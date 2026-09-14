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
            seedRecipes(database);
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
        seedRecipes(database);
    }

    private void seedRecipes(SQLiteDatabase database) {
        addRecipe(database, "Durban Mutton Curry",
                "A rich KwaZulu-Natal curry with tender mutton, potatoes and aromatic spices.",
                steps("Cut the mutton into evenly sized pieces.",
                        "Peel and cut the potatoes.",
                        "Finely chop the onion and tomatoes.",
                        "Measure the curry powder and cooking oil."),
                steps("Heat the oil in a large pot.",
                        "Brown the mutton and temporarily remove it.",
                        "Cook the onion until soft, then stir in the curry powder for one minute.",
                        "Add the tomatoes and cook until a thick sauce forms.",
                        "Return the mutton, add enough water for simmering and cover until it starts to soften.",
                        "Add the potatoes, cook until tender, then thicken the sauce uncovered and serve."),
                ingredient("mutton", 500, "g"), ingredient("potato", 3, "item"),
                ingredient("onion", 1, "item"), ingredient("tomato", 2, "item"),
                ingredient("curry powder", 2, "tbsp"), ingredient("cooking oil", 2, "tbsp"));
        addRecipe(database, "Mutton Bunny Chow",
                "Durban curry served inside a hollowed loaf of bread.",
                steps("Cut the mutton and potatoes into small pieces.",
                        "Chop the onion and tomatoes.",
                        "Measure the curry powder and oil.",
                        "Cut the bread into portions without hollowing it yet."),
                steps("Heat the oil and brown the mutton.",
                        "Add the onion and cook until soft.",
                        "Stir in the curry powder, then add the tomatoes.",
                        "Add the potatoes and enough water for simmering.",
                        "Cover and cook until the meat and potatoes are tender.",
                        "Hollow out the bread, fill it with curry and serve with the bread centre."),
                ingredient("bread", 1, "item"), ingredient("mutton", 500, "g"),
                ingredient("potato", 2, "item"), ingredient("onion", 1, "item"),
                ingredient("tomato", 2, "item"), ingredient("curry powder", 2, "tbsp"),
                ingredient("cooking oil", 2, "tbsp"));
        addRecipe(database, "Bean Bunny Chow",
                "A meat-free Durban bunny filled with curried sugar beans.",
                steps("Drain the cooked sugar beans.",
                        "Chop the onion and tomatoes.",
                        "Measure the curry powder and oil.",
                        "Cut the bread into portions."),
                steps("Heat the oil and cook the onion until soft.",
                        "Add the curry powder and stir for one minute.",
                        "Add the tomatoes and cook until a sauce forms.",
                        "Add the sugar beans and simmer until the curry thickens.",
                        "Hollow out the bread and fill it with the bean curry.",
                        "Serve with the removed bread centre."),
                ingredient("bread", 1, "item"), ingredient("sugar beans", 2, "cup"),
                ingredient("onion", 1, "item"), ingredient("tomato", 2, "item"),
                ingredient("curry powder", 1, "tbsp"), ingredient("cooking oil", 2, "tbsp"));
        addRecipe(database, "Cape Malay Fish Curry",
                "A gently spiced Cape curry with fish and coconut milk.",
                steps("Cut the fish into large pieces.",
                        "Finely chop the onion and tomatoes.",
                        "Measure the coconut milk, curry powder and oil."),
                steps("Heat the oil in a wide pan.",
                        "Cook the onion until soft, then stir in the curry powder.",
                        "Add the tomatoes and cook until they break down.",
                        "Pour in the coconut milk and simmer gently.",
                        "Place the fish into the sauce and cover.",
                        "Cook gently until the fish flakes easily, avoiding excessive stirring."),
                ingredient("fish", 500, "g"), ingredient("coconut milk", 1, "tin"),
                ingredient("onion", 1, "item"), ingredient("tomato", 2, "item"),
                ingredient("curry powder", 1, "tbsp"), ingredient("cooking oil", 2, "tbsp"));
        addRecipe(database, "Chicken Breyani",
                "Layered spiced chicken and rice in the South African Indian style.",
                steps("Cut the chicken into portions and coat it with breyani spice.",
                        "Rinse the rice until the water runs mostly clear.",
                        "Peel and cut the potatoes.",
                        "Slice the onions and measure the oil."),
                steps("Heat the oil and brown the potatoes, then remove them.",
                        "Cook the onions until golden.",
                        "Add the chicken and cook until lightly browned.",
                        "Partially cook the rice in a separate pot.",
                        "Layer the rice, chicken and potatoes in a large pot.",
                        "Cover tightly and cook over low heat until tender, then mix gently before serving."),
                ingredient("chicken", 500, "g"), ingredient("rice", 2, "cup"),
                ingredient("potato", 3, "item"), ingredient("onion", 2, "item"),
                ingredient("breyani spice", 2, "tbsp"), ingredient("cooking oil", 3, "tbsp"));
        addRecipe(database, "Bobotie",
                "Cape spiced mince baked beneath a savoury egg custard.",
                steps("Chop the onion.",
                        "Beat the eggs and milk together.",
                        "Measure the curry powder and oil.",
                        "Preheat the oven to 180 degrees Celsius."),
                steps("Heat the oil and cook the onion until soft.",
                        "Add the curry powder and stir briefly.",
                        "Add the beef mince and cook until browned.",
                        "Transfer the mince mixture to a baking dish.",
                        "Pour the egg and milk mixture evenly over the mince.",
                        "Bake until the topping is set and golden, then rest briefly before serving."),
                ingredient("beef mince", 500, "g"), ingredient("onion", 1, "item"),
                ingredient("egg", 2, "item"), ingredient("milk", 1, "cup"),
                ingredient("curry powder", 1, "tbsp"), ingredient("cooking oil", 1, "tbsp"));
        addRecipe(database, "Tomato Bredie",
                "A slow-cooked Cape stew of mutton, tomato and potato.",
                steps("Cut the mutton into pieces.",
                        "Chop the tomatoes and onion.",
                        "Peel and cut the potatoes.",
                        "Measure the cooking oil."),
                steps("Heat the oil and brown the mutton.",
                        "Add the onion and cook until soft.",
                        "Add the tomatoes and mix thoroughly.",
                        "Cover and simmer until the mutton starts becoming tender.",
                        "Add the potatoes and continue cooking until everything is soft.",
                        "Simmer uncovered if the sauce needs to thicken."),
                ingredient("mutton", 500, "g"), ingredient("tomato", 4, "item"),
                ingredient("potato", 3, "item"), ingredient("onion", 1, "item"),
                ingredient("cooking oil", 2, "tbsp"));
        addRecipe(database, "Chakalaka",
                "A spicy vegetable relish enjoyed across South Africa.",
                steps("Chop the onion and green pepper.",
                        "Peel and grate the carrots.",
                        "Chop the tomatoes.",
                        "Measure the curry powder and cooking oil."),
                steps("Heat the oil in a large pan.",
                        "Cook the onion and green pepper until slightly soft.",
                        "Add the curry powder and stir briefly.",
                        "Add the carrots and cook for several minutes.",
                        "Add the tomatoes, then stir in the baked beans.",
                        "Simmer until the vegetables are tender and the mixture is thick."),
                ingredient("onion", 1, "item"), ingredient("carrot", 3, "item"),
                ingredient("green pepper", 1, "item"), ingredient("tomato", 3, "item"),
                ingredient("baked beans", 1, "tin"), ingredient("curry powder", 1, "tbsp"),
                ingredient("cooking oil", 2, "tbsp"));
        addRecipe(database, "Pap and Tomato Relish",
                "Creamy maize meal served with a simple tomato and onion relish.",
                steps("Measure the mealie meal.",
                        "Chop the tomatoes and onion.",
                        "Measure the cooking oil."),
                steps("Bring water to a gentle boil in a pot.",
                        "Gradually add the mealie meal while stirring.",
                        "Cover and cook over low heat, stirring occasionally.",
                        "Heat the oil in a separate pan and cook the onion until soft.",
                        "Add the tomatoes and simmer until a relish forms.",
                        "Serve the tomato relish over or alongside the pap."),
                ingredient("mealie meal", 2, "cup"), ingredient("tomato", 3, "item"),
                ingredient("onion", 1, "item"), ingredient("cooking oil", 1, "tbsp"));
        addRecipe(database, "Samp and Beans",
                "A comforting traditional combination of samp and sugar beans.",
                steps("Rinse the samp and sugar beans.",
                        "Soak them overnight or for several hours.",
                        "Drain the soaked samp and beans.",
                        "Finely chop the onion."),
                steps("Place the samp and beans in a large pot and cover with fresh water.",
                        "Simmer slowly until both are soft.",
                        "Add more water during cooking when necessary.",
                        "Heat the oil in a separate pan and cook the onion.",
                        "Stir the onion into the samp and beans.",
                        "Continue cooking until the mixture is creamy, then serve warm."),
                ingredient("samp", 2, "cup"), ingredient("sugar beans", 1, "cup"),
                ingredient("onion", 1, "item"), ingredient("cooking oil", 1, "tbsp"));
        addRecipe(database, "Boerewors and Pap",
                "Grilled boerewors with pap and tomato relish.",
                steps("Measure the mealie meal.",
                        "Chop the tomatoes and onion.",
                        "Keep the boerewors coil intact where possible.",
                        "Measure the cooking oil."),
                steps("Grill or pan-cook the boerewors over medium heat until cooked through.",
                        "Bring water to a gentle boil in a separate pot.",
                        "Gradually add the mealie meal while stirring.",
                        "Cover and cook the pap over low heat.",
                        "Heat the oil in a pan, cook the onion, then add the tomatoes.",
                        "Serve the boerewors with the pap and tomato relish."),
                ingredient("boerewors", 500, "g"), ingredient("mealie meal", 2, "cup"),
                ingredient("tomato", 3, "item"), ingredient("onion", 1, "item"),
                ingredient("cooking oil", 1, "tbsp"));
        addRecipe(database, "Vetkoek and Curried Mince",
                "Golden fried dough filled with gently spiced mince.",
                steps("Combine the flour and yeast.",
                        "Gradually add warm water and mix into a soft dough.",
                        "Knead the dough and leave it covered until doubled in size.",
                        "Chop the onion and divide the risen dough into portions."),
                steps("Cook the beef mince and onion in a pan.",
                        "Add the curry powder and cook until the mince is browned.",
                        "Heat the cooking oil in a deep pot.",
                        "Carefully lower the dough portions into the hot oil.",
                        "Fry until golden on both sides and drain on absorbent paper.",
                        "Cut each vetkoek open and fill it with curried mince."),
                ingredient("flour", 4, "cup"), ingredient("yeast", 1, "packet"),
                ingredient("beef mince", 500, "g"), ingredient("onion", 1, "item"),
                ingredient("curry powder", 1, "tbsp"), ingredient("cooking oil", 1, "L"));
        addRecipe(database, "Milk Tart",
                "A classic cinnamon-dusted South African custard tart.",
                steps("Preheat the oven to 180 degrees Celsius.",
                        "Use part of the flour and butter to prepare a simple pastry base.",
                        "Press the pastry into a tart dish.",
                        "Beat the eggs and measure the remaining ingredients."),
                steps("Bake the pastry base until lightly golden.",
                        "Heat most of the milk gently in a saucepan.",
                        "Mix the remaining milk with the flour, sugar and eggs.",
                        "Slowly add the mixture to the warm milk and stir until thick.",
                        "Pour the custard into the pastry shell and sprinkle with cinnamon.",
                        "Allow the tart to cool, then refrigerate before serving."),
                ingredient("milk", 1, "L"), ingredient("flour", 1, "cup"),
                ingredient("sugar", 1, "cup"), ingredient("egg", 2, "item"),
                ingredient("cinnamon", 1, "tsp"), ingredient("butter", 100, "g"));
        addRecipe(database, "Koeksisters",
                "Braided fried pastries soaked in cold spiced syrup.",
                steps("Prepare a sugar and cinnamon syrup and allow it to become completely cold.",
                        "Combine the flour and baking powder.",
                        "Gradually add water and mix into a soft dough.",
                        "Roll out the dough, cut it into strips and braid the strips."),
                steps("Heat the cooking oil in a deep pot.",
                        "Fry the shaped dough in small batches.",
                        "Turn each koeksister until both sides are golden.",
                        "Remove it from the oil and drain briefly.",
                        "Immediately place the hot koeksister into the cold syrup.",
                        "Allow it to absorb the syrup, then remove and cool."),
                ingredient("flour", 4, "cup"), ingredient("sugar", 3, "cup"),
                ingredient("cinnamon", 1, "tsp"), ingredient("cooking oil", 1, "L"),
                ingredient("baking powder", 2, "tsp"));
        addRecipe(database, "Masala Beans Curry",
                "A homestyle Durban bean curry finished with fresh coriander.",
                steps("Drain the cooked sugar beans.",
                        "Chop the onion and tomatoes.",
                        "Wash and chop the coriander.",
                        "Measure the curry powder and cooking oil."),
                steps("Heat the oil and cook the onion until soft.",
                        "Add the curry powder and stir for one minute.",
                        "Add the tomatoes and cook until a sauce forms.",
                        "Stir in the sugar beans.",
                        "Simmer until the curry becomes thick.",
                        "Add the coriander shortly before serving and mix gently."),
                ingredient("sugar beans", 2, "cup"), ingredient("onion", 1, "item"),
                ingredient("tomato", 2, "item"), ingredient("curry powder", 1, "tbsp"),
                ingredient("coriander", 1, "bunch"), ingredient("cooking oil", 2, "tbsp"));
    }

    private void addRecipe(SQLiteDatabase database, String name, String description,
                           String preparation, String instructions,
                           RecipeSeedIngredient... ingredients) {
        ContentValues recipeValues = new ContentValues();
        recipeValues.put(COLUMN_RECIPE_NAME, name);
        recipeValues.put(COLUMN_DESCRIPTION, description);
        recipeValues.put(COLUMN_PREPARATION, preparation);
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

    private String steps(String... instructions) {
        StringBuilder numberedSteps = new StringBuilder();
        for (int index = 0; index < instructions.length; index++) {
            if (index > 0) {
                numberedSteps.append("\n\n");
            }
            numberedSteps.append(index + 1).append(". ").append(instructions[index]);
        }
        return numberedSteps.toString();
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
