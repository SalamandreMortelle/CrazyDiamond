package CrazyDiamond.Controller;

import CrazyDiamond.Model.*;
import javafx.animation.AnimationTimer;
import javafx.geometry.BoundingBox;
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
import java.util.stream.Stream;

public class VisiteurAffichageEnvironnement implements VisiteurEnvironnement {

    CanvasAffichageEnvironnement cae;

    // AnimationTimer utilisé pour animer les contours des obstacles sélectionnés, des rayons, etc.
    AnimationTimer anim_timer ;

    /** Map contenant les contours, visibles dans les limites du canvas, de chacun des obstacles de l'environnement.
     * Ces contours sont constitués des contours des surfaces d'une part et des contours de la masse d'autre part (si
     * l'obstacle a une épaisseur [obstacle "avec matière"]).
     */
    private final Map<Obstacle, ContoursObstacle> contours_visibles_obstacles;

    private static final Logger LOGGER = Logger.getLogger( "CrazyDiamond" );


    int nombre_pas_angulaire_par_arc = 300 ;

    private final Font fonte_labels = Font.font("Serif", FontPosture.ITALIC, -1);

    // Marge des labels sur l'axe X
    private final double marge_label_x = 5d;

    // Marge des labels sur l'axe Y
    private final double marge_label_y = -5d;
    private final double facteur_zoom_grand_label = 2d ;
    private final double facteur_zoom_petit_label = 1.3d ;

    // Intervalle clignotement en nanosecondes ;
    long periode_clignotement = 1000000000/4 ; // 0.25s en nanosecondes
    private long dernier_instant_clignotement = 0;

    public VisiteurAffichageEnvironnement(CanvasAffichageEnvironnement cae) {

        if (cae==null)
            throw new IllegalArgumentException("Le CanvasAffichageEnvironnement du visiteur d'affichage ne peut pas être 'null'.") ;

        this.cae = cae;

        contours_visibles_obstacles = new HashMap<>() ;

        anim_timer = new AnimationTimer() {

            @Override
            public void handle(long now) {

                // Inutile d'afficher à nouveau si une période de clignotement ne s'est pas écoulée. Si jamais l'environnement
                // est modifié au cours de la période, la nouvelle position de la sélection sera affichée immédiatement
                // (cf. méthode apresVisiteEnvironnement()). Sans cette garde, la méthode afficheSelections serait appelée à
                // haute fréquence (60 fps si possible), ce qui entraine une charge CPU de 3 à 5% en permanence. Quel gâchis !
                if (((now - dernier_instant_clignotement)< periode_clignotement))
                    return ;

                dernier_instant_clignotement = now ;

                afficheSelections(now);

            }

        } ;

        // Le timer tourne en permanence : inutile d'appeler rafraichirAffichage quand on sélectionne/déselectionne un élément
        anim_timer.start();

    }

    private void afficheSelections(long now) {

        cae.effacerSelection();

        cae.selection().stream_sources().forEach(s -> afficheSelectionSource(s,now));
        cae.selection().stream_obstacles().forEach(o -> afficheSelectionObstacle(o,now,cae.selection().nombreObstaclesReels()==1));
        cae.selection().stream_socs().forEach(soc -> afficheSelectionSystemeOptiqueCentre(soc,now));

        afficheZoneSelectionRectangulaire();
    }

    private void afficheZoneSelectionRectangulaire() {
        BoundingBox zone = cae.zoneSelectionRectangulaire() ;

        if (zone==null)
            return;

        GraphicsContext gc = cae.gc_selection() ;
        Paint pf = gc.getFill() ;
        Paint s = gc.getStroke() ;

        double lw = gc.getLineWidth() ;
        double pas = cae.resolution() ;

        gc.setLineWidth(1*pas);

        gc.setLineDashes(3*pas,3*pas);

        gc.setStroke(Color.WHITE);
        gc.setFill(Color.BLACK);


        gc.strokeRect(zone.getMinX(),zone.getMinY(),zone.getWidth(),zone.getHeight());

        gc.setLineDashes(null);
        gc.setLineWidth(lw);

        gc.setFill(pf);
        gc.setStroke(s);
    }


    @Override
    public void avantVisiteEnvironnement(Environnement e) {

        // Réinitialiser les contours déjà calculés, pour ne pas garer les contours d'objets qui ont été supprimés
        // de l'Environnement
        contours_visibles_obstacles.clear();

        // NB : On pourrait envisager une optimisation en mémorisant les (morceaux de) contours précédemment calculés
        // de chaque obstacle, et en se contentant de calculer, si besoin, les nouveaux morceaux manquants lorsque le visiteur
        // est appelé pour réafficher à nouveau l'obstacle.
        // Il faudrait alors que la map contienne des java.lang.ref.WeakReference de chaque Obstacle (au lieu de références simples),
        // afin que le GC puisse effacer ceux qui ont été supprimés de l'environnement et qui ne seront plus jamais affichés, si on veut
        // éviter une fuite mémoire.

    }

    @Override
    public void apresVisiteEnvironnement(Environnement e) {

        // Actualisation de l'affichage des sélections (en dehors du timer d'animation : now = 0)
        afficheSelections(dernier_instant_clignotement);

    }


        @Override
    public void avantVisiteSources() {
        VisiteurEnvironnement.super.avantVisiteSources();

        cae.gc_affichage().setGlobalAlpha(0.6);

        cae.gc_affichage().setGlobalBlendMode(BlendMode.HARD_LIGHT);

        cae.gc_affichage().setLineJoin(StrokeLineJoin.ROUND);

        cae.gc_affichage().setLineWidth(2*cae.resolution());
    }

    @Override
    public void apresVisiteSources() {
        VisiteurEnvironnement.super.apresVisiteSources();

        cae.gc_affichage().setLineWidth(cae.resolution());

        cae.gc_affichage().setGlobalBlendMode(BlendMode.SRC_OVER);

        cae.gc_affichage().setLineJoin(StrokeLineJoin.MITER);

        cae.gc_affichage().setGlobalAlpha(1.0);
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
        GraphicsContext gc = cae.gc_affichage();

        Paint s = gc.getStroke();
        gc.setStroke(seg.couleurContour());

        // TODO : A optimiser pour ne tracer que dans la zone visible (rechercher les intersections du segment avec les bords)

//        double lw = gc_affichage.getLineWidth() ;
//        gc_affichage.setLineWidth(2*eg.resolution);

        if (seg.rayonDiaphragmeProperty().get()==0d) {
            traceLigne(seg.depart() , seg.arrivee());
            // On ne s'embête pas à clipper le segment dans la zone visible... Mais l'épaisseur de celui-ci peut devenir
            // très mince si on zoome beaucoup...

            ContoursObstacle co = new ContoursObstacle();
            Contour c_surf = new Contour();

            c_surf.ajoutePoint(seg.x1(), seg.y1());
            c_surf.ajoutePoint(seg.x2(), seg.y2());
            co.ajouterContourSurface(c_surf);

            contours_visibles_obstacles.put(seg, co);
        } else { // Présence d'une pupille dans le segment
            Point2D dep = seg.depart() ;
            Point2D dep_pup = seg.departPupille() ;
            Point2D arr = seg.arrivee() ;
            Point2D arr_pup = seg.arriveePupille() ;
            traceLigne(dep,dep_pup);
            traceLigne(arr_pup,arr);

            ContoursObstacle co = new ContoursObstacle();
            Contour c_surf = new Contour();

            c_surf.ajoutePoint(dep);
            c_surf.ajoutePoint(dep_pup);
            co.ajouterContourSurface(c_surf);

            c_surf = new Contour() ;
            c_surf.ajoutePoint(arr_pup);
            c_surf.ajoutePoint(arr);
            co.ajouterContourSurface(c_surf);

            contours_visibles_obstacles.put(seg, co);

        }

        gc.setStroke(s);

    }

    @Override
    public void visiteCercle(Cercle cercle) {

        GraphicsContext gc = cae.gc_affichage() ;

        Paint s = gc.getStroke() ;
        Paint pf = gc.getFill() ;

        Paint couleur_masse = cercle.couleurMatiere() ;
        Paint couleur_bord = cercle.couleurContour() ;

        gc.setStroke(couleur_bord);
        gc.setFill(couleur_masse);

        ContoursObstacle co = cercle.couper(cae.boite_limites(), nombre_pas_angulaire_par_arc) ;

        contours_visibles_obstacles.put(cercle,co) ;

        cae.afficherContoursObstacle(co) ;

        // TODO : on pourrait aussi utiliser un gc_affichage.restore() (précédé d'un gc_affichage.save() en début de méthode)
        gc.setFill(pf);
        gc.setStroke(s);

    }

    public void afficheSelectionObstacle(Obstacle o_selectionne, long temps, boolean avec_poignees) {
        if (o_selectionne instanceof Groupe grp) {
            for (Obstacle o : grp.iterableObstaclesReelsDepuisArrierePlan())
                afficheSelectionObstacle(o,temps,avec_poignees);
        } else
            afficheSelectionObstacleReel(o_selectionne,temps,avec_poignees);

    }
    private void afficheSelectionObstacleReel(Obstacle o, long temps,boolean avec_poignees) {

        ContoursObstacle co = contours_visibles_obstacles.get(o) ;

        // Si les contours de l'obstacle ne sont pas encore calculés, ne rien faire
        if (co == null)
            return ;

        // On affiche le clignotement de sélection directement dans le gc_selection
        GraphicsContext gc = cae.gc_selection() ;

        Paint pf = gc.getFill() ;

        Paint s = gc.getStroke() ;
        double lw = gc.getLineWidth() ;
        double pas = cae.resolution() ;
        gc.setLineWidth(2*pas);

        gc.setLineDashes(5*pas,10*pas);

        if ((temps/ periode_clignotement)%2==0) {
            gc.setStroke(Color.WHITE);
            gc.setFill(Color.BLACK);

        }
        else {
            gc.setStroke(Color.BLACK);
            gc.setFill(Color.WHITE);

        }

        cae.afficherContourSurfaceObstacle(co,gc) ;

        gc.setLineDashes(null);
        gc.setLineWidth(lw);

        if (avec_poignees)
            cae.afficherPoignees(o.positions_poignees(),gc);

        gc.setFill(pf);
        gc.setStroke(s);
    }

    private void afficheSelectionSource(Source src, long temps) {
        GraphicsContext gc = cae.gc_selection() ;
        Paint pf = gc.getFill() ;

        Paint s = gc.getStroke() ;
        double lw = gc.getLineWidth() ;
        double pas = cae.resolution() ;
        gc.setLineWidth(1*pas);

        gc.setLineDashes(5*pas,10*pas);

        if ((temps/ periode_clignotement)%2==0) {
            gc.setStroke(Color.WHITE);
            gc.setFill(Color.BLACK);

        }
        else {
            gc.setStroke(Color.BLACK);
            gc.setFill(Color.WHITE);

        }

        if (src.type()== Source.TypeSource.PROJECTEUR) {
            Point2D[] extremites = src.extremitesProjecteur() ;
            traceLigne(extremites[0] ,extremites[1]);
        }

        gc.setLineDashes(null);
        gc.setLineWidth(lw);

        cae.afficherPoignees(src.positions_poignees(),gc);

        gc.setFill(pf);
        gc.setStroke(s);

    }

    private void afficheSelectionSystemeOptiqueCentre(SystemeOptiqueCentre soc, long temps) {
        GraphicsContext gc = cae.gc_selection() ;
        Paint pf = gc.getFill() ;

        Paint s = gc.getStroke() ;
        double lw = gc.getLineWidth() ;
        double pas = cae.resolution() ;
        gc.setLineWidth(1*pas);

//        gc_affichage.setLineDashes(5*pas,10*pas);

        if ((temps/ periode_clignotement)%2==0) {
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

            cae.tracerContour(c,gc);

            gc.setLineDashes();
        }


//        gc_affichage.setLineDashes();
        gc.setLineWidth(lw);

        cae.afficherPoignees(soc.positions_poignees(),gc);

        gc.setFill(pf);
        gc.setStroke(s);

    }

    @Override
    public void visiteDemiPlan(DemiPlan dp) {
        GraphicsContext gc = cae.gc_affichage() ;

        Paint s = gc.getStroke() ;
        Paint pf = gc.getFill() ;

        Paint couleur_masse = dp.couleurMatiere() ;
        Paint couleur_bord  = dp.couleurContour() ;

        gc.setStroke(couleur_bord);
        gc.setFill(couleur_masse);

        ContoursObstacle co = dp.couper(cae.boite_limites()) ;

        contours_visibles_obstacles.put(dp,co) ;

        cae.afficherContoursObstacle(co) ;

        // TODO : on pourrait aussi utiliser un gc_affichage.restore() (précédé d'un gc_affichage.save() en début de méthode)
        gc.setFill(pf);
        gc.setStroke(s);

    }

    /**
     * Visite un rectangle et l'affiche (contour+masse). Cette méthode ne réalise aucun tracé, ni remplissage hors de
     * la zone visible du CanvasAffichageEnvironnement (optimisation de la mémoire du Canvas qui n'est pas sollicitée
     * pour les éléments ou parties d'éléments qui sont invisibles.
     * @param rect : le rectangle à afficher
     */
    @Override
    public void visiteRectangle(Rectangle rect) {
        GraphicsContext gc = cae.gc_affichage() ;

        Paint s = gc.getStroke() ;
        Paint pf = gc.getFill() ;

        Paint couleur_masse = rect.couleurMatiere() ;
        Paint couleur_bord  = rect.couleurContour() ;

        gc.setStroke(couleur_bord);
        gc.setFill(couleur_masse);

        ContoursObstacle co = rect.couper(cae.boite_limites()) ;

        contours_visibles_obstacles.put(rect,co) ;

        cae.afficherContoursObstacle(co) ;

        // TODO : on pourrait aussi utiliser un gc_affichage.restore() (précédé d'un gc_affichage.save() en début de méthode)
        gc.setFill(pf);
        gc.setStroke(s);
    }

    @Override
    public void visiteLentille(Lentille lentille) {
        visiteCompositionPourObstacle(lentille.composition(),lentille);
    }
    @Override
    public void visitePrisme(Prisme prisme) {
        GraphicsContext gc = cae.gc_affichage() ;

        Paint s = gc.getStroke() ;
        Paint pf = gc.getFill() ;

        Paint couleur_masse = prisme.couleurMatiere() ;
        Paint couleur_bord  = prisme.couleurContour() ;

        gc.setStroke(couleur_bord);
        gc.setFill(couleur_masse);

        ContoursObstacle co = prisme.couper(cae.boite_limites()) ;

        contours_visibles_obstacles.put(prisme,co) ;

        cae.afficherContoursObstacle(co) ;

        // TODO : on pourrait aussi utiliser un gc_affichage.restore() (précédé d'un gc_affichage.save() en début de méthode)
        gc.setFill(pf);
        gc.setStroke(s);
    }

    @Override
    public void visiteConique(Conique conique) {
        GraphicsContext gc = cae.gc_affichage() ;

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

        contours_visibles_obstacles.put(conique,co) ;

        cae.afficherContoursObstacle(co) ;

        gc.setFill(pf);
        gc.setStroke(s);
        // Note : on pourrait aussi utiliser gc_affichage.save() au début de la méthode puis gc_affichage.restore() à la fin
    }

    private void tracerChemin(CheminLumiere c) {

        GraphicsContext gc = cae.gc_affichage();

        Paint s = gc.getStroke() ;
        gc.setStroke(c.couleur);

        double alpha = gc.getGlobalAlpha() ;

        LOGGER.log(Level.FINER,"Tracé du chemin {0}",c);

        int cpt = 0 ;

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
                        traceLigne(r.depart(), p_sortie_boite_limites);
                        LOGGER.log(Level.FINER, "Point d'arrivée hors limites, ramené en {0}, {1}", new Object[]{p_sortie_boite_limites.getX(), p_sortie_boite_limites.getY()});
                    }

                    if (r.phenomene_origine!= Rayon.PhenomeneOrigine.EMISSION_SOURCE && cae.prolongementsArriereVisibles()) {
                        Point2D p_avant_depart = cae.premiere_intersection(r.supportGeometrique().prolongementAvantDepart());
                        if (p_avant_depart != null) cae.montrerProlongementArriere(r.depart(), p_avant_depart);
                    }

                } else if (!cae.contient(pt_deb) && cae.contient(pt_arr)) {
                    Point2D p_entree_boite_limites = cae.premiere_intersection_avec_limites(r) ;
                    if (p_entree_boite_limites!=null) {
                        traceLigne(p_entree_boite_limites, r.arrivee());
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
                        traceLigne(p_entree_boite_limites, p_sortie_boite_limites);
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
                    traceLigne(r.depart(), r.arrivee());

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
//                    gc_affichage.strokeLine(r.depart.getX(), r.depart.getY(), c.point_sortie_environnement.getX(), c.point_sortie_environnement.getY());

                Point2D p_sortie_boite_limites = cae.derniere_intersection_avec_limites(r) ;

                if (p_sortie_boite_limites != null) {
                    if (cae.contient(r.depart())) {
                        traceLigne(r.depart(), p_sortie_boite_limites);
                        LOGGER.log(Level.FINER, "Rayon infini {0} entre {1},{2} et {3},{4}", new Object[]{cpt, r.depart().getX(), r.depart().getY(), p_sortie_boite_limites.getX(), p_sortie_boite_limites.getY()});

                        if (r.phenomene_origine!= Rayon.PhenomeneOrigine.EMISSION_SOURCE && cae.prolongementsArriereVisibles()) {
                            Point2D p_avant_depart = cae.premiere_intersection(r.supportGeometrique().prolongementAvantDepart());
                            if (p_avant_depart != null) cae.montrerProlongementArriere(r.depart(), p_avant_depart);
                        }

                    } else {
                        Point2D p_entree_boite_limites = cae.premiere_intersection_avec_limites(r) ;
                        traceLigne(p_entree_boite_limites, p_sortie_boite_limites);
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

        visiteCompositionPourObstacle(c,c);

    }

    private void visiteCompositionPourObstacle(Composition c,Obstacle o_appartenance) {
        if (c.estVide())
            return;

        GraphicsContext gc = cae.gc_affichage() ;

        Paint s = gc.getStroke() ;
        Paint pf = gc.getFill() ;

        Paint couleur_masse = c.couleurMatiere() ;
        Paint couleur_bord = c.couleurContour() ;

        gc.setStroke(couleur_bord);
        gc.setFill(couleur_masse);

        VisiteurCollecteContours vcc = new VisiteurCollecteContours(cae.boite_limites()) ;

        // Le VisiteurCollecteContours va construire le résultat (la "solution" de la composition)
        c.accepte(vcc);

        ContoursObstacle contours_resultat = vcc.contours(c.typeSurface()) ;

        contours_visibles_obstacles.put(o_appartenance,contours_resultat) ;

        cae.afficherContoursObstacle(contours_resultat) ;

        // TODO : on pourrait aussi utiliser un gc_affichage.restore() (précédé d'un gc_affichage.save() en début de méthode)
        gc.setFill(pf);
        gc.setStroke(s);
    }

    @Override
    public void visiteCompositionDeuxObstacles(CompositionDeuxObstacles c) {

    }

    @Override
    public void visiteSystemeOptiqueCentre(SystemeOptiqueCentre soc)  {

        // Visite récursive de tous les sous-SOCs
        soc.sousSystemesOptiquesCentresPremierNiveau().forEach(this::visiteSystemeOptiqueCentre);

        GraphicsContext gc = cae.gc_affichage() ;

        Paint s = gc.getStroke() ;

        Paint couleur_axe = soc.couleurAxe() ;

        gc.setStroke(couleur_axe);


        Contour c = soc.couper(cae.boite_limites()) ;

        double res = cae.resolution() ;

        if (c!=null) {



//            Point2D origine = soc.origine();
//            Point2D perp = soc.perpendiculaireDirection();


//            // Marquage de l'origine du repère
//            gc_affichage.strokeLine(origine.getX() + 10 * res * perp.getX(), origine.getY() + 10 * res * perp.getY(),
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
//                        gc_affichage.setStroke(Color.RED);
//
//                        if (cae.boite_limites().contains(pt))
//                            gc_affichage.strokeLine(pt.getX() + 10 * res * perp.getX(), pt.getY() + 10 * res * perp.getY(),
//                                    pt.getX() - 10 * res * perp.getX(), pt.getY() - 10 * res * perp.getY());
//
//                        gc_affichage.setStroke(couleur_axe);
//
//                    }
//                } catch (Exception e) {
//                    LOGGER.log(Level.SEVERE,"Exception lors de la recherche des dioptres du SOC",e);
                    }

            }

        }

        for (RencontreDioptreParaxial rdp : soc.dioptresRencontres()) {
            if (!rdp.ignorer())
                afficheDiaphragmeAntecedentDe(soc,rdp,Color.LIGHTGREY);
        }

        afficheDiaphragme(soc,soc.pupilleEntree(),Color.GREEN);
        afficheDiaphragme(soc,soc.lucarneEntree(),Color.LIGHTGREEN);

        afficheRayonsMarginaux(soc);
        
        afficheRayonsLimiteChamps(soc,true,true,true);

        if (soc.MontrerPlansFocaux()) {
            marquePositionSurAxeSOC(soc,soc.ZGeometriquePlanFocalObjet(),Color.LIGHTBLUE,300,"Fo");
            marquePositionSurAxeSOC(soc,soc.ZGeometriquePlanFocalImage(),Color.LIGHTBLUE,300,"Fi");
        }

        if (soc.MontrerPlansPrincipaux()) {
            marquePositionSurAxeSOC(soc,soc.ZGeometriquePlanPrincipalObjet(),Color.LIGHTYELLOW,300,"Ho");
            marquePositionSurAxeSOC(soc,soc.ZGeometriquePlanPrincipalImage(),Color.LIGHTYELLOW,300,"Hi");
        }

        if (soc.MontrerPlansNodaux()) {
            marquePositionSurAxeSOC(soc,soc.ZGeometriquePlanNodalObjet(),Color.PALEVIOLETRED,300,"No");
            marquePositionSurAxeSOC(soc,soc.ZGeometriquePlanNodalImage(),Color.PALEVIOLETRED,300,"Ni");
        }

        if (soc.MontrerObjet()) {
            afficheFlechePerpendiculaireAxeSOC(soc, soc.ZGeometriqueObjet(), Color.GREEN, soc.HObjet());
            afficheLabelSOC(soc, soc.ZGeometriqueObjet(), 0d, Color.GREEN,"Ao");
            afficheLabelSOC(soc, soc.ZGeometriqueObjet(), soc.HObjet(), Color.GREEN,"Bo");

        }

        if (soc.MontrerImage() && soc.HImage()!=null) {
            afficheFlechePerpendiculaireAxeSOC(soc, soc.ZGeometriqueImage(), Color.GREEN, soc.HImage());
            afficheLabelSOC(soc, soc.ZGeometriqueImage(), 0d, Color.GREEN,"Ai");
            afficheLabelSOC(soc, soc.ZGeometriqueImage(), soc.HImage(), Color.GREEN,"Bi");

            afficheConstructionImage(soc);
        }


        // TODO : on pourrait aussi utiliser un gc_affichage.restore() (précédé d'un gc_affichage.save() en début de méthode)
        gc.setStroke(s);


    }

    private void afficheConstructionImage(SystemeOptiqueCentre soc) {

        if (soc.ZGeometriqueObjet()==null)
            return;

        GraphicsContext gc = cae.gc_affichage() ;
        Color c_constr = Color.GREEN ;
        Paint s = gc.getStroke() ;
        Paint f = gc.getFill() ;

        gc.setStroke(c_constr);
        gc.setFill(c_constr);

        traceParalleleAxeOptiqueEmergentParFoyerImage(soc);

        traceIncidentParPointNodalObjetEmergentParalleleParPointNodalImage(soc);

        traceIncidentParFoyerObjetEmergentParalleleAxeOptique(soc);

        gc.setFill(f);
        gc.setStroke(s);

    }

    private void traceIncidentParFoyerObjetEmergentParalleleAxeOptique(SystemeOptiqueCentre soc) {

        if (soc.ZGeometriquePlanFocalObjet()==null||soc.ZGeometriqueImage()==null||soc.HImage()==null)
            return;

        //
        // Rayon passant par le foyer objet, émerge du plan principal image parallèlement à l'axe du système à même
        // hauteur que son intersection avec le plan principal objet,
        //
        Point2D pt_obj = soc.point(soc.ZGeometriqueObjet(), soc.HObjet()) ; // Point Bo
        Point2D pt_foc_obj = soc.point(soc.ZGeometriquePlanFocalObjet(), 0)  ;
        Point2D pt_img = soc.point(soc.ZGeometriqueImage(), soc.HImage()) ; // Point Bi

//        if (Environnement.quasiConfondus(pt_obj,pt_foc_obj))
//            return;
//        if (Environnement.quasiEgal(pt_obj.getX(), pt_foc_obj.getX())) // Il faudrait tester que pt_obj et pt_foc_obj ont m^me valeur de Zoptique...
//            return ;
        if (Environnement.quasiEgal(soc.ZGeometriqueObjet(),soc.ZGeometriquePlanFocalObjet()))
            return;

        DemiDroiteOuSegment dd_incident_fo = new DemiDroiteOuSegment(pt_obj, pt_foc_obj.subtract(pt_obj)) ;
        Point2D pt_entree_incident_fo = soc.intersectionDroiteSupportAvecPlan(dd_incident_fo, soc.ZPlanEntree()) ;
        Point2D int_avec_plan_pr_obj = soc.intersectionDroiteSupportAvecPlan(dd_incident_fo, soc.ZGeometriquePlanPrincipalObjet());

        // 1. Jusqu'au plan d'entrée
        if (soc.ZGeometriqueObjet()< soc.ZPlanEntree()) { // Les rayons incidents vont partir de Bo
            if (soc.ZGeometriquePlanFocalObjet()< soc.ZGeometriqueObjet()) {
                traceLignePointillee(pt_foc_obj, pt_obj);
                if (soc.ZGeometriquePlanPrincipalObjet()< soc.ZGeometriquePlanFocalObjet())
                    traceLignePointillee(int_avec_plan_pr_obj, pt_foc_obj);
            } else if (soc.ZGeometriquePlanPrincipalObjet()< soc.ZGeometriqueObjet())
                traceLignePointillee(int_avec_plan_pr_obj, pt_obj);

            traceLigneFlechee(pt_obj, pt_entree_incident_fo);
        }
        else { // Les rayons incidents vont partir de -Infini, en direction de Bo
            if (dd_incident_fo.direction().dotProduct(soc.direction())>0) // Recherche du rayon incident qui pointe vers Bo, en partant de Bo
                dd_incident_fo.renverseDirection();
            dd_incident_fo.definirDepart(pt_entree_incident_fo);
            Point2D pt_entree_bl_incident_fo = cae.boite_limites().contains(pt_entree_incident_fo)?
                    cae.boite_limites().premiere_intersection(dd_incident_fo):cae.boite_limites().derniere_intersection(dd_incident_fo);
            if (pt_entree_bl_incident_fo!=null)
                traceLigneFlechee(pt_entree_bl_incident_fo,pt_entree_incident_fo) ;
        }

        // 2. Jusqu'au plan principal objet (s'il se trouve après le plan d'entrée), avec un prolongement vers le Fo si nécessaire.
        if (soc.ZGeometriqueObjet()> soc.ZPlanEntree()) {
            traceLignePointillee(pt_entree_incident_fo, pt_obj);
            if (soc.ZGeometriquePlanFocalObjet() > soc.ZGeometriqueObjet()) {
                traceLignePointillee(pt_obj, pt_foc_obj);
                if (soc.ZGeometriquePlanPrincipalObjet() > soc.ZGeometriquePlanFocalObjet())
                    traceLignePointillee(pt_foc_obj, int_avec_plan_pr_obj);
            } else if (soc.ZGeometriquePlanPrincipalObjet() > soc.ZGeometriqueObjet())
                traceLignePointillee(pt_obj, int_avec_plan_pr_obj);
        } else {
            if (soc.ZGeometriquePlanFocalObjet() > soc.ZPlanEntree()) {
                traceLignePointillee(pt_entree_incident_fo, pt_foc_obj);
                if (soc.ZGeometriquePlanPrincipalObjet() > soc.ZGeometriquePlanFocalObjet())
                    traceLignePointillee(pt_foc_obj, int_avec_plan_pr_obj);
            } else if (soc.ZGeometriquePlanPrincipalObjet()> soc.ZPlanEntree())
                traceLignePointillee(pt_entree_incident_fo,int_avec_plan_pr_obj);
        }

        // 3. Du plan principal objet au plan principal image
        DemiDroiteOuSegment dd_incident_parallele = new DemiDroiteOuSegment(int_avec_plan_pr_obj, soc.direction()) ;
        Point2D proj_int_sur_plan_pr_img = soc.intersectionDroiteSupportAvecPlan(dd_incident_parallele, soc.ZGeometriquePlanPrincipalImage()) ;
        traceLignePointillee(int_avec_plan_pr_obj,proj_int_sur_plan_pr_img);

        // 4. Après le plan principal image

        Point2D pt_sortie_emergent_parallele = soc.intersectionDroiteSupportAvecPlan(dd_incident_parallele, soc.ZGeometriquePlanSortie()) ;
        DemiDroiteOuSegment dd_emergent_parallele = new DemiDroiteOuSegment(pt_sortie_emergent_parallele, soc.direction()) ;

        Point2D pt_sortie_bl_emergent_parallele = cae.boite_limites().contains(pt_sortie_emergent_parallele)?
                cae.boite_limites().premiere_intersection(dd_emergent_parallele):cae.boite_limites().derniere_intersection(dd_emergent_parallele) ;

        if (pt_sortie_bl_emergent_parallele!=null)
            traceLigneFlechee(pt_sortie_emergent_parallele, pt_sortie_bl_emergent_parallele);

        // Prolongements arrière (lignes de rappel) vers le plan principal image, le point image s'ils se trouvent avant le plan de sortie
        if(soc.ZGeometriquePlanPrincipalImage()< soc.ZGeometriquePlanSortie() ) {
            traceLignePointillee(proj_int_sur_plan_pr_img,pt_sortie_emergent_parallele);
            if (soc.ZGeometriqueImage()< soc.ZGeometriquePlanPrincipalImage())
                traceLignePointillee(pt_img,proj_int_sur_plan_pr_img);
        } else if (soc.ZGeometriqueImage()< soc.ZGeometriquePlanSortie())
            traceLignePointillee(pt_img,pt_sortie_emergent_parallele);
    }

    private void traceIncidentParPointNodalObjetEmergentParalleleParPointNodalImage(SystemeOptiqueCentre soc) {

        if (soc.ZGeometriquePlanNodalObjet()==null||soc.ZGeometriquePlanNodalImage()==null
                ||soc.ZGeometriqueImage()==null||soc.HImage()==null)
            return;


        //
        // Rayon passant par le point nodal objet, émerge du point nodal image parallèlement au rayon incident
        //
        Point2D pt_obj = soc.point(soc.ZGeometriqueObjet(), soc.HObjet()) ; // Point Bo
        Point2D pt_nod_obj = soc.point(soc.ZGeometriquePlanNodalObjet(), 0)  ;
        Point2D pt_nod_img = soc.point(soc.ZGeometriquePlanNodalImage(), 0)  ;
        Point2D pt_img = soc.point(soc.ZGeometriqueImage(), soc.HImage()) ; // Point Bi

//        if (Environnement.quasiConfondus(pt_obj,pt_nod_obj))
//            return ;
//        if (Environnement.quasiEgal(pt_obj.getX(), pt_nod_obj.getX())) // Il faut en fait tester s'ils ont même Z
//            return ;
        if (Environnement.quasiEgal(soc.ZGeometriqueObjet(),soc.ZGeometriquePlanNodalObjet()))
            return;

        DemiDroiteOuSegment dd_incident_no = new DemiDroiteOuSegment(pt_obj, pt_nod_obj.subtract(pt_obj)) ;  // BoNo
        Point2D pt_entree_incident_no = soc.intersectionDroiteSupportAvecPlan(dd_incident_no, soc.ZPlanEntree()) ;

        // 1. Jusqu'au plan d'entrée
        if (soc.ZGeometriqueObjet()< soc.ZPlanEntree()) { // Les rayons incidents vont partir de Bo
            if (soc.ZGeometriquePlanNodalObjet()< soc.ZGeometriqueObjet())
                traceLignePointillee(pt_nod_obj, pt_obj);
            traceLigneFlechee(pt_obj, pt_entree_incident_no);

        }
        else { // Les rayons incidents vont partir de -Infini, en direction de Bo
            if (dd_incident_no.direction().dotProduct(soc.direction())>0) // Recherche du rayon incident qui pointe vers Bo, en partant de Bo
                dd_incident_no.renverseDirection();
            dd_incident_no.definirDepart(pt_entree_incident_no);
            Point2D pt_entree_bl_incident_no = cae.boite_limites().contains(pt_entree_incident_no)?
                    cae.boite_limites().premiere_intersection(dd_incident_no):cae.boite_limites().derniere_intersection(dd_incident_no);
            if (pt_entree_bl_incident_no!=null)
                traceLigneFlechee(pt_entree_bl_incident_no,pt_entree_incident_no) ;
        }

        // 2. Jusqu'au plan nodal objet (s'il se trouve après le plan d'entrée)
        if (soc.ZGeometriquePlanNodalObjet()> soc.ZPlanEntree())
            traceLignePointillee(pt_entree_incident_no, pt_nod_obj);

        // 3. Jusqu'au plan nodal image (tracé des parallèles entre les plans nodaux)
        Point2D pt_pl_ni_incident_no = soc.intersectionDroiteSupportAvecPlan(dd_incident_no, soc.ZGeometriquePlanNodalImage()) ;
        traceLignePointillee(pt_nod_obj,pt_pl_ni_incident_no);

        DemiDroiteOuSegment dd_emergent_ni = new DemiDroiteOuSegment(pt_nod_img,dd_incident_no.direction()) ; // Emergent parallèle
        Point2D pt_pl_no_emergent_ni = soc.intersectionDroiteSupportAvecPlan(dd_emergent_ni, soc.ZGeometriquePlanNodalObjet()) ;
        traceLignePointillee(pt_pl_no_emergent_ni, pt_nod_img);

        // 4. Après le plan nodal image
        Point2D pt_sortie_emergent_ni = soc.intersectionDroiteSupportAvecPlan(dd_emergent_ni, soc.ZGeometriquePlanSortie()) ;

        if (dd_emergent_ni.direction().dotProduct(soc.direction())<0)
            dd_emergent_ni.renverseDirection();
        dd_emergent_ni.definirDepart(pt_sortie_emergent_ni);

        Point2D pt_sortie_bl_emergent_ni = cae.boite_limites().contains(pt_sortie_emergent_ni)?
                cae.boite_limites().premiere_intersection(dd_emergent_ni):cae.boite_limites().derniere_intersection(dd_emergent_ni) ;

        if (pt_sortie_bl_emergent_ni!=null)
            traceLigneFlechee(pt_sortie_emergent_ni, pt_sortie_bl_emergent_ni);

        // Prolongements arrière (lignes de rappel) vers le plan nodal image, le point image s'ils se trouvent avant le plan de sortie
        if(soc.ZGeometriquePlanNodalImage()< soc.ZGeometriquePlanSortie() ) {
                traceLignePointillee(pt_nod_img,pt_sortie_emergent_ni);
                if (soc.ZGeometriqueImage()< soc.ZGeometriquePlanNodalImage())
                    traceLignePointillee(pt_img, pt_nod_img);
        } else if (soc.ZGeometriqueImage()< soc.ZGeometriquePlanSortie())
            traceLignePointillee(pt_img,pt_sortie_emergent_ni);
    }

    private void traceParalleleAxeOptiqueEmergentParFoyerImage(SystemeOptiqueCentre soc) {
        //
        // Rayon parallèle à l'axe optique, émerge sur le plan principal image à même hauteur que son intersection avec
        // le plan principal objet et passe par le foyer image
        //

        if (soc.ZGeometriquePlanFocalImage()==null||soc.ZGeometriqueImage()==null
                ||soc.ZGeometriquePlanPrincipalObjet()==null||soc.ZGeometriquePlanPrincipalImage()==null)
            return;

        Point2D pt_obj = soc.point(soc.ZGeometriqueObjet(), soc.HObjet()) ; // Point Bo
        Point2D pt_obj_proj_pl_entree = soc.point(soc.ZPlanEntree(), soc.HObjet()) ; // Point Bo projeté orth sur Plan Entrée
        Point2D pt_foc_img = soc.point(soc.ZGeometriquePlanFocalImage(), 0)  ;
        Point2D pt_img = soc.point(soc.ZGeometriqueImage(), soc.HImage()) ; // Point Bi

        Point2D proj_sur_plan_pr_obj = soc.point(soc.ZGeometriquePlanPrincipalObjet(), soc.HObjet())  ;
        Point2D proj_sur_plan_pr_img = soc.point(soc.ZGeometriquePlanPrincipalImage(), soc.HObjet())  ;

//        if (Environnement.quasiConfondus(proj_sur_plan_pr_img,pt_foc_img))
//            return;
//        if (Environnement.quasiEgal(proj_sur_plan_pr_img.getX(), pt_foc_img.getX()))
//            return ;


        // 1. Jusqu'au plan d'entrée
        if(soc.ZGeometriqueObjet()< soc.ZPlanEntree()) { // Les rayons incidents vont partir de Bo
            if (soc.ZGeometriquePlanPrincipalObjet()< soc.ZGeometriqueObjet())
                traceLignePointillee(proj_sur_plan_pr_obj, pt_obj);

            traceLigneFlechee(pt_obj, pt_obj_proj_pl_entree);
        }
        else { // Les rayons incidents vont partir de -Infini, en direction de Bo
            DemiDroiteOuSegment dd_incident_par_bo = new DemiDroiteOuSegment(pt_obj_proj_pl_entree, soc.direction().multiply(-1));
            Point2D pt_entree_bl_incident_bo = cae.boite_limites().contains(pt_obj_proj_pl_entree)?
                cae.boite_limites().premiere_intersection(dd_incident_par_bo):cae.boite_limites().derniere_intersection(dd_incident_par_bo);
            if (pt_entree_bl_incident_bo!=null)
                traceLigneFlechee(pt_entree_bl_incident_bo, pt_obj_proj_pl_entree) ;
        }

        // 2. Du plan d'entrée jusqu'au plan principal objet (s'il se trouve après le plan d'entrée)
        if (soc.ZGeometriquePlanPrincipalObjet()> soc.ZPlanEntree())
            traceLignePointillee(pt_obj_proj_pl_entree, proj_sur_plan_pr_obj);

        // 3. Du plan principal objet au plan principal image
        traceLignePointillee(proj_sur_plan_pr_obj,proj_sur_plan_pr_img);

        // 4. Après le plan principal image
        DemiDroiteOuSegment dd_emergent_fi = new DemiDroiteOuSegment(proj_sur_plan_pr_img, pt_foc_img.subtract(proj_sur_plan_pr_img)) ;
        Point2D pt_sortie_emergent_fi = soc.intersectionDroiteSupportAvecPlan(dd_emergent_fi, soc.ZGeometriquePlanSortie()) ;

        if (dd_emergent_fi.direction().dotProduct(soc.direction())<0)
            dd_emergent_fi.renverseDirection();
        dd_emergent_fi.definirDepart(pt_sortie_emergent_fi);

        Point2D pt_sortie_bl_emergent_fi = cae.boite_limites().contains(pt_sortie_emergent_fi)?
                cae.boite_limites().premiere_intersection(dd_emergent_fi):cae.boite_limites().derniere_intersection(dd_emergent_fi);

        if (pt_sortie_bl_emergent_fi!=null)  // Du point de sortie du SOC au point de sortie de la zone visible
            traceLigneFlechee(pt_sortie_emergent_fi, pt_sortie_bl_emergent_fi);

        // Prolongements arrière (lignes de rappel) vers le plan focal image, le plan principal image, le point image s'ils se trouvent avant le plan de sortie
        if(soc.ZGeometriquePlanFocalImage()< soc.ZGeometriquePlanSortie() || soc.ZGeometriquePlanPrincipalImage()< soc.ZGeometriquePlanSortie()) {
            if (soc.ZGeometriquePlanFocalImage()< soc.ZGeometriquePlanPrincipalImage()) {
                traceLignePointillee(pt_foc_img, pt_sortie_emergent_fi);
                if (soc.ZGeometriqueImage()< soc.ZGeometriquePlanFocalImage())
                    traceLignePointillee(pt_img, pt_foc_img);

            }
            else {
                traceLignePointillee(proj_sur_plan_pr_img, pt_sortie_emergent_fi);
                if (soc.ZGeometriqueImage()< soc.ZGeometriquePlanPrincipalImage())
                    traceLignePointillee(pt_img,proj_sur_plan_pr_img);

            }
        } else if (soc.ZGeometriqueImage()< soc.ZGeometriquePlanSortie())
            traceLignePointillee(pt_img,pt_sortie_emergent_fi);
    }


    private void traceLigne(Point2D dep, Point2D arr) {
        cae.gc_affichage().strokeLine(dep.getX(),dep.getY(),arr.getX(), arr.getY());
    }

    private void traceLigneFlechee(Point2D dep, Point2D arr) {
        traceLigne(dep,arr);

//        if (dep.getX()==arr.getX() && dep.getY()==arr.getY())
//            return ;

        Point2D dir = arr.subtract(dep).normalize() ;
        Point2D perp = new Point2D(-dir.getY(),dir.getX()) ;
        
        double res = cae.resolution() ;
        
        Point2D pt_extremite = dep.midpoint(arr) ;
        
        Point2D pt_depart_pointe_gauche ;
        Point2D pt_depart_pointe_droite ;

        pt_depart_pointe_gauche = pt_extremite.add(dir.multiply(-8 * res)).add(perp.multiply(+4 * res));
        pt_depart_pointe_droite = pt_extremite.add(dir.multiply(-8 * res)).add(perp.multiply(-4 * res));


        cae.gc_affichage().fillPolygon(
                new double[]{pt_depart_pointe_gauche.getX(), pt_extremite.getX(), pt_depart_pointe_droite.getX()},
                new double[]{pt_depart_pointe_gauche.getY(), pt_extremite.getY(), pt_depart_pointe_droite.getY()},
                3
        );
//        traceLigne(pt_depart_pointe_gauche,pt_extremite);
//        traceLigne(pt_depart_pointe_droite,pt_extremite);
    }
    
    private void traceLignePointillee(Point2D dep, Point2D arr) {
        cae.gc_affichage().setLineDashes(1* cae.resolution(),3*cae.resolution());
        cae.gc_affichage().strokeLine(dep.getX(),dep.getY(),arr.getX(), arr.getY());
        cae.gc_affichage().setLineDashes() ;
    }

    // Affichage des rayons limites du diaphragme d'ouverture (rayons marginaux)
    private void afficheRayonsMarginaux(SystemeOptiqueCentre soc) {

        if (soc.dioptresRencontres()==null)
            return ;

        GraphicsContext gc = cae.gc_affichage() ;
        double res = cae.resolution() ;

        Color c_rm = Color.BLUE ;

        Point2D origine = soc.origine() ;
        Point2D perp = soc.perpendiculaireDirection();

        // Point objet sur l'axe
        Point2D pobjet = origine.add(soc.direction().multiply(soc.ZGeometriqueObjet())) ;
        Point2D pimage = (soc.ZGeometriqueImage()!=null?origine.add(soc.direction().multiply(soc.ZGeometriqueImage())):null) ;

        Point2D pt_prec_haut = pobjet ;
        Point2D pt_prec_bas = pobjet ;

        Paint s = gc.getStroke() ;

        gc.setStroke(c_rm);

        // Objet virtuel ?
        if (soc.dioptresRencontres().size()>0 && soc.dioptresRencontres().get(0).ZGeometrique()<soc.ZGeometriqueObjet())
            gc.setLineDashes(2*res,6*res);

        for (RencontreDioptreParaxial intersection : soc.dioptresRencontres()) {

            if (intersection.HLimiteOuverture()==null)
                continue;

            Point2D pt = origine.add(soc.direction().multiply(intersection.ZGeometrique())) ;
            Point2D pt_haut = pt.add(perp.multiply(intersection.HLimiteOuverture())) ;
            Point2D pt_bas  = pt.add(perp.multiply(-intersection.HLimiteOuverture())) ;

            traceLigne(pt_prec_haut,pt_haut);
            traceLigne(pt_prec_bas,pt_bas);

            gc.setLineDashes(); // Arrêt des pointillés (s'il y en avait)

            pt_prec_haut = pt_haut ;
            pt_prec_bas  = pt_bas ;
        }

        if (soc.ZGeometriqueImage()!=null) {
            // Objet virtuel ?
            if (soc.dioptresRencontres().size() > 0 && soc.dioptresRencontres().get(soc.dioptresRencontres().size() - 1).ZGeometrique() > soc.ZGeometriqueImage())
                gc.setLineDashes(2 * res, 6 * res);

            traceLigne(pt_prec_haut, pimage);
            traceLigne(pt_prec_bas, pimage);
        }

        gc.setLineDashes(); // Arrêt des pointillés (s'il y en avait)

        gc.setStroke(s);

    }

    private void afficheDiaphragmeAntecedentDe(SystemeOptiqueCentre soc, RencontreDioptreParaxial it_avec_diaph, Color c) {

        if (it_avec_diaph.rayonDiaphragme()==null)
            return ;

        GraphicsContext gc = cae.gc_affichage() ;
        double res = cae.resolution() ;


        double z_d =it_avec_diaph.ZGeometrique(), h_d = it_avec_diaph.rayonDiaphragme();
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

        traceLigne(p_dep,p_s1);
        traceLigne(p_s1,p_s2);
        traceLigne(p_s2,p_arr);

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

        GraphicsContext gc = cae.gc_affichage() ;
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
        traceLigne(p_debut_diaphragme_1,p_fin_diaphragme_1);
        traceLigne(p_debut_diaphragme_2,p_fin_diaphragme_2);

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

        GraphicsContext gc = cae.gc_affichage() ;
        double res = cae.resolution() ;

        Point2D origine = soc.origine() ;
        Point2D perp = soc.perpendiculaireDirection();

        // Points objets sur l'axe
        Point2D pobjet = origine.add(soc.direction().multiply(soc.ZGeometriqueObjet())) ;

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

//        gc_affichage.beginPath();
        
        Paint s = gc.getStroke() ;
        
        Point2D pt_prec_cm_haut = pt_objet_cm_haut ;
        Point2D pt_prec_cm_bas = pt_objet_cm_bas ;
        Point2D pt_prec_cpl_haut = pt_objet_cpl_haut ;
        Point2D pt_prec_cpl_bas = pt_objet_cpl_bas ;
        Point2D pt_prec_ct_haut = pt_objet_ct_haut ;
        Point2D pt_prec_ct_bas = pt_objet_ct_bas ;

        // Objet virtuel ?
        if (soc.dioptresRencontres().size()>0 && soc.dioptresRencontres().get(0).ZGeometrique()<soc.ZGeometriqueObjet())
            gc.setLineDashes(2*res,6*res);

        for (RencontreDioptreParaxial intersection : soc.dioptresRencontres()) {

//            if (intersection.HLimiteChamp()==null)
//                continue;

            Point2D pt = origine.add(soc.direction().multiply(intersection.ZGeometrique())) ;

            if (champ_moyen&&intersection.HLimiteChamp()!=null) {
                Point2D pt_cm_haut = pt.add(perp.multiply(intersection.HLimiteChamp()));
                Point2D pt_cm_bas = pt.add(perp.multiply(-intersection.HLimiteChamp()));

                gc.setStroke(c_rm);

                traceLigne(pt_prec_cm_haut, pt_cm_haut);
//                gc_affichage.strokeLine(pt_prec_cm_bas.getX(), pt_prec_cm_bas.getY(), pt_cm_bas.getX(), pt_cm_bas.getY());

                pt_prec_cm_haut = pt_cm_haut ;
                pt_prec_cm_bas  = pt_cm_bas ;
            }

            if (champ_pleine_lumiere&&intersection.HLimiteChampPleineLumiere()!=null) {
                Point2D pt_cpl_haut = pt.add(perp.multiply(intersection.HLimiteChampPleineLumiere()));
                Point2D pt_cpl_bas = pt.add(perp.multiply(-intersection.HLimiteChampPleineLumiere()));

                gc.setStroke(c_rpl);

                traceLigne(pt_prec_cpl_haut, pt_cpl_haut);
//                gc_affichage.strokeLine(pt_prec_cpl_bas.getX(), pt_prec_cpl_bas.getY(), pt_cpl_bas.getX(), pt_cpl_bas.getY());

                pt_prec_cpl_haut = pt_cpl_haut ;
                pt_prec_cpl_bas  = pt_cpl_bas ;
            }

            if (champ_total&&intersection.HLimiteChampTotal()!=null) {
                Point2D pt_ct_haut = pt.add(perp.multiply(intersection.HLimiteChampTotal()));
                Point2D pt_ct_bas = pt.add(perp.multiply(-intersection.HLimiteChampTotal()));

                gc.setStroke(c_rct);

                traceLigne(pt_prec_ct_haut, pt_ct_haut);
//                gc_affichage.strokeLine(pt_prec_ct_bas.getX(), pt_prec_ct_bas.getY(), pt_ct_bas.getX(), pt_ct_bas.getY());

                pt_prec_ct_haut = pt_ct_haut ;
                pt_prec_ct_bas  = pt_ct_bas ;
            }

//            gc_affichage.setStroke(s) ; // Restauration de la couleur de départ
            gc.setLineDashes(); // Arrêt des pointillés (s'il y en avait)

        }
        gc.setStroke(s) ; // Restauration de la couleur de départ
        gc.setLineDashes(); // Arrêt des pointillés (s'il y en avait)

        if (soc.ZGeometriqueImage()==null || soc.ZGeometriquePlanSortie()==null)
            return ;

        // Image virtuelle ?
        if (soc.dioptresRencontres().size()>0 && soc.ZGeometriquePlanSortie()>soc.ZGeometriqueImage())
            gc.setLineDashes(2*res,6*res);

        // Points image d'arrivée
        Point2D pimage = origine.add(soc.direction().multiply(soc.ZGeometriqueImage())) ;

        if (champ_moyen && soc.RChampMoyenImage()!=null) {
            Point2D pt_image_cm_haut = pimage.add(perp.multiply(soc.RChampMoyenImage()));
            Point2D pt_image_cm_bas = pimage.add(perp.multiply(-soc.RChampMoyenImage()));
            gc.setStroke(c_rm);
            traceLigne(pt_prec_cm_haut,pt_image_cm_haut);
//            gc_affichage.strokeLine(pt_prec_cm_bas.getX(),pt_prec_cm_bas.getY(),pt_image_cm_bas.getX(),pt_image_cm_bas.getY());
        }
        if (champ_pleine_lumiere && soc.RChampPleineLumiereImage()!=null) {
            Point2D pt_image_cpl_haut = pimage.add(perp.multiply(soc.RChampPleineLumiereImage()));
            Point2D pt_image_cpl_bas = pimage.add(perp.multiply(-soc.RChampPleineLumiereImage()));
            gc.setStroke(c_rpl);
            traceLigne(pt_prec_cpl_haut,pt_image_cpl_haut);
//            gc_affichage.strokeLine(pt_prec_cpl_bas.getX(),pt_prec_cpl_bas.getY(),pt_image_cpl_bas.getX(),pt_image_cpl_bas.getY());
        }
        if (champ_total && soc.RChampTotalImage()!=null) {
            Point2D pt_image_ct_haut = pimage.add(perp.multiply(soc.RChampTotalImage()));
            Point2D pt_image_ct_bas = pimage.add(perp.multiply(-soc.RChampTotalImage()));
            gc.setStroke(c_rct);
            traceLigne(pt_prec_ct_haut,pt_image_ct_haut);
//            gc_affichage.strokeLine(pt_prec_ct_bas.getX(),pt_prec_ct_bas.getY(),pt_image_ct_bas.getX(),pt_image_ct_bas.getY());
        }

        gc.setStroke(s);
        gc.setLineDashes(); // Arrêt des pointillés (s'il y en avait)

//        gc_affichage.stroke();

    }


    private void marquePositionSurAxeSOC(SystemeOptiqueCentre soc, Double z_sur_axe, Color c,double hauteur) {
        marquePositionSurAxeSOC(soc,z_sur_axe,c,hauteur,null);
    }

    /**
     * Marque une position d'abscisse donnée sur l'axe optique d'un SOC, avec une hauteur et une couleur de marque paramétrables
     * @param soc SOC sur lequel on fait le marquage
     * @param z_sur_axe position de la marque sur l'axe de révolution Z du SOC
     * @param c couleur de la marque
     * @param hauteur : hauteur en nombre de resolutions de l'environnement
     * @param label : texte à positionner sur la marque
     */
    private void marquePositionSurAxeSOC(SystemeOptiqueCentre soc, Double z_sur_axe, Color c,double hauteur, String label) {

        if (z_sur_axe==null)
            return;

        double demi_hauteur =0.5*hauteur ;

        GraphicsContext gc = cae.gc_affichage() ;
        double res = cae.resolution() ;

        Point2D origine = soc.origine();
        Point2D perp = soc.perpendiculaireDirection();


        Point2D pt = origine.add(soc.direction().multiply(z_sur_axe)) ;

        gc.save();

        gc.setStroke(c);

        gc.strokeLine(pt.getX() + demi_hauteur * res * perp.getX(), pt.getY() + demi_hauteur * res * perp.getY(),
                pt.getX() - demi_hauteur * res * perp.getX(), pt.getY() - demi_hauteur * res * perp.getY());

        if (label != null) {

            gc.setFill(c);

            // Position du texte à afficher en coordonnées du GC du Canvas
            Point2D pos_texte_gc = gc.getTransform().transform(pt.getX(), pt.getY()).add(marge_label_x, marge_label_y);

            // Nouvelle transformation à appliquer : simple homothétie centrée sur le point d'affichage du texte
            Affine zoom_texte = new Affine();
            zoom_texte.appendScale(facteur_zoom_grand_label, facteur_zoom_grand_label, pos_texte_gc.getX(), pos_texte_gc.getY());
            gc.setTransform(zoom_texte);

            // Ecriture de l'étiquette
            gc.setFont(fonte_labels);

            gc.fillText(label, pos_texte_gc.getX(), pos_texte_gc.getY());

        }

        gc.restore();

    }

    private void afficheLabelSOC(SystemeOptiqueCentre soc, Double z_sur_axe,double hauteur_sur_axe , Color c, String label) {

        if (z_sur_axe==null)
            return;

        GraphicsContext gc = cae.gc_affichage() ;
        double res = cae.resolution() ;

        Point2D origine = soc.origine();
        Point2D perp = soc.perpendiculaireDirection();

        Point2D pt = origine.add(soc.direction().multiply(z_sur_axe)).add(perp.multiply(hauteur_sur_axe)) ;

        gc.save();

        gc.setStroke(c);

        if (label != null) {

            gc.setFill(c);

            // Position du texte à afficher en coordonnées du GC du Canvas
            Point2D pos_texte_gc = gc.getTransform().transform(pt.getX(), pt.getY()).add(marge_label_x, marge_label_y);

            // Nouvelle transformation à appliquer : simple homothétie centrée sur le point d'affichage du texte
            Affine zoom_texte = new Affine();
            zoom_texte.appendScale(facteur_zoom_petit_label, facteur_zoom_petit_label, pos_texte_gc.getX(), pos_texte_gc.getY());
            gc.setTransform(zoom_texte);

            // Écriture de l'étiquette
            gc.setFont(fonte_labels);

            gc.fillText(label, pos_texte_gc.getX(), pos_texte_gc.getY());

        }

        gc.restore();

    }

    private void afficheFlechePerpendiculaireAxeSOC(SystemeOptiqueCentre soc, Double z_sur_axe, Color c,Double hauteur_fleche) {
        if (z_sur_axe==null || hauteur_fleche==null)
            return;

        double res = cae.resolution() ;

        GraphicsContext gc = cae.gc_affichage() ;

        Point2D origine = soc.origine();
        Point2D perp = soc.perpendiculaireDirection();

        Point2D pt_depart = origine.add(soc.direction().multiply(z_sur_axe)) ;
        Point2D pt_extremite = pt_depart.add(perp.multiply(hauteur_fleche)) ;

        Point2D pt_depart_pointe_gauche ;
        Point2D pt_depart_pointe_droite ;

        if (hauteur_fleche>=0) {
            pt_depart_pointe_gauche = pt_extremite.add(soc.direction().multiply(-4 * res)).add(perp.multiply(-8 * res));
            pt_depart_pointe_droite = pt_extremite.add(soc.direction().multiply(+4 * res)).add(perp.multiply(-8 * res));
        } else { // Renversement du sens de la pointe de la flèche si hauteur_fleche < 0
            pt_depart_pointe_gauche = pt_extremite.add(soc.direction().multiply(-4 * res)).add(perp.multiply(8 * res));
            pt_depart_pointe_droite = pt_extremite.add(soc.direction().multiply(+4 * res)).add(perp.multiply(8 * res));
        }

        Paint s = gc.getStroke() ;

        gc.setStroke(c);


        gc.strokeLine(pt_depart.getX() , pt_depart.getY() ,
                pt_depart.getX() + hauteur_fleche * perp.getX(), pt_depart.getY() + hauteur_fleche * perp.getY());

        traceLigne(pt_depart_pointe_gauche,pt_extremite);
        traceLigne(pt_depart_pointe_droite,pt_extremite);

        gc.setStroke(s) ;
    }

    public ContoursObstacle contoursVisiblesObstacle(Obstacle o) {
        return contours_visibles_obstacles.get(o) ;
    }

    public Stream<Obstacle> streamObstaclesVisibles() {
        return contours_visibles_obstacles.keySet().stream();
    }
}
