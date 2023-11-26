package CrazyDiamond.Model;


import javafx.beans.property.*;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.util.*;

public class CompositionDeuxObstacles implements Obstacle, Identifiable,Nommable, ElementAvecContour, ElementAvecMatiere {

    private final Imp_Identifiable imp_identifiable ;
    private final Imp_Nommable imp_nommable;
    private final Imp_ElementAvecContour imp_elementAvecContour;
    private final Imp_ElementAvecMatiere imp_elementAvecMatiere;

    enum Operateur { UNION, INTERSECTION , DIFFERENCE, DIFFERENCE_SYMETRIQUE } ;

    // Vu qu'on déclare les obstacles 1 et 2 comme final, on n'a sans doute pas besoin d'en faire des ObjectProperty
    // ce sont les propriétés des obstacles sous-jacents qui sont observables
    private final ObjectProperty<Obstacle> obstacle1 ;
    private final ObjectProperty<Obstacle> obstacle2 ;

    private final ObjectProperty<Operateur> operateur ;

    private static int compteur_composition_deux_obstacles = 0;

    private BooleanProperty appartenance_composition ;

    public CompositionDeuxObstacles(Obstacle ob1, Operateur op, Obstacle ob2) throws IllegalArgumentException {
        this(
                new Imp_Identifiable(),
                new Imp_Nommable("Composition Deux Obstacles " + (++compteur_composition_deux_obstacles)),
                new Imp_ElementAvecContour(null),
                new Imp_ElementAvecMatiere(null, null,1.0,null),
                ob1,op,ob2
        ) ;

    }
    public CompositionDeuxObstacles(Imp_Identifiable ii,Imp_Nommable in,Imp_ElementAvecContour iec, Imp_ElementAvecMatiere iem ,Obstacle ob1, Operateur op, Obstacle ob2) throws IllegalArgumentException {

        imp_identifiable = ii ;
        imp_nommable = in ;
        imp_elementAvecContour = iec ;
        imp_elementAvecMatiere = iem ;

        obstacle1 = new SimpleObjectProperty<Obstacle>(ob1) ;
        obstacle2 = new SimpleObjectProperty<Obstacle>(ob2) ;

        operateur = new SimpleObjectProperty<Operateur>(op)  ;

        appartenance_composition = new SimpleBooleanProperty(false) ;

    }
    @Override public String id() { return imp_identifiable.id(); }

    @Override public String nom() {  return imp_nommable.nom(); }
    @Override public StringProperty nomProperty() { return imp_nommable.nomProperty(); }

    @Override public Color couleurContour() { return imp_elementAvecContour.couleurContour();}
    @Override public ObjectProperty<Color> couleurContourProperty() { return imp_elementAvecContour.couleurContourProperty(); }

    @Override public Color couleurMatiere() { return imp_elementAvecMatiere.couleurMatiere(); }
    @Override public ObjectProperty<Color> couleurMatiereProperty() { return imp_elementAvecMatiere.couleurMatiereProperty(); }

    @Override public void definirTraitementSurface(TraitementSurface traitement_surf) { imp_elementAvecContour.definirTraitementSurface(traitement_surf);}
    @Override public TraitementSurface traitementSurface() {return imp_elementAvecContour.traitementSurface() ;}
    @Override public ObjectProperty<TraitementSurface> traitementSurfaceProperty() {return imp_elementAvecContour.traitementSurfaceProperty() ;}

    @Override public DoubleProperty tauxReflexionSurfaceProperty() {return imp_elementAvecContour.tauxReflexionSurfaceProperty() ; }
    @Override public void definirTauxReflexionSurface(double taux_refl) {imp_elementAvecContour.definirTauxReflexionSurface(taux_refl);}
    @Override public double tauxReflexionSurface() {return imp_elementAvecContour.tauxReflexionSurface();}

    @Override public void definirOrientationAxePolariseur(double angle_pol) {imp_elementAvecContour.definirOrientationAxePolariseur(angle_pol);}
    @Override public double orientationAxePolariseur() {return imp_elementAvecContour.orientationAxePolariseur() ;}
    @Override public DoubleProperty orientationAxePolariseurProperty() {return imp_elementAvecContour.orientationAxePolariseurProperty() ;}

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
    @Override public void definirTypeSurface(TypeSurface type_surf) { imp_elementAvecMatiere.definirTypeSurface(type_surf); }
    @Override public TypeSurface typeSurface() { return imp_elementAvecMatiere.typeSurface(); }
    @Override public ObjectProperty<TypeSurface> typeSurfaceProperty() { return imp_elementAvecMatiere.typeSurfaceProperty(); }

    @Override public void definirNatureMilieu(NatureMilieu nature_mil) { imp_elementAvecMatiere.definirNatureMilieu(nature_mil); }
    @Override public NatureMilieu natureMilieu() { return imp_elementAvecMatiere.natureMilieu(); }
    @Override public ObjectProperty<NatureMilieu> natureMilieuProperty() { return imp_elementAvecMatiere.natureMilieuProperty(); }

    @Override public void definirIndiceRefraction(double indice_refraction) { imp_elementAvecMatiere.definirIndiceRefraction(indice_refraction);   }
    @Override public double indiceRefraction() { return imp_elementAvecMatiere.indiceRefraction(); }
    @Override public DoubleProperty indiceRefractionProperty() {  return imp_elementAvecMatiere.indiceRefractionProperty(); }

    @Override public String toString() { return nom(); }

    public void appliquerSurIdentifiable(ConsumerAvecException<Object, IOException> consumer) throws IOException {
        consumer.accept(imp_identifiable);
    }
    public void appliquerSurNommable(ConsumerAvecException<Object,IOException> consumer) throws IOException {
        consumer.accept(imp_nommable);
    }
    public void appliquerSurElementAvecContour(ConsumerAvecException<Object,IOException> consumer) throws IOException {
        consumer.accept(imp_elementAvecContour);
    }
    public void appliquerSurElementAvecMatiere(ConsumerAvecException<Object,IOException> consumer) throws IOException {
        consumer.accept(imp_elementAvecMatiere);
    }

    @Override
    public void retaillerPourSourisEn(Point2D pos_souris) {

    }

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
            case 0 :  // UNION
               if (est_sur_ob1 && !est_dans_ob2) return ob1.normale(p) ;
               if (est_sur_ob2 && !est_dans_ob1) return ob2.normale(p) ;
            break ;
            case 1 : // INTERSECTION
                if (est_sur_ob1 && est_dans_ob2) return ob1.normale(p) ;
                if (est_sur_ob2 && est_dans_ob1) return ob2.normale(p) ;
            break ;
            case 2 : // DIFFERENCE
                if (est_sur_ob1 && !est_dans_ob2) return ob1.normale(p) ;
                if (est_sur_ob2 && est_dans_ob1) return ob2.normale(p).multiply(-1.0) ;
            break;
            case 3 : // DIFFERENCE_SYMETRIQUE
                if (est_sur_ob1 && !est_dans_ob2) return ob1.normale(p) ;
                if (est_sur_ob2 && !est_dans_ob1) return ob2.normale(p) ;
                if (est_sur_ob1 && est_dans_ob2) return ob1.normale(p).multiply(-1.0) ;
                if (est_sur_ob2 && est_dans_ob1) return ob2.normale(p).multiply(-1.0) ;
            break;
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
    public void ajouterRappelSurChangementToutePropriete(RappelSurChangement rap) {

        imp_elementAvecContour.ajouterRappelSurChangementToutePropriete(rap);
        imp_elementAvecMatiere.ajouterRappelSurChangementToutePropriete(rap);


        operateur.addListener((observable, oldValue, newValue) -> {rap.rappel(); });

        obstacle1.get().ajouterRappelSurChangementToutePropriete(rap);
        obstacle2.get().ajouterRappelSurChangementToutePropriete(rap);

//        obstacle1.addListener((observable, oldValue, newValue) -> {rap.rappel(); });
//        obstacle2.addListener((observable, oldValue, newValue) -> {rap.rappel(); });
    }

    @Override
    public void ajouterRappelSurChangementTouteProprieteModifiantChemin(RappelSurChangement rap) {

        imp_elementAvecContour.ajouterRappelSurChangementTouteProprieteModifiantChemin(rap);
        imp_elementAvecMatiere.ajouterRappelSurChangementTouteProprieteModifiantChemin(rap);

        operateur.addListener((observable, oldValue, newValue) -> {rap.rappel(); });

        obstacle1.get().ajouterRappelSurChangementTouteProprieteModifiantChemin(rap);
        obstacle2.get().ajouterRappelSurChangementTouteProprieteModifiantChemin(rap);

//        obstacle1.addListener((observable, oldValue, newValue) -> {rap.rappel(); });
//        obstacle2.addListener((observable, oldValue, newValue) -> {rap.rappel(); });

    }

    @Override
    public void tournerAutourDe(Point2D centre_rot, double angle_rot_deg) {

    }

    @Override
    public void definirAppartenanceComposition(boolean b) {this.appartenance_composition.set(b);}
    @Override
    public boolean appartientAComposition() {return this.appartenance_composition.get() ;}

    @Override
    public void convertirDistances(double facteur_conversion) {
        obstacle1.get().convertirDistances(facteur_conversion);
        obstacle2.get().convertirDistances(facteur_conversion);
    }


}