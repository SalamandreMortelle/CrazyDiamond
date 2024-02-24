package CrazyDiamond.Model;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class CommandeDefinirUnParametreDoubleDistance<R> extends Commande {

    // Le récepteur de la commande
    R recepteur ;

    // Paramètre de la commande
    Double valeur;

    // Données en cas d'annulation
    Double precedente_valeur ;

    Supplier<Double> methode_pour_lire_parametre ;
    Consumer<Double> methode_pour_definir_parametre ;

    public CommandeDefinirUnParametreDoubleDistance(R recepteur, Double valeur, Supplier<Double> methode_pour_lire_parametre, Consumer<Double> methode_pour_definir_parametre) {
        this.recepteur = recepteur ;
        this.valeur = valeur ;
        this.methode_pour_lire_parametre = methode_pour_lire_parametre ;
        this.methode_pour_definir_parametre = methode_pour_definir_parametre ;
    }

    @Override
    public void executer() {
        Double nouveau_precedente_valeur = methode_pour_lire_parametre.get() ;

        if (valeur.equals(nouveau_precedente_valeur))
            return;

        precedente_valeur = nouveau_precedente_valeur ;

        methode_pour_definir_parametre.accept(valeur);

        enregistrer();
    }

    @Override
    public void annuler() { methode_pour_definir_parametre.accept(precedente_valeur); }

    @Override
    protected void convertirDistances(double facteur_conversion) {
        valeur = valeur * facteur_conversion ;
        precedente_valeur = precedente_valeur*facteur_conversion ;
    }
}
