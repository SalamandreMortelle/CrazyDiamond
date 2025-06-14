package CrazyDiamond.Controller;

import CrazyDiamond.Model.*;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.logging.Level;
import java.util.logging.Logger;

public class PanneauRectangle {

    // Modèle
    Rectangle rectangle ;
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
    public VBox vbox_panneau_racine;
    @FXML
    public VBox vbox_positionnement_absolu;
    @FXML
    private HBox hbox_positionnement_relatif_dans_soc;
    @FXML
    private PanneauPositionnementElementDansSOC hbox_positionnement_relatif_dans_socController;

    @FXML
    private Spinner<Double> spinner_xcentre;
    @FXML
    private Spinner<Double> spinner_ycentre;

    @FXML
    private Spinner<Double> spinner_largeur;

    @FXML
    private Spinner<Double> spinner_hauteur;

    @FXML
    public Spinner<Double> spinner_orientation;
    @FXML
    public Slider slider_orientation;

    public PanneauRectangle(Rectangle r,boolean dans_composition,CanvasAffichageEnvironnement cnv) {

        if (r==null)
            throw new IllegalArgumentException("L'objet Rectangle attaché au PanneauRectangle ne peut pas être 'null'") ;

        this.rectangle = r ;
        this.dans_composition=dans_composition;
        this.canvas = cnv ;

    }

    public void initialize() {
        LOGGER.log(Level.INFO,"Initialisation du PanneauRectangle et de ses liaisons") ;

        baseElementIdentifieController.initialize(rectangle);

        hbox_positionnement_relatif_dans_socController.initialize(canvas,rectangle);

        UtilitairesVue.gererAppartenanceSOC(rectangle,baseContourController,vbox_panneau_racine,vbox_positionnement_absolu, hbox_positionnement_relatif_dans_soc);

        UtilitairesVue.gererAppartenanceComposition(dans_composition,rectangle,baseContour,baseContourController,baseMatiere,baseMatiereController) ;
//        if (!dans_composition) {
//            baseContourController.initialize(rectangle);
//            baseMatiereController.initialize(rectangle);
//        } else {
//            baseMatiere.setVisible(false);
//            baseContour.setVisible(false);
//        }

        // Prise en compte automatique de la position et de l'orientation
        rectangle.positionEtOrientationObjectProperty().addListener(new ChangeListenerAvecGarde<>(this::prendreEnComptePositionEtOrientation));

        // Position Xcentre
        spinner_xcentre.getStyleClass().add(Spinner.STYLE_CLASS_ARROWS_ON_RIGHT_HORIZONTAL) ;
//        spinner_xcentre.editableProperty().bind(rectangle.appartenanceSystemeOptiqueProperty().not()) ;
//        spinner_xcentre.disableProperty().bind(rectangle.appartenanceSystemeOptiqueProperty()) ;
        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_xcentre, rectangle.xCentre(), this::definirXCentreRectangle);

        // Position Ycentre
//        spinner_ycentre.editableProperty().bind(rectangle.appartenanceSystemeOptiqueProperty().not()) ;
//        spinner_ycentre.disableProperty().bind(rectangle.appartenanceSystemeOptiqueProperty()) ;
        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_ycentre, rectangle.yCentre(), this::definirYCentreRectangle);

        // Orientation
        spinner_orientation.getValueFactory().setWrapAround(true);
//        spinner_orientation.editableProperty().bind(rectangle.appartenanceSystemeOptiqueProperty().not()) ;
//        spinner_orientation.disableProperty().bind(rectangle.appartenanceSystemeOptiqueProperty()) ;
        OutilsControleur.integrerSpinnerDoubleValidant(spinner_orientation,rectangle.orientation(),this::definirOrientation);

        slider_orientation.valueProperty().set(rectangle.orientation());
        slider_orientation.valueProperty().addListener(new ChangeListenerAvecGarde<>(this::definirOrientation));
        slider_orientation.disableProperty().bind(rectangle.appartenanceSystemeOptiqueProperty()) ;

        // Largeur
        rectangle.largeurProperty().addListener(new ChangeListenerAvecGarde<>(this::prendreEnCompteLargeur));
        spinner_largeur.getStyleClass().add(Spinner.STYLE_CLASS_ARROWS_ON_RIGHT_HORIZONTAL) ;
        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_largeur, rectangle.largeur(),this::definirLargeur);

        // Hauteur
        rectangle.hauteurProperty().addListener(new ChangeListenerAvecGarde<>(this::prendreEnCompteHauteur));
        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_hauteur, rectangle.hauteur(),this::definirHauteur);

    }

    private void definirOrientation(Number or) {
        new CommandeDefinirUnParametre<>(rectangle,or.doubleValue(),rectangle::orientation,rectangle::definirOrientation).executer();
    }

    private void definirHauteur(Double h) {
        new CommandeDefinirUnParametreDoubleDistance<>(rectangle,h,rectangle::hauteur,rectangle::definirHauteur).executer() ;
    }

    private void prendreEnCompteHauteur(Number l) {
        spinner_hauteur.getValueFactory().valueProperty().set(l.doubleValue());
    }

    private void definirLargeur(Double l) {
        new CommandeDefinirUnParametreDoubleDistance<>(rectangle,l,rectangle::largeur,rectangle::definirLargeur).executer() ;
    }

    private void prendreEnCompteLargeur(Number l) {
        spinner_largeur.getValueFactory().valueProperty().set(l.doubleValue());
    }

    private void definirXCentreRectangle(Double x_c) {
        new CommandeDefinirUnParametrePoint<>(rectangle,new Point2D(x_c,rectangle.yCentre()),rectangle::centre,rectangle::definirCentre).executer();
    }
    private void definirYCentreRectangle(Double y_c) {
        new CommandeDefinirUnParametrePoint<>(rectangle,new Point2D(rectangle.xCentre(),y_c),rectangle::centre,rectangle::definirCentre).executer();        
    }

    private void prendreEnComptePositionEtOrientation(PositionEtOrientation nouvelle_pos_et_or) {
        spinner_xcentre.getValueFactory().valueProperty().set(nouvelle_pos_et_or.position().getX());
        spinner_ycentre.getValueFactory().valueProperty().set(nouvelle_pos_et_or.position().getY());
        spinner_orientation.getValueFactory().valueProperty().set(nouvelle_pos_et_or.orientation_deg());
        slider_orientation.valueProperty().set(nouvelle_pos_et_or.orientation_deg());
    }


}
