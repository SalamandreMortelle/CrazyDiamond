package CrazyDiamond.Model;

public class CommandeSupprimerSource extends Commande {

    // Le récepteur de la commande
    Environnement environnement ;
    Source source;

//    ArrayList<Obstacle> obstacles_du_soc ;

    public CommandeSupprimerSource(Environnement env, Source source_a_supprimer) {
        this.environnement = env ;
        this.source = source_a_supprimer ;
//        this.obstacles_du_soc = new ArrayList<>(source_a_supprimer.obstacles_centres().size()) ;
    }

    @Override
    public void executer() {
//        obstacles_du_soc.addAll(source.obstacles_centres()) ;
//        environnement.supprimerSystemeOptiqueCentre(source);  // Retire (détache) tous les obstacles du soc
        environnement.supprimerSource(source);
        enregistrer();
    }

    @Override
    public void annuler() {
//        obstacles_du_soc.forEach(source::ajouterObstacle);
//        environnement.ajouterSystemeOptiqueCentre(source);
        environnement.ajouterSource(source);
    }

    protected void convertirDistances(double facteur_conversion) {

        // Si la source fait partie de l'environnement, c'est ce dernier qui se charge d'en convertir les coordonnées ;
        // sinon (suppression de la source a été rétablie), il faut le faire ici.
        if (!environnement.sources().contains(source))
            source.convertirDistances(facteur_conversion);
    }

}
