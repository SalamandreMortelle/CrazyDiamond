package CrazyDiamond.Model;

import javafx.beans.property.ObjectProperty;
import javafx.geometry.Point2D;

import java.util.List;

public interface ElementDeSOC extends /*Identifiable,*/Nommable {

    void definirSOCParent(SystemeOptiqueCentre soc) ;
    SystemeOptiqueCentre SOCParent() ;
    ObjectProperty<SystemeOptiqueCentre> systemeOptiqueParentProperty() ;
    SystemeOptiqueCentre SOCParentDirect() ;

    default void ajouter(ElementDeSOC e) { // Ne fait rien pour les Obstacles (car on ne peut pas y ajouter d'élément) / surchargé pour les SOC
        throw new IllegalCallerException("Cette méthode ne devrait pas être appelée");
    }
    default void ajouter(List<ElementDeSOC> elements_a_ajouter) {
        throw new IllegalCallerException("Cette méthode ne devrait pas être appelée");
    }

    default void detacherElementsCentres() { }  // Ne fait rien pour les Obstacles / surchargé pour les SOC

    default boolean sansSOCParentActif() {
        // TODO : A étudier...
        return  (SOCParent()==null /* || !SOCParent().referenceDirectement(this) */) ;

    }

    Point2D pointSurAxeRevolution() ;
    double orientation() ;

    void translater(Point2D tr) ;
    void tournerAutourDe(Point2D centre_rot, double angle_rot_deg) ;

    void ajouterRappelSurChangementToutePropriete(Object cle_observateur,RappelSurChangement rappel);
    void retirerRappelSurChangementToutePropriete(Object cle_observateur);

//    void ajouterRappelSurChangementTouteProprieteModifiantElementsCardinaux(Object cle,RappelSurChangement rappel) ;

    default boolean comprend(ElementDeSOC el) { return comprend((Obstacle)el) ; }
    boolean comprend(Obstacle o) ;

    boolean estReflechissant() ;

    default void accepte(VisiteurEnvironnement v) { } // Ne fait rien pour les Obstacles / surchargé pour les SOC

    default boolean estUnSOC() { return false ; }

    default boolean estUnObstacle() { return false ; }



    void convertirDistances(double facteur_conversion);


    default double positionDansSOCParent() {
        if (SOCParent()==null)
            return 0;

        return SOCParent().direction().dotProduct(pointDeReferencePourPositionnementDansSOCParent().subtract(SOCParent().origine())) ;
    }


    default Point2D pointDeReferencePourPositionnementDansSOCParent() { return null ;}

    default void definirPointDeReferencePourPositionnementDansSOCParent(Point2D point2D) { }

    default ObjectProperty<PositionEtOrientation> positionEtOrientationProperty() { return null; }
    default ObjectProperty<Point2D> positionProperty() { return null; }
}
