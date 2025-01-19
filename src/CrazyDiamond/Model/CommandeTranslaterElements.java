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
        socs.forEach(soc -> translaterSiPossible(soc,vecteur));

        enregistrer();
    }

    private void translaterSiPossible(ElementDeSOC el, Point2D tr) {
        // Si l'un des SOC ancêtres fait partie des SOCs à translater, ne pas le translater, car c'est ce SOC
        // qui va être translaté dans son ensemble.
        SystemeOptiqueCentre soc_pere = el.SOCParent() ;

        if (soc_pere==null) {
            el.translater(tr);
            return;
        }

        for (SystemeOptiqueCentre soc_ancetre = soc_pere ; soc_ancetre!=null ; soc_ancetre = soc_ancetre.SOCParent())
            if (socs.contains(soc_ancetre))
                return ;


        Point2D tr_sur_axe = soc_pere.vecteurDirecteurAxe().multiply(soc_pere.vecteurDirecteurAxe().dotProduct(tr));
        el.translater(tr_sur_axe);
    }

//    private void translaterSiPossible(SystemeOptiqueCentre soc, Point2D tr) {
//        // Si l'un des SOC ancêtres fait partie des SOCs à translater, ne pas translater, car c'est ce SOC
//        // qui va être translaté dans son ensemble.
//
//        SystemeOptiqueCentre soc_pere = soc.SOCParent() ;
//
//        if (soc_pere==null)
//            soc.translater(tr);
//
//        for (SystemeOptiqueCentre soc_ancetre = soc_pere ; soc_ancetre!=null ; soc_ancetre = soc_ancetre.SOCParent())
//            if (socs.contains(soc_ancetre))
//                return ;
//
//        Point2D tr_sur_axe = soc_pere.vecteurDirecteurAxe().multiply(soc_pere.vecteurDirecteurAxe().dotProduct(tr));
//        soc.translater(tr_sur_axe);
//
//    }

    @Override
    public void annuler() {

        Point2D vecteur_opp = vecteur.multiply(-1d) ;

        sources.forEach(source -> source.translater(vecteur_opp));
        obstacles.forEach(obstacle -> translaterSiPossible(obstacle,vecteur_opp));
//        socs.forEach(soc -> soc.translater(vecteur_opp));
        socs.forEach(soc -> translaterSiPossible(soc,vecteur_opp));


    }



    @Override
    protected void convertirDistances(double facteur_conversion) {
        vecteur = vecteur.multiply(facteur_conversion) ;
    }
}
