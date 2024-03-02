package CrazyDiamond.Model;

public class CommandeAjouterObstacleDansComposition extends Commande {

    // Le récepteur de la commande
    Environnement environnement ;
    Composition composition;

    // Paramètre
    Obstacle obstacle;

    // Etat initial
    int index_dans_environnement = -1 ;
    Composition composition_contenant_obstacle = null ;
    SystemeOptiqueCentre soc_contenant_obstacle = null ;

    public CommandeAjouterObstacleDansComposition(Environnement environnement, Composition composition, Obstacle obs_a_ajouter) {
        this.environnement = environnement ;
        this.composition = composition ;
        this.obstacle = obs_a_ajouter ;
    }

    @Override
    public void executer() {
        composition_contenant_obstacle = environnement.compositionContenant(obstacle) ;
        soc_contenant_obstacle = environnement.systemeOptiqueCentreContenant(obstacle) ;

        if (composition_contenant_obstacle == null) {// Obstacle glissé non inclus dans une composition
            index_dans_environnement = environnement.indexObstacle(obstacle) ;
            environnement.supprimerObstacle(obstacle); // On le retire de l'environnement
        }
        else
            environnement.compositionContenant(obstacle).retirerObstacle(obstacle); // On le retire de la composition dont il fait partie

        composition.ajouterObstacle(obstacle);

        enregistrer();
    }

    @Override
    public void annuler() {

        composition.retirerObstacle(obstacle);

        // On commence par remettre l'obstacle dans son SOC d'origine, afin que l'appel à ajouterObstacleEnPosition qui suit
        // se charge de le repositionner à sa bonne place dans le SOC
        if (soc_contenant_obstacle!=null)
            soc_contenant_obstacle.ajouterObstacle(obstacle);
        environnement.ajouterObstacleEnPosition(obstacle,index_dans_environnement);
        if (composition_contenant_obstacle!=null)
            composition_contenant_obstacle.ajouterObstacle(obstacle);

    }

    protected void convertirDistances(double facteur_conversion) {
        // Si l'obstacle fait partie de l'environnement, c'est ce dernier qui se charge d'en convertir les coordonnées ;
        // sinon (ajout de l'obstacle a été annulé), il faut le faire ici.
        if (!environnement.obstacles().contains(obstacle) && !obstacle.appartientAComposition())
            obstacle.convertirDistances(facteur_conversion);
    }

}
