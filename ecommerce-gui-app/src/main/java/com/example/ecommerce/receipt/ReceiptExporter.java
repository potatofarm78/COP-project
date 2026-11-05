package com.example.ecommerce.receipt;

import com.example.ecommerce.checkout.CheckoutReceipt;

import java.io.IOException;
import java.nio.file.Path;

/** Saves a purchase receipt and returns the location of the downloaded file. */
@FunctionalInterface
public interface ReceiptExporter {
    Path export(CheckoutReceipt receipt) throws IOException;
}
