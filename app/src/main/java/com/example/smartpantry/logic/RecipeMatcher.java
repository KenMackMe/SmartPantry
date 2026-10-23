package com.example.smartpantry.logic;

import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * The strict-matching algorithm (assignment Section 2.3).
 *
 * <p>A recipe is suggested ONLY if every single ingredient it requires is
 * present in the pantry in at least the required quantity. If a recipe needs 5
 * ingredients and the pantry has 4 of them, the recipe is NOT suggested.</p>
 *
 * <p>The class is pure Java with no Android imports so the rule can be tested
 * quickly with ordinary JUnit tests (see RecipeMatcherTest).</p>
 */
public final class RecipeMatcher {

    private RecipeMatcher() {
        // Static utility class.
    }

    /** Checks one recipe: collects every ingredient the pantry cannot cover. */
    public static MatchResult evaluate(Recipe recipe, PantryStock stock) {
        List<RecipeIngredient> missing = new ArrayList<>();
        for (RecipeIngredient need : recipe.getIngredients()) {
            if (!stock.covers(need)) {
                missing.add(need);
            }
        }
        return new MatchResult(recipe, missing);
    }

    /** The main "Suggested Recipes" list: strict matches only, A-Z by recipe name. */
    public static List<MatchResult> strictMatches(List<Recipe> recipes, PantryStock stock) {
        List<MatchResult> out = new ArrayList<>();
        for (Recipe recipe : recipes) {
            MatchResult result = evaluate(recipe, stock);
            if (result.isStrictMatch()) {
                out.add(result);
            }
        }
        sortByName(out);
        return out;
    }

    /**
     * Optional stretch goal: recipes missing exactly ONE ingredient. The UI
     * shows these in a separate tab, never mixed into the strict list.
     */
    public static List<MatchResult> almostThere(List<Recipe> recipes, PantryStock stock) {
        List<MatchResult> out = new ArrayList<>();
        for (Recipe recipe : recipes) {
            MatchResult result = evaluate(recipe, stock);
            if (result.isAlmostThere()) {
                out.add(result);
            }
        }
        sortByName(out);
        return out;
    }

    private static void sortByName(List<MatchResult> results) {
        Collections.sort(results, new Comparator<MatchResult>() {
            @Override
            public int compare(MatchResult a, MatchResult b) {
                return a.getRecipe().getName().compareToIgnoreCase(b.getRecipe().getName());
            }
        });
    }
}
