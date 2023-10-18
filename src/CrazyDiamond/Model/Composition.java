package CrazyDiamond.Model;


import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;

public class Composition implements Obstacle, Identifiable, Nommable, ElementAvecContour, ElementAvecMatiere {

    private final Imp_Identifiable imp_identifiable ;
    private final Imp_Nommable imp_nommable;
    private final Imp_ElementAvecContour imp_elementAvecContour;
    private final Imp_ElementAvecMatiere imp_elementAvecMatiere;

    private final BooleanProperty appartenance_systeme_optique_centre;
    private final BooleanProperty appartenance_composition;

    public enum Operateur {
        UNION("UNION"),
        INTERSECTION("INTERSECTION"),
        DIFFERENCE("DIFFERENCE"),
        DIFFERENCE_SYMETRIQUE("DIFFERENCE_SYMETRIQUE");
        private final String value;

        Operateur(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        public static Operateur fromValue(String text) {
            for (Operateur op : Operateur.values()) {
                if (String.valueOf(op.value).equals(text)) {
                    return op;
                }
            }
            return null;
        }

    }

    private final ListProperty<Obstacle> elements;

    private final ObjectProperty<Operateur> operateur;

    private static int compteur_composition = 0;

    public Composition(Operateur op) throws IllegalArgumentException {

        this(
                new Imp_Identifiable(),
                new Imp_Nommable("Composition " + (++compteur_composition)) ,
                new Imp_ElementAvecContour(null),
                new Imp_ElementAvecMatiere(null, null, 1.0, null),
                op
        ) ;

    }

    public Composition(Imp_Identifiable ii,Imp_Nommable in,Imp_ElementAvecContour iec, Imp_ElementAvecMatiere iem , Operateur op) throws IllegalArgumentException {

        imp_identifiable = ii ;
        imp_nommable = in ;
        imp_elementAvecContour = iec;
        imp_elementAvecMatiere = iem;

        ObservableList<Obstacle> olo = FXCollections.observableArrayList();

        elements = new SimpleListProperty<Obstacle>(olo);

        operateur = new SimpleObjectProperty<Operateur>(op);

        appartenance_systeme_optique_centre = new SimpleBooleanProperty(false);
        appartenance_composition = new SimpleBooleanProperty(false);

    }
    @Override public String id() { return imp_identifiable.id(); }

    @Override
    public String nom() {
        return imp_nommable.nom();
    }

    @Override
    public StringProperty nomProperty() {
        return imp_nommable.nomProperty();
    }

    @Override
    public Color couleurContour() {
        return imp_elementAvecContour.couleurContour();
    }

    @Override
    public ObjectProperty<Color> couleurContourProperty() {
        return imp_elementAvecContour.couleurContourProperty();
    }

    @Override
    public ObjectProperty<TraitementSurface> traitementSurfaceProperty() {
        return imp_elementAvecContour.traitementSurfaceProperty();
    }

    @Override
    public void definirTraitementSurface(TraitementSurface traitement_surf) {
        imp_elementAvecContour.definirTraitementSurface(traitement_surf);
    }

    @Override
    public TraitementSurface traitementSurface() {
        return imp_elementAvecContour.traitementSurface();
    }

    @Override
    public DoubleProperty tauxReflexionSurfaceProperty() {
        return imp_elementAvecContour.tauxReflexionSurfaceProperty();
    }

    @Override
    public void definirTauxReflexionSurface(double taux_refl) {
        imp_elementAvecContour.definirTauxReflexionSurface(taux_refl);
    }

    @Override
    public double tauxReflexionSurface() {
        return imp_elementAvecContour.tauxReflexionSurface();
    }

    @Override
    public void definirOrientationAxePolariseur(double angle_pol) {
        imp_elementAvecContour.definirOrientationAxePolariseur(angle_pol);
    }

    @Override
    public double orientationAxePolariseur() {
        return imp_elementAvecContour.orientationAxePolariseur();
    }

    @Override
    public DoubleProperty orientationAxePolariseurProperty() {
        return imp_elementAvecContour.orientationAxePolariseurProperty();
    }

    @Override
    public Color couleurMatiere() {
        return imp_elementAvecMatiere.couleurMatiere();
    }

    @Override
    public ObjectProperty<Color> couleurMatiereProperty() {
        return imp_elementAvecMatiere.couleurMatiereProperty();
    }

    @Override
    public void definirTypeSurface(TypeSurface type_surf) {
        imp_elementAvecMatiere.definirTypeSurface(type_surf);
    }

    @Override
    public TypeSurface typeSurface() {
        return imp_elementAvecMatiere.typeSurface();
    }

    @Override
    public ObjectProperty<TypeSurface> typeSurfaceProperty() {
        return imp_elementAvecMatiere.typeSurfaceProperty();
    }

    @Override
    public void definirNatureMilieu(NatureMilieu nature_mil) {
        imp_elementAvecMatiere.definirNatureMilieu(nature_mil);
    }

    @Override
    public NatureMilieu natureMilieu() {
        return imp_elementAvecMatiere.natureMilieu();
    }

    @Override
    public ObjectProperty<NatureMilieu> natureMilieuProperty() {
        return imp_elementAvecMatiere.natureMilieuProperty();
    }

    @Override
    public void definirIndiceRefraction(double indice_refraction) {
        imp_elementAvecMatiere.definirIndiceRefraction(indice_refraction);
    }

    @Override
    public double indiceRefraction() {
        return imp_elementAvecMatiere.indiceRefraction();
    }

    @Override
    public DoubleProperty indiceRefractionProperty() {
        return imp_elementAvecMatiere.indiceRefractionProperty();
    }

    @Override
    public String toString() {
        return nom();
    }

    public ObservableList<Obstacle> elements() {
        return elements.get();
    }

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
    public void translater(Point2D vecteur) {

        for (Obstacle o : elements)
            o.translater(vecteur);

    }

    @Override
    public void accepte(VisiteurEnvironnement v) {
        v.visiteComposition(this);
    }

    @Override
    public void accepte(VisiteurElementAvecMatiere v) {
        v.visiteComposition(this);
    }

    public Operateur operateur() {
        return operateur.get();
    }

    public ObjectProperty<Operateur> operateurProperty() {
        return operateur;
    }

    public void definirOperateur(Operateur op) {
        operateur.setValue(op);
    }

    public void ajouterObstacle(Obstacle o) {

        if (this.elements.contains(o))
            return;

//        o.ajouterRappelSurChangementTouteProprieteModifiantChemin( this::illuminerToutesSources); ;

        this.elements.add(o);

        o.definirAppartenanceComposition(true);

    }

    public void retirerObstacle(Obstacle o) {
        elements.remove(o);

        // TODO : ajouter un listener sur la liste des obstacles ?

        o.definirAppartenanceComposition(false);

    }

    public void ajouterListenerListeObstacles(ListChangeListener<Obstacle> lcl_o) {
        elements.addListener(lcl_o);

        //Il faut aussi détecter les changements qui interviennent dans les sous-compositions
        for (Obstacle o : elements) {
            if (o.getClass() == Composition.class) {
                Composition comp = (Composition) o ;
                comp.ajouterListenerListeObstacles(lcl_o) ;
            }

        }
    }
    @Override
    public boolean comprend(Obstacle o) {

        for (Obstacle ob : elements) {
            if (ob.comprend(o))
                return true ;
        }

        return Obstacle.super.comprend(o);
    }

    public Composition composition_contenant(Obstacle o) {
        for (Obstacle ob : elements) {
            if (ob.comprend(o)) {
                Composition c_cont = ob.composition_contenant(o);
                return (c_cont!=null?c_cont:this) ;
            }
        }

        return null ;

    }

    public void retirerObstacleParHexHashcode(String obstacle_hex_hashcode) {
        Predicate<Obstacle> filtre = opo -> ((Integer.toHexString(opo.hashCode())).equals(obstacle_hex_hashcode));
        elements.removeIf(filtre);

    }

    @Override
    public boolean contient(Point2D p) {

        boolean resultat_vrai = (typeSurface() == TypeSurface.CONVEXE);
        boolean resultat_faux = (typeSurface() != TypeSurface.CONVEXE);

        switch (operateur.get()) {
            case UNION -> {

                for (Obstacle o : elements) {
                    if (o.contient(p)) return resultat_vrai;
                }

                return resultat_faux;

            }
            case INTERSECTION -> {

                if (elements.size() == 0)
                    return resultat_faux;

                for (Obstacle o : elements) {
                    if (!o.contient(p)) return resultat_faux;
                }

                return resultat_vrai;

            }
            case DIFFERENCE -> {

                if (elements.size() == 0)
                    return resultat_faux;

                Iterator<Obstacle> ito = elements.iterator();

                Obstacle ob_principal = ito.next();

                if (!ob_principal.contient(p)) return resultat_faux;

                while (ito.hasNext()) {
                    Obstacle ob = ito.next();
                    // Rappel convention : si le point est à la surface de l'obstacle (= de la composition), il est
                    // contenu dedans
                    if (ob.contient_strict(p)) return resultat_faux;
                }

                return resultat_vrai;
            }
            case DIFFERENCE_SYMETRIQUE -> {
                if (elements.size() == 0)
                    return resultat_faux;

                boolean est_dans_un_obstacle = false;

                for (Obstacle ob : elements) {
                    if (ob.contient(p)) {
                        if (!est_dans_un_obstacle)
                            est_dans_un_obstacle = true;
                        else { // Le point est au moins dans deux obstacles : il n'est donc pas dans la différence
                            // symétrique, sauf si il est à la surface du deuxième

                            if (!ob.aSurSaSurface(p))
                                return resultat_faux; // Le point est strictement contenu dans le deuxième obstacle : on l'écarte
                        }
                    }
                }
                // Si le point n'est que dans un et un seul obstacle, il est contenu dans l'obstacle
                return (est_dans_un_obstacle ? resultat_vrai : resultat_faux);
            }

        }

        throw new IllegalStateException("Composition::contient : operateur inconnu");
    }

    @Override
    public boolean aSurSaSurface(Point2D p) {

        switch (operateur.get()) {
            case UNION -> {

                boolean est_sur_une_surface = false;
                boolean est_strictement_dans_un_obstacle = false;
                for (Obstacle o : elements) {
                    if (!est_strictement_dans_un_obstacle && o.contient_strict(p))
                        est_strictement_dans_un_obstacle = true;
                    else if (o.aSurSaSurface(p))
                        est_sur_une_surface = true;

//                    if (o.aSurSaSurface(p))
//                        est_sur_une_surface = true ;
                }
                return (est_sur_une_surface && !est_strictement_dans_un_obstacle);
            }
            case INTERSECTION -> {
                boolean est_sur_une_surface = false;
                for (Obstacle o : elements) {

                    // Le point doit être contenu dans tous les obstacles pour être dans leur INTERSECTION
                    if (!o.contient(p))
                        return false;

                    // Il doit aussi être au moins sur une surface
                    if (o.aSurSaSurface(p))
                        est_sur_une_surface = true;
                }
                return est_sur_une_surface;
            }
            case DIFFERENCE -> {

                if (elements.size() == 0)
                    return false;

                Iterator<Obstacle> ito = elements.iterator();

                Obstacle ob_principal = ito.next(); // On sait déjà que la liste des obstacles n'est pas vide

                if (!ob_principal.contient(p)) return false;

                boolean est_sur_surface_ob_principal = ob_principal.aSurSaSurface(p);
                boolean est_sur_surface_autre_ob = false;

                while (ito.hasNext()) {
                    Obstacle ob = ito.next();

                    if (ob.contient_strict(p))
                        return false;

                    if (ob.aSurSaSurface(p))
                        est_sur_surface_autre_ob = true;
                }

                return (est_sur_surface_ob_principal || est_sur_surface_autre_ob);
            }
            case DIFFERENCE_SYMETRIQUE -> {
                boolean est_sur_une_surface = false;
                boolean est_contenu_strictement_dans_un_obstacle = false;

                for (Obstacle o : elements) {

                    if (o.contient_strict(p)) {
                        if (est_contenu_strictement_dans_un_obstacle)
                            return false; // Le point est contenu strictement dans deux obstacles au moins : il ne peut
                            // pas être dans la DIFFERENCE_SYMETRIQUE
                        else
                            est_contenu_strictement_dans_un_obstacle = true;
                    }

                    if (o.aSurSaSurface(p))
                        est_sur_une_surface = true;

                }

                return est_sur_une_surface;

//                return ((ob1.aSurSaSurface(p) && (!ob2.contient(p))) || (ob2.aSurSaSurface(p) && (!ob1.contient(p)))
//                        || (ob1.aSurSaSurface(p) && ob2.contient(p)) || (ob2.aSurSaSurface(p) && ob1.contient(p)));
            }
        }

        throw new IllegalStateException("Composition::aSurSaSurface : operateur inconnu");
    }

    protected Obstacle estSurSurfaceDe(Point2D p) throws Exception {

        Obstacle obst = null;

        Exception ex = new IllegalStateException("Composition::estSurSurfaceDe : le point n'est pas à la surface de la Composition");

        switch (operateur.get()) {
            case UNION -> {

                boolean est_sur_une_surface = false;
                for (Obstacle o : elements) {
                    if (est_sur_une_surface && o.contient_strict(p))
                        throw ex; // Point strictement inclus dans un des obstacles : ne peut pas être à la surface de leur UNION
                    if (o.aSurSaSurface(p)) {
                        est_sur_une_surface = true;
                        obst = o;
                    }
                }
                if (obst == null)
                    throw ex;

                return obst;
            }
            case INTERSECTION -> {
                boolean est_sur_une_surface = false;
                for (Obstacle o : elements) {

                    // Le point doit être contenu dans tous les obstacles pour être dans leur INTERSECTION
                    if (!o.contient(p))
                        throw ex;

                    // Il doit aussi être au moins sur une surface
                    if (o.aSurSaSurface(p)) {
                        est_sur_une_surface = true;
                        obst = o;
                    }
                }
                if (obst == null)
                    throw ex;

                return obst;
            }
            case DIFFERENCE -> {

                if (elements.size() == 0)
                    throw ex;

                Iterator<Obstacle> ito = elements.iterator();

                Obstacle ob_principal = ito.next(); // On sait déjà que la liste des obstacles n'est pas vide

                if (!ob_principal.contient(p)) throw ex;

                boolean est_sur_surface_ob_principal = ob_principal.aSurSaSurface(p);

                boolean est_sur_surface_autre_ob = false;

                while (ito.hasNext()) {
                    Obstacle ob = ito.next();

                    if (ob.contient_strict(p))
                        throw ex;

                    if (ob.aSurSaSurface(p)) {
                        est_sur_surface_autre_ob = true;
                        obst = ob;
                    }
                }

                if (est_sur_surface_ob_principal)
                    return ob_principal;

                if (obst == null)
                    throw ex;

                return obst;
            }
            case DIFFERENCE_SYMETRIQUE -> {
                boolean est_sur_une_surface = false;
                boolean est_contenu_strictement_dans_un_obstacle = false;

                for (Obstacle ob : elements) {

                    if (ob.contient_strict(p)) {
                        if (est_contenu_strictement_dans_un_obstacle)
                            throw ex; // Le point est contenu strictement dans deux obstacles au moins : il ne peut
                            // pas être dans la DIFFERENCE_SYMETRIQUE
                        else
                            est_contenu_strictement_dans_un_obstacle = true;
                    }

                    if (ob.aSurSaSurface(p)) {
                        est_sur_une_surface = true;
                        obst = ob;
                    }

                }

                if (obst == null)
                    throw ex;

                return obst;

            }
        }

        throw new IllegalStateException("Composition::estSurSurfaceDe : le point n'est pas à la surface de la Composition");
    }

    @Override
    public Point2D normale(Point2D p) throws Exception {
//        Obstacle ob1 = obstacle1.get() ;
//        Obstacle ob2 = obstacle2.get() ;
//
//        boolean est_sur_ob1 = ob1.aSurSaSurface(p) ;
//        boolean est_sur_ob2 = ob2.aSurSaSurface(p) ;
//
//        boolean est_dans_ob1 = ob1.contient(p) ;
//        boolean est_dans_ob2 = ob2.contient(p) ;

        // Identification de l'obstacle sur la surface duquel se trouve le point
        Obstacle obst = estSurSurfaceDe(p);

        double coeff_renversement = (typeSurface() == TypeSurface.CONVEXE ? 1.0 : -1.0);

        switch (operateur.get()) {
            case UNION, INTERSECTION -> {
                return obst.normale(p).multiply(coeff_renversement);
            }
            case DIFFERENCE -> {
                if (/* obst== elements.get(0) &&*/ elements.indexOf(obst) == 0)
                    return obst.normale(p).multiply(coeff_renversement);

                return obst.normale(p).multiply(-1.0).multiply(coeff_renversement);
            }
            case DIFFERENCE_SYMETRIQUE -> {

                for (Obstacle ob : elements) {
                    if (ob.contient_strict(p))
                        return obst.normale(p).multiply(-1.0).multiply(coeff_renversement);
                }

                return obst.normale(p).multiply(coeff_renversement);
            }
        }

        throw new Exception("Impossible de trouver la normale d'un point qui n'est pas sur la surface de la Composition.");

    }

    @Override
    public ArrayList<Point2D> cherche_toutes_intersections(Rayon r) {

        ArrayList<Point2D> resultats = new ArrayList<>(2 * elements.size());

        // A priori, pas besoin de la classe Intersection (pas besoin de savoir quel obstacle est rencontré à chaque intersection)
        //ArrayList<Intersection> liste_intersections = new ArrayList<>(2* obstacles.size()) ;
        //ArrayList<Point2D> liste_intersections = new ArrayList<>(2* elements.size()) ;


//        for (Obstacle o : obstacles) {
//            ArrayList<Point2D> intersections_o = o.cherche_toutes_intersections(r) ;
//
//            for (Point2D p : intersections_o)
//                liste_intersections.add(new Intersection(p,o)) ;
//        }

        for (Obstacle o : elements) {
            ArrayList<Point2D> intersections_o = o.cherche_toutes_intersections(r);

            LOGGER.log(Level.FINER, "{0} intersection(s) trouvée(s) avec l'obstacle {1} de la Composition : {2} ", new Object[]{intersections_o.size(), o, intersections_o});

            // On ne garde que les points qui sont sur la surface de la composition
            for (Point2D p : intersections_o) {

                if (aSurSaSurface(p)) {
                    resultats.add(p);
                    LOGGER.log(Level.FINER, "    L'intersection {0} est sur la surface de la Composition", p);
                }
            }

        }

        Comparator<Point2D> comparateur = (p1, p2) -> {

            double distance_p1_depart = p1.subtract(r.depart()).magnitude();
            double distance_p2_depart = p2.subtract(r.depart()).magnitude();

            return Double.compare(distance_p1_depart, distance_p2_depart);

        };


//        Comparator<Intersection> comparateur = (i1, i2) -> {
//
//            double distance_i1_depart = i1.point.subtract(r.depart).magnitude() ;
//            double distance_i2_depart = i2.point.subtract(r.depart).magnitude() ;
//
//            return Double.compare(distance_i1_depart, distance_i2_depart);
//
//        } ;

//        liste_intersections.sort(comparateur);
        resultats.sort(comparateur);

//        // On ne garde que les points qui sont sur la surface de la composition
//        for (Intersection i : liste_intersections) {
//            if (aSurSaSurface(i.point))
//                resultats.add(i.point) ;
//        }

        return resultats;

    }

    @Override
    public Point2D cherche_intersection(Rayon r, ModeRecherche mode) {

        ArrayList<Point2D> intersections = cherche_toutes_intersections(r);

        LOGGER.log(Level.FINER, "{0} intersection(s) trouvée(s) avec la Composition {1} : {2}", new Object[]{intersections.size(), this, intersections});

        if (intersections.size() == 0)
            return null;

        if (mode == ModeRecherche.PREMIERE)
            return intersections.get(0);

        // mode == DERNIERE
        return intersections.get(intersections.size() - 1);

    }


    @Override
    public void ajouterRappelSurChangementToutePropriete(RappelSurChangement rap) {

        imp_elementAvecContour.ajouterRappelSurChangementToutePropriete(rap);
        imp_elementAvecMatiere.ajouterRappelSurChangementToutePropriete(rap);


        operateur.addListener((observable, oldValue, newValue) -> {
            rap.rappel();
        });

        for (Obstacle o : elements)
            o.ajouterRappelSurChangementToutePropriete(rap);

//        obstacle1.get().ajouterRappelSurChangementToutePropriete(rap);
//        obstacle2.get().ajouterRappelSurChangementToutePropriete(rap);

//        obstacle1.addListener((observable, oldValue, newValue) -> {rap.rappel(); });
//        obstacle2.addListener((observable, oldValue, newValue) -> {rap.rappel(); });
    }

    @Override
    public void ajouterRappelSurChangementTouteProprieteModifiantChemin(RappelSurChangement rap) {

        imp_elementAvecContour.ajouterRappelSurChangementTouteProprieteModifiantChemin(rap);
        imp_elementAvecMatiere.ajouterRappelSurChangementTouteProprieteModifiantChemin(rap);

        operateur.addListener((observable, oldValue, newValue) -> {
            rap.rappel();
        });

        for (Obstacle o : elements)
            o.ajouterRappelSurChangementTouteProprieteModifiantChemin(rap);

//        obstacle1.get().ajouterRappelSurChangementTouteProprieteModifiantChemin(rap);
//        obstacle2.get().ajouterRappelSurChangementTouteProprieteModifiantChemin(rap);

//        obstacle1.addListener((observable, oldValue, newValue) -> {rap.rappel(); });
//        obstacle2.addListener((observable, oldValue, newValue) -> {rap.rappel(); });

    }


    @Override
    public boolean aSymetrieDeRevolution() {

        Obstacle o_prec = null;
        Double direction_commune = null;

        for (Obstacle o : elements) {

            // Si un des éléments de la Composition n'est pas centrable, la Composition ne l'est pas.
            // Il pourrait théoriquement l'être, par exemple si l'obstacle non centrable est exclu de la Composition
            // par le jeu des intersections ou des différences avec les autres obstacles, mais nous ne voulons pas
            // rentrer ces cas particuliers qui n'ont que peu d'intérêt : ce genre de situation n'apparait ni dans les
            // lentilles ni dans les objectifs nous semble-t-il.
            if (!o.aSymetrieDeRevolution())
                return false;

            if (o_prec != null) {

                if (o_prec.aUneOrientation() && o.aUneOrientation()) {
                    if ((o_prec.orientation() % 180d) != (o.orientation() % 180d))
                        return false;
                }

                // Cas où o_prec.pointSurAxeRevolution() et o.pointSurAxeRevolution() sont confondus : rien à controler
                if (o_prec.pointSurAxeRevolution().equals(o.pointSurAxeRevolution()))
                    continue;

                DemiDroiteOuSegment seg = DemiDroiteOuSegment.construireSegment(o_prec.pointSurAxeRevolution(), o.pointSurAxeRevolution());

                double nouvelle_direction_commune = seg.angle() % 180d;

                if (o_prec.aUneOrientation()) {
                    if (!Environnement.quasiEgal((o_prec.orientation() % 180d), nouvelle_direction_commune))
                        //                    if ((o_prec.orientation() % 180d) != (seg.angle() % 180d))
                        return false;
                } else if (o.aUneOrientation()) {
                    if (!Environnement.quasiEgal((o.orientation() % 180d), nouvelle_direction_commune))
                        //                        if ((o.orientation() % 180d) != (seg.angle() % 180d))
                        return false;
                }

                //                if (direction_commune!=null && direction_commune!=nouvelle_direction_commune)
                if (direction_commune != null && !Environnement.quasiEgal(direction_commune, nouvelle_direction_commune))
                    return false;

                direction_commune = nouvelle_direction_commune;
            }
            o_prec = o;
        }
        return true;
    }

    @Override
    public Point2D pointSurAxeRevolution() {
        return ((elements.size() > 0 && aSymetrieDeRevolution()) ? elements.get(0).pointSurAxeRevolution() : null);
    }

    @Override
    public boolean estOrientable() {
        return true;
    }

    @Override
    public void definirOrientation(double orientation_deg) {

        if (elements.size() == 0)
            return;

        if (!aSymetrieDeRevolution())
            return;

        double delta_ori = 0d;

        // On oriente le premier élément
        delta_ori = orientation_deg - elements.get(0).orientation();
        elements.get(0).definirOrientation(orientation_deg);

        // Les éléments suivants doivent rester à même distance du premier, rotation par rapport au "centre" (=point sur axe révolution) du premier
        Point2D centre_rot = elements.get(0).pointSurAxeRevolution();


        for (int i = 1; i < elements.size(); i++) {
            Obstacle o = elements.get(i);
            o.tournerAutourDe(centre_rot, delta_ori);
        }

    }

    @Override
    public void tournerAutourDe(Point2D centre_rot, double angle_rot_deg) {

        for (Obstacle o : elements)
            o.tournerAutourDe(centre_rot, angle_rot_deg);

    }

    @Override
    public double orientation() {

        if (elements.size() == 0) return 0d;

        Obstacle o_prec = null;

        for (Obstacle o : elements) {

            if (o_prec != null) {

                // Cas où o_prec.pointSurAxeRevolution() et o.pointSurAxeRevolution() sont confondus : rien à controler
                if (o_prec.pointSurAxeRevolution().equals(o.pointSurAxeRevolution()))
                    continue;

                DemiDroiteOuSegment seg = DemiDroiteOuSegment.construireSegment(o_prec.pointSurAxeRevolution(), o.pointSurAxeRevolution());

//                double nouvelle_direction_commune = seg.angle() ;

                return seg.angle();
            }

            o_prec = o;
        }

        // Si tous les élements avaient même centre, ou si il n'y avait qu'un élement, on retourne l'orientation du premier
        return elements.get(0).orientation();
//        return Obstacle.super.orientation();
    }

    @Override
    public Double courbureRencontreeAuSommet(Point2D pt_sur_surface, Point2D direction) throws Exception {
        // Identification de l'obstacle sur la surface duquel se trouve le point
        Obstacle obst = estSurSurfaceDe(pt_sur_surface);

//        return (direction.dotProduct(normale(pt_sur_surface))<=0d?
//                obst.courbureRencontreeAuSommet(pt_sur_surface,direction):-obst.courbureRencontreeAuSommet(pt_sur_surface,direction)) ;

        return obst.courbureRencontreeAuSommet(pt_sur_surface, direction);
    }

    @Override
    public void definirAppartenanceSystemeOptiqueCentre(boolean b) {
        this.appartenance_systeme_optique_centre.set(b);

        // Tous les obstacles de la Composition sont concernés aussi (permet de désactiver certains contrôles de ces obstacles
        // dans les vues des panneaux, comme le contrôle de l'orientation)
        for (Obstacle o : elements)
            o.definirAppartenanceSystemeOptiqueCentre(b);
    }

    @Override
    public boolean appartientASystemeOptiqueCentre() {
        return this.appartenance_systeme_optique_centre.get();
    }

    @Override
    public void definirAppartenanceComposition(boolean b) {
        this.appartenance_composition.set(b);

        for (Obstacle o : elements)
            o.definirAppartenanceComposition(b);

    }
    @Override
    public boolean appartientAComposition() {return this.appartenance_composition.get() ;}


    /**
     * @return
     */
    @Override
    public double rayonDiaphragmeMaximumConseille() {

        double res = Double.MAX_VALUE ;

        for (Obstacle o : elements) {
            if (o.rayonDiaphragmeMaximumConseille()<res)
                res = o.rayonDiaphragmeMaximumConseille() ;
        }

        return res ;
    }

    @Override
    public Double ZMinorantSurAxe(Point2D origine_axe, Point2D direction_axe) {

        Double z_resultat = null;

        for (Obstacle o : elements) {
            Double z_min = o.ZMinorantSurAxe(origine_axe,direction_axe) ;

            if (z_min==null)
                continue;

            // On prend le z_min même s'il n'est pas sur la surface de la composition : on ne cherche qu'un minorant
            // et pas le z de la premiere interaction de la composition avec l'axe
            if (z_resultat==null || z_min<=z_resultat)
                z_resultat = z_min ;
        }

        return z_resultat ;
    }

    @Override
    public Double abscisseIntersectionSuivanteSurAxe(Point2D origine_axe, Point2D direction_axe, double z_depart, boolean sens_z_croissants, Double z_inter_prec) {

//        Double z_intersection = null;
        Double z_resultat = null;
        double distance_min = Double.MAX_VALUE;
        Point2D p_depart = origine_axe.add(direction_axe.multiply(z_depart));

        for (Obstacle o : elements) {
//            z_intersection = o.abscisseIntersectionSuivanteSurAxe(origine_axe, direction_axe, z_depart, sens_z_croissants);
            ArrayList<Double> z_intersections = o.abscissesToutesIntersectionsSurAxe(origine_axe, direction_axe, z_depart, sens_z_croissants,z_inter_prec);

//            z_intersection = (z_intersections.size()==0?null:z_intersections.get(0)) ;

            for (Double z_intersection : z_intersections) {

//                if (z_intersection == null)
//                    continue;

                Point2D p_intersection = origine_axe.add(direction_axe.multiply(z_intersection));

                double distance = Math.abs(z_intersection - z_depart);

//            double distance = p_intersection.distance(p_depart) ;

                // On ne garde que les points qui sont sur la surface de la composition
                if (distance < distance_min && aSurSaSurface(p_intersection)) {
                    z_resultat = z_intersection;
                    distance_min = distance;
                }
            }
        }

        return z_resultat;

    }

    @Override
    public ArrayList<Double> abscissesToutesIntersectionsSurAxe(Point2D origine_axe, Point2D direction_axe, double z_depart, boolean sens_z_croissants, Double z_inter_prec) {

        ArrayList<Double> resultats = new ArrayList<>(2 * elements.size());

        // A priori, pas besoin de la classe Intersection (pas besoin de savoir quel obstacle est rencontré à chaque intersection)
        //ArrayList<Intersection> liste_intersections = new ArrayList<>(2* obstacles.size()) ;
        //ArrayList<Point2D> liste_intersections = new ArrayList<>(2* elements.size()) ;


//        for (Obstacle o : obstacles) {
//            ArrayList<Point2D> intersections_o = o.cherche_toutes_intersections(r) ;
//
//            for (Point2D p : intersections_o)
//                liste_intersections.add(new Intersection(p,o)) ;
//        }

        for (Obstacle o : elements) {
            ArrayList<Double> z_intersections = o.abscissesToutesIntersectionsSurAxe(origine_axe, direction_axe, z_depart, sens_z_croissants,z_inter_prec);
//            ArrayList<Point2D> intersections_o = o.cherche_toutes_intersections(r) ;

            LOGGER.log(Level.FINER, "{0} intersection(s) trouvée(s) avec l'obstacle {1} de la Composition : {2} ", new Object[]{z_intersections.size(), o, this});

            // On ne garde que les points qui sont sur la surface de la composition
//            for (Point2D p : intersections_o) {
            for (Double z : z_intersections) {

                Point2D p = origine_axe.add(direction_axe.multiply(z));

                if (aSurSaSurface(p)) {
//                    resultats.add(p);
                    resultats.add(z);
                    LOGGER.log(Level.FINER, "    L'intersection z={0} est sur la surface de la Composition", z);
                }
            }

        }

        Comparator<Double> comparateur = (z1, z2) -> {

//            double distance_p1_depart = p1.subtract(r.depart()).magnitude() ;
            double distance_z1_depart = Math.abs(z1 - z_depart);
//            double distance_p2_depart = p2.subtract(r.depart()).magnitude() ;
            double distance_z2_depart = Math.abs(z2 - z_depart);

            return Double.compare(distance_z1_depart, distance_z2_depart);

        };


//        Comparator<Intersection> comparateur = (i1, i2) -> {
//
//            double distance_i1_depart = i1.point.subtract(r.depart).magnitude() ;
//            double distance_i2_depart = i2.point.subtract(r.depart).magnitude() ;
//
//            return Double.compare(distance_i1_depart, distance_i2_depart);
//
//        } ;

//        liste_intersections.sort(comparateur);
        resultats.sort(comparateur);

//        // On ne garde que les points qui sont sur la surface de la composition
//        for (Intersection i : liste_intersections) {
//            if (aSurSaSurface(i.point))
//                resultats.add(i.point) ;
//        }

        return resultats;


    }
}