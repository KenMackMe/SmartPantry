package com.example.smartpantry.model;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * One ingredient the user currently has at home.
 *
 * <p>This is a plain Java object (no Android classes) so it can be unit tested
 * on the JVM. The expiry date is stored as an ISO-8601 string (yyyy-MM-dd) or
 * {@code null} when the user did not enter one.</p>
 */
public class PantryItem {

    /** Database row id, or -1 when the item has not been saved yet. */
    private long id;
    private String name;
    private double quantity;
    private String unit;
    /** ISO date (yyyy-MM-dd) or null. */
    private String expiry;

    public PantryItem(long id, String name, double quantity, String unit, String expiry) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiry = expiry;
    }

    /** Convenience constructor for a brand-new (unsaved) item. */
    public PantryItem(String name, double quantity, String unit, String expiry) {
        this(-1, name, quantity, unit, expiry);
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getExpiry() { return expiry; }
    public void setExpiry(String expiry) { this.expiry = expiry; }

    /** @return the parsed expiry date, or null if there is none / it is malformed. */
    public LocalDate getExpiryDate() {
        if (expiry == null || expiry.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(expiry.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** An item is expired once its expiry date is strictly before today. */
    public boolean isExpired(LocalDate today) {
        LocalDate date = getExpiryDate();
        return date != null && date.isBefore(today);
    }
}
