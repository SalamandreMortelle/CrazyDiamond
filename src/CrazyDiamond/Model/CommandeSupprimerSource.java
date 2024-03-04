package CrazyDiamond.Model;

public class CommandeSupprimerSource extends Commande {

    // Le récepteur de la commande
    Environnement environnement ;
    Source source;

    public CommandeSupprimerSource(Environnement env, Source source_a_supprimer) {
        this.environnement = env ;
        this.source = source_a_supprimer ;
    }

    @Override
    public void executer() {
        environnement.supprimerSource(source);
        enregistrer();
    }

    @Override
    public void annuler() {
        environnement.ajouterSource(source);
    }

    protected void convertirDistances(double facteur_conversion) {
        // Si la source fait partie de l'environnement, c'est ce dernier qui se charge d'en convertir les coordonnées ;
        // sinon (suppression de la source a été rétablie), il faut le faire ici.
        if (!environnement.sources().contains(source))
            source.convertirDistances(facteur_conversion);
    }

}
