package CrazyDiamond.Model;

import javafx.beans.property.StringProperty;

import java.io.IOException;

public abstract class BaseElementNommable {

    private final Imp_Nommable imp_nommable;

    BaseElementNommable(String nom) {
        this(new Imp_Nommable(nom)) ;
    }

    BaseElementNommable(Imp_Nommable in) {
        this.imp_nommable = in ;
    }

    public String nom() {  return imp_nommable.nom(); }
    public StringProperty nomProperty() { return imp_nommable.nomProperty(); }
    public String toString() { return nom(); }

    public void appliquerSurNommable(ConsumerAvecException<Object,IOException> consumer) throws IOException {
        consumer.accept(imp_nommable);
    }

}
