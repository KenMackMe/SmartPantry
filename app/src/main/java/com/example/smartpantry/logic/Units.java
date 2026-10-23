package com.example.smartpantry.logic;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Unit handling for the strict-matching rule.
 *
 * <p>Every unit belongs to one {@link Dimension} (weight, volume or count) and
 * has a factor that converts it to that dimension's base unit (grams,
 * millilitres or "pieces"). That lets "1 kg flour" in the pantry satisfy a
 * recipe that asks for "200 g flour", and "0.5 l milk" satisfy "300 ml milk".</p>
 *
 * <p>Quantities in DIFFERENT dimensions (e.g. pantry "garlic 50 g" vs recipe
 * "garlic 3 pcs") cannot be compared reliably, so they are deliberately treated
 * as NOT enough. This errs on the strict side, which is what the brief wants.</p>
 */
public final class Units {

    /** The three kinds of quantity the app can compare. */
    public enum Dimension { WEIGHT, VOLUME, COUNT }

    /** Units offered in the pantry form's spinner (must match res/values/strings.xml). */
    public static final String[] DISPLAY_UNITS =
            {"pcs", "g", "kg", "ml", "l", "tsp", "tbsp", "cup", "oz", "lb"};

    /** Alias -> (dimension, factor to base unit). */
    private static final Map<String, UnitInfo> UNITS = new HashMap<>();

    private static final class UnitInfo {
        final Dimension dimension;
        final double factor;

        UnitInfo(Dimension dimension, double factor) {
            this.dimension = dimension;
            this.factor = factor;
        }
    }

    private static void register(Dimension d, double factor, String... aliases) {
        for (String alias : aliases) {
            UNITS.put(alias, new UnitInfo(d, factor));
        }
    }

    static {
        // Weight -> grams
        register(Dimension.WEIGHT, 1, "g", "gram", "grams", "gm", "gms");
        register(Dimension.WEIGHT, 1000, "kg", "kgs", "kilo", "kilos", "kilogram", "kilograms");
        register(Dimension.WEIGHT, 0.001, "mg", "milligram", "milligrams");
        register(Dimension.WEIGHT, 28.3495, "oz", "ounce", "ounces");
        register(Dimension.WEIGHT, 453.592, "lb", "lbs", "pound", "pounds");

        // Volume -> millilitres
        register(Dimension.VOLUME, 1, "ml", "mls", "cc", "millilitre", "millilitres",
                "milliliter", "milliliters");
        register(Dimension.VOLUME, 1000, "l", "litre", "litres", "liter", "liters", "ltr");
        register(Dimension.VOLUME, 5, "tsp", "tsps", "teaspoon", "teaspoons");
        register(Dimension.VOLUME, 15, "tbsp", "tbsps", "tbs", "tablespoon", "tablespoons");
        register(Dimension.VOLUME, 240, "cup", "cups");

        // Anything you count one by one -> pieces
        register(Dimension.COUNT, 1, "pcs", "pc", "piece", "pieces", "each", "unit", "units",
                "whole", "clove", "cloves", "slice", "slices", "can", "cans", "tin", "tins",
                "bunch", "bunches", "pack", "packs", "packet", "packets");
    }

    private Units() {
        // Static utility class.
    }

    /** Lower-cases, trims and strips a trailing dot ("Tbsp." -> "tbsp"). */
    private static String key(String unit) {
        if (unit == null) {
            return "";
        }
        String k = unit.trim().toLowerCase(Locale.ROOT);
        if (k.endsWith(".")) {
            k = k.substring(0, k.length() - 1);
        }
        return k;
    }

    /** Unknown or blank units are treated as plain counts. */
    private static UnitInfo info(String unit) {
        UnitInfo info = UNITS.get(key(unit));
        return info != null ? info : new UnitInfo(Dimension.COUNT, 1);
    }

    public static Dimension dimensionOf(String unit) {
        return info(unit).dimension;
    }

    /** Converts a quantity to its dimension's base unit (g, ml or pieces). */
    public static double toBase(double quantity, String unit) {
        return quantity * info(unit).factor;
    }

    /** Formats 2.0 as "2" and 1.5 as "1.5" (max two decimals), locale independent. */
    public static String formatQuantity(double quantity) {
        DecimalFormat format = new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.US));
        return format.format(quantity);
    }
}
