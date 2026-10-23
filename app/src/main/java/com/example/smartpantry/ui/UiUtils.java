package com.example.smartpantry.ui;

import com.example.smartpantry.logic.Units;
import com.example.smartpantry.model.RecipeIngredient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/** Small formatting helpers shared by several screens. */
final class UiUtils {

    private UiUtils() {
        // Static utility class.
    }

    /** "tomatoes" -> "Tomatoes" (display only; the stored text is unchanged). */
    static String capitalize(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        return text.substring(0, 1).toUpperCase(Locale.getDefault()) + text.substring(1);
    }

    /** 2026-10-18 -> "18 Oct 2026" in the device's language. */
    static String formatDate(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault()));
    }

    /** @return the parsed ISO date, or null if the text is null/blank/malformed. */
    static LocalDate parseDate(String iso) {
        if (iso == null || iso.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(iso.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** "3 pcs", "1.5 cup" */
    static String amount(double quantity, String unit) {
        return Units.formatQuantity(quantity) + " " + unit;
    }

    /** "Tomato (4 pcs)" - used in the "Missing:" line of the Almost There list. */
    static String describeIngredient(RecipeIngredient ingredient) {
        return capitalize(ingredient.getName()) + " ("
                + amount(ingredient.getQuantity(), ingredient.getUnit()) + ")";
    }
}
