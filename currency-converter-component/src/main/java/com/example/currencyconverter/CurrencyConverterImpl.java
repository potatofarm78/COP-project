package com.example.currencyconverter;

import com.example.currencyconverter.exception.InvalidCurrencyException;

import java.util.Locale;
import java.util.Map;

/**
 * Stateless, thread-safe converter using illustrative, fixed exchange rates.
 */
public final class CurrencyConverterImpl implements CurrencyConverter {

    // Each value represents the number of currency units equal to one USD.
    private static final Map<String, Double> RATES_PER_USD = Map.of(
            "USD", 1.0,
            "EUR", 0.92,
            "GBP", 0.78,
            "KES", 129.0
    );

    @Override
    public double convert(double amount, String fromCurrency, String toCurrency) {
        if (!Double.isFinite(amount) || amount < 0.0) {
            throw new IllegalArgumentException("Amount must be a non-negative, finite number.");
        }

        double fromRate = exchangeRateFor(fromCurrency);
        double toRate = exchangeRateFor(toCurrency);

        // The cross-rate is equivalent to converting through the common USD base.
        double convertedAmount = amount * (toRate / fromRate);
        if (!Double.isFinite(convertedAmount)) {
            throw new IllegalArgumentException("Converted amount is too large to represent.");
        }

        return convertedAmount;
    }

    private double exchangeRateFor(String currency) {
        if (currency == null || currency.isBlank()) {
            throw new InvalidCurrencyException("Currency code must not be null or blank.");
        }

        String normalizedCode = currency.strip().toUpperCase(Locale.ROOT);
        Double rate = RATES_PER_USD.get(normalizedCode);
        if (rate == null) {
            throw new InvalidCurrencyException("Unsupported currency code: " + normalizedCode + ".");
        }

        return rate;
    }
}
