package CrazyDiamond.Controller;

import CrazyDiamond.Model.*;
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

    @FXML
    private Spinner<Double> spinner_y ;

    @FXML
    private Spinner<Double> spinner_orientation ;

    @FXML
    private Slider slider_orientation ;

    @FXML
    private Spinner<Integer> spinner_nombre_rayons ;

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

    @FXML
    private Slider slider_ouverture_pinceau ;

    @FXML
    private Label label_projecteur ;

    @FXML
    private Spinner<Double> spinner_largeur_projecteur ;

    @FXML
    private Spinner<Integer> spinner_nombre_reflexions ;

    @FXML
    private ColorPicker colorpicker;

    @FXML
    public CheckBox checkbox_polarisation;
    @FXML
    public Label label_orientation_champ_electrique;

    @FXML
    public Spinner<Double> spinner_orientation_champ_electrique;
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

        // Prise en compte automatique de la position et de l'orientation
        source.positionEtOrientationObjectProperty().addListener(new ChangeListenerAvecGarde<>(this::prendreEnComptePositionEtOrientation));

        // Position : X centre
        spinner_x.getStyleClass().add(Spinner.STYLE_CLASS_ARROWS_ON_RIGHT_HORIZONTAL) ;
        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_x, source.xPosition(), this::definirXPositionSource);

        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_y, source.yPosition(), this::definirYPositionSource);

        // Orientation
        spinner_orientation.getValueFactory().setWrapAround(true);
        OutilsControleur.integrerSpinnerDoubleValidant(spinner_orientation,source.orientation(),this::definirOrientation);

        slider_orientation.valueProperty().set(source.orientation());
        slider_orientation.valueProperty().addListener(new ChangeListenerAvecGarde<>(this::definirOrientation));

        // Lumière polarisée
        source.lumierePolariseeProperty().addListener(new ChangeListenerAvecGarde<>(this::prendreEnCompteLumierePolarisee));
        checkbox_polarisation.selectedProperty().addListener(new ChangeListenerAvecGarde<>(this::definirLumierePolarisee));

        // Orientation du champ électrique (si la lumière est polarisée)
        label_orientation_champ_electrique.disableProperty().bind(source.lumierePolariseeProperty().not());
        spinner_orientation_champ_electrique.getValueFactory().setWrapAround(true);
        spinner_orientation_champ_electrique.disableProperty().bind(source.lumierePolariseeProperty().not()) ;

        source.angleChampElectriqueProperty().addListener(new ChangeListenerAvecGarde<>(this::prendreEnCompteAngleChampElectrique));
        OutilsControleur.integrerSpinnerDoubleValidant(spinner_orientation_champ_electrique,source.angleChampElectrique(),this::definirAngleChampElectrique);

        slider_orientation_champ_electrique.disableProperty().bind(source.lumierePolariseeProperty().not()) ;
        slider_orientation_champ_electrique.valueProperty().addListener(new ChangeListenerAvecGarde<>(this::definirAngleChampElectrique));

        // Nb. rayons
        source.nombreRayonsProperty().addListener(new ChangeListenerAvecGarde<>(this::prendreEnCompteNombreRayons));
        OutilsControleur.integrerSpinnerEntierValidant(spinner_nombre_rayons,source.nombreRayons(),this::definirNombreRayons);

        // Ouverture pinceau
        source.ouverturePinceauProperty().addListener(new ChangeListenerAvecGarde<>(this::prendreEnCompteOuverturePinceau));
        OutilsControleur.integrerSpinnerDoubleValidant(spinner_ouverture_pinceau,source.ouverturePinceau(),this::definirOuverturePinceau);
        slider_ouverture_pinceau.valueProperty().addListener(new ChangeListenerAvecGarde<>(this::definirOuverturePinceau));

        // Largeur projecteur
        source.largeurProjecteurProperty().addListener(new ChangeListenerAvecGarde<>(this::prendreEnCompteLargeurProjecteur));
        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_largeur_projecteur, source.largeurProjecteur(),this::definirLargeurProjecteur);

        // Nombre reflexions
        source.nombreMaximumRencontresObstacleProperty().addListener(new ChangeListenerAvecGarde<>(this::prendreEnCompteNombreMaximumRencontresObstacle));
        OutilsControleur.integrerSpinnerEntierValidant(spinner_nombre_reflexions,source.nombreMaximumRencontresObstacle(),this::definirNombreMaximumRencontresObstacle);

        // Couleur
        colorpicker.valueProperty().set(source.couleur());
        source.couleurProperty().addListener(new ChangeListenerAvecGarde<>(colorpicker::setValue));
        colorpicker.valueProperty().addListener((observableValue, c_avant, c_apres)
                -> new CommandeDefinirUnParametre<>(source, c_apres, source::couleur, source::definirCouleur).executer());

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

        source.typeProperty().addListener(new ChangeListenerAvecGarde<>(this::prendreEnCompteType));

        choix_type_source.selectedToggleProperty().addListener((observable, oldValue,newValue) -> {
            LOGGER.log(Level.FINE,"Choix type source passe de {0} à {1}", new Object[] { oldValue,newValue} ) ;

            if (choix_type_source.getSelectedToggle()==choix_pinceau)
                new CommandeDefinirUnParametre<>(source, Source.TypeSource.PINCEAU,source::type,source::definirType).executer();

            if (choix_type_source.getSelectedToggle()==choix_projecteur)
                new CommandeDefinirUnParametre<>(source, Source.TypeSource.PROJECTEUR,source::type,source::definirType).executer();
        });

    }

    private void prendreEnCompteType(Source.TypeSource ts) {
        switch (ts) {
            case PINCEAU -> choix_pinceau.setSelected(true);
            case PROJECTEUR ->  choix_projecteur.setSelected(true);
        }
    }

    private void definirNombreMaximumRencontresObstacle(int nb_m) {
        new CommandeDefinirUnParametre<>(source,nb_m,source::nombreMaximumRencontresObstacle,source::definirNombreMaximumRencontresObstacle).executer();
    }

    private void prendreEnCompteNombreMaximumRencontresObstacle(Number n_m) {
        spinner_nombre_reflexions.getValueFactory().valueProperty().set(n_m.intValue()) ;
    }

    private void definirLargeurProjecteur(Number larg) {
        new CommandeDefinirUnParametreDoubleDistance<>(source,larg.doubleValue(),source::largeurProjecteur,source::definirLargeurProjecteur).executer();
    }

    private void prendreEnCompteLargeurProjecteur(Number larg) {
        spinner_largeur_projecteur.getValueFactory().valueProperty().set(larg.doubleValue());
    }

    private void definirOuverturePinceau(Number ouv) {
        new CommandeDefinirUnParametre<>(source,ouv.doubleValue(),source::ouverturePinceau,source::definirOuverturePinceau).executer();
    }

    private void prendreEnCompteOuverturePinceau(Number ouv) {
        spinner_ouverture_pinceau.getValueFactory().valueProperty().set(ouv.doubleValue());
        slider_ouverture_pinceau.valueProperty().set(ouv.doubleValue());
    }

    private void prendreEnCompteNombreRayons(Number nb_r) {
        spinner_nombre_rayons.getValueFactory().valueProperty().set(nb_r.intValue());
    }

    private void definirNombreRayons(int nb_r) {
        new CommandeDefinirUnParametre<>(source,nb_r,source::nombreRayons,source::definirNombreRayons).executer();
    }
    private void definirAngleChampElectrique(Number ang) {
        new CommandeDefinirUnParametre<>(source,ang.doubleValue(),source::angleChampElectrique,source::definirAngleChampElectrique).executer();
    }

    private void prendreEnCompteAngleChampElectrique(Number or) {
        spinner_orientation_champ_electrique.getValueFactory().valueProperty().set(or.doubleValue());
        slider_orientation_champ_electrique.valueProperty().set(or.doubleValue());
    }

    private void definirLumierePolarisee(boolean pol) {
        new CommandeDefinirUnParametre<>(source,pol,source::lumierePolarisee,source::definirLumierePolarisee).executer() ;
    }

    private void prendreEnCompteLumierePolarisee(boolean pol) {
        checkbox_polarisation.setSelected(pol);
    }

    private void definirOrientation(Number or) {
        new CommandeDefinirUnParametre<>(source,or.doubleValue(),source::orientation,source::definirOrientation).executer();
    }

    private void definirXPositionSource(Double x_c) {
        new CommandeDefinirUnParametrePoint<>(source,new Point2D(x_c,source.yPosition()),source::position,source::definirPosition).executer();
    }
    private void definirYPositionSource(Double y_c) {
        new CommandeDefinirUnParametrePoint<>(source,new Point2D(source.xPosition(),y_c),source::position,source::definirPosition).executer();
    }

    private void prendreEnComptePositionEtOrientation(PositionEtOrientation nouvelle_pos_et_or) {
        spinner_x.getValueFactory().valueProperty().set(nouvelle_pos_et_or.position().getX());
        spinner_y.getValueFactory().valueProperty().set(nouvelle_pos_et_or.position().getY());
        spinner_orientation.getValueFactory().valueProperty().set(nouvelle_pos_et_or.orientation_deg());
        slider_orientation.valueProperty().set(nouvelle_pos_et_or.orientation_deg());
    }

}
