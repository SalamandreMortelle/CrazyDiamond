package CrazyDiamond.Controller;

import CrazyDiamond.Model.ElementAvecContour;
import CrazyDiamond.Model.TraitementSurface;
import javafx.fxml.FXML;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleGroup;
import javafx.util.StringConverter;

import java.util.logging.Level;
import java.util.logging.Logger;

// Controleur du sous-panneau des propriétés du contour d'un obstacle
public class PanneauElementAvecContour {

    private static final Logger LOGGER = Logger.getLogger( "CrazyDiamond" );
    public ToggleGroup choix_traitement_surface;
    public RadioButton choix_aucun;
    public RadioButton choix_absorbant;
    public RadioButton choix_reflechissant;
    public RadioButton choix_semi_reflechissant;
    public Slider slider_taux_reflexion_surface;
    public RadioButton choix_polarisant;
    public Slider slider_orientation_axe_polariseur;

    //    DoubleProperty poourcentage_taux_reflexion ;
    @FXML
    private ColorPicker colorpicker_contour;

    public PanneauElementAvecContour() {

//        this.poourcentage_taux_reflexion = new SimpleDoubleProperty(0d) ;

    }

    public void initialize(ElementAvecContour element_avec_contour) {

        // Couleur contour
        colorpicker_contour.valueProperty().bindBidirectional(element_avec_contour.couleurContourProperty());

        if (element_avec_contour.traitementSurface()== TraitementSurface.AUCUN)
            choix_aucun.setSelected(true);
        if (element_avec_contour.traitementSurface()==TraitementSurface.ABSORBANT)
            choix_absorbant.setSelected(true);
        if (element_avec_contour.traitementSurface()==TraitementSurface.REFLECHISSANT)
            choix_reflechissant.setSelected(true);
        if (element_avec_contour.traitementSurface()==TraitementSurface.PARTIELLEMENT_REFLECHISSANT)
            choix_semi_reflechissant.setSelected(true);
        if (element_avec_contour.traitementSurface()==TraitementSurface.POLARISANT)
            choix_polarisant.setSelected(true);

        slider_taux_reflexion_surface.valueProperty().bindBidirectional(element_avec_contour.tauxReflexionSurfaceProperty());
        slider_taux_reflexion_surface.disableProperty().bind(choix_semi_reflechissant.selectedProperty().not());

        slider_orientation_axe_polariseur.valueProperty().bindBidirectional(element_avec_contour.orientationAxePolariseurProperty());
        slider_orientation_axe_polariseur.disableProperty().bind(choix_polarisant.selectedProperty().not());

//        slider_taux_reflexion_surface.valueProperty().bindBidirectional(element_avec_contour.tauxReflexionSurfaceProperty());
//        element_avec_contour.tauxReflexionSurfaceProperty().addListener((observable, oldValue, newValue)->{
//                poourcentage_taux_reflexion.set(100d*newValue.doubleValue());
//            LOGGER.log(Level.FINE,"Nouvelle valeur du taux de réflexion : ",newValue) ;
//        });

        slider_taux_reflexion_surface.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(Double aDouble) {
                return Math.round(100d*aDouble.doubleValue())+"%" ;
            }

            @Override
            public Double fromString(String s) {
                return null;
            }
        });

//        slider_taux_reflexion_surface.valueProperty().bindBidirectional(poourcentage_taux_reflexion);

//        poourcentage_taux_reflexion.addListener((observable,oldValue,newValue)->{
//            element_avec_contour.tauxReflexionSurfaceProperty().set(newValue.doubleValue()/100d);
//        });



        // Ce listener est mono-directionnel Vue > Modèle (mais l'état initial du toggle traitement surface est déjà positionné)
        choix_traitement_surface.selectedToggleProperty().addListener((observable, oldValue,newValue) -> {
            LOGGER.log(Level.FINE,"Choix traitement surface passe de {0} à {1}", new Object[] {oldValue,newValue}) ;

//            if (newValue== choix_reflechissant && element_avec_matiere.natureMilieu()!= NatureMilieu.REFLECHISSANT)
//                element_avec_matiere.definirNatureMilieu(NatureMilieu.REFLECHISSANT);

            if (newValue==choix_aucun && element_avec_contour.traitementSurface()!= TraitementSurface.AUCUN)
                element_avec_contour.definirTraitementSurface(TraitementSurface.AUCUN);

            if (newValue== choix_absorbant && element_avec_contour.traitementSurface()!= TraitementSurface.ABSORBANT)
                element_avec_contour.definirTraitementSurface(TraitementSurface.ABSORBANT);

            if (newValue==choix_reflechissant && element_avec_contour.traitementSurface()!= TraitementSurface.REFLECHISSANT)
                element_avec_contour.definirTraitementSurface(TraitementSurface.REFLECHISSANT);

            if (newValue==choix_semi_reflechissant && element_avec_contour.traitementSurface()!= TraitementSurface.PARTIELLEMENT_REFLECHISSANT)
                element_avec_contour.definirTraitementSurface(TraitementSurface.PARTIELLEMENT_REFLECHISSANT);

            if (newValue==choix_polarisant && element_avec_contour.traitementSurface()!= TraitementSurface.POLARISANT)
                element_avec_contour.definirTraitementSurface(TraitementSurface.POLARISANT);



        });

        element_avec_contour.traitementSurfaceProperty().addListener( (observableValue, oldValue, newValue) -> {
            LOGGER.log(Level.FINE,"Traitement surface passe de {0} à {1}", new Object[] {oldValue,newValue}) ;

//            if (newValue== NatureMilieu.REFLECHISSANT && choix_nature_milieu.getSelectedToggle()!= choix_reflechissant)
//                choix_nature_milieu.selectToggle(choix_reflechissant);

            if (newValue== TraitementSurface.ABSORBANT && choix_traitement_surface.getSelectedToggle()!= choix_absorbant)
                choix_traitement_surface.selectToggle(choix_absorbant);

            if (newValue== TraitementSurface.AUCUN  && choix_traitement_surface.getSelectedToggle()!=choix_aucun)
                choix_traitement_surface.selectToggle(choix_aucun);

            if (newValue== TraitementSurface.REFLECHISSANT  && choix_traitement_surface.getSelectedToggle()!=choix_reflechissant)
                choix_traitement_surface.selectToggle(choix_reflechissant);

            if (newValue== TraitementSurface.PARTIELLEMENT_REFLECHISSANT  && choix_traitement_surface.getSelectedToggle()!=choix_semi_reflechissant)
                choix_traitement_surface.selectToggle(choix_semi_reflechissant);

            if (newValue== TraitementSurface.POLARISANT  && choix_traitement_surface.getSelectedToggle()!=choix_polarisant)
                choix_traitement_surface.selectToggle(choix_polarisant);

        } );


    }
}
