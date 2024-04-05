package CrazyDiamond.Model;

import java.util.ArrayList;
import java.util.List;

public class CommandeRetirerObstaclesDeSystemeOptiqueCentre extends Commande {

    // Le récepteur de la commande
    SystemeOptiqueCentre soc;

    // Parametre
    List<Obstacle> obstacles;

    public CommandeRetirerObstaclesDeSystemeOptiqueCentre(SystemeOptiqueCentre soc, Obstacle obs_a_retirer) {
        this.soc = soc ;
        this.obstacles = new ArrayList<>(1) ;
        this.obstacles.add(obs_a_retirer) ;

    }
    public CommandeRetirerObstaclesDeSystemeOptiqueCentre(SystemeOptiqueCentre soc, List<Obstacle> obs_a_retirer) {
        this.soc = soc ;
        this.obstacles = new ArrayList<>(obs_a_retirer.size()) ;
        this.obstacles.addAll(obs_a_retirer) ;
    }

    @Override
    public void executer() {
        obstacles.forEach(soc::retirerObstacleCentre) ;

        enregistrer();
    }

    @Override
    public void annuler() {
        obstacles.forEach(soc::ajouterObstacleCentre);
    }

}
