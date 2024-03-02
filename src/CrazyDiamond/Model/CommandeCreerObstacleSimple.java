package CrazyDiamond.Model;

public class CommandeCreerObstacleSimple extends Commande {

    // Le récepteur de la commande
    Environnement environnement ;
    Obstacle obstacle_cree;


    public CommandeCreerObstacleSimple(Environnement env, Obstacle obstacle_deja_cree) {
        this.environnement = env ;
        this.obstacle_cree = obstacle_deja_cree ;
    }

    @Override
    public void executer() {
        environnement.ajouterObstacle(obstacle_cree);
        enregistrer();
    }

    @Override
    public void annuler() {
        environnement.supprimerObstacle(obstacle_cree);
    }

    protected void convertirDistances(double facteur_conversion) {

        // Si l'obstacle fait partie de l'environnement, c'est ce dernier qui se charge d'en convertir les distances ;
        // sinon (création de l'obstacle a été annulée), il faut le faire ici.
        if (!environnement.obstacles().contains(obstacle_cree))
            obstacle_cree.convertirDistances(facteur_conversion);
    }

}
