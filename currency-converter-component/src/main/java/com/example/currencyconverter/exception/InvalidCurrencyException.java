package com.example.currencyconverter.exception;

/**
 * Signals that a currency code is missing or unsupported by the converter.
 */
public class InvalidCurrencyException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates an exception with an explanation of the invalid currency.
     *
     * @param message a description of the validation failure
     */
    public InvalidCurrencyException(String message) {
        super(message);
    }
}
