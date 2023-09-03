package CrazyDiamond.Model;

import java.util.ArrayList;
import java.util.List;

public class ContoursObstacle {

    protected ArrayList<Contour> contours_surface ;
    protected ArrayList<Contour> contours_masse ;

    public ContoursObstacle() {
        this(10,20) ;
    }

    public ContoursObstacle(int nombre_contours_surface, int nombre_contours_masse) {
        this.contours_surface = new ArrayList<Contour>(nombre_contours_surface);
        this.contours_masse = new ArrayList<Contour>(nombre_contours_masse);
    }

    public void ajouterContourSurface(Contour c) {
        contours_surface.add(c) ;
    }

    public void ajouterContourMasse(Contour c) {
        contours_masse.add(c) ;
    }

    public void effacerContoursSurface() {
        contours_surface.clear();
    }

    public void effacerContoursMasse() {
        contours_masse.clear();
    }

    public List<Contour> contoursSurface() { return contours_surface ; }
    public List<Contour> contoursMasse() { return contours_masse ; }

    public void effacerContours() {
        effacerContoursSurface();
        effacerContoursMasse();
    }


    // TODO : ajouter méthodes pour convertir les contours en Path, ou en instructions de traçage/remplissage dans le Canvas

}
