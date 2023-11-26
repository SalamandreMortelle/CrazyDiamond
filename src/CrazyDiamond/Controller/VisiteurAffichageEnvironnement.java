package CrazyDiamond.Controller;

import CrazyDiamond.Model.*;
import javafx.animation.AnimationTimer;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.transform.Affine;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VisiteurAffichageEnvironnement implements VisiteurEnvironnement {

    CanvasAffichageEnvironnement cae;

    // AnimationTimer utilisé pour animer les contours des obstacles sélectionnés, des rayons, etc.
    AnimationTimer anim_timer ;

    private  Map<Obstacle, ContoursObstacle> contours_obstacles ;

    private static final Logger LOGGER = Logger.getLogger( "CrazyDiamond" );


    int nombre_pas_angulaire_par_arc = 300 ;

    private final Font fonte_labels = Font.font("Serif", FontPosture.ITALIC, -1);

    // Marge des labels sur l'axe X
    private final double marge_label_x = 5d;

    // Marge des labels sur l'axe Y
    private final double marge_label_y = -5d;
    private final int facteur_zoom_label = 2;


    public VisiteurAffichageEnvironnement(CanvasAffichageEnvironnement cae) {

        if (cae==null)
            throw new IllegalArgumentException("Le CanvasAffichageEnvironnement du visiteur d'affichage ne peut pas être 'null'.") ;

        this.cae = cae;

        contours_obstacles = new HashMap<>() ;

        anim_timer = new AnimationTimer() {

            long dernierNanoTime = 0 ;

            @Override
            public void handle(long now) {

//                float secondesDepuisDerniereFrame =0f ;
//
//                if (dernierNanoTime != 0)
//                    secondesDepuisDerniereFrame =  ((now - dernierNanoTime)/1e9) ;
//
//                dernierNanoTime = now ;
//
                Obstacle os = cae.obstacleSelectionne() ;
                Source ss = cae.sourceSelectionnee() ;
                SystemeOptiqueCentre soc = cae.systemeOptiqueCentreSelectionne() ;

                if (ss!=null) {
                    afficheSelectionSource(ss, now) ;
                } else if (os != null) {
                    afficheSelectionObstacle(os, now) ;
                } else if (soc != null) {
                    afficheSelectionSystemeOptiqueCentre(soc, now) ;
                }


            }

        } ;

        anim_timer.start();

    }




    @Override
    public void visiteEnvironnement(Environnement e) {

        // Ré-initialiser les contours déjà calculés, pour ne pas garer les contours d'objets qui ont été supprimés
        // de l'Environnement
        contours_obstacles.clear();

        // NB : On pourrait envisager une optimisation en mémorisant les (morceaux de) contours précédemment calculés
        // de chaque obstacle, et en se contentant de calculer, si besoin, les nouveaux morceaux manquants lorsque le visiteur
        // est appelé pour ré-afficher à nouveau l'obstacle.
        // Il faudrait alors que la map contienne des java.lang.ref.WeakReference de chaque Obstacle (au lieu de références simples),
        // afin que le GC puisse effacer ceux qui ont été supprimés de l'environnement et qui ne seront plus jamais affichés, si on veut
        // éviter une fuite mémoire.

    }



    @Override
    public void avantVisiteSources() {
        VisiteurEnvironnement.super.avantVisiteSources();

        cae.gc().setGlobalAlpha(0.6);

        cae.gc().setGlobalBlendMode(BlendMode.HARD_LIGHT);

        cae.gc().setLineJoin(StrokeLineJoin.ROUND);

        cae.gc().setLineWidth(2*cae.resolution());
    }

    @Override
    public void apresVisiteSources() {
        VisiteurEnvironnement.super.apresVisiteSources();

        cae.gc().setLineWidth(cae.resolution());

        cae.gc().setGlobalBlendMode(BlendMode.SRC_OVER);

        cae.gc().setLineJoin(StrokeLineJoin.MITER);

        cae.gc().setGlobalAlpha(1.0);
    }

    // Affiche tous les chemins lumiere émis par la Source s. Les chemins doivent avoir été préalablement calculés
    // par un appel à Source.illuminer()
    @Override
    public void visiteSource(Source s) {

        Iterator<CheminLumiere> itc = s.iterateur_chemins() ;

        if (itc==null)
            return;

        while (itc.hasNext()) {
            tracerChemin(itc.next());
        }
    }

    @Override
    public void visiteSegment(Segment seg) {
        GraphicsContext gc = cae.gc();

        Paint s = gc.getStroke();
        gc.setStroke(seg.couleurContour());

        // TODO : A optimiser pour ne tracer que dans la zone visible (rechercher les intersections du segment avec les bords)

//        double lw = gc.getLineWidth() ;
//        gc.setLineWidth(2*eg.resolution);

        if (seg.rayonDiaphragmeProperty().get()==0d) {
            gc.strokeLine(seg.x1(), seg.y1(), seg.x2(), seg.y2());

            ContoursObstacle co = new ContoursObstacle();
            Contour c_surf = new Contour();

            c_surf.ajoutePoint(seg.x1(), seg.y1());
            c_surf.ajoutePoint(seg.x2(), seg.y2());
            co.ajouterContourSurface(c_surf);

            contours_obstacles.put(seg, co);
        } else { // Présence d'une pupille dans le segment
            Point2D dep = seg.depart() ;
            Point2D dep_pup = seg.departPupille() ;
            Point2D arr = seg.arrivee() ;
            Point2D arr_pup = seg.arriveePupille() ;
            gc.strokeLine(dep.getX(),dep.getY(),dep_pup.getX(),dep_pup.getY());
            gc.strokeLine(arr_pup.getX(),arr_pup.getY(),arr.getX(),arr.getY());

            ContoursObstacle co = new ContoursObstacle();
            Contour c_surf = new Contour();

            c_surf.ajoutePoint(dep);
            c_surf.ajoutePoint(dep_pup);
            co.ajouterContourSurface(c_surf);

            c_surf = new Contour() ;
            c_surf.ajoutePoint(arr_pup);
            c_surf.ajoutePoint(arr);
            co.ajouterContourSurface(c_surf);

            contours_obstacles.put(seg, co);

        }

//        gc.setLineWidth(lw);
        gc.setStroke(s);

    }

    @Override
    public void visiteParabole(Parabole para) {
        GraphicsContext gc = cae.gc() ;

        Paint s = gc.getStroke() ;
        gc.setStroke(para.couleurContour());

        double a = para.a() ;
        double b = para.b() ;
        double c = para.c() ;
        TypeSurface type = para.typeSurface() ;

        double x = cae.xmin() ;
        double y = a*x*x + b*x + c ;

        gc.beginPath() ;

        gc.moveTo(x,y);

        double pas = cae.resolutionX() ;

        ArrayList<Double> xpoints = new ArrayList<Double>(0);
        ArrayList<Double> ypoints = new ArrayList<Double>(0);


        do {
            x += pas ;
            y = a*x*x + b*x + c ;

            gc.lineTo(x,y);

            xpoints.add(x) ;
            ypoints.add(y) ;
        } while (x< cae.xmax()) ;

        if ( ( (a>0) && type == TypeSurface.CONCAVE ) || ( (a<0) && type == TypeSurface.CONVEXE ) ) {
            // On remplit en dessous

            xpoints.add(cae.xmax()) ;
            ypoints.add(a* cae.xmax()* cae.xmax()+b* cae.xmax()+c) ;

            xpoints.add(cae.xmax()) ;
            ypoints.add(cae.ymin()) ;

            xpoints.add(cae.xmin());
            ypoints.add(cae.ymin());

            xpoints.add(cae.xmin()) ;
            ypoints.add(a* cae.xmin()* cae.xmin()+b* cae.xmin()+c) ;
        } else {
            // On remplit au-dessus
            xpoints.add(cae.xmax()) ;
            ypoints.add(a* cae.xmax()* cae.xmax()+b* cae.xmax()+c) ;

            xpoints.add(cae.xmax()) ;
            ypoints.add(cae.ymax()) ;

            xpoints.add(cae.xmin());
            ypoints.add(cae.ymax());

            xpoints.add(cae.xmin()) ;
            ypoints.add(a* cae.xmin()* cae.xmin()+b* cae.xmin()+c) ;
        }

        Paint pf = gc.getFill() ;

        gc.setFill(para.couleurMatiere());

        CanvasAffichageEnvironnement.remplirPolygone(cae,xpoints,ypoints);

        gc.setFill(pf);

        // Dessin du contour de la parabole
        gc.stroke();

        gc.setStroke(s);
        // Note : on pourrait aussi utiliser gc.save() au début de la méthode puis gc.restore() à la fin

    }


    //    public ReflexionParabolique.VisiteurAffichageEnvironnement.Contour extraire_sommets_cercle(Cercle c) {
//        ReflexionParabolique.VisiteurAffichageEnvironnement.Contour sommets = new ReflexionParabolique.VisiteurAffichageEnvironnement.Contour() ;
//
//        // TODO...
//
//        return sommets ;
//    }

    @Override
    public void visiteCercle(Cercle cercle) {

        GraphicsContext gc = cae.gc() ;

        Paint s = gc.getStroke() ;
        Paint pf = gc.getFill() ;

        Paint couleur_masse = cercle.couleurMatiere() ;
        Paint couleur_bord = cercle.couleurContour() ;

        gc.setStroke(couleur_bord);
        gc.setFill(couleur_masse);

        ContoursObstacle co = cercle.couper(cae.boite_limites(), nombre_pas_angulaire_par_arc) ;

        contours_obstacles.put(cercle,co) ;

        cae.afficherContoursObstacle(co) ;

        // TODO : on pourrait aussi utiliser un gc.restore() (précédé d'un gc.save() en début de méthode)
        gc.setFill(pf);
        gc.setStroke(s);

    }

    public void afficheSelectionObstacle(Obstacle o, long temps) {

        ContoursObstacle co = contours_obstacles.get(o) ;

        // Si les contours de l'obstacle ne sont pas déjà calculés, ne rien faire
        if (co == null)
            return ;

        GraphicsContext gc = cae.gc() ;
        Paint pf = gc.getFill() ;

//        Paint couleur_masse = o.couleurMatiere() ;
//        gc.setFill(couleur_masse);

        Paint s = gc.getStroke() ;
        double lw = gc.getLineWidth() ;
        double pas = cae.resolution() ;
        gc.setLineWidth(2*pas);

        // Intervalle clignotement en nanosecondes ;
        long intervalle_clignotement = 1000000000/4 ;

        gc.setLineDashes(5*pas,10*pas);

        if ((temps/intervalle_clignotement)%2==0) {
            gc.setStroke(Color.WHITE);
            gc.setFill(Color.BLACK);

        }
        else {
            gc.setStroke(Color.BLACK);
            gc.setFill(Color.WHITE);

        }

//        double[] dashes = gc.getLineDashes()  ;

        cae.afficherContourSurfaceObstacle(co) ;

        gc.setLineDashes(null);
        gc.setLineWidth(lw);

        cae.afficherPoignees(o.positions_poignees());

        gc.setFill(pf);
        gc.setStroke(s);
    }

    private void afficheSelectionSource(Source src, long temps) {
        GraphicsContext gc = cae.gc() ;
        Paint pf = gc.getFill() ;

//        Paint couleur_masse = o.couleurMatiere() ;
//        gc.setFill(couleur_masse);

        Paint s = gc.getStroke() ;
        double lw = gc.getLineWidth() ;
        double pas = cae.resolution() ;
        gc.setLineWidth(1*pas);

        // Intervalle clignotement en nanosecondes ;
        long intervalle_clignotement = 1000000000/4 ;

        gc.setLineDashes(5*pas,10*pas);

        if ((temps/intervalle_clignotement)%2==0) {
            gc.setStroke(Color.WHITE);
            gc.setFill(Color.BLACK);

        }
        else {
            gc.setStroke(Color.BLACK);
            gc.setFill(Color.WHITE);

        }

//        double[] dashes = gc.getLineDashes()  ;

        if (src.type()== Source.TypeSource.PROJECTEUR) {
//            cae.afficherContourSurfaceObstacle(co);
            Point2D vect_perp = new Point2D(-src.direction().getY(),src.direction().getX()).normalize() ;

//            double x1 = src.xPosition()-0.5* src.largeurProjecteur()*vect_perp.getX() ;
//            double y1 = src.yPosition()-0.5* src.largeurProjecteur()*vect_perp.getY() ;
            Point2D p1 = src.position().subtract(vect_perp.multiply(0.5d*src.largeurProjecteur())) ;

//            double x2 = src.xPosition()+0.5* src.largeurProjecteur()*vect_perp.getX() ;
//            double y2 = src.yPosition()+0.5* src.largeurProjecteur()*vect_perp.getY() ;
            Point2D p2 = src.position().add(vect_perp.multiply(0.5d*src.largeurProjecteur())) ;

//            gc.strokeLine(x1,y1,x2,y2);
            gc.strokeLine(p1.getX(),p1.getY(),p2.getX(),p2.getY());

        }

        gc.setLineDashes(null);
        gc.setLineWidth(lw);

        cae.afficherPoignees(src.positions_poignees());

        gc.setFill(pf);
        gc.setStroke(s);

    }

    private void afficheSelectionSystemeOptiqueCentre(SystemeOptiqueCentre soc, long temps) {
        GraphicsContext gc = cae.gc() ;
        Paint pf = gc.getFill() ;


        Paint s = gc.getStroke() ;
        double lw = gc.getLineWidth() ;
        double pas = cae.resolution() ;
        gc.setLineWidth(1*pas);

        // Intervalle clignotement en nanosecondes ;
        long intervalle_clignotement = 1000000000/4 ;

//        gc.setLineDashes(5*pas,10*pas);

        if ((temps/intervalle_clignotement)%2==0) {
            gc.setStroke(Color.WHITE);
            gc.setFill(Color.BLACK);

        }
        else {
            gc.setStroke(Color.BLACK);
            gc.setFill(Color.WHITE);

        }

        Contour c = soc.couper(cae.boite_limites()) ;

        if (c!=null) {

            double res = cae.resolution() ;

            Point2D origine = soc.origine();
            Point2D perp = soc.perpendiculaireDirection();

            gc.strokeLine(origine.getX() + 10 * res * perp.getX(), origine.getY() + 10 * res * perp.getY(),
                    origine.getX() - 10 * res * perp.getX(), origine.getY() - 10 * res * perp.getY());

            gc.setLineDashes(12 * res, 6 * res, 4 * res, 6 * res);

            cae.tracerContour(c);

            gc.setLineDashes();
        }


//        gc.setLineDashes();
        gc.setLineWidth(lw);

        cae.afficherPoignees(soc.positions_poignees());

        gc.setFill(pf);
        gc.setStroke(s);

    }


    @Override
    public void visiteDemiPlan(DemiPlan dp) {
        GraphicsContext gc = cae.gc() ;

        Paint s = gc.getStroke() ;
        Paint pf = gc.getFill() ;

        Paint couleur_masse = dp.couleurMatiere() ;
        Paint couleur_bord  = dp.couleurContour() ;

        gc.setStroke(couleur_bord);
        gc.setFill(couleur_masse);

        ContoursObstacle co = dp.couper(cae.boite_limites()) ;

        contours_obstacles.put(dp,co) ;

        cae.afficherContoursObstacle(co) ;

        // TODO : on pourrait aussi utiliser un gc.restore() (précédé d'un gc.save() en début de méthode)
        gc.setFill(pf);
        gc.setStroke(s);

    }

    public void afficherContoursObstacle(ContoursObstacle co) {

    }

//    @Override
//    public void visiteCercle(Cercle cercle) {
//
//        GraphicsContext gc = cae.gc() ;
//
//        Paint s = gc.getStroke() ;
//        Paint pf = gc.getFill() ;
//
//        Paint couleur_masse = cercle.couleurMatiere() ;
//        Paint couleur_bord = cercle.couleurContour() ;
//
//        gc.setStroke(couleur_bord);
//        gc.setFill(couleur_masse);
//
//        double rayon = cercle.rayon() ;
//
//        double x_centre = cercle.Xcentre() ;
//        double y_centre = cercle.Ycentre() ;
//
//
//        if ( (x_centre+rayon < cae.xmin()) || (x_centre-rayon > cae.xmax())
//                || (y_centre+rayon < cae.ymin()) || (y_centre-rayon > cae.ymax())
//                || (x_centre< cae.xmin() && y_centre> cae.ymax() && (cercle.centre().subtract(cae.xmin(), cae.ymax()).magnitude()>rayon))
//                || (x_centre> cae.xmax() && y_centre> cae.ymax() && (cercle.centre().subtract(cae.xmax(), cae.ymax()).magnitude()>rayon))
//                || (x_centre> cae.xmax() && y_centre< cae.ymin() && (cercle.centre().subtract(cae.xmax(), cae.ymin()).magnitude()>rayon))
//                || (x_centre< cae.xmin() && y_centre< cae.ymin() && (cercle.centre().subtract(cae.xmin(), cae.ymin()).magnitude()>rayon))
//        ) { // Aucune partie du contour du cercle n'est visible
//
//            if (cercle.typeSurface() == Obstacle.TypeSurface.CONVEXE)
//                return ;
//            else  // Concave
//                cae.gc.fillRect(cae.xmin(), cae.ymin(), cae.xmax() - cae.xmin(), cae.ymax()- cae.ymin());
//
//        }
//
//        double[][] i_droites = cercle.intersections_verticale(cae.xmax(), cae.ymin(), cae.ymax(),true) ;
//        double[][] i_hautes  = cercle.intersections_horizontale(cae.ymax(), cae.xmin(), cae.xmax(),false) ;
//        double[][] i_gauches = cercle.intersections_verticale(cae.xmin(), cae.ymin(), cae.ymax(),false) ;
//        double[][] i_basses  = cercle.intersections_horizontale(cae.ymin(), cae.xmin(), cae.xmax(),true) ;
//
//        SelecteurCoins sc = new SelecteurCoins(cae.xmin(), cae.ymin(), cae.xmax(), cae.ymax());
//
////        System.out.println("Nombre d'intersections avec les bords : "+n_intersections);
//
//        // Tableau qui contiendra au plus 4 intervalles [theta min, theta max] où la courbe est visible
//        // ordonnés dans le sens trigonométrique en partant dy coin BD de l'écran
//        ArrayList<Double> valeurs_theta_intersection = new ArrayList<Double>(8) ;
//
//        ArrayList<Double> valeurs_x_intersection = new ArrayList<Double>(8) ;
//        ArrayList<Double> valeurs_y_intersection = new ArrayList<Double>(8) ;
//
//        // DONE : remplacer les 40 lignes ci-dessous par une sous-fonction (utilisée par cette méthode et par traace_conique_methode2_simplifie)
//
//        int n_intersections = sc.ordonneIntersections(i_droites,i_hautes,i_gauches,i_basses,
//                                valeurs_theta_intersection,valeurs_x_intersection,valeurs_y_intersection) ;
//
////        int n_intersections = i_hautes.length + i_gauches.length +i_basses.length + i_droites.length ;
////
////        for (int i = 0 ; i < i_droites.length ; i++) {
////            valeurs_theta_intersection.add(i_droites[i][1]);
////            valeurs_x_intersection.add(cae.xmax()) ;
////            valeurs_y_intersection.add(i_droites[i][0]) ;
////        }
////        for (int i = 0 ; i < i_hautes.length ; i++) {
////            valeurs_theta_intersection.add(i_hautes[i][1]);
////            valeurs_x_intersection.add(i_hautes[i][0]) ;
////            valeurs_y_intersection.add(cae.ymax()) ;
////        }
////        for (int i = 0 ; i < i_gauches.length ; i++) {
////            valeurs_theta_intersection.add(i_gauches[i][1]);
////            valeurs_x_intersection.add(cae.xmin()) ;
////            valeurs_y_intersection.add(i_gauches[i][0]) ;
////        }
////        for (int i = 0 ; i < i_basses.length ; i++) {
////            valeurs_theta_intersection.add(i_basses[i][1]);
////            valeurs_x_intersection.add(i_basses[i][0]) ;
////            valeurs_y_intersection.add(cae.ymin()) ;
////        }
////
////        if (n_intersections!=valeurs_theta_intersection.size())
////            System.err.println("On a un problème");
//
//        // Si aucune intersection, --ou si 1 seule intersection (TODO : tester le cas à 1 intersection)
//        if (n_intersections<=1) {
//
//            // Cercle entièrement contenu dans la zone visible ?
//            if (cae.boite_limites().contains(cercle.point_sur_cercle(0))) {
//                ArrayList<Double> x_arc = cercle.xpoints_sur_cercle( 0, 2 * Math.PI, nombre_pas_angulaire_par_arc);
//                ArrayList<Double> y_arc = cercle.ypoints_sur_cercle( 0, 2 * Math.PI, nombre_pas_angulaire_par_arc);
//
//                // Rappel : on est par défaut en FillRule NON_ZERO => pour faire une surface avec un trou, il suffit
//                // de faire deux contours dans des sens contraires (trigo et antitrigo)
//                cae.gc.beginPath();
//
//                // Tracé du contour, ou du trou (chemin fermé), dans le sens trigo
//                cae.completerPathAvecContourFerme(x_arc, y_arc);
//                // TODO : à remplacer par methode arcTo / plus propre qu'un polygone, et peut-être plus rapide...
//
//                // Tracé du contour (apparemment, cela ne termine pas le path, on peut continuer à lui ajouter des éléments
//                cae.gc.stroke();
//
//                if (cercle.typeSurface() == Obstacle.TypeSurface.CONCAVE) {
//                    // Tracé du rectangle de la zone visible, dans le sens antitrigo : le Path de l'ellipse sera un trou
//                    // dans cette zone
//                    cae.completerPathAvecContourZoneVisibleAntitrigo();
//                }
//
//                // Le fill déclenche aussi l'appel closePath
//                cae.gc.fill();
//
//            } else { // Aucun point de la surface n'est dans la zone visible
//                if (cercle.contient(cae.boite_limites().centre())) {
//
//                    sc.selectionne_tous();
//
//                    // Toute la zone visible est dans la masse de l'objet conique
//                    CanvasAffichageEnvironnement.remplirPolygone(cae, sc.xcoins_selectionne(true), sc.ycoins_selectionne(true));
//                } else {
//                    // Toute la zone visible est hors de la masse de la conique
//                    // rien à faire
//                }
//            }
//
//            // C'est fini
//            return;
//        }
//
//        // Au moins 2 intersections, et jusqu'à 8...
//
//        ArrayList<Double> x_masse = new ArrayList<Double>(nombre_pas_angulaire_par_arc+4) ;
//        ArrayList<Double> y_masse = new ArrayList<Double>(nombre_pas_angulaire_par_arc+4) ;
//
//        // Boucle sur les intersections, dans le sens trigo par rapport au centre de l'écran
//        for (int i=0 ; i<valeurs_theta_intersection.size(); i++) {
//            double theta_deb = valeurs_theta_intersection.get(i) ;
//            if (theta_deb<0)
//                theta_deb += 2*Math.PI ;
//
//            int i_suivant = (i + 1) % (valeurs_theta_intersection.size()) ;
//            double theta_fin ;
//
//            if (i_suivant != i)
//                theta_fin = valeurs_theta_intersection.get(i_suivant) ;
//            else
//                theta_fin=theta_deb + 2*Math.PI ;
//            if (theta_fin<0)
//                theta_fin += 2*Math.PI ;
//
//            double x_deb = valeurs_x_intersection.get(i) ;
//            double y_deb = valeurs_y_intersection.get(i) ;
//            Point2D pt_deb = new Point2D(x_deb,y_deb) ;
//            double x_fin = valeurs_x_intersection.get(i_suivant) ;
//            double y_fin = valeurs_y_intersection.get(i_suivant) ;
//            Point2D pt_fin = new Point2D(x_fin,y_fin) ;
//
//            if (theta_fin<theta_deb)
//                theta_fin += 2*Math.PI ;
//
//
//            Point2D pt = cercle.point_sur_cercle((theta_deb+theta_fin)/2 ) ;
//
//            // Si cet arc est visible
//            if (pt!=null && cae.boite_limites().contains(pt)) {
//                ArrayList<Double> x_arc = new ArrayList<Double>(nombre_pas_angulaire_par_arc) ;
//                ArrayList<Double> y_arc = new ArrayList<Double>(nombre_pas_angulaire_par_arc) ;
//
//
//                // Ajouter le point exact de l'intersection pt_deb pour éviter les décrochages dûs au pas du tracé
//                x_arc.add(x_deb) ;
//                y_arc.add(y_deb) ;
//
//                x_arc.addAll(cercle.xpoints_sur_cercle(theta_deb,theta_fin,nombre_pas_angulaire_par_arc)) ;
//                y_arc.addAll(cercle.ypoints_sur_cercle(theta_deb,theta_fin,nombre_pas_angulaire_par_arc)) ;
//
//                // Ajouter le point exact de l'intersection pt_fin pour éviter les décrochages dûs au pas du tracé
//                x_arc.add(x_fin) ;
//                y_arc.add(y_fin) ;
//
//                // On trace l'arc de ce contour visible
//                cae.tracerPolyligne(x_arc,y_arc);
//                // TODO : à remplacer par methode arcTo / plus propre qu'un polygone, et peut-être plus rapide...
//
//                x_masse.addAll(x_arc) ;
//                y_masse.addAll(y_arc) ;
//                // TODO : voir la masse comme un Path et la construire avec le methode arcTo / plus propre qu'un polygone, et peut-être plus rapide...
//
//                x_arc.clear();
//                y_arc.clear();
//
//                // Si les 2 intersections sont sur un même bord et que leur milieu est dans la conique, il n'y a pas
//                // d'autre arc de contour à tracer, on peut sortir tout de suite de la boucle sur les intersections
//                if ( (x_deb==x_fin || y_deb==y_fin) && cercle.contient(pt_deb.midpoint(pt_fin)))
//                    break ;
//
//                // Sinon, chercher les coins contigus (càd non séparés des extrémités par une intersection) et qui sont
//                // dans l'interieur du contour, que la conique soit convexe ou concave
//                SelecteurCoins sc_coins_interieurs = sc.sequence_coins_continus(false,pt_deb,pt_fin,valeurs_x_intersection,valeurs_y_intersection) ;
//
//                if(     ( cercle.typeSurface()== Obstacle.TypeSurface.CONVEXE
//                        && cercle.contient(sc_coins_interieurs.coin(sc_coins_interieurs.coin_depart)) )
//                        || ( cercle.typeSurface()== Obstacle.TypeSurface.CONCAVE
//                        && !cercle.contient(sc_coins_interieurs.coin(sc_coins_interieurs.coin_depart)) )
//                ) {
//                    // Les ajouter au tracé du contour c
//                    x_masse.addAll(sc_coins_interieurs.xcoins_selectionne_antitrigo(true));
//                    y_masse.addAll(sc_coins_interieurs.ycoins_selectionne_antitrigo(true));
//
//                    break ;
//                }
//
//            } else { // Arc non visible
//
//                // Ajouter la sequence des coins de cette portion (dans ordre trigo) si ils sont dans la conique (et si il y en a)
//                SelecteurCoins sc_coins_interieurs = sc.sequence_coins_continus(true,pt_deb,pt_fin,valeurs_x_intersection,valeurs_y_intersection) ;
//
//                if(  ( cercle.typeSurface()== Obstacle.TypeSurface.CONVEXE
//                        && cercle.contient(sc_coins_interieurs.coin(sc_coins_interieurs.coin_depart)) )
//                        || ( cercle.typeSurface()== Obstacle.TypeSurface.CONCAVE
//                        && !cercle.contient(sc_coins_interieurs.coin(sc_coins_interieurs.coin_depart)) )
//                ) {
//                    // Les ajouter au contour de masse
//                    // TODO : voir la masse comme un Path constitué d'arc (arcTo et de points) : tracé sera plus efficace
//                    // mais pour l'utilisation de Clipper, il faudra continuer à le voir comme un polygone...
//                    x_masse.addAll(sc_coins_interieurs.xcoins_selectionne(true));
//                    y_masse.addAll(sc_coins_interieurs.ycoins_selectionne(true));
//                }
//
//            }
//        } // Fin boucle sur intersections
//
//        cae.gc.beginPath();
//
//        if (cercle.typeSurface() == Obstacle.TypeSurface.CONCAVE) {
//            // Tracé du rectangle de la zone visible, dans le sens antitrigo : le Path de l'ellipse sera un trou
//            // dans cette zone
//            cae.gc.moveTo(cae.xmax(), cae.ymin());
//            cae.gc.lineTo(cae.xmin(), cae.ymin());
//            cae.gc.lineTo(cae.xmin(), cae.ymax());
//            cae.gc.lineTo(cae.xmax(), cae.ymax());
//        }
//        // Tracé du contour, ou du trou (chemin fermé), dans le sens trigo
//        cae.completerPathAvecContourFerme(x_masse,y_masse);
//
//        cae.gc.fill();
//
//
//
//    }

//      //  @Override
//    public void visiteCercle_old(Cercle cercle) {
//
//        GraphicsContext gc = cae.gc() ;
//
//        Paint s = gc.getStroke() ;
//        Paint pf = gc.getFill() ;
//
//        Paint couleur_masse = cercle.couleurMatiere() ;
//        Paint couleur_bord = cercle.couleurContour() ;
//
//        gc.setStroke(couleur_bord);
//        gc.setFill(couleur_masse);
//
//        double rayon = cercle.rayon() ;
//
//        double x_centre = cercle.Xcentre() ;
//        double y_centre = cercle.Ycentre() ;
//
//
//        if ( (x_centre+rayon < cae.xmin()) || (x_centre-rayon > cae.xmax())
//          || (y_centre+rayon < cae.ymin()) || (y_centre-rayon > cae.ymax())
//          || (x_centre< cae.xmin() && y_centre> cae.ymax() && (cercle.centre().subtract(cae.xmin(), cae.ymax()).magnitude()>rayon))
//          || (x_centre> cae.xmax() && y_centre> cae.ymax() && (cercle.centre().subtract(cae.xmax(), cae.ymax()).magnitude()>rayon))
//          || (x_centre> cae.xmax() && y_centre< cae.ymin() && (cercle.centre().subtract(cae.xmax(), cae.ymin()).magnitude()>rayon))
//          || (x_centre< cae.xmin() && y_centre< cae.ymin() && (cercle.centre().subtract(cae.xmin(), cae.ymin()).magnitude()>rayon))
//        ) { // Aucune partie du contour du cercle n'est visible
//
//            if (cercle.typeSurface() == Obstacle.TypeSurface.CONVEXE)
//                return ;
//            else  // Concave
//                cae.gc.fillRect(cae.xmin(), cae.ymin(), cae.xmax() - cae.xmin(), cae.ymax()- cae.ymin());
//
//        }
//
//
//
//        if (cercle.typeSurface() == Obstacle.TypeSurface.CONVEXE)
//            gc.fillOval(x_centre-rayon,y_centre-rayon,2*rayon,2*rayon);
//        else { // Concave
//
//            double xg = Math.min(cae.xmin(),x_centre-rayon) ;
//            double xd = Math.max(cae.xmax(),x_centre+rayon) ;
//            double yb = Math.min(cae.ymin(),y_centre-rayon) ;
//            double yh = Math.max(cae.ymax(),y_centre+rayon) ;
//
//            double pas = cae.resolutionX() ;
//
//            double x,y ;
//
//            // Trace partie haute
//            ArrayList<Double> xpoints = new ArrayList<Double>(300);
//            ArrayList<Double> ypoints = new ArrayList<Double>(300);
//
//            xpoints.add(xg) ;
//            ypoints.add(y_centre) ;
//
//            x = x_centre-rayon ;
//            y = y_centre ;
//
//            xpoints.add(x) ;
//            ypoints.add(y) ;
//
//            do {
//                x += pas ;
//                y = y_centre + Math.sqrt( rayon*rayon - (x-x_centre)*(x-x_centre) )  ;
//
//                xpoints.add(x) ;
//                ypoints.add(y) ;
//            } while (x<x_centre+rayon) ;
//
//
//            xpoints.add(x_centre+rayon) ;
//            ypoints.add(y_centre) ;
//
//            xpoints.add(xd) ;
//            ypoints.add(y_centre) ;
//
//            xpoints.add(xd) ;
//            ypoints.add(yh) ;
//
//            xpoints.add(xg) ;
//            ypoints.add(yh) ;
//
//            xpoints.add(xg) ;
//            ypoints.add(y_centre) ;
//
//            CanvasAffichageEnvironnement.remplirPolygone(cae,xpoints,ypoints);
//
//            // Trace partie basse
//            xpoints.clear();
//            ypoints.clear();
//
//            xpoints.add(xg) ;
//            ypoints.add(y_centre) ;
//
//            x = x_centre-rayon ;
//            y = y_centre ;
//
//            xpoints.add(x) ;
//            ypoints.add(y) ;
//
//            do {
//                x += pas ;
//                y = y_centre - Math.sqrt( rayon*rayon - (x-x_centre)*(x-x_centre) )  ;
//
//                xpoints.add(x) ;
//                ypoints.add(y) ;
//            } while (x<x_centre+rayon) ;
//
//            xpoints.add(x_centre+rayon) ;
//            ypoints.add(y_centre) ;
//
//            xpoints.add(xd) ;
//            ypoints.add(y_centre) ;
//
//            xpoints.add(xd) ;
//            ypoints.add(yb) ;
//
//            xpoints.add(xg) ;
//            ypoints.add(yb) ;
//
//            xpoints.add(xg) ;
//            ypoints.add(y_centre) ;
//
//            CanvasAffichageEnvironnement.remplirPolygone(cae,xpoints,ypoints);
//
//        }
//
//        gc.strokeOval(x_centre-rayon,y_centre-rayon,2*rayon,2*rayon);
//
//        gc.setFill(pf);
//
//        gc.setStroke(s);
//        // Note : on pourrait aussi utiliser gc.save() au début de la méthode puis gc.restore() à la fin
//
//    }

    /**
     * visite un rectangle et l'affiche (contour+masse). Cette méthode ne réalise aucun tracé, ni remplissage hors de
     * la zone visible du CanvasAffichageEnvironnement (optimisation de la mémoire du Canvas qui n'est pas sollicitée
     * pour les éléments ou parties d'éléments qui sont invisibles.
     * @param rect : le rectangle à afficher
     */
    @Override
    public void visiteRectangle(Rectangle rect) {
        GraphicsContext gc = cae.gc() ;

        Paint s = gc.getStroke() ;
        Paint pf = gc.getFill() ;

        Paint couleur_masse = rect.couleurMatiere() ;
        Paint couleur_bord  = rect.couleurContour() ;

        gc.setStroke(couleur_bord);
        gc.setFill(couleur_masse);

        ContoursObstacle co = rect.couper(cae.boite_limites()) ;

        contours_obstacles.put(rect,co) ;

        cae.afficherContoursObstacle(co) ;

        // TODO : on pourrait aussi utiliser un gc.restore() (précédé d'un gc.save() en début de méthode)
        gc.setFill(pf);
        gc.setStroke(s);
    }

    @Override
    public void visitePrisme(Prisme prisme) {
        GraphicsContext gc = cae.gc() ;

        Paint s = gc.getStroke() ;
        Paint pf = gc.getFill() ;

        Paint couleur_masse = prisme.couleurMatiere() ;
        Paint couleur_bord  = prisme.couleurContour() ;

        gc.setStroke(couleur_bord);
        gc.setFill(couleur_masse);

        ContoursObstacle co = prisme.couper(cae.boite_limites()) ;

        contours_obstacles.put(prisme,co) ;

        cae.afficherContoursObstacle(co) ;

        // TODO : on pourrait aussi utiliser un gc.restore() (précédé d'un gc.save() en début de méthode)
        gc.setFill(pf);
        gc.setStroke(s);
    }

//    @Override
//    public void visiteRectangle(Rectangle rect) {
//        GraphicsContext gc = cae.gc() ;
//
//        Paint s = gc.getStroke() ;
//        Paint pf = gc.getFill() ;
//
//        Paint couleur_masse = rect.couleurMatiere() ;
//        Paint couleur_bord = rect.couleurContour() ;
//
//        gc.setStroke(couleur_bord);
//        gc.setFill(couleur_masse);
//
//        BoiteLimites boite = rect.boite() ;
//
//        // Boite totalement hors zone visible (et n'englobe pas la zone visible)
//        if (  boite.getMinY() > cae.ymax() || boite.getMaxY() < cae.ymin()
//                || boite.getMinX() > cae.xmax() || boite.getMaxX() < cae.xmin() ) {
//
//            if (rect.typeSurface() == Obstacle.TypeSurface.CONCAVE)
//                gc.fillRect(cae.xmin(),cae.ymin(),cae.xmax()-cae.xmin(),cae.ymax()-cae.ymin());
//
//            gc.setFill(pf);
//            gc.setStroke(s);
//
//            return ; // Rien à faire si le rectangle est convexe et hors zone
//
//        }
//
//        // Boite englobe totalement la zone visible
//        if ( ( boite.getMinY() < cae.ymin() && boite.getMaxY() > cae.ymax() )
//                && ( boite.getMinX() < cae.xmin() &&  boite.getMaxX() > cae.xmax() ) ) {
//
//            if (rect.typeSurface() == Obstacle.TypeSurface.CONVEXE)
//                gc.fillRect(cae.xmin(),cae.ymin(),cae.xmax()-cae.xmin(),cae.ymax()-cae.ymin());
//
//            gc.setFill(pf);
//            gc.setStroke(s);
//
//            return ; // Rien à faire si le rectangle est concave et englobe la zone visible
//        }
//
//
//
//        // Construction du contour visible du rectangle, dans le sens trigo
//        Contour c = new Contour(4) ;
//
//        // Coin HD est-il visible
//        if (cae.boite_limites().contains(boite.getMaxX(),boite.getMaxY()))
//            c.ajoutePoint(boite.getMaxX(), boite.getMaxY());
//        else { // Coin HD hors zone
//            // Coin HD trop à droite mais pas trop haut (morceau de l'horizontale droite visible)
//            if ( boite.getMaxX()>cae.xmax() && boite.getMaxY()<=cae.ymax())
//                c.ajoutePoint(cae.xmax(),boite.getMaxY());
//            // Sinon reste le cas trop haut mais rien à faire dans ce cas
//        }
//
//        // Coin HG est-il visible
//        if (cae.boite_limites().contains(boite.getMinX(),boite.getMaxY()))
//            c.ajoutePoint(boite.getMinX(), boite.getMaxY());
//        else { // Coin HG hors zone
//            // Coin HG trop à gauche mais pas trop haut (morceau horizontale haute visible)
//            if (boite.getMinX() < cae.xmin() && boite.getMaxY() <= cae.ymax()) {
//                c.ajoutePoint(cae.xmin(), boite.getMaxY()); // Point de sortie
//                if (c.taille() > 1)
//                    cae.tracerPolyligne(c.xpoints, c.ypoints);
//                c.raz();
//            } else if (boite.getMinX()>=cae.xmin() && boite.getMaxY() > cae.ymax() ) { // Point HG trop haut mais pas trop à gauche (morceau verticale gauche visible)
//                c.ajoutePoint(boite.getMinX(),cae.ymax());
//            } // Sinon reste le cas trop haut ET trop à gauche mais rien à faire dans ce cas
//        }
//
//        // Coin BG est-il visible
//        if (cae.boite_limites().contains(boite.getMinX(),boite.getMinY()))
//            c.ajoutePoint(boite.getMinX(), boite.getMinY());
//        else { // Coin BG hors zone
//            // Coin BG trop bas mais pas trop à gauche (morceau verticale gauche visible)
//            if ( boite.getMinX()>=cae.xmin() && boite.getMinY() < cae.ymin() ) {
//                c.ajoutePoint(boite.getMinX(), cae.ymin()); // Point de sortie
//                if (c.taille() > 1)
//                    cae.tracerPolyligne(c.xpoints, c.ypoints);
//                c.raz();
//            } else if (boite.getMinY()>=cae.ymin() && boite.getMinX()<cae.xmin() ) { // Point BG trop à gauche mais pas trop bas (morceau horizontale basse visible)
//                c.ajoutePoint(cae.xmin(), boite.getMinY()); // Nouveau pt de départ (entrée dans zone)
//            } // Sinon reste le cas trop bas ET trop à gauche mais rien à faire dans ce cas
//
//        }
//
//        // Coin BD est-il visible
//        if (cae.boite_limites().contains(boite.getMaxX(),boite.getMinY()))
//            c.ajoutePoint(boite.getMaxX(), boite.getMinY());
//        else { // Coin BD hors zone
//            // Coin BD pas trop bas mais trop à droite (morceau horizontale basse visible)
//            if ( boite.getMinY()>=cae.ymin() && boite.getMaxX() > cae.xmax()) {
//                c.ajoutePoint(cae.xmax(), boite.getMinY());
//                if (c.taille() > 1)
//                    cae.tracerPolyligne(c.xpoints, c.ypoints);
//                c.raz();
//            } else if (boite.getMaxX()<=cae.xmax() && boite.getMinY()<cae.ymin()) {// Coin BD trop bas mais pas trop à droite (morceau verticale droite visible)
//                c.ajoutePoint(boite.getMaxX(),cae.ymin()); // Nouveau point de départ (entrée dans zone)
//            }
//        }
//
//        // Retour sur HD pour bouclage éventuel du circuit :
//        // Coin HD est-il visible
//        if (cae.boite_limites().contains(boite.getMaxX(),boite.getMaxY()))
//            c.ajoutePoint(boite.getMaxX(), boite.getMaxY());
//        else { // Coin HD hors zone
//            // Coin HD trop haut mais pas trop à droite (morceau verticale droite visible)
//            if ( boite.getMaxY()>cae.ymax() && boite.getMaxX()<=cae.xmax() )
//                c.ajoutePoint(boite.getMaxX(),cae.ymax());
//            // Sinon reste le cas trop à droite mais  rien à faire dans ce cas
//        }
//
//        if (c.taille() > 1)
//            cae.tracerPolyligne(c.xpoints, c.ypoints);
//
//        BoiteLimites partie_visible = boite.couper(cae.boite_limites()) ;
//
//        if (partie_visible==null) // Ne doit pas arriver : ce cas est déjà écarté (cf. returns en debut de fonction)
//            throw new IllegalStateException("Problème dans le tracé du rectangle "+rect) ;
//
//        if (rect.typeSurface()== Obstacle.TypeSurface.CONVEXE)
//            gc.fillRect(partie_visible.getMinX(),partie_visible.getMinY(),partie_visible.getWidth(),partie_visible.getHeight()) ;
//        else { //CONCAVE
//            cae.gc.beginPath();
//            cae.completerPathAvecContourZoneVisibleAntitrigo();
//            cae.completerPathAvecContourBoiteTrigo(partie_visible);
//            cae.gc.fill(); // Declenche aussi la cloture du chemin
//        }
//
//
////        if (rect.typeSurface()== Obstacle.TypeSurface.CONVEXE)
////            gc.fillRect(boite.getMinX(),boite.getMinY(),boite.getWidth(),boite.getHeight()) ;
////        else { // CONCAVE
////            gc.fillRect(cae.xmin(), cae.ymin(),boite.getMinX()- cae.xmin(), cae.ymax()- cae.ymin()); // Partie gauche
////            gc.fillRect(boite.getMaxX(), cae.ymin(), cae.xmax()-boite.getMaxX(), cae.ymax()- cae.ymin()); // Partie droite
////            gc.fillRect(boite.getMinX(), cae.ymin(),boite.getWidth(),boite.getMinY()- cae.ymin()) ; // Partie centrale basse
////            gc.fillRect(boite.getMinX(),boite.getMaxY(),boite.getWidth(), cae.ymax()-boite.getHeight()-boite.getMinY()); // Partie centrale haute
////        }
//
////        gc.strokeRect(boite.getMinX(),boite.getMinY(),boite.getWidth(),boite.getHeight());
//
//        gc.setFill(pf);
//        gc.setStroke(s);
//        // Note : on pourrait aussi utiliser gc.save() au début de la méthode puis gc.restore() à la fin
//
//    }


    @Override
    public void visiteConique(Conique conique) {
        GraphicsContext gc = cae.gc() ;

        Paint s  = gc.getStroke() ;
        Paint pf = gc.getFill() ;

        Paint couleur_masse = conique.couleurMatiere() ;
        Paint couleur_bord  = conique.couleurContour() ;

        gc.setStroke(couleur_bord);
        gc.setFill(couleur_masse);

//        trace_conique_methode1(conique);
//        trace_conique_methode2(conique);
//        trace_conique_methode2_simplifie(conique);

        ContoursObstacle co = conique.couper(cae.boite_limites(), nombre_pas_angulaire_par_arc) ;

        contours_obstacles.put(conique,co) ;

        cae.afficherContoursObstacle(co) ;

        gc.setFill(pf);
        gc.setStroke(s);
        // Note : on pourrait aussi utiliser gc.save() au début de la méthode puis gc.restore() à la fin
    }

    public void tracerCheminsDeSource(Source s) {



    }

    private void tracerChemin(CheminLumiere c) {

        GraphicsContext gc = cae.gc();

        Paint s = gc.getStroke() ;
        gc.setStroke(c.couleur);

        double alpha = gc.getGlobalAlpha() ;
//        gc.setLineWidth(2.0);

        LOGGER.log(Level.FINER,"Tracé du chemin {0}",c);

        int cpt = 0 ;

//        for (Rayon r : c) {
        for (CheminLumiere ch : c) {

            Rayon r=ch.rayon() ;

            Point2D normale = ch.normale() ;

            if (cae.normalesVisibles() && normale!=null)
                cae.montrerNormale(r.arrivee(),normale) ;

            cpt++ ;

            gc.setGlobalAlpha(r.ratio_puissance);
            if (!r.estInfini()) {
                Point2D pt_deb = new Point2D(r.depart().getX(), r.depart().getY()) ;
                Point2D pt_arr = new Point2D(r.arrivee().getX(), r.arrivee().getY()) ;

                LOGGER.log(Level.FINER,"Rayon {0} entre {1}, {2} et {3}, {4}",new Object [] {cpt,pt_deb.getX(),pt_deb.getY(),pt_arr.getX(),pt_arr.getY()});

                // Il faut tracer jusqu'aux limites du canvas et pas au-delà, car le Canvas n'affiche pas correctement
                // des lignes dont les extrémités sont bien au-delà des limites du Canvas (elles deviennent très fines,
                // voire invisibles)
                if (cae.contient(pt_deb) && !cae.contient(pt_arr)) {
                    Point2D p_sortie_boite_limites = cae.derniere_intersection_avec_limites(r) ;
                    if (p_sortie_boite_limites!=null) {
                        gc.strokeLine(r.depart().getX(), r.depart().getY(), p_sortie_boite_limites.getX(), p_sortie_boite_limites.getY());
                        LOGGER.log(Level.FINER, "Point d'arrivée hors limites, ramené en {0}, {1}", new Object[]{p_sortie_boite_limites.getX(), p_sortie_boite_limites.getY()});
                    }

                    if (r.phenomene_origine!= Rayon.PhenomeneOrigine.EMISSION_SOURCE && cae.prolongementsArriereVisibles()) {
                        Point2D p_avant_depart = cae.premiere_intersection(r.supportGeometrique().prolongementAvantDepart());
                        if (p_avant_depart != null) cae.montrerProlongementArriere(r.depart(), p_avant_depart);
                    }

                } else if (!cae.contient(pt_deb) && cae.contient(pt_arr)) {
                    Point2D p_entree_boite_limites = cae.premiere_intersection_avec_limites(r) ;
                    if (p_entree_boite_limites!=null) {
                        gc.strokeLine(p_entree_boite_limites.getX(), p_entree_boite_limites.getY(), r.arrivee().getX(), r.arrivee().getY());
                        LOGGER.log(Level.FINER, "Point de départ hors limites, ramené en {0}, {1}", new Object[]{p_entree_boite_limites.getX(), p_entree_boite_limites.getY()});
                    }

                    if (cae.prolongementsAvantVisibles()) {
                        Point2D p_apres_arrivee = cae.premiere_intersection(r.supportGeometrique().prolongementApresArrivee());
                        if (p_apres_arrivee != null) cae.montrerProlongementAvant(r.arrivee(), p_apres_arrivee);
                    }

                } else if (!cae.contient(pt_deb) && !cae.contient(pt_arr)) {
                    Point2D p_entree_boite_limites = cae.premiere_intersection_avec_limites(r) ;
                    Point2D p_sortie_boite_limites = cae.derniere_intersection_avec_limites(r) ;

                    if (p_entree_boite_limites!=null && p_sortie_boite_limites!=null) {
                        gc.strokeLine(p_entree_boite_limites.getX(), p_entree_boite_limites.getY(), p_sortie_boite_limites.getX(), p_sortie_boite_limites.getY());
                        LOGGER.log(Level.FINER, "Points d'arrivée et de départ hors limites, ramenés en {0}, {1} et {2}, {3}", new Object[]{p_entree_boite_limites.getX(), p_entree_boite_limites.getY(), p_sortie_boite_limites.getX(), p_sortie_boite_limites.getY()});
                    }
                    else {
                        if (r.phenomene_origine != Rayon.PhenomeneOrigine.EMISSION_SOURCE && cae.prolongementsArriereVisibles()) {
                            Point2D p_avant_depart = cae.premiere_intersection(r.supportGeometrique().prolongementAvantDepart());
                            if (p_avant_depart != null) {
                                Point2D p_avant_depart_arr = cae.derniere_intersection(r.supportGeometrique().prolongementAvantDepart());
                                if (p_avant_depart_arr != null)
                                    cae.montrerProlongementArriere(p_avant_depart, p_avant_depart_arr);
                            }
                        }

                        if (cae.prolongementsAvantVisibles()) {
                            Point2D p_apres_arrivee = cae.premiere_intersection(r.supportGeometrique().prolongementApresArrivee());
                            if (p_apres_arrivee != null) {
                                Point2D p_apres_arrivee_arr = cae.derniere_intersection(r.supportGeometrique().prolongementApresArrivee());
                                if (p_apres_arrivee_arr != null)
                                    cae.montrerProlongementAvant(p_apres_arrivee, p_apres_arrivee_arr);
                            }
                        }
                    }

                } else {
                    LOGGER.log(Level.FINER,"Point d'arrivée et de départ dans les limites") ;
                    gc.strokeLine(r.depart().getX(), r.depart().getY(), r.arrivee().getX(), r.arrivee().getY());

                    if (r.phenomene_origine!= Rayon.PhenomeneOrigine.EMISSION_SOURCE && cae.prolongementsArriereVisibles()) {
                        Point2D p_avant_depart = cae.premiere_intersection(r.supportGeometrique().prolongementAvantDepart());
                        if (p_avant_depart != null) cae.montrerProlongementArriere(r.depart(), p_avant_depart);
                    }

                    if (cae.prolongementsAvantVisibles()) {
                        Point2D p_apres_arrivee = cae.premiere_intersection(r.supportGeometrique().prolongementApresArrivee());
                        if (p_apres_arrivee != null) cae.montrerProlongementAvant(r.arrivee(), p_apres_arrivee);
                    }

                }

            }
            else {  // r est infini
//                if (c.point_sortie_environnement!=null)
//                    gc.strokeLine(r.depart.getX(), r.depart.getY(), c.point_sortie_environnement.getX(), c.point_sortie_environnement.getY());

                Point2D p_sortie_boite_limites = cae.derniere_intersection_avec_limites(r) ;

                if (p_sortie_boite_limites != null) {
                    if (cae.contient(r.depart())) {
                        gc.strokeLine(r.depart().getX(), r.depart().getY(), p_sortie_boite_limites.getX(), p_sortie_boite_limites.getY());
                        LOGGER.log(Level.FINER, "Rayon infini {0} entre {1},{2} et {3},{4}", new Object[]{cpt, r.depart().getX(), r.depart().getY(), p_sortie_boite_limites.getX(), p_sortie_boite_limites.getY()});

                        if (r.phenomene_origine!= Rayon.PhenomeneOrigine.EMISSION_SOURCE && cae.prolongementsArriereVisibles()) {
                            Point2D p_avant_depart = cae.premiere_intersection(r.supportGeometrique().prolongementAvantDepart());
                            if (p_avant_depart != null) cae.montrerProlongementArriere(r.depart(), p_avant_depart);
                        }

                    } else {
                        Point2D p_entree_boite_limites = cae.premiere_intersection_avec_limites(r) ;
                        gc.strokeLine(p_entree_boite_limites.getX(), p_entree_boite_limites.getY(), p_sortie_boite_limites.getX(), p_sortie_boite_limites.getY());
                    }
                } else { // Le rayon commence hors de la zone visible et ne la traverse pas
                    if (r.phenomene_origine!= Rayon.PhenomeneOrigine.EMISSION_SOURCE && cae.prolongementsArriereVisibles()) {
                        Point2D p_avant_depart = cae.premiere_intersection(r.supportGeometrique().prolongementAvantDepart());
                        if (p_avant_depart != null) {
                            Point2D p_avant_depart_arr = cae.derniere_intersection(r.supportGeometrique().prolongementAvantDepart());
                            if (p_avant_depart_arr != null)
                                cae.montrerProlongementArriere(p_avant_depart, p_avant_depart_arr);
                        }
                    }
                }

            }

        } // Fin boucle sur les CheminLumiere

        gc.setGlobalAlpha(alpha);
        gc.setStroke(s);
    }

    @Override
    public void visiteComposition(Composition c) {

        if (c.elements().size()==0)
            return ;

        GraphicsContext gc = cae.gc() ;

        Paint s = gc.getStroke() ;
        Paint pf = gc.getFill() ;

        Paint couleur_masse = c.couleurMatiere() ;
        Paint couleur_bord = c.couleurContour() ;

        gc.setStroke(couleur_bord);
        gc.setFill(couleur_masse);

        VisiteurCollecteContours vcc = new VisiteurCollecteContours(cae) ;

        // Le VisiteurCollecteContours va construire le résultat (la "solution" de la composition)
        c.accepte(vcc);

        ContoursObstacle contours_resultat = vcc.contours() ;

        contours_obstacles.put(c,contours_resultat) ;

        if (c.typeSurface() == TypeSurface.CONCAVE) {
            // Tracé du rectangle de la zone visible, dans le sens antitrigo : le Path de la composition sera un trou
            // dans cette zone
            contours_resultat.ajouterContourMasse(cae.boite_limites().construireContourAntitrigo());
        }


        cae.afficherContoursObstacle(contours_resultat) ;

        // TODO : on pourrait aussi utiliser un gc.restore() (précédé d'un gc.save() en début de méthode)
        gc.setFill(pf);
        gc.setStroke(s);

    }

    @Override
    public void visiteCompositionDeuxObstacles(CompositionDeuxObstacles c) {

    }

    @Override
    public void visiteSystemeOptiqueCentre(SystemeOptiqueCentre soc)  {

        GraphicsContext gc = cae.gc() ;

        Paint s = gc.getStroke() ;

        Paint couleur_axe = soc.couleurAxe() ;

        gc.setStroke(couleur_axe);


        Contour c = soc.couper(cae.boite_limites()) ;

        double res = cae.resolution() ;

        if (c!=null) {



            Point2D origine = soc.origine();
            Point2D perp = soc.perpendiculaireDirection();


//            // Marquage de l'origine du repère
//            gc.strokeLine(origine.getX() + 10 * res * perp.getX(), origine.getY() + 10 * res * perp.getY(),
//                    origine.getX() - 10 * res * perp.getX(), origine.getY() - 10 * res * perp.getY());

            // Tracé de l'axe
            gc.setLineDashes(12 * res, 6 * res, 4 * res, 6 * res);
            cae.tracerContour(c);
            gc.setLineDashes();

            // Marquage de l'origine du repère
            marquePositionSurAxeSOC(soc,0d,soc.couleurAxe(),20);

//            // Calcule les intersections des dioptres avec l'axe, la matrice de transfert et les plans cardinaux
//            soc.calculeElementsCardinaux();

            // Marquage des dioptres
            if (soc.MontrerDioptres()&&soc.InterSectionsSurAxe()!=null) {

//                ArrayList<SystemeOptiqueCentre.DioptreParaxial> intersections ;

//                try {
//                    intersections = soc.intersectionsAvecAxe();

//                    soc.calculeElementsCardinaux();

                    for (DioptreParaxial intersection : soc.InterSectionsSurAxe()) {

                        // Marquage de la position du dioptre
                        marquePositionSurAxeSOC(soc,intersection.z(),Color.RED,50);
                        marquePositionSurAxeSOC(soc,intersection.z(),Color.RED,50);

//                        Point2D pt = origine.add(soc.direction().multiply(intersection.x_intersection)) ;
//
//                        gc.setStroke(Color.RED);
//
//                        if (cae.boite_limites().contains(pt))
//                            gc.strokeLine(pt.getX() + 10 * res * perp.getX(), pt.getY() + 10 * res * perp.getY(),
//                                    pt.getX() - 10 * res * perp.getX(), pt.getY() - 10 * res * perp.getY());
//
//                        gc.setStroke(couleur_axe);
//
//                    }
//                } catch (Exception e) {
//                    LOGGER.log(Level.SEVERE,"Exception lors de la recherche des dioptres du SOC",e);
                    }

            }

        }

        for (RencontreDioptreParaxial its : soc.dioptresRencontres()) {
            afficheDiaphragmeAntecedentDe(soc,its,Color.LIGHTGREY);
        }

        afficheDiaphragme(soc,soc.pupilleEntree(),Color.GREEN);
        afficheDiaphragme(soc,soc.lucarneEntree(),Color.LIGHTGREEN);

        afficheRayonsMarginaux(soc);
        
        afficheRayonsLimiteChamps(soc,true,true,true);

        if (soc.MontrerPlansFocaux()) {
            marquePositionSurAxeSOC(soc,soc.ZPlanFocal1(),Color.LIGHTBLUE,300,"F");
            marquePositionSurAxeSOC(soc,soc.ZPlanFocal2(),Color.LIGHTBLUE,300,"F'");
        }

        if (soc.MontrerPlansPrincipaux()) {
            marquePositionSurAxeSOC(soc,soc.ZPlanPrincipal1(),Color.LIGHTYELLOW,300,"H");
            marquePositionSurAxeSOC(soc,soc.ZPlanPrincipal2(),Color.LIGHTYELLOW,300,"H'");
        }

        if (soc.MontrerPlansNodaux()) {
            marquePositionSurAxeSOC(soc,soc.ZPlanNodal1(),Color.PALEVIOLETRED,300,"N");
            marquePositionSurAxeSOC(soc,soc.ZPlanNodal2(),Color.PALEVIOLETRED,300,"N'");
        }

        if (soc.MontrerObjet())
            afficheFlechePerpendiculaireAxeSOC(soc,soc.ZObjet(),Color.GREEN ,soc.HObjet());

        if (soc.MontrerImage())
            afficheFlechePerpendiculaireAxeSOC(soc,soc.ZImage(),Color.GREEN ,soc.HImage());


        // TODO : on pourrait aussi utiliser un gc.restore() (précédé d'un gc.save() en début de méthode)
        gc.setStroke(s);


    }

    // Affichage des rayons limites du diaphragme d'ouverture (rayons marginaux)
    private void afficheRayonsMarginaux(SystemeOptiqueCentre soc) {

        if (soc.dioptresRencontres()==null)
            return ;

        GraphicsContext gc = cae.gc() ;
        double res = cae.resolution() ;

        Color c_rm = Color.BLUE ;

        Point2D origine = soc.origine() ;
        Point2D perp = soc.perpendiculaireDirection();

        // Point objet sur l'axe
        Point2D pobjet = origine.add(soc.direction().multiply(soc.ZObjet())) ;
        Point2D pimage = (soc.ZImage()!=null?origine.add(soc.direction().multiply(soc.ZImage())):null) ;

//        gc.beginPath();
//
//        gc.moveTo(pobjet.getX(),pobjet.getY());

        Point2D pt_prec_haut = pobjet ;
        Point2D pt_prec_bas = pobjet ;

        Paint s = gc.getStroke() ;

        gc.setStroke(c_rm);

        // Objet virtuel ?
        if (soc.dioptresRencontres().size()>0 && soc.dioptresRencontres().get(0).ZIntersection()<soc.ZObjet())
            gc.setLineDashes(2*res,6*res);

        for (RencontreDioptreParaxial intersection : soc.dioptresRencontres()) {

            if (intersection.HLimiteOuverture()==null)
                continue;

            Point2D pt = origine.add(soc.direction().multiply(intersection.ZIntersection())) ;
            Point2D pt_haut = pt.add(perp.multiply(intersection.HLimiteOuverture())) ;
            Point2D pt_bas  = pt.add(perp.multiply(-intersection.HLimiteOuverture())) ;

            gc.strokeLine(pt_prec_haut.getX(),pt_prec_haut.getY(),pt_haut.getX(),pt_haut.getY());
            gc.strokeLine(pt_prec_bas.getX(),pt_prec_bas.getY(),pt_bas.getX(),pt_bas.getY());

            gc.setLineDashes(); // Arrêt des pointillés (s'il y en avait)

            pt_prec_haut = pt_haut ;
            pt_prec_bas  = pt_bas ;
        }

        if (soc.ZImage()!=null) {
            // Objet virtuel ?
            if (soc.dioptresRencontres().size() > 0 && soc.dioptresRencontres().get(soc.dioptresRencontres().size() - 1).ZIntersection() > soc.ZImage())
                gc.setLineDashes(2 * res, 6 * res);

            gc.strokeLine(pt_prec_haut.getX(), pt_prec_haut.getY(), pimage.getX(), pimage.getY());
            gc.strokeLine(pt_prec_bas.getX(), pt_prec_bas.getY(), pimage.getX(), pimage.getY());
        }

        gc.setLineDashes(); // Arrêt des pointillés (s'il y en avait)

        //        gc.stroke();

        gc.setStroke(s);

    }

    private void afficheDiaphragmeAntecedentDe(SystemeOptiqueCentre soc, RencontreDioptreParaxial it_avec_diaph, Color c) {

        if (it_avec_diaph.rayonDiaphragme()==null)
            return ;

        GraphicsContext gc = cae.gc() ;
        double res = cae.resolution() ;


        double z_d =it_avec_diaph.ZIntersection(), h_d = it_avec_diaph.rayonDiaphragme();
        double z_ant_d = it_avec_diaph.antecedentDiaphragme().z() , h_ant_d = Math.abs(it_avec_diaph.antecedentDiaphragme().hauteur()) ;

        double pos_lien = Math.max(h_ant_d,h_d)+15*res+10*res+20*res ;


        Point2D origine = soc.origine() ;
        Point2D perp = soc.perpendiculaireDirection();

        Point2D p_dep = origine.add(soc.direction().multiply(z_d)).add(perp.multiply(-h_d)) ;
        Point2D p_s1  = origine.add(soc.direction().multiply(z_d)).add(perp.multiply(-pos_lien)) ;
        Point2D p_s2  = origine.add(soc.direction().multiply(z_ant_d)).add(perp.multiply(-pos_lien)) ;
        Point2D p_arr  = origine.add(soc.direction().multiply(z_ant_d)).add(perp.multiply(-h_ant_d-15*res)) ;

        Paint p = gc.getStroke() ;

        gc.setStroke(c);

        gc.strokeLine(p_dep.getX(),p_dep.getY(),p_s1.getX(),p_s1.getY());
        gc.strokeLine(p_s1.getX(),p_s1.getY(),p_s2.getX(),p_s2.getY());
        gc.strokeLine(p_s2.getX(),p_s2.getY(),p_arr.getX(),p_arr.getY());

        Point2D p_p_1 = p_arr.add(soc.direction().multiply(+10*res)).add(perp.multiply(-10*res)) ;
        Point2D p_p_2 = p_arr.add(soc.direction().multiply(-10*res)).add(perp.multiply(-10*res)) ;
        double[] x_pointe = { p_p_1.getX() , p_arr.getX(), p_p_2.getX() } ;
        double[] y_pointe = { p_p_1.getY() , p_arr.getY(), p_p_2.getY() } ;

        Paint f = gc.getFill() ;
        gc.setFill(c);
        gc.fillPolygon(x_pointe,y_pointe,3);

        gc.setFill(f);
        gc.setStroke(p);

        afficheDiaphragme(soc,it_avec_diaph.diaphragme(),c);
        afficheDiaphragme(soc,it_avec_diaph.antecedentDiaphragme(),c);

    }

    private void afficheDiaphragme(SystemeOptiqueCentre soc, SystemeOptiqueCentre.PositionElement pos_diaph, Color c) {

        if (pos_diaph==null)
            return ;

        double z_d = pos_diaph.z(), h_d = pos_diaph.hauteur();

        GraphicsContext gc = cae.gc() ;
        double res = cae.resolution() ;

        Point2D origine = soc.origine() ;
        Point2D perp = soc.perpendiculaireDirection();

        // Points objets sur l'axe
        Point2D p_diaphragme_sur_axe = origine.add(soc.direction().multiply(z_d)) ;
        Point2D p_debut_diaphragme_1 = p_diaphragme_sur_axe.add(perp.multiply(h_d)) ;
        Point2D p_fin_diaphragme_1 = p_debut_diaphragme_1.add(perp.multiply((h_d>0?1d:-1d)* 15*res)) ;
        Point2D p_debut_diaphragme_2 = p_diaphragme_sur_axe.add(perp.multiply(-h_d)) ;
        Point2D p_fin_diaphragme_2 = p_debut_diaphragme_2.add(perp.multiply((h_d>0?-1d:1d)* 15*res)) ;

        Paint p = gc.getStroke() ;
        double lw =gc.getLineWidth() ;

        gc.setStroke(c);
        gc.setLineWidth(3*res);
        gc.strokeLine(p_debut_diaphragme_1.getX(),p_debut_diaphragme_1.getY(),p_fin_diaphragme_1.getX(), p_fin_diaphragme_1.getY());
        gc.strokeLine(p_debut_diaphragme_2.getX(),p_debut_diaphragme_2.getY(),p_fin_diaphragme_2.getX(), p_fin_diaphragme_2.getY());

        gc.setLineWidth(lw);
        gc.setStroke(p);

    }
    private void afficheRayonsLimiteChamps(SystemeOptiqueCentre soc,boolean champ_moyen,boolean champ_pleine_lumiere,boolean champ_total) {

        if ( (!champ_moyen) && (!champ_pleine_lumiere) && (!champ_total))
            return ;

        if (soc.dioptresRencontres()==null || soc.dioptresRencontres().size()==0)
            return ;

        Color c_rm = Color.YELLOW ;
        Color c_rpl = Color.WHITE ;
        Color c_rct = Color.DARKGREY ;

        GraphicsContext gc = cae.gc() ;
        double res = cae.resolution() ;

        Point2D origine = soc.origine() ;
        Point2D perp = soc.perpendiculaireDirection();

        // Points objets sur l'axe
        Point2D pobjet = origine.add(soc.direction().multiply(soc.ZObjet())) ;

        Point2D pt_objet_cm_haut  = null, pt_objet_cm_bas  = null;
        Point2D pt_objet_cpl_haut = null, pt_objet_cpl_bas = null ;
        Point2D pt_objet_ct_haut  = null, pt_objet_ct_bas  = null ;
        
        if (champ_moyen && soc.RChampMoyenObjet()!=null) {
            pt_objet_cm_haut = pobjet.add(perp.multiply(soc.RChampMoyenObjet()));
            pt_objet_cm_bas = pobjet.add(perp.multiply(-soc.RChampMoyenObjet()));
        }
        if (champ_pleine_lumiere && soc.RChampPleineLumiereObjet()!=null) {
            pt_objet_cpl_haut = pobjet.add(perp.multiply(soc.RChampPleineLumiereObjet()));
            pt_objet_cpl_bas = pobjet.add(perp.multiply(-soc.RChampPleineLumiereObjet()));
        }
        if (champ_total && soc.RChampTotalObjet()!=null) {
            pt_objet_ct_haut = pobjet.add(perp.multiply(soc.RChampTotalObjet()));
            pt_objet_ct_bas = pobjet.add(perp.multiply(-soc.RChampTotalObjet()));
        }

//        gc.beginPath();
        
        Paint s = gc.getStroke() ;
        
        Point2D pt_prec_cm_haut = pt_objet_cm_haut ;
        Point2D pt_prec_cm_bas = pt_objet_cm_bas ;
        Point2D pt_prec_cpl_haut = pt_objet_cpl_haut ;
        Point2D pt_prec_cpl_bas = pt_objet_cpl_bas ;
        Point2D pt_prec_ct_haut = pt_objet_ct_haut ;
        Point2D pt_prec_ct_bas = pt_objet_ct_bas ;

        // Objet virtuel ?
        if (soc.dioptresRencontres().size()>0 && soc.dioptresRencontres().get(0).ZIntersection()<soc.ZObjet())
            gc.setLineDashes(2*res,6*res);

        for (RencontreDioptreParaxial intersection : soc.dioptresRencontres()) {

//            if (intersection.HLimiteChamp()==null)
//                continue;

            Point2D pt = origine.add(soc.direction().multiply(intersection.ZIntersection())) ;

            if (champ_moyen&&intersection.HLimiteChamp()!=null) {
                Point2D pt_cm_haut = pt.add(perp.multiply(intersection.HLimiteChamp()));
                Point2D pt_cm_bas = pt.add(perp.multiply(-intersection.HLimiteChamp()));

                gc.setStroke(c_rm);

                gc.strokeLine(pt_prec_cm_haut.getX(), pt_prec_cm_haut.getY(), pt_cm_haut.getX(), pt_cm_haut.getY());
//                gc.strokeLine(pt_prec_cm_bas.getX(), pt_prec_cm_bas.getY(), pt_cm_bas.getX(), pt_cm_bas.getY());

                pt_prec_cm_haut = pt_cm_haut ;
                pt_prec_cm_bas  = pt_cm_bas ;
            }

            if (champ_pleine_lumiere&&intersection.HLimiteChampPleineLumiere()!=null) {
                Point2D pt_cpl_haut = pt.add(perp.multiply(intersection.HLimiteChampPleineLumiere()));
                Point2D pt_cpl_bas = pt.add(perp.multiply(-intersection.HLimiteChampPleineLumiere()));

                gc.setStroke(c_rpl);

                gc.strokeLine(pt_prec_cpl_haut.getX(), pt_prec_cpl_haut.getY(), pt_cpl_haut.getX(), pt_cpl_haut.getY());
//                gc.strokeLine(pt_prec_cpl_bas.getX(), pt_prec_cpl_bas.getY(), pt_cpl_bas.getX(), pt_cpl_bas.getY());

                pt_prec_cpl_haut = pt_cpl_haut ;
                pt_prec_cpl_bas  = pt_cpl_bas ;
            }

            if (champ_total&&intersection.HLimiteChampTotal()!=null) {
                Point2D pt_ct_haut = pt.add(perp.multiply(intersection.HLimiteChampTotal()));
                Point2D pt_ct_bas = pt.add(perp.multiply(-intersection.HLimiteChampTotal()));

                gc.setStroke(c_rct);

                gc.strokeLine(pt_prec_ct_haut.getX(), pt_prec_ct_haut.getY(), pt_ct_haut.getX(), pt_ct_haut.getY());
//                gc.strokeLine(pt_prec_ct_bas.getX(), pt_prec_ct_bas.getY(), pt_ct_bas.getX(), pt_ct_bas.getY());

                pt_prec_ct_haut = pt_ct_haut ;
                pt_prec_ct_bas  = pt_ct_bas ;
            }

//            gc.setStroke(s) ; // Restauration de la couleur de départ
            gc.setLineDashes(); // Arrêt des pointillés (s'il y en avait)

        }
        gc.setStroke(s) ; // Restauration de la couleur de départ
        gc.setLineDashes(); // Arrêt des pointillés (s'il y en avait)

        if (soc.ZImage()==null || soc.ZPlanSortie()==null)
            return ;

        // Image virtuelle ?
        if (soc.dioptresRencontres().size()>0 && soc.ZPlanSortie()>soc.ZImage())
            gc.setLineDashes(2*res,6*res);

        // Points image d'arrivée
        Point2D pimage = origine.add(soc.direction().multiply(soc.ZImage())) ;

        if (champ_moyen && soc.RChampMoyenImage()!=null) {
            Point2D pt_image_cm_haut = pimage.add(perp.multiply(soc.RChampMoyenImage()));
            Point2D pt_image_cm_bas = pimage.add(perp.multiply(-soc.RChampMoyenImage()));
            gc.setStroke(c_rm);
            gc.strokeLine(pt_prec_cm_haut.getX(),pt_prec_cm_haut.getY(),pt_image_cm_haut.getX(),pt_image_cm_haut.getY());
//            gc.strokeLine(pt_prec_cm_bas.getX(),pt_prec_cm_bas.getY(),pt_image_cm_bas.getX(),pt_image_cm_bas.getY());
        }
        if (champ_pleine_lumiere && soc.RChampPleineLumiereImage()!=null) {
            Point2D pt_image_cpl_haut = pimage.add(perp.multiply(soc.RChampPleineLumiereImage()));
            Point2D pt_image_cpl_bas = pimage.add(perp.multiply(-soc.RChampPleineLumiereImage()));
            gc.setStroke(c_rpl);
            gc.strokeLine(pt_prec_cpl_haut.getX(),pt_prec_cpl_haut.getY(),pt_image_cpl_haut.getX(),pt_image_cpl_haut.getY());
//            gc.strokeLine(pt_prec_cpl_bas.getX(),pt_prec_cpl_bas.getY(),pt_image_cpl_bas.getX(),pt_image_cpl_bas.getY());
        }
        if (champ_total && soc.RChampTotalImage()!=null) {
            Point2D pt_image_ct_haut = pimage.add(perp.multiply(soc.RChampTotalImage()));
            Point2D pt_image_ct_bas = pimage.add(perp.multiply(-soc.RChampTotalImage()));
            gc.setStroke(c_rct);
            gc.strokeLine(pt_prec_ct_haut.getX(),pt_prec_ct_haut.getY(),pt_image_ct_haut.getX(),pt_image_ct_haut.getY());
//            gc.strokeLine(pt_prec_ct_bas.getX(),pt_prec_ct_bas.getY(),pt_image_ct_bas.getX(),pt_image_ct_bas.getY());
        }

        gc.setStroke(s);
        gc.setLineDashes(); // Arrêt des pointillés (s'il y en avait)


//        gc.stroke();

    }


    private void marquePositionSurAxeSOC(SystemeOptiqueCentre soc, Double z_sur_axe, Color c,double hauteur) {
        marquePositionSurAxeSOC(soc,z_sur_axe,c,hauteur,null);
    }

    /**
     * Marque une position d'abscisse donnée sur l'axe optique d'un SOC, avec une hauteur et une couleur de marque paramétrables
     * @param soc
     * @param z_sur_axe
     * @param c
     * @param hauteur : hauteur en nombre de resolutions de l'environnement
     * @param label : texte à positionner sur la marque
     */
    private void marquePositionSurAxeSOC(SystemeOptiqueCentre soc, Double z_sur_axe, Color c,double hauteur, String label) {

        if (z_sur_axe==null)
            return;

        double demi_hauteur =0.5*hauteur ;

        GraphicsContext gc = cae.gc() ;
        double res = cae.resolution() ;

        Point2D origine = soc.origine();
        Point2D perp = soc.perpendiculaireDirection();


        Point2D pt = origine.add(soc.direction().multiply(z_sur_axe.doubleValue())) ;

//        Paint s = gc.getStroke() ;
//        Paint f = gc.getFill() ;

        gc.save();

        gc.setStroke(c);

        gc.strokeLine(pt.getX() + demi_hauteur * res * perp.getX(), pt.getY() + demi_hauteur * res * perp.getY(),
                pt.getX() - demi_hauteur * res * perp.getX(), pt.getY() - demi_hauteur * res * perp.getY());

        if (label != null) {

            gc.setFill(c);

//            // On met provisoirement de côté la transformation actuelle
//            Affine aff = gc.getTransform();

            // Position du texte à afficher en coordonnées du GC du Canvas
            Point2D pos_texte_gc = gc.getTransform().transform(pt.getX(), pt.getY()).add(marge_label_x, marge_label_y);

            // Nouvelle transformation à appliquer : simple homothétie centré sur le point d'affichage du texte
            Affine zoom_texte = new Affine();
            zoom_texte.appendScale(facteur_zoom_label, facteur_zoom_label, pos_texte_gc.getX(), pos_texte_gc.getY());
            gc.setTransform(zoom_texte);

            // Ecriture de l'étiquette
            gc.setFont(fonte_labels);

            gc.fillText(label, pos_texte_gc.getX(), pos_texte_gc.getY());

//            // Restauration de la transformation
//            gc.setTransform(aff);
//
//            // Restauration de la couleur de remplissage
//            gc.setFill(f);
        }

//        gc.setStroke(s) ;
        gc.restore();

    }

    /**
     * @param soc
     * @param z_sur_axe
     * @param c
     * @param hauteur_fleche : hauteur de la flèche (qui peut être négative, si la flèche est sous l'axe)
     */
    private void afficheFlechePerpendiculaireAxeSOC(SystemeOptiqueCentre soc, Double z_sur_axe, Color c,Double hauteur_fleche) {
        if (z_sur_axe==null || hauteur_fleche==null)
            return;

        GraphicsContext gc = cae.gc() ;

        Point2D origine = soc.origine();
        Point2D perp = soc.perpendiculaireDirection();


        Point2D pt = origine.add(soc.direction().multiply(z_sur_axe.doubleValue())) ;

        //if (cae.boite_limites().contains(pt)) {

            Paint s = gc.getStroke() ;

            gc.setStroke(c);

            gc.strokeLine(pt.getX() , pt.getY() ,
                    pt.getX() + hauteur_fleche * perp.getX(), pt.getY() + hauteur_fleche * perp.getY());

            gc.setStroke(s) ;
        //}

    }


//    @Override
//    public void visiteDemiPlan_old(DemiPlan dp) {
//
//        Rayon r = new Rayon(dp.origine(),dp.direction()) ;
//        Point2D p_inter1 = cae.boite_limites().premiere_intersection(r) ;
//        Point2D p_inter2 = cae.boite_limites().derniere_intersection(r) ;
//
//        if (p_inter1 != null && p_inter1.equals(p_inter2))
//            p_inter2 = null ;
//
//        Rayon r_opp = new Rayon(dp.origine(),dp.direction().multiply(-1.0)) ;
//        Point2D p_inter_opp1 = cae.boite_limites().premiere_intersection(r_opp) ;
//        Point2D p_inter_opp2 = cae.boite_limites().derniere_intersection(r_opp) ;
//
//        if (p_inter_opp1 != null && p_inter_opp1.equals(p_inter_opp2))
//            p_inter_opp2 = null ;
//
//        ArrayList<Point2D> its = new ArrayList<Point2D>(2) ;
//
//        if (cae.boite_limites.aSurSaSurface(dp.origine()))
//            its.add(dp.origine()) ;
//
//        if (p_inter1!=null)
//            its.add(p_inter1) ;
//        if (p_inter2!=null)
//            its.add(p_inter2) ;
//        if (p_inter_opp1!=null)
//            its.add(p_inter_opp1) ;
//        if (p_inter_opp2!=null)
//            its.add(p_inter_opp2) ;
//
//
//        if(its.size()>2) {
//            throw new IllegalStateException("Une ligne ne peut pas avoir plus de 2 points d'intersection avec la boite limite de l'environnement.") ;
//        }
//
//
//        GraphicsContext gc = cae.gc() ;
//
//        Paint s = gc.getStroke() ;
//        Paint pf = gc.getFill() ;
//
//        Paint couleur_masse = dp.couleurMatiere() ;
//        Paint couleur_bord = dp.couleurContour() ;
//
//        gc.setStroke(couleur_bord);
//        gc.setFill(couleur_masse);
//
//        BoiteLimites boite = cae.boite_limites() ;
//
//        // Pas d'intersection ou 1 seul intersection (avec un coin)
//        if (its.size()<2) {
//            if (dp.contient(boite.centre()))
//                gc.fillRect(boite.getMinX(), boite.getMinY(), boite.getWidth(), boite.getHeight());
//            else
//                return;
//        }
//
//        ArrayList<Double> x_noeuds = new ArrayList<Double>(4) ;
//        ArrayList<Double> y_noeuds = new ArrayList<Double>(4) ;
//
//        // On tourne dans le sens trigo, en partant du haut droit
//        if (dp.contient(boite.hautDroit())) {
//            x_noeuds.add(boite.hautDroit().getX());
//            y_noeuds.add(boite.hautDroit().getY());
//        }
//
//        if (Environnement.quasiEgalite(its.get(0).getY(), cae.ymax())) {
//            x_noeuds.add(its.get(0).getX()) ;
//            y_noeuds.add(its.get(0).getY()) ;
//        } else if (Environnement.quasiEgalite(its.get(1).getY(), cae.ymax())) {
//            x_noeuds.add(its.get(1).getX()) ;
//            y_noeuds.add(its.get(1).getY()) ;
//        }
//
//        if (dp.contient(boite.hautGauche())) {
//            x_noeuds.add(boite.hautGauche().getX());
//            y_noeuds.add(boite.hautGauche().getY());
//        }
//
//        if (Environnement.quasiEgalite(its.get(0).getX(), cae.xmin())) {
//            x_noeuds.add(its.get(0).getX()) ;
//            y_noeuds.add(its.get(0).getY()) ;
//        } else if (Environnement.quasiEgalite(its.get(1).getX(), cae.xmin())) {
//            x_noeuds.add(its.get(1).getX()) ;
//            y_noeuds.add(its.get(1).getY()) ;
//        }
//
//        if (dp.contient(boite.basGauche())) {
//            x_noeuds.add(boite.basGauche().getX());
//            y_noeuds.add(boite.basGauche().getY());
//        }
//
//        if (Environnement.quasiEgalite(its.get(0).getY(), cae.ymin())) {
//            x_noeuds.add(its.get(0).getX()) ;
//            y_noeuds.add(its.get(0).getY()) ;
//        } else if (Environnement.quasiEgalite(its.get(1).getY(), cae.ymin())) {
//            x_noeuds.add(its.get(1).getX()) ;
//            y_noeuds.add(its.get(1).getY()) ;
//        }
//
//        if (dp.contient(boite.basDroit())) {
//            x_noeuds.add(boite.basDroit().getX());
//            y_noeuds.add(boite.basDroit().getY());
//        }
//
//        if (Environnement.quasiEgalite(its.get(0).getX(), cae.xmax())) {
//            x_noeuds.add(its.get(0).getX()) ;
//            y_noeuds.add(its.get(0).getY()) ;
//        } else if (Environnement.quasiEgalite(its.get(1).getX(), cae.xmax())) {
//            x_noeuds.add(its.get(1).getX()) ;
//            y_noeuds.add(its.get(1).getY()) ;
//        }
//
//        /////
//
//        CanvasAffichageEnvironnement.remplirPolygone(cae,x_noeuds,y_noeuds);
//
////        CanvasAffichageEnvironnement.tracerPolyligne(eg,x_noeuds,y_noeuds);
//
//        gc.strokeLine(its.get(0).getX(),its.get(0).getY(),its.get(1).getX(),its.get(1).getY());
//
//        gc.setFill(pf);
//
//        gc.setStroke(s);
//        // Note : on pourrait aussi utiliser gc.save() au début de la méthode puis gc.restore() à la fin
//
//    }

//    private void trace_conique_methode2(Conique conique) {
//        double e = conique.excentricite.get() ;
//
//        double[][] i_droites = conique.intersections_verticale(cae.xmax(), cae.ymin(), cae.ymax(),true) ;
//        double[][] i_hautes  = conique.intersections_horizontale(cae.ymax(), cae.xmin(), cae.xmax(),false) ;
//        double[][] i_gauches = conique.intersections_verticale(cae.xmin(), cae.ymin(), cae.ymax(),false) ;
//        double[][] i_basses  = conique.intersections_horizontale(cae.ymin(), cae.xmin(), cae.xmax(),true) ;
//
//        int n_intersections = i_hautes.length + i_gauches.length +i_basses.length + i_droites.length ;
//
//        SelecteurCoins sc = new SelecteurCoins(cae.xmin(), cae.ymin(), cae.xmax(), cae.ymax());
//
////        System.out.println("Nombre d'intersections avec les bords : "+n_intersections);
//
//        // Tableau qui contiendra au plus 4 intervalles [theta min, theta max] où la courbe est visible
//        // ordonnés dans le sens trigonométrique en partant de de l'axe X par rapport au centre de l'écran
//        ArrayList<Double> valeurs_theta_intersection = new ArrayList<Double>(8) ;
//
//        ArrayList<Double> valeurs_x_intersection = new ArrayList<Double>(8) ;
//        ArrayList<Double> valeurs_y_intersection = new ArrayList<Double>(8) ;
//
//        for (int i = 0 ; i < i_droites.length ; i++) {
//            valeurs_theta_intersection.add(i_droites[i][1]);
//            valeurs_x_intersection.add(cae.xmax()) ;
//            valeurs_y_intersection.add(i_droites[i][0]) ;
//        }
//        for (int i = 0 ; i < i_hautes.length ; i++) {
//            valeurs_theta_intersection.add(i_hautes[i][1]);
//            valeurs_x_intersection.add(i_hautes[i][0]) ;
//            valeurs_y_intersection.add(cae.ymax()) ;
//        }
//        for (int i = 0 ; i < i_gauches.length ; i++) {
//            valeurs_theta_intersection.add(i_gauches[i][1]);
//            valeurs_x_intersection.add(cae.xmin()) ;
//            valeurs_y_intersection.add(i_gauches[i][0]) ;
//        }
//        for (int i = 0 ; i < i_basses.length ; i++) {
//            valeurs_theta_intersection.add(i_basses[i][1]);
//            valeurs_x_intersection.add(i_basses[i][0]) ;
//            valeurs_y_intersection.add(cae.ymin()) ;
//        }
//
//        if (n_intersections!=valeurs_theta_intersection.size())
//            System.err.println("On a un problème");
//
//        // Si aucune intersection, --ou si 1 seule intersection (TODO : tester le cas à 1 intersection avec une ellipse)
//        if (n_intersections<=1) {
//
//            // Ellipse entièrement contenue dans la zone visible ?
//            if (e<1.0 && cae.boite_limites().contains(conique.point_sur_conique(0))) {
//                ArrayList<Double> x_arc = conique.xpoints_sur_conique(0,2*Math.PI, nombre_pas_angulaire_par_arc) ;
//                ArrayList<Double> y_arc = conique.ypoints_sur_conique(0,2*Math.PI, nombre_pas_angulaire_par_arc) ;
//                cae.tracerPolyligne(x_arc,y_arc);
//
//                if (conique.typeSurface() == Obstacle.TypeSurface.CONVEXE)
//                    CanvasAffichageEnvironnement.remplirPolygone(cae,x_arc,y_arc);
//                else { // CONCAVE
//                    x_arc.add(cae.xmax()) ;
//                    y_arc.add(conique.point_sur_conique(2*Math.PI).getY()) ;
//                    sc.selectionne_tous();
//                    sc.coin_depart = SelecteurCoins.Coin.BD ;
//                    x_arc.addAll(sc.xcoins_selectionne_antitrigo(true));
//                    y_arc.addAll(sc.ycoins_selectionne_antitrigo(true));
//                    x_arc.add(cae.xmax()) ;
//                    y_arc.add(conique.point_sur_conique(0.0).getY()) ;
//
//                    CanvasAffichageEnvironnement.remplirPolygone(cae, x_arc, y_arc);
//                }
//            }
//            else { // Aucun point du dioptre n'est dans la zone visible
//                if (conique.contient(cae.boite_limites().centre())) {
//
//                    sc.selectionne_tous();
//
//                    // Toute la zone visible est dans la masse de l'objet conique
//                    CanvasAffichageEnvironnement.remplirPolygone(cae, sc.xcoins_selectionne(true), sc.ycoins_selectionne(true));
//                } else {
//                    // Toute la zone visible est hors de la masse de la conique
//                    // rien à faire
//                }
//            }
//
//            // C'est fini
//            return ;
//        }
//
//        // Au moins 2 intersections, et jusqu'à 8...
//
//        ArrayList<Double> x_masse = new ArrayList<Double>(nombre_pas_angulaire_par_arc+4) ;
//        ArrayList<Double> y_masse = new ArrayList<Double>(nombre_pas_angulaire_par_arc+4) ;
//
//        // Boucle sur les intersections, dans le sens trigo par rapport au centre de l'écran
//        for (int i=0 ; i<valeurs_theta_intersection.size(); i++) {
//            double theta_deb = valeurs_theta_intersection.get(i) ;
//            if (theta_deb<0)
//                theta_deb += 2*Math.PI ;
//
//            int i_suivant = (i + 1) % (valeurs_theta_intersection.size()) ;
//            double theta_fin ;
//
//            if (i_suivant != i)
//                theta_fin = valeurs_theta_intersection.get(i_suivant) ;
//            else
//                theta_fin=theta_deb + 2*Math.PI ;
//            if (theta_fin<0)
//                theta_fin += 2*Math.PI ;
//
//            double x_deb = valeurs_x_intersection.get(i) ;
//            double y_deb = valeurs_y_intersection.get(i) ;
//            Point2D pt_deb = new Point2D(x_deb,y_deb) ;
//            double x_fin = valeurs_x_intersection.get(i_suivant) ;
//            double y_fin = valeurs_y_intersection.get(i_suivant) ;
//            Point2D pt_fin = new Point2D(x_fin,y_fin) ;
//
//            if (theta_fin<theta_deb)
//                theta_fin += 2*Math.PI ;
//
//
//            Point2D pt = conique.point_sur_conique((theta_deb+theta_fin)/2 ) ;
//
//            // Si cet arc est visible
//            if (pt!=null && cae.boite_limites().contains(pt)) {
//                ArrayList<Double> x_arc = new ArrayList<Double>(nombre_pas_angulaire_par_arc) ;
//                ArrayList<Double> y_arc = new ArrayList<Double>(nombre_pas_angulaire_par_arc) ;
//
//                // Ajouter le point exact de l'intersection pt_deb pour éviter les décrochages dûs au pas du tracé
//                x_arc.add(x_deb) ;
//                y_arc.add(y_deb) ;
//
//                x_arc.addAll(conique.xpoints_sur_conique(theta_deb,theta_fin, nombre_pas_angulaire_par_arc)) ;
//                y_arc.addAll(conique.ypoints_sur_conique(theta_deb,theta_fin, nombre_pas_angulaire_par_arc)) ;
//
//                // Ajouter le point exact de l'intersection pt_fin pour éviter les décrochages dûs au pas du tracé
//                x_arc.add(x_fin) ;
//                y_arc.add(y_fin) ;
//
//                // On trace l'arc
//                cae.tracerPolyligne(x_arc,y_arc);
//
//                // On ajoute ces mêmes points au contour de masse
//                x_masse.add(x_deb) ;
//                y_masse.add(y_deb) ;
//                x_masse.addAll(conique.xpoints_sur_conique(theta_deb,theta_fin, nombre_pas_angulaire_par_arc)) ;
//                y_masse.addAll(conique.ypoints_sur_conique(theta_deb,theta_fin, nombre_pas_angulaire_par_arc)) ;
//                x_masse.add(x_fin) ;
//                y_masse.add(y_fin) ;
//
//                // Si les 2 intersections sont sur un même bord et que leur milieu est dans la conique, remplir le contour
//                // et le re-initialiser
//                if ( (x_deb==x_fin || y_deb==y_fin) && conique.contient(pt_deb.midpoint(pt_fin))) {
//                    CanvasAffichageEnvironnement.remplirPolygone(cae,x_arc,y_arc);
//                    x_arc.clear();
//                    y_arc.clear();
//                    // Passer à l'intersection suivante
//                    continue;
//                }
//
//                // Chercher les coins contigus (càd non séparés des extrémités par une intersection) et qui sont dans la
//                // conique dans le sens anti-trigonométrique
//                SelecteurCoins sc_masse = sc.sequence_coins_continus(false,pt_deb,pt_fin,valeurs_x_intersection,valeurs_y_intersection) ;
//
//                if( sc_masse.est_selectionne(sc_masse.coin_depart) && conique.contient(sc_masse.coin(sc_masse.coin_depart)) ) {
//                    // Les ajouter au contour de masse
//                    x_masse.addAll(sc_masse.xcoins_selectionne_antitrigo(true));
//                    y_masse.addAll(sc_masse.ycoins_selectionne_antitrigo(true));
//
//                    // Le tracer
//                    CanvasAffichageEnvironnement.remplirPolygone(cae, x_masse, y_masse);
//
//                    // Le reinitialiser pour la suite
//                    x_masse.clear();
//                    y_masse.clear();
//
//                }
//
//            } else { // Arc non visible
//
//                // Ajouter la sequence des coins de cette portion (dans ordre trigo) si ils sont dans la conique
//                SelecteurCoins sc_masse = sc.sequence_coins_continus(true,pt_deb,pt_fin,valeurs_x_intersection,valeurs_y_intersection) ;
//                if( sc_masse.est_selectionne(sc_masse.coin_depart) && conique.contient(sc_masse.coin(sc_masse.coin_depart)) ) {
//                    // Les ajouter au contour de masse
//                    x_masse.addAll(sc_masse.xcoins_selectionne(true));
//                    y_masse.addAll(sc_masse.ycoins_selectionne(true));
//                }
//
//            }
//        } // Fin boucle sur intersections
//
//        if (!x_masse.isEmpty())
//            CanvasAffichageEnvironnement.remplirPolygone(cae, x_masse, y_masse);
//
//    }

//    private void trace_conique_methode2_simplifie(Conique conique) {
//        double e = conique.excentricite.get() ;
//
//        double[][] i_droites = conique.intersections_verticale(cae.xmax(), cae.ymin(), cae.ymax(),true) ;
//        double[][] i_hautes  = conique.intersections_horizontale(cae.ymax(), cae.xmin(), cae.xmax(),false) ;
//        double[][] i_gauches = conique.intersections_verticale(cae.xmin(), cae.ymin(), cae.ymax(),false) ;
//        double[][] i_basses  = conique.intersections_horizontale(cae.ymin(), cae.xmin(), cae.xmax(),true) ;
//
////        int n_intersections = i_hautes.length + i_gauches.length +i_basses.length + i_droites.length ;
//
//        SelecteurCoins sc = new SelecteurCoins(cae.xmin(), cae.ymin(), cae.xmax(), cae.ymax());
//
////        System.out.println("Nombre d'intersections avec les bords : "+n_intersections);
//
//        // Tableau qui contiendra au plus 4 intervalles [theta min, theta max] où la courbe est visible
//        // ordonnés dans le sens trigonométrique en partant de de l'axe X par rapport au centre de l'écran
//        ArrayList<Double> valeurs_theta_intersection = new ArrayList<Double>(8) ;
//
//        ArrayList<Double> valeurs_x_intersection = new ArrayList<Double>(8) ;
//        ArrayList<Double> valeurs_y_intersection = new ArrayList<Double>(8) ;
//
//        int n_intersections = sc.ordonneIntersections(i_droites, i_hautes, i_gauches, i_basses,
//                valeurs_theta_intersection, valeurs_x_intersection, valeurs_y_intersection);
//
//
////        for (int i = 0 ; i < i_droites.length ; i++) {
////            valeurs_theta_intersection.add(i_droites[i][1]);
////            valeurs_x_intersection.add(cae.xmax()) ;
////            valeurs_y_intersection.add(i_droites[i][0]) ;
////        }
////        for (int i = 0 ; i < i_hautes.length ; i++) {
////            valeurs_theta_intersection.add(i_hautes[i][1]);
////            valeurs_x_intersection.add(i_hautes[i][0]) ;
////            valeurs_y_intersection.add(cae.ymax()) ;
////        }
////        for (int i = 0 ; i < i_gauches.length ; i++) {
////            valeurs_theta_intersection.add(i_gauches[i][1]);
////            valeurs_x_intersection.add(cae.xmin()) ;
////            valeurs_y_intersection.add(i_gauches[i][0]) ;
////        }
////        for (int i = 0 ; i < i_basses.length ; i++) {
////            valeurs_theta_intersection.add(i_basses[i][1]);
////            valeurs_x_intersection.add(i_basses[i][0]) ;
////            valeurs_y_intersection.add(cae.ymin()) ;
////        }
////
////        if (n_intersections!=valeurs_theta_intersection.size())
////            System.err.println("On a un problème");
//
//        // Si aucune intersection, --ou si 1 seule intersection (TODO : tester le cas à 1 intersection avec une ellipse)
//        if (n_intersections<=1) {
//
//            // Ellipse entièrement contenue dans la zone visible ?
//            if (e<1.0 && cae.boite_limites().contains(conique.point_sur_conique(0))) {
//                ArrayList<Double> x_arc = conique.xpoints_sur_conique(0,2*Math.PI, nombre_pas_angulaire_par_arc) ;
//                ArrayList<Double> y_arc = conique.ypoints_sur_conique(0,2*Math.PI, nombre_pas_angulaire_par_arc) ;
//
//                // Rappel : on est par défaut en FillRule NON_ZERO => pour faire une surface avec un trou, il suffit
//                // de faire deux contours dans des sens contraires (trigo et antitrigo)
//                cae.gc.beginPath();
//
//                // Tracé du contour, ou du trou (chemin fermé), dans le sens trigo
//                cae.completerPathAvecContourFerme(x_arc,y_arc);
//
//                // Tracé du contour (apparemment, cela ne termine pas le path, on peut continuer à lui ajouter des éléments
//                cae.gc.stroke();
//
//                if (conique.typeSurface() == Obstacle.TypeSurface.CONCAVE) {
//                    // Tracé du rectangle de la zone visible, dans le sens antitrigo : le Path de l'ellipse sera un trou
//                    // dans cette zone
//                    cae.completerPathAvecContourZoneVisibleAntitrigo();
//                }
//
//                // Le fill déclenche aussi l'appel closePath
//                cae.gc.fill();
//
//            }
//            else { // Aucun point du dioptre n'est dans la zone visible
//                if (conique.contient(cae.boite_limites().centre())) {
//
//                    sc.selectionne_tous();
//
//                    // Toute la zone visible est dans la masse de l'objet conique
//                    CanvasAffichageEnvironnement.remplirPolygone(cae, sc.xcoins_selectionne(true), sc.ycoins_selectionne(true));
//                } else {
//                    // Toute la zone visible est hors de la masse de la conique
//                    // rien à faire
//                }
//            }
//
//            // C'est fini
//            return ;
//        }
//
//        // Au moins 2 intersections, et jusqu'à 8...
//
//        ArrayList<Double> x_masse = new ArrayList<Double>(nombre_pas_angulaire_par_arc+4) ;
//        ArrayList<Double> y_masse = new ArrayList<Double>(nombre_pas_angulaire_par_arc+4) ;
//
//        // Boucle sur les intersections, dans le sens trigo par rapport au centre de l'écran
//        for (int i=0 ; i<valeurs_theta_intersection.size(); i++) {
//            double theta_deb = valeurs_theta_intersection.get(i) ;
//            if (theta_deb<0)
//                theta_deb += 2*Math.PI ;
//
//            int i_suivant = (i + 1) % (valeurs_theta_intersection.size()) ;
//            double theta_fin ;
//
//            if (i_suivant != i)
//                theta_fin = valeurs_theta_intersection.get(i_suivant) ;
//            else
//                theta_fin=theta_deb + 2*Math.PI ;
//            if (theta_fin<0)
//                theta_fin += 2*Math.PI ;
//
//            double x_deb = valeurs_x_intersection.get(i) ;
//            double y_deb = valeurs_y_intersection.get(i) ;
//            Point2D pt_deb = new Point2D(x_deb,y_deb) ;
//            double x_fin = valeurs_x_intersection.get(i_suivant) ;
//            double y_fin = valeurs_y_intersection.get(i_suivant) ;
//            Point2D pt_fin = new Point2D(x_fin,y_fin) ;
//
//            if (theta_fin<theta_deb)
//                theta_fin += 2*Math.PI ;
//
//
//            Point2D pt = conique.point_sur_conique((theta_deb+theta_fin)/2 ) ;
//
//            // Si cet arc est visible
//            if (pt!=null && cae.boite_limites().contains(pt)) {
//                ArrayList<Double> x_arc = new ArrayList<Double>(nombre_pas_angulaire_par_arc) ;
//                ArrayList<Double> y_arc = new ArrayList<Double>(nombre_pas_angulaire_par_arc) ;
//
//                // Ajouter le point exact de l'intersection pt_deb pour éviter les décrochages dûs au pas du tracé
//                x_arc.add(x_deb) ;
//                y_arc.add(y_deb) ;
//
//                x_arc.addAll(conique.xpoints_sur_conique(theta_deb,theta_fin, nombre_pas_angulaire_par_arc)) ;
//                y_arc.addAll(conique.ypoints_sur_conique(theta_deb,theta_fin, nombre_pas_angulaire_par_arc)) ;
//
//                // Ajouter le point exact de l'intersection pt_fin pour éviter les décrochages dûs au pas du tracé
//                x_arc.add(x_fin) ;
//                y_arc.add(y_fin) ;
//
//                // On trace l'arc de ce contour visible
//                cae.tracerPolyligne(x_arc,y_arc);
//
//                x_masse.addAll(x_arc) ;
//                y_masse.addAll(y_arc) ;
//
//                x_arc.clear();
//                y_arc.clear();
//
//                // Si les 2 intersections sont sur un même bord et que leur milieu est dans la conique, il n'y a pas
//                // d'autre arc de contour à tracer, on peut sortir tout de suite de la boucle sur les intersections
//                if ( (x_deb==x_fin || y_deb==y_fin) && conique.contient(pt_deb.midpoint(pt_fin)))
//                    break ;
//
//                // Sinon, chercher les coins contigus (càd non séparés des extrémités par une intersection) et qui sont
//                // dans l'interieur du contour, que la conique soit convexe ou concave
//                SelecteurCoins sc_coins_interieurs = sc.sequence_coins_continus(false,pt_deb,pt_fin,valeurs_x_intersection,valeurs_y_intersection) ;
//
//                if(     ( conique.typeSurface()== Obstacle.TypeSurface.CONVEXE
//                          && conique.contient(sc_coins_interieurs.coin(sc_coins_interieurs.coin_depart)) )
//                      || ( conique.typeSurface()== Obstacle.TypeSurface.CONCAVE
//                        && !conique.contient(sc_coins_interieurs.coin(sc_coins_interieurs.coin_depart)) )
//                   ) {
//                    // Les ajouter au tracé du contour c
//                    x_masse.addAll(sc_coins_interieurs.xcoins_selectionne_antitrigo(true));
//                    y_masse.addAll(sc_coins_interieurs.ycoins_selectionne_antitrigo(true));
//
//                    break ;
//                }
//
//            } else { // Arc non visible
//
//                // Ajouter la sequence des coins de cette portion (dans ordre trigo) si ils sont dans la conique (et si il y en a)
//                SelecteurCoins sc_coins_interieurs = sc.sequence_coins_continus(true,pt_deb,pt_fin,valeurs_x_intersection,valeurs_y_intersection) ;
//                if(  ( conique.typeSurface()== Obstacle.TypeSurface.CONVEXE
//                        && conique.contient(sc_coins_interieurs.coin(sc_coins_interieurs.coin_depart)) )
//                        || ( conique.typeSurface()== Obstacle.TypeSurface.CONCAVE
//                        && !conique.contient(sc_coins_interieurs.coin(sc_coins_interieurs.coin_depart)) )
//                ) {
//                    // Les ajouter au contour de masse
//                    x_masse.addAll(sc_coins_interieurs.xcoins_selectionne(true));
//                    y_masse.addAll(sc_coins_interieurs.ycoins_selectionne(true));
//                }
//
//            }
//        } // Fin boucle sur intersections
//
//        cae.gc.beginPath();
//
//        if (conique.typeSurface() == Obstacle.TypeSurface.CONCAVE) {
//            // Tracé du rectangle de la zone visible, dans le sens antitrigo : le Path de l'ellipse sera un trou
//            // dans cette zone
//            cae.gc.moveTo(cae.xmax(), cae.ymin());
//            cae.gc.lineTo(cae.xmin(), cae.ymin());
//            cae.gc.lineTo(cae.xmin(), cae.ymax());
//            cae.gc.lineTo(cae.xmax(), cae.ymax());
//        }
//        // Tracé du contour, ou du trou (chemin fermé), dans le sens trigo
//        cae.completerPathAvecContourFerme(x_masse,y_masse);
//
//        cae.gc.fill();
//
//    }




//    // Retourne la sequence des coins reliés continument à un point de départ pt_deb sur un (autre) bord dans le sens
//    // trigo ou antitrigo (en partant de pt_fin jusqu'à pt_deb), jusqu'au bord où se trouve un point final pt_fin
//    private SelecteurCoins sequence_coins_continus(boolean sens_trigo, Point2D pt_deb, Point2D pt_fin, Collection<Double> x_inter, Collection<Double>  y_inter) {
//
//        SelecteurCoins sc = new SelecteurCoins(cae.xmin(), cae.ymin(), cae.xmax(), cae.ymax());
//
//        ArrayList<SelecteurCoins.Coin> res = new ArrayList<SelecteurCoins.Coin>(4) ;
//
//        SelecteurCoins.Coin c_courant = SelecteurCoins.Coin.HD;
//
//        if (sens_trigo) {
//            if (pt_deb.getX() == cae.xmin())
//                c_courant = SelecteurCoins.Coin.BG;
//            if (pt_deb.getX() == cae.xmax())
//                c_courant = SelecteurCoins.Coin.HD;
//            if (pt_deb.getY() == cae.ymin())
//                c_courant = SelecteurCoins.Coin.BD;
//            if (pt_deb.getY() == cae.ymax())
//                c_courant = SelecteurCoins.Coin.HG;
//        } else { // Sens antitrigo
//            if (pt_fin.getX() == cae.xmin())
//                c_courant = SelecteurCoins.Coin.HG;
//            if (pt_fin.getX() == cae.xmax())
//                c_courant = SelecteurCoins.Coin.BD;
//            if (pt_fin.getY() == cae.ymin())
//                c_courant = SelecteurCoins.Coin.BG;
//            if (pt_fin.getY() == cae.ymax())
//                c_courant = SelecteurCoins.Coin.HD;
//        }
//
//        sc.coin_depart = c_courant ;
//
//        Point2D pt_courant = pt_deb ;
//
//        if (!sens_trigo)
//            pt_courant = pt_fin ;
//
//        while ( !intersection_bord_entre(pt_courant,sc.coin(c_courant),x_inter,y_inter) ) {
//            sc.selectionne(c_courant);
//            pt_courant = sc.coin(c_courant) ;
//
//            if (sens_trigo)
//                c_courant = sc.coin_suivant(c_courant) ;
//            else
//                c_courant = sc.coin_precedent(c_courant) ;
//        }
//
//        return sc ;
//
//    }
//
//    // Recherche une intersection entre deux points d'un même bord
//    private boolean intersection_bord_entre(Point2D pt_deb,Point2D pt_fin, Collection<Double> x_inter, Collection<Double>  y_inter) {
//
//        Iterator<Double> itx = x_inter.iterator() ;
//        Iterator<Double> ity = y_inter.iterator() ;
//
//        double x,y ;
//
//        while (itx.hasNext() && ity.hasNext()) {
//            x = itx.next();
//            y = ity.next() ;
//            if (pt_deb.getX()==pt_fin.getX()) { // On cherche sur un bord vertical
//                if (x == pt_deb.getX()) {
//                    if (pt_deb.getY()< y && y < pt_fin.getY())
//                        return true ;
//                    if (pt_fin.getY()< y && y < pt_deb.getY())
//                        return true ;
//                }
//            } else if (pt_deb.getY()==pt_fin.getY()) { // On cherche sur un bord horizontal
//                if (y == pt_deb.getY()) {
//                    if (pt_deb.getX()< x && x < pt_fin.getX())
//                        return true ;
//                    if (pt_fin.getX()< x && x < pt_deb.getX())
//                        return true ;
//                }
//            } else
//                System.err.println("Je ne devrais pas être ici");
//        }
//        return false;
//    }


//
//    protected Point2D point_sur_cercle(Cercle cercle,double theta) {
//
//        double x_centre = cercle.Xcentre() ;
//        double y_centre = cercle.Ycentre() ;
//        double rayon = cercle.rayon(); ;
//
//        return new Point2D(x_centre + rayon*Math.cos(theta), y_centre+rayon*Math.sin(theta)) ;
//
//    }
//
//    protected ArrayList<Double> xpoints_sur_cercle(Cercle cercle, double theta_debut,double theta_fin) {
//        ArrayList<Double> xpoints_cercle = new ArrayList<Double>() ;
//
//        double pas = (theta_fin-theta_debut) / nombre_pas_angulaire_par_arc ;
//
//        double theta = theta_debut ;
//
//        Point2D pt;
//        do {
//            pt = point_sur_cercle(cercle,theta);
//
//            if (pt != null)
//                xpoints_cercle.add(pt.getX());
//
//            theta += pas;
//        } while (theta <= theta_fin);
//
//        // Point final pour theta_fin, pour rattraper les erreurs d'arrondi
//        pt = point_sur_cercle(cercle,theta_fin);
//        if (pt != null)
//            xpoints_cercle.add(pt.getX());
//
//        return xpoints_cercle ;
//    }
//
//    protected ArrayList<Double> ypoints_sur_cercle(Cercle cercle,double theta_debut,double theta_fin) {
//        ArrayList<Double> ypoints_cercle = new ArrayList<Double>() ;
//
//        double pas = (theta_fin-theta_debut) / nombre_pas_angulaire_par_arc ;
//
//        double theta = theta_debut ;
//
//        Point2D pt;
//        do {
//            pt = point_sur_cercle(cercle,theta);
//
//            if (pt != null)
//                ypoints_cercle.add(pt.getY());
//
//            theta += pas;
//        } while (theta <= theta_fin);
//
//        // Point final pour theta_fin, pour rattraper les erreurs d'arrondi
//        pt = point_sur_cercle(cercle,theta_fin);
//        if (pt != null)
//            ypoints_cercle.add(pt.getY());
//
//        return ypoints_cercle ;
//    }



}
