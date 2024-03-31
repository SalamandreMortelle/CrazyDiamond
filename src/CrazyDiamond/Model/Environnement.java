package CrazyDiamond.Model;

import CrazyDiamond.Controller.ElementsSelectionnes;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;

import java.util.Iterator;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Environnement {

    static double resolution = 0.00001 ;

    // Valeurs par défaut de certains attributs
    private static final double indice_refraction_par_defaut = 1.0 ;

    private static final Unite unite_par_defaut = Unite.M ;
    private static final Color couleur_fond_par_defaut = Color.BLACK ;
    private static final boolean reflexion_avec_refraction_par_defaut = false ;

    protected final BooleanProperty reflexion_avec_refraction ;

    /**
     * Unité utilisée pour l'affichage et la sérialisation (sauvegarde) des longueurs de l'Environnement.
     * <br/> L'unité utilisée dans les calculs de l'application reste toujours le mètre, et ne dépend pas de l'unité choisie
     * pour l'affichage.
     */
    protected final ObjectProperty<Unite> unite ;

    protected final ObjectProperty<Color> couleur_fond;

    protected final StringProperty commentaire ;

    protected final DoubleProperty indice_refraction ;

//    private final ListProperty<Obstacle> obstacles ;
    private final ObjectProperty<Groupe> groupe_racine_obstacles;
    private final ListProperty<Source> sources ;
    private final ListProperty<SystemeOptiqueCentre> systemes_optiques_centres ;

    private boolean suspendre_illumination_sources = false ;
    private boolean suspendre_rafraichissement_affichages = false ;
    private boolean suspendre_calculs_elements_cardinaux_soc = false ;

    // Récupération du logger
    private static final Logger LOGGER = Logger.getLogger( "CrazyDiamond" );
    private Unite prochaine_unite = null ;

    // TODO : une clip zone non rectangulaire (environnement circulaire, etc.)

    // TODO : propriétés des limites de l'environnement : immatérielle (par défaut), réfléchissante, absorbante...
    // NON : si on veut des limites réfléchissantes, il suffit de créer un obstacle Rectangle Convexe pour délimiter la zone


    public Environnement() {
        this(unite_par_defaut, couleur_fond_par_defaut,reflexion_avec_refraction_par_defaut) ;
    }

    public Environnement(Unite unite, Color c_fond,boolean refl_avec_refraction) {

        this.unite = new SimpleObjectProperty<>(unite) ;

        this.indice_refraction = new SimpleDoubleProperty(indice_refraction_par_defaut) ;

        this.couleur_fond = new SimpleObjectProperty<>(c_fond) ;

        this.reflexion_avec_refraction = new SimpleBooleanProperty(refl_avec_refraction) ;
        reflexion_avec_refraction.addListener((observable, oldValue,newValue) -> this.illuminerToutesSources());

        this.commentaire = new SimpleStringProperty("") ;


        groupe_racine_obstacles = new SimpleObjectProperty<>(new Groupe("Groupe racine",false));
//      ObservableList<Obstacle> olo = FXCollections.observableArrayList() ;
//      obstacles = new SimpleListProperty<>(olo);

        ObservableList<Source> ols = FXCollections.observableArrayList() ;
        sources   = new SimpleListProperty<>(ols);

        ObservableList<SystemeOptiqueCentre> olsoc = FXCollections.observableArrayList() ;
        systemes_optiques_centres   = new SimpleListProperty<>(olsoc) ;

        // NB : Les rayons étant dans les sources, l'environnement n'a pas besoin d'être à l'écoute des changements de
        // source : si une source est retirée, ses rayons (chemins) disparaissent avec elle

//        lcl_sources = (ListChangeListener<Source>) c -> {
//            while (c.next()) {
//                // Rien à faire pour les sources mises à jour, les bindings ont déjà été définis lors de l'ajout de la source
//                if (c.wasUpdated()) {
//                    System.out.println("Source modifiée : ");
//                    for (int i = c.getFrom(); i < c.getTo(); ++i) {
//                        // updates ...
//                        sources.get(i).illuminer();
//                    }
//                } else {
//                    // Rien à faire pour les sources retirées / leurs chemins disparaissent avec elles (car les chemins
//                    // sont des attributs des sources)
//                    // for (Source remitem : c.getRemoved()) {  }
//
//                    if (c.wasAdded())
//                    for (Source additem : c.getAddedSubList()) {
//                        System.out.println("Source ajoutée : "+additem);
//                        additem.positionXProperty().addListener((observable, oldValue, newValue) -> {
//                            System.out.println("X passe de "+oldValue+" à "+newValue);
//
//                            additem.illuminer();
//                        });
//                    }
//                }
//            }
//        };

        // Si des obstacles sont ajoutés ou supprimés, il faut recalculer les tracés des rayons des sources
        ListChangeListener<Obstacle>  lcl_obstacles_pour_illumination = change -> {
            illuminerToutesSources();
//            while (change.next()) {
//
//                if (change.wasRemoved()) {
//                    //                  for (Source remitem : change.getRemoved()) { }
//                    LOGGER.log(Level.FINE,"Détection de la suppression d'un obstacle") ;
//                    illuminerToutesSources();
//                } else if (change.wasAdded()) {
//                    LOGGER.log(Level.FINE,"Détection de l'ajout d'un obstacle") ;
////                    for (Obstacle additem : change.getAddedSubList()) {
////                        System.out.println("ENV : Obstacle ajouté : " + additem);
////                        additem.ajouterRappelSurChangementToutePropriete(this::rafraichirAffichage);
////                    }
//                    illuminerToutesSources();
//                }
//            }
        };

//        obstacles.addListener(lcl_obstacles);

        // Ajoute le listener dans les obstacles de 1er niveau et, récursivement, dans tous les sous-groupes, et dans toutes les compositions
        groupeRacine().ajouterListChangeListener(lcl_obstacles_pour_illumination);

    }

    public Groupe groupeRacine() {
        return groupe_racine_obstacles.getValue() ;
    }
    public Unite unite() { return (prochaine_unite==null?unite.get():prochaine_unite) ;}

    public ObjectProperty<Unite> uniteProperty() {return unite ;}

    public double indiceRefraction() { return indice_refraction.get(); }
    public DoubleProperty indiceRefractionProperty() { return indice_refraction ; }
    public void definirIndiceRefraction(double indice) { this.indice_refraction.set(indice); }


    public Color couleurFond() {
        return couleur_fond.get();
    }
    public ObjectProperty<Color> couleurFondProperty() { return couleur_fond; }
    public void definirCouleurFond(Color c) { couleur_fond.set(c); }

    public boolean reflexionAvecRefraction() {
        return reflexion_avec_refraction.get() ;
    }

    public void accepter(VisiteurEnvironnement v) {

        v.avantVisiteEnvironnement(this);

//        Iterator<Obstacle> ito = obstacles.iterator() ;
        // Parcours en profondeur des obstacles : de l'arrière-plan vers l'avant plan
        Iterable<Obstacle> ito = groupeRacine().iterableObstaclesDepuisArrierePlan() ;
        Iterator<Source>   its = sources.iterator() ;
        Iterator<SystemeOptiqueCentre> itsoc = systemes_optiques_centres.iterator() ;

        // Parcours des obstacles
        v.avantVisiteObstacles();

        for (Obstacle o : ito)
            o.accepte(v);
//        while (ito.hasNext())
//            ito.next().accepte(v);

        v.apresVisiteObstacles();

        // Parcours des sources
        v.avantVisiteSources();

        while (its.hasNext())
            its.next().accepte(v);

        v.apresVisiteSources();

        // Parcours des SOC : pour afficher leurs axes, et (si besoin) leurs éléments cardinaux
        v.avantVisiteSystemesOptiquesCentres();

        while (itsoc.hasNext())
            itsoc.next().accepte(v);

        v.apresVisiteSystemesOptiquesCentres();

        v.apresVisiteEnvironnement(this);
    }

    public void ajouterListenerListeSources(ListChangeListener<Source> lcl_s) {
        sources.addListener(lcl_s);
    }

    public void ajouterListChangeListenerObstacles(ListChangeListener<Obstacle> lcl_o) {
//        groupe_racine_obstacles.addListener(lcl_o);
        groupeRacine().ajouterListChangeListener(lcl_o);

//        //Il faut aussi détecter les changements qui interviennent dans les compositions
//        for (Obstacle o : groupe_racine_obstacles) {
//            if (o.getClass() == Composition.class) {
//                Composition comp = (Composition) o ;
//                comp.ajouterListChangeListener(lcl_o) ;
//            }
//        }

    }

    public void enleverListChangeListenerObstacles(ListChangeListener<Obstacle> lcl_o) {
        groupeRacine().enleverListChangeListener(lcl_o);
    }
    public void enleverTousLesListChangeListenersObstacles() {
        groupeRacine().enleverTousLesListChangeListeners();
    }
    public void ajouterListenerListeSystemesOptiquesCentres(ListChangeListener<SystemeOptiqueCentre> lcl_soc) {
        systemes_optiques_centres.addListener(lcl_soc);
    }

    public int nombreSources() { return sources.size(); }
//    public int nombreObstacles() { return obstacles.size(); }
    public int nombreObstaclesPremierNiveau() { return groupeRacine().nombreObstaclesPremierNiveau(); }
    public int nombreSystemesOptiquesCentres() { return systemes_optiques_centres.size(); }


    /**
     * Iterateur retournant tous les obstacles de l'environnement, réels ou non (hormis les Obstacles inclus dans les
     * Compositions) en commençant par l'arrière-plan et jusqu'au premier-plan.
     * @return : l'itérateur
     */
    public Iterator<Obstacle> iterateur_obstacles() {
//        return groupe_racine_obstacles.iterator() ;
        return groupeRacine().iterateurObstaclesDepuisArrierePlan() ;
    }

    public Iterator<Obstacle> iterateur_obstacles_reels() {
//        return groupe_racine_obstacles.iterator() ;
        return groupeRacine().iterateurObstaclesReelsEnProfondeur() ;
    }

    public ListIterator<Obstacle> iterateur_inverse_liste_obstacles_reels() {
//        return obstacles.listIterator(obstacles.size()) ;
        return groupeRacine().iterateurInverseObstaclesReelsDepuisPremierPlan() ;
    }

    public Iterator<Obstacle> iterateur_obstacles_premier_niveau() {
//        return groupe_racine_obstacles.iterator() ;
        return groupeRacine().iterateurPremierNiveau() ;
    }

    public boolean obstaclesReelsComprennent(Obstacle o) {
        return groupeRacine().obstaclesReelsComprennent(o) ;
    }
    public boolean obstaclesComprennent(Obstacle o) {
        return groupeRacine().obstaclesComprennent(o) ;
    }
    public int indexObstacleALaRacine(Obstacle o) {
        return groupeRacine().indexObstacleALaRacine(o) ;
    }

    public ObservableList<Source> sources() {
        return sources.get() ;
    }

    private ListIterator<Source> iterateur_liste_sources_sur_fin() {
        return sources.listIterator(sources.size()) ;
    }

    public Iterator<Source> iterateur_sources() {
        return sources.iterator() ;
    }

    public ObservableList<SystemeOptiqueCentre> systemesOptiquesCentres() {
        return systemes_optiques_centres.get() ;
    }

    private ListIterator<SystemeOptiqueCentre> iterateur_liste_soc_sur_fin() {
        return systemes_optiques_centres.listIterator(systemes_optiques_centres.size()) ;
    }

    public Iterator<SystemeOptiqueCentre> iterateur_systemesOptiquesCentres() {return systemes_optiques_centres.iterator() ;}


    public void ajouterSource(Source s) {

        if (this.sources.contains(s))
            return ;

        // Il faut faire les liaisons avant d'ajouter la source à la liste, car la liste est observable par des vues
        // qui doivent être notifiées de l'ajout après que les chemins des rayons ont été recalculés (via appel
        // à Source::illuminer)

        s.ajouterRappelSurChangementTouteProprieteModifiantChemin(s::illuminer);

        s.illuminer();

        this.sources.add(s) ;

    }

    public void ajouterSystemeOptiqueCentre(SystemeOptiqueCentre s) {
        if (this.systemes_optiques_centres.contains(s))
            return ;

        this.systemes_optiques_centres.add(s) ;
    }

    public SystemeOptiqueCentre systemeOptiqueCentreContenant(Obstacle o) {
        for (SystemeOptiqueCentre soc : systemes_optiques_centres)
            if (soc.comprend(o))
                return soc ;

        return null ;
    }

    public Composition compositionContenant(Obstacle o) {
        return groupeRacine().compositionContenant(o) ;
//        for (Obstacle ob  : groupe_racine_obstacles) {
//            if (ob.getClass() == Composition.class && ob.comprend(o))
//                    return ob.composition_contenant(o);
//        }
//
//        return null ;
    }
    public Groupe groupeContenant(Obstacle o) {
        return groupeRacine().sousGroupeContenant(o) ;
    }


//    public Source derniereSource() {
//        if (sources.size()>0)
//            return sources.get(sources.size()-1) ;
//
//        return null ;
//    }

    public void ajouterObstacleALaRacine(Obstacle o) {

        if (groupeRacine().comprend(o))
            return;
//        if (this.groupe_racine_obstacles.contains(o))
//            return;

        // A VERIFIER :  a priori inutile les éléments du groupe racine sont déjà surveillés par lcl_illumination ; cf. ligne 143
//        o.ajouterRappelSurChangementTouteProprieteModifiantChemin( this::illuminerToutesSources);

        groupeRacine().ajouterObstacle(o);
        // Tous les ListChangeListeners déjà définis sur le groupe racine sont automatiquement appliqués à l'Obstacle
        // ajouté et à ses éventuels fils grâce à l'appel ci-dessus.

//        this.groupe_racine_obstacles.add(o);

       // TODO ajouter un listener sur la liste des obstacles et appeler illuminerTouteSource lors d'un ajout => inutile c'est automatique dans l'objet Groupe

    }

//    public List<Obstacle> obstacles() {
//        return groupe_racine_obstacles;
//    }



    public void deplacerObstacleEnPositionALaRacine(Obstacle o_a_deplacer, int i_pos) {
        if (!groupeRacine().estALaRacine(o_a_deplacer))
            return;
//        groupe_racine_obstacles.remove(o_a_deplacer);
//        groupe_racine_obstacles.add(i_pos,o_a_deplacer);

        groupeRacine().deplacerObstacleEnPositionALaRacine(o_a_deplacer,i_pos);

        repositionnerObstacleDansSoc(o_a_deplacer, i_pos);
        // TODO : Ligne précédente à revoir car ne marche que quand les SOCs ne contiennent que des obstacles à la racine
        // A remplacer à terme par qqh comme :
        // repositionnerObstacleDansSoc(o_a_deplacer,groupeRacine().indexParmiObstaclesReels(o_a_deplacer));

    }

    public void ajouterObstacleEnPositionALaRacine(Obstacle o_a_ajouter, int i_pos_dans_env) {
        if (groupeRacine().estALaRacine(o_a_ajouter))
            return;
//        if (this.groupe_racine_obstacles.contains(o_a_ajouter))
//            return;
//
//        o_a_ajouter.ajouterRappelSurChangementTouteProprieteModifiantChemin( this::illuminerToutesSources);
//
//        groupe_racine_obstacles.add(i_pos_dans_env,o_a_ajouter);
        groupeRacine().ajouterObstacleEnPosition(o_a_ajouter,i_pos_dans_env);

        repositionnerObstacleDansSoc(o_a_ajouter, i_pos_dans_env);
        // TODO : Ligne précédente à revoir car ne marche que quand les SOCs ne contiennent que des obstacles à la racine
        // A remplacer à terme par qqh comme :
        // repositionnerObstacleDansSoc(o_a_deplacer,groupeRacine().indexParmiObstaclesReels(o_a_ajouter));
    }

    protected void repositionnerObstacleDansSoc(Obstacle o_a_deplacer, int i_pos_dans_env) {

        if (o_a_deplacer.appartientASystemeOptiqueCentre()) {

            SystemeOptiqueCentre soc = systemeOptiqueCentreContenant(o_a_deplacer) ;

            // Déplacer l'obstacle dans le SOC sachant qu'il est maintenant à la position i_pos_dans_env dans l'environnement
            soc.deplacerObstacle(o_a_deplacer, i_pos_dans_env) ;
        }
    }

//    public Obstacle dernierObstacle() {
//        if (groupe_racine_obstacles.size()>0)
//            return groupe_racine_obstacles.get(groupe_racine_obstacles.size()-1) ;
//
//        return null ;
//    }


    public void supprimerSource(Source s) {

        // TODO : voir si interet à faire les unbind

        sources.remove(s) ;

    }

    public void supprimerSystemeOptiqueCentre(SystemeOptiqueCentre s) {
        s.detacherObstacles() ;

        systemes_optiques_centres.remove(s) ;
    }

    public void illuminerToutesSources() {

        if (suspendre_illumination_sources)
            return;

        Iterator<Source> its = iterateur_sources();

        while (its.hasNext())
            its.next().illuminer();
    }


    /**
     * Supprime l'obstacle o de l'Environnement, qu'il fasse partie d'un Groupe (le Groupe Racine ou un sous-groupe), ou
     * d'une Composition. S'il faisait partie d'n SOC, il en est retiré
     * @param o : l'obstacle à supprimer
     */
    public void supprimerObstacle(Obstacle o) {

        // Si l'obstacle appartient à un SOC, on l'en retire
        if (o.appartientASystemeOptiqueCentre())
            systemeOptiqueCentreContenant(o).retirerObstacleCentre(o);

        // On le retire de son parent (Groupe ou Composition)
        o.parent().retirerObstacle(o);
    }

    /**
     * Retire (supprime) un obstacle de l'environnement.
     * S'il appartenait à une composition, il en est retiré avant d'être supprimé de l'environnement.
     * S'il appartenait à un SOC, sa référence en est retirée avant qu'il soit supprimé de l'environnement.
     * @param o : obstacle à retirer.
     */
    public void supprimerObstacleALaRacine(Obstacle o) {

        if (o.appartientAComposition()) {
            Composition comp_contenante = compositionContenant(o);

            comp_contenante.retirerObstacle(o) ;
            if (comp_contenante.appartientASystemeOptiqueCentre())
                o.definirAppartenanceSystemeOptiqueCentre(false);
        }
        // TODO: gérer le retrait d'un obstacle qui appartient à un sous-groupe
//        else if (o.appartientAGroupe()) { // Attention : tous les obtacles de l'environnement appartiennent au groupe Racine...
//            Groupe grp_contenant = groupeContenant(o);
//
//            grp_contenant.retirerObstacle(o) ;
//            if (grp_contenant.appartientASystemeOptiqueCentre())
//                o.definirAppartenanceSystemeOptiqueCentre(false);
//        }


        if (o.appartientASystemeOptiqueCentre())
            systemeOptiqueCentreContenant(o).retirerObstacleCentre(o);

        // TODO : il faudrait enlever les listeners qui avaient été mis (via Obstacle.ajouterRappelSurChangementTouteProprieteModifiantChemin)
        // Car le fait d'enlever puis de ré-ajouter un obstacle de l'environnement fait qu'il déclenchera (notifiera) deux fois tous ses rappels
        // mais il faudrait alors penser à appeler ajouterRappel.. lorsqu'on ajoute cet obstacle dans une composition. Complexe. Plus simple de tolérer
        // les notifications redondantes.
//        groupe_racine_obstacles.remove(o) ;
        groupeRacine().retirerObstacle(o);

        // TODO : ajouter un listener sur la liste des obstacles et appeler illuminerTouteSource lors d'un retrait => INUTILE c'est automatique lors
        // du retrait du groupe racine car un lcl_illumination a été ajouté par l'environnement lors de l'ajout de l'obstacle

    }

    /**
     *
     * @param p : point pour lequel on cherche un obstacle qui le contient
     * @return le dernier des obstacles qui contient le point (dans la liste des obstacles de l'environnement), ou
     * null s'il n'y en a aucun.
     */
    public Obstacle obstacle_contenant(Point2D p) {
        Iterator<Obstacle> ito = iterateur_obstacles_reels() ;

        // Verifier que le point de départ du rayon n'est pas dans un obstacle
        // TODO : supprimer le test ci-dessous quand la refraction sera gérée (le point de départ pourra être dans un obstacle)
        while (ito.hasNext()) {
            Obstacle o = ito.next() ;
            if (o.contient(p)) {
                LOGGER.log(Level.FINEST,"Le point de départ du rayon est dans l'obstacle {0}",o) ;
                return o;
            }
        }

        return null ;
    }

    public Obstacle obstacleReelAuPremierPlanContenant(Point2D p) {
        ListIterator<Obstacle> ito = iterateur_inverse_liste_obstacles_reels() ;

        // Verifier que le point de départ du rayon n'est pas dans un obstacle
        // TODO : supprimer le test ci-dessous quand la refraction sera gérée (le point de départ pourra être dans un obstacle)
        while (ito.hasPrevious()) {
            Obstacle o = ito.previous() ;
            if (o.estReel() && o.contient(p)) {
                LOGGER.log(Level.FINEST,"Le point de départ du rayon est dans l'obstacle {0}",o) ;
                return o;
            }
        }

        return null ;
    }

    public Obstacle obstacle_a_selectionner(Point2D p) {

        // On commence par chercher l'obstacle réel qui se trouve au premier plan au point p
        Obstacle obs_reel = obstacleReelAuPremierPlanContenant(p) ;

        if (obs_reel==null)
            return null ;

        return groupeRacine().plus_grand_groupe_solidaire_contenant(obs_reel);

    }



    public Obstacle obstacleReelAuPremierPlanTresProcheDe(Point2D p, double tolerance) {
        ListIterator<Obstacle> ito = iterateur_inverse_liste_obstacles_reels()  ;

        // Verifier que le point de départ du rayon n'est pas dans un obstacle
        // TODO : supprimer le test ci-dessous quand la refraction sera gérée (le point de départ pourra être dans un obstacle)
        while (ito.hasPrevious()) {
            Obstacle o = ito.previous() ;
            if (o.estTresProcheDe(p,tolerance)) {
                LOGGER.log(Level.FINEST,"Point très proche de l'obstacle {0}",o) ;
                return o;
            }
        }

        return null ;
    }

    public Source derniere_source_tres_proche(Point2D p, double tolerance_pointage) {
        ListIterator<Source> its = iterateur_liste_sources_sur_fin()  ;

        while (its.hasPrevious()) {
            Source s = its.previous() ;
            if (s.est_tres_proche_de(p,tolerance_pointage)) {
                LOGGER.log(Level.FINEST,"Point très proche de la source {0}",s) ;
                return s;
            }
        }

        return null ;

    }

    public SystemeOptiqueCentre dernier_soc_tres_proche(Point2D p, double tolerance_pointage) {
        ListIterator<SystemeOptiqueCentre> its = iterateur_liste_soc_sur_fin()  ;

        while (its.hasPrevious()) {
            SystemeOptiqueCentre soc = its.previous() ;
            if (soc.est_tres_proche_de(p,tolerance_pointage)) {
                LOGGER.log(Level.FINEST,"Point très proche du SOC {0}",soc) ;
                return soc;
            }
        }

        return null ;

    }

    /**
     *
     * @param p : point pour lequel on cherche un obstacle qui le contient autre que l'obstacle obs
     * @param obs : l'obstacle a exclure des résultats
     * @return un Obstacle différent de obs qui contient le point (le dernier trouvé dans la liste des obstacles de l'environnement),
     * ou null s'il n'y en a aucun.
     */
    public Obstacle autre_obstacle_contenant(Point2D p, Obstacle obs) {
        //Iterator<Obstacle> ito = iterateur_obstacles() ;
        ListIterator<Obstacle> ito = iterateur_inverse_liste_obstacles_reels() ;

//        Obstacle resultat = null ;

        while (ito.hasPrevious()) {
            Obstacle o = ito.previous() ;
            if (o.contient(p) && (o!=obs)) {
                LOGGER.log(Level.FINEST,"Autre obstacle contenant le point trouvé : {0}",o ) ;
                return o ;
            }
        }

//        return resultat ;
        return null ;
    }

    /**
     * Retourne l'obstacle dans le milieu duquel émerge un rayon incident r qui part de p_rencontre_dioptre
     * l'obstacle retourné contient forcément un milieu (i.e. il a une épaisseur)
     * @param r : le rayon incident
     * @param p_rencontre_dioptre : le point où le rayon r rencontre un dioptre (point qui appartient donc à la surface d'un des obstacles)
     * @return l'obstacle d'émergence du rayon incident
     */
    public Obstacle obstacle_emergence(Rayon r, Point2D p_rencontre_dioptre, Obstacle obs_rencontre) throws Exception {
        return obstacle_emergence_dans_soc(r,p_rencontre_dioptre,obs_rencontre,null) ;
    }

    /**
     * Retourne l'obstacle du SOC dans le milieu duquel émerge un rayon incident r qui part de p_rencontre_dioptre
     * l'obstacle retourné contient forcément un milieu (i.e. il a une épaisseur)
     * @param r : le rayon incident
     * @param p_rencontre_dioptre : le point où le rayon r rencontre un dioptre (point qui appartient donc à la surface d'un des obstacles du SOC si un SOC est fourni)
     * @return l'obstacle d'émergence du rayon incident
     */
    public Obstacle obstacle_emergence_dans_soc(Rayon r, Point2D p_rencontre_dioptre,Obstacle obs_rencontre, SystemeOptiqueCentre soc) throws Exception {

        // On parcourt les obstacles depuis la fin jusqu'au début pour tenir compte de la précédence sur l'axe Z (Z-order)
        ListIterator<Obstacle> ito = iterateur_inverse_liste_obstacles_reels();

        while (ito.hasPrevious()) {
            Obstacle o = ito.previous();

            // Si un SOC est passé en paramètre, on ne traite que les obstacles de ce SOC
            if (soc!=null && !soc.comprend(o))
                continue ;

            // On ignore les obstacles sans épaisseur
            if (o.natureMilieu()==NatureMilieu.PAS_DE_MILIEU)
                continue;

            boolean a_sur_surface = (o == obs_rencontre || o.aSurSaSurface(p_rencontre_dioptre)) ;

            // Si p_rencontre_dioptre est strictement contenu dans o, o est l'obstacle d'émergence
            if (!a_sur_surface && o.contient(p_rencontre_dioptre))
                return o ;

            // Sinon, si o a p_rencontre_dioptre sur sa surface et que r entre dans l'obstacle o, o est l'obstacle d'émergence
            if (a_sur_surface && o.normale(p_rencontre_dioptre).dotProduct(r.direction())<0d)
                return o ;
        }

        return null ;
    }

    public static boolean quasiEgal(double a, double b) {
        return Math.abs(a - b) <= Environnement.resolution;
    }

    public static boolean quasiInferieurOuEgal(double a, double b) {
        return a <= (b + Environnement.resolution);
    }

    public static boolean quasiSuperieurOuEgal(double a, double b) {
        return (a + Environnement.resolution) >= b;
    }

    public static boolean quasiEgal(double a, double b,double tolerance) {
        return Math.abs(a - b) <= tolerance;
    }

    public static boolean quasiInferieurOuEgal(double a, double b,double tolerance) {
        return a <= (b + tolerance);
    }

    public static boolean quasiSuperieurOuEgal(double a, double b,double tolerance) {
        return (a + tolerance) >= b;
    }

    public static boolean quasiConfondus(Point2D a, Point2D b) {
        return ( quasiEgal(a.getX(),b.getX()) && quasiEgal(a.getY(),b.getY()) ) ;
    }

    /**
     * Recherche parmi tous les obstacles de l'environnement, y compris ceux qui font partie de compositions,
     * et y compris parmi les groupes si l'un
     * d'entre eux porte l'identifiant obs_id, et le retourne.
     * @param obs_id : identifiant d'obstacle recherché
     * @return l'obstacle trouvé, ou 'null' sinon.
     */
    public Obstacle obstacle(String obs_id) {

//        for (Obstacle o : groupe_racine_obstacles) {
        for (Obstacle o : groupeRacine().iterableObstaclesDepuisArrierePlan()) {

            Obstacle o_trouve = o.obstacle_avec_id(obs_id) ;

            if (o_trouve!=null)
                return o_trouve ;
        }

        return null ;
    }

    public BooleanProperty reflexionAvecRefractionProperty() {
        return reflexion_avec_refraction ;
    }

    public StringProperty commentaireProperty() {
        return commentaire ;
    }


    public void definirCommentaire(String commentaire_saisi) {
        commentaire.set(commentaire_saisi);
    }

    public String commentaire() {
        return commentaire.get() ;
    }

    public void changerUnite(Unite originelle, Unite cible) {

        prochaine_unite = cible ;

        suspendre_illumination_sources = true ;
        suspendre_rafraichissement_affichages = true ;
        suspendre_calculs_elements_cardinaux_soc = true ;
        convertirDistances(originelle.valeur/cible.valeur) ;
        suspendre_calculs_elements_cardinaux_soc = false ;
        suspendre_rafraichissement_affichages = false ;
        suspendre_illumination_sources = false ;

        // Inutile de recalculer les éléments cardinaux (et la matrice de transfert) : ils sont inchangés ; il faut juste
        // convertir leurs positions/dimensions dans la nouvelle unité au niveau de la méthode SystemeOptiqueCentre::convertirDistancesEtRafraichirAffichage
//        for (SystemeOptiqueCentre soc : systemes_optiques_centres)
//            soc.calculeElementsCardinaux();


        illuminerToutesSources();

        unite.set(cible); // Déclenche la redéfinition des x_min_g, xmax_g, y_centre_g de la zone visible du
        // CanvasAffichageEnvironnement qui contient cet environnement

        prochaine_unite = null ;

    }
    private void convertirDistances(double facteur_conversion) {
//        for (Obstacle o : groupe_racine_obstacles)
        for (Obstacle o : groupeRacine().iterableObstaclesReelsDepuisArrierePlan())
            o.convertirDistances(facteur_conversion);
        for (Source s : sources)
            s.convertirDistances(facteur_conversion);
        for (SystemeOptiqueCentre soc : systemes_optiques_centres)
            soc.convertirDistances(facteur_conversion);

        Commande.convertirDistancesHistoriques(facteur_conversion) ;
    }

    public boolean rafraichissementAffichagesSuspendu() { return suspendre_rafraichissement_affichages ;}

    public boolean calculsElementsCardinauxSocSuspendus() { return suspendre_calculs_elements_cardinaux_soc ; }

    public String suffixeUnite() { return " "+unite().symbole ; }

    public void ajouterElements(ElementsSelectionnes es) {
        es.stream_sources().forEach(this::ajouterSource);
        es.stream_obstacles().forEach(this::ajouterObstacleALaRacine);
        es.stream_socs().forEach(this::ajouterSystemeOptiqueCentre);
    }

    public int indexDansParent(Obstacle o) {
        return o.parent().indexALaRacine(o) ;
//        return groupeRacine().indexParmiObstaclesReels(o) ;
//        return groupeRacine().indexObstacleALaRacine(o);
    }

    public int indexParmiObstaclesReels(Obstacle o) {
        return groupeRacine().indexParmiObstaclesReels(o) ;
//        return groupeRacine().indexObstacleALaRacine(o);
    }


    public Obstacle obstacle(int index_a_la_racine) {
//        return groupe_racine_obstacles.get(indexDansParent);
        return groupeRacine().obstacle(index_a_la_racine) ;
    }

    public boolean estALaRacine(Obstacle o) {
        return groupeRacine().estALaRacine(o);
    }
}

