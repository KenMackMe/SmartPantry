package com.example.smartpantry.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.smartpantry.model.PantryItem;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SQLite persistence layer (SQLiteOpenHelper).
 *
 * <h3>Tables</h3>
 * <pre>
 * pantry_items       (id PK, name, quantity, unit, expiry)            -- user data, full CRUD
 * recipes            (id PK, name, minutes, steps)                    -- seeded on first run
 * recipe_ingredients (id PK, recipe_id FK -> recipes.id, name, quantity, unit)
 * </pre>
 *
 * <p>The database file lives in the app's private storage, so everything the
 * user saves survives the app being closed and re-opened.</p>
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "smart_pantry.db";
    private static final int DB_VERSION = 1;

    // ---- pantry_items ----
    private static final String T_PANTRY = "pantry_items";
    private static final String P_ID = "id";
    private static final String P_NAME = "name";
    private static final String P_QTY = "quantity";
    private static final String P_UNIT = "unit";
    private static final String P_EXPIRY = "expiry";

    // ---- recipes ----
    private static final String T_RECIPES = "recipes";
    private static final String R_ID = "id";
    private static final String R_NAME = "name";
    private static final String R_MINUTES = "minutes";
    private static final String R_STEPS = "steps";

    // ---- recipe_ingredients ----
    private static final String T_ING = "recipe_ingredients";
    private static final String I_ID = "id";
    private static final String I_RECIPE_ID = "recipe_id";
    private static final String I_NAME = "name";
    private static final String I_QTY = "quantity";
    private static final String I_UNIT = "unit";

    private static DatabaseHelper instance;

    /** Single shared helper (uses the application context to avoid leaking an Activity). */
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // Needed so ON DELETE CASCADE works on recipe_ingredients.
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + T_PANTRY + " ("
                + P_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + P_NAME + " TEXT NOT NULL, "
                + P_QTY + " REAL NOT NULL, "
                + P_UNIT + " TEXT NOT NULL, "
                + P_EXPIRY + " TEXT)");

        db.execSQL("CREATE TABLE " + T_RECIPES + " ("
                + R_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + R_NAME + " TEXT NOT NULL, "
                + R_MINUTES + " INTEGER NOT NULL, "
                + R_STEPS + " TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + T_ING + " ("
                + I_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + I_RECIPE_ID + " INTEGER NOT NULL REFERENCES " + T_RECIPES + "(" + R_ID
                + ") ON DELETE CASCADE, "
                + I_NAME + " TEXT NOT NULL, "
                + I_QTY + " REAL NOT NULL, "
                + I_UNIT + " TEXT NOT NULL)");

        seedRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Version 1 is the first release, so there is nothing to migrate yet.
        // For a future schema change, add ALTER TABLE statements here instead of
        // dropping tables (which would delete the user's pantry).
    }

    /** Pre-loads the recipe collection the first time the database is created. */
    private void seedRecipes(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            for (Recipe recipe : RecipeCatalog.all()) {
                ContentValues rv = new ContentValues();
                rv.put(R_NAME, recipe.getName());
                rv.put(R_MINUTES, recipe.getMinutes());
                rv.put(R_STEPS, recipe.getSteps());
                long recipeId = db.insert(T_RECIPES, null, rv);

                for (RecipeIngredient ing : recipe.getIngredients()) {
                    ContentValues iv = new ContentValues();
                    iv.put(I_RECIPE_ID, recipeId);
                    iv.put(I_NAME, ing.getName());
                    iv.put(I_QTY, ing.getQuantity());
                    iv.put(I_UNIT, ing.getUnit());
                    db.insert(T_ING, null, iv);
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    // =====================================================================
    //  PANTRY ITEMS - Create / Read / Update / Delete
    // =====================================================================

    /** CREATE: inserts a new pantry item and returns its new row id (or -1 on failure). */
    public long insertPantryItem(PantryItem item) {
        return getWritableDatabase().insert(T_PANTRY, null, toValues(item));
    }

    /** READ: every pantry item, A-Z by name. */
    public List<PantryItem> getAllPantryItems() {
        List<PantryItem> items = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query(T_PANTRY, null, null, null,
                null, null, P_NAME + " COLLATE NOCASE ASC")) {
            while (c.moveToNext()) {
                items.add(fromCursor(c));
            }
        }
        return items;
    }

    /** READ: one pantry item by id, or null if it does not exist. */
    public PantryItem getPantryItem(long id) {
        try (Cursor c = getReadableDatabase().query(T_PANTRY, null, P_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            return c.moveToFirst() ? fromCursor(c) : null;
        }
    }

    /** UPDATE: overwrites the stored values of an existing item. @return rows changed (1 on success) */
    public int updatePantryItem(PantryItem item) {
        return getWritableDatabase().update(T_PANTRY, toValues(item), P_ID + " = ?",
                new String[]{String.valueOf(item.getId())});
    }

    /** DELETE: removes one item. @return rows deleted (1 on success) */
    public int deletePantryItem(long id) {
        return getWritableDatabase().delete(T_PANTRY, P_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    /** DELETE: removes every pantry item (used by the Settings "clear pantry" button). */
    public int deleteAllPantryItems() {
        return getWritableDatabase().delete(T_PANTRY, null, null);
    }

    private ContentValues toValues(PantryItem item) {
        ContentValues v = new ContentValues();
        v.put(P_NAME, item.getName());
        v.put(P_QTY, item.getQuantity());
        v.put(P_UNIT, item.getUnit());
        v.put(P_EXPIRY, item.getExpiry());      // null is stored as SQL NULL
        return v;
    }

    private PantryItem fromCursor(Cursor c) {
        return new PantryItem(
                c.getLong(c.getColumnIndexOrThrow(P_ID)),
                c.getString(c.getColumnIndexOrThrow(P_NAME)),
                c.getDouble(c.getColumnIndexOrThrow(P_QTY)),
                c.getString(c.getColumnIndexOrThrow(P_UNIT)),
                c.getString(c.getColumnIndexOrThrow(P_EXPIRY)));
    }

    // =====================================================================
    //  RECIPES (read-only for the user)
    // =====================================================================

    /** READ: all recipes with their ingredient lists. */
    public List<Recipe> getAllRecipes() {
        SQLiteDatabase db = getReadableDatabase();

        // 1) load every ingredient once and group them by recipe id
        Map<Long, List<RecipeIngredient>> byRecipe = new HashMap<>();
        try (Cursor c = db.query(T_ING, null, null, null, null, null, I_ID + " ASC")) {
            while (c.moveToNext()) {
                long recipeId = c.getLong(c.getColumnIndexOrThrow(I_RECIPE_ID));
                List<RecipeIngredient> list = byRecipe.get(recipeId);
                if (list == null) {
                    list = new ArrayList<>();
                    byRecipe.put(recipeId, list);
                }
                list.add(ingredientFromCursor(c));
            }
        }

        // 2) load the recipes and attach their ingredients
        List<Recipe> recipes = new ArrayList<>();
        try (Cursor c = db.query(T_RECIPES, null, null, null, null, null,
                R_NAME + " COLLATE NOCASE ASC")) {
            while (c.moveToNext()) {
                long id = c.getLong(c.getColumnIndexOrThrow(R_ID));
                List<RecipeIngredient> ingredients = byRecipe.get(id);
                recipes.add(new Recipe(id,
                        c.getString(c.getColumnIndexOrThrow(R_NAME)),
                        c.getInt(c.getColumnIndexOrThrow(R_MINUTES)),
                        c.getString(c.getColumnIndexOrThrow(R_STEPS)),
                        ingredients));
            }
        }
        return recipes;
    }

    /** READ: a single recipe by id, or null if it does not exist. */
    public Recipe getRecipe(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Recipe recipe = null;

        try (Cursor c = db.query(T_RECIPES, null, R_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            if (c.moveToFirst()) {
                List<RecipeIngredient> ingredients = new ArrayList<>();
                try (Cursor ic = db.query(T_ING, null, I_RECIPE_ID + " = ?",
                        new String[]{String.valueOf(id)}, null, null, I_ID + " ASC")) {
                    while (ic.moveToNext()) {
                        ingredients.add(ingredientFromCursor(ic));
                    }
                }
                recipe = new Recipe(id,
                        c.getString(c.getColumnIndexOrThrow(R_NAME)),
                        c.getInt(c.getColumnIndexOrThrow(R_MINUTES)),
                        c.getString(c.getColumnIndexOrThrow(R_STEPS)),
                        ingredients);
            }
        }
        return recipe;
    }

    private RecipeIngredient ingredientFromCursor(Cursor c) {
        return new RecipeIngredient(
                c.getString(c.getColumnIndexOrThrow(I_NAME)),
                c.getDouble(c.getColumnIndexOrThrow(I_QTY)),
                c.getString(c.getColumnIndexOrThrow(I_UNIT)));
    }
}
