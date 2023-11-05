package CrazyDiamond.Controller;

import CrazyDiamond.Model.ChangeListenerAvecGarde;
import CrazyDiamond.Model.DemiPlan;
import CrazyDiamond.Model.PositionEtOrientation;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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

    @FXML
    private Spinner<Double> spinner_xorigine ;
    // La déclaration de cet attribut est requise pour faire un binding "persistant" entre la variable numérique du modèle
    // et l'ObjectProperty<Double> à l'intérieur du Spinner Value Factory qui encapsule la valueProperty du Spinner. Il
    // créé une StrongRef qui permet de s'assurer qu'il n'y aura pas de garbage collection intempestif de cet ObjectProperty.
    // Cette obligation vient de la Property du Spinner Value Factory qui est de type ObjectProperty<Double> (ou Integer...)
    // et non de type DoubleProperty comme la Property du modèle, qu'il faut donc convertir avec la méthode asObject et stocker
    // en tant que tel, pour pouvoir réaliser le binding.
    private ObjectProperty<Double> demiplan_xorigine_object_property;

    @FXML
    private Spinner<Double> spinner_yorigine ;
    private ObjectProperty<Double> demiplan_yorigine_object_property; // Attribut requis (cf. supra)

    @FXML
    private Spinner<Double> spinner_orientation;
    private ObjectProperty<Double> orientation_object_property;

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

        demi_plan.positionEtOrientationObjectProperty().addListener(new ChangeListenerAvecGarde<PositionEtOrientation>(this::prendreEnComptePositionEtOrientation));

//        demi_plan.axeObjectProperty().addListener(new ChangeListener<PositionEtOrientation>() {
//            private boolean changement_en_cours = false ;
//            @Override
//            public void changed(ObservableValue<? extends PositionEtOrientation> observableValue, PositionEtOrientation old_value, PositionEtOrientation new_value) {
//                if (!changement_en_cours) {
//                    try {
//                        changement_en_cours = true;
//                        spinner_xorigine.getValueFactory().valueProperty().set(new_value.position().getX());
//                        spinner_yorigine.getValueFactory().valueProperty().set(new_value.position().getY());
//                        spinner_orientation.getValueFactory().valueProperty().set(new_value.orientation_deg());
//                        slider_orientation.valueProperty().set(new_value.orientation_deg());
//                    } finally {
//                        changement_en_cours = false ;
//                    }
//                }
//            }
//        });


        // Position
//        demiplan_xorigine_object_property = demi_plan.xOrigineProperty().asObject() ;
//        spinner_xorigine.getValueFactory().valueProperty().bindBidirectional(demiplan_xorigine_object_property);

        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_xorigine, demi_plan.xOrigine(), this::definirXOrigineDemiPlan);

//        spinner_xorigine.getValueFactory().valueProperty().set(demi_plan.xOrigine());
//        spinner_xorigine.getValueFactory().valueProperty().addListener(new ChangeListener<Double>() {
//            private boolean changement_en_cours = false ; ;
//
//            @Override
//            public void changed(ObservableValue<? extends Double> observableValue, Double old_value, Double new_value) {
//                if (!changement_en_cours) {
//                    try {
//                        changement_en_cours = true ;
//                        demi_plan.definirOrigine(new Point2D(new_value,demi_plan.yOrigine()));
//                    } finally {
//                        changement_en_cours = false ;
//                    }
//                }
//            }
//        });


        spinner_xorigine.getStyleClass().add(Spinner.STYLE_CLASS_ARROWS_ON_RIGHT_HORIZONTAL) ;

        spinner_xorigine.editableProperty().bind(demi_plan.appartenanceSystemeOptiqueProperty().not()) ;
        spinner_xorigine.disableProperty().bind(demi_plan.appartenanceSystemeOptiqueProperty()) ;

//        canvas.ajustePasEtAffichageSpinnerValueFactoryDistance((SpinnerValueFactory.DoubleSpinnerValueFactory) spinner_xorigine.getValueFactory());

        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_yorigine, demi_plan.yOrigine(), this::definirYOrigineDemiPlan);
//        demiplan_yorigine_object_property = demi_plan.yOrigineProperty().asObject() ;
//        spinner_yorigine.getValueFactory().valueProperty().bindBidirectional(demiplan_yorigine_object_property);
//        spinner_yorigine.getValueFactory().valueProperty().set(demi_plan.yOrigine());
//        spinner_yorigine.getValueFactory().valueProperty().addListener(new ChangeListener<Double>() {
//            private boolean changement_en_cours = false ; ;
//
//            @Override
//            public void changed(ObservableValue<? extends Double> observableValue, Double old_value, Double new_value) {
//                if (!changement_en_cours) {
//                    try {
//                        changement_en_cours = true ;
//                        demi_plan.definirOrigine(new Point2D(demi_plan.xOrigine(),new_value));
//                    } finally {
//                        changement_en_cours = false ;
//                    }
//                }
//            }
//        });

//        canvas.ajustePasEtAffichageSpinnerValueFactoryDistance((SpinnerValueFactory.DoubleSpinnerValueFactory) spinner_yorigine.getValueFactory());

        spinner_yorigine.editableProperty().bind(demi_plan.appartenanceSystemeOptiqueProperty().not()) ;
        spinner_yorigine.disableProperty().bind(demi_plan.appartenanceSystemeOptiqueProperty()) ;

        // Orientation
        spinner_orientation.getValueFactory().setWrapAround(true);

        OutilsControleur.integrerSpinnerDoubleValidant(spinner_orientation,demi_plan.orientation(),demi_plan::definirOrientation);

//        orientation_object_property = demi_plan.orientationProperty().asObject() ;
//        spinner_orientation.getValueFactory().valueProperty().bindBidirectional(orientation_object_property);
//        spinner_orientation.getValueFactory().valueProperty().set(demi_plan.orientation());
//        spinner_orientation.getValueFactory().valueProperty().addListener(new ChangeListener<Double>() {
//            private boolean changement_en_cours = false ; ;
//
//            @Override
//            public void changed(ObservableValue<? extends Double> observableValue, Double old_value, Double new_value) {
//                if (!changement_en_cours) {
//                    try {
//                        changement_en_cours = true ;
//                        demi_plan.definirOrientation(new_value);
//                    } finally {
//                        changement_en_cours = false ;
//                    }
//                }
//            }
//        });



        spinner_orientation.editableProperty().bind(demi_plan.appartenanceSystemeOptiqueProperty().not()) ;
        spinner_orientation.disableProperty().bind(demi_plan.appartenanceSystemeOptiqueProperty()) ;

//        slider_orientation.valueProperty().bindBidirectional(demi_plan.orientationProperty());
        slider_orientation.valueProperty().set(demi_plan.orientation());
        slider_orientation.valueProperty().addListener(new ChangeListener<>() {
            private boolean changement_en_cours = false ; ;

            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number old_value, Number new_value) {
                if (!changement_en_cours) {
                    try {
                        changement_en_cours = true ;
                        demi_plan.definirOrientation(new_value.doubleValue());
                    } finally {
                        changement_en_cours = false ;
                    }
                }
            }
        });

        slider_orientation.disableProperty().bind(demi_plan.appartenanceSystemeOptiqueProperty()) ;

    }

    private void definirXOrigineDemiPlan(Double x_f) {demi_plan.definirOrigine(new Point2D(x_f,demi_plan.yOrigine()));}
    private void definirYOrigineDemiPlan(Double y_f) {demi_plan.definirOrigine(new Point2D(demi_plan.xOrigine(),y_f));}

    private void prendreEnComptePositionEtOrientation(PositionEtOrientation nouvelle_pos_et_or) {
        spinner_xorigine.getValueFactory().valueProperty().set(nouvelle_pos_et_or.position().getX());
        spinner_yorigine.getValueFactory().valueProperty().set(nouvelle_pos_et_or.position().getY());
        spinner_orientation.getValueFactory().valueProperty().set(nouvelle_pos_et_or.orientation_deg());
        slider_orientation.valueProperty().set(nouvelle_pos_et_or.orientation_deg());
    }


}
