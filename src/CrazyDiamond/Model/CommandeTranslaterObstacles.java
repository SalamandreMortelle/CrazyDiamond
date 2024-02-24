package CrazyDiamond.Model;

import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.List;

public class CommandeTranslaterObstacles extends Commande {

    // Le récepteur de la commande
    ArrayList<Obstacle> obstacles ;

    // Paramètres de la commande
    Point2D vecteur;

    public CommandeTranslaterObstacles(Point2D vecteur, List<Obstacle> obstacles) {
        this.vecteur = vecteur ;
        this.obstacles = new ArrayList<>(obstacles.size()) ;
        this.obstacles.addAll(obstacles);
    }

    @Override
    public void executer() {

        obstacles.forEach(obstacle -> obstacle.translater(vecteur));

        enregistrer();
    }


    @Override
    public void annuler() {

        Point2D vecteur_opp = vecteur.multiply(-1d) ;

        obstacles.forEach(obstacle -> obstacle.translater(vecteur_opp));

    }

    @Override
    protected void convertirDistances(double facteur_conversion) {
        vecteur = vecteur.multiply(facteur_conversion) ;
    }
}
