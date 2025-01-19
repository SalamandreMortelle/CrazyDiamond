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

    Groupe groupe_contenant_obstacle = null ;
    SystemeOptiqueCentre soc_contenant_obstacle = null ;

    public CommandeAjouterObstacleDansComposition(Environnement environnement, Composition composition, Obstacle obs_a_ajouter) {
        this.environnement = environnement ;
        this.composition = composition ;
        this.obstacle = obs_a_ajouter ;
    }

    @Override
    public void executer() {
        composition_contenant_obstacle = environnement.plusPetiteCompositionContenant(obstacle) ;
        // Inutile de chercher le groupe qui contient l'obstacle s'il fait partie d'une composition
        groupe_contenant_obstacle = (composition_contenant_obstacle==null?environnement.groupeContenant(obstacle):null) ;
        soc_contenant_obstacle = obstacle.SOCParent() ;
//        soc_contenant_obstacle = environnement.systemeOptiqueCentrePremierNiveauContenant(obstacle) ;

        if (groupe_contenant_obstacle == null && composition_contenant_obstacle == null) {// Obstacle non inclus dans un groupe ; il est donc directement à la racine de l'environnement
            index_dans_environnement = environnement.indexObstacleALaRacine(obstacle) ;
            environnement.supprimerObstacleALaRacine(obstacle); // On le retire de l'environnement
        }
        else {
            if (composition_contenant_obstacle != null) // On vérifie d'abord que l'objet est dans une composition, car dans ce cas, il ne peut pas être directement dans un Groupe
                composition_contenant_obstacle.retirerObstacle(obstacle); // On le retire de la composition dont il fait partie
            else
                groupe_contenant_obstacle.retirerObstacle(obstacle); // On le retire du groupe dont il fait partie
        }

        composition.ajouterObstacle(obstacle);

        enregistrer();
    }

    @Override
    public void annuler() {

        composition.retirerObstacle(obstacle);

        // On commence par remettre l'obstacle dans son SOC d'origine, afin que l'appel à ajouterObstacleEnPositionALaRacine qui suit
        // se charge de le repositionner à sa bonne place dans le SOC
        if (soc_contenant_obstacle!=null)
            soc_contenant_obstacle.ajouterObstacleCentre(obstacle);
        environnement.ajouterObstacleEnPositionALaRacine(obstacle,index_dans_environnement);

        if (composition_contenant_obstacle!=null)
            composition_contenant_obstacle.ajouterObstacle(obstacle);
        else if (groupe_contenant_obstacle !=null)
            groupe_contenant_obstacle.ajouterObstacle(obstacle);

    }

    protected void convertirDistances(double facteur_conversion) {
        // Si l'obstacle fait partie de l'environnement, c'est ce dernier qui se charge d'en convertir les coordonnées ;
        // sinon (ajout de l'obstacle a été annulé), il faut le faire ici.
//        if (!environnement.obstacles().contains(obstacle) && !obstacle.appartientAComposition())
        if (!environnement.obstaclesReelsComprennent(obstacle) && !obstacle.appartientAComposition() && !obstacle.appartientAGroupe())
            obstacle.convertirDistances(facteur_conversion);
    }

}
