package CrazyDiamond.Model;

public class CommandeCreerSource extends Commande {

    // Le récepteur de la commande
    Environnement environnement ;
    Source source_cree;


    public CommandeCreerSource(Environnement env, Source source_deja_cree) {
        this.environnement = env ;
        this.source_cree = source_deja_cree ;
    }

    @Override
    public void executer() {
        environnement.ajouterSource(source_cree);
        enregistrer();
    }

    @Override
    public void annuler() {
        environnement.supprimerSource(source_cree);
    }

    protected void convertirDistances(double facteur_conversion) {

        // Si la source fait partie de l'environnement, c'est ce dernier qui se charge d'en convertir les distances ;
        // sinon (création de l'obstacle a été annulée), il faut le faire ici.
        if (!environnement.sources().contains(source_cree))
            source_cree.convertirDistances(facteur_conversion);
    }

}
