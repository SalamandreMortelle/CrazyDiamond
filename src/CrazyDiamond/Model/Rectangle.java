package CrazyDiamond.Model;

import javafx.beans.property.*;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class Rectangle extends BaseObstacleAvecContourEtMatiere implements Obstacle, Identifiable, Nommable,ElementAvecContour,ElementAvecMatiere {

    private final ObjectProperty<PositionEtOrientation> position_orientation ;

    protected final DoubleProperty largeur ;
    protected final DoubleProperty hauteur ;

    private static int compteur_rectangle ;

    public Rectangle(String nom, double  x_centre, double y_centre, double largeur, double hauteur, double orientation_deg,TypeSurface type_surface) throws IllegalArgumentException {
        this(nom,type_surface,x_centre,y_centre,largeur,hauteur,orientation_deg,null,1.5,null,null) ;
    }

    public Rectangle(TypeSurface type_surface, double  x_centre, double y_centre, double largeur, double hauteur, double orientation_deg) throws IllegalArgumentException {
        this(null,type_surface,x_centre,y_centre,largeur,hauteur,orientation_deg,null,1.5,null,null) ;
    }

    public Rectangle(String nom, TypeSurface type_surface, double  x_centre, double y_centre, double largeur, double hauteur,
                     double orientation_deg, NatureMilieu nature_milieu, double indice_refraction, Color couleur_matiere, Color couleur_contour) throws IllegalArgumentException {
        super(nom != null ? nom :"Rectangle "+(++compteur_rectangle),
                type_surface, nature_milieu, indice_refraction, couleur_matiere, couleur_contour);

        if (largeur==0d || hauteur==0d)
            throw new IllegalArgumentException("Un rectangle doit avoir une largeur et une hauteur non nulles.") ;

        this.position_orientation = new SimpleObjectProperty<>(new PositionEtOrientation(new Point2D(x_centre,y_centre),orientation_deg)) ;

        this.largeur = new SimpleDoubleProperty(largeur);
        this.hauteur = new SimpleDoubleProperty(hauteur);

        ajouterListeners();
    }

    public Rectangle(Imp_Identifiable ii,Imp_Nommable in,Imp_ElementAvecContour iec, Imp_ElementAvecMatiere iem, double  x_centre, double y_centre, double largeur, double hauteur, double orientation_deg) throws IllegalArgumentException {
        super(ii,in,iec,iem) ;

        if (largeur==0d || hauteur==0d)
            throw new IllegalArgumentException("Un rectangle doit avoir une largeur et une hauteur non nulles.") ;

        this.position_orientation = new SimpleObjectProperty<>(new PositionEtOrientation(new Point2D(x_centre,y_centre),orientation_deg)) ;

        this.largeur = new SimpleDoubleProperty(largeur);
        this.hauteur = new SimpleDoubleProperty(hauteur);

        ajouterListeners();
    }

    private void ajouterListeners() {
        position_orientation.addListener((observable, oldValue, newValue) -> declencherRappelsSurChangementTouteProprieteModifiantChemin());

        largeur.addListener((observable, oldValue, newValue) -> declencherRappelsSurChangementTouteProprieteModifiantChemin());
        hauteur.addListener((observable, oldValue, newValue) -> declencherRappelsSurChangementTouteProprieteModifiantChemin());

        position_orientation.addListener((observable, oldValue, newValue) -> declencherRappelsSurChangementToutePropriete());

        largeur.addListener((observable, oldValue, newValue) -> declencherRappelsSurChangementToutePropriete());
        hauteur.addListener((observable, oldValue, newValue) -> declencherRappelsSurChangementToutePropriete());
    }

    @Override
    public Double courbureRencontreeAuSommet(Point2D pt_sur_surface, Point2D direction) {
        return null ;
    }

    public void definirCentre(Point2D centre) {position_orientation.set(new PositionEtOrientation(centre,orientation()));}

    public void definirLargeur(double larg) {
//        this.largeur.set(larg);
        double larg_init ;
        Point2D pt_ref_init = null;

        if (SOCParent()!=null) {
            larg_init = largeur();

            pt_ref_init = pointDeReferencePourPositionnementDansSOCParent();
        }

        this.largeur.set(Math.abs(larg));

        if (SOCParent()!=null)
            definirCentre(pt_ref_init.add(SOCParent().direction().multiply(0.5*larg)));
//            definirCentre(centre().add(SOCParent().direction().multiply(0.5*(larg-larg_init))));

//        if (SOCParent()!=null) {// A revoir. Attention au cas où une conique à n seul sommet devient une conique à deux sommets, ou l'inverse...
//            Point2D or = position_orientation.get().direction() ;
//            if (SOCParent().direction().dotProduct(or) <= 0)
//                definirCentre(centre().add(or.multiply((larg_init - larg) )));
//            else
//                definirCentre(centre().add(or.multiply(-(larg_init - larg) )));
//        }



    }
    public void definirHauteur(double haut) { this.hauteur.set(haut); }

    public DoubleProperty largeurProperty() { return largeur; }
    public double largeur() { return largeur.get(); }
    public DoubleProperty hauteurProperty() { return hauteur; }
    public double hauteur() { return hauteur.get(); }

    public ObjectProperty<PositionEtOrientation> positionEtOrientationObjectProperty() {
        return position_orientation ;
    }

    public void translater(Point2D vecteur) {
        position_orientation.set(new PositionEtOrientation(centre().add(vecteur),orientation()));
    }
    @Override
    public void translaterParCommande(Point2D vecteur) {
        new CommandeDefinirUnParametrePoint<>(this,centre().add(vecteur),this::centre,this::definirCentre).executer() ;
    }

    public void accepte(VisiteurEnvironnement v) {
        v.visiteRectangle(this);
    }

    @Override
    public void accepte(VisiteurElementAvecMatiere v) {
        v.visiteRectangle(this);
    }

    public ContoursObstacle couper(BoiteLimiteGeometrique boite) {
        return couper(boite,true) ;
    }

    DemiDroiteOuSegment cote(BordRectangle b) {

        if (b==BordRectangle.HAUT) return DemiDroiteOuSegment.construireSegment(coin(Coin.HD),coin(Coin.HG)) ;
        if (b==BordRectangle.GAUCHE) return  DemiDroiteOuSegment.construireSegment(coin(Coin.HG),coin(Coin.BG)) ;
        if (b==BordRectangle.BAS) return DemiDroiteOuSegment.construireSegment(coin(Coin.BG),coin(Coin.BD)) ;

        return DemiDroiteOuSegment.construireSegment(coin(Coin.BD),coin(Coin.HD)) ;

    }
    ContoursObstacle couper(BoiteLimiteGeometrique boite, boolean avec_contours_surface) {

        ContoursObstacle contours = new ContoursObstacle() ;

        Contour c_masse = new Contour() ;
        Contour c_surface = null ;

        BordRectangle bord_init = null ;
        BordRectangle bord_prec = null ;
        Point2D intersection;

        boolean trace_surface = false ;

        // On part du Bord HAUT du Rectangle
        BordRectangle cote_courant = BordRectangle.HAUT ;
        DemiDroiteOuSegment seg_cote_courant = cote(cote_courant) ;

        if (boite.contains(coin(Coin.HD))) { // Le coin HD est visible
            // On l'ajoute au contour de masse et au contour de surface
            c_masse.ajoutePoint(coin(Coin.HD));
            c_surface = new Contour() ;
            c_surface.ajoutePoint(coin(Coin.HD));
            trace_surface = true ;
        }

        do { // Boucle sur les côtés du Rectangle this (cote_courant/seg_cote_courant) parcourus dans le sens trigo

            // On va stocker, pour chaque intersection (deux au plus) entre la zone visible et le cote sec_courant du
            // Rectangle : le Bord (de la zone visible "boite") + le point d'intersection.
            Pair<BordRectangle,Point2D>[] intersections_avec_bords  = new Pair[2];

//            // Recherche des intersections (de 0 à 2) de seg_cote avec boite limite
            for (BordRectangle b : BordRectangle.values()) {
                intersection =  seg_cote_courant.intersectionAvec(boite.bord(b)) ;
                if (intersection != null) {
                    if (intersections_avec_bords[0]==null) {
                        intersections_avec_bords[0] = new Pair<>(b,intersection) ;
                    } else {
                        intersections_avec_bords[1] = new Pair<>(b,intersection)  ;
                    }
                }
            }

            // Il faut que les interserctions soient classées de la plus proche du départ du seg_cote_courant à la plus éloignée
//            if (intersections_avec_bords[1]!=null && intersections_avec_bords[0].getValue().subtract(seg_cote_courant.depart()).magnitude()>intersections_avec_bords[1].getValue().subtract(seg_cote_courant.depart()).magnitude()) {
            if (intersections_avec_bords[1]!=null && intersections_avec_bords[0].getValue().distance(seg_cote_courant.depart())>intersections_avec_bords[1].getValue().distance(seg_cote_courant.depart())) {
                Pair<BordRectangle,Point2D> tampon = new Pair<>(intersections_avec_bords[0].getKey(),intersections_avec_bords[0].getValue()) ;
                intersections_avec_bords[0] = new Pair<>(intersections_avec_bords[1].getKey(),intersections_avec_bords[1].getValue()) ;
                intersections_avec_bords[1] = tampon ;
            }


            // Si seg_cote a une première intersection
            if (intersections_avec_bords[0]!=null) {

                if (bord_init==null)
                    bord_init = intersections_avec_bords[0].getKey() ;

                if (bord_prec!=null)
                    boite.completerContourAvecCoinsConsecutifsEntreBordsContenusDansObstacle(bord_prec,intersections_avec_bords[0].getKey(),c_masse);

                c_masse.ajoutePoint(intersections_avec_bords[0].getValue());

                // Initialiser un nouveau c_surface si nécessaire
                if (trace_surface) {
                    c_surface.ajoutePoint(intersections_avec_bords[0].getValue());
                    trace_surface = false ;
                }
                else { // Début du tracé d'un nouveau morceau de surface
                    if (c_surface!=null)
                        contours.ajouterContourSurface(c_surface);

                    c_surface = new Contour() ;
                    c_surface.ajoutePoint(intersections_avec_bords[0].getValue());
                    trace_surface = true ;
                }


                bord_prec = intersections_avec_bords[0].getKey() ;
            }

            // Si l'extrêmité suivante du côté courant du Rectangle est visible
            if (boite.contains(coin(cote_courant.coin_suivant()))) {

                // L'ajouter au contour de masse, et au contour de surface
                c_masse.ajoutePoint(coin(cote_courant.coin_suivant()));
                c_surface.ajoutePoint(coin(cote_courant.coin_suivant()));
                bord_prec = null ;
            } else {
                if (intersections_avec_bords[1]!=null) { // 2ème intersection => Le contour sort de la zone visible
                    c_masse.ajoutePoint(intersections_avec_bords[1].getValue());
                    c_surface.ajoutePoint(intersections_avec_bords[1].getValue());
                    // Si il y a une 2ème intersection, le tracé de la surface s'interrompt forcément (2ème intersection = sortie de la zone visible)
                    trace_surface = false ;
                    bord_prec = intersections_avec_bords[1].getKey() ;
                }
            }

            cote_courant = cote_courant.bord_suivant() ;
            seg_cote_courant = cote(cote_courant) ;
        } while (cote_courant != BordRectangle.HAUT) ;

        if (bord_prec!=null)
            boite.completerContourAvecCoinsConsecutifsEntreBordsContenusDansObstacle(bord_prec,bord_init,c_masse);

        // Aucune partie du contour du rectangle n'est visible, et le centre de la zone visible est dans le Rectangle
        if (c_masse.nombrePoints() == 0 && this.contient(boite.centre())) {

            contours.ajouterContourMasse(boite.construireContour());

            return contours ;

        }

        // À partir d'ici, on sait que c_masse n'est pas vide : une partie du contour du Rectangle est visible.

        // Si le Rectangle est CONCAVE, c_masse est un "trou" dans la zone visible
        if (typeSurface() == TypeSurface.CONCAVE && c_masse.nombrePoints()>0)
            contours.ajouterContourMasse(boite.construireContourAntitrigo());

        contours.ajouterContourMasse(c_masse);

        if (c_surface!=null)
            contours.ajouterContourSurface(c_surface);

        return contours ;

    }

    public void retaillerPourSourisEn(Point2D pos_souris) {
        // Si on est sur le point de départ, ne rien faire
        if (pos_souris.equals(centre()))
            return ;

//        largeur.set(2d * Math.abs(pos_souris.getX() - xCentre()));
//        hauteur.set(2d * Math.abs(pos_souris.getY() - yCentre()));
        definirLargeur(2d * Math.abs(pos_souris.getX() - xCentre()));
        definirHauteur(2d * Math.abs(pos_souris.getY() - yCentre()));

    }

    @Override
    public void retaillerSelectionPourSourisEn(Point2D pos_souris) {

        if (!appartientASystemeOptiqueCentre()) {
            // Si on est sur le point de départ, ne rien faire
            if (pos_souris.equals(centre()))
                return ;

            // Calculer l'écart angulaire entre le Coin HD où se trouve la poignée et la position de la souris, par rapport
            // au centre du Rectangle
            Point2D vec_centre_hd = coin(Coin.HD).subtract(centre()) ;
            Point2D vec_centre_pos = pos_souris.subtract(centre()) ;
            double delta_orientation= vec_centre_pos.angle(vec_centre_hd) ;

            if (produit_vectoriel_simplifie(vec_centre_pos,vec_centre_hd)>0)
                delta_orientation = -delta_orientation ;

            double nouvelle_orientation = (orientation() + delta_orientation)%360d ;

            if (nouvelle_orientation<0)
                nouvelle_orientation += 360d ;

            definirOrientation(nouvelle_orientation);
        } else {

            // Le rectangle appartient à un SOC : on ne peut donc pas en changer l'orientation (qui doit rester celle du SOC)
            // mais on peut en changer la largeur et la hauteur
//            Point2D vec_centre_pos = pos_souris.subtract(centre()) ;
            Point2D vec_ptref_pos = pos_souris.subtract(pointDeReferencePourPositionnementDansSOCParent()) ;

            double longueur_diag_demi_rect = vec_ptref_pos.magnitude() ;

            if (longueur_diag_demi_rect==0)
                return;

            double angle = Math.atan2(vec_ptref_pos.getY(),vec_ptref_pos.getX()) ;

            if (angle<0)
                angle+= 2*Math.PI ;

            double alpha = angle - Math.toRadians(orientation()) ;

//            definirLargeur(Math.abs(longueur_diag_demi_rect*Math.cos(alpha))); // Va déclencher un repositionnement du centre du rectangle
            double cos_alpha = Math.cos(alpha);
            boolean meme_sens = SOCParent().direction().dotProduct(direction())>0 ;

            if ( (cos_alpha>0 && meme_sens) || (cos_alpha<0 && !meme_sens) )
//            if ( cos_alpha>0 )
                definirLargeur(longueur_diag_demi_rect*Math.abs(cos_alpha)); // Va déclencher un repositionnement du centre du rectangle
            else
                definirLargeur(0);
            definirHauteur(Math.abs(2d*longueur_diag_demi_rect*Math.sin(alpha)));
        }

//
//            Point2D pt_ref = pointDeReferencePourPositionnementDansSOCParent() ;
//
//            // Si on est sur le pt de ref du positionnement dans le SOC parent, ne rien faire
//            if (pos_souris.equals(pt_ref))
//                return ;
//
//            definirLargeur(Math.abs(pos_souris.subtract(pt_ref).dotProduct(SOCParent().direction())));
//
////            Point2D vec_pt_ref_pos_souris = pos_souris.subtract(pt_ref) ;
////
////            double prod_scal = SOCParent().direction().dotProduct(vec_pt_ref_pos_souris) ;
////
////            if (prod_scal>0)
////                definirLargeur(Math.pow(vec_pt_ref_pos_souris.magnitude(),2)/ (2*prod_scal) );
////            else
////                definirLargeur(0);


    }

    @Override
    public Contour positions_poignees() {
        Contour c_poignees = new Contour(1);

        if (SOCParent()==null || SOCParent().direction().dotProduct(direction())>0)
            c_poignees.ajoutePoint(coin(Coin.HD));
        else
            c_poignees.ajoutePoint(coin(Coin.BG));

//        c_poignees.ajoutePoint(x_centre.get()+demi_largeur , y_centre.get()+demi_hauteur);
//        c_poignees.ajoutePoint(x_centre.get()-demi_largeur , y_centre.get()+demi_hauteur);
//        c_poignees.ajoutePoint(x_centre.get()-demi_largeur , y_centre.get()-demi_hauteur);
//        c_poignees.ajoutePoint(x_centre.get()+demi_largeur , y_centre.get()-demi_hauteur);

        return c_poignees;
    }

    private Point2D vecteur_directeur() {
        double theta = Math.toRadians(orientation()) ;

        double cos_theta = Math.cos(theta) ;
        double sin_theta = Math.sin(theta) ;

       return new Point2D(cos_theta,sin_theta) ;
    }
    public Point2D centre() { return position_orientation.get().position() ; }
    public double xCentre() { return centre().getX() ; }
    public double yCentre() { return centre().getY() ; }

    public Point2D coin(Coin c) {
        double theta = Math.toRadians(orientation()) ;
        double cos_theta = Math.cos(theta) ;
        double sin_theta = Math.sin(theta) ;

        if (c==Coin.HD)
            return new Point2D(xCentre()+0.5d*largeur.get()*cos_theta-0.5d*hauteur.get()*sin_theta,
                    yCentre()+0.5d*largeur.get()*sin_theta+0.5d*hauteur.get()*cos_theta) ;

        if (c==Coin.HG)
            return new Point2D(xCentre()-0.5d*largeur.get()*cos_theta-0.5d*hauteur.get()*sin_theta,
                    yCentre()-0.5d*largeur.get()*sin_theta+0.5d*hauteur.get()*cos_theta) ;

        if (c==Coin.BG)
            return new Point2D(xCentre()-0.5d*largeur.get()*cos_theta+0.5d*hauteur.get()*sin_theta,
                    yCentre()-0.5d*largeur.get()*sin_theta-0.5d*hauteur.get()*cos_theta) ;

        return new Point2D(xCentre()+0.5d*largeur.get()*cos_theta+0.5d*hauteur.get()*sin_theta,
                yCentre()+0.5d*largeur.get()*sin_theta-0.5d*hauteur.get()*cos_theta) ;

    }

    private Point2D[] coins() {
        double theta = Math.toRadians(orientation()) ;
        double cos_theta = Math.cos(theta) ;
        double sin_theta = Math.sin(theta) ;

        Point2D[] coins = new Point2D[4] ;

        // On part du coin Haut Droit, abstraction faite de l'orientation du rectangle, et on tourne dans le sens trigo

        coins[0] = new Point2D(xCentre()+0.5d*largeur.get()*cos_theta-0.5d*hauteur.get()*sin_theta,
                yCentre()+0.5d*largeur.get()*sin_theta+0.5d*hauteur.get()*cos_theta) ;
        coins[1] = new Point2D(xCentre()-0.5d*largeur.get()*cos_theta-0.5d*hauteur.get()*sin_theta,
                yCentre()-0.5d*largeur.get()*sin_theta+0.5d*hauteur.get()*cos_theta) ;
        coins[2] = new Point2D(xCentre()-0.5d*largeur.get()*cos_theta+0.5d*hauteur.get()*sin_theta,
                yCentre()-0.5d*largeur.get()*sin_theta-0.5d*hauteur.get()*cos_theta) ;
        coins[3] = new Point2D(xCentre()+0.5d*largeur.get()*cos_theta+0.5d*hauteur.get()*sin_theta,
                yCentre()+0.5d*largeur.get()*sin_theta-0.5d*hauteur.get()*cos_theta) ;

        return coins ;

    }

    @Override
    public  boolean contient(Point2D p) {

        boolean dans_rect = false ;

        Point2D p_dans_ref_oriente = point_dans_ref_oriente(p) ;

        double x_or = p_dans_ref_oriente.getX() ;
        double y_or = p_dans_ref_oriente.getY() ;

        double larg = largeur.get() ;
        double haut = hauteur.get() ;

        if (-larg/2d <= x_or && x_or <= larg/2d && -haut/2d <= y_or && y_or <= haut/2d)
            dans_rect = true ;

        if (typeSurface()==TypeSurface.CONVEXE)
            return dans_rect || this.aSurSaSurface(p)  ;
        else
            return (!dans_rect) || this.aSurSaSurface(p) ;

    }

    @Override
    public  boolean aSurSaSurface(Point2D p) {

        Point2D p_dans_ref_oriente = point_dans_ref_oriente(p) ;

        double x_or = p_dans_ref_oriente.getX() ;
        double y_or = p_dans_ref_oriente.getY() ;

        double larg = largeur.get() ;
        double haut = hauteur.get() ;

        if ( ( Environnement.quasiEgal(-larg/2d,x_or)||Environnement.quasiEgal(larg/2d,x_or) )
            && (-haut/2d<=y_or && y_or<= haut/2d) )
                return true ;

        return (Environnement.quasiEgal(-haut / 2d, y_or) || Environnement.quasiEgal(haut / 2d, y_or))
                && (-larg / 2d <= x_or && x_or <= larg / 2d);
    }

    private Point2D point_dans_ref_oriente(Point2D p) {
        Point2D vec_centre_p = p.subtract(centre()) ;

        double angle_avec_vec_dir = Math.atan2(vec_centre_p.getY(),vec_centre_p.getX()) - Math.atan2(vecteur_directeur().getY(),vecteur_directeur().getX()) ;

        double dist_centre_p = vec_centre_p.magnitude() ;

        double x_proj_sur_dir = dist_centre_p*Math.cos(angle_avec_vec_dir) ;
        double y_proj_sur_dir = dist_centre_p*Math.sin(angle_avec_vec_dir) ;

        return new Point2D(x_proj_sur_dir,y_proj_sur_dir) ;
    }

    private double produit_vectoriel_simplifie(Point2D v1, Point2D v2) {
        return (v1.getX()*v2.getY()-v1.getY()*v2.getX()) ;
    }

    @Override
    public Point2D normale(Point2D p) throws Exception {

        if (!this.aSurSaSurface(p))
            throw new Exception("Impossible de trouver la normale d'un point qui n'est pas sur la surface du rectangle.") ;

        Point2D p_dans_ref_oriente = point_dans_ref_oriente(p) ;

        double x_or = p_dans_ref_oriente.getX() ;
        double y_or = p_dans_ref_oriente.getY() ;

        double larg = largeur.get() ;
        double haut = hauteur.get() ;

        Point2D norm = null ;

        double cos_theta = Math.cos(Math.toRadians(orientation())) ;
        double sin_theta = Math.sin(Math.toRadians(orientation())) ;

        if (Environnement.quasiEgal(-larg/2d,x_or))
            norm = new Point2D(-cos_theta,-sin_theta) ;
        else if (Environnement.quasiEgal(larg/2d,x_or))
            norm = new Point2D(cos_theta,sin_theta) ;
        else if (Environnement.quasiEgal(-haut/2d , y_or))
            norm = new Point2D(sin_theta,-cos_theta) ;
        else if (Environnement.quasiEgal(haut/2d , y_or))
            norm = new Point2D(-sin_theta,cos_theta) ;

        if (norm == null)
            throw new Exception("Erreur lors du calcul de la normale.") ;

        if (typeSurface()==TypeSurface.CONCAVE)
            return norm.multiply(-1.0) ;

        return norm ;
    }

    @Override
    public Point2D cherche_intersection(Rayon r, ModeRecherche mode) {

        return cherche_intersection_avec_demidroite_ou_segment(r.supportGeometrique(),mode) ;

    }

    public Point2D cherche_intersection_avec_demidroite_ou_segment(DemiDroiteOuSegment dd_ou_s, ModeRecherche mode) {

        Point2D[] intersections = cherche_toutes_intersections_avec_demidroite_ou_segment(dd_ou_s) ;

        if (intersections.length == 0)
            return null ;

        if (intersections.length == 1) {
            if (Environnement.nonQuasiConfondus(intersections[0], dd_ou_s.depart()))
                return intersections[0];
            else
                return null;
        }

        // Il y a deux intersections

        if (mode==ModeRecherche.PREMIERE && Environnement.nonQuasiConfondus(intersections[0], dd_ou_s.depart()))
            return intersections[0] ;

        return intersections[1] ;
    }

    /**
     * @param dd_ou_s
     * @return toutes les intersections (0, 1 ou 2) du rectangle avec une demi-droite ou un segment, classées de la plus proche du
     * point de départ de la demi-droite ou du segment, à la plus éloignée.
     */
    public Point2D[] cherche_toutes_intersections_avec_demidroite_ou_segment(DemiDroiteOuSegment dd_ou_s) {

        Point2D[] intersections  ;

        Point2D[] coins = coins() ;

        Point2D p_inter = null ;
        Point2D p1 = null  ;
        Point2D p2 = null ;

        DemiDroiteOuSegment bord_haut = DemiDroiteOuSegment.construireSegment(coins[0],coins[1]) ;

        p_inter = dd_ou_s.intersectionAvec(bord_haut) ;
        if (p_inter!=null) p1=p_inter ;

        DemiDroiteOuSegment bord_gauche = DemiDroiteOuSegment.construireSegment(coins[1],coins[2]) ;
        p_inter = dd_ou_s.intersectionAvec(bord_gauche) ;
        if (p_inter!=null) {
            if (p1==null)
                p1 = p_inter ;
            else
                p2 = p_inter ;
        }

        if (p2==null) {
            DemiDroiteOuSegment bord_bas = DemiDroiteOuSegment.construireSegment(coins[2], coins[3]);
            p_inter = dd_ou_s.intersectionAvec(bord_bas);
            if (p_inter != null) {
                if (p1 == null)
                    p1 = p_inter ;
                else
                    p2 = p_inter;
            }
        }

        if (p2==null) {
            DemiDroiteOuSegment bord_droit = DemiDroiteOuSegment.construireSegment(coins[3], coins[0]);
            p_inter = dd_ou_s.intersectionAvec(bord_droit);
            if (p_inter != null) {
                if (p1 == null)
                    p1 = p_inter ;
                else
                    p2 = p_inter;
            }
        }

        if (p1==null && p2==null) {
            return new Point2D[0] ;
        }

        if (p2==null) {

            intersections = new Point2D[1] ;

            intersections[0]=p1 ;

            return intersections;
        }

        // Il y a deux intersections
        intersections = new Point2D[2] ;

        if (p2.subtract(dd_ou_s.depart()).magnitude()>p1.subtract(dd_ou_s.depart()).magnitude()) {
            intersections[0] = p1 ;
            intersections[1] = p2 ;
        } else {
            intersections[0] = p2 ;
            intersections[1] = p1 ;
        }

        return intersections ;

    }

    public boolean aSymetrieDeRevolution() {return true ;}

    @Override
    public Point2D pointSurAxeRevolution() {
        return centre();
    }

    @Override
    public boolean estOrientable() {
        return true ;
    }

    @Override
    public void tournerAutourDe(Point2D centre_rot, double angle_rot_deg) {
//        Rotate r = new Rotate(angle_rot_deg,centre_rot.getX(),centre_rot.getY()) ;
//
//        Point2D nouveau_centre = r.transform(centre()) ;

        // Il faut ramener la nouvelle orientation entre 0 et 360° car les spinners et sliders "orientation" des
        // panneaux contrôleurs imposent ces limites via leurs min/max
//        double nouvelle_or = (orientation()+angle_rot_deg)%360 ;
//        if (nouvelle_or<0) nouvelle_or+=360 ;
//
//        position_orientation.set(new PositionEtOrientation(nouveau_centre,Obstacle.nouvelleOrientationApresRotation(orientation(),angle_rot_deg)/*orientation()+angle_rot_deg*/));
        position_orientation.set(Environnement.nouvellePositionEtOrientationApresRotation(position_orientation.get(),centre_rot,angle_rot_deg)) ;
    }

    @Override
    public void definirOrientation(double orientation_deg)  {
        position_orientation.set(new PositionEtOrientation(centre(),orientation_deg));
    }

    @Override
    public boolean aUneOrientation() {
        return true;
    }

    @Override
    public double orientation()  {
        return position_orientation.get().orientation_deg() ;
    }

    @Override
    public Double rayonDiaphragmeParDefaut() {
        return hauteur.get()*0.5d;
    }

    @Override
    public double rayonDiaphragmeMaximumConseille() {
        return hauteur.get()*0.5d;
    }

    @Override
    public List<DioptreParaxial> dioptresParaxiaux(PositionEtOrientation axe) {

        if (Environnement.quasiEgal(largeur.get(),0d)) // Pas de dioptres si la largeur est quasi nulle
            return new ArrayList<>(0) ;

        ArrayList<DioptreParaxial> resultat = new ArrayList<>(2) ;

        double demi_largeur = largeur.get()*0.5d ;

        double z_centre = centre().subtract(axe.position()).dotProduct(axe.direction()) ;

        double z_int_min = z_centre - demi_largeur ;
        double z_int_max = z_centre + demi_largeur ;

        DioptreParaxial d_z_min ;
        DioptreParaxial d_z_max ;

        if (typeSurface()==TypeSurface.CONVEXE) {
            d_z_min = new DioptreParaxial(z_int_min, null, 0d , indiceRefraction(), this);
            d_z_max = new DioptreParaxial(z_int_max, null, indiceRefraction(), 0d, this);
        } else {
            d_z_min = new DioptreParaxial(z_int_min, null, indiceRefraction(),0d, this);
            d_z_max = new DioptreParaxial(z_int_max, null, 0d,indiceRefraction(), this);
        }

        resultat.add(d_z_min) ;
        resultat.add(d_z_max) ;

        return resultat ;

    }

    @Override
    public void convertirDistances(double facteur_conversion) {
        largeur.set(largeur()*facteur_conversion);
        hauteur.set(hauteur()*facteur_conversion);
        position_orientation.set(new PositionEtOrientation(centre().multiply(facteur_conversion),orientation()));
    }

    Point2D direction() {
        return position_orientation.get().direction() ;
    }

    @Override
    public Point2D pointDeReferencePourPositionnementDansSOCParent() {
//        return centre() ;

//        if (SOCParent().direction().dotProduct(direction())>=0)
            return centre().subtract(SOCParent().direction().multiply(0.5*largeur())) ;
//        else
//            return centre().add(SOCParent().direction().multiply(0.5*largeur())) ;
    }

    @Override
    public void definirPointDeReferencePourPositionnementDansSOCParent(Point2D pt_ref) {
//        definirCentre(pt_ref);
//        if (SOCParent().direction().dotProduct(position_orientation.get().direction())>=0)
            definirCentre(pt_ref.add(SOCParent().direction().multiply(0.5*largeur())));
//        else
//            definirCentre(pt_ref.subtract(SOCParent().direction().multiply(0.5*largeur())));
    }

    @Override
    public ObjectProperty<PositionEtOrientation> positionEtOrientationProperty() { return position_orientation ; }

}
