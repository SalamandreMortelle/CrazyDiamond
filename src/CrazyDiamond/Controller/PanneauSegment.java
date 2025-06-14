package CrazyDiamond.Controller;

import CrazyDiamond.Model.*;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.logging.Level;
import java.util.logging.Logger;

public class PanneauSegment {

    // Modèle
    Segment segment ;
    private final boolean dans_composition;

    CanvasAffichageEnvironnement canvas;

    // Récupération du logger
    private static final Logger LOGGER = Logger.getLogger( "CrazyDiamond" );

    @FXML
    private VBox baseElementIdentifie;
    @FXML
    private PanneauElementIdentifie baseElementIdentifieController ;

    // Controleur du sous-panneau générique pour les attributs de contour
    @FXML
    private VBox baseContour;
    @FXML
    private PanneauElementAvecContour baseContourController;

    @FXML
    private VBox baseSansEpaisseur;

    @FXML
    public VBox vbox_panneau_racine;
    @FXML
    public VBox vbox_positionnement_absolu;
    @FXML
    private HBox hbox_positionnement_relatif_dans_soc;
    @FXML
    private PanneauPositionnementElementDansSOC hbox_positionnement_relatif_dans_socController;

    @FXML
    private Spinner<Double> spinner_xcentre ;

    @FXML
    private Spinner<Double> spinner_ycentre ;

    @FXML
    private Spinner<Double> spinner_longueur;

    @FXML
    public Spinner<Double> spinner_r_diaphragme;
    @FXML
    public Slider slider_r_diaphragme;

    private final DoubleProperty pourcentage_ouverture_diaphragme;
    private boolean garde_recalcul_rayon_diaphragme = false ;

    @FXML
    public Spinner<Double> spinner_orientation;
    @FXML
    public Slider slider_orientation;



    public PanneauSegment(Segment s,boolean dans_composition,CanvasAffichageEnvironnement cnv) {
        LOGGER.log(Level.INFO,"Construction du PanneauSegment") ;

        if (s==null)
            throw new IllegalArgumentException("L'objet Segment attaché au PanneauSegment ne peut pas être 'null'") ;

        this.segment = s;
        this.dans_composition = dans_composition ;
        this.canvas = cnv ;

        this.pourcentage_ouverture_diaphragme =new SimpleDoubleProperty(0d) ;

    }

    public void initialize() {
        LOGGER.log(Level.INFO,"Initialisation du PanneauSegment et de ses liaisons") ;

        baseElementIdentifieController.initialize(segment);

        hbox_positionnement_relatif_dans_socController.initialize(canvas,segment);

        UtilitairesVue.gererAppartenanceSOC(segment,baseContourController,vbox_panneau_racine,vbox_positionnement_absolu, hbox_positionnement_relatif_dans_soc);

        UtilitairesVue.gererAppartenanceComposition(dans_composition,segment,baseContour,baseContourController,null,null) ;

//        if (!dans_composition)
//            baseContourController.initialize(segment);
//        else
//            baseContour.setVisible(false);

        // Prise en compte automatique de la position et de l'orientation
        segment.positionEtOrientationObjectProperty().addListener(new ChangeListenerAvecGarde<>(this::prendreEnComptePositionEtOrientation));

        // Position : X centre
        spinner_xcentre.getStyleClass().add(Spinner.STYLE_CLASS_ARROWS_ON_RIGHT_HORIZONTAL) ;
        spinner_xcentre.editableProperty().bind(segment.appartenanceSystemeOptiqueProperty().not()) ;
        spinner_xcentre.disableProperty().bind(segment.appartenanceSystemeOptiqueProperty()) ;
        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_xcentre, segment.xCentre(), this::definirXCentreSegment);

        // Position : X centre
        spinner_ycentre.editableProperty().bind(segment.appartenanceSystemeOptiqueProperty().not()) ;
        spinner_ycentre.disableProperty().bind(segment.appartenanceSystemeOptiqueProperty()) ;
        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_ycentre, segment.yCentre(), this::definirYCentreSegment);

        // Orientation
        spinner_orientation.getValueFactory().setWrapAround(true);
        spinner_orientation.editableProperty().bind(segment.appartenanceSystemeOptiqueProperty().not()) ;
        spinner_orientation.disableProperty().bind(segment.appartenanceSystemeOptiqueProperty()) ;
        OutilsControleur.integrerSpinnerDoubleValidant(spinner_orientation,segment.orientation(),this::definirOrientation);

        slider_orientation.valueProperty().set(segment.orientation());
        slider_orientation.disableProperty().bind(segment.appartenanceSystemeOptiqueProperty()) ;
        slider_orientation.valueProperty().addListener(new ChangeListenerAvecGarde<>(this::definirOrientation));

        // Longueur
        segment.longueurProperty().addListener(new ChangeListenerAvecGarde<>(this::prendreEnCompteLongueur));
        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_longueur, segment.longueur(),this::definirLongueur);

        // Pupille
        segment.rayonDiaphragmeProperty().addListener(new ChangeListenerAvecGarde<>(this::prendreEnCompteRayonDiaphragme));
        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_r_diaphragme, segment.rayonDiaphragme(),this::definirRayonDiaphragme);

        // Limiter le rayon de la pupille à la moitié de la longueur du segment
        ((SpinnerValueFactory.DoubleSpinnerValueFactory) spinner_r_diaphragme.getValueFactory()).maxProperty().bind(segment.longueurProperty().multiply(0.5d));

        segment.rayonDiaphragmeProperty().addListener((observable, oldValue, newValue)->{
            double longueur = segment.longueur() ;
            if (longueur>0) {
                // Garde : comme il y a (un peu plus bas) un listener sur le pourcentage_ouverture_diaphragme qui peut
                // lui-même venir modifier la propriété rayonDiaphragme et re-déclencher le présent listener, mieux vaut
                // une garde pour éviter une boucle infinie (même si, en pratique, on dirait qu'elle ne se produit pas)
                garde_recalcul_rayon_diaphragme = true ;
                pourcentage_ouverture_diaphragme.set(100d * newValue.doubleValue() / (0.5d*longueur));
                garde_recalcul_rayon_diaphragme = false ;
            }
        });

        segment.longueurProperty().addListener((observable, oldValue, newValue)->{
            if (newValue.doubleValue()>0) {
                garde_recalcul_rayon_diaphragme = true;
                pourcentage_ouverture_diaphragme.set(100 * segment.rayonDiaphragme() / (newValue.doubleValue()*0.5d));
                garde_recalcul_rayon_diaphragme = false;
            }
        });

        slider_r_diaphragme.setLabelFormatter(new StringConverter<>() {
            @Override public String toString(Double aDouble) {
                return aDouble.intValue()+"%" ;
            }
            @Override public Double fromString(String s) {
                return null;
            }
        });

        slider_r_diaphragme.valueProperty().bindBidirectional(pourcentage_ouverture_diaphragme);

        // Initialisation du pourcentage ouverture rayon diaphragme (nécessaire quand on instancie depuis une désérialisation)
        pourcentage_ouverture_diaphragme.set(100 * segment.rayonDiaphragme() / (segment.longueur()*0.5d));

        pourcentage_ouverture_diaphragme.addListener((observable, oldValue, newValue)
            -> {
                // Garde : Si c'est un changement du rayon diaphragme qui a déclenché le présent listener, inutile de
                // recalculer le rayon diaphragme. Permet d'éviter une potentielle boucle infini" (même si en pratique,
                // elle ne semble pas avoir lieu)
                if (!garde_recalcul_rayon_diaphragme)
                    segment.rayonDiaphragmeProperty().set(newValue.doubleValue()*segment.longueur()*0.5d/100d);
            } ) ;

    }

    private void definirRayonDiaphragme(Double r_d) {
        new CommandeDefinirUnParametreDoubleDistance<>(segment,r_d,segment::rayonDiaphragme,segment::definirRayonDiaphragme).executer() ;
    }

    private void prendreEnCompteRayonDiaphragme(Number r_d) {
        spinner_r_diaphragme.getValueFactory().valueProperty().set(r_d.doubleValue());
    }

    private void definirLongueur(Double lg) {
        new CommandeDefinirUnParametreDoubleDistance<>(segment,lg,segment::longueur,segment::definirLongueur).executer() ;
    }

    private void prendreEnCompteLongueur(Number lg) {
        spinner_longueur.getValueFactory().valueProperty().set(lg.doubleValue());
    }

    private void definirXCentreSegment(Double x_c) {
        new CommandeDefinirUnParametrePoint<>(segment,new Point2D(x_c,segment.centre().getY()),segment::centre,segment::definirCentre).executer();
    }
    private void definirYCentreSegment(Double y_c) {
        new CommandeDefinirUnParametrePoint<>(segment,new Point2D(segment.centre().getX(),y_c),segment::centre,segment::definirCentre).executer();
    }
    private void definirOrientation(Number or) {
        new CommandeDefinirUnParametre<>(segment,or.doubleValue(),segment::orientation,segment::definirOrientation).executer();
    }

    private void prendreEnComptePositionEtOrientation(PositionEtOrientation nouvelle_pos_et_or) {
        spinner_xcentre.getValueFactory().valueProperty().set(nouvelle_pos_et_or.position().getX());
        spinner_ycentre.getValueFactory().valueProperty().set(nouvelle_pos_et_or.position().getY());
        spinner_orientation.getValueFactory().valueProperty().set(nouvelle_pos_et_or.orientation_deg());
        slider_orientation.valueProperty().set(nouvelle_pos_et_or.orientation_deg());
    }


}
