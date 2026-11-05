package com.example.ecommerce.ui;

import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;

/** Scalable globe-and-wordmark logo, drawn as vectors so it works without emoji fonts. */
public final class BrandLogo extends HBox {

    public BrandLogo() {
        setId("globalmart-logo");
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(12);

        StackPane globe = createGlobe(42);
        globe.setId("globe-icon");
        globe.setAccessibleText("GlobalMart globe logo");
        Label name = new Label("GlobalMart");
        name.setId("brand-name");
        name.getStyleClass().add("store-title");
        getChildren().addAll(globe, name);
    }

    public static StackPane createGlobe(double size) {
        Circle outline = new Circle(18, 18, 15);
        Ellipse meridian = new Ellipse(18, 18, 6, 15);
        Line equator = new Line(3, 18, 33, 18);
        for (Shape shape : new Shape[]{outline, meridian, equator}) {
            shape.setFill(Color.TRANSPARENT);
            shape.setStroke(Color.web("#8be1d7"));
            shape.setStrokeWidth(1.8);
        }

        Group drawing = new Group(outline, meridian, equator);
        drawing.setScaleX(size / 36.0);
        drawing.setScaleY(size / 36.0);
        StackPane icon = new StackPane(drawing);
        icon.setMinSize(size, size);
        icon.setPrefSize(size, size);
        icon.setMaxSize(size, size);
        return icon;
    }
}
