package CrazyDiamond.Model;

public class CommandeDeplacerObstacleDeCompositionDansEnvironnement extends Commande {

    // Le récepteur de la commande
    Environnement environnement ;


    // Parametres
    Obstacle obstacle;

    int position ;

    // Etat initial
    Composition composition;


    public CommandeDeplacerObstacleDeCompositionDansEnvironnement(Environnement environnement, Obstacle obs_a_deplacer,int pos_cible_dans_env) {
        this.environnement = environnement ;
        this.obstacle = obs_a_deplacer ;
        this.position = pos_cible_dans_env ;

        // Dé-commenter la ligne suivante si besoin d'enregistrer cette commande directement sans l'exécuter
//        memoriserEtatInitial();
    }

    private void memoriserEtatInitial() {
        this.composition = environnement.compositionContenant(obstacle) ;
    }

    @Override
    public void executer() {

        memoriserEtatInitial();

        // On le retire de sa composition d'appartenance
        composition.retirerObstacle(obstacle);

        // On le positionne dans l'environnement, à la position souhaitée
        environnement.ajouterObstacleEnPosition(obstacle,position);

        enregistrer();
    }

    @Override
    public void annuler() {

        environnement.supprimerObstacle(obstacle);

        composition.ajouterObstacle(obstacle);

    }

}
