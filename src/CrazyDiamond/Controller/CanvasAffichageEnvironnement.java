package CrazyDiamond.Controller;

import CrazyDiamond.Model.*;
import de.lighti.clipper.Point.LongPoint;
import de.lighti.clipper.Path;

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
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.stage.Screen;

import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

// Cette classe est à la fois la Vue et le Controleur qui permet d'afficher un Environnement dans un Canvas JavaFX
// redimensionnable et dynamique lié (via des Bindings) à tous les objets (sources+obstacles) de l'Environnement.
public class CanvasAffichageEnvironnement extends ResizeableCanvas {

    // Modèle
    protected final Environnement environnement;
    protected Obstacle obstacle_selectionne = null ;
    public Source source_selectionnee = null ;
    protected SystemeOptiqueCentre soc_selectionne = null ;

    // TODO : En faire une propriete et l'afficher dans le Panneau environnemnt ?? Bof...
    protected BoiteLimiteGeometrique boite_limites ;

    // TODO : supprimer cet attribut inutile (le gc est dans le ResizeAble Canvas parent)
    protected final GraphicsContext gc ;

    // Récupération du logger
    private static final Logger LOGGER = Logger.getLogger( "CrazyDiamond" );

    private ListChangeListener<Source> lcl_sources ;

    private ListChangeListener<Obstacle> lcl_obstacles ;

    private ListChangeListener<SystemeOptiqueCentre> lcl_socs ;

    private VisiteurAffichageEnvironnement visiteur_affichage ;

    // Taille du canevas initial, en pixels (attention à la cohérence avec taille de la scène
    // dans la classe ReflexionParabolique)
    protected double largeurCourante;
    protected double hauteurCourante;

    protected double largeurInitiale;
    protected double hauteurInitiale;

    // Veut-on préserver le ratio d'aspect x/y (si 'false' un cercle en coordonnées géométriques donnera une ellipse en
    // coordonnées graphiques, et les angles en coordonnées géométriques ne sont pas conservés en coordonnées graphiques)
    protected boolean preserver_ratio_xy = true ;

    // Limites par défaut de la zone d'intérêt (zone visible)
    protected static double xmin_par_defaut = -2.0 , xmax_par_defaut = 2.0 ;
    protected static double ymin_par_defaut = -1.0, ymax_par_defaut = 2.0 ; // NB : ce ymax sera ignoré si preserver_ratio_xy = true (sa valeur sera calculée)

    protected static double largeur_graphique_par_defaut = 800 ;
    protected static double hauteur_graphique_par_defaut = 600 ;

    protected double xmin_init, ymin_init ;
    protected double xmax_init, ymax_init ;

    // Transformation pour passer des coordonnées écran aux coordonnées géométriques
    Affine transformation_inverse ;

    protected double largeur_graphique ;
    protected double hauteur_graphique ;

    protected double resolution_x ;
    protected double resolution_y ;

    protected DoubleProperty resolution ;

    // Facteur de tolerance pointage
    private final double facteur_tolerance_pointage = 7.0 ;
    private final double facteur_pas_distance = 10.0 ;

    protected final BooleanProperty normales_visibles ;
    protected final ObjectProperty<Color> couleur_normales ;
    protected final BooleanProperty  prolongements_avant_visibles ;
    protected final BooleanProperty  prolongements_arriere_visibles ;

    protected final MapProperty<SystemeOptiqueCentre,Boolean> montrer_plans_focaux_de_soc ;
    protected final MapProperty<SystemeOptiqueCentre,Boolean> montrer_plans_principaux_de_soc ;
    protected final MapProperty<SystemeOptiqueCentre,Boolean> montrer_plans_nodaux_de_soc ;
    private static final Color couleur_normales_par_defaut = Color.GREEN ;

    private static final boolean normales_visibles_par_defaut = false ;

    public Color couleurNormales() { return couleur_normales.get() ; }

    public void definirCouleurNormales(Color value) {
        couleur_normales.setValue(value);
    }

    public boolean normalesVisibles() { return normales_visibles.get() ;  }
    public boolean prolongementsAvantVisibles() { return prolongements_avant_visibles.get() ;  }
    public boolean prolongementsArriereVisibles() { return prolongements_arriere_visibles.get() ;  }

    public SystemeOptiqueCentre soc_pointe_en(Point2D pt_g) {
        if (poignee_soc_pointee_en(pt_g))
            return soc_selectionne ;

        // Le SOC ne peut être pointé et sélectionné que par son axe : il n'a pas d'épaisseur
        // SystemeOptiqueCentre soc = environnement.dernier_soc_contenant(pt_g) ;

        return environnement.dernier_soc_tres_proche(pt_g, tolerance_pointage());
    }

    public boolean poignee_soc_pointee_en(Point2D pclic) {
        if (soc_selectionne == null)
            return false ;

        Contour poignees = soc_selectionne.positions_poignees() ;

        if (poignees==null)
            return false ;

        if (poignees.comporte_point_proche_de(pclic,tolerance_pointage()))
            return true ;

        return false ;

    }


    protected ConvertisseurDoubleValidantAffichageDistance convertisseur_affichage_distance;

    public CanvasAffichageEnvironnement(Environnement e, double larg_g, double haut_g, double xmin, double ymin, double xmax, double ymax, boolean preserver_ratio_xy) throws IllegalArgumentException {

        super(larg_g,haut_g) ;

        this.environnement = e ;

        visiteur_affichage = new VisiteurAffichageEnvironnement(this) ;

        xmin_init = xmin ;
        ymin_init = ymin ;
        xmax_init = xmax ;
        ymax_init = ymax ;

        largeur_graphique = larg_g ;
        hauteur_graphique = haut_g ;

        largeurCourante = this.getWidth() ;
        hauteurCourante = this.getHeight();

        largeurInitiale = largeurCourante ;
        hauteurInitiale = hauteurCourante ;

        this.preserver_ratio_xy = preserver_ratio_xy ;

        // Recalcul du ymax si nécessité de preserver le ratio d'aspect x/y
        if (preserver_ratio_xy)
            ymax_init = ymin_init +  (xmax_init - xmin_init) * hauteurCourante / largeurCourante;

        this.definirLimitesAffichageEnvironnement(xmin_init,ymin_init,xmax_init,ymax_init);

        if (larg_g<=0.0 || haut_g<=0.0)
            throw new IllegalArgumentException("Hauteur et Largeur graphiques du canvas d'affichage de l'Environnement doivent être strictement positives.") ;

        gc = this.getGraphicsContext2D() ;

        resolution = new SimpleDoubleProperty() ;

        // Cet appel va notamment définir la valeur de la résolution
        integrerLimitesDansGraphicsContext();

        convertisseur_affichage_distance = new ConvertisseurDoubleValidantAffichageDistance(this) ;


        resolution.addListener( ( (observableValue, oldValue, newValue) -> {
                    LOGGER.log(Level.FINER,"resolution passe de {0} à {1}",new Object[] {oldValue,newValue});
                    convertisseur_affichage_distance.caleSurResolution();
                } )
        );


        this.normales_visibles = new SimpleBooleanProperty(normales_visibles_par_defaut) ;
        normales_visibles.addListener((observable, oldValue,newValue) -> {
            this.rafraichirDecor();
        });

        this.couleur_normales = new SimpleObjectProperty<Color>(couleur_normales_par_defaut) ;
        couleur_normales.addListener((observable, oldValue,newValue) -> {
            this.rafraichirDecor();
        });

        this.prolongements_avant_visibles = new SimpleBooleanProperty(false) ;
        prolongements_avant_visibles.addListener((observable, oldValue,newValue) -> {
            this.rafraichirDecor();
        });

        this.prolongements_arriere_visibles = new SimpleBooleanProperty(false) ;
        prolongements_arriere_visibles.addListener((observable, oldValue,newValue) -> {
            this.rafraichirDecor();
        });

    this.montrer_plans_focaux_de_soc = new SimpleMapProperty<SystemeOptiqueCentre, Boolean>() ;
    this.montrer_plans_principaux_de_soc = new SimpleMapProperty<SystemeOptiqueCentre, Boolean>() ;
    this.montrer_plans_nodaux_de_soc = new SimpleMapProperty<SystemeOptiqueCentre, Boolean>() ;


    }

    public CanvasAffichageEnvironnement(Environnement e, double larg_g, double haut_g, double xmin, double ymin, double xmax, double ymax) {
        this(e,larg_g,haut_g,xmin,ymin,xmax,ymax,true) ;
    }

    public CanvasAffichageEnvironnement(Environnement e, double larg_g, double haut_g) {
        this(e,larg_g,haut_g,xmin_par_defaut,ymin_par_defaut,xmax_par_defaut,ymax_par_defaut,true) ;
    }

    public CanvasAffichageEnvironnement(Environnement e) {
        this(e,largeur_graphique_par_defaut,hauteur_graphique_par_defaut,xmin_par_defaut,ymin_par_defaut,xmax_par_defaut,ymax_par_defaut,true) ;
    }

    public void initialize() {

        setBackground(new Background(new BackgroundFill(environnement.couleurFond(),null,null)));

//        environnement.couleur_fond.addListener((observable, oldValue,newValue) -> {
        environnement.couleurFondProperty().addListener((observable, oldValue,newValue) -> {

            BackgroundFill bf = new BackgroundFill(newValue,null,null) ;

            setBackground(new Background(bf));

        });

//        environnement.reflexion_avec_refraction.addListener((observable, oldValue,newValue) -> {
        environnement.reflexionAvecRefractionProperty().addListener((observable, oldValue,newValue) -> {
            this.rafraichirDecor();
        });

        // Intégration des rappels sur les éventuelles sources déjà présentes dans l'environnement (peut arriver si on a chargé l'environnement)
        Iterator<Source> its = environnement.iterateur_sources() ;
        while (its.hasNext())
            its.next().ajouterRappelSurChangementToutePropriete(this::rafraichirDecor);


        // Détection des sources ajoutées ou supprimées
        lcl_sources = (ListChangeListener<Source>) change -> {
            while (change.next()) {
    //                if (c.wasPermutated())   { for (int i = c.getFrom(); i < c.getTo(); ++i) {  } }
    //                else if (c.wasUpdated()) { for (int i = c.getFrom(); i < c.getTo(); ++i) { /* environnement.sources.get(i) */ } }
    //                else
                if (change.wasRemoved()) {
    //                  for (Source remitem : change.getRemoved()) { }

                    LOGGER.log(Level.FINER,"Source supprimée");
                    rafraichirDecor();
                } else if (change.wasAdded()) {
                    for (Source additem : change.getAddedSubList()) {

                        LOGGER.log(Level.FINER,"Source ajoutée : {0}",additem);

                        LOGGER.log(Level.FINER,"Création des liaisons pour la Source {0}",additem);

                        additem.ajouterRappelSurChangementToutePropriete(this::rafraichirDecor);

                        rafraichirDecor();

                    }
                }
            }
        };

        // Enregistrer listener des sources
        environnement.ajouterListenerListeSources(lcl_sources);

        // Intégration des rappels sur les éventuels obstacles déjà présents dans l'environnement (peut arriver si on a chargé l'environnement)
        Iterator<Obstacle> ito = environnement.iterateur_obstacles() ;
        while (ito.hasNext())
            ito.next().ajouterRappelSurChangementToutePropriete(this::rafraichirDecor);

        lcl_obstacles = (ListChangeListener<Obstacle>) change -> {
            while (change.next()) {

                if (change.wasRemoved()) {
                    //                  for (Source remitem : change.getRemoved()) { }
                    LOGGER.log(Level.FINER,"Obstacle supprimé");
                    rafraichirDecor();
                } else if (change.wasAdded()) {

                    for (Obstacle additem : change.getAddedSubList()) {
                        LOGGER.log(Level.FINER,"Obstacle ajouté : {0}" , additem);

                        additem.ajouterRappelSurChangementToutePropriete(this::rafraichirDecor);
                    }

                    rafraichirDecor();
                }

            }
        } ;

        // Enregistrer listener des obstacles
        environnement.ajouterListenerListeObstacles(lcl_obstacles);

        // Intégration des rappels sur les éventuels SOC déjà présents dans l'environnement (peut arriver si on a chargé l'environnement)
        Iterator<SystemeOptiqueCentre> itsoc = environnement.iterateur_systemesOptiquesCentres() ;
        while (itsoc.hasNext())
            itsoc.next().ajouterRappelSurChangementToutePropriete(this::rafraichirDecor);


        // Détection des socs ajoutés ou supprimés
        lcl_socs = (ListChangeListener<SystemeOptiqueCentre>) change -> {
            while (change.next()) {
                //                if (c.wasPermutated())   { for (int i = c.getFrom(); i < c.getTo(); ++i) {  } }
                //                else if (c.wasUpdated()) { for (int i = c.getFrom(); i < c.getTo(); ++i) { /* environnement.sources.get(i) */ } }
                //                else
                if (change.wasRemoved()) {
                    //                  for (Source remitem : change.getRemoved()) { }

                    LOGGER.log(Level.FINER,"SOC supprimé");
                    rafraichirDecor();
                } else if (change.wasAdded()) {
                    for (SystemeOptiqueCentre additem : change.getAddedSubList()) {

                        LOGGER.log(Level.FINER,"SOC ajouté : {0}",additem);

                        LOGGER.log(Level.FINER,"Création des liaisons pour le SOC {0}",additem);

                        // Il faut un rappel pour redessiner l'axe en cas de changement d'une propriété du SOC, y compris sa matrice de transfert
                        // NB : Si ce rappel se déclenche, il est dommage qu'il y en ait déjà un de déclenché par les obstacles du SOC eux-mêmes, quand on
                        // change ses propriétés : il faudrait s'en passer et ne garder que le rappel ci-dessous...
                        additem.ajouterRappelSurChangementToutePropriete(this::rafraichirDecor);

                        rafraichirDecor();

                    }
                }
            }
        };

        // Enregistrer listener des SOCs
        environnement.ajouterListenerListeSystemesOptiquesCentres(lcl_socs);

        // TODO...
//        MapChangeListener<SystemeOptiqueCentre,Boolean> mcl_soc = (MapChangeListener<SystemeOptiqueCentre, Boolean>) change -> {
//            if (change.wasAdded())
//                change.getKey()
//        } ;



        widthProperty().addListener((observable, oldValue, newValue) -> {
            LOGGER.log(Level.FINER,"Largeur du Canvas passe de {0} à {1}",new Object[] {oldValue,newValue});

            if (newValue.doubleValue()> Screen.getPrimary().getVisualBounds().getWidth())
                return ;

            definirLimitesEtDimensionsGraphiques(newValue.doubleValue(),getHeight(), xmin_init, ymax_init -(ymax_init - ymin_init)*hauteurCourante/hauteurInitiale, xmin_init +(xmax_init - xmin_init)*newValue.doubleValue()/largeurInitiale, ymax_init);

//            LOGGER.log(Level.FINER,"["+ xmin_init +","+ (ymax_init -(ymax_init - ymin_init)*hauteurCourante/hauteurInitiale) +","+(xmin_init +(xmax_init - xmin_init)*newValue.doubleValue()/largeurInitiale)+","+ ymax_init +"]");
            largeurCourante = newValue.doubleValue();

            rafraichirDecor();
        });

        heightProperty().addListener((observable, oldValue, newValue) -> {
            LOGGER.log(Level.FINER,"Hauteur du Canvas passe de {0} à {1}",new Object[] {oldValue,newValue});

            if (newValue.doubleValue()>Screen.getPrimary().getVisualBounds().getHeight())
                return ;

            definirLimitesEtDimensionsGraphiques(getWidth(),newValue.doubleValue(), xmin_init, ymax_init -(ymax_init - ymin_init)*newValue.doubleValue()/hauteurInitiale, xmin_init +(xmax_init - xmin_init)*largeurCourante/largeurInitiale, ymax_init);
//            LOGGER.log(Level.FINER,"["+ xmin_init +","+(ymax_init -(ymax_init - ymin_init)*newValue.doubleValue()/hauteurInitiale)+","+ xmin_init +(xmax_init - xmin_init)*largeurCourante/largeurInitiale +","+ ymax_init +"]");
            hauteurCourante = newValue.doubleValue();
            rafraichirDecor();
        });

        setOnScroll(this::traiterMoletteSourisCanvas);

    }

    public double xmin() { return boite_limites.getMinX() ; }

    public double xmax() {
        return boite_limites.getMaxX() ;
    }

    public double ymin() {
        return boite_limites.getMinY() ;
    }

    public double ymax() {
        return boite_limites.getMaxY() ;
    }
    public double facteurPasDistance() { return facteur_pas_distance ; }

    public void ajustePasEtAffichageSpinnerValueFactoryDistance(SpinnerValueFactory.DoubleSpinnerValueFactory dsv) {
        dsv.amountToStepByProperty().bind(resolutionProperty().multiply(facteurPasDistance()));
//        dsv.setConverter(convertisseurAffichageDistance());
    }

    protected final void definirLimitesAffichageEnvironnement(double xmin, double ymin, double xmax, double ymax) {

        if ( (xmin>xmax) || (ymin>ymax) )
            throw new IllegalArgumentException("xmax de la zone visible doit être plus grand que xmin. Idem pour ymax et ymin.") ;

        boite_limites = new BoiteLimiteGeometrique(xmin,ymin,xmax-xmin,ymax-ymin) ;

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

    public Obstacle obstacleSelectionne() { return obstacle_selectionne ; }

    public void selectionneObstacle(Obstacle o) {
        Obstacle prec_obs = obstacle_selectionne ;
        obstacle_selectionne = o ;

        deselectionneSource();
        deselectionneSystemeOptiqueCentre();

        if (o!=prec_obs)
            rafraichirDecor();
    }
    public void deselectionneObstacle() {
        if (obstacle_selectionne==null)
            return ;

        obstacle_selectionne = null;
        rafraichirDecor();
    }

    public Source sourceSelectionnee() { return source_selectionnee ; }

    public void selectionneSource(Source s) {
        Source prec_s = source_selectionnee ;
        source_selectionnee = s ;

        deselectionneObstacle();
        deselectionneSystemeOptiqueCentre();

        if (s!=prec_s)
            rafraichirDecor();
    }


    public void deselectionneSource() {
        if (source_selectionnee==null)
            return ;

        source_selectionnee = null;
        rafraichirDecor();
    }

    public SystemeOptiqueCentre systemeOptiqueCentreSelectionne() { return soc_selectionne ;}

    public void selectionneSystemeOptiqueCentre(SystemeOptiqueCentre s) {
        SystemeOptiqueCentre prec_soc = soc_selectionne ;
        soc_selectionne = s ;

        deselectionneSource();
        deselectionneObstacle();

        if (s!=prec_soc)
            rafraichirDecor();
    }

    public void deselectionneSystemeOptiqueCentre() {
        if (soc_selectionne==null)
            return ;

        soc_selectionne = null;
        rafraichirDecor();
    }


    public double tolerance_pointage() {
        return facteur_tolerance_pointage*resolution.get() ;
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

        double zoom_factor_step = 0.1 ;

        double facteur ;

        if (scrollEvent.getDeltaY()<0)
            facteur = (1+zoom_factor_step) ;
        else
            facteur = (1-zoom_factor_step) ;

        // Calcul des coordonnées du centre qui doit rester au milieu de l'écran après le zoom
        double xcentre = (xmin() + xmax()) / 2;
        double ycentre = (ymin() + ymax()) / 2;

        double nouveau_xmin = xcentre - facteur*(xcentre- xmin()) ;
        double nouveau_ymin = ycentre - facteur*(ycentre- ymin()) ;
        double nouveau_xmax = xcentre + facteur*(xmax() -xcentre) ;
        double nouveau_ymax = ycentre + facteur*(ymax() -ycentre) ;

        xmin_init =nouveau_xmin ;
        ymin_init =nouveau_ymin ;
        xmax_init =nouveau_xmax ;
        ymax_init =nouveau_ymax ;

        largeurInitiale = this.getWidth() ;
        hauteurInitiale = this.getHeight();
        largeurCourante = largeurInitiale ;
        hauteurCourante = hauteurInitiale ;

        definirLimites(nouveau_xmin,nouveau_ymin,nouveau_xmax,nouveau_ymax);

        rafraichirDecor();

    }


    public void definirLimites(double xmin, double ymin, double xmax, double ymax) {

        definirLimitesAffichageEnvironnement(xmin, ymin, xmax, ymax);

        integrerLimitesDansGraphicsContext();
    }

    public void definirDimensionsGraphiques(double l_graphique, double h_graphique) {
        largeur_graphique = l_graphique ;
        hauteur_graphique = h_graphique ;

        integrerLimitesDansGraphicsContext();
    }

    public void definirLimitesEtDimensionsGraphiques(double larg_g, double haut_g, double xmin, double ymin, double xmax, double ymax) {

        definirLimitesAffichageEnvironnement(xmin, ymin, xmax, ymax);

        largeur_graphique = larg_g ;
        hauteur_graphique = haut_g ;

        integrerLimitesDansGraphicsContext();

    }

    public void definirLimites(BoundingBox b_limites) {
        definirLimitesAffichageEnvironnement(b_limites.getMinX(),b_limites.getMinY(),b_limites.getMaxX(),b_limites.getMaxY());

        integrerLimitesDansGraphicsContext();
    }

    protected final void integrerLimitesDansGraphicsContext() {

        double xmin = boite_limites.getMinX() ;
        double ymin = boite_limites.getMinY() ;
        double xmax = boite_limites.getMaxX() ;
        double ymax = boite_limites.getMaxY() ;

        // Resolution = dimensions d'un pixel graphique en coordonnées géométriques de l'Environnement
        resolution_x = (xmax-xmin) / largeur_graphique ;
        resolution_y = (ymax-ymin) / hauteur_graphique ;

        LOGGER.log(Level.FINEST, "Nouvelle resolution : {0}",Math.min(resolution_x,resolution_y)) ;

        resolution.set(Math.min(resolution_x,resolution_y)) ;

        // Définition de la transformation affine qui permet de passer des coordonnées géométriques aux coord. écran
        Affine nouvelle_transform = new Affine() ;
        nouvelle_transform.appendTranslation(-xmin * largeur_graphique / (xmax - xmin), hauteur_graphique + ymin * hauteur_graphique / (ymax - ymin)); ;
        nouvelle_transform.appendScale(largeur_graphique / (xmax - xmin), -hauteur_graphique / (ymax - ymin));

        gc.setTransform(nouvelle_transform);

        // Calcul et stockage de la transformation inverse
        try {
            transformation_inverse = gc.getTransform().createInverse();
        } catch (NonInvertibleTransformException e)  {
            System.exit(1) ;
        } ;

        // Il faut ramener l'épaisseur du trait en coordonnées géométriques
        gc.setLineWidth(resolution.get());

    }

    public GraphicsContext gc() { return gc; }

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

    public void rafraichirDecor() {

//        Paint s = gc.getStroke() ;
//        Paint f = gc.getFill() ;
//
//        gc.setStroke(couleur_fond);
//        gc.setFill(couleur_fond);
//
        // Effacer la zone visible (rend les pixels transparents : le fond prédéfini dans le parent remplit la zone visible)
        gc.clearRect(xmin(), ymin(),xmax() - xmin(), ymax()-ymin());
//
//        gc.setStroke(s);
//        gc.setFill(f);

        environnement.accepter(visiteur_affichage);
    }

    public void montrerPoint(Point2D pt) {

        double rayon = (xmax()-xmin())/20 ;

        Paint s = gc.getStroke() ;
        gc.setStroke(Color.GREEN);

        gc.strokeOval(pt.getX()-rayon,pt.getY()-rayon,2*rayon,2*rayon);

        gc.setStroke(s);
    }

    public void montrerNormale(Point2D dep, Point2D normale) {
        Paint s = gc.getStroke() ;
        gc.setStroke(couleurNormales());

        // La normale est déjà normalisée (a priori)
//        normale = normale.normalize().multiply(30*resolution()) ;
        normale = normale.multiply(30*resolution()) ;

        Point2D arr = dep.add(normale) ;

        gc.strokeLine(dep.getX(),dep.getY(),arr.getX(),arr.getY());

        gc.setStroke(s);

    }
    public void montrerProlongementAvant(Point2D dep, Point2D arr) {
//        Paint s = gc.getStroke() ;
//        gc.setStroke(couleurNormales());
        gc().setLineWidth(1*resolution());

        gc.setLineDashes(2*resolution(),3*resolution());

        gc.strokeLine(dep.getX(),dep.getY(),arr.getX(),arr.getY());

        gc.setLineDashes();

        gc().setLineWidth(2*resolution());

//        gc.setStroke(s);

    }

    public void montrerProlongementArriere(Point2D dep, Point2D arr) {
//        Paint s = gc.getStroke() ;
//        gc.setStroke(Color.RED);
        gc().setLineWidth(1*resolution());

        gc.setLineDashes(2*resolution(),5*resolution());

        gc.strokeLine(dep.getX(),dep.getY(),arr.getX(),arr.getY());

        gc.setLineDashes();

        gc().setLineWidth(2*resolution());

//        gc.setStroke(s);

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
        eg.gc.fillPolygon( xpd,ypd,xpd.length);

    }

    public void remplirContour(Contour c) {
        double[] xpd = new double[c.nombrePoints()];
        double[] ypd = new double[c.nombrePoints()];

//        Iterator<Double> itx = c.xpoints.iterator() ;
        Iterator<Double> itx = c.iterateurX() ;
//        Iterator<Double> ity = c.ypoints.iterator() ;
        Iterator<Double> ity = c.iterateurY() ;

        int i = 0 ;
        while (itx.hasNext() && ity.hasNext()) {
            xpd[i] = itx.next();
            ypd[i] = ity.next() ;
            i++ ;
        }

        // Remplissage du contour (rappel : par défaut la FillRule du GraphicsContext est NON_ZERO : si les contours sont tracés dans
        // des sens contraires, le dernier tracé produit un trou dans le précédént)
        gc.fillPolygon( xpd,ypd,xpd.length);

    }

    public void afficherContourSurfaceObstacle(ContoursObstacle co) {
        gc.beginPath();

        for (Contour c_surface : co.contoursSurface()) {
            tracerContour(c_surface);
        }

    }

    // Affichage d'un obstacle (contours+masse remplie) à partir de son ContourObstacle préalablement calculé
    public void afficherContoursObstacle(ContoursObstacle co) {

        gc.beginPath();

        for (Contour c_masse : co.contoursMasse())
            completerPathAvecContour(c_masse);

        gc.closePath(); // Appel pas nécessaire, semble-t-il

        gc.fill();

        // On considère que le contour de masse (qui s'arrête aux limites de la zone visible) est aussi le contour visible
        // de l'obstacle => la couleur de contour sera appliquée aussi sur les bords de la zone visible
//        gc.stroke();

        // En désactivant la ligne suivante, la couleur de contour ne sera pas appliquée sur les bords de la zone visible
        // mais elle ne sera pas non plus appliquée
        for (Contour c_surface : co.contoursSurface()) {
//            gc.beginPath();
//            completerPathAvecContour(c_surface);
//            gc.stroke();
              tracerContour(c_surface);
        }

       // gc.closePath();


    }

    public void completerPathAvecContour(Contour c) {
        double[] xpd = new double[c.nombrePoints()];
        double[] ypd = new double[c.nombrePoints()];

        Iterator<Double> itx = c.iterateurX() ;
        Iterator<Double> ity = c.iterateurY() ;

        double xdep,ydep ;

        if (itx.hasNext()&&ity.hasNext()) {
            xdep = itx.next() ;
            ydep = ity.next() ;
            gc.moveTo(xdep,ydep);

            while (itx.hasNext() && ity.hasNext())
                gc.lineTo(itx.next(),ity.next());

            // Fermeture du chemin
            gc.lineTo(xdep,ydep);
        }


    }

    public void tracerContour(Contour c) {

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

    public void afficherPoignees(Contour c) {

        if (c==null)
            return;

        Iterator<Double> itx = c.iterateurX() ;
        Iterator<Double> ity = c.iterateurY() ;

        int i = 0 ;
        while (itx.hasNext() && ity.hasNext()) {
            double xpoignee = itx.next();
            double ypoignee = ity.next() ;
            gc.fillRect(xpoignee-tolerance_pointage()/2,ypoignee-tolerance_pointage()/2,tolerance_pointage(),tolerance_pointage());
            i++ ;
        }

    }

    public boolean poignee_obstacle_pointee_en(Point2D pclic) {

        if (obstacle_selectionne == null)
            return false ;

        Contour poignees = obstacle_selectionne.positions_poignees() ;

        if (poignees==null)
            return false ;

        if (poignees.comporte_point_proche_de(pclic,tolerance_pointage()))
            return true ;

        return false ;
    }

    public boolean poignee_source_pointee_en(Point2D pclic) {

        if (source_selectionnee == null)
            return false ;

        Contour poignees = source_selectionnee.positions_poignees() ;

        if (poignees==null)
            return false ;

        if (poignees.comporte_point_proche_de(pclic,tolerance_pointage()))
            return true ;

        return false ;


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
        gc.strokePolyline( xpd,ypd,xpd.length);

    }

    public void completerPathAvecContourFerme(Collection<Double> xpoints, Collection<Double> ypoints) {

//        double[] xpd = new double[xpoints.size()];
//        double[] ypd = new double[ypoints.size()];

//        eg.gc.beginPath();

        Iterator<Double> itx = xpoints.iterator() ;
        Iterator<Double> ity = ypoints.iterator() ;

        double xdep,ydep ;

        if (itx.hasNext()&&ity.hasNext()) {
            xdep = itx.next() ;
            ydep = ity.next() ;
            gc.moveTo(xdep,ydep);

            while (itx.hasNext() && ity.hasNext())
                gc.lineTo(itx.next(),ity.next());

            // Fermeture du chemin
            gc.lineTo(xdep,ydep);
        }


//        eg.gc.closePath();

    }

    public void completerPathAvecContourBoiteTrigo(BoiteLimiteGeometrique b) {

        gc.moveTo(b.getMaxX(),b.getMaxY());
        gc.lineTo(b.getMinX(),b.getMaxY());
        gc.lineTo(b.getMinX(),b.getMinY());
        gc.lineTo(b.getMaxX(),b.getMinY());

        // Fermeture du chemin
        gc.lineTo(b.getMaxX(),b.getMaxY());

    }


    public void completerPathAvecContourZoneVisibleAntitrigo() {

        gc.moveTo(xmax(), ymin());
        gc.lineTo(xmin(), ymin());
        gc.lineTo(xmin(), ymax());
        gc.lineTo(xmax(), ymax());

    }

    public static void tracerEtRemplirContourDepuisPath(CanvasAffichageEnvironnement eg, Path p) {

        double[] xpd = new double[p.size()];
        double[] ypd = new double[p.size()];

        Iterator<LongPoint> itp = p.iterator() ;

        int i = 0;
        while (itp.hasNext()) {
            LongPoint lp = itp.next() ;
            xpd[i] = lp.getX() ;
            ypd[i] = lp.getY() ;
            i++ ;
        }

        // Remplissage de l'intérieur du polygone
        eg.gc.fillPolygon( xpd,ypd,xpd.length);

        // Tracé du contour du polygone
        eg.gc.strokePolyline( xpd,ypd,xpd.length);


    }

    public BoiteLimiteGeometrique boite_limites() {
        return boite_limites;
    }

    public Environnement environnement() {
        return environnement;
    }

    public Obstacle obstacle_pointe_en(Point2D pt_g) {

        if (poignee_obstacle_pointee_en(pt_g))
            return obstacle_selectionne ;

        Obstacle obs = environnement.dernier_obstacle_contenant(pt_g) ;

        if (obs == null)
            obs = environnement.dernier_obstacle_tres_proche(pt_g, tolerance_pointage());

        return obs ;

    }




    public Source source_pointee_en(Point2D pt_g) {

        if (poignee_source_pointee_en(pt_g))
            return source_selectionnee ;

//        Source s = environnement.derniere_source_contenant(pt_g) ;
//
//        if (s == null)
//            s = environnement.derniere_source_tres_proche(pt_g, tolerance_pointage());
//
//        return s ;

        return environnement.derniere_source_tres_proche(pt_g, tolerance_pointage());

    }

}
