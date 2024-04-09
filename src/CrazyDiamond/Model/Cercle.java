package CrazyDiamond.Model;

import javafx.beans.property.*;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;

import java.util.ArrayList;
import java.util.List;

public class Cercle extends BaseObstacleAvecContourEtMatiere implements Obstacle, Identifiable, Nommable,ElementAvecContour,ElementAvecMatiere,ObstaclePolaire  {

    private final ObjectProperty<Point2D> centre ;

    protected DoubleProperty rayon;

    private static int compteur_cercle = 0 ;

    public static void razCompteur() { compteur_cercle = 0 ; }

    public Cercle(TypeSurface type_surface, double xcentre, double ycentre, double rayon) throws IllegalArgumentException {
        this(null,xcentre,ycentre,rayon,type_surface);
    }

    public Cercle(String nom, double xcentre, double ycentre, double rayon, TypeSurface type_surface) throws IllegalArgumentException {
        this(nom,type_surface,xcentre,ycentre,rayon,null,1.5,null,null) ;
    }

    public Cercle(String nom, TypeSurface type_surface, double xcentre, double ycentre, double rayon, NatureMilieu nature_milieu, double indice_refraction, Color couleur_matiere, Color couleur_contour) throws IllegalArgumentException {
        super(nom!=null?nom:"Cercle "+(++compteur_cercle),
                type_surface,nature_milieu,indice_refraction,couleur_matiere,couleur_contour);

        if (rayon <= 0)
            throw new IllegalArgumentException("Le rayon doit être positif.");

        this.centre = new SimpleObjectProperty<>(new Point2D(xcentre,ycentre)) ;
        this.rayon = new SimpleDoubleProperty(rayon) ;

    }


    // Constructeur utilisé par le Deserializer
    public Cercle(Imp_Identifiable ii, Imp_Nommable iei, Imp_ElementAvecContour iac, Imp_ElementAvecMatiere iam, double xcentre, double ycentre, double rayon) throws IllegalArgumentException {
        super(ii,iei,iac,iam);

        if (rayon <= 0)
            throw new IllegalArgumentException("Le rayon doit être positif.");

        this.centre = new SimpleObjectProperty<>(new Point2D(xcentre,ycentre)) ;
        this.rayon = new SimpleDoubleProperty(rayon) ;
    }

    @Override
    public Double courbureRencontreeAuSommet(Point2D pt_sur_surface, Point2D direction) throws Exception {
        return (direction.dotProduct(normale(pt_sur_surface))<=0d?rayon():-rayon())*(typeSurface()==TypeSurface.CONVEXE?1d:-1d) ;
    }

    public void definirCentre(Point2D c) {
        centre.set(c) ;
    }
    public void definirCentre(double xc,double yc) {
        centre.set(new Point2D(xc,yc)) ;
    }

    public void definirRayon(double r) {
        this.rayon.set(r);
    }

    public double xCentre() { return centre().getX(); }
    public double yCentre() { return centre().getY(); }

    public double rayon() { return rayon.get(); }

    @Override
    public void translater(Point2D vecteur) {
          definirCentre(centre().add(vecteur)) ;
    }

    @Override
    public void translaterParCommande(Point2D vecteur) {
        new CommandeDefinirUnParametrePoint<>(this,centre().add(vecteur),this::centre,this::definirCentre).executer() ;
    }


    @Override
    public Contour positions_poignees() {
        Contour c_poignees = new Contour(4) ;

        c_poignees.ajoutePoint(centre().add(rayon(),0));
        c_poignees.ajoutePoint(centre().add(0,rayon()));
        c_poignees.ajoutePoint(centre().add(-rayon(),0));
        c_poignees.ajoutePoint(centre().add(0,-rayon()));

        return c_poignees ;
    }

    public DoubleProperty rayonProperty() { return rayon ; }

    @Override
    public void accepte(VisiteurElementAvecMatiere v) {
        v.visiteCercle(this);
    }

    @Override
    public void accepte(VisiteurEnvironnement v) {
        v.visiteCercle(this);
    }

    public ObjectProperty<Point2D> centreProperty() {
        return centre ;
    }
    public Point2D centre() {
        return centre.get() ;
    }

    @Override
    public void ajouterRappelSurChangementToutePropriete(RappelSurChangement rap) {
        super.ajouterRappelSurChangementToutePropriete(rap);

        centre.addListener((observable, oldValue, newValue) -> rap.rappel());
        rayon.addListener((observable, oldValue, newValue) -> rap.rappel());

    }

    @Override
    public void ajouterRappelSurChangementTouteProprieteModifiantChemin(RappelSurChangement rap) {
        super.ajouterRappelSurChangementTouteProprieteModifiantChemin(rap);

        centre.addListener((observable, oldValue, newValue) -> rap.rappel());
        rayon.addListener((observable, oldValue, newValue) -> rap.rappel());
    }

    @Override
    public void retaillerPourSourisEn(Point2D pos_souris) {
        // Si on est sur le centre, ne rien faire
        if (pos_souris.equals(centre()))
            return ;

        definirRayon(pos_souris.subtract(centre()).magnitude());
    }

    @Override
    public void convertirDistances(double facteur_conversion) {
        definirCentre( centre().multiply(facteur_conversion) ) ;
        definirRayon( rayon()*facteur_conversion);
    }

    @Override
    public boolean contient(Point2D p) {

        if (typeSurface()==TypeSurface.CONVEXE)
            return Environnement.quasiInferieurOuEgal(p.subtract(centre()).magnitude(),rayon.get()) ;
        else
            return Environnement.quasiSuperieurOuEgal(p.subtract(centre()).magnitude(),rayon.get()) ;
    }

    @Override
    public Double rayon_polaire(double theta) {
        return rayon.doubleValue() ;
    }

    @Override
    public Point2D centre_polaire() {
        return centre();
    }

    @Override
    public boolean aSurSaSurface(Point2D p) {

//        return Environnement.quasiEgal(centre().distance(p),rayon.get() ) ;

        // Pour éviter les racines carrées
        Point2D p_vect = p.subtract(centre()) ;
        double x_p = p_vect.getX() ;
        double y_p = p_vect.getY() ;
        return Environnement.quasiEgal(x_p*x_p+y_p*y_p,rayon.get()*rayon.get() ) ;
    }

    @Override
    public Point2D normale(Point2D p) throws Exception {
     // Cette exception est parfois levée ; elle n'apporte rien
//        if (!this.aSurSaSurface(p))
//            throw new Exception("Impossible de trouver la normale d'un point qui n'est pas sur la surface du cercle.");

        Point2D centre = this.centre() ;

        if (typeSurface()==TypeSurface.CONVEXE)
            return (p.subtract(centre).normalize());
        else
            return (p.subtract(centre).normalize()).multiply(-1.0);

    }

    @Override
    public boolean aSymetrieDeRevolution() {return true ;}
    @Override
    public Point2D pointSurAxeRevolution() {
        return centre() ;
    }

    @Override
    public boolean estOrientable() {
        return true ;
    }
    @Override
    public boolean aUneOrientation() {
        return false;
    }

    @Override
    public void tournerAutourDe(Point2D centre_rot, double angle_rot_deg) {
        Rotate r = new Rotate(angle_rot_deg,centre_rot.getX(),centre_rot.getY()) ;

        centre.set(r.transform(centre()));
    }

    @Override
    public void definirOrientation(double orientation_deg)  {
        // Rien à faire : un cercle est invariant par rotation autour de son centre
    }


    @Override
    public Double rayonDiaphragmeParDefaut() {
        return rayon();
    }

    @Override public double rayonDiaphragmeMaximumConseille() { return rayon() ; }

    /**
     * Calcule l'abscisse du centre du cercle par rapport à un axe
     * @param axe : l'axe par rapport auquel on souhaite connaître l'abscisse du centre
     */
    private double ZCentre(PositionEtOrientation axe) {

        double z_centre ;

        if (centre().subtract(axe.position()).dotProduct(axe.direction())>=0)
            z_centre = centre().distance(axe.position()) ;
        else
            z_centre = -centre().distance(axe.position()) ;

        return z_centre ;

    }

    @Override
    public List<DioptreParaxial> dioptresParaxiaux(PositionEtOrientation axe) {

        if (Environnement.quasiEgal(2*rayon(),0d)) // Pas de dioptres si le diamètre est quasi nul
            return new ArrayList<>(0) ;

        ArrayList<DioptreParaxial> resultat = new ArrayList<>(2) ;

        double z_centre = centre().subtract(axe.position()).dotProduct(axe.direction()) ;

        double z_int_min = z_centre - rayon() ;
        double z_int_max = z_centre + rayon() ;

        DioptreParaxial d_z_min ;
        DioptreParaxial d_z_max ;

        if (typeSurface()==TypeSurface.CONVEXE) {
            d_z_min = new DioptreParaxial(z_int_min, rayon(), 0d , indiceRefraction(), this);
            d_z_max = new DioptreParaxial(z_int_max, -rayon(), indiceRefraction(), 0d, this);
        } else {
            d_z_min = new DioptreParaxial(z_int_min, rayon(), indiceRefraction(),0d, this);
            d_z_max = new DioptreParaxial(z_int_max, -rayon(), 0d,indiceRefraction(), this);
        }

        resultat.add(d_z_min) ;
        resultat.add(d_z_max) ;

        return resultat ;

    }


    // TODO : Écrire une implémentation spécifique  de la méthode cherche_toutes_intersections(Rayon r) plus optimisée
    //  que l'implémentation par défaut
    // @Override
    // public ArrayList<Point2D> cherche_toutes_intersections(Rayon r)

    @Override
    public Point2D cherche_intersection(Rayon r, ModeRecherche mode) {

        double xdir = r.direction().getX();
        double ydir = r.direction().getY();

        double xdep = r.depart().getX();
        double ydep = r.depart().getY();

        Point2D centre = this.centre() ;

        double rayon = this.rayon.get() ;

        // Cas particulier du rayon vertical
        if (Environnement.quasiEgal(r.direction().getX(),0)) {

            if ( ( xdep < centre.getX()-rayon ) || (xdep > centre.getX()+rayon ) )
                return null ;

            // double deltay = Math.sqrt(rayon*rayon - (xdep-centre.getX())*(xdep-centre.getX())) ;
            // Forme (a+b)*(a-b) pour limiter les erreurs d'arrondi : cf. https://docs.oracle.com/cd/E19957-01/806-3568/ncg_goldberg.html
            double deltay = Math.sqrt((rayon+(xdep-centre.getX()))*(rayon-(xdep-centre.getX()))) ;

            double yhaut = centre.getY()+deltay ;
            double ybas  = centre.getY()-deltay ;

            if (this.aSurSaSurface(r.depart())) {
                if (ydir > 0 && ydep < centre().getY())
                    return new Point2D(xdep, yhaut);
                else if (ydir < 0 && ydep > centre().getY())
                    return new Point2D(xdep, ybas);
                else
                    return null ;
            }

            // Depart à l'intérieur de la zone délimitée par le cercle, mais pas sur sa surface pour autant
            if ( ( typeSurface() == TypeSurface.CONCAVE && !this.contient(r.depart()) ) || (typeSurface() == TypeSurface.CONVEXE && this.contient(r.depart())) ) {
                if (ydir > 0 && ydep < yhaut )
                    return new Point2D(xdep, yhaut);
                else if (ydir < 0 && ydep > ybas)
                    return new Point2D(xdep, ybas);
                else
                    return null ;
            }

            // Le point de départ est à l'extérieur du cercle
            if (ydir > 0 && ydep < ybas)
                return (mode == ModeRecherche.PREMIERE) ? new Point2D(xdep, ybas) : new Point2D(xdep, yhaut);

            if (ydir < 0 && ydep > yhaut)
                return (mode == ModeRecherche.PREMIERE) ? new Point2D(xdep,yhaut) : new Point2D(xdep, ybas) ;

            return null ;
        }

        // Cas général, rayon non vertical

        double ar = r.direction().getY() / r.direction().getX();
        double br = r.depart().getY() - r.depart().getX() * (r.direction().getY() / r.direction().getX());

        double aeq = (1 + ar * ar);
        double beq = 2 * (ar * (br - centre.getY()) - centre.getX());
        double ceq = centre.getX() * centre.getX() + (br - centre.getY()) * (br - centre.getY()) - rayon * rayon;

        // TODO : calculer autrement pour éviter les pb d'arrondis (cf. article NCG Goldberg)
        double discr = beq * beq - 4 * aeq * ceq;

        if (discr < 0)
            return null;

        double xinter = 0;
        double yinter;

        if (discr == 0.0) { // Rayon tangent au cercle

            xinter = -beq / (2 * aeq);

        } else { // Discriminant positif : renvoyer la bonne racine

            double x1 = (-beq - Math.sqrt(discr)) / (2 * aeq);
            double y1 = ar*x1 + br ;
            double x2 = (-beq + Math.sqrt(discr)) / (2 * aeq);
            double y2 = ar*x2 + br ;

            // Cas où le point de départ est sur la surface du cercle
            if (this.aSurSaSurface(r.depart())) {

                Point2D p1 = new Point2D(x1,y1) ;
                Point2D p2 = new Point2D(x2,y2) ;

//                if (p1.subtract(r.depart).magnitude()<p2.subtract(r.depart).magnitude()) {
                if (r.depart().distance(p1)< r.depart().distance(p2)) {
                    // Le point de départ est en (x1,y1) : on retourne donc l'autre intersection
                    if ( (xdir >= 0 && x2 > x1) || (xdir <= 0 && x2 < x1) )
                        return p2 ;
                } else  {
                    // Le point de départ est en (x2,y2) : on retourne donc l'autre intersection
                    if ( (xdir >= 0 && x1 > x2) || (xdir <= 0 && x1 < x2) )
                        return p1 ;
                }

//
//                if (Environnement.quasiEgal(p1.subtract(r.depart).magnitude(), 0.0)) {
//                    // Le point de départ est en (x1,y1) : on retourne donc l'autre intersection
//                    if ( (xdir >= 0 && x2 > x1) || (xdir <= 0 && x2 < x1) )
//                        return new Point2D(x2,y2) ;
//                } else if (Environnement.quasiEgal(p2.subtract(r.depart).magnitude(), 0.0)) {
//                    // Le point de départ est en (x2,y2) : on retourne donc l'autre intersection
//                    if ( (xdir >= 0 && x1 > x2) || (xdir <= 0 && x1 < x2) )
//                        return new Point2D(x1,y1) ;
//                }
//                else // TODO : il arrive que ce oups ait lieu. A creuser...
//                    throw new IllegalStateException("Oups. Je ne devrais pas être ici") ;

                return null ;

            }


            if ( ( typeSurface() == TypeSurface.CONCAVE && !this.contient(r.depart()) )
                    || ( typeSurface() == TypeSurface.CONVEXE && this.contient(r.depart()) ) ) {
                // Depart à l'intérieur de la zone délimitée par le cercle mais pas sur sa surface pour autant
                if (xdir > 0)
                    xinter = Math.max(x1, x2);
                else
                    xinter = Math.min(x1, x2);
            } else {
                // Départ à l'extérieur de la zone délimitée par le cercle
                if (xdir > 0)
                    xinter = (mode == ModeRecherche.PREMIERE) ? Math.min(x1, x2) : Math.max(x1,x2) ;
                else
                    xinter = (mode == ModeRecherche.PREMIERE) ? Math.max(x1, x2) : Math.min(x1,x2) ;
            }

        }


        if ( (xdir>0 && xdep  > xinter ) || (xdir <0 && xdep < xinter))
            return null ;

        yinter = ar*xinter + br ;

        return new Point2D(xinter, yinter);
    }

    /**
     * Calcule la ou les éventuelles intersections d'un cercle avec une verticale
     * @param x_verticale : abscisse de la verticale
     * @param ymin : valeur minimale du y solution
     * @param ymax : valeur maximale du y solution
     * @param y_sol_croissant : true si les solutions sont attendues par ordre x croissant, false si c'est l'ordre
     *                        décroissant qui est attendu.
     * @return tableau contenant 0, 1 ou 2 solutions composée(s) du y et du theta de la solution, ordonnées par y croissant
     * NB : si une intersection se trouve à l'une des extrémités de la verticale elle n'est pas retournée, car on la trouvera
     *      comme intersection sur l'horizontale
     *
     */
    @Override
    public double[][]  intersections_verticale(double x_verticale, double ymin, double ymax, boolean y_sol_croissant) {
        double rayon = rayon() ;

//        double x_centre = xCentre() ;
//        double y_centre = yCentre() ;
        double x_centre = centre().getX() ;
        double y_centre = centre().getY() ;

        double alpha = 0.0 ;

        if (Math.abs( (x_verticale-x_centre)/rayon ) < 1.0 ) {
            alpha = Math.acos((x_verticale-x_centre)/rayon) ; // alpha vaut entre 0 et PI
        } else if  (Math.abs( (x_verticale-x_centre)/rayon ) == 1.0 ) {
            if (y_centre> ymin && y_centre< ymax) {
                double[][] y_solutions = new double[1][2];
                alpha = ((x_verticale>x_centre)?0.0:Math.PI);
                y_solutions[0][0] = y_centre;
                y_solutions[0][1] = alpha;
                return y_solutions ;
            }
        } else {// Pas d'intersection
            return new double[0][0];
        }

        // Jusqu'à deux intersections possibles
        double y_sol1 = y_centre+rayon*Math.sin(alpha) ;
        double y_sol2 = y_centre-rayon*Math.sin(alpha) ;

        if ( ymin < y_sol1 && y_sol1 < ymax && ymin < y_sol2 && y_sol2 < ymax ) { // Deux intersections visibles
            double[][] y_solutions = new double[2][2];
            if ( y_sol_croissant ) {
                y_solutions[0][0] = y_sol2; // y_sol2 est toujours la plus petite car alpha est entre 0 et PI donc sin(alpha)>0
                y_solutions[0][1] = -alpha;
                y_solutions[1][0] = y_sol1;
                y_solutions[1][1] = alpha;
            } else { // Classement dans l'ordre des y décroissants
                y_solutions[0][0] = y_sol1;
                y_solutions[0][1] = alpha;
                y_solutions[1][0] = y_sol2;
                y_solutions[1][1] = -alpha;

            }
            return y_solutions ;
        }

        if ( ymin < y_sol1 && y_sol1 < ymax ) { // Seule l'intersection 1 est visible
            double[][] y_solutions = new double[1][2];
            y_solutions[0][0] = y_sol1;
            y_solutions[0][1] = alpha;
            return y_solutions ;
        }

        if ( ymin < y_sol2 && y_sol2 < ymax ) {
            double[][] y_resultats = new double[1][2];
            y_resultats[0][0] = y_sol2;
            y_resultats[0][1] = -alpha;
            return y_resultats ;
        }

        return new double[0][0] ;

    }

    /**
     * Calcule la ou les éventuelles intersections d'un cercle avec une horizontale
     * @param y_horizontale : abscisse de l'horizontale
     * @param xmin : valeur minimale du x solution
     * @param xmax : valeur maximale du x solution
     * @param x_sol_croissant : true si les solutions sont attendues par ordre de x croissant, false si c'est l'ordre
     *                        décroissant qui est attendu.
     * @return tableau contenant 0, 1 ou 2 solution composée(s) du x et du theta de la solution
     */
    @Override
    public double[][] intersections_horizontale(double y_horizontale, double xmin, double xmax,boolean x_sol_croissant) {
        double rayon = rayon() ;

        double x_centre = centre().getX() ;
        double y_centre = centre().getY() ;

        double alpha = 0.0  ;

        if (Math.abs( (y_horizontale-y_centre)/rayon ) < 1.0 ) {
            alpha = Math.asin((y_horizontale-y_centre)/rayon) ; // alpha vaut entre -PI/2 et PI/2
        } else if  (Math.abs( (y_horizontale-y_centre)/rayon ) == 1.0 ) {
            if (x_centre>= xmin && x_centre <= xmax) {
                double[][] x_solutions = new double[1][2];
                alpha = ((y_horizontale>y_centre)?Math.PI/2:-Math.PI/2);
                x_solutions[0][0] = x_centre;
                x_solutions[0][1] = alpha;
//                x_solutions[0][1] = (alpha>0?alpha:2*Math.PI+alpha);
                return x_solutions ;
            }
        } else {// Pas d'intersection
            return new double[0][0];
        }

        // Jusqu'à deux intersections possible
        double x_sol1 = x_centre+rayon*Math.cos(alpha) ;
        double x_sol2 = x_centre-rayon*Math.cos(alpha) ;

        if ( xmin <= x_sol1 && x_sol1 <= xmax && xmin <= x_sol2 && x_sol2 <= xmax ) { // Deux intersections visibles
            double[][] x_solutions = new double[2][2];
            if ( x_sol_croissant ) {
                x_solutions[0][0] = x_sol2; // x_sol2 est toujours la plus petite car alpha est entre  -PI/2 et PI/2 donc cos(alpha)>0
                x_solutions[0][1] = Math.PI-alpha;
                x_solutions[1][0] = x_sol1;
                x_solutions[1][1] = alpha;
            } else { // Classement dans l'ordre des x décroissants
                x_solutions[0][0] = x_sol1;
                x_solutions[0][1] = alpha;
                x_solutions[1][0] = x_sol2;
                x_solutions[1][1] = Math.PI-alpha;

            }
            return x_solutions ;
        }

        if ( xmin <= x_sol1 && x_sol1 <= xmax ) { // Seule l'intersection 1 est visible
            double[][] x_solutions = new double[1][2];
            x_solutions[0][0] = x_sol1;
            x_solutions[0][1] = alpha;
            return x_solutions ;
        }

        if ( xmin <= x_sol2 && x_sol2 <= xmax ) {
            double[][] x_solutions = new double[1][2];
            x_solutions[0][0] = x_sol2;
            x_solutions[0][1] = Math.PI-alpha;
            return x_solutions ;
        }

        return new double[0][0] ;

    }

    protected Point2D point_sur_cercle(double theta) {

        double rayon = rayon();

        return centre().add(rayon*Math.cos(theta), rayon*Math.sin(theta) ) ;

    }

//    @Override
//    public boolean estCouvertPar(BoiteLimiteGeometrique zone_rect) {
//
//        // Source: https://prograide.com/pregunta/10454/detection-des-collisions-entre-cercle-et-rectangle-intersection
//
//        double distance_centres_x = Math.abs(xCentre() - zone_rect.getCenterX());
//        double distance_centres_y = Math.abs(yCentre() - zone_rect.getCenterY());
//
//        if (distance_centres_x > (zone_rect.getWidth()/2 + rayon()))
//            return false;
//        if (distance_centres_y > (zone_rect.getHeight()/2 + rayon()))
//            return false;
//        if (distance_centres_x <= (zone_rect.getWidth()/2))
//            return true;
//        if (distance_centres_y <= (zone_rect.getHeight()/2))
//            return true;
//
//        double distance_coin_x = distance_centres_x - zone_rect.getWidth()/2 ;
//        double distance_coin_y = distance_centres_y - zone_rect.getHeight()/2 ;
//
//        return distance_coin_x*distance_coin_x + distance_coin_y*distance_coin_y <= rayon()*rayon() ;
//
//    }

    protected Contour arc_de_cercle(double theta_debut, double theta_fin, int nombre_pas_angulaire_par_arc) {

        Contour c = new Contour(nombre_pas_angulaire_par_arc) ;

        double pas = (theta_fin-theta_debut) / nombre_pas_angulaire_par_arc ;

        double theta = theta_debut ;

        Point2D pt;
        do {
            pt = point_sur_cercle(theta);

            c.ajoutePoint(pt.getX(),pt.getY());

            theta += pas;
        } while (theta < theta_fin);

        // Point final pour theta_fin, pour rattraper les erreurs d'arrondi
        pt = point_sur_cercle(theta_fin);

        if (pt != null)
            c.ajoutePoint(pt.getX(),pt.getY());

        return c ;

    }

    protected ArrayList<Double> xpoints_sur_cercle(double theta_debut,double theta_fin, int nombre_pas_angulaire_par_arc) {
        ArrayList<Double> xpoints_cercle = new ArrayList<>() ;

        double pas = (theta_fin-theta_debut) / nombre_pas_angulaire_par_arc ;

        double theta = theta_debut ;

        Point2D pt;
        do {
            pt = point_sur_cercle(theta);

            if (pt != null)
                xpoints_cercle.add(pt.getX());

            theta += pas;
        } while (theta <= theta_fin);

        // Point final pour theta_fin, pour rattraper les erreurs d'arrondi
        pt = point_sur_cercle(theta_fin);
        if (pt != null)
            xpoints_cercle.add(pt.getX());

        return xpoints_cercle ;
    }

    protected ArrayList<Double> ypoints_sur_cercle(double theta_debut,double theta_fin, int nombre_pas_angulaire_par_arc) {
        ArrayList<Double> ypoints_cercle = new ArrayList<>() ;

        double pas = (theta_fin-theta_debut) / nombre_pas_angulaire_par_arc ;

        double theta = theta_debut ;

        Point2D pt;
        do {
            pt = point_sur_cercle(theta);

            if (pt != null)
                ypoints_cercle.add(pt.getY());

            theta += pas;
        } while (theta <= theta_fin);

        // Point final pour theta_fin, pour rattraper les erreurs d'arrondi
        pt = point_sur_cercle(theta_fin);
        if (pt != null)
            ypoints_cercle.add(pt.getY());

        return ypoints_cercle ;
    }

}
