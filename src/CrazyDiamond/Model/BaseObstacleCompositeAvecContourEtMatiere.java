package CrazyDiamond.Model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.paint.Color;

import java.io.IOException;

public class BaseObstacleCompositeAvecContourEtMatiere extends BaseObstacleCompositeAvecContour {

    private final Imp_ElementAvecMatiere imp_elementAvecMatiere ;

    BaseObstacleCompositeAvecContourEtMatiere(String nom, TypeSurface type_surface,NatureMilieu nature_milieu,double indice_refraction,Color couleur_matiere,Color couleur_contour) {
        super(nom, couleur_contour);
        this.imp_elementAvecMatiere = new Imp_ElementAvecMatiere(type_surface,nature_milieu ,indice_refraction,couleur_matiere) ;
        this.imp_elementAvecMatiere.ajouterListeners(this) ;
    }

    BaseObstacleCompositeAvecContourEtMatiere(Imp_Identifiable ii, Imp_Nommable in, Imp_ElementComposite ic, Imp_ElementAvecContour iac,Imp_ElementAvecMatiere iam) {
        super(ii, in, ic, iac);
        this.imp_elementAvecMatiere = iam ;
        this.imp_elementAvecMatiere.ajouterListeners(this) ;
    }

    public Color couleurMatiere() { return imp_elementAvecMatiere.couleurMatiere(); }
    public void definirCouleurMatiere(Color couleur) { imp_elementAvecMatiere.definirCouleurMatiere(couleur); }
    public ObjectProperty<Color> couleurMatiereProperty() { return imp_elementAvecMatiere.couleurMatiereProperty(); }

    public void definirTypeSurface(TypeSurface type_surf) { imp_elementAvecMatiere.definirTypeSurface(type_surf); }
    public TypeSurface typeSurface() { return imp_elementAvecMatiere.typeSurface(); }
    public ObjectProperty<TypeSurface> typeSurfaceProperty() { return imp_elementAvecMatiere.typeSurfaceProperty(); }

    public void definirNatureMilieu(NatureMilieu nature_mil) { imp_elementAvecMatiere.definirNatureMilieu(nature_mil); }
    public NatureMilieu natureMilieu() { return imp_elementAvecMatiere.natureMilieu(); }
    public ObjectProperty<NatureMilieu> natureMilieuProperty() { return imp_elementAvecMatiere.natureMilieuProperty(); }

    public void definirIndiceRefraction(double indice_refraction) { imp_elementAvecMatiere.definirIndiceRefraction(indice_refraction);   }
    public double indiceRefraction() { return imp_elementAvecMatiere.indiceRefraction(); }
    public DoubleProperty indiceRefractionProperty() {  return imp_elementAvecMatiere.indiceRefractionProperty(); }

    public void appliquerSurElementAvecMatiere(ConsumerAvecException<Object, IOException> consumer) throws IOException {
        consumer.accept(imp_elementAvecMatiere);
    }

}
