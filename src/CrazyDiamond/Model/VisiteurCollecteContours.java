package CrazyDiamond.Model;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import clipper2.core.PointD;
import clipper2.core.PathD;
import clipper2.core.PathsD;
import clipper2.core.FillRule;
import clipper2.core.ClipType;
import clipper2.engine.ClipperD;

import static CrazyDiamond.Model.Composition.Operateur.DIFFERENCE_SYMETRIQUE;

public class VisiteurCollecteContours implements VisiteurElementAvecMatiere {

    final BoiteLimiteGeometrique boite_limites;
    private PathsD paths;

    // Récupération du logger
    private static final Logger LOGGER = Logger.getLogger( "CrazyDiamond" );

    // Même Fill Rule que le GraphicsContext du CanvasAffichageEnvironnement
    private final FillRule fillRule = FillRule.NonZero;

    // Précision (en nombre de décimales) du résultat des opérations de clipping
    private final int precision = 7 ;

    int nombre_pas_angulaire_par_arc = 800;

    public VisiteurCollecteContours(BoiteLimiteGeometrique boite_limites) {
        this.boite_limites = boite_limites;
        this.paths = new PathsD(1);
    }

    public PathsD paths() { return paths; }

    // Cette méthode n'est appelée que pour les compositions (c'est pourquoi elle ne travaille que sur les contours de masse)
    public ContoursObstacle contours(TypeSurface typeSurface) {

        ContoursObstacle co = new ContoursObstacle() ;

        for (PathD p : paths) {
               co.ajouterContourMasse(construireContourDepuisPathClipper(p,false));

                // TODO: voir si il y a un moyen de construire plus correctement le contour de surface,
                //  en n'y incluant pas les bords de la zone visible

                // Pour les compositions, il faut toujours fermer le contour de surface, car tous les obstacles de la
                // composition ont été préalablement "coupés" (clippés) par la zone visible.
                co.ajouterContourSurface(construireContourDepuisPathClipper(p,true));
        }

        if (typeSurface == TypeSurface.CONCAVE) {
            // Tracé du rectangle de la zone visible, dans le sens antitrigo : le Path de la composition sera un trou
            // dans cette zone
            co.ajouterContourMasse(boite_limites.construireContourAntitrigo());
        }

        return co ;
    }

    private Contour construireContourDepuisPathClipper(PathD path,boolean ferme_contour) {

        Contour c = new Contour(path.size()) ;

        for (PointD pt : path)
            c.ajoutePoint(pt.x,pt.y);

        if (ferme_contour)
            c.ferme();

        return c ;

    }

    protected void construirePathsClipperDepuisContourObstacle(ContoursObstacle co) {

        for (Contour c_masse : co.contours_masse)
            paths.add(c_masse.convertirEnPathClipperFerme());


//        for (Contour c_surface : co.contours_surface)
//            tracerContour(c_surface);

    }

    @Override
    public void visiteCercle(Cercle cercle) {

        ContoursObstacle co = cercle.couper(boite_limites, nombre_pas_angulaire_par_arc,false) ;

        construirePathsClipperDepuisContourObstacle(co);

    }

    @Override
    public void visiteRectangle(Rectangle rect) {

        ContoursObstacle co = rect.couper(boite_limites, false) ;

        construirePathsClipperDepuisContourObstacle(co);

    }

    @Override
    public void visitePrisme(Prisme prisme) {

        ContoursObstacle co = prisme.couper(boite_limites, false) ;

        construirePathsClipperDepuisContourObstacle(co);

    }

    @Override
    public void visiteLentille(Lentille lentille) {
        visiteComposition(lentille.composition());
    }

    @Override
    public void visiteConique(Conique conique) {

        ContoursObstacle co = conique.couper(boite_limites, nombre_pas_angulaire_par_arc,false) ;

        construirePathsClipperDepuisContourObstacle(co);

    }

    @Override
    public void visiteDemiPlan(DemiPlan dp) {
        ContoursObstacle co = dp.couper(boite_limites, false) ;

        construirePathsClipperDepuisContourObstacle(co);

    }

    @Override
    public void visiteComposition(Composition c) {

        if (c.elements().size() == 0)
            return;

        if (c.elements().size() == 1) {
            paths = construirePathsObstacle(c.elements().get(0)) ;

            return ;
        }

        if (c.operateur() != DIFFERENCE_SYMETRIQUE) {

            Iterator<Obstacle> ito = c.elements().iterator();

            final ClipperD clp = new ClipperD(precision) ; // Précision à la 4ème décimale

            // Construction du paths subject avec le premier obstacle
            PathsD paths_subject = construirePathsObstacle(ito.next()) ;

            if (paths_subject != null)
                clp.AddSubjects(paths_subject);
            else
                System.err.println("paths subject est null");

//            System.out.println("paths subject contient "+paths_subject.size()+" paths");

            // Construction du paths du deuxième objet de la Composition pour initialiser autres_paths
            PathsD autres_paths = construirePathsObstacle(ito.next()) ;

            // Compléter avec les autres paths restants
            while (ito.hasNext()) {

                Obstacle obs_suivant = ito.next();

                switch (c.operateur()) {
                    case UNION, DIFFERENCE -> autres_paths = construireClipPaths(autres_paths, construirePathsObstacle(obs_suivant), ClipType.Union);
                    case INTERSECTION -> autres_paths = construireClipPaths(autres_paths, construirePathsObstacle(obs_suivant), ClipType.Intersection);
                }
            }

            if (autres_paths != null)
                clp.AddClips(autres_paths);
            else
                LOGGER.log(Level.WARNING,"autres_paths est null");

//            System.out.println("autres_paths contient "+autres_paths.size()+" paths");

            ClipType clipType = ClipType.Union;

            switch (c.operateur()) {
                case UNION -> clipType = ClipType.Union;
                case INTERSECTION -> clipType = ClipType.Intersection;
                case DIFFERENCE -> clipType = ClipType.Difference;
            }

            // Si une partie au moins des paths sujets et clips est visible (sinon clp.execute() renvoie une erreur)
            if ( (paths_subject !=null && paths_subject.size()>0) || (autres_paths!=null && autres_paths.size()>0) ) {
                // Construire la composition des obstacles, le résultat se mettra directement dans l'attribut paths
                if (!clp.Execute(clipType, fillRule,paths))
                    LOGGER.log(Level.SEVERE,"Échec du clipping de la composition");
            }

        } else { // DIFFERENCE SYMETRIQUE

            PathsD resultat = new PathsD(1) ;

            Iterator<Obstacle> ito = c.elements().iterator();

            Obstacle o_courant ;

            while (ito.hasNext()) {

                o_courant = ito.next() ;

                LOGGER.log(Level.FINE,"Obstacle courant : {0}",o_courant);

                // Initialiser avec un paths vide
                PathsD union_autres_paths = new PathsD(1) ;

                // Construire l'union de tous les autres paths d'obstacles, excepté celui de l'obstacle o_courant
                for (Obstacle obs : c.elements()) {
                    if (obs!=o_courant) {
                        LOGGER.log(Level.FINE,"Autre obstacle à unir : {0}",obs);

                        if (union_autres_paths != null && union_autres_paths.size() == 0)
                            union_autres_paths = construirePathsObstacle(obs) ;
                        else
                            union_autres_paths = construireClipPaths(union_autres_paths, construirePathsObstacle(obs), ClipType.Union);
                    }
                }

                PathsD difference;

                if (union_autres_paths!=null && union_autres_paths.size()==0)
                    difference = construirePathsObstacle(o_courant) ;
                else
                    // Construire la différence entre l'obstacle courant et tous les autres
                    difference = construireClipPaths(construirePathsObstacle(o_courant), union_autres_paths,ClipType.Difference );

                LOGGER.log(Level.FINE,"Différence construite contenant {0} paths",difference.size());

                if (difference!=null && difference.size()>0)
                    // Compléter le résultat en faisant son union avec cette différence
                    resultat = construireClipPaths(resultat,difference,ClipType.Union) ;

                LOGGER.log(Level.FINE,"Différence unie aux autres contenant {0} paths", resultat.size());
            }

            paths = resultat ;

        }

    }

    private PathsD construirePathsObstacle(Obstacle obs) {

        // Méthode appelée uniquement dans visisteComposition. Donc, comme les éléments sans matière (segments) ne peuvent
        // apparaître dans une composition, on n'a pas à les gérer
        if ( ! (obs instanceof ElementAvecMatiere) )
            return null ;

        VisiteurCollecteContours vcc = new VisiteurCollecteContours(boite_limites) ;

        obs.accepte(vcc);

        return vcc.paths() ;

    }

    private PathsD construireClipPaths(PathsD paths_a, PathsD paths_b, ClipType operation) {

        final ClipperD clp = new ClipperD(precision);

        clp.AddSubjects(paths_a);
        clp.AddClips(paths_b);

        PathsD resultat = new PathsD(1) ;

        // Construction de l'union des deux Paths, le résultat se mettra directement dans resultat
        if (clp.Execute( operation,  fillRule, resultat))
            return resultat  ;

        LOGGER.log(Level.SEVERE,"Erreur lors de l'exécution du clip() pour l'opération : {0}",operation);

        return null ;
    }

    @Override
    public void visiteCompositionDeuxObstacles(CompositionDeuxObstacles c) {
    }

}
