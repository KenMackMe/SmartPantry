package com.example.smartpantry.logic;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Turns free-text ingredient names into a canonical "key" so that trivial
 * differences do not break matching.
 *
 * <p>The assignment explicitly says a naive exact-string match that fails on
 * "tomato" vs "tomatoes" will be marked down, so every name (from the pantry
 * AND from the recipes) is passed through {@link #normalize(String)} before
 * being compared. Examples that all end up as the same key:</p>
 * <ul>
 *   <li>"Tomato", "tomatoes", "  TOMATOES  ", "Fresh tomatoes" -> "tomato"</li>
 *   <li>"Potato" / "Potatoes" -> "potato"</li>
 *   <li>"Green onions" / "Scallions" -> "spring onion"</li>
 *   <li>"Yogurt" -> "yoghurt", "Spaghetti" -> "pasta"</li>
 * </ul>
 *
 * <p>This class has no Android dependencies, so it is unit tested on the JVM.</p>
 */
public final class IngredientNormalizer {

    /** Words that describe preparation/size, not the ingredient itself. */
    private static final Set<String> DESCRIPTORS = new HashSet<>(Arrays.asList(
            "fresh", "large", "small", "medium", "big", "ripe", "raw", "whole",
            "chopped", "diced", "sliced", "minced", "grated", "peeled", "frozen"));

    /** Words that already end in "s" but are NOT plurals. */
    private static final Set<String> INVARIANT = new HashSet<>(Arrays.asList(
            "hummus", "couscous", "asparagus", "molasses", "swiss", "citrus",
            "watercress", "cress", "bass", "lens", "series", "species"));

    /** Alternative names -> one canonical name (applied AFTER singularising). */
    private static final Map<String, String> SYNONYMS = new HashMap<>();

    static {
        SYNONYMS.put("green onion", "spring onion");
        SYNONYMS.put("scallion", "spring onion");
        SYNONYMS.put("capsicum", "bell pepper");
        SYNONYMS.put("sweet pepper", "bell pepper");
        SYNONYMS.put("aubergine", "eggplant");
        SYNONYMS.put("courgette", "zucchini");
        SYNONYMS.put("coriander", "cilantro");
        SYNONYMS.put("yogurt", "yoghurt");
        SYNONYMS.put("garbanzo bean", "chickpea");
        SYNONYMS.put("plain flour", "flour");
        SYNONYMS.put("all purpose flour", "flour");
        SYNONYMS.put("wheat flour", "flour");
        SYNONYMS.put("extra virgin olive oil", "olive oil");
        SYNONYMS.put("sunflower oil", "vegetable oil");
        SYNONYMS.put("canola oil", "vegetable oil");
        SYNONYMS.put("cooking oil", "vegetable oil");
        SYNONYMS.put("spaghetti", "pasta");
        SYNONYMS.put("penne", "pasta");
        SYNONYMS.put("fusilli", "pasta");
        SYNONYMS.put("linguine", "pasta");
        SYNONYMS.put("tagliatelle", "pasta");
        SYNONYMS.put("minced beef", "ground beef");
        SYNONYMS.put("chicken fillet", "chicken breast");
    }

    private IngredientNormalizer() {
        // Static utility class - not meant to be instantiated.
    }

    /**
     * @param raw the name exactly as typed by the user / written in the recipe
     * @return the canonical key, or an empty string if the text contains no letters
     */
    public static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        // 1. lower-case and turn anything that is not a letter into a space
        //    ("Tomato, chopped" / "all-purpose" / "Onion (red)" all become words)
        String cleaned = raw.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\s]", " ");

        // 2. split into words, drop descriptors, singularise each remaining word
        StringBuilder key = new StringBuilder();
        for (String word : cleaned.trim().split("\\s+")) {
            if (word.isEmpty() || DESCRIPTORS.contains(word)) {
                continue;
            }
            if (key.length() > 0) {
                key.append(' ');
            }
            key.append(singularize(word));
        }

        // 3. map synonyms ("scallion" -> "spring onion") to one canonical name
        String result = key.toString();
        String canonical = SYNONYMS.get(result);
        return canonical != null ? canonical : result;
    }

    /**
     * Small rule-based singulariser. It does not need to be linguistically
     * perfect - it only has to turn the plural and singular forms of the SAME
     * word into the SAME string, because both sides of every comparison go
     * through it.
     */
    static String singularize(String word) {
        if (word.length() <= 3 || INVARIANT.contains(word)) {
            return word;
        }

        String w = word;
        if (w.endsWith("ies") && w.length() > 4) {
            w = w.substring(0, w.length() - 3) + "y";           // berries -> berry
        } else if (w.endsWith("oes")) {
            w = w.substring(0, w.length() - 2);                  // tomatoes -> tomato
        } else if (w.endsWith("leaves") || w.endsWith("loaves") || w.endsWith("halves")) {
            w = w.substring(0, w.length() - 3) + "f";            // leaves -> leaf
        } else if (w.endsWith("ches") || w.endsWith("shes")
                || w.endsWith("xes") || w.endsWith("sses")) {
            w = w.substring(0, w.length() - 2);                  // peaches -> peach
        } else if (w.endsWith("ss") || w.endsWith("us") || w.endsWith("is")) {
            // already singular: swiss, hummus, asparagus, basis ...
        } else if (w.endsWith("s")) {
            w = w.substring(0, w.length() - 1);                  // eggs -> egg, cloves -> clove
        }

        // "cookie" and "cookies" must meet in the middle: cookies -> cooky (via
        // the "ies" rule above), so cookie -> cooky as well.
        if (w.endsWith("ie") && w.length() > 3) {
            w = w.substring(0, w.length() - 2) + "y";
        }
        return w;
    }
}
