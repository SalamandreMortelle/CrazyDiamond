package CrazyDiamond.Model;

import javafx.beans.property.*;
import javafx.scene.paint.Color;

import java.io.IOException;

public abstract class BaseObstacleAvecContour extends BaseObstacle {

    private final Imp_ElementAvecContour imp_elementAvecContour ;


    BaseObstacleAvecContour(String nom) {
        super(nom);
        this.imp_elementAvecContour = new Imp_ElementAvecContour(null) ;
    }

    BaseObstacleAvecContour(Imp_Identifiable ii, Imp_Nommable in, Imp_ElementAvecContour iac) {
        super(ii, in);
        this.imp_elementAvecContour = iac ;
    }

    public Color couleurContour() { return imp_elementAvecContour.couleurContour(); }
    public void definirCouleurContour(Color c) { imp_elementAvecContour.definirCouleurContour(c); }
    public ObjectProperty<Color> couleurContourProperty() { return imp_elementAvecContour.couleurContourProperty(); }

    public void definirTraitementSurface(TraitementSurface traitement_surf) { imp_elementAvecContour.definirTraitementSurface(traitement_surf);}
    public TraitementSurface traitementSurface() {return imp_elementAvecContour.traitementSurface() ;}
    public DoubleProperty tauxReflexionSurfaceProperty() {return imp_elementAvecContour.tauxReflexionSurfaceProperty() ; }

    public ObjectProperty<TraitementSurface> traitementSurfaceProperty() {return imp_elementAvecContour.traitementSurfaceProperty() ;}
    public void definirTauxReflexionSurface(double taux_refl) {imp_elementAvecContour.definirTauxReflexionSurface(taux_refl);}
    public double tauxReflexionSurface() {return imp_elementAvecContour.tauxReflexionSurface();}

    public void definirOrientationAxePolariseur(double angle_pol) {imp_elementAvecContour.definirOrientationAxePolariseur(angle_pol);}
    public double orientationAxePolariseur() {return imp_elementAvecContour.orientationAxePolariseur() ;}
    public DoubleProperty orientationAxePolariseurProperty() {return imp_elementAvecContour.orientationAxePolariseurProperty() ;}

    public void appliquerSurElementAvecContour(ConsumerAvecException<Object,IOException> consumer) throws IOException {
        consumer.accept(imp_elementAvecContour);
    }

    protected void ajouterRappelSurChangementToutePropriete(RappelSurChangement rap) {
        imp_elementAvecContour.ajouterRappelSurChangementToutePropriete(rap);
    }

    protected void ajouterRappelSurChangementTouteProprieteModifiantChemin(RappelSurChangement rap) {
        imp_elementAvecContour.ajouterRappelSurChangementTouteProprieteModifiantChemin(rap);
    }
}
