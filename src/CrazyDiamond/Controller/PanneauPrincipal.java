package CrazyDiamond.Controller;

import CrazyDiamond.Model.*;
import CrazyDiamond.Serializer.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.cfg.ContextAttributes;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

// Classe du Contrôleur du Panneau principal
public class PanneauPrincipal {

    static private final JsonMapper jsonMapper = new JsonMapper();
    static private final SimpleModule simpleModule = new SimpleModule();
    static private final FileChooser fileChooser = new FileChooser();


    static {

        // Serializers pour la sauvegarde dans un fichier
        simpleModule.addSerializer(CanvasAffichageEnvironnement.class, new AffichageEnvironnementSerializer());

        simpleModule.addSerializer(Environnement.class, new EnvironnementSerializer());

        simpleModule.addSerializer(Imp_Identifiable.class, new Imp_IdentifiableSerializer());
        simpleModule.addSerializer(Imp_Nommable.class, new Imp_NommableSerializer());
        simpleModule.addSerializer(Imp_ElementAvecContour.class, new Imp_ElementAvecContourSerializer());
        simpleModule.addSerializer(Imp_ElementAvecMatiere.class, new Imp_ElementAvecMatiereSerializer());
        simpleModule.addSerializer(Imp_ElementSansEpaisseur.class, new Imp_ElementSansEpaisseurSerializer());

        simpleModule.addSerializer(Cercle.class, new CercleSerializer());
        simpleModule.addSerializer(Conique.class, new ConiqueSerializer());
        simpleModule.addSerializer(DemiPlan.class, new DemiPlanSerializer());
        simpleModule.addSerializer(Prisme.class, new PrismeSerializer());
        simpleModule.addSerializer(Rectangle.class, new RectangleSerializer());
        simpleModule.addSerializer(Segment.class, new SegmentSerializer());
        simpleModule.addSerializer(Composition.class, new CompositionSerializer());

        simpleModule.addSerializer(Source.class, new SourceSerializer());

        simpleModule.addSerializer(SystemeOptiqueCentre.class, new SystemeOptiqueCentreSerializer());

        // Serializer pour l'ajout d'éléments dans le presse-papier
        simpleModule.addSerializer(ElementsSelectionnes.class, new ElementsSelectionnesSerializer());

        // Deserializers, pour le chargement ou l'import depuis un fichier
        simpleModule.addDeserializer(CanvasAffichageEnvironnement.class, new AffichageEnvironnementDeserializer()) ;

        simpleModule.addSerializer(SystemeOptiqueCentre.class, new SystemeOptiqueCentreSerializer());

        simpleModule.addDeserializer(Environnement.class, new EnvironnementDeserializer());

        simpleModule.addDeserializer(Imp_Identifiable.class, new Imp_IdentifiableDeserializer());
        simpleModule.addDeserializer(Imp_Nommable.class, new Imp_NommableDeserializer());
        simpleModule.addDeserializer(Imp_ElementAvecContour.class, new Imp_ElementAvecContourDeserializer());
        simpleModule.addDeserializer(Imp_ElementAvecMatiere.class, new Imp_ElementAvecMatiereDeserializer());
        simpleModule.addDeserializer(Imp_ElementSansEpaisseur.class, new Imp_ElementSansEpaisseurDeserializer());

        simpleModule.addDeserializer(Cercle.class, new CercleDeserializer());
        simpleModule.addDeserializer(Conique.class, new ConiqueDeserializer());
        simpleModule.addDeserializer(DemiPlan.class, new DemiPlanDeserializer());
        simpleModule.addDeserializer(Prisme.class, new PrismeDeserializer());
        simpleModule.addDeserializer(Rectangle.class, new RectangleDeserializer());
        simpleModule.addDeserializer(Segment.class, new SegmentDeserializer());
        simpleModule.addDeserializer(Composition.class, new CompositionDeserializer());

        simpleModule.addDeserializer(Source.class, new SourceDeserializer());

        simpleModule.addDeserializer(SystemeOptiqueCentre.class, new SystemeOptiqueCentreDeserializer());

        // Serializer pour instancier des éléments depuis le presse-papier
        simpleModule.addDeserializer(ElementsSelectionnes.class, new ElementsSelectionnesDeserializer());


        // Enregistrement du module
        jsonMapper.registerModule(simpleModule);

        // only allow .crd files to be selected using chooser
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Crazy Diamond files (*.crd)", "*.crd")
        );
        // set initial directory somewhere user will recognise
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

    }

    public Label label_gauche;
    public Label label_droit;

    private final Environnement environnement ;

    private CanvasAffichageEnvironnement nouveau_canvas_affichage_environnement = null;

    CanvasAffichageEnvironnement canvas_environnement;


    // Récupération du logger
    private static final Logger LOGGER = Logger.getLogger( "CrazyDiamond" );

    private static final ResourceBundle rb = ResourceBundle.getBundle("CrazyDiamond") ;


    protected Outil outil_courant = null ;

    private boolean glisser_en_cours  = false ;
    private boolean glisser_juste_termine = false ;

    ListChangeListener<Source> lcl_sources ;
    ListChangeListener<Obstacle> lcl_obstacles ;

    ListChangeListener<SystemeOptiqueCentre> lcl_socs ;

    Map<Object,Node> map_element_panneau_droit;
    Map<Object,Node> map_element_panneau_bas;

    Node panneau_parametres_environnement;
    Node panneau_parametres_affichage_environnement;

    @FXML
    private VBox panneauParametresEnvironnement;
    @FXML
    private PanneauParametresEnvironnement panneauParametresEnvironnementController ;
    @FXML
    private PanneauParametresAffichageEnvironnement panneauParametresAffichageEnvironnementController ;

    @FXML
    public ToggleButton selection;

    @FXML
    private BorderPane racine ;

    @FXML
    private ToggleGroup choix_mode ;

    @FXML
    private Toggle ajout_source ;

    @FXML
    private Toggle ajout_demi_plan ;

    @FXML
    private Toggle ajout_segment ;

    @FXML
    private Toggle ajout_prisme ;

    @FXML
    private Toggle ajout_rectangle ;

    @FXML
    private Toggle ajout_cercle ;

    @FXML
    private Toggle ajout_conique ;

    @FXML
    public Button ajout_composition;
//    public Toggle ajout_composition;

    @FXML
    public Toggle ajout_axe_soc;


    @FXML
    // ScrollPane du panneau droit
    public ScrollPane scrollpane_droit_element_courant;

    @FXML
    // AnchorPane du panneau bas
    public AnchorPane anchorpane_bas_element_courant;

    @FXML
    private ListView<Source> listview_sources;

    @FXML
    private TreeView<Obstacle> treeview_obstacles;

    // Liste des Systèmes Optiques Centrés avec leurs éléments
    @FXML
    public TreeView<ElementArbreSOC> treeview_socs;


    // Table donnent le nom des fichiers .fxml de panneau associé à chaque obstacle d'environnement
    private static final Map<Class<?>,String> dico_fxml = Map.ofEntries(
            Map.entry(DemiPlan.class, "View/PanneauDemiPlan.fxml"),
            Map.entry(Segment.class, "View/PanneauSegment.fxml"),
            Map.entry(Prisme.class, "View/PanneauPrisme.fxml"),
            Map.entry(Rectangle.class, "View/PanneauRectangle.fxml"),
            Map.entry(Cercle.class, "View/PanneauCercle.fxml"),
            Map.entry(Conique.class, "View/PanneauConique.fxml"),
            Map.entry(Composition.class, "View/PanneauComposition.fxml")
    );

    private Source source_en_attente_de_panneau ;

    private SystemeOptiqueCentre soc_en_attente_de_panneau ;
    private Obstacle obstacle_en_attente_de_panneau ;
    private boolean obstacle_en_attente_de_panneau_dans_composition;

    private OutilSelection outilSelection ;
    private OutilAjoutSource outilSource;
    private OutilAjoutObstacle outilDemiPlan;
    private OutilAjoutObstacle outilSegment;
    private OutilAjoutObstacle outilPrisme;
    private OutilAjoutObstacle outilRectangle;
    private OutilAjoutObstacle outilCercle;
    private OutilAjoutObstacle outilConique;
    private OutilCreerComposition outilComposition;
    private OutilAjoutSystemeOptiqueCentre outilSystemeOptiqueCentre;


    public PanneauPrincipal(CanvasAffichageEnvironnement cae) {

        LOGGER.log(Level.FINE,"Construction du PanneauPrincipal") ;

        if (cae==null)
            throw new IllegalArgumentException("L'objet affichage environnement attaché au PanneauPrincipal ne peut pas être 'null'") ;

        canvas_environnement = cae ;
        environnement = cae.environnement() ;
    }


    public void initialize() {

        setUpDependecyInjector();

        try {
            panneau_parametres_affichage_environnement = DependencyInjection.load("View/PanneauParametresAffichageEnvironnement.fxml");

            LOGGER.log(Level.FINE,"Panneau parametres affichage environnement créé");
        } catch (IOException e) {

            LOGGER.log( Level.SEVERE, "Exception lors de l'accès au fichier .fxml .",e);

            System.exit(1);
        }

        // Initialiser le contrôleur du panneau des paramètres de l'environnement
        panneauParametresEnvironnementController.initialize(environnement) ;

        // Le panneau des paramètres d'environnement est affiché par défaut dans le panneau de droite : récupérons sa référence
        // pour pouvoir le ré-afficher plus tard.
        panneau_parametres_environnement = scrollpane_droit_element_courant.getContent() ;

        // TODO : voir si interet à creer une vue séparée (fichier .fxml avec classe Custom) pour le CanvasAffichageEnvironnement
        // L'appel au initialize() se ferait automatiquement par le loaderFXML. Les handlers pour tous les évènements souris
        // pourraient y être définis aussi.
        canvas_environnement.initialize();

        StackPane stack_racine = new StackPane(canvas_environnement, canvas_environnement.texte_commentaire) ;
        stack_racine.setMinWidth(0);  // Permet au StackPane de se réduire autant que possible, ce qui évite que ce soit
        stack_racine.setMinHeight(0); //  les panneaux latéraux qui rétrecissent quand on réduit la largeur de la fenêtre.

        canvas_environnement.texte_commentaire.wrappingWidthProperty().bind(canvas_environnement.widthProperty());

        StackPane.setAlignment(canvas_environnement.texte_commentaire, Pos.BOTTOM_CENTER);

        racine.setCenter(stack_racine);

        creerOutils();

        outil_courant = outilSource;

        racine.addEventFilter(KeyEvent.KEY_PRESSED, this::traiterTouchePressee) ;

        canvas_environnement.texte_commentaire.setMouseTransparent(true);

        canvas_environnement.setOnMouseClicked(this::traiterClicSourisCanvas);
        canvas_environnement.setOnMouseMoved(this::traiterDeplacementSourisCanvas);
        canvas_environnement.setOnMouseEntered(mouseEvent -> canvas_environnement.getScene().setCursor(outil_courant.curseurSouris())) ;
        canvas_environnement.setOnMouseExited(mouseEvent  -> canvas_environnement.getScene().setCursor(Cursor.DEFAULT));
        // Pour détecter le début d'un cliquer glisser (NB : il n'existe pas de handler pour détecter directement la fin
        // d'un glisser ; pourrait se faire avec un appel à startFullDrag() dans le handler setOnDragDetected mais dans
        // ce cas tous les Nodes de la scène (càd tous les boutons, listview, treeview, etc.) recevraient des notifications
        // DragEvent lorsqu'ils sont survolés par le glisser, ce qui n'a aucun intérêt dans notre cas).
        // On utilis donc un flag glisser_en_cours pour savoir si un glisser est en cours
//        canvas_environnement.setOnDragDetected(this::traiterDebutGlisser);

        canvas_environnement.setOnMousePressed(this::traiterBoutonSourisPresse);

        canvas_environnement.setOnMouseDragged(this::traiterGlisserSourisCanvas);
        // TODO : à utiliser :
//        canvas_environnement.setOnMouseDragReleased(  );
        canvas_environnement.setOnMouseReleased(this::traiterBoutonSourisRelache);

        map_element_panneau_droit = new HashMap<>(8) ;
        map_element_panneau_bas = new HashMap<>(4) ;

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Initialisation de la liste des sources : rattachement à la liste observable des sources de l'environnement

        listview_sources.setCellFactory(new SourceListCellFactory(environnement));
        listview_sources.setItems(environnement.sources());

        // Intégration dans la vue des éventuelles sources déjà présentes dans l'environnement (peut arriver si on a chargé l'environnement)
        Iterator<Source> its = environnement.iterateur_sources() ;
        while (its.hasNext())
            integrerSourceDansVue(its.next());

        listview_sources.focusModelProperty().getValue().focusedItemProperty().addListener( (obs,old_val,new_val) -> {
            if (new_val == null) {
                scrollpane_droit_element_courant.setContent(panneau_parametres_environnement);
                return;
            }

            // Dé-selection d'un éventuel élément déjà sélectionné dans les autres listes/arbres (pour que l'utilisateur
            // puisse le re-sélectionner et en avoir le détail dans le panneau droit)
            treeview_obstacles.getSelectionModel().clearSelection();
            treeview_socs.getSelectionModel().clearSelection();

            scrollpane_droit_element_courant.setContent(map_element_panneau_droit.get(new_val));

            // Si on est en mode sélection, sélectionner les obstacles dans le canvas
            if (modeCourant()==selection) {
                canvas_environnement.selection().vider();
                canvas_environnement.selection().definirUnite(environnement.unite());
                canvas_environnement.selection().ajouter(new_val) ;
            }
        } ) ;

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Initialisation de l'arbre des obstacles

        // Définition de la Cell Factory qui se chargera d'afficher de créer une cellule dans l'arborescence pour chaque
        // Obstacle (éventuellement avec des images), de gérer le drag and drop et de mettre à jour le nom affiché si le
        // nom de l'obstacle change
        treeview_obstacles.setCellFactory(new ObstacleTreeCellFactory(environnement));

        // TreeView exige un objet racine (qu'on ne montrera pas) : créons donc un objet caché, qui n'est pas dans l'environnement
        Obstacle ob_racine = new Cercle(TypeSurface.CONVEXE,0,0,1.0) ;
        Cercle.razCompteur() ;

        treeview_obstacles.setShowRoot(false);
        treeview_obstacles.setRoot(new TreeItem<>(ob_racine));
        treeview_obstacles.getRoot().setExpanded(true);

        // Intégration dans la vue des éventuels obstacles déjà présents dans l'environnement (peut arriver si on a chargé l'environnement)
        environnement.obstacles().forEach(o->integrerObstacleDansVue(o,treeview_obstacles.getRoot()));
//        Iterator<Obstacle> ito = environnement.iterateur_obstacles() ;
//        while (ito.hasNext())
//            integrerObstacleDansVue(ito.next(),treeview_obstacles.getRoot());

        treeview_obstacles.focusModelProperty().getValue().focusedItemProperty().addListener( (obs,old_val,new_val) -> {
            if (new_val == null) {
                scrollpane_droit_element_courant.setContent(panneau_parametres_environnement);
                return;
            }

            // Dé-selection d'un éventuel élément déjà sélectionné dans les autres listes/arbres (pour que l'utilisateur
            // puisse le re-sélectionner et en avoir le détail dans le panneau droit)
            listview_sources.getSelectionModel().clearSelection();
            treeview_socs.getSelectionModel().clearSelection();

            scrollpane_droit_element_courant.setContent(map_element_panneau_droit.get(new_val.getValue()));

            // Si on est en mode sélection, sélectionner les obstacles dans le canvas
            if (modeCourant()==selection) {
                canvas_environnement.selection().vider();
                canvas_environnement.selection().definirUnite(environnement.unite());
                canvas_environnement.selection().ajouter(new_val.getValue()) ;
            }
        } ) ;

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Initialisation de la liste des socs : rattachement à la liste observable des socs de l'environnement
        treeview_socs.setCellFactory(new ArbreSOCTreeCellFactory(environnement));
        //treeview_socs.setItems(environnement.systemesOptiquesCentres());

        // TreeView exige un objet racine (qu'on ne montrera pas) : créons donc un objet caché, qui n'est pas dans l'environnement
        ElementArbreSOC el_racine = new ElementArbreSOC() ;

        treeview_socs.setShowRoot(false);
        treeview_socs.setRoot(new TreeItem<>(el_racine));
        treeview_socs.getRoot().setExpanded(true);

        // Intégration dans la vue des éventuels SOCs déjà présents dans l'environnement (peut arriver si on a chargé l'environnement)
        environnement.systemesOptiquesCentres().forEach(s->integrerSystemeOptiqueCentreDansVue(s,treeview_socs.getRoot()));
//        environnement.systemesOptiquesCentres().forEach(this::integrerSystemeOptiqueCentreDansVue);


        treeview_socs.focusModelProperty().getValue().focusedItemProperty().addListener((observableValue, old_val, new_val) -> {

            if (new_val == null) {
                scrollpane_droit_element_courant.setContent(panneau_parametres_environnement);
                return;
            }

            // Dé-selection d'un éventuel élément déjà sélectionné dans les autres listes/arbres (pour que l'utilisateur
            // puisse le re-sélectionner et en avoir le détail dans le panneau droit)
            listview_sources.getSelectionModel().clearSelection();
            treeview_obstacles.getSelectionModel().clearSelection();

            scrollpane_droit_element_courant.setContent(map_element_panneau_droit.get(new_val.getValue().contenu()));

            // Si on est en mode sélection, sélectionner l'objet dans le canvas
            if (modeCourant() == selection) {
                canvas_environnement.selection().vider();
                canvas_environnement.selection().definirUnite(environnement.unite());
                if (new_val.getValue().soc!=null)
                    canvas_environnement.selection().ajouter(new_val.getValue().soc) ;
                else if (new_val.getValue().obstacle!=null)
                    canvas_environnement.selection().ajouter(new_val.getValue().obstacle);
            }

            if (new_val.getValue().soc!=null) {
                Node panneau_a_ajouter = map_element_panneau_bas.get(new_val.getValue().soc);
                if (!anchorpane_bas_element_courant.getChildren().contains(panneau_a_ajouter)) {
                    AnchorPane.setTopAnchor(panneau_a_ajouter, 1.0);
                    AnchorPane.setBottomAnchor(panneau_a_ajouter, 1.0);
                    AnchorPane.setLeftAnchor(panneau_a_ajouter, 1.0);
                    AnchorPane.setRightAnchor(panneau_a_ajouter, 1.0);

                    anchorpane_bas_element_courant.getChildren().clear();
                    anchorpane_bas_element_courant.getChildren().add(panneau_a_ajouter);
                }

            }
        });



        // Gestion des "modes" d'ajout : source, segment, demi-plan, etc.
        choix_mode.selectedToggleProperty().addListener((observable, oldValue,newValue) -> {

            LOGGER.log(Level.FINER,"Choix mode passe de {0} à {1}",new Object[] {oldValue,newValue}) ;

            // Ruse pour empêcher la déselection de tous les ToggleButtons :
            if (newValue==null)
                oldValue.setSelected(true); // Va rappeler cette méthode

            // Interruption de l'outil actuel
            if (newValue!=oldValue && outil_courant!=null)
                outil_courant.interrompre();

        });

        class changeListenerOutil implements ChangeListener<Boolean>{
            private final Outil outil;
            changeListenerOutil(Outil outil) { this.outil = outil ; }
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {
                if (oldValue==newValue)
                    return;
                if (newValue) {
                    outil_courant = outil ;
                    outil.prendre();
                } else
                    outil.deposer();
            }
        }

        // Sélection du nouvel outil approprié
        selection.selectedProperty().addListener( new changeListenerOutil(outilSelection) ) ;
        ajout_source.selectedProperty().addListener( new changeListenerOutil(outilSource) ) ;
        ajout_demi_plan.selectedProperty().addListener( new changeListenerOutil(outilDemiPlan) ) ;
        ajout_segment.selectedProperty().addListener( new changeListenerOutil(outilSegment) ) ;
        ajout_prisme.selectedProperty().addListener( new changeListenerOutil(outilPrisme) ) ;
        ajout_cercle.selectedProperty().addListener( new changeListenerOutil(outilCercle) ) ;
        ajout_rectangle.selectedProperty().addListener( new changeListenerOutil(outilRectangle) ) ;
        ajout_conique.selectedProperty().addListener( new changeListenerOutil(outilConique) ) ;
        ajout_axe_soc.selectedProperty().addListener( new changeListenerOutil(outilSystemeOptiqueCentre) ) ;
//        ajout_composition.selectedProperty().addListener( new changeListenerOutil(outilComposition) ) ;
        ajout_composition.setOnAction(e -> {

            outilComposition.prendre();

//            outil_courant.deposer();
//            Outil outil_precedent = outil_courant ;
//            outil_courant = outilComposition ;
//            outilComposition.prendre();
//            outilComposition.deposer();
//            // Reour à l'outil précédent
//            outil_precedent.prendre();
        });

        lcl_sources = change -> {
            while (change.next()) {
                if (change.wasRemoved()) {
                    for (Source remitem : change.getRemoved()) {
                        LOGGER.log(Level.FINE,"Source supprimée : {0}",remitem.nom()) ;

                        listview_sources.getSelectionModel().clearSelection();

                        canvas_environnement.selection().retireSource(remitem);

                        map_element_panneau_droit.remove(remitem) ;

                        // NB : inutile de changer le contenu du panneau droit : c'est fait grâce au listener sur
                        // le focusModelProperty (cf. plus haut dans cette méthode)
//                        scrollpane_droit_element_courant.setContent(panneau_parametres_environnement);

                    }

                } else if (change.wasAdded()) {
                    for (Source additem : change.getAddedSubList()) {

                        LOGGER.log(Level.FINE,"Source ajoutée : {0}",additem.nom()) ;

                        integrerSourceDansVue(additem);

                        // Effet à contrôler : si on colle un ensemble de sources dans l'Environnement, elles seront
                        // toutes sélectionnées successivement jusqu'à la dernière. Un peu inutile...
                        listview_sources.getSelectionModel().select(additem);

                    }
                }

            }
        };

        environnement.ajouterListenerListeSources(lcl_sources);

        lcl_obstacles = change -> {
            while (change.next()) {
                if (change.wasRemoved()) {
                    for (Obstacle remitem : change.getRemoved()) {
                        LOGGER.log(Level.FINE,"Obstacle supprimé : {0}",remitem.nom()) ;

                        TreeItem<Obstacle> tio_a_supprimer = chercheItemDansTreeItem(remitem,treeview_obstacles.getRoot()) ;
                        if (tio_a_supprimer!=null && tio_a_supprimer.getParent()!=null)
                            tio_a_supprimer.getParent().getChildren().remove(tio_a_supprimer) ;

                        treeview_obstacles.getSelectionModel().clearSelection();
                        canvas_environnement.selection().retireObstacle(remitem);

                        map_element_panneau_droit.remove(remitem) ;
                        // NB : inutile de changer le contenu du panneau droit : c'est fait grâce au listener sur
                        // le focusModelProperty (cf. plus haut dans cette méthode)

//                        scrollpane_droit_element_courant.setContent(panneau_parametres_environnement);

                    }
                } else if (change.wasAdded()) {

                    for (Obstacle additem : change.getAddedSubList()) {
                        LOGGER.log(Level.FINE, "Obstacle ajouté : {0}", additem.nom());

                        if (environnement.rang(additem) >= 0) // additem fait partie des obstacles de l'environnement (1er niveau)
                            integrerObstacleDansVue(additem, treeview_obstacles.getRoot(), environnement.rang(additem));
//                        else { // additem fait partie d'une Composition / Ne rien faire (NDLR : reste à comprendre pourquoi, mais ça semble fonctionner comme ça...)
// TODO : Comprendre pourquoi ça fonctionne :-)
//                        }
                    }
                }
            }
        };

        environnement.ajouterListenerListeObstacles(lcl_obstacles);

        lcl_socs = change -> {
            while (change.next()) {
                if (change.wasRemoved()) {
                    for (SystemeOptiqueCentre remitem : change.getRemoved()) {
                        LOGGER.log(Level.FINE,"SOC supprimé : {0}",remitem.nom()) ;

                        TreeItem<ElementArbreSOC> tio_a_supprimer = chercheItemDansTreeItem(chercheItemSOCDansArbreSOC(remitem,treeview_socs.getRoot()).getValue(),treeview_socs.getRoot()) ;
                        if (tio_a_supprimer!=null && tio_a_supprimer.getParent()!=null)
                            tio_a_supprimer.getParent().getChildren().remove(tio_a_supprimer) ;

                        treeview_socs.getSelectionModel().clearSelection();
                        canvas_environnement.selection().retireSoc(remitem);

                        map_element_panneau_droit.remove(remitem) ;

                        // NB : il faut aussi changer le contenu du panneau droit : même si c'est fait grâce au listener sur
                        // le focusModelProperty (cf. plus haut dans cette méthode), si le SOC a été retiré, il disparait
                        // de la listview des SOCs mais en gardant son focus (pas d'évènement de changement de propriété de focus
                        // dans ce cas)
                        scrollpane_droit_element_courant.setContent(panneau_parametres_environnement);

                        map_element_panneau_bas.remove(remitem) ;
                        anchorpane_bas_element_courant.getChildren().clear();

                    }

                } else if (change.wasAdded()) {
                    for (SystemeOptiqueCentre additem : change.getAddedSubList()) {

                        LOGGER.log(Level.FINE,"SOC ajouté : {0}",additem.nom()) ;

                        integrerSystemeOptiqueCentreDansVue(additem,treeview_socs.getRoot());

                    }
                }

            }
        };

        environnement.ajouterListenerListeSystemesOptiquesCentres(lcl_socs);

    }

    private void creerOutils() {

        outilSelection = new OutilSelection(canvas_environnement,jsonMapper) ;

        outilSource = new OutilAjoutSource(canvas_environnement);

        outilDemiPlan = new OutilAjoutObstacle(canvas_environnement) {
            public Obstacle creerObstacle(double x, double y) {
                return new DemiPlan(TypeSurface.CONVEXE,x, y,0.0) ;
            }
        };
        outilSegment = new OutilAjoutObstacle(canvas_environnement) {
            public Obstacle creerObstacle(double x, double y) {
                return new Segment(x, y, canvas_environnement.resolution(), 0d,0d) ;
            }
        };
        outilPrisme = new OutilAjoutObstacle(canvas_environnement) {
            public Obstacle creerObstacle(double x, double y) {
                return new Prisme(TypeSurface.CONVEXE, x, y, 60, canvas_environnement.resolution(),0.0) ;
            }
        };
        outilRectangle = new OutilAjoutObstacle(canvas_environnement) {
            public Obstacle creerObstacle(double x, double y) {
                return new Rectangle(TypeSurface.CONVEXE, x, y, x+ canvas_environnement.resolution(), y- canvas_environnement.resolution(),0.0) ;
            }
        };
        outilCercle = new OutilAjoutObstacle(canvas_environnement) {
            public Obstacle creerObstacle(double x, double y) {
                return new Cercle(TypeSurface.CONVEXE, x, y, cae.resolution()) ;
//                CommandeCreerCercle cmd = new CommandeCreerCercle(environnement,TypeSurface.CONVEXE, x, y, cae.resolution()) ;
//                cmd.executer();
//                return cmd.cercleCree() ;
            }
        };
        outilConique = new OutilAjoutObstacle(canvas_environnement) {
            public Obstacle creerObstacle(double x, double y) {
                return new Conique(TypeSurface.CONVEXE, x, y, 0.0, canvas_environnement.resolution(),1.0) ;
            }
        };

        outilComposition = new OutilCreerComposition(canvas_environnement) ;
        //        outilComposition = new OutilAjoutObstacle(canvas_environnement) {
//            public Obstacle creerObstacle(double x, double y) {
//                return new Composition(Composition.Operateur.UNION) ;
//            }
//        };

        outilSystemeOptiqueCentre = new OutilAjoutSystemeOptiqueCentre(canvas_environnement) ;
    }




    private void setUpDependecyInjector() {
        // TODO plus tard : set bundle
        //DependencyInjection.setBundle(ResourceBundle.getBundle("greetings", Locale.FRENCH));

        // Il est possible que l'Application (c'est-à-dire la classe CrazyDiamond) ait déjà injecté sa propre méthode fabrique
        // de création de contrôleur pour PanneauPrincipal, qui récupère l'environnement depuis son attribut CrazyDiamond.environnement_initial_a_charger.
        // On la remplace par une méthode fabrique qui va maintenant récupérer l'environnement du PanneauPrincipal courant et le passer
        // en paramètre du constructeur d'un nouveau PanneauPrincipal, créé lorsqu'on charge un nouvel environnement depuis un fichier.
        DependencyInjection.removeInjectionMethod(PanneauPrincipal.class);

        // Create factory
        Callable<?> controleurPanneauPrincipalFactory = () -> new PanneauPrincipal(nouveau_canvas_affichage_environnement);

        // Save the factory in the injector
        DependencyInjection.addInjectionMethod(PanneauPrincipal.class, controleurPanneauPrincipalFactory);


        //create factory
        Callable<?> controleurPanneauSourceFactory = () -> new PanneauSource(source_en_attente_de_panneau , canvas_environnement);

        //save the factory in the injector
        DependencyInjection.addInjectionMethod(PanneauSource.class, controleurPanneauSourceFactory) ;

        Callable<?> controleurPanneauParametresAffichageEnvironnementFactory = () -> new PanneauParametresAffichageEnvironnement(canvas_environnement);

        DependencyInjection.addInjectionMethod(PanneauParametresAffichageEnvironnement.class, controleurPanneauParametresAffichageEnvironnementFactory) ;


        Callable<?> controleurPanneauDemiPlanFactory = () -> new PanneauDemiPlan((DemiPlan)obstacle_en_attente_de_panneau,obstacle_en_attente_de_panneau_dans_composition, canvas_environnement);

        DependencyInjection.addInjectionMethod(PanneauDemiPlan.class, controleurPanneauDemiPlanFactory) ;

        Callable<?> controleurPanneauSegmentFactory = () -> new PanneauSegment((Segment)obstacle_en_attente_de_panneau,obstacle_en_attente_de_panneau_dans_composition, canvas_environnement);

        DependencyInjection.addInjectionMethod(PanneauSegment.class, controleurPanneauSegmentFactory) ;

        Callable<?> controleurPanneauPrismeFactory = () -> new PanneauPrisme((Prisme)obstacle_en_attente_de_panneau,obstacle_en_attente_de_panneau_dans_composition, canvas_environnement);

        DependencyInjection.addInjectionMethod(PanneauPrisme.class, controleurPanneauPrismeFactory) ;

        Callable<?> controleurPanneauRectangleFactory = () -> new PanneauRectangle((Rectangle)obstacle_en_attente_de_panneau,obstacle_en_attente_de_panneau_dans_composition, canvas_environnement);

        DependencyInjection.addInjectionMethod(PanneauRectangle.class, controleurPanneauRectangleFactory) ;

        Callable<?> controleurPanneauCercleFactory = () -> new PanneauCercle((Cercle)obstacle_en_attente_de_panneau,obstacle_en_attente_de_panneau_dans_composition, canvas_environnement);

        DependencyInjection.addInjectionMethod(PanneauCercle.class, controleurPanneauCercleFactory) ;

        Callable<?> controleurPanneauConiqueFactory = () -> new PanneauConique((Conique)obstacle_en_attente_de_panneau,obstacle_en_attente_de_panneau_dans_composition, canvas_environnement);

        DependencyInjection.addInjectionMethod(PanneauConique.class, controleurPanneauConiqueFactory) ;

        Callable<?> controleurPanneauCompositionFactory = () -> new PanneauComposition((Composition) obstacle_en_attente_de_panneau,obstacle_en_attente_de_panneau_dans_composition, canvas_environnement);

        DependencyInjection.addInjectionMethod(PanneauComposition.class, controleurPanneauCompositionFactory) ;

        Callable<?> controleurPanneauSystemeOptiqueCentre = () -> new PanneauSystemeOptiqueCentre(soc_en_attente_de_panneau, canvas_environnement);

        DependencyInjection.addInjectionMethod(PanneauSystemeOptiqueCentre.class, controleurPanneauSystemeOptiqueCentre) ;

        Callable<?> controleurPanneauAnalyseParaxialeSystemeOptiqueCentre = () -> new PanneauAnalyseParaxialeSystemeOptiqueCentre(soc_en_attente_de_panneau, canvas_environnement);

        DependencyInjection.addInjectionMethod(PanneauAnalyseParaxialeSystemeOptiqueCentre.class, controleurPanneauAnalyseParaxialeSystemeOptiqueCentre) ;

    }

    @FXML
    public void traiterClicSourisCanvas(MouseEvent me) {

        if (glisser_juste_termine){
            glisser_juste_termine = false ;
            return ;
        }

        if (glisser_en_cours)
            return ;

        Point2D pclic = canvas_environnement.gc_vers_g(me.getX(),me.getY()) ;

        LOGGER.log(Level.FINER,"Clic en ({0},{1})",new Object[] {pclic.getX(),pclic.getY()});

        if (outil_courant!=null)
            outil_courant.traiterClicSourisCanvas(me);

    }

    public void traiterTouchePressee(KeyEvent keyEvent)
    {
        switch (keyEvent.getCode()) {
            case Z -> {
                if (!keyEvent.isControlDown())
                    break ; // Ne pas consommer l'évènement pour que les champs texte, spinners, etc. puissent le recevoir

                Commande.annulerDerniereCommande();

                keyEvent.consume();
            }
            case Y -> {
                if (!keyEvent.isControlDown())
                    break ; // Ne pas consommer l'évènement pour que les champs texte, spinners, etc. puissent le recevoir

                Commande.retablirCommande();

                keyEvent.consume();
            }

        }

        if (outil_courant!=null)
            outil_courant.traiterTouchePressee(keyEvent);
    }

    protected void integrerSourceDansVue(Source s) {

        // Rafraichissement automatique de la liste des sources quand le nom de la source change
        ChangeListener<String> listenerNom = (obs, oldName, newName) -> listview_sources.refresh();
        s.nomProperty().addListener(listenerNom);

        Parent panneau_source_courante = null ;

        LOGGER.log(Level.FINE,"Tentative de chargement du PanneauSource") ;

        source_en_attente_de_panneau = s ;

        try {
            panneau_source_courante = DependencyInjection.load("View/PanneauSource.fxml");
            LOGGER.log(Level.FINE,"PanneauSource créé : {0}",panneau_source_courante) ;
        } catch (IOException e) {
            System.err.println("Exception lors de l'accès au fichier .fxml : "+e.getMessage());
            System.exit(1);
        }

        map_element_panneau_droit.put(s,panneau_source_courante) ;

        scrollpane_droit_element_courant.setContent(panneau_source_courante);

    }

    protected void integrerSystemeOptiqueCentreDansVue(SystemeOptiqueCentre soc, TreeItem<ElementArbreSOC> parent) {

        TreeItem<ElementArbreSOC> tio = ajouterItemDansTreeItem(parent,new ElementArbreSOC(soc));

        ObservableList<Obstacle> obstacles_centres = soc.obstacles_centres() ;

        soc.obstacles_centres().forEach(oc->ajouterItemDansTreeItem(tio,new ElementArbreSOC(oc)));
        observerElementsDeSOC(tio, obstacles_centres);


        Parent panneau_droit_soc_courant = null ;

        LOGGER.log(Level.FINE,"Tentative de chargement du PanneauSystemeOptiqueCentre") ;

        soc_en_attente_de_panneau = soc ;

        try {
            panneau_droit_soc_courant = DependencyInjection.load("View/PanneauSystemeOptiqueCentre.fxml");
            LOGGER.log(Level.FINE,"PanneauSystemeOptiqueCentre créé : {0}",panneau_droit_soc_courant) ;
        } catch (IOException e) {
            System.err.println("Exception lors de l'accès au fichier .fxml : "+e.getMessage());
            System.exit(1);
        }

        map_element_panneau_droit.put(soc,panneau_droit_soc_courant) ;

        scrollpane_droit_element_courant.setContent(panneau_droit_soc_courant);

        Parent panneau_bas_soc_courant = null ;

        LOGGER.log(Level.FINE,"Tentative de chargement du PanneauAnalyseParaxialeSystemeOptiqueCentre") ;

        try {
            panneau_bas_soc_courant = DependencyInjection.load("View/PanneauAnalyseParaxialeSystemeOptiqueCentre.fxml");
            LOGGER.log(Level.FINE,"PanneauAnalyseParaxialeSystemeOptiqueCentre créé : {0}",panneau_droit_soc_courant) ;
        } catch (IOException e) {
            System.err.println("Exception lors de l'accès au fichier .fxml : "+e.getMessage());
            System.exit(1);
        }

        map_element_panneau_bas.put(soc,panneau_bas_soc_courant) ;

        if (!anchorpane_bas_element_courant.getChildren().contains(panneau_bas_soc_courant)) {
            AnchorPane.setTopAnchor(panneau_bas_soc_courant,1.0);
            AnchorPane.setBottomAnchor(panneau_bas_soc_courant,1.0);
            AnchorPane.setLeftAnchor(panneau_bas_soc_courant,1.0);
            AnchorPane.setRightAnchor(panneau_bas_soc_courant,1.0);

            anchorpane_bas_element_courant.getChildren().clear();
            anchorpane_bas_element_courant.getChildren().add(panneau_bas_soc_courant);

        }

    }

    private void observerElementsDeSOC(TreeItem<ElementArbreSOC> tio, ObservableList<Obstacle> obstacles_centres) {
        obstacles_centres.addListener((ListChangeListener<Obstacle>) c -> {
            while (c.next()) {
//                if (c.wasReplaced()) {
//                    if (c.getAddedSubList().get(0)==c.getList().get(c.getFrom())) ;
//                } else
                    if (c.wasRemoved()) {
                        List<? extends Obstacle> o_supprimes = c.getRemoved();
                        for (Obstacle o_s : o_supprimes) {
                            tio.getChildren().removeIf(tioc -> (tioc.getValue().obstacle == o_s));
                        }
                    }
//                  else
                    // Attention, quand le tri de la liste des obstacles centrés se déclenche (cf. méthode SOC::ajoutObstacle)
                    // on reçoit des changements de remplacement (wasReplaced) pour lesquels wasRemoved et wasAdded sont tous les deux vrais :
                    // il faut donc gérer simultanément le retrait et l'ajout (pas de else)
                if (c.wasAdded()) {
                    List<? extends Obstacle> o_ajoutes = c.getAddedSubList() ;
                    o_ajoutes.forEach(oc->ajouterItemDansTreeItem(tio,new ElementArbreSOC(oc),c.getFrom())) ;
                }


            }
        });
    }

    /**
     * Ajoute un obstacle dans l'arbre des obstacles et met en place un Listener sur son nom pour assurer sa mise à jour
     * automatique dans l'arbre.
     * @param parent : Le TreeItem<Obstacle> sous lequel on souhaite ajouter l'obstacle
     * @param it_a_ajouter : L'obstacle à ajouter
     */
    protected <IT> TreeItem<IT> ajouterItemDansTreeItem(TreeItem<IT> parent, IT it_a_ajouter) {

        TreeItem<IT> tio = new TreeItem<>(it_a_ajouter) ;

        parent.getChildren().add(tio) ;

        return tio ;

    }

    protected <IT> TreeItem<IT> ajouterItemDansTreeItem(TreeItem<IT> parent, IT it_a_ajouter, int i_pos) {

        TreeItem<IT> tio = new TreeItem<>(it_a_ajouter) ;

        parent.getChildren().add(i_pos,tio) ;

        return tio ;

    }


    protected Node creerPanneauSimplePourObstacle(Obstacle o,boolean dans_composition) {
        Parent panneau_obstacle_courant = null ;

        LOGGER.log(Level.FINE,"Tentative de chargement du Panneau d'un Obstacle") ;

        obstacle_en_attente_de_panneau = o ;
        obstacle_en_attente_de_panneau_dans_composition = dans_composition ;

        try {
            panneau_obstacle_courant = DependencyInjection.load(dico_fxml.get(o.getClass()));
            LOGGER.log(Level.FINE,"Panneau créé : {0}",panneau_obstacle_courant) ;
        } catch (IOException e) {
            // NB : Mettre un point d'arrêt sur la ligne suivante pour obtenir des details lors d'une erreur au chargement d'un fichier .fxml
            LOGGER.log(Level.SEVERE,"Exception lors de l'accès au fichier .fxml : Message : "+e.getMessage(),e);
            LOGGER.log(Level.SEVERE,"Exception lors de l'accès au fichier .fxml : Cause : "+e.getCause(),e);
            System.exit(1);
        }

        // Enregistrer le nouveau panneau
        map_element_panneau_droit.put(o,panneau_obstacle_courant) ;

        LOGGER.log(Level.FINE,"Ajout de {0} associé au panneau {1} dans la map", new Object[] {o,panneau_obstacle_courant});

        return  panneau_obstacle_courant ;
    }

    protected void integrerObstacleDansVue(Obstacle o, TreeItem<Obstacle> parent) {

        creerPanneauSimplePourObstacle(o, parent != treeview_obstacles.getRoot()) ;

        TreeItem<Obstacle> tio = ajouterItemDansTreeItem(parent,o);

        if (o.getClass()==Composition.class) {
            ObservableList<Obstacle> obstacles = ((Composition) o).elements() ;

            obstacles.forEach(oi->integrerObstacleDansVue(oi,tio));
            observerElementsDeComposition(tio, obstacles);

        }

    }

    private void observerElementsDeComposition(TreeItem<Obstacle> tio, ObservableList<Obstacle> obstacles) {
        obstacles.addListener((ListChangeListener<Obstacle>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {

                    List<? extends Obstacle> o_ajoutes = c.getAddedSubList() ;

                    for (Obstacle o_a : o_ajoutes) {
                        integrerObstacleDansVue(o_a, tio);
                    }

                }
                else if (c.wasRemoved()) {
                    List<? extends Obstacle> o_supprimes = c.getRemoved() ;
                    for (Obstacle o_s : o_supprimes) {
                        tio.getChildren().removeIf(tioc -> ( tioc.getValue() == o_s ) ) ;
                    }
                }

            }
        });
    }

    protected void integrerObstacleDansVue(Obstacle o, TreeItem<Obstacle> parent, int i_pos) {

        creerPanneauSimplePourObstacle(o, parent != treeview_obstacles.getRoot()) ;

        TreeItem<Obstacle> tio = ajouterItemDansTreeItem(parent,o,i_pos);

        if (o.getClass()==Composition.class) {
            ObservableList<Obstacle> obstacles = ((Composition) o).elements() ;

            obstacles.forEach(oi->integrerObstacleDansVue(oi,tio));
            observerElementsDeComposition(tio, obstacles);
//            for (Obstacle oi : obstacles) {
//                integrerObstacleDansVue(oi,tio);
//            }
        }

    }


    private <IT> TreeItem<IT> chercheItemDansTreeItem(IT o_a_trouver, TreeItem<IT> tio) {

        TreeItem<IT> resultat = null ;

        for (TreeItem<IT> tio_fils : tio.getChildren()) {
            if (tio_fils.getValue()==o_a_trouver) // Bingo !
                return tio_fils ; // Pas besoin de chercher plus loin

            if (!tio_fils.isLeaf()) { // Descente dans le fils si ce n'est pas une feuille de l'arbre

                resultat = chercheItemDansTreeItem(o_a_trouver,tio_fils) ;

                if (resultat!=null) // Bingo !
                        return resultat ;
            }
        }

        return resultat ; // Forcément null

    }

    private TreeItem<ElementArbreSOC> chercheItemSOCDansArbreSOC(SystemeOptiqueCentre soc_a_trouver, TreeItem<ElementArbreSOC> tio) {
        for (TreeItem<ElementArbreSOC> tioc : tio.getChildren()) {
            if (Objects.equals(tioc.getValue().soc,soc_a_trouver))
                return tioc ;
        }
        return null ;
    }


    @FXML
    public void traiterDeplacementSourisCanvas(MouseEvent me) {

        outil_courant.traiterDeplacementSourisCanvas(me);

        Point2D pos_souris = canvas_environnement.gc_vers_g(me.getX(),me.getY()) ;

        // Affichage des infos en bas de l'écran
        String sb = "(X : "
                + canvas_environnement.convertisseurAffichageDistance().toString(pos_souris.getX())
                + " , Y : "
                + canvas_environnement.convertisseurAffichageDistance().toString(pos_souris.getY())
                + ") " + environnement.unite().symbole;

        label_droit.setText(sb);

        Obstacle obs = canvas_environnement.obstacle_pointe_en(pos_souris) ;
        if (obs!=null) {
            label_gauche.setText(obs.nom() + " (" + obs.natureMilieu().toString().toLowerCase() + (obs.natureMilieu() == NatureMilieu.TRANSPARENT ? " n=" + obs.indiceRefraction()+")":")"));
        } else {
            Source src = canvas_environnement.source_pointee_en(pos_souris) ;
            label_gauche.setText(src != null ? src.nom() + " (" + src.type().toString().toLowerCase()+")" : "-");
        }

    }

    public Toggle modeCourant() {
        return choix_mode.selectedToggleProperty().getValue() ;
    }

    public void traiterBoutonSourisPresse(MouseEvent mouseEvent) {

        LOGGER.log(Level.FINER,"Bouton souris pressé") ;

        glisser_juste_termine = false ;

        if (outil_courant!=null)
            outil_courant.traiterBoutonSourisPresse(mouseEvent);

    }

    public void traiterGlisserSourisCanvas(MouseEvent mouseEvent) {

        glisser_en_cours = true ;

        if (outil_courant!=null )
            outil_courant.traiterGlisserSourisCanvas(mouseEvent);


    }

    public void traiterBoutonSourisRelache(MouseEvent mouseEvent) {

        // Ne rien à faire si ce n'est pas la fin d'un glisser
        if (!glisser_en_cours)
            return ;

        // Dans cette méthode, on ne traite que la fin d'un glisser
        glisser_en_cours = false ;
        glisser_juste_termine = true ;

        if (outil_courant!=null)
            outil_courant.traiterBoutonSourisRelacheFinGlisser(mouseEvent);

    }

//    public void traiterCreationComposition() {
//
//        ButtonType okButtonType = new ButtonType(rb.getString("bouton.dialogue.composition.ok"), ButtonBar.ButtonData.OK_DONE);
//        ButtonType annulerButtonType = new ButtonType(rb.getString("bouton.dialogue.composition.annuler"), ButtonBar.ButtonData.CANCEL_CLOSE);
//        Dialog<ArrayList<Obstacle>> boite_dialogue = new Dialog<>() ;
//
//        boite_dialogue.setTitle(rb.getString("titre.dialogue.composition"));
//        boite_dialogue.setHeaderText(rb.getString("invite.dialogue.composition"));
//
//        ObservableList<Obstacle> obstacles_a_proposer =  FXCollections.observableArrayList();
//
//        Iterator<Obstacle> ito =  environnement.iterateur_obstacles() ;
//        while (ito.hasNext()) {
//            Obstacle o = ito.next() ;
//            // Rechercher si l'obstacle o implémente l'interface ElementAvecMatiere car c'est requis pour faire partie d'une composition
//            // S"assurer aussi qu'il ne fait pas partie d'un SOC
//            if (o instanceof ElementAvecMatiere && !o.appartientASystemeOptiqueCentre())
//                obstacles_a_proposer.add( o ) ;
//        }
//
//        ListView<Obstacle> lo = new ListView<>(obstacles_a_proposer) ;
//
//        ScrollPane sp = new ScrollPane(lo) ;
//        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
//
//        lo.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
//
//        boite_dialogue.getDialogPane().setContent(lo);
//
//        boite_dialogue.setResultConverter( buttonType -> {
//            if (buttonType == okButtonType)
//                return new ArrayList<>(lo.getSelectionModel().getSelectedItems()) ;
//
//            return null ;
//        });
//
//        boite_dialogue.getDialogPane().getButtonTypes().add(okButtonType);
//        boite_dialogue.getDialogPane().getButtonTypes().add(annulerButtonType);
//
//
//        Optional<ArrayList<Obstacle>> op_obstacles_choisis =  boite_dialogue.showAndWait() ;
//        if (op_obstacles_choisis.isPresent()) {
//
//            ArrayList<Obstacle> obstacles_choisis = op_obstacles_choisis.get() ;
//
//            LOGGER.log(Level.INFO,"Obstacles choisis pour composition : {0}",obstacles_choisis) ;
//
//            Composition compo = new Composition(Composition.Operateur.UNION);
//
//            for(Obstacle o : obstacles_choisis) {
//                environnement.supprimerObstacle(o);
//                compo.ajouterObstacle(o);
//            }
//
//            environnement.ajouterObstacle(compo);
//        }
//
//
//    }

    public void traiterDefinitionParametresEnvironnement() {
        listview_sources.getSelectionModel().clearSelection();
        treeview_obstacles.getSelectionModel().clearSelection();
        treeview_socs.getSelectionModel().clearSelection();

        scrollpane_droit_element_courant.setContent(panneau_parametres_environnement);

    }

    public void traiterDefinitionParametresAffichage() {
        listview_sources.getSelectionModel().clearSelection();
        treeview_obstacles.getSelectionModel().clearSelection();
        treeview_socs.getSelectionModel().clearSelection();

        scrollpane_droit_element_courant.setContent(panneau_parametres_affichage_environnement);
    }


    public void traiterNouvelEnvironnement() {

        nouveau_canvas_affichage_environnement = new CanvasAffichageEnvironnement(new Environnement()) ;

        Parent nouvelle_racine = null ;

        Commande.effacerHistoriques();

        try {
            // Cette injection va récupérer le nouvel environnement depuis l'attribut nouvel_environnement du PanneauPrincipal actuel
            nouvelle_racine = DependencyInjection.load("View/PanneauPrincipal.fxml");

            LOGGER.log(Level.FINE,"Panneau principal créé");
        } catch (IOException e) {

            LOGGER.log( Level.SEVERE, "Exception lors de l'accès au fichier .fxml .",e);

            System.exit(1);
        }

        // Remplacement du PanneauPrincipal courant par le nouveau (racine vaut 'null' apres cette instruction)
        racine.getScene().setRoot(nouvelle_racine);

        Stage s = (Stage) nouvelle_racine.getScene().getWindow() ;
        s.setUserData(null);
        s.setTitle("Crazy Diamond");


    }
    public void traiterChargerEnvironnement() {

        // Ouverture d'un FileChooser modal sur la fenêtre principale (un paramètre 'null' l'aurait rendu amodal)
        File fichier_a_charger = fileChooser.showOpenDialog(canvas_environnement.getScene().getWindow());

        if (fichier_a_charger == null)
            return ;

        try {

            ContextAttributes ca = ContextAttributes.getEmpty() ;
            ca = ca.withSharedAttribute("largeur_graphique", canvas_environnement.largeurGraphique()) ;
            ca = ca.withSharedAttribute("hauteur_graphique", canvas_environnement.hauteurGraphique()) ;

            ObjectReader or = jsonMapper.readerFor(CanvasAffichageEnvironnement.class).with(ca) ;

            // Préparation du nouveau canvas qui va remplacer l'actuel (le jsonMapper l'a déjà associé au nouvel Environnement chargé)
            nouveau_canvas_affichage_environnement = or.readValue(fichier_a_charger,CanvasAffichageEnvironnement.class) ;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,"Exception lors du chargement du fichier : ",e) ;

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Impossible de charger l'environnement ou l'affichage associé");
            alert.setContentText(e.getMessage()+System.lineSeparator()+"in :"+System.lineSeparator()+e.getStackTrace()[0].toString());
            alert.showAndWait();

        }

        // Si on n'a pas pu construire un nouvel environnement, il n'y a rien d'autre à faire
        if (nouveau_canvas_affichage_environnement==null)
            return;

        Commande.effacerHistoriques();

        Parent nouvelle_racine = null ;

        try {

            // Cette injection va récupérer le nouveau canvas affichage depuis l'attribut nouveau_canvas_affichage_environnement
            // du PanneauPrincipal actuel et le nouvel environnement qu'il contient.
            nouvelle_racine = DependencyInjection.load("View/PanneauPrincipal.fxml");

            LOGGER.log(Level.FINE,"Panneau principal créé");
        } catch (IOException e) {

            LOGGER.log( Level.SEVERE, "Exception lors de l'accès au fichier .fxml .",e);

            System.exit(1);
        }

        nouveau_canvas_affichage_environnement.rafraichirAffichage();

        // Remplacement du PanneauPrincipal courant par le nouveau (racine vaut 'null' apres cette instruction)
        racine.getScene().setRoot(nouvelle_racine);

        Stage s = (Stage) nouvelle_racine.getScene().getWindow() ;
        s.setUserData(fichier_a_charger);
        s.setTitle(fichier_a_charger.getName()+" - Crazy Diamond");

    }
    public void traiterSauvegarderEnvironnement() {

        Window window = racine.getScene().getWindow() ;

        // Ouverture d'un FileChooser modal sur la fenêtre principale (un paramètre 'null' l'aurait rendu amodal)
        if (window.getUserData() == null) {
            fileChooser.setInitialFileName("Nouveau");

            // Mémorisation du File ouvert
            window.setUserData(fileChooser.showSaveDialog(racine.getScene().getWindow()));
        }

        if (window.getUserData() == null)
            return;

        sauvegarderEnvironnement();


    }

    public void traiterSauvegarderEnvironnementSous() {

        Window window = racine.getScene().getWindow() ;

        // Ouverture d'un FileChooser modal sur la fenêtre principale (un paramètre 'null' l'aurait rendu amodal)

        if (window.getUserData()!=null) {
            fileChooser.setInitialDirectory(((File)window.getUserData()).getParentFile());
        }

        window.setUserData(fileChooser.showSaveDialog(racine.getScene().getWindow()));

        if (window.getUserData() == null)
            return;

        sauvegarderEnvironnement();
    }

    private void sauvegarderEnvironnement() {
        String json;

        try {
            json = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(canvas_environnement);
        } catch (JsonProcessingException e) {
            LOGGER.log(Level.SEVERE,"Exception lors de la sérialisation en JSON de l'Environnement ou des propriétés d'afffichage associées : ",e);

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Impossible de sauvegarder l'environnement");
            alert.setContentText(e.getMessage()+System.lineSeparator()+e.getCause());
            alert.showAndWait();
            return ;
        }

        Window window = racine.getScene().getWindow() ;

        FileOutputStream fos;

        try {
            fos = new FileOutputStream((File)window.getUserData());
        } catch (FileNotFoundException e) {
            LOGGER.log(Level.SEVERE,"Exception lors de la l'accès au fichier de sauvegarde : ",e);

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Impossible de sauvegarder l'environnement");
            alert.setContentText(e.getMessage()+System.lineSeparator()+e.getCause());
            alert.showAndWait();
            return ;
        }

        try {
            fos.write(json.getBytes(StandardCharsets.UTF_8));
            fos.close();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,"Exception lors de la l'écriture dans le fichier de sauvegarde : ",e);

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Impossible de sauvegarder l'environnement");
            alert.setContentText(e.getMessage()+System.lineSeparator()+e.getCause());
            alert.showAndWait();
        }

        Stage s = (Stage) racine.getScene().getWindow() ;
        s.setTitle( ((File)s.getUserData()).getName() );

    }

    public void traiterImporter() {
        // Ouverture d'un FileChooser modal sur la fenêtre principale (un paramètre 'null' l'aurait rendu amodal)
        File fichier_a_charger = fileChooser.showOpenDialog(canvas_environnement.getScene().getWindow());

        if (fichier_a_charger == null)
            return ;

        try {
            ContextAttributes ca = ContextAttributes.getEmpty() ;
            // Passage d'un environnement hote dans lequel l'ObjectReader va ajouter les éléments importables du fichier
            ca = ca.withSharedAttribute("environnement_hote", environnement) ;
            ElementsSelectionnes es_importes = new ElementsSelectionnes() ;
            ca = ca.withSharedAttribute("elements_importes", es_importes) ;

            ObjectReader or = jsonMapper.readerFor(Environnement.class).with(ca) ;
            or.readValue(fichier_a_charger,Environnement.class) ;

            // Passage en mode sélection et sélection des éléments importés
            selection.setSelected(true);
//            outil_courant = outilSelection ;
//            outil_courant.prendre();
            canvas_environnement.definirSelection(es_importes);

//            if (or.getAttributes().getAttribute("elements_importes")!=null)
//                canvas_environnement.definirSelection((ElementsSelectionnes) or.getAttributes().getAttribute("elements_importes")) ;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,"Exception lors du chargement du fichier : ",e) ;

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Impossible de charger l'environnement ou l'affichage associé");
            alert.setContentText(e.getMessage()+System.lineSeparator()+"in :"+System.lineSeparator()+e.getStackTrace()[0].toString());
            alert.showAndWait();
        }

    }
}
