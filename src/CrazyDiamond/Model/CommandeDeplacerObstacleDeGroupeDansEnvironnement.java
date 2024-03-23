package CrazyDiamond.Model;

public class CommandeDeplacerObstacleDeGroupeDansEnvironnement extends Commande {

    // Le récepteur de la commande
    Environnement environnement ;


    // Parametres
    Obstacle obstacle;

    int position_cible_dans_env;

    // Etat initial
    Groupe groupe;
    int position_depart_dans_groupe;


    public CommandeDeplacerObstacleDeGroupeDansEnvironnement(Environnement environnement, Obstacle obs_a_deplacer, int pos_cible_dans_env) {
        this.environnement = environnement ;
        this.obstacle = obs_a_deplacer ;
        this.position_cible_dans_env = pos_cible_dans_env ;

        // Dé-commenter la ligne suivante si besoin d'enregistrer cette commande directement sans l'exécuter
//        memoriserEtatInitial();
    }

    private void memoriserEtatInitial() {

        this.groupe = environnement.groupeContenant(obstacle) ;
        this.position_depart_dans_groupe = groupe.indexObstacleALaRacine(obstacle) ;

    }

    @Override
    public void executer() {

        memoriserEtatInitial();

        // On le retire de sa composition d'appartenance
        groupe.retirerObstacle(obstacle);

        // On le positionne dans l'environnement, à la position souhaitée
        environnement.ajouterObstacleEnPositionALaRacine(obstacle, position_cible_dans_env);

        enregistrer();
    }

    @Override
    public void annuler() {

        environnement.supprimerObstacleALaRacine(obstacle);

//        groupe.ajouterObstacle(obstacle);
        groupe.ajouterObstacleEnPosition(obstacle,position_depart_dans_groupe);

        // TODO : gérer ce repositionnement de l'obstacle dans son éventuel soc d'appartenance
//        environnement.repositionnerObstacleDansSoc(o_a_ajouter, i_pos_dans_env);
    }

}
