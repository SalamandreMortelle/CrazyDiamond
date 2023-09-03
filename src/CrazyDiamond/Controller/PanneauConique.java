package CrazyDiamond.Controller;

import CrazyDiamond.Model.ChangeListenerAvecGarde;
import CrazyDiamond.Model.Conique;
import CrazyDiamond.Model.PositionEtOrientation;
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

        conique.positionEtOrientationObjectProperty().addListener(new ChangeListenerAvecGarde<PositionEtOrientation>(this::prendreEnComptePositionEtOrientation));

//        conique.positionEtOrientationObjectProperty().addListener(new ChangeListener<PositionEtOrientation>() {
//            private boolean changement_en_cours = false ;
//            @Override
//            public void changed(ObservableValue<? extends PositionEtOrientation> observableValue, PositionEtOrientation old_value, PositionEtOrientation new_value) {
//                if (!changement_en_cours) {
//                    try {
//                        changement_en_cours = true;
//                        spinner_xfoyer.getValueFactory().valueProperty().set(new_value.position().getX());
//                        spinner_yfoyer.getValueFactory().valueProperty().set(new_value.position().getY());
//                        spinner_orientation.getValueFactory().valueProperty().set(new_value.orientation_deg());
//                        slider_orientation.valueProperty().set(new_value.orientation_deg());
//                    } finally {
//                        changement_en_cours = false ;
//                    }
//                }
//            }
//        });




//        xfoyer_object_property = conique.xFoyerProperty().asObject() ;
//        spinner_xfoyer.getValueFactory().valueProperty().bindBidirectional(xfoyer_object_property);

        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_xfoyer, conique.xFoyer(), this::definirXFoyerConique);
        spinner_xfoyer.getStyleClass().add(Spinner.STYLE_CLASS_ARROWS_ON_RIGHT_HORIZONTAL) ;

//        spinner_xfoyer.getValueFactory().valueProperty().set(conique.xFoyer());
//        spinner_xfoyer.getValueFactory().valueProperty().addListener(new ChangeListener<Double>() {
//            private boolean changement_en_cours = false ; ;
//
//            @Override
//            public void changed(ObservableValue<? extends Double> observableValue, Double old_value, Double new_value) {
//                if (!changement_en_cours) {
//                    try {
//                        changement_en_cours = true ;
//                        conique.definirFoyer(new Point2D(new_value,conique.yFoyer()));
//                    } finally {
//                        changement_en_cours = false ;
//                    }
//                }
//            }
//        });
//
//
//        spinner_xfoyer.getStyleClass().add(Spinner.STYLE_CLASS_ARROWS_ON_RIGHT_HORIZONTAL) ;
//
//        canvas.ajustePasEtAffichageSpinnerValueFactoryDistance((SpinnerValueFactory.DoubleSpinnerValueFactory) spinner_xfoyer.getValueFactory());

        spinner_xfoyer.editableProperty().bind(conique.appartenanceSystemeOptiqueProperty().not()) ;
        spinner_xfoyer.disableProperty().bind(conique.appartenanceSystemeOptiqueProperty()) ;


//        yfoyer_object_property = conique.yFoyerProperty().asObject() ;
//        spinner_yfoyer.getValueFactory().valueProperty().bindBidirectional(yfoyer_object_property);
        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_yfoyer, conique.xFoyer(), this::definirYFoyerConique);

//        spinner_yfoyer.getValueFactory().valueProperty().set(conique.yFoyer());
//        spinner_yfoyer.getValueFactory().valueProperty().addListener(new ChangeListener<Double>() {
//            private boolean changement_en_cours = false ; ;
//
//            @Override
//            public void changed(ObservableValue<? extends Double> observableValue, Double old_value, Double new_value) {
//                if (!changement_en_cours) {
//                    try {
//                        changement_en_cours = true ;
//                        conique.definirFoyer(new Point2D(conique.xFoyer(),new_value));
//                    } finally {
//                        changement_en_cours = false ;
//                    }
//                }
//            }
//        });
//
//        canvas.ajustePasEtAffichageSpinnerValueFactoryDistance((SpinnerValueFactory.DoubleSpinnerValueFactory) spinner_yfoyer.getValueFactory());

        spinner_yfoyer.editableProperty().bind(conique.appartenanceSystemeOptiqueProperty().not()) ;
        spinner_yfoyer.disableProperty().bind(conique.appartenanceSystemeOptiqueProperty()) ;


        // Orientation
        spinner_orientation.getValueFactory().setWrapAround(true);

        OutilsControleur.integrerSpinnerDoubleValidant(spinner_orientation,conique.orientation(),conique::definirOrientation);

//        orientation_object_property = conique.orientationProperty().asObject() ;
//        spinner_orientation.getValueFactory().valueProperty().bindBidirectional(orientation_object_property);
//        spinner_orientation.getValueFactory().valueProperty().set(conique.orientation());
//        spinner_orientation.getValueFactory().valueProperty().addListener(new ChangeListener<Double>() {
//            private boolean changement_en_cours = false ; ;
//
//            @Override
//            public void changed(ObservableValue<? extends Double> observableValue, Double old_value, Double new_value) {
//                if (!changement_en_cours) {
//                    try {
//                        changement_en_cours = true ;
//                        conique.definirOrientation(new_value);
//                    } finally {
//                        changement_en_cours = false ;
//                    }
//                }
//            }
//        });

        spinner_orientation.editableProperty().bind(conique.appartenanceSystemeOptiqueProperty().not()) ;
        spinner_orientation.disableProperty().bind(conique.appartenanceSystemeOptiqueProperty()) ;

//        slider_orientation.valueProperty().bindBidirectional(conique.orientationProperty());
        slider_orientation.valueProperty().set(conique.orientation());
        slider_orientation.valueProperty().addListener(new ChangeListener<>() {
            private boolean changement_en_cours = false ; ;

            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number old_value, Number new_value) {
                if (!changement_en_cours) {
                    try {
                        changement_en_cours = true ;
                        conique.definirOrientation(new_value.doubleValue());
                    } finally {
                        changement_en_cours = false ;
                    }
                }
            }
        });

        slider_orientation.disableProperty().bind(conique.appartenanceSystemeOptiqueProperty()) ;

        // Paramètre
        parametre_object_property = conique.parametreProperty().asObject() ;
        spinner_parametre.getValueFactory().valueProperty().bindBidirectional(parametre_object_property);

        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_parametre, conique.parametre());
//        canvas.ajustePasEtAffichageSpinnerValueFactoryDistance((SpinnerValueFactory.DoubleSpinnerValueFactory) spinner_parametre.getValueFactory());

        excentricite_object_property = conique.excentriciteProperty().asObject() ;
        spinner_excentricite.getValueFactory().valueProperty().bindBidirectional(excentricite_object_property);

        OutilsControleur.integrerSpinnerDoubleValidant(spinner_excentricite, conique.excentricite(), conique::definirExcentricite);

    }

    private void definirXFoyerConique(Double x_f) {conique.definirFoyer(new Point2D(x_f,conique.yFoyer()));}
    private void definirYFoyerConique(Double y_f) {conique.definirFoyer(new Point2D(conique.xFoyer(),y_f));}

    private void prendreEnComptePositionEtOrientation(PositionEtOrientation nouvelle_pos_et_or) {
        spinner_xfoyer.getValueFactory().valueProperty().set(nouvelle_pos_et_or.position().getX());
        spinner_yfoyer.getValueFactory().valueProperty().set(nouvelle_pos_et_or.position().getY());
        spinner_orientation.getValueFactory().valueProperty().set(nouvelle_pos_et_or.orientation_deg());
        slider_orientation.valueProperty().set(nouvelle_pos_et_or.orientation_deg());
    }
}
