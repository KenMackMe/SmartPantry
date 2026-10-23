package com.example.smartpantry.logic;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class UnitsTest {

    private static final double DELTA = 1e-6;

    @Test
    public void weightUnitsConvertToGrams() {
        assertEquals(1000, Units.toBase(1, "kg"), DELTA);
        assertEquals(28.3495, Units.toBase(1, "oz"), DELTA);
        assertEquals(Units.Dimension.WEIGHT, Units.dimensionOf("Grams"));
    }

    @Test
    public void volumeUnitsConvertToMillilitres() {
        assertEquals(1000, Units.toBase(1, "l"), DELTA);
        assertEquals(15, Units.toBase(1, "tbsp"), DELTA);
        assertEquals(5, Units.toBase(1, "tsp."), DELTA);        // trailing dot tolerated
        assertEquals(240, Units.toBase(1, "cup"), DELTA);
        assertEquals(Units.Dimension.VOLUME, Units.dimensionOf("ML"));
    }

    @Test
    public void countAndUnknownUnitsAreCounts() {
        assertEquals(Units.Dimension.COUNT, Units.dimensionOf("pcs"));
        assertEquals(Units.Dimension.COUNT, Units.dimensionOf("cloves"));
        assertEquals(Units.Dimension.COUNT, Units.dimensionOf("handful"));
        assertEquals(Units.Dimension.COUNT, Units.dimensionOf(null));
    }

    @Test
    public void quantitiesAreFormattedWithoutTrailingZeros() {
        assertEquals("2", Units.formatQuantity(2.0));
        assertEquals("1.5", Units.formatQuantity(1.5));
        assertEquals("0.25", Units.formatQuantity(0.25));
        assertEquals("0.33", Units.formatQuantity(1.0 / 3));
    }
}
