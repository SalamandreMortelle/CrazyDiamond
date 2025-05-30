package CrazyDiamond.Model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class Groupe extends BaseObstacleComposite implements Obstacle, Identifiable, Nommable {

    private final BooleanProperty elements_solidaires ;

    private static int compteur_groupe = 0;

//    private final ListChangeListener<Obstacle> lcl_reconstruction_listes_obstacles = change -> {
//        construireListesObstacles();
//    };

//    private ArrayList<Obstacle> liste_obstacles_reels;
//    private ArrayList<Obstacle> liste_obstacles;


    public Groupe(boolean solidaire) throws IllegalArgumentException {
        this("Groupe " + (++compteur_groupe),solidaire) ;
    }

    public Groupe(String nom,boolean solidaire) throws IllegalArgumentException {
        this(
                new Imp_Identifiable(),
                new Imp_Nommable(nom),
                new Imp_ElementComposite(),
                solidaire
        ) ;
    }

    public Groupe(Imp_Identifiable ii, Imp_Nommable in,Imp_ElementComposite ic,boolean solidaire) throws IllegalArgumentException {
        super(ii,in,ic);

        // Ce groupe est le parent de tous ses éléments
        for (Obstacle o : ic.elements())
            o.definirParent(this);

        this.elements_solidaires = new SimpleBooleanProperty(solidaire) ;

//        construireListesObstacles();

//        this.ajouterListChangeListenerDesGroupes(lcl_reconstruction_listes_obstacles);
    }

//    public int indexObstacleALaRacine(Obstacle o) {
//        return elements().indexOf(o) ;
//    }

    /**
     * Retourne la plus petite composition contenant l'obstacle o
     * @param o obstacle recherché
     * @return la plus petite Composition trouvée
     */
    public Composition plusPetiteCompositionContenant(Obstacle o) {
        for (Obstacle ob  : iterableObstaclesReelsDepuisArrierePlan()) { // Marcherait tout aussi bien en cherchant avec iterableObstaclesDepuisArrierePlan
            if (ob instanceof Composition && ob.comprend(o))
                return ob.composition_contenant(o);
        }

        return null ;
    }

    /**
     * Retourne la plus grande composition contenant l'obstacle o
     * @param o obstacle recherché
     * @return la plus grande Composition trouvée
     */
    public Composition plusGrandeCompositionContenant(Obstacle o) {
        for (Obstacle ob  : iterableObstaclesReelsEnLargeurDepuisPremierPlan()) {
            if (ob instanceof Composition compo && compo.comprend(o))
                return compo ;
        }

        return null ;
    }

    /**
     * Retourne l'obstacle qui est, ou qui contient dans l'une de ses composantes (cas des groupes)
     * dans l'une de ses composantes privées (cas des lentilles par exemple), l'obstacle o
     * @param o l'obstale dont on cherche le contenant (qui peut être lui-même)
     * @return l'obstacle trouvé qui peut être l'obstacle o lui-même
     */
    public Obstacle obstacleContenant(Obstacle o) {
        for (Obstacle ob  : iterableObstaclesReelsEnLargeurDepuisPremierPlan()) {
            if (ob.comprend(o))
                return ob ;
        }

        return null ;
    }


    public Groupe sousGroupeContenant(Obstacle o) {
        for (Obstacle ob  : elements()) {
            if (ob instanceof Groupe && ob.comprend(o))
                return ob.groupe_contenant(o);
        }

        return null ;
    }

    /**
     * Retourne le groupe qui contient directement (au premier niveau) l'obstacle o, c'est-à-dire le plus petit groupe
     * contenant l'obstacle o
     * @param o l'obstacle dont on cherche le groupe d'appartenance
     * @return le groupe d'appartenance trouvé ou null s'il n'y en a pas
     */
    public Groupe groupe_contenant(Obstacle o) {
        for (Obstacle ob : elements()) {
            if (ob.comprend(o)) {
                Groupe g_cont = ob.groupe_contenant(o);
                return (g_cont!=null?g_cont:this) ;
            }
        }

        return null ;
    }

    public Groupe plusGrandGroupeSolidaireContenant(Obstacle obs_reel) {

        // On extrait la liste (du plus englobant jusqu'au plus petit) des groupes qui contiennent obs_reel
        List<Groupe> groupes = groupes_contenant(obs_reel) ;

        // On parcourt ces groupes à l'envers : du plus petit au plus englobant
        ListIterator<Groupe> itr = groupes.listIterator(groupes.size()) ;

        Groupe resultat = null ;

        while (itr.hasPrevious()) {
            Groupe g = itr.previous() ;
            if (g.elementsSolidaires())  // Tant que les éléments du groupe sont solidaires, on peut poursuivre la recherche au niveau supérieur
                resultat = g ;
            else
                return resultat;
        }

        return resultat ;
    }

    public List<Groupe> groupes_contenant(Obstacle o) {

        ArrayList<Groupe> resultat = new ArrayList<>(1) ;

        cherche_prochain_groupe_contenant(o,resultat);

        return resultat ;
    }

    private void cherche_prochain_groupe_contenant(Obstacle o, List<Groupe> l) {
        for (Obstacle ob : elements()) {
            if ( (ob instanceof Groupe) && ob.comprend(o)) {
                l.add((Groupe)ob) ;
                ((Groupe)ob).cherche_prochain_groupe_contenant(o,l);
            }
        }

    }

    /**
     * Retourne le premier groupe contenant l'obstacle o
     * @param o : l'obstacle recherché
     * @return le groupe trouvé, ou 'null' s'il n'y en a pas
     */
    public Groupe premier_groupe_contenant(Obstacle o) {
        for (Obstacle ob : elements()) {
            if (ob.comprend(o)) {
                return ( (ob instanceof Groupe) ? (Groupe) ob : null ) ;
            }
        }

        return null ;
    }

    @Override
    public boolean estReel() {return false;}

    public boolean elementsSolidaires() { return elements_solidaires.getValue() ; }
    public BooleanProperty elementsSolidairesProperty() { return elements_solidaires ; }

    @Override
    public boolean peutContenirObstaclesFils() { return true ;}
    @Override
    public boolean contientObstaclesFils() { return elements().size()>0 ;}
    @Override
    public List<Obstacle> obstaclesFils() { return elements() ; }

    public Iterable<Obstacle> iterableParcoursEnLargeur() {
        return () -> new IterateurGroupePrefixe(this, false);
    }
    public Iterator<Obstacle> iterateurParcoursEnLargeur() {
        return new IterateurGroupePrefixe(this, false);
    }

    public Iterable<Obstacle> iterableObstaclesDepuisArrierePlan() {
        // Le parcours prefixe en profondeur correspond à un parcours de l'arrière-plan vers le premier plan de l'Environnement
        return this::iterateurObstaclesDepuisArrierePlan;
    }

    public Iterator<Obstacle> iterateurObstaclesDepuisArrierePlan() {
        // Le parcours prefixe en profondeur correspond à un parcours de l'arrière-plan vers le premier plan de l'Environnement
        return new IterateurGroupePrefixe(this, true);
    }

    public Iterable<Obstacle> iterableObstaclesDepuisPremierPlan() {
        // Le parcours postfixe en profondeur correspond à un parcours de l'arrière-plan vers le premier plan de l'Environnement
        return this::iterateurObstaclesDepuisPremierPlan;
    }

    public Iterator<Obstacle> iterateurObstaclesDepuisPremierPlan() {
        // Le parcours postfixe en profondeur correspond à un parcours de l'avant-plan vers l'arrière-plan de l'Environnement
        return new IterateurGroupePostfixe(this, true);
    }

    Iterator<Obstacle> iterateurObstaclesReelsDepuisArrierePlan() {
        // Le parcours prefixe en profondeur correspond à un parcours de l'arrière-plan vers l'avant-plan de l'Environnement
        return new IterateurGroupePrefixeObstaclesReels(this,true) ;
    }
    public Iterable<Obstacle> iterableObstaclesReelsDepuisArrierePlan() {
        return this::iterateurObstaclesReelsDepuisArrierePlan;
    }
    Iterator<Obstacle> iterateurObstaclesReelsDepuisPremierPlan() {
        // Le parcours postfixe en profondeur correspond à un parcours de l'avant-plan vers l'arrière-plan de l'Environnement
        return new IterateurGroupePostfixeObstaclesReels(this,true) ;
    }
    public Iterable<Obstacle> iterableObstaclesReelsDepuisPremierPlan() {
        return this::iterateurObstaclesReelsDepuisPremierPlan;
    }
    Iterator<Obstacle> iterateurObstaclesReelsEnLargeurDepuisArrierePlan() {
        // Le parcours prefixe en largeur correspond à un parcours de chaque niveau, de l'arrière-plan vers l'avant-plan de l'Environnement
        return new IterateurGroupePrefixeObstaclesReels(this,false) ;
    }
    public Iterable<Obstacle> iterableObstaclesReelsEnLargeurDepuisArrierePlan() {
        return this::iterateurObstaclesReelsEnLargeurDepuisArrierePlan;
    }
    Iterator<Obstacle> iterateurObstaclesReelsEnLargeurDepuisPremierPlan() {
        // Le parcours postfixe correspond à un parcours de chaque niveau, de l'avant-plan vers l'arrière-plan de l'Environnement
        return new IterateurGroupePostfixeObstaclesReels(this,false) ;
    }
    public Iterable<Obstacle> iterableObstaclesReelsEnLargeurDepuisPremierPlan() {
        return this::iterateurObstaclesReelsEnLargeurDepuisPremierPlan;
    }

    public void definirElementsSolidaires(boolean solidaires) {
        elements_solidaires.set(solidaires);
    }

//    public void ajouterListChangeListener(ListChangeListener<Obstacle> lcl_o) {
//
//        super.ajouterListChangeListener(lcl_o);
////        elementsObservables().addListener(lcl_o);
//
////        ajouterListChangeListenerDesGroupes(lcl_o);
//    }

//    private void ajouterListChangeListenerDesGroupes(ListChangeListener<Obstacle> lcl_o) {
//
//        this.elementsObservables().addListener(lcl_o);
//
//        for (Obstacle o : elements()) {
//            if (o instanceof Groupe grp) // Détection des changements qui interviennent dans les groupes
//                grp.ajouterListChangeListenerDesGroupes(lcl_o);
//        }
//
//    }


//    /**
//     * Enleve le ListChangeListener passé en paramètre des groupes
//     * @param lcl_o : le ListChangeListener à enlever
//     */
//    private void enleverListChangeListenerDesGroupes(ListChangeListener<Obstacle> lcl_o) {
//
//        for (Obstacle o : elements()) {
//            if (o instanceof Groupe grp) // Détection des changements qui interviennent dans les groupes
//                grp.enleverListChangeListenerDesGroupes(lcl_o);
//        }
//
//        elementsObservables().removeListener(lcl_o);
////        super.enleverListChangeListener(lcl_o);
//    }

//    private void construireListesObstacles() {
//        liste_obstacles_reels = new ArrayList<>(2*nombreObstaclesPremierNiveau()) ;
//        liste_obstacles       = new ArrayList<>(2*nombreObstaclesPremierNiveau()) ;
//
//        Iterable<Obstacle> it_obs = iterableObstaclesDepuisArrierePlan() ;
//        for (Obstacle o : it_obs) {
//            liste_obstacles.add(o) ;
//            if (o.estReel())
//                liste_obstacles_reels.add(o) ;
//        }
//
//    }


    @Override
    public Commande commandeCreation(Environnement env) {
        return new CommandeCreerGroupe(env,this) ;
    }
    @Override
    public void retaillerPourSourisEn(Point2D pos_souris) {}

    @Override
    public void accepte(VisiteurEnvironnement v) {
        v.visiteGroupe(this);
    }

    /**
     * Ajoute un obstacle au premier niveau du Groupe, à la suite des autres (donc au premier plan).
     * Les utilisateurs de cette méthode doivent veiller, si nécessaire, à retirer l'obstacle de l'environnement avant
     * d'appeler cette méthode.
     * Il est également de leur responsabilité de le replacer en bonne place dans le SOC auquel il appartenait si cet
     * ajout fait suite à un retrait dans le cadre d'un déplacement.
     *
     * @param o_a_ajouter : obstacle à ajouter
     */
    public void ajouterObstacle(Obstacle o_a_ajouter) {

        if (this.elements().contains(o_a_ajouter))
            return;

        // On définit l'appartenance à la composition avant de faire l'ajout, car les listeners du composite parent vont
        // se charger d'intégrer l'obstacle dans la vue (PanneauPrincipal) et de lui créer un panneau, qui n'est pas le
        // même selon que l'obstacle appartient à une composition ou non.
        o_a_ajouter.definirParent(this);

        super.ajouterObstacle(o_a_ajouter);

        o_a_ajouter.definirSOCParent(this.SOCParent());

    }

    public void ajouterObstacleEnPosition(Obstacle o, int i_pos) {
        if (this.elements().contains(o))
            return;

        // On définit l'appartenance à la composition avant de faire l'ajout, car les listeners du composite parent vont
        // se charger d'intégrer l'obstacle dans la vue (PanneauPrincipal) et de lui créer un panneau, qui n'est pas le
        // même selon que l'obstacle appartient à une composition ou non.
        o.definirParent(this);

        super.ajouterObstacleEnPosition(o,i_pos);

    }

    /**
     * Retire un obstacle du Groupe.
     * Il est de la responsabilité des appealants de s'assurer que l'obstacle est également retiré d'un éventuel SOC
     * d'appartenance, dont la classe Groupe n'a pas connaissance (ou qu'il est déplacé correctement dans le SOC, si le
     * retrait est suivi d'un rajout dans le cadre d'un déplacement).
     * @param o l'obstacle à retirer
     */
    public void retirerObstacle(Obstacle o) {
        super.retirerObstacle(o);

        // TODO : ajouter un listener sur la liste des obstacles ?

        // On fait comme pour les SOCs : laisser la référence au Parent dans les obstacles
        //  retirés qui ne sont plus actifs, pour faciliter l'annulation de leur retrait dans les Commandes (plus besoin
        //  de mémoriser le Parent avant retrait dans la Commande)
//        o.definirParent(null);  // Supprimer cette ligne ?

        // Un obstacle retiré prut garder la réf de son SOC Parent
//        if (o.SOCParent()!=null)
//            o.definirSOCParent(null);

//        if (o instanceof Groupe grp) {
//            grp.enleverListChangeListenerDesGroupes(lcl_reconstruction_listes_obstacles);
//        }

    }

    public Composition composition_contenant(Obstacle o) {
        throw new IllegalCallerException("Cette méthode ne devrait pas être appelée") ;
    }

    @Override
    public boolean contient(Point2D p) {
        throw new IllegalCallerException("Cette méthode ne devrait pas être appelée") ;
    }

    @Override
    public boolean aSurSaSurface(Point2D p) {
        throw new IllegalCallerException("Cette méthode ne devrait pas être appelée") ;
    }

    protected Obstacle estSurSurfaceDe(Point2D p) {
        throw new IllegalCallerException("Cette méthode ne devrait pas être appelée") ;
    }

    @Override
    public Point2D normale(Point2D p) throws Exception {
        throw new IllegalCallerException("Cette méthode ne devrait pas être appelée") ;
    }

    @Override
    public ArrayList<Point2D> cherche_toutes_intersections(Rayon r) {
        // Un groupe d'obstacle n'est qu'un conteneur logique sans existence physique, il n'a pas d'intersections avec
        // les rayons, ce sont les obstacles qu'il contient qui en ont.
        throw new IllegalCallerException("Cette méthode ne devrait pas être appelée") ;
    }

    @Override
    public Point2D cherche_intersection(Rayon r, ModeRecherche mode) {
        // Un groupe d'obstacle n'est qu'un conteneur logique sans existence physique, il n'a pas d'intersections avec
        // les rayons, ce sont les obstacles qu'il contient qui en ont.
        throw new IllegalCallerException("Cette méthode ne devrait pas être appelée") ;
    }

    @Override
    public void definirOrientationAxePolariseur(double angle_pol) {
        // TODO : pour un Groupe, je pense qu'il ne faut pas que cette méthode soit utilisée (contrairement aux Compositions)
        throw new IllegalCallerException("Cette méthode ne devrait pas être appelée") ;
    }

    @Override
    public double orientationAxePolariseur() {
        // TODO : pour un Groupe, je pense qu'il ne faut pas que cette méthode soit utilisée (contrairement aux Compositions)
        throw new IllegalCallerException("Cette méthode ne devrait pas être appelée") ;
    }

    @Override
    public DoubleProperty orientationAxePolariseurProperty() {
        // TODO : pour un Groupe, je pense qu'il ne faut pas que cette méthode soit utilisée (contrairement aux Compositions)
        throw new IllegalCallerException("Cette méthode ne devrait pas être appelée") ;
    }

    @Override
    public Double courbureRencontreeAuSommet(Point2D pt_sur_surface, Point2D direction) throws Exception {
        // TODO : pour un Groupe, je pense qu'il ne faut pas que cette méthode soit utilisée (contrairement aux Compositions)
        throw new IllegalCallerException("Cette méthode ne devrait pas être appelée") ;
    }

    @Override
    public double rayonDiaphragmeMaximumConseille() {
        // TODO : pour un Groupe, je pense qu'il ne faut pas que cette méthode soit utilisée (contrairement aux Compositions)
        throw new IllegalCallerException("Cette méthode ne devrait pas être appelée") ;
    }

    @Override
    public List<DioptreParaxial> dioptresParaxiaux(PositionEtOrientation axe) {
//        // TODO : pour un Groupe, je pense qu'il ne faut pas que cette méthode soit utilisée (contrairement aux Compositions)
//        throw new IllegalCallerException("Cette méthode ne devrait pas être appelée") ;

        int nb_obs = nombreObstaclesPremierNiveau() ;
        ArrayList<DioptreParaxial> dioptres_groupe = new ArrayList<>(2*nb_obs) ;

        for (int i = 0 ; i<nb_obs ; i++) {
            Obstacle o = obstacle(i) ;

            List<DioptreParaxial> dioptres_o = o.dioptresParaxiaux(axe);

            if (dioptres_o.isEmpty()) // Pour écarter les Cercles (ou les ellipses...) de rayon (ou de paramètre) nul
                continue;

            dioptres_groupe.addAll(dioptres_o) ;
        }

        // Tri par Z croissant et Rc "croissant"
        dioptres_groupe.sort(DioptreParaxial.comparateur) ;

        // A priori cette fusion n'est pas nécessaire tant qu'on ne cherche que la position du premier des dioptres paraxiaux
        return DioptreParaxial.fusionneDioptres(dioptres_groupe) ;
    }

     private List<DioptreParaxial> fusionneDioptres(List<DioptreParaxial> liste_dioptres) {
         // TODO : pour un Groupe, je pense qu'il ne faut pas que cette méthode soit utilisée (contrairement aux Compositions)
         throw new IllegalCallerException("Cette méthode ne devrait pas être appelée") ;

    }

    public boolean obstaclesReelsComprennent(Obstacle o_cherche) {
//        return liste_obstacles_reels.contains(o_cherche);

        if (!o_cherche.estReel())
            return false ;

        for (Obstacle o : iterableObstaclesReelsDepuisArrierePlan())
            if (o==o_cherche)
                return true ;

        return false;
    }

    public int indexParmiObstacles(Obstacle o_cherche) {
//        return liste_obstacles.indexOf(o_cherche) ;

        int resultat = 0 ;

        for (Obstacle o : iterableObstaclesDepuisArrierePlan()) {
            if (o == o_cherche)
                return resultat;
            ++resultat ;
        }

        return -1 ;
    }

    public boolean obstaclesComprennent(Obstacle o_cherche) {
//        return  liste_obstacles.contains(o_cherche) ;

        for (Obstacle o : iterableObstaclesDepuisArrierePlan())
            if (o==o_cherche)
                return true ;

        return false;
    }

    public int nombreObstacles() {
        int resultat = 0 ;
        for (Obstacle o : iterableObstaclesDepuisArrierePlan())
            ++resultat ;
        return resultat ;
    }

    public int nombreObstaclesReels() {
        int resultat = 0 ;
        for (Obstacle o : iterableObstaclesReelsDepuisArrierePlan())
            ++resultat ;
        return resultat ;
    }

    @Override
    public Point2D pointDeReferencePourPositionnementDansSOCParent() {
        List<DioptreParaxial> dioptres = dioptresParaxiaux(SOCParent().axe()) ;
        if (dioptres.size()>0)
            return SOCParent().origine().add(SOCParent().direction().multiply(dioptres.get(0).ZGeometrique()))  ;

        return Point2D.ZERO ;
    }

    @Override
    public void definirPointDeReferencePourPositionnementDansSOCParent(Point2D pt_ref) {
        translater(pt_ref.subtract(pointDeReferencePourPositionnementDansSOCParent()));
    }


}