package CrazyDiamond.Controller;

import CrazyDiamond.Model.ChangeListenerAvecGarde;
import CrazyDiamond.Model.PositionEtOrientation;
import CrazyDiamond.Model.Segment;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.logging.Level;
import java.util.logging.Logger;

public class PanneauSegment {

    // Modèle
    Segment segment ;
    private boolean dans_composition;

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
//    @FXML
//    private PanneauElementSansEpaisseur baseSansEpaisseurController;

//    CanvasAffichageEnvironnement eg ;

//    @FXML
//    private TextField textfield_nom ;

    @FXML
    private Spinner<Double> spinner_xcentre ;
    // La déclaration de cet attribut est requise pour faire un binding "persistant" entre la variable numérique du modèle
    // et l'ObjectProperty<Double> à l'intérieur du Spinner Value Factory qui encapsule la valueProperty du Spinner. Il
    // créé une StrongRef qui permet de s'assurer qu'il n'y aura pas de garbage collection intempestif de cet ObjectProperty.
    // Cette obligation vient de la Property du Spinner Value Factory qui est de type ObjectProperty<Double> (ou Integer...)
    // et non de type DoubleProperty comme la Property du modèle, qu'il faut donc convertir avec la méthode asObject et stocker
    // en tant que tel, pour pouvoir réaliser le binding.
    private ObjectProperty<Double> segment_xcentre_object_property;

    @FXML
    private Spinner<Double> spinner_ycentre ;
    private ObjectProperty<Double> segment_ycentre_object_property; // Attribut requis (cf. supra)

    @FXML
    private Spinner<Double> spinner_longueur;
    private ObjectProperty<Double> segment_longueur_object_property; // Attribut requis (cf. supra)

    @FXML
    public Spinner<Double> spinner_r_diaphragme;
    private ObjectProperty<Double> segment_rayon_diaphragme_object_property; // Attribut requis (cf. supra)
    @FXML
    public Slider slider_r_diaphragme;

    private final DoubleProperty pourcentage_ouverture_diaphragme;

    @FXML
    public Spinner<Double> spinner_orientation;
    private ObjectProperty<Double> orientation_object_property;
    @FXML
    public Slider slider_orientation;


//    @FXML
//    private ColorPicker colorpicker_contour;


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

        if (!dans_composition)
            baseContourController.initialize(segment);
        else
            baseContour.setVisible(false);

        segment.positionEtOrientationObjectProperty().addListener(new ChangeListenerAvecGarde<PositionEtOrientation>(this::prendreEnComptePositionEtOrientation));

//        segment.positionEtOrientationObjectProperty().addListener(new ChangeListener<PositionEtOrientation>() {
//            private boolean changement_en_cours = false ;
//            @Override
//            public void changed(ObservableValue<? extends PositionEtOrientation> observableValue, PositionEtOrientation old_value, PositionEtOrientation new_value) {
//                if (!changement_en_cours) {
//                    try {
//                        changement_en_cours = true;
//                        spinner_xcentre.getValueFactory().valueProperty().set(new_value.position().getX());
//                        spinner_ycentre.getValueFactory().valueProperty().set(new_value.position().getY());
//                        spinner_orientation.getValueFactory().valueProperty().set(new_value.orientation_deg());
//                        slider_orientation.valueProperty().set(new_value.orientation_deg());
//                    } finally {
//                        changement_en_cours = false ;
//                    }
//                }
//            }
//        });


//        baseSansEpaisseurController.initialize(segment);

        // Position
//        segment_xcentre_object_property = segment.xCentreProperty().asObject() ;
//        spinner_xcentre.getValueFactory().valueProperty().bindBidirectional(segment_xcentre_object_property);
        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_xcentre, segment.xCentre(), this::definirXCentreSegment);

//        spinner_xcentre.getValueFactory().valueProperty().set(segment.xCentre());
//        spinner_xcentre.getValueFactory().valueProperty().addListener(new ChangeListener<Double>() {
//            private boolean changement_en_cours = false ; ;
//
//            @Override
//            public void changed(ObservableValue<? extends Double> observableValue, Double old_value, Double new_value) {
//                if (!changement_en_cours) {
//                    try {
//                        changement_en_cours = true ;
//                        segment.definirCentre(new Point2D(new_value,segment.yCentre()));
//                    } finally {
//                        changement_en_cours = false ;
//                    }
//                }
//            }
//        });

        spinner_xcentre.getStyleClass().add(Spinner.STYLE_CLASS_ARROWS_ON_RIGHT_HORIZONTAL) ;
//        canvas.ajustePasEtAffichageSpinnerValueFactoryDistance((SpinnerValueFactory.DoubleSpinnerValueFactory) spinner_xcentre.getValueFactory());
        spinner_xcentre.editableProperty().bind(segment.appartenanceSystemeOptiqueProperty().not()) ;
        spinner_xcentre.disableProperty().bind(segment.appartenanceSystemeOptiqueProperty()) ;


//        segment_ycentre_object_property = segment.yCentreProperty().asObject() ;
//        spinner_ycentre.getValueFactory().valueProperty().bindBidirectional(segment_ycentre_object_property);
        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_ycentre, segment.yCentre(), this::definirYCentreSegment);

//        spinner_ycentre.getValueFactory().valueProperty().set(segment.yCentre());
//        spinner_ycentre.getValueFactory().valueProperty().addListener(new ChangeListener<Double>() {
//            private boolean changement_en_cours = false ; ;
//
//            @Override
//            public void changed(ObservableValue<? extends Double> observableValue, Double old_value, Double new_value) {
//                if (!changement_en_cours) {
//                    try {
//                        changement_en_cours = true ;
//                        segment.definirCentre(new Point2D(segment.xCentre(),new_value));
//                    } finally {
//                        changement_en_cours = false ;
//                    }
//                }
//            }
//        });

//        canvas.ajustePasEtAffichageSpinnerValueFactoryDistance((SpinnerValueFactory.DoubleSpinnerValueFactory) spinner_ycentre.getValueFactory());
        spinner_ycentre.editableProperty().bind(segment.appartenanceSystemeOptiqueProperty().not()) ;
        spinner_ycentre.disableProperty().bind(segment.appartenanceSystemeOptiqueProperty()) ;

        // Longueur
        segment_longueur_object_property = segment.longueurProperty().asObject() ;
        spinner_longueur.getValueFactory().valueProperty().bindBidirectional(segment_longueur_object_property);
        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_longueur, segment.longueur()/*segment::definirLongueur*/);
//        canvas.ajustePasEtAffichageSpinnerValueFactoryDistance((SpinnerValueFactory.DoubleSpinnerValueFactory) spinner_longueur.getValueFactory());

        // Pupille
        segment_rayon_diaphragme_object_property = segment.rayonDiaphragmeProperty().asObject() ;
        spinner_r_diaphragme.getValueFactory().valueProperty().bindBidirectional(segment_rayon_diaphragme_object_property);
        // Limiter le rayon de la pupille à la moitié de la longueur du segment
        ((SpinnerValueFactory.DoubleSpinnerValueFactory) spinner_r_diaphragme.getValueFactory()).maxProperty().bind(segment.longueurProperty().multiply(0.5d));
        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_r_diaphragme, segment.rayonDiaphragme()/*segment::definirRayonDiaphragme*/);
        //        canvas.ajustePasEtAffichageSpinnerValueFactoryDistance((SpinnerValueFactory.DoubleSpinnerValueFactory) spinner_r_diaphragme.getValueFactory());

        segment.rayonDiaphragmeProperty().addListener((observable, oldValue, newValue)->{
            double longueur = segment.longueurProperty().doubleValue() ;
            if (longueur>0)
                pourcentage_ouverture_diaphragme.set(100d*newValue.doubleValue()/(0.5d*longueur));
        });
        segment.longueurProperty().addListener((observable, oldValue, newValue)->{

            if (newValue.doubleValue()>0)
                pourcentage_ouverture_diaphragme.set(100*segment.rayonDiaphragmeProperty().doubleValue()/ (newValue.doubleValue()*0.5d));
        });

        slider_r_diaphragme.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(Double aDouble) {
                return aDouble.intValue()+"%" ;
            }

            @Override
            public Double fromString(String s) {
                return null;
            }
        });

        slider_r_diaphragme.valueProperty().bindBidirectional(pourcentage_ouverture_diaphragme);

        pourcentage_ouverture_diaphragme.addListener((observable, oldValue, newValue)->{
            segment.rayonDiaphragmeProperty().set(newValue.doubleValue()*segment.longueurProperty().doubleValue()*0.5d/100d);
        });


//        slider_pupille.valueProperty().bindBidirectional(segment.pupilleProperty());

        // Orientation
        spinner_orientation.getValueFactory().setWrapAround(true);
        spinner_orientation.editableProperty().bind(segment.appartenanceSystemeOptiqueProperty().not()) ;
        spinner_orientation.disableProperty().bind(segment.appartenanceSystemeOptiqueProperty()) ;

//        orientation_object_property = segment.orientationProperty().asObject() ;
//        spinner_orientation.getValueFactory().valueProperty().bindBidirectional(orientation_object_property);
        OutilsControleur.integrerSpinnerDoubleValidant(spinner_orientation,segment.orientation(),segment::definirOrientation);
//        spinner_orientation.getValueFactory().valueProperty().set(segment.orientation());
//        spinner_orientation.getValueFactory().valueProperty().addListener(new ChangeListener<Double>() {
//            private boolean changement_en_cours = false ; ;
//
//            @Override
//            public void changed(ObservableValue<? extends Double> observableValue, Double old_value, Double new_value) {
//                if (!changement_en_cours) {
//                    try {
//                        changement_en_cours = true ;
//                        segment.definirOrientation(new_value);
//                    } finally {
//                        changement_en_cours = false ;
//                    }
//                }
//            }
//        });

//        slider_orientation.valueProperty().bindBidirectional(segment.orientationProperty());
        slider_orientation.valueProperty().set(segment.orientation());
        slider_orientation.valueProperty().addListener(new ChangeListener<>() {
            private boolean changement_en_cours = false ; ;

            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number old_value, Number new_value) {
                if (!changement_en_cours) {
                    try {
                        changement_en_cours = true ;
                        segment.definirOrientation(new_value.doubleValue());
                    } finally {
                        changement_en_cours = false ;
                    }
                }
            }
        });

        slider_orientation.disableProperty().bind(segment.appartenanceSystemeOptiqueProperty()) ;
    }

    private void definirXCentreSegment(Double x_c) {segment.definirCentre(new Point2D(x_c,segment.yCentre()));}
    private void definirYCentreSegment(Double y_c) {segment.definirCentre(new Point2D(segment.xCentre(),y_c));}

    private void prendreEnComptePositionEtOrientation(PositionEtOrientation nouvelle_pos_et_or) {
        spinner_xcentre.getValueFactory().valueProperty().set(nouvelle_pos_et_or.position().getX());
        spinner_ycentre.getValueFactory().valueProperty().set(nouvelle_pos_et_or.position().getY());
        spinner_orientation.getValueFactory().valueProperty().set(nouvelle_pos_et_or.orientation_deg());
        slider_orientation.valueProperty().set(nouvelle_pos_et_or.orientation_deg());
    }


}
