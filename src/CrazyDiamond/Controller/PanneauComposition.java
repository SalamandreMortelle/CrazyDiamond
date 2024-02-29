package CrazyDiamond.Controller;

import CrazyDiamond.Model.CommandeDefinirUnParametre;
import CrazyDiamond.Model.Composition;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;

import java.util.logging.Level;
import java.util.logging.Logger;

public class PanneauComposition {

    // Modèle
    Composition composition ;
    private final boolean dans_composition;

    CanvasAffichageEnvironnement canvas;

    // Récupération du logger
    private static final Logger LOGGER = Logger.getLogger( "CrazyDiamond" );

    @FXML
    private VBox baseElementIdentifie;
    @FXML
    private PanneauElementIdentifie baseElementIdentifieController ;

    // Contrôleurs des sous-panneaux génériques pour les attributs de contour, et de matière
    @FXML
    private VBox baseContour;
    @FXML
    private PanneauElementAvecContour baseContourController;

    @FXML
    private VBox baseMatiere;
    @FXML
    private PanneauElementAvecMatiere baseMatiereController;

    @FXML
    private ToggleGroup choix_operation;
    @FXML
    public RadioButton choix_union;
    @FXML
    public RadioButton choix_intersection;
    @FXML
    public RadioButton choix_difference;
    @FXML
    public RadioButton choix_difference_symetrique;


    public PanneauComposition(Composition c,boolean dans_composition, CanvasAffichageEnvironnement cnv) {
        LOGGER.log(Level.INFO,"Construction du PanneauComposition") ;

        if (c==null)
            throw new IllegalArgumentException("L'objet Composition attaché au PanneauComposition ne peut pas être 'null'") ;

        this.composition = c;
        this.dans_composition=dans_composition ;
        this.canvas = cnv ;

    }

    public void initialize() {
        LOGGER.log(Level.INFO,"Initialisation du PanneauComposition et de ses liaisons") ;

        baseElementIdentifieController.initialize(composition);
        if (!dans_composition) {
            baseContourController.initialize(composition);
            baseMatiereController.initialize(composition);
        }else {
            baseMatiere.setVisible(false);
            baseContour.setVisible(false);
        }

        switch (composition.operateur()) {
            case UNION -> choix_union.setSelected(true);
            case INTERSECTION -> choix_intersection.setSelected(true);
            case DIFFERENCE -> choix_difference.setSelected(true);
            case DIFFERENCE_SYMETRIQUE -> choix_difference_symetrique.setSelected(true);
        }

        choix_operation.selectedToggleProperty().addListener((observable, oldValue,newValue) -> {
            LOGGER.log(Level.FINE,"Choix operation passe de {0} à {1}", new Object[] {oldValue,newValue}) ;

            if (choix_operation.getSelectedToggle()==choix_union)
                definirOperateur(Composition.Operateur.UNION);

            if (choix_operation.getSelectedToggle()==choix_intersection)
                definirOperateur(Composition.Operateur.INTERSECTION);

            if (choix_operation.getSelectedToggle()==choix_difference)
                definirOperateur(Composition.Operateur.DIFFERENCE);

            if (choix_operation.getSelectedToggle()==choix_difference_symetrique)
                definirOperateur(Composition.Operateur.DIFFERENCE_SYMETRIQUE);
        } ) ;

        composition.operateurProperty().addListener( (observableValue, oldValue, newValue) -> {
            LOGGER.log(Level.FINE,"Opérateur passe de {0} à {1}", new Object[] {oldValue,newValue}) ;

            if (newValue == Composition.Operateur.UNION && choix_operation.getSelectedToggle() != choix_union)
                choix_operation.selectToggle(choix_union);

            if (newValue == Composition.Operateur.INTERSECTION && choix_operation.getSelectedToggle() != choix_intersection)
                choix_operation.selectToggle(choix_intersection);

            if (newValue == Composition.Operateur.DIFFERENCE && choix_operation.getSelectedToggle() != choix_difference)
                choix_operation.selectToggle(choix_difference);

            if (newValue == Composition.Operateur.DIFFERENCE_SYMETRIQUE && choix_operation.getSelectedToggle() != choix_difference_symetrique)
                choix_operation.selectToggle(choix_difference_symetrique);

        } ) ;

    }

    public void definirOperateur(Composition.Operateur op) {
        new CommandeDefinirUnParametre<>(composition,op,composition::operateur,composition::definirOperateur).executer();
    }

}
