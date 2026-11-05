package com.example.currencyconverter;

import com.example.currencyconverter.exception.InvalidCurrencyException;

/**
 * Reusable contract for converting amounts between supported currencies.
 * Implementations handle exchange rates independently of any user interface.
 */
public interface CurrencyConverter {

    /**
     * Converts a non-negative, finite amount without rounding the result.
     * Currency codes are case-insensitive and may contain surrounding whitespace.
     *
     * @param amount the amount in the source currency
     * @param fromCurrency the source currency code, for example {@code USD}
     * @param toCurrency the target currency code, for example {@code EUR}
     * @return the equivalent amount in the target currency
     * @throws InvalidCurrencyException if either code is null, blank, or unsupported
     * @throws IllegalArgumentException if the amount is negative or non-finite,
     *                                  or the converted result overflows
     */
    double convert(double amount, String fromCurrency, String toCurrency);
}
