package CrazyDiamond.Controller;

import CrazyDiamond.Model.ChangeListenerAvecGarde;
import CrazyDiamond.Model.PositionEtOrientation;
import CrazyDiamond.Model.Rectangle;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
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

//    CanvasAffichageEnvironnement eg ;

//    @FXML
//    private TextField textfield_nom ;

    @FXML
    private Spinner<Double> spinner_xcentre;
    // La déclaration de cet attribut est requise pour faire un binding "persistant" entre la variable numérique du modèle
    // et l'ObjectProperty<Double> à l'intérieur du Spinner Value Factory qui encapsule la valueProperty du Spinner. Il
    // créé une StrongRef qui permet de s'assurer qu'il n'y aura pas de garbage collection intempestif de cet ObjectProperty.
    // Cette obligation vient de la Property du Spinner Value Factory qui est de type ObjectProperty<Double> (ou Integer...)
    // et non de type DoubleProperty comme la Property du modèle, qu'il faut donc convertir avec la méthode asObject et stocker
    // en tant que tel, pour pouvoir réaliser le binding.
    private ObjectProperty<Double> rectangle_xcentre_object_property;

    @FXML
    private Spinner<Double> spinner_ycentre;
    private ObjectProperty<Double> rectangle_ycentre_object_property; // Attribut requis (cf. supra)

    @FXML
    private Spinner<Double> spinner_largeur;
    private ObjectProperty<Double> rectangle_largeur_object_property; // Attribut requis (cf. supra)

    @FXML
    private Spinner<Double> spinner_hauteur;
    private ObjectProperty<Double> rectangle_hauteur_object_property; // Attribut requis (cf. supra)

    @FXML
    public Spinner<Double> spinner_orientation;
    private ObjectProperty<Double> orientation_object_property;
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

        if (!dans_composition) {
            baseContourController.initialize(rectangle);
            baseMatiereController.initialize(rectangle);
        } else {
            baseMatiere.setVisible(false);
            baseContour.setVisible(false);
        }

        rectangle.positionEtOrientationObjectProperty().addListener(new ChangeListenerAvecGarde<PositionEtOrientation>(this::prendreEnComptePositionEtOrientation));

//        rectangle.axeObjectProperty().addListener(new ChangeListener<PositionEtOrientation>() {
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

        // Position Xcentre
//        rectangle_xcentre_object_property = rectangle.xCentreProperty().asObject() ;
//        spinner_xcentre.getValueFactory().valueProperty().bindBidirectional(rectangle_xcentre_object_property);
        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_xcentre, rectangle.xCentre(), this::definirXCentreRectangle);

//        spinner_xcentre.getValueFactory().valueProperty().set(rectangle.xCentre());
//        spinner_xcentre.getValueFactory().valueProperty().addListener(new ChangeListener<Double>() {
//            private boolean changement_en_cours = false ; ;
//
//            @Override
//            public void changed(ObservableValue<? extends Double> observableValue, Double old_value, Double new_value) {
//                if (!changement_en_cours) {
//                    try {
//                        changement_en_cours = true ;
//                        rectangle.definirCentre(new Point2D(new_value,rectangle.yCentre()));
//                    } finally {
//                        changement_en_cours = false ;
//                    }
//                }
//            }
//        });

        spinner_xcentre.getStyleClass().add(Spinner.STYLE_CLASS_ARROWS_ON_RIGHT_HORIZONTAL) ;
//        canvas.ajustePasEtAffichageSpinnerValueFactoryDistance((SpinnerValueFactory.DoubleSpinnerValueFactory) spinner_xcentre.getValueFactory());
        spinner_xcentre.editableProperty().bind(rectangle.appartenanceSystemeOptiqueProperty().not()) ;
        spinner_xcentre.disableProperty().bind(rectangle.appartenanceSystemeOptiqueProperty()) ;

        // Position Ycentre
//        rectangle_ycentre_object_property = rectangle.yCentreProperty().asObject() ;
//        spinner_ycentre.getValueFactory().valueProperty().bindBidirectional(rectangle_ycentre_object_property);
        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_ycentre, rectangle.yCentre(), this::definirYCentreRectangle);

//        spinner_ycentre.getValueFactory().valueProperty().set(rectangle.yCentre());
//        spinner_ycentre.getValueFactory().valueProperty().addListener(new ChangeListener<Double>() {
//            private boolean changement_en_cours = false ; ;
//
//            @Override
//            public void changed(ObservableValue<? extends Double> observableValue, Double old_value, Double new_value) {
//                if (!changement_en_cours) {
//                    try {
//                        changement_en_cours = true ;
//                        rectangle.definirCentre(new Point2D(rectangle.xCentre(),new_value));
//                    } finally {
//                        changement_en_cours = false ;
//                    }
//                }
//            }
//        });

//        canvas.ajustePasEtAffichageSpinnerValueFactoryDistance((SpinnerValueFactory.DoubleSpinnerValueFactory) spinner_ycentre.getValueFactory());
        spinner_ycentre.editableProperty().bind(rectangle.appartenanceSystemeOptiqueProperty().not()) ;
        spinner_ycentre.disableProperty().bind(rectangle.appartenanceSystemeOptiqueProperty()) ;

        // Largeur
        rectangle_largeur_object_property = rectangle.largeurProperty().asObject() ;
        spinner_largeur.getValueFactory().valueProperty().bindBidirectional(rectangle_largeur_object_property);
//        spinner_largeur.getValueFactory().valueProperty().bind(rectangle_largeur_object_property);
        spinner_largeur.getStyleClass().add(Spinner.STYLE_CLASS_ARROWS_ON_RIGHT_HORIZONTAL) ;
        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_largeur, rectangle.largeur());
//        canvas.ajustePasEtAffichageSpinnerValueFactoryDistance((SpinnerValueFactory.DoubleSpinnerValueFactory) spinner_largeur.getValueFactory());

        // Hauteur
        rectangle_hauteur_object_property = rectangle.hauteurProperty().asObject() ;
        spinner_hauteur.getValueFactory().valueProperty().bindBidirectional(rectangle_hauteur_object_property);
        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_hauteur, rectangle.hauteur());
//        canvas.ajustePasEtAffichageSpinnerValueFactoryDistance((SpinnerValueFactory.DoubleSpinnerValueFactory) spinner_hauteur.getValueFactory());

        // Orientation
        spinner_orientation.getValueFactory().setWrapAround(true);
        spinner_orientation.editableProperty().bind(rectangle.appartenanceSystemeOptiqueProperty().not()) ;
        spinner_orientation.disableProperty().bind(rectangle.appartenanceSystemeOptiqueProperty()) ;

//        orientation_object_property = rectangle.orientationProperty().asObject() ;
//        spinner_orientation.getValueFactory().valueProperty().bindBidirectional(orientation_object_property);

        OutilsControleur.integrerSpinnerDoubleValidant(spinner_orientation,rectangle.orientation(),rectangle::definirOrientation);

//        spinner_orientation.getValueFactory().valueProperty().set(rectangle.orientation());
//        spinner_orientation.getValueFactory().valueProperty().addListener(new ChangeListener<Double>() {
//            private boolean changement_en_cours = false ; ;
//
//            @Override
//            public void changed(ObservableValue<? extends Double> observableValue, Double old_value, Double new_value) {
//                if (!changement_en_cours) {
//                    try {
//                        changement_en_cours = true ;
//                        rectangle.definirOrientation(new_value);
//                    } finally {
//                        changement_en_cours = false ;
//                    }
//                }
//            }
//        });

//        slider_orientation.valueProperty().bindBidirectional(rectangle.orientationProperty());
//        slider_orientation.disableProperty().bind(rectangle.appartenanceSystemeOptiqueProperty()) ;
        slider_orientation.valueProperty().set(rectangle.orientation());
        slider_orientation.valueProperty().addListener(new ChangeListener<>() {
            private boolean changement_en_cours = false ; ;

            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number old_value, Number new_value) {
                if (!changement_en_cours) {
                    try {
                        changement_en_cours = true ;
                        rectangle.definirOrientation(new_value.doubleValue());
                    } finally {
                        changement_en_cours = false ;
                    }
                }
            }
        });

        slider_orientation.disableProperty().bind(rectangle.appartenanceSystemeOptiqueProperty()) ;


    }

    private void definirXCentreRectangle(Double x_c) {rectangle.definirCentre(new Point2D(x_c,rectangle.yCentre()));}
    private void definirYCentreRectangle(Double y_c) {rectangle.definirCentre(new Point2D(rectangle.xCentre(),y_c));}

    private void prendreEnComptePositionEtOrientation(PositionEtOrientation nouvelle_pos_et_or) {
        spinner_xcentre.getValueFactory().valueProperty().set(nouvelle_pos_et_or.position().getX());
        spinner_ycentre.getValueFactory().valueProperty().set(nouvelle_pos_et_or.position().getY());
        spinner_orientation.getValueFactory().valueProperty().set(nouvelle_pos_et_or.orientation_deg());
        slider_orientation.valueProperty().set(nouvelle_pos_et_or.orientation_deg());
    }


}
