package CrazyDiamond.Controller;

import CrazyDiamond.Model.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.logging.Level;
import java.util.logging.Logger;

//public class PanneauConique extends PanneauBaseObstacleAvecMatiere {
public class PanneauConique  {

    Conique conique ;
    private boolean dans_composition;

    CanvasAffichageEnvironnement canvas;


    // Récupération du logger
    private static final Logger LOGGER = Logger.getLogger( "CrazyDiamond" );

    @FXML
    private VBox baseElementIdentifie;
    @FXML
    private PanneauElementIdentifie baseElementIdentifieController ;

    // Controleurs des sous-panneaux génériques pour les attributs de contour, et de matière
    @FXML
    private VBox baseContour;
    @FXML
    private PanneauElementAvecContour baseContourController;

    @FXML
    private VBox baseMatiere;
    @FXML
    private PanneauElementAvecMatiere baseMatiereController;

    // Contrôles de la vue :
    @FXML
    private Spinner<Double> spinner_xfoyer;
    private ObjectProperty<Double> xfoyer_object_property;

    @FXML
    private Spinner<Double> spinner_yfoyer;
    private ObjectProperty<Double> yfoyer_object_property;

    @FXML
    private Spinner<Double> spinner_parametre;
    private ObjectProperty<Double> parametre_object_property;

    @FXML
    private Spinner<Double> spinner_excentricite;
    private ObjectProperty<Double> excentricite_object_property;

    @FXML
    private Spinner<Double> spinner_orientation;
    private ObjectProperty<Double> orientation_object_property;

    @FXML
    private Slider slider_orientation;



    public PanneauConique(Conique c,boolean dans_composition,CanvasAffichageEnvironnement cnv) {
        LOGGER.log(Level.INFO,"Construction du PanneauConique") ;

        this.conique = c ;
        this.dans_composition = dans_composition ;
        this.canvas = cnv ;

    }

    public void initialize() {
        LOGGER.log(Level.INFO,"Initialisation du PanneauConique et de ses liaisons") ;

        baseElementIdentifieController.initialize(conique);

        if (!dans_composition) {
            baseContourController.initialize(conique);
            baseMatiereController.initialize(conique);
        } else {
            baseMatiere.setVisible(false);
            baseContour.setVisible(false);
        }

        // Prise en compte automatique de la position et de l'orientation de la conique
        conique.positionEtOrientationObjectProperty().addListener(new ChangeListenerAvecGarde<PositionEtOrientation>(this::prendreEnComptePositionEtOrientation));

        // X Foyer
        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_xfoyer, conique.xFoyer(), this::definirXFoyerConique);
        spinner_xfoyer.getStyleClass().add(Spinner.STYLE_CLASS_ARROWS_ON_RIGHT_HORIZONTAL) ;

        spinner_xfoyer.editableProperty().bind(conique.appartenanceSystemeOptiqueProperty().not()) ;
        spinner_xfoyer.disableProperty().bind(conique.appartenanceSystemeOptiqueProperty()) ;

        // Y Foyer
        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_yfoyer, conique.xFoyer(), this::definirYFoyerConique);

        spinner_yfoyer.editableProperty().bind(conique.appartenanceSystemeOptiqueProperty().not()) ;
        spinner_yfoyer.disableProperty().bind(conique.appartenanceSystemeOptiqueProperty()) ;

        // Orientation
        spinner_orientation.getValueFactory().setWrapAround(true);

        OutilsControleur.integrerSpinnerDoubleValidant(spinner_orientation,conique.orientation(),this::definirOrientation);

        spinner_orientation.editableProperty().bind(conique.appartenanceSystemeOptiqueProperty().not()) ;
        spinner_orientation.disableProperty().bind(conique.appartenanceSystemeOptiqueProperty()) ;

        slider_orientation.valueProperty().set(conique.orientation());
        slider_orientation.valueProperty().addListener(new ChangeListenerAvecGarde<>(this::definirOrientation)); ;

        slider_orientation.disableProperty().bind(conique.appartenanceSystemeOptiqueProperty()) ;

        // Paramètre
        conique.parametreProperty().addListener(new ChangeListenerAvecGarde<>(this::prendreEnCompteParametre));
        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_parametre, conique.parametre(),this::definirParametre);

        // Excentricité
        conique.excentriciteProperty().addListener(new ChangeListenerAvecGarde<>(this::prendreEnCompteExcentricite));
        OutilsControleur.integrerSpinnerDoubleValidant(spinner_excentricite, conique.excentricite(), this::definirExcentricite);

    }

    private void definirExcentricite(Double nouvelle_excentricite) {
        new CommandeDefinirUnParametre<>(conique,nouvelle_excentricite,conique::excentricite,conique::definirExcentricite).executer();
    }

    private void prendreEnCompteExcentricite(Number nouvelle_excentricite) {
        spinner_excentricite.getValueFactory().valueProperty().set(nouvelle_excentricite.doubleValue());
    }

    private void definirParametre(Double nouveau_rayon) {
        new CommandeDefinirUnParametreDoubleDistance<>(conique,nouveau_rayon,conique::parametre,conique::definirParametre).executer() ;
    }

    private void prendreEnCompteParametre(Number nouveau_parametre) {
        spinner_parametre.getValueFactory().valueProperty().set(nouveau_parametre.doubleValue());
    }

    private void definirOrientation(Double or) {
        new CommandeDefinirUnParametre<>(conique,or,conique::orientation,conique::definirOrientation).executer();
    }
    private void definirOrientation(Number or) {
        new CommandeDefinirUnParametre<>(conique,or.doubleValue(),conique::orientation,conique::definirOrientation).executer();
    }

    private void definirXFoyerConique(Double x_f) {
        new CommandeDefinirUnParametrePoint<>(conique,new Point2D(x_f,conique.foyer().getY()),conique::foyer,conique::definirFoyer).executer();

    }
    private void definirYFoyerConique(Double y_f) {
        new CommandeDefinirUnParametrePoint<>(conique,new Point2D(conique.foyer().getX(),y_f),conique::foyer,conique::definirFoyer).executer();
    }

    private void prendreEnComptePositionEtOrientation(PositionEtOrientation nouvelle_pos_et_or) {
        spinner_xfoyer.getValueFactory().valueProperty().set(nouvelle_pos_et_or.position().getX());
        spinner_yfoyer.getValueFactory().valueProperty().set(nouvelle_pos_et_or.position().getY());
        spinner_orientation.getValueFactory().valueProperty().set(nouvelle_pos_et_or.orientation_deg());
        slider_orientation.valueProperty().set(nouvelle_pos_et_or.orientation_deg());
    }
}
