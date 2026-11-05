package com.example.ecommerce.ui;

import com.example.currencyconverter.exception.InvalidCurrencyException;
import com.example.ecommerce.checkout.CheckoutReceipt;
import com.example.ecommerce.checkout.PurchaseResult;
import com.example.ecommerce.checkout.PurchaseService;
import com.example.ecommerce.model.CartItem;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.model.ProductCatalogue;
import com.example.ecommerce.model.ShoppingCart;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Spinner;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * Programmatically wired JavaFX view. Observable cart properties keep the tables and totals current.
 */
public final class StoreView extends BorderPane {

    private final ShoppingCart cart = new ShoppingCart();
    private final PurchaseService purchases;
    private final Executor executor;
    private final Consumer<Path> openReceipt;
    private final BooleanProperty checkoutBusy = new SimpleBooleanProperty(false);
    private final Label checkoutStatus = new Label("Your PDF receipt downloads automatically after checkout.");
    private final ComboBox<String> currencySelector = new ComboBox<>(
            FXCollections.observableArrayList("USD", "EUR", "GBP", "KES"));

    public StoreView(PurchaseService purchases, Executor executor, Consumer<Path> openReceipt) {
        this.purchases = Objects.requireNonNull(purchases);
        this.executor = Objects.requireNonNull(executor);
        this.openReceipt = Objects.requireNonNull(openReceipt);
        currencySelector.setId("currency-selector");
        currencySelector.setValue("USD");
        currencySelector.setPrefWidth(110);

        setPadding(new Insets(20));
        getStyleClass().add("store-root");
        setTop(createHeader());

        SplitPane content = new SplitPane(createProductPanel(), createCartPanel());
        content.getStyleClass().add("store-split");
        content.setDividerPositions(0.45);
        content.disableProperty().bind(checkoutBusy);
        setCenter(content);

        Label footer = new Label("Prices are listed in USD. Local-currency conversion is applied at checkout.");
        footer.getStyleClass().add("muted-text");
        checkoutStatus.setId("checkout-status");
        checkoutStatus.setWrapText(true);
        checkoutStatus.getStyleClass().add("muted-text");
        ProgressIndicator progress = new ProgressIndicator();
        progress.setMaxSize(20, 20);
        progress.visibleProperty().bind(checkoutBusy);
        progress.managedProperty().bind(checkoutBusy);
        HBox status = new HBox(10, progress, checkoutStatus);
        status.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(checkoutStatus, Priority.ALWAYS);
        VBox footerBox = new VBox(6, footer, status);
        BorderPane.setMargin(footerBox, new Insets(14, 0, 0, 0));
        setBottom(footerBox);
    }

    private VBox createHeader() {
        Label subtitle = new Label("Shop internationally. Check out in your local currency.");
        subtitle.getStyleClass().add("store-subtitle");
        VBox header = new VBox(7, new BrandLogo(), subtitle);
        header.getStyleClass().add("store-header");
        BorderPane.setMargin(header, new Insets(0, 0, 18, 0));
        return header;
    }

    private VBox createProductPanel() {
        TableView<Product> table = new TableView<>(FXCollections.observableArrayList(ProductCatalogue.products()));
        table.setId("product-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setFixedCellSize(48);

        TableColumn<Product, String> name = new TableColumn<>("Product");
        name.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().name()));
        name.setMinWidth(150);

        TableColumn<Product, BigDecimal> price = new TableColumn<>("Price (USD)");
        price.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().priceUsd()));
        price.setCellFactory(column -> new UsdPriceCell<>());
        price.setMinWidth(110);

        TableColumn<Product, Product> action = new TableColumn<>("Action");
        action.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue()));
        action.setCellFactory(column -> new AddToCartCell());
        action.setMinWidth(125);
        action.setSortable(false);
        table.getColumns().setAll(List.of(name, price, action));

        Label hint = new Label(ProductCatalogue.products().size()
                + " products available. Scroll to browse, and click Add to Cart again to add another unit.");
        hint.setWrapText(true);
        hint.getStyleClass().add("muted-text");
        VBox panel = panel("Product Catalogue", hint, table);
        panel.setMinWidth(430);
        VBox.setVgrow(table, Priority.ALWAYS);
        return panel;
    }

    private VBox createCartPanel() {
        // Sorting is a view transformation; the cart's protected list is never reordered directly.
        SortedList<CartItem> sortedItems = new SortedList<>(cart.getItems());
        TableView<CartItem> table = new TableView<>(sortedItems);
        sortedItems.comparatorProperty().bind(table.comparatorProperty());
        table.setId("cart-table");
        table.setPlaceholder(new Label("Your cart is empty. Add a product to get started."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setFixedCellSize(48);

        TableColumn<CartItem, String> name = new TableColumn<>("Product");
        name.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getProduct().name()));
        name.setMinWidth(140);

        TableColumn<CartItem, Integer> quantity = new TableColumn<>("Quantity");
        quantity.setCellValueFactory(cell -> cell.getValue().quantityProperty().asObject());
        quantity.setCellFactory(column -> new QuantityCell());
        quantity.setMinWidth(95);
        quantity.setMaxWidth(100);
        quantity.setSortable(false);

        TableColumn<CartItem, BigDecimal> lineTotal = new TableColumn<>("Line total (USD)");
        lineTotal.setCellValueFactory(cell -> cell.getValue().lineTotalUsdProperty());
        lineTotal.setCellFactory(column -> new UsdPriceCell<>());
        lineTotal.setMinWidth(125);

        TableColumn<CartItem, CartItem> remove = new TableColumn<>("Action");
        remove.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue()));
        remove.setCellFactory(column -> new RemoveFromCartCell());
        remove.setMinWidth(85);
        remove.setMaxWidth(95);
        remove.setSortable(false);
        table.getColumns().setAll(List.of(name, quantity, lineTotal, remove));

        Label count = new Label();
        count.setId("cart-count");
        count.textProperty().bind(Bindings.format("%d item(s)", cart.itemCountProperty()));
        count.getStyleClass().add("muted-text");

        Label total = new Label();
        total.setId("usd-total");
        total.textProperty().bind(Bindings.createStringBinding(
                () -> formatMoney(cart.getTotalUsd()) + " USD", cart.totalUsdProperty()));
        total.getStyleClass().add("cart-total");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox summary = new HBox(10, new Label("Cart total"), spacer, total);
        summary.setAlignment(Pos.CENTER_LEFT);

        HBox currencyRow = new HBox(12, new Label("Local currency"), currencySelector);
        currencyRow.setAlignment(Pos.CENTER_LEFT);

        Button checkout = new Button("Checkout");
        checkout.setId("checkout-button");
        checkout.getStyleClass().add("primary-button");
        checkout.setPrefWidth(145);
        checkout.disableProperty().bind(Bindings.isEmpty(cart.getItems())
                .or(currencySelector.valueProperty().isNull()).or(checkoutBusy));
        checkout.setOnAction(event -> checkout());

        Button clear = new Button("Clear Cart");
        clear.setId("clear-cart-button");
        clear.disableProperty().bind(Bindings.isEmpty(cart.getItems()).or(checkoutBusy));
        clear.setOnAction(event -> cart.clear());

        HBox actions = new HBox(12, checkout, clear);
        Label hint = new Label("Use the quantity arrows to choose 1–99 units per product.");
        hint.setWrapText(true);
        hint.getStyleClass().add("muted-text");

        VBox panel = panel("Shopping Cart", count, table, hint, summary, currencyRow, actions);
        panel.setMinWidth(530);
        VBox.setVgrow(table, Priority.ALWAYS);
        return panel;
    }

    private VBox panel(String heading, Node... children) {
        Label title = new Label(heading);
        title.getStyleClass().add("section-title");
        VBox panel = new VBox(14);
        panel.getStyleClass().add("store-panel");
        panel.getChildren().add(title);
        panel.getChildren().addAll(children);
        return panel;
    }

    private void checkout() {
        if (checkoutBusy.get()) {
            return;
        }
        CheckoutReceipt receipt;
        try {
            receipt = purchases.prepareReceipt(cart, currencySelector.getValue());
        } catch (InvalidCurrencyException exception) {
            showError("Currency conversion error", exception.getMessage());
            return;
        } catch (IllegalArgumentException | IllegalStateException exception) {
            showError("Checkout could not be completed", exception.getMessage());
            return;
        }

        checkoutBusy.set(true);
        checkoutStatus.setText("Downloading your PDF receipt…");
        Task<PurchaseResult> task = new Task<>() {
            @Override
            protected PurchaseResult call() throws Exception {
                return purchases.completeCheckout(receipt);
            }
        };
        task.setOnSucceeded(event -> {
            PurchaseResult result = task.getValue();
            cart.clear();
            checkoutBusy.set(false);
            checkoutStatus.setText("Receipt downloaded: " + result.receiptPath().getFileName());
            showReceipt(result);
        });
        task.setOnFailed(event -> {
            checkoutBusy.set(false);
            checkoutStatus.setText("Receipt download failed. Your cart is ready to retry.");
            showError("Unable to download receipt",
                    "Check that your receipt folder is writable. Your cart has been kept so you can retry.\n"
                            + Objects.toString(task.getException().getMessage(), ""));
        });
        executor.execute(task);
    }

    private void showReceipt(PurchaseResult result) {
        TextArea text = new TextArea(result.receipt().toDisplayText());
        text.setId("receipt-text");
        text.setEditable(false);
        text.setWrapText(true);
        text.setPrefRowCount(12);
        text.getStyleClass().add("receipt-text");

        Label path = new Label("Downloaded automatically to:\n" + result.receiptPath());
        path.setId("receipt-path");
        path.setWrapText(true);
        Hyperlink open = new Hyperlink("Open downloaded PDF receipt");
        open.setId("open-receipt-link");
        open.setOnAction(event -> openReceipt.accept(result.receiptPath()));
        VBox details = new VBox(12, text, path, open);
        VBox.setVgrow(text, Priority.ALWAYS);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("GlobalMart Receipt");
        alert.setHeaderText("Purchase complete — your PDF receipt has been downloaded.");
        alert.initOwner(getScene().getWindow());
        alert.initModality(Modality.WINDOW_MODAL);
        alert.setResizable(true);
        alert.getDialogPane().setId("checkout-receipt");
        alert.getDialogPane().getStylesheets().addAll(getScene().getStylesheets());
        alert.getDialogPane().setContent(details);
        alert.getDialogPane().setPrefWidth(740);
        alert.show();
    }

    private void showError(String heading, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("GlobalMart");
        alert.setHeaderText(heading);
        alert.setContentText(message);
        alert.getDialogPane().setId("store-error-dialog");
        alert.initOwner(getScene().getWindow());
        alert.show();
    }

    private static String formatMoney(BigDecimal amount) {
        return String.format(Locale.ROOT, "%,.2f", amount);
    }

    private static final class UsdPriceCell<T> extends TableCell<T, BigDecimal> {

        private UsdPriceCell() {
            setAlignment(Pos.CENTER_RIGHT);
        }

        @Override
        protected void updateItem(BigDecimal value, boolean empty) {
            super.updateItem(value, empty);
            setText(empty || value == null ? null : "$" + formatMoney(value));
        }
    }

    private final class AddToCartCell extends TableCell<Product, Product> {

        private final Button button = new Button("Add to Cart");

        private AddToCartCell() {
            button.getStyleClass().add("primary-button");
            button.setOnAction(event -> {
                Product product = getItem();
                if (product != null) {
                    try {
                        cart.add(product);
                    } catch (IllegalArgumentException exception) {
                        showError("Cart update", exception.getMessage());
                    }
                }
            });
        }

        @Override
        protected void updateItem(Product product, boolean empty) {
            super.updateItem(product, empty);
            setText(null);
            button.setId(empty || product == null ? null : "add-" + product.id());
            setGraphic(empty || product == null ? null : button);
        }
    }

    private final class QuantityCell extends TableCell<CartItem, Integer> {

        private final Spinner<Integer> spinner = new Spinner<>(1, ShoppingCart.MAX_QUANTITY, 1);
        private boolean updating;

        private QuantityCell() {
            spinner.setEditable(false);
            spinner.setPrefWidth(82);
            spinner.valueProperty().addListener((observable, oldValue, newValue) -> {
                CartItem item = getTableRow() == null ? null : getTableRow().getItem();
                if (!updating && item != null && newValue != null) {
                    cart.updateQuantity(item, newValue);
                }
            });
        }

        @Override
        protected void updateItem(Integer quantity, boolean empty) {
            super.updateItem(quantity, empty);
            setText(null);
            if (empty || quantity == null) {
                spinner.setId(null);
                setGraphic(null);
                return;
            }

            // Recycled TableView cells must synchronize without writing back to the cart.
            updating = true;
            try {
                spinner.getValueFactory().setValue(quantity);
            } finally {
                updating = false;
            }

            CartItem item = getTableRow() == null ? null : getTableRow().getItem();
            if (item != null) {
                spinner.setId("quantity-" + item.getProduct().id());
                spinner.setAccessibleText("Quantity for " + item.getProduct().name());
            }
            setGraphic(spinner);
        }
    }

    private final class RemoveFromCartCell extends TableCell<CartItem, CartItem> {

        private final Button button = new Button("Remove");

        private RemoveFromCartCell() {
            button.getStyleClass().add("remove-button");
            button.setOnAction(event -> cart.remove(getItem()));
        }

        @Override
        protected void updateItem(CartItem item, boolean empty) {
            super.updateItem(item, empty);
            setText(null);
            button.setId(empty || item == null ? null : "remove-" + item.getProduct().id());
            setGraphic(empty || item == null ? null : button);
        }
    }
}
