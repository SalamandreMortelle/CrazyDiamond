package CrazyDiamond.Model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;

import java.util.Objects;

public class Imp_ElementAvecMatiere {

    protected final ObjectProperty<Color> couleur_matiere;
    private final ObjectProperty<TypeSurface> type_surface;

    private final ObjectProperty<NatureMilieu> nature_milieu;
    private final DoubleProperty indice_refraction ;

    public Color couleurMatiere() {
        return couleur_matiere.get();
    }
    public void definirCouleurMatiere(Color couleur) {  couleur_matiere.set(couleur); }
    public ObjectProperty<Color> couleurMatiereProperty() {
        return couleur_matiere;
    }

    public Imp_ElementAvecMatiere(TypeSurface type_surface, NatureMilieu nature_milieu, double indice_refraction, Color couleur_matiere) {

        this.type_surface = new SimpleObjectProperty<>(Objects.requireNonNullElse(type_surface, TypeSurface.CONVEXE));

        this.nature_milieu = new SimpleObjectProperty<>(Objects.requireNonNullElse(nature_milieu, NatureMilieu.TRANSPARENT));

        this.indice_refraction = new SimpleDoubleProperty(indice_refraction) ;

        if (couleur_matiere == null)
            this.couleur_matiere = new SimpleObjectProperty<>(ElementAvecMatiere.couleur_matiere_par_defaut_property.getValue()) ;
        else
            this.couleur_matiere = new SimpleObjectProperty<>(couleur_matiere) ;

    }

    public void ajouterListeners(BaseObstacleAvecContourEtMatiere boacm) {
        this.type_surface.addListener((observable, oldValue, newValue) -> boacm.declencherRappelsSurChangementToutePropriete()) ;
        this.couleur_matiere.addListener((observable, oldValue, newValue) -> boacm.declencherRappelsSurChangementToutePropriete()) ;
        this.nature_milieu.addListener((observable, oldValue, newValue) -> boacm.declencherRappelsSurChangementToutePropriete()) ;
        this.indice_refraction.addListener((observable, oldValue, newValue) -> boacm.declencherRappelsSurChangementToutePropriete()) ;
    }

//    public void ajouterRappelSurChangementToutePropriete(RappelSurChangement rap) {
//        type_surface.addListener((observable, oldValue, newValue) -> { rap.rappel(); });
//        couleur_matiere.addListener((observable, oldValue, newValue) -> { rap.rappel(); });
//        nature_milieu.addListener((observable, oldValue, newValue) -> { rap.rappel(); });
//        indice_refraction.addListener((observable, oldValue, newValue) -> { rap.rappel(); });
//
//    }

    public void ajouterRappelSurChangementTouteProprieteModifiantChemin(RappelSurChangement rap) {
        type_surface.addListener((observable, oldValue, newValue) -> rap.rappel());
        nature_milieu.addListener((observable, oldValue, newValue) -> rap.rappel());
        indice_refraction.addListener((observable, oldValue, newValue) -> rap.rappel());

    }

    public void definirTypeSurface(TypeSurface type_surf) {
        if (type_surface.get() == type_surf)
            return ;

        type_surface.set(type_surf);
    }

    public TypeSurface typeSurface() {
        return type_surface.get();
    }

    public ObjectProperty<TypeSurface> typeSurfaceProperty() {
        return type_surface;
    }

    public void definirNatureMilieu(NatureMilieu nature_mil) {
        if (nature_milieu.get() == nature_mil)
            return ;

        nature_milieu.set(nature_mil);
    }

    public NatureMilieu natureMilieu() {
        return nature_milieu.get();
    }

    public ObjectProperty<NatureMilieu> natureMilieuProperty() {
        return nature_milieu;
    }

    public void definirIndiceRefraction(double indice_refraction) {
        if (this.indice_refraction.get() == indice_refraction)
            return ;

        this.indice_refraction.set(indice_refraction);
    }

    public double indiceRefraction() { return indice_refraction.doubleValue() ; }
    public DoubleProperty indiceRefractionProperty() {
        return indice_refraction ;
    }

}
