package com.example.smartpantry.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A recipe: a name, cooking time, the ingredients it needs and its method. */
public class Recipe {

    private final long id;
    private final String name;
    private final int minutes;
    /** Method steps, one per line (separated by '\n'). */
    private final String steps;
    private final List<RecipeIngredient> ingredients;

    public Recipe(long id, String name, int minutes, String steps, List<RecipeIngredient> ingredients) {
        this.id = id;
        this.name = name;
        this.minutes = minutes;
        this.steps = steps;
        this.ingredients = ingredients == null
                ? new ArrayList<RecipeIngredient>()
                : new ArrayList<>(ingredients);
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public int getMinutes() { return minutes; }
    public String getSteps() { return steps; }

    public List<RecipeIngredient> getIngredients() {
        return Collections.unmodifiableList(ingredients);
    }
}
