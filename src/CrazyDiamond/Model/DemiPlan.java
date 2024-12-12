package CrazyDiamond.Model;

import javafx.beans.property.*;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class DemiPlan extends BaseObstacleAvecContourEtMatiere implements Obstacle, Identifiable, Nommable, ElementAvecContour, ElementAvecMatiere {

    private final ObjectProperty<PositionEtOrientation> position_orientation ;

    private static int compteur_demi_plan = 0 ;

    public DemiPlan(String nom, double x_origine, double y_origine, double orientation_deg,TypeSurface type_surface) throws IllegalArgumentException {
        this(nom,type_surface,x_origine,y_origine,orientation_deg,null,1.5,null,null) ;
    }
    public DemiPlan(TypeSurface type_surface, double x_origine, double y_origine, double orientation_deg) throws IllegalArgumentException {
        this(null,type_surface,x_origine,y_origine,orientation_deg,null,1.5,null,null) ;
    }

    public DemiPlan(String nom, TypeSurface type_surface, double x_origine, double y_origine, double orientation_deg,
                    NatureMilieu nature_milieu, double indice_refraction, Color couleur_matiere, Color couleur_contour) throws IllegalArgumentException {
        super(nom != null ? nom : "Demi-plan " + (++compteur_demi_plan),
                type_surface, nature_milieu, indice_refraction, couleur_matiere, couleur_contour);

        this.position_orientation = new SimpleObjectProperty<>(new PositionEtOrientation(new Point2D(x_origine,y_origine),orientation_deg)) ;
    }

    public DemiPlan(Imp_Identifiable ii,Imp_Nommable in,Imp_ElementAvecContour iec, Imp_ElementAvecMatiere iem , double x_origine, double y_origine, double orientation_deg) throws IllegalArgumentException {
        super(ii,in,iec,iem) ;

        this.position_orientation = new SimpleObjectProperty<>(new PositionEtOrientation(new Point2D(x_origine,y_origine),orientation_deg)) ;
    }

    @Override
    public Double courbureRencontreeAuSommet(Point2D pt_sur_surface, Point2D direction) {
        return null ;
    }

    public ObjectProperty<PositionEtOrientation> positionEtOrientationObjectProperty() { return position_orientation ;}

    @Override
    public List<DioptreParaxial> dioptresParaxiaux(PositionEtOrientation axe) {

        ArrayList<DioptreParaxial> resultat = new ArrayList<>(1) ;

        double z_origine = origine().subtract(axe.position()).dotProduct(axe.direction()) ;

        DioptreParaxial d_z_origine ;

        if (typeSurface()==TypeSurface.CONVEXE)
            d_z_origine = new DioptreParaxial(z_origine, null,  indiceRefraction(), 0d, this);
        else
            d_z_origine = new DioptreParaxial(z_origine, null, 0d, indiceRefraction() , this);

//        if (Math.abs(axe.orientation_deg() - orientation())>90d) // Cet écart vaut 0 ou 180°
//            d_z_origine.permuterIndicesAvantApres();
        if (normale().dotProduct(axe.direction())<0)
            d_z_origine.permuterIndicesAvantApres();

        resultat.add(d_z_origine) ;

        return resultat ;

    }

    public Point2D origine() {return position_orientation.get().position(); }
    public double xOrigine() { return position_orientation.get().position().getX(); }
    public double yOrigine() { return position_orientation.get().position().getY();  }

    // Direction de la frontière du demi-plan
    public Point2D directionPlan() {
        Point2D p_norm = normale() ;
        return new Point2D(-p_norm.getY(),p_norm.getX()) ;
    }

    @Override
    public void translater(Point2D vecteur) {
        position_orientation.set(new PositionEtOrientation(origine().add(vecteur),orientation()));
    }
    @Override
    public void translaterParCommande(Point2D vecteur) {
        new CommandeDefinirUnParametrePoint<>(this,origine().add(vecteur),this::origine,this::definirOrigine).executer() ;
    }
    @Override
    public boolean contient(Point2D p) {

        Point2D p_orig = origine() ;

        double ori_rad = Math.toRadians(orientation()) ;

        Point2D p_norm = new Point2D(Math.cos(ori_rad),Math.sin(ori_rad)) ;

//        boolean dans_demi_plan = false ;

        if (typeSurface() == TypeSurface.CONVEXE)
            return Environnement.quasiInferieurOuEgal(p.subtract(p_orig).dotProduct(p_norm),0.0) ;
//            dans_demi_plan = ( p.subtract(p_orig).dotProduct(p_norm) <= 0 ) ;
        else
            return Environnement.quasiSuperieurOuEgal(p.subtract(p_orig).dotProduct(p_norm),0.0) ;
//            dans_demi_plan = ( p.subtract(p_orig).dotProduct(p_norm) >= 0 ) ;

//        return dans_demi_plan || this.aSurSaSurface(p) ;

    }

    @Override
    public boolean aSurSaSurface(Point2D p) {
        Point2D p_orig = origine() ;

        double ori_rad = Math.toRadians(orientation()) ;

        Point2D p_norm = new Point2D(Math.cos(ori_rad),Math.sin(ori_rad)) ;

        return Environnement.quasiEgal( p.subtract(p_orig).dotProduct(p_norm) , 0.0 ) ;
    }

    @Override
    public Point2D normale(Point2D p) throws Exception {

        // Pour un demi plan, la normale est la même en tout point
        return normale() ;

    }

    public Point2D normale() {
//        double ori_rad = Math.toRadians(orientation()) ;
//        Point2D norm = new Point2D(Math.cos(ori_rad),Math.sin(ori_rad)) ;

        Point2D norm = position_orientation.get().direction() ;
        return norm.multiply(typeSurface() == TypeSurface.CONVEXE?1d:-1d) ;

//        if (typeSurface() == TypeSurface.CONVEXE)
//            return norm ;
//        else
//            return norm.multiply(-1.0) ;

    }

    @Override
    public ArrayList<Point2D> cherche_toutes_intersections(Rayon r) {

        ArrayList<Point2D> resultats = new ArrayList<>(1) ;

        Point2D pt_int = cherche_intersection(r,ModeRecherche.PREMIERE) ;

        if (pt_int != null)
            resultats.add(pt_int) ;

        return resultats ;
    }

    @Override
    public Point2D cherche_intersection(Rayon r, ModeRecherche mode) {

        if (aSurSaSurface(r.depart()))
            return null ;

        double ori_rad = Math.toRadians(orientation()) ;

        Point2D p_norm = new Point2D(Math.cos(ori_rad),Math.sin(ori_rad)) ;

        // Le rayon et la ligne sont-ils parallèles ?
        if  (Environnement.quasiEgal(p_norm.dotProduct(r.direction()),0.0))
                return null ;

        // Recherche du parametre t du rayon ( depart + t * direction ) de l'intersection
        double t = (origine().subtract(r.depart()).dotProduct(p_norm)) / (r.direction().dotProduct(p_norm)) ;

        Point2D pint = r.depart().add(r.direction().multiply(t)) ;

        if (r.direction().getX()>0 && pint.getX()< r.depart().getX()) { return null; }

        if (r.direction().getX()<0 && pint.getX()> r.depart().getX()){ return null; }

        if (r.direction().getY()>0 && pint.getY()< r.depart().getY()){ return null; }

        if (r.direction().getY()<0 && pint.getY()> r.depart().getY()){ return null; }

        LOGGER.log(Level.FINEST,"Intersection trouvée : {0}",pint) ;

        return pint ;

    }

    @Override
    public void accepte(VisiteurEnvironnement v) {
        v.visiteDemiPlan(this );
    }

    @Override
    public void accepte(VisiteurElementAvecMatiere v) {
        v.visiteDemiPlan(this);
    }

    public ContoursObstacle couper(BoiteLimiteGeometrique boite) {
        return couper(boite,true) ;
    }

    ContoursObstacle couper(BoiteLimiteGeometrique boite, boolean avec_contours_surface) {

        ContoursObstacle contours = new ContoursObstacle() ;

        double xmin = boite.getMinX() ;
        double xmax = boite.getMaxX() ;
        double ymin = boite.getMinY() ;
        double ymax = boite.getMaxY() ;

//        Rayon r = new Rayon(origine(),direction()) ;
        DemiDroiteOuSegment s = new DemiDroiteOuSegment(origine(), directionPlan()) ;

        Point2D p_inter1 = boite.premiere_intersection(s) ;
        Point2D p_inter2 = boite.derniere_intersection(s) ;

//        if (p_inter1 != null && p_inter2!=null && Environnement.quasiConfondus(p_inter1,p_inter2))
        if (p_inter1 != null && p_inter1.equals(p_inter2))
            p_inter2 = null ;

//        Rayon r_opp = new Rayon(origine(),direction().multiply(-1.0)) ;
        DemiDroiteOuSegment s_opp = new DemiDroiteOuSegment(origine(), directionPlan().multiply(-1.0)) ;
        Point2D p_inter_opp1 = boite.premiere_intersection(s_opp) ;
        Point2D p_inter_opp2 = boite.derniere_intersection(s_opp) ;

//        if (p_inter_opp1 != null && p_inter_opp2 !=null &&  Environnement.quasiConfondus(p_inter_opp1,p_inter_opp2))
        if (p_inter_opp1 != null && p_inter_opp1.equals(p_inter_opp2))
            p_inter_opp2 = null ;

        ArrayList<Point2D> its = new ArrayList<>(2) ;

        // Inutile et dangereux
//        if (boite.aSurSaSurface(origine()))
//            its.add(origine()) ;

        if (p_inter1!=null)
            its.add(p_inter1) ;
        if (p_inter2!=null)
            its.add(p_inter2) ;
        if (p_inter_opp1!=null)
            its.add(p_inter_opp1) ;
        if (p_inter_opp2!=null)
            its.add(p_inter_opp2) ;


        if(its.size()>2) {
            throw new IllegalStateException("Une ligne ne peut pas avoir plus de 2 points d'intersection avec la boite limite de l'environnement.") ;
        }


        // Pas d'intersection ou 1 seule intersection (avec un coin)
        if (its.size()<2) {
            if (contient(boite.centre()))
                contours.ajouterContourMasse(boite.construireContour());
            return contours ;
        }

        Contour contour_masse = new Contour(4) ;

        // On tourne dans le sens trigo, en partant du haut droit
        if (contient(boite.coin(Coin.HD))) {
            contour_masse.ajoutePoint(boite.coin(Coin.HD));
        }

        if (Environnement.quasiEgal(its.get(0).getY(), ymax)) {
            contour_masse.ajoutePoint(its.get(0));
        } else if (Environnement.quasiEgal(its.get(1).getY(), ymax)) {
            contour_masse.ajoutePoint(its.get(1));
        }

        if (contient(boite.coin(Coin.HG))) {
            contour_masse.ajoutePoint(boite.coin(Coin.HG));
        }

        if (Environnement.quasiEgal(its.get(0).getX(), xmin)) {
            contour_masse.ajoutePoint(its.get(0));
        } else if (Environnement.quasiEgal(its.get(1).getX(), xmin)) {
            contour_masse.ajoutePoint(its.get(1));
        }

        if (contient(boite.coin(Coin.BG))) {
            contour_masse.ajoutePoint(boite.coin(Coin.BG));
        }

        if (Environnement.quasiEgal(its.get(0).getY(), ymin)) {
            contour_masse.ajoutePoint(its.get(0));
        } else if (Environnement.quasiEgal(its.get(1).getY(), ymin)) {
            contour_masse.ajoutePoint(its.get(1));
        }

        if (contient(boite.coin(Coin.BD))) {
            contour_masse.ajoutePoint(boite.coin(Coin.BD));
        }

        if (Environnement.quasiEgal(its.get(0).getX(), xmax)) {
            contour_masse.ajoutePoint(its.get(0));
        } else if (Environnement.quasiEgal(its.get(1).getX(), xmax)) {
            contour_masse.ajoutePoint(its.get(1));
        }

       contour_masse.ferme();

        if (contour_masse.nombrePoints()>0)
            contours.ajouterContourMasse(contour_masse);

        if (avec_contours_surface) {
            Contour contour_surface = new Contour(2);
            contour_surface.ajoutePoint(its.get(0));
            contour_surface.ajoutePoint(its.get(1));

            contours.ajouterContourSurface(contour_surface);
        }

        return contours ;

    }

    public void definirOrigine(Point2D origine)  {
        position_orientation.set(new PositionEtOrientation(origine,orientation()));
    }

    public void definirAxeNormale(Point2D axe_n) {
        double angle_deg = axe_n.angle(new Point2D(1.0,0.0)) ;

        if (axe_n.getY()>=0)
            position_orientation.set(new PositionEtOrientation(origine(),angle_deg));
        else
            position_orientation.set(new PositionEtOrientation(origine(),360d-angle_deg));
    }

    @Override
    public void retaillerPourSourisEn(Point2D pos_souris) {
        // Si on est sur l'origine, ne rien faire
//        if (pos_souris.getX()==x_origine.get() && pos_souris.getY()==y_origine.get())
        if (pos_souris.equals(origine()))
            return ;

        if (!appartientASystemeOptiqueCentre())
            definirAxeNormale(pos_souris.subtract(origine()));

    }

    @Override
    public Contour positions_poignees() {
        // Si le demi-plan appartient à un SOC, inutile d'afficher une poignée, car on ne peut pas le retailler
        if (appartientASystemeOptiqueCentre())
            return null ;

        Contour c_poignees = new Contour(4);

        c_poignees.ajoutePoint(origine().add(Math.cos(Math.toRadians(orientation())), Math.sin(Math.toRadians(orientation())))) ;

        return c_poignees;
    }

    @Override
    public void ajouterRappelSurChangementToutePropriete(RappelSurChangement rap) {
        super.ajouterRappelSurChangementToutePropriete(rap);

        position_orientation.addListener((observable, oldValue, newValue) -> rap.rappel());
    }

    @Override
    public void ajouterRappelSurChangementTouteProprieteModifiantChemin(RappelSurChangement rap) {
        super.ajouterRappelSurChangementTouteProprieteModifiantChemin(rap);

        position_orientation.addListener((observable, oldValue, newValue) -> rap.rappel());
    }

    @Override
    public boolean aSymetrieDeRevolution() { return true ; }

    @Override
    public Point2D pointSurAxeRevolution() {
        return origine();
    }

    @Override
    public boolean estOrientable() { return true ; }

    @Override
    public void definirOrientation(double orientation_deg)  { position_orientation.set(new PositionEtOrientation(origine(),orientation_deg)); }

    @Override
    public boolean aUneOrientation() {
        return true;
    }

    @Override
    public double orientation() { return position_orientation.get().orientation_deg() ; }

    @Override
    public void tournerAutourDe(Point2D centre_rot, double angle_rot_deg) {
//        Rotate r = new Rotate(angle_rot_deg,centre_rot.getX(),centre_rot.getY()) ;
//
//        Point2D nouvelle_origine = r.transform(origine()) ;

        // Il faut ramener la nouvelle orientation entre 0 et 360° car les spinners et sliders "orientation" des
        // panneaux contrôleurs imposent ces limites via leurs min/max
//        double nouvelle_or = (orientation()+angle_rot_deg)%360 ;
//        if (nouvelle_or<0) nouvelle_or+=360 ;
//
//        System.out.println("Obstacle "+this+" : orientation "+orientation()+" ===> "+(orientation()+angle_rot_deg));
//        System.out.println("%360 : "+(orientation()+angle_rot_deg)%360 );
//        System.out.println("IEEERemainder : "+Math.IEEEremainder(orientation()+angle_rot_deg,360) );
//        position_orientation.set(new PositionEtOrientation(nouvelle_origine,(orientation()+angle_rot_deg)%360));
//        position_orientation.set(new PositionEtOrientation(nouvelle_origine,Obstacle.nouvelleOrientationApresRotation(orientation(),angle_rot_deg)));

        position_orientation.set(Obstacle.nouvellePositionEtOrientationApresRotation(position_orientation.get(),centre_rot,angle_rot_deg)) ;
    }

    @Override
    public void convertirDistances(double facteur_conversion) {
        position_orientation.set(new PositionEtOrientation(origine().multiply(facteur_conversion),orientation()));
    }

}
