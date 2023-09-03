package CrazyDiamond.Controller;

import CrazyDiamond.Model.ChangeListenerAvecGarde;
import CrazyDiamond.Model.PositionEtOrientation;
import CrazyDiamond.Model.Source;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.logging.Level;
import java.util.logging.Logger;

public class PanneauSource {

    // Modèle
    Source source ;
    CanvasAffichageEnvironnement canvas;

    // Récupération du logger
    private static final Logger LOGGER = Logger.getLogger( "CrazyDiamond" );

    @FXML
    private VBox baseElementIdentifie;
    @FXML
    private PanneauElementIdentifie baseElementIdentifieController ;

    @FXML
    private Spinner<Double> spinner_x ;
    // La déclaration de cet attribut est requise pour faire un binding "persistant" entre la variable numérique du modèle
    // et l'ObjectProperty<Double> à l'intérieur du Spinner Value Factory qui encapsule la valueProperty du Spinner. Il
    // créé une StrongRef qui permet de s'assurer qu'il n'y aura pas de garbage collection intempestif de cet ObjectProperty.
    // Cette obligation vient de la Property du Spinner Value Factory qui est de type ObjectProperty<Double> (ou Integer...)
    // et non de type DoubleProperty comme la Property du modèle, qu'il faut donc convertir avec la méthode asObject et stocker
    // en tant que tel, pour pouvoir réaliser le binding.
    private ObjectProperty<Double> position_x_value_property ;

    @FXML
    private Spinner<Double> spinner_y ;
    private ObjectProperty<Double> position_y_value_property ; // Attribut requis (cf. supra)

    @FXML
    private Spinner<Double> spinner_orientation ;
    private ObjectProperty<Double> orientation_value_property ; // Attribut requis (cf. supra)

    @FXML
    private Slider slider_orientation ;

    @FXML
    private Spinner<Integer> spinner_nombre_rayons ;
    private ObjectProperty<Integer> nombre_rayons_value_property ; // Attribut requis (cf. supra)

    @FXML
    private ToggleGroup choix_type_source ;

    @FXML
    private Toggle choix_pinceau ;

    @FXML
    private Toggle choix_projecteur ;

    @FXML
    private Label label_pinceau ;

    @FXML
    private Spinner<Double> spinner_ouverture_pinceau ;
    private ObjectProperty<Double> ouverture_pinceau_value_property ;

    @FXML
    private Slider slider_ouverture_pinceau ;

    @FXML
    private Label label_projecteur ;

    @FXML
    private Spinner<Double> spinner_largeur_projecteur ;
    private ObjectProperty<Double> largeur_projecteur_value_property ;

    @FXML
    private Spinner<Integer> spinner_nombre_reflexions ;
    private ObjectProperty<Integer> nombre_reflexions_value_property ;

    @FXML
    private ColorPicker colorpicker;

    @FXML
    public CheckBox checkbox_polarisation;
    @FXML
    public Label label_orientation_champ_electrique;

    @FXML
    public Spinner<Double> spinner_orientation_champ_electrique;
    private ObjectProperty<Double> orientation_champ_electrique_value_property ; // Attribut requis (cf. supra)
    @FXML
    public Slider slider_orientation_champ_electrique;


    public PanneauSource(Source source , CanvasAffichageEnvironnement cnv) {
        LOGGER.log(Level.INFO,"Construction du PanneauSource") ;

        if (source==null)
            throw new IllegalArgumentException("L'objet Source attaché au PanneauSource ne peut pas être 'null'") ;

        this.source = source;
        this.canvas = cnv ;
    }

    public void initialize() {

        LOGGER.log(Level.INFO,"Initialisation du PanneauSource et de ses liaisons") ;

        baseElementIdentifieController.initialize(source);

        // Lier cette Vue à son Modèle...

//        slider_largeur_projecteur.setMax(Math.max(source.environnement().largeur(),source.environnement().hauteur()));
//        slider_largeur_projecteur.setMajorTickUnit(slider_largeur_projecteur.getMax()/10);

        // Position
        source.positionEtOrientationObjectProperty().addListener(new ChangeListenerAvecGarde<PositionEtOrientation>(this::prendreEnComptePositionEtOrientation));

        // Conserver la property du modèle (convertie en ObjectPropery<Double>) dans une variable membre pour garder
        // une reference forte et éviter que le GC ne supprime le binding de façon intempestive. Idem avec tous les autres spinners...
        spinner_x.getStyleClass().add(Spinner.STYLE_CLASS_ARROWS_ON_RIGHT_HORIZONTAL) ;
        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_x, source.xPosition(), this::definirXPositionSource);
//        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_x,source.position().getX());
//        position_x_value_property = source.positionXProperty().asObject() ;
//        spinner_x.getValueFactory().valueProperty().bindBidirectional(position_x_value_property);


//        canvas.ajustePasEtAffichageSpinnerValueFactoryDistance((SpinnerValueFactory.DoubleSpinnerValueFactory) spinner_x.getValueFactory());

        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_y, source.yPosition(), this::definirYPositionSource);
//        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_y,source.position().getY());
//        position_y_value_property = source.positionYProperty().asObject() ;
//        spinner_y.getValueFactory().valueProperty().bindBidirectional(position_y_value_property);

//        canvas.ajustePasEtAffichageSpinnerValueFactoryDistance((SpinnerValueFactory.DoubleSpinnerValueFactory) spinner_y.getValueFactory());

        // Orientation
        spinner_orientation.getValueFactory().setWrapAround(true);

        OutilsControleur.integrerSpinnerDoubleValidant(spinner_orientation,source.orientation(),source::definirOrientation);
//        OutilsControleur.integrerSpinnerDoubleValidant(spinner_orientation,source.orientation());
//        orientation_value_property = source.orientationProperty().asObject() ;
//        spinner_orientation.getValueFactory().valueProperty().bindBidirectional(orientation_value_property);


//        slider_orientation.valueProperty().bindBidirectional(source.orientationProperty());
        slider_orientation.valueProperty().addListener(new ChangeListener<>() {
            private boolean changement_en_cours = false ; ;

            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number old_value, Number new_value) {
                if (!changement_en_cours) {
                    try {
                        changement_en_cours = true ;
                        source.definirOrientation(new_value.doubleValue());
                    } finally {
                        changement_en_cours = false ;
                    }
                }
            }
        });


        // Lumière polarisée
        checkbox_polarisation.selectedProperty().bindBidirectional(source.lumierePolariseeProperty());

        // Orientation du champ electrique (si la lumière est polarisée)
        label_orientation_champ_electrique.disableProperty().bind(source.lumierePolariseeProperty().not());
        spinner_orientation_champ_electrique.getValueFactory().setWrapAround(true);
        spinner_orientation_champ_electrique.disableProperty().bind(source.lumierePolariseeProperty().not()) ;

        orientation_champ_electrique_value_property = source.angleChampElectriqueProperty().asObject() ;
        spinner_orientation_champ_electrique.getValueFactory().valueProperty().bindBidirectional(orientation_champ_electrique_value_property);
        OutilsControleur.integrerSpinnerDoubleValidant(spinner_orientation_champ_electrique,source.angleChampElectrique());

        slider_orientation_champ_electrique.valueProperty().bindBidirectional(source.angleChampElectriqueProperty());
        slider_orientation_champ_electrique.disableProperty().bind(source.lumierePolariseeProperty().not()) ;


        // Nb. rayons
        nombre_rayons_value_property = source.nombreRayonsProperty().asObject() ;
        spinner_nombre_rayons.getValueFactory().valueProperty().bindBidirectional(nombre_rayons_value_property);
        OutilsControleur.integrerSpinnerEntierValidant(spinner_nombre_rayons,source.nombreRayons());

        // Ouverture pinceau
        ouverture_pinceau_value_property = source.ouverturePinceauProperty().asObject() ;
        spinner_ouverture_pinceau.getValueFactory().valueProperty().bindBidirectional(ouverture_pinceau_value_property);
        OutilsControleur.integrerSpinnerDoubleValidant(spinner_ouverture_pinceau,source.ouverturePinceau());
        slider_ouverture_pinceau.valueProperty().bindBidirectional(source.ouverturePinceauProperty());

        // Largeur projecteur
        largeur_projecteur_value_property = source.largeurProjecteurProperty().asObject() ;
        spinner_largeur_projecteur.getValueFactory().valueProperty().bindBidirectional(largeur_projecteur_value_property);
        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_largeur_projecteur, source.largeurProjecteur());
//        canvas.ajustePasEtAffichageSpinnerValueFactoryDistance((SpinnerValueFactory.DoubleSpinnerValueFactory) spinner_largeur_projecteur.getValueFactory());

        // Nombre reflexions
        nombre_reflexions_value_property = source.nombreMaximumRencontresObstacleProperty().asObject() ;
        spinner_nombre_reflexions.getValueFactory().valueProperty().bindBidirectional(nombre_reflexions_value_property);
        OutilsControleur.integrerSpinnerEntierValidant(spinner_nombre_reflexions,source.nombreMaximumRencontresObstacle());

        // Couleur
        colorpicker.valueProperty().bindBidirectional(source.couleurProperty());

        // Sélection, activation et désactivation automatiques des contrôles pinceau
        label_pinceau.disableProperty().bind(source.typeProperty().isNotEqualTo(Source.TypeSource.PINCEAU)) ;
        spinner_ouverture_pinceau.disableProperty().bind(source.typeProperty().isNotEqualTo(Source.TypeSource.PINCEAU)) ;
        slider_ouverture_pinceau.disableProperty().bind(source.typeProperty().isNotEqualTo(Source.TypeSource.PINCEAU)) ;

        // Sélection, activation et désactivation automatiques des contrôles projecteur
        label_projecteur.disableProperty().bind(source.typeProperty().isNotEqualTo(Source.TypeSource.PROJECTEUR));
        spinner_largeur_projecteur.disableProperty().bind(source.typeProperty().isNotEqualTo(Source.TypeSource.PROJECTEUR));

        if (source.type()== Source.TypeSource.PINCEAU)
            choix_pinceau.setSelected(true);

        if (source.type()== Source.TypeSource.PROJECTEUR)
            choix_projecteur.setSelected(true);

        // Ce listener est mono-directionnel Vue > Modèle (mais l'état initial du toggle pinceau/projecteur est déjà positionné)
        choix_type_source.selectedToggleProperty().addListener((observable, oldValue,newValue) -> {
            LOGGER.log(Level.FINE,"Choix type source passe de {0} à {1}", new Object[] { oldValue,newValue} ) ;

            if (choix_type_source.getSelectedToggle()==choix_pinceau)
                source.definirType(Source.TypeSource.PINCEAU);

            if (choix_type_source.getSelectedToggle()==choix_projecteur)
                source.definirType(Source.TypeSource.PROJECTEUR);

        });

        source.typeProperty().addListener(((observableValue, oldValue, newValue) -> {
            LOGGER.log(Level.FINE,"Type source passe de {0} à {1}", new Object[] { oldValue,newValue} ) ;

            if (newValue== Source.TypeSource.PINCEAU && choix_type_source.getSelectedToggle()!=choix_pinceau)
                choix_type_source.selectToggle(choix_pinceau);

            if (newValue== Source.TypeSource.PROJECTEUR && choix_type_source.getSelectedToggle()!=choix_projecteur)
                choix_type_source.selectToggle(choix_projecteur);
        }));

    }

    private void definirXPositionSource(Double x_c) {source.definirPosition(new Point2D(x_c,source.position().getY()));}
    private void definirYPositionSource(Double y_c) {source.definirPosition(new Point2D(source.position().getX(),y_c));}

    private void prendreEnComptePositionEtOrientation(PositionEtOrientation nouvelle_pos_et_or) {
        spinner_x.getValueFactory().valueProperty().set(nouvelle_pos_et_or.position().getX());
        spinner_y.getValueFactory().valueProperty().set(nouvelle_pos_et_or.position().getY());
        spinner_orientation.getValueFactory().valueProperty().set(nouvelle_pos_et_or.orientation_deg());
        slider_orientation.valueProperty().set(nouvelle_pos_et_or.orientation_deg());
    }


}
