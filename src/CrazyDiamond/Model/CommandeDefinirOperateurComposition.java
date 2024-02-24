package CrazyDiamond.Model;

public class CommandeDefinirOperateurComposition extends Commande {

    // Le récepteur de la commande
    Composition composition ;

    // Paramètre de la commande
    Composition.Operateur operateur ;

    // Données en cas d'annulation
    Composition.Operateur precedent_operateur ;
    public CommandeDefinirOperateurComposition(Composition c, Composition.Operateur op) {
        this.composition = c ;
        this.operateur = op ;
    }


    @Override
    public void executer() {
        Composition.Operateur nouveau_precedent_operateur = composition.operateur() ;

        if (operateur == nouveau_precedent_operateur)
            return;

        precedent_operateur = nouveau_precedent_operateur ;

        composition.definirOperateur(operateur);

        enregistrer();
    }

    @Override
    public void annuler() {
        composition.definirOperateur(precedent_operateur);
    }

    @Override
    protected void convertirDistances(double facteur_conversion) { }

}
