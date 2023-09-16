package CrazyDiamond.Controller;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Region;
import javafx.stage.Screen;

public class ResizeableCanvas extends Region {

    private final Canvas canvas;

    protected final GraphicsContext gc ;

    ResizeableCanvas(double width, double height) {
        //set the width and height of this and the canvas as the same
        setWidth(width);
        setHeight(height);
        canvas = new Canvas(width, height);

        //add the canvas as a child
        getChildren().add(canvas);

        //bind the canvas width and height to the region
        canvas.widthProperty().bind(this.widthProperty());
        canvas.heightProperty().bind(this.heightProperty());

        gc = canvas.getGraphicsContext2D() ;

        setMaxHeight(Screen.getPrimary().getVisualBounds().getHeight());
        setPrefHeight(Screen.getPrimary().getVisualBounds().getHeight());

    }

}