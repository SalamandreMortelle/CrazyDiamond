package CrazyDiamond.Model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;

public interface ElementAvecContour {

    ObjectProperty<Color> couleur_contour_par_defaut_property = new SimpleObjectProperty<>(Color.WHITE) ;
//    Color couleur_contour_par_defaut = Color.BLUE ;

    public Color couleurContour() ;

    public ObjectProperty<Color> couleurContourProperty() ;

    public void definirTraitementSurface(TraitementSurface traitement_surf) ;
    public TraitementSurface traitementSurface() ;
    public ObjectProperty<TraitementSurface> traitementSurfaceProperty();

    public void definirTauxReflexionSurface(double taux_refl) ;
    public double tauxReflexionSurface();
    public DoubleProperty tauxReflexionSurfaceProperty();


    public void definirOrientationAxePolariseur(double angle_pol) ;
    public double orientationAxePolariseur() ;
    public DoubleProperty orientationAxePolariseurProperty();

    public void ajouterRappelSurChangementTouteProprieteModifiantChemin(RappelSurChangement rap) ;
    public void ajouterRappelSurChangementToutePropriete(RappelSurChangement rap) ;

}
