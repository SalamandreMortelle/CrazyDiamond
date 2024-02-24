package CrazyDiamond.Model;

import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.List;

public class CommandeTranslaterElements extends Commande {

    // Le récepteur de la commande
    ArrayList<Source> sources ;
    ArrayList<Obstacle> obstacles ;
    ArrayList<SystemeOptiqueCentre> socs ;

    Environnement environnement ;

    // Paramètres de la commande
    Point2D vecteur;

    public CommandeTranslaterElements(Environnement env, Point2D vecteur, List<Source> sources,List<Obstacle> obstacles,List<SystemeOptiqueCentre> socs) {
        this.environnement = env ;
        this.vecteur = vecteur ;
        this.sources   = new ArrayList<>(sources.size()) ;
        this.obstacles = new ArrayList<>(obstacles.size()) ;
        this.socs      = new ArrayList<>(socs.size()) ;
        this.sources.addAll(sources);
        this.obstacles.addAll(obstacles);
        this.socs.addAll(socs);
    }

    @Override
    public void executer() {

        sources.forEach(source -> source.translater(vecteur));
        obstacles.forEach(obstacle -> translaterSiPossible(obstacle,vecteur));
        socs.forEach(soc -> soc.translater(vecteur));

        enregistrer();
    }

    private void translaterSiPossible(Obstacle o, Point2D tr) {
        if (!o.appartientASystemeOptiqueCentre())
            o.translater(tr);
        else {
            SystemeOptiqueCentre soc = environnement.systemeOptiqueCentreContenant(o);

            // Si l'obstacle fait partie d'un SOC à translater, ne pas le translater car c'est le SOC
            // qui va être translaté dans son ensemble.
            if (socs.contains(soc))
                return ;

            Point2D tr_sur_axe = soc.vecteurDirecteurAxe().multiply(soc.vecteurDirecteurAxe().dotProduct(tr));
            o.translater(tr_sur_axe);
        }

    }

    @Override
    public void annuler() {

        Point2D vecteur_opp = vecteur.multiply(-1d) ;

        sources.forEach(source -> source.translater(vecteur_opp));
        obstacles.forEach(obstacle -> translaterSiPossible(obstacle,vecteur_opp));
        socs.forEach(soc -> soc.translater(vecteur_opp));


    }

    @Override
    protected void convertirDistances(double facteur_conversion) {
        vecteur = vecteur.multiply(facteur_conversion) ;
    }
}
