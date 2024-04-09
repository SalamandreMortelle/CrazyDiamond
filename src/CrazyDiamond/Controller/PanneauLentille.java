package CrazyDiamond.Controller;

import CrazyDiamond.Model.*;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.layout.VBox;

import java.util.logging.Level;
import java.util.logging.Logger;

public class PanneauLentille {

    // Modèle
    Lentille lentille ;
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
    private Spinner<Double> spinner_xcentre;
    @FXML
    private Spinner<Double> spinner_ycentre;

    @FXML
    private Spinner<Double> spinner_epaisseur;
    @FXML
    private Spinner<Double> spinner_diametre;

    @FXML
    private Spinner<Double> spinner_r_courbure_1;
    @FXML
    private Spinner<Double> spinner_r_courbure_2;

    @FXML
    public Spinner<Double> spinner_orientation;
    @FXML
    public Slider slider_orientation;

    public PanneauLentille(Lentille l, boolean dans_composition, CanvasAffichageEnvironnement cnv) {

        if (l==null)
            throw new IllegalArgumentException("L'objet Lentille attaché au PanneauLentille ne peut pas être 'null'") ;

        this.lentille = l ;
        this.dans_composition=dans_composition;
        this.canvas = cnv ;

    }

    public void initialize() {
        LOGGER.log(Level.INFO,"Initialisation du PanneauLentille et de ses liaisons") ;

        baseElementIdentifieController.initialize(lentille);

        if (!dans_composition) {
            baseContourController.initialize(lentille);
            baseMatiereController.initialize(lentille);
        } else {
            baseMatiere.setVisible(false);
            baseContour.setVisible(false);
        }

        // Prise en compte automatique de la position et de l'orientation
        lentille.positionEtOrientationObjectProperty().addListener(new ChangeListenerAvecGarde<>(this::prendreEnComptePositionEtOrientation));

        // Position Xcentre
        spinner_xcentre.getStyleClass().add(Spinner.STYLE_CLASS_ARROWS_ON_RIGHT_HORIZONTAL) ;
        spinner_xcentre.editableProperty().bind(lentille.appartenanceSystemeOptiqueProperty().not()) ;
        spinner_xcentre.disableProperty().bind(lentille.appartenanceSystemeOptiqueProperty()) ;
        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_xcentre, lentille.xCentre(), this::definirXCentreLentille);

        // Position Ycentre
        spinner_ycentre.editableProperty().bind(lentille.appartenanceSystemeOptiqueProperty().not()) ;
        spinner_ycentre.disableProperty().bind(lentille.appartenanceSystemeOptiqueProperty()) ;
        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_ycentre, lentille.yCentre(), this::definirYCentreLentille);

        // Orientation
        spinner_orientation.getValueFactory().setWrapAround(true);
        spinner_orientation.editableProperty().bind(lentille.appartenanceSystemeOptiqueProperty().not()) ;
        spinner_orientation.disableProperty().bind(lentille.appartenanceSystemeOptiqueProperty()) ;
        OutilsControleur.integrerSpinnerDoubleValidant(spinner_orientation,lentille.orientation(),this::definirOrientation);

        slider_orientation.valueProperty().set(lentille.orientation());
        slider_orientation.valueProperty().addListener(new ChangeListenerAvecGarde<>(this::definirOrientation));
        slider_orientation.disableProperty().bind(lentille.appartenanceSystemeOptiqueProperty()) ;

        // Epaisseur
        lentille.epaisseurProperty().addListener(new ChangeListenerAvecGarde<>(this::prendreEnCompteEpaisseur));
        spinner_epaisseur.getStyleClass().add(Spinner.STYLE_CLASS_ARROWS_ON_RIGHT_HORIZONTAL) ;
        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_epaisseur, lentille.epaisseur(),this::definirEpaisseur);

        // Diamètre
        lentille.diametreProperty().addListener(new ChangeListenerAvecGarde<>(this::prendreEnCompteDiametre));
        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_diametre, lentille.diametre(),this::definirDiametre);

        // Rayon Courbure 1
        lentille.rayonCourbure1Property().addListener(new ChangeListenerAvecGarde<>(this::prendreEnCompteRayonCourbure1));
//        spinner_r_courbure_1.getStyleClass().add(Spinner.STYLE_CLASS_ARROWS_ON_RIGHT_HORIZONTAL) ;
        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_r_courbure_1, lentille.rayonCourbure1(),this::definirRayonCourbure1);

        // Rayon Courbure 2
        lentille.rayonCourbure2Property().addListener(new ChangeListenerAvecGarde<>(this::prendreEnCompteRayonCourbure2));
//        spinner_r_courbure_1.getStyleClass().add(Spinner.STYLE_CLASS_ARROWS_ON_RIGHT_HORIZONTAL) ;
        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_r_courbure_2, lentille.rayonCourbure1(),this::definirRayonCourbure2);

    }

    private void definirOrientation(Number or) {
        new CommandeDefinirUnParametre<>(lentille,or.doubleValue(),lentille::orientation,lentille::definirOrientation).executer();
    }
    private void definirEpaisseur(Double l) {
        new CommandeDefinirUnParametreDoubleDistance<>(lentille,l,lentille::epaisseur,lentille::definirEpaisseur).executer() ;
    }
    private void definirDiametre(Double h) {
        new CommandeDefinirUnParametreDoubleDistance<>(lentille,h,lentille::diametre,lentille::definirDiametre).executer() ;
    }
    private void definirRayonCourbure1(Double l) {
        new CommandeDefinirUnParametreDoubleDistance<>(lentille,l,lentille::rayonCourbure1,lentille::definirRayonCourbure1).executer() ;
    }
    private void definirRayonCourbure2(Double l) {
        new CommandeDefinirUnParametreDoubleDistance<>(lentille,l,lentille::rayonCourbure2,lentille::definirRayonCourbure2).executer() ;
    }

    private void prendreEnCompteEpaisseur(Number l) {
        spinner_epaisseur.getValueFactory().valueProperty().set(l.doubleValue());
    }
    private void prendreEnCompteDiametre(Number l) {
        spinner_diametre.getValueFactory().valueProperty().set(l.doubleValue());
    }

    private void prendreEnCompteRayonCourbure1(Number l) {
        spinner_r_courbure_1.getValueFactory().valueProperty().set(l.doubleValue());
    }
    private void prendreEnCompteRayonCourbure2(Number l) {
        spinner_r_courbure_2.getValueFactory().valueProperty().set(l.doubleValue());
    }

    private void definirXCentreLentille(Double x_c) {
        new CommandeDefinirUnParametrePoint<>(lentille,new Point2D(x_c,lentille.yCentre()),lentille::centre,lentille::definirCentre).executer();
    }
    private void definirYCentreLentille(Double y_c) {
        new CommandeDefinirUnParametrePoint<>(lentille,new Point2D(lentille.xCentre(),y_c),lentille::centre,lentille::definirCentre).executer();        
    }

    private void prendreEnComptePositionEtOrientation(PositionEtOrientation nouvelle_pos_et_or) {
        spinner_xcentre.getValueFactory().valueProperty().set(nouvelle_pos_et_or.position().getX());
        spinner_ycentre.getValueFactory().valueProperty().set(nouvelle_pos_et_or.position().getY());
        spinner_orientation.getValueFactory().valueProperty().set(nouvelle_pos_et_or.orientation_deg());
        slider_orientation.valueProperty().set(nouvelle_pos_et_or.orientation_deg());
    }


}
