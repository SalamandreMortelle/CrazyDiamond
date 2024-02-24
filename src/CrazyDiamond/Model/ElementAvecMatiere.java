package CrazyDiamond.Model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;

public interface ElementAvecMatiere {

    // Propriété statique
    ObjectProperty<Color> couleur_matiere_par_defaut_property = new SimpleObjectProperty<>(Color.DARKGREY) ;

    public Color couleurMatiere() ;
    void definirCouleurMatiere(Color couleur);

    public ObjectProperty<Color> couleurMatiereProperty() ;

    public void ajouterRappelSurChangementToutePropriete(RappelSurChangement rap) ;

    public void ajouterRappelSurChangementTouteProprieteModifiantChemin(RappelSurChangement rap) ;

    public void definirTypeSurface(TypeSurface type_surf) ;

    public TypeSurface typeSurface() ;

    public ObjectProperty<TypeSurface> typeSurfaceProperty() ;

    public void definirNatureMilieu(NatureMilieu nature_mil) ;

    public NatureMilieu natureMilieu() ;

    public ObjectProperty<NatureMilieu> natureMilieuProperty() ;

    public void definirIndiceRefraction(double indice_refraction) ;

    public double indiceRefraction() ;

    public DoubleProperty indiceRefractionProperty() ;


}
