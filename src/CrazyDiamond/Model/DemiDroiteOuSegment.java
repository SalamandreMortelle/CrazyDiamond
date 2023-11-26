package CrazyDiamond.Model;

import javafx.geometry.Point2D;

/**
 * Support Géométrique (d'un rayon, mais pas seulement) qui peut être une demi-droite si le rayon est infini (il n'est
 * alors défini que par un point de départ et un vecteur directeur), ou un segment de droite (défini par un point de
 * départ et un point d'arrivée) s'il est fini.
 */
public class DemiDroiteOuSegment {

    public static DemiDroiteOuSegment construireSegment(double xdep, double ydep, double xarr, double yarr) {
        DemiDroiteOuSegment s = new DemiDroiteOuSegment() ;
        s.definirDepartEtArrivee(new Point2D(xdep,ydep),new Point2D(xarr,yarr));

        return s ;
    }
    public static DemiDroiteOuSegment construireSegment(Point2D dep, Point2D arr) {
        DemiDroiteOuSegment s = new DemiDroiteOuSegment() ;
        s.definirDepartEtArrivee(dep,arr);

        return s ;
    }

    private Point2D depart ;
    /**
     * l'arrivee n'est pas définie (null) s'il s'agit d'une demi-droite.
     */
    private Point2D arrivee ;
    /**
     * Vecteur directeur de la demi-droite ou du segment.
     * Si demi-droite : la norme de la direction est quelconque
     * Si segment : la norme de la direction est égal à la longueur du segment
     */
    private Point2D direction ;

    public DemiDroiteOuSegment() {
        this (new Point2D(0d,0d),new Point2D(1d,0d)) ;
    }


    public DemiDroiteOuSegment(Point2D depart, Point2D direction) {
        this.depart = depart;
        this.direction = direction.normalize();
        this.arrivee = null ;
    }

//    public DemiDroiteOuSegment(DemiDroiteOuSegment autre) {
//        // Une copie de surface suffit car les attributs sont de type Point2D (qi ne peut pas être modifié une fois créé)
//        this.depart = autre.depart ;
//        this.arrivee = autre.arrivee ;
//        this.direction = autre.direction ;
//
//    }

//    public DemiDroiteOuSegment(Point2D depart, Point2D direction, Point2D arrivee) {
//        this.depart = depart;
//        this.direction = direction ;
//        this.arrivee = arrivee;
//    }

    // TODO : ne marche que pour les segments ! A revoir pour les demi-droites
//    public boolean contient(Point2D p) {
//        if (p.getX() < Math.min(depart.getX(), arrivee.getX()) || p.getX() > Math.max(depart.getX(), arrivee.getX()))
//            return false;
//
//        if (depart.getX() == arrivee.getX()) {
//            if (p.getX() == depart.getX() && p.getY() > Math.min(depart.getY(), arrivee.getY()) && p.getY() < Math.max(depart.getY(), arrivee.getY()))
//                return true;
//
//            return false;
//        }
//
//        double a = (arrivee.getY() - depart.getY()) / (arrivee.getX() - depart.getX());
//        double yseg = a * (p.getX() - depart.getX()) + depart.getY();
//
//        return Environnement.quasiEgal(yseg, p.getY());
//
//    }

    public void definirDepart(Point2D dep) {
        this.depart = dep ;
        this.direction = (this.arrivee!=null?this.arrivee.subtract(dep).normalize():null) ;
    }

    public void definirArrivee(Point2D arr) {
        this.arrivee = arr ;

        if (arr!=null)
            this.direction = arr.subtract(this.depart).normalize();
    }


    public Point2D milieu() {
        if (arrivee == null)
            return null ;

        return new Point2D((depart.getX()+arrivee.getX())/2d,(depart.getY()+arrivee.getY())/2d ) ;

    }

    public void tournerAutourDe(Point2D centre_rot,double angle_rot_deg) {

        double angle_rot = Math.toRadians(angle_rot_deg) ;

        Point2D dep = depart.subtract(centre_rot) ;

        Point2D nouvelle_pos_depart = new Point2D(centre_rot.getX() + dep.getX()*Math.cos(angle_rot)-dep.getY()*Math.sin(angle_rot),
                centre_rot.getY() + dep.getX()*Math.sin(angle_rot)+dep.getY()*Math.cos(angle_rot)) ;

        Point2D arr = arrivee.subtract(centre_rot) ;

        Point2D nouvelle_pos_arrivee = new Point2D(centre_rot.getX() + arr.getX()*Math.cos(angle_rot)-arr.getY()*Math.sin(angle_rot),
                centre_rot.getY() + arr.getX()*Math.sin(angle_rot)+arr.getY()*Math.cos(angle_rot)) ;

        definirDepartEtArrivee(nouvelle_pos_depart,nouvelle_pos_arrivee);

    }



    public void definirDepart(double x, double y) {definirDepart(new Point2D(x,y));}
    public void definirArrivee(double x, double y) {definirArrivee(new Point2D(x,y));}

    public void definirDepartEtArrivee(Point2D dep, Point2D arr) {
        this.depart = dep ;
        this.arrivee = arr ;
        if (arr!=null)
            this.direction = arr.subtract(this.depart).normalize() ;
    }

    public double angle() {
//        return direction.angle(1.0d,0.0d) ;

        return Math.toDegrees(Math.atan2(direction().getY(), direction.getX())) ;
    }

//    public definirDirection(Point2D dir) {
//        this.direction = dir ;
//        this.arrivee
//    }

    /**
     * Retourne, si il existe, le point d'intersection entre deux DemiDroiteOuSegment
     * Attention : si le point de départ de l'un est sur l'autre, le résultat est indéterminé du fait des erreurs
     * d'arrondis sur les flottants de type 'double' : le point de contact peut être retourné ou pas...
     * @param autre_support
     * @return le point d'intersection
     */
    public Point2D intersectionAvec(DemiDroiteOuSegment autre_support) {

        Point2D r  ; // Vecteur directeur du support this
        if (arrivee != null) // Le support est un segment
            r = arrivee.subtract(depart) ; // Pour que la norme de r égale la longueur du segment
        else // Le support est une demi-droite (ou une droite)
            r = direction ; // La norme de r n'a pas d'importance pour la suite du calcul

        Point2D s  ; // Vecteur directeur de autre_support
        if (autre_support.arrivee != null) // L'autre support est un segment
            s = autre_support.arrivee.subtract(autre_support.depart) ; // Pour que la norme de t égale la longueur du segment
        else // Le support est une demi-droite (ou une droite)
            s = autre_support.direction ; // La norme de t n'a pas d'importance pour la suite du calcul

        double r_vectoriel_s = produit_vectoriel_simplifie(r,s) ;

        if (r_vectoriel_s==0)
            return null ;

        Point2D q_moins_p = autre_support.depart.subtract(depart) ;
        double t = produit_vectoriel_simplifie(q_moins_p,s) / r_vectoriel_s ;
        double u = produit_vectoriel_simplifie(q_moins_p,r) / r_vectoriel_s ;

        // Equation paramétrique vectorielle de paramètre t de la droite support this : depart+t*r
        // Equation paramétrique vectorielle de paramètre u de la droite support autre_support : autre_support.depart+u*s

        // Intersection avant l'origine d'une (au moins) des demi-droites => pas d'intersection, inutile de chercher plus loin
        if (t< 0d  || u< 0d )
            return null ;

        if (arrivee == null && autre_support.arrivee == null) {// Deux demi-droites
            if (0d <= t  && 0d <= u)
                return depart.add(r.multiply(t)) ;
            else
                return null ;
        }

        if (arrivee != null && autre_support.arrivee == null) {// Un segment et une demi-droite
            if (0d <= t && t <= 1d && 0d <= u)
                return depart.add(r.multiply(t)) ;
            else
                return null ;
        }

        if (arrivee == null && autre_support.arrivee != null) {// Une demi-droite et un segment
            if (0d <= t && 0d <=u && u <= 1d)
                return autre_support.depart.add(s.multiply(u)) ;
            else
                return null ;
        }


        if (arrivee != null && autre_support.arrivee != null) {// Deux segments
            if (0d <= t && t <= 1d && 0d <=u && u <= 1d)
                return depart.add(r.multiply(t)) ;
            else
                return null ;
        }

        return null ;

    }

    private double produit_vectoriel_simplifie(Point2D v1, Point2D v2) {
        return (v1.getX()*v2.getY()-v1.getY()*v2.getX()) ;
    }

    public Point2D normale() {
        return new Point2D(-direction.getY(), direction.getX()).normalize();
    }

    public Point2D depart() {return depart;}
    public Point2D arrivee() {return arrivee;}

    public Point2D direction() {return direction;}

    public double longueur() {
        if (arrivee!=null && depart!=null)
            return arrivee.subtract(depart).magnitude() ;

        throw new RuntimeException("Impossible de calculer la longueur d'une droite ou demi-droite") ;
    }

    public DemiDroiteOuSegment prolongementAvantDepart() {
        return new DemiDroiteOuSegment(this.depart,this.direction.multiply(-1.0)) ;
    }

    public DemiDroiteOuSegment prolongementApresArrivee() {

        if (this.arrivee==null)
            return null ;

        return new DemiDroiteOuSegment(this.arrivee,this.direction) ;
    }


}
