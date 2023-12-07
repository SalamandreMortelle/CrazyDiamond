package CrazyDiamond.Controller;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Region;
import javafx.stage.Screen;

public class ResizeableCanvas extends Region {

    // Canvas rattaché à la scène qui affiche l'état actuel de l'environnement à l'utilisateur
    private final Canvas canvas_affichage;

    // Canvas dans lequel on trace "en surimpression" les contours clignotants des éléments sélectionnés (sources, obstacles ou SOCs)
    private final Canvas canvas_selection;

    protected GraphicsContext gc_affichage;
    protected GraphicsContext gc_selection;

    ResizeableCanvas(double width, double height) {
        //set the width and height of this and the canvas as the same
        setWidth(width);
        setHeight(height);
        canvas_affichage = new Canvas(width, height);
        canvas_selection = new Canvas(width, height);

        //add the canvas as a child
        getChildren().add(canvas_affichage);
        getChildren().add(canvas_selection);

        //bind the canvas width and height to the region
        canvas_affichage.widthProperty().bind(this.widthProperty());
        canvas_affichage.heightProperty().bind(this.heightProperty());
        canvas_selection.widthProperty().bind(this.widthProperty());
        canvas_selection.heightProperty().bind(this.heightProperty());

        gc_affichage = canvas_affichage.getGraphicsContext2D() ;
        gc_selection = canvas_selection.getGraphicsContext2D() ;

        setMaxHeight(Screen.getPrimary().getVisualBounds().getHeight());
        setPrefHeight(Screen.getPrimary().getVisualBounds().getHeight());

    }

}