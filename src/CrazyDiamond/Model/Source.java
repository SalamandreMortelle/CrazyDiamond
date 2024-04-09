package CrazyDiamond.Model;

import javafx.beans.property.*;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Source extends BaseElementNommable implements Nommable {

    // Récupération du logger
    private static final Logger LOGGER = Logger.getLogger( "CrazyDiamond" );
    private static final int nombre_rayons_par_defaut = 1 ;
    private static final int nombre_max_obstacles_rencontres_par_defaut = 7 ;

    private static int compteur_source ;

    protected static ObjectProperty<Color> couleur_par_defaut_property = new SimpleObjectProperty<>(Color.YELLOW) ;

    protected Environnement environnement ;

    private final ObjectProperty<PositionEtOrientation> position_orientation ;

    protected final IntegerProperty nombre_maximum_rencontres_obstacle;

    protected final ObjectProperty<Color> couleur ;

    // Les chemins n'ont pas à être observables : on sait qu'ils doivent être recalculés dès qu'une autre propriété observable change
    protected ArrayList<CheminLumiere> chemins ;

    public static Property<Color> couleurParDefautProperty() { return couleur_par_defaut_property ; }

    public Contour positions_poignees() {
            Contour c_poignees = new Contour(1) ;

//            c_poignees.ajoutePoint(position_x.get(),position_y.get());
            c_poignees.ajoutePoint(position());

            return c_poignees ;
    }

    public void retaillerPourSourisEn(Point2D pos_souris) {
        // TODO
    }

    public void retaillerSelectionPourSourisEn(Point2D position) {

    }

    public void translater(Point2D vecteur) {
        position_orientation.set(new PositionEtOrientation(position().add(vecteur),orientation()));
    }

    public Commande commandeCreation(Environnement env) {
        return new CommandeCreerSource(env,this) ;
    }


    public enum TypeSource {
        LASER("LASER"),
        PINCEAU("PINCEAU"),
        PROJECTEUR("PROJECTEUR");

        private final String value;

        TypeSource(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        public static TypeSource fromValue(String text) {
            for (TypeSource t_source : TypeSource.values()) {
                if (String.valueOf(t_source.value).equals(text)) {
                    return t_source;
                }
            }
            return null;
        }

    }

    public TypeSource type() {
        return type.get();
    }

    public void definirType(TypeSource t) {
        // Ne rien faire si le type à définir est déjà la type actuel
        if (this.type.get()==t)
            return ;
        this.type.set(t);
    }

    protected final ObjectProperty<TypeSource> type;

    private final IntegerProperty nombre_rayons;
    private final DoubleProperty ouverture_pinceau;
    private final DoubleProperty largeur_projecteur;

    private final BooleanProperty lumiere_polarisee;

    /**
     * Angle du champ électrique E par rapport au plan d'incidence (n'est défini que si lumierePolarisee == true):
     * 0° signifie que le champ E est dans le plan d'incidence : l'onde est polarisée Transverse Magnetique (TM)
     * 90° signifie que le champ E est perpendiculaire au plan d'incidence : l'onde est polarisée Transverse Electrique (TE)
     */
    private final DoubleProperty angle_champ_electrique;

    public DoubleProperty angleChampElectriqueProperty() {return angle_champ_electrique;}
    public double angleChampElectrique() { return angle_champ_electrique.get() ;}
    public void definirAngleChampElectrique(double ang) { angle_champ_electrique.set(ang); }

    public BooleanProperty lumierePolariseeProperty() { return lumiere_polarisee; }
    public boolean lumierePolarisee() { return lumiere_polarisee.get() ;}
    public void definirLumierePolarisee(boolean pol) { lumiere_polarisee.set(pol); }

    public Environnement environnement() {
        return environnement;
    }

    public int nombreMaximumRencontresObstacle() {
        return nombre_maximum_rencontres_obstacle.get();
    }
    public void definirNombreMaximumRencontresObstacle(int nb_m) { nombre_maximum_rencontres_obstacle.set(nb_m) ; }

    public IntegerProperty nombreMaximumRencontresObstacleProperty() {
        return nombre_maximum_rencontres_obstacle;
    }

    public int nombreRayons() {return nombre_rayons.get();}
    public void definirNombreRayons(int nb_r) { nombre_rayons.set(nb_r); }
    public IntegerProperty nombreRayonsProperty() {
        return nombre_rayons;
    }

    public double ouverturePinceau() {
        return ouverture_pinceau.get();
    }

    public DoubleProperty ouverturePinceauProperty() {
        return ouverture_pinceau;
    }

    public double largeurProjecteur() {
        return largeur_projecteur.get();
    }

    public DoubleProperty largeurProjecteurProperty() {
        return largeur_projecteur;
    }

    public double orientation() { return position_orientation.get().orientation_deg(); }
    public ObjectProperty<TypeSource> typeProperty() { return type; }
    public ObjectProperty<Color> couleurProperty() { return couleur; }
    public Color couleur() { return couleur.get(); }

    public void definirCouleur(Color c) { couleur.set(c); }

    public Point2D[] extremitesProjecteur() {
        Point2D dir = direction() ;
        Point2D vect_perp = new Point2D(-dir.getY(),dir.getX()) ;

        Point2D[] res = new Point2D[2] ;

        res[0] = position().subtract(vect_perp.multiply(0.5d*largeurProjecteur())) ;
        res[1] = position().add(vect_perp.multiply(0.5d*largeurProjecteur())) ;

        return res ;
    }

    public Source(Environnement environnement, Point2D position, double orientation, TypeSource type , int nb_rayons, Color couleur) {
        this(environnement,position, orientation,type,nb_rayons,couleur,false,0d,nombre_max_obstacles_rencontres_par_defaut) ;
    }

    public Source(Environnement environnement, Point2D position, double orientation, TypeSource type , int nb_rayons, Color couleur, boolean lumiere_polarisee , double angle_polarisation, int nombre_max_obstacles_rencontres) {
        this(environnement, new Imp_Nommable( "Source "+(++compteur_source)),position, orientation,type,nb_rayons,couleur,lumiere_polarisee,angle_polarisation,nombre_max_obstacles_rencontres) ;
    }

    public Source(Environnement environnement, Imp_Nommable iei, Point2D position, double orientation, TypeSource type, int nb_rayons, Color couleur, boolean lumiere_polarisee, double angle_champ_electrique, int nombre_max_obstacles_rencontres) {
        this(environnement, iei,position, orientation,type,nb_rayons, 0d,1d,couleur,lumiere_polarisee,angle_champ_electrique,nombre_max_obstacles_rencontres) ;

    }
    public Source(Environnement environnement, Imp_Nommable iei, Point2D position, double orientation_deg, TypeSource type, int nb_rayons, double ouverture_pinceau, double largeur_projecteur, Color couleur, boolean lumiere_polarisee, double angle_champ_electrique, int nombre_max_obstacles_rencontres) {
        super(iei);

        if (environnement == null)
            throw new IllegalArgumentException("Une source doit appartenir à un environnement.") ;

        if (position == null)
            throw new IllegalArgumentException("La position d'une source ne peut pas être indéfinie (null).") ;

        if (nb_rayons<1)
            throw new IllegalArgumentException("Le nombre de rayons d'une source doit être strictement positif.") ;

        this.environnement = environnement ;

        this.nombre_maximum_rencontres_obstacle = new SimpleIntegerProperty(nombre_max_obstacles_rencontres) ;

        this.position_orientation = new SimpleObjectProperty<>(new PositionEtOrientation(position, orientation_deg)) ;

        this.type = new SimpleObjectProperty<>(Objects.requireNonNullElse(type, TypeSource.PINCEAU));

        this.nombre_rayons = new SimpleIntegerProperty(nb_rayons) ;

        this.ouverture_pinceau = new SimpleDoubleProperty(ouverture_pinceau) ;
        this.largeur_projecteur = new SimpleDoubleProperty(largeur_projecteur) ;

        if (couleur == null)
            this.couleur = new SimpleObjectProperty<>(couleur_par_defaut_property.getValue()) ;
        else
            this.couleur = new SimpleObjectProperty<>(couleur) ;

        this.lumiere_polarisee = new SimpleBooleanProperty(lumiere_polarisee) ;
        this.angle_champ_electrique = new SimpleDoubleProperty(angle_champ_electrique) ;
    }

    public Source(Environnement environnement, Point2D position, double orientation, TypeSource type) {
        this(environnement,position, orientation,type,nombre_rayons_par_defaut,couleur_par_defaut_property.getValue()) ;
    }

    public Source(Environnement environnement, Point2D position, double orientation, TypeSource type , int nb_rayons) {
        this(environnement,position, orientation,type,nb_rayons,couleur_par_defaut_property.getValue()) ;
    }

    public void accepte(VisiteurEnvironnement v) {
        v.visiteSource(this);
    }

    public void ajouterRappelSurChangementToutePropriete(RappelSurChangement rap) {
        ajouterRappelSurChangementTouteProprieteModifiantChemin(rap);
    }

    public void ajouterRappelSurChangementTouteProprieteModifiantChemin(RappelSurChangement rap) {
        position_orientation.addListener((observable, oldValue, newValue) -> rap.rappel());
        nombre_maximum_rencontres_obstacle.addListener((observable, oldValue, newValue) -> rap.rappel());
        nombre_rayons.addListener((observable, oldValue, newValue) -> rap.rappel());
        ouverture_pinceau.addListener((observable, oldValue, newValue) -> rap.rappel());
        largeur_projecteur.addListener((observable, oldValue, newValue) -> rap.rappel());
        type.addListener((observable, oldValue, newValue) -> rap.rappel());
        couleur.addListener((observable, oldValue, newValue) -> rap.rappel());
        lumiere_polarisee.addListener((observable, oldvalue, newValue) -> rap.rappel());
        angle_champ_electrique.addListener((observable, oldvalue, newValue) -> rap.rappel());
    }

    public void definirDirection(Point2D direction) {

        if (direction == null) {
            throw new IllegalArgumentException("La direction de la source ne peut pas être null.") ;
        }

        if (direction.magnitude() == 0.0) {
            throw new IllegalArgumentException("La direction de la source ne peut pas être un vecteur nul.") ;
        }

        double angle_deg = direction.angle(new Point2D(1,0)) ;

        if (direction.getY()>=0)
            definirOrientation(angle_deg) ;
        else
            definirOrientation(360-angle_deg);

    }

    public void definirPosition(Point2D pos) {position_orientation.set(new PositionEtOrientation(pos,orientation()));}

    public void definirOrientation(double or) {
        position_orientation.set(new PositionEtOrientation(position(),or)) ;
    }

    public void definirOuverturePinceau(double ouverture_pinceau) {
        if (ouverture_pinceau > 0)
            this.ouverture_pinceau.set(ouverture_pinceau) ;
    }
    public void definirLargeurProjecteur(double largeur_projecteur) {
        if (largeur_projecteur > 0)
            this.largeur_projecteur.set(largeur_projecteur) ;
    }

    public double xPosition() { return position_orientation.get().position().getX() ; }
    public double yPosition() { return position_orientation.get().position().getY() ; }

    public Point2D position() {
        return position_orientation.get().position() ;
    }

    public Point2D direction() {
        return new Point2D(Math.rint(1.0E10*Math.cos(Math.toRadians(orientation())))/1.0E10, Math.rint(1E10*Math.sin(Math.toRadians(orientation())))/1.0E10) ;
    }

    boolean est_tres_proche_de(Point2D p,double tolerance) {

        double x1 = 0, y1 = 0 ;
        double x2 = 0, y2 = 0 ;

        TypeSource ts = type.get() ;
        Point2D pos = position() ;

        if (ts == TypeSource.LASER || ts == TypeSource.PINCEAU) {
            x1 = pos.getX() ;
            y1 = pos.getY() ;

            x2 = x1 ;
            y2 = y1 ;
        } else if (ts == TypeSource.PROJECTEUR) {
            Point2D vect_perp = new Point2D(-direction().getY(),direction().getX()).normalize() ;

            x1 = pos.getX()-0.5* largeur_projecteur.doubleValue()*vect_perp.getX() ;
            y1 = pos.getY()-0.5* largeur_projecteur.doubleValue()*vect_perp.getY() ;

            x2 = pos.getX()+0.5* largeur_projecteur.doubleValue()*vect_perp.getX() ;
            y2 = pos.getY()+0.5* largeur_projecteur.doubleValue()*vect_perp.getY() ;

        }

        if (p.getX()+tolerance <Math.min(x1, x2))
            return false;
        if (p.getX()-tolerance > Math.max(x1, x2))
            return false;

        if (x1 == x2) {
            return Environnement.quasiEgal(p.getX(), x1, tolerance)
                    && Environnement.quasiSuperieurOuEgal(p.getY(), Math.min(y1, y2), tolerance)
                    && Environnement.quasiInferieurOuEgal(p.getY(), Math.max(y1, y2), tolerance);
        }

        double a = (y2 - y1) / (x2 - x1) ;
        double yseg = a * (p.getX() - x1) + y1 ;

        return Environnement.quasiEgal(yseg, p.getY(),tolerance) ;

    }
    public void illuminer() {

        if (chemins == null)
            chemins = new ArrayList<>(nombre_rayons.intValue()) ;
        else
            // On oublie les chemins précédemmment calculés
            chemins.clear();

        Point2D position  = this.position() ;
        Point2D direction = this.direction() ;

        switch (type.get()) {
            case LASER -> lancer_rayon_si_possible(position,direction);
            case PINCEAU -> {

                if (nombre_rayons.intValue()==1) {
                    lancer_rayon_si_possible(position,direction);
                } else { // Pinceau avec plus d'un rayon

                    Obstacle obs_contenant = environnement.obstacleReelAuPremierPlanContenant(position) ;

                    if (!peut_emettre_depuis_point_dans_obstacle(obs_contenant,position))
                        return ;

                    double indice_milieu_traverse = (obs_contenant==null?environnement.indiceRefraction():obs_contenant.indiceRefraction()) ;
                    // NB : On pourrait peut-être se passer de la ligne ci-dessus car l'indice traversé par le rayon sera calculé (de
                    // manière plus fiable a priori : en tenant compte de la direction du rayon, plutôt que de la position du point
                    // d'origine, qui pourrait être sur un dioptre, à la limite entre deux milieux)
                    // dans la méthode calculerCheminDuRayon()

                    for (int i = 0; i < nombre_rayons.intValue(); i++ ) {
                        double theta = -ouverture_pinceau.doubleValue() / 2 + i * (ouverture_pinceau.doubleValue() / (nombre_rayons.intValue()-1));

                        Affine rotation = new Affine() ;
                        rotation.appendRotation(theta);

                        Rayon r_pinceau = new Rayon(position,rotation.transform(direction),
                                indice_milieu_traverse,
                                Rayon.PhenomeneOrigine.EMISSION_SOURCE, 1.0, lumiere_polarisee.get(), angle_champ_electrique.get())  ;

                        lancer(r_pinceau,false) ;
                    }
                }
            }
            case PROJECTEUR -> {
                if (nombre_rayons.intValue()==1) {
                    lancer_rayon_si_possible(position,direction);

                } else {

                    Point2D vect_perp = new Point2D(-direction.getY(),direction.getX()).normalize() ;

                    double xa = position.getX()-0.5* largeur_projecteur.doubleValue()*vect_perp.getX() ;
                    double ya = position.getY()-0.5* largeur_projecteur.doubleValue()*vect_perp.getY() ;

                    double xb = position.getX()+0.5* largeur_projecteur.doubleValue()*vect_perp.getX() ;
                    double yb = position.getY()+0.5* largeur_projecteur.doubleValue()*vect_perp.getY() ;

                    double delta_x = (xb-xa) / (nombre_rayons.intValue()-1) ;
                    double delta_y = (yb-ya) / (nombre_rayons.intValue()-1) ;


                    for (int i = 0; i < nombre_rayons.intValue(); i++ ) {
                        Point2D pdep_rayon = new Point2D(xa+i*delta_x,ya+i*delta_y) ;

                        lancer_rayon_si_possible(pdep_rayon,direction);

                    }
                }

            }
        }

    }

    private void lancer_rayon_si_possible(Point2D pos, Point2D dir){

        Rayon r_emis = creer_rayon_si_possible(pos,dir) ;

        if (r_emis==null)
            return ;

        lancer(r_emis);

    }
    private Rayon creer_rayon_si_possible(Point2D pos, Point2D dir) {

        Obstacle obs_contenant = environnement.obstacleReelAuPremierPlanContenant(pos) ;

        if (!peut_emettre_depuis_point_dans_obstacle(obs_contenant,pos))
            return null ;

        double indice_traverse = (obs_contenant==null?environnement.indiceRefraction():obs_contenant.indiceRefraction()) ;
        // NB : On pourrait peut-être se passer de la ligne ci-dessus car l'indice traversé par le rayon sera calculé (de
        // manière plus fiable a priori : en tenant compte de la direction du rayon, plutôt que de la position du point
        // d'origine, qui pourrait être sur un dioptre, à la limite entre deux milieux)
        // dans la méthode calculerCheminDuRayon()

        return new Rayon(pos,dir,indice_traverse,Rayon.PhenomeneOrigine.EMISSION_SOURCE, 1.0, lumiere_polarisee.get(), angle_champ_electrique.get()) ;

    }
    private boolean peut_emettre_depuis(Point2D point) {

        Obstacle obs_contenant = environnement.obstacleReelAuPremierPlanContenant(point) ;

        if (obs_contenant == null)
            return true ;

        return peut_emettre_depuis_point_dans_obstacle(obs_contenant,point) ;

    }

    private boolean peut_emettre_depuis_point_dans_obstacle(Obstacle o, Point2D pt_dep) {
        if (o == null)
            return true ;

        NatureMilieu nature_mil_obs = o.natureMilieu() ;

        TraitementSurface trait_surf_obs = o.traitementSurface() ;

        if (o.aSurSaSurface(pt_dep) && (trait_surf_obs != TraitementSurface.ABSORBANT))
            return true ;

        return nature_mil_obs == NatureMilieu.TRANSPARENT;

    }
    protected void lancer(Rayon r) {
        lancer(r,true);
    }

    protected void lancer(Rayon r, boolean verifier_posssibilite_emettre) {

        if (verifier_posssibilite_emettre && !peut_emettre_depuis(r.depart()))
            return;

        CheminLumiere chemin = new CheminLumiere(couleur.getValue(),0) ;

        calculerCheminDuRayon(chemin,r,0) ;

        chemins.add(chemin) ;
    }


    /**
     * Complete le CheminLumiere chemin avec le rayon r (dont seuls le départ et la direction sont initialement renseignés) et ses rayons
     * réfléchis et transmis
     * @param chemin : chemin à compléter
     * @param r : rayon à ajouter
     * @param nombre_obstacles_rencontres : le nombre d'obstacles rencontrés
     */
    protected void calculerCheminDuRayon(CheminLumiere chemin, Rayon r, int nombre_obstacles_rencontres) {

        // Par défaut considérer que le rayon ne rencontre pas d'obstacle
        Obstacle obstacle_rencontre = null;

        Point2D p_inter_le_plus_proche = null ;

        // Attention : si le point de départ du rayon est hors zone visible, il peut y avoir 0 ou 2
        // intersections (voire une seule s'il passe par un coin)

        // Considérer qu'il n'y a aucune intersection = on va aussi aller chercher les intersections hors
        // de la zone visible
        double distance_inter_la_plus_proche = Double.MAX_VALUE ;

        Iterator<Obstacle> ito = environnement.iterateur_obstacles_reels() ;

        // Lister toutes les intersections du rayon avec les stream_obstacles qui sont dans l'environnement (càd visibles)
        while (ito.hasNext()) {

            Obstacle o = ito.next() ;

            // Prendre l'intersection la plus proche du départ du rayon, repérer l'obstacle qui a été atteint
            Point2D p_inter = o.premiere_intersection(r);

            LOGGER.log(Level.FINER,"Intersection en {0} avec obstacle {1}",new Object[] {p_inter,o});

//            // Si l'intersection est hors zone visible, passer à l'obstacle suivant
//            if (!boite_limites.contains(p_inter))
//                continue;

            // Si pas d'intersection, passer à l'obstacle suivant
            if (p_inter==null)
                continue ;

            double dist = p_inter.subtract(r.depart()).magnitude();

            // Si cette intersection avec l'obstacle o courant est la plus proche, ou si l'obstacle o englobe la
            // précédente intersection trouvée (alors qu'il se trouve après dans la liste des obstacles, ce qui signifie
            // qu'il n'est pas masqué par le précédent), cette intersection est celle qu'il faut considérer.
            if (dist < distance_inter_la_plus_proche || (p_inter_le_plus_proche!=null && o.contient(p_inter_le_plus_proche))) {
                obstacle_rencontre = o;
                p_inter_le_plus_proche = p_inter;
                distance_inter_la_plus_proche = dist;
            }
        }

        // Si il n'y a pas d'intersection avec un obstacle, terminer le chemin par un rayon infini
        if (obstacle_rencontre == null) {

            // Ajouter un rayon infini (qui n'a pas de point d'arrivée) pour terminer le chemin

            Obstacle obs_traverse = environnement.obstacleReelAuPremierPlanContenant(r.depart().add(r.direction())) ;

            r.indice_milieu_traverse = ((obs_traverse==null)?environnement.indiceRefraction():obs_traverse.indiceRefraction()) ;
            // NB : si r est le rayon de départ émis par la source, un indice refraction avait déjà été défini (dans Source::illuminer)
            //      mais si le point de départ de r était à la surface d'un dioptre, mieux vaut "avancer" un peu sur le rayon (qui
            //      ne rencontre aucun obstacle) pour être sûr du milieu traversé.
            // TODO (optimisation) : les deux lignes précédentes font qu'il ne sert à rien d'avoir défini au préalable l'indice du milieu
            //  traversé par le rayon r, comme on le fait dans le cas où le rayon r est un rayon source (PhenomeneOrigine==EMISSION_SOURCE).

            chemin.ajouteRayon(r);

            // Fin du chemin
            return;
        }

        // Sinon, faire de cette intersection le point d'arrivée du rayon
        r.definirArrivee(p_inter_le_plus_proche);

        Obstacle obs_traverse = environnement.obstacleReelAuPremierPlanContenant(r.depart().midpoint(p_inter_le_plus_proche)) ;
        r.indice_milieu_traverse = (obs_traverse!=null?obs_traverse.indiceRefraction():environnement.indiceRefraction()) ;
        // TODO (optimisation) : les deux lignes précédentes font qu'il ne sert à rien d'avoir défini au préalable l'indice du milieu
        //  traversé par le rayon r, comme on le fait dans le cas où le rayon r est un rayon source (PhenomeneOrigine==EMISSION_SOURCE).

        chemin.ajouteRayon(r);

        nombre_obstacles_rencontres++ ;

        if (nombre_obstacles_rencontres > nombre_maximum_rencontres_obstacle.get())
            return;

        RayonsRefracteEtReflechi rayons_res = null ;

        try {
                rayons_res = Obstacle.rayonsRefracteEtReflechi(obstacle_rencontre,r,environnement) ;

            // TODO : Optimisation possible : on a déjà calculé l'intersection : on pourrait la passer dans l'appel précédent car la
            //        méthode rayonReflechi commence par refaire ce calcul d'intersection.
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,"Exception inattendue lors du calcul des rayons réfractés et réfléchis suivants",e);
        }

        // Arrêt de la récurrence si pas de rayon suivant transmis ou réflechi
        if (rayons_res.rayon_refracte == null && rayons_res.rayon_reflechi == null)
            return ;

        chemin.normale = rayons_res.normale ;

        if (rayons_res.rayon_refracte != null)
            calculerCheminDuRayon(chemin.creerCheminRayonTransmisSuivant(),rayons_res.rayon_refracte,nombre_obstacles_rencontres);

        if (rayons_res.rayon_reflechi != null)
            calculerCheminDuRayon(chemin.creerCheminRayonReflechiSuivant(),rayons_res.rayon_reflechi,nombre_obstacles_rencontres);


    }

    public void eteindre() {
        chemins.clear();
    }

    public Iterator<CheminLumiere> iterateur_chemins() {
        if (chemins==null)
            return null ;

        return chemins.iterator() ;
    }

    public ObjectProperty<PositionEtOrientation> positionEtOrientationObjectProperty() {
        return position_orientation ;
    }

    public void convertirDistances(double facteur_conversion) {
        position_orientation.set(new PositionEtOrientation(position().multiply(facteur_conversion),orientation()));
        largeur_projecteur.set(largeurProjecteur()*facteur_conversion);
    }

}
