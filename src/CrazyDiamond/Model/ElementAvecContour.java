package CrazyDiamond.Model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;

public interface ElementAvecContour {

    ObjectProperty<Color> couleur_contour_par_defaut_property = new SimpleObjectProperty<>(Color.WHITE) ;
//    Color couleur_contour_par_defaut = Color.BLUE ;

    Color couleurContour() ;
    void definirCouleurContour(Color color);

    ObjectProperty<Color> couleurContourProperty() ;

    void definirTraitementSurface(TraitementSurface traitement_surf) ;
    TraitementSurface traitementSurface() ;
    ObjectProperty<TraitementSurface> traitementSurfaceProperty();

    void definirTauxReflexionSurface(double taux_refl) ;
    double tauxReflexionSurface();
    DoubleProperty tauxReflexionSurfaceProperty();


    void definirOrientationAxePolariseur(double angle_pol) ;
    double orientationAxePolariseur() ;
    DoubleProperty orientationAxePolariseurProperty();

    void ajouterRappelSurChangementToutePropriete(Object cle,RappelSurChangement rap) ;
    void ajouterRappelSurChangementTouteProprieteModifiantChemin(RappelSurChangement rap) ;

}
