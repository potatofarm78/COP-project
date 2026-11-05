package com.example.ecommerce.model;

import java.math.BigDecimal;
import java.util.List;

/** Immutable store catalogue. Add or edit products here to update the shop's inventory. */
public final class ProductCatalogue {

    private static final List<Product> PRODUCTS = List.of(
            new Product("laptop", "Laptop", new BigDecimal("1200.00")),
            new Product("headphones", "Headphones", new BigDecimal("150.00")),
            new Product("smartwatch", "Smartwatch", new BigDecimal("200.00")),
            new Product("smartphone", "Smartphone", new BigDecimal("800.00")),
            new Product("tablet", "Tablet", new BigDecimal("450.00")),
            new Product("mouse", "Wireless Mouse", new BigDecimal("35.00")),
            new Product("console", "Gaming Console", new BigDecimal("499.00")),
            new Product("camera", "Digital Camera", new BigDecimal("649.00")),
            new Product("speaker", "Bluetooth Speaker", new BigDecimal("79.00")),
            new Product("monitor", "27-inch Monitor", new BigDecimal("249.00")),
            new Product("keyboard", "Mechanical Keyboard", new BigDecimal("89.00")),
            new Product("dock", "USB-C Dock", new BigDecimal("69.00")),
            new Product("ssd", "Portable SSD", new BigDecimal("129.00")),
            new Product("power-bank", "Power Bank", new BigDecimal("45.00")),
            new Product("router", "Wi-Fi Router", new BigDecimal("99.00")),
            new Product("desk-lamp", "Desk Lamp", new BigDecimal("39.00")),
            new Product("kettle", "Electric Kettle", new BigDecimal("35.00")),
            new Product("coffee-maker", "Coffee Maker", new BigDecimal("85.00")),
            new Product("blender", "Blender", new BigDecimal("65.00")),
            new Product("air-fryer", "Air Fryer", new BigDecimal("119.00")),
            new Product("backpack", "Travel Backpack", new BigDecimal("49.00")),
            new Product("water-bottle", "Water Bottle", new BigDecimal("18.00")),
            new Product("yoga-mat", "Yoga Mat", new BigDecimal("29.00")),
            new Product("running-shoes", "Running Shoes", new BigDecimal("95.00"))
    );

    private ProductCatalogue() {
    }

    public static List<Product> products() {
        return PRODUCTS;
    }
}
