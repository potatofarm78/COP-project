package com.example.ecommerce.checkout;

import com.example.currencyconverter.CurrencyConverter;
import com.example.ecommerce.model.ShoppingCart;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * Builds receipts using an injected CurrencyConverter, without knowing its exchange rates.
 */
public final class CheckoutService {

    private final CurrencyConverter converter;

    public CheckoutService(CurrencyConverter converter) {
        this.converter = Objects.requireNonNull(converter, "Currency converter is required.");
    }

    public CheckoutReceipt checkout(ShoppingCart cart, String localCurrency) {
        Objects.requireNonNull(cart, "Shopping cart is required.");
        if (cart.isEmpty()) {
            throw new IllegalStateException("Your shopping cart is empty. Add a product before checking out.");
        }

        BigDecimal totalUsd = cart.getTotalUsd();

        // Cross the reusable component's double-based API only after summing exact USD prices.
        double converted = converter.convert(totalUsd.doubleValue(), "USD", localCurrency);
        BigDecimal convertedTotal = BigDecimal.valueOf(converted).setScale(2, RoundingMode.HALF_UP);

        List<CheckoutReceipt.LineItem> lines = cart.getItems().stream()
                .map(item -> new CheckoutReceipt.LineItem(item.getProduct().name(), item.getQuantity(),
                        item.getProduct().priceUsd(), item.getLineTotalUsd()))
                .toList();

        return new CheckoutReceipt("GM-" + UUID.randomUUID(), Instant.now(), lines, totalUsd,
                localCurrency.strip().toUpperCase(Locale.ROOT), convertedTotal);
    }
}
