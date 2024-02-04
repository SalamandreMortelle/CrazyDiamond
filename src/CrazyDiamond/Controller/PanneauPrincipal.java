package CrazyDiamond.Controller;

import CrazyDiamond.Model.*;
import CrazyDiamond.Serializer.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.cfg.ContextAttributes;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

// Classe du Controleur du Panneau principal
public class PanneauPrincipal {

    static private final JsonMapper jsonMapper = new JsonMapper();
    static private final SimpleModule simpleModule = new SimpleModule();
    static private final FileChooser fileChooser = new FileChooser();

    static protected final DataFormat format_crazy_diamond_elements = new DataFormat("application/crazy-diamond.elements");

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

//    protected Source source_en_cours_ajout = null ;
//
//    protected Obstacle obstacle_en_cours_ajout = null ;
//
//    protected SystemeOptiqueCentre soc_en_cours_ajout = null ;

    private boolean glisser_en_cours  = false ;
    private boolean glisser_juste_termine = false ;
    private Point2D p_debut_glisser ;

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
    public Toggle ajout_composition;

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

    // Menu contextuel avec l'entrée "Supprimer" (pour la liste des sources)
    private final ContextMenu menuContextuelSources ;

    // Liste observable des sources selectionnees
    ObservableList<Source>  sources_selectionnees ;

    @FXML
    private TreeView<Obstacle> treeview_obstacles;

    private final ContextMenu menuContextuelObstacles ;

    // Liste observable des obstacles sélectionnés dans l'arborescence
    ObservableList<TreeItem<Obstacle>> obstacles_selectionnes_dans_arborescence;

    // Liste des Systèmes Optiques Centrés avec leurs éléments
    @FXML
    public TreeView<ElementArbreSOC> treeview_socs;

    // Menu contextuel avec l'entrée "Supprimer" (pour la liste des sources)
    private final ContextMenu menuContextuelSocSupprimer;
    // et avec l'entrée retirer (pour retirer un obstacle d'un SOC depuis l'arbre des SOCs)
    private final ContextMenu menuContextuelSocRetirerObstacle;

    // Liste observable des éléments d'arbre des SOCs (SOC ou Obstacle dans le SOC) sélectionnés
    ObservableList<TreeItem<ElementArbreSOC>> socs_selectionnes;


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

//    private boolean retaillage_selection_en_cours = false ;
//    private boolean selection_rectangulaire_en_cours;
//    private Point2D p_debut_glisser_selection;

    private OutilSelection outilSelection ;
    private OutilAjoutSource outilSource;
    private OutilAjoutObstacle outilDemiPlan;
    private OutilAjoutObstacle outilSegment;
    private OutilAjoutObstacle outilPrisme;
    private OutilAjoutObstacle outilRectangle;
    private OutilAjoutObstacle outilCercle;
    private OutilAjoutObstacle outilConique;
    private OutilAjoutObstacle outilComposition;
    private OutilAjoutSystemeOptiqueCentre outilSystemeOptiqueCentre;


    public PanneauPrincipal(CanvasAffichageEnvironnement cae) {

        LOGGER.log(Level.FINE,"Construction du PanneauPrincipal") ;

        if (cae==null)
            throw new IllegalArgumentException("L'objet affichage environnement attaché au PanneauPrincipal ne peut pas être 'null'") ;

        canvas_environnement = cae ;
        environnement = cae.environnement() ;

        menuContextuelSources = new ContextMenu() ;
        MenuItem deleteItemSource = new MenuItem(rb.getString("supprimer.source"));
        deleteItemSource.setOnAction(event -> environnement.retirerSource(listview_sources.getSelectionModel().getSelectedItem()));
        menuContextuelSources.getItems().add(deleteItemSource);

        menuContextuelObstacles = new ContextMenu() ;
        MenuItem deleteItemObstacle = new MenuItem(rb.getString("supprimer.obstacle"));
        deleteItemObstacle.setOnAction(event -> environnement.retirerObstacle(treeview_obstacles.getSelectionModel().getSelectedItem().getValue()));
        menuContextuelObstacles.getItems().add(deleteItemObstacle);


        // TODO : définir ces menus contextuels dans le TreeCellFactory

        menuContextuelSocSupprimer = new ContextMenu() ;
        MenuItem deleteItemSoc = new MenuItem(rb.getString("supprimer.soc"));
        deleteItemSoc.setOnAction(event -> environnement.retirerSystemeOptiqueCentre(treeview_socs.getSelectionModel().getSelectedItem().getValue().soc));
        menuContextuelSocSupprimer.getItems().add(deleteItemSoc);

        menuContextuelSocRetirerObstacle = new ContextMenu() ;
        MenuItem retirerItemSoc = new MenuItem(rb.getString("retirer.obstacle.soc"));
        retirerItemSoc.setOnAction(event -> treeview_socs.getSelectionModel().getSelectedItem().getParent().getValue().soc
                .retirerObstacleCentre(treeview_socs.getSelectionModel().getSelectedItem().getValue().obstacle));
        menuContextuelSocRetirerObstacle.getItems().add(retirerItemSoc);


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

        racine.addEventFilter(KeyEvent.KEY_PRESSED,key_event -> {
                switch (key_event.getCode()) {
//                case ESCAPE -> {
//                    if (source_en_cours_ajout != null) {
//                        // On retire la source courante, ce qui va rafraichir les chemins et le décor
//                        environnement.retirerSource(source_en_cours_ajout);
//                        source_en_cours_ajout = null;
//                    }
//                    else
//                        if (obstacle_en_cours_ajout != null) {
//                        // On retire la source courante, ce qui va rafraichir les chemins et le décor
//                        environnement.retirerObstacle(obstacle_en_cours_ajout);
//                        obstacle_en_cours_ajout = null;
//                    }
//                    else
//                        if (soc_en_cours_ajout!= null) {
//                        // On retire le soc courant, [ce qui va rafraichir les chemins et le décor ?]
//                        environnement.retirerSystemeOptiqueCentre(soc_en_cours_ajout);
//                        soc_en_cours_ajout = null;
//                    }
//                    else
//                    if (canvas_environnement.selection().nombreElements()>0)
//                        canvas_environnement.selection().vider();
//                    else if (retaillage_selection_en_cours)
//                        retaillage_selection_en_cours = false ;
//                    else
//                        break;
//
//                    key_event.consume();
//                }
//                case LEFT -> {
//
//                    if (canvas_environnement.selection().estVide())
//                        break; // Ne pas consommer l'évènement pour que les champs texte, spinners, etc. puissent le recevoir
//
//                    canvas_environnement.translaterSelection(new Point2D(-canvas_environnement.resolution(), 0.0)) ;
//                    key_event.consume();
//                }
//                case RIGHT ->  {
//
//                    if (canvas_environnement.selection().estVide())
//                        break; // Ne pas consommer l'évènement pour que les champs texte, spinners, etc. puissent le recevoir
//
//                    canvas_environnement.translaterSelection(new Point2D(canvas_environnement.resolution(),0.0)) ;
//                    key_event.consume();
//                }
//                case UP -> {
//
//                    if (canvas_environnement.selection().estVide())
//                        break; // Ne pas consommer l'évènement pour que les champs texte, spinners, etc. puissent le recevoir
//
//                    canvas_environnement.translaterSelection(new Point2D(0.0, canvas_environnement.resolution())) ;
//                    key_event.consume();
//                }
//                case DOWN ->  {
//                    if (canvas_environnement.selection().estVide())
//                        break; // Ne pas consommer l'évènement pour que les champs texte, spinners, etc. puissent le recevoir
//
//                    canvas_environnement.translaterSelection(new Point2D(0.0,-canvas_environnement.resolution())) ;
//                    key_event.consume();
//                }
                case A -> {
                    if (!key_event.isControlDown())
                        break ; // Ne pas consommer l'évènement pour que les champs texte, spinners, etc. puissent le recevoir

                    selectionnerTout() ;

                    key_event.consume();
                }
                case C -> {
                    if (!key_event.isControlDown() || canvas_environnement.selection().estVide())
                        break ; // Ne pas consommer l'évènement pour que les champs texte, spinners, etc. puissent le recevoir

                    Clipboard clipboard = Clipboard.getSystemClipboard();
                    ClipboardContent content = new ClipboardContent();

                    String json = serialiserElementsSelectionnes();

                    if (json!=null) {
                        content.put(format_crazy_diamond_elements, json);
                        content.putString(json);
                        clipboard.setContent(content);
                    }

                    key_event.consume();
                }
                case X -> {
                    if (!key_event.isControlDown())
                        break ; // Ne pas consommer l'évènement pour que les champs texte, spinners, etc. puissent le recevoir

                    Clipboard clipboard = Clipboard.getSystemClipboard();
                    ClipboardContent content = new ClipboardContent();

                    String json = serialiserElementsSelectionnes();

                    if (json!=null) {
                        content.put(format_crazy_diamond_elements, json);
                        content.putString(json);
                        clipboard.setContent(content);

                        supprimerElementsSelectionnes() ;
                    }


                    key_event.consume();
                }
                case V -> {
                    if (!key_event.isControlDown())
                        break ; // Ne pas consommer l'évènement pour que les champs texte, spinners, etc. puissent le recevoir

                    Clipboard clipboard = Clipboard.getSystemClipboard();

                    ElementsSelectionnes es = null ;

                    try {
                        ContextAttributes ca = ContextAttributes.getEmpty() ;
                        // Passage d'un environnement hote dans lequel l'ObjectReader va ajouter les éléments importables du fichier
                        ca = ca.withSharedAttribute("environnement_hote", environnement) ;

                        ObjectReader or = jsonMapper.readerFor(ElementsSelectionnes.class).with(ca) ;
                        if (clipboard.hasContent(format_crazy_diamond_elements))
                            es = or.readValue(clipboard.getContent(format_crazy_diamond_elements).toString(),ElementsSelectionnes.class) ;
                        else if (clipboard.hasString()) // Si le clipbpard contient une string, on tente de la parser comme du JSON CrazyDiamond
                            es = or.readValue(clipboard.getString(),ElementsSelectionnes.class) ;

                        if (es!=null)
                            canvas_environnement.definirSelection(es) ;

                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE,"Exception lors de la lecture du presse-papier") ;

                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setHeaderText("Impossible d'instancier de nouveaux éléments à partir des éléments du presse-papier");
                        alert.setContentText(e.getMessage()+System.lineSeparator()+"in :"+System.lineSeparator()+e.getStackTrace()[0].toString());
                        alert.showAndWait();
                    }


                    key_event.consume();
                }

            } } );


        creerOutils();

        outil_courant = outilSource;

        racine.addEventFilter(KeyEvent.KEY_PRESSED, this::traiterTouchePressee) ;

        // TODO : voir si on pourrait utiliser setMouseTransparent sur le texte_commentaire, plutôt
//        canvas_environnement.texte_commentaire.setMouseTransparent(true);

        canvas_environnement.setOnMouseClicked(this::traiterClicSourisCanvas);
        canvas_environnement.texte_commentaire.setOnMouseClicked(canvas_environnement::fireEvent);
        canvas_environnement.setOnMouseMoved(this::traiterDeplacementSourisCanvas);
        canvas_environnement.texte_commentaire.setOnMouseMoved(canvas_environnement::fireEvent);

        // Pour détecter le début d'un cliquer glisser (NB : il n'existe pas de handler pour détecter directement la fin
        // d'un glisser ; pourrait se faire avec un appel à startFullDrag() dans le handler setOnDragDetected mais dans
        // ce cas tous les Nodes de la scène (càd tous les boutons, listview, treeview, etc.) recevraient des notifications
        // DragEvent lorsqu'ils sont survolés par le glisser, ce qui n'a aucun intérêt dans notre cas).
        // On utilis donc un flag glisser_en_cours pour savoir si un glisser est en cours
//        canvas_environnement.setOnDragDetected(this::traiterDebutGlisser);

        canvas_environnement.setOnMousePressed(this::traiterBoutonSourisPresse);
        canvas_environnement.texte_commentaire.setOnMousePressed(canvas_environnement::fireEvent);


        canvas_environnement.setOnMouseDragged(this::traiterGlisserSourisCanvas);
        canvas_environnement.texte_commentaire.setOnMouseDragged(canvas_environnement::fireEvent);
        // TODO : à utiliser :
//        canvas_environnement.setOnMouseDragReleased(  );
//        canvas_environnement.texte_commentaire.setOnMouseDragReleased(  );
        canvas_environnement.setOnMouseReleased(this::traiterBoutonSourisRelache);
        canvas_environnement.texte_commentaire.setOnMouseReleased(canvas_environnement::fireEvent);

        map_element_panneau_droit = new HashMap<>(8) ;
        map_element_panneau_bas = new HashMap<>(4) ;

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Initialisation de la liste des sources : rattachement à la liste observable des sources de l'environnement
        listview_sources.setItems(environnement.sources());

        // Intégration dans la vue des éventuelles sources déjà présentes dans l'environnement (peut arriver si on a chargé l'environnement)
        Iterator<Source> its = environnement.iterateur_sources() ;
        while (its.hasNext())
            integrerSourceDansVue(its.next());

        // Maintenir une référence vers la liste observable des sources actuellement sélectionnées dans la listview
        sources_selectionnees = listview_sources.getSelectionModel().getSelectedItems() ;

        // Brancher ou débrancher le menu contextuel de suppression selon qu'il y a une source sélectionnée ou non
        // Et mettre le bon panneau de contenu
        sources_selectionnees.addListener((ListChangeListener<Source>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {

                    treeview_obstacles.getSelectionModel().clearSelection();
                    treeview_socs.getSelectionModel().clearSelection();

                    List<? extends Source> s_ajoutees = c.getAddedSubList() ;
                    // Afficher le panneau correspondant à la dernière source ajoutée
                    scrollpane_droit_element_courant.setContent(map_element_panneau_droit.get(s_ajoutees.get(s_ajoutees.size()-1)));

                    if (modeCourant()==selection) {
                        canvas_environnement.selection().vider();
                        canvas_environnement.selection().definirUnite(environnement.unite());
                        s_ajoutees.forEach(s -> canvas_environnement.selection().ajouter(s));
                    }

                    if (listview_sources.getContextMenu()==null)
                        listview_sources.setContextMenu(menuContextuelSources);

                }
                else if (c.wasRemoved())  {
                    scrollpane_droit_element_courant.setContent(panneau_parametres_environnement);

                    if (sources_selectionnees.isEmpty())
                        listview_sources.setContextMenu(null);
                }

            }
        });

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

        // Maintenir une référence vers la liste observable des obstacles actuellement sélectionnés dans la listview
        obstacles_selectionnes_dans_arborescence = treeview_obstacles.getSelectionModel().getSelectedItems() ;

        // Brancher ou débrancher le menu contextuel de suppression selon qu'il y a un obstacle sélectionné ou non
        // Et mettre le bon panneau de contenu
        obstacles_selectionnes_dans_arborescence.addListener((ListChangeListener<TreeItem<Obstacle>>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {

                    listview_sources.getSelectionModel().clearSelection();
                    treeview_socs.getSelectionModel().clearSelection();

                    List<? extends TreeItem<Obstacle>> o_ajoutes = c.getAddedSubList() ;

                    Obstacle dernier_obstacle_de_selection = o_ajoutes.get(o_ajoutes.size()-1).getValue() ;

                    // Afficher le panneau correspondant au dernier obstacle ajouté
                    scrollpane_droit_element_courant.setContent(map_element_panneau_droit.get(dernier_obstacle_de_selection));

                    // Si on est en mode sélection, sélectionner les obstacles dans le canvas
                    if (modeCourant()==selection) {
                        canvas_environnement.selection().vider();
                        canvas_environnement.selection().definirUnite(environnement.unite());
                        o_ajoutes.forEach(tio -> canvas_environnement.selection().ajouter(tio.getValue())) ;
                    }

                    if (treeview_obstacles.getContextMenu()==null)
                        treeview_obstacles.setContextMenu(menuContextuelObstacles);
                }
                else if (c.wasRemoved()) {
                    scrollpane_droit_element_courant.setContent(panneau_parametres_environnement);

                    if (obstacles_selectionnes_dans_arborescence.isEmpty())
                        treeview_obstacles.setContextMenu(null);
                }

            }
        });

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

        // Maintenir une référence vers la liste observable des éléments d'arbre des SOCs actuellement sélectionnées dans la TreeView
        socs_selectionnes = treeview_socs.getSelectionModel().getSelectedItems() ;

        // Brancher ou débrancher le menu contextuel de suppression selon qu'il y a un SOC sélectionné ou non
        // Et mettre le bon panneau de contenu
        socs_selectionnes.addListener((ListChangeListener<TreeItem<ElementArbreSOC>>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {

                    listview_sources.getSelectionModel().clearSelection();
                    treeview_obstacles.getSelectionModel().clearSelection();

                    List<? extends TreeItem<ElementArbreSOC>> el_soc_ajoutes = c.getAddedSubList() ;

                    // Cette variable contiendra soit un SOC soit un obstacle qui fait partie d'un SOC
                    ElementArbreSOC dernier_element_de_selection = el_soc_ajoutes.get(el_soc_ajoutes.size()-1).getValue() ;

                    // Afficher le panneau droit correspondant au dernier SOC  ou obstacle ajouté dans la sélection
                    scrollpane_droit_element_courant.setContent(map_element_panneau_droit.get(dernier_element_de_selection.contenu()));
//                  scrollpane_droit_element_courant.setContent(map_element_panneau_droit.get(el_soc_ajoutes.get(el_soc_ajoutes.size()-1)));

                    // Afficher le panneau bas correspondant au dernier SOC ajouté
                    if (dernier_element_de_selection.soc !=null) {
                        Node panneau_a_ajouter = map_element_panneau_bas.get(dernier_element_de_selection.soc);
//                      Node panneau_a_ajouter = map_element_panneau_bas.get(el_soc_ajoutes.get(el_soc_ajoutes.size()-1)) ;
                        if (!anchorpane_bas_element_courant.getChildren().contains(panneau_a_ajouter)) {
                            AnchorPane.setTopAnchor(panneau_a_ajouter, 1.0);
                            AnchorPane.setBottomAnchor(panneau_a_ajouter, 1.0);
                            AnchorPane.setLeftAnchor(panneau_a_ajouter, 1.0);
                            AnchorPane.setRightAnchor(panneau_a_ajouter, 1.0);

                            anchorpane_bas_element_courant.getChildren().clear();
                            anchorpane_bas_element_courant.getChildren().add(panneau_a_ajouter);
                        }

                        // Si on est en mode sélection, sélectionner l'objet dans le canvas
                        if (modeCourant() == selection) {
                            canvas_environnement.selection().vider();
                            canvas_environnement.selection().definirUnite(environnement.unite());
                            el_soc_ajoutes.forEach(el_soc -> {
                                if (el_soc.getValue().soc!=null)
                                    canvas_environnement.selection().ajouter(el_soc.getValue().soc) ;
                                else if (el_soc.getValue().obstacle!=null)
                                    canvas_environnement.selection().ajouter(el_soc.getValue().obstacle);

                            });
                        }

                        // TODO déplacer la définition de ce Context menu dans SystemeOptiqueCentreTreeCellFactory
                        if (treeview_socs.getContextMenu() == null || treeview_socs.getContextMenu()==menuContextuelSocRetirerObstacle)
                            treeview_socs.setContextMenu(menuContextuelSocSupprimer);

                    } else { // C'est un des obstacles d'un SOC qui est sélectionné
                        // TODO déplacer la définition de ce Context menu dans SystemeOptiqueCentreTreeCellFactory

                        // Définir un context menu permettant de retirer l'obstacle du  + afficher le panneau associé à l'obstacle
                        if (treeview_socs.getContextMenu() == null || treeview_socs.getContextMenu()==menuContextuelSocSupprimer)
                            treeview_socs.setContextMenu(menuContextuelSocRetirerObstacle);

                    }
                }
                else if (c.wasRemoved())  {
                    scrollpane_droit_element_courant.setContent(panneau_parametres_environnement);

                    // TODO déplacer la définition de ce Context menu dans SystemeOptiqueCentreTreeCellFactory
                    if (socs_selectionnes.isEmpty())
                        treeview_socs.setContextMenu(null);
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
//            if (oldValue == ajout_source && source_en_cours_ajout != null)
//                source_en_cours_ajout = null ;

            if (oldValue == selection && canvas_environnement.selection().nombreElements()>0)
                canvas_environnement.selection().vider();

            if (oldValue != selection && newValue==selection)
                canvas_environnement.selection().vider();

            // Sélection du nouvel outil approprié
            if (newValue == selection && outil_courant != outilSelection)
                outil_courant = outilSelection;
            else if (newValue == ajout_source && outil_courant != outilSource)
                outil_courant = outilSource;
            else if (newValue == ajout_demi_plan && outil_courant != outilDemiPlan)
                outil_courant = outilDemiPlan;
            else if (newValue == ajout_segment && outil_courant != outilSegment)
                outil_courant = outilSegment;
            else if (newValue == ajout_prisme && outil_courant != outilPrisme)
                outil_courant = outilPrisme;
            else if (newValue == ajout_cercle && outil_courant != outilCercle)
                outil_courant = outilCercle;
            else if (newValue == ajout_rectangle && outil_courant != outilRectangle)
                outil_courant = outilRectangle;
            else if (newValue == ajout_conique && outil_courant != outilConique)
                outil_courant = outilConique;
            else if (newValue == ajout_composition && outil_courant != outilComposition)
                outil_courant = outilComposition;
            else if (newValue == ajout_axe_soc && outil_courant != outilSystemeOptiqueCentre)
                outil_courant = outilSystemeOptiqueCentre;

        });

//        ajout_cercle.selectedProperty().addListener((observable, oldValue,newValue) -> {
//            if (newValue==oldValue)
//                return;
//            if (newValue)
//                outil_courant = outilCercle ;
//            else
//                outil_courant.interrompre();
//        });

        lcl_sources = change -> {
            while (change.next()) {
                if (change.wasRemoved()) {
                    for (Source remitem : change.getRemoved()) {
                        LOGGER.log(Level.FINE,"Source supprimée : {0}",remitem.nom()) ;

                        listview_sources.getSelectionModel().clearSelection();

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

        outilSelection = new OutilSelection(canvas_environnement) ;

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
            }
        };
        outilConique = new OutilAjoutObstacle(canvas_environnement) {
            public Obstacle creerObstacle(double x, double y) {
                return new Conique(TypeSurface.CONVEXE, x, y, 0.0, canvas_environnement.resolution(),1.0) ;
            }
        };
        outilComposition = new OutilAjoutObstacle(canvas_environnement) {
            public Obstacle creerObstacle(double x, double y) {
                return new Composition(Composition.Operateur.UNION) ;
            }
        };

        outilSystemeOptiqueCentre = new OutilAjoutSystemeOptiqueCentre(canvas_environnement) ;
    }

    private void selectionnerTout() {

        canvas_environnement.selection().vider();
        canvas_environnement.selection().definirUnite(environnement.unite()) ;

        Iterator<Obstacle> ito = environnement.iterateur_obstacles() ;
        while (ito.hasNext())
            canvas_environnement.selection().ajouter(ito.next());

        Iterator<Source> its = environnement.iterateur_sources() ;
        while (its.hasNext())
            canvas_environnement.selection().ajouter(its.next());

        Iterator<SystemeOptiqueCentre> itsoc = environnement.iterateur_systemesOptiquesCentres() ;
        while (itsoc.hasNext())
            canvas_environnement.selection().ajouter(itsoc.next());

    }

    private void supprimerElementsSelectionnes() {
        ElementsSelectionnes es = canvas_environnement.selection() ;

        // Le retrait des obstacles, sources et socs de l'environnement altère (cf. callbacks ListChangeListener dans
        // l'Environnement) les éléments sélectionnés que l'on est en train de parcourir, ce qui lèverait une exception.
        // Pour éviter cela, commençons par faire une copie (non profonde) de la sélection.
        ElementsSelectionnes es_copie = new ElementsSelectionnes(es) ;

        es_copie.stream_obstacles().forEach(environnement::retirerObstacle);
        es_copie.stream_sources().forEach(environnement::retirerSource);
        es_copie.stream_socs().forEach(environnement::retirerSystemeOptiqueCentre);

    }

    private String serialiserElementsSelectionnes() {

        String json = null ;

        try {
            json = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(canvas_environnement.selection());
        } catch (JsonProcessingException e) {
            LOGGER.log(Level.SEVERE,"Exception lors de la sérialisation en JSON des éléments sélectionnés ",e.getMessage());

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Impossible de sérialiser les éléments sélectionnés");
            alert.setContentText(e.getMessage()+System.lineSeparator()+e.getCause());
            alert.showAndWait();
        }

        return json ;

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
//        if (true)
//            return ;
        ///////////////

        // TODO : Voir ce qu'on fait de ce bloc ; à faire plutôt quand on passe de l'outil de sélection à un autre
//        if (modeCourant() != selection) {
//            canvas_environnement.selection().vider();
//        }

//        if (modeCourant() == selection && canvas_environnement.selection().obstacleUnique() != null) {
//            if (!retaillage_selection_en_cours) { // On commence un retaillage
//                if (canvas_environnement.poignee_obstacle_pointee_en(pclic)) {
//                    LOGGER.log(Level.FINE, "Poignée sélectionnée");
//                    retaillage_selection_en_cours = true;
//                }
//                else
//                    LOGGER.log(Level.FINE, "Poignée non sélectionnée");
//            } else { // Retaillage de sélection était en cours : on le termine
//                canvas_environnement.selection().obstacleUnique().retaillerSelectionPourSourisEn(pclic);
//                retaillage_selection_en_cours = false ;
//            }
//        }  else if (modeCourant() == selection && canvas_environnement.selection().sourceUnique() != null) {
//            if (!retaillage_selection_en_cours) { // On commence un retaillage
//                if (canvas_environnement.poignee_source_pointee_en(pclic)) {
//                    LOGGER.log(Level.FINE, "Poignée source sélectionnée");
//                    retaillage_selection_en_cours = true;
//                }
//                else
//                    LOGGER.log(Level.FINE, "Poignée source non sélectionnée");
//
//            } else { // Retaillage de sélection était en cours : on le termine
//                canvas_environnement.selection().sourceUnique().retaillerPourSourisEn(pclic);
//                retaillage_selection_en_cours = false ;
//            }
//        }


    }

    public void traiterTouchePressee(KeyEvent keyEvent)
    {
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

//        // Rafraichissement automatique de la liste des socs quand le nom du SOC change
//        ChangeListener<String> listenerNom = (obs, oldName, newName) -> listview_socs.refresh();
//        s.nomProperty().addListener(listenerNom);

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

        /////// A ENLEVER
//        if (true)
//            return ;

        Obstacle obs = canvas_environnement.obstacle_pointe_en(pos_souris) ;
        if (obs!=null) {
            label_gauche.setText(obs.nom() + " (" + obs.natureMilieu().toString().toLowerCase() + (obs.natureMilieu() == NatureMilieu.TRANSPARENT ? " n=" + obs.indiceRefraction()+")":")"));
        } else {
            Source src = canvas_environnement.source_pointee_en(pos_souris) ;
            label_gauche.setText(src != null ? src.nom() + " (" + src.type().toString().toLowerCase()+")" : "-");
        }

//        if (canvas_environnement.selection().obstacleUnique() !=null && retaillage_selection_en_cours) {
//            canvas_environnement.selection().obstacleUnique().retaillerSelectionPourSourisEn(pos_souris);
//        } else if (canvas_environnement.selection().sourceUnique() !=null && retaillage_selection_en_cours) {
//            canvas_environnement.selection().sourceUnique().retaillerPourSourisEn(pos_souris);
//        } else if (canvas_environnement.selection().socUnique() !=null && retaillage_selection_en_cours) {
//            canvas_environnement.selection().socUnique().retaillerPourSourisEn(pos_souris);
//        }

    }

    public Toggle modeCourant() {
        return choix_mode.selectedToggleProperty().getValue() ;
    }

    public void traiterBoutonSourisPresse(MouseEvent mouseEvent) {

        LOGGER.log(Level.FINER,"Bouton souris pressé") ;

        glisser_juste_termine = false ;

//        if (!mouseEvent.isDragDetect()) { // Si ce n'est pas le début d'un glisser on ne fait rien de plus
//            System.out.println("************** PAS LE DEBUT D'UN GLISSER");
//            return;
//        }
//        else {
//            System.out.println("************** DEBUT D'UN GLISSER");
//        }
//        glisser_en_cours = true ;

        // C'est peut-être le début d'un glisser de souris : enregistrons la position de début de glisser
        p_debut_glisser = new Point2D(mouseEvent.getX(),mouseEvent.getY());

//        p_debut_glisser_selection = new Point2D(mouseEvent.getX(),mouseEvent.getY());

        if (outil_courant!=null)
            outil_courant.traiterBoutonSourisPresse(mouseEvent);

//        p_debut_glisser_selection = new Point2D(mouseEvent.getX(),mouseEvent.getY());
//
//        if (modeCourant()==selection) {
//            Point2D pclic = canvas_environnement.gc_vers_g(mouseEvent.getX(), mouseEvent.getY());
//
//            Obstacle o_avant = canvas_environnement.selection().obstacleUnique() ;
//            Source   s_avant = canvas_environnement.selection().sourceUnique();
//            SystemeOptiqueCentre   soc_avant = canvas_environnement.selection().socUnique() ;
//
//            Obstacle o_pointe  = canvas_environnement.obstacle_pointe_en(pclic) ;
//            Source   s_pointee = canvas_environnement.source_pointee_en(pclic) ;
//            SystemeOptiqueCentre   soc_pointe = canvas_environnement.soc_pointe_en(pclic) ;
//
//            if (!retaillage_selection_en_cours) {
//                if (s_pointee!=null && !canvas_environnement.selection().comprend(s_pointee)) {
//                    canvas_environnement.selection().definirUnite(environnement.unite());
//                    canvas_environnement.selection().selectionnerUniquement(s_pointee);
//                } else if (o_pointe!=null && !canvas_environnement.selection().comprend(o_pointe)) {
//                    canvas_environnement.selection().definirUnite(environnement.unite());
//                    canvas_environnement.selection().selectionnerUniquement(o_pointe);
//                } else if (soc_pointe!=null && !canvas_environnement.selection().comprend(soc_pointe)) {
//                    canvas_environnement.selection().definirUnite(environnement.unite());
//                    canvas_environnement.selection().selectionnerUniquement(soc_pointe);
//                } else {
//                    canvas_environnement.selection().vider();
//                    selection_rectangulaire_en_cours = true ;
//                }
//            }
//
//            if (canvas_environnement.selection().obstacleUnique()!=o_avant) {
//                treeview_obstacles.getSelectionModel().select(chercheItemDansTreeItem(canvas_environnement.selection().obstacleUnique(), treeview_obstacles.getRoot()));
//            }
//            if (canvas_environnement.selection().sourceUnique()!=s_avant) {
//                listview_sources.getSelectionModel().select(canvas_environnement.selection().sourceUnique());
//            }
//            if (canvas_environnement.selection().socUnique()!=soc_avant) {
//                treeview_socs.getSelectionModel().select(chercheItemSOCDansArbreSOC(canvas_environnement.selection().socUnique(),treeview_socs.getRoot()));
//            }
//
//        }

    }

    public void traiterGlisserSourisCanvas(MouseEvent mouseEvent) {

        glisser_en_cours = true ;
//        if (!mouseEvent.isDragDetect() )
//            return;
//        if ( p_debut_glisser == null )
//            return;

        if (outil_courant!=null )
            outil_courant.traiterGlisserSourisCanvas(mouseEvent);

        canvas_environnement.getScene().setCursor(Cursor.MOVE);

        Point2D p_fin_glisser = new Point2D(mouseEvent.getX(),mouseEvent.getY());

        Point2D p_debut_glisser_g = canvas_environnement.gc_vers_g(p_debut_glisser.getX(),p_debut_glisser.getY());
//        Point2D p_debut_glisser_selection_g = canvas_environnement.gc_vers_g(p_debut_glisser_selection.getX(), p_debut_glisser_selection.getY());
        Point2D p_fin_glisser_g   = canvas_environnement.gc_vers_g(p_fin_glisser.getX(),p_fin_glisser.getY());

        Point2D v_glisser_g = p_fin_glisser_g.subtract(p_debut_glisser_g) ;

        // La position actuelle de la souris devient le nouveau point de depart pour la suite du glisser
        p_debut_glisser = p_fin_glisser ;

//        if (modeCourant() == selection) {
//            if (selection_rectangulaire_en_cours) {
//                BoiteLimiteGeometrique zone_rect = new BoiteLimiteGeometrique(
//                   Math.min(p_debut_glisser_selection_g.getX(),p_fin_glisser_g.getX()),
//                   Math.min(p_debut_glisser_selection_g.getY(),p_fin_glisser_g.getY()),
//                   Math.abs(p_debut_glisser_selection_g.getX()-p_fin_glisser_g.getX()),
//                   Math.abs(p_debut_glisser_selection_g.getY()-p_fin_glisser_g.getY())
//                ) ;
//
//                canvas_environnement.selectionnerParZoneRectangulaire(zone_rect);
//            }
//            else
//                canvas_environnement.translaterSelection(v_glisser_g);
//        } else
        if (modeCourant() != selection) {
            canvas_environnement.translaterLimites(v_glisser_g.getX(),v_glisser_g.getY());
        }

        canvas_environnement.rafraichirAffichage();

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

//        // Si un rayon était en cours de placement, on l'oublie
//        source_en_cours_ajout = null ;

//        canvas_environnement.selectionnerParZoneRectangulaire(null);
//        selection_rectangulaire_en_cours = false ;

        canvas_environnement.getScene().setCursor(Cursor.DEFAULT);

        Point2D p_fin_glisser = new Point2D(mouseEvent.getX(),mouseEvent.getY());

        Point2D p_debut_glisser_g = canvas_environnement.gc_vers_g(p_debut_glisser.getX(),p_debut_glisser.getY());
        Point2D p_fin_glisser_g   = canvas_environnement.gc_vers_g(p_fin_glisser.getX(),p_fin_glisser.getY());

        Point2D v_glisser_g = p_fin_glisser_g.subtract(p_debut_glisser_g) ;

//        // Etait-on en train de déplacer un obstacle sélectionné ?
//        if (modeCourant() == selection && canvas_environnement.selection().nombreElements() >0 ) {
//            canvas_environnement.translaterSelection(v_glisser_g);
//        } else

        if (modeCourant()!=selection || canvas_environnement.selection().nombreElements() == 0)
        { // Sinon, aucun élément n'était sélectionné : on était en train de déplacer la zone visible
            canvas_environnement.translaterLimites(v_glisser_g.getX(), v_glisser_g.getY());
            canvas_environnement.rafraichirAffichage();
        }

    }

    public void traiterCreationComposition() {

        ButtonType okButtonType = new ButtonType(rb.getString("bouton.dialogue.composition.ok"), ButtonBar.ButtonData.OK_DONE);
        ButtonType annulerButtonType = new ButtonType(rb.getString("bouton.dialogue.composition.annuler"), ButtonBar.ButtonData.CANCEL_CLOSE);
        Dialog<ArrayList<Obstacle>> boite_dialogue = new Dialog<>() ;

        boite_dialogue.setTitle(rb.getString("titre.dialogue.composition"));
        boite_dialogue.setHeaderText(rb.getString("invite.dialogue.composition"));

        ObservableList<Obstacle> obstacles_a_proposer =  FXCollections.observableArrayList();

        Iterator<Obstacle> ito =  environnement.iterateur_obstacles() ;
        while (ito.hasNext()) {
            Obstacle o = ito.next() ;
            // Rechercher si l'obstacle o implémente l'interface ElementAvecMatiere car c'est requis pour faire partie d'une composition
            // S"assurer aussi qu'il ne fait pas partie d'un SOC
            if (o instanceof ElementAvecMatiere && !o.appartientASystemeOptiqueCentre())
                obstacles_a_proposer.add( o ) ;
        }

        ListView<Obstacle> lo = new ListView<>(obstacles_a_proposer) ;

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

            LOGGER.log(Level.INFO,"Obstacles choisis pour composition : {0}",obstacles_choisis) ;

            Composition compo = new Composition(Composition.Operateur.UNION);

            for(Obstacle o : obstacles_choisis) {
                environnement.retirerObstacle(o);
                compo.ajouterObstacle(o);
            }

            environnement.ajouterObstacle(compo);
        }


    }

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

            ObjectReader or = jsonMapper.readerFor(Environnement.class).with(ca) ;
            or.readValue(fichier_a_charger,Environnement.class) ;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,"Exception lors du chargement du fichier : ",e) ;

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Impossible de charger l'environnement ou l'affichage associé");
            alert.setContentText(e.getMessage()+System.lineSeparator()+"in :"+System.lineSeparator()+e.getStackTrace()[0].toString());
            alert.showAndWait();
        }

    }
}
