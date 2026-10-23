package com.example.smartpantry.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Unit tests: singular/plural and other trivial differences must not break matching. */
public class IngredientNormalizerTest {

    private static void assertSame(String a, String b) {
        assertEquals("'" + a + "' should match '" + b + "'",
                IngredientNormalizer.normalize(a), IngredientNormalizer.normalize(b));
    }

    @Test
    public void tomatoMatchesTomatoes() {
        assertSame("tomato", "tomatoes");
        assertSame("Tomato", "  TOMATOES ");
    }

    @Test
    public void commonPluralsMatchSingulars() {
        assertSame("egg", "eggs");
        assertSame("potato", "Potatoes");
        assertSame("onion", "onions");
        assertSame("garlic clove", "garlic cloves");
        assertSame("berry", "berries");
        assertSame("peach", "peaches");
        assertSame("loaf", "loaves");
        assertSame("cookie", "cookies");
        assertSame("avocado", "avocados");
        assertSame("lentil", "lentils");
        assertSame("oat", "oats");
    }

    @Test
    public void wordsThatEndInSButAreNotPluralsAreKept() {
        assertEquals("hummus", IngredientNormalizer.normalize("Hummus"));
        assertEquals("couscous", IngredientNormalizer.normalize("couscous"));
        assertEquals("asparagus", IngredientNormalizer.normalize("Asparagus"));
        assertEquals("molasses", IngredientNormalizer.normalize("molasses"));
    }

    @Test
    public void descriptorsAndPunctuationAreIgnored() {
        assertSame("tomato", "Fresh, chopped tomatoes");
        assertSame("onion", "Large Onion (diced)");
        assertSame("flour", "all-purpose flour");
    }

    @Test
    public void synonymsMapToOneName() {
        assertSame("spring onion", "scallions");
        assertSame("spring onion", "green onions");
        assertSame("bell pepper", "capsicum");
        assertSame("yoghurt", "Yogurt");
        assertSame("pasta", "spaghetti");
    }

    @Test
    public void differentIngredientsStayDifferent() {
        assertTrue(!IngredientNormalizer.normalize("butter").equals(IngredientNormalizer.normalize("buttermilk")));
        assertTrue(!IngredientNormalizer.normalize("rice").equals(IngredientNormalizer.normalize("rice flour")));
    }

    @Test
    public void textWithoutLettersNormalisesToEmpty() {
        assertEquals("", IngredientNormalizer.normalize("123"));
        assertEquals("", IngredientNormalizer.normalize("   "));
        assertEquals("", IngredientNormalizer.normalize(null));
    }
}
