package CrazyDiamond.Controller;

import CrazyDiamond.Model.*;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.layout.VBox;

import java.util.logging.Level;
import java.util.logging.Logger;

public class PanneauDemiPlan {

    // Modèle
    DemiPlan demi_plan ;
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
    private Spinner<Double> spinner_xorigine ;

    @FXML
    private Spinner<Double> spinner_yorigine ;

    @FXML
    private Spinner<Double> spinner_orientation;

    @FXML
    private Slider slider_orientation;

    public PanneauDemiPlan(DemiPlan dp, boolean dans_composition ,CanvasAffichageEnvironnement cnv) {
        LOGGER.log(Level.INFO,"Construction du PanneauDemiPlan") ;

        if (dp==null)
            throw new IllegalArgumentException("L'objet DemiPlan attaché au PanneauDemiPlan ne peut pas être 'null'") ;

        this.demi_plan = dp;
        this.dans_composition = dans_composition ;
        this.canvas = cnv ;
    }

    public void initialize() {
        LOGGER.log(Level.INFO,"Initialisation du PanneauDemiPlan et de ses liaisons") ;

        baseElementIdentifieController.initialize(demi_plan);

        if (!dans_composition) {
            baseContourController.initialize(demi_plan);
            baseMatiereController.initialize(demi_plan);
        }else {
            baseMatiere.setVisible(false);
            baseContour.setVisible(false);
        }

        // Prise en compte automatique de la position et de l'orientation
        demi_plan.positionEtOrientationObjectProperty().addListener(new ChangeListenerAvecGarde<>(this::prendreEnComptePositionEtOrientation));

        // Position : X origine
        spinner_xorigine.getStyleClass().add(Spinner.STYLE_CLASS_ARROWS_ON_RIGHT_HORIZONTAL) ;
        spinner_xorigine.editableProperty().bind(demi_plan.appartenanceSystemeOptiqueProperty().not()) ;
        spinner_xorigine.disableProperty().bind(demi_plan.appartenanceSystemeOptiqueProperty()) ;
        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_xorigine, demi_plan.xOrigine(), this::definirXOrigineDemiPlan);

        // Position : Y origine
        spinner_yorigine.editableProperty().bind(demi_plan.appartenanceSystemeOptiqueProperty().not()) ;
        spinner_yorigine.disableProperty().bind(demi_plan.appartenanceSystemeOptiqueProperty()) ;
        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_yorigine, demi_plan.yOrigine(), this::definirYOrigineDemiPlan);

        // Orientation
        spinner_orientation.getValueFactory().setWrapAround(true);
        spinner_orientation.editableProperty().bind(demi_plan.appartenanceSystemeOptiqueProperty().not()) ;
        spinner_orientation.disableProperty().bind(demi_plan.appartenanceSystemeOptiqueProperty()) ;
        OutilsControleur.integrerSpinnerDoubleValidant(spinner_orientation,demi_plan.orientation(),this::definirOrientation);

        slider_orientation.valueProperty().set(demi_plan.orientation());
        slider_orientation.disableProperty().bind(demi_plan.appartenanceSystemeOptiqueProperty()) ;
        slider_orientation.valueProperty().addListener(new ChangeListenerAvecGarde<>(this::definirOrientation));
    }

    private void definirOrientation(Number or) {
        new CommandeDefinirUnParametre<>(demi_plan,or.doubleValue(),demi_plan::orientation,demi_plan::definirOrientation).executer();
    }
    private void definirXOrigineDemiPlan(Double x_o) {
        new CommandeDefinirUnParametrePoint<>(demi_plan,new Point2D(x_o,demi_plan.yOrigine()),demi_plan::origine,demi_plan::definirOrigine).executer();
    }
    private void definirYOrigineDemiPlan(Double y_o) {
        new CommandeDefinirUnParametrePoint<>(demi_plan,new Point2D(demi_plan.xOrigine(),y_o),demi_plan::origine,demi_plan::definirOrigine).executer();
    }

    private void prendreEnComptePositionEtOrientation(PositionEtOrientation nouvelle_pos_et_or) {
        spinner_xorigine.getValueFactory().valueProperty().set(nouvelle_pos_et_or.position().getX());
        spinner_yorigine.getValueFactory().valueProperty().set(nouvelle_pos_et_or.position().getY());
        spinner_orientation.getValueFactory().valueProperty().set(nouvelle_pos_et_or.orientation_deg());
        slider_orientation.valueProperty().set(nouvelle_pos_et_or.orientation_deg());
    }

}
