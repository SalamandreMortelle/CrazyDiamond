package CrazyDiamond.Model;

import java.util.ArrayList;

public class CommandeCreerGroupe extends Commande {

    // Le récepteur de la commande
    Environnement environnement ;
    Groupe groupe_cree;

    // Paramètres de la commande
    ArrayList<Obstacle> elements;

    public CommandeCreerGroupe(Environnement env) {
        this.environnement = env ;
        this.elements = new ArrayList<>(2) ;
        groupe_cree = new Groupe() ;
    }

    public CommandeCreerGroupe(Environnement env, Groupe groupe_deja_cree) {
        this.environnement = env ;
        this.elements = new ArrayList<>(groupe_deja_cree.elements().size()) ;
        this.elements.addAll(groupe_deja_cree.elements());
        this.groupe_cree = groupe_deja_cree ;
    }

    @Override
    public void executer() {
        elements.forEach(environnement::supprimerObstacleALaRacine);
        elements.forEach(groupe_cree::ajouterObstacle);
        environnement.ajouterObstacleALaRacine(groupe_cree);
        enregistrer();
    }

    @Override
    public void annuler() {
        elements.forEach(groupe_cree::retirerObstacle);
        elements.forEach(environnement::ajouterObstacleALaRacine);
        environnement.supprimerObstacleALaRacine(groupe_cree);
    }

}
