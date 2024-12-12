package CrazyDiamond.Model;

import javafx.beans.property.*;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class Segment extends BaseObstacleAvecContourSansEpaisseur implements Obstacle, Identifiable, Nommable,ElementAvecContour,ElementSansEpaisseur {

    // Orientation est celle de la normale au segment (0° = segment vertical)
    private final ObjectProperty<PositionEtOrientation> position_orientation ;
    private final DoubleProperty longueur;

    // Ouverture de la pupille
    private final DoubleProperty rayon_diaphragme;
    private ObjectProperty<Double> pupille_object;

    private final DemiDroiteOuSegment segment_support ;

    private static int compteur_segment;

    public Segment(double x_centre, double y_centre, double longueur, double orientation_deg,double rayon_diaphragme) throws IllegalArgumentException {
        this(null,x_centre,y_centre,longueur,orientation_deg,rayon_diaphragme,null,null) ;
    }

    public Segment(String nom,  double  x_centre, double y_centre, double longueur, double orientation_deg,double rayon_diaphragme , NatureMilieu nature_milieu, Color couleur_contour) throws IllegalArgumentException {
        super(nom != null ? nom : "Segment " + (++compteur_segment), nature_milieu, couleur_contour);

        // TODO : A partir d'ici, code dupliqué avec le constructeur qui suit => A factoriser mais on dirait que ce n'est pas possible
        // les propriétés "final" ne peuvent pas être initialisées dans une sous-fonction du constructeur. Pfff..
        if (longueur==0d)
            throw new IllegalArgumentException("Un segment ne peut pas être de longueur nulle.");

        this.position_orientation = new SimpleObjectProperty<>(new PositionEtOrientation(new Point2D(x_centre,y_centre),orientation_deg)) ;

        this.longueur = new SimpleDoubleProperty(longueur);
        this.rayon_diaphragme = new SimpleDoubleProperty(rayon_diaphragme);

        segment_support = new DemiDroiteOuSegment() ;

        segment_support.definirDepartEtArrivee(new Point2D(x_centre,y_centre-longueur/2d),new Point2D(x_centre,y_centre+longueur/2d));
        segment_support.tournerAutourDe(segment_support.milieu(),orientation_deg);

        this.position_orientation.addListener((observable, oldValue, newValue) -> {
            segment_support.definirDepartEtArrivee(
                    new Point2D(newValue.position().getX(),newValue.position().getY()-this.longueur.get()/2d),
                    new Point2D(newValue.position().getX(),newValue.position().getY()+this.longueur.get()/2d));
            segment_support.tournerAutourDe(segment_support.milieu(), newValue.orientation_deg()%360d);

        }) ;

        this.longueur.addListener((observable, oldValue, newValue) -> {
            double demi_l = (newValue.doubleValue())/2d ;
            segment_support.definirDepartEtArrivee(
                    segment_support.milieu().add(segment_support.direction().multiply(-demi_l)),
                    segment_support.milieu().add(segment_support.direction().multiply(+demi_l))
            );
        }) ;

    }

    public Segment(Imp_Identifiable ii,Imp_Nommable in,Imp_ElementAvecContour iec, Imp_ElementSansEpaisseur iese,
                   double x_centre, double y_centre, double longueur, double orientation_deg,double rayon_diaphragme) throws IllegalArgumentException {
        super(ii,in,iec,iese) ;

        if (longueur==0d)
            throw new IllegalArgumentException("Un segment ne peut pas être de longueur nulle.");

        this.position_orientation = new SimpleObjectProperty<>(new PositionEtOrientation(new Point2D(x_centre,y_centre),orientation_deg)) ;

        this.longueur = new SimpleDoubleProperty(longueur);
        this.rayon_diaphragme = new SimpleDoubleProperty(rayon_diaphragme);

        segment_support = new DemiDroiteOuSegment() ;

        segment_support.definirDepartEtArrivee(new Point2D(x_centre,y_centre-longueur/2d),new Point2D(x_centre,y_centre+longueur/2d));
        segment_support.tournerAutourDe(segment_support.milieu(),orientation_deg);

        this.position_orientation.addListener((observable, oldValue, newValue) -> {
            segment_support.definirDepartEtArrivee(
                    new Point2D(newValue.position().getX(),newValue.position().getY()-this.longueur.get()/2d),
                    new Point2D(newValue.position().getX(),newValue.position().getY()+this.longueur.get()/2d));
            segment_support.tournerAutourDe(segment_support.milieu(), newValue.orientation_deg()%360d);

        }) ;

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
//        Rotate r = new Rotate(angle_rot_deg,centre_rot.getX(),centre_rot.getY()) ;
//
//        Point2D nouveau_centre = r.transform(centre()) ;

        // Il faut ramener la nouvelle orientation entre 0 et 360° car les spinners et sliders "orientation" des
        // panneaux contrôleurs imposent ces limites via leurs min/max
//        double nouvelle_or = (orientation()+angle_rot_deg)%360 ;
//        if (nouvelle_or<0) nouvelle_or+=360 ;
//
//        position_orientation.set(new PositionEtOrientation(nouveau_centre,Obstacle.nouvelleOrientationApresRotation(orientation(),angle_rot_deg)/*orientation()+angle_rot_deg*/));
        position_orientation.set(Obstacle.nouvellePositionEtOrientationApresRotation(position_orientation.get(),centre_rot,angle_rot_deg)) ;
    }

    public Point2D centre() { return position_orientation.get().position() ; }

    @Override
    public Double courbureRencontreeAuSommet(Point2D pt_sur_surface, Point2D direction) throws Exception {
        return null ;
    }

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

    public Point2D departPupille() {return centre().add(segment_support.direction().multiply(-rayon_diaphragme.get()));}

    public Point2D arriveePupille() {return centre().add(segment_support.direction().multiply(rayon_diaphragme.get()));}

    public double xCentre() { return  centre().getX() ; }

    public double yCentre() { return  centre().getY() ; }

    public DoubleProperty longueurProperty() {
        return longueur;
    }
    public double longueur() { return  longueur.get() ; }

    public DoubleProperty rayonDiaphragmeProperty() {return rayon_diaphragme;}
    public double rayonDiaphragme() { return  rayon_diaphragme.get() ; }

    public void translater(Point2D vecteur) {
        position_orientation.set(new PositionEtOrientation(centre().add(vecteur),orientation()));
    }

    @Override
    public void translaterParCommande(Point2D vecteur) {
        new CommandeDefinirUnParametrePoint<>(this,centre().add(vecteur),this::centre,this::definirCentre).executer() ;
    }
    public void accepte(VisiteurEnvironnement v) {
        v.visiteSegment(this);
    }

    @Override
    public void ajouterRappelSurChangementToutePropriete(RappelSurChangement rap) {
        super.ajouterRappelSurChangementToutePropriete(rap);

        position_orientation.addListener((observable, oldValue, newValue) -> rap.rappel());
        longueur.addListener((observable, oldValue, newValue) -> rap.rappel());
        rayon_diaphragme.addListener((observable, oldValue, newValue) -> rap.rappel());
    }

    @Override
    public void ajouterRappelSurChangementTouteProprieteModifiantChemin(RappelSurChangement rap) {
        super.ajouterRappelSurChangementTouteProprieteModifiantChemin(rap);

        position_orientation.addListener((observable, oldValue, newValue) -> rap.rappel());

        longueur.addListener((observable, oldValue, newValue) -> rap.rappel());
        rayon_diaphragme.addListener((observable, oldValue, newValue) -> rap.rappel());
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
            rayon_diaphragme.set(Math.min(rayon_diaphragme.get(),0.5d*longueur.get()));

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
            return Environnement.quasiEgal(p.getX(), x1()) && p.getY() > Math.min(y1(), y2()) && p.getY() < Math.max(y1(), y2());
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
    public boolean estTresProcheDe(Point2D p, double tolerance) {

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

        if (res!=null && res.subtract(centre()).magnitude()< (rayon_diaphragme.get()))
            return null ;

        return res ;

    }

    public boolean aSymetrieDeRevolution() {return true ;}

    @Override
    public Point2D pointSurAxeRevolution() {
        return centre() ; /* new Point2D((x1()+ x2())/2,(y1()+ y2())/2 ) ; */
    }

    @Override
    public boolean estOrientable() {
        return true ;
    }

    @Override
    public void definirOrientation(double orientation_deg) {
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
    public boolean aUneProprieteDiaphragme() {
        return true ;
    }

    @Override
    public ObjectProperty<Double> diaphragmeProperty() {
        if (pupille_object==null)
            pupille_object = rayon_diaphragme.asObject() ;
        return pupille_object ;
    }

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
    public List<DioptreParaxial> dioptresParaxiaux(PositionEtOrientation axe) {

        ArrayList<DioptreParaxial> resultat = new ArrayList<>(1) ;

        double z_centre = centre().subtract(axe.position()).dotProduct(axe.direction()) ;

        DioptreParaxial d_z_centre = new DioptreParaxial(z_centre, null, 0d , 0d, this);

        resultat.add(d_z_centre) ;

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

    @Override
    public void convertirDistances(double facteur_conversion) {

        position_orientation.set(new PositionEtOrientation(centre().multiply(facteur_conversion),orientation()));

        if (facteur_conversion>=1) {
            longueur.set(longueur()*facteur_conversion);
            rayon_diaphragme.set(rayonDiaphragme()*facteur_conversion);
        } else {
            rayon_diaphragme.set(rayonDiaphragme()*facteur_conversion);
            longueur.set(longueur()*facteur_conversion);
        }
    }

}
