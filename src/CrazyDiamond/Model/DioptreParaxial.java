package CrazyDiamond.Model;

import javafx.beans.property.*;

import java.util.Comparator;
import java.util.List;

public class DioptreParaxial /* implements Comparable<DioptreParaxial>*/ {

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

    static protected Comparator<DioptreParaxial> comparateur = (d1,d2) -> {

        if (!Environnement.quasiEgal(d1.z(),d2.z()))
            return Double.compare(d1.z(),d2.z()) ;
        else { // Z égaux : il faut comparer les rayons de courbure
            if (d1.rayonCourbure()==null && d2.rayonCourbure()==null) // Dioptres plans confondus l'un contre l'autre, ou superposés.
                // Il n'est pas nécessaire de prioriser le dioptre qui a un milieu avant (ou après) car ces deux dioptres
                // seront systématiquement fusionnés via un appel à fusionneAvecDioptreConfondu()
                return 0 ;

            if (d1.rayonCourbure()==null && d2.rayonCourbure()!=null)
                return Double.compare(0d,d2.rayonCourbure()) ;

            if (d1.rayonCourbure()!=null && d2.rayonCourbure()==null)
                return Double.compare(d1.rayonCourbure(),0) ;

            if (Environnement.quasiEgal(d1.rayonCourbure(), d2.rayonCourbure())) // Dioptres confondus "encastrés", ou superposés.
                // Il n'est pas nécessaire de prioriser le dioptre qui a un milieu avant (ou après) car ces deux dioptres
                // seront systématiquement fusionnés via un appel à fusionneAvecDioptreConfondu()
                return 0 ;

            if (d1.rayonCourbure()<0d && d2.rayonCourbure()>0d)
                return -1 ;

            if (d1.rayonCourbure()>0d && d2.rayonCourbure()<0d)
                return 1 ;

            // Les deux rayons de courbure sont de même signe : le plus petit (en tenant compte du signe) gagne
            return Double.compare(d2.rayonCourbure(),d1.rayonCourbure()) ;

        }

    } ;

    public boolean estConfonduAvec(DioptreParaxial d_autre) {

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
    protected void fusionneAvecDioptreConfondu(DioptreParaxial d_autre_confondu) {

        // NB : Cette métode n'est appelée que pour fusionner les dioptres d'une composition, qui ne peut ni contenir
        // d'obstacles sans épaisseur, ni donc d'obstacles avec une propriété (Property) diaphragme (puisque seul le
        // segment, sans épaisseur, dispose d'une telle propréité)

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

    public DioptreParaxial(double z_intersection, Double r_courbure) { // Création d'un dioptre virtuel : seul sa position géométrique (Z et Rc) compte
        this(z_intersection, r_courbure, 0.0, 0.0, null);
    }

    public DioptreParaxial(double z_intersection, Double r_courbure, double indice_avant, double indice_apres, Obstacle obs_surface) {

        this.z_intersection = new SimpleDoubleProperty(z_intersection);
        this.r_courbure = new SimpleObjectProperty<Double>(r_courbure);


        if (obs_surface!=null) { // Pas un dioptre virtuel
            this.indice_avant = new SimpleDoubleProperty(indice_avant);
            this.indice_apres = new SimpleDoubleProperty(indice_apres);

            if (obs_surface.aUneProprieteDiaphragme()) { // Ce cas ne concerne que les obs_surface de type Segment qui ont une propriété diaphragme
                this.r_diaphragme = new SimpleObjectProperty<Double>(obs_surface.diaphragmeProperty().getValue());
                this.r_diaphragme.bindBidirectional(obs_surface.diaphragmeProperty());
            } else
                this.r_diaphragme = new SimpleObjectProperty<Double>(obs_surface.rayonDiaphragmeParDefaut());

            this.obs_surface = new SimpleObjectProperty<Obstacle>(obs_surface);

        } else { // Cas où ce dioptre est "virtuel" (pas rattaché à un obstacle), seul sa position géométrique (Z et Rc) compte
            // Ne rien faire : on n'a pas besoin de la propriété Rdiaphragme dans ce cas, ni de la propriété obs_surface
            // this.r_diaphragme = new SimpleObjectProperty<Double>(null);
            // this.obs_surface = new SimpleObjectProperty<Obstacle>(null);
        }


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

    static void supprimeDioptresEntre(List<DioptreParaxial> liste_d, DioptreParaxial d_deb_suppr, DioptreParaxial d_fin_suppr) {


        liste_d.removeIf(d -> ( DioptreParaxial.comparateur.compare(d,d_deb_suppr)>=0
                && DioptreParaxial.comparateur.compare(d_fin_suppr,d)>=0 ) ) ;

    }
    /**
     * Supprime de liste_d les dioptres qui sont avant d_limite, ou confondus avec lui
     * @param liste_d
     * @param d_limite
     */
    private static void supprimeDioptresAvant(List<DioptreParaxial> liste_d, DioptreParaxial d_limite) {
        liste_d.removeIf(d -> DioptreParaxial.comparateur.compare(d_limite,d)>=0 ) ;
    }

    /**
     * Supprime de liste_d les dioptres qui sont après d_limite, ou confondus avec lui
     * @param liste_d
     * @param d_limite
     */
    static void supprimeDioptresApres(List<DioptreParaxial> liste_d, DioptreParaxial d_limite) {
        liste_d.removeIf(d -> DioptreParaxial.comparateur.compare(d,d_limite)>=0 ) ;
    }


    // Pré-condition (?) : dans liste_d les dioptres sont classés par ordre de profondeur : de l'arrière-plan vers l'avant plan
    // comme la liste des obstacles_centres du SOC => Cette précondition n'est pas nécessaire s'il n'y a pas de dioptes confondus
    // dans liste_d, ce qui est le cas (cf. appelant : les dioptres confondus ont tous été supprimés au fur et à mesure)
    static double captureIndiceAvant(List<DioptreParaxial> liste_d, DioptreParaxial d_limite) {

        DioptreParaxial[] dioptres_encadrant = dioptresEncadrant(liste_d,d_limite) ;

        DioptreParaxial d_le_plus_proche_avant_limite = dioptres_encadrant[0] ;
        DioptreParaxial d_sur_limite = dioptres_encadrant[1] ;
        DioptreParaxial d_le_plus_proche_apres_limite = dioptres_encadrant[2] ;

        if (d_sur_limite!=null)
            return d_sur_limite.indiceAvant() ;

        if (d_le_plus_proche_avant_limite!=null)
            return d_le_plus_proche_avant_limite.indiceApres() ;

        if (d_le_plus_proche_apres_limite!=null)
            return d_le_plus_proche_apres_limite.indiceAvant() ;

        return 0d ;
    }

    static double captureIndiceApres(List<DioptreParaxial> liste_d, DioptreParaxial d_limite) {

        DioptreParaxial[] dioptres_encadrant = dioptresEncadrant(liste_d,d_limite) ;

        DioptreParaxial d_le_plus_proche_avant_limite = dioptres_encadrant[0] ;
        DioptreParaxial d_sur_limite = dioptres_encadrant[1] ;
        DioptreParaxial d_le_plus_proche_apres_limite = dioptres_encadrant[2] ;

        if (d_sur_limite!=null)
            return d_sur_limite.indiceApres() ;

        if (d_le_plus_proche_avant_limite!=null)
            return d_le_plus_proche_avant_limite.indiceApres() ;

        // il n'y avait pas de dioptre avant, ou sur la limite, il y en a peut-être un après
        if (d_le_plus_proche_apres_limite!=null)
            return d_le_plus_proche_apres_limite.indiceAvant() ;

        return 0d ;
    }

    /**
     * Retourne les deux dioptres qui "encadrent au plus près" un dioptre d_cible donné
     * le premier dioptre retourné est le plus proche avant ou sur d_cible
     * le deuxième est strictement sur la limite
     * le troisième est strictement après la limite
     * @param liste_d
     * @param d_cible
     * @return
     */
    private static DioptreParaxial[] dioptresEncadrant(List<DioptreParaxial> liste_d, DioptreParaxial d_cible) {

        DioptreParaxial d_le_plus_proche_avant_limite = null ;
        DioptreParaxial d_sur_limite = null ;
        DioptreParaxial d_le_plus_proche_apres_limite = null ;

        for (DioptreParaxial d : liste_d) {

            int cmp_avec_limite = DioptreParaxial.comparateur.compare(d_cible,d) ;

            if (cmp_avec_limite<0) { // d est strictement après la limite

                if (d_le_plus_proche_apres_limite==null)
                    d_le_plus_proche_apres_limite = d ;
                else {
                    int cmp_avec_d_plus_proche_apres_limite  = DioptreParaxial.comparateur.compare(d_le_plus_proche_apres_limite,d) ;

                    if (cmp_avec_d_plus_proche_apres_limite>=0)
                        d_le_plus_proche_apres_limite = d;
                }

                continue;
            }

            if (cmp_avec_limite==0) {// d est confondu avec le dioptre limite
                d_sur_limite = d ;
                continue ;
            }

            // d est donc strictement avant le dioptre limite

            if (d_le_plus_proche_avant_limite==null)
                d_le_plus_proche_avant_limite = d ;
            else {
                int cmp_avec_d_plus_proche_avant_limite = DioptreParaxial.comparateur.compare(d, d_le_plus_proche_avant_limite);

                if (cmp_avec_d_plus_proche_avant_limite>=0)
                    // d est après (ou sur) le d_le_plus_proche : on le garde. S'il est dessus, on le garde, car il a été
                    // trouvé après, ce qui signifie qu'il recouvre le précédent.
                    d_le_plus_proche_avant_limite = d ;
            }

        }

        DioptreParaxial[] resultat = new DioptreParaxial[3] ;

        resultat[0] = d_le_plus_proche_avant_limite ;
        resultat[1] = d_sur_limite ;
        resultat[2] = d_le_plus_proche_apres_limite ;

        return resultat ;
    }

    public void convertirDistances(double facteur_conversion) {
        z_intersection.set(z_intersection.get()*facteur_conversion);
        if (r_courbure.get()!=null) r_courbure.set(r_courbure.get()*facteur_conversion);
        if (r_diaphragme.get()!=null) r_diaphragme.set(r_diaphragme.get()*facteur_conversion);
    }
}
