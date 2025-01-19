package CrazyDiamond.Model;

public class CommandeSupprimerObstacle extends Commande {

    // Le récepteur de la commande
    Environnement environnement ;
    Obstacle obstacle;

//    ArrayList<Obstacle> obstacles_du_soc ;

    boolean est_annulee = false;
    int index_dans_composite_parent = -1 ;
//    Composition composition_contenant_obstacle = null ;
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

        // TODO: pour l'instant, on ne gère que la suppression d'obstacles qui sont à la racine
        if (environnement.estALaRacine(obstacle)) {
            environnement.supprimerObstacleALaRacine(obstacle);
            // Si l'obstacle est à la racine de l'environnement, il peut faire partie d'un SOC ; il faut alors l'en
            // retirer : c'est l'intérêt de la méthode 'Environnement::supprimerObstacleALaRacine'
        } else {
//            throw new IllegalStateException("La suppression d'un obstacle qui appartient à un Groupe n'est pas gérée.");
            obstacle.parent().retirerObstacle(obstacle); // Retire l'obstacle de son Groupe ou de sa Composition d'appartenance
        }

        est_annulee = false ;
        enregistrer();
    }

    private void memoriserEtatInitial() {
//        index_dans_composite_parent = environnement.indexObstacleALaRacine(obstacle) ;
        index_dans_composite_parent = obstacle.parent().indexObstacleALaRacine(obstacle) ;
//        composition_contenant_obstacle = obstacle.appartientAComposition()?environnement.plusPetiteCompositionContenant(obstacle):null ;
        soc_contenant_obstacle = obstacle.SOCParent() ;
//        soc_contenant_obstacle = obstacle.appartientASystemeOptiqueCentre()?environnement.systemeOptiqueCentrePremierNiveauContenant(obstacle):null ;
    }

    @Override
    public void annuler() {
        // On commence par remettre l'obstacle dans son SOC d'origine, afin que l'appel à ajouterObstacleEnPosition qui
        // suit se charge de le repositionner à sa bonne place dans le SOC
        if (soc_contenant_obstacle!=null)
            soc_contenant_obstacle.ajouterObstacleCentre(obstacle);

//        if (obstacle.SOCParent()!=null)
//            obstacle.SOCParent().ajouterObstacleCentre(obstacle);

        obstacle.parent().ajouterObstacleEnPosition(obstacle,index_dans_composite_parent);

//        environnement.ajouterObstacleEnPositionALaRacine(obstacle, index_dans_composite_parent);
//        if (composition_contenant_obstacle!=null)
//            composition_contenant_obstacle.ajouterObstacle(obstacle);

        est_annulee = true ;
    }

    protected void convertirDistances(double facteur_conversion) {
        // Si l'obstacle fait partie de l'environnement, c'est ce dernier qui se charge d'en convertir les coordonnées ;
        // sinon (suppression de l'obstacle a été rétablie), il faut le faire ici.
//        if (!environnement.obstaclesComprennent(obstacle) && !obstacle.appartientAComposition())

        // Si la commande n'est pas annulée, c'est que les obstacles ne font plus partie de l'environnement (ils n'y
        // sont plus référencés) : on doit donc se charger de faire la conversion.
        if (!est_annulee)
            obstacle.convertirDistances(facteur_conversion);
    }

}
