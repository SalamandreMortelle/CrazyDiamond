package CrazyDiamond.Model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import CrazyDiamond.Controller.CanvasAffichageEnvironnement;

import clipper2.core.*;
import clipper2.engine.ClipperD;

import static CrazyDiamond.Model.Composition.Operateur.DIFFERENCE_SYMETRIQUE;

public class VisiteurCollecteContours implements VisiteurElementAvecMatiere {

    final CanvasAffichageEnvironnement cae;
    private PathsD paths;

    // Récupération du logger
    private static final Logger LOGGER = Logger.getLogger( "CrazyDiamond" );

    // Même Fill Rule que le GraphicsContext du CanvasAffichageEnvironnement
    private final FillRule fillRule = FillRule.NonZero;

    // Précision (en nombre de décimales) du résultat des opérations de clipping
    private final int precision = 7 ;

    int nombre_pas_angulaire_par_arc = 800;

    public VisiteurCollecteContours(CanvasAffichageEnvironnement cae) {
        this.cae = cae;
        this.paths = new PathsD(1);
    }

    public PathsD paths() { return paths; }

    // Cette méthode n'est appelée que pour les compositions
    public ContoursObstacle contours() {

        ContoursObstacle co = new ContoursObstacle() ;

        for (PathD p : paths) {
               co.ajouterContourMasse(construireContourDepuisPathClipper(p,false));

                // TODO: voir si il y a un moyen de construire plus correctement le contour de surface,
                //  en n'y incluant pas les bords de la zone visible


                // Pour les compositions, il faut toujours fermer le contour de surface car tous les obstacles de la
                // composition ont été préalablement "coupés" (clippés) par la zone visible
                co.ajouterContourSurface(construireContourDepuisPathClipper(p,true));
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
            paths.add(construirePathClipperFermeDepuisContour(c_masse));

//        for (Contour c_surface : co.contours_surface)
//            tracerContour(c_surface);

    }

    @Override
    public void visiteCercle(Cercle cercle) {

        ContoursObstacle co = cercle.couper(cae.boite_limites(), nombre_pas_angulaire_par_arc,false) ;

        construirePathsClipperDepuisContourObstacle(co);

    }

    @Override
    public void visiteRectangle(Rectangle rect) {

        ContoursObstacle co = rect.couper(cae.boite_limites(), false) ;

        construirePathsClipperDepuisContourObstacle(co);

    }

    @Override
    public void visitePrisme(Prisme prisme) {

        ContoursObstacle co = prisme.couper(cae.boite_limites(), false) ;

        construirePathsClipperDepuisContourObstacle(co);

    }

    @Override
    public void visiteConique(Conique conique) {

        ContoursObstacle co = conique.couper(cae.boite_limites(), nombre_pas_angulaire_par_arc,false) ;

        construirePathsClipperDepuisContourObstacle(co);

    }

    @Override
    public void visiteDemiPlan(DemiPlan dp) {
        ContoursObstacle co = dp.couper(cae.boite_limites(), false) ;

        construirePathsClipperDepuisContourObstacle(co);

    }

    @Override
    public void visiteParabole(Parabole para) {

        double a = para.a.get() ;
        double b = para.b.get() ;
        double c = para.c.get() ;
        TypeSurface type = para.typeSurface() ;

        double xmin = cae.xmin();
        double xmax = cae.xmax();
        double ymin = cae.ymin();
        double ymax = cae.ymax();

        double x = xmin ;
        double y ;

        double pas = cae.resolutionX() ;

        int nb_points = (int) Math.round((xmax-xmin)/pas)+1 ;

        ArrayList<Double> xpoints = new ArrayList<>(nb_points+4);
        ArrayList<Double> ypoints = new ArrayList<>(nb_points+4);

        do {
            x += pas ;
            y = a*x*x + b*x + c ;

            xpoints.add(x) ;
            ypoints.add(y) ;
        } while (x< xmax) ;

        if ( ( (a>0) && type == TypeSurface.CONCAVE ) || ( (a<0) && type == TypeSurface.CONVEXE ) ) {
            // On remplit en dessous

            xpoints.add(xmax) ;
            ypoints.add(a* xmax* xmax+b* xmax+c) ;

            xpoints.add(xmax) ;
            ypoints.add(ymin) ;

            xpoints.add(xmin);
            ypoints.add(ymin);

            xpoints.add(xmin) ;
            ypoints.add(a* xmin* xmin+b* xmin+c) ;

            // Remettre dans le sens trigo (peut-être pas strictement nécessaire)
            Collections.reverse(xpoints);
            Collections.reverse(ypoints);

        } else {
            // On remplit au-dessus
            xpoints.add(xmax) ;
            ypoints.add(a* xmax* xmax+b* xmax+c) ;

            xpoints.add(xmax) ;
            ypoints.add(ymax) ;

            xpoints.add(xmin);
            ypoints.add(ymax);

            xpoints.add(xmin) ;
            ypoints.add(a* xmin* xmin+b* xmin+c) ;
        }

        paths.add(construirePathClipperFermeDepuisCoordonneesContour(xpoints,ypoints)) ;

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
            if (paths_subject.size()>0 || autres_paths.size()>0) {
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

                        if (union_autres_paths.size() == 0)
                            union_autres_paths = construirePathsObstacle(obs) ;
                        else
                            union_autres_paths = construireClipPaths(union_autres_paths, construirePathsObstacle(obs), ClipType.Union);
                    }
                }

                PathsD difference = null ;

                if (union_autres_paths.size()==0)
                    difference = construirePathsObstacle(o_courant) ;
                else
                    // Construire la différence entre l'obstacle courant et tous les autres
                    difference = construireClipPaths(construirePathsObstacle(o_courant), union_autres_paths,ClipType.Difference );

                LOGGER.log(Level.FINE,"Différence construite contenant {0} paths",difference.size());

                if (difference.size()>0)
                    // Compléter le résultat en faisant son union avec cette différence
                    resultat = construireClipPaths(resultat,difference,ClipType.Union) ;

                LOGGER.log(Level.FINE,"Différence unie aux autres contenant {0} paths",resultat.size());
            }

            paths = resultat ;

        }

    }

    private PathsD construirePathsObstacle(Obstacle obs) {

        if ( ! (obs instanceof ElementAvecMatiere) )
            return null ;

        VisiteurCollecteContours vcc = new VisiteurCollecteContours(cae) ;

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

    public PathD construirePathClipperFermeDepuisContour(Contour c) {
       return construirePathClipperFermeDepuisCoordonneesContour(c.xpoints,c.ypoints) ;
    }

    public PathD construirePathClipperFermeDepuisCoordonneesContour(Collection<Double> xpoints, Collection<Double> ypoints) {

        PathD contour = new PathD(xpoints.size());

        Iterator<Double> itx = xpoints.iterator();
        Iterator<Double> ity = ypoints.iterator();

        double xdep, ydep;

        if (itx.hasNext() && ity.hasNext()) {
            xdep = itx.next();
            ydep = ity.next();

            contour.add(new PointD(xdep, ydep));

            while (itx.hasNext() && ity.hasNext())
                contour.add(new PointD(itx.next(), ity.next()));

            // Fermeture du contour
            if ( (contour.get(contour.size()-1).x != xdep) || (contour.get(contour.size()-1).y != ydep) )
                contour.add(new PointD(xdep,ydep));
        }

        return contour;
    }

}
