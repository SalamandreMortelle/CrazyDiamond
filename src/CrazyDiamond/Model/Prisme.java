package CrazyDiamond.Model;

import javafx.beans.property.*;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.util.Pair;

public class Prisme extends BaseObstacleAvecContourEtMatiere implements Obstacle, Identifiable, Nommable,ElementAvecContour,ElementAvecMatiere {

    private final ObjectProperty<PositionEtOrientation> position_orientation ;
    protected final DoubleProperty angle_sommet;
    protected final DoubleProperty largeur_base;

    private static int compteur_prisme;

    public Prisme(TypeSurface type_surface, double x_centre, double y_centre, double angle_sommet, double largeur_base, double orientation_deg) throws IllegalArgumentException {
        this(null,type_surface,x_centre,y_centre,angle_sommet,largeur_base,orientation_deg,null,1.5,null,null) ;
    }

    public Prisme(String nom, TypeSurface type_surface, double x_centre, double y_centre, double angle_sommet, double largeur_base, double orientation_deg, NatureMilieu nature_milieu, double indice_refraction, Color couleur_matiere, Color couleur_contour) throws IllegalArgumentException {
        super(nom != null ? nom : "Prisme " + (++compteur_prisme),
                type_surface, nature_milieu, indice_refraction, couleur_matiere, couleur_contour);

        if (angle_sommet <=0d || largeur_base <=0d)
            throw new IllegalArgumentException("Un prisme doit avoir un angle au sommet et une largeur de base strictement positifs.") ;

        this.position_orientation = new SimpleObjectProperty<>(new PositionEtOrientation(new Point2D(x_centre,y_centre),orientation_deg)) ;

        this.angle_sommet = new SimpleDoubleProperty(angle_sommet);
        this.largeur_base = new SimpleDoubleProperty(largeur_base);
    }

    public Prisme(Imp_Identifiable ii,Imp_Nommable in,Imp_ElementAvecContour iec, Imp_ElementAvecMatiere iem, double x_centre, double y_centre, double angle_sommet, double largeur_base, double orientation_deg) throws IllegalArgumentException {
        super(ii,in,iec,iem) ;

        if (angle_sommet <=0d || largeur_base <=0d)
            throw new IllegalArgumentException("Un prisme doit avoir un angle au sommet et une largeur de base strictement positifs.") ;

        this.position_orientation = new SimpleObjectProperty<>(new PositionEtOrientation(new Point2D(x_centre,y_centre),orientation_deg)) ;

        this.angle_sommet = new SimpleDoubleProperty(angle_sommet);
        this.largeur_base = new SimpleDoubleProperty(largeur_base);

    }

    @Override
    public Double courbureRencontreeAuSommet(Point2D pt_sur_surface, Point2D direction) {
        return null ;
    }

    public void definirCentre(Point2D centre) {position_orientation.set(new PositionEtOrientation(centre,orientation()));}

    public void definirAngleSommet(double ang_s) { this.angle_sommet.set(ang_s); }
    public void definirLargeurBase(double larg_b) { this.largeur_base.set(larg_b); }
    public void definirOrientation(double or) {  position_orientation.set(new PositionEtOrientation(centre(),or)); }

    public double xCentre() { return centre().getX(); }
    public double yCentre() { return centre().getY(); }
    public DoubleProperty angleSommetProperty() { return angle_sommet; }
    public double angleSommet() { return angle_sommet.get(); }
    public DoubleProperty largeurBaseProperty() { return largeur_base; }
    public double largeurBase() { return largeur_base.get(); }

    public double orientation() { return position_orientation.get().orientation_deg() ;}

    public void translater(Point2D vecteur) {
        position_orientation.set(new PositionEtOrientation(centre().add(vecteur),orientation()));
    }
    @Override
    public void translaterParCommande(Point2D vecteur) {
        new CommandeDefinirUnParametrePoint<>(this,centre().add(vecteur),this::centre,this::definirCentre).executer() ;
    }

    public void accepte(VisiteurEnvironnement v) {
        v.visitePrisme(this);
    }

    @Override
    public void accepte(VisiteurElementAvecMatiere v) {
        v.visitePrisme(this);
    }

    public ContoursObstacle couper(BoiteLimiteGeometrique boite) {
        return couper(boite,true) ;
    }

    DemiDroiteOuSegment cote(BordPrisme b) {
        if (b == BordPrisme.GAUCHE) return DemiDroiteOuSegment.construireSegment(sommet(Sommet.H),sommet(Sommet.BG)) ;
        if (b == BordPrisme.BAS) return  DemiDroiteOuSegment.construireSegment(sommet(Sommet.BG),sommet(Sommet.BD)) ;
        return DemiDroiteOuSegment.construireSegment(sommet(Sommet.BD),sommet(Sommet.H)) ;
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
//        position_orientation.set(new PositionEtOrientation(nouveau_centre,Obstacle.nouvelleOrientationApresRotation(orientation(),angle_rot_deg)));
//        position_orientation.set(new PositionEtOrientation(nouveau_centre,orientation()+angle_rot_deg));

        position_orientation.set(Environnement.nouvellePositionEtOrientationApresRotation(position_orientation.get(),centre_rot,angle_rot_deg)) ;
    }

    ContoursObstacle couper(BoiteLimiteGeometrique boite, boolean avec_contours_surface) {

        ContoursObstacle contours = new ContoursObstacle() ;

        Contour c_masse = new Contour() ;
        Contour c_surface = null ;

        BordRectangle bord_init = null ;
        BordRectangle bord_prec = null ;
        Point2D intersection;

        boolean trace_surface = false ;

        BordPrisme cote_courant = BordPrisme.GAUCHE ;
        DemiDroiteOuSegment seg_cote_courant = cote(cote_courant) ;

        if (boite.contains(sommet(Sommet.H))) {
            c_masse.ajoutePoint(sommet(Sommet.H));
            c_surface = new Contour() ;
            c_surface.ajoutePoint(sommet(Sommet.H));
            trace_surface = true ;
        }

        do { // Boucle sur les côtés du Prisme this

            Pair<BordRectangle,Point2D>[] intersections_avec_bords  = new Pair[2];

//            // Recherche des intersections (de 0 à 2) de seg_cote_courant avec boite limite
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
            if (intersections_avec_bords[1]!=null && intersections_avec_bords[0].getValue().subtract(seg_cote_courant.depart()).magnitude()>intersections_avec_bords[1].getValue().subtract(seg_cote_courant.depart()).magnitude()) {
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
            if (boite.contains(sommet(cote_courant.sommet_suivant()))) {

                // L'ajouter au contour de masse, et au contour de surface
                c_masse.ajoutePoint(sommet(cote_courant.sommet_suivant()));
                c_surface.ajoutePoint(sommet(cote_courant.sommet_suivant()));
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
        } while (cote_courant != BordPrisme.GAUCHE) ;

        if (bord_prec!=null)
            boite.completerContourAvecCoinsConsecutifsEntreBordsContenusDansObstacle(bord_prec,bord_init,c_masse);

        // Aucune partie du contour du rectangle n'est visible, et le centre de la zone visible est dans le Prisme
        if (c_masse.nombrePoints() == 0 && this.contient(boite.centre())) {

            contours.ajouterContourMasse(boite.construireContour());

            return contours ;

        }

        // A partir d'ici, on sait que c_masse n'est pas vide : une partie du contour du Prisme est visible

        // Si le Prisme est CONCAVE, c_masse est un "trou" dans la zone visible
        if (typeSurface() == TypeSurface.CONCAVE && c_masse.nombrePoints()>0)
            contours.ajouterContourMasse(boite.construireContourAntitrigo());

        contours.ajouterContourMasse(c_masse);

        if (c_surface!=null)
            contours.ajouterContourSurface(c_surface);

        return contours ;

    }

    @Override
    public void ajouterRappelSurChangementToutePropriete(RappelSurChangement rap) {
        super.ajouterRappelSurChangementToutePropriete(rap);

        position_orientation.addListener((observable, oldValue, newValue) -> rap.rappel());

        angle_sommet.addListener((observable, oldValue, newValue) -> rap.rappel());
        largeur_base.addListener((observable, oldValue, newValue) -> rap.rappel());
    }

    @Override
    public void     ajouterRappelSurChangementTouteProprieteModifiantChemin(RappelSurChangement rap) {
        super.ajouterRappelSurChangementTouteProprieteModifiantChemin(rap);

        position_orientation.addListener((observable, oldValue, newValue) -> rap.rappel());

        angle_sommet.addListener((observable, oldValue, newValue) -> rap.rappel());
        largeur_base.addListener((observable, oldValue, newValue) -> rap.rappel());
    }

    public void retaillerPourSourisEn(Point2D pos_souris) {
        // Si on est sur le point de départ, ne rien faire
        if (pos_souris.equals(centre()))
            return ;

        // Calcul de la nouvelle largeur base (angle au sommet est supposé connu et ne change pas)
        largeur_base.set(Math.abs(2d*(pos_souris.getX()- xCentre())));

        angle_sommet.set(2*Math.toDegrees(Math.abs(Math.atan((pos_souris.getX()- xCentre())/(3*(yCentre() - pos_souris.getY()))))));
    }


    @Override
    public void retaillerSelectionPourSourisEn(Point2D pos_souris) {
        // Si on est sur le point de départ, ne rien faire
        if (pos_souris.equals(centre()))
            return ;

        // Calculer l'écart angulaire entre le sommet H où se trouve la poignée et la position de la souris, par rapport
        // au centre du Prisme
        Point2D vec_centre_bd = sommet(Sommet.BD).subtract(centre()) ;

        Point2D vec_centre_pos = pos_souris.subtract(centre()) ;

        double delta_orientation= vec_centre_pos.angle(vec_centre_bd) ;

        if (produit_vectoriel_simplifie(vec_centre_pos,vec_centre_bd)>0d)
            delta_orientation = -delta_orientation ;

        double nouvelle_orientation = (orientation() + delta_orientation)%360d ;

        if (nouvelle_orientation<0)
            nouvelle_orientation += 360d ;

        definirOrientation(nouvelle_orientation);
    }


    @Override
    public Contour positions_poignees() {
        Contour c_poignees = new Contour(1);

        c_poignees.ajoutePoint(sommet(Sommet.BD));

        return c_poignees;
    }

    private Point2D vecteur_directeur() {
        double theta = Math.toRadians(orientation()) ;

        double cos_theta = Math.cos(theta) ;
        double sin_theta = Math.sin(theta) ;

        return new Point2D(cos_theta,sin_theta) ;
    }
    public Point2D centre() { return position_orientation.get().position() ; }

    public Point2D sommet(Sommet s) {
        double theta = Math.toRadians(orientation()) ;
        double cos_theta = Math.cos(theta) ;
        double sin_theta = Math.sin(theta) ;

        double hauteur = largeur_base.get()/(2*Math.tan(Math.toRadians(angle_sommet.get())/2d)) ;

        if (s==Sommet.H) {
            double x_avant_rot = 0d ;
            double y_avant_rot =  2*hauteur/3d;
            return new Point2D(xCentre() + x_avant_rot*cos_theta-y_avant_rot*sin_theta,yCentre() + x_avant_rot*sin_theta+y_avant_rot*cos_theta) ;
        }

        if (s==Sommet.BG) {
            double x_avant_rot = - largeur_base.get()/2;
            double y_avant_rot =  - hauteur/3d;
            return new Point2D(xCentre() +x_avant_rot*cos_theta-y_avant_rot*sin_theta,yCentre() + x_avant_rot*sin_theta+y_avant_rot*cos_theta) ;
        }

        // Sommet BD
        double x_avant_rot =  largeur_base.get()/2;
        double y_avant_rot =  - hauteur/3d;
        return new Point2D(xCentre() +x_avant_rot*cos_theta-y_avant_rot*sin_theta,yCentre() + x_avant_rot*sin_theta+y_avant_rot*cos_theta) ;

    }

    private Point2D[] sommets() {
        double theta = Math.toRadians(orientation()) ;
        double cos_theta = Math.cos(theta) ;
        double sin_theta = Math.sin(theta) ;

        Point2D[] coins = new Point2D[3] ;

        // On part du sommet Haut, abstraction faite de l'orientation du prisme, et on tourne dans le sens trigo

        double hauteur = largeur_base.get()/(2*Math.tan(Math.toRadians(angle_sommet.get())/2d)) ;

        double x_avant_rot = 0d;
        double y_avant_rot = 2*hauteur/3;

        coins[0] = new Point2D(xCentre()+x_avant_rot*cos_theta-y_avant_rot*sin_theta,yCentre()+x_avant_rot*sin_theta+y_avant_rot*cos_theta) ;

        x_avant_rot =  - largeur_base.get()/2;
        y_avant_rot =  - hauteur/3;

        coins[1] = new Point2D(xCentre()+x_avant_rot*cos_theta-y_avant_rot*sin_theta,yCentre()+x_avant_rot*sin_theta+y_avant_rot*cos_theta) ;

        x_avant_rot = + largeur_base.get()/2;
        y_avant_rot = - hauteur/3;

        coins[2] =  new Point2D(xCentre()+x_avant_rot*cos_theta-y_avant_rot*sin_theta,yCentre()+x_avant_rot*sin_theta+y_avant_rot*cos_theta) ;

        return coins ;

    }

    @Override
    public  boolean contient(Point2D p) {

        boolean dans_prisme = produit_vectoriel_simplifie(cote(BordPrisme.GAUCHE).direction(), p.subtract(sommet(Sommet.H))) > 0
                && produit_vectoriel_simplifie(cote(BordPrisme.BAS).direction(), p.subtract(sommet(Sommet.BG))) > 0
                && produit_vectoriel_simplifie(cote(BordPrisme.DROIT).direction(), p.subtract(sommet(Sommet.BD))) > 0;

        if (typeSurface()==TypeSurface.CONVEXE)
            return dans_prisme || this.aSurSaSurface(p)  ;
        else
            return (!dans_prisme) || this.aSurSaSurface(p) ;

    }

    @Override
    public  boolean aSurSaSurface(Point2D p) {

//        if (cote(BordPrisme.GAUCHE).contient(p))
//            return true ;
//
//        if (cote(BordPrisme.BAS).contient(p))
//            return true ;
//
//        if (cote(BordPrisme.DROIT).contient(p))
//            return true ;

        Point2D vec_sommet_h_p = p.subtract(sommet(Sommet.H)) ;
        double vec_sommet_h_p_mag = p.subtract(sommet(Sommet.H)).magnitude() ;

        if ( Environnement.quasiEgal(produit_vectoriel_simplifie(cote(BordPrisme.GAUCHE).direction(),vec_sommet_h_p),0d)
          && (0d<=vec_sommet_h_p_mag && vec_sommet_h_p_mag <= cote(BordPrisme.GAUCHE).longueur() ) ) {
            return true;
        }

        Point2D vec_sommet_bg_p = p.subtract(sommet(Sommet.BG)) ;
        double vec_sommet_bg_p_mag = p.subtract(sommet(Sommet.BG)).magnitude() ;

        if ( Environnement.quasiEgal(produit_vectoriel_simplifie(cote(BordPrisme.BAS).direction(),vec_sommet_bg_p),0d)
                && (0d<=vec_sommet_bg_p_mag && vec_sommet_bg_p_mag <= cote(BordPrisme.BAS).longueur() ) ) {
            return true;
        }

        Point2D vec_sommet_bd_p = p.subtract(sommet(Sommet.BD)) ;
        double vec_sommet_bd_p_mag = p.subtract(sommet(Sommet.BD)).magnitude() ;

        return Environnement.quasiEgal(produit_vectoriel_simplifie(cote(BordPrisme.DROIT).direction(), vec_sommet_bd_p), 0d)
                && (0d <= vec_sommet_bg_p_mag && vec_sommet_bd_p_mag <= cote(BordPrisme.DROIT).longueur());
    }

    private double produit_vectoriel_simplifie(Point2D v1, Point2D v2) {
        return (v1.getX()*v2.getY()-v1.getY()*v2.getX()) ;
    }

    @Override
    public Point2D normale(Point2D p) throws Exception {

        Point2D norm = null  ;

        Point2D vec_sommet_h_p = p.subtract(sommet(Sommet.H)) ;
        double vec_sommet_h_p_mag = p.subtract(sommet(Sommet.H)).magnitude() ;

        while (true) {
            if (Environnement.quasiEgal(produit_vectoriel_simplifie(cote(BordPrisme.GAUCHE).direction(), vec_sommet_h_p), 0d)
                    && (0d <= vec_sommet_h_p_mag && vec_sommet_h_p_mag <= cote(BordPrisme.GAUCHE).longueur())) {
                norm = cote(BordPrisme.GAUCHE).normale().multiply(-1d);
                break ;
            }

            Point2D vec_sommet_bg_p = p.subtract(sommet(Sommet.BG));
            double vec_sommet_bg_p_mag = p.subtract(sommet(Sommet.BG)).magnitude();

            if (Environnement.quasiEgal(produit_vectoriel_simplifie(cote(BordPrisme.BAS).direction(), vec_sommet_bg_p), 0d)
                    && (0d <= vec_sommet_bg_p_mag && vec_sommet_bg_p_mag <= cote(BordPrisme.BAS).longueur())) {
                norm = cote(BordPrisme.BAS).normale().multiply(-1d);
                break ;
            }

            Point2D vec_sommet_bd_p = p.subtract(sommet(Sommet.BD));
            double vec_sommet_bd_p_mag = p.subtract(sommet(Sommet.BD)).magnitude();

            if (Environnement.quasiEgal(produit_vectoriel_simplifie(cote(BordPrisme.DROIT).direction(), vec_sommet_bd_p), 0d)
                    && (0d <= vec_sommet_bg_p_mag && vec_sommet_bd_p_mag <= cote(BordPrisme.DROIT).longueur()))
                norm = cote(BordPrisme.DROIT).normale().multiply(-1d);

            break ;

        }

        if (norm==null)
            throw new Exception("Impossible de trouver la normale d'un point qui n'est pas sur la surface du prisme.") ;


        if (typeSurface()==TypeSurface.CONCAVE)
            return norm.multiply(-1.0) ;

        return norm ;
    }

    @Override
    public Point2D cherche_intersection(Rayon r, ModeRecherche mode) {

        return cherche_intersection_avec_demidroite_ou_segment(r.supportGeometrique(),mode) ;

//        System.out.println("Intersection du rayon "+r+" avec rectangle "+this+" : "+inte);
    }

    public Point2D cherche_intersection_avec_demidroite_ou_segment(DemiDroiteOuSegment dd_ou_s, ModeRecherche mode) {

        Point2D[] intersections = cherche_toutes_intersections_avec_demidroite_ou_segment(dd_ou_s) ;

//        int n = intersections.length;
//        System.out.println(n+" intersections avec "+this+" : "+(n>0?intersections[0]:"-")+" , "+(n>1?intersections[1]:"-"));

        if (intersections.length == 0)
            return null ;

        if (intersections.length == 1) {
            if (!Environnement.quasiConfondus(intersections[0],dd_ou_s.depart()))
                return intersections[0];
            else
                return null;
        }

        // Il y a deux intersections

        if (mode==ModeRecherche.PREMIERE && !Environnement.quasiConfondus(intersections[0],dd_ou_s.depart()))
            return intersections[0] ;

        return intersections[1] ;
    }

    /**
     * @param dd_ou_s demi-droite ou segment
     * @return toutes les intersections (0, 1 ou 2) du prisme avec une demi-droite ou un segment, classées de la plus proche du
     * point de départ de la demi-droite ou du segment, à la plus éloignée.
     */
    public Point2D[] cherche_toutes_intersections_avec_demidroite_ou_segment(DemiDroiteOuSegment dd_ou_s) {

        Point2D[] intersections  ;

        Point2D[] sommets = sommets() ;

        Point2D p_inter;
        Point2D p1 = null  ;
        Point2D p2 = null ;

        DemiDroiteOuSegment bord_gauche = DemiDroiteOuSegment.construireSegment(sommets[0],sommets[1]) ;

        p_inter = dd_ou_s.intersectionAvec(bord_gauche) ;
        if (p_inter!=null) p1=p_inter ;

        DemiDroiteOuSegment bord_bas = DemiDroiteOuSegment.construireSegment(sommets[1],sommets[2]) ;
        p_inter = dd_ou_s.intersectionAvec(bord_bas) ;
        if (p_inter!=null) {
            if (p1==null)
                p1 = p_inter ;
            else
                p2 = p_inter ;
        }

        if (p2==null) {
            DemiDroiteOuSegment bord_droit = DemiDroiteOuSegment.construireSegment(sommets[2], sommets[0]);
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

    public ObjectProperty<PositionEtOrientation> positionEtOrientationObjectProperty() {
        return position_orientation ;
    }

    @Override
    public void convertirDistances(double facteur_conversion) {
        position_orientation.set(new PositionEtOrientation(centre().multiply(facteur_conversion),orientation()));
        largeur_base.set(largeurBase()*facteur_conversion);
    }

    public enum BordPrisme {

        GAUCHE(0), BAS(1), DROIT(2) ;

        public final int index;

        BordPrisme(int index) {
            this.index = index;
        }

        public BordPrisme bord_suivant() {

            if (this==GAUCHE) return BAS ;
            if (this==BAS) return DROIT ;
            return GAUCHE ;

        }

        public Sommet sommet_suivant() {

            if (this==GAUCHE) return Sommet.BG ;
            if (this==BAS) return Sommet.BD ;

            return Sommet.H ;

        }

    }

    public enum Sommet {
        H(0),
        BG(1),
        BD(2);

        public final int index;

        Sommet(int index) {
            this.index = index;
        }

        public Sommet sommet_suivant() {

            if (this == H) return Sommet.BG ;
            if (this == BG) return Sommet.BD ;

            return Sommet.H ;
        }

        public BordPrisme bord_suivant() {

            if (this == H) return BordPrisme.GAUCHE;
            if (this == BG) return BordPrisme.BAS ;

            return BordPrisme.DROIT ;
        }

    }

}
