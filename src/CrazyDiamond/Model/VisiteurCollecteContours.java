package CrazyDiamond.Model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

//import de.lighti.clipper.*;
import CrazyDiamond.Controller.CanvasAffichageEnvironnement;
import de.lighti.clipper.Clipper;
import de.lighti.clipper.Clipper.ClipType;
import de.lighti.clipper.Clipper.PolyFillType;
import de.lighti.clipper.DefaultClipper;
import de.lighti.clipper.Path;
import de.lighti.clipper.Paths;
import de.lighti.clipper.Point.LongPoint;

import static CrazyDiamond.Model.Composition.Operateur.DIFFERENCE_SYMETRIQUE;


public class VisiteurCollecteContours implements VisiteurElementAvecMatiere {

    final CanvasAffichageEnvironnement cae;

    private Paths paths;

    // Récupération du logger
    private static final Logger LOGGER = Logger.getLogger( "CrazyDiamond" );

    // Même Fill Rule que le GraphicsContext du CanvasAffichageEnvironnement
    private PolyFillType fillType = PolyFillType.NON_ZERO;

    // Facteur d'echelle pour éviter les erreurs d'arrondis lors de la conversion des coordonnées en entier
    // pour construire le Path (cf. http://www.angusj.com/delphi/clipper/documentation/Docs/Overview/FAQ.htm , "How do I
    // use floating point coordinates with Clipper ?")
    private double facteur_echelle = 1E7 ;

    int nombre_pas_angulaire_par_arc = 800;

    public VisiteurCollecteContours(CanvasAffichageEnvironnement cae) {
        this.cae = cae;
        this.paths = new Paths(1) ;
    }

    public Paths paths() { return paths; }

    // Cette méthode n'est appelée que pour les compositions
    public ContoursObstacle contours() {

        ContoursObstacle co = new ContoursObstacle() ;

        for (Path p : paths) {
               co.ajouterContourMasse(construireContourDepuisPathClipper(p,false));

                // TODO: voir si il y a un moyen de construire plus correctement le contour de surface,
                //  en n'y incluant pas les bords de la zone visible


                // Pour les compositions, il faut toujours fermer le contour de surface car tous les obstacles de la
                // composition ont été préalablement "coupés" (clippés) par la zone visible
                co.ajouterContourSurface(construireContourDepuisPathClipper(p,true));
        }


        return co ;
    }

    private Contour construireContourDepuisPathClipper(Path path,boolean ferme_contour) {

        Contour c = new Contour(path.size()) ;

        for (LongPoint pt : path)
            c.ajoutePoint(pt.getX()/facteur_echelle,pt.getY()/facteur_echelle);

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
        double y = a*x*x + b*x + c ;

//        gc.moveTo(x,y);

        double pas = cae.resolutionX() ;

        int nb_points = (int) Math.round((xmax-xmin)/pas)+1 ;

        ArrayList<Double> xpoints = new ArrayList<Double>(nb_points+4);
        ArrayList<Double> ypoints = new ArrayList<Double>(nb_points+4);

        do {
            x += pas ;
            y = a*x*x + b*x + c ;

//            gc.lineTo(x,y);

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

            final DefaultClipper clp = new DefaultClipper(Clipper.STRICTLY_SIMPLE);

            // Construction du paths subject avec le premier obstacle
            Paths paths_subject = construirePathsObstacle(ito.next()) ;

            if (paths_subject != null)
                clp.addPaths(paths_subject, Clipper.PolyType.SUBJECT, true);
            else
                System.err.println("paths subject est null");

//            System.out.println("paths subject contient "+paths_subject.size()+" paths");


            // Construction du paths du deuxieme objet de la Composition pour initialiser autres_paths
            Paths autres_paths = construirePathsObstacle(ito.next()) ;

            // Compléter avec les autres paths restants
            while (ito.hasNext()) {

                Obstacle obs_suivant = ito.next();

                switch (c.operateur()) {
                    case UNION, DIFFERENCE -> {
                        autres_paths = construireClipPaths(autres_paths, construirePathsObstacle(obs_suivant), ClipType.UNION);
                    }
                    case INTERSECTION -> {
                        autres_paths = construireClipPaths(autres_paths, construirePathsObstacle(obs_suivant), ClipType.INTERSECTION);
                    }
                }
            }

            if (autres_paths != null)
                clp.addPaths(autres_paths, Clipper.PolyType.CLIP, true);
            else
                System.err.println("autres_paths est null ;");

//            System.out.println("autres_paths contient "+autres_paths.size()+" paths");


            ClipType clipType = ClipType.UNION;

            switch (c.operateur()) {
                case UNION -> clipType = ClipType.UNION;
                case INTERSECTION -> clipType = ClipType.INTERSECTION;
                case DIFFERENCE -> clipType = ClipType.DIFFERENCE;
            }

            // Si une partie au moins des paths sujets et clips est visible (sinon clp.execute() renvoie une erreur)
            if (paths_subject.size()>0 || autres_paths.size()>0) {
                // Construire la composition des obstacles, le résultat se mettra directement dans l'attribut paths
                if (!clp.execute(clipType, paths, fillType, fillType))
                    System.err.println("Operation failed");

            }

        } else { // DIFFERENCE SYMETRIQUE

            Paths resultat = new Paths(1) ;

            Iterator<Obstacle> ito = c.elements().iterator();

            Obstacle o_courant ;

            while (ito.hasNext()) {

                o_courant = ito.next() ;

                LOGGER.log(Level.FINE,"Obstacle courant : {0}",o_courant);

                // Initialiser avec un paths vide
                Paths union_autres_paths = new Paths(1) ;

                // Construire l'union de tous les autres paths d'obstacles, excepté celui de l'obstacle o_courant
                for (Obstacle obs : c.elements()) {
                    if (obs!=o_courant) {
                        LOGGER.log(Level.FINE,"Autre obstacle à unir : {0}",obs);

                        if (union_autres_paths.size() == 0)
                            union_autres_paths = construirePathsObstacle(obs) ;
                        else
                            union_autres_paths = construireClipPaths(union_autres_paths, construirePathsObstacle(obs), ClipType.UNION);
                    }
                };

                Paths difference = null ;

                if (union_autres_paths.size()==0)
                    difference = construirePathsObstacle(o_courant) ;
                else
                    // Construire la différence entre l'obstacle courant et tous les autres
                    difference = construireClipPaths(construirePathsObstacle(o_courant), union_autres_paths,ClipType.DIFFERENCE );

                LOGGER.log(Level.FINE,"Différence construite contenant {0} paths",difference.size());

                if (difference.size()>0)
                    // Compléter le résultat en faisant son union avec cette différence
                    resultat = construireClipPaths(resultat,difference,ClipType.UNION) ;

                LOGGER.log(Level.FINE,"Différence unie aux autres contenant {0} paths",resultat.size());
            }

            paths = resultat ;

        }

    }

    private Paths construirePathsObstacle(Obstacle obs) {

        if ( ! (obs instanceof ElementAvecMatiere) )
            return null ;

        VisiteurCollecteContours vcc = new VisiteurCollecteContours(cae) ;

        obs.accepte(vcc);

        return vcc.paths() ;

    }

    private Paths construireClipPaths(Paths paths_a, Paths paths_b, ClipType operation) {

        final DefaultClipper clp = new DefaultClipper(Clipper.STRICTLY_SIMPLE);

        clp.addPaths(paths_a, Clipper.PolyType.SUBJECT, true );
        clp.addPaths(paths_b, Clipper.PolyType.CLIP, true );

        Paths resultat = new Paths(1) ;

        // Construction de l'union des deux Paths, le résultat se mettra directement dans resultat
        if (clp.execute( operation, resultat, fillType, fillType ))
            return resultat  ;

        LOGGER.log(Level.SEVERE,"Erreur lors de l'exécution du clip() pour l'opération : {0}",operation);

        return null ;

    }

    @Override
    public void visiteCompositionDeuxObstacles(CompositionDeuxObstacles c) {

    }

    public Path construirePathClipperFermeDepuisContour(Contour c) {
       return construirePathClipperFermeDepuisCoordonneesContour(c.xpoints,c.ypoints) ;
    }

    public Path construirePathClipperFermeDepuisCoordonneesContour(Collection<Double> xpoints, Collection<Double> ypoints) {

        Path contour = new Path(xpoints.size());

        Iterator<Double> itx = xpoints.iterator();
        Iterator<Double> ity = ypoints.iterator();

        double xdep, ydep;

        if (itx.hasNext() && ity.hasNext()) {
            xdep = itx.next()*facteur_echelle;
            ydep = ity.next()*facteur_echelle;

            contour.add(new LongPoint((long) xdep, (long) ydep));

            while (itx.hasNext() && ity.hasNext())
                contour.add(new LongPoint((long) Math.round(itx.next()*facteur_echelle), (long) Math.round(ity.next()*facteur_echelle)));

            // Fermeture du contour
            if ( (contour.get(contour.size()-1).getX() != (long) xdep) || (contour.get(contour.size()-1).getY() != (long) ydep) )
                contour.add(new LongPoint((long) xdep, (long) ydep));
        }

        return contour;
    }

}
