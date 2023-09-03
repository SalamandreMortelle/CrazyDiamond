package CrazyDiamond.Model;

import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

// Classe utilitaire pour tracer la conique graphique
class SelecteurCoins {

    protected double xmin,ymin,xmax,ymax ;

    // Coins sélectionnés dans l'ordre trigonométrique : Haut-Droite, Haut-Gauche, Bas-Gauche, Bas-Droite
    public boolean[] coins_selectionnes = {false, false, false, false} ;
    protected double[] xpoints  ;
    protected double[] ypoints ;

    public Coin coin_depart = Coin.HD ;

    public SelecteurCoins(double x_min,double y_min,double x_max, double y_max) {
        xmin = x_min ;
        ymin = y_min ;
        xmax = x_max ;
        ymax = y_max ;

        xpoints = new double[4] ;
        xpoints[0] = xmax ;
        xpoints[1] = xmin ;
        xpoints[2] = xmin ;
        xpoints[3] = xmax ;

        ypoints = new double[4] ;
        ypoints[0] = ymax ;
        ypoints[1] = ymax ;
        ypoints[2] = ymin ;
        ypoints[3] = ymin ;

    }

    public void selectionne(Coin coin) {
        coins_selectionnes[coin.index] = true ;
    }

    public void deselectionne(Coin coin) {
        coins_selectionnes[coin.index] = false ;
    }

    public void selectionne_tous() {
        for (int i=0 ; i < 4 ; i++)
            coins_selectionnes[i] = true ;
    }

    public boolean est_selectionne(Coin coin) {
        return (coins_selectionnes[coin.index] == true) ;

    }

    public void deselectionne_tous() {
        for (int i=0 ; i < 4 ; i++)
            coins_selectionnes[i] = false ;
    }

    public void inverse_selection() {
        for (int i=0 ; i < 4 ; i++)
            coins_selectionnes[i] = !coins_selectionnes[i] ;
    }

    public Point2D coin(Coin c) {
        return new Point2D(xpoints[c.index],ypoints[c.index]) ;
    }

    // Retourne le coins suivant dans l'ordre trigonométrique
    public Coin coin_suivant(Coin c) {
        if (c== Coin.HD)
            return  Coin.HG ;
        if (c== Coin.HG)
            return  Coin.BG ;
        if (c== Coin.BG)
            return  Coin.BD ;
        if (c== Coin.BD)
            return  Coin.HD ;

        System.err.println("Je ne devrais pas être ici.");

        return null ;
    }
    public Coin coin_precedent(Coin c) {
        if (c== Coin.HD)
            return  Coin.BD ;
        if (c== Coin.BD)
            return  Coin.BG ;
        if (c== Coin.BG)
            return  Coin.HG ;
        if (c== Coin.HG)
            return  Coin.HD ;

        System.err.println("Je ne devrais pas être ici.");

        return null ;
    }


    // Retourne les coins sélectionnés dans l'ordre trigonométrique
    protected ArrayList<Double> xcoins_selectionne(boolean selectionne) {
        ArrayList<Double> xc = new ArrayList<Double>(4 );

        int i_coin ;

        for (int i = 0; i < 4 ; i++) {
            i_coin = (coin_depart.index+i)%4 ;
            if (coins_selectionnes[i_coin]==selectionne)
                xc.add(xpoints[i_coin]) ;
        }

        return xc ;
    }

    protected Contour coins_selectionne(boolean selectionne) {

        Contour c = new Contour(4) ;

        int i_coin ;

        for (int i = 0; i < 4 ; i++) {
            i_coin = (coin_depart.index+i)%4 ;
            if (coins_selectionnes[i_coin]==selectionne)
                c.ajoutePoint(xpoints[i_coin],ypoints[i_coin] );
        }

        return c ;

    }


    protected ArrayList<Double> xcoins_selectionne_antitrigo(boolean selectionne) {
        ArrayList<Double> xc = new ArrayList<Double>(4 );

        int i_coin ;

        for (int i = 0; i < 4 ; i++) {
            i_coin = (4+coin_depart.index-i)%4 ;
            if (coins_selectionnes[i_coin]==selectionne)
                xc.add(xpoints[i_coin]) ;
        }

        return xc ;

    }

    protected Contour coins_selectionne_antitrigo(boolean selectionne) {

        Contour c = new Contour(4) ;

        int i_coin ;

        for (int i = 0; i < 4 ; i++) {
            i_coin = (4+coin_depart.index-i)%4 ;
            if (coins_selectionnes[i_coin]==selectionne)
                c.ajoutePoint(xpoints[i_coin],ypoints[i_coin] );
        }

        return c ;

    }

    protected ArrayList<Double> xcoins_selectionne_antitrigo_depuis(boolean selectionne, Coin coin_dep) {
        ArrayList<Double> xc = new ArrayList<Double>(4 );

        if (coins_selectionnes[coin_dep.index]==selectionne)
            xc.add(xpoints[coin_dep.index]) ;
        if (coins_selectionnes[(4+coin_dep.index-1)%4]==selectionne)
            xc.add(xpoints[(4+coin_dep.index-1)%4]) ;
        if (coins_selectionnes[(4+coin_dep.index-2)%4]==selectionne)
            xc.add(xpoints[(4+coin_dep.index-2)%4]) ;
        if (coins_selectionnes[(4+coin_dep.index-3)%4]==selectionne)
            xc.add(xpoints[(4+coin_dep.index-3)%4]) ;

        return xc ;
    }

    protected ArrayList<Double> ycoins_selectionne(boolean selectionne) {
        ArrayList<Double> yc = new ArrayList<Double>(4 );

        int i_coin ;

        for (int i = 0; i < 4 ; i++) {
            i_coin = (coin_depart.index+i)%4 ;
            if (coins_selectionnes[i_coin]==selectionne)
                yc.add(ypoints[i_coin]) ;
        }

        return yc ;

    }

    protected ArrayList<Double> ycoins_selectionne_antitrigo(boolean selectionne) {
        ArrayList<Double> yc = new ArrayList<Double>(4 );

        int i_coin ;

        for (int i = 0; i < 4 ; i++) {
            i_coin = (4+coin_depart.index-i)%4 ;
            if (coins_selectionnes[i_coin]==selectionne)
                yc.add(ypoints[i_coin]) ;
        }

        return yc ;

    }

    protected ArrayList<Double> ycoins_selectionne_antitrigo_depuis(boolean selectionne, Coin coin_depart) {
        ArrayList<Double> yc = new ArrayList<Double>(4 );

        if (coins_selectionnes[coin_depart.index]==selectionne)
            yc.add(xpoints[coin_depart.index]) ;
        if (coins_selectionnes[(4+coin_depart.index-1)%4]==selectionne)
            yc.add(xpoints[(4+coin_depart.index-1)%4]) ;
        if (coins_selectionnes[(4+coin_depart.index-2)%4]==selectionne)
            yc.add(xpoints[(4+coin_depart.index-2)%4]) ;
        if (coins_selectionnes[(4+coin_depart.index-3)%4]==selectionne)
            yc.add(xpoints[(4+coin_depart.index-3)%4]) ;

        return yc ;
    }

    int ordonneIntersections(double[][] i_droites, double[][] i_hautes, double[][] i_gauches,double[][] i_basses,
                             ArrayList<Double> valeurs_theta_intersection,
                             ArrayList<Double> valeurs_x_intersection,
                             ArrayList<Double> valeurs_y_intersection) {

        int n_intersections = i_hautes.length + i_gauches.length +i_basses.length + i_droites.length ;

        for (double[] i_droite : i_droites) {
            valeurs_theta_intersection.add(i_droite[1]);
            valeurs_x_intersection.add(xmax);
            valeurs_y_intersection.add(i_droite[0]);
        }
        for (double[] i_haute : i_hautes) {
            valeurs_theta_intersection.add(i_haute[1]);
            valeurs_x_intersection.add(i_haute[0]);
            valeurs_y_intersection.add(ymax);
        }
        for (double[] i_gauch : i_gauches) {
            valeurs_theta_intersection.add(i_gauch[1]);
            valeurs_x_intersection.add(xmin);
            valeurs_y_intersection.add(i_gauch[0]);
        }
        for (double[] i_bass : i_basses) {
            valeurs_theta_intersection.add(i_bass[1]);
            valeurs_x_intersection.add(i_bass[0]);
            valeurs_y_intersection.add(ymin);
        }

        if (n_intersections!=valeurs_theta_intersection.size())
            System.err.println("On a un problème");


        return n_intersections ;
    }

    // Retourne la sequence des coins reliés continument à un point de départ pt_deb sur un (autre) bord dans le sens
    // trigo ou antitrigo (en partant de pt_fin jusqu'à pt_deb), jusqu'au bord où se trouve un point final pt_fin
    protected SelecteurCoins sequence_coins_continus(boolean sens_trigo, Point2D pt_deb, Point2D pt_fin, Collection<Double> x_inter, Collection<Double>  y_inter) {

        SelecteurCoins sc = new SelecteurCoins(xmin, ymin, xmax, ymax);

        Coin c_courant = Coin.HD;

        if (sens_trigo) {
            if (pt_deb.getX() == xmin)
                c_courant = Coin.BG;
            if (pt_deb.getX() == xmax)
                c_courant = Coin.HD;
            if (pt_deb.getY() == ymin)
                c_courant = Coin.BD;
            if (pt_deb.getY() == ymax)
                c_courant = Coin.HG;
        } else { // Sens antitrigo
            if (pt_fin.getX() == xmin)
                c_courant = Coin.HG;
            if (pt_fin.getX() == xmax)
                c_courant = Coin.BD;
            if (pt_fin.getY() == ymin)
                c_courant = Coin.BG;
            if (pt_fin.getY() == ymax)
                c_courant = Coin.HD;
        }

        sc.coin_depart = c_courant ;

        Point2D pt_courant = pt_deb ;

        if (!sens_trigo)
            pt_courant = pt_fin ;

        while ( !intersection_bord_entre(pt_courant,sc.coin(c_courant),x_inter,y_inter) ) {
            sc.selectionne(c_courant);
            pt_courant = sc.coin(c_courant) ;

            if (sens_trigo)
                c_courant = sc.coin_suivant(c_courant) ;
            else
                c_courant = sc.coin_precedent(c_courant) ;
        }

        return sc ;

    }


    // Recherche une intersection entre deux points d'un même bord
    private boolean intersection_bord_entre(Point2D pt_deb,Point2D pt_fin, Collection<Double> x_inter, Collection<Double>  y_inter) {

        Iterator<Double> itx = x_inter.iterator() ;
        Iterator<Double> ity = y_inter.iterator() ;

        double x,y ;

        while (itx.hasNext() && ity.hasNext()) {
            x = itx.next();
            y = ity.next() ;
            if (pt_deb.getX()==pt_fin.getX()) { // On cherche sur un bord vertical
                if (x == pt_deb.getX()) {
                    if (pt_deb.getY()< y && y < pt_fin.getY())
                        return true ;
                    if (pt_fin.getY()< y && y < pt_deb.getY())
                        return true ;
                }
            } else if (pt_deb.getY()==pt_fin.getY()) { // On cherche sur un bord horizontal
                if (y == pt_deb.getY()) {
                    if (pt_deb.getX()< x && x < pt_fin.getX())
                        return true ;
                    if (pt_fin.getX()< x && x < pt_deb.getX())
                        return true ;
                }
            } else
                System.err.println("Je ne devrais pas être ici");
        }
        return false;
    }


}
