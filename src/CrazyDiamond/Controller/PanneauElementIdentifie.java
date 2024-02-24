package CrazyDiamond.Controller;

import CrazyDiamond.Model.ChangeListenerAvecGarde;
import CrazyDiamond.Model.CommandeDefinirUnParametre;
import CrazyDiamond.Model.Nommable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class PanneauElementIdentifie {

    @FXML
    private TextField textfield_nom ;

    public void initialize(Nommable element_identifie) {

        // Nom
        textfield_nom.setText(element_identifie.nom());
        element_identifie.nomProperty().addListener(new ChangeListenerAvecGarde<>(textfield_nom::setText));

        textfield_nom.textProperty().addListener((observableValue, t_avant, t_apres)
            -> new CommandeDefinirUnParametre<>(element_identifie,t_apres,element_identifie::nom,element_identifie::definirNom).executer());
    }

}
