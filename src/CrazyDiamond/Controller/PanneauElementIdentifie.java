package CrazyDiamond.Controller;

import CrazyDiamond.Model.Nommable;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class PanneauElementIdentifie {

    @FXML
    private TextField textfield_nom ;

    public void initialize(Nommable element_identifie) {

        textfield_nom.textProperty().bindBidirectional(element_identifie.nomProperty());

    }

}
