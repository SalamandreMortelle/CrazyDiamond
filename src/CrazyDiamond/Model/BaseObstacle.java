package CrazyDiamond.Model;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.io.IOException;

public abstract class BaseObstacle extends BaseElementNommable {

    private final Imp_Identifiable imp_identifiable ;

//    private final BooleanProperty appartenance_composition ;
//    private final BooleanProperty appartenance_groupe;
    private final ObjectProperty<BaseObstacleComposite> composite_parent;
//    private final BooleanProperty appartenance_systeme_optique_centre ;

    /**
     * Plus petit SOC contenant l'obstacle. Si l'obstacle fait partie d'un composite qui appartient directement, ou
     * indirectement (via d'autres Composites), à un SOC, alors ce SOC est aussi considéré comme le SOC conteneur de
     * l'obstacle.
     * Rappel : les Compositions ne contiennent pas de groupes.
     */
    private final ObjectProperty<SystemeOptiqueCentre> soc_conteneur;

    BaseObstacle(String nom) {
        this(new Imp_Identifiable(),new Imp_Nommable(nom)) ;
    }

    BaseObstacle(Imp_Identifiable ii, Imp_Nommable in) {
        super(in) ;
        this.imp_identifiable = ii ;
//        this.appartenance_composition = new SimpleBooleanProperty(false) ;
//        this.appartenance_systeme_optique_centre = new SimpleBooleanProperty(false) ;
//        this.appartenance_groupe = new SimpleBooleanProperty(false) ;
        this.composite_parent = new SimpleObjectProperty<>(null);
        this.soc_conteneur = new SimpleObjectProperty<>(null);
    }

    public String id() { return imp_identifiable.id(); }

    public BaseObstacleComposite parent() { return composite_parent.get();}
    public void definirParent(BaseObstacleComposite parent) { this.composite_parent.set(parent); }

    public SystemeOptiqueCentre SOCParent() { return soc_conteneur.get() ;}
    public ObjectProperty<SystemeOptiqueCentre> systemeOptiqueParentProperty() { return soc_conteneur ; }

    public void definirSOCParent(SystemeOptiqueCentre soc) { this.soc_conteneur.set(soc); }

    public Obstacle obstacleAvecId(String obs_id) {
        return id().equals(obs_id)?(Obstacle)this:null ;
    }

    public void appliquerSurIdentifiable(ConsumerAvecException<Object, IOException> consumer) throws IOException {
        consumer.accept(imp_identifiable);
    }

//    public void definirAppartenanceComposition(boolean b) {this.appartenance_composition.set(b);}
    public boolean appartientAComposition() {return this.composite_parent.get()!=null && this.composite_parent.get() instanceof Composition;}

//    public void definirAppartenanceSystemeOptiqueCentre(boolean b) {this.appartenance_systeme_optique_centre.set(b);}

//    public boolean appartientASystemeOptiqueCentre() {return this.appartenance_systeme_optique_centre.get() ;}

    // Indique si l'obstacle fait directement partie d'un SOC ou fait partie d'un Composite qui appartient à un SOC
    public boolean appartientASystemeOptiqueCentre() { return this.SOCParent()!=null ; }
    public BooleanBinding appartenanceSystemeOptiqueProperty() {return this.soc_conteneur.isNotNull() ;}

//    public void definirAppartenanceGroupe(boolean b) { this.appartenance_groupe.set(b); }
    public boolean appartientAGroupe() {return this.composite_parent.get()!=null && this.composite_parent.get() instanceof Groupe;}
    public boolean appartientAComposite() {return this.composite_parent.get()!=null ;}

}
