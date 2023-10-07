package CrazyDiamond.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;

import java.util.logging.Level;
import java.util.logging.Logger;

public class PanneauParametresAffichageEnvironnement {

    @FXML
    public ColorPicker colorpicker_couleurs_normales;
    @FXML
    public CheckBox checkbox_normales_visibles;
    public CheckBox checkbox_prolongements_avant_visibles;
    public CheckBox checkbox_prolongements_arriere_visibles;
    public CheckBox checkbox_commentaire_visible;

    // Modèle
    CanvasAffichageEnvironnement cae ;

    // Récupération du logger
    private static final Logger LOGGER = Logger.getLogger( "CrazyDiamond" );

    public PanneauParametresAffichageEnvironnement(CanvasAffichageEnvironnement cae) {

        LOGGER.log(Level.INFO,"Construction du PanneauParametresAffichageEnvironnement") ;

        if (cae==null)
            throw new IllegalArgumentException("L'objet CanvasAffichageEnvironnement attaché au PanneauParametresAffichageEnvironnement ne peut pas être 'null'") ;

        this.cae = cae;
    }

    public void initialize() {

        LOGGER.log(Level.INFO,"Initialisation du PanneauSource et de ses liaisons") ;

        colorpicker_couleurs_normales.valueProperty().bindBidirectional(cae.couleur_normales);

        checkbox_normales_visibles.selectedProperty().bindBidirectional(cae.normales_visibles);

        checkbox_prolongements_avant_visibles.selectedProperty().bindBidirectional(cae.prolongements_avant_visibles);
        checkbox_prolongements_arriere_visibles.selectedProperty().bindBidirectional(cae.prolongements_arriere_visibles);

        checkbox_commentaire_visible.selectedProperty().bindBidirectional(cae.commentaire_visible);

    }

}
