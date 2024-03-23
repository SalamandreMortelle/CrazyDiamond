package CrazyDiamond.Controller;

import CrazyDiamond.Model.CommandeDefinirUnParametre;
import CrazyDiamond.Model.Groupe;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;

import java.util.logging.Level;
import java.util.logging.Logger;

public class PanneauGroupe {

    // Modèle
    Groupe groupe;
    private final boolean dans_composition; // TODO : Interdire qu'un groupe fasse partie d'une composition

    CanvasAffichageEnvironnement canvas;

    // Récupération du logger
    private static final Logger LOGGER = Logger.getLogger( "CrazyDiamond" );

    @FXML
    private VBox baseElementIdentifie;
    @FXML
    private PanneauElementIdentifie baseElementIdentifieController ;

    @FXML
    private CheckBox choix_solidarisation;


    public PanneauGroupe(Groupe g, boolean dans_composition, CanvasAffichageEnvironnement cnv) {
        LOGGER.log(Level.INFO,"Construction du PanneauComposition") ;

        if (g==null)
            throw new IllegalArgumentException("L'objet Groupe attaché au PanneauGroupe ne peut pas être 'null'") ;

        this.groupe = g;
        this.dans_composition=dans_composition ;
        this.canvas = cnv ;

    }

    public void initialize() {
        LOGGER.log(Level.INFO,"Initialisation du PanneauGroupe et de ses liaisons") ;

        baseElementIdentifieController.initialize(groupe);

        choix_solidarisation.setSelected(groupe.elementsSolidaires());

        choix_solidarisation.selectedProperty().addListener((observable, oldValue,newValue) -> {
            LOGGER.log(Level.FINE,"Checkbox solidarisation passe de {0} à {1}", new Object[] {oldValue,newValue}) ;

            definirSolidarisation(choix_solidarisation.isSelected());
        } ) ;

        groupe.elementsSolidairesProperty().addListener( (observableValue, oldValue, newValue) -> {
            LOGGER.log(Level.FINE,"elements_solidaires passe de {0} à {1}", new Object[] {oldValue,newValue}) ;

            if (newValue && !choix_solidarisation.isSelected())
                choix_solidarisation.setSelected(true);

            if (!newValue && choix_solidarisation.isSelected())
                choix_solidarisation.setSelected(false);

        } ) ;

    }

    public void definirSolidarisation(boolean solidaire) {
        new CommandeDefinirUnParametre<>(groupe,solidaire, groupe::elementsSolidaires, groupe::definirElementsSolidaires).executer();
    }

}
