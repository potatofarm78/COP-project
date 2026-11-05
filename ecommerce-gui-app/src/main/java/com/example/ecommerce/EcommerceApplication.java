package com.example.ecommerce;

import com.example.currencyconverter.CurrencyConverterImpl;
import com.example.ecommerce.checkout.PurchaseService;
import com.example.ecommerce.receipt.PdfReceiptExporter;
import com.example.ecommerce.ui.BrandLogo;
import com.example.ecommerce.ui.StoreView;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * JavaFX entry point for GlobalMart, with direct shopping and background receipt downloads.
 */
public final class EcommerceApplication extends Application {

    public static final String WINDOW_TITLE = "GlobalMart";

    private final PurchaseService purchases;
    private final ExecutorService worker = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "globalmart-worker");
        thread.setDaemon(true);
        return thread;
    });

    public EcommerceApplication() {
        this(new PurchaseService(new CurrencyConverterImpl(),
                new PdfReceiptExporter(configuredDirectory("GLOBALMART_RECEIPT_DIR",
                        Path.of(System.getProperty("user.home"), "Downloads", "GlobalMart")))));
    }

    /** Dependency injection allows GUI tests to use an isolated receipt directory. */
    public EcommerceApplication(PurchaseService purchases) {
        this.purchases = Objects.requireNonNull(purchases);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle(WINDOW_TITLE);
        StoreView store = new StoreView(purchases, worker,
                path -> getHostServices().showDocument(path.toUri().toString()));
        installScene(stage, store, 1180, 840, 1050, 760);

        StackPane icon = BrandLogo.createGlobe(64);
        icon.resize(64, 64);
        icon.layout();
        SnapshotParameters snapshot = new SnapshotParameters();
        snapshot.setFill(Color.TRANSPARENT);
        stage.getIcons().add(icon.snapshot(snapshot, new WritableImage(64, 64)));
        stage.show();
    }

    private void installScene(Stage stage, Parent root, double width, double height,
                              double minimumWidth, double minimumHeight) {
        Rectangle2D screen = Screen.getPrimary().getVisualBounds();
        // Bound the initial client area before showing the window, leaving room for its frame.
        double sceneWidth = Math.min(width, Math.max(1, screen.getWidth() - 32));
        double sceneHeight = Math.min(height, Math.max(1, screen.getHeight() - 64));
        Scene scene = new Scene(root, sceneWidth, sceneHeight);
        scene.getStylesheets().add(Objects.requireNonNull(
                EcommerceApplication.class.getResource("/styles/store.css"),
                "Store stylesheet is missing.").toExternalForm());
        stage.setMinWidth(Math.min(minimumWidth, sceneWidth));
        stage.setMinHeight(Math.min(minimumHeight, sceneHeight));
        stage.setScene(scene);
        stage.sizeToScene();
    }

    private static Path configuredDirectory(String environmentKey, Path fallback) {
        String configured = System.getenv(environmentKey);
        return configured == null || configured.isBlank() ? fallback : Path.of(configured);
    }

    @Override
    public void stop() {
        worker.shutdownNow();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
