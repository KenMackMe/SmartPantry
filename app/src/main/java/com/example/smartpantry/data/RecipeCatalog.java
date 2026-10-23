package com.example.smartpantry.data;

import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.List;

/**
 * The built-in recipe collection that is pre-loaded ("seeded") into the
 * database the first time the app runs (see DatabaseHelper#onCreate).
 *
 * <p>Kept as plain Java data (no Android classes) so unit tests can run the
 * matching logic against the real recipes.</p>
 *
 * <p>Ingredient format: {@code "name:quantity:unit"}.</p>
 */
public final class RecipeCatalog {

    private RecipeCatalog() {
        // Static utility class.
    }

    /** @return all seed recipes (ids are 0 - the database assigns the real ids). */
    public static List<Recipe> all() {
        List<Recipe> list = new ArrayList<>();

        list.add(recipe("Tomato Omelette", 10,
                new String[]{
                        "Beat the eggs with the salt.",
                        "Melt the butter in a pan over medium heat.",
                        "Add the diced tomato and cook for one minute.",
                        "Pour in the eggs, cook until set, then fold and serve."},
                "Egg:3:pcs", "Tomato:1:pcs", "Butter:10:g", "Salt:0.5:tsp"));

        list.add(recipe("Cheese Toastie", 10,
                new String[]{
                        "Butter one side of each slice of bread.",
                        "Place the cheese between the slices, buttered sides out.",
                        "Toast in a pan for 2-3 minutes per side until golden and melted."},
                "Bread:4:pcs", "Cheese:80:g", "Butter:20:g"));

        list.add(recipe("Garlic Butter Pasta", 20,
                new String[]{
                        "Boil the pasta in salted water until al dente, then drain.",
                        "Melt the butter and gently fry the crushed garlic for one minute.",
                        "Toss the pasta through the garlic butter and season with salt."},
                "Pasta:200:g", "Garlic:3:pcs", "Butter:30:g", "Salt:1:tsp"));

        list.add(recipe("Tomato Pasta", 25,
                new String[]{
                        "Cook the pasta in salted water and drain.",
                        "Soften the chopped onion and garlic in the olive oil.",
                        "Add the chopped tomatoes and simmer for 10 minutes.",
                        "Stir the sauce through the pasta and season with salt."},
                "Pasta:200:g", "Tomato:4:pcs", "Onion:1:pcs", "Garlic:2:pcs",
                "Olive oil:2:tbsp", "Salt:1:tsp"));

        list.add(recipe("Egg Fried Rice", 20,
                new String[]{
                        "Cook the rice, spread it on a tray and let it cool.",
                        "Fry the chopped onion in the oil until soft.",
                        "Push the onion aside, scramble the eggs in the pan.",
                        "Add the rice and soy sauce and stir-fry for 3 minutes."},
                "Rice:1.5:cup", "Egg:2:pcs", "Onion:1:pcs", "Soy sauce:2:tbsp",
                "Vegetable oil:1:tbsp"));

        list.add(recipe("Fluffy Pancakes", 20,
                new String[]{
                        "Whisk the flour, sugar, egg and milk into a smooth batter.",
                        "Melt a little butter in a pan over medium heat.",
                        "Pour in small rounds of batter and cook until bubbles form.",
                        "Flip and cook until golden on both sides."},
                "Flour:200:g", "Milk:300:ml", "Egg:1:pcs", "Sugar:1:tbsp", "Butter:20:g"));

        list.add(recipe("Scrambled Eggs on Toast", 10,
                new String[]{
                        "Toast the bread.",
                        "Beat the eggs with the salt.",
                        "Melt the butter, add the eggs and stir gently until just set.",
                        "Spoon over the toast and serve."},
                "Egg:3:pcs", "Bread:2:pcs", "Butter:15:g", "Salt:0.25:tsp"));

        list.add(recipe("Creamy Mashed Potatoes", 30,
                new String[]{
                        "Peel and chop the potatoes, then boil until soft (about 15 minutes).",
                        "Drain well and return to the pot.",
                        "Add the butter, warm milk and salt, then mash until smooth."},
                "Potato:4:pcs", "Butter:40:g", "Milk:100:ml", "Salt:1:tsp"));

        list.add(recipe("Cheesy Baked Potato", 60,
                new String[]{
                        "Heat the oven to 200 C and prick the potatoes with a fork.",
                        "Bake for 45-50 minutes until the skins are crisp.",
                        "Split open, add the butter and salt, then top with the grated cheese.",
                        "Return to the oven for 5 minutes to melt the cheese."},
                "Potato:2:pcs", "Cheese:60:g", "Butter:20:g", "Salt:0.5:tsp"));

        list.add(recipe("Chicken Rice Bowl", 35,
                new String[]{
                        "Cook the rice according to the packet instructions.",
                        "Season the chicken with salt and sear in the olive oil until cooked through.",
                        "Fry the sliced onion and garlic in the same pan for 3 minutes.",
                        "Slice the chicken and serve over the rice with the onion and garlic."},
                "Chicken breast:300:g", "Rice:1:cup", "Onion:1:pcs", "Garlic:2:pcs",
                "Olive oil:1:tbsp", "Salt:1:tsp"));

        list.add(recipe("Hearty Lentil Soup", 40,
                new String[]{
                        "Soften the chopped onion, carrots and garlic in the olive oil.",
                        "Add the rinsed lentils and the stock.",
                        "Simmer for 25-30 minutes until the lentils are tender.",
                        "Blend partly for a thicker soup, or serve as is."},
                "Lentils:200:g", "Onion:1:pcs", "Carrot:2:pcs", "Garlic:2:pcs",
                "Vegetable stock:1:l", "Olive oil:1:tbsp"));

        list.add(recipe("Vegetable Stir-Fry", 15,
                new String[]{
                        "Slice the carrots, bell pepper and onion into thin strips.",
                        "Heat the oil in a wok until very hot.",
                        "Stir-fry the vegetables for 4-5 minutes.",
                        "Add the soy sauce, toss and serve immediately."},
                "Carrot:2:pcs", "Bell pepper:1:pcs", "Onion:1:pcs", "Soy sauce:2:tbsp",
                "Vegetable oil:1:tbsp"));

        list.add(recipe("Cheese Omelette", 10,
                new String[]{
                        "Beat the eggs.",
                        "Melt the butter in a pan and pour in the eggs.",
                        "When almost set, sprinkle over the grated cheese.",
                        "Fold in half and cook for another 30 seconds."},
                "Egg:3:pcs", "Cheese:50:g", "Butter:10:g"));

        list.add(recipe("French Toast", 15,
                new String[]{
                        "Whisk the eggs, milk and sugar in a shallow dish.",
                        "Soak each slice of bread for about 20 seconds per side.",
                        "Fry in melted butter until golden on both sides."},
                "Bread:4:pcs", "Egg:2:pcs", "Milk:100:ml", "Butter:20:g", "Sugar:1:tbsp"));

        list.add(recipe("Banana Oat Porridge", 10,
                new String[]{
                        "Simmer the oats and milk in a pot, stirring, for 5 minutes.",
                        "Slice the banana and stir half of it into the porridge.",
                        "Top with the rest of the banana and drizzle with honey."},
                "Oats:80:g", "Milk:300:ml", "Banana:1:pcs", "Honey:1:tbsp"));

        list.add(recipe("Tuna Pasta", 20,
                new String[]{
                        "Cook the pasta and drain.",
                        "Drain the tuna and flake it into a bowl.",
                        "Add the finely chopped onion and the olive oil.",
                        "Toss everything with the warm pasta."},
                "Pasta:200:g", "Tuna:1:pcs", "Onion:0.5:pcs", "Olive oil:1:tbsp"));

        list.add(recipe("Egg Fried Noodles", 15,
                new String[]{
                        "Cook the noodles, drain and rinse with cold water.",
                        "Scramble the eggs in the hot oil and set aside.",
                        "Stir-fry the sliced spring onions for 1 minute.",
                        "Add the noodles, eggs and soy sauce and toss for 2 minutes."},
                "Noodles:200:g", "Egg:2:pcs", "Spring onion:2:pcs", "Soy sauce:2:tbsp",
                "Vegetable oil:1:tbsp"));

        list.add(recipe("Stovetop Mac and Cheese", 30,
                new String[]{
                        "Boil the macaroni until tender and drain.",
                        "Melt the butter, stir in the flour and cook for one minute.",
                        "Slowly whisk in the milk until the sauce thickens.",
                        "Stir in the cheese until melted, then mix in the macaroni."},
                "Macaroni:250:g", "Cheese:150:g", "Milk:300:ml", "Butter:30:g", "Flour:2:tbsp"));

        list.add(recipe("Beans on Toast", 10,
                new String[]{
                        "Toast the bread and spread with butter.",
                        "Warm the baked beans in a small pot.",
                        "Spoon the beans over the toast."},
                "Baked beans:1:pcs", "Bread:2:pcs", "Butter:10:g"));

        list.add(recipe("Avocado Toast", 5,
                new String[]{
                        "Toast the bread.",
                        "Mash the avocado with a squeeze of lemon juice and the salt.",
                        "Spread generously over the toast."},
                "Avocado:1:pcs", "Bread:2:pcs", "Lemon:1:pcs", "Salt:0.25:tsp"));

        list.add(recipe("Simple Tomato Soup", 30,
                new String[]{
                        "Melt the butter and soften the chopped onion and garlic.",
                        "Add the chopped tomatoes and the stock.",
                        "Simmer for 20 minutes, then blend until smooth."},
                "Tomato:6:pcs", "Onion:1:pcs", "Garlic:2:pcs", "Vegetable stock:500:ml",
                "Butter:20:g"));

        list.add(recipe("Chicken Stir-Fry", 25,
                new String[]{
                        "Slice the chicken, bell pepper and onion into strips.",
                        "Heat the oil and brown the chicken for 5-6 minutes.",
                        "Add the vegetables and crushed garlic and stir-fry for 4 minutes.",
                        "Stir through the soy sauce and serve."},
                "Chicken breast:250:g", "Bell pepper:1:pcs", "Onion:1:pcs", "Soy sauce:2:tbsp",
                "Garlic:2:pcs", "Vegetable oil:1:tbsp"));

        list.add(recipe("Yoghurt Oat Parfait", 5,
                new String[]{
                        "Layer half the yoghurt in a glass.",
                        "Add half the oats and sliced banana.",
                        "Repeat the layers and finish with a drizzle of honey."},
                "Yoghurt:200:g", "Oats:40:g", "Honey:1:tbsp", "Banana:1:pcs"));

        list.add(recipe("Spinach Cheese Omelette", 12,
                new String[]{
                        "Wilt the spinach in the melted butter for one minute.",
                        "Pour the beaten eggs over the spinach.",
                        "Add the cheese, fold the omelette and cook until set."},
                "Egg:3:pcs", "Spinach:50:g", "Cheese:40:g", "Butter:10:g"));

        list.add(recipe("Quick Bacon Carbonara", 25,
                new String[]{
                        "Cook the pasta and keep a cup of the water.",
                        "Fry the bacon until crisp.",
                        "Beat the eggs with the grated cheese and black pepper.",
                        "Toss the hot pasta with the bacon, then stir in the egg mixture off the heat, loosening with pasta water."},
                "Pasta:200:g", "Egg:2:pcs", "Cheese:60:g", "Bacon:100:g", "Black pepper:0.5:tsp"));

        return list;
    }

    /** Builds one recipe; steps are numbered and joined with newlines. */
    private static Recipe recipe(String name, int minutes, String[] steps, String... ingredients) {
        StringBuilder method = new StringBuilder();
        for (int i = 0; i < steps.length; i++) {
            if (i > 0) {
                method.append('\n');
            }
            method.append(i + 1).append(". ").append(steps[i]);
        }

        List<RecipeIngredient> parsed = new ArrayList<>();
        for (String spec : ingredients) {
            String[] parts = spec.split(":");
            parsed.add(new RecipeIngredient(parts[0], Double.parseDouble(parts[1]), parts[2]));
        }
        return new Recipe(0, name, minutes, method.toString(), parsed);
    }
}
