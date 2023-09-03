package CrazyDiamond.Controller;

import CrazyDiamond.Model.ElementSansEpaisseur;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;

import java.util.logging.Logger;

public class PanneauElementSansEpaisseur {

    // Récupération du logger
    private static final Logger LOGGER = Logger.getLogger( "CrazyDiamond" );

    @FXML
    private ToggleGroup choix_nature_milieu ;

    @FXML
    private RadioButton choix_absorbant;

    @FXML
    public RadioButton choix_reflechissant;

    public void initialize(ElementSansEpaisseur element_sans_epaisseur) {

//        // Nature du milieu ; un élément sans épaisseur ne peut être que REFLECHISSANT ou ABSORBANT (il ne peut ps être
//        // REFRINGENT)
//
//        if (element_sans_epaisseur.natureMilieu() == NatureMilieu.REFLECHISSANT)
//            choix_reflechissant.setSelected(true);
//
//        if (element_sans_epaisseur.natureMilieu() == NatureMilieu.ABSORBANT)
//            choix_absorbant.setSelected(true);
//
//
//        // Ce listener est mono-directionnel Vue > Modèle (mais l'état initial du toggle nature milieu est déjà positionné)
//        choix_nature_milieu.selectedToggleProperty().addListener((observable, oldValue,newValue) -> {
//            LOGGER.log(Level.FINE,"Choix nature passe de {0} à {1}", new Object[] {oldValue,newValue}) ;
//
//            if (newValue== choix_reflechissant && element_sans_epaisseur.natureMilieu()!= NatureMilieu.REFLECHISSANT)
//                element_sans_epaisseur.definirNatureMilieu(NatureMilieu.REFLECHISSANT);
//
//            if (newValue== choix_absorbant && element_sans_epaisseur.natureMilieu()!= NatureMilieu.ABSORBANT)
//                element_sans_epaisseur.definirNatureMilieu(NatureMilieu.ABSORBANT);
//
//        });
//
//        element_sans_epaisseur.natureMilieuProperty().addListener( (observableValue, oldValue, newValue) -> {
//            LOGGER.log(Level.FINE,"TNature milieu passe de {0} à {1}", new Object[] {oldValue,newValue}) ;
//
//            if (newValue== NatureMilieu.REFLECHISSANT && choix_nature_milieu.getSelectedToggle()!= choix_reflechissant)
//                choix_nature_milieu.selectToggle(choix_reflechissant);
//
//            if (newValue== NatureMilieu.ABSORBANT && choix_nature_milieu.getSelectedToggle()!= choix_absorbant)
//                choix_nature_milieu.selectToggle(choix_absorbant);
//
//        } );

    }

}
