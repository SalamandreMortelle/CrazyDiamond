package CrazyDiamond.Model;

import java.util.ArrayList;

public class CommandeCreerComposition extends Commande {

    // Le récepteur de la commande
    Environnement environnement ;
    Composition composition_cree;

    // Paramètres de la commande
    Composition.Operateur operateur ;
    ArrayList<Obstacle> composants ;

    public CommandeCreerComposition(Environnement env, Composition.Operateur op) {
        this.environnement = env ;
        this.operateur = op ;
        this.composants = new ArrayList<>(2) ;
        composition_cree = new Composition(op) ;
    }

    public CommandeCreerComposition(Environnement env, Composition composition_deja_cree) {
        this.environnement = env ;
        this.operateur = composition_deja_cree.operateur() ;
        this.composants = new ArrayList<>(composition_deja_cree.elements().size()) ;
        composition_deja_cree.elements().forEach(composants::add);
        this.composition_cree = composition_deja_cree ;
    }

    @Override
    public void executer() {
        composants.forEach(environnement::retirerObstacle);
        composants.forEach(composition_cree::ajouterObstacle);
        environnement.ajouterObstacle(composition_cree);
        enregistrer();
    }

    @Override
    public void annuler() {
        composants.forEach(composition_cree::retirerObstacle);
        composants.forEach(environnement::ajouterObstacle);
        environnement.retirerObstacle(composition_cree);
    }

}
