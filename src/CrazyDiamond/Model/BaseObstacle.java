package CrazyDiamond.Model;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.io.IOException;
import java.util.LinkedHashMap;

public abstract class BaseObstacle extends BaseElementNommable {

    private final Imp_Identifiable imp_identifiable;

    private final ObjectProperty<BaseObstacleComposite> composite_parent;

    /**
     * Plus petit SOC contenant l'obstacle. Si l'obstacle fait partie d'un composite qui appartient directement, ou
     * indirectement (via d'autres Composites), à un SOC, alors ce SOC est aussi considéré comme le SOC conteneur de
     * l'obstacle.
     * Rappel : les Compositions ne contiennent pas de groupes.
     */
    private final ObjectProperty<SystemeOptiqueCentre> soc_conteneur;

    protected final LinkedHashMap<Object,RappelSurChangement> rappels_sur_changement_toute_propriete;
    protected final LinkedHashMap<Object,RappelSurChangement> rappels_sur_changement_toute_propriete_modifiant_chemin;

    BaseObstacle(String nom) {
        this(new Imp_Identifiable(), new Imp_Nommable(nom));
    }

    BaseObstacle(Imp_Identifiable ii, Imp_Nommable in) {
        super(in);
        this.imp_identifiable = ii;
        this.composite_parent = new SimpleObjectProperty<>(null);
        this.soc_conteneur = new SimpleObjectProperty<>(null);
        this.rappels_sur_changement_toute_propriete = new LinkedHashMap<>(2);
        this.rappels_sur_changement_toute_propriete_modifiant_chemin = new LinkedHashMap<>(2);
    }

    public String id() {
        return imp_identifiable.id();
    }

    public BaseObstacleComposite parent() {
        return composite_parent.get();
    }

    public void definirParent(BaseObstacleComposite parent) {
        this.composite_parent.set(parent);
    }

    public SystemeOptiqueCentre SOCParent() {
        return soc_conteneur.get();
    }

    public ObjectProperty<SystemeOptiqueCentre> systemeOptiqueParentProperty() {
        return soc_conteneur;
    }

    public void definirSOCParent(SystemeOptiqueCentre soc) {
        this.soc_conteneur.set(soc);
    }

    public Obstacle obstacleAvecId(String obs_id) {
        return id().equals(obs_id) ? (Obstacle) this : null;
    }

    public void appliquerSurIdentifiable(ConsumerAvecException<Object, IOException> consumer) throws IOException {
        consumer.accept(imp_identifiable);
    }

    public boolean appartientAComposition() {
        return this.composite_parent.get() != null && this.composite_parent.get() instanceof Composition;
    }


    // Indique si l'obstacle fait directement partie d'un SOC ou fait partie d'un Composite qui appartient à un SOC
    public boolean appartientASystemeOptiqueCentre() {
        return this.SOCParent() != null;
    }

    public BooleanBinding appartenanceSystemeOptiqueProperty() {
        return this.soc_conteneur.isNotNull();
    }

    public boolean appartientAGroupe() {
        return this.composite_parent.get() != null && this.composite_parent.get() instanceof Groupe;
    }

    public boolean appartientAComposite() {
        return this.composite_parent.get() != null;
    }

    public void ajouterRappelSurChangementToutePropriete(Object cle_observateur,RappelSurChangement rap) {
        rappels_sur_changement_toute_propriete.put(cle_observateur,rap);
    }
    public void retirerRappelSurChangementToutePropriete(Object cle_observateur) {
        rappels_sur_changement_toute_propriete.remove(cle_observateur);
    }
    public void declencherRappelsSurChangementToutePropriete() {
        rappels_sur_changement_toute_propriete.forEach( (cle_observateur,rap) -> rap.rappel());
    }

    protected void propagerRappelsSurChangementToutePropriete(Obstacle ob_cible) {
        rappels_sur_changement_toute_propriete.forEach(
                ob_cible::ajouterRappelSurChangementToutePropriete);
    }

    protected void retirerRappelsPropagesSurChangementToutePropriete(Obstacle ob_cible) {
        rappels_sur_changement_toute_propriete.forEach(
                (cle_observateur,rap) -> ob_cible.retirerRappelSurChangementToutePropriete(cle_observateur));
    }


    public void ajouterRappelSurChangementTouteProprieteModifiantChemin(Object cle_observateur, RappelSurChangement rap) {
        rappels_sur_changement_toute_propriete_modifiant_chemin.put(cle_observateur,rap);
    }
    public void retirerRappelSurChangementTouteProprieteModifiantChemin(Object cle_observateur) {
        rappels_sur_changement_toute_propriete_modifiant_chemin.remove(cle_observateur);
    }
    public void declencherRappelsSurChangementTouteProprieteModifiantChemin() {
        rappels_sur_changement_toute_propriete_modifiant_chemin.forEach( (cle_observateur,rap) -> rap.rappel());
    }

    protected void propagerRappelsSurChangementTouteProprieteModifiantChemin(Obstacle bo_cible) {
        rappels_sur_changement_toute_propriete_modifiant_chemin.forEach(
                bo_cible::ajouterRappelSurChangementTouteProprieteModifiantChemin);
    }

    protected void retirerRappelsPropagesSurChangementTouteProprieteModifiantChemin(Obstacle ob_cible) {
        rappels_sur_changement_toute_propriete_modifiant_chemin.forEach(
                (cle_observateur,rap) -> ob_cible.retirerRappelSurChangementTouteProprieteModifiantChemin(cle_observateur));
    }


}
