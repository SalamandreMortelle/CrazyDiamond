package CrazyDiamond.Controller;

import CrazyDiamond.Model.*;

import clipper2.core.PathD;
import clipper2.core.PointD;
import javafx.beans.property.*;
import javafx.collections.ListChangeListener;
import javafx.geometry.BoundingBox;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.scene.transform.Affine;
import javafx.stage.Screen;

import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

// Cette classe est à la fois la Vue et le Controleur qui permet d'afficher un Environnement dans un Canvas JavaFX
// redimensionnable et dynamique lié (via des Bindings) à tous les objets (sources+obstacles+socs) de l'Environnement.
public class CanvasAffichageEnvironnement extends ResizeableCanvas {

    // Modèle
    protected Environnement environnement;

    /**
     * Liste des sources, obstacles et socs sélectionnés dans ce canvas
     */
    protected ElementsSelectionnes selection ;

    // Rectangle des limites géométriques de l'environnement, en unités de l'environnement
    private BoiteLimiteGeometrique boite_limites ;


    // Récupération du logger
    private static final Logger LOGGER = Logger.getLogger( "CrazyDiamond" );

    private final VisiteurAffichageEnvironnement visiteur_affichage ;

    // Limites par défaut de la zone d'intérêt (zone visible)

    /**
     * Abscisses minimales et maximales de la zone visible géométrique exprimées en unités de l'Environnement.
     */
    protected static double xmin_g_par_defaut = -2.0 , xmax_g_par_defaut = 2.0 ;

    /** Ordonnée y du milieu de la zone visible. Sa hauteur sera calculée de manière à respecter le ratio d'aspect
     *  graphique (largeur_graphique/hauteur_graphique)
      */
    protected static double ycentre_g_par_defaut = 0.0 ;

    protected static double largeur_graphique_par_defaut = 800 ;
    protected static double hauteur_graphique_par_defaut = 600 ;

    private static final boolean normales_visibles_par_defaut = false ;
    private static final boolean prolongements_avant_visibles_par_defaut = false ;
    private static final boolean prolongements_arriere_visibles_par_defaut = false ;
    private static final boolean commentaire_visible_par_defaut = false ;
    private static final Color couleur_normales_par_defaut = Color.GREEN ;


    protected double largeur_graphique ;
    protected double hauteur_graphique ;

    // Transformation pour passer des coordonnées écran aux coordonnées géométriques
    Affine transformation_inverse ;

    protected double resolution_x ;
    protected double resolution_y ;

    protected DoubleProperty resolution ;

    protected final BooleanProperty normales_visibles ;
    protected final ObjectProperty<Color> couleur_normales ;
    protected final BooleanProperty  prolongements_avant_visibles ;
    protected final BooleanProperty  prolongements_arriere_visibles ;
    protected final BooleanProperty  commentaire_visible ;

    protected Text texte_commentaire ;

    protected final MapProperty<SystemeOptiqueCentre,Boolean> montrer_plans_focaux_de_soc ;
    protected final MapProperty<SystemeOptiqueCentre,Boolean> montrer_plans_principaux_de_soc ;
    protected final MapProperty<SystemeOptiqueCentre,Boolean> montrer_plans_nodaux_de_soc ;

    protected ConvertisseurDoubleValidantAffichageDistance convertisseur_affichage_distance;
    private boolean suspendre_rafraichir_decor = false ;

    private BoiteLimiteGeometrique zone_selection_rectangulaire;

    public Color couleurNormales() { return couleur_normales.get() ; }

    public void definirCouleurNormales(Color value) {
        couleur_normales.setValue(value);
    }

    public boolean normalesVisibles() { return normales_visibles.get() ;  }
    public void definirNormalesVisibles(boolean b) { normales_visibles.set(b) ;  }
    public boolean prolongementsAvantVisibles() { return prolongements_avant_visibles.get() ;  }
    public void definirProlongementsAvantVisibles(boolean b) { prolongements_avant_visibles.set(b) ;  }
    public boolean prolongementsArriereVisibles() { return prolongements_arriere_visibles.get() ;  }
    public void definirProlongementsArriereVisibles(boolean b) { prolongements_arriere_visibles.set(b) ;  }
    public boolean commentaireVisible() { return commentaire_visible.get() ;  }
    public void definirCommentaireVisible(boolean b) { commentaire_visible.set(b) ;  }


    /**
     * Créée un Canvas graphique (javafx.scene.canvas) d'Affichage d'un Environnement e de dimensions graphiques
     * larg_gc x haut_gc montrant l'environnement entre les abscisses géométriques xmin_g et xmax_g (exprimées en unités
     * de l'environnement), de telle sorte que l'ordonnée géométrique ycentre_g soit au centre de la zone visible.
     * @param e : Environnement à afficher
     * @param larg_gc : Largeur graphique de la zone visible (en pixels)
     * @param haut_gc : Hauteur graphique de la zone visible (en pixels)
     * @param xmin_g : Abscisse géométrique minimale visible (en unités de l'Environnement e)
     * @param xmax_g : Abscisse géométrique maximale visible (en unités de l'Environnement e)
     * @param ycentre_g : Ordonnée géométrique du centre de la zone visible (en unités de l'Environnement e)
     * @param normales_visibles : Indique si les normales doivent être affichées
     * @param prolongements_avant_visibles : Indique si les prolongements des rayons vers l'avant doivent être affichés (en pointillés)
     * @param prolongements_arriere_visibles : Indique si les prolongements des rayons vers l'arrière doivent être affichés (en pointillés)
     * @param commentaire_visible : Indique si le commentaire (description textuelle) de l'Environnement doit être affiché
     * @param couleur_normales : Couleur à utiliser pour afficher les normales
     * @throws IllegalArgumentException si hauteur ou largeur fournies sont négatives
     */
    public CanvasAffichageEnvironnement(Environnement e, double larg_gc, double haut_gc, double xmin_g, double xmax_g, double ycentre_g,
                                        boolean normales_visibles,
                                        boolean prolongements_avant_visibles,
                                        boolean prolongements_arriere_visibles,
                                        boolean commentaire_visible,
                                        Color couleur_normales) throws IllegalArgumentException {

        super(larg_gc,haut_gc) ;

        if (larg_gc<=0.0 || haut_gc<=0.0)
            throw new IllegalArgumentException("Hauteur et Largeur graphiques du canvas d'affichage de l'Environnement doivent être strictement positives.") ;

        resolution = new SimpleDoubleProperty() ;

        // On définit l'environnement
        this.environnement = e ;

        definirDimensionsGraphiquesEtLimites(larg_gc,haut_gc,xmin_g,xmax_g,ycentre_g);

        this.environnement.uniteProperty().addListener( ( (observableValue, oldValue, newValue) -> {
                    LOGGER.log(Level.FINER,"unite passe de {0} à {1}",new Object[] {oldValue,newValue});
                    convertirDistancesEtRafraichirAffichage(oldValue.valeur/newValue.valeur);
                } )
        ) ;

        selection = new ElementsSelectionnes(environnement().unite()) ;

        visiteur_affichage = new VisiteurAffichageEnvironnement(this) ;

        // Convertisseur d'affichage des distances qui ajustera automatiquement le nombreElements de décimales à afficher en
        // fonction de la résolution de la résolution du Canvas, et qui fait de la virgule le séparateur des décimales.
        convertisseur_affichage_distance = new ConvertisseurDoubleValidantAffichageDistance(this) ;

        // Inutile d'ajouter un listener : le binding avec la résolution du canvas est faite dans le constructeur du convertisseur.
//        resolution.addListener( ( (observableValue, oldValue, newValue) -> {
//                    LOGGER.log(Level.FINER,"resolution passe de {0} à {1}",new Object[] {oldValue,newValue});
//                    convertisseur_affichage_distance.caleSurResolution();
//                } )
//        );

        this.normales_visibles = new SimpleBooleanProperty(normales_visibles) ;
        this.normales_visibles.addListener((observable, oldValue,newValue) -> this.rafraichirAffichage());

        this.couleur_normales = new SimpleObjectProperty<>(couleur_normales) ;
        this.couleur_normales.addListener((observable, oldValue,newValue) -> this.rafraichirAffichage());

        this.prolongements_avant_visibles = new SimpleBooleanProperty(prolongements_avant_visibles) ;
        this.prolongements_avant_visibles.addListener((observable, oldValue,newValue) -> this.rafraichirAffichage());

        this.prolongements_arriere_visibles = new SimpleBooleanProperty(prolongements_arriere_visibles) ;
        this.prolongements_arriere_visibles.addListener((observable, oldValue,newValue) -> this.rafraichirAffichage());

        this.commentaire_visible = new SimpleBooleanProperty(commentaire_visible) ;
        this.commentaire_visible.addListener((observable, oldValue,newValue) -> {
            // TODO : Mettre à jour le controle Text qui se trouve au-dessus du Canvas
        });

        this.texte_commentaire = new Text() ;
        this.texte_commentaire.setFill(Color.WHITE);
        this.texte_commentaire.visibleProperty().bindBidirectional(this.commentaire_visible);
        this.texte_commentaire.textProperty().bind(environnement.commentaireProperty());

        this.montrer_plans_focaux_de_soc = new SimpleMapProperty<>() ;
        this.montrer_plans_principaux_de_soc = new SimpleMapProperty<>() ;
        this.montrer_plans_nodaux_de_soc = new SimpleMapProperty<>() ;
    }

    public CanvasAffichageEnvironnement(Environnement e, double larg_gc, double haut_gc, double xmin_g, double xmax_g, double ycentre_g) throws IllegalArgumentException {
        this(e,larg_gc,haut_gc,xmin_g,xmax_g,ycentre_g,
                normales_visibles_par_defaut,
                prolongements_avant_visibles_par_defaut,
                prolongements_arriere_visibles_par_defaut,
                commentaire_visible_par_defaut,
                couleur_normales_par_defaut) ;
    }

    public CanvasAffichageEnvironnement(Environnement e, double larg_gc, double haut_gc) {
        this(e,larg_gc,haut_gc, xmin_g_par_defaut, xmax_g_par_defaut, ycentre_g_par_defaut) ;
    }

    public CanvasAffichageEnvironnement(Environnement e) {
        this(e,largeur_graphique_par_defaut,hauteur_graphique_par_defaut, xmin_g_par_defaut, xmax_g_par_defaut, ycentre_g_par_defaut) ;
    }

    public void initialize() {

        setBackground(new Background(new BackgroundFill(environnement.couleurFond(),null,null)));

        environnement.couleurFondProperty().addListener((observable, oldValue,newValue) -> {

            BackgroundFill bf = new BackgroundFill(newValue,null,null) ;

            setBackground(new Background(bf));

        });

        environnement.reflexionAvecRefractionProperty().addListener((observable, oldValue,newValue) -> this.rafraichirAffichage());

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Initialisation et mise en observation de la liste des sources.

        // Intégration des rappels sur les éventuelles sources déjà présentes dans l'environnement (peut arriver si on a
        // chargé l'environnement)
        Iterator<Source> its = environnement.iterateur_sources() ;
        while (its.hasNext())
            its.next().ajouterRappelSurChangementToutePropriete(this::rafraichirAffichage);

        // Détection des sources ajoutées ou supprimées
        ListChangeListener<Source> lcl_sources = change -> {
            while (change.next()) {
                if (change.wasRemoved()) {
                    LOGGER.log(Level.FINER, "Source supprimée");
                    rafraichirAffichage();
                } else if (change.wasAdded()) {
                    for (Source additem : change.getAddedSubList()) {

                        LOGGER.log(Level.FINER, "Source ajoutée : {0}", additem);
                        LOGGER.log(Level.FINER, "Création des liaisons pour la Source {0}", additem);

                        additem.ajouterRappelSurChangementToutePropriete(this::rafraichirAffichage);

                        rafraichirAffichage();

                    }
                }
            }
        };

        // Enregistrer listener des sources
        environnement.ajouterListenerListeSources(lcl_sources);

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Initialisation et mise en observation de la liste des obstacles.

        // Intégration des rappels sur les éventuels obstacles déjà présents dans l'environnement (peut arriver si on a chargé l'environnement)
        Iterator<Obstacle> ito = environnement.iterateur_obstacles() ;
        while (ito.hasNext())
            ito.next().ajouterRappelSurChangementToutePropriete(this::rafraichirAffichage);

        ListChangeListener<Obstacle> lcl_obstacles = change -> {
            while (change.next()) {

                if (change.wasRemoved()) {
                    LOGGER.log(Level.FINER, "Obstacle supprimé");
                    rafraichirAffichage();
                } else if (change.wasAdded()) {

                    for (Obstacle additem : change.getAddedSubList()) {
                        LOGGER.log(Level.FINER, "Obstacle ajouté : {0}", additem);

                        additem.ajouterRappelSurChangementToutePropriete(this::rafraichirAffichage);

                    }

                    rafraichirAffichage();
                }

            }
        };

        // Enregistrer listener des obstacles
        environnement.ajouterListChangeListenerObstacles(lcl_obstacles);

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Initialisation et mise en observation de la liste des SOCs.

        // Intégration des rappels sur les éventuels SOC déjà présents dans l'environnement (peut arriver si on a chargé l'environnement)
        Iterator<SystemeOptiqueCentre> itsoc = environnement.iterateur_systemesOptiquesCentres() ;
        while (itsoc.hasNext())
            itsoc.next().ajouterRappelSurChangementToutePropriete(this::rafraichirAffichage);


        // Détection des socs ajoutés ou supprimés
        //                if (c.wasPermutated())   { for (int i = c.getFrom(); i < c.getTo(); ++i) {  } }
        //                else if (c.wasUpdated()) { for (int i = c.getFrom(); i < c.getTo(); ++i) { /* environnement.sources.get(i) */ } }
        //                else
        //                  for (Source remitem : change.getRemoved()) { }
        // Il faut un rappel pour redessiner l'axe en cas de changement d'une propriété du SOC, y compris sa matrice de transfert
        // NB : Si ce rappel se déclenche, il est dommage qu'il y en ait déjà un de déclenché par les obstacles du SOC eux-mêmes, quand on
        // change ses propriétés : il faudrait s'en passer et ne garder que le rappel ci-dessous...
        ListChangeListener<SystemeOptiqueCentre> lcl_socs = change -> {
            while (change.next()) {
                //                if (c.wasPermutated())   { for (int i = c.getFrom(); i < c.getTo(); ++i) {  } }
                //                else if (c.wasUpdated()) { for (int i = c.getFrom(); i < c.getTo(); ++i) { /* environnement.sources.get(i) */ } }
                //                else
                if (change.wasRemoved()) {
                    //                  for (Source remitem : change.getRemoved()) { }

                    LOGGER.log(Level.FINER, "SOC supprimé");
                    rafraichirAffichage();
                } else if (change.wasAdded()) {
                    for (SystemeOptiqueCentre additem : change.getAddedSubList()) {

                        LOGGER.log(Level.FINER, "SOC ajouté : {0}", additem);

                        LOGGER.log(Level.FINER, "Création des liaisons pour le SOC {0}", additem);

                        // Il faut un rappel pour redessiner l'axe en cas de changement d'une propriété du SOC, y compris sa matrice de transfert
                        // NB : Si ce rappel se déclenche, il est dommage qu'il y en ait déjà un de déclenché par les obstacles du SOC eux-mêmes, quand on
                        // change ses propriétés : il faudrait s'en passer et ne garder que le rappel ci-dessous...
                        additem.ajouterRappelSurChangementToutePropriete(this::rafraichirAffichage);

                        rafraichirAffichage();

                    }
                }
            }
        };

        // Enregistrer listener des SOCs
        environnement.ajouterListenerListeSystemesOptiquesCentres(lcl_socs);

        widthProperty().addListener((observable, oldValue, newValue) -> {
            LOGGER.log(Level.FINER,"Largeur du Canvas passe de {0} à {1}",new Object[] {oldValue,newValue});

            if (newValue.doubleValue()> Screen.getPrimary().getVisualBounds().getWidth())
                return ;

            largeur_graphique = newValue.doubleValue() ;

            double delta_largeur = (largeur_graphique - oldValue.doubleValue()) * resolution_x ;

            boite_limites = new BoiteLimiteGeometrique(xmin(),ymin(),largeur()+delta_largeur,hauteur()) ;
//            LOGGER.log(Level.FINER,"["+ xmin_init +","+ (ymax_init -(ymax_init - ymin_init)*hauteurCourante/hauteurInitiale) +","+(xmin_init +(xmax_init - xmin_init)*newValue.doubleValue()/largeurInitiale)+","+ ymax_init +"]");

            rafraichirAffichage();
        });

        heightProperty().addListener((observable, oldValue, newValue) -> {
            LOGGER.log(Level.FINER,"Hauteur du Canvas passe de {0} à {1}",new Object[] {oldValue,newValue});

            if (newValue.doubleValue()>Screen.getPrimary().getVisualBounds().getHeight())
                return ;

            hauteur_graphique = newValue.doubleValue();

            double delta_hauteur = (hauteur_graphique - oldValue.doubleValue()) * resolution_y ;

            boite_limites = new BoiteLimiteGeometrique(xmin(),ymin()-delta_hauteur,largeur(),hauteur()+delta_hauteur) ;

//            LOGGER.log(Level.FINER,"["+ xmin_init +","+(ymax_init -(ymax_init - ymin_init)*newValue.doubleValue()/hauteurInitiale)+","+ xmin_init +(xmax_init - xmin_init)*largeurCourante/largeurInitiale +","+ ymax_init +"]");

            rafraichirAffichage();
        });

        setOnScroll(this::traiterMoletteSourisCanvas);
        texte_commentaire.setOnScroll(this::fireEvent);

    }

    public double xmin() { return boite_limites.getMinX() ; }

    public double xmax() {
        return boite_limites.getMaxX() ;
    }

    public double ymin() {return boite_limites.getMinY() ;}

    public double ymax() {
        return boite_limites.getMaxY() ;
    }

    public double xcentre() {
        return boite_limites.getCenterX() ;
    }

    public double ycentre() {
        return boite_limites.getCenterY() ;
    }


    public double facteurPasDistance() {
        return 10.0;
    }

    public void ajustePasEtAffichageSpinnerValueFactoryDistance(SpinnerValueFactory.DoubleSpinnerValueFactory dsv) {
        dsv.amountToStepByProperty().bind(resolutionProperty().multiply(facteurPasDistance()));
//        dsv.setConverter(convertisseurAffichageDistance());
    }

    public Point2D premiere_intersection_avec_limites(Rayon r) {

        return boite_limites.premiere_intersection(r.supportGeometrique()) ;
    }

    public Point2D derniere_intersection_avec_limites(Rayon r) {
        return boite_limites.derniere_intersection(r.supportGeometrique()) ;
    }

    public double largeur() {
        return boite_limites.getWidth() ;
    }

    public double hauteur() {
        return boite_limites.getHeight() ;
    }


    public ElementsSelectionnes selection() { return selection ; }

    public void definirSelection(ElementsSelectionnes elements) {
        selection = elements ;
    }

    public double tolerance_pointage() {
        // Facteur de tolerance pointage
        double facteur_tolerance_pointage = 7.0;
        return facteur_tolerance_pointage *resolution.get() ;
    }

    public void traiterMoletteSourisCanvas(ScrollEvent scrollEvent) {
//        LOGGER.log(Level.FINER,"DeltaX "+scrollEvent.getDeltaX());
//        LOGGER.log(Level.FINER,"DeltaY "+scrollEvent.getDeltaY());
//        LOGGER.log(Level.FINER,"MultiplierX "+scrollEvent.getMultiplierX());
//        LOGGER.log(Level.FINER,"MultiplierY "+scrollEvent.getMultiplierY());
//        LOGGER.log(Level.FINER,"Total deltaX "+scrollEvent.getTotalDeltaX());
//        LOGGER.log(Level.FINER,"Total deltaY "+scrollEvent.getTotalDeltaY());
//        LOGGER.log(Level.FINER,"Event Type "+scrollEvent.getEventType());

        // Si ce n'est pas une action de la molette, ne rien faire
        if (scrollEvent.getEventType()!=ScrollEvent.SCROLL)
            return ;

        double nouveau_facteur ;

        double pas_facteur_zoom_molette_souris = 0.1;
        if (scrollEvent.getDeltaY()<0)
            nouveau_facteur = (1 + pas_facteur_zoom_molette_souris) ;
        else
            nouveau_facteur = (1 - pas_facteur_zoom_molette_souris) ;

        // Calcul des coordonnées du centre qui doit rester au milieu de l'écran après le zoom
        double xcentre = (xmin() + xmax()) / 2;
        double ycentre = (ymin() + ymax()) / 2;

        double nouveau_xmin = xcentre - nouveau_facteur*(xcentre - xmin()) ;
        double nouveau_xmax = xcentre + nouveau_facteur*(xmax() - xcentre) ;

        try {
            definirLimites(nouveau_xmin, nouveau_xmax, ycentre );
        }  catch (IllegalArgumentException e) {
            return ;
        }

        rafraichirAffichage();

    }

    /**
     * Définit les limites (en coordonnées géométriques et en unités de l'Environnement) de la zone à afficher dans le Canvas (zone visible).
     * Cette méthode définit la transformation affine du GraphicsContext du canvas de telle manière que les bords de ce
     * GraphicsContext (de taille largeur_graphique X hauteur_graphique) correspondent aux coordonnées des bords passés
     * en paramètres.
     * <p>
     * NB : Si on se contente de translater "en bloc" les limites, sans avoir changé la largeur_graphique ou la hauteur_graphique,
     * utiliser la méthode translaterLimites() plutôt que celle-ci, pour éviter des calculs inutiles.
     * <p>
     * Pré-condition : largeur_graphique et hauteur-graphique sont définis
     * <p>
     * Post-conditions : les résolutions (x et y) sont calculées, la matrice de transformation gc_affichage -> g et son inverse
     * g -> gc_affichage sont calculées, les nouvelles limites sont enregistrées (dans la propriété boite_limites)
     *
     * @param xmin_g : x minimal visible en coordonnées géométriques et en unités de l'environnement
     * @param xmax_g : y maximal visible en coordonnées géométriques et en unités de l'environnement
     * @param ycentre_g : y du centre de la zone visible en coordonnées géométriques et en unités de l'environnement
         */
    public void definirLimites(double xmin_g, double xmax_g, double ycentre_g) throws IllegalArgumentException {

        if ( (xmin_g>xmax_g) )
            throw new IllegalArgumentException("xmax de la zone à afficher doit être plus grand que xmin.") ;

        // Recalage de y_min et y_max pour préserver le ratio d'aspect tout en maintenant l'ordonnée (y_min+y_max)/2
        // pour le milieu de la vue
        double nouv_ymin_g = ((2*ycentre_g)-(xmax_g-xmin_g)* hauteur_graphique/largeur_graphique)*0.5d ;
        double nouv_ymax_g = ((2*ycentre_g)+(xmax_g-xmin_g)* hauteur_graphique/largeur_graphique)*0.5d ;

        // Resolution = dimensions (en unités de l'environnement) d'un pixel graphique en coordonnées géométriques de l'Environnement
        double nouvelle_resolution_x = (xmax_g-xmin_g) / largeur_graphique ;
        double nouvelle_resolution_y = (nouv_ymax_g-nouv_ymin_g) / hauteur_graphique ;
        double nouvelle_resolution = Math.min(nouvelle_resolution_x,nouvelle_resolution_y) ;

        double resolution_minimale_autorisee = 1E-4; // Un dix-millième d'unité de l'environnement par pixel
        if (nouvelle_resolution< resolution_minimale_autorisee)
            throw new IllegalArgumentException("La zone à afficher est trop petite (précision max d'une coordonnée dans un javafx.scene.canvas.GraphicsContext est limitée à celle d'un float soit environ 1E-7)") ;

        double resolution_maximale_autorisee = 1E2; // 100 unités d'environnement par pixel
        if (nouvelle_resolution> resolution_maximale_autorisee)
            throw new IllegalArgumentException("La zone à afficher est trop grande (exposant max d'une coordonnée dans un javafx.scene.canvas.GraphicsContext est limitée à celui d'un float soit environ 1E38)") ;

        // Tous les contrôles de paramètres ont été faits : on peut modifier les propriétés du Canvas
        boite_limites = new BoiteLimiteGeometrique(xmin_g,nouv_ymin_g,xmax_g-xmin_g,nouv_ymax_g-nouv_ymin_g) ;

        resolution_x = nouvelle_resolution_x ;
        resolution_y = nouvelle_resolution_y ;

        resolution.set(nouvelle_resolution) ;

        LOGGER.log(Level.FINEST, "Nouvelle resolution (dimension d'un pixel dans l'espace géométrique): {0}",nouvelle_resolution) ;

        // Définition de la transformation affine qui permet de passer des coordonnées géométriques, exprimées en unités
        // de l'environnement, aux coordonnées sur l'écran, en préservant le ratio largeur_graphique/hauteur_graphique
        Affine nouvelle_transform = new Affine(largeur_graphique/(xmax_g-xmin_g)    ,0,-largeur_graphique*xmin_g/(xmax_g-xmin_g),
                                               0,-hauteur_graphique/(nouv_ymax_g-nouv_ymin_g),hauteur_graphique*nouv_ymax_g/(nouv_ymax_g-nouv_ymin_g) ) ;

        // On applique la transformation à gc_travail et gc_affichage, et au gc_selection
        gc_affichage.setTransform(nouvelle_transform);
        gc_selection.setTransform(nouvelle_transform);

        // Calcul de l'inverse (on sait que les coeff. xy et yx de la matrice de transfo sont nuls => calcul est simplifié,
        // inutile d'utiliser la méthode createInverse())

        transformation_inverse =
                new Affine( 1/ gc_affichage.getTransform().getMxx(),0,-gc_affichage.getTransform().getTx()/ gc_affichage.getTransform().getMxx(),
                        0,1/ gc_affichage.getTransform().getMyy(),-gc_affichage.getTransform().getTy()/ gc_affichage.getTransform().getMyy()) ;

        // Il faut ramener l'épaisseur du trait en coordonnées géométriques
        // ...pour les deux gc
        gc_affichage.setLineWidth(resolution.get());
        gc_selection.setLineWidth(resolution.get());

    }

    /**
     * Translate les limites de la zone visible, sans changer d'échelle (ni de résolution)
     * @param depl_x_g : déplacement sur l'axe X en coordonnées géométriques de l'environnement
     * @param depl_y_g : déplacement sur l'axe X en coordonnées géométriques de l'environnement
     */
    public void translaterLimites(double depl_x_g, double depl_y_g) {

        boite_limites = new BoiteLimiteGeometrique(xmin()-depl_x_g,ymin()-depl_y_g,xmax()-xmin(),ymax()-ymin()) ;

        // Translation des deux gc_affichage
        gc_affichage.translate(depl_x_g, depl_y_g);
        gc_selection.translate(depl_x_g, depl_y_g);

        transformation_inverse.setMxx(1/ gc_affichage.getTransform().getMxx());
        transformation_inverse.setMyy(1/ gc_affichage.getTransform().getMyy());
        transformation_inverse.setTx(-(depl_x_g+ gc_affichage.getTransform().getTx())/ gc_affichage.getTransform().getMxx());
        transformation_inverse.setTy(-(depl_y_g+ gc_affichage.getTransform().getTy())/ gc_affichage.getTransform().getMyy());

    }

    public void definirDimensionsGraphiques(double larg_g, double haut_g) {
        largeur_graphique = larg_g;
        hauteur_graphique = haut_g;
    }

    public void definirDimensionsGraphiquesEtLimites(double larg_g, double haut_g, double xmin, double xmax, double ycentre) {
        definirDimensionsGraphiques(larg_g,haut_g);
        definirLimites(xmin, xmax, ycentre);
    }

    public GraphicsContext gc_affichage() { return gc_affichage; }
    public GraphicsContext gc_selection() { return gc_selection; }

    public ConvertisseurDoubleValidantAffichageDistance convertisseurAffichageDistance() { return convertisseur_affichage_distance; }

    public double largeurGraphique() {
        return largeur_graphique;
    }

    public double hauteurGraphique() {
        return hauteur_graphique ;
    }

    public double resolutionX() {
        return resolution_x ;
    }

    public double resolutionY() {
        return resolution_y ;
    }

    public double resolution() {
        return resolution.get() ;
    }

    public DoubleProperty resolutionProperty() {
        return resolution ;
    }

    public void rafraichirAffichage() {

        if (suspendre_rafraichir_decor || environnement.rafraichissementAffichagesSuspendu())
            return ;

        // Effacer la zone visible (rend les pixels transparents : le fond prédéfini dans le parent remplit la zone visible)
        gc_affichage.clearRect(xmin(), ymin(),xmax() - xmin(), ymax()-ymin());

        // Mono thread :

        // Dessiner l'environnement sur le gc_travail
        environnement.accepter(visiteur_affichage);

    }

    public void montrerPoint(Point2D pt) {

        double rayon = (xmax()-xmin())/20 ;

        Paint s = gc_affichage.getStroke() ;
        gc_affichage.setStroke(Color.GREEN);

        gc_affichage.strokeOval(pt.getX()-rayon,pt.getY()-rayon,2*rayon,2*rayon);

        gc_affichage.setStroke(s);
    }

    public void montrerNormale(Point2D dep, Point2D normale) {
        Paint s = gc_affichage.getStroke() ;
        gc_affichage.setStroke(couleurNormales());

        // La normale est déjà normalisée (a priori)
        normale = normale.multiply(30*resolution()) ;

        Point2D arr = dep.add(normale) ;

        gc_affichage.strokeLine(dep.getX(),dep.getY(),arr.getX(),arr.getY());

        gc_affichage.setStroke(s);

    }
    public void montrerProlongementAvant(Point2D dep, Point2D arr) {
//        Paint s = gc_affichage.getStroke() ;
//        gc_affichage.setStroke(couleurNormales());
        gc_affichage.setLineWidth(1*resolution());

        gc_affichage.setLineDashes(2*resolution(),3*resolution());

        gc_affichage.strokeLine(dep.getX(),dep.getY(),arr.getX(),arr.getY());

        gc_affichage.setLineDashes();

        gc_affichage.setLineWidth(2*resolution());

//        gc_affichage.setStroke(s);

    }

    public void montrerProlongementArriere(Point2D dep, Point2D arr) {
//        Paint s = gc_affichage.getStroke() ;
//        gc_affichage.setStroke(Color.RED);
        gc_affichage.setLineWidth(1*resolution());

        gc_affichage.setLineDashes(2*resolution(),5*resolution());

        gc_affichage.strokeLine(dep.getX(),dep.getY(),arr.getX(),arr.getY());

        gc_affichage.setLineDashes();

        gc_affichage.setLineWidth(2*resolution());

//        gc_affichage.setStroke(s);

    }


    public Point2D gc_vers_g(double xgc, double ygc) {
        return transformation_inverse.transform(xgc,ygc) ;
    }

    public static void remplirPolygone(CanvasAffichageEnvironnement eg, Collection<Double> xpoints, Collection<Double> ypoints) {

        double[] xpd = new double[xpoints.size()];
        double[] ypd = new double[ypoints.size()];

        Iterator<Double> itx = xpoints.iterator() ;
        Iterator<Double> ity = ypoints.iterator() ;

        int i = 0 ;
        while (itx.hasNext() && ity.hasNext()) {
            xpd[i] = itx.next();
            ypd[i] = ity.next() ;
            i++ ;
        }

        // Remplissage de l'intérieur du polygone
        eg.gc_affichage.fillPolygon( xpd,ypd,xpd.length);

    }

    public void remplirContour(Contour c) {
        double[] xpd = new double[c.nombrePoints()];
        double[] ypd = new double[c.nombrePoints()];

        Iterator<Double> itx = c.iterateurX() ;
        Iterator<Double> ity = c.iterateurY() ;

        int i = 0 ;
        while (itx.hasNext() && ity.hasNext()) {
            xpd[i] = itx.next();
            ypd[i] = ity.next() ;
            i++ ;
        }

        // Remplissage du contour (rappel : par défaut la FillRule du GraphicsContext est NON_ZERO : si les contours sont
        // tracés dans des sens contraires, le dernier tracé produit un trou dans le précédent)
        gc_affichage.fillPolygon( xpd,ypd,xpd.length);

    }

    public void afficherContourSurfaceObstacle(ContoursObstacle co,GraphicsContext gc) {
        gc.beginPath();

        for (Contour c_surface : co.contoursSurface()) {
            tracerContour(c_surface,gc);
        }
    }

    // Affichage d'un obstacle (contours+masse remplie) à partir de son ContourObstacle préalablement calculé
    public void afficherContoursObstacle(ContoursObstacle co) {

        gc_affichage.beginPath();

        for (Contour c_masse : co.contoursMasse())
            completerPathAvecContour(c_masse);

        gc_affichage.closePath(); // Appel pas nécessaire, semble-t-il

        gc_affichage.fill();

        // On considère que le contour de masse (qui s'arrête aux limites de la zone visible) est aussi le contour visible
        // de l'obstacle => la couleur de contour sera appliquée aussi sur les bords de la zone visible
//        gc_affichage.stroke();

        // En désactivant la ligne suivante, la couleur de contour ne sera pas appliquée sur les bords de la zone visible,
        // mais elle ne sera pas non plus appliquée.
        for (Contour c_surface : co.contoursSurface()) {
//            gc_affichage.beginPath();
//            completerPathAvecContour(c_surface);
//            gc_affichage.stroke();
              tracerContour(c_surface);
        }

       // gc_affichage.closePath();


    }

    public void completerPathAvecContour(Contour c) {

        Iterator<Double> itx = c.iterateurX() ;
        Iterator<Double> ity = c.iterateurY() ;

        double xdep,ydep ;

        if (itx.hasNext()&&ity.hasNext()) {
            xdep = itx.next() ;
            ydep = ity.next() ;
            gc_affichage.moveTo(xdep,ydep);

            while (itx.hasNext() && ity.hasNext())
                gc_affichage.lineTo(itx.next(),ity.next());

            // Fermeture du chemin
            gc_affichage.lineTo(xdep,ydep);
        }

    }


    public void tracerContour(Contour c) {
        tracerContour(c,gc_affichage);
    }

    public void tracerContour(Contour c, GraphicsContext gc) {

        double[] xpd = new double[c.nombrePoints()];
        double[] ypd = new double[c.nombrePoints()];

        Iterator<Double> itx = c.iterateurX() ;
        Iterator<Double> ity = c.iterateurY() ;

        int i = 0 ;
        while (itx.hasNext() && ity.hasNext()) {
            xpd[i] = itx.next();
            ypd[i] = ity.next() ;
            i++ ;
        }

        // Tracé du contour du polygone
        gc.strokePolyline( xpd,ypd,xpd.length);
    }

    public void afficherPoignees(Contour c,GraphicsContext gc) {

        if (c==null)
            return;

        Iterator<Double> itx = c.iterateurX() ;
        Iterator<Double> ity = c.iterateurY() ;

        while (itx.hasNext() && ity.hasNext()) {
            double xpoignee = itx.next();
            double ypoignee = ity.next() ;
            gc.fillRect(xpoignee-tolerance_pointage()/2,ypoignee-tolerance_pointage()/2,tolerance_pointage(),tolerance_pointage());
        }
    }

    public boolean poigneeOstacleDeSelectionPointeeEn(Point2D pclic) {

        if (selection().obstacleUnique()==null)
            return false ;

//        if (obstacle_selectionne == null)
//            return false ;

        Contour poignees = selection().obstacleUnique().positions_poignees() ;

        if (poignees==null)
            return false ;

        return poignees.comporte_point_proche_de(pclic, tolerance_pointage());
    }

    public Point2D poigneeSelectionObstacleUnique() {

        if (selection().obstacleUnique()==null)
            return null ;

        Contour poignees = selection().obstacleUnique().positions_poignees() ;

        if (poignees==null)
            return null ;

        Iterator<Double> itx = poignees.iterateurX();
        Iterator<Double> ity = poignees.iterateurY();

        if (itx.hasNext() && ity.hasNext())
            return new Point2D(itx.next(),ity.next()) ;

        return null ;

    }
    public boolean poignee_source_pointee_en(Point2D pclic) {

        if (selection().sourceUnique()==null)
            return false ;

//        if (source_selectionnee == null)
//            return false ;

        Contour poignees = selection().sourceUnique().positions_poignees() ;

        if (poignees==null)
            return false ;

        return poignees.comporte_point_proche_de(pclic, tolerance_pointage());
    }

    public void tracerPolyligne(Collection<Double> xpoints, Collection<Double> ypoints) {

        double[] xpd = new double[xpoints.size()];
        double[] ypd = new double[ypoints.size()];

        Iterator<Double> itx = xpoints.iterator() ;
        Iterator<Double> ity = ypoints.iterator() ;

        int i = 0 ;
        while (itx.hasNext() && ity.hasNext()) {
            xpd[i] = itx.next();
            ypd[i] = ity.next() ;
            i++ ;
        }

        // Tracé du contour du polygone
        gc_affichage.strokePolyline( xpd,ypd,xpd.length);
    }

    public void completerPathAvecContourFerme(Collection<Double> xpoints, Collection<Double> ypoints) {

//        eg.gc_affichage.beginPath();

        Iterator<Double> itx = xpoints.iterator() ;
        Iterator<Double> ity = ypoints.iterator() ;

        double xdep,ydep ;

        if (itx.hasNext()&&ity.hasNext()) {
            xdep = itx.next() ;
            ydep = ity.next() ;
            gc_affichage.moveTo(xdep,ydep);

            while (itx.hasNext() && ity.hasNext())
                gc_affichage.lineTo(itx.next(),ity.next());

            // Fermeture du chemin
            gc_affichage.lineTo(xdep,ydep);
        }

//        eg.gc_affichage.closePath();

    }

    public void completerPathAvecContourBoiteTrigo(BoiteLimiteGeometrique b) {

        gc_affichage.moveTo(b.getMaxX(),b.getMaxY());
        gc_affichage.lineTo(b.getMinX(),b.getMaxY());
        gc_affichage.lineTo(b.getMinX(),b.getMinY());
        gc_affichage.lineTo(b.getMaxX(),b.getMinY());

        // Fermeture du chemin
        gc_affichage.lineTo(b.getMaxX(),b.getMaxY());

    }


    public void completerPathAvecContourZoneVisibleAntitrigo() {
        gc_affichage.moveTo(xmax(), ymin());
        gc_affichage.lineTo(xmin(), ymin());
        gc_affichage.lineTo(xmin(), ymax());
        gc_affichage.lineTo(xmax(), ymax());
    }

    public static void tracerEtRemplirContourDepuisPath(CanvasAffichageEnvironnement eg, PathD p) {
        double[] xpd = new double[p.size()];
        double[] ypd = new double[p.size()];

        Iterator<PointD> itp = p.iterator() ;

        int i = 0;
        while (itp.hasNext()) {
            PointD lp = itp.next() ;
            xpd[i] = lp.x ;
            ypd[i] = lp.y ;
            i++ ;
        }

        // Remplissage de l'intérieur du polygone
        eg.gc_affichage.fillPolygon( xpd,ypd,xpd.length);

        // Tracé du contour du polygone
        eg.gc_affichage.strokePolyline( xpd,ypd,xpd.length);
    }

    public BoiteLimiteGeometrique boite_limites() {
        return boite_limites;
    }

    public Environnement environnement() {
        return environnement;
    }

    public Obstacle obstacleReelPointeAuPremierPlan(Point2D pt_g) {

        if (poigneeOstacleDeSelectionPointeeEn(pt_g))
            return selection().obstacleUnique() ;

        Obstacle obs = environnement.obstacleReelAuPremierPlanContenant(pt_g) ;

        if (obs == null)
            obs = environnement.obstacleReelAuPremierPlanTresProcheDe(pt_g, tolerance_pointage());


        return obs ;

    }
    public Obstacle obstaclePointeASelectionner(Point2D pt_g) {

        Obstacle obs_reel = obstacleReelPointeAuPremierPlan(pt_g) ;

        Groupe grp_appartenance = environnement.groupeRacine().plus_grand_groupe_solidaire_contenant(obs_reel) ;

        return (grp_appartenance!=null?grp_appartenance:obs_reel) ;
    }

    public Source source_pointee_en(Point2D pt_g) {

        if (poignee_source_pointee_en(pt_g))
            return selection.sourceUnique() ;

//        Source s = environnement.derniere_source_contenant(pt_g) ;
//
//        if (s == null)
//            s = environnement.derniere_source_tres_proche(pt_g, tolerance_pointage());
//
//        return s ;

        return environnement.derniere_source_tres_proche(pt_g, tolerance_pointage());
    }

    public SystemeOptiqueCentre soc_pointe_en(Point2D pt_g) {
        if (poignee_soc_pointee_en(pt_g))
            return selection().socUnique() ;

        // Le SOC ne peut être pointé et sélectionné que par son axe : il n'a pas d'épaisseur
        // SystemeOptiqueCentre soc = environnement.dernier_soc_contenant(pt_g) ;

        return environnement.dernier_soc_tres_proche(pt_g, tolerance_pointage());
    }

    public boolean poignee_soc_pointee_en(Point2D pclic) {
        if (selection().socUnique() == null)
            return false ;

        Contour poignees = selection().socUnique().positions_poignees() ;

        if (poignees==null)
            return false ;

        return poignees.comporte_point_proche_de(pclic, tolerance_pointage());

    }

    public void definirEnvironnement(Environnement nouvel_environnement) {
        environnement = nouvel_environnement ;
    }

    /**
     * Indique si le Canvas d'Affichage contient le point géométrique pt_g, dont les coordonnées sont exprimées en
     * unités de l'environnement
     * @param pt_g point
     * @return true si le Vanvas contient le point, false sinon
     */
    public boolean contient(Point2D pt_g) {
        return boite_limites.contains(pt_g) ;
    }

    public Point2D premiere_intersection(DemiDroiteOuSegment s) {
        return boite_limites.premiere_intersection(s) ;
    }

    public Point2D derniere_intersection(DemiDroiteOuSegment s) {
        return boite_limites.derniere_intersection(s) ;
    }

    /**
     * Convertit les dimensions de la zone géométrique visible en les multipliant par le facteur de conversion transmis
     * @param facteur_conversion facteur de conversion de l'ancienne vers la nouvelle unité
     */
    public void convertirDistancesEtRafraichirAffichage(double facteur_conversion) {
        suspendre_rafraichir_decor = true ;
        definirLimites(xmin()*facteur_conversion,xmax()*facteur_conversion,ycentre()*facteur_conversion);
        suspendre_rafraichir_decor = false ;
        rafraichirAffichage();
    }

    public void effacerSelection() {
        gc_selection.clearRect(xmin(), ymin(),xmax() - xmin(), ymax()-ymin());
    }

    private void translaterSiPossible(Obstacle o, Point2D tr) {
        if (!o.appartientASystemeOptiqueCentre())
            o.translater(tr);
        else {
            SystemeOptiqueCentre soc = environnement.systemeOptiqueCentreContenant(o);

            // Si l'obstacle fait partie d'un SOC qui est lui-même sélectionné, ne pas le translater car c'est le SOC
            // qui va être translaté dans son ensemble.
            if (selection().comprend(soc))
                return ;

            Point2D tr_sur_axe = soc.vecteurDirecteurAxe().multiply(soc.vecteurDirecteurAxe().dotProduct(tr));
            o.translater(tr_sur_axe);
        }

    }

    public void translaterSelection(Point2D tr) {

        if (tr.getX()==0d && tr.getY()==0d)
            return;

        selection().stream_obstacles().forEach(obs -> translaterSiPossible(obs, tr));
        selection().stream_sources().forEach(src -> src.translater(tr));
        selection().stream_socs().forEach(soc -> soc.translater(tr));
    }

    public void selectionnerParZoneRectangulaire(BoiteLimiteGeometrique zone_rect) {
        zone_selection_rectangulaire = zone_rect ;

        if (zone_rect==null)
            return;

        selection().vider();
        selection().definirUnite(environnement().unite()) ;

        // Sélection des obstacles (ou groupes d'obstacles solidaires) s'ils rencontrent la zone de sélection
        visiteur_affichage.streamObstaclesVisibles().forEach(obstacle -> {
            if(visiteur_affichage.contoursVisiblesObstacle(obstacle).intersecte(zone_rect)) {

                Groupe grp_appartenance = environnement.groupeRacine().plus_grand_groupe_solidaire_contenant(obstacle);

                selection().ajouter(grp_appartenance != null ? grp_appartenance : obstacle);
            }
        }) ;

        // Sélection des sources
        environnement().sources().forEach(s->{
            if (zone_rect.contains(s.position()))
                selection().ajouter(s);
            else {
                if (s.type()== Source.TypeSource.PROJECTEUR) {
                    Point2D[] extremites = s.extremitesProjecteur() ;
                    DemiDroiteOuSegment prj = DemiDroiteOuSegment.construireSegment(extremites[0],extremites[1]) ;
                    if (zone_rect.intersecte(prj))
                        selection().ajouter(s);
                }
            }
        });

        // Sélection des SOCs si leur origine est dans la zone de sélection
        environnement().systemesOptiquesCentres().forEach(soc -> {
            if (zone_rect.contains(soc.origine()))
                selection().ajouter(soc);
        });
    }


    public BoundingBox zoneSelectionRectangulaire() { return zone_selection_rectangulaire ; }

}
