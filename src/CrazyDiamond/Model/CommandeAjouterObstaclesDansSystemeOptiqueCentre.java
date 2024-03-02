package CrazyDiamond.Model;

import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.List;

public class CommandeAjouterObstaclesDansSystemeOptiqueCentre extends Commande {

    // Le récepteur de la commande
    SystemeOptiqueCentre soc;

    // Paramètre
    List<Obstacle> obstacles;

    // Informations pour l'annulation
    ArrayList<Point2D> translations;
    ArrayList<Double> rotations;


    public CommandeAjouterObstaclesDansSystemeOptiqueCentre(SystemeOptiqueCentre soc, Obstacle obs_a_ajouter) {
        this.obstacles = new ArrayList<>(1) ;
        this.obstacles.add(obs_a_ajouter) ;
        initialiser(soc);
    }
    public CommandeAjouterObstaclesDansSystemeOptiqueCentre(SystemeOptiqueCentre soc, List<Obstacle> obs_a_ajouter) {
        this.obstacles = new ArrayList<>(obs_a_ajouter.size()) ;
        this.obstacles.addAll(obs_a_ajouter) ;
        initialiser(soc);
    }
    private void initialiser(SystemeOptiqueCentre soc) {
        this.soc = soc ;
        translations = new ArrayList<>(obstacles.size()) ;
        rotations = new ArrayList<>(obstacles.size()) ;

        obstacles.forEach(o -> {
            translations.add(soc.translationPourAjoutObstacle(o));
            rotations.add(soc.angleRotationPourAjoutObstacle(o));
        });

    }

    @Override
    public void executer() {
        obstacles.forEach(soc::ajouterObstacle) ;

        enregistrer();
    }

    @Override
    public void annuler() {
        Obstacle o ;
        for (int i = 0 ; i < obstacles.size() ; ++i) {
            o = obstacles.get(i);
            soc.retirerObstacleCentre(o);
            o.tournerAutourDe(o.pointSurAxeRevolution(), -rotations.get(i));
            o.translater(translations.get(i).multiply(-1d));
        }
    }

    protected void convertirDistances(double facteur_conversion) {
        translations.forEach(t -> t.multiply(facteur_conversion));
    }

}
