package CrazyDiamond.Model;

import javafx.beans.property.*;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Conique quelconque (ellipse, parabole ou hyperbole) dont l'axe foxal fait un angle arbitraire avec l'axe X.
 * Dans le cas d'une hyperbole (excentricité e > 1), seule la branche la plus proche du foyer est prise en compte.
 * La branche pour laquelle r(theta) est négatif est ignorée.
 */
public class Conique implements Obstacle, Identifiable, Nommable,ElementAvecContour,ElementAvecMatiere,ObstaclePolaire {

    private final Imp_Identifiable imp_identifiable ;
    private final Imp_Nommable imp_nommable;
    private final Imp_ElementAvecContour imp_elementAvecContour ;
    private final Imp_ElementAvecMatiere imp_elementAvecMatiere ;

    private final ObjectProperty<PositionEtOrientation> position_orientation ;
//    protected final DoubleProperty x_foyer ;
//    protected final DoubleProperty y_foyer ;
//
//    protected final DoubleProperty orientation ;

    private final BooleanProperty appartenance_systeme_optique_centre;
    private BooleanProperty appartenance_composition ;

    @Override
    public Double rayon_polaire(double theta) {

        Point2D foyer = foyer() ;
        double p = parametre.get() ;
        double e = excentricite.get() ;

        double theta_axe_focal = theta_axe_focal() ;

        double den = (1+e*Math.cos(theta-theta_axe_focal)) ;

        if (den < 0)
            return null ;

        Double r ;

        if (den == 0.0)
            r = Double.MAX_VALUE ;
        else
            r = p / (1+e*Math.cos(theta-theta_axe_focal)) ;

        return r ;

    }

    @Override
    public Point2D centre_polaire() {
        return foyer();
    }

    public Point2D foyer() { return position_orientation.get().position() ; }

    public ObjectProperty<PositionEtOrientation> positionEtOrientationObjectProperty() { return position_orientation ;}

    public Point2D axe_focal() {
        return new Point2D(Math.cos(Math.toRadians(position_orientation.get().orientation_deg())),Math.sin(Math.toRadians(position_orientation.get().orientation_deg()))) ;
    }



    protected final DoubleProperty parametre;
    protected final DoubleProperty excentricite;

    private static int compteur_conique = 0 ;

    public Conique(TypeSurface type_surface, double x_foyer, double y_foyer, double orientation_deg, double parametre, double excentricite) throws IllegalArgumentException {
        this(
                new Imp_Identifiable(),
                new Imp_Nommable("Conique "+(++compteur_conique)),
                new Imp_ElementAvecContour(null),
                new Imp_ElementAvecMatiere(type_surface,null ,1.0,null),
                x_foyer,y_foyer,orientation_deg,parametre,excentricite
        ) ;

    }
    public Conique(Imp_Identifiable ii,Imp_Nommable in,Imp_ElementAvecContour iec, Imp_ElementAvecMatiere iem , double x_foyer, double y_foyer, double orientation_deg, double parametre, double excentricite) throws IllegalArgumentException {

        if (parametre <= 0 || excentricite <0)
            throw new IllegalArgumentException("L'excentricité d'une conique doit être positive, et le paramètre strictement positif.");

        imp_identifiable = ii ;
        imp_nommable = in ;
        imp_elementAvecContour = iec ;
        imp_elementAvecMatiere = iem ;

        this.position_orientation = new SimpleObjectProperty<PositionEtOrientation>(new PositionEtOrientation(new Point2D(x_foyer,y_foyer),orientation_deg)) ;

//        this.x_foyer = new SimpleDoubleProperty(x_foyer) ;
//        this.y_foyer = new SimpleDoubleProperty(y_foyer) ;
//
//        this.orientation = new SimpleDoubleProperty(orientation_deg) ;
//
        this.parametre = new SimpleDoubleProperty(parametre) ;
        this.excentricite = new SimpleDoubleProperty(excentricite) ;

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
        return (direction.dotProduct(normale(pt_sur_surface))<=0d?parametre.get():-parametre.get()) *(typeSurface()==TypeSurface.CONVEXE?1d:-1d);
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

    public void definirParametre(double p) {
        this.parametre.set(p);
    }

    public void definirExcentricite(double e) {
        this.excentricite.set(e);
    }

    public void definirAxeFocal(Point2D axe_f) {
        double angle_deg = axe_f.angle(new Point2D(1.0,0.0)) ;

        if (axe_f.getY()>=0)
            position_orientation.set(new PositionEtOrientation(foyer(),angle_deg));
//            orientation.set(angle_deg);
        else
            position_orientation.set(new PositionEtOrientation(foyer(),360d-angle_deg));
//        orientation.set(360-angle_deg);
    }

    @Override
    public void translater(Point2D vecteur) {
        position_orientation.set(new PositionEtOrientation(foyer().add(vecteur),orientation()));

//        position_orientation.set(new PositionEtOrientation(new Point2D(xFoyer()+ vecteur.getX(),yFoyer()+ vecteur.getY()),orientation()));
//        x_foyer.set(vecteur.getX()+x_foyer.get()) ;
//        y_foyer.set(vecteur.getY()+y_foyer.get()) ;
    }


    @Override
    public void ajouterRappelSurChangementToutePropriete(RappelSurChangement rap) {
        imp_elementAvecContour.ajouterRappelSurChangementToutePropriete(rap);
        imp_elementAvecMatiere.ajouterRappelSurChangementToutePropriete(rap);

        position_orientation.addListener((observable, oldValue, newValue) -> { rap.rappel(); });
//        x_foyer.addListener((observable, oldValue, newValue) -> { rap.rappel(); });
//        y_foyer.addListener((observable, oldValue, newValue) -> { rap.rappel(); });
//        orientation.addListener((observable, oldValue, newValue) -> { rap.rappel(); });
        parametre.addListener((observable, oldValue, newValue) -> { rap.rappel(); });
        excentricite.addListener((observable, oldValue, newValue) -> { rap.rappel(); });
    }

    @Override
    public void ajouterRappelSurChangementTouteProprieteModifiantChemin(RappelSurChangement rap) {
        imp_elementAvecContour.ajouterRappelSurChangementTouteProprieteModifiantChemin(rap);
        imp_elementAvecMatiere.ajouterRappelSurChangementTouteProprieteModifiantChemin(rap);

        position_orientation.addListener((observable, oldValue, newValue) -> { rap.rappel(); });
//        x_foyer.addListener((observable, oldValue, newValue) -> { rap.rappel(); });
//        y_foyer.addListener((observable, oldValue, newValue) -> { rap.rappel(); });
//        orientation.addListener((observable, oldValue, newValue) -> { rap.rappel(); });
        parametre.addListener((observable, oldValue, newValue) -> { rap.rappel(); });
        excentricite.addListener((observable, oldValue, newValue) -> { rap.rappel(); });

    }

    @Override
    public void retaillerPourSourisEn(Point2D pos_souris) {
        // Si on est sur le centre, ne rien faire
//        if (pos_souris.getX()==x_foyer.get() && pos_souris.getY()==y_foyer.get())
        if (pos_souris.equals(foyer()))
            return ;

        // p = e . d (où d est la distance à la directrice de la conique)
        double d = pos_souris.subtract(foyer()).magnitude() ;
        parametre.set(excentricite.get()*d);

        // On ne peut changer l'orientation (l'axe focal) que si la conique n'appartient pas à un SOC
        if (!appartientASystemeOptiqueCentre())
            definirAxeFocal(pos_souris.subtract(foyer())) ;
    }

    @Override
    public Contour positions_poignees() {
        Contour c_poignees = new Contour(4);

        // Distance de la directrice au foyer
        double d = parametre.get()/ excentricite.get() ;

        c_poignees.ajoutePoint(foyer().add(d*Math.cos(theta_axe_focal()), d*Math.sin(theta_axe_focal()))) ;
//        c_poignees.ajoutePoint(x_foyer.get() + d*Math.cos(theta_axe_focal()), y_foyer.get()+d*Math.sin(theta_axe_focal()));

        return c_poignees;
    }

    @Override
    public void accepte(VisiteurEnvironnement v) {
        v.visiteConique(this);
    }

    @Override
    public void accepte(VisiteurElementAvecMatiere v) {
        v.visiteConique(this);
    }

    public double xFoyer() { return position_orientation.get().position().getX() ; }
//    public DoubleProperty xFoyerProperty() { return x_foyer ;}

    public double yFoyer() { return position_orientation.get().position().getY() ; }
//    public DoubleProperty yFoyerProperty() { return y_foyer ;}

    public double  excentricite() { return excentricite.get() ;}
    public DoubleProperty excentriciteProperty() { return excentricite ;}

    public double parametre() { return parametre.get() ;}
    public DoubleProperty parametreProperty() { return parametre ;}

//    public DoubleProperty orientationProperty() { return orientation ;}

    public double theta_axe_focal() {
        return Math.toRadians(position_orientation.get().orientation_deg()) ;
    }

    @Override
    public boolean contient(Point2D pt) {

        Point2D foyer = this.foyer() ;
        double e = this.excentricite.get() ;
        double p = this.parametre.get() ;

        double r_p = pt.subtract(foyer).magnitude() ;

        double theta = Math.toRadians(pt.subtract(foyer).angle(axe_focal())) ;
        double r ;

        double denominateur = 1 +e*Math.cos(theta) ;

        if (denominateur > 0.0)
            r = p / denominateur ;
        else // Cas d'une parabole où pt est sur l'axe focal avec theta = PI /
             // hyperbole où pt est dans le secteur angulaire de la branche ignorée (celle pour laquelle r(theta) < 0)
            r = Double.MAX_VALUE ;

//        boolean dans_conique = (r_p<=r)  ;

        if (typeSurface() == TypeSurface.CONVEXE)
            return Environnement.quasiInferieurOuEgal(r_p,r);
//            return dans_conique || Environnement.quasiEgal(r,r_p);
//            return (r_p <= r) ;
        else
            return Environnement.quasiSuperieurOuEgal(r_p,r);
//            return (!dans_conique) || Environnement.quasiEgal(r,r_p);
//            return (r_p > r) ;

    }

    @Override
    public boolean aSurSaSurface(Point2D pt) {
        Point2D foyer = this.foyer() ;
        double e = this.excentricite.get() ;
        double p = this.parametre.get() ;

        // Distance du point p au foyer
        Point2D vec = pt.subtract(foyer) ;
        double vec_x = vec.getX() ;
        double vec_y = vec.getY() ;
        double r_p_carre = vec_x*vec_x+vec_y*vec_y ;
//        double r_p = pt.subtract(foyer).magnitude() ;

        double theta = Math.toRadians(pt.subtract(foyer).angle(axe_focal())) ;
//        double r ;
        double r_carre ;

        double denominateur = 1 +e*Math.cos(theta) ;

        if (denominateur > 0.0)
//            r = p / denominateur ;
            r_carre = p*p / (denominateur*denominateur) ;
        else // Cas d'une parabole où pt est sur l'axe focal avec theta = PI /
            // hyperbole où pt est dans le secteur angulaire de la branche ignorée (celle pour laquelle r(theta) < 0)
//            r = Double.MAX_VALUE ;
            r_carre = Double.MAX_VALUE ;

        return Environnement.quasiEgal(r_carre,r_p_carre) ;
    }

    @Override
    public Point2D normale(Point2D pt) throws Exception {
        Point2D foyer = this.foyer() ;
        Point2D axe_focal = this.axe_focal() ;
        double p = this.parametre.get() ;
        double e = this.excentricite.get() ;

        Point2D r_f1 = pt.subtract(foyer) ;

        Point2D foyer2 ;

        if (e!=1.0) // Ellipse ou Hyperbole
            foyer2 = foyer.subtract( axe_focal.normalize().multiply(( 2*p*e)/(1-e*e)) ) ;
        else // Parabole
            foyer2 = foyer ;

        Point2D r_f2 = pt.subtract(foyer2) ;

        r_f1 = r_f1.normalize() ;

        if (e!=1) // Ellipse ou hyperbole
            r_f2 = r_f2.normalize() ;
        else // Parabole : c'est comme si le deuxième foyer était à l'infini sur l'axe focal
            r_f2 = axe_focal.normalize() ;

        Point2D norm ;
        if (e<=1.0) // La normale en un point M d'une ellipse est la bissectrice de l'angle intérieur FMF'
            norm = (r_f1.add(r_f2)).normalize() ;
        else // La normale en un point M d'une hyperbole est la bissectrice de l'angle extérieur FMF'
            norm = (r_f1.subtract(r_f2)).normalize() ;

        if (typeSurface() == TypeSurface.CONVEXE)
            return norm ;
        else
            return norm.multiply(-1.0) ;
    }

    // TODO : Écrire une implémentation spécifique  de la méthode cherche_toutes_intersections(Rayon r)
    // plus optimisée que l'implémentation par défaut
    // @Override
    // public ArrayList<Point2D> cherche_toutes_intersections(Rayon r)


    @Override
    public Point2D cherche_intersection(Rayon r, ModeRecherche mode) {

        Point2D foyer = this.foyer() ;
        Point2D axe_focal = this.axe_focal() ;

        double p = this.parametre.get() ;
        double e = this.excentricite.get() ;

        Point2D dir = r.direction().normalize() ;

        double theta_axe_focal = Math.acos(axe_focal.getX()) ;
        if (axe_focal.getY()<0)
            theta_axe_focal = -1.0*theta_axe_focal ;

        // Solutions de l'équation trigonométrique
        double c = (r.depart().getY()-foyer.getY())*dir.getX() - (r.depart().getX()-foyer.getX())*dir.getY() ;
        double a = -c*e*axe_focal.getX() - p*dir.getY() ;
        double b = p*dir.getX() - c*e*axe_focal.getY() ;

        // Pas d'intersection
        if ( Math.abs(c) > Math.sqrt(a*a+b*b) )
            return null ;

        double gamma = Math.acos(a/Math.sqrt(a*a+b*b)) ;

        if (b<0)
            gamma = (-1.0)*gamma ;

        double theta_inter_1 = gamma - Math.acos(c/Math.sqrt(a*a+b*b)) ;
        double theta_inter_2 = gamma + Math.acos(c/Math.sqrt(a*a+b*b)) ;

        double r_inter_1 = p / (1+e*Math.cos(theta_inter_1-theta_axe_focal)) ;
        double r_inter_2 = p / (1+e*Math.cos(theta_inter_2-theta_axe_focal)) ;

        Point2D inter1 = new Point2D(foyer.getX()+r_inter_1*Math.cos(theta_inter_1) , foyer.getY()+r_inter_1*Math.sin(theta_inter_1)) ;
        Point2D inter2 = new Point2D(foyer.getX()+r_inter_2*Math.cos(theta_inter_2) , foyer.getY()+r_inter_2*Math.sin(theta_inter_2)) ;

        Point2D inter = null ;

        // Selection du "bon" point d'intersection en fonction de la direction du rayon :

//+++++++++++++++++++
        // Cas où le point de départ est sur la surface
        if (this.aSurSaSurface(r.depart())) {

//            Point2D p1 = new Point2D(x1,y1) ;
//            Point2D p2 = new Point2D(x2,y2) ;

            double x1 = inter1.getX() ;
            double y1 = inter1.getY() ;
            double x2 = inter2.getX() ;
            double y2 = inter2.getY() ;

            double xdir = r.direction().getX() ;
            double ydir = r.direction().getY() ;

            if (Environnement.quasiEgal(inter1.subtract(r.depart()).magnitude(), 0.0)) {

                // Le point de départ est en (x1,y1) : on retourne donc l'autre intersection
                if ( Environnement.quasiEgal(xdir,0) ) { // Rayon vertical
                    if ( (ydir>0 && y2>y1 ) || (ydir<0 && y2<y1) )
                        return new Point2D(x2,y2) ;
                    else
                        return null ;
                }

                if ( (xdir > 0 && x2 > x1) || (xdir <0 && x2 < x1) )
                    return new Point2D(x2,y2) ;

            } else if (Environnement.quasiEgal(inter2.subtract(r.depart()).magnitude(), 0.0)) {

                // Le point de départ est en (x2,y2) : on retourne donc l'autre intersection
                if ( Environnement.quasiEgal(xdir,0) ) { // Rayon vertical
                    if ( (ydir>0 && y1>y2 ) || (ydir<0 && y1<y2) )
                        return new Point2D(x1,y1) ;
                    else
                        return null ;
                }

                if ( (xdir > 0 && x1 > x2) || (xdir <0 && x1 < x2) )
                    return new Point2D(x1,y1) ;

            }
            // Ligne ci-dessous supprimée car il arrive que cette exception se déclenche (pour des rayons dans la masse de la conique ??)
//            else throw new IllegalStateException("Oups. Je ne devrais pas être ici") ;

            return null ;
        }
//-----------------------------

        if ( ( typeSurface() == TypeSurface.CONCAVE && !this.contient(r.depart()) ) || ( typeSurface() == TypeSurface.CONVEXE && this.contient(r.depart()) ) ) {
            // Point de départ du rayon est dans la zone limitée par la conique : il n'y a qu'une intersection (au plus) dans cette
            // configuration : la première intersection est donc aussi la dernière

            if ( Environnement.quasiEgal(dir.getX(),0) && dir.getY()>0) { // Direction du rayon = vertical vers le haut => intersection = point le plus haut
                if (( r_inter_2 < 0 || inter1.getY() > inter2.getY()) && r_inter_1 >0)
                    inter = new Point2D(inter1.getX(), inter1.getY());
                else if (r_inter_2 >0)
                    inter = new Point2D(inter2.getX(), inter2.getY());
            } else if (Environnement.quasiEgal(dir.getX(),0) && dir.getY()<0) { // Direction du rayon = vertical vers le bas => intersection = point le plus bas
                if (( r_inter_2 < 0 || inter1.getY() < inter2.getY()) && r_inter_1 >0)
                    inter = new Point2D(inter1.getX(), inter1.getY());
                else if (r_inter_2 >0)
                    inter = new Point2D(inter2.getX(), inter2.getY());
            } else if (dir.getX() > 0) { // Direction du rayon = vers la droite => intersection = point le plus à droite
                if (( r_inter_2 < 0 || inter1.getX() > inter2.getX()) && r_inter_1 >0)
                    inter = new Point2D(inter1.getX(), inter1.getY());
                else if (r_inter_2 >0)
                    inter = new Point2D(inter2.getX(), inter2.getY());
            } else { // Direction du rayon = vers la gauche => intersection = point le plus à gauche
                if (( r_inter_2 < 0 || inter1.getX() < inter2.getX()) && r_inter_1 >0)
                    inter = new Point2D(inter1.getX(), inter1.getY());
                else if (r_inter_2 >0)
                    inter = new Point2D(inter2.getX(), inter2.getY());
            }
        } else { // Le point de départ du rayon est hors de la zone limitée par la conique

            if (Environnement.quasiEgal(dir.getX(),0) && dir.getY()>0) { // Direction du rayon = vertical vers le haut => premiere intersection = point le plus bas

                if (r_inter_1 >0 && r_inter_2 >0)
                    inter = (mode == ModeRecherche.PREMIERE) ? point_le_plus_bas(inter1, inter2) : point_le_plus_haut(inter1, inter2) ;
                else if (r_inter_1 >0 || r_inter_2 >0)
                    inter = (r_inter_1>0) ? inter1 : inter2 ;

            } else if (Environnement.quasiEgal(dir.getX(),0) && dir.getY()<0) { // Direction du rayon = vertical vers le bas => premiere intersection = point le plus haut

                if (r_inter_1 >0 && r_inter_2 >0)
                    inter = (mode == ModeRecherche.PREMIERE) ? point_le_plus_haut(inter1, inter2) : point_le_plus_bas(inter1, inter2) ;
                else if (r_inter_1 >0 || r_inter_2 >0)
                    inter = (r_inter_1>0) ? inter1 : inter2 ;

            } else if (dir.getX() > 0) { // Direction du rayon = vers la droite => premiere intersection = point le plus à gauche

                if (r_inter_1 >0 && r_inter_2 >0)
                    inter = (mode == ModeRecherche.PREMIERE) ? point_le_plus_a_gauche(inter1, inter2) : point_le_plus_a_droite(inter1, inter2) ;
                else if (r_inter_1 >0 || r_inter_2 >0)
                    inter = (r_inter_1>0) ? inter1 : inter2 ;

            } else { // Direction du rayon = vers la gauche => premiere intersection = point le plus à droite

                if (r_inter_1 >0 && r_inter_2 >0)
                    inter = (mode == ModeRecherche.PREMIERE) ? point_le_plus_a_droite(inter1, inter2) : point_le_plus_a_gauche(inter1, inter2) ;
                else if (r_inter_1 >0 || r_inter_2 >0)
                    inter = (r_inter_1>0) ? inter1 : inter2 ;

            }
        }

        // Cas particulier de la surface CONCAVE avec point de départ sur la surface
//        if ( /* (e<1) && */  (typeSurface() == TypeSurface.CONCAVE && this.aSurSaSurface(r.depart)) )  {
//            inter = null ;
//            if (dir.getX() == 0 && dir.getY()>0) { // Direction du rayon = vertical vers le haut => premiere intersection = point le plus haut
//
//                if (r_inter_1 >0 && r_inter_2 >0)
//                    inter = (mode == ModeRecherche.PREMIERE) ? point_le_plus_haut(inter1, inter2) : point_le_plus_bas(inter1, inter2) ;
//                else if (r_inter_1 >0 || r_inter_2 >0)
//                    inter = (r_inter_1>0) ? inter1 : inter2 ;
//
//            } else if (dir.getX() == 0 && dir.getY()<0) { // Direction du rayon = vertical vers le bas => premiere intersection = point le plus bas
//
//                if (r_inter_1 >0 && r_inter_2 >0)
//                    inter = (mode == ModeRecherche.PREMIERE) ? point_le_plus_bas(inter1, inter2) : point_le_plus_haut(inter1, inter2) ;
//                else if (r_inter_1 >0 || r_inter_2 >0)
//                    inter = (r_inter_1>0) ? inter1 : inter2 ;
//
//            } else if (dir.getX() > 0) { // Direction du rayon = vers la droite => premiere intersection = point le plus à droite
//
//                if (r_inter_1 >0 && r_inter_2 >0)
//                    inter = (mode == ModeRecherche.PREMIERE) ? point_le_plus_a_droite(inter1, inter2) : point_le_plus_a_gauche(inter1, inter2) ;
//                else if (r_inter_1 >0 || r_inter_2 >0)
//                    inter = (r_inter_1>0) ? inter1 : inter2 ;
//
//            } else { // Direction du rayon = vers la gauche => premiere intersection = point le plus à gauche
//
//                if (r_inter_1 >0 && r_inter_2 >0)
//                    inter = (mode == ModeRecherche.PREMIERE) ? point_le_plus_a_gauche(inter1, inter2) : point_le_plus_a_droite(inter1, inter2) ;
//                else if (r_inter_1 >0 || r_inter_2 >0)
//                    inter = (r_inter_1>0) ? inter1 : inter2 ;
//
//            }
//
//            // Si la seule intersection possible était le point de départ qui était sur la surface, c'est qu'il n'y a pas d'intersection
//            if (inter!=null && Environnement.quasiEgalite(r.depart.subtract(inter).magnitude(),0.0 ))
//                return null ;
//        }

        if (typeSurface() == TypeSurface.CONVEXE && this.aSurSaSurface(r.depart()))
            return null ;

        if (inter == null)
            return null ;

        // Vérifier si le point d'intersection trouvé est du bon côté du départ du rayon (i.e. dans la direction du rayon)
        if (    (dir.getX()==0 && ( (dir.getY() >0 && r.depart().getY() > inter.getY()) || (dir.getY()<0 && r.depart().getY() < inter.getY())))
                || (dir.getX()>0 && r.depart().getX()  > inter.getX())
                || (dir.getX() <0 && r.depart().getX() < inter.getX()))
            return null ;


        return inter ;
    }

    static protected Point2D point_le_plus_haut(Point2D p1, Point2D p2) {
        return (p1.getY() > p2.getY())?p1:p2 ;
    }

    static protected Point2D point_le_plus_bas(Point2D p1, Point2D p2) {
        return (p1.getY() < p2.getY())?p1:p2 ;
    }

    static protected Point2D point_le_plus_a_gauche(Point2D p1, Point2D p2) {
        return (p1.getX() < p2.getX())?p1:p2 ;
    }

    static protected Point2D point_le_plus_a_droite(Point2D p1, Point2D p2) {
        return (p1.getX() > p2.getX())?p1:p2 ;
    }

    /**
     * Calcule la ou les éventuelles intersections d'une conique avec une verticale
     * @param x_verticale : abscisse de la verticale
     * @param ymin : valeur minimale du y solution
     * @param ymax : valeur maximale du y solution
     * @param y_sol_croissant : true si les solutions sont attendues par ordre x croissant, false si c'est l'ordre
     *                        décroissant qui est attendu.
     * @return tableau contenant 0, 1 ou 2 solution composée(s) du y et du theta de la solution, ordonnées par y croissant
     * NB : si une intersection se trouve à l'une des extrémités de la verticale elle n'est pas retournée car on la trouvera
     *      comme intersection sur l'horizontale
     *
     */
    @Override
    public double[][]  intersections_verticale(double x_verticale, double ymin, double ymax, boolean y_sol_croissant) {
        Point2D foyer = this.foyer() ;
        double p = this.parametre.get() ;
        double e = this.excentricite.get() ;

        double a,b,c,d, theta_perp ;

        if (x_verticale - foyer.getX() >=0) {
            d = (x_verticale - foyer.getX());
            theta_perp = 0 ;
        }
        else {
            d = (foyer.getX() - x_verticale);
            theta_perp = Math.PI ;
        }

        // Calcul des valeurs de theta des solutions (si il y en a)
        double[] sol = solutions_eq_trigo(p*Math.cos(theta_perp) - e*d*Math.cos(theta_axe_focal()) , p*Math.sin(theta_perp)-e*d*Math.sin(theta_axe_focal()),d) ;

        // Pas d'intersection
        if (sol.length == 0)
            return new double[0][0] ;

        // 1 intersection
        if (sol.length == 1) {
            double r_inter = p / (1+e*Math.cos(sol[0]-theta_axe_focal())) ;

            double y_solution = foyer.getY()+r_inter*Math.sin(sol[0]) ;

            // Inégalités strictes pour ne pas prendre les extrémités
            if (ymin < y_solution && y_solution < ymax && r_inter>0) {
                double[][] y_solutions = new double[1][2];
                y_solutions[0][0] = y_solution ;
                y_solutions[0][1] = sol[0] ;
                return y_solutions;
            }

            return new double[0][0] ;
        }

        // 2 intersections
        if (sol.length == 2) {
            double r_inter_1 = p / (1+e*Math.cos(sol[0]-theta_axe_focal())) ;
            double r_inter_2 = p / (1+e*Math.cos(sol[1]-theta_axe_focal())) ;

            double y_solution_1 = foyer.getY()+r_inter_1*Math.sin(sol[0]) ;
            double y_solution_2 = foyer.getY()+r_inter_2*Math.sin(sol[1]) ;

            if (ymin < y_solution_1 && y_solution_1 < ymax && ymin < y_solution_2 && y_solution_2 < ymax && r_inter_1 > 0 && r_inter_2 > 0) {
                double[][] y_solutions = new double[2][2];

                if ( (y_sol_croissant && y_solution_1<y_solution_2) || (!y_sol_croissant && y_solution_1>y_solution_2) ) {
                    y_solutions[0][0] = y_solution_1;
                    y_solutions[0][1] = sol[0];
                    y_solutions[1][0] = y_solution_2;
                    y_solutions[1][1] = sol[1];
                } else {
                    y_solutions[0][0] = y_solution_2;
                    y_solutions[0][1] = sol[1];
                    y_solutions[1][0] = y_solution_1;
                    y_solutions[1][1] = sol[0];
                }
                return y_solutions;
            }

            if (ymin < y_solution_1 && y_solution_1 < ymax && r_inter_1 > 0) {
                double[][] y_solutions = new double[1][2];
                y_solutions[0][0] = y_solution_1 ;
                y_solutions[0][1] = sol[0] ;
                return y_solutions;
            }

            if (ymin < y_solution_2 && y_solution_2 < ymax && r_inter_2 > 0) {
                double[][] y_solutions = new double[1][2];
                y_solutions[0][0] = y_solution_2 ;
                y_solutions[0][1] = sol[1] ;
                return y_solutions;
            }

            // Aucune des deux solutions n'est dans la zone visible
            return new double[0][0] ;
        }

        System.err.println("Je ne devrais pas être ici");

        return new double[0][0] ;

    }

    /**
     * Calcule la ou les éventuelles intersections d'une conique avec une horizontale
     * @param y_horizontale : abscisse de l'horizontale
     * @param xmin : valeur minimale du x solution
     * @param xmax : valeur maximale du x solution
     * @param x_sol_croissant : true si les solutions sont attendues par ordre de x croissant, false si c'est l'ordre
     *                        décroissant qui est attendu.
     * @return tableau contenant 0, 1 ou 2 solution(s) composée(s) du x et du theta de la solution
     */
    @Override
    public double[][] intersections_horizontale(double y_horizontale, double xmin, double xmax,boolean x_sol_croissant) {
        double a,b,c,d, theta_perp ;
        Point2D foyer = this.foyer() ;
        double p = this.parametre.get() ;
        double e = this.excentricite.get() ;

        if (y_horizontale - foyer.getY() >=0) {
            d = (y_horizontale - foyer.getY());
            theta_perp = Math.PI/2 ;
        }
        else {
            d = (foyer.getY() - y_horizontale);
            theta_perp = -Math.PI/2 ;
        }

        double[] sol = solutions_eq_trigo(p*Math.cos(theta_perp) - e*d*Math.cos(theta_axe_focal()) , p*Math.sin(theta_perp)-e*d*Math.sin(theta_axe_focal()),d) ;

        // Pas d'intersection
        if (sol.length == 0)
            return new double[0][0] ;

        // 1 intersection
        if (sol.length == 1) {
            double r_inter = p / (1+e*Math.cos(sol[0]-theta_axe_focal())) ;

            double x_solution = foyer.getX()+r_inter*Math.cos(sol[0]) ;

            if (xmin <= x_solution && x_solution <= xmax && r_inter>0) {
                double[][] x_solutions = new double[1][2];
                x_solutions[0][0] = x_solution ;
                x_solutions[0][1] = sol[0] ;
                return x_solutions;
            }

            return new double[0][0] ;
        }

        // 2 intersections
        if (sol.length == 2) {
            double r_inter_1 = p / (1+e*Math.cos(sol[0]-theta_axe_focal())) ;
            double r_inter_2 = p / (1+e*Math.cos(sol[1]-theta_axe_focal())) ;

            double x_solution_1 = foyer.getX()+r_inter_1*Math.cos(sol[0]) ;
            double x_solution_2 = foyer.getX()+r_inter_2*Math.cos(sol[1]) ;

            if ( xmin <= x_solution_1 && x_solution_1 <= xmax && xmin <= x_solution_2 && x_solution_2 <= xmax && r_inter_1 > 0 && r_inter_2>0) {
                double[][] x_solutions = new double[2][2];

                if ( (x_sol_croissant && x_solution_1<x_solution_2) || (!x_sol_croissant && x_solution_1>x_solution_2) ) {
                    x_solutions[0][0] = x_solution_1;
                    x_solutions[0][1] = sol[0];
                    x_solutions[1][0] = x_solution_2;
                    x_solutions[1][1] = sol[1];
                } else {
                    x_solutions[0][0] = x_solution_2;
                    x_solutions[0][1] = sol[1];
                    x_solutions[1][0] = x_solution_1;
                    x_solutions[1][1] = sol[0];
                }

                return x_solutions;
            }

            if (xmin <= x_solution_1 && x_solution_1 <= xmax && r_inter_1 > 0) {
                double[][] x_solutions = new double[1][2];
                x_solutions[0][0] = x_solution_1 ;
                x_solutions[0][1] = sol[0] ;
                return x_solutions;
            }

            if (xmin <= x_solution_2 && x_solution_2 <= xmax && r_inter_2 > 0) {
                double[][] x_solutions = new double[1][2];
                x_solutions[0][0] = x_solution_2 ;
                x_solutions[0][1] = sol[1] ;
                return x_solutions;
            }

            // Aucune des deux solutions n'est dans la zone visible
            return new double[0][0] ;

        }

        System.err.println("Je ne devrais pas être ici");

        return new double[0][0] ;
    }


        // Solutions d'une équation trigonométrique a*cos(x)+b*sin(x) = c
    protected static double[] solutions_eq_trigo(double a, double  b, double c) {

        if (a==0.0 && b==0.0)
            return new double[0] ;

        double module = Math.sqrt(a*a+b*b) ;

        if ( Math.abs(c) > module )
            return new double[0] ;

        double gamma = Math.acos(a/module) ;

        if (b<0)
            gamma = (-1.0)*gamma ;

        if (c==module) {
            double[] res = new double [1] ;
            res[0] = gamma ;

            return res ;
        }

        double theta_inter_1 = gamma - Math.acos(c/module) ;
        double theta_inter_2 = gamma + Math.acos(c/module) ;

        double[] res = new double [2] ;

        res[0] = gamma - Math.acos(c/module) ;
        res[1] = gamma + Math.acos(c/module) ;

        return res ;

    }

    public Point2D point_sur_conique(double theta) {

        Point2D foyer = foyer() ;
        double p = parametre.get() ;
        double e = excentricite.get() ;

        double theta_axe_focal = theta_axe_focal() ;

        double den = (1+e*Math.cos(theta-theta_axe_focal)) ;

        if (den < 0)
            return null ;

        double r ;

        if (den == 0.0)
            r = Double.MAX_VALUE ;
        else
            r = p / (1+e*Math.cos(theta-theta_axe_focal)) ;

        return new Point2D(foyer.getX()+r*Math.cos(theta), foyer.getY()+r*Math.sin(theta)) ;

    }

//    protected Contour arc_de_conique(double theta_debut,double theta_fin, int nombre_pas_angulaire_par_arc) {
//
//        Contour c = new Contour(nombre_pas_angulaire_par_arc) ;
//
//        double pas = (theta_fin-theta_debut) / nombre_pas_angulaire_par_arc ;
//
//        double theta = theta_debut ;
//
//        Point2D pt;
//        do {
//            pt = point_sur_conique(theta);
//
//            c.ajoutePoint(pt.getX(),pt.getY());
//
//            theta += pas;
//        } while (theta < theta_fin);
//
//        // Point final pour theta_fin, pour rattraper les erreurs d'arrondi
//        pt = point_sur_conique(theta_fin);
//
//        if (pt != null)
//            c.ajoutePoint(pt.getX(),pt.getY());
//
//        return c ;
//
//    }


    protected ArrayList<Double> xpoints_sur_conique(double theta_debut, double theta_fin, int nombre_pas_angulaire_par_arc) {
        ArrayList<Double> xpoints_conique = new ArrayList<Double>() ;

        double pas = (theta_fin-theta_debut) / nombre_pas_angulaire_par_arc ;

        double theta = theta_debut ;

        Point2D pt;
        do {
            pt = point_sur_conique(theta);

            if (pt != null)
                xpoints_conique.add(pt.getX());

            theta += pas;
        } while (theta <= theta_fin);

        // Point final pour theta_fin, pour rattraper les erreurs d'arrondi
        pt = point_sur_conique(theta_fin);
        if (pt != null)
            xpoints_conique.add(pt.getX());

        return xpoints_conique ;
    }

    protected ArrayList<Double> ypoints_sur_conique(double theta_debut,double theta_fin, int nombre_pas_angulaire_par_arc) {
        ArrayList<Double> ypoints_conique = new ArrayList<Double>() ;

        double pas = (theta_fin-theta_debut) / nombre_pas_angulaire_par_arc ;

        double theta = theta_debut ;

        Point2D pt;
        do {
            pt = point_sur_conique(theta);

            if (pt != null)
                ypoints_conique.add(pt.getY());

            theta += pas;
        } while (theta <= theta_fin);

        // Point final pour theta_fin, pour rattraper les erreurs d'arrondi
        pt = point_sur_conique(theta_fin);
        if (pt != null)
            ypoints_conique.add(pt.getY());

        return ypoints_conique ;
    }

//    public ContoursObstacle couper_old(BoiteLimites boite, int nombre_pas_angulaire_par_arc) {
//
//        ContoursObstacle contours = new ContoursObstacle() ;
//
////        double e = excentricite.get() ;
//
//        double xmin = boite.getMinX() ;
//        double xmax = boite.getMaxX() ;
//        double ymin = boite.getMinY() ;
//        double ymax = boite.getMaxY() ;
//
//        double[][] i_droites = intersections_verticale(xmax, ymin, ymax,true) ;
//        double[][] i_hautes  = intersections_horizontale(ymax, xmin, xmax,false) ;
//        double[][] i_gauches = intersections_verticale(xmin, ymin, ymax,false) ;
//        double[][] i_basses  = intersections_horizontale(ymin, xmin, xmax,true) ;
//
//        SelecteurCoins sc = new SelecteurCoins(xmin, ymin, xmax, ymax);
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
//        // Si aucune intersection, --ou si 1 seule intersection (TODO : tester le cas à 1 intersection avec une ellipse)
//        if (n_intersections<=1) {
//
//            // Ellipse entièrement contenue dans la zone visible ?
////            if (e<1.0 && boite.contains(point_sur_conique(0))) {
//            if (boite.contains(point_sur_conique(0))) {
////                ArrayList<Double> x_arc = xpoints_sur_conique(0,2*Math.PI, nombre_pas_angulaire_par_arc) ;
////                ArrayList<Double> y_arc = ypoints_sur_conique(0,2*Math.PI, nombre_pas_angulaire_par_arc) ;
//
//                // Rappel : on est par défaut en FillRule NON_ZERO => pour faire une surface avec un trou, il suffit
//                // de faire deux contours dans des sens contraires (trigo et antitrigo)
////                cae.gc.beginPath();
//
//                // Tracé du contour, ou du trou (chemin fermé), dans le sens trigo
////                cae.completerPathAvecContourFerme(x_arc,y_arc);
//
//                Contour arc = arc_de_conique(0, 2 * Math.PI, nombre_pas_angulaire_par_arc) ;
//
//                contours.ajouterContourSurface(arc);
//
//                // Tracé du contour (apparemment, cela ne termine pas le path, on peut continuer à lui ajouter des éléments
////                cae.gc.stroke();
//
//                if (typeSurface() == Obstacle.TypeSurface.CONCAVE) {
//                    // Tracé du rectangle de la zone visible, dans le sens antitrigo : le Path de l'ellipse sera un trou
//                    // dans cette zone
//                    contours.ajouterContourMasse(boite.construireContourAntitrigo());
//
////                    cae.completerPathAvecContourZoneVisibleAntitrigo();
//                }
//
//                Contour arc_masse = new Contour(arc) ;
//                contours.ajouterContourMasse(arc_masse);
//
//                // Le fill déclenche aussi l'appel closePath
////                cae.gc.fill();
//
//            }
//            else { // Aucun point de la surface n'est dans la zone visible
//                if (contient(boite.centre())) {
//
////                    sc.selectionne_tous();
//
//                    // Toute la zone visible est dans la masse de l'objet conique
//                    contours.ajouterContourMasse(boite.construireContour());
////                    CanvasAffichageEnvironnement.remplirPolygone(cae, sc.xcoins_selectionne(true), sc.ycoins_selectionne(true));
//                } else {
//                    // Toute la zone visible est hors de la masse de la conique
//                    // rien à faire
//                }
//            }
//
//            // C'est fini
//            return contours ;
//        }
//
//        // Au moins 2 intersections, et jusqu'à 8...
//
////        ArrayList<Double> x_masse = new ArrayList<Double>(nombre_pas_angulaire_par_arc+4) ;
////        ArrayList<Double> y_masse = new ArrayList<Double>(nombre_pas_angulaire_par_arc+4) ;
//
//        Contour arc_masse = new Contour() ;
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
//            Point2D pt = point_sur_conique((theta_deb+theta_fin)/2 ) ;
//
//            // Si cet arc est visible
//            if (pt!=null && boite.contains(pt)) {
////                ArrayList<Double> x_arc = new ArrayList<Double>(nombre_pas_angulaire_par_arc) ;
////                ArrayList<Double> y_arc = new ArrayList<Double>(nombre_pas_angulaire_par_arc) ;
////
////                // Ajouter le point exact de l'intersection pt_deb pour éviter les décrochages dûs au pas du tracé
////                x_arc.add(x_deb) ;
////                y_arc.add(y_deb) ;
////
////                x_arc.addAll(xpoints_sur_conique(theta_deb,theta_fin, nombre_pas_angulaire_par_arc)) ;
////                y_arc.addAll(ypoints_sur_conique(theta_deb,theta_fin, nombre_pas_angulaire_par_arc)) ;
////
////                // Ajouter le point exact de l'intersection pt_fin pour éviter les décrochages dûs au pas du tracé
////                x_arc.add(x_fin) ;
////                y_arc.add(y_fin) ;
//
//                Contour arc = arc_de_conique(theta_deb,theta_fin,nombre_pas_angulaire_par_arc) ;
//
//                // Ajouter les points d'intersection exacts pour un tracé précis
//                arc.ajoutePointDevant(x_deb,y_deb);
//                arc.ajoutePoint(x_fin,y_fin);
//
//                // Cet arc est à la fois un morceau de la surface de l'obstacle et un morceau du contour de la masse
//                contours.ajouterContourSurface(arc);
//
//                arc_masse.concatene(arc);
//
////                // On trace l'arc de ce contour visible
////                cae.tracerPolyligne(x_arc,y_arc);
////
////                x_masse.addAll(x_arc) ;
////                y_masse.addAll(y_arc) ;
////
////                x_arc.clear();
////                y_arc.clear();
//
//                // Si les 2 intersections sont sur un même bord et que leur milieu est dans la conique, il n'y a pas
//                // d'autre arc de contour à tracer, on peut sortir tout de suite de la boucle sur les intersections
//                if ( (x_deb==x_fin || y_deb==y_fin) && contient(pt_deb.midpoint(pt_fin)))
//                    break ;
//
//                // Sinon, chercher les coins contigus (càd non séparés des extrémités par une intersection) et qui sont
//                // dans l'interieur du contour, que la conique soit convexe ou concave
//                SelecteurCoins sc_coins_interieurs = sc.sequence_coins_continus(false,pt_deb,pt_fin,valeurs_x_intersection,valeurs_y_intersection) ;
//
//                if(     ( typeSurface()== Obstacle.TypeSurface.CONVEXE
//                        && contient(sc_coins_interieurs.coin(sc_coins_interieurs.coin_depart)) )
//                        || ( typeSurface()== Obstacle.TypeSurface.CONCAVE
//                        && !contient(sc_coins_interieurs.coin(sc_coins_interieurs.coin_depart)) )
//                ) {
//                    // Les ajouter au tracé du contour de masse
//                    arc_masse.concatene(sc_coins_interieurs.coins_selectionne_antitrigo(true));
//
////                    x_masse.addAll(sc_coins_interieurs.xcoins_selectionne_antitrigo(true));
////                    y_masse.addAll(sc_coins_interieurs.ycoins_selectionne_antitrigo(true));
//
//                    break ;
//                }
//
//            } else { // Arc non visible
//
//                // Ajouter la sequence des coins de cette portion (dans ordre trigo) si ils sont dans la conique (et si il y en a)
//                SelecteurCoins sc_coins_interieurs = sc.sequence_coins_continus(true,pt_deb,pt_fin,valeurs_x_intersection,valeurs_y_intersection) ;
//                if(  ( typeSurface()== Obstacle.TypeSurface.CONVEXE
//                        && contient(sc_coins_interieurs.coin(sc_coins_interieurs.coin_depart)) )
//                        || ( typeSurface()== Obstacle.TypeSurface.CONCAVE
//                        && !contient(sc_coins_interieurs.coin(sc_coins_interieurs.coin_depart)) )
//                ) {
//                    // Les ajouter au contour de masse
//                    arc_masse.concatene(sc_coins_interieurs.coins_selectionne(true));
//
////                    x_masse.addAll(sc_coins_interieurs.xcoins_selectionne(true));
////                    y_masse.addAll(sc_coins_interieurs.ycoins_selectionne(true));
//                }
//
//            }
//        } // Fin boucle sur intersections
//
////        cae.gc.beginPath();
//
//        if (typeSurface() == Obstacle.TypeSurface.CONCAVE) {
//            // Tracé du rectangle de la zone visible, dans le sens antitrigo : le Path de l'ellipse sera un trou
//            // dans cette zone
//            contours.ajouterContourMasse(boite.construireContourAntitrigo());
////            cae.gc.moveTo(xmax, ymin);
////            cae.gc.lineTo(xmin, ymin);
////            cae.gc.lineTo(xmin, ymax);
////            cae.gc.lineTo(xmax, ymax);
//        }
//        // Tracé du contour, ou du trou (chemin fermé), dans le sens trigo
////        cae.completerPathAvecContourFerme(x_masse,y_masse);
//
//        contours.ajouterContourMasse(arc_masse);
//
////        cae.gc.fill();
//
//        return contours ;
//
//    }

    @Override
    public boolean aSymetrieDeRevolution() { return true ;}


    @Override
    public Point2D pointSurAxeRevolution() {
        return foyer() ;
    }
    @Override
    public boolean estOrientable() {
        return true ;
    }

    public void definirFoyer(Point2D foyer)  {
        position_orientation.set(new PositionEtOrientation(foyer,orientation()));
//        this.orientation.set(orientation_deg);
    }

    @Override
    public void definirOrientation(double orientation_deg)  {
        position_orientation.set(new PositionEtOrientation(foyer(),orientation_deg));
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
    public void definirAppartenanceSystemeOptiqueCentre(boolean b) {this.appartenance_systeme_optique_centre.set(b);}
    @Override
    public boolean appartientASystemeOptiqueCentre() {return this.appartenance_systeme_optique_centre.get() ;}
    @Override
    public void definirAppartenanceComposition(boolean b) {this.appartenance_composition.set(b);}
    @Override
    public boolean appartientAComposition() {return this.appartenance_composition.get() ;}

    @Override
    public Double rayonDiaphragmeParDefaut() {
        return parametre.get();
    }

    @Override public double rayonDiaphragmeMaximumConseille() { return parametre.get() ; }

    @Override
    public Double abscisseIntersectionSuivanteSurAxe(Point2D origine_axe, Point2D direction_axe, double z_depart, boolean sens_z_croissants, Double z_inter_prec) {

        double z_foyer = foyer().distance(origine_axe)*(foyer().subtract(origine_axe).dotProduct(direction_axe)>=0?1d:-1d) ;

        double p = parametre() ;
        double e = excentricite() ;

        Double z_int_min = z_foyer - p/(1+e) ;
        Double z_int_max = (e<1? z_foyer + p/(1-e) : null) ;

        if (z_inter_prec!=null) {
            if (z_inter_prec.equals(z_int_min))
                return (sens_z_croissants?(p!=0d?z_int_max:null):null) ;
            if (Objects.equals(z_int_max, z_inter_prec))
                return (sens_z_croissants?null:(p!=0d?z_int_min:null)) ;
        }

//        // Cas particuliers où le point de départ est sur une des intersections
//        if (Environnement.quasiEgal(z_depart,z_int_min))
//            return (sens_z_croissants? z_int_max:null) ;
//        if (z_int_max!=null && Environnement.quasiEgal(z_depart,z_int_max))
//            return (sens_z_croissants?null:z_int_min) ;

        if (z_depart==z_int_min) return z_int_min ;
        if (Objects.equals(z_depart,z_int_max)) return z_int_max ;


        // Cas général
        if (z_depart<z_int_min)
            return (sens_z_croissants?z_int_min:null) ;
        else if (z_int_min<z_depart && (z_int_max==null || z_depart<z_int_max))
            return (sens_z_croissants?z_int_max:z_int_min) ; // Ce genre d'expression exige que z_int_min soit de type Double comme z_int_max (sinon exception lorsque z_int_max est null)
        else // z_depart>z_int_max
            return (sens_z_croissants?null:z_int_max) ;

    }

    @Override
    public ArrayList<Double> abscissesToutesIntersectionsSurAxe(Point2D origine_axe, Point2D direction_axe, double z_depart,boolean sens_z_croissants, Double z_inter_prec) {

        ArrayList<Double> resultat = new ArrayList<>(2) ;

        double z_foyer ;

        if (foyer().subtract(origine_axe).dotProduct(direction_axe)>=0)
            z_foyer = foyer().distance(origine_axe) ;
        else
            z_foyer = -foyer().distance(origine_axe) ;

        double p = parametre.get() ;
        double e = excentricite.get() ;

        double z_int_min = z_foyer - p/(1+e) ;
        Double z_int_max = (e<1? z_foyer + p/(1-e) : null) ;

        // S'assurer de ne pas retourner à nouveau l'intersection z_inter_prec
        if (z_inter_prec!=null) {
            if (z_int_min==z_inter_prec) {
                if (sens_z_croissants) resultat.add(z_int_max);
                return resultat ;
            }
            if (Objects.equals(z_inter_prec,z_int_max)) {
                if (!sens_z_croissants) resultat.add(z_int_min);
                return resultat ;
            }
        }

//        // Cas particuliers où le point de départ est sur une des intersections
//        if (Environnement.quasiEgal(z_depart,z_int_min)) {
//            if (sens_z_croissants) resultat.add(z_int_max);
//            return resultat ;
//        }
//        if (Environnement.quasiEgal(z_depart,z_int_max)) {
//            if (!sens_z_croissants) resultat.add(z_int_min);
//            return resultat ;
//        }

        // Cas général
        if (z_depart<z_int_min) {

            if (!sens_z_croissants)
                return resultat ;

            resultat.add(z_int_min) ;
            if (p!=0d) resultat.add(z_int_max) ;

        }        else if (z_int_min<z_depart && z_depart<z_int_max) {
            if (sens_z_croissants)
                resultat.add(z_int_max) ;
            else
                resultat.add(z_int_min) ;

        }
        else // z_depart>z_int_max
        {
            if (sens_z_croissants)
                return resultat ;

            resultat.add(z_int_max) ;
            if (p!=0d) resultat.add(z_int_min) ;

        }

        return resultat ;

    }

    @Override
    public void tournerAutourDe(Point2D centre_rot, double angle_rot_deg) {
        Rotate r = new Rotate(angle_rot_deg,centre_rot.getX(),centre_rot.getY()) ;

        Point2D nouveau_foyer = r.transform(foyer()) ;
//        Point2D nouveau_foyer = r.transform(x_foyer.get(),y_foyer.get()) ;
//
//        x_foyer.set(nouveau_foyer.getX());
//        y_foyer.set(nouveau_foyer.getY());
//
//        orientation.set(orientation.get()+angle_rot_deg);

        position_orientation.set(new PositionEtOrientation(nouveau_foyer,orientation()+angle_rot_deg));
    }
}
