package CrazyDiamond.Model;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public abstract class BaseObstacleComposite extends BaseObstacle {

    private final Imp_ElementComposite imp_elementComposite ;

    BaseObstacleComposite(String nom) {
        super(nom);
        this.imp_elementComposite = new Imp_ElementComposite();
        this.imp_elementComposite.ajouterListeners(this) ;
    }

    BaseObstacleComposite(Imp_Identifiable ii, Imp_Nommable in,Imp_ElementComposite ic) {
        super(ii, in);
        this.imp_elementComposite = ic ;
        this.imp_elementComposite.ajouterListeners(this) ;
    }

    protected ObservableList<Obstacle> elementsObservables() { return imp_elementComposite.elementsObservables(); }
    public List<Obstacle> elements() { return imp_elementComposite.elements(); }
    public boolean estVide() {return imp_elementComposite.estVide();}

    public void ajouterObstacle(Obstacle o_a_ajouter) {
        ajouterObstacleEnPosition(o_a_ajouter,-1);
    }

    public void ajouterObstacleEnPosition(Obstacle o_a_ajouter, int i_pos) {
        o_a_ajouter.definirParent(this); // On commence par définir le parent (sinon problème)

        o_a_ajouter.definirSOCParent(SOCParent());

        if (i_pos==-1)
            imp_elementComposite.ajouterObstacle(o_a_ajouter);
        else
            imp_elementComposite.ajouterObstacleEnPosition(o_a_ajouter,i_pos);

        propagerRappelsSurChangementToutePropriete(o_a_ajouter);
        propagerRappelsSurChangementTouteProprieteModifiantChemin(o_a_ajouter);
    }

    public void retirerObstacle(Obstacle o) {
        imp_elementComposite.retirerObstacle(o);

        if (o.SOCParent()!=null)
            o.definirSOCParent(null);

        retirerRappelsPropagesSurChangementToutePropriete(o);
        retirerRappelsPropagesSurChangementTouteProprieteModifiantChemin(o);
//        o.definirParent(null);
    }

    public boolean comprend(Obstacle o) {return (imp_elementComposite.comprend(o) || this.equals(o)) ;}
    public boolean comprend(ElementDeSOC el) { return false ; } // Si cette méthode est appelée, c'est forcément avec un
    // ElementDeSOC qui est un SOC, car si c'est un Obstacle, c'est la méthode précédente comprend(Obstacle p) qui est appelée.
    // Or un Composite ne peut pas contenir de soc.

    public Obstacle obstacleAvecId(String obs_id) {
        Obstacle o_trouve = imp_elementComposite.obstacle_avec_id(obs_id) ;
        return (o_trouve!=null?o_trouve:super.obstacleAvecId(obs_id)) ;
    }

    public void ajouterRappelSurChangementToutePropriete(Object cle_observateur,RappelSurChangement rap) {
        super.ajouterRappelSurChangementToutePropriete(cle_observateur,rap);
        imp_elementComposite.ajouterRappelSurChangementToutePropriete(cle_observateur,rap);
    }
    public void retirerRappelSurChangementToutePropriete(Object cle_observateur) {
        super.retirerRappelSurChangementToutePropriete(cle_observateur);
        imp_elementComposite.retirerRappelSurChangementToutePropriete(cle_observateur);
    }

    public void ajouterRappelSurChangementTouteProprieteModifiantChemin(Object cle_observateur,RappelSurChangement rap) {
        super.ajouterRappelSurChangementTouteProprieteModifiantChemin(cle_observateur,rap);
        imp_elementComposite.ajouterRappelSurChangementTouteProprieteModifiantChemin(cle_observateur,rap);
    }
    public void retirerRappelSurChangementTouteProprieteModifiantChemin(Object cle_observateur) {
        super.retirerRappelSurChangementTouteProprieteModifiantChemin(cle_observateur);
        imp_elementComposite.retirerRappelSurChangementTouteProprieteModifiantChemin(cle_observateur);
    }

    public void ajouterListChangeListener(ListChangeListener<Obstacle> lcl_o) {imp_elementComposite.ajouterListChangeListener(lcl_o);}

    public void enleverListChangeListener(ListChangeListener<Obstacle> lcl_o) {imp_elementComposite.enleverListChangeListener(lcl_o);}

    public void enleverTousLesListChangeListeners() {imp_elementComposite.enleverTousLesListChangeListeners();}

    public int nombreObstaclesPremierNiveau() {return imp_elementComposite.nombreObstaclesPremierNiveau();}

    public Iterator<Obstacle> iterateurPremierNiveau() {return imp_elementComposite.iterateurPremierNiveau();}

    public Obstacle obstacle(int index_a_la_racine) {return imp_elementComposite.obstacle(index_a_la_racine); }
    public int indexObstacleALaRacine(Obstacle o) { return imp_elementComposite.index(o); }

    public void translater(Point2D vecteur) { imp_elementComposite.translater(vecteur);}

    public void translaterParCommande(Point2D vecteur) {imp_elementComposite.translaterParCommande(vecteur);}

    public boolean aSymetrieDeRevolution() { return imp_elementComposite.aSymetrieDeRevolution(); }

    public Point2D pointSurAxeRevolution() {return imp_elementComposite.pointSurAxeRevolution();}

    public boolean estOrientable() {return imp_elementComposite.estOrientable() ;}

    public void definirOrientation(double orientation_deg) {imp_elementComposite.definirOrientation(orientation_deg);}

    public void tournerAutourDe(Point2D centre_rot, double angle_rot_deg) {imp_elementComposite.tournerAutourDe(centre_rot,angle_rot_deg);}

    public double orientation() {return imp_elementComposite.orientation() ;}

//    public void definirAppartenanceSystemeOptiqueCentre(boolean b) {
//        super.definirAppartenanceSystemeOptiqueCentre(b);
//
//        imp_elementComposite.definirAppartenanceSystemeOptiqueCentre(b);
//    }
    public void definirSOCParent(SystemeOptiqueCentre soc) {
        super.definirSOCParent(soc);

        imp_elementComposite.definirSOCParent(soc) ;
    }

//    public void definirAppartenanceComposition(boolean b) {
//        super.definirAppartenanceComposition(b);
//
//        imp_elementComposite.definirAppartenanceComposition(b);
//    }

    public void convertirDistances(double facteur_conversion) { imp_elementComposite.convertirDistances(facteur_conversion);}

    public boolean estALaRacine(Obstacle o) {return imp_elementComposite.estALaRacine(o) ;}
    public void deplacerObstacleEnPositionALaRacine(Obstacle o_a_deplacer, int i_pos) {
        imp_elementComposite.deplacerObstacleEnPositionALaRacine(o_a_deplacer,i_pos);
    }

    public void appliquerSurElementComposite(ConsumerAvecException<Object, IOException> consumer) throws IOException {
        consumer.accept(imp_elementComposite);
    }

}
