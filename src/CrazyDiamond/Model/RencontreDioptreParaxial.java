package CrazyDiamond.Model;

import javafx.beans.property.*;
import javafx.scene.transform.Affine;

public class RencontreDioptreParaxial /* implements Comparable<DioptreParaxial>*/ {

    // Abscisse de l'intersection dans le référentiel du SOC
    DoubleProperty z_intersection;

    // Rayon algébrique de courbure de la surface rencontrée, au niveau du point de rencontre, ou "null" si le dioptre est plan
    ObjectProperty<Double> r_courbure;

    // Indice du milieu "avant" la surface (lorsque x est juste inférieur à x_intersection)
    DoubleProperty indice_avant;
    // Indice du milieu "après" la surface (lorsque x est juste supérieur à x_intersection)
    DoubleProperty indice_apres;

    ObjectProperty<Obstacle> obs_surface;

    /**
     * Rayon du diaphragme de l'obstacle, son contenu est null si l'obstacle n'a pas de diaphragme
     */
    ObjectProperty<Double> r_diaphragme;

    BooleanProperty ignorer;

    StringProperty sens;

    StringProperty est_diaphragme_ouverture;
    StringProperty est_diaphragme_champ;
    StringProperty est_diaphragme_champ_pleine_lumiere;
    StringProperty est_diaphragme_champ_total;

    ObjectProperty<Double> h_limite_ouverture;
    ObjectProperty<Double> h_limite_champ;
    ObjectProperty<Double> h_limite_champ_pleine_lumiere;
    ObjectProperty<Double> h_limite_champ_total;

    ObjectProperty<SystemeOptiqueCentre.PositionElement> antecedent_diaphragme;

    // TODO : pas nécessaire d'en faire une Property
    Affine matrice_transfert_partielle;
    boolean sens_plus_en_sortie_matrice_partielle;

    public boolean estConfonduAvec(RencontreDioptreParaxial d_autre) {

        if (!Environnement.quasiEgal(z(),d_autre.z()))
            return false ;

        if (rayonCourbure()==null && d_autre.rayonCourbure()==null)
            return true ;

        if (rayonCourbure()==null && d_autre.rayonCourbure()!=null)
            return false ;

        if (rayonCourbure()!=null && d_autre.rayonCourbure()==null)
            return false ;

        return Environnement.quasiEgal(rayonCourbure(), d_autre.rayonCourbure());

    }

    public boolean estInutile() {

        return (indiceAvant() == indiceApres() && rayonDiaphragme() == null) ;

    }
    protected void fusionneAvecDioptreConfondu(RencontreDioptreParaxial d_autre_confondu) {

            if (rayonDiaphragme()==null && d_autre_confondu.rayonDiaphragme()!=null)
                r_diaphragme.set(d_autre_confondu.rayonDiaphragme()); // On conserve le diaphragme qui n'est pas 'null'
            else if (rayonDiaphragme()!=null && d_autre_confondu.rayonDiaphragme()!=null) {
                if (d_autre_confondu.rayonDiaphragme() < rayonDiaphragme())
                    r_diaphragme.set(d_autre_confondu.rayonDiaphragme()); // On conserve le plus petit des diaphragmes
            }

            if (indiceAvant()==0d && d_autre_confondu.indiceAvant()!=0d )
                indice_avant.set(d_autre_confondu.indiceAvant());

            if (indiceApres()==0d && d_autre_confondu.indiceApres()!=0d)
                indice_apres.set(d_autre_confondu.indiceApres());

    }


    // Constructeur à partir d'un dioptre paraxial : n'initialise pas les propriétés r_diaphragme et ignorer qui
    // relèvent des modalités de traversée du dioptre et sont définies dans un second temps
    public RencontreDioptreParaxial(DioptreParaxial dioptre,boolean sens_plus) {

        // Pour les propriétés qui sont propres à chaque traversée du dioptre (i.e. qui dépendent du sens de
        // propagation de la lumière, ou de choix faits par l'utilisateur), on crée une nouvelle propriété.
        // Pour les autres, qui sont des propriétés intrinsèques de l'obstacles, on se contente de reprendre (copîer)
        // la référence de la propriété existante.

        // Propriétés générales du dioptre, ne dépendant pas du sens de propagation de la lumière : on référence directement
        // la Property du dioptre de base
        this.z_intersection = dioptre.z_intersection;
        this.obs_surface = dioptre.obs_surface;
        this.r_diaphragme = dioptre.r_diaphragme;

        // Propriétés spécifiques de la rencontre du dioptre, qui dépendent du sens de propagation
        this.sens = new SimpleStringProperty(sens_plus?"⟶":"⟵");
        this.indice_avant = new SimpleDoubleProperty(sens_plus?dioptre.indiceAvant():dioptre.indiceApres());
        this.indice_apres = new SimpleDoubleProperty(sens_plus?dioptre.indiceApres():dioptre.indiceAvant());
        if (dioptre.rayonCourbure()!=null)
            this.r_courbure = new SimpleObjectProperty<>((sens_plus?1d:-1d)*dioptre.rayonCourbure());
        else
            this.r_courbure = new SimpleObjectProperty<>(null);

        this.ignorer = new SimpleBooleanProperty(false);
        this.sens = new SimpleStringProperty("⟶");

        this.est_diaphragme_ouverture = new SimpleStringProperty("");
        this.est_diaphragme_champ = new SimpleStringProperty("");
        this.est_diaphragme_champ_pleine_lumiere = new SimpleStringProperty("");
        this.est_diaphragme_champ_total = new SimpleStringProperty("");

        this.h_limite_ouverture = new SimpleObjectProperty<>(null);
        this.h_limite_champ = new SimpleObjectProperty<>(null);
        this.h_limite_champ_pleine_lumiere = new SimpleObjectProperty<>(null);
        this.h_limite_champ_total = new SimpleObjectProperty<>(null);

        this.antecedent_diaphragme = new SimpleObjectProperty<>(null);

        this.matrice_transfert_partielle = null;

        // TODO : revoir à quoi sert ce flag : faut-il l'initialiser en fonction de sens_plus ??
        this.sens_plus_en_sortie_matrice_partielle = true;

    }

    public void appliquerModalitesTraverseeDioptrePrecedentesSiApplicables(SystemeOptiqueCentre.ModalitesTraverseeDioptre modalites_prec) {
        if (modalitesTraverseeDioptrePrecedentesApplicables(modalites_prec)) {
            if (!this.obstacleSurface().aUneProprieteDiaphragme())
                this.r_diaphragme.set(modalites_prec.r_diaphragme);
            this.ignorer.set(modalites_prec.ignorer);
        }
    }

    public boolean modalitesTraverseeDioptrePrecedentesApplicables(SystemeOptiqueCentre.ModalitesTraverseeDioptre modalites_prec) {
        return modalites_prec != null && modalites_prec.obs_surface == this.obstacleSurface();
    }

    public void permuterIndicesAvantApres() {
        double ind = indice_avant.get();
        indice_avant.set(indice_apres.get());
        indice_apres.set(ind);
    }

    public void propagerIndiceAvant() {
        indice_apres.set(indice_avant.get());
    }

    public void propagerIndiceApres() {
        indice_avant.set(indice_apres.get());
    }

    public double ZIntersection() {
        return z_intersection.get();
    }

    public Double rayonCourbure() {
        return r_courbure.get();
    }

    public ObjectProperty<Double> rayonCourbureProperty() {
        return r_courbure;
    }

    public double indiceAvant() {
        return indice_avant.get();
    }

    public double indiceApres() {
        return indice_apres.get();
    }

    public Obstacle obstacleSurface() {
        return obs_surface.get();
    }

    public Double rayonDiaphragme() {
        return r_diaphragme.get();
    }

    public ObjectProperty<Double> rayonDiaphragmeProperty() {
        return r_diaphragme;
    }

    public boolean ignorer() {
        return ignorer.get();
    }

    public String sens() {
        return sens.get();
    }

    public String estDiaphragmeOuverture() {
        return est_diaphragme_ouverture.get();
    }

    public String estDiaphragmeChamp() {
        return est_diaphragme_champ.get();
    }

    private String estDiaphragmeChampPleineLumiere() {
        return est_diaphragme_champ_pleine_lumiere.get();
    }

    private String estDiaphragmeChampTotal() {
        return est_diaphragme_champ_total.get();
    }

    public Double HLimiteOuverture() {
        return h_limite_ouverture.get();
    }

    public Double HLimiteChamp() {
        return h_limite_champ.get();
    }

    public Double HLimiteChampPleineLumiere() {
        return h_limite_champ_pleine_lumiere.get();
    }

    public Double HLimiteChampTotal() {
        return h_limite_champ_total.get();
    }

    public SystemeOptiqueCentre.PositionElement antecedentDiaphragme() {
        return antecedent_diaphragme.get();
    }

    public Affine matriceTransfertPartielle() {
        return matrice_transfert_partielle;
    }

    public boolean sensPlusEnSortieMatricePartielle() {
        return sens_plus_en_sortie_matrice_partielle;
    }

    public void activerDeclenchementCalculElementsCardinauxSiChangementModalitesTraversee(SystemeOptiqueCentre soc) {

        this.ignorer.addListener((observable, oldValue, newValue) -> soc.calculeElementsCardinaux());

        // Si l'obstacle a "nativement" une propriété diaphragme, inutile de déclencher un calcul des éléments
        // cardinaux en cas de changement de cette dernière : c'est déjà pris en charge par le rappel sur changement
        // de toute propriété lorsque l'obstacle a été ajouté au SOC (cf. SystemeOptiqueCentre::ajouterObstacle)
        if (!obstacleSurface().aUneProprieteDiaphragme()) {
            this.r_diaphragme.addListener((observable, oldValue, newValue) -> soc.calculeElementsCardinaux());
        }
    }

    public SystemeOptiqueCentre.PositionElement diaphragme() {
        if (rayonDiaphragme() == null)
            return null;

        return new SystemeOptiqueCentre.PositionElement(ZIntersection(), rayonDiaphragme());
    }

    public Double z() {
        return z_intersection.get();
    }

    public DoubleProperty zProperty() {
        return z_intersection;
    }

    public DoubleProperty indiceAvantProperty() {
        return indice_avant;
    }

    public DoubleProperty indiceApresProperty() {
        return indice_apres;
    }

    public BooleanProperty ignorerProperty() {
        return ignorer;
    }

    public StringProperty sensProperty() {
        return sens;
    }

    public StringProperty estDiaphragmeOuvertureProperty() {
        return est_diaphragme_ouverture;
    }

    public StringProperty estDiaphragmeChampProperty() {
        return est_diaphragme_champ;
    }

    public StringProperty estDiaphragmeChampPleineLumiereProperty() {
        return est_diaphragme_champ_pleine_lumiere;
    }

    public StringProperty estDiaphragmeChampTotalProperty() {
        return est_diaphragme_champ_total;
    }

    public void convertirDistances(double facteur_conversion) {

        // z_intersection, r_courbure et r_diaphragme n'ont pas à être converti s : ils pointent vers les attributs de
        // la classe DioptreParaxial qui ont déjà été convertis
//        z_intersection.set(z_intersection.get()*facteur_conversion);
//        if (r_diaphragme.get()!=null) r_diaphragme.set(r_diaphragme.get()*facteur_conversion);

        if (r_courbure.get()!=null) r_courbure.set(r_courbure.get()*facteur_conversion);

        if (antecedent_diaphragme.get()!=null)
            antecedent_diaphragme.set(
                    new SystemeOptiqueCentre.PositionElement(
                            antecedent_diaphragme.get().z()*facteur_conversion,
                            antecedent_diaphragme.get().hauteur()*facteur_conversion)
            );

        if (h_limite_ouverture.get()!=null) h_limite_ouverture.set(h_limite_ouverture.get()*facteur_conversion);

        if (h_limite_champ.get()!=null) h_limite_champ.set(h_limite_champ.get()*facteur_conversion);
        if (h_limite_champ_pleine_lumiere.get()!=null) h_limite_champ_pleine_lumiere.set(h_limite_champ_pleine_lumiere.get()*facteur_conversion);
        if (h_limite_champ_total.get()!=null) h_limite_champ_total.set(h_limite_champ_total.get()*facteur_conversion);

    }

    public void definirRayonDiaphragme(double r_d) {r_diaphragme.set(r_d);}
    public void definirIgnorer(boolean ig) {ignorer.set(ig);}

}
