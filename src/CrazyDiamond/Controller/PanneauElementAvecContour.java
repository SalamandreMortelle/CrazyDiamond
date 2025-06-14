package CrazyDiamond.Controller;

import CrazyDiamond.Model.*;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleGroup;
import javafx.util.StringConverter;

import java.util.logging.Level;
import java.util.logging.Logger;

// Contrôleur du sous-panneau des propriétés du contour d'un obstacle
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

    ChangeListener<SystemeOptiqueCentre> change_listener_grand_parent ;

    @FXML
    private ColorPicker colorpicker_contour;

    public PanneauElementAvecContour() {
        change_listener_grand_parent = (observableGrandParentSOC, oldGrandParentSOC, newGrandParentSOC) -> {
            if (newGrandParentSOC!=null)
                interdireChoixSurfaceReflechissante();
            else
                autoriserChoixSurfaceReflechissante();
        } ;
    }

    public void initialize(ElementAvecContour element_avec_contour) {

        // Couleur contour
        colorpicker_contour.valueProperty().set(element_avec_contour.couleurContour());
        element_avec_contour.couleurContourProperty().addListener(new ChangeListenerAvecGarde<>(colorpicker_contour.valueProperty()::set));
        colorpicker_contour.valueProperty().addListener((observableValue, c_avant, c_apres)
                -> new CommandeDefinirUnParametre<>(element_avec_contour, c_apres, element_avec_contour::couleurContour, element_avec_contour::definirCouleurContour).executer());

        // Traitement surface
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

        // Taux réflexion surface
        element_avec_contour.tauxReflexionSurfaceProperty().addListener(new ChangeListenerAvecGarde<>(this::prendreEnCompteTauxReflexionSurface));
        slider_taux_reflexion_surface.valueProperty().addListener((observableValue, tr_avant, tr_apres)
                -> new CommandeDefinirUnParametre<>(element_avec_contour, tr_apres.doubleValue(),
                element_avec_contour::tauxReflexionSurface,
                element_avec_contour::definirTauxReflexionSurface
        ).executer()) ;

        slider_taux_reflexion_surface.disableProperty().bind(choix_semi_reflechissant.selectedProperty().not());

        // Orientation axe polariseur
        element_avec_contour.orientationAxePolariseurProperty().addListener(new ChangeListenerAvecGarde<>(this::prendreEnCompteOrientationAxePolariseur));
        slider_orientation_axe_polariseur.valueProperty().addListener((observableValue, or_avant, or_apres)
                -> new CommandeDefinirUnParametre<>(element_avec_contour,or_apres.doubleValue(),
                element_avec_contour::orientationAxePolariseur,
                element_avec_contour::definirOrientationAxePolariseur).executer() );

        slider_orientation_axe_polariseur.disableProperty().bind(choix_polarisant.selectedProperty().not());

        slider_taux_reflexion_surface.setLabelFormatter(new StringConverter<>() {
            @Override
            public String toString(Double aDouble) {
                return Math.round(100d * aDouble) + "%";
            }

            @Override
            public Double fromString(String s) {
                return null;
            }
        });

        // Traitement surface
        element_avec_contour.traitementSurfaceProperty().addListener( new ChangeListenerAvecGarde<>(this::prendreEnCompteTraitementSurface) ) ;
        choix_traitement_surface.selectedToggleProperty().addListener((observable, oldValue,newValue) -> {
            LOGGER.log(Level.FINE,"Choix traitement surface passe de {0} à {1}", new Object[] {oldValue,newValue}) ;

            if (newValue==choix_aucun && element_avec_contour.traitementSurface()!= TraitementSurface.AUCUN)
                new CommandeDefinirUnParametre<>(element_avec_contour,TraitementSurface.AUCUN,element_avec_contour::traitementSurface,element_avec_contour::definirTraitementSurface).executer();

            if (newValue== choix_absorbant && element_avec_contour.traitementSurface()!= TraitementSurface.ABSORBANT)
                new CommandeDefinirUnParametre<>(element_avec_contour,TraitementSurface.ABSORBANT,element_avec_contour::traitementSurface,element_avec_contour::definirTraitementSurface).executer();

            if (newValue==choix_reflechissant && element_avec_contour.traitementSurface()!= TraitementSurface.REFLECHISSANT)
                new CommandeDefinirUnParametre<>(element_avec_contour,TraitementSurface.REFLECHISSANT,element_avec_contour::traitementSurface,element_avec_contour::definirTraitementSurface).executer();

            if (newValue==choix_semi_reflechissant && element_avec_contour.traitementSurface()!= TraitementSurface.PARTIELLEMENT_REFLECHISSANT)
                new CommandeDefinirUnParametre<>(element_avec_contour,TraitementSurface.PARTIELLEMENT_REFLECHISSANT,element_avec_contour::traitementSurface,element_avec_contour::definirTraitementSurface).executer();

            if (newValue==choix_polarisant && element_avec_contour.traitementSurface()!= TraitementSurface.POLARISANT)
                new CommandeDefinirUnParametre<>(element_avec_contour,TraitementSurface.POLARISANT,element_avec_contour::traitementSurface,element_avec_contour::definirTraitementSurface).executer();
        });



    }

    private void prendreEnCompteTraitementSurface(TraitementSurface ts) {
        switch (ts) {
            case AUCUN -> choix_aucun.setSelected(true);
            case ABSORBANT -> choix_absorbant.setSelected(true);
            case REFLECHISSANT -> choix_reflechissant.setSelected(true);
            case PARTIELLEMENT_REFLECHISSANT -> choix_semi_reflechissant.setSelected(true);
            case POLARISANT -> choix_polarisant.setSelected(true);
        }
    }

    private void prendreEnCompteOrientationAxePolariseur(Number or) {
        slider_orientation_axe_polariseur.valueProperty().set(or.doubleValue());
    }

    private void prendreEnCompteTauxReflexionSurface(Number tr) {
        slider_taux_reflexion_surface.valueProperty().set(tr.doubleValue());
    }

    public void interdireChoixSurfaceReflechissante() {
        choix_reflechissant.setDisable(true);
        slider_taux_reflexion_surface.setMax(0.5);
    }

    public void autoriserChoixSurfaceReflechissante() {
        choix_reflechissant.setDisable(false);
        slider_taux_reflexion_surface.setMax(1.0);
    }

//    public void definirChangeListenerGrandParent(ChangeListener<SystemeOptiqueCentre> cl_grand_parent) {
//        change_listener_grand_parent = cl_grand_parent ;
//    }

    public ChangeListener<SystemeOptiqueCentre> changeListenerGrandParent() {
        return change_listener_grand_parent ;
    }

}
