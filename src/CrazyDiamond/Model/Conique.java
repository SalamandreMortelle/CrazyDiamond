package CrazyDiamond.Model;

import javafx.beans.property.*;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * Conique quelconque (ellipse, parabole ou hyperbole) dont l'axe focal fait un angle arbitraire avec l'axe X.
 * Dans le cas d'une hyperbole (excentricité e > 1), seule la branche la plus proche du foyer est prise en compte.
 * La branche pour laquelle r(theta) est négatif est ignorée.
 */
public class Conique extends BaseObstacleAvecContourEtMatiere implements Obstacle, Identifiable, Nommable,ElementAvecContour,ElementAvecMatiere,ObstaclePolaire {

    private final ObjectProperty<PositionEtOrientation> position_orientation ;

    @Override
    public Double rayon_polaire(double theta) {

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
        this(null,type_surface,x_foyer,y_foyer,orientation_deg,parametre,excentricite,null,1.5,null,null) ;
    }

    public Conique(String nom, double x_foyer, double y_foyer, double orientation_deg,double parametre, double excentricite, TypeSurface type_surface) throws IllegalArgumentException {
        this(nom,type_surface,x_foyer,y_foyer,orientation_deg,parametre,excentricite,null,1.5,null,null) ;
    }
    public Conique(String nom, TypeSurface type_surface, double x_foyer, double y_foyer, double orientation_deg,
                   double parametre, double excentricite, NatureMilieu nature_milieu, double indice_refraction, Color couleur_matiere, Color couleur_contour) throws IllegalArgumentException {
        super(nom!=null?nom:"Conique "+(++compteur_conique),
                type_surface,nature_milieu,indice_refraction,couleur_matiere,couleur_contour);

        if (parametre <= 0 || excentricite <0)
            throw new IllegalArgumentException("L'excentricité d'une conique doit être positive, et le paramètre strictement positif.");

        this.position_orientation = new SimpleObjectProperty<>(new PositionEtOrientation(new Point2D(x_foyer,y_foyer),orientation_deg)) ;

        this.parametre = new SimpleDoubleProperty(parametre) ;
        this.excentricite = new SimpleDoubleProperty(excentricite) ;
    }

    public Conique(Imp_Identifiable ii,Imp_Nommable ien,Imp_ElementAvecContour iec, Imp_ElementAvecMatiere iem , double x_foyer, double y_foyer, double orientation_deg, double parametre, double excentricite) throws IllegalArgumentException {
        super(ii,ien,iec,iem);

        if (parametre <= 0 || excentricite <0)
            throw new IllegalArgumentException("L'excentricité d'une conique doit être positive, et le paramètre strictement positif.");

        this.position_orientation = new SimpleObjectProperty<>(new PositionEtOrientation(new Point2D(x_foyer,y_foyer),orientation_deg)) ;

        this.parametre = new SimpleDoubleProperty(parametre) ;
        this.excentricite = new SimpleDoubleProperty(excentricite) ;
    }


    @Override
    public Double courbureRencontreeAuSommet(Point2D pt_sur_surface, Point2D direction) throws Exception {
        return (direction.dotProduct(normale(pt_sur_surface))<=0d?parametre.get():-parametre.get()) *(typeSurface()==TypeSurface.CONVEXE?1d:-1d);
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
        else
            position_orientation.set(new PositionEtOrientation(foyer(),360d-angle_deg));
    }

    @Override
    public void translater(Point2D vecteur) {
        position_orientation.set(new PositionEtOrientation(foyer().add(vecteur),orientation()));
    }
    @Override
    public void translaterParCommande(Point2D vecteur) {
        new CommandeDefinirUnParametrePoint<>(this,foyer().add(vecteur),this::foyer,this::definirFoyer).executer() ;
    }

    @Override
    public void ajouterRappelSurChangementToutePropriete(RappelSurChangement rap) {
        super.ajouterRappelSurChangementToutePropriete(rap);

        position_orientation.addListener((observable, oldValue, newValue) -> rap.rappel());
        parametre.addListener((observable, oldValue, newValue) -> rap.rappel());
        excentricite.addListener((observable, oldValue, newValue) -> rap.rappel());
    }

    @Override
    public void ajouterRappelSurChangementTouteProprieteModifiantChemin(RappelSurChangement rap) {
        super.ajouterRappelSurChangementTouteProprieteModifiantChemin(rap);

        position_orientation.addListener((observable, oldValue, newValue) -> rap.rappel());
        parametre.addListener((observable, oldValue, newValue) -> rap.rappel());
        excentricite.addListener((observable, oldValue, newValue) -> rap.rappel());
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

    public double yFoyer() { return position_orientation.get().position().getY() ; }

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

//        double theta_inter_1 = gamma - Math.acos(c/module) ;
//        double theta_inter_2 = gamma + Math.acos(c/module) ;

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
        ArrayList<Double> xpoints_conique = new ArrayList<>() ;

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
        ArrayList<Double> ypoints_conique = new ArrayList<>() ;

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
    public double orientation() { return position_orientation.get().orientation_deg() ; }

    @Override
    public Double rayonDiaphragmeParDefaut() {
        return parametre.get();
    }

    @Override public double rayonDiaphragmeMaximumConseille() { return parametre.get() ; }

    @Override
    public List<DioptreParaxial> dioptresParaxiaux(PositionEtOrientation axe) {

        double p = parametre.get() ;
        double e = excentricite.get() ;

        if (e<1d && Environnement.quasiEgal(2*p/(1-e*e),0d))
            // Pas de dioptres si le diamètre sur l'axe est quasi nul (évite les dioptres avec un Rcourbure quasi nul)
            return new ArrayList<>(0) ;

        ArrayList<DioptreParaxial> resultat = new ArrayList<>((e<1d?2:1)) ;

        double z_foyer = foyer().subtract(axe.position()).dotProduct(axe.direction()) ;

        Double z_int_min ;
        Double z_int_max ;

        Point2D normale = position_orientation.get().direction() ;
        normale.multiply(typeSurface() == TypeSurface.CONVEXE?1d:-1d) ;

//        if (Math.abs(axe.orientation_deg() - orientation())>90d) { // Cet écart angulaire vaut soit 0 soit 180° (cf. SOC::positionnerObstacle)
        if (normale.dotProduct(axe.direction())<0) {
            z_int_min = z_foyer - p / (1 + e);
            z_int_max = (e < 1d ? z_foyer + p / (1 - e) : null);
        }
        else {
            z_int_min = (e < 1d ? z_foyer - p / (1 - e) : null);
            z_int_max = z_foyer + p / (1 + e);
        }

        DioptreParaxial d_z_min = null ;
        DioptreParaxial d_z_max = null ;

        if (typeSurface()==TypeSurface.CONVEXE) {
            if (z_int_min!=null)
                d_z_min = new DioptreParaxial(z_int_min, parametre(), 0d , indiceRefraction(), this);
            if (z_int_max != null)
                d_z_max = new DioptreParaxial(z_int_max, -parametre(), indiceRefraction(), 0d, this);
        } else {
            if (z_int_min!=null)
                d_z_min = new DioptreParaxial(z_int_min, parametre(), indiceRefraction(),0d, this);
            if (z_int_max != null)
                d_z_max = new DioptreParaxial(z_int_max, -parametre(), 0d,indiceRefraction(), this);
        }

        if (z_int_min!=null)
            resultat.add(d_z_min) ;

        if (z_int_max!=null)
            resultat.add(d_z_max) ;

        return resultat ;

    }

    @Override
    public void tournerAutourDe(Point2D centre_rot, double angle_rot_deg) {
//        Rotate r = new Rotate(angle_rot_deg,centre_rot.getX(),centre_rot.getY()) ;
//
//        Point2D nouveau_foyer = r.transform(foyer()) ;

        // Il faut ramener la nouvelle orientation entre 0 et 360° car les spinners et sliders "orientation" des
        // panneaux contrôleurs imposent ces limites via leurs min/max
//        double nouvelle_or = (orientation()+angle_rot_deg)%360 ;
//        if (nouvelle_or<0) nouvelle_or+=360 ;
//
//        position_orientation.set(new PositionEtOrientation(nouveau_foyer,Obstacle.nouvelleOrientationApresRotation(orientation(),angle_rot_deg)));
//        position_orientation.set(new PositionEtOrientation(nouveau_foyer,orientation()+angle_rot_deg));
        position_orientation.set(Obstacle.nouvellePositionEtOrientationApresRotation(position_orientation.get(),centre_rot,angle_rot_deg)) ;
    }

    @Override
    public void convertirDistances(double facteur_conversion) {
        position_orientation.set(new PositionEtOrientation(foyer().multiply(facteur_conversion),orientation()));
        parametre.set(parametre()*facteur_conversion);
    }

}
