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
import javafx.event.ActionEvent;
import javafx.event.Event;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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

    static {

        // Serializers
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

        // Deserializers
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

    CanvasAffichageEnvironnement canvas_affichage_environnement;


    // Récupération du logger
    private static final Logger LOGGER = Logger.getLogger( "CrazyDiamond" );

    private static final ResourceBundle rb = ResourceBundle.getBundle("CrazyDiamond") ;


    protected Source source_en_cours_ajout = null ;

    protected Obstacle obstacle_en_cours_ajout = null ;

    protected SystemeOptiqueCentre soc_en_cours_ajout = null ;

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
//    public ScrollPane scrollpane_bas_element_courant;

    @FXML
    private ListView<Source> listview_sources;

    // Menu contexteuel avec l'entrée "Supprimer" (pour la liste des sources)
    private final ContextMenu menuContextuelSources ;

    // Liste observable des sources selectionnees
    ObservableList<Source>  sources_selectionnees ;

    @FXML
    private TreeView<Obstacle> treeview_obstacles;

    private final ContextMenu menuContextuelObstacles ;

    // Liste observable des obstacles selectionnés dans l'arborescence
    ObservableList<TreeItem<Obstacle>> obstacles_selectionnes_dans_arborescence;

    // Liste des Systèmes Optiques Centrés
    @FXML
    public ListView<SystemeOptiqueCentre> listview_socs;

    // Menu contextuel avec l'entrée "Supprimer" (pour la liste des sources)
    private final ContextMenu menuContextuelSoc ;

    // Liste observable des SOC selectionnées
    ObservableList<SystemeOptiqueCentre> socs_selectionnes;


    // Table donnent le nom des fichiers. fxml de panneau associé à chaque obstacle d'environnement
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

    private boolean retaillage_selection_en_cours = false ;


    public PanneauPrincipal(CanvasAffichageEnvironnement cae) {

        LOGGER.log(Level.FINE,"Construction du PanneauPrincipal") ;

        if (cae==null)
            throw new IllegalArgumentException("L'objet affichage environnement attaché au PanneauPrincipal ne peut pas être 'null'") ;

        canvas_affichage_environnement = cae ;
        environnement = cae.environnement() ;

        menuContextuelSources = new ContextMenu() ;
        MenuItem deleteItemSource = new MenuItem(rb.getString("supprimer.source"));
        deleteItemSource.setOnAction(event -> environnement.retirerSource(listview_sources.getSelectionModel().getSelectedItem()));
        menuContextuelSources.getItems().add(deleteItemSource);

        menuContextuelObstacles = new ContextMenu() ;
        MenuItem deleteItemObstacle = new MenuItem(rb.getString("supprimer.obstacle"));
        deleteItemObstacle.setOnAction(event -> environnement.retirerObstacle(treeview_obstacles.getSelectionModel().getSelectedItem().getValue()));
        menuContextuelObstacles.getItems().add(deleteItemObstacle);

        menuContextuelSoc = new ContextMenu() ;
        MenuItem deleteItemSoc = new MenuItem(rb.getString("supprimer.soc"));
        deleteItemSoc.setOnAction(event -> environnement.retirerSystemeOptiqueCentre(listview_socs.getSelectionModel().getSelectedItem()));
        menuContextuelSoc.getItems().add(deleteItemSoc);

    }


    public void initialize() {

        setUpDependecyInjector();

//        if (nouvel_environnement == null)
//            canvas_affichage_environnement = new CanvasAffichageEnvironnement(environnement) ;

        try {
            panneau_parametres_affichage_environnement = DependencyInjection.load("View/PanneauParametresAffichageEnvironnement.fxml");

            LOGGER.log(Level.FINE,"Panneau parametres affichage environnement créé");
        } catch (IOException e) {

            LOGGER.log( Level.SEVERE, "Exception lors de l'accès au fichier .fxml .",e);

            System.exit(1);
        }

        // Initialiser le controleur du panneau des paramètres de l'environnement
        panneauParametresEnvironnementController.initialize(environnement) ;

        // Le panneau des paramètres d'environnement est affiché par défaut dans le panneau de droite : récupérons sa référence
        // pour pouvoir le ré-afficher plus tard
        panneau_parametres_environnement = scrollpane_droit_element_courant.getContent() ;

        // TODO : voir si interet à creer une vue séparée (fichier .fxml avec classe Custom) pour le CanvasAffichageEnvironnement
        // L'appel au initialize() se ferait automatiquement par le loaderFXML. Les handlers pour tous les évènements souris
        // pourraient y être définis aussi.
        canvas_affichage_environnement.initialize();

        StackPane stack_racine = new StackPane(canvas_affichage_environnement, canvas_affichage_environnement.texte_commentaire) ;
        stack_racine.setMinWidth(0);  // Permet au StackPane de se réduire autant que possible, ce qui évite que ce soit
        stack_racine.setMinHeight(0); //  les panneaux latéraux qui rétrecissent quand on réduit la largeur de la fenêtre.

        canvas_affichage_environnement.texte_commentaire.wrappingWidthProperty().bind(canvas_affichage_environnement.widthProperty());

        StackPane.setAlignment(canvas_affichage_environnement.texte_commentaire, Pos.BOTTOM_CENTER);
//        StackPane stack_racine = new StackPane( canvas_affichage_environnement.texte_commentaire) ;
//
        racine.setCenter(stack_racine);
//        racine.setCenter(canvas_affichage_environnement);



        racine.addEventFilter(KeyEvent.KEY_PRESSED,key_event -> {
                switch (key_event.getCode()) {
                case ESCAPE -> {
                    if (source_en_cours_ajout != null) {
                        // On retire la source courante, ce qui va rafraichir les chemins et le décor
                        environnement.retirerSource(source_en_cours_ajout);
                        source_en_cours_ajout = null;
                    }
                    if (obstacle_en_cours_ajout != null) {
                        // On retire la source courante, ce qui va rafraichir les chemins et le décor
                        environnement.retirerObstacle(obstacle_en_cours_ajout);
                        obstacle_en_cours_ajout = null;
                    }
                    if (soc_en_cours_ajout!= null) {
                        // On retire le soc courant, [ce qui va rafraichir les chemins et le décor ?]
                        environnement.retirerSystemeOptiqueCentre(soc_en_cours_ajout);
                        soc_en_cours_ajout = null;
                    }
                    if (canvas_affichage_environnement.obstacleSelectionne() != null) {
                        canvas_affichage_environnement.deselectionneObstacle();
                    }
                    if (canvas_affichage_environnement.sourceSelectionnee() != null) {
                        canvas_affichage_environnement.deselectionneSource();
                    }
                    if (canvas_affichage_environnement.systemeOptiqueCentreSelectionne() != null) {
                        canvas_affichage_environnement.deselectionneSystemeOptiqueCentre();
                    }
                    if (retaillage_selection_en_cours)
                        retaillage_selection_en_cours = false ;

                    key_event.consume();
                }
                case LEFT -> {

                    Point2D tr = new Point2D(-canvas_affichage_environnement.resolution(),0.0) ;

                    if (canvas_affichage_environnement.obstacleSelectionne() != null) {
                        Obstacle o = canvas_affichage_environnement.obstacleSelectionne() ;
                        if (!o.appartientASystemeOptiqueCentre())
                            o.translater(tr);
                        else {
                            SystemeOptiqueCentre soc = environnement.systemeOptiqueCentreContenant(o);
                            Point2D tr_sur_axe = soc.vecteurDirecteurAxe().multiply(soc.vecteurDirecteurAxe().dotProduct(tr)) ;
                            o.translater(tr_sur_axe);
                        }
                    }
                    else if (canvas_affichage_environnement.sourceSelectionnee() != null)
                        canvas_affichage_environnement.sourceSelectionnee().translater(tr);
                    else if (canvas_affichage_environnement.systemeOptiqueCentreSelectionne() != null)
                        canvas_affichage_environnement.systemeOptiqueCentreSelectionne().translater(tr);
                    else
                        break ; // Ne pas consommer l'évènement pour que les champs texte, spinners, etc. puissent le recevoir


                    key_event.consume();
                }
                case RIGHT ->  {
                    Point2D tr = new Point2D(canvas_affichage_environnement.resolution(),0.0) ;

                    if (canvas_affichage_environnement.obstacleSelectionne() != null) {
                        Obstacle o = canvas_affichage_environnement.obstacleSelectionne() ;
                        if (!o.appartientASystemeOptiqueCentre())
                            o.translater(tr);
                        else {
                            SystemeOptiqueCentre soc = environnement.systemeOptiqueCentreContenant(o);
                            Point2D tr_sur_axe = soc.vecteurDirecteurAxe().multiply(soc.vecteurDirecteurAxe().dotProduct(tr)) ;
                            o.translater(tr_sur_axe);
                        }
                    }
                    else if (canvas_affichage_environnement.sourceSelectionnee() != null)
                        canvas_affichage_environnement.sourceSelectionnee().translater(tr);
                    else if (canvas_affichage_environnement.systemeOptiqueCentreSelectionne() != null)
                        canvas_affichage_environnement.systemeOptiqueCentreSelectionne().translater(tr);
                    else
                        break ; // Ne pas consommer l'évènement pour que les champs texte, spinners, etc. puissent le recevoir

                    key_event.consume();
                }
                case UP -> {
                    Point2D tr = new Point2D(0.0,canvas_affichage_environnement.resolution()) ;

                    if (canvas_affichage_environnement.obstacleSelectionne() != null) {
                        Obstacle o = canvas_affichage_environnement.obstacleSelectionne() ;
                        if (!o.appartientASystemeOptiqueCentre())
                            o.translater(tr);
                        else {
                            SystemeOptiqueCentre soc = environnement.systemeOptiqueCentreContenant(o);
                            Point2D tr_sur_axe = soc.vecteurDirecteurAxe().multiply(soc.vecteurDirecteurAxe().dotProduct(tr)) ;
                            o.translater(tr_sur_axe);
                        }
                    }
                    else if (canvas_affichage_environnement.sourceSelectionnee() != null)
                        canvas_affichage_environnement.sourceSelectionnee().translater(tr);
                    else if (canvas_affichage_environnement.systemeOptiqueCentreSelectionne() != null)
                        canvas_affichage_environnement.systemeOptiqueCentreSelectionne().translater(tr);
                    else
                        break ; // Ne pas consommer l'évènement pour que les champs texte, spinners, etc. puissent le recevoir

                    key_event.consume();
                }
                case DOWN ->  {
                    Point2D tr = new Point2D(0.0,-canvas_affichage_environnement.resolution()) ;

                    if (canvas_affichage_environnement.obstacleSelectionne() != null) {
                        Obstacle o = canvas_affichage_environnement.obstacleSelectionne() ;
                        if (!o.appartientASystemeOptiqueCentre())
                            o.translater(tr);
                        else {
                            SystemeOptiqueCentre soc = environnement.systemeOptiqueCentreContenant(o);
                            Point2D tr_sur_axe = soc.vecteurDirecteurAxe().multiply(soc.vecteurDirecteurAxe().dotProduct(tr)) ;
                            o.translater(tr_sur_axe);
                        }
                    }
                    else if (canvas_affichage_environnement.sourceSelectionnee() != null)
                        canvas_affichage_environnement.sourceSelectionnee().translater(tr);
                    else if (canvas_affichage_environnement.systemeOptiqueCentreSelectionne() != null)
                        canvas_affichage_environnement.systemeOptiqueCentreSelectionne().translater(tr);
                    else
                        break ; // Ne pas consommer l'évènement pour que les champs texte, spinners, etc. puissent le recevoir

                    key_event.consume();
                }

            } } );

        canvas_affichage_environnement.setOnMouseClicked(this::traiterClicSourisCanvas);
//        canvas_affichage_environnement.texte_commentaire.addEventHandler(MouseEvent.MOUSE_CLICKED,canvas_affichage_environnement::fireEvent);
        canvas_affichage_environnement.texte_commentaire.setOnMouseClicked(canvas_affichage_environnement::fireEvent);
        canvas_affichage_environnement.setOnMouseMoved(this::traiterDeplacementSourisCanvas);
        canvas_affichage_environnement.texte_commentaire.setOnMouseMoved(canvas_affichage_environnement::fireEvent);
//        canvas_affichage_environnement.texte_commentaire.addEventHandler(MouseEvent.MOUSE_MOVED,canvas_affichage_environnement::fireEvent);
//        canvas_affichage_environnement.texte_commentaire.addEventHandler(MouseEvent.MOUSE_MOVED, mouseEvent -> {
//            canvas_affichage_environnement.fireEvent(mouseEvent);
//        } );

        canvas_affichage_environnement.setOnMousePressed(this::traiterBoutonSourisPresse);
        canvas_affichage_environnement.texte_commentaire.setOnMousePressed(canvas_affichage_environnement::fireEvent);
        canvas_affichage_environnement.setOnMouseDragged(this::traiterGlisserSourisCanvas);
        canvas_affichage_environnement.texte_commentaire.setOnMouseDragged(canvas_affichage_environnement::fireEvent);
        canvas_affichage_environnement.setOnMouseReleased(this::traiterBoutonSourisRelache);
        canvas_affichage_environnement.texte_commentaire.setOnMouseReleased(canvas_affichage_environnement::fireEvent);

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
                    listview_socs.getSelectionModel().clearSelection();

                    List<? extends Source> s_ajoutees = c.getAddedSubList() ;
                    // Afficher le panneau correspondant à la dernière source ajoutée
                    scrollpane_droit_element_courant.setContent(map_element_panneau_droit.get(s_ajoutees.get(s_ajoutees.size()-1)));

                    Source derniere_source_de_selection = s_ajoutees.get(s_ajoutees.size()-1) ;

                    // Si on est en mode sélection, sélectionner l'objet dans le canvas
                    if (modeCourant()==selection) {
                        canvas_affichage_environnement.selectionneSource(derniere_source_de_selection);
                    }


                    if (listview_sources.getContextMenu()==null)
                        listview_sources.setContextMenu(menuContextuelSources);

                }
                else if (c.wasRemoved())  {
                    scrollpane_droit_element_courant.setContent(panneau_parametres_environnement);

//                    listview_sources.getSelectionModel().clearSelection();

                    if (sources_selectionnees.isEmpty())
                        listview_sources.setContextMenu(null);
                }

            }
        });

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Initialisation de l'arbre des obstacles
        // TreeView exige un objet racine (qu'on ne montrera pas) : créons donc un objet caché, qui n'est pas dans l'environnement
        Obstacle ob_racine = new Cercle(TypeSurface.CONVEXE,0,0,1.0) ;
        Cercle.razCompteur() ;

        treeview_obstacles.setShowRoot(false);
        treeview_obstacles.setRoot(new TreeItem<>(ob_racine));
        treeview_obstacles.getRoot().setExpanded(true);

        // Intégration dans la vue des éventuels obstacles déjà présents dans l'environnement (peut arriver si on a chargé l'environnement)
        Iterator<Obstacle> ito = environnement.iterateur_obstacles() ;
        while (ito.hasNext())
            integrerObstacleDansVue(ito.next(),treeview_obstacles.getRoot());

        // Maintenir une référence vers la liste observable des obstacles actuellement sélectionnés dans la listview
        obstacles_selectionnes_dans_arborescence = treeview_obstacles.getSelectionModel().getSelectedItems() ;

        // Brancher ou débrancher le menu contextuel de suppression selon qu'il y a un obstacle sélectionné ou non
        // Et mettre le bon panneau de contenu
        obstacles_selectionnes_dans_arborescence.addListener((ListChangeListener<TreeItem<Obstacle>>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {

                    listview_sources.getSelectionModel().clearSelection();
                    listview_socs.getSelectionModel().clearSelection();

                    List<? extends TreeItem<Obstacle>> o_ajoutes = c.getAddedSubList() ;

                    Obstacle dernier_obstacle_de_selection = o_ajoutes.get(o_ajoutes.size()-1).getValue() ;

                    // Afficher le panneau correspondant au dernier obstacle ajouté
                    scrollpane_droit_element_courant.setContent(map_element_panneau_droit.get(dernier_obstacle_de_selection));

                    // Si on est en mode sélection, sélectionner l'objet dans le canvas
                    if (modeCourant()==selection)
                        canvas_affichage_environnement.selectionneObstacle(dernier_obstacle_de_selection);

                    if (treeview_obstacles.getContextMenu()==null)
                        treeview_obstacles.setContextMenu(menuContextuelObstacles);
                }
                else if (c.wasRemoved()) {
                    scrollpane_droit_element_courant.setContent(panneau_parametres_environnement);

//                    treeview_obstacles.getSelectionModel().clearSelection();

                    if (obstacles_selectionnes_dans_arborescence.isEmpty())
                        treeview_obstacles.setContextMenu(null);
                }

            }
        });


        // Initialisation de la liste des socs : rattachement à la liste observable des socs de l'environnement
        listview_socs.setItems(environnement.systemesOptiquesCentres());

        // Intégration dans la vue des éventuels obstacles déjà présents dans l'environnement (peut arriver si on a chargé l'environnement)
        Iterator<SystemeOptiqueCentre> itso = environnement.iterateur_systemesOptiquesCentres() ;
        while (itso.hasNext())
            integrerSystemeOptiqueCentreDansVue(itso.next());

        // Maintenir une référence vers la liste observable des socs actuellement sélectionnées dans la listview
        socs_selectionnes = listview_socs.getSelectionModel().getSelectedItems() ;

        // Brancher ou débrancher le menu contextuel de suppression selon qu'il y a un SOC sélectionné ou non
        // Et mettre le bon panneau de contenu
        socs_selectionnes.addListener((ListChangeListener<SystemeOptiqueCentre>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {

                    listview_sources.getSelectionModel().clearSelection();
                    treeview_obstacles.getSelectionModel().clearSelection();


                    List<? extends SystemeOptiqueCentre> soc_ajoutes = c.getAddedSubList() ;

                    // Afficher le panneau droit correspondant au dernier SOC ajouté dans la sélection
                    scrollpane_droit_element_courant.setContent(map_element_panneau_droit.get(soc_ajoutes.get(soc_ajoutes.size()-1)));
                    // Afficher le panneau bas correspondant au dernier SOC ajouté
                    Node panneau_a_ajouter = map_element_panneau_bas.get(soc_ajoutes.get(soc_ajoutes.size()-1)) ;
                    if (!anchorpane_bas_element_courant.getChildren().contains(panneau_a_ajouter)) {
                        AnchorPane.setTopAnchor(panneau_a_ajouter,1.0);
                        AnchorPane.setBottomAnchor(panneau_a_ajouter,1.0);
                        AnchorPane.setLeftAnchor(panneau_a_ajouter,1.0);
                        AnchorPane.setRightAnchor(panneau_a_ajouter,1.0);

                        anchorpane_bas_element_courant.getChildren().clear();
                        anchorpane_bas_element_courant.getChildren().add(panneau_a_ajouter);

//                    scrollpane_bas_element_courant.setContent(map_element_panneau_bas.get(soc_ajoutes.get(soc_ajoutes.size()-1)));
                    }

                    SystemeOptiqueCentre dernier_soc_de_selection = soc_ajoutes.get(soc_ajoutes.size()-1) ;

                    // Si on est en mode sélection, sélectionner l'objet dans le canvas
                    if (modeCourant()==selection) {
                        canvas_affichage_environnement.selectionneSystemeOptiqueCentre(dernier_soc_de_selection);
                    }

                    if (listview_socs.getContextMenu()==null)
                        listview_socs.setContextMenu(menuContextuelSoc);

                }
                else if (c.wasRemoved())  {
                    scrollpane_droit_element_courant.setContent(panneau_parametres_environnement);
//                    scrollpane_bas_element_courant.setContent(null);
//                    scrollpane_bas_element_courant.getChildren().clear();

//                    listview_sources.getSelectionModel().clearSelection();

                    if (socs_selectionnes.isEmpty())
                        listview_socs.setContextMenu(null);
                }

            }
        });



        // Gestion des "modes" d'ajout : source, segment, demi-plan, etc.
        choix_mode.selectedToggleProperty().addListener((observable, oldValue,newValue) -> {

            LOGGER.log(Level.FINER,"Choix mode passe de {0} à {1}",new Object[] {oldValue,newValue}) ;

            // Ruse pour empêcher la déselection de tous les ToggleButtons :
            if (newValue==null)
                oldValue.setSelected(true);

            if (oldValue == ajout_source && source_en_cours_ajout != null)
                source_en_cours_ajout = null ;

            if (oldValue == selection && canvas_affichage_environnement.obstacleSelectionne() != null)
                canvas_affichage_environnement.deselectionneObstacle();
            if (oldValue == selection && canvas_affichage_environnement.sourceSelectionnee() != null)
                canvas_affichage_environnement.deselectionneSource();
            if (oldValue == selection && canvas_affichage_environnement.systemeOptiqueCentreSelectionne() != null)
                canvas_affichage_environnement.deselectionneSystemeOptiqueCentre();

            if (oldValue != selection && newValue==selection && treeview_obstacles.getSelectionModel().getSelectedItem()!=null)
                canvas_affichage_environnement.selectionneObstacle(treeview_obstacles.getSelectionModel().getSelectedItem().getValue());
            if (oldValue != selection && newValue==selection && listview_sources.getSelectionModel().getSelectedItem()!=null)
                canvas_affichage_environnement.selectionneSource(listview_sources.getSelectionModel().getSelectedItem());
            if (oldValue != selection && newValue==selection && listview_socs.getSelectionModel().getSelectedItem()!=null)
                canvas_affichage_environnement.selectionneSystemeOptiqueCentre(listview_socs.getSelectionModel().getSelectedItem());

        });

        lcl_sources = change -> {
            while (change.next()) {
                //                if (c.wasPermutated())   { for (int i = c.getFrom(); i < c.getTo(); ++i) {  } }
                //                else if (c.wasUpdated()) { for (int i = c.getFrom(); i < c.getTo(); ++i) { /* environnement.sources.get(i) */ } }
                //                else
                if (change.wasRemoved()) {
                    for (Source remitem : change.getRemoved()) {
                        LOGGER.log(Level.FINE,"Source supprimée : {0}",remitem.nom()) ;

                        listview_sources.getSelectionModel().clearSelection();

                    }

                } else if (change.wasAdded()) {
                    for (Source additem : change.getAddedSubList()) {

                        LOGGER.log(Level.FINE,"Source ajoutée : {0}",additem.nom()) ;

                        integrerSourceDansVue(additem);

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

                        TreeItem<Obstacle> tio_a_supprimer = chercheObstacleDansTreeItem(remitem,treeview_obstacles.getRoot()) ;
                        if (tio_a_supprimer!=null && tio_a_supprimer.getParent()!=null)
                            tio_a_supprimer.getParent().getChildren().remove(tio_a_supprimer) ;

                        treeview_obstacles.getSelectionModel().clearSelection();

//                        if (canvas_affichage_environnement.obstacleSelectionne()==remitem)
                            canvas_affichage_environnement.deselectionneObstacle();

                    }
                } else if (change.wasAdded()) {

                    for (Obstacle additem : change.getAddedSubList()) {
                        LOGGER.log(Level.FINE,"Obstacle ajouté : {0}",additem.nom()) ;

                        integrerObstacleDansVue(additem,treeview_obstacles.getRoot());
                    }
                }
            }
        };

        environnement.ajouterListenerListeObstacles(lcl_obstacles);

        lcl_socs = change -> {
            while (change.next()) {
                //                if (c.wasPermutated())   { for (int i = c.getFrom(); i < c.getTo(); ++i) {  } }
                //                else if (c.wasUpdated()) { for (int i = c.getFrom(); i < c.getTo(); ++i) { /* environnement.sources.get(i) */ } }
                //                else
                if (change.wasRemoved()) {
                    for (SystemeOptiqueCentre remitem : change.getRemoved()) {
                        LOGGER.log(Level.FINE,"SOC supprimé : {0}",remitem.nom()) ;

                        listview_socs.getSelectionModel().clearSelection();

                    }

                } else if (change.wasAdded()) {
                    for (SystemeOptiqueCentre additem : change.getAddedSubList()) {

                        LOGGER.log(Level.FINE,"SOC ajouté : {0}",additem.nom()) ;

                        integrerSystemeOptiqueCentreDansVue(additem);

                    }
                }

            }
        };

        environnement.ajouterListenerListeSystemesOptiquesCentres(lcl_socs);







//        new ParaboleGraphique(eg, Obstacle.TypeSurface.CONCAVE, 1,0.0,0.0) ;
//        new ParaboleGraphique(eg, Obstacle.TypeSurface.CONCAVE ,-1,0.0,2.5) ;
//
//        new RectangleGraphique(eg, Obstacle.TypeSurface.CONVEXE,0.4,0.45,0.6,0.55) ;

//        new RectangleGraphique(eg, Obstacle.TypeSurface.CONCAVE,-0.9,0.1,0.9,0.9) ;
//        new ParaboleGraphique(eg, Obstacle.TypeSurface.CONVEXE ,-1,0.0,0.5) ;

//        new CercleGraphique(eg, Obstacle.TypeSurface.CONCAVE,0,1.5,1) ;
//        new RectangleGraphique(eg, Obstacle.TypeSurface.CONVEXE,0.1,0.6,0.2,0.8) ;
//        new SegmentGraphique(eg,0,0,0,1) ;
//        new SegmentGraphique(eg,-0.5,0.4,0.5,0.5) ;

        // Conique : Ellipse avec axe foxal tourné de -45°
//        new ConiqueGraphique(eg, Obstacle.TypeSurface.CONVEXE, Math.sqrt(3.0/4.0),0,1.0,-1.0,0.25,Math.sqrt(3.0/4.0)) ;
//        new ConiqueGraphique(eg, Obstacle.TypeSurface.CONVEXE, Math.sqrt(3.0/4.0),0,1.0,0.0,0.25,1.5) ;

        // Ellipse
//        new ConiqueGraphique(eg, Obstacle.TypeSurface.CONCAVE, Math.sqrt(3.0/4.0)+0.4,0,0.1,0.05,1.2,0.7) ;
//        new ConiqueGraphique(eg, Obstacle.TypeSurface.CONCAVE, Math.sqrt(3.0/4.0),0.0,0,2,0.7) ;

 //       environnement.ajouterObstacle(new Conique(Obstacle.TypeSurface.CONCAVE, Math.sqrt(3.0/4.0),0.0,0,2,0.7)) ;

        // Parabole
//        new ConiqueGraphique(eg, Obstacle.TypeSurface.CONCAVE, Math.sqrt(3.0/4.0),0,0.5,0.3,0.25,1.0) ;

        // Hyperbole
//        new ConiqueGraphique(eg, Obstacle.TypeSurface.CONCAVE, Math.sqrt(3.0/4.0),0,0.6,-0.5,0.25,1.5) ;

        // Petit cercle centré sur le foyer
//        new CercleGraphique(eg, Obstacle.TypeSurface.CONVEXE,Math.sqrt(3.0/4.0),0,0.05) ;
//        Cercle cerc1 = new Cercle(TypeSurface.CONVEXE,0,0,1.0) ;
//        cerc1.definirIndiceRefraction(1.4d);
//        environnement.ajouterObstacle(cerc1) ;
//        Cercle cerc2 = new Cercle(TypeSurface.CONVEXE,1,0,0.8) ;
//        cerc2.definirIndiceRefraction(1.8d);
//        environnement.ajouterObstacle(cerc2) ;
//
//        SystemeOptiqueCentre soc = new SystemeOptiqueCentre(environnement, new Point2D(0d,0d),0d) ;
//        soc.ajouterObstacle(cerc1);
//        soc.ajouterObstacle(cerc2);
//        environnement.ajouterSystemeOptiqueCentre(soc);


//        cerc.definirXcentre(cerc.Xcentre()-0.1);

//        Cercle cerc1 = new Cercle(Obstacle.TypeSurface.CONVEXE,0.6,0,0.9) ;
//        Cercle cerc2 = new Cercle(Obstacle.TypeSurface.CONVEXE,-0.3,0,0.4) ;
//       Composition compo = new Composition(cerc1, Composition.Operateur.DIFFERENCE, cerc2) ;


//        Composition compo = new Composition(co1, Composition.Operateur.DIFFERENCE, co2) ;
//        Composition compo = new Composition(co1, Composition.Operateur.DIFFERENCE_SYMETRIQUE, co2) ;

//        CompositionDeuxObstacles compo = new CompositionDeuxObstacles(co1, Composition.Operateur.INTERSECTION, co2) ;

//        Conique co1 = new Conique( TypeSurface.CONVEXE, 0.0,0.0,300.0,0.53,0.6) ;
//        Conique co2 = new Conique( TypeSurface.CONVEXE, 0.0,0.0,341.0,0.31,0.7) ;
//        Composition compo = new Composition(Composition.Operateur.UNION) ;
//        compo.ajouterObstacle(co1);
//        compo.ajouterObstacle(co2);
//        environnement.ajouterObstacle(compo) ;

    }

    private void setUpDependecyInjector() {
        // TODO plus tard : set bundle
        //DependencyInjection.setBundle(ResourceBundle.getBundle("greetings", Locale.FRENCH));

        // Il est possible que l'Application (i.e. la classe CrazyDiamond) ait déjà injecté sa propre méthode fabrique
        // de création de contrôleur pour PanneauPrincipal, qui récupère l'environnement depuis son attribut CrazyDiamond.environnement_initial_a_charger.
        // On la remplace par une méthode fabrique qui va maintenant récupérer l'environnement du PanneauPrincipal courant et le passer
        // en paramètre du constructeur d'un nouveau PanneauPrincipal, créé lorsqu'on charge un nouvel environnement depuis un fichier.
        DependencyInjection.removeInjectionMethod(PanneauPrincipal.class);

        // Create factory
        Callable<?> controleurPanneauPrincipalFactory = () -> new PanneauPrincipal(nouveau_canvas_affichage_environnement);

        // Save the factory in the injector
        DependencyInjection.addInjectionMethod(PanneauPrincipal.class, controleurPanneauPrincipalFactory);


        //create factory
        Callable<?> controleurPanneauSourceFactory = () -> new PanneauSource(source_en_attente_de_panneau , canvas_affichage_environnement);

        //save the factory in the injector
        DependencyInjection.addInjectionMethod(PanneauSource.class, controleurPanneauSourceFactory) ;

        Callable<?> controleurPanneauParametresAffichageEnvironnementFactory = () -> new PanneauParametresAffichageEnvironnement(canvas_affichage_environnement);

        DependencyInjection.addInjectionMethod(PanneauParametresAffichageEnvironnement.class, controleurPanneauParametresAffichageEnvironnementFactory) ;


        Callable<?> controleurPanneauDemiPlanFactory = () -> new PanneauDemiPlan((DemiPlan)obstacle_en_attente_de_panneau,obstacle_en_attente_de_panneau_dans_composition,canvas_affichage_environnement);

        DependencyInjection.addInjectionMethod(PanneauDemiPlan.class, controleurPanneauDemiPlanFactory) ;

        Callable<?> controleurPanneauSegmentFactory = () -> new PanneauSegment((Segment)obstacle_en_attente_de_panneau,obstacle_en_attente_de_panneau_dans_composition,canvas_affichage_environnement);

        DependencyInjection.addInjectionMethod(PanneauSegment.class, controleurPanneauSegmentFactory) ;

        Callable<?> controleurPanneauPrismeFactory = () -> new PanneauPrisme((Prisme)obstacle_en_attente_de_panneau,obstacle_en_attente_de_panneau_dans_composition,canvas_affichage_environnement);

        DependencyInjection.addInjectionMethod(PanneauPrisme.class, controleurPanneauPrismeFactory) ;

        Callable<?> controleurPanneauRectangleFactory = () -> new PanneauRectangle((Rectangle)obstacle_en_attente_de_panneau,obstacle_en_attente_de_panneau_dans_composition,canvas_affichage_environnement);

        DependencyInjection.addInjectionMethod(PanneauRectangle.class, controleurPanneauRectangleFactory) ;

        Callable<?> controleurPanneauCercleFactory = () -> new PanneauCercle((Cercle)obstacle_en_attente_de_panneau,obstacle_en_attente_de_panneau_dans_composition,canvas_affichage_environnement);

        DependencyInjection.addInjectionMethod(PanneauCercle.class, controleurPanneauCercleFactory) ;

        Callable<?> controleurPanneauConiqueFactory = () -> new PanneauConique((Conique)obstacle_en_attente_de_panneau,obstacle_en_attente_de_panneau_dans_composition,canvas_affichage_environnement);

        DependencyInjection.addInjectionMethod(PanneauConique.class, controleurPanneauConiqueFactory) ;

        Callable<?> controleurPanneauCompositionFactory = () -> new PanneauComposition((Composition) obstacle_en_attente_de_panneau,obstacle_en_attente_de_panneau_dans_composition,canvas_affichage_environnement);

        DependencyInjection.addInjectionMethod(PanneauComposition.class, controleurPanneauCompositionFactory) ;

        Callable<?> controleurPanneauSystemeOptiqueCentre = () -> new PanneauSystemeOptiqueCentre(soc_en_attente_de_panneau,canvas_affichage_environnement);

        DependencyInjection.addInjectionMethod(PanneauSystemeOptiqueCentre.class, controleurPanneauSystemeOptiqueCentre) ;

        Callable<?> controleurPanneauAnalyseParaxialeSystemeOptiqueCentre = () -> new PanneauAnalyseParaxialeSystemeOptiqueCentre(soc_en_attente_de_panneau,canvas_affichage_environnement);

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

        Point2D pclic = canvas_affichage_environnement.gc_vers_g(me.getX(),me.getY()) ;

        LOGGER.log(Level.FINER,"Clic en ({0},{1})",new Object[] {pclic.getX(),pclic.getY()});


        // Note : la gestion du mode sélection se fait dans le handler traiterBoutonSourisPresse
        if ( modeCourant() == ajout_source )
            traiterClicSourisPourAjoutSource(pclic);
        else if (modeCourant() == ajout_demi_plan)
            traiterClicSourisPourAjoutDemiPlan(pclic);
        else if (modeCourant() == ajout_segment)
            traiterClicSourisPourAjoutSegment(pclic);
        else if (modeCourant() == ajout_prisme)
            traiterClicSourisPourAjoutPrisme(pclic);
        else if (modeCourant() == ajout_rectangle)
            traiterClicSourisPourAjoutRectangle(pclic);
        else if (modeCourant() == ajout_cercle)
            traiterClicSourisPourAjoutCercle(pclic);
        else if (modeCourant() == ajout_conique)
            traiterClicSourisPourAjoutConique(pclic);
        else if (modeCourant() == ajout_axe_soc)
            traiterClicSourisPourAjoutSystemeOptiqueCentre(pclic);

        if (modeCourant() != selection) {
            canvas_affichage_environnement.deselectionneObstacle();
            canvas_affichage_environnement.deselectionneSource();
        }

        if (modeCourant() == selection && canvas_affichage_environnement.obstacleSelectionne() != null) {
            if (!retaillage_selection_en_cours) { // On commence un retaillage
                if (canvas_affichage_environnement.poignee_obstacle_pointee_en(pclic)) {
                    LOGGER.log(Level.FINE, "Poignée sélectionnée");
                    retaillage_selection_en_cours = true;
                }
                else
                    LOGGER.log(Level.FINE, "Poignée non sélectionnée");

//                    retaillage_selection_en_cours = false ;
            } else { // Retaillage de sélection était en cours : on le termine
                canvas_affichage_environnement.obstacleSelectionne().retaillerSelectionPourSourisEn(pclic);
                retaillage_selection_en_cours = false ;
            }
        }  else if (modeCourant() == selection && canvas_affichage_environnement.sourceSelectionnee() != null) {
            if (!retaillage_selection_en_cours) { // On commence un retaillage
                if (canvas_affichage_environnement.poignee_source_pointee_en(pclic)) {
                    LOGGER.log(Level.FINE, "Poignée source sélectionnée");
                    retaillage_selection_en_cours = true;
                }
                else
                    LOGGER.log(Level.FINE, "Poignée source non sélectionnée");

//                    retaillage_selection_en_cours = false ;
            } else { // Retaillage de sélection était en cours : on le termine
                canvas_affichage_environnement.source_selectionnee.retaillerPourSourisEn(pclic);
                retaillage_selection_en_cours = false ;
            }
        }


    }

    private void traiterClicSourisPourAjoutSystemeOptiqueCentre(Point2D pclic) {

        if (soc_en_cours_ajout == null) {

            // Création d'un nouveau SOC
            soc_en_cours_ajout = new SystemeOptiqueCentre(environnement,pclic,0.0) ;

            return ;
        }

        // On est sur le 2ème clic qui fige l'orientation du SOC en cours d'ajout
        Point2D direction = pclic.subtract(soc_en_cours_ajout.origine()) ;

        if (direction.magnitude()==0.0)
            return;

        soc_en_cours_ajout.definirDirection(pclic.subtract(soc_en_cours_ajout.origine()));

        soc_en_cours_ajout = null ;

    }


    protected void traiterClicSourisPourAjoutSource(Point2D  pclic) {

        if (source_en_cours_ajout == null) { // On vient de commencer le tracé d'une nouvelle source

            // Création d'une nouvelle source
            source_en_cours_ajout = new Source(environnement, pclic, 0.0, Source.TypeSource.PINCEAU);

            return ;
        }

        // On est donc sur le 2ème clic, qui fige la direction de la source

        Point2D direction = pclic.subtract(source_en_cours_ajout.position()) ;

        if (direction.magnitude()==0.0)
            return;

        source_en_cours_ajout.definirDirection(pclic.subtract(source_en_cours_ajout.position()));

        source_en_cours_ajout = null ;

    }

    protected void traiterClicSourisPourAjoutDemiPlan(Point2D pclic) {

        if (obstacle_en_cours_ajout == null) { // On vient de commencer le tracé d'un nouveau segment

            // Création d'un nouveau demiplan
            obstacle_en_cours_ajout = new DemiPlan(TypeSurface.CONVEXE,pclic.getX(), pclic.getY(),0.0) ;

            return ;
        }

        obstacle_en_cours_ajout.retaillerPourSourisEn(pclic);

        // Enregistrer l'obstacle courant dans l'environnement (si pas déjà fait suite à un mouvement de la souris :
        // cette méthode ne fait rien si la source est déjà ajoutée)
        environnement.ajouterObstacle(obstacle_en_cours_ajout);

        obstacle_en_cours_ajout = null ;

    }

    protected void traiterClicSourisPourAjoutSegment(Point2D pclic) {

        if (obstacle_en_cours_ajout == null) { // On vient de commencer le tracé d'un nouveau segment

            // Création d'un nouveau segment
            obstacle_en_cours_ajout = new Segment(pclic.getX(), pclic.getY(), canvas_affichage_environnement.resolution(), 0d,0d) ;

            return ;
        }

        obstacle_en_cours_ajout.retaillerPourSourisEn(pclic);

        // Enregistrer la source courante dans l'environnement (si pas déjà fait suite à un mouvement de la souris :
        // cette méthode ne fait rien si la source est déjà ajoutée)
         environnement.ajouterObstacle(obstacle_en_cours_ajout);

         obstacle_en_cours_ajout = null ;

    }

    protected void traiterClicSourisPourAjoutPrisme(Point2D pclic) {
        if (obstacle_en_cours_ajout == null) { // On vient de commencer le tracé d'un nouveau rectangle

            // Création d'un nouveau prisme
            obstacle_en_cours_ajout = new Prisme(TypeSurface.CONVEXE, pclic.getX(), pclic.getY(), 60, canvas_affichage_environnement.resolution(),0.0) ;

            return ;
        }

        obstacle_en_cours_ajout.retaillerPourSourisEn(pclic);

        // Enregistrer la source courante dans l'environnement (si pas déjà fait suite à un mouvement de la souris :
        // cette méthode ne fait rien si la source est déjà ajoutée)
        environnement.ajouterObstacle(obstacle_en_cours_ajout);

        obstacle_en_cours_ajout = null ;
    }

    protected void traiterClicSourisPourAjoutRectangle(Point2D pclic) {
        if (obstacle_en_cours_ajout == null) { // On vient de commencer le tracé d'un nouveau rectangle

            // Création d'un nouveau rectangle
            obstacle_en_cours_ajout = new Rectangle(TypeSurface.CONVEXE, pclic.getX(), pclic.getY(), pclic.getX()+canvas_affichage_environnement.resolution(), pclic.getY()-canvas_affichage_environnement.resolution(),0.0) ;

            return ;
        }

        obstacle_en_cours_ajout.retaillerPourSourisEn(pclic);

        // Enregistrer la source courante dans l'environnement (si pas déjà fait suite à un mouvement de la souris :
        // cette méthode ne fait rien si la source est déjà ajoutée)
        environnement.ajouterObstacle(obstacle_en_cours_ajout);

        obstacle_en_cours_ajout = null ;
    }

    private void traiterClicSourisPourAjoutCercle(Point2D pclic) {
        if (obstacle_en_cours_ajout == null) { // On vient de commencer le tracé d'un nouveau cercle

            // Création d'un nouveau cercle
            obstacle_en_cours_ajout = new Cercle(TypeSurface.CONVEXE, pclic.getX(), pclic.getY(),canvas_affichage_environnement.resolution()) ;

            return ;
        }

        obstacle_en_cours_ajout.retaillerPourSourisEn(pclic);

        // Enregistrer la source courante dans l'environnement (si pas déjà fait suite à un mouvement de la souris :
        // cette méthode ne fait rien si la source est déjà ajoutée)
        environnement.ajouterObstacle(obstacle_en_cours_ajout);

        obstacle_en_cours_ajout = null ;
    }

    private void traiterClicSourisPourAjoutConique(Point2D pclic) {
        if (obstacle_en_cours_ajout == null) { // On vient de commencer le tracé d'un nouveau cercle

            // Création d'un nouveau rectangle
            obstacle_en_cours_ajout = new Conique(TypeSurface.CONVEXE, pclic.getX(), pclic.getY(), 0.0,canvas_affichage_environnement.resolution(),1.0);

            return ;
        }

        obstacle_en_cours_ajout.retaillerPourSourisEn(pclic);

        // Enregistrer la source courante dans l'environnement (si pas déjà fait suite à un mouvement de la souris :
        // cette méthode ne fait rien si la source est déjà ajoutée)
        environnement.ajouterObstacle(obstacle_en_cours_ajout);

        obstacle_en_cours_ajout = null ;

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

    protected void integrerSystemeOptiqueCentreDansVue(SystemeOptiqueCentre s) {

        // Rafraichissement automatique de la liste des socs quand le nom du SOC change
        ChangeListener<String> listenerNom = (obs, oldName, newName) -> listview_socs.refresh();
        s.nomProperty().addListener(listenerNom);

        Parent panneau_droit_soc_courant = null ;

        LOGGER.log(Level.FINE,"Tentative de chargement du PanneauSystemeOptiqueCentre") ;

        soc_en_attente_de_panneau = s ;

        try {
            panneau_droit_soc_courant = DependencyInjection.load("View/PanneauSystemeOptiqueCentre.fxml");
            LOGGER.log(Level.FINE,"PanneauSystemeOptiqueCentre créé : {0}",panneau_droit_soc_courant) ;
        } catch (IOException e) {
            System.err.println("Exception lors de l'accès au fichier .fxml : "+e.getMessage());
            System.exit(1);
        }

        map_element_panneau_droit.put(s,panneau_droit_soc_courant) ;

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

        map_element_panneau_bas.put(s,panneau_bas_soc_courant) ;

//        scrollpane_bas_element_courant.getChildren().set(0,panneau_bas_soc_courant);

        if (!anchorpane_bas_element_courant.getChildren().contains(panneau_bas_soc_courant)) {
            AnchorPane.setTopAnchor(panneau_bas_soc_courant,1.0);
            AnchorPane.setBottomAnchor(panneau_bas_soc_courant,1.0);
            AnchorPane.setLeftAnchor(panneau_bas_soc_courant,1.0);
            AnchorPane.setRightAnchor(panneau_bas_soc_courant,1.0);

            anchorpane_bas_element_courant.getChildren().clear();
            anchorpane_bas_element_courant.getChildren().add(panneau_bas_soc_courant);

        }

//        scrollpane_bas_element_courant.setContent(panneau_bas_soc_courant);


    }


    /**
     * Ajoute un obstacle dans l'arbre des obstacles et met en place un Listener sur son nom pour assurer sa mise à jour
     * automatique dans l'arbre.
     * @param parent : Le TreeItem<Obstacle> sous lequel on souhaite ajouter l'obstacle
     * @param o_a_ajouter : L'obstacle à ajouter
     */
    protected TreeItem<Obstacle> ajouterObstacleDansArbre(TreeItem<Obstacle> parent, Obstacle o_a_ajouter) {

        TreeItem<Obstacle> tio = new TreeItem<>(o_a_ajouter) ;

        parent.getChildren().add(tio) ;

        // Mise sur écoute du nom de l'obstacle pour mettre à jour l'item de l'arbre quand il est modifié
        ChangeListener<String> listenerNom = (obs, oldName, newName) -> {
            TreeItem.TreeModificationEvent<Obstacle> event = new TreeItem.TreeModificationEvent<>(TreeItem.valueChangedEvent(), tio);
            Event.fireEvent(tio, event);
        };
        o_a_ajouter.nomProperty().addListener(listenerNom);

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
            System.err.println("Exception lors de l'accès au fichier .fxml : Message : "+e.getMessage());
            System.err.println("Exception lors de l'accès au fichier .fxml : Cause : "+e.getCause());
            System.exit(1);
        }

        // Enregistrer le nouveau panneau
        map_element_panneau_droit.put(o,panneau_obstacle_courant) ;

        LOGGER.log(Level.FINE,"Ajout de {0} associé au panneau {1} dans la map", new Object[] {o,panneau_obstacle_courant});

        return  panneau_obstacle_courant ;
    }

    protected void integrerObstacleDansVue(Obstacle o, TreeItem<Obstacle> parent) {

        creerPanneauSimplePourObstacle(o, parent != treeview_obstacles.getRoot()) ;

        TreeItem<Obstacle> tio = ajouterObstacleDansArbre(parent,o);

        if (o.getClass()==Composition.class) {
             ObservableList<Obstacle> obstacles = ((Composition) o).elements() ;

            for (Obstacle oi : obstacles) {
                integrerObstacleDansVue(oi,tio);
            }
        }

    }


    private TreeItem<Obstacle> chercheObstacleDansTreeItem(Obstacle o_a_trouver, TreeItem<Obstacle> tio) {

        TreeItem<Obstacle> resultat = null ;

        for (TreeItem<Obstacle> tio_fils : tio.getChildren()) {
            if (tio_fils.getValue()==o_a_trouver) // Bingo !
                return tio_fils ; // Pas besoin de chercher plus loin

            if (!tio_fils.isLeaf()) { // Descente dans le fils si ce n'est pas une feuille de l'arbre

                resultat = chercheObstacleDansTreeItem(o_a_trouver,tio_fils) ;

                if (resultat!=null) // Bingo !
                        return resultat ;
            }
        }

        return resultat ; // Forcément null

    }

    @FXML
    public void traiterDeplacementSourisCanvas(MouseEvent me) {

        Point2D pos_souris = canvas_affichage_environnement.gc_vers_g(me.getX(),me.getY()) ;


        // Affichage des infos en bas de l'écran

//        // TODO : Adapter le nombre de décimales au facteur de zoom
//        String coord = String.format("(X : %.4f , Y : %.4f)",pos_souris.getX(),pos_souris.getY()) ;
//        label_droit.setText(coord);

        String sb = "(X : "
                + canvas_affichage_environnement.convertisseurAffichageDistance().toString(pos_souris.getX())
                + " , Y : "
                + canvas_affichage_environnement.convertisseurAffichageDistance().toString(pos_souris.getY())

                + ") " + environnement.unite().symbole;

        label_droit.setText(sb);

        Obstacle obs = canvas_affichage_environnement.obstacle_pointe_en(pos_souris) ;
        Source src;

        if (obs!=null) {
            label_gauche.setText(obs.nom() + " (" + obs.natureMilieu().toString().toLowerCase() + (obs.natureMilieu() == NatureMilieu.TRANSPARENT ? " n=" + obs.indiceRefraction()+")":")"));
        } else {
            src = canvas_affichage_environnement.source_pointee_en(pos_souris) ;
            label_gauche.setText(src != null ? src.nom() + " (" + src.type().toString().toLowerCase()+")" : "-");

        }

//        if (source_en_cours_ajout == null && obstacle_en_cours_ajout == null && !retaillage_selection_en_cours)
//            return;

        // Si c'est un glisser, on oublie le point de départ potentiel
        if (glisser_en_cours) {
            source_en_cours_ajout = null ;
            return ;
        }


        if (source_en_cours_ajout !=null) {
            Point2D direction = pos_souris.subtract(source_en_cours_ajout.position());

            if (direction.magnitude() == 0.0)
                return;

            source_en_cours_ajout.definirDirection(pos_souris.subtract(source_en_cours_ajout.position()));

            environnement.ajouterSource(source_en_cours_ajout); // Ne fait rien si source_courante est déjà dans l'environnement

            listview_sources.getSelectionModel().select(source_en_cours_ajout);
        }

        if (obstacle_en_cours_ajout !=null) {
            obstacle_en_cours_ajout.retaillerPourSourisEn(pos_souris);

            // Ajouter l'obstacle dans l'environnement, si pas déjà fait (cette méthode ne fait rien si l'obstacle est déjà ajoutée)
            environnement.ajouterObstacle(obstacle_en_cours_ajout);

            treeview_obstacles.getSelectionModel().select(chercheObstacleDansTreeItem(obstacle_en_cours_ajout,treeview_obstacles.getRoot()));
        }

        if (soc_en_cours_ajout !=null) {
            Point2D direction = pos_souris.subtract(soc_en_cours_ajout.origine());

            if (direction.magnitude() == 0.0)
                return;

            soc_en_cours_ajout.definirDirection(pos_souris.subtract(soc_en_cours_ajout.origine()));

            environnement.ajouterSystemeOptiqueCentre(soc_en_cours_ajout); // Ne fait rien si source_courante est déjà dans l'environnement

            listview_socs.getSelectionModel().select(soc_en_cours_ajout);
        }


        if (canvas_affichage_environnement.obstacleSelectionne() !=null && retaillage_selection_en_cours) {
            canvas_affichage_environnement.obstacleSelectionne().retaillerSelectionPourSourisEn(pos_souris);
        } else if (canvas_affichage_environnement.sourceSelectionnee() !=null && retaillage_selection_en_cours) {
            canvas_affichage_environnement.sourceSelectionnee().retaillerPourSourisEn(pos_souris);
        } else if (canvas_affichage_environnement.systemeOptiqueCentreSelectionne() !=null && retaillage_selection_en_cours) {
            canvas_affichage_environnement.systemeOptiqueCentreSelectionne().retaillerPourSourisEn(pos_souris);
        }


    }

    public Toggle modeCourant() {
        return choix_mode.selectedToggleProperty().getValue() ;
    }

    public void traiterBoutonSourisPresse(MouseEvent mouseEvent) {

        LOGGER.log(Level.FINER,"Bouton souris pressé") ;

        glisser_juste_termine = false ;

        // C'est peut-être le début d'un glisser de souris : enregistrer la position de début de glisser
        p_debut_glisser = new Point2D(mouseEvent.getX(),mouseEvent.getY());

        if (modeCourant()==selection) {
            Point2D pclic = canvas_affichage_environnement.gc_vers_g(mouseEvent.getX(), mouseEvent.getY());

            Obstacle o_avant = canvas_affichage_environnement.obstacleSelectionne() ;
            Source   s_avant = canvas_affichage_environnement.sourceSelectionnee() ;
            SystemeOptiqueCentre   soc_avant = canvas_affichage_environnement.systemeOptiqueCentreSelectionne() ;

            Obstacle o_pointe  = canvas_affichage_environnement.obstacle_pointe_en(pclic) ;
            Source   s_pointee = canvas_affichage_environnement.source_pointee_en(pclic) ;
            SystemeOptiqueCentre   soc_pointe = canvas_affichage_environnement.soc_pointe_en(pclic) ;

            if (!retaillage_selection_en_cours) {
                if (s_pointee!=null) {
                    canvas_affichage_environnement.deselectionneObstacle();
                    canvas_affichage_environnement.deselectionneSystemeOptiqueCentre();

                    canvas_affichage_environnement.selectionneSource(s_pointee);
                }
                else if (o_pointe!=null) {
                    canvas_affichage_environnement.deselectionneSource();
                    canvas_affichage_environnement.deselectionneSystemeOptiqueCentre();

                    canvas_affichage_environnement.selectionneObstacle(o_pointe);
                } else if (soc_pointe!=null) {
                    canvas_affichage_environnement.deselectionneSource();
                    canvas_affichage_environnement.deselectionneObstacle();

                    canvas_affichage_environnement.selectionneSystemeOptiqueCentre(soc_pointe);
                } else {
                    canvas_affichage_environnement.deselectionneSource();
                    canvas_affichage_environnement.deselectionneObstacle();
                    canvas_affichage_environnement.deselectionneSystemeOptiqueCentre();
                }
            }

            if (canvas_affichage_environnement.obstacleSelectionne()!=o_avant) {
                treeview_obstacles.getSelectionModel().select(chercheObstacleDansTreeItem(canvas_affichage_environnement.obstacleSelectionne(), treeview_obstacles.getRoot()));
            }
            if (canvas_affichage_environnement.sourceSelectionnee()!=s_avant) {
                listview_sources.getSelectionModel().select(canvas_affichage_environnement.sourceSelectionnee());
            }
            if (canvas_affichage_environnement.systemeOptiqueCentreSelectionne()!=soc_avant) {
                listview_socs.getSelectionModel().select(canvas_affichage_environnement.systemeOptiqueCentreSelectionne());
            }

        }

    }

    public void traiterGlisserSourisCanvas(MouseEvent mouseEvent) {

        glisser_en_cours = true ;

        canvas_affichage_environnement.getScene().setCursor(Cursor.MOVE);

        Point2D p_fin_glisser = new Point2D(mouseEvent.getX(),mouseEvent.getY());

        Point2D p_debut_glisser_g = canvas_affichage_environnement.gc_vers_g(p_debut_glisser.getX(),p_debut_glisser.getY());
        Point2D p_fin_glisser_g   = canvas_affichage_environnement.gc_vers_g(p_fin_glisser.getX(),p_fin_glisser.getY());

        Point2D v_glisser_g = p_fin_glisser_g.subtract(p_debut_glisser_g) ;

        // La position actuelle de la souris devient le nouveau point de début de glisser
        p_debut_glisser = p_fin_glisser ;

        if (modeCourant() == selection && canvas_affichage_environnement.obstacleSelectionne() != null) {

            Obstacle ob_select = canvas_affichage_environnement.obstacleSelectionne() ;
            SystemeOptiqueCentre soc_contenant = environnement.systemeOptiqueCentreContenant(ob_select);

            if (soc_contenant==null)
                ob_select.translater(v_glisser_g);
            else {
                Point2D vec_dir = soc_contenant.vecteurDirecteurAxe() ;
                ob_select.translater(vec_dir.multiply(vec_dir.dotProduct(v_glisser_g)));
            }

//            canvas_affichage_environnement.obstacleSelectionne().translater(v_glisser_g);
//            canvas_affichage_environnement.deselectionneObstacle();
        } else if (modeCourant() == selection && canvas_affichage_environnement.sourceSelectionnee() != null) {
            canvas_affichage_environnement.sourceSelectionnee().translater(v_glisser_g);
        } else if (modeCourant() == selection && canvas_affichage_environnement.systemeOptiqueCentreSelectionne() != null) {
            canvas_affichage_environnement.systemeOptiqueCentreSelectionne().translater(v_glisser_g);
        } else {
            canvas_affichage_environnement.translaterLimites(v_glisser_g.getX(),v_glisser_g.getY());
        }

        canvas_affichage_environnement.rafraichirAffichage();

    }

    public void traiterBoutonSourisRelache(MouseEvent mouseEvent) {

        // Ne rien à faire si ce n'est pas la fin d'un glisser
        if (!glisser_en_cours)
            return ;

        // Dans cette méthode, on ne traite que la fin d'un glisser
        glisser_en_cours = false ;
        glisser_juste_termine = true ;

        // Si un rayon était en cours de placement, on l'oublie
        source_en_cours_ajout = null ;

        canvas_affichage_environnement.getScene().setCursor(Cursor.DEFAULT);

        Point2D p_fin_glisser = new Point2D(mouseEvent.getX(),mouseEvent.getY());

        Point2D p_debut_glisser_g = canvas_affichage_environnement.gc_vers_g(p_debut_glisser.getX(),p_debut_glisser.getY());
        Point2D p_fin_glisser_g   = canvas_affichage_environnement.gc_vers_g(p_fin_glisser.getX(),p_fin_glisser.getY());

        Point2D v_glisser_g = p_fin_glisser_g.subtract(p_debut_glisser_g) ;

        // Etait-on en train de déplacer un obstacle sélectionné ?
        if (modeCourant() == selection && canvas_affichage_environnement.obstacleSelectionne() != null) {

            // Autre approche à étudier :
            //   ob_select.translater(v_glisser_g);
            //   ...et mettre les lignes ci-dessous dans la méthode translater() de chaque obstacle
            // Ou alors :
            // Lorsqu'un obstacle est mis dans un SOC, lui ajouter un listener spécial sur x_centre, y_centre pour
            // calculer l'un en fonction de l'autre, le long de l'axe du SOC : mais si le déplacement est perpendiculaire
            // à l'axe, comment faire pour bloquer le déplacement ??

            Obstacle ob_select = canvas_affichage_environnement.obstacleSelectionne() ;
            SystemeOptiqueCentre soc_contenant = environnement.systemeOptiqueCentreContenant(ob_select);

            if (soc_contenant==null)
                ob_select.translater(v_glisser_g);
            else {
                Point2D vec_dir = soc_contenant.vecteurDirecteurAxe() ;
                ob_select.translater(vec_dir.multiply(vec_dir.dotProduct(v_glisser_g)));
            }

//            canvas_affichage_environnement.deselectionneObstacle();
        }
        // Etait-on en train de déplacer une source sélectionnée ?
        else if (modeCourant() == selection && canvas_affichage_environnement.sourceSelectionnee() != null) {
            canvas_affichage_environnement.sourceSelectionnee().translater(v_glisser_g);
        // Etait-on en train de déplacer une source sélectionnée ?
        } else if (modeCourant() == selection && canvas_affichage_environnement.systemeOptiqueCentreSelectionne() != null) {
            canvas_affichage_environnement.systemeOptiqueCentreSelectionne().translater(v_glisser_g);
        // Sinon, aucun élément n'était sélectionné : on était en train de déplacer la zone visible
        } else {
            canvas_affichage_environnement.translaterLimites(v_glisser_g.getX(), v_glisser_g.getY());

            canvas_affichage_environnement.rafraichirAffichage();
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
            // Rechercher si l'obstacle o implemente l'interface ElementAvecMatiere car eux seuls peuvent faire partie d'une composition
            if (o instanceof ElementAvecMatiere)
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
        listview_socs.getSelectionModel().clearSelection();

        scrollpane_droit_element_courant.setContent(panneau_parametres_environnement);

    }

    public void traiterDefinitionParametresAffichage() {
        listview_sources.getSelectionModel().clearSelection();
        treeview_obstacles.getSelectionModel().clearSelection();
        listview_socs.getSelectionModel().clearSelection();

        scrollpane_droit_element_courant.setContent(panneau_parametres_affichage_environnement);
    }


    public void traiterNouvelEnvironnement() {

        nouveau_canvas_affichage_environnement = new CanvasAffichageEnvironnement(new Environnement()) ;
//        nouvel_environnement = new Environnement() ;

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
        File fichier_a_charger = fileChooser.showOpenDialog(canvas_affichage_environnement.getScene().getWindow());

        if (fichier_a_charger == null)
            return ;

        try {

            ContextAttributes ca = ContextAttributes.getEmpty() ;
            ca = ca.withSharedAttribute("largeur_graphique", canvas_affichage_environnement.largeurGraphique()) ;
            ca = ca.withSharedAttribute("hauteur_graphique", canvas_affichage_environnement.hauteurGraphique()) ;

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

        if (nouvelle_racine==null)
            return ;

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
            window.setUserData(fileChooser.showSaveDialog(racine.getScene().getWindow()));
            // oldTODO : il doit y avoir un moyen plus direct de retrouver la Fenêtre principale...
//            fichier_sauvegarde = fileChooser.showSaveDialog(canvas_affichage_environnement.getScene().getWindow());
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

        // oldTODO : il doit y avoir un moyen plus direct de retrouver la Fenêtre principale...
//        fichier_sauvegarde = fileChooser.showSaveDialog(canvas_affichage_environnement.getScene().getWindow());

        if (window.getUserData() == null)
            return;

        sauvegarderEnvironnement();
    }

    private void sauvegarderEnvironnement() {
        String json = null ;

        try {
//            json.append(jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(environnement));
            json = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(canvas_affichage_environnement);
        } catch (JsonProcessingException e) {
            LOGGER.log(Level.SEVERE,"Exception lors de la sérialisation en JSON de l'Environnement ou des propriétés d'afffichage associées : ",e);

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Impossible de sauvegarder l'environnement");
            alert.setContentText(e.getMessage()+System.lineSeparator()+e.getCause());
            alert.showAndWait();
            return ;
        }

//        System.out.println(json);

        Window window = racine.getScene().getWindow() ;

        FileOutputStream fos = null;
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
        File fichier_a_charger = fileChooser.showOpenDialog(canvas_affichage_environnement.getScene().getWindow());

        if (fichier_a_charger == null)
            return ;

        try {

            ContextAttributes ca = ContextAttributes.getEmpty() ;
            // Passage d'un environnement hote dans lequel l'ObjectReader va ajouter les éléments importables du fichier
            ca = ca.withSharedAttribute("environnement_hote", environnement) ;

//            ObjectReader or = jsonMapper.readerFor(CanvasAffichageEnvironnement.class).with(ca) ;
//            or.readValue(fichier_a_charger,CanvasAffichageEnvironnement.class) ;

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
