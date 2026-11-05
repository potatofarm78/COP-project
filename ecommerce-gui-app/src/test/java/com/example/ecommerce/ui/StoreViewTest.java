package com.example.ecommerce.ui;

import com.example.currencyconverter.CurrencyConverterImpl;
import com.example.ecommerce.EcommerceApplication;
import com.example.ecommerce.checkout.PurchaseService;
import com.example.ecommerce.model.CartItem;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.receipt.PdfReceiptExporter;
import com.example.ecommerce.receipt.ReceiptExporter;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/** Exercises actual JavaFX windows and controls using the test-only headless Monocle toolkit. */
class StoreViewTest {

    @TempDir
    Path directory;

    private Stage stage;
    private EcommerceApplication application;
    private Path receiptDirectory;
    private final AtomicReference<ReceiptExporter> exporter = new AtomicReference<>();

    @BeforeAll
    static void startJavaFx() throws InterruptedException {
        CountDownLatch ready = new CountDownLatch(1);
        Platform.startup(() -> {
            Platform.setImplicitExit(false);
            ready.countDown();
        });
        assertTrue(ready.await(30, TimeUnit.SECONDS), "JavaFX toolkit did not start.");
    }

    @AfterAll
    static void stopJavaFx() {
        Platform.exit();
    }

    @BeforeEach
    void openStore() throws Exception {
        receiptDirectory = directory.resolve("Downloads with spaces");
        exporter.set(new PdfReceiptExporter(receiptDirectory));
        PurchaseService purchases = new PurchaseService(new CurrencyConverterImpl(),
                receipt -> exporter.get().export(receipt));
        onJavaFxThread(() -> {
            stage = new Stage();
            application = new EcommerceApplication(purchases);
            application.start(stage);
            assertTrue(stage.getScene().getRoot() instanceof StoreView);
            return null;
        });
    }

    @AfterEach
    void closeWindows() throws Exception {
        onJavaFxThread(() -> {
            application.stop();
            List.copyOf(Window.getWindows()).forEach(Window::hide);
            return null;
        });
    }

    @Test
    void opensTheRequiredWindowWithProductsAndAnEmptyCart() throws Exception {
        onJavaFxThread(() -> {
            assertEquals("GlobalMart", stage.getTitle());
            HBox logo = (HBox) node("globalmart-logo");
            assertEquals("globe-icon", logo.getChildren().get(0).getId());
            assertEquals("GlobalMart", ((Label) logo.getChildren().get(1)).getText());
            assertFalse(stage.getIcons().isEmpty());
            assertEquals(24, this.<Product>table("product-table").getItems().size());
            assertTrue(this.<CartItem>table("cart-table").getItems().isEmpty());
            assertEquals(List.of("USD", "EUR", "GBP", "KES"), currencySelector().getItems());
            assertEquals("USD", currencySelector().getValue());
            assertTrue(button("checkout-button").isDisabled());
            assertEquals("0.00 USD", label("usd-total").getText());
            return null;
        });
    }

    @Test
    void buttonsQuantityControlsSortingAndClearUpdateTheCart() throws Exception {
        onJavaFxThread(() -> {
            button("add-laptop").fire();
            button("add-laptop").fire();
            button("add-headphones").fire();

            TableView<CartItem> cartTable = table("cart-table");
            assertEquals(2, cartTable.getItems().size());
            assertEquals("3 item(s)", label("cart-count").getText());
            assertEquals("2,550.00 USD", label("usd-total").getText());
            assertFalse(button("checkout-button").isDisabled());

            // Sorting must work even though the underlying cart collection is read-only.
            cartTable.getSortOrder().setAll(List.of(cartTable.getColumns().get(0)));
            cartTable.sort();
            assertEquals("Headphones", cartTable.getItems().get(0).getProduct().name());

            quantitySpinner("quantity-laptop").getValueFactory().setValue(3);
            assertEquals("4 item(s)", label("cart-count").getText());
            assertEquals("3,750.00 USD", label("usd-total").getText());

            button("remove-headphones").fire();
            assertEquals("3,600.00 USD", label("usd-total").getText());
            assertEquals(1, cartTable.getItems().size());

            button("clear-cart-button").fire();
            assertTrue(cartTable.getItems().isEmpty());
            assertEquals("0 item(s)", label("cart-count").getText());
            assertEquals("0.00 USD", label("usd-total").getText());
            assertTrue(button("checkout-button").isDisabled());
            return null;
        });
    }

    @Test
    void checkoutDownloadsPdfAndShowsConvertedReceipt() throws Exception {
        onJavaFxThread(() -> {
            button("add-laptop").fire();
            button("add-laptop").fire();
            button("add-headphones").fire();
            currencySelector().setValue("EUR");
            TableView<Product> products = table("product-table");
            products.scrollTo(products.getItems().size() - 1);
            return null;
        });
        // Exercise a newly added product after TableView recycles its off-screen cells.
        awaitJavaFx(() -> {
            Parent root = stage.getScene().getRoot();
            root.applyCss();
            root.layout();
            return root.lookup("#add-running-shoes") != null;
        });
        onJavaFxThread(() -> {
            button("add-running-shoes").fire();
            button("checkout-button").fire();
            assertTrue(this.<Product>table("product-table").isDisabled());
            return null;
        });
        awaitJavaFx(() -> dialog("checkout-receipt") != null);
        onJavaFxThread(() -> {
            Window dialog = dialog("checkout-receipt");
            TextArea text = (TextArea) dialog.getScene().lookup("#receipt-text");
            assertTrue(text.getText().contains("2 x Laptop"));
            assertTrue(text.getText().contains("1 x Headphones"));
            assertTrue(text.getText().contains("1 x Running Shoes"));
            assertTrue(text.getText().contains("Total (USD): 2,645.00 USD"));
            assertTrue(text.getText().contains("Total (EUR): 2,433.40 EUR"));
            assertTrue(((Label) dialog.getScene().lookup("#receipt-path")).getText()
                    .contains(receiptDirectory.toString()));
            assertNotNull(dialog.getScene().lookup("#open-receipt-link"));
            assertTrue(this.<CartItem>table("cart-table").getItems().isEmpty());
            assertTrue(button("checkout-button").isDisabled());
            dialog.hide();
            return null;
        });
        try (var receipts = Files.list(receiptDirectory)) {
            List<Path> files = receipts.toList();
            assertEquals(1, files.size());
            assertTrue(files.get(0).getFileName().toString().endsWith(".pdf"));
            assertTrue(Files.size(files.get(0)) > 0);
        }
    }

    @Test
    void downloadFailureKeepsTheCartAndAllowsRetry() throws Exception {
        exporter.set(receipt -> { throw new IOException("Receipt folder is unavailable."); });
        onJavaFxThread(() -> {
            button("add-headphones").fire();
            button("checkout-button").fire();
            return null;
        });
        awaitJavaFx(() -> dialog("store-error-dialog") != null);
        onJavaFxThread(() -> {
            assertEquals(1, this.<CartItem>table("cart-table").getItems().size());
            assertTrue(label("checkout-status").getText().contains("ready to retry"));
            dialog("store-error-dialog").hide();
            return null;
        });
        exporter.set(new PdfReceiptExporter(receiptDirectory));
        onJavaFxThread(() -> { button("checkout-button").fire(); return null; });
        awaitJavaFx(() -> dialog("checkout-receipt") != null);
        onJavaFxThread(() -> {
            assertTrue(this.<CartItem>table("cart-table").getItems().isEmpty());
            return null;
        });
    }

    private Node node(String id) {
        Parent root = stage.getScene().getRoot();
        root.applyCss();
        root.layout();
        Node node = root.lookup("#" + id);
        assertNotNull(node, "Missing JavaFX control: " + id);
        return node;
    }

    private Button button(String id) {
        return (Button) node(id);
    }

    private Label label(String id) {
        return (Label) node(id);
    }

    private Window dialog(String id) {
        return Window.getWindows().stream()
                .filter(window -> window != stage && window.isShowing()
                        && window.getScene().lookup("#" + id) != null)
                .findFirst().orElse(null);
    }

    private void awaitJavaFx(Callable<Boolean> condition) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
        while (!onJavaFxThread(condition)) {
            if (System.nanoTime() >= deadline) {
                fail("Timed out waiting for JavaFX operation.");
            }
            Thread.sleep(25);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> TableView<T> table(String id) {
        return (TableView<T>) node(id);
    }

    @SuppressWarnings("unchecked")
    private ComboBox<String> currencySelector() {
        return (ComboBox<String>) node("currency-selector");
    }

    @SuppressWarnings("unchecked")
    private Spinner<Integer> quantitySpinner(String id) {
        return (Spinner<Integer>) node(id);
    }

    private static <T> T onJavaFxThread(Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action);
        Platform.runLater(task);
        return task.get(30, TimeUnit.SECONDS);
    }
}
