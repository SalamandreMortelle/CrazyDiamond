package CrazyDiamond.Model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

// Classe de base pour tous les éléments constitutifs d'un environnement (sources, obstacles...)
public class Imp_Nommable {

    protected final StringProperty nom ;

    public String nom() {
        return nom.get();
    }

    public StringProperty nomProperty() {
        return nom;
    }

    public Imp_Nommable(String nom) {
        this.nom = new SimpleStringProperty(nom);
    }
}
