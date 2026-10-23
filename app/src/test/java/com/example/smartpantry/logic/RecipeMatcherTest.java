package com.example.smartpantry.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.smartpantry.data.RecipeCatalog;
import com.example.smartpantry.model.PantryItem;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.model.RecipeIngredient;

import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Unit tests for the strict-matching rule (assignment Section 2.3) - the piece
 * of logic the marker will test directly.
 */
public class RecipeMatcherTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 18);

    // ---- helpers -----------------------------------------------------

    private static PantryItem item(String name, double qty, String unit) {
        return new PantryItem(name, qty, unit, null);
    }

    private static RecipeIngredient need(String name, double qty, String unit) {
        return new RecipeIngredient(name, qty, unit);
    }

    private static Recipe recipe(String name, RecipeIngredient... needs) {
        return new Recipe(1, name, 10, "1. Cook it.", Arrays.asList(needs));
    }

    private static PantryStock stock(PantryItem... items) {
        return PantryStock.from(Arrays.asList(items), false, TODAY);
    }

    private static List<String> names(List<MatchResult> results) {
        List<String> out = new ArrayList<>();
        for (MatchResult r : results) {
            out.add(r.getRecipe().getName());
        }
        return out;
    }

    // ---- THE rule: 5 needed, 4 owned -> NOT suggested ------------------

    @Test
    public void recipeNeedingFiveIngredientsWithOnlyFourIsNotSuggested() {
        Recipe r = recipe("Five", need("a", 1, "pcs"), need("b", 1, "pcs"), need("c", 1, "pcs"),
                need("d", 1, "pcs"), need("e", 1, "pcs"));
        PantryStock s = stock(item("a", 1, "pcs"), item("b", 1, "pcs"),
                item("c", 1, "pcs"), item("d", 1, "pcs"));

        List<MatchResult> suggested = RecipeMatcher.strictMatches(Collections.singletonList(r), s);
        assertTrue("4 of 5 ingredients must NOT be suggested", suggested.isEmpty());
    }

    @Test
    public void recipeIsSuggestedOnceTheFifthIngredientIsAdded() {
        Recipe r = recipe("Five", need("a", 1, "pcs"), need("b", 1, "pcs"), need("c", 1, "pcs"),
                need("d", 1, "pcs"), need("e", 1, "pcs"));
        PantryStock s = stock(item("a", 1, "pcs"), item("b", 1, "pcs"), item("c", 1, "pcs"),
                item("d", 1, "pcs"), item("e", 1, "pcs"));

        assertEquals(1, RecipeMatcher.strictMatches(Collections.singletonList(r), s).size());
    }

    @Test
    public void recipeAppearsAndDisappearsAsAnIngredientIsAddedAndRemoved() {
        // Mirrors the live demonstration the video must show.
        Recipe omelette = recipe("Omelette", need("egg", 3, "pcs"), need("butter", 10, "g"));
        List<Recipe> all = Collections.singletonList(omelette);

        PantryItem eggs = item("Eggs", 6, "pcs");
        PantryItem butter = item("Butter", 250, "g");

        assertEquals(0, RecipeMatcher.strictMatches(all, stock(eggs)).size());            // no butter
        assertEquals(1, RecipeMatcher.strictMatches(all, stock(eggs, butter)).size());    // add butter
        assertEquals(0, RecipeMatcher.strictMatches(all, stock(butter)).size());          // remove eggs
    }

    // ---- quantity ---------------------------------------------------

    @Test
    public void presentButInsufficientQuantityIsNotAMatch() {
        Recipe r = recipe("Omelette", need("egg", 3, "pcs"));
        assertTrue(RecipeMatcher.strictMatches(Collections.singletonList(r),
                stock(item("egg", 2, "pcs"))).isEmpty());
    }

    @Test
    public void exactQuantityIsEnough() {
        Recipe r = recipe("Omelette", need("egg", 3, "pcs"));
        assertEquals(1, RecipeMatcher.strictMatches(Collections.singletonList(r),
                stock(item("egg", 3, "pcs"))).size());
    }

    @Test
    public void quantitiesOfTheSameIngredientInSeveralRowsAreAddedTogether() {
        Recipe r = recipe("Pancakes", need("milk", 300, "ml"));
        assertEquals(1, RecipeMatcher.strictMatches(Collections.singletonList(r),
                stock(item("Milk", 200, "ml"), item("milk", 0.2, "l"))).size());
    }

    @Test
    public void zeroQuantityRowsDoNotCount() {
        Recipe r = recipe("Toast", need("bread", 1, "pcs"));
        assertTrue(RecipeMatcher.strictMatches(Collections.singletonList(r),
                stock(item("bread", 0, "pcs"))).isEmpty());
    }

    // ---- real-world messiness (units and names) ----------------------

    @Test
    public void unitDifferencesAreConverted() {
        Recipe flour = recipe("Cake", need("flour", 200, "g"));
        assertEquals(1, RecipeMatcher.strictMatches(Collections.singletonList(flour),
                stock(item("Flour", 1, "kg"))).size());

        Recipe milk = recipe("Porridge", need("milk", 300, "ml"));
        assertEquals(1, RecipeMatcher.strictMatches(Collections.singletonList(milk),
                stock(item("Milk", 0.5, "l"))).size());

        Recipe oil = recipe("Salad", need("olive oil", 2, "tbsp"));
        assertEquals(1, RecipeMatcher.strictMatches(Collections.singletonList(oil),
                stock(item("Olive oil", 100, "ml"))).size());

        // 1 cup (240 ml) is NOT enough for 300 ml
        assertTrue(RecipeMatcher.strictMatches(Collections.singletonList(milk),
                stock(item("Milk", 1, "cup"))).isEmpty());
    }

    @Test
    public void ouncesAndPoundsAreConverted() {
        Recipe r = recipe("Mash", need("potato", 500, "g"));
        assertEquals(1, RecipeMatcher.strictMatches(Collections.singletonList(r),
                stock(item("Potato", 1.2, "lb"))).size());       // 1.2 lb = ~544 g
        assertTrue(RecipeMatcher.strictMatches(Collections.singletonList(r),
                stock(item("Potato", 10, "oz"))).isEmpty());     // 10 oz = ~283 g
    }

    @Test
    public void tomatoVsTomatoesAndOtherPluralsMatch() {
        Recipe r = recipe("Salad", need("Tomato", 2, "pcs"), need("Onion", 1, "pcs"));
        assertEquals(1, RecipeMatcher.strictMatches(Collections.singletonList(r),
                stock(item("tomatoes", 3, "pcs"), item("ONIONS", 2, "pcs"))).size());
    }

    @Test
    public void incomparableUnitsAreTreatedAsNotEnough() {
        // pantry has garlic by weight, recipe wants cloves: cannot be compared -> strict
        Recipe r = recipe("Garlic bread", need("garlic", 3, "pcs"));
        assertTrue(RecipeMatcher.strictMatches(Collections.singletonList(r),
                stock(item("garlic", 50, "g"))).isEmpty());
    }

    // ---- edge cases ------------------------------------------------

    @Test
    public void emptyPantrySuggestsNothing() {
        assertTrue(RecipeMatcher.strictMatches(RecipeCatalog.all(), stock()).isEmpty());
    }

    @Test
    public void recipeWithNoIngredientsIsNeverSuggested() {
        Recipe empty = new Recipe(1, "Nothing", 1, "", new ArrayList<RecipeIngredient>());
        assertTrue(RecipeMatcher.strictMatches(Collections.singletonList(empty), stock()).isEmpty());
        assertTrue(RecipeMatcher.almostThere(Collections.singletonList(empty), stock()).isEmpty());
    }

    @Test
    public void expiredItemsCountByDefaultButCanBeIgnored() {
        Recipe r = recipe("Toast", need("bread", 1, "pcs"));
        PantryItem expired = new PantryItem("Bread", 2, "pcs", "2026-10-01");
        List<PantryItem> pantry = Collections.singletonList(expired);

        assertEquals(1, RecipeMatcher.strictMatches(Collections.singletonList(r),
                PantryStock.from(pantry, false, TODAY)).size());
        assertTrue(RecipeMatcher.strictMatches(Collections.singletonList(r),
                PantryStock.from(pantry, true, TODAY)).isEmpty());
    }

    @Test
    public void itemExpiringTodayIsNotYetExpired() {
        PantryItem today = new PantryItem("Milk", 1, "l", "2026-10-18");
        assertFalse(today.isExpired(TODAY));
        assertTrue(new PantryItem("Milk", 1, "l", "2026-10-17").isExpired(TODAY));
    }

    // ---- "Almost There" (optional stretch) ----------------------

    @Test
    public void almostThereListsOnlyRecipesMissingExactlyOneIngredient() {
        Recipe missingOne = recipe("One short", need("a", 1, "pcs"), need("b", 1, "pcs"));
        Recipe missingTwo = recipe("Two short", need("a", 1, "pcs"), need("b", 1, "pcs"),
                need("c", 1, "pcs"));
        Recipe complete = recipe("Complete", need("a", 1, "pcs"));
        List<Recipe> all = Arrays.asList(missingOne, missingTwo, complete);
        PantryStock s = stock(item("a", 1, "pcs"));

        assertEquals(Collections.singletonList("Complete"), names(RecipeMatcher.strictMatches(all, s)));
        assertEquals(Collections.singletonList("One short"), names(RecipeMatcher.almostThere(all, s)));
    }

    @Test
    public void strictListNeverContainsAlmostThereRecipes() {
        Recipe missingOne = recipe("One short", need("a", 1, "pcs"), need("b", 1, "pcs"));
        List<MatchResult> strict = RecipeMatcher.strictMatches(
                Collections.singletonList(missingOne), stock(item("a", 1, "pcs")));
        assertTrue(strict.isEmpty());
    }

    // ---- the real seeded recipes ---------------------------------------

    @Test
    public void catalogHasAtLeastFifteenRecipesWithIngredientsAndSteps() {
        List<Recipe> all = RecipeCatalog.all();
        assertTrue("need 15-20+ recipes", all.size() >= 15);
        for (Recipe r : all) {
            assertFalse(r.getName() + " has no ingredients", r.getIngredients().isEmpty());
            assertFalse(r.getName() + " has no steps", r.getSteps().trim().isEmpty());
        }
    }

    @Test
    public void everySeededRecipeIsSuggestedWhenThePantryHoldsExactlyItsIngredients() {
        for (Recipe r : RecipeCatalog.all()) {
            List<PantryItem> pantry = new ArrayList<>();
            for (RecipeIngredient ing : r.getIngredients()) {
                pantry.add(item(ing.getName(), ing.getQuantity(), ing.getUnit()));
            }
            PantryStock s = PantryStock.from(pantry, false, TODAY);
            assertTrue(r.getName() + " should be suggested",
                    names(RecipeMatcher.strictMatches(Collections.singletonList(r), s))
                            .contains(r.getName()));
        }
    }

    @Test
    public void removingAnySingleIngredientRemovesEverySeededRecipeThatNeedsIt() {
        for (Recipe r : RecipeCatalog.all()) {
            for (int skip = 0; skip < r.getIngredients().size(); skip++) {
                List<PantryItem> pantry = new ArrayList<>();
                for (int i = 0; i < r.getIngredients().size(); i++) {
                    if (i == skip) {
                        continue;
                    }
                    RecipeIngredient ing = r.getIngredients().get(i);
                    pantry.add(item(ing.getName(), ing.getQuantity(), ing.getUnit()));
                }
                PantryStock s = PantryStock.from(pantry, false, TODAY);

                // The recipe may only still be suggested if the skipped ingredient is
                // covered by another line of the same recipe (never the case here).
                assertTrue(r.getName() + " must NOT be suggested without '"
                                + r.getIngredients().get(skip).getName() + "'",
                        RecipeMatcher.strictMatches(Collections.singletonList(r), s).isEmpty());
                assertEquals(1, RecipeMatcher.almostThere(Collections.singletonList(r), s).size());
            }
        }
    }
}
