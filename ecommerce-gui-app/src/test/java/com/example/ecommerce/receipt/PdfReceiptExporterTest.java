package com.example.ecommerce.receipt;

import com.example.currencyconverter.CurrencyConverterImpl;
import com.example.ecommerce.checkout.CheckoutReceipt;
import com.example.ecommerce.checkout.CheckoutService;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.model.ShoppingCart;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PdfReceiptExporterTest {

    @TempDir
    Path directory;

    @Test
    void writesAReadablePdfWithItemsAndTotals() throws IOException {
        CheckoutReceipt receipt = receipt();
        Path output = new PdfReceiptExporter(directory.resolve("Downloads with spaces")).export(receipt);

        assertTrue(Files.isRegularFile(output));
        assertTrue(output.getFileName().toString().endsWith(".pdf"));
        try (PDDocument document = Loader.loadPDF(output.toFile())) {
            String text = new PDFTextStripper().getText(document);
            assertEquals("GlobalMart", document.getDocumentInformation().getAuthor());
            assertTrue(text.contains(receipt.receiptNumber()));
            assertTrue(text.contains("Purchased:"));
            assertTrue(text.contains("2 x Laptop"));
            assertTrue(text.contains("Total (USD): 2,400.00 USD"));
            assertTrue(text.contains("Total (EUR): 2,208.00 EUR"));
        }
    }

    @Test
    void givesEachPurchaseAUniqueFileAndNeverOverwritesReceipts() throws IOException {
        PdfReceiptExporter exporter = new PdfReceiptExporter(directory);
        CheckoutReceipt first = receipt();
        Path firstFile = exporter.export(first);
        byte[] original = Files.readAllBytes(firstFile);
        Path secondFile = exporter.export(receipt());

        assertNotEquals(firstFile, secondFile);
        assertThrows(FileAlreadyExistsException.class, () -> exporter.export(first));
        assertArrayEquals(original, Files.readAllBytes(firstFile));
    }

    @Test
    void longReceiptsKeepAllItemsAcrossMultiplePages() throws IOException {
        List<CheckoutReceipt.LineItem> items = IntStream.range(0, 120)
                .mapToObj(index -> new CheckoutReceipt.LineItem("Item " + index, 1,
                        new BigDecimal("1.00"), new BigDecimal("1.00"))).toList();
        CheckoutReceipt receipt = new CheckoutReceipt("GM-many-items", Instant.now(), items,
                new BigDecimal("120.00"), "USD", new BigDecimal("120.00"));
        Path output = new PdfReceiptExporter(directory).export(receipt);

        try (PDDocument document = Loader.loadPDF(output.toFile())) {
            assertTrue(document.getNumberOfPages() > 1);
            String text = new PDFTextStripper().getText(document);
            assertTrue(text.contains("Item 0"));
            assertTrue(text.contains("Item 119"));
            assertTrue(text.contains("Total (USD): 120.00 USD"));
        }
    }

    private CheckoutReceipt receipt() {
        ShoppingCart cart = new ShoppingCart();
        Product laptop = new Product("laptop", "Laptop", new BigDecimal("1200.00"));
        cart.add(laptop);
        cart.add(laptop);
        return new CheckoutService(new CurrencyConverterImpl()).checkout(cart, "EUR");
    }
}
