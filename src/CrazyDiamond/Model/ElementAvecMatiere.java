package CrazyDiamond.Model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;

public interface ElementAvecMatiere {

    // Propriété statique
    ObjectProperty<Color> couleur_matiere_par_defaut_property = new SimpleObjectProperty<>(Color.DARKGREY) ;

    Color couleurMatiere() ;
    void definirCouleurMatiere(Color couleur);

    ObjectProperty<Color> couleurMatiereProperty() ;

    void ajouterRappelSurChangementToutePropriete(Object cle,RappelSurChangement rap) ;

    void ajouterRappelSurChangementTouteProprieteModifiantChemin(RappelSurChangement rap) ;

    void definirTypeSurface(TypeSurface type_surf) ;

    TypeSurface typeSurface() ;

    ObjectProperty<TypeSurface> typeSurfaceProperty() ;

    void definirNatureMilieu(NatureMilieu nature_mil) ;

    NatureMilieu natureMilieu() ;

    ObjectProperty<NatureMilieu> natureMilieuProperty() ;

    void definirIndiceRefraction(double indice_refraction) ;

    double indiceRefraction() ;

    DoubleProperty indiceRefractionProperty() ;


}
