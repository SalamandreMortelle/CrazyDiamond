package CrazyDiamond.Model;

import javafx.beans.property.ObjectProperty;
import javafx.geometry.Point2D;

public interface ElementSansEpaisseur {

    void ajouterRappelSurChangementToutePropriete(Object cle,RappelSurChangement rap) ;

    void ajouterRappelSurChangementTouteProprieteModifiantChemin(RappelSurChangement rap) ;

    void definirNatureMilieu(NatureMilieu nature_mil) ;

    NatureMilieu natureMilieu() ;

    ObjectProperty<NatureMilieu> natureMilieuProperty() ;

    default boolean contient(Point2D p) {
        return false;
    }

}
