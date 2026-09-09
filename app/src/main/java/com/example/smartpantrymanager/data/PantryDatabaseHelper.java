package com.example.smartpantrymanager.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.smartpantrymanager.model.PantryItem;

import java.util.ArrayList;
import java.util.List;

public class PantryDatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "smart_pantry.db";
    public static final int DATABASE_VERSION = 1;

    public static final String TABLE_PANTRY_ITEMS = "pantry_items";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_QUANTITY = "quantity";
    public static final String COLUMN_UNIT = "unit";
    public static final String COLUMN_EXPIRY_DATE = "expiry_date";

    private static final String CREATE_PANTRY_TABLE =
            "CREATE TABLE " + TABLE_PANTRY_ITEMS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " TEXT NOT NULL COLLATE NOCASE, " +
                    COLUMN_QUANTITY + " REAL NOT NULL CHECK (" + COLUMN_QUANTITY + " > 0), " +
                    COLUMN_UNIT + " TEXT NOT NULL, " +
                    COLUMN_EXPIRY_DATE + " TEXT NOT NULL)";

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
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY_ITEMS);
        onCreate(database);
    }
}
