package CrazyDiamond.Model;

import javafx.geometry.Point2D;

import java.util.ArrayList;

public interface ObstaclePolaire {

    default Contour arc_polaire(double theta_debut,double theta_fin, int nombre_pas_angulaire_par_arc)  {
        Contour c = new Contour(nombre_pas_angulaire_par_arc) ;

        double pas = (theta_fin-theta_debut) / nombre_pas_angulaire_par_arc ;

        double theta = theta_debut ;

        Point2D pt;
        do {
            pt = point_polaire(theta);

            if (pt != null)
                c.ajoutePoint(pt.getX(),pt.getY());

            theta += pas;
        } while (theta < theta_fin);

        // Point final pour theta_fin, pour rattraper les erreurs d'arrondi
        pt = point_polaire(theta_fin);

        if (pt != null)
            c.ajoutePoint(pt.getX(),pt.getY());

        return c ;
    }

    default Point2D point_polaire(double theta) {

        Double r_polaire = rayon_polaire(theta) ;

        if (r_polaire==null)
            return null ;

        double r = r_polaire.doubleValue() ;

        return new Point2D(centre_polaire().getX()+r*Math.cos(theta), centre_polaire().getY()+r*Math.sin(theta)) ;

    }

    double[][] intersections_horizontale(double y_horizontale, double xmin, double xmax,boolean x_sol_croissant) ;
    double[][] intersections_verticale(double x_verticale, double ymin, double ymax, boolean y_sol_croissant) ;

    public TypeSurface typeSurface() ;

    boolean contient(Point2D p) ;

    Double rayon_polaire(double theta) ;

    Point2D centre_polaire() ;

    default ContoursObstacle couper(BoiteLimiteGeometrique boite, int nombre_pas_angulaire_par_arc) {
        return couper(boite,nombre_pas_angulaire_par_arc,true) ;
    }

    default ContoursObstacle couper(BoiteLimiteGeometrique boite, int nombre_pas_angulaire_par_arc, boolean avec_contours_surface) {
        ContoursObstacle contours = new ContoursObstacle() ;

        double xmin = boite.getMinX() ;
        double xmax = boite.getMaxX() ;
        double ymin = boite.getMinY() ;
        double ymax = boite.getMaxY() ;

        double[][] i_droites = intersections_verticale(xmax, ymin, ymax,true) ;
        double[][] i_hautes  = intersections_horizontale(ymax, xmin, xmax,false) ;
        double[][] i_gauches = intersections_verticale(xmin, ymin, ymax,false) ;
        double[][] i_basses  = intersections_horizontale(ymin, xmin, xmax,true) ;

        SelecteurCoins sc = new SelecteurCoins(xmin, ymin, xmax, ymax);


        // Tableau qui contiendra au plus 4 intervalles [theta min, theta max] où la courbe est visible
        // ordonnés dans le sens trigonométrique en partant de de l'axe X par rapport au centre de l'écran
        ArrayList<Double> valeurs_theta_intersection = new ArrayList<Double>(8) ;

        ArrayList<Double> valeurs_x_intersection = new ArrayList<Double>(8) ;
        ArrayList<Double> valeurs_y_intersection = new ArrayList<Double>(8) ;

        int n_intersections = sc.ordonneIntersections(i_droites, i_hautes, i_gauches, i_basses,
                valeurs_theta_intersection, valeurs_x_intersection, valeurs_y_intersection);

        // Si aucune intersection, --ou si 1 seule intersection (TODO : tester le cas à 1 intersection avec une ellipse)
        if (n_intersections<=1) {

            // Ellipse entièrement contenue dans la zone visible ?
            if (boite.contains(point_polaire(0))) {

                Contour arc = arc_polaire(0, 2 * Math.PI, nombre_pas_angulaire_par_arc) ;

                if (avec_contours_surface)
                    contours.ajouterContourSurface(arc);

                if (typeSurface() == TypeSurface.CONCAVE) {
                    // Tracé du rectangle de la zone visible, dans le sens antitrigo : le Path de l'ellipse sera un trou
                    // dans cette zone
                    contours.ajouterContourMasse(boite.construireContourAntitrigo());
                }

                Contour arc_masse = new Contour(arc) ;
                contours.ajouterContourMasse(arc_masse);

            }
            else { // Aucun point de la surface n'est dans la zone visible
                if (contient(boite.centre())) {
                    // Toute la zone visible est dans la masse de l'objet conique
                    contours.ajouterContourMasse(boite.construireContour());
                } else {
                    // Toute la zone visible est hors de la masse de la conique
                    // rien à faire
                }
            }

            // C'est fini
            return contours ;
        }

        // Au moins 2 intersections, et jusqu'à 8...

        Contour arc_masse = new Contour() ;

        // Boucle sur les intersections, dans le sens trigo par rapport au centre de l'écran
        for (int i=0 ; i<valeurs_theta_intersection.size(); i++) {
            double theta_deb = valeurs_theta_intersection.get(i) ;
            if (theta_deb<0)
                theta_deb += 2*Math.PI ;

            int i_suivant = (i + 1) % (valeurs_theta_intersection.size()) ;
            double theta_fin ;

            if (i_suivant != i)
                theta_fin = valeurs_theta_intersection.get(i_suivant) ;
            else
                theta_fin=theta_deb + 2*Math.PI ;
            if (theta_fin<0)
                theta_fin += 2*Math.PI ;

            double x_deb = valeurs_x_intersection.get(i) ;
            double y_deb = valeurs_y_intersection.get(i) ;
            Point2D pt_deb = new Point2D(x_deb,y_deb) ;
            double x_fin = valeurs_x_intersection.get(i_suivant) ;
            double y_fin = valeurs_y_intersection.get(i_suivant) ;
            Point2D pt_fin = new Point2D(x_fin,y_fin) ;

            if (theta_fin<theta_deb)
                theta_fin += 2*Math.PI ;


            Point2D pt = point_polaire((theta_deb+theta_fin)/2 ) ;

            // Si cet arc est visible
            if (pt!=null && boite.contains(pt)) {

                Contour arc = arc_polaire(theta_deb,theta_fin,nombre_pas_angulaire_par_arc) ;

                // Ajouter les points d'intersection exacts pour un tracé précis
                arc.ajoutePointDevant(x_deb,y_deb);
                arc.ajoutePoint(x_fin,y_fin);

                // Cet arc est à la fois un morceau de la surface de l'obstacle et un morceau de son contour de masse
                if (avec_contours_surface)
                    contours.ajouterContourSurface(arc);

                arc_masse.concatene(arc);

                // Si les 2 intersections sont sur un même bord et que leur milieu est dans la conique, il n'y a pas
                // d'autre arc de contour à tracer, on peut sortir tout de suite de la boucle sur les intersections
                if ( (x_deb==x_fin || y_deb==y_fin) && contient(pt_deb.midpoint(pt_fin)))
                    break ;

                // Sinon, chercher les coins contigus (càd non séparés des extrémités par une intersection) et qui sont
                // dans l'interieur du contour, que la conique soit convexe ou concave
                SelecteurCoins sc_coins_interieurs = sc.sequence_coins_continus(false,pt_deb,pt_fin,valeurs_x_intersection,valeurs_y_intersection) ;

                if(     ( typeSurface()== TypeSurface.CONVEXE
                        && contient(sc_coins_interieurs.coin(sc_coins_interieurs.coin_depart)) )
                        || ( typeSurface()== TypeSurface.CONCAVE
                        && !contient(sc_coins_interieurs.coin(sc_coins_interieurs.coin_depart)) )
                ) {
                    // Les ajouter au tracé du contour de masse
                    arc_masse.concatene(sc_coins_interieurs.coins_selectionne_antitrigo(true));

                    break ;
                }

            } else { // Arc non visible

                // Ajouter la sequence des coins de cette portion (dans ordre trigo) si ils sont dans la conique (et si il y en a)
                SelecteurCoins sc_coins_interieurs = sc.sequence_coins_continus(true,pt_deb,pt_fin,valeurs_x_intersection,valeurs_y_intersection) ;
                if(  ( typeSurface()== TypeSurface.CONVEXE
                        && contient(sc_coins_interieurs.coin(sc_coins_interieurs.coin_depart)) )
                        || ( typeSurface()== TypeSurface.CONCAVE
                        && !contient(sc_coins_interieurs.coin(sc_coins_interieurs.coin_depart)) )
                ) {
                    // Les ajouter au contour de masse
                    arc_masse.concatene(sc_coins_interieurs.coins_selectionne(true));
                }

            }
        } // Fin boucle sur intersections

        if (typeSurface() == TypeSurface.CONCAVE) {
            // Tracé du rectangle de la zone visible, dans le sens antitrigo : le Path de l'ellipse sera un trou
            // dans cette zone
            contours.ajouterContourMasse(boite.construireContourAntitrigo());
        }
        // Tracé du contour, ou du trou (chemin fermé), dans le sens trigo
        contours.ajouterContourMasse(arc_masse);

        return contours ;

    }

}
