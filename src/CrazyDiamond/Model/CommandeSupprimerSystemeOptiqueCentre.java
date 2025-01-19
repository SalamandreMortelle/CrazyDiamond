package CrazyDiamond.Model;

public class CommandeSupprimerSystemeOptiqueCentre extends Commande {

    // Le récepteur de la commande
    Environnement environnement ;
    SystemeOptiqueCentre soc;



    // INUTILE, car un SOC supprimé garde la liste de ses éléments :
//    ArrayList<ElementDeSOC> elements_du_soc;

    public CommandeSupprimerSystemeOptiqueCentre(Environnement env, SystemeOptiqueCentre soc_a_supprimer) {
        this.environnement = env ;
        this.soc = soc_a_supprimer ;
        // INUTILE, car un SOC supprimé garde la liste de ses éléments :
        // this.elements_du_soc = new ArrayList<>(soc_a_supprimer.elements_centres_premier_niveau().size()) ;
    }

    @Override
    public void executer() {
        // INUTILE, car un SOC supprimé garde la liste de ses éléments :
//        elements_du_soc.addAll(soc.elements_centres_premier_niveau()) ;
        environnement.retirerSystemeOptiqueCentre(soc);  // Retire (détache) tous les éléments du soc
        enregistrer();
    }

    @Override
    public void annuler() {
        // On replace les obstacles du SOC supprimé dans celui-ci :
        // INUTILE, car un SOC supprimé garde la liste de ses éléments
//        elements_du_soc.forEach(soc::ajouter);
        if (soc.SOCParent()==null)
            environnement.ajouterSystemeOptiqueCentre(soc);
        else // Même supprimé, un SOC conserve la référence de son SOC parent
            soc.SOCParent().ajouterSystemeOptiqueCentre(soc);
    }

    protected void convertirDistances(double facteur_conversion) {

        // Si le SOC fait partie de l'environnement (à n'importe quel niveau), c'est ce dernier qui se charge d'en
        // convertir les distances ; sinon (réf. du SOC maintenu en cas d'annulation de sa suppression), il faut le
        // faire ici.
        if (!environnement.systemesOptiquesCentres().contains(soc))
            soc.convertirDistances(facteur_conversion);
    }

}
