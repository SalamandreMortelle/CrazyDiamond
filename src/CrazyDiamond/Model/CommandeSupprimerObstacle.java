package CrazyDiamond.Model;

public class CommandeSupprimerObstacle extends Commande {

    // Le récepteur de la commande
    Environnement environnement ;
    Obstacle obstacle;

//    ArrayList<Obstacle> obstacles_du_soc ;

    int index_dans_environnement = -1 ;
    Composition composition_contenant_obstacle = null ;
    SystemeOptiqueCentre soc_contenant_obstacle = null ;

    public CommandeSupprimerObstacle(Environnement env, Obstacle obstacle_a_supprimer) {
        this.environnement = env ;
        this.obstacle = obstacle_a_supprimer ;
        // Dé-commenter la ligne suivante si besoin d'enregistrer cette commande directement sans l'exécuter
//        memoriserEtatInitial();
    }

    @Override
    public void executer() {

        memoriserEtatInitial();

        environnement.supprimerObstacleALaRacine(obstacle);

        enregistrer();
    }

    private void memoriserEtatInitial() {
        index_dans_environnement = environnement.indexObstacleALaRacine(obstacle) ;
        composition_contenant_obstacle = obstacle.appartientAComposition()?environnement.compositionContenant(obstacle):null ;
        soc_contenant_obstacle = obstacle.appartientASystemeOptiqueCentre()?environnement.systemeOptiqueCentreContenant(obstacle):null ;
    }

    @Override
    public void annuler() {
        // On commence par remettre l'obstacle dans son SOC d'origine, afin que l'appel à ajouterObstacleEnPositionALaRacine qui suit
        // se charge de le repositionner à sa bonne place dans le SOC
        if (soc_contenant_obstacle!=null)
            soc_contenant_obstacle.ajouterObstacle(obstacle);
        environnement.ajouterObstacleEnPositionALaRacine(obstacle,index_dans_environnement);
        if (composition_contenant_obstacle!=null)
            composition_contenant_obstacle.ajouterObstacle(obstacle);
    }

    protected void convertirDistances(double facteur_conversion) {
        // Si l'obstacle fait partie de l'environnement, c'est ce dernier qui se charge d'en convertir les coordonnées ;
        // sinon (suppression de l'obstacle a été rétablie), il faut le faire ici.
        if (!environnement.obstaclesComprennent(obstacle) && !obstacle.appartientAComposition())
            obstacle.convertirDistances(facteur_conversion);
    }

}
