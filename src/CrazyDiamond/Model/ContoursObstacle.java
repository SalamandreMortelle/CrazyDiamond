package CrazyDiamond.Model;

import clipper2.core.*;
import clipper2.engine.ClipperD;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ContoursObstacle {

    private static final Logger LOGGER = Logger.getLogger( "CrazyDiamond" );

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

//        if (boite_limites.getMinX()>boite_limites.getMaxX())
//            return false ;
//        if (boite_limites.getMinY()>boite_limites.getMaxY())
//            return false ;
//
//        if (boite_limites.getMinX()==boite_limites.getMaxX())
//            return false ;
//        if (boite_limites.getMinY()==boite_limites.getMaxY())
//            return false ;


        RectD boite = new RectD(boite_limites.getMinX(), boite_limites.getMinY(),boite_limites.getMaxX(),boite_limites.getMaxY()) ;
//        RectD boite = new RectD(boite_limites.getMinX(), boite_limites.getMaxY(),boite_limites.getMaxX(),boite_limites.getMinY()) ;

        PathsD subjects_masse = convertirContoursMasseEnPathsClipperFerme() ;

        PathsD rect_clips = new PathsD() ;
        PathD rect_clip = new PathD() ;

        rect_clip.add(new PointD(boite_limites.getMinX(), boite_limites.getMinY())) ;
        rect_clip.add(new PointD(boite_limites.getMaxX(), boite_limites.getMinY())) ;
        rect_clip.add(new PointD(boite_limites.getMaxX(), boite_limites.getMaxY())) ;
        rect_clip.add(new PointD(boite_limites.getMinX(), boite_limites.getMaxY())) ;
        rect_clip.add(new PointD(boite_limites.getMinX(), boite_limites.getMinY())) ;

        rect_clips.add(rect_clip) ;

        final ClipperD clp = new ClipperD(precision) ; // Précision à la 7ème décimale

        clp.AddClips(rect_clips);
        clp.AddSubjects(subjects_masse);


        PathsD res = new PathsD() ;
        // Si une partie au moins des paths sujets et clips est visible (sinon clp.execute() renvoie une erreur)
        if ( (subjects_masse !=null && subjects_masse.size()>0) || (rect_clips!=null && rect_clips.size()>0) ) {
            // Construire la composition des stream_obstacles, le résultat se mettra directement dans l'attribut paths
            if (!clp.Execute(ClipType.Intersection, FillRule.NonZero,res))
                LOGGER.log(Level.SEVERE,"Échec du clipping des obstacles par la zone de sélection");
        }

//        PathsD res = null ;
//        try {
//            if ( (subjects_masse !=null && subjects_masse.size()>0) && boite!=null )
//                res = Clipper.ExecuteRectClip(boite, subjects_masse, precision, false);
//            // Ne fonctionne pas, la librairie Clipper2 retourne une exception :
//            // java.lang.NullPointerException: Cannot invoke "clipper2.rectclip.RectClip$Location.ordinal()" because "loc.argValue" is null
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        if (res != null && res.size()>0)
            return true ;


        for (Contour c_s : contours_surface) {
            if (c_s.nombrePoints()==2) // Les surfaces sans matiere sont formées de segments entre deux points
                if (boite_limites.intersecte(DemiDroiteOuSegment.construireSegment(c_s.point(0),c_s.point(1))))
                    return true ;
        }

//        PathsD subjects_surface = convertirContoursSurfacesSansMatiereEnPathsClipperOuvert();
//
//            // Ne fonctionne pas, la librairie Clipper2 retourne une exception :
//            // java.lang.NullPointerException: Cannot invoke "clipper2.rectclip.RectClip$Location.ordinal()" because "loc.argValue" is null
//        res = Clipper.ExecuteRectClipLines(boite, subjects_surface,precision) ;
//        return (res!=null && res.size()>0) ;

        return false ;
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
