package CrazyDiamond.Model;

import java.util.ArrayList;

public class CommandeSupprimerSystemeOptiqueCentre extends Commande {

    // Le récepteur de la commande
    Environnement environnement ;
    SystemeOptiqueCentre soc;

    ArrayList<Obstacle> obstacles_du_soc ;

    public CommandeSupprimerSystemeOptiqueCentre(Environnement env, SystemeOptiqueCentre soc_a_supprimer) {
        this.environnement = env ;
        this.soc = soc_a_supprimer ;
        this.obstacles_du_soc = new ArrayList<>(soc_a_supprimer.obstacles_centres().size()) ;
    }

    @Override
    public void executer() {
        obstacles_du_soc.addAll(soc.obstacles_centres()) ;
        environnement.supprimerSystemeOptiqueCentre(soc);  // Retire (détache) tous les obstacles du soc
        enregistrer();
    }

    @Override
    public void annuler() {
        obstacles_du_soc.forEach(soc::ajouterObstacle);
        environnement.ajouterSystemeOptiqueCentre(soc);
    }

    protected void convertirDistances(double facteur_conversion) {

        // Si le SOC fait partie de l'environnement, c'est ce dernier qui se charge d'en convertir les distances ;
        // sinon (suppression de l'obstacle a été rétablie), il faut le faire ici.
        if (!environnement.systemesOptiquesCentres().contains(soc))
            soc.convertirDistances(facteur_conversion);
    }

}
