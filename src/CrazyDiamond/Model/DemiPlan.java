package CrazyDiamond.Model;

import javafx.beans.property.*;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class DemiPlan implements Obstacle, Identifiable, Nommable, ElementAvecContour, ElementAvecMatiere {

    private final Imp_Identifiable imp_identifiable ;
    private final Imp_Nommable imp_nommable;
    private final Imp_ElementAvecContour imp_elementAvecContour ;
    private final Imp_ElementAvecMatiere imp_elementAvecMatiere ;

    private final ObjectProperty<PositionEtOrientation> position_orientation ;

    private static int compteur_demi_plan = 0 ;
    private final BooleanProperty appartenance_composition;
    private final BooleanProperty appartenance_systeme_optique_centre;

    public DemiPlan(TypeSurface type_surface, double x_origine, double y_origine, double orientation_deg) throws IllegalArgumentException {
        this(
                new Imp_Identifiable(),
                new Imp_Nommable("Demi-plan " + (++compteur_demi_plan)),
                new Imp_ElementAvecContour(null),
                new Imp_ElementAvecMatiere(type_surface, null,1.0,null),
                x_origine,y_origine,orientation_deg
        );
    }
    public DemiPlan(Imp_Identifiable ii,Imp_Nommable in,Imp_ElementAvecContour iec, Imp_ElementAvecMatiere iem , double x_origine, double y_origine, double orientation_deg) throws IllegalArgumentException {

        imp_identifiable = ii ;
        imp_nommable = in ;
        imp_elementAvecContour = iec;
        imp_elementAvecMatiere = iem;

        this.position_orientation = new SimpleObjectProperty<PositionEtOrientation>(new PositionEtOrientation(new Point2D(x_origine,y_origine),orientation_deg)) ;

        this.appartenance_composition = new SimpleBooleanProperty(false) ;
        this.appartenance_systeme_optique_centre = new SimpleBooleanProperty(false) ;

    }

    @Override public String id() { return imp_identifiable.id(); }

    @Override public String nom() {  return imp_nommable.nom(); }
    @Override public StringProperty nomProperty() { return imp_nommable.nomProperty(); }

    @Override public Color couleurContour() { return imp_elementAvecContour.couleurContour();}
    @Override public ObjectProperty<Color> couleurContourProperty() { return imp_elementAvecContour.couleurContourProperty(); }

    @Override public void definirTraitementSurface(TraitementSurface traitement_surf) { imp_elementAvecContour.definirTraitementSurface(traitement_surf);}
    @Override public TraitementSurface traitementSurface() {return imp_elementAvecContour.traitementSurface() ;}
    @Override public ObjectProperty<TraitementSurface> traitementSurfaceProperty() {return imp_elementAvecContour.traitementSurfaceProperty() ;}
    @Override public DoubleProperty tauxReflexionSurfaceProperty() {return imp_elementAvecContour.tauxReflexionSurfaceProperty() ; }
    @Override public void definirTauxReflexionSurface(double taux_refl) {imp_elementAvecContour.definirTauxReflexionSurface(taux_refl);}
    @Override public double tauxReflexionSurface() {return imp_elementAvecContour.tauxReflexionSurface();}

    @Override public void definirOrientationAxePolariseur(double angle_pol) {imp_elementAvecContour.definirOrientationAxePolariseur(angle_pol);}
    @Override public double orientationAxePolariseur() {return imp_elementAvecContour.orientationAxePolariseur() ;}
    @Override public DoubleProperty orientationAxePolariseurProperty() {return imp_elementAvecContour.orientationAxePolariseurProperty() ;}

    @Override
    public Double courbureRencontreeAuSommet(Point2D pt_sur_surface, Point2D direction) throws Exception {
        return null ;
    }


    @Override public Color couleurMatiere() { return imp_elementAvecMatiere.couleurMatiere(); }
    @Override public ObjectProperty<Color> couleurMatiereProperty() { return imp_elementAvecMatiere.couleurMatiereProperty(); }

    @Override public void definirTypeSurface(TypeSurface type_surf) { imp_elementAvecMatiere.definirTypeSurface(type_surf); }
    @Override public TypeSurface typeSurface() { return imp_elementAvecMatiere.typeSurface(); }
    @Override public ObjectProperty<TypeSurface> typeSurfaceProperty() { return imp_elementAvecMatiere.typeSurfaceProperty(); }

    @Override public void definirNatureMilieu(NatureMilieu nature_mil) { imp_elementAvecMatiere.definirNatureMilieu(nature_mil); }
    @Override public NatureMilieu natureMilieu() { return imp_elementAvecMatiere.natureMilieu(); }
    @Override public ObjectProperty<NatureMilieu> natureMilieuProperty() { return imp_elementAvecMatiere.natureMilieuProperty(); }

    @Override public void definirIndiceRefraction(double indice_refraction) { imp_elementAvecMatiere.definirIndiceRefraction(indice_refraction);   }
    @Override public double indiceRefraction() { return imp_elementAvecMatiere.indiceRefraction(); }
    @Override public DoubleProperty indiceRefractionProperty() {  return imp_elementAvecMatiere.indiceRefractionProperty(); }

    public BooleanProperty appartenanceSystemeOptiqueProperty() {return appartenance_systeme_optique_centre ;}

    @Override public String toString() { return nom(); }

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

        if (Math.abs(axe.orientation_deg() - orientation())>90d) // Cet écart vaut 0 ou 180°
            d_z_origine.permuterIndicesAvantApres();

        resultat.add(d_z_origine) ;

        return resultat ;

    }


    public Point2D origine() {return position_orientation.get().position(); }
    public double xOrigine() { return position_orientation.get().position().getX(); }
    public double yOrigine() { return position_orientation.get().position().getY();  }

    // Direction de la frontière du demi-plan
    public Point2D direction() {

        double ori_rad = Math.toRadians(orientation()) ;
        Point2D p_norm = new Point2D(Math.cos(ori_rad),Math.sin(ori_rad)) ;

        return new Point2D(-p_norm.getY(),p_norm.getX()) ;
    }

    public void appliquerSurIdentifiable(ConsumerAvecException<Object, IOException> consumer) throws IOException {
        consumer.accept(imp_identifiable);
    }
    public void appliquerSurNommable(ConsumerAvecException<Object,IOException> consumer) throws IOException {
        consumer.accept(imp_nommable);
    }
    public void appliquerSurElementAvecContour(ConsumerAvecException<Object,IOException> consumer) throws IOException {
        consumer.accept(imp_elementAvecContour);
    }
    public void appliquerSurElementAvecMatiere(ConsumerAvecException<Object,IOException> consumer) throws IOException {
        consumer.accept(imp_elementAvecMatiere);
    }

    @Override
    public void translater(Point2D vecteur) {
        position_orientation.set(new PositionEtOrientation(origine().add(vecteur),orientation()));
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
        double ori_rad = Math.toRadians(orientation()) ;

        Point2D norm = new Point2D(Math.cos(ori_rad),Math.sin(ori_rad)) ;

        if (typeSurface() == TypeSurface.CONVEXE)
            return norm ;
        else
            return norm.multiply(-1.0) ;

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
        DemiDroiteOuSegment s = new DemiDroiteOuSegment(origine(),direction()) ;

        Point2D p_inter1 = boite.premiere_intersection(s) ;
        Point2D p_inter2 = boite.derniere_intersection(s) ;

        if (p_inter1 != null && p_inter1.equals(p_inter2))
            p_inter2 = null ;

//        Rayon r_opp = new Rayon(origine(),direction().multiply(-1.0)) ;
        DemiDroiteOuSegment s_opp = new DemiDroiteOuSegment(origine(),direction().multiply(-1.0)) ;
        Point2D p_inter_opp1 = boite.premiere_intersection(s_opp) ;
        Point2D p_inter_opp2 = boite.derniere_intersection(s_opp) ;

        if (p_inter_opp1 != null && p_inter_opp1.equals(p_inter_opp2))
            p_inter_opp2 = null ;

        ArrayList<Point2D> its = new ArrayList<Point2D>(2) ;

        if (boite.aSurSaSurface(origine()))
            its.add(origine()) ;

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


        // Pas d'intersection ou 1 seul intersection (avec un coin)
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
//            orientation.set(angle_deg);
        else
            position_orientation.set(new PositionEtOrientation(origine(),360d-angle_deg));
//        orientation.set(360-angle_deg);
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

        // Si le demi-plan appartient à un SOC, innutile d'afficher une poignée car on ne peut pas le retailler
        if (appartientASystemeOptiqueCentre())
            return null ;

        Contour c_poignees = new Contour(4);

        c_poignees.ajoutePoint(origine().add(Math.cos(Math.toRadians(orientation())), Math.sin(Math.toRadians(orientation())))) ;
//        c_poignees.ajoutePoint(x_origine.get() + Math.cos(Math.toRadians(orientation.get())),
//                               y_origine.get() + Math.sin(Math.toRadians(orientation.get())));

        return c_poignees;
    }


    @Override
    public void ajouterRappelSurChangementToutePropriete(RappelSurChangement rap) {

        imp_elementAvecContour.ajouterRappelSurChangementToutePropriete(rap);
        imp_elementAvecMatiere.ajouterRappelSurChangementToutePropriete(rap);

        position_orientation.addListener((observable, oldValue, newValue) -> { rap.rappel(); });
    }

    @Override
    public void ajouterRappelSurChangementTouteProprieteModifiantChemin(RappelSurChangement rap) {

        imp_elementAvecContour.ajouterRappelSurChangementTouteProprieteModifiantChemin(rap);
        imp_elementAvecMatiere.ajouterRappelSurChangementTouteProprieteModifiantChemin(rap);

        position_orientation.addListener((observable, oldValue, newValue) -> { rap.rappel(); });
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
//    public void definirOrientation(double orientation_deg) {
//        this.orientation.set(orientation_deg);
//    }
    public void definirOrientation(double orientation_deg)  { position_orientation.set(new PositionEtOrientation(origine(),orientation_deg)); }


    @Override
    public boolean aUneOrientation() {
        return true;
    }

    @Override
    public double orientation() { return position_orientation.get().orientation_deg() ; }

    @Override
    public void definirAppartenanceSystemeOptiqueCentre(boolean b) {this.appartenance_systeme_optique_centre.set(b);}
    @Override
    public boolean appartientASystemeOptiqueCentre() {return this.appartenance_systeme_optique_centre.get() ;}

    @Override
    public void definirAppartenanceComposition(boolean b) {this.appartenance_composition.set(b);}
    @Override
    public boolean appartientAComposition() {return this.appartenance_composition.get() ;}

    @Override
    public void tournerAutourDe(Point2D centre_rot, double angle_rot_deg) {
        Rotate r = new Rotate(angle_rot_deg,centre_rot.getX(),centre_rot.getY()) ;

        Point2D nouvelle_origine = r.transform(origine()) ;

        position_orientation.set(new PositionEtOrientation(nouvelle_origine,orientation()+angle_rot_deg));
    }

}
