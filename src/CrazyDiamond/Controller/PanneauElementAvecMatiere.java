package CrazyDiamond.Controller;

import CrazyDiamond.Model.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

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

    @FXML
    private RadioButton choix_transparent ;

    @FXML
    public Label label_indice;

    @FXML
    public Spinner<Double> spinner_indice_refraction;
    private ObjectProperty<Double> spinner_indice_refraction_object_property; // Attribut requis

    private ElementAvecMatiere element_avec_matiere ;

    public void initialize(ElementAvecMatiere element_avec_matiere) {

        this.element_avec_matiere = element_avec_matiere ;

        // Couleur matière
        colorpicker_matiere.valueProperty().set(element_avec_matiere.couleurMatiere());
        element_avec_matiere.couleurMatiereProperty().addListener(new ChangeListenerAvecGarde<>(colorpicker_matiere::setValue));
//        element_avec_matiere.couleurMatiereProperty().addListener(new ChangeListenerAvecGarde<>(colorpicker_matiere.valueProperty()::set));
        colorpicker_matiere.valueProperty().addListener((observableValue, c_avant, c_apres)
                -> new CommandeDefinirUnParametre<>(element_avec_matiere, c_apres, element_avec_matiere::couleurMatiere, element_avec_matiere::definirCouleurMatiere).executer());

        // Indice réfraction
        element_avec_matiere.indiceRefractionProperty().addListener(new ChangeListenerAvecGarde<Number>(this::prendreEnCompteIndiceRefraction));
        OutilsControleur.integrerSpinnerDoubleValidant(spinner_indice_refraction, element_avec_matiere.indiceRefraction(), this::definirIndiceRefraction);

        // Convexite
        element_avec_matiere.typeSurfaceProperty().addListener(new ChangeListenerAvecGarde<>(this::prendreEnCompteConvexite));

        choix_convexite.selectedToggleProperty().addListener((observable, oldValue,newValue) -> {
            LOGGER.log(Level.FINE,"Choix convexite passe de {0} à {1}", new Object[] {oldValue,newValue}) ;

            if (choix_convexite.getSelectedToggle()==choix_convexe)
                new CommandeDefinirUnParametre<>(element_avec_matiere, TypeSurface.CONVEXE, element_avec_matiere::typeSurface, element_avec_matiere::definirTypeSurface).executer();
            else if (choix_convexite.getSelectedToggle()==choix_concave)
                new CommandeDefinirUnParametre<>(element_avec_matiere, TypeSurface.CONCAVE, element_avec_matiere::typeSurface, element_avec_matiere::definirTypeSurface).executer();

        });

        // Nature du milieu
        element_avec_matiere.natureMilieuProperty().addListener(new ChangeListenerAvecGarde<>(this::prendreEnCompteNatureMilieu));
        choix_nature_milieu.selectedToggleProperty().addListener((observable, oldValue,newValue) -> {
            LOGGER.log(Level.FINE,"Choix nature milieu passe de {0} à {1}", new Object[] {oldValue,newValue}) ;

            if (newValue== choix_absorbant && element_avec_matiere.natureMilieu()!= NatureMilieu.ABSORBANT)
                new CommandeDefinirUnParametre<>(element_avec_matiere, NatureMilieu.ABSORBANT, element_avec_matiere::natureMilieu, element_avec_matiere::definirNatureMilieu).executer();

            if (newValue==choix_transparent && element_avec_matiere.natureMilieu()!= NatureMilieu.TRANSPARENT)
                new CommandeDefinirUnParametre<>(element_avec_matiere, NatureMilieu.TRANSPARENT, element_avec_matiere::natureMilieu, element_avec_matiere::definirNatureMilieu).executer();

        });

        label_indice.disableProperty().bind(element_avec_matiere.natureMilieuProperty().isNotEqualTo(NatureMilieu.TRANSPARENT)) ;
        spinner_indice_refraction.disableProperty().bind(element_avec_matiere.natureMilieuProperty().isNotEqualTo(NatureMilieu.TRANSPARENT)) ;

    }

    private void prendreEnCompteNatureMilieu(NatureMilieu nm) {
        switch (nm) {
            case ABSORBANT -> choix_absorbant.setSelected(true);
            case TRANSPARENT -> choix_transparent.setSelected(true);
        }
    }

    private void prendreEnCompteConvexite(TypeSurface ts) {
        switch (ts) {
            case CONVEXE -> choix_convexe.setSelected(true);
            case CONCAVE -> choix_concave.setSelected(true);
        }
    }

    private void prendreEnCompteIndiceRefraction(Number indice) {
        spinner_indice_refraction.getValueFactory().valueProperty().set(indice.doubleValue()) ;
    }

    private void definirIndiceRefraction(Double indice) {
        new CommandeDefinirUnParametre<>(element_avec_matiere, indice,
                element_avec_matiere::indiceRefraction,
                element_avec_matiere::definirIndiceRefraction
        ).executer();

    }

}

