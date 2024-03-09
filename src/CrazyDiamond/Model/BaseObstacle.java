package CrazyDiamond.Model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.io.IOException;

public abstract class BaseObstacle extends BaseElementNommable {

    private final Imp_Identifiable imp_identifiable ;

    private final BooleanProperty appartenance_composition ;
    private final BooleanProperty appartenance_systeme_optique_centre ;
    private final BooleanProperty appartenance_groupe;

    BaseObstacle(String nom) {
        this(new Imp_Identifiable(),new Imp_Nommable(nom)) ;
    }

    BaseObstacle(Imp_Identifiable ii, Imp_Nommable in) {
        super(in) ;
        this.imp_identifiable = ii ;
        this.appartenance_composition = new SimpleBooleanProperty(false) ;
        this.appartenance_systeme_optique_centre = new SimpleBooleanProperty(false) ;
        this.appartenance_groupe = new SimpleBooleanProperty(false) ;
    }

    public String id() { return imp_identifiable.id(); }

    public void appliquerSurIdentifiable(ConsumerAvecException<Object, IOException> consumer) throws IOException {
        consumer.accept(imp_identifiable);
    }

    public void definirAppartenanceComposition(boolean b) {this.appartenance_composition.set(b);}
    public boolean appartientAComposition() {return this.appartenance_composition.get() ;}

    public void definirAppartenanceSystemeOptiqueCentre(boolean b) {this.appartenance_systeme_optique_centre.set(b);}
    public boolean appartientASystemeOptiqueCentre() {return this.appartenance_systeme_optique_centre.get() ;}
    public BooleanProperty appartenanceSystemeOptiqueProperty() {return appartenance_systeme_optique_centre ;}

    public void definirAppartenanceGroupe(boolean b) { this.appartenance_groupe.set(b); }
    public boolean appartientAGroupe() {return this.appartenance_groupe.get() ;}

}
