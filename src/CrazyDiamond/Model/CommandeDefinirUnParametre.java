package CrazyDiamond.Model;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class CommandeDefinirUnParametre<R,P> extends Commande {

    // Le récepteur de la commande
    R recepteur ;

    // Paramètre de la commande
    P valeur;

    // Données en cas d'annulation
    P precedente_valeur ;

    Supplier<P> methode_pour_lire_parametre ;
    Consumer<P> methode_pour_definir_parametre ;

    public CommandeDefinirUnParametre(R recepteur, P valeur, Supplier<P> methode_pour_lire_parametre, Consumer<P> methode_pour_definir_parametre) {
        this.recepteur = recepteur ;
        this.valeur = valeur ;
        this.methode_pour_lire_parametre = methode_pour_lire_parametre ;
        this.methode_pour_definir_parametre = methode_pour_definir_parametre ;
    }

    @Override
    public void executer() {
        P nouveau_precedente_valeur = methode_pour_lire_parametre.get() ;

        if (valeur.equals(nouveau_precedente_valeur))
            return;

        precedente_valeur = nouveau_precedente_valeur ;

        methode_pour_definir_parametre.accept(valeur);

        enregistrer();
    }

    @Override
    public void annuler() { methode_pour_definir_parametre.accept(precedente_valeur); }

}
