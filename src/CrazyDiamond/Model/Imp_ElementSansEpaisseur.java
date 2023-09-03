package CrazyDiamond.Model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.Objects;

public class Imp_ElementSansEpaisseur {

    private final ObjectProperty<NatureMilieu> nature_milieu;

    public void ajouterRappelSurChangementToutePropriete(RappelSurChangement rap) {
        nature_milieu.addListener((observable, oldValue, newValue) -> rap.rappel());
    }

    public void ajouterRappelSurChangementTouteProprieteModifiantChemin(RappelSurChangement rap) {
        nature_milieu.addListener((observable, oldValue, newValue) -> rap.rappel());
    }

    public Imp_ElementSansEpaisseur(NatureMilieu nature_milieu) throws IllegalArgumentException {

        if (nature_milieu==NatureMilieu.TRANSPARENT)
           throw new IllegalArgumentException("Un élément sans épaisseur ne peut pas être transparent.") ;

        this.nature_milieu = new SimpleObjectProperty<NatureMilieu>(Objects.requireNonNullElse(nature_milieu, NatureMilieu.PAS_DE_MILIEU));

    }


    public void definirNatureMilieu(NatureMilieu nature_mil) {
        if (nature_milieu.get() == nature_mil)
            return ;

        nature_milieu.set(nature_mil);
    }

    public NatureMilieu natureMilieu() {
        return nature_milieu.get();
    }

    public ObjectProperty<NatureMilieu> natureMilieuProperty() {
        return nature_milieu;
    }


}
