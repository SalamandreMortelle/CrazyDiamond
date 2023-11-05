package CrazyDiamond.Controller;

import CrazyDiamond.Model.ChangeListenerAvecGarde;
import CrazyDiamond.Model.PositionEtOrientation;
import CrazyDiamond.Model.Prisme;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.logging.Level;
import java.util.logging.Logger;

public class PanneauPrisme {

    // Modèle
    Prisme prisme ;
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
    private ObjectProperty<Double> prisme_xcentre_object_property;

    @FXML
    private Spinner<Double> spinner_ycentre;
    private ObjectProperty<Double> prisme_ycentre_object_property; // Attribut requis (cf. supra)

    @FXML
    private Spinner<Double> spinner_largeur_base;
    private ObjectProperty<Double> prisme_largeur_base_object_property; // Attribut requis (cf. supra)

    @FXML
    private Spinner<Double> spinner_angle_sommet;
    private ObjectProperty<Double> prisme_angle_sommet_object_property; // Attribut requis (cf. supra)
    @FXML
    public Slider slider_angle_sommet;

    @FXML
    public Spinner<Double> spinner_orientation;
    private ObjectProperty<Double> orientation_object_property;

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
        LOGGER.log(Level.INFO,"Initialisation du PanneauRectangle et de ses liaisons") ;

        baseElementIdentifieController.initialize(prisme);

        if (!dans_composition) {
            baseContourController.initialize(prisme);
            baseMatiereController.initialize(prisme);
        } else {
            baseMatiere.setVisible(false);
            baseContour.setVisible(false);
        }

        prisme.positionEtOrientationObjectProperty().addListener(new ChangeListenerAvecGarde<PositionEtOrientation>(this::prendreEnComptePositionEtOrientation));

//        prisme.axeObjectProperty().addListener(new ChangeListener<PositionEtOrientation>() {
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
//        prisme_xcentre_object_property = prisme.xCentreProperty().asObject() ;
//        spinner_xcentre.getValueFactory().valueProperty().bindBidirectional(prisme_xcentre_object_property);
        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_xcentre, prisme.xCentre(), this::definirXCentrePrisme);

        //        spinner_xcentre.getValueFactory().valueProperty().set(prisme.xCentre());
//        spinner_xcentre.getValueFactory().valueProperty().addListener(new ChangeListener<Double>() {
//            private boolean changement_en_cours = false ; ;
//
//            @Override
//            public void changed(ObservableValue<? extends Double> observableValue, Double old_value, Double new_value) {
//                if (!changement_en_cours) {
//                    try {
//                        changement_en_cours = true ;
//                        prisme.definirCentre(new Point2D(new_value,prisme.yCentre()));
//                    } finally {
//                        changement_en_cours = false ;
//                    }
//                }
//            }
//        });

        spinner_xcentre.getStyleClass().add(Spinner.STYLE_CLASS_ARROWS_ON_RIGHT_HORIZONTAL) ;
//        canvas.ajustePasEtAffichageSpinnerValueFactoryDistance((SpinnerValueFactory.DoubleSpinnerValueFactory) spinner_xcentre.getValueFactory());

        // Position Ycentre
//        prisme_ycentre_object_property = prisme.yCentreProperty().asObject() ;
//        spinner_ycentre.getValueFactory().valueProperty().bindBidirectional(prisme_ycentre_object_property);
        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_ycentre, prisme.yCentre(), this::definirYCentrePrisme);

//        spinner_ycentre.getValueFactory().valueProperty().set(prisme.yCentre());
//        spinner_ycentre.getValueFactory().valueProperty().addListener(new ChangeListener<Double>() {
//            private boolean changement_en_cours = false ; ;
//
//            @Override
//            public void changed(ObservableValue<? extends Double> observableValue, Double old_value, Double new_value) {
//                if (!changement_en_cours) {
//                    try {
//                        changement_en_cours = true ;
//                        prisme.definirCentre(new Point2D(prisme.xCentre(),new_value));
//                    } finally {
//                        changement_en_cours = false ;
//                    }
//                }
//            }
//        });
//
//        canvas.ajustePasEtAffichageSpinnerValueFactoryDistance((SpinnerValueFactory.DoubleSpinnerValueFactory) spinner_ycentre.getValueFactory());

        // Largeur base
        prisme_largeur_base_object_property = prisme.largeurBaseProperty().asObject() ;
        spinner_largeur_base.getValueFactory().valueProperty().bindBidirectional(prisme_largeur_base_object_property);
        spinner_largeur_base.getStyleClass().add(Spinner.STYLE_CLASS_ARROWS_ON_RIGHT_HORIZONTAL) ;
        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_largeur_base, prisme.largeurBase());
//        canvas.ajustePasEtAffichageSpinnerValueFactoryDistance((SpinnerValueFactory.DoubleSpinnerValueFactory) spinner_largeur_base.getValueFactory());

        // Angle sommet
        prisme_angle_sommet_object_property = prisme.angleSommetProperty().asObject() ;
        spinner_angle_sommet.getValueFactory().valueProperty().bindBidirectional(prisme_angle_sommet_object_property);
        OutilsControleur.integrerSpinnerDoubleValidant(spinner_angle_sommet,prisme.angleSommet(),prisme::definirAngleSommet);

        slider_angle_sommet.valueProperty().bindBidirectional(prisme.angleSommetProperty());

        // Orientation
        spinner_orientation.getValueFactory().setWrapAround(true);

        OutilsControleur.integrerSpinnerDoubleValidant(spinner_orientation,prisme.orientation(),prisme::definirOrientation);

//        orientation_object_property = prisme.orientationProperty().asObject() ;
//        spinner_orientation.getValueFactory().valueProperty().bindBidirectional(orientation_object_property);

//        spinner_orientation.getValueFactory().valueProperty().set(prisme.orientation());
//        spinner_orientation.getValueFactory().valueProperty().addListener(new ChangeListener<Double>() {
//            private boolean changement_en_cours = false ; ;
//
//            @Override
//            public void changed(ObservableValue<? extends Double> observableValue, Double old_value, Double new_value) {
//                if (!changement_en_cours) {
//                    try {
//                        changement_en_cours = true ;
//                        prisme.definirOrientation(new_value);
//                    } finally {
//                        changement_en_cours = false ;
//                    }
//                }
//            }
//        });

        slider_orientation.valueProperty().set(prisme.orientation());
        slider_orientation.valueProperty().addListener(new ChangeListener<>() {
            private boolean changement_en_cours = false ; ;

            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number old_value, Number new_value) {
                if (!changement_en_cours) {
                    try {
                        changement_en_cours = true ;
                        prisme.definirOrientation(new_value.doubleValue());
                    } finally {
                        changement_en_cours = false ;
                    }
                }
            }
        });

//        slider_orientation.valueProperty().bindBidirectional(prisme.orientationProperty());

    }

    private void definirXCentrePrisme(Double x_c) {prisme.definirCentre(new Point2D(x_c,prisme.yCentre()));}
    private void definirYCentrePrisme(Double y_c) {prisme.definirCentre(new Point2D(prisme.xCentre(),y_c));}

    private void prendreEnComptePositionEtOrientation(PositionEtOrientation nouvelle_pos_et_or) {
        spinner_xcentre.getValueFactory().valueProperty().set(nouvelle_pos_et_or.position().getX());
        spinner_ycentre.getValueFactory().valueProperty().set(nouvelle_pos_et_or.position().getY());
        spinner_orientation.getValueFactory().valueProperty().set(nouvelle_pos_et_or.orientation_deg());
        slider_orientation.valueProperty().set(nouvelle_pos_et_or.orientation_deg());
    }


}
