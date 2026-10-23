package com.example.smartpantry.logic;

import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** The outcome of checking ONE recipe against the pantry. */
public final class MatchResult {

    private final Recipe recipe;
    private final List<RecipeIngredient> missing;

    public MatchResult(Recipe recipe, List<RecipeIngredient> missing) {
        this.recipe = recipe;
        this.missing = new ArrayList<>(missing);
    }

    public Recipe getRecipe() {
        return recipe;
    }

    /** Ingredients that are absent from the pantry or present in too small a quantity. */
    public List<RecipeIngredient> getMissing() {
        return Collections.unmodifiableList(missing);
    }

    /**
     * THE strict rule: a recipe is only "suggested" when nothing is missing.
     * A recipe with no ingredients at all is never suggested (an empty list
     * would otherwise be "vacuously" satisfied).
     */
    public boolean isStrictMatch() {
        return !recipe.getIngredients().isEmpty() && missing.isEmpty();
    }

    /** Exactly one ingredient short - used only for the optional "Almost There" list. */
    public boolean isAlmostThere() {
        return !recipe.getIngredients().isEmpty() && missing.size() == 1;
    }
}
