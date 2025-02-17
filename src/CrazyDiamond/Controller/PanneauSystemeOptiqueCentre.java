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

    @FXML
    private Slider slider_orientation;

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

        menuContextuelObstacleCentre.getItems().add(deleteItemSoc);

    }

    public void initialize() {
        LOGGER.log(Level.INFO,"Initialisation du PanneauSystemeOptiqueCentre et de ses liaisons") ;

        baseElementIdentifieController.initialize(soc);

        hbox_positionnement_relatif_dans_socController.initialize(canvas,soc);

        UtilitairesVue.gererAppartenanceSOC(soc,vbox_panneau_racine,vbox_positionnement_absolu, hbox_positionnement_relatif_dans_soc);

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

        listview_obstacles_centres.setContextMenu(menuContextuelObstacleCentre);

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
