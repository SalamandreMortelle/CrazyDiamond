package CrazyDiamond.Model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;

public class Imp_ElementAvecContour {

//    private static final Color couleur_contour_par_defaut = Color.BLUE ;

    protected final ObjectProperty<Color> couleur_contour;

    private final ObjectProperty<TraitementSurface> traitement_surface;
    private final DoubleProperty taux_reflexion_surface;
    private final DoubleProperty orientation_axe_polariseur;

    public Imp_ElementAvecContour(Color couleur_contour) {
        this(couleur_contour,TraitementSurface.AUCUN,0.0,0.0) ;
    }

    public Imp_ElementAvecContour(Color couleur_contour, TraitementSurface traitement_surf, double taux_refl_surf, double angle_pol) {

        if (couleur_contour == null)
            this.couleur_contour = new SimpleObjectProperty<>(ElementAvecContour.couleur_contour_par_defaut_property.getValue()) ;
        else
            this.couleur_contour = new SimpleObjectProperty<>(couleur_contour) ;

        this.traitement_surface = new SimpleObjectProperty<>(traitement_surf) ;
        this.taux_reflexion_surface = new SimpleDoubleProperty(taux_refl_surf) ;
        this.orientation_axe_polariseur = new SimpleDoubleProperty(angle_pol) ;
    }

    public void ajouterListeners(BaseObstacle bo) {
        this.couleur_contour.addListener((observable, oldValue, newValue) -> bo.declencherRappelsSurChangementToutePropriete());
        this.traitement_surface.addListener((observable, oldValue, newValue) -> bo.declencherRappelsSurChangementToutePropriete());
        this.taux_reflexion_surface.addListener((observable, oldValue, newValue) -> bo.declencherRappelsSurChangementToutePropriete());
        this.orientation_axe_polariseur.addListener((observable, oldValue, newValue) -> bo.declencherRappelsSurChangementToutePropriete());

    }

    public Color couleurContour() { return couleur_contour.get() ;}
    public void definirCouleurContour(Color c) { couleur_contour.set(c); }

    public ObjectProperty<Color> couleurContourProperty() {
        return couleur_contour;
    }

    
//    public void ajouterRappelSurChangementToutePropriete(RappelSurChangement rap) {
//        couleur_contour.addListener((observable, oldValue, newValue) -> { rap.rappel(); });
//        traitement_surface.addListener((observable, oldValue, newValue) -> { rap.rappel(); });
//        taux_reflexion_surface.addListener((observable, oldValue, newValue) -> { rap.rappel(); });
//        orientation_axe_polariseur.addListener((observable, oldValue, newValue) -> { rap.rappel(); });
//
//    }

    public void ajouterRappelSurChangementTouteProprieteModifiantChemin(RappelSurChangement rap) {
        // Trois propriétés de cette classe ont une incidence sur le chemin de la lumiere
        traitement_surface.addListener((observable, oldValue, newValue) -> rap.rappel());
        taux_reflexion_surface.addListener((observable, oldValue, newValue) -> rap.rappel());
        orientation_axe_polariseur.addListener((observable, oldValue, newValue) -> rap.rappel());
    }
    
    public TraitementSurface traitementSurface() {
        return traitement_surface.get() ;
    }
    public void definirTraitementSurface(TraitementSurface traitement_surf) {
        traitement_surface.set(traitement_surf);

        if ((traitement_surf==TraitementSurface.AUCUN || traitement_surf==TraitementSurface.ABSORBANT || traitement_surf==TraitementSurface.POLARISANT)&& tauxReflexionSurface() !=0.0)
            definirTauxReflexionSurface(0.0);
        if ((traitement_surf==TraitementSurface.REFLECHISSANT)&& tauxReflexionSurface() !=1.0)
            definirTauxReflexionSurface(1.0);

    }
    public ObjectProperty<TraitementSurface> traitementSurfaceProperty() { return traitement_surface ; }

    public double tauxReflexionSurface() {

        if (traitement_surface.get()==TraitementSurface.AUCUN || traitement_surface.get()== TraitementSurface.ABSORBANT || traitement_surface.get()==TraitementSurface.POLARISANT)
            return 0.0 ;
        if (traitement_surface.get()==TraitementSurface.REFLECHISSANT)
            return 1.0 ;

        return taux_reflexion_surface.get() ;
    }
    public DoubleProperty tauxReflexionSurfaceProperty() {return taux_reflexion_surface ;}

    public void definirTauxReflexionSurface(double taux_refl) {
        taux_reflexion_surface.set(taux_refl);

        if ( (taux_refl==1.0) && traitement_surface.get()!=TraitementSurface.REFLECHISSANT)
            traitement_surface.set(TraitementSurface.REFLECHISSANT);
        if ( (taux_refl==0.0) && traitement_surface.get()!=TraitementSurface.AUCUN)
            traitement_surface.set(TraitementSurface.AUCUN);

    }

    public void definirOrientationAxePolariseur(double angle_pol) {
        orientation_axe_polariseur.set(angle_pol);
    }

    public double orientationAxePolariseur() {
        return orientation_axe_polariseur.get() ;
    }

    public DoubleProperty orientationAxePolariseurProperty(){
        return orientation_axe_polariseur;
    }

}
