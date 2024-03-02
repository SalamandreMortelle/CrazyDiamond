package CrazyDiamond.Model;

public class CommandeCreerSystemeOptiqueCentre extends Commande {

    // Le récepteur de la commande
    Environnement environnement ;
    SystemeOptiqueCentre soc_cree;


    public CommandeCreerSystemeOptiqueCentre(Environnement env, SystemeOptiqueCentre soc_deja_cree) {
        this.environnement = env ;
        this.soc_cree = soc_deja_cree ;
    }

    @Override
    public void executer() {
        environnement.ajouterSystemeOptiqueCentre(soc_cree);
        enregistrer();
    }

    @Override
    public void annuler() {
        environnement.supprimerSystemeOptiqueCentre(soc_cree);
    }

    protected void convertirDistances(double facteur_conversion) {

        // Si le SOC fait partie de l'environnement, c'est ce dernier qui se charge d'en convertir les distances ;
        // sinon (création de l'obstacle a été annulée), il faut le faire ici.
        if (!environnement.systemesOptiquesCentres().contains(soc_cree))
            soc_cree.convertirDistances(facteur_conversion);
    }

}
