package com.example.ecommerce.checkout;

import java.nio.file.Path;
import java.util.Objects;

/** Completed checkout: the immutable receipt and its downloaded PDF. */
public record PurchaseResult(CheckoutReceipt receipt, Path receiptPath) {

    public PurchaseResult {
        Objects.requireNonNull(receipt);
        Objects.requireNonNull(receiptPath);
    }
}
