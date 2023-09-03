package CrazyDiamond.Model;

import javafx.geometry.Point2D;

/**
 * Classe définissant un rayon lumineux qui traverse en ligne droite un milieu dont l'indice de réfraction est le même
 * de son point de départ à son point d'arrivée. Le vecteur directeur du rayon est maintenu dans l'attribut direction.
 * Le rayon est également caractérisé par le phénomène qui en est à l'origine (Emission depuis une source, réflexion,
 * transmission, ou diffusion [ce dernier phénomène n'est pas modélisé à ce jour]).
 * Le ratio de puissance du rayon par rapport à la puissance du rayon initial issu de la source est maintenu également.
 * Enfin, on peut définir que l'onde lumineuse portée par le rayon est polarisée, et caractériser cette polarisation
 * par l'angle du champ électrique E par rapport au plan d'incidence du rayon (c'est à dire le plan de la figure)
 */
public class Rayon {
    public PhenomeneOrigine phenomene_origine ;
    private final DemiDroiteOuSegment support ;
    public double indice_milieu_traverse ;

    /**
     * Fraction de la puissance d'un rayon issu de la source portée par ce rayon (entre 0 et 1)
     * Si le PhenomeOrigine est une REFLEXION, ce ratio est une réflectance du dioptre d'où provient le rayon
     * Si le PhenomeOrigine est une TRANSMISSION, ce ratio est une transmittance
     */
    public double ratio_puissance ;
    public boolean est_polarisee = false ;
    public double angle_champ_electrique = 0.0 ;


    Rayon(Point2D dep, Point2D dir) throws IllegalArgumentException {
        this(dep,dir,1.0) ;
    }

    Rayon(Point2D dep, Point2D dir,PhenomeneOrigine ph_orig) throws IllegalArgumentException {
        this(dep,dir,1.0,ph_orig,1.0) ;
    }

    Rayon(Point2D dep, Point2D dir,double indice_milieu_traverse) throws IllegalArgumentException {
        this(dep,dir,indice_milieu_traverse,PhenomeneOrigine.TRANSMISSION,1.0) ;
    }
    Rayon(Point2D dep, Point2D dir,double indice_milieu_traverse,PhenomeneOrigine ph_orig,double ratio_puissance) throws IllegalArgumentException {
        this(dep,dir,indice_milieu_traverse,ph_orig,ratio_puissance,false,0.0) ;
    }
    Rayon(Point2D dep, Point2D dir,double indice_milieu_traverse,PhenomeneOrigine ph_orig,double ratio_puissance,boolean est_polarisee,double angle_champ_electrique) throws IllegalArgumentException {


        if (dir.getX() == 0.0 && dir.getY()==0)
            throw new IllegalArgumentException("Le vecteur directeur d'un rayon ne peut pas être nul.") ;

        support = new DemiDroiteOuSegment(dep,dir) ;
        //support = new SupportGeometriqueRayon(dep,dir.normalize()) ; // TODO : Verifier l'utilité de la normalisation du vecteur directeur

        this.indice_milieu_traverse = indice_milieu_traverse ;

        this.phenomene_origine = ph_orig ;

        this.ratio_puissance = ratio_puissance ;

        this.est_polarisee = est_polarisee ;
        this.angle_champ_electrique = angle_champ_electrique ;
    }

    Rayon(Point2D dep, Point2D dir, Point2D arr,PhenomeneOrigine ph_orig) throws IllegalArgumentException {
        this(dep,dir,arr,1.0,ph_orig) ;
    }
    Rayon(Point2D dep, Point2D dir, Point2D arr) throws IllegalArgumentException {
        this(dep,dir,arr,1.0) ;
    }

    Rayon(Point2D dep, Point2D dir, Point2D arr,double indice_milieu_traverse) throws IllegalArgumentException {
        this(dep,dir,arr,indice_milieu_traverse,PhenomeneOrigine.TRANSMISSION) ;
    }
    Rayon(Point2D dep, Point2D dir, Point2D arr,double indice_milieu_traverse,PhenomeneOrigine ph_orig) throws IllegalArgumentException {
        this(dep, dir, arr, indice_milieu_traverse,ph_orig,false,0.0);
    }
    Rayon(Point2D dep, Point2D dir, Point2D arr,double indice_milieu_traverse,PhenomeneOrigine ph_orig,boolean est_polarisee,double angle_champ_electrique) throws IllegalArgumentException {

        if (dir.getX() == 0.0 && dir.getY()==0)
            throw new IllegalArgumentException("Le vecteur directeur d'un rayon ne peut pas être nul.") ;

        support = new DemiDroiteOuSegment() ;
        support.definirDepartEtArrivee(dep,arr) ;
//        support = new SupportGeometriqueRayon(dep,dir.normalize(),arr) ; // TODO : Verifier l'utilité de la normalisation du vecteur directeur

        this.indice_milieu_traverse = indice_milieu_traverse ;
        this.phenomene_origine = ph_orig ;

        this.est_polarisee = est_polarisee ;
        this.angle_champ_electrique = angle_champ_electrique ;
    }

    public boolean estInfini() {
        return support.arrivee() ==null ;
    }

    public DemiDroiteOuSegment supportGeometrique() {
        return support ;
    }

    public Point2D depart() {return support.depart();}

    public Point2D direction() {return support.direction();}
    public Point2D arrivee() {return support.arrivee();}
    public void definirArrivee(Point2D p_arr) {
        support.definirArrivee(p_arr);}

    public enum PhenomeneOrigine { EMISSION_SOURCE , REFLEXION , TRANSMISSION, DIFFUSION } ;

}
