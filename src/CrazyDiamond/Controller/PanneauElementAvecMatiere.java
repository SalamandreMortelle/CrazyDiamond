package CrazyDiamond.Controller;

import CrazyDiamond.Model.ElementAvecMatiere;
import CrazyDiamond.Model.NatureMilieu;
import CrazyDiamond.Model.TypeSurface;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.logging.Level;
import java.util.logging.Logger;

// Controleur du sous-panneau des propriétés de matière d'un obstacle
public class PanneauElementAvecMatiere {

    // Récupération du logger
    private static final Logger LOGGER = Logger.getLogger( "CrazyDiamond" );

    @FXML
    private ColorPicker colorpicker_matiere;

    @FXML
    private ToggleGroup choix_convexite ;

    @FXML
    private Toggle choix_convexe ;

    @FXML
    private Toggle choix_concave ;

    @FXML
    private ToggleGroup choix_nature_milieu ;

    @FXML
    private RadioButton choix_absorbant;

//    @FXML
//    public RadioButton choix_reflechissant;

    @FXML
    private RadioButton choix_transparent ;

    @FXML
    public Label label_indice;

    @FXML
    public Spinner<Double> spinner_indice_refraction;
    private ObjectProperty<Double> spinner_indice_refraction_object_property; // Attribut requis


    public void initialize(ElementAvecMatiere element_avec_matiere) {

        // Couleurs
        colorpicker_matiere.valueProperty().bindBidirectional( element_avec_matiere.couleurMatiereProperty() );

        if (element_avec_matiere.typeSurface() == TypeSurface.CONVEXE)
            choix_convexe.setSelected(true);

        if (element_avec_matiere.typeSurface() == TypeSurface.CONCAVE)
            choix_concave.setSelected(true);

        OutilsControleur.integrerSpinnerDoubleValidant(spinner_indice_refraction, element_avec_matiere.indiceRefraction()/*, element_avec_matiere::definirIndiceRefraction*/);
        spinner_indice_refraction_object_property = element_avec_matiere.indiceRefractionProperty().asObject() ;
        spinner_indice_refraction.getValueFactory().valueProperty().bindBidirectional(spinner_indice_refraction_object_property);


        // Ce listener est mono-directionnel Vue > Modèle (mais l'état initial du toggle convexité est déjà positionné)
        choix_convexite.selectedToggleProperty().addListener((observable, oldValue,newValue) -> {
            LOGGER.log(Level.FINE,"Choix convexite passe de {0} à {1}", new Object[] {oldValue,newValue}) ;

            if (choix_convexite.getSelectedToggle()==choix_convexe)
                element_avec_matiere.definirTypeSurface(TypeSurface.CONVEXE);

            if (choix_convexite.getSelectedToggle()==choix_concave)
                element_avec_matiere.definirTypeSurface(TypeSurface.CONCAVE);

        });

        element_avec_matiere.typeSurfaceProperty().addListener((observableValue, oldValue, newValue) -> {
            LOGGER.log(Level.FINE,"Type surface passe de {0} à {1}", new Object[] {oldValue,newValue}) ;

            if (newValue== TypeSurface.CONVEXE && choix_convexite.getSelectedToggle()!=choix_convexe)
                choix_convexite.selectToggle(choix_convexe);

            if (newValue== TypeSurface.CONCAVE && choix_convexite.getSelectedToggle()!=choix_concave)
                choix_convexite.selectToggle(choix_concave);
        });

        // Nature du milieu

//        if (element_avec_matiere.natureMilieu() == NatureMilieu.REFLECHISSANT)
//            choix_reflechissant.setSelected(true);

        if (element_avec_matiere.natureMilieu() == NatureMilieu.ABSORBANT)
            choix_absorbant.setSelected(true);

        if (element_avec_matiere.natureMilieu() == NatureMilieu.TRANSPARENT)
            choix_transparent.setSelected(true);


        label_indice.disableProperty().bind(element_avec_matiere.natureMilieuProperty().isNotEqualTo(NatureMilieu.TRANSPARENT)) ;
        spinner_indice_refraction.disableProperty().bind(element_avec_matiere.natureMilieuProperty().isNotEqualTo(NatureMilieu.TRANSPARENT)) ;


        // Ce listener est mono-directionnel Vue > Modèle (mais l'état initial du toggle nature milieu est déjà positionné)
        choix_nature_milieu.selectedToggleProperty().addListener((observable, oldValue,newValue) -> {
            LOGGER.log(Level.FINE,"Choix nature milieu passe de {0} à {1}", new Object[] {oldValue,newValue}) ;

//            if (newValue== choix_reflechissant && element_avec_matiere.natureMilieu()!= NatureMilieu.REFLECHISSANT)
//                element_avec_matiere.definirNatureMilieu(NatureMilieu.REFLECHISSANT);

            if (newValue== choix_absorbant && element_avec_matiere.natureMilieu()!= NatureMilieu.ABSORBANT)
                element_avec_matiere.definirNatureMilieu(NatureMilieu.ABSORBANT);

            if (newValue==choix_transparent && element_avec_matiere.natureMilieu()!= NatureMilieu.TRANSPARENT)
                element_avec_matiere.definirNatureMilieu(NatureMilieu.TRANSPARENT);

        });

        element_avec_matiere.natureMilieuProperty().addListener( (observableValue, oldValue, newValue) -> {
            LOGGER.log(Level.FINE,"Nature milieu passe de {0} à {1}", new Object[] {oldValue,newValue}) ;

//            if (newValue== NatureMilieu.REFLECHISSANT && choix_nature_milieu.getSelectedToggle()!= choix_reflechissant)
//                choix_nature_milieu.selectToggle(choix_reflechissant);

            if (newValue== NatureMilieu.ABSORBANT && choix_nature_milieu.getSelectedToggle()!= choix_absorbant)
                choix_nature_milieu.selectToggle(choix_absorbant);

            if (newValue== NatureMilieu.TRANSPARENT && choix_nature_milieu.getSelectedToggle()!=choix_transparent)
                choix_nature_milieu.selectToggle(choix_transparent);

        } );

    }

}

