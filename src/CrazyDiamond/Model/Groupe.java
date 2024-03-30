package CrazyDiamond.Model;

import javafx.beans.property.*;
import javafx.collections.ListChangeListener;
import javafx.geometry.Point2D;

import java.util.*;
import java.util.logging.Level;

public class Groupe extends BaseObstacleComposite implements Obstacle, Identifiable, Nommable {

    private final BooleanProperty elements_solidaires ;

    private static int compteur_groupe = 0;

    private final ListChangeListener<Obstacle> lcl_reconstruction_listes_obstacles = change -> {
        while (change.next()) {

            if (change.wasRemoved()) {
                LOGGER.log(Level.FINER, "Obstacle supprimé du groupe");
                construireListesObstacles();
            } else if (change.wasAdded()) {
                construireListesObstacles();
//                for (Obstacle additem : change.getAddedSubList()) {
//                    LOGGER.log(Level.FINER, "Obstacle ajouté dans le groupe : {0}", additem);
//                }
            }

        }
    };
    private ArrayList<Obstacle> liste_obstacles_reels;
    private ArrayList<Obstacle> liste_obstacles;


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

        this.elements_solidaires = new SimpleBooleanProperty(solidaire) ;

//        this.liste_obstacles = new ArrayList<>(1) ;
//        this.liste_obstacles.add(this) ;
//        this.liste_obstacles_reels = new ArrayList<>(0) ;

        construireListesObstacles();

        this.ajouterListChangeListenerDesGroupes(lcl_reconstruction_listes_obstacles);
    }

    public int indexObstacleALaRacine(Obstacle o) {
        return elements().indexOf(o) ;
    }

    public Composition compositionContenant(Obstacle o) {
        for (Obstacle ob  : liste_obstacles_reels) {
            if (ob instanceof Composition && ob.comprend(o))
                return ob.composition_contenant(o);
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

    public Groupe plus_grand_groupe_solidaire_contenant(Obstacle obs_reel) {

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
        return () -> new GroupeIterator(this, true);
    }
    public Iterator<Obstacle> iterateurParcoursEnLargeur() {
        return new GroupeIterator(this, true);
    }

    public Iterable<Obstacle> iterableObstaclesDepuisArrierePlan() {
        // Le parcours des groupes et obstacles en profondeur correspond à un parcours de l'arrière-plan vers le premier plan de l'Environnement
        return () -> new GroupeIterator(this, false);
    }

    public Iterator<Obstacle> iterateurObstaclesDepuisArrierePlan() {
        // Le parcours des groupes et obstacles en profondeur correspond à un parcours de l'arrière-plan vers le premier plan de l'Environnement
        return new GroupeIterator(this, false);
    }

    public void definirElementsSolidaires(boolean solidaires) {
        elements_solidaires.set(solidaires);
    }

    private void ajouterListChangeListenerDesGroupes(ListChangeListener<Obstacle> lcl_o) {

        elements().addListener(lcl_o);

        for (Obstacle o : elements()) {
            if (o instanceof Groupe grp) // Détection des changements qui interviennent dans les groupes
                grp.ajouterListChangeListenerDesGroupes(lcl_o);
        }

    }

    /**
     * Enleve le ListChangeListener passé en paramètre des groupes
     * @param lcl_o : le ListChangeListener à enlever
     */
    private void enleverListChangeListenerDesGroupes(ListChangeListener<Obstacle> lcl_o) {
        for (Obstacle o : elements()) {
            if (o instanceof Groupe grp) // Détection des changements qui interviennent dans les groupes
                grp.enleverListChangeListenerDesGroupes(lcl_o);
        }

        elements().removeListener(lcl_o);
    }

    private void construireListesObstacles() {
        liste_obstacles_reels = new ArrayList<>(2*nombreObstaclesPremierNiveau()) ;
        liste_obstacles       = new ArrayList<>(2*nombreObstaclesPremierNiveau()) ;

        Iterable<Obstacle> it_obs = iterableObstaclesDepuisArrierePlan() ;
        for (Obstacle o : it_obs) {
            liste_obstacles.add(o) ;
            if (o.estReel())
                liste_obstacles_reels.add(o) ;
        }

    }

    /**
     * Iterateur sur les objets réels du groupe, depuis l'arrière-plan vers le premier plan.
     * @return l'itérateur
     */
    Iterator<Obstacle> iterateurObstaclesReelsEnProfondeur() {
        // DONE: Optimisation possible pour cette méthode et la suivante (iterateurInverseObstaclesReelsDepuisPremierPlan):
        // Faire de liste_obstacles_reels un attribut privé de la classe et le construire uniquement quand on ajoute ou
        // enlève un obstacle du groupe (ajouterObstacleALaRacine/retirerObstacle). NB : Il faut aussi ajouter, récursivement, un
        // ListChangeListener spécifique pour déclencher la reconstruction quand on ajoute ou enlève des obstacles d'un
        // sous-groupe, via ajouterListChangeListenerSurGroupesUniquement.
//        List<Obstacle> liste_obstacles_reels = construireListeObstaclesReels() ;
        return liste_obstacles_reels.iterator() ;
    }
    public Iterable<Obstacle> iterableObstaclesReelsDepuisArrierePlan() {
        // Le parcours des groupes et obstacles en profondeur correspond à un parcours de l'arrière-plan vers l'avant-plan de l'Environnement
        return this::iterateurObstaclesReelsEnProfondeur;
    }
    /**
     * Iterateur sur les objets réels du groupe, depuis le premier plan vers l'arrière-plan.
     * @return l'itérateur
     */
    ListIterator<Obstacle> iterateurInverseObstaclesReelsDepuisPremierPlan() {
        return liste_obstacles_reels.listIterator(liste_obstacles_reels.size()) ;
    }

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
     * NB : Les utilisateurs de cette méthode doivent veiller à retirer l'obstacle de l'environnement avant d'appeler
     * cette méthode.
     * @param o : obstacle à ajouter
     */
    public void ajouterObstacle(Obstacle o) {

        if (this.elements().contains(o))
            return;

        super.ajouterObstacle(o);

        if (o instanceof Groupe grp) {
            grp.ajouterListChangeListenerDesGroupes(lcl_reconstruction_listes_obstacles);
        }

        o.definirAppartenanceGroupe(true);
    }

    public void retirerObstacle(Obstacle o) {
        super.retirerObstacle(o);

        // TODO : ajouter un listener sur la liste des obstacles ?

        o.definirAppartenanceGroupe(false);

        if (o instanceof Groupe grp) {
            grp.enleverListChangeListenerDesGroupes(lcl_reconstruction_listes_obstacles);
        }

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
        // TODO : pour un Groupe, je pense qu'il ne faut pas que cette méthode soit utilisée (contrairement aux Compositions)
        throw new IllegalCallerException("Cette méthode ne devrait pas être appelée") ;
    }

     private List<DioptreParaxial> fusionneDioptres(List<DioptreParaxial> liste_dioptres) {
         // TODO : pour un Groupe, je pense qu'il ne faut pas que cette méthode soit utilisée (contrairement aux Compositions)
         throw new IllegalCallerException("Cette méthode ne devrait pas être appelée") ;

    }

    public boolean obstaclesReelsComprennent(Obstacle o) {
        return liste_obstacles_reels.contains(o) ;
    }
    public boolean obstaclesComprennent(Obstacle o) {
        return liste_obstacles.contains(o) ;
    }

}