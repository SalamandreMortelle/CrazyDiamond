package CrazyDiamond.Model;

import javafx.beans.property.*;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;

import java.util.*;

public class CompositionDeuxObstacles extends BaseObstacleAvecContourEtMatiere implements Obstacle, Identifiable,Nommable, ElementAvecContour, ElementAvecMatiere {

    enum Operateur { UNION, INTERSECTION , DIFFERENCE, DIFFERENCE_SYMETRIQUE }

    // Vu qu'on déclare les obstacles 1 et 2 comme final, on n'a sans doute pas besoin d'en faire des ObjectProperty
    // ce sont les propriétés des obstacles sous-jacents qui sont observables
    private final ObjectProperty<Obstacle> obstacle1 ;
    private final ObjectProperty<Obstacle> obstacle2 ;

    private final ObjectProperty<Operateur> operateur ;

    private static int compteur_composition_deux_obstacles = 0;

    public CompositionDeuxObstacles(Obstacle ob1, Operateur op, Obstacle ob2) throws IllegalArgumentException {
        this(null,ob1,op,ob2,null,null,1.0,null,null) ;
    }

    public CompositionDeuxObstacles(String nom, Obstacle ob1, Operateur op, Obstacle ob2, TypeSurface type_surface, NatureMilieu nature_milieu, double indice_refraction, Color couleur_matiere, Color couleur_contour) throws IllegalArgumentException {
        super(nom != null ? nom :"Composition Deux Obstacles " + (++compteur_composition_deux_obstacles),
                type_surface, nature_milieu, indice_refraction, couleur_matiere, couleur_contour);

        obstacle1 = new SimpleObjectProperty<>(ob1) ;
        obstacle2 = new SimpleObjectProperty<>(ob2) ;

        operateur = new SimpleObjectProperty<>(op)  ;

        ajouterListeners();
    }

    public CompositionDeuxObstacles(Imp_Identifiable ii,Imp_Nommable in,Imp_ElementAvecContour iec, Imp_ElementAvecMatiere iem ,Obstacle ob1, Operateur op, Obstacle ob2) throws IllegalArgumentException {
        super (ii,in,iec,iem) ;

        obstacle1 = new SimpleObjectProperty<>(ob1) ;
        obstacle2 = new SimpleObjectProperty<>(ob2) ;

        operateur = new SimpleObjectProperty<>(op)  ;

        ajouterListeners();
    }

    private void ajouterListeners() {
        operateur.addListener((observable, oldValue, newValue) -> declencherRappelsSurChangementToutePropriete());
    }

    @Override
    public Double courbureRencontreeAuSommet(Point2D pt_sur_surface, Point2D direction) throws Exception {
        // Identification de l'obstacle sur la surface duquel se trouve le point
        Obstacle obst = null ;

        if (obstacle1.get().aSurSaSurface(pt_sur_surface))
            obst = obstacle1.get() ;
        else if (obstacle2.get().aSurSaSurface(pt_sur_surface))
            obst = obstacle2.get() ;

        return (direction.dotProduct(normale(pt_sur_surface))<=0d?
                obst.courbureRencontreeAuSommet(pt_sur_surface,direction):-obst.courbureRencontreeAuSommet(pt_sur_surface,direction)) ;
    }

    @Override
    public void retaillerPourSourisEn(Point2D pos_souris) {}

    @Override
    public void accepte(VisiteurEnvironnement v) {
        v.visiteCompositionDeuxObstacles(this);
    }

    @Override
    public boolean contient(Point2D p) {

        Obstacle ob1 = obstacle1.get() ;
        Obstacle ob2 = obstacle2.get() ;

        switch (operateur.get().ordinal()) {
            case 0 :  // UNION
                return (ob1.contient(p) || ob2.contient(p)) ;
            case 1 : // INTERSECTION
                return (ob1.contient(p) && ob2.contient(p)) ;
            case 2 : // DIFFERENCE
                return (ob1.contient(p) && (!ob2.contient(p))) ;
            case 3 : // DIFFERENCE_SYMETRIQUE
                return (ob1.contient(p) ^ ob2.contient(p)) ;
        }

        throw new IllegalStateException("Composition::contient : operateur inconnu");
    }


    @Override
    public boolean aSurSaSurface(Point2D p) {
        Obstacle ob1 = obstacle1.get() ;
        Obstacle ob2 = obstacle2.get() ;

        switch (operateur.get().ordinal()) {
            case 0 :  // UNION
                return ( (ob1.aSurSaSurface(p) && (!ob2.contient(p)) ) || (ob2.aSurSaSurface(p) && (!ob1.contient(p)) ) ) ;
            case 1 : // INTERSECTION
                return ( (ob1.aSurSaSurface(p) && ob2.contient(p) ) || (ob2.aSurSaSurface(p) && ob1.contient(p) ) ) ;
            case 2 : // DIFFERENCE
                return ( ob1.aSurSaSurface(p) && (!ob2.contient(p)) || (ob2.aSurSaSurface(p) && ob1.contient(p) ) );
            case 3 : // DIFFERENCE_SYMETRIQUE
                return ( (ob1.aSurSaSurface(p) && (!ob2.contient(p)) ) || (ob2.aSurSaSurface(p) && (!ob1.contient(p)) )
                      || ( ob1.aSurSaSurface(p) && ob2.contient(p) ) || ( ob2.aSurSaSurface(p) && ob1.contient(p) ) ) ;
        }

        throw new IllegalStateException("Composition::aSurSaSurface : operateur inconnu");
    }

    @Override
    public Point2D normale(Point2D p) throws Exception {
        Obstacle ob1 = obstacle1.get() ;
        Obstacle ob2 = obstacle2.get() ;

        boolean est_sur_ob1 = ob1.aSurSaSurface(p) ;
        boolean est_sur_ob2 = ob2.aSurSaSurface(p) ;

        boolean est_dans_ob1 = ob1.contient(p) ;
        boolean est_dans_ob2 = ob2.contient(p) ;

        switch (operateur.get().ordinal()) {
            case 0 -> {  // UNION
                if (est_sur_ob1 && !est_dans_ob2) return ob1.normale(p);
                if (est_sur_ob2 && !est_dans_ob1) return ob2.normale(p);
            }
            case 1 -> { // INTERSECTION
                if (est_sur_ob1 && est_dans_ob2) return ob1.normale(p);
                if (est_sur_ob2 && est_dans_ob1) return ob2.normale(p);
            }
            case 2 -> { // DIFFERENCE
                if (est_sur_ob1 && !est_dans_ob2) return ob1.normale(p);
                if (est_sur_ob2 && est_dans_ob1) return ob2.normale(p).multiply(-1.0);
            }
            case 3 -> { // DIFFERENCE_SYMETRIQUE
                if (est_sur_ob1 && !est_dans_ob2) return ob1.normale(p);
                if (est_sur_ob2 && !est_dans_ob1) return ob2.normale(p);
                if (est_sur_ob1 && est_dans_ob2) return ob1.normale(p).multiply(-1.0);
                if (est_sur_ob2 && est_dans_ob1) return ob2.normale(p).multiply(-1.0);
            }
        }

        throw new Exception("Impossible de trouver la normale d'un point qui n'est pas sur la surface de la Composition.");

    }

    @Override
    public Point2D cherche_intersection(Rayon r, ModeRecherche mode) {

        Obstacle ob1 = obstacle1.get() ;
        Obstacle ob2 = obstacle2.get() ;

        Point2D pint1_p = ob1.premiere_intersection(r) ;
        Point2D pint2_p = ob2.premiere_intersection(r) ;

        Point2D pint1_d = ob1.derniere_intersection(r) ;
        Point2D pint2_d = ob2.derniere_intersection(r) ;


        // TODO : pour pouvoir gérer des rayons dont le départ est dans la Composition, il faudra parfois (selon l'opération)
        // chercher les dernières intersections avec chaque obstacle, plutôt que les premières (or la methode Obstacle::intersection
        // ne retourne que la première.

        Point2D p_plus_proche_p   ; // Point le plus proche parmi les 2 intersections premières
        Point2D p_moins_proche_p = null ; // Point le moins proche parmi les 2 intersections premières

        Point2D p_plus_proche_d   ; // Point le plus proche parmi les 2 intersections dernières
        Point2D p_moins_proche_d = null ; // Point le moins proche parmi les 2 intersections dernières

        if (pint1_p == null && pint2_p == null)
            return null ;

        if (pint1_p != null && pint2_p == null)
            p_plus_proche_p = pint1_p ;
        else if (pint1_p == null)
            p_plus_proche_p = pint2_p ;
        else {
            if (pint1_p.subtract(r.depart()).magnitude() <= pint2_p.subtract(r.depart()).magnitude()) {
                p_plus_proche_p  = pint1_p;
                p_moins_proche_p = pint2_p ;
            }
            else {
                p_plus_proche_p  = pint2_p;
                p_moins_proche_p = pint1_p ;
            }
        }

        if (pint1_d == null && pint2_d == null) {
            p_plus_proche_d  = null;
            p_moins_proche_d = null ;
        }

        if (pint1_d != null && pint2_d == null)
            p_plus_proche_d = pint1_d ;
        else if (pint1_d == null && pint2_d != null)
            p_plus_proche_d = pint2_d ;
        else {
            if (pint1_d.subtract(r.depart()).magnitude() <= pint2_d.subtract(r.depart()).magnitude()) {
                p_plus_proche_d  = pint1_d ;
                p_moins_proche_d = pint2_d ;
            }
            else {
                p_plus_proche_d  = pint2_d ;
                p_moins_proche_d = pint1_d ;
            }
        }

        ArrayList<Point2D> liste_ordonne = new ArrayList<>(Arrays.asList(p_plus_proche_p,p_moins_proche_p,p_plus_proche_d,p_moins_proche_d)) ;
        liste_ordonne.removeIf(Objects::isNull) ;
        ordonne_par_proximite(r.depart(),liste_ordonne) ;

//        boolean est_sur_ob1 = ob1.aSurSaSurface(p) ;
//        boolean est_sur_ob2 = ob2.aSurSaSurface(p) ;
//
//        boolean est_dans_ob1 = ob1.contient(p) ;
//        boolean est_dans_ob2 = ob2.contient(p) ;

        switch (operateur.get()) {
            case UNION -> {  // UNION
                // TODO : à revoir pour gérer un rayon dont le départ est à l'intérieur de la composition

                if (mode == ModeRecherche.PREMIERE ) {
                    if (p_plus_proche_p != null && (p_plus_proche_p == pint1_p) && !ob2.contient(p_plus_proche_p))
                        return p_plus_proche_p;
                    if (p_plus_proche_p != null && (p_plus_proche_p == pint2_p) && !ob1.contient(p_plus_proche_p))
                        return p_plus_proche_p;
//                    if (p_moins_proche_p != null && (p_moins_proche_p == pint1_p) && !ob2.contient(p_moins_proche_p))
//                        return p_moins_proche_p;
//                    if (p_moins_proche_p != null && (p_moins_proche_p == pint2_p) && !ob1.contient(p_moins_proche_p))
//                        return p_moins_proche_p;
                } else { // Recherche de la dernière intersection

                }
            }
            case INTERSECTION -> { // INTERSECTION
                if (p_plus_proche_p != null && (p_plus_proche_p == pint1_p) && ob2.contient(p_plus_proche_p))
                    return p_plus_proche_p;
                if (p_plus_proche_p != null && (p_plus_proche_p == pint2_p) && ob1.contient(p_plus_proche_p))
                    return p_plus_proche_p;
                if (p_moins_proche_p != null && (p_moins_proche_p == pint1_p) && ob2.contient(p_moins_proche_p))
                    return p_moins_proche_p;
                if (p_moins_proche_p != null && (p_moins_proche_p == pint2_p) && ob1.contient(p_moins_proche_p))
                    return p_moins_proche_p;
            }
            case DIFFERENCE -> { // DIFFERENCE

                for (Point2D p : liste_ordonne)
                    if (ob1.contient(p) && ( (!ob2.contient(p)) || ob2.aSurSaSurface(p)) )
                        return p ;
            }
            case DIFFERENCE_SYMETRIQUE -> { // DIFFERENCE_SYMETRIQUE
                if (p_plus_proche_p != null && (p_plus_proche_p == pint1_p) && (!ob2.contient(p_plus_proche_p) || ob1.aSurSaSurface(p_plus_proche_p) ))
                    return p_plus_proche_p;
                if (p_plus_proche_p != null && (p_plus_proche_p == pint2_p) && (!ob1.contient(p_plus_proche_p) || ob2.aSurSaSurface(p_plus_proche_p) ))
                    return p_plus_proche_p;
            }
        }

        return null ;

    }

    protected List<Point2D> ordonne_par_proximite(Point2D p , List<Point2D> points) {

        Comparator<Point2D> comparateur = (Point2D p1, Point2D p2) -> (int) (p1.subtract(p).magnitude()-p2.subtract(p).magnitude()) ;

        points.sort(comparateur);

        return points ;

    }

    @Override
    public void ajouterRappelSurChangementToutePropriete(Object cle,RappelSurChangement rap) {
        super.ajouterRappelSurChangementToutePropriete(cle,rap);

//        operateur.addListener((observable, oldValue, newValue) -> rap.rappel());

        obstacle1.get().ajouterRappelSurChangementToutePropriete(cle,rap);
        obstacle2.get().ajouterRappelSurChangementToutePropriete(cle,rap);

//        obstacle1.addListener((observable, oldValue, newValue) -> {rap.rappel(); });
//        obstacle2.addListener((observable, oldValue, newValue) -> {rap.rappel(); });
    }

    @Override
    public void ajouterRappelSurChangementTouteProprieteModifiantChemin(RappelSurChangement rap) {
        super.ajouterRappelSurChangementTouteProprieteModifiantChemin(rap);

        operateur.addListener((observable, oldValue, newValue) -> rap.rappel());

        obstacle1.get().ajouterRappelSurChangementTouteProprieteModifiantChemin(rap);
        obstacle2.get().ajouterRappelSurChangementTouteProprieteModifiantChemin(rap);

//        obstacle1.addListener((observable, oldValue, newValue) -> {rap.rappel(); });
//        obstacle2.addListener((observable, oldValue, newValue) -> {rap.rappel(); });

    }

    @Override
    public void tournerAutourDe(Point2D centre_rot, double angle_rot_deg) {

    }

    @Override
    public void convertirDistances(double facteur_conversion) {
        obstacle1.get().convertirDistances(facteur_conversion);
        obstacle2.get().convertirDistances(facteur_conversion);
    }

    @Override
    public void translater(Point2D vecteur) {
        obstacle1.get().translater(vecteur);
        obstacle2.get().translater(vecteur);
    }

    @Override
    public void translaterParCommande(Point2D vecteur) {
        ArrayList<Obstacle> obstacles = new ArrayList<>(2) ;

        if (obstacle1!=null) obstacles.add(obstacle1.get()) ;
        if (obstacle2!=null) obstacles.add(obstacle2.get()) ;

        new CommandeTranslaterObstacles(vecteur, obstacles).executer();
    }
}