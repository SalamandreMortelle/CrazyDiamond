package CrazyDiamond.Controller;

import CrazyDiamond.Model.ChangeListenerAvecGarde;
import CrazyDiamond.Model.Obstacle;
import CrazyDiamond.Model.PositionEtOrientation;
import CrazyDiamond.Model.SystemeOptiqueCentre;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PanneauSystemeOptiqueCentre {


    // Modèle
    SystemeOptiqueCentre soc ;

    CanvasAffichageEnvironnement canvas;

    // Récupération du logger
    private static final Logger LOGGER = Logger.getLogger( "CrazyDiamond" );
    private static final ResourceBundle rb = ResourceBundle.getBundle("CrazyDiamond") ;

    @FXML
    private VBox baseElementIdentifie;
    @FXML
    private PanneauElementIdentifie baseElementIdentifieController ;

    @FXML
    private Spinner<Double> spinner_xorigine ;
    // La déclaration de cet attribut est requise pour faire un binding "persistant" entre la variable numérique du modèle
    // et l'ObjectProperty<Double> à l'intérieur du Spinner Value Factory qui encapsule la valueProperty du Spinner. Il
    // créé une StrongRef qui permet de s'assurer qu'il n'y aura pas de garbage collection intempestif de cet ObjectProperty.
    // Cette obligation vient de la Property du Spinner Value Factory qui est de type ObjectProperty<Double> (ou Integer...)
    // et non de type DoubleProperty comme la Property du modèle, qu'il faut donc convertir avec la méthode asObject et stocker
    // en tant que tel, pour pouvoir réaliser le binding.
    private ObjectProperty<Double> soc_xorigine_object_property;

    @FXML
    private Spinner<Double> spinner_yorigine ;
    private ObjectProperty<Double> soc_yorigine_object_property; // Attribut requis (cf. supra)

    @FXML
    private Spinner<Double> spinner_orientation;
    private ObjectProperty<Double> orientation_object_property;

    @FXML
    private Slider slider_orientation;

    @FXML
    private ColorPicker colorpicker_axe;

    @FXML
    private ListView<Obstacle> listview_obstacles_centres;

    private final ContextMenu menuContextuelObstacleCentre ;

//    public CheckBox checkbox_dioptres;
//    public CheckBox checkbox_plans_focaux;
//    public CheckBox checkbox_plans_principaux;
//    public CheckBox checkbox_plans_nodaux;


    public PanneauSystemeOptiqueCentre(SystemeOptiqueCentre soc, CanvasAffichageEnvironnement cnv) {
        LOGGER.log(Level.INFO,"Construction du SOC") ;

        if (soc==null)
            throw new IllegalArgumentException("L'objet SystemeOptiqueCentre attaché au PanneauSystemeOptiqueCentre ne peut pas être 'null'") ;

        this.soc = soc;
        this.canvas = cnv ;

        menuContextuelObstacleCentre = new ContextMenu() ;
        MenuItem deleteItemSoc = new MenuItem(rb.getString("supprimer.obstacle_centre"));
        deleteItemSoc.setOnAction(event -> soc.retirerObstacleCentre(listview_obstacles_centres.getSelectionModel().getSelectedItem()));
        menuContextuelObstacleCentre.getItems().add(deleteItemSoc);

    }

    public void initialize() {
        LOGGER.log(Level.INFO,"Initialisation du PanneauSystemeOptiqueCentre et de ses liaisons") ;

        baseElementIdentifieController.initialize(soc);

        // Position
        soc.axeObjectProperty().addListener(new ChangeListenerAvecGarde<>(this::prendreEnComptePositionEtOrientation));
//        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_xorigine,soc.origine().getX());
        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_xorigine, soc.XOrigine(), this::definirXOrigineSOC);
//        soc_xorigine_object_property = soc.XOrigineProperty().asObject() ;
//        spinner_xorigine.getValueFactory().valueProperty().bindBidirectional(soc_xorigine_object_property);

        spinner_xorigine.getStyleClass().add(Spinner.STYLE_CLASS_ARROWS_ON_RIGHT_HORIZONTAL) ;

//        canvas.ajustePasEtAffichageSpinnerValueFactoryDistance((SpinnerValueFactory.DoubleSpinnerValueFactory) spinner_xorigine.getValueFactory());

//        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_yorigine,soc.origine().getY());
        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_yorigine, soc.YOrigine(), this::definirYOrigineSOC);
//        soc_yorigine_object_property = soc.YOrigineProperty().asObject() ;
//        spinner_yorigine.getValueFactory().valueProperty().bindBidirectional(soc_yorigine_object_property);

//        canvas.ajustePasEtAffichageSpinnerValueFactoryDistance((SpinnerValueFactory.DoubleSpinnerValueFactory) spinner_yorigine.getValueFactory());

        // Orientation
        spinner_orientation.getValueFactory().setWrapAround(true);

        OutilsControleur.integrerSpinnerDoubleValidant(spinner_orientation,soc.orientation(),soc::definirOrientation);
//        OutilsControleur.integrerSpinnerDoubleValidant(spinner_orientation,soc.orientation());
//        orientation_object_property = soc.orientationProperty().asObject() ;
//        spinner_orientation.getValueFactory().valueProperty().bindBidirectional(orientation_object_property);

//        slider_orientation.valueProperty().bindBidirectional(soc.orientationProperty());
        slider_orientation.valueProperty().addListener(new ChangeListener<>() {
            private boolean changement_en_cours = false ;

            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number old_value, Number new_value) {
                if (!changement_en_cours) {
                    try {
                        changement_en_cours = true ;
                        soc.definirOrientation(new_value.doubleValue());
                    } finally {
                        changement_en_cours = false ;
                    }
                }
            }
        });


        // Couleurs
        colorpicker_axe.valueProperty().bindBidirectional( soc.couleurAxeProperty() );

        // Liste des obstacles centrés
        listview_obstacles_centres.setItems(soc.obstacles_centres());

        for (Obstacle o : soc.obstacles_centres()) {
            // Rafraichissement automatique de la liste des obstacles du SOC quand le nom d'un obstacle change
            ChangeListener<String> listenerNom = (obs, oldName, newName) -> listview_obstacles_centres.refresh();
            o.nomProperty().addListener(listenerNom);
        }


//        if (listview_obstacles_centres.getContextMenu()==null)
        listview_obstacles_centres.setContextMenu(menuContextuelObstacleCentre);

//        checkbox_dioptres.selectedProperty().bindBidirectional(soc.MontrerDioptresProperty());
//        checkbox_plans_focaux.selectedProperty().bindBidirectional(soc.MontrerPlansFocauxProperty());
//        checkbox_plans_principaux.selectedProperty().bindBidirectional(soc.MontrerPlansPrincipauxProperty());
//        checkbox_plans_nodaux.selectedProperty().bindBidirectional(soc.MontrerPlansNodauxProperty());

    }

    public void ajouterObstacle(ActionEvent actionEvent) throws Exception {
        // Afficher dialogue avec la liste des Obstacles ayant une symétrie de révolution

        ButtonType okButtonType = new ButtonType(rb.getString("bouton.dialogue.soc.ok"), ButtonBar.ButtonData.OK_DONE);
        ButtonType annulerButtonType = new ButtonType(rb.getString("bouton.dialogue.soc.annuler"), ButtonBar.ButtonData.CANCEL_CLOSE);
        Dialog<ArrayList<Obstacle>> boite_dialogue = new Dialog<>() ;

        boite_dialogue.setTitle(rb.getString("titre.dialogue.composition"));
        boite_dialogue.setHeaderText(rb.getString("invite.dialogue.composition"));

        ObservableList<Obstacle> obstacles_a_proposer =  FXCollections.observableArrayList();

        Iterator<Obstacle> ito =  canvas.environnement().iterateur_obstacles() ;
        while (ito.hasNext()) {
            Obstacle o = ito.next() ;
            // Rechercher si l'obstacle o implémente l'interface ElementAvecMatiere car eux seuls peuvent faire partie d'une composition
            if ( o.aSymetrieDeRevolution() && !soc.comprend(o) && canvas.environnement().systemeOptiqueCentreContenant(o)==null )
                obstacles_a_proposer.add( o ) ;
        }

        ListView<Obstacle> lo = new ListView<>(obstacles_a_proposer) ;

        // TODO Limiter la composition à deux objets : proposer deux listview en sélection SINGLE côte à côte (mais
        // interdire de choisir le même objet dans les deux listes... : retirer de la 2ème l'objet sélectionné dans
        // la première, et le remettre si il n'est plus sélectionné dans la première... Mais quid si on sélectionne
        // d'abord dans la 2eme liste, avant la première ??

        ScrollPane sp = new ScrollPane(lo) ;
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        lo.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        boite_dialogue.getDialogPane().setContent(lo);

        boite_dialogue.setResultConverter( buttonType -> {
            if (buttonType == okButtonType)
                return new ArrayList<>(lo.getSelectionModel().getSelectedItems()) ;

            return null ;
        });

        boite_dialogue.getDialogPane().getButtonTypes().add(okButtonType);
        boite_dialogue.getDialogPane().getButtonTypes().add(annulerButtonType);

        Optional<ArrayList<Obstacle>> op_obstacles_choisis =  boite_dialogue.showAndWait() ;
        if (op_obstacles_choisis.isPresent()) {

            ArrayList<Obstacle> obstacles_choisis = op_obstacles_choisis.get() ;

            LOGGER.log(Level.INFO,"Obstacles choisis pour SOC : {0}",obstacles_choisis) ;

            for(Obstacle o : obstacles_choisis) {
//                o.integrerDansSystemeOptiqueCentre(soc);
//                integrerObstacle(o);
                    soc.ajouterObstacle(o) ;

                    // Rafraichissement automatique de la liste des obstacles du SOC quand le nom de l'obstacle ajouté change
                    ChangeListener<String> listenerNom = (obs, oldName, newName) -> listview_obstacles_centres.refresh();
                    o.nomProperty().addListener(listenerNom);

//                environnement.retirerObstacle(o);
//                compo.ajouterObstacle(o);
            }

//            environnement.ajouterObstacle(compo);
        }

    }

//    void integrerObstacle(Obstacle o) throws Exception {
//        if (!o.aSymetrieDeRevolution())
//            throw new UnsupportedOperationException("Impossible d'intégrer l'Obstacle "+o+" dans le Système Optique Centré "+this+" car il n'a pas de symétrie de révolution.") ;
//
//        Point2D axe_soc = soc.direction() ;
//        Point2D point_sur_axe_revolution = o.pointSurAxeRevolution().subtract(soc.origine()) ;
//
//        double distance_algebrique_point_sur_axe_revolution_axe_soc = (point_sur_axe_revolution.getX()*axe_soc.getY()-point_sur_axe_revolution.getY()*axe_soc.getX()) ;
//
//        // Peut-être faut-il prendre l'opposé :  à tester...
//        Point2D translation = soc.perpendiculaireDirection().multiply(distance_algebrique_point_sur_axe_revolution_axe_soc) ;
//
//        o.translater(translation);
//
//        if (!o.estOrientable())
//            return ;
//
//        o.definirOrientation(soc.orientation()) ;
//
////        throw new NoSuchMethodException("La méthode integrerDansSystemeOptiqueCentre() n'est pas implémentée par l'Obstacle "+this) ;
//    }

    private void definirXOrigineSOC(Double x_o) {soc.definirPosition(new Point2D(x_o, soc.YOrigine()));}
    private void definirYOrigineSOC(Double y_o) {soc.definirPosition(new Point2D(soc.XOrigine(),y_o));}

    private void prendreEnComptePositionEtOrientation(PositionEtOrientation nouvelle_pos_et_or) {
        spinner_xorigine.getValueFactory().valueProperty().set(nouvelle_pos_et_or.position().getX());
        spinner_yorigine.getValueFactory().valueProperty().set(nouvelle_pos_et_or.position().getY());
        spinner_orientation.getValueFactory().valueProperty().set(nouvelle_pos_et_or.orientation_deg());
        slider_orientation.valueProperty().set(nouvelle_pos_et_or.orientation_deg());
    }


}
