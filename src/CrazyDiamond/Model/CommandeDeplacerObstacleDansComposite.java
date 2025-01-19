package CrazyDiamond.Model;

public class CommandeDeplacerObstacleDansComposite extends Commande {

    // Le récepteur de la commande
    Environnement environnement ;

    // Parametres
    Obstacle obstacle;

    BaseObstacleComposite composite_cible ;
    int position_cible_dans_composite_cible;

    // Etat initial
    BaseObstacleComposite composite_initial;
    int position_dans_composite_initial;

    // Eventuel SOC auquel l'obstacle appartient initialement
    SystemeOptiqueCentre soc_initial ;

    public CommandeDeplacerObstacleDansComposite(Environnement environnement, Obstacle obs_a_deplacer, Obstacle composite_cible, int pos_cible_dans_composite) {

        if (! (composite_cible instanceof BaseObstacleComposite boc_cible))
            throw new IllegalArgumentException("CommandeDeplacerObstacleDansComposite : l'obstacle parent cible passé en paramètre doit-être un Composite") ;

        this.environnement = environnement ;
        this.obstacle = obs_a_deplacer ;
        this.composite_cible =  boc_cible ;
        this.position_cible_dans_composite_cible = pos_cible_dans_composite ;

        System.out.println("Déplacement de l'obstacle "+obstacle+" vers position "+position_cible_dans_composite_cible+" sous le composite "+composite_cible) ;

        // Dé-commenter la ligne suivante si besoin d'enregistrer cette commande directement sans l'exécuter
//        memoriserEtatInitial();
    }

    private void memoriserEtatInitial() {

//        this.groupe = environnement.groupeContenant(obstacle) ;
        this.composite_initial = obstacle.parent() ;
        this.position_dans_composite_initial = composite_initial.indexObstacleALaRacine(obstacle) ;

        this.soc_initial = obstacle.SOCParent() ;
//        this.soc_initial = ( environnement.systemeOptiqueCentreReferencant(obstacle) ) ;

    }

    @Override
    public void executer() {

        memoriserEtatInitial();

        // On le retire de son composite d'appartenance
        composite_initial.retirerObstacle(obstacle);

        if (soc_initial!=null) {
//            obstacle.definirAppartenanceSystemeOptiqueCentre(false);
            soc_initial.retirer(obstacle);
        } else
            obstacle.definirSOCParent(null); // Même si l'obstacle n'est pas directement référencé par un SOC (soc_initial==null),
                                             // son composite d'appartenance en faisait peut-être partie auquel cas, il faut
                                             // malgré tout indiquer qu'il n'appartient plus à un SOC.
//            obstacle.definirAppartenanceSystemeOptiqueCentre(false);


        if (composite_cible.appartientASystemeOptiqueCentre())
            obstacle.definirSOCParent(composite_cible.SOCParent());
//            obstacle.definirAppartenanceSystemeOptiqueCentre(true);

        // On le positionne dans l'environnement, à la position souhaitée
        composite_cible.ajouterObstacleEnPosition(obstacle, position_cible_dans_composite_cible);

        if (soc_initial!=null) {
//            obstacle.definirAppartenanceSystemeOptiqueCentre(false);
            soc_initial.ajouterObstacleCentre(obstacle); // Repositionne l'obstacle dans le SOC en tenant compte de sa nouvelle position (Z order) dans l'Environnement
        }

        enregistrer();
    }

    @Override
    public void annuler() {

        environnement.supprimerObstacle(obstacle);

        composite_initial.ajouterObstacleEnPosition(obstacle, position_dans_composite_initial);

        // On remet l'obstacle dans son soc initial
        if (soc_initial!=null)
            soc_initial.ajouterObstacleCentre(obstacle);

        // TODO : gérer ce repositionnement de l'obstacle dans son éventuel soc d'appartenance
//        environnement.repositionnerObstacleDansSoc(o_a_ajouter, i_pos_dans_env);
    }

}
