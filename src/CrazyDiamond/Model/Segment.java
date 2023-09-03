package CrazyDiamond.Model;

import javafx.beans.property.*;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;

import java.io.IOException;
import java.util.ArrayList;

public class Segment implements Obstacle, Identifiable, Nommable,ElementAvecContour,ElementSansEpaisseur {

    private final Imp_Identifiable imp_identifiable ;
    private final Imp_Nommable imp_nommable;
    private final Imp_ElementAvecContour imp_elementAvecContour;
    private final Imp_ElementSansEpaisseur imp_elementSansEpaisseur;

//    private final DoubleProperty x_centre;
//    private final DoubleProperty y_centre;
//
//    // Orientation de la normale au segment (0° = segment vertical)
//    private final DoubleProperty orientation;
    // Orientation est celle de la normale au segment (0° = segment vertical)
    private final ObjectProperty<PositionEtOrientation> position_orientation ;
    private final DoubleProperty longueur;

    // Ouverture de la pupille
    private final DoubleProperty rayon_diaphragme;
    private ObjectProperty<Double> pupille_object;

    private final DemiDroiteOuSegment segment_support ;

    private static int compteur_segment;
    private final BooleanProperty appartenance_systeme_optique_centre;

    public Segment(double x_centre, double y_centre, double longueur, double orientation,double rayon_diaphragme) throws IllegalArgumentException {
        this(
            new Imp_Identifiable(),
            new Imp_Nommable("Segment " + (++compteur_segment)),
            new Imp_ElementAvecContour(null) ,
            new Imp_ElementSansEpaisseur(null),
            x_centre,y_centre,longueur,orientation,rayon_diaphragme
        ) ;
    }
    public Segment(Imp_Identifiable ii,Imp_Nommable in,Imp_ElementAvecContour iec, Imp_ElementSansEpaisseur iese,double x_centre, double y_centre, double longueur, double orientation,double rayon_diaphragme) throws IllegalArgumentException {

        if (longueur==0d)
            throw new IllegalArgumentException("Un segment ne peut pas être de longueur nulle.");

        imp_identifiable = ii ;
        imp_nommable = in ;
        imp_elementAvecContour = iec;
        imp_elementSansEpaisseur = iese;

        this.position_orientation = new SimpleObjectProperty<PositionEtOrientation>(new PositionEtOrientation(new Point2D(x_centre,y_centre),orientation)) ;
//        this.x_centre = new SimpleDoubleProperty(x_centre);
//        this.y_centre = new SimpleDoubleProperty(y_centre);
//        this.orientation = new SimpleDoubleProperty(orientation);

        this.longueur = new SimpleDoubleProperty(longueur);
        this.rayon_diaphragme = new SimpleDoubleProperty(rayon_diaphragme);

        this.appartenance_systeme_optique_centre = new SimpleBooleanProperty(false) ;

        segment_support = new DemiDroiteOuSegment() ;

        segment_support.definirDepartEtArrivee(new Point2D(x_centre,y_centre-longueur/2d),new Point2D(x_centre,y_centre+longueur/2d));
        segment_support.tournerAutourDe(segment_support.milieu(),orientation);

        this.position_orientation.addListener((observable, oldValue, newValue) -> {
            segment_support.definirDepartEtArrivee(
                    new Point2D(newValue.position().getX(),newValue.position().getY()-this.longueur.get()/2d),
                    new Point2D(newValue.position().getX(),newValue.position().getY()+this.longueur.get()/2d));
            segment_support.tournerAutourDe(segment_support.milieu(), newValue.orientation_deg()%360d);

        }) ;

//        this.x_centre.addListener((observable, oldValue, newValue) -> {
//            double delta_x = newValue.doubleValue() - segment_support.milieu().getX();
//            segment_support.definirDepartEtArrivee(
//                    new Point2D(segment_support.depart().getX()+delta_x,segment_support.depart().getY()),
//                    new Point2D(segment_support.arrivee().getX()+delta_x,segment_support.arrivee().getY())
//            );
//        }) ;
//
//        this.y_centre.addListener((observable, oldValue, newValue) -> {
//            double delta_y = newValue.doubleValue() - segment_support.milieu().getY();
//            segment_support.definirDepartEtArrivee(
//                    new Point2D(segment_support.depart().getX(),segment_support.depart().getY()+delta_y),
//                    new Point2D(segment_support.arrivee().getX(),segment_support.arrivee().getY()+delta_y)
//            );
//        }) ;
//
//        this.orientation.addListener((observable, oldValue, newValue) -> {
//            segment_support.definirDepartEtArrivee(new Point2D(this.x_centre.get(),this.y_centre.get()-this.longueur.get()/2d),
//                    new Point2D(this.x_centre.get(),this.y_centre.get()+this.longueur.get()/2d));
//            segment_support.tournerAutourDe(segment_support.milieu(), newValue.doubleValue()%360d);
//
//        } ) ;

        this.longueur.addListener((observable, oldValue, newValue) -> {
            double demi_l = (newValue.doubleValue())/2d ;
            segment_support.definirDepartEtArrivee(
                    segment_support.milieu().add(segment_support.direction().multiply(-demi_l)),
                    segment_support.milieu().add(segment_support.direction().multiply(+demi_l))
            );
        }) ;



    }

    @Override
    public void tournerAutourDe(Point2D centre_rot, double angle_rot_deg) {
        Rotate r = new Rotate(angle_rot_deg,centre_rot.getX(),centre_rot.getY()) ;

        Point2D nouveau_centre = r.transform(centre()) ;

//        x_centre.set(nouveau_centre.getX());
//        y_centre.set(nouveau_centre.getY());
//
//        orientation.set(orientation.get()+angle_rot_deg);

        position_orientation.set(new PositionEtOrientation(nouveau_centre,orientation()+angle_rot_deg));
    }

    public Point2D centre() { return position_orientation.get().position() ; }

    @Override public String id() { return imp_identifiable.id(); }


    @Override public String nom() {return imp_nommable.nom();}

    @Override public String toString() { return nom(); }

    @Override public StringProperty nomProperty() {return imp_nommable.nomProperty();}

    @Override public Color couleurContour() {return imp_elementAvecContour.couleurContour();}

    @Override public ObjectProperty<Color> couleurContourProperty() {return imp_elementAvecContour.couleurContourProperty();}

    @Override public void definirTraitementSurface(TraitementSurface traitement_surf) { imp_elementAvecContour.definirTraitementSurface(traitement_surf);}
    @Override public TraitementSurface traitementSurface() {return imp_elementAvecContour.traitementSurface() ;}
    @Override public ObjectProperty<TraitementSurface> traitementSurfaceProperty() {return imp_elementAvecContour.traitementSurfaceProperty() ;}
    @Override public DoubleProperty tauxReflexionSurfaceProperty() {return imp_elementAvecContour.tauxReflexionSurfaceProperty() ; }

    @Override public void definirOrientationAxePolariseur(double angle_pol) {imp_elementAvecContour.definirOrientationAxePolariseur(angle_pol);}
    @Override public double orientationAxePolariseur() {return imp_elementAvecContour.orientationAxePolariseur() ;}
    @Override public DoubleProperty orientationAxePolariseurProperty() {return imp_elementAvecContour.orientationAxePolariseurProperty() ;}
    @Override
    public Double courbureRencontreeAuSommet(Point2D pt_sur_surface, Point2D direction) throws Exception {
        return null ;
    }

    @Override public void definirTauxReflexionSurface(double taux_refl) {imp_elementAvecContour.definirTauxReflexionSurface(taux_refl);}
    @Override public double tauxReflexionSurface() {return imp_elementAvecContour.tauxReflexionSurface();}

    @Override public void definirNatureMilieu(NatureMilieu nature_mil) { imp_elementSansEpaisseur.definirNatureMilieu(nature_mil); }
    @Override public NatureMilieu natureMilieu() { return imp_elementSansEpaisseur.natureMilieu(); }
    @Override public ObjectProperty<NatureMilieu> natureMilieuProperty() { return imp_elementSansEpaisseur.natureMilieuProperty(); }

    public double x1() {
        return segment_support.depart().getX();
    }
    public double y1() {
        return segment_support.depart().getY();
    }

    public Point2D depart() { return segment_support.depart(); }

    public double x2() {
        return segment_support.arrivee().getX();
    }
    public double y2() {
        return segment_support.arrivee().getY();
    }
    public Point2D arrivee() { return segment_support.arrivee(); }

    public double x1Pupille() {return centre().add(segment_support.direction().multiply(-rayon_diaphragme.get())).getX() ; }
    public double y1Pupille() {return centre().add(segment_support.direction().multiply(-rayon_diaphragme.get())).getY() ; }
    public Point2D departPupille() {return centre().add(segment_support.direction().multiply(-rayon_diaphragme.get()));}

    public double x2Pupille() {return centre().add(segment_support.direction().multiply(rayon_diaphragme.get())).getX() ; }
    public double y2Pupille() {return centre().add(segment_support.direction().multiply(rayon_diaphragme.get())).getY() ; }
    public Point2D arriveePupille() {return centre().add(segment_support.direction().multiply(rayon_diaphragme.get()));}

//    public DoubleProperty xCentreProperty() {
//        return x_centre;
//    }
    public double xCentre() { return  centre().getX() ; }
//    public DoubleProperty yCentreProperty() {
//        return y_centre;
//    }
    public double yCentre() { return  centre().getY() ; }

    public DoubleProperty longueurProperty() {
        return longueur;
    }
    public double longueur() { return  longueur.get() ; }

//    public DoubleProperty orientationProperty() {
//        return orientation;
//    }
    public DoubleProperty rayonDiaphragmeProperty() {return rayon_diaphragme;}
    public double rayonDiaphragme() { return  rayon_diaphragme.get() ; }


    public BooleanProperty appartenanceSystemeOptiqueProperty() {return appartenance_systeme_optique_centre ;}
    
    public void translater(Point2D vecteur) {

        position_orientation.set(new PositionEtOrientation(centre().add(vecteur),orientation()));
//        x_centre.set(vecteur.getX()+x_centre.get()) ;
//        y_centre.set(vecteur.getY()+y_centre.get()) ;

//        segment_support.definirDepartEtArrivee(new Point2D(X1(),Y1()), new Point2D(X2(),Y2()));

    }

    public void accepte(VisiteurEnvironnement v) {
        v.visiteSegment(this);
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
    public void appliquerSurElementSansEpaisseur(ConsumerAvecException<Object,IOException> consumer) throws IOException {
        consumer.accept(imp_elementSansEpaisseur);
    }

    @Override
    public void ajouterRappelSurChangementToutePropriete(RappelSurChangement rap) {
        imp_elementAvecContour.ajouterRappelSurChangementToutePropriete(rap);
        imp_elementSansEpaisseur.ajouterRappelSurChangementToutePropriete(rap);

        position_orientation.addListener((observable, oldValue, newValue) -> {rap.rappel();});
//        x_centre.addListener((observable, oldValue, newValue) -> {rap.rappel();});
//        y_centre.addListener((observable, oldValue, newValue) -> {rap.rappel();});
//        orientation.addListener((observable, oldValue, newValue) -> {rap.rappel();});
        longueur.addListener((observable, oldValue, newValue) -> {rap.rappel();});
        rayon_diaphragme.addListener((observable, oldValue, newValue) -> {rap.rappel();});

    }

    @Override
    public void ajouterRappelSurChangementTouteProprieteModifiantChemin(RappelSurChangement rap) {
        imp_elementAvecContour.ajouterRappelSurChangementTouteProprieteModifiantChemin(rap);
        imp_elementSansEpaisseur.ajouterRappelSurChangementTouteProprieteModifiantChemin(rap);

        position_orientation.addListener((observable, oldValue, newValue) -> {rap.rappel();});
//        x_centre.addListener((observable, oldValue, newValue) -> {rap.rappel();});
//        y_centre.addListener((observable, oldValue, newValue) -> {rap.rappel();});
//        orientation.addListener((observable, oldValue, newValue) -> {rap.rappel();});
        longueur.addListener((observable, oldValue, newValue) -> {rap.rappel();});
        rayon_diaphragme.addListener((observable, oldValue, newValue) -> {rap.rappel();});

    }

    @Override
    public void retaillerPourSourisEn(Point2D pos_souris) {

        // Si on est sur le point de départ, ne rien faire
        if (pos_souris.equals(centre()))
            return ;

        if (!appartientASystemeOptiqueCentre()) {
            Point2D centre = centre();

            Point2D vec_centre_pos = pos_souris.subtract(centre);
            longueur.set(2d * vec_centre_pos.magnitude());
            rayon_diaphragme.set(Math.min(rayon_diaphragme.get(),longueur.get()));

            double or = Math.toDegrees(Math.atan2(vec_centre_pos.getY(), vec_centre_pos.getX()));

            if (or - 90d < 0)
                or += 360d;

            definirOrientation((or - 90d) % 360d);
        } else { // Le segment est dans un SOC : on ne peut pas en changer l'orientation, mais seulement la longueur

            double nouvelle_longueur = 2*Math.abs(produit_vectoriel_simplifie(segment_support.normale(),pos_souris.subtract(centre()))) ;
            longueur.set(nouvelle_longueur);
            rayon_diaphragme.set(Math.min(rayon_diaphragme.get(),longueur.get()));

        }
    }

    @Override
    public Contour positions_poignees() {
        Contour c_poignees = new Contour(4);

//        c_poignees.ajoutePoint(x1.get() , y1.get());
        c_poignees.ajoutePoint(x2() , y2());

        return c_poignees;
    }

    private double produit_vectoriel_simplifie(Point2D v1, Point2D v2) {
        return (v1.getX()*v2.getY()-v1.getY()*v2.getX()) ;
    }

    // La méthode "contient()" renvoie toujours false pour un ElementSansEpaisseur => l'interface ElementSansEpaisseur
    // fournit cette implémentation par défaut qui renvoie toujours false ;
    @Override
    public boolean contient(Point2D p) {
        return ElementSansEpaisseur.super.contient(p);
    }

    @Override
    public boolean aSurSaSurface(Point2D p) {


        if (Environnement.quasiEgal(x1(), x2())) {
            if ( Environnement.quasiEgal(p.getX(), x1()) && p.getY() > Math.min(y1(), y2()) && p.getY() < Math.max(y1(), y2()))
                return true;

            return false;
        }

        //        if (Environnement.quasiInferieurOuEgal(p.getX(),Math.min(X1(), X2())) || Environnement.quasiInferieurOuEgal( Math.max(X1(),X2()),p.getX()) )
        if (p.getX() < Math.min(x1(), x2()) || p.getX() > Math.max(x1(), x2()))
            return false;

        double a = (y2() - y1()) / (x2() - x1());
        double yseg = a * (p.getX() - x1()) + y1();

//        return Environnement.quasiEgal(yseg, p.getY());
        if (Environnement.quasiEgal(yseg, p.getY()))
            return !(p.subtract(centre()).magnitude() < (rayon_diaphragme.get() / 2d)) ;

        return false ;

    }

    @Override
    public boolean est_tres_proche_de(Point2D p,double tolerance) {

        if (p.getX()+tolerance <Math.min(x1(), x2()))
            return false;
        if (p.getX()-tolerance > Math.max(x1(), x2()))
            return false;

        if (x1() == x2()) {
            return Environnement.quasiEgal(p.getX(), x1(), tolerance)
                    && Environnement.quasiSuperieurOuEgal(p.getY(), Math.min(y1(), y2()), tolerance)
                    && Environnement.quasiInferieurOuEgal(p.getY(), Math.max(y1(), y2()), tolerance);

        }

        double a = (y2() - y1()) / (x2() - x1());
        double yseg = a * (p.getX() - x1()) + y1();

//        return Environnement.quasiEgal(yseg, p.getY(),tolerance) ;
        if (Environnement.quasiEgal(yseg, p.getY(),tolerance))
            return !(p.subtract(centre()).magnitude() < (rayon_diaphragme.get() / 2d)) ;

        return false ;

    }

    @Override
    public Point2D normale(Point2D p) throws Exception {

        return segment_support.normale() ;

//        Point2D ab = (new Point2D(x2.get(), y2.get())).subtract(new Point2D(x1.get(), y1.get()));
//        Point2D normale = (new Point2D(-ab.getY(), ab.getX())).normalize();
//
//        if (normale.getY() < 0)
//            return normale.multiply(-1.0);
//
//        if (normale.getY() == 0 && normale.getX() > 0)
//            return normale.multiply(-1.0);
//
//        return normale;
    }

    @Override
    public ArrayList<Point2D> cherche_toutes_intersections(Rayon r) {

        ArrayList<Point2D> resultats = new ArrayList<>(1);

        resultats.add(cherche_intersection(r,ModeRecherche.PREMIERE));

        return resultats;
    }


    @Override
    public Point2D cherche_intersection(Rayon r, ModeRecherche mode) {
        if (aSurSaSurface(r.depart()))
            return null ;

        Point2D res = segment_support.intersectionAvec(r.supportGeometrique()) ;

        if (res!=null && res.subtract(centre()).magnitude()< (rayon_diaphragme.get()/2d))
            return null ;

        return res ;


    }

    public Point2D cherche_intersection_old(Rayon r, ModeRecherche mode) {
        if (aSurSaSurface(r.depart()))
            return null ;

        double xinter ;
        double yinter ;

        // Segment vertical
        if (x1() == x2()) {

            // Rayon vertical ?
            if (r.direction().getX() == 0)
                return null ;

            double aprime = r.direction().getY() / r.direction().getX() ;

            xinter = x1() ;
            yinter = aprime*(x1() - r.depart().getX()) + r.depart().getY() ;

            if ( (Math.min(y1(), y2()) > yinter) || (yinter > Math.max(y1(), y2())) )
                return null ;

            if (r.direction().getX()>0 && xinter < r.depart().getX())
                return null ;

            if (r.direction().getX()<0 && xinter > r.depart().getX())
                return null ;

            return new Point2D(xinter, yinter) ;
        }

        // Rayon vertical
        if (r.direction().getX()==0) {
            if ( (r.depart().getX()<Math.min(x1(), x2())) || (r.depart().getX()>Math.max(x1(), x2())) )
                return null ;

            double a      = (y2()- y1())/(x2()- x1()) ;
            yinter = a*(r.depart().getX() - x1()) + y1() ;

            if (r.direction().getY()>0 && yinter < r.depart().getY())
                return null ;

            if (r.direction().getY()<0 && yinter > r.depart().getY())
                return null ;

            return new Point2D(r.depart().getX(),yinter) ;

        }

        double aprime = r.direction().getY() / r.direction().getX() ;
        double a      = (y2()- y1())/(x2()- x1()) ;

        if (a==aprime) // Rayon parallèle au segment
            return null ;

        // Cas général : segment non vertical, et rayon non vertical, rayon et segment non parallèles

        xinter = ( a* x1() - aprime * r.depart().getX() + ( r.depart().getY() - y1() ) ) / ( a - aprime ) ;
        yinter = a*(xinter - x1()) + y1() ;


        if (xinter<Math.min(x1(), x2()) || xinter>Math.max(x1(), x2()) )
            return null ;

        if (r.direction().getX()>0 && xinter< r.depart().getX())
            return null ;

        if (r.direction().getX()<0 && xinter> r.depart().getX())
            return null ;

        if (r.direction().getY()>0 && yinter< r.depart().getY())
            return null ;

        if (r.direction().getY()<0 && yinter> r.depart().getY())
            return null ;

        return new Point2D(xinter,yinter) ;
    }

    public boolean aSymetrieDeRevolution() {return true ;}

    @Override
    public Point2D pointSurAxeRevolution() {
        return new Point2D((x1()+ x2())/2,(y1()+ y2())/2 ) ;
    }

    @Override
    public boolean estOrientable() {
        return true ;
    }

    @Override
    public void definirOrientation(double orientation_deg) {
        position_orientation.set(new PositionEtOrientation(centre(),orientation_deg));

//        orientation.set(orientation_deg);

//        double delta_orientation = Math.toRadians(orientation_deg - orientation()) ;
//
//        Point2D centre = new Point2D((X1()+X2())/2d,(Y1()+Y2())/2d ) ;
//
//        Point2D p1_r = new Point2D(X1()-centre.getX() , Y1()-centre.getY() ) ;
//        Point2D p2_r = new Point2D(X2()-centre.getX() , Y2()-centre.getY() ) ;
//
//        x1.set( centre.getX() + p1_r.getX()*Math.cos(delta_orientation)-p1_r.getY()*Math.sin(delta_orientation) ) ;
//        y1.set( centre.getY() + p1_r.getX()*Math.sin(delta_orientation)+p1_r.getY()*Math.cos(delta_orientation) ) ;
//
//        x2.set( centre.getX() + p2_r.getX()*Math.cos(delta_orientation)-p2_r.getY()*Math.sin(delta_orientation) ) ;
//        y2.set( centre.getY() + p2_r.getX()*Math.sin(delta_orientation)+p2_r.getY()*Math.cos(delta_orientation) ) ;

    }

    @Override
    public boolean aUneOrientation() {
        return true;
    }

    @Override
    public double orientation()  {

        return position_orientation.get().orientation_deg() ;
//        return orientation.get() ;

//         Point2D normale = DemiDroiteOuSegment.construireSegment(x1.get(),y1.get(),x2.get(),y2.get()).normale() ;
//
//         double res = Math.toDegrees(Math.atan2(normale.getY(), normale.getX())) ;
//
//         if (res<0)
//             res=360d+res ;
//
//         return res ;

    }

    @Override
    public void definirAppartenanceSystemeOptiqueCentre(boolean b) {this.appartenance_systeme_optique_centre.set(b);}
    @Override
    public boolean appartientASystemeOptiqueCentre() {return this.appartenance_systeme_optique_centre.get() ;}

    /**
     * @return
     */
    @Override
    public boolean aUneProprieteDiaphragme() {
        return true ;
    }

    /**
     * @return
     */
    @Override
    public ObjectProperty<Double> diaphragmeProperty() {
        if (pupille_object==null)
            pupille_object = rayon_diaphragme.asObject() ;
        return pupille_object ;
    }

    /**
     * @param diaph_max_conseille
     */
    @Override
    public void forcerRayonDiaphragmeMaximumConseille(Double diaph_max_conseille) {
        if (diaph_max_conseille!=null)
            longueur.set(diaph_max_conseille);
    }

    @Override
    public double rayonDiaphragmeMaximumConseille() {
        return longueur.get();
    }

    @Override
    public Double abscissePremiereIntersectionSurAxe(Point2D origine_axe, Point2D direction_axe, double z_depart,boolean sens_z_croissants, Double z_inter_prec) {

        double z_centre ;

        if (centre().subtract(origine_axe).dotProduct(direction_axe)>=0)
            z_centre = centre().distance(origine_axe) ;
        else
            z_centre = -centre().distance(origine_axe) ;

        // S'assurer de ne pas retourner à nouveau l'intersection z_inter_prec
        if (z_inter_prec!=null && z_centre==z_inter_prec)
            return null ;

//        // Cas particuliers où le point de départ est sur l'intersection
//        if (Environnement.quasiEgal(z_depart,z_centre))
//            return null ;

        // Cas général
        if (z_depart<z_centre)
            return (sens_z_croissants?z_centre:null) ;

        // z_depart > z_centre
        return (sens_z_croissants?null:z_centre) ;

    }

    @Override
    public ArrayList<Double> abscissesToutesIntersectionsSurAxe(Point2D origine_axe, Point2D direction_axe, double z_depart,boolean sens_z_croissants, Double z_inter_prec) {

        ArrayList<Double> resultat = new ArrayList<>(2) ;

        double z_centre ;

        if (centre().subtract(origine_axe).dotProduct(direction_axe)>=0)
            z_centre = centre().distance(origine_axe) ;
        else
            z_centre = -centre().distance(origine_axe) ;


        // S'assurer de ne pas retourner à nouveau l'intersection z_inter_prec
        if (z_inter_prec!=null && z_centre==z_inter_prec) {
                return resultat ;
        }

//        // Cas particulier où le point de départ est sur l'intersection
//        if (Environnement.quasiEgal(z_depart,z_centre))
//            return resultat ;

        // Cas général
        if (z_depart<z_centre && sens_z_croissants) {
            resultat.add(z_centre) ;
        }
        else if (z_depart > z_centre && !sens_z_croissants) {
            resultat.add(z_centre) ;
        }

        return resultat ;

    }

    public ObjectProperty<PositionEtOrientation> positionEtOrientationObjectProperty() {
        return position_orientation ;
    }

    public void definirCentre(Point2D centre) {
        position_orientation.set(new PositionEtOrientation(centre,orientation()));
    }

    public void definirLongueur(double lng) {
        longueur.set(lng);
    }

    public void definirRayonDiaphragme(Double r_d) {
        rayon_diaphragme.set(r_d);
    }
}
