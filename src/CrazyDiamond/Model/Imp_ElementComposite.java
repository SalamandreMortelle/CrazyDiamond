package CrazyDiamond.Model;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Imp_ElementComposite {

    // Récupération du logger
    Logger LOGGER = Logger.getLogger( "CrazyDiamond" );

    private final ListProperty<Obstacle> elements;

    // TODO : supprimer cette liste de ListChangeListener devenue inutile grâce aux rappels avec propagation automatique
    //  dans les composites
    private final ArrayList<ListChangeListener<Obstacle>> observateurs_des_elements ;

    public Imp_ElementComposite() {

        ObservableList<Obstacle> olo = FXCollections.observableArrayList();
        elements = new SimpleListProperty<>(olo);

        this.observateurs_des_elements = new ArrayList<>(1) ;

    }

    public void ajouterListeners(BaseObstacle bo) {
        ListChangeListener<Obstacle> lcl_elements = change -> {
            while (change.next()) {

                if (change.wasRemoved()) {
                    LOGGER.log(Level.FINER, "Obstacle supprimé du Composite");
                    bo.declencherRappelsSurChangementTouteProprieteModifiantChemin();
                    bo.declencherRappelsSurChangementToutePropriete();

                } else if (change.wasAdded()) {

                    for (Obstacle additem : change.getAddedSubList()) {
                        LOGGER.log(Level.FINER, "Obstacle ajouté dans le Composite : {0}", additem);
                        bo.declencherRappelsSurChangementTouteProprieteModifiantChemin();
                        bo.declencherRappelsSurChangementToutePropriete();
                    }
                }
            }
        };
        // Ajout récursif du rappel dans tous les sous-groupes et dans toutes les sous-compositions
        ajouterListChangeListener(lcl_elements);

    }

    protected ObservableList<Obstacle> elementsObservables() {return elements.get();}
    public List<Obstacle> elements() {return elements.get();}

    public boolean estVide() {
        return elements.size()==0 ;
    }

    public Obstacle obstacle(int index) {return elements.get(index) ;}
    public int index(Obstacle o) { return elements.indexOf(o) ;}

    public void ajouterObstacle(Obstacle o) {

        if (this.elements.contains(o))
            return;

        // TODO : il faudrait peut-être vérifier si l'obstacle o appartient à l'environnement car sinon, il n'y aura pas de notification
        // des rappels en cas de modification de ses propriétés (car ces rappels sont ajoutés lors de l'ajout de l'obstacle à l'environnement)

        this.elements.add(o);  // Le listener des éléments (cf. constructeur de BaseObstacleComposite) se charge
                               // d'observer les modifications des propriétés de l'objet ajouté o, et de déclencher les
                               // rappels.

        if (o instanceof BaseObstacleComposite boc) {
            observateurs_des_elements.forEach(boc::ajouterListChangeListener);
        }

    }

    public void ajouterObstacleEnPosition(Obstacle o_a_ajouter, int i_pos_a_la_racine) {
        if (estALaRacine(o_a_ajouter))
            return;

        elements().add(i_pos_a_la_racine,o_a_ajouter);

        if (o_a_ajouter instanceof BaseObstacleComposite boc) {
            observateurs_des_elements.forEach(boc::ajouterListChangeListener);
        }
    }

    public void deplacerObstacleEnPositionALaRacine(Obstacle o_a_deplacer, int i_pos) {
        if (!estALaRacine(o_a_deplacer))
            throw new IllegalCallerException("Tentative de déplacer un élément qui n'est pas à la racine") ;

        elements().remove(o_a_deplacer) ;
        elements.add(i_pos,o_a_deplacer);
    }

    public void retirerObstacle(Obstacle o) {
        elements.remove(o);

        if (o instanceof BaseObstacleComposite boc) {
            observateurs_des_elements.forEach(boc::enleverListChangeListener);
        }
    }

    public boolean comprend(Obstacle o) {

        for (Obstacle ob : elements) {
            if (ob.comprend(o))
                return true ;
        }

        return false;
    }

    public Obstacle obstacle_avec_id(String obs_id) {

        for (Obstacle ob : elements) {
            Obstacle o_trouve = ob.obstacleAvecId(obs_id) ;
            if (o_trouve!=null)
                return o_trouve ;
        }

        return null ;
//        return Obstacle.super.obstacle_avec_id(obs_id);
    }

    /**
     * Ajoute un ListChangeListener sur le composite et sur tous les sous-composites (sous-groupes, sous-compositions)
     * Tout ajout ou retrait d'un élément dans le composite ou dans un de ses sous-composites déclenchera
     * le listener.
     * De plus, lorsqu'un nouvel obstacle sera ajouté dans le Groupe, les listeners déjà enregistrés lui seront
     * automatiquement ajoutés (cf . {link #ajouterObstacle}).
     * Si le listener a déjà été ajouté, rien n'est fait.
     * @param lcl_o le listener à ajouter
     */
    public void ajouterListChangeListener(ListChangeListener<Obstacle> lcl_o) {
//        if (observateurs_des_elements.contains(lcl_o)) // Observateur déjà enregistré : ne rien faire
//            return;

        elements.addListener(lcl_o);
        observateurs_des_elements.add(lcl_o);

        // Mise sur écoute récursive des sous-composites
        for (Obstacle o : elements) {
            if (o instanceof BaseObstacleComposite boc) // Détection des changements qui interviennent dans les sous-composites de tous types
                boc.ajouterListChangeListener(lcl_o);
        }

//        for (Obstacle o : elements) {
//            if (o instanceof Groupe) // Détection des changements qui interviennent dans les groupes
//                ((Groupe)o).ajouterListChangeListener(lcl_o);
//            else if (o instanceof Composition) {// Détection des changements qui interviennent dans les compositions
//                ((Composition) o).ajouterListChangeListener(lcl_o);
//            }
//        }

    }

    public void enleverListChangeListener(ListChangeListener<Obstacle> lcl_o) {
        if (!observateurs_des_elements.contains(lcl_o)) // Observateur pas enregistré : rien à faire
            return;

        for (Obstacle o : elements) {
            if (o instanceof BaseObstacleComposite boc) // Détection des changements qui interviennent dans les groupes
                boc.enleverListChangeListener(lcl_o);
        }

        elements.removeListener(lcl_o);
        observateurs_des_elements.remove(lcl_o);
    }

    public void enleverTousLesListChangeListeners() {
        for (ListChangeListener<Obstacle> lcl_o : observateurs_des_elements) {
            for (Obstacle o : elements) {
                if (o instanceof BaseObstacleComposite boc) // Détection des changements qui interviennent dans les groupes
                    boc.enleverListChangeListener(lcl_o);
            }
            elements.removeListener(lcl_o);
//            observateurs_des_elements.remove(lcl_o);
        }
        observateurs_des_elements.clear();
    }



    public void ajouterRappelSurChangementToutePropriete(Object cle_observateur,RappelSurChangement rap) {

        for (Obstacle o : elements)
            o.ajouterRappelSurChangementToutePropriete(cle_observateur,rap);

        // Dans un Composite, il faut aussi mettre en observation la liste des éléments pour réagir aux ajouts et aux
        // suppressions d'éléments
//        surveillerListeElements(rap);
    }

    public void retirerRappelSurChangementToutePropriete(Object cle_observateur) {
        for (Obstacle o : elements)
            o.retirerRappelSurChangementToutePropriete(cle_observateur);

        // Dans un Composite, il faut aussi mettre en observation la liste des éléments pour réagir aux ajouts et aux
        // suppressions d'éléments
//        arreterSurveillanceListeElements(rap);

    }


    public void ajouterRappelSurChangementTouteProprieteModifiantChemin(Object cle_observateur,RappelSurChangement rap) {
        for (Obstacle o : elements)
            o.ajouterRappelSurChangementTouteProprieteModifiantChemin(cle_observateur,rap);

        // Dans un Composite, il faut aussi mettre en observation la liste des éléments pour réagir aux ajouts et aux
        // suppressions d'éléments
//        surveillerListeElements(rap);
    }

    public void retirerRappelSurChangementTouteProprieteModifiantChemin(Object cle_observateur) {
        for (Obstacle o : elements)
            o.retirerRappelSurChangementTouteProprieteModifiantChemin(cle_observateur);
    }

    public int nombreObstaclesPremierNiveau() {
        return elements.size() ;
    }

    public Iterator<Obstacle> iterateurPremierNiveau() {
        return elements.iterator() ;
    }

    public void translater(Point2D vecteur) {
        for (Obstacle o : elements)
            o.translater(vecteur);
    }

    public void translaterParCommande(Point2D vecteur) {
        new CommandeTranslaterObstacles(vecteur, elements()).executer();
    }

    public boolean aSymetrieDeRevolution() {
        Obstacle o_prec = null;
        Double direction_commune = null;

//        for (Obstacle o : iterableObstaclesReelsDepuisArrierePlan()) {
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

                double nouvelle_direction_commune = Math.IEEEremainder(seg.angle(),180) /*seg.angle() % 180d*/ ;

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

    public Point2D pointSurAxeRevolution() {
        return ((elements.size() > 0 && aSymetrieDeRevolution()) ? elements.get(0).pointSurAxeRevolution() : null);
    }

    public boolean estOrientable() {
        return true;
    }

    public void definirOrientation(double orientation_deg) {
        if (elements.size() == 0)
            return;

        if (!aSymetrieDeRevolution())
            return;

        // On oriente le premier élément
//        double delta_ori = orientation_deg - elements.get(0).orientation();
        double delta_ori = Math.IEEEremainder(orientation_deg - elements.get(0).orientation(),180);

        elements.get(0).definirOrientation(orientation_deg);

        // Les éléments suivants doivent rester à même distance du premier, rotation par rapport au "centre" (=point sur axe révolution) du premier
        Point2D centre_rot = elements.get(0).pointSurAxeRevolution();

        for (int i = 1; i < elements.size(); i++) {
            Obstacle o = elements.get(i);
            o.tournerAutourDe(centre_rot, delta_ori);
        }
    }

    public void tournerAutourDe(Point2D centre_rot, double angle_rot_deg) {
        for (Obstacle o : elements)
            o.tournerAutourDe(centre_rot, angle_rot_deg);

    }

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

        // Si tous les éléments avaient même centre, ou s'il n'y avait qu'un élément, on retourne l'orientation du premier.
        return elements.get(0).orientation();
//        return Obstacle.super.orientation();
    }

//    public void definirAppartenanceSystemeOptiqueCentre(boolean b) {
//        // Tous les obstacles du Composite sont concernés aussi (permet de désactiver certains contrôles de ces obstacles
//        // dans les vues des panneaux, comme le contrôle de l'orientation).
//        for (Obstacle o : elements)
//            o.definirAppartenanceSystemeOptiqueCentre(b);
//    }

    public void definirSOCParent(SystemeOptiqueCentre soc) {
        // Tous les obstacles du Composite sont concernés aussi (permet de désactiver certains contrôles de ces obstacles
        // dans les vues des panneaux, comme le contrôle de l'orientation).
        for (Obstacle o : elements)
            o.definirSOCParent(soc);
    }

//    public void definirAppartenanceComposition(boolean b) {
//        // Tous les obstacles du Composite sont concernés aussi
//        for (Obstacle o : elements)
//            o.definirAppartenanceComposition(b);
//    }

    public void convertirDistances(double facteur_conversion) {
        for (Obstacle o : elements)
            o.convertirDistances(facteur_conversion);
    }

    public boolean estALaRacine(Obstacle o) {
        return elements.contains(o) ;
    }
}
