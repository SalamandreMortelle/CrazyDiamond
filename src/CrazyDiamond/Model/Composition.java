package CrazyDiamond.Model;


import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
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

        elements = new SimpleListProperty<>(olo);

        operateur = new SimpleObjectProperty<>(op);

        appartenance_systeme_optique_centre = new SimpleBooleanProperty(false);
        appartenance_composition = new SimpleBooleanProperty(false);

    }
    @Override public String id() { return imp_identifiable.id(); }
    @Override public String nom() {
        return imp_nommable.nom();
    }
    @Override public StringProperty nomProperty() {
        return imp_nommable.nomProperty();
    }

    @Override public Color couleurContour() {return imp_elementAvecContour.couleurContour();}
    @Override public void definirCouleurContour(Color c) { imp_elementAvecContour.definirCouleurContour(c); }
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
    @Override public void definirCouleurMatiere(Color couleur) { imp_elementAvecMatiere.definirCouleurMatiere(couleur); }
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

    public boolean estVide() {
        return elements().size()==0 ;
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
    public Commande commandeCreation(Environnement env) {
        return new CommandeCreerComposition(env,this) ;
    }
    @Override
    public void retaillerPourSourisEn(Point2D pos_souris) {}

    @Override
    public void translater(Point2D vecteur) {
        for (Obstacle o : elements)
            o.translater(vecteur);
    }
    @Override
    public void translaterParCommande(Point2D vecteur) {
        new CommandeTranslaterObstacles(vecteur, elements()).executer();
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

    /**
     * Ajoute un obstacle dans la Composition.
     * NB : Les utilisateurs de cette méthode doivent veiller à retirer l'obstacle de l'environnement avant d'appeler
     * cette méthode.
     * @param o : obstacle à ajouter
     */
    public void ajouterObstacle(Obstacle o) {

        if (this.elements.contains(o))
            return;

        // TODO : il faudrait peut-être vérifier si l'obstacle appartient à l'environnement car sinon, il n'y aura pas de notification
        // des rappels en cas de modification de ses propriétés (car ces rappels sont ajoutés lors de l'ajout de l'obstacle à l'environnement)

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

    @Override
    public Obstacle obstacle_avec_id(String obs_id) {

        for (Obstacle ob : elements) {
            Obstacle o_trouve = ob.obstacle_avec_id(obs_id) ;
            if (o_trouve!=null)
                return o_trouve ;
        }

        return Obstacle.super.obstacle_avec_id(obs_id);
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
                for (Obstacle o : elements) {
                    // Le point doit être contenu dans tous les obstacles pour être dans leur INTERSECTION
                    if (!o.contient(p))
                        throw ex;

                    // Il doit aussi être au moins sur une surface
                    if (o.aSurSaSurface(p))
                        obst = o;
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

                while (ito.hasNext()) {
                    Obstacle ob = ito.next();

                    if (ob.contient_strict(p))
                        throw ex;

                    if (ob.aSurSaSurface(p)) {
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

        operateur.addListener((observable, oldValue, newValue) -> rap.rappel());

        for (Obstacle o : elements) {
            o.ajouterRappelSurChangementToutePropriete(rap);
        }

        // Dans une composition, il faut aussi mettre en observation la liste de ses éléments pour réagir aux ajouts
        // et aux suppressions d'éléments
        ListChangeListener<Obstacle> lcl_elements = change -> {
            while (change.next()) {

                if (change.wasRemoved()) {
                    LOGGER.log(Level.FINER, "Obstacle supprimé de composition");
                    rap.rappel();

                } else if (change.wasAdded()) {

                    for (Obstacle additem : change.getAddedSubList()) {
                        LOGGER.log(Level.FINER, "Obstacle ajouté dans la composition : {0}", additem);
                        rap.rappel();

                    }
                }

            }
        };
        elements.addListener(lcl_elements);

    }

    @Override
    public void ajouterRappelSurChangementTouteProprieteModifiantChemin(RappelSurChangement rap) {

        imp_elementAvecContour.ajouterRappelSurChangementTouteProprieteModifiantChemin(rap);
        imp_elementAvecMatiere.ajouterRappelSurChangementTouteProprieteModifiantChemin(rap);

        operateur.addListener((observable, oldValue, newValue) -> rap.rappel());

        for (Obstacle o : elements)
            o.ajouterRappelSurChangementTouteProprieteModifiantChemin(rap);

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

        // On oriente le premier élément
        double delta_ori = orientation_deg - elements.get(0).orientation();
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
    public List<DioptreParaxial> dioptresParaxiaux(PositionEtOrientation axe) {

        ArrayList<DioptreParaxial> resultat = new ArrayList<>(2*elements.size()) ;
        ArrayList<DioptreParaxial> dioptres_composition = new ArrayList<>(2*elements.size()) ;

        // Pour les UNIONs et les INTERSECTIONs
        int nb_obs_contenant = 0 ;

        // Pour les DIFFERENCEs
        Obstacle obs_principal = (elements.size()>0?elements.get(0):null) ;
        int nb_obs_principal_contenant = 0 ;
        int nb_obs_secondaire_contenant = 0 ;

        // Pour les INTERSECTIONs
        int nb_obs_avec_matiere = 0 ;

        // Construction de la liste "brute" des dioptres de tous les obstacles de la composition. Comptage du nombre
        // total d'obstacles (hors obstacles sans matière comme un cercle de rayon nul) et du nombre d'obstacles qui
        // s'étendent sur z = -infini
        for (int i = 0 ; i<elements.size() ; i++) {

            Obstacle o = elements.get(i) ;

            List<DioptreParaxial> dioptres_o = o.dioptresParaxiaux(axe);

            if (dioptres_o.isEmpty()) // Pour écarter les Cercles (ou les ellipses...) de rayon (ou de paramètre) nul
                continue;

            if (dioptres_o.get(0).indiceAvant() > 0d ) {
                ++nb_obs_contenant;

                if (i==0)
                    ++nb_obs_principal_contenant ;
                else
                    ++nb_obs_secondaire_contenant ;
            }

            ++nb_obs_avec_matiere; // Rappel : une Composition ne peut contenir que des stream_obstacles avec matière (pas de segments)

            dioptres_composition.addAll(dioptres_o) ;
        }

        // Tri par Z croissant et Rc "croissant"
        dioptres_composition.sort(DioptreParaxial.comparateur) ;

        for (DioptreParaxial d_c : dioptres_composition) {

            // On remplace tous les indices non nuls des dioptres de la composition par l'indice de la Composition
            if (d_c.indiceAvant()>0d)
                d_c.indice_avant.set(indiceRefraction());
            if (d_c.indiceApres()>0d)
                d_c.indice_apres.set(indiceRefraction());

            switch (operateur()) {
                case UNION -> {

                    if (nb_obs_contenant>0) { // On est déjà dans un obstacle
                        if (d_c.indiceApres() > 0) { // Entrée dans un obstacle
                            ++nb_obs_contenant;
                        } else { // Sortie d'un obstacle
                            --nb_obs_contenant;
                            if (nb_obs_contenant == 0)
                                resultat.add(d_c);
                        }
                    } else { // On n'est pas dans un obstacle
                        if (d_c.indiceApres() > 0) { // Entrée dans un obstacle
                            ++nb_obs_contenant;
                            resultat.add(d_c) ;
                        } else { // Sortie d'un obstacle
                            LOGGER.log(Level.SEVERE,"Impossible de sortir d'un obstacle sans y être entré.") ;
                        }

                    }

                }
                case INTERSECTION -> {
                    if (nb_obs_contenant==nb_obs_avec_matiere) { // On est dans la Composition car on est dans tous ses obstacles
                        if (d_c.indiceApres()==0) { // Sortie d'un obstacle
                            resultat.add(d_c) ;
                            --nb_obs_contenant ;
                        } else { // Entrée dans un obstacle
                            LOGGER.log(Level.SEVERE,"Impossible de rentrer dans un obstacle si on est déjà dans tous.") ;
                        }
                    } else { // On n'est pas dans la Composition
                        if (d_c.indiceApres() > 0) { // Entrée dans un obstacle
                            ++nb_obs_contenant;
                            if (nb_obs_contenant == nb_obs_avec_matiere) // Est_on maintenant dans tous les obstacles ?
                                resultat.add(d_c) ;
                        } else { // Sortie d'un obstacle
                            --nb_obs_contenant ;
                        }

                    }
                }
                case DIFFERENCE -> {
                    if (nb_obs_principal_contenant>0) { // On est déjà dans l'obstacle principal de la DIFFERENCE
                        if (nb_obs_secondaire_contenant==0) { // On n'est pas encore dans un objet secondaire
                            if (d_c.indiceApres()>0) { // Entrée dans un obstacle (forcément secondaire)
                                d_c.permuterIndicesAvantApres(); // Cette entrée dans l'obstacle nous fait sortir de la compo : il faut permuter les indices
                                resultat.add(d_c);
                                ++nb_obs_secondaire_contenant ;
                            } else { // Sortie d'un obstacle
                                if (d_c.obstacleSurface()==obs_principal) { // Sortie de l'obstacle principal
                                    resultat.add(d_c) ;
                                    --nb_obs_principal_contenant ;
                                } else { // Sortie d'un objet (forcément secondaire)
                                    LOGGER.log(Level.SEVERE,"Impossible de sortir d'un obstacle secondaire sans y être entré.") ;
                                }
                            }
                        } else { // On est déjà dans un objet secondaire
                            if (d_c.indiceApres()>0) { // Entrée dans un obstacle (forcément secondaire)
                                ++nb_obs_secondaire_contenant ;
                            } else { // Sortie d'un obstacle
                                if (d_c.obstacleSurface()==obs_principal) { // Sortie de l'obstacle principal
                                    --nb_obs_principal_contenant ;
                                } else { // Sortie d'un objet secondaire
                                    --nb_obs_secondaire_contenant ;
                                    if (nb_obs_secondaire_contenant==0) {
                                        d_c.permuterIndicesAvantApres(); // Cette sortie dans un obstacle secondaire nous fait entrer dans la compo : il faut permuter les indices
                                        resultat.add(d_c);
                                    }
                                }
                            }
                        }
                    } else { // On n'est pas encore dans l'obstacle principal
                        if (nb_obs_secondaire_contenant==0) { // On n'est pas encore dans un objet secondaire
                            if (d_c.indiceApres()>0) { // Entrée dans un obstacle
                                if (d_c.obstacleSurface()==obs_principal) { // Entrée dans l'obstacle principal
                                    resultat.add(d_c) ;
                                    ++nb_obs_principal_contenant ;
                                } else { // Entrée dans un obstacle secondaire
                                    ++nb_obs_secondaire_contenant ;
                                }
                            } else { // Sortie d'un obstacle
                                LOGGER.log(Level.SEVERE,"Impossible de sortir d'un obstacle sans y être entré.") ;
                            }
                        } else { // On est déjà dans un ou plusieurs obstacles secondaires
                            if (d_c.indiceApres()>0) { // Entrée dans un obstacle
                                if (d_c.obstacleSurface()==obs_principal) { // Entrée dans l'obstacle principal
                                    ++nb_obs_principal_contenant ;
                                } else { // Entrée dans un obstacle secondaire
                                    ++nb_obs_secondaire_contenant ;
                                }
                            } else { // Sortie d'un obstacle (forcément secondaire)
                                --nb_obs_secondaire_contenant ;
                            }
                        }
                    } // Fin du cas "pas encore dans l'objet principal"
                }
                case DIFFERENCE_SYMETRIQUE -> {
                    if (nb_obs_contenant==0) { // On n'est pas encore dans un obstacle
                        if (d_c.indiceApres()>0) { // On entre dans un obstacle
                            resultat.add(d_c) ;
                            ++nb_obs_contenant ;
                        } else { // On sort d'un obstacle
                            LOGGER.log(Level.SEVERE,"Impossible de sortir d'un obstacle sans y être entré.") ;
                        }
                    } else { // On est déjà dans un ou plusieurs obstacles
                        if (d_c.indiceApres()>0) { // On entre dans un (autre) obstacle
                            ++nb_obs_contenant ;
                            if (nb_obs_contenant==2) {// Si on est maintenant dans deux obstacles, on vient de sortir de la composition -> dioptre à ajouter
                                d_c.permuterIndicesAvantApres(); // Cette entrée dans un obstacle nous fait sortir de la compo : il faut permuter les indices
                                resultat.add(d_c);
                            }
                        } else { // On sort d'un obstacle
                            --nb_obs_contenant ;
                            if (nb_obs_contenant<=1) {

                                if (nb_obs_contenant==1) // Si on n'est plus que dans un obstacle, c'est que cette sortie
                                    // nous a fait entrer dans la compo : il faut permuter les indices.
                                    d_c.permuterIndicesAvantApres();

                                resultat.add(d_c);
                            }
                        }
                    }
                }
            }


        }


        //  On peut se retrouver avec des dioptres qui sont "confondus" (même Z, même Rc algébrique) que l'on peut
        //  "fusionner" en mettant à jour de façon cohérente les indices avant/après (et en tenant compte de la présence
        //  éventuelle de diaphragmes, qu'il faut alors conserver)
        // NB : ce traitement de fusion n'est probablement pas strictement nécessaire, mais avoir des dioptres inutiles
        // complique inutilement les calculs de la matrice de transfert optique, et de toutes les propriétés optiques du SOC

        return fusionneDioptres(resultat) ;
//        return resultat ;

    }

     private List<DioptreParaxial> fusionneDioptres(List<DioptreParaxial> liste_dioptres) {

        ArrayList<DioptreParaxial> resultat_fusionne = new ArrayList<>(liste_dioptres.size()) ;

        DioptreParaxial d_prec = null;

        for (DioptreParaxial d_courant : liste_dioptres) {

            if (d_prec != null) {
                if (d_prec.estConfonduAvec(d_courant)) {
                    d_prec.fusionneAvecDioptreConfondu(d_courant);
                    continue; // On saute le dioptre courant d_res, puisqu'il a été fusionné dans le précédent
                } else {
                    if (d_prec.estInutile())
                        resultat_fusionne.remove(resultat_fusionne.size()-1) ;
                }
            }

            resultat_fusionne.add(d_courant) ;

            d_prec = d_courant;
        }

        int dernier_index = resultat_fusionne.size()-1 ;
        if (dernier_index>=0 && resultat_fusionne.get(dernier_index).estInutile())
            resultat_fusionne.remove(dernier_index) ;

        return resultat_fusionne ;

    }

    @Override
    public void convertirDistances(double facteur_conversion) {

        for (Obstacle o : elements)
            o.convertirDistances(facteur_conversion);

    }


}