package CrazyDiamond.Model;

public class CommandeCreerSystemeOptiqueCentreVide extends Commande {

    // Le récepteur de la commande
    Environnement environnement ;
    SystemeOptiqueCentre soc_cree;


    public CommandeCreerSystemeOptiqueCentreVide(Environnement env, SystemeOptiqueCentre soc_deja_cree) {
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
        environnement.retirerSystemeOptiqueCentre(soc_cree);
    }

    protected void convertirDistances(double facteur_conversion) {

        // Si le SOC fait partie de l'environnement, c'est ce dernier qui se charge d'en convertir les distances ;
        // sinon (création du SOC a été annulée), il faut le faire ici.
        // NB : il suffit de chercher parmi les SOC de 1er niveau, car un soc nouvellement créé l'est toujours au 1er niveau
        // de l'environnement
        if (!environnement.systemesOptiquesCentresPremierNiveau().contains(soc_cree))
            soc_cree.convertirDistances(facteur_conversion);
    }

}
