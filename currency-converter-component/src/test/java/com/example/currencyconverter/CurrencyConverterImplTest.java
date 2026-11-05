package com.example.currencyconverter;

import com.example.currencyconverter.exception.InvalidCurrencyException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CurrencyConverterImplTest {

    private static final double TOLERANCE = 1.0e-9;

    private final CurrencyConverter converter = new CurrencyConverterImpl();

    @ParameterizedTest
    @CsvSource({
            "100.0, USD, EUR, 92.0",
            "100.0, USD, GBP, 78.0",
            "100.0, USD, KES, 12900.0",
            "92.0, EUR, USD, 100.0",
            "78.0, GBP, USD, 100.0",
            "129.0, KES, USD, 1.0",
            "92.0, EUR, GBP, 78.0",
            "78.0, GBP, KES, 12900.0",
            "12900.0, KES, EUR, 92.0"
    })
    void convertsKnownAmounts(double amount, String from, String to, double expected) {
        assertEquals(expected, converter.convert(amount, from, to), TOLERANCE);
    }

    @Test
    void preservesAmountWhenCurrenciesMatch() {
        assertEquals(25.75, converter.convert(25.75, "GBP", "GBP"));
        assertEquals(Double.MAX_VALUE, converter.convert(Double.MAX_VALUE, "GBP", "GBP"));
    }

    @Test
    void acceptsCaseInsensitiveCodesAndSurroundingWhitespace() {
        assertEquals(92.0, converter.convert(100.0, "  usd ", " eUr  "), TOLERANCE);
    }

    @Test
    void convertsZeroAmount() {
        assertEquals(0.0, converter.convert(0.0, "EUR", "KES"));
    }

    @Test
    void preservesPrecisionAcrossARoundTrip() {
        double converted = converter.convert(123.45, "EUR", "KES");
        double restored = converter.convert(converted, "KES", "EUR");

        assertEquals(123.45, restored, TOLERANCE);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   ", "CAD", "US"})
    void rejectsInvalidSourceCurrencies(String currency) {
        assertThrows(InvalidCurrencyException.class,
                () -> converter.convert(100.0, currency, "USD"));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   ", "JPY", "EURO"})
    void rejectsInvalidTargetCurrencies(String currency) {
        assertThrows(InvalidCurrencyException.class,
                () -> converter.convert(100.0, "USD", currency));
    }

    @ParameterizedTest
    @ValueSource(doubles = {-1.0, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
    void rejectsInvalidAmounts(double amount) {
        assertThrows(IllegalArgumentException.class,
                () -> converter.convert(amount, "USD", "EUR"));
    }

    @Test
    void rejectsConversionOverflow() {
        assertThrows(IllegalArgumentException.class,
                () -> converter.convert(Double.MAX_VALUE, "USD", "KES"));
    }
}
