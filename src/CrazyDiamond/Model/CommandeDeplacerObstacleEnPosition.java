package CrazyDiamond.Model;

public class CommandeDeplacerObstacleEnPosition extends Commande {

    // Le récepteur de la commande
    Environnement environnement ;


    // Parametres
    Obstacle obstacle;

    int position_cible;

    // Etat initial
    int position_initiale ;

    public CommandeDeplacerObstacleEnPosition(Environnement environnement, Obstacle obs_a_deplacer, int pos_cible_dans_env) {
        this.environnement = environnement ;
        this.obstacle = obs_a_deplacer ;
        this.position_cible = pos_cible_dans_env ;

        // Dé-commenter la ligne suivante si besoin d'enregistrer cette commande directement sans l'exécuter
        // memoriserEtatInitial();

    }

    private void memoriserEtatInitial() {
        this.position_initiale = environnement.indexObstacleALaRacine(obstacle) ;
    }

    @Override
    public void executer() {
        memoriserEtatInitial();

        // NB : cette méthode se charge aussi de repositionner l'obstacle correctement à l'intérieur de son éventuel soc
        // d'appartenance.
        environnement.deplacerObstacleEnPositionALaRacine(obstacle, position_cible) ;

        enregistrer();
    }

    @Override
    public void annuler() {
        environnement.deplacerObstacleEnPositionALaRacine(obstacle, position_initiale) ;
    }

}
