package CrazyDiamond.Model;

import java.util.ArrayList;
import java.util.List;
import clipper2.core.PathsD;
import clipper2.Clipper;
import clipper2.core.RectD;

public class ContoursObstacle {


    // Même précision que la classe VisiteurCollecteContours
    private final int precision = 7 ;
    protected ArrayList<Contour> contours_surface ;
    protected ArrayList<Contour> contours_masse ;

    public ContoursObstacle() {
        this(10,20) ;
    }

    public ContoursObstacle(int nombre_contours_surface, int nombre_contours_masse) {
        this.contours_surface = new ArrayList<>(nombre_contours_surface);
        this.contours_masse = new ArrayList<>(nombre_contours_masse);
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

    public boolean intersecte(BoiteLimiteGeometrique boite_limites) {

        RectD boite = new RectD(boite_limites.getMinX(), boite_limites.getMaxY(),boite_limites.getMaxX(),boite_limites.getMinY()) ;

        PathsD subjects_masse = convertirContoursMasseEnPathsClipperFerme() ;

        PathsD res = Clipper.ExecuteRectClip(boite,subjects_masse,precision,false);

        if (res.size()>0)
            return true ;

        PathsD subjects_surface = convertirContoursSurfacesSansMatiereEnPathsClipperOuvert();

        res = Clipper.ExecuteRectClipLines(boite, subjects_surface,precision) ;

        return (res.size()>0) ;

    }

    private PathsD convertirContoursMasseEnPathsClipperFerme() {

        PathsD resultat = new PathsD(contours_masse.size()) ;

        for (Contour c_m : contours_masse)
            resultat.add(c_m.convertirEnPathClipperFerme()) ;

        return resultat ;

    }

    private PathsD convertirContoursSurfacesSansMatiereEnPathsClipperOuvert() {

        PathsD resultat = new PathsD(contours_surface.size()) ;

        for (Contour c_s : contours_surface) {
            if (c_s.nombrePoints()==2) // Les surfaces sans matiere sont formées de segments entre deux points
                resultat.add(c_s.convertirEnPathClipperOuvert());
        }

        return resultat ;

    }


}
