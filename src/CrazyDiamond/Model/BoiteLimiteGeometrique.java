package CrazyDiamond.Model;

import javafx.geometry.BoundingBox;
import javafx.geometry.Point2D;

import java.util.ArrayList;

/**
 * Rectangle droit (horizontal) géométrique
 */
public class BoiteLimiteGeometrique extends BoundingBox {

    protected enum ModeRecherche { PREMIERE, DERNIERE } ;

    public BoiteLimiteGeometrique(double xmin, double ymin, double largeur, double hauteur) {
        super(xmin,ymin,largeur,hauteur);
    }

    public Point2D centre() {
        return new Point2D((getMinX()+getMaxX())/2,(getMinY()+getMaxY())/2 ) ;
    }

    public Point2D coin(Coin c) {
        if (c==Coin.HD) return new Point2D(getMaxX() ,getMaxY()) ;
        if (c==Coin.HG) return new Point2D(getMinX() ,getMaxY()) ;
        if (c==Coin.BG) return new Point2D(getMinX() ,getMinY()) ;
        return new Point2D(getMaxX() ,getMinY()) ;
    }

    public DemiDroiteOuSegment bord(BordRectangle b) {

        double xmin = getMinX() ;
        double xmax = getMaxX() ;
        double ymin = getMinY() ;
        double ymax = getMaxY() ;

        if (b== BordRectangle.HAUT)
            return DemiDroiteOuSegment.construireSegment(xmax,ymax,xmin,ymax) ;

        if (b== BordRectangle.GAUCHE)
            return DemiDroiteOuSegment.construireSegment(xmin,ymax,xmin,ymin) ;

        if (b== BordRectangle.BAS)
            return DemiDroiteOuSegment.construireSegment(xmin,ymin,xmax,ymin) ;

        return DemiDroiteOuSegment.construireSegment(xmax,ymin,xmax,ymax) ;

    }

    public BoiteLimiteGeometrique couper(BoiteLimiteGeometrique zone_visible) {

        if (getMinX()>zone_visible.getMaxX() || getMaxX()<zone_visible.getMinX()) return null ;
        if (getMinY()>zone_visible.getMaxY() || getMaxY()<zone_visible.getMinY()) return null ;

        double nouveau_xmin = Math.max(getMinX(), zone_visible.getMinX()) ;
        double nouveau_ymin = Math.max(getMinY(), zone_visible.getMinY()) ;
        double nouveau_xmax = Math.min(getMaxX(), zone_visible.getMaxX()) ;
        double nouveau_ymax = Math.min(getMaxY(), zone_visible.getMaxY()) ;

        return new BoiteLimiteGeometrique(nouveau_xmin,nouveau_ymin,nouveau_xmax-nouveau_xmin,nouveau_ymax-nouveau_ymin) ;

    }

    /**
     * Trouve la première intersection d'une demi-droite (dans le sens de sa marche) avec la boite limites
     * @param s support geometrique d'un tayon (demi-droite ou segment)
     * @return le point d'intersection
     */
//    public Point2D premiere_intersection(Rayon r) { return cherche_intersection(r,ModeRecherche.PREMIERE) ; }
    public Point2D premiere_intersection(DemiDroiteOuSegment s) { return cherche_intersection(s,ModeRecherche.PREMIERE) ; }
    /**
     * Trouve la dernière intersection d'une demi-droite (dans le sens de sa marche) avec la boite limites
     * @param s upport geometrique d'un tayon (demi-droite ou segment)
     * @return le point d'intersection
     */
//    public Point2D derniere_intersection(Rayon r) {
    public Point2D derniere_intersection(DemiDroiteOuSegment s) {
        return cherche_intersection(s,ModeRecherche.DERNIERE) ;
    }

    protected Point2D cherche_intersection(DemiDroiteOuSegment s, ModeRecherche mode) {
        double xdep = s.depart().getX() ;
        double ydep = s.depart().getY() ;

        double xdir = s.direction().getX() ;
        double ydir = s.direction().getY() ;

        if (this.aSurSaSurface(s.depart())) {
            if ( (xdep == getMinX() && xdir<0) || (xdep==getMaxX() && xdir>0) )
                return null ;
            if ( (ydep == getMinY() && ydir<0) || (ydep==getMaxY() && ydir>0) )
                return null ;
        }

        if (this.contains(s.depart())) {
            // Cas particulier du rayon vertical
            if (xdir == 0)
                if ( ydir>0)
                    return new Point2D(xdep,getMaxY()) ;
                else
                    return new Point2D(xdep,getMinY()) ;

            // Cas particulier du rayon horizontal
            if (ydir == 0)
                if ( xdir>0)
                    return new Point2D(getMaxX(),ydep) ;
                else
                    return new Point2D(getMinX(),ydep) ;

            // Cas général

            double xinter_plafond = (getMaxY()-ydep)*xdir/ydir+xdep ;
            double xinter_sol     = (getMinY()-ydep)*xdir/ydir+xdep ;
            double yinter_droite = (ydir/xdir)*(getMaxX()-xdep) + ydep ;
            double yinter_gauche = (ydir/xdir)*(getMinX()-xdep) + ydep ;


            if (xdir>0 && ydir >0)
                if (xinter_plafond> getMaxX())
                    return new Point2D(getMaxX(),yinter_droite) ;
                else
                    return new Point2D(xinter_plafond,getMaxY()) ;

            if (xdir>0 && ydir <0)
                if (xinter_sol > getMaxX())
                    return new Point2D(getMaxX(),yinter_droite) ;
                else
                    return new Point2D(xinter_sol,getMinY()) ;

            if (xdir<0 && ydir >0)
                if (xinter_plafond<getMinX())
                    return new Point2D(getMinX(),yinter_gauche) ;
                else
                    return new Point2D(xinter_plafond,getMaxY()) ;

            // Sinon (xdir < 0 && ydir < 0)
            if (xinter_sol<getMinX())
                return new Point2D(getMinX(),yinter_gauche) ;
            else
                return new Point2D(xinter_sol,getMinY()) ;

        }
        else { // Point départ n'est pas dans la boite limites ; on part de l'extérieur ; il peut y avoir deux intersections
            // Cas particulier du rayon vertical
            if (xdir == 0.0)
                if ( ydir>0.0)
                    if (xdep>=getMinX() && xdep <=getMaxX() && ydep <= getMinY())
                        if (mode==ModeRecherche.PREMIERE) {
                            if (s.arrivee() == null || s.arrivee().getY() >= getMinY()) //
                            return new Point2D(xdep, getMinY());
                            else
                                return null;
                        } else {
                            if (s.arrivee() ==null  || s.arrivee().getY()>=getMaxY()) //
                                return new Point2D(xdep, getMaxY());
                            else
                                return null ; // On considère que la première intersection ne peut pas servir de dernière intersection
                        }
                    else
                        return null ;
                else // Rayon va vers le bas
                    if (xdep>=getMinX() && xdep <=getMaxX() && ydep >= getMaxY())
                        if (mode==ModeRecherche.PREMIERE) {
                            if (s.arrivee() == null || s.arrivee().getY() <= getMaxY()) //
                                return new Point2D(xdep, getMaxY());
                            else
                                return null;
                        } else {
                            if (s.arrivee() ==null  || s.arrivee().getY() <= getMinY()) //
                                return new Point2D(xdep, getMinY());
                            else
                                return null ; // On considère que la première intersection ne peut pas servir de dernière intersection
                        }
                    else
                        return null ;

            // Cas particulier du rayon horizontal
            if (ydir == 0.0)
                if ( xdir>0.0)
                    if (ydep>=getMinY() && ydep <=getMaxY() && xdep <= getMinX())
                        if (mode==ModeRecherche.PREMIERE) {
                            if (s.arrivee() == null || s.arrivee().getX() >= getMinX()) //
                             return new Point2D(getMinX(), ydep);
                            else
                                return null; //
                        }
                        else {
                            if (s.arrivee() == null || s.arrivee().getX() >= getMaxX()) //
                             return new Point2D(getMaxX(), ydep);
                            else
                                return null ; // On considère que la première intersection ne peut pas servir de dernière intersection
                        }
                    else
                        return null ;
                else // Rayon va vers la gauche
                    if (ydep>=getMinY() && ydep <=getMaxY() && xdep > getMaxX())
                        if (mode==ModeRecherche.PREMIERE) {
                            if (s.arrivee() == null || s.arrivee().getX() <= getMaxX()) //
                            return new Point2D(getMaxX(), ydep);
                            else
                                return null ;
                        }
                        else {
                            if (s.arrivee() == null || s.arrivee().getX() <= getMinX()) //
                            return new Point2D(getMinX(), ydep);
                            else
                                return null ; // On considère que la première intersection ne peut pas servir de dernière intersection
                        }
                    else
                        return null ;

            // Cas général

            if (ydep > getMaxY() && ydir > 0.0)
                return null ;
            if (ydep < getMinY() && ydir < 0.0)
                return null ;

            if (xdep > getMaxX() && xdir > 0.0)
                return null ;
            if (xdep < getMinX() && xdir < 0.0)
                return null ;

            double xinter_plafond = (getMaxY()-ydep)*xdir/ydir+xdep ;
            double xinter_sol     = (getMinY()-ydep)*xdir/ydir+xdep ;
            double yinter_droite = (ydir/xdir)*(getMaxX()-xdep) + ydep ;
            double yinter_gauche = (ydir/xdir)*(getMinX()-xdep) + ydep ;

            ArrayList<Point2D> its = new ArrayList<Point2D>(2) ;

            if (getMinX() <= xinter_plafond && xinter_plafond <= getMaxX())
                if (s.arrivee() == null || (ydir<0 && s.arrivee().getY()<=getMaxY()) || (ydir>0 && s.arrivee().getY()>=getMaxY()) )
                its.add(new Point2D(xinter_plafond, getMaxY()));
            if (getMinX() <= xinter_sol && xinter_sol <= getMaxX())
                if (s.arrivee() == null || (ydir>0 && s.arrivee().getY()>=getMinY()) || (ydir<0 && s.arrivee().getY()<=getMinY()) )
                its.add(new Point2D(xinter_sol, getMinY()));
            if (getMinY() <= yinter_droite  && yinter_droite <= getMaxY())
                if (s.arrivee() == null || (xdir<0 && s.arrivee().getX()<=getMaxX()) || (xdir>0 && s.arrivee().getX()>=getMaxX()) )
                its.add(new Point2D(getMaxX(),yinter_droite));
            if (getMinY() <= yinter_gauche  && yinter_gauche <= getMaxY())
                if (s.arrivee() == null || (xdir>0 && s.arrivee().getX()>=getMinX()) || (xdir<0 && s.arrivee().getX()<=getMinX()))
                its.add(new Point2D(getMinX(),yinter_gauche));

            if (its.size()>2)
                System.err.println("On a un problème.");

            if (its.size()==1)
                return its.get(0) ;

            if (its.size()==2) {

                if (its.get(0).subtract(s.depart()).magnitude()>=its.get(1).subtract(s.depart()).magnitude()) {
                    if (mode==ModeRecherche.PREMIERE)
                        return its.get(1) ;
                    else
                        return its.get(0) ;
                } else {
                    if (mode==ModeRecherche.PREMIERE)
                        return its.get(0) ;
                    else
                        return its.get(1) ;
                }
            }
            return null ;
        }

    }


    public  boolean aSurSaSurface(Point2D p) {
        if ( ( Environnement.quasiEgal(p.getY(),getMinY()) ) || Environnement.quasiEgal(p.getY(),getMaxY() ) )
            if ( (getMinX()<=p.getX()) && (p.getX()<=getMaxX()) )
                return true ;

        if ( ( Environnement.quasiEgal(p.getX(),getMinX()) ) || Environnement.quasiEgal(p.getX(),getMaxX() ) )
            if ( (getMinY()<=p.getY()) && (p.getY()<=getMaxY()) )
                return true ;

        return false ;
    }

    public Contour construireContour() {
        Contour c = new Contour(5) ;

        c.ajoutePoint(getMinX(),getMinY());
        c.ajoutePoint(getMaxX(),getMinY());
        c.ajoutePoint(getMaxX(),getMaxY());
        c.ajoutePoint(getMinX(),getMaxY());
        c.ajoutePoint(getMinX(),getMinY());

        return c ;
    }

    public Contour construireContourAntitrigo() {
        Contour c = new Contour(5);

        c.ajoutePoint(getMinX(),getMinY());
        c.ajoutePoint(getMinX(),getMaxY());
        c.ajoutePoint(getMaxX(),getMaxY());
        c.ajoutePoint(getMaxX(),getMinY());
        c.ajoutePoint(getMinX(),getMinY());

        return c;
    }

    public void completerContourAvecCoinsConsecutifsEntreBordsContenusDansObstacle(BordRectangle b1, BordRectangle b2, Obstacle obst, Contour cont) {

        if (b1 == b2)
            return;

        Coin c = b1.coin_suivant();

        while (true) {
            Point2D pos_coin = coin(c);

//            if (obst.contient(pos_coin))
                cont.ajoutePoint(pos_coin);
//            else
//                return;

            if (c.bord_suivant() == b2)
                return;

            c = c.coin_suivant();
        }

    }
}
