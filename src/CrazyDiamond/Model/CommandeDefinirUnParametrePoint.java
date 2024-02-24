package CrazyDiamond.Model;

import javafx.geometry.Point2D;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class CommandeDefinirUnParametrePoint<R> extends Commande {

    // Le récepteur de la commande
    R recepteur ;

    // Paramètre de la commande
    Point2D valeur;

    // Données en cas d'annulation
    Point2D precedente_valeur ;

    Supplier<Point2D> methode_pour_lire_parametre ;
    Consumer<Point2D> methode_pour_definir_parametre ;

    public CommandeDefinirUnParametrePoint(R recepteur, Point2D valeur, Supplier<Point2D> methode_pour_lire_parametre, Consumer<Point2D> methode_pour_definir_parametre) {
        this.recepteur = recepteur ;
        this.valeur = valeur ;
        this.methode_pour_lire_parametre = methode_pour_lire_parametre ;
        this.methode_pour_definir_parametre = methode_pour_definir_parametre ;
    }

    @Override
    public void executer() {
        Point2D nouveau_precedente_valeur = methode_pour_lire_parametre.get() ;

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
        valeur = valeur.multiply(facteur_conversion) ;
        precedente_valeur = precedente_valeur.multiply(facteur_conversion) ;
    }
}
