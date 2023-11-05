package CrazyDiamond.Model;

import javafx.geometry.Point2D;

public record PositionEtOrientation(Point2D position, double orientation_deg) {

    /**
     * @return le vecteur unitaire directeur de l'orientation
     */
    Point2D direction() { return new Point2D(Math.cos(Math.toRadians(orientation_deg)),Math.sin(Math.toRadians(orientation_deg))) ; }
}
