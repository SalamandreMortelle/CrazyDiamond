package CrazyDiamond.Model;

import javafx.beans.property.StringProperty;

public interface Nommable {

    String nom() ;

    StringProperty nomProperty() ;

    default void definirNom(String nom) {
        nomProperty().set(nom);
    }
}
