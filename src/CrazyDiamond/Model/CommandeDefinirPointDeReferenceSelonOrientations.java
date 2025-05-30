package CrazyDiamond.Model;

import javafx.geometry.Point2D;

import java.util.function.DoubleFunction;
import java.util.function.ObjDoubleConsumer;

public class CommandeDefinirPointDeReferenceSelonOrientations extends Commande {

    // Le récepteur de la commande
    ElementDeSOC element_de_soc;

    SystemeOptiqueCentre soc_parent ;

    // Paramètre de la commande
    Point2D valeur;

    // Données en cas d'annulation
    Point2D precedente_valeur ;

    DoubleFunction<Point2D> methode_pour_lire_pt_ref;
    ObjDoubleConsumer<Point2D> methode_pour_definir_pt_ref;

    public CommandeDefinirPointDeReferenceSelonOrientations(ElementDeSOC element_de_soc, SystemeOptiqueCentre soc_parent, Point2D valeur, DoubleFunction<Point2D> methode_pour_lire_pt_ref, ObjDoubleConsumer<Point2D> methode_pour_definir_pt_ref) {
        this.element_de_soc = element_de_soc ;
        this.soc_parent = soc_parent ;
        this.valeur = valeur ;
        this.methode_pour_lire_pt_ref = methode_pour_lire_pt_ref;
        this.methode_pour_definir_pt_ref = methode_pour_definir_pt_ref;
    }

    @Override
    public void executer() {
        Point2D nouveau_precedente_valeur = methode_pour_lire_pt_ref.apply(element_de_soc.orientation()) ;

        if (valeur.equals(nouveau_precedente_valeur))
            return;

        precedente_valeur = nouveau_precedente_valeur ;

        methode_pour_definir_pt_ref.accept(valeur, element_de_soc.orientation());

        enregistrer();
    }

    @Override
    public void annuler() { methode_pour_definir_pt_ref.accept(precedente_valeur, element_de_soc.orientation()); }

    @Override
    protected void convertirDistances(double facteur_conversion) {
        valeur = valeur.multiply(facteur_conversion) ;
        precedente_valeur = precedente_valeur.multiply(facteur_conversion) ;
    }
}
