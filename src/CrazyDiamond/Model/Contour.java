package CrazyDiamond.Model;

import clipper2.core.PathD;
import clipper2.core.PointD;
import javafx.geometry.Point2D;
import java.util.ArrayList;
import java.util.Iterator;

public class Contour {
    ArrayList<Double> xpoints ;
    ArrayList<Double> ypoints ;


    public Contour() {
        this(40) ;
    }

    public Contour(int nombre) {
        xpoints = new ArrayList<>(nombre) ;
        ypoints = new ArrayList<>(nombre) ;
    }

    public Contour(Contour c) {
        xpoints = new ArrayList<>(c.xpoints) ;
        ypoints = new ArrayList<>(c.ypoints) ;

    }

    public void ajoutePoint(Point2D pt) {
        xpoints.add(pt.getX());
        ypoints.add(pt.getY());
    }
    public void ajoutePoint(double x, double y) {
        xpoints.add(x) ;
        ypoints.add(y) ;
    }

    public void ajoutePointDevant(Point2D pt) {
        xpoints.add(0,pt.getX());
        ypoints.add(0,pt.getY());
    }

    public void ajoutePointDevant(double x, double y) {
        xpoints.add(0,x) ;
        ypoints.add(0,y) ;
    }

    public void concatene(Contour c) {
        xpoints.addAll(c.xpoints) ;
        ypoints.addAll(c.ypoints) ;
    }

    public void ferme() {
        if (xpoints.size()<3)
            throw new IllegalStateException("Impossible de fermer un Contour qui n'a pas au moins 3 points.") ;

        if ( (!xpoints.get(xpoints.size() - 1).equals(xpoints.get(0))) || (!ypoints.get(ypoints.size() - 1).equals(ypoints.get(0))) ) {
            ajoutePoint(xpoints.get(0),ypoints.get(0));
        }

    }

    public int nombrePoints() { return xpoints.size() ; }

    public void raz() {
        xpoints.clear();
        ypoints.clear();
    }

    public boolean comporte_point_proche_de(Point2D pclic, double tolerance_pointage) {
        Iterator<Double> itx = xpoints.iterator() ;
        Iterator<Double> ity = ypoints.iterator() ;

        while (itx.hasNext() && ity.hasNext()) {
            double xpoignee = itx.next();
            double ypoignee = ity.next() ;

            if (Math.abs(pclic.getX()-xpoignee)<=tolerance_pointage && Math.abs(pclic.getY()-ypoignee)<=tolerance_pointage)
                return true ;
        }

        return false ;

    }

    public Iterator<Double> iterateurX() {return xpoints.iterator() ; }
    public Iterator<Double> iterateurY() {return ypoints.iterator() ; }

    public PathD convertirEnPathClipperFerme() {

        PathD path = new PathD(xpoints.size());

        Iterator<Double> itx = iterateurX();
        Iterator<Double> ity = iterateurY();

        double xdep, ydep;

        if (itx.hasNext() && ity.hasNext()) {
            xdep = itx.next();
            ydep = ity.next();

            path.add(new PointD(xdep, ydep));

            while (itx.hasNext() && ity.hasNext())
                path.add(new PointD(itx.next(), ity.next()));

            // Fermeture du contour
            if ( (path.get(path.size()-1).x != xdep) || (path.get(path.size()-1).y != ydep) )
                path.add(new PointD(xdep,ydep));
        }

        return path;
    }

    public PathD convertirEnPathClipperOuvert() {

        PathD path = new PathD(xpoints.size());

        Iterator<Double> itx = iterateurX();
        Iterator<Double> ity = iterateurY();

        double xdep, ydep;

        if (itx.hasNext() && ity.hasNext()) {
            xdep = itx.next();
            ydep = ity.next();

            path.add(new PointD(xdep, ydep));

            while (itx.hasNext() && ity.hasNext())
                path.add(new PointD(itx.next(), ity.next()));

        }

        return path;
    }

    Point2D point(int index){
        return new Point2D(xpoints.get(index),ypoints.get(index)) ;
    }

}
