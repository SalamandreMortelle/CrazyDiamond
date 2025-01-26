package CrazyDiamond.Controller;

import CrazyDiamond.Model.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.*;
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
    public VBox vbox_panneau_racine;

    @FXML
    private VBox baseElementIdentifie;
    @FXML
    private PanneauElementIdentifie baseElementIdentifieController;

    // Panneau qui n'est pas dans la vue (pas dans le .fxml) mais qu'on ajoutera à la place d'autres éléments si ce SOC
    // devient un élément d'un SOC parent
//    private Parent panneau_positionnement_element_dans_soc;

    @FXML
    private HBox hbox_positionnement_relatif_dans_soc;
    @FXML
    // NB : ne pas changer le nom de ce contrôleur : il est construit (injecté) par le FXML loader (lorsqu'il rencontre
    // un <fx:include>) en concaténant "Controller" au nom (fx:id) de l'élément (vue) associé.
    private PanneauPositionnementElementDansSOC hbox_positionnement_relatif_dans_socController;

    @FXML
    private VBox vbox_positionnement_absolu;

    @FXML
    private HBox hbox_x_origine;
    @FXML
    private Spinner<Double> spinner_xorigine ;
    @FXML
    private HBox hbox_y_origine;
    @FXML
    private Spinner<Double> spinner_yorigine ;

    @FXML
    private Label label_orientation;

    @FXML
    private Spinner<Double> spinner_orientation;

//    private Spinner<Double> spinner_position_dans_soc ;

    @FXML
    private Slider slider_orientation;

//    @FXML
//    private Spinner<Double> spinner_position_dans_soc;

    @FXML
    private ColorPicker colorpicker_axe;

    @FXML
    private ListView<ElementDeSOC> listview_obstacles_centres;

    private final ContextMenu menuContextuelObstacleCentre ;

    public PanneauSystemeOptiqueCentre(SystemeOptiqueCentre soc, CanvasAffichageEnvironnement cnv) {
        LOGGER.log(Level.INFO,"Construction du SOC") ;

        if (soc==null)
            throw new IllegalArgumentException("L'objet SystemeOptiqueCentre attaché au PanneauSystemeOptiqueCentre ne peut pas être 'null'") ;

        this.soc = soc;
        this.canvas = cnv ;

        menuContextuelObstacleCentre = new ContextMenu() ;
        MenuItem deleteItemSoc = new MenuItem(rb.getString("supprimer.obstacle_centre"));
        deleteItemSoc.setOnAction(event
                -> new CommandeRetirerElementsDeSystemeOptiqueCentre(soc,listview_obstacles_centres.getSelectionModel().getSelectedItem()).executer());
//        deleteItemSoc.setOnAction(event -> soc.retirerElementPremierNiveau(listview_obstacles_centres.getSelectionModel().getSelectedItem()));
        menuContextuelObstacleCentre.getItems().add(deleteItemSoc);

    }

    public void initialize() {
        LOGGER.log(Level.INFO,"Initialisation du PanneauSystemeOptiqueCentre et de ses liaisons") ;

        baseElementIdentifieController.initialize(soc);


//        try {
//            // On garde une référence vers le Node (panneau) de positionnement dans SOC (pour l'ajouter dans le panneau SOC
//            // à la place des autres champs de positionnement si le SOC est un élémént d'un SOC parent.
//            // NB : Attention le constructeur du controleur suppose que le soc_en_attente_de_creation ait été renseigné
//            // (cf. SetUpDependencyInjector dans PanneauPrincipal)
//            panneau_positionnement_element_dans_soc = DependencyInjection.load("View/PanneauPositionnementElementDansSOC.fxml");
//            LOGGER.log(Level.FINE, "PanneauPositionnementElementDansSOC créé : {0}", panneau_positionnement_element_dans_soc);
//        } catch (IOException e) {
//            System.err.println("Exception lors de l'accès au fichier .fxml : " + e.getMessage());
//            System.exit(1);
//        }

//        PanneauPositionnementElementDansSOC controler = (PanneauPositionnementElementDansSOC) panneau_positionnement_element_dans_soc.getUserData() ;
//        spinner_position_dans_soc = controler.spinnerPositionDansSOC() ;

//        spinner_position_dans_soc = (Spinner<Double>) panneau_positionnement_element_dans_soc.getChildrenUnmodifiable().get(1);

        hbox_positionnement_relatif_dans_socController.initialize(canvas,soc);

        UtilitairesVue.gererAppartenanceSOC(soc,vbox_panneau_racine,vbox_positionnement_absolu, hbox_positionnement_relatif_dans_soc);
//        if (soc.SOCParent()!=null) {
//
//            int pos = vbox_panneau_racine.getChildren().indexOf(vbox_positionnement_absolu) ;
//            vbox_panneau_racine.getChildren().remove(vbox_positionnement_absolu);
//            vbox_panneau_racine.getChildren().add(pos, panneau_positionnement_element_dans_soc);
//
//            // Force le spinner du panneau positionnement à s'initialiser
//            // TODO : voir si c'est vraiment nécessaire
//            soc.definirOrigine(soc.origine());
//        }
//
//        soc.systemeOptiqueParentProperty().addListener( (observableValue, oldValue, newValue) ->{
//            LOGGER.log(Level.FINE, "SOC Parent passe de {0} à {1}", new Object[]{oldValue, newValue});
//
//            if (oldValue==null && newValue!=null) { // Ajout de ce SOC dans un SOC parent
//
//                int pos = vbox_panneau_racine.getChildren().indexOf(vbox_positionnement_absolu) ;
//                vbox_panneau_racine.getChildren().remove(vbox_positionnement_absolu);
//                vbox_panneau_racine.getChildren().add(pos, panneau_positionnement_element_dans_soc);
//
//            } else if (oldValue !=null && newValue==null) { // Retrait de ce SOC d'un SOC Parent
//
//                int pos = vbox_panneau_racine.getChildren().indexOf(panneau_positionnement_element_dans_soc) ;
//                vbox_panneau_racine.getChildren().remove(panneau_positionnement_element_dans_soc);
//                vbox_panneau_racine.getChildren().add(pos,vbox_positionnement_absolu);
//
//            }
//
//        });

//        // Récupération du controleur du panneau de positionnement qui se trouve dans le UserData de la vue (cf. classe
//        // DependyInjection qui renseigne ce UserData lors du chargement du fichier .fxml
//        panneauPositionnementElementDansSOC = (PanneauPositionnementElementDansSOC) panneau_pos_soc_dans_soc.getUserData();

        // Prise en compte automatique de la position et de l'orientation        
        soc.axeObjectProperty().addListener(new ChangeListenerAvecGarde<>(this::prendreEnComptePositionEtOrientation));

        // Position : X origine
        spinner_xorigine.getStyleClass().add(Spinner.STYLE_CLASS_ARROWS_ON_RIGHT_HORIZONTAL) ;
        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_xorigine, soc.XOrigine(), this::definirXOrigineSOC);

        // Position : Y origine
        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_yorigine, soc.YOrigine(), this::definirYOrigineSOC);

        // Orientation
        spinner_orientation.getValueFactory().setWrapAround(true);
        OutilsControleur.integrerSpinnerDoubleValidant(spinner_orientation,soc.orientation(),this::definirOrientation);

        slider_orientation.valueProperty().set(soc.orientation());
        slider_orientation.valueProperty().addListener(new ChangeListenerAvecGarde<>(this::definirOrientation));

        // Couleurs
        colorpicker_axe.valueProperty().set(soc.couleurAxe());
        soc.couleurAxeProperty().addListener(new ChangeListenerAvecGarde<>(colorpicker_axe::setValue));
        colorpicker_axe.valueProperty().addListener((observableValue, c_avant, c_apres)
                -> new CommandeDefinirUnParametre<>(soc, c_apres, soc::couleurAxe, soc::definirCouleurAxe).executer());

        // Liste des obstacles centrés
        listview_obstacles_centres.setItems(soc.elementsCentresRacine());

        ListChangeListener<ElementDeSOC> lcl_el = change -> {
            while (change.next()) {
                if (change.wasRemoved()) {
                    for (ElementDeSOC elc_retire : change.getRemoved()) {
                        LOGGER.log(Level.FINE,"Obstacle centré supprimé : {0}",elc_retire.nom()) ;
                        // Rien à faire en cas de suppression
                    }

                } else if (change.wasAdded()) {
                    for (ElementDeSOC elc_ajoute : change.getAddedSubList()) {
                        LOGGER.log(Level.FINE,"Obstacle centré ajouté : {0}",elc_ajoute.nom()) ;
                        // Rafraichissement automatique de la listview quand le nom de l'obstacle change
                        elc_ajoute.nomProperty().addListener((obs, oldName, newName) -> listview_obstacles_centres.refresh());
                    }
                }

            }
        };


        soc.elementsCentresRacine().addListener(lcl_el);

//        for (Obstacle o : soc.obstacles_centres()) {
//            // Rafraichissement automatique de la liste des obstacles du SOC quand le nom d'un obstacle change
//            ChangeListener<String> listenerNom = (obs, oldName, newName) -> listview_obstacles_centres.refresh();
//            o.nomProperty().addListener(listenerNom);
//        }


//        if (listview_obstacles_centres.getContextMenu()==null)
        listview_obstacles_centres.setContextMenu(menuContextuelObstacleCentre);

//        checkbox_dioptres.selectedProperty().bindBidirectional(soc.MontrerDioptresProperty());
//        checkbox_plans_focaux.selectedProperty().bindBidirectional(soc.MontrerPlansFocauxProperty());
//        checkbox_plans_principaux.selectedProperty().bindBidirectional(soc.MontrerPlansPrincipauxProperty());
//        checkbox_plans_nodaux.selectedProperty().bindBidirectional(soc.MontrerPlansNodauxProperty());

    }

    private void definirOrientation(Number or) {
        new CommandeDefinirUnParametre<>(soc,or.doubleValue(),soc::orientation,soc::definirOrientation).executer();
    }


    public void ajouterObstacle(ActionEvent actionEvent) throws Exception {

        // Afficher dialogue avec la liste des Obstacles ayant une symétrie de révolution
        ButtonType okButtonType = new ButtonType(rb.getString("bouton.dialogue.soc.ok"), ButtonBar.ButtonData.OK_DONE);
        ButtonType annulerButtonType = new ButtonType(rb.getString("bouton.dialogue.soc.annuler"), ButtonBar.ButtonData.CANCEL_CLOSE);
        Dialog<ArrayList<ElementDeSOC>> boite_dialogue = new Dialog<>();

        boite_dialogue.setTitle(rb.getString("titre.dialogue.soc"));
        boite_dialogue.setHeaderText(rb.getString("invite.dialogue.soc"));

        ObservableList<ElementDeSOC> elts_a_proposer = FXCollections.observableArrayList();

        // Seuls les obstacles de premier niveau sont proposés dans la modale d'ajout
        Iterator<Obstacle> ito = canvas.environnement().iterateur_obstacles_premier_niveau();
        while (ito.hasNext()) {
            Obstacle o = ito.next();
            // Vérifier si l'obstacle o a une symétrie de révolution (requis pour faire partie d'un SOC) et s'il n'est pas déjà dans un SOC
//            if (o.aSymetrieDeRevolution() && !soc.comprend(o) && canvas.environnement().systemeOptiqueCentrePremierNiveauContenant(o) == null)
            if (o.aSymetrieDeRevolution() && o.SOCParent()==null) // A voir : on pourrait aussi proposer l'ajout d'obstacle qui ont un SOC parent différent de soc
                elts_a_proposer.add(o);
        }

        // TODO : ne pas proposer des SOC qui font partie des ancêtres
        List<SystemeOptiqueCentre> ancetres = soc.ancetres() ;
        canvas.environnement().systemesOptiquesCentres().stream()
                .filter(s -> (!ancetres.contains(s) && s!=soc && s.SOCParent()!=soc))
                .forEach(elts_a_proposer::add);

//        elts_a_proposer.addAll(canvas.environnement().systemesOptiquesCentres());
        ListView<ElementDeSOC> lo = new ListView<>(elts_a_proposer);


        // TODO Limiter la composition à deux objets : proposer deux listview en sélection SINGLE côte à côte (mais
        // interdire de choisir le même objet dans les deux listes... : retirer de la 2ème l'objet sélectionné dans
        // la première, et le remettre si il n'est plus sélectionné dans la première... Mais quid si on sélectionne
        // d'abord dans la 2eme liste, avant la première ??

        ScrollPane sp = new ScrollPane(lo);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        lo.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        boite_dialogue.getDialogPane().setContent(lo);

        boite_dialogue.setResultConverter(buttonType -> {
            if (buttonType == okButtonType)
                return new ArrayList<>(lo.getSelectionModel().getSelectedItems());

            return null;
        });

        boite_dialogue.getDialogPane().getButtonTypes().add(okButtonType);
        boite_dialogue.getDialogPane().getButtonTypes().add(annulerButtonType);

        Optional<ArrayList<ElementDeSOC>> op_elts_choisis = boite_dialogue.showAndWait();
        if (op_elts_choisis.isPresent()) {

            ArrayList<ElementDeSOC> elements_choisis = op_elts_choisis.get();

            LOGGER.log(Level.INFO, "Obstacles choisis pour SOC : {0}", elements_choisis);

            new CommandeAjouterElementsDansSystemeOptiqueCentre(canvas.environnement(),soc, elements_choisis).executer();
        }
    }
            //            for(Obstacle o : obstacles_choisis) {
//                soc.ajouterObstacleALaRacine(o);
//            }

//            for(Obstacle o : obstacles_choisis) {
////                o.integrerDansSystemeOptiqueCentre(soc);
////                integrerObstacle(o);
//
//                    soc.ajouterObstacleALaRacine(o) ;
//
////                    // Rafraichissement automatique de la liste des obstacles du SOC quand le nom de l'obstacle ajouté change
////                    ChangeListener<String> listenerNom = (obs, oldName, newName) -> listview_obstacles_centres.refresh();
////                    o.nomProperty().addListener(listenerNom);
//
////                environnement.supprimerObstacleALaRacine(o);
////                compo.ajouterObstacleALaRacine(o);
//            }
//
////            environnement.ajouterObstacleALaRacine(compo);
////        }
//
//    }

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

    private void definirXOrigineSOC(Double x_o) {
        new CommandeDefinirUnParametrePoint<>(soc,new Point2D(x_o,soc.origine().getY()),soc::origine,soc::definirOrigine).executer();
    }
    private void definirYOrigineSOC(Double y_o) {
        new CommandeDefinirUnParametrePoint<>(soc,new Point2D(soc.origine().getX(), y_o),soc::origine,soc::definirOrigine).executer();
    }

    private void prendreEnComptePositionEtOrientation(PositionEtOrientation nouvelle_pos_et_or) {
        spinner_xorigine.getValueFactory().valueProperty().set(nouvelle_pos_et_or.position().getX());
        spinner_yorigine.getValueFactory().valueProperty().set(nouvelle_pos_et_or.position().getY());
        spinner_orientation.getValueFactory().valueProperty().set(nouvelle_pos_et_or.orientation_deg());
        slider_orientation.valueProperty().set(nouvelle_pos_et_or.orientation_deg());
    }



}
