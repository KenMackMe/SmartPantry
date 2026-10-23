# Smart Pantry Manager

A Java Android app that helps reduce food waste. The user records the ingredients they
actually have at home (their "pantry"), and the app suggests **only** the recipes they can
cook right now, with no shopping trip required.

Built for *Mobile App Development 700 - Practical Assignment* (Java, Android Studio, SQLite).

## Features

| Screen | What it does |
| --- | --- |
| **Pantry** (launcher) | RecyclerView of all ingredients from the database. Tap a row to edit, tap the bin icon to delete, tap **+** to add. Optional expiring-soon banner. |
| **Add / Edit Ingredient** | One form for create and update: name, quantity, unit (spinner), optional expiry date (date picker). Full input validation. Delete is available when editing. |
| **Suggested Recipes** | Runs the strict-matching rule against the current pantry. Tab 1 "Ready to cook" lists only full matches. Tab 2 "Almost there" (bonus) lists recipes missing exactly one ingredient, clearly separated. Friendly empty-state message when nothing matches. |
| **Recipe Detail** | Full ingredient list (green tick = in pantry, red cross = missing) and numbered method. |
| **Settings** | Toggle for expiring-soon alerts, toggle to ignore expired ingredients when matching, and a "clear pantry" button. Stored in SharedPreferences. |

Navigation uses a **bottom navigation bar** (Pantry / Recipes / Settings) and explicit **Intents**
(with extras for the item id and recipe id) between screens.

25 recipes are **pre-loaded into the database on first run** (`RecipeCatalog` → `DatabaseHelper.onCreate`).

## Database choice: SQLite (SQLiteOpenHelper)

I chose **SQLite** because:

* The pantry is personal, small and needs to work **offline** (in the kitchen, with no signal).
* It needs no server, account or API key, so the project runs after a plain "Run" in Android Studio.
* `SQLiteOpenHelper` is the approach covered in the module's persistent-data chapter, and it
  gives full CRUD with data that survives closing and re-opening the app.
* The strict-matching algorithm reads all rows into memory, so SQL joins are not needed and a
  hosted database would add latency and failure modes for no benefit.

### Data model

```
pantry_items        recipes                recipe_ingredients
------------        -------                ------------------
id (PK)             id (PK)  <───────┐     id (PK)
name                name             └──── recipe_id (FK, ON DELETE CASCADE)
quantity (REAL)     minutes                name
unit                steps                  quantity (REAL)
expiry (TEXT, ISO yyyy-MM-dd, nullable)    unit
```

`recipes` 1 ──── * `recipe_ingredients`. `pantry_items` is independent: it is matched to recipes
by the matching logic, not by a foreign key (the user types free-text ingredient names).

## The strict-matching rule

A recipe is suggested **only if every ingredient it needs is in the pantry in at least the required
quantity.** 5 needed, 4 owned → **not** suggested. Implemented in `logic/RecipeMatcher.java`:

1. `PantryStock.from(...)` merges pantry rows by canonical name and converts quantities to base
   units (g, ml, pieces), so "Milk 500 ml" + "Milk 1 l" = 1500 ml.
2. `RecipeMatcher.evaluate(...)` collects every recipe ingredient the stock cannot cover.
3. A recipe is a strict match only when that list is **empty** (and the recipe has ingredients).

Robustness to real-world messiness:

* **Names**: `IngredientNormalizer` lower-cases, strips punctuation and descriptors
  ("fresh", "chopped"…), singularises, and maps synonyms, so `tomato` = `Tomatoes` = `fresh tomatoes`,
  `scallions` = `spring onion`, `yogurt` = `yoghurt`.
* **Units**: `Units` converts within a dimension (g/kg/oz/lb, ml/l/tsp/tbsp/cup, pieces).
  1 kg flour satisfies 200 g; 1 cup milk (240 ml) does **not** satisfy 300 ml.
* **Design decisions** (documented so you can defend them in the video):
  * Amounts in *different* dimensions (pantry "garlic 50 g" vs recipe "garlic 3 pcs") cannot be
    compared reliably, so they are treated as **not enough** (the strict side).
  * Expired items still count by default; Settings → "Ignore expired ingredients" excludes them.
  * A recipe with zero ingredients is never suggested.

The logic classes (`model/`, `logic/`, `data/RecipeCatalog`) have **no Android imports**, so they are
unit tested on the JVM (`app/src/test`).

## Setup and run

Requirements: **Android Studio** (Hedgehog or newer), JDK 17 (bundled with Android Studio),
Android SDK 34 installed through the SDK Manager.

1. Open Android Studio → **File ▸ Open** → select the `SmartPantryManager` folder.
2. Let Gradle sync (Android Studio downloads Gradle 8.7 and the dependencies automatically).
   If it offers to upgrade the Android Gradle Plugin, you can accept or ignore it.
3. Create/choose an emulator (API 26+) or plug in a device with USB debugging on.
4. Press **Run ▶** (`app`).
5. First launch seeds the recipes. Add ingredients on the Pantry screen, then open the **Recipes** tab.

Run the unit tests: right-click `app/src/test` → **Run Tests**, or `./gradlew test` from a terminal.

To try the strict rule quickly: add *Egg 3 pcs* and *Butter 20 g* → **Cheese Omelette** needs cheese, so
it appears under *Almost there*. Add *Cheese 50 g* → it moves to *Ready to cook*. Delete the cheese → it
disappears again.

## Project structure

```
app/src/main/java/com/example/smartpantry/
  model/   PantryItem, Recipe, RecipeIngredient          (plain Java)
  logic/   IngredientNormalizer, Units, PantryStock,
           RecipeMatcher, MatchResult                    (plain Java, unit tested)
  data/    DatabaseHelper (SQLite CRUD), RecipeCatalog (seed data), Prefs (SharedPreferences)
  ui/      BaseActivity, PantryActivity, AddEditIngredientActivity,
           SuggestedRecipesActivity, RecipeDetailActivity, SettingsActivity,
           PantryAdapter, RecipeAdapter, UiUtils
app/src/main/res/  layouts, menus, vector icons, strings, colours, theme
app/src/test/      JUnit tests for the matching logic
```

## Scope notes

* No Google Maps, mapping SDK or location/GPS is used (no location permission is declared).
* No payments; not published to the Play Store.
* Written entirely in Java; no Kotlin plugin is applied.
