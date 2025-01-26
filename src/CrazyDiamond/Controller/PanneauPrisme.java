package CrazyDiamond.Controller;

import CrazyDiamond.Model.*;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.logging.Level;
import java.util.logging.Logger;

public class PanneauPrisme {

    // Modèle
    Prisme prisme ;
    private final boolean dans_composition;

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

    @FXML
    private Spinner<Double> spinner_xcentre;
    @FXML
    private Spinner<Double> spinner_ycentre;

    @FXML
    private Spinner<Double> spinner_largeur_base;

    @FXML
    private Spinner<Double> spinner_angle_sommet;
    @FXML
    public Slider slider_angle_sommet;

    @FXML
    public Spinner<Double> spinner_orientation;

    @FXML
    public Slider slider_orientation;

    public PanneauPrisme(Prisme p,boolean dans_composition,CanvasAffichageEnvironnement cnv) {

        if (p==null)
            throw new IllegalArgumentException("L'objet Prisme attaché au PanneauPrisme ne peut pas être 'null'") ;

        this.prisme = p ;
        this.dans_composition=dans_composition;
        this.canvas = cnv ;

    }

    public void initialize() {
        LOGGER.log(Level.INFO,"Initialisation du PanneauPrisme et de ses liaisons") ;

        baseElementIdentifieController.initialize(prisme);

        UtilitairesVue.gererAppartenanceComposition(dans_composition,prisme,baseContour,baseContourController,baseMatiere,baseMatiereController) ;
//        if (!dans_composition) {
//            baseContourController.initialize(prisme);
//            baseMatiereController.initialize(prisme);
//        } else {
//            baseMatiere.setVisible(false);
//            baseContour.setVisible(false);
//        }

        // Prise en compte automatique de la position et de l'orientation
        prisme.positionEtOrientationObjectProperty().addListener(new ChangeListenerAvecGarde<>(this::prendreEnComptePositionEtOrientation));

        // Position : X centre
        spinner_xcentre.getStyleClass().add(Spinner.STYLE_CLASS_ARROWS_ON_RIGHT_HORIZONTAL) ;
        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_xcentre, prisme.xCentre(), this::definirXCentrePrisme);

        // Position : Y centre
        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_ycentre, prisme.yCentre(), this::definirYCentrePrisme);

        // Orientation
        spinner_orientation.getValueFactory().setWrapAround(true);
        OutilsControleur.integrerSpinnerDoubleValidant(spinner_orientation,prisme.orientation(),this::definirOrientation);

        slider_orientation.valueProperty().set(prisme.orientation());
        slider_orientation.valueProperty().addListener(new ChangeListenerAvecGarde<>(this::definirOrientation));

        // Largeur base
        prisme.largeurBaseProperty().addListener(new ChangeListenerAvecGarde<>(this::prendreEnCompteLargeurBase));
        spinner_largeur_base.getStyleClass().add(Spinner.STYLE_CLASS_ARROWS_ON_RIGHT_HORIZONTAL) ;
        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_largeur_base, prisme.largeurBase(),this::definirLargeurBase);

        // Angle sommet
        prisme.angleSommetProperty().addListener(new ChangeListenerAvecGarde<>(this::prendreEnCompteAngleSommet));
        OutilsControleur.integrerSpinnerDoubleValidant(spinner_angle_sommet,prisme.angleSommet(),this::definirAngleSommet);
        slider_angle_sommet.valueProperty().set(prisme.angleSommet());
        slider_angle_sommet.valueProperty().addListener(new ChangeListenerAvecGarde<>(this::definirAngleSommet));

    }

    private void definirOrientation(Number or) {
            new CommandeDefinirUnParametre<>(prisme,or.doubleValue(),prisme::orientation,prisme::definirOrientation).executer();
    }
    private void definirAngleSommet(Number a_s) {
        new CommandeDefinirUnParametre<>(prisme,a_s.doubleValue(),prisme::angleSommet,prisme::definirAngleSommet).executer() ;
    }

    private void prendreEnCompteAngleSommet(Number a_s) {
        spinner_angle_sommet.getValueFactory().valueProperty().set(a_s.doubleValue());
        slider_angle_sommet.valueProperty().set(a_s.doubleValue());
    }

    private void definirLargeurBase(Double l_b) {
        new CommandeDefinirUnParametreDoubleDistance<>(prisme,l_b,prisme::largeurBase,prisme::definirLargeurBase).executer() ;
    }

    private void prendreEnCompteLargeurBase(Number l_b) {
        spinner_largeur_base.getValueFactory().valueProperty().set(l_b.doubleValue());
    }

    private void definirXCentrePrisme(Double x_c) {
        new CommandeDefinirUnParametrePoint<>(prisme,new Point2D(x_c,prisme.yCentre()),prisme::centre,prisme::definirCentre).executer();
    }
    private void definirYCentrePrisme(Double y_c) {
        new CommandeDefinirUnParametrePoint<>(prisme,new Point2D(prisme.xCentre(),y_c),prisme::centre,prisme::definirCentre).executer();
    }

    private void prendreEnComptePositionEtOrientation(PositionEtOrientation nouvelle_pos_et_or) {
        spinner_xcentre.getValueFactory().valueProperty().set(nouvelle_pos_et_or.position().getX());
        spinner_ycentre.getValueFactory().valueProperty().set(nouvelle_pos_et_or.position().getY());
        spinner_orientation.getValueFactory().valueProperty().set(nouvelle_pos_et_or.orientation_deg());
        slider_orientation.valueProperty().set(nouvelle_pos_et_or.orientation_deg());
    }

}
