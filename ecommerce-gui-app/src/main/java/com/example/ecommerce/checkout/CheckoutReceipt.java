package com.example.ecommerce.checkout;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Immutable snapshot of the purchase, independent of subsequent cart changes.
 */
public record CheckoutReceipt(String receiptNumber, Instant purchasedAt,
                              List<LineItem> items, BigDecimal totalUsd,
                              String localCurrency, BigDecimal convertedTotal) {

    public CheckoutReceipt {
        Objects.requireNonNull(receiptNumber, "Receipt number is required.");
        if (!receiptNumber.matches("[A-Za-z0-9-]+")) {
            throw new IllegalArgumentException("Receipt number may contain only letters, numbers and hyphens.");
        }
        Objects.requireNonNull(purchasedAt, "Purchase time is required.");
        items = List.copyOf(items);
        Objects.requireNonNull(totalUsd, "USD total is required.");
        Objects.requireNonNull(localCurrency, "Local currency is required.");
        Objects.requireNonNull(convertedTotal, "Converted total is required.");
    }

    public String toDisplayText() {
        String date = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'", Locale.ROOT)
                .withZone(ZoneOffset.UTC).format(purchasedAt);
        StringBuilder text = new StringBuilder("GLOBALMART RECEIPT\n");
        text.append("Receipt: ").append(receiptNumber).append('\n');
        text.append("Purchased: ").append(date).append("\n\n");

        for (LineItem item : items) {
            text.append(String.format(Locale.ROOT, "%d x %s @ %,.2f USD = %,.2f USD%n",
                    item.quantity(), item.productName(), item.unitPriceUsd(), item.lineTotalUsd()));
        }

        text.append(String.format(Locale.ROOT,
                "%nTotal (USD): %,.2f USD%nTotal (%s): %,.2f %s%n",
                totalUsd, localCurrency, convertedTotal, localCurrency));
        return text.toString();
    }

    public record LineItem(String productName, int quantity,
                           BigDecimal unitPriceUsd, BigDecimal lineTotalUsd) {
    }
}
