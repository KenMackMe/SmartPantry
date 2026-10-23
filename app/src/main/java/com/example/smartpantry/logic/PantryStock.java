package com.example.smartpantry.logic;

import com.example.smartpantry.model.PantryItem;
import com.example.smartpantry.model.RecipeIngredient;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A "snapshot" of the pantry that is optimised for matching.
 *
 * <p>Pantry rows are merged by canonical ingredient name and by dimension, so
 * two separate rows such as "Milk 500 ml" and "Milk 1 l" count as 1500 ml of
 * milk. All amounts are held in base units (g, ml, pieces).</p>
 */
public final class PantryStock {

    /** Tolerance for floating point rounding (e.g. 0.1 + 0.2 != 0.3 exactly). */
    private static final double EPSILON = 1e-9;

    /** canonical name -> totals indexed by {@link Units.Dimension#ordinal()}. */
    private final Map<String, double[]> totals = new HashMap<>();

    private PantryStock() {
    }

    /**
     * Builds a stock snapshot from the pantry rows.
     *
     * @param items         every row in the pantry table
     * @param ignoreExpired when true, items whose expiry date is before {@code today} are skipped
     * @param today         the date used for the expiry check
     */
    public static PantryStock from(List<PantryItem> items, boolean ignoreExpired, LocalDate today) {
        PantryStock stock = new PantryStock();
        for (PantryItem item : items) {
            if (ignoreExpired && item.isExpired(today)) {
                continue;
            }
            String key = IngredientNormalizer.normalize(item.getName());
            if (key.isEmpty() || item.getQuantity() <= 0) {
                continue;
            }
            Units.Dimension dimension = Units.dimensionOf(item.getUnit());
            double[] row = stock.totals.get(key);
            if (row == null) {
                row = new double[Units.Dimension.values().length];
                stock.totals.put(key, row);
            }
            row[dimension.ordinal()] += Units.toBase(item.getQuantity(), item.getUnit());
        }
        return stock;
    }

    /**
     * @return true only if the pantry holds AT LEAST the quantity the recipe
     *         line asks for, in a comparable unit.
     */
    public boolean covers(RecipeIngredient need) {
        String key = IngredientNormalizer.normalize(need.getName());
        if (key.isEmpty()) {
            return false;                       // a nameless requirement can never be satisfied
        }
        double[] row = totals.get(key);
        if (row == null) {
            return false;                       // ingredient is not in the pantry at all
        }
        double have = row[Units.dimensionOf(need.getUnit()).ordinal()];
        double required = Units.toBase(need.getQuantity(), need.getUnit());
        return have + EPSILON >= required;
    }
}
