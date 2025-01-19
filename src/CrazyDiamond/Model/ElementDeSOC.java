package CrazyDiamond.Model;

import javafx.geometry.Point2D;

import java.util.List;

public interface ElementDeSOC extends /*Identifiable,*/Nommable {

    void definirSOCParent(SystemeOptiqueCentre soc) ;
    SystemeOptiqueCentre SOCParent() ;
    SystemeOptiqueCentre SOCParentDirect() ;

    default void ajouter(ElementDeSOC e) { // Ne fait rien pour les Obstacles (car on ne peut pas y ajouter d'élément) / surchargé pour les SOC
        throw new IllegalCallerException("Cette méthode ne devrait pas être appelée");
    }
    default void ajouter(List<ElementDeSOC> elements_a_ajouter) {
        throw new IllegalCallerException("Cette méthode ne devrait pas être appelée");
    }

    default void detacherElementsCentres() { }  // Ne fait rien pour les Obstacles / surchargé pour les SOC

    Point2D pointSurAxeRevolution() ;
    double orientation() ;

    void translater(Point2D tr) ;
    void tournerAutourDe(Point2D centre_rot, double angle_rot_deg) ;

    void ajouterRappelSurChangementToutePropriete(RappelSurChangement rappel);
    void ajouterRappelSurChangementTouteProprieteModifiantElementsCardinaux(RappelSurChangement rappel) ;

    default boolean comprend(ElementDeSOC el) { return comprend((Obstacle)el) ; }
    boolean comprend(Obstacle o) ;

    default void accepte(VisiteurEnvironnement v) { } // Ne fait rien pour les Obstacles / surchargé pour les SOC

    default boolean estUnSOC() { return false ; }

    default boolean estUnObstacle() { return false ; }

    void convertirDistances(double facteur_conversion);
}
