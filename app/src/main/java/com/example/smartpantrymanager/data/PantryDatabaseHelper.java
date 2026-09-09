package com.example.smartpantrymanager.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.smartpantrymanager.model.PantryItem;

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
