# 🌐 GlobalMart — JavaFX Store and Reusable Currency Converter

A **Java 17+ Maven multi-module project** for a university component-based
software development assignment. A JavaFX desktop store uses a separate,
reusable currency conversion JAR to produce multi-currency checkout receipts.

GlobalMart opens directly to a store with a globe-and-wordmark logo, a catalogue
of **24 products**, an interactive cart, multi-currency checkout, and automatic
PDF receipt downloads.

## Group Members

Replace these placeholders with the five group members' names:

1. [Member 1 Name]
2. [Member 2 Name]
3. [Member 3 Name]
4. [Member 4 Name]
5. [Member 5 Name]

## Project Structure

```text
COP-project/
├── pom.xml                                 # Parent POM and shared Java 17 configuration
├── README.md
├── .gitignore
├── currency-converter-component/
│   ├── pom.xml                             # Standalone reusable JAR
│   └── src/
│       ├── main/java/com/example/currencyconverter/
│       │   ├── CurrencyConverter.java
│       │   ├── CurrencyConverterImpl.java
│       │   └── exception/InvalidCurrencyException.java
│       └── test/java/com/example/currencyconverter/
│           └── CurrencyConverterImplTest.java
└── ecommerce-gui-app/
    ├── pom.xml                             # Component dependency and JavaFX configuration
    └── src/
        ├── main/
        │   ├── java/com/example/ecommerce/
        │   │   ├── EcommerceApplication.java
        │   │   ├── ui/
        │   │   │   ├── BrandLogo.java
        │   │   │   └── StoreView.java
        │   │   ├── model/
        │   │   │   ├── Product.java
        │   │   │   ├── ProductCatalogue.java
        │   │   │   ├── CartItem.java
        │   │   │   └── ShoppingCart.java
        │   │   ├── checkout/
        │   │   │   ├── CheckoutService.java
        │   │   │   ├── CheckoutReceipt.java
        │   │   │   ├── PurchaseService.java
        │   │   │   └── PurchaseResult.java
        │   │   └── receipt/
        │   │       ├── ReceiptExporter.java
        │   │       └── PdfReceiptExporter.java
        │   └── resources/styles/store.css
        └── test/java/com/example/ecommerce/
            ├── model/ShoppingCartTest.java
            ├── checkout/CheckoutServiceTest.java
            ├── checkout/PurchaseServiceTest.java
            ├── receipt/PdfReceiptExporterTest.java
            └── ui/StoreViewTest.java
```

The parent uses `pom` packaging and aggregates exactly two modules. Both child
modules use `jar` packaging. The component has no external runtime dependencies
and no dependency on the GUI module or JavaFX.

## Requirements and Setup

| Requirement | Details |
| --- | --- |
| JDK | Java Development Kit **17 or newer** |
| Maven | Apache Maven **3.8 or newer** |
| Desktop | A graphical Windows, macOS, or Linux desktop for opening the application |
| Network | Internet access on the first build to download dependencies and plugins |
| PDF viewer | A default PDF viewer for the receipt dialog's **Open downloaded PDF receipt** link |

Install a JDK and Maven, and make their commands available on your `PATH`.
Verify them in a terminal:

```bash
java -version
javac -version
mvn -version
```

`mvn -version` should report a Java version of at least 17. If it uses a different
JDK from the one you installed, set `JAVA_HOME` to your JDK installation and
update `PATH` accordingly.

JavaFX **17.0.12** is declared in the parent POM. The GUI module depends on
`javafx-controls`, which also supplies `javafx-graphics` and `javafx-base`.
Maven resolves the platform-specific JavaFX libraries for the computer performing
the build. A separate JavaFX SDK download is unnecessary.

Apache PDFBox **3.0.4** generates the receipts and is downloaded by Maven.

On Linux, JavaFX needs the GTK 3 desktop libraries. For example, on a compatible
Ubuntu/Debian installation:

```bash
sudo apt update
sudo apt install libgtk-3-0
```

Use a normal desktop session to launch the store. Automated GUI tests run
headlessly through a test-only Monocle dependency and do not require a display.

## Compile and Run the JavaFX Application

Open a terminal **in the project root**, where the parent `pom.xml` is located.
These Maven commands work on Windows, macOS, and Linux.

### 1. Build the complete project

```bash
mvn clean install
```

This command:

1. Builds the parent project and reusable component before the GUI module.
2. Compiles Java source code targeting Java 17.
3. Runs the converter, cart, checkout, PDF, and headless JavaFX tests.
4. Packages the component and application JARs.
5. Installs the parent and both modules into the local Maven repository.

Installing the component first allows the application-only launch command below
to resolve its dependency.

### 2. Open the store window

```bash
mvn -pl ecommerce-gui-app javafx:run
```

The configured JavaFX Maven plugin starts
`com.example.ecommerce.EcommerceApplication`, supplies the JavaFX module path,
and opens the window titled **GlobalMart**, with a globe logo before the name.
The product catalogue and shopping cart are available immediately.

Keep the terminal open while using the store. Close the application window to
finish the run. Use the same launch command whenever you want to open it again.

After changing source files, rebuild with `mvn clean install` before launching.

### Generated artifacts

```text
currency-converter-component/target/currency-converter-component-1.0.0.jar
ecommerce-gui-app/target/ecommerce-gui-app-1.0.0.jar
```

The JavaFX Maven launch goal supplies the GUI application's required libraries
at runtime. The reusable component JAR can be consumed independently by another
Java application.

## How to Use the Store

1. **Browse products.** The left-hand **Product Catalogue** table lists 24
   products and their prices in USD. Scroll through the list to see the full range
   of electronics, home items, accessories, and fitness products.
2. **Add a product.** Click its **Add to Cart** button. The product appears in
   the right-hand **Shopping Cart** table with quantity `1`.
3. **Add more units.** Click the same product's **Add to Cart** button again,
   or use its quantity spinner's up/down arrows. Quantities range from **1 to 99**.
   Repeated additions use one cart row per product.
4. **Review the cart.** Item counts, line totals, and the USD cart total update
   immediately. Product and line-total headers can be used to sort the cart.
5. **Remove items.** Click a row's **Remove** button to remove that product, or
   click **Clear Cart** to remove all products.
6. **Choose your local currency.** Use the **Local currency** dropdown to select
   **USD**, **EUR**, **GBP**, or **KES**. The default is USD.
7. **Check out.** Click **Checkout**. The application converts the USD total and
   automatically downloads a PDF receipt. A progress indicator is displayed
   while the receipt is saved.
8. **View the downloaded receipt.** The receipt dialog shows the items, quantities,
   receipt ID, date, both totals, and PDF file location. Use **Open downloaded PDF
   receipt** to open it in your default PDF viewer.
9. **Continue shopping.** Successful checkout clears the purchased cart. Close
   the receipt and add products for your next purchase.

**Checkout** and **Clear Cart** are disabled while the cart is empty. Any cart
validation or currency conversion error is displayed in a JavaFX error dialog.
Products and cart data are held in memory for the current application session.
Downloaded receipts persist between application runs. During checkout, the cart
controls are disabled to prevent duplicate purchases or changes to the purchased items.

## Automatic PDF Receipt Downloads

Every successful checkout saves a unique, branded PDF automatically to:

```text
<your user home>/Downloads/GlobalMart/GlobalMart-GM-<unique-id>.pdf
```

The folder is created automatically. Each PDF contains the purchased products,
quantities, USD unit and line prices, USD grand total, local-currency total,
receipt ID, and purchase time in UTC.

PDFs embed a bundled font and can span multiple pages. Existing receipts are
never overwritten. If the download fails, the GUI keeps your cart for retry.

You can customize the download directory by setting this environment variable
before launching Maven:

| Variable | Purpose | Default |
| --- | --- | --- |
| `GLOBALMART_RECEIPT_DIR` | Directory receiving PDF downloads | `<user home>/Downloads/GlobalMart` |

## Product Catalogue

The store includes these 24 hardcoded products:

| Product | Unit price (USD) |
| --- | ---: |
| Laptop | $1,200.00 |
| Headphones | $150.00 |
| Smartwatch | $200.00 |
| Smartphone | $800.00 |
| Tablet | $450.00 |
| Wireless Mouse | $35.00 |
| Gaming Console | $499.00 |
| Digital Camera | $649.00 |
| Bluetooth Speaker | $79.00 |
| 27-inch Monitor | $249.00 |
| Mechanical Keyboard | $89.00 |
| USB-C Dock | $69.00 |
| Portable SSD | $129.00 |
| Power Bank | $45.00 |
| Wi-Fi Router | $99.00 |
| Desk Lamp | $39.00 |
| Electric Kettle | $35.00 |
| Coffee Maker | $85.00 |
| Blender | $65.00 |
| Air Fryer | $119.00 |
| Travel Backpack | $49.00 |
| Water Bottle | $18.00 |
| Yoga Mat | $29.00 |
| Running Shoes | $95.00 |

Products are defined in
`ecommerce-gui-app/src/main/java/com/example/ecommerce/model/ProductCatalogue.java`.
Edit that list to add products or change prices, then rebuild the project.

### Example purchase

Add two laptops and one pair of headphones, select **EUR**, and click **Checkout**.
The receipt contains:

```text
GLOBALMART RECEIPT
Receipt: GM-<unique-id>
Purchased: yyyy-MM-dd HH:mm:ss UTC

2 x Laptop @ 1,200.00 USD = 2,400.00 USD
1 x Headphones @ 150.00 USD = 150.00 USD

Total (USD): 2,550.00 USD
Total (EUR): 2,346.00 EUR
```

## Logic Used

### 1. How the GUI is wired

`EcommerceApplication` extends JavaFX `Application`. Its `start(Stage)` method
shows `StoreView`, loads `store.css`, and installs the globe window icon.
`BrandLogo` draws a scalable globe beside the GlobalMart wordmark using JavaFX
shapes. `ProductCatalogue` supplies the immutable catalogue of 24 products.

`StoreView` builds the interface using JavaFX controls and layouts directly in
Java. A `SplitPane` contains the product and cart sections. Each section uses a
`TableView`; custom `TableCell` implementations provide **Add to Cart**, quantity
spinner, and **Remove** controls. A `ComboBox<String>` provides the currency
selection, and button action handlers connect user events to the cart and purchase
services. UI updates occur on the JavaFX thread. PDF writing runs in a background
`Task` so the interface stays responsive during checkout.

The cart exposes an observable list and read-only JavaFX properties. Table
columns observe quantity and line-total properties, and labels bind to the total
and item count. A list extractor observes changes to existing quantities, so
the total is recomputed after additions, quantity changes, removals, and clears.
A `SortedList` lets the table sort its display without modifying the protected
cart collection.

### 2. How the shopping cart works

- `Product` is an immutable Java record with an ID, name, and USD price.
- `CartItem` represents one product and its quantity.
- `ShoppingCart.add(product)` creates a row for a new product or increments an
  existing row identified by the same product ID.
- `ShoppingCart.updateQuantity(...)` validates the **1–99** quantity range.
- A line total is `unitPriceUsd × quantity`.
- The grand total is the sum of all line totals; the item count is the sum of
  all quantities.
- Removing a row or clearing the cart recomputes the observable totals.

USD prices and cart calculations use **`BigDecimal`** to keep decimal amounts
exact. Checkout converts the already-computed grand total to `double` only when
calling the component's required API. The converted receipt total is rounded
to two decimal places using `RoundingMode.HALF_UP`.

`CheckoutReceipt` captures an immutable copy of the purchased lines and totals.
It also captures the purchase time and unique receipt number, remaining
valid after the GUI clears the original cart. `PurchaseService` saves the PDF
and returns a `PurchaseResult` with the receipt and download path. Checkout
completes after a successful PDF save. `PdfReceiptExporter` uses PDFBox for branded,
paginated documents with an embedded font.

### 3. How the reusable component is decoupled

The component defines this public contract:

```java
public double convert(double amount, String fromCurrency, String toCurrency);
```

`CurrencyConverterImpl` implements `CurrencyConverter` and owns the exchange-rate
map, currency validation, and conversion mathematics. It contains no application
or JavaFX code and can be reused wherever its JAR is available.

The GUI module declares a Maven dependency on `currency-converter-component`.
`EcommerceApplication` chooses the concrete implementation at startup and passes
it into `PurchaseService`. `CheckoutService` receives it through the
**`CurrencyConverter` interface**, so checkout does not read or duplicate the
exchange-rate map.

The dependency direction is:

```text
JavaFX StoreView → PurchaseService → CheckoutService → CurrencyConverter → CurrencyConverterImpl
                              └── ReceiptExporter → downloaded PDF
```

A different converter implementation can be provided at startup while preserving
the GUI and cart workflow. The component can also be used by a different Java
application without bringing in this store's classes.

### 4. Currency conversion mathematics

The component uses an immutable hardcoded map. Every rate is expressed as
**currency units per 1 USD**:

| Code | Currency | Rate per 1 USD |
| --- | --- | ---: |
| USD | United States dollar | 1.00 |
| EUR | Euro | 0.92 |
| GBP | British pound sterling | 0.78 |
| KES | Kenyan shilling | 129.00 |

These are illustrative fixed rates. Currency codes are case-insensitive, and
surrounding whitespace is ignored.

Conversion through the common USD base can be written as:

```text
amountInUSD = amount / rate[fromCurrency]
convertedAmount = amountInUSD × rate[toCurrency]
```

The implementation uses the equivalent cross-rate formula:

```text
convertedAmount = amount × (rate[toCurrency] / rate[fromCurrency])
```

For the example cart, `2550 USD × (0.92 / 1.00) = 2346 EUR`.
The component also supports non-USD source currencies; for example,
`92 EUR × (0.78 / 0.92) = 78 GBP`.

The reusable component returns an unrounded `double`. The application's receipt
layer performs the two-decimal rounding needed for presentation.

### 5. Validation and custom exceptions

- `InvalidCurrencyException` extends `IllegalArgumentException` and is thrown
  for null, blank, or unsupported currency codes.
- The converter accepts zero and positive finite amounts. Negative amounts,
  `NaN`, infinity, and conversion overflow cause an `IllegalArgumentException`.
- The cart rejects quantities outside its permitted range.
- The checkout service rejects empty carts, including callers outside the GUI.
- The GUI catches expected validation errors and presents them in `Alert` dialogs.

## Package the Reusable Component into a JAR

From the project root, build only the component and its parent:

```bash
mvn -pl currency-converter-component -am clean package
```

The standalone library is created here:

```text
currency-converter-component/target/currency-converter-component-1.0.0.jar
```

It contains `CurrencyConverter`, `CurrencyConverterImpl`, and
`InvalidCurrencyException`. JavaFX, GUI classes, test classes, and JUnit are
excluded from the component JAR.

### Reuse the component in another Maven project

Install the parent and component locally:

```bash
mvn -pl currency-converter-component -am clean install
```

Add this dependency to the other project's `pom.xml`:

```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>currency-converter-component</artifactId>
    <version>1.0.0</version>
</dependency>
```

Use the API from your Java code:

```java
import com.example.currencyconverter.CurrencyConverter;
import com.example.currencyconverter.CurrencyConverterImpl;

CurrencyConverter converter = new CurrencyConverterImpl();
double totalInEuros = converter.convert(2550.0, "USD", "EUR"); // 2346.0
```

## Run the Automated Tests

Run all tests from the project root:

```bash
mvn test
```

The tests verify:

- Direct, reverse, and cross-currency conversions and invalid input handling.
- Cart quantity changes, repeated additions, removal, clearing, quantity limits,
  and exact decimal arithmetic.
- Checkout conversion through the interface, receipt rounding, and immutable
  receipt snapshots.
- Readable PDF content, unique files, no receipt overwrites, and multi-page receipts.
- Actual JavaFX window creation, product buttons, quantity spinners, sorting,
  live totals, scrolling to newly added catalogue products, receipt download,
  checkout retry, and cart reset.

GUI tests use **Monocle** in the Maven test process. Its dependency and headless
properties have test scope only; `javafx:run` opens the normal graphical desktop app.
Tests use isolated temporary receipt directories under the build output.

## Troubleshooting

| Problem | Resolution |
| --- | --- |
| `mvn` or `java` is not recognized | Install Maven/JDK and check `PATH` and `JAVA_HOME`. |
| Maven reports a Java version below 17 | Point `JAVA_HOME` to a JDK 17+ installation, then recheck `mvn -version`. |
| The GUI module cannot resolve the component | Run `mvn clean install` from the root before `mvn -pl ecommerce-gui-app javafx:run`. |
| JavaFX runtime components or classes are missing | Launch with the documented `javafx:run` goal so Maven supplies the JavaFX module path and dependencies. |
| Linux reports GTK or native toolkit errors | Install GTK 3 and use a JDK matching your platform architecture. |
| `Unable to open DISPLAY` on Linux | Start the GUI from a graphical desktop session. `mvn test` can still run headlessly. |
| A first build fails while downloading dependencies | Check the network or Maven proxy settings, then rerun the build. |
| A PDF download fails | Ensure the receipt directory is writable, or set `GLOBALMART_RECEIPT_DIR` to another directory and restart. Your cart is retained for retry. |
| The PDF-open link does not open a viewer | Install or configure your operating system's default PDF application, or open the displayed file directly. |

To remove generated build artifacts from both modules:

```bash
mvn clean
```
