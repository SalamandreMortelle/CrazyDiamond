package CrazyDiamond.Model;

/**
 * Cette commande permet de déplacer à la racine de l'environnement un soc qui fait partie d'un SOC conteneur
 */
public class CommandeDeplacerSystemeOptiqueCentreDansRacineEnvironnement extends Commande {

    // Le récepteur de la commande
    SystemeOptiqueCentre soc;
    Environnement environnement ;


    // Informations pour l'annulation : état initial

    SystemeOptiqueCentre soc_parent_origine;

    public CommandeDeplacerSystemeOptiqueCentreDansRacineEnvironnement(Environnement env, SystemeOptiqueCentre soc_a_deplacer) {

        if (soc_a_deplacer.SOCParent()==null)
            throw new IllegalStateException("On ne peut pas déplacer à la racine de l'environnement un soc qui s'y trouve déjà.") ;

        this.environnement = env ;
        initialiser(soc_a_deplacer);
    }
    private void initialiser(SystemeOptiqueCentre soc_a_deplacer) {
        this.soc = soc_a_deplacer ;
        this.soc_parent_origine = soc_a_deplacer.SOCParent() ;
    }

    @Override
    public void executer() {

        soc.SOCParent().retirer(soc);

        environnement.ajouterSystemeOptiqueCentre(soc);

        enregistrer();
    }

    @Override
    public void annuler() {

        environnement.retirerSystemeOptiqueCentre(soc);

        soc_parent_origine.ajouterSystemeOptiqueCentre(soc);

    }

}
