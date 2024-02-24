package CrazyDiamond.Model;


import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

// Interface commune pour tous les obstacles qui ont une influence sur le chemin du rayon lumineux
// Cette interface convient si un rayon ne peut avoir plus de deux intersections avec l'Obstacle
// Cela convient donc pour des obstacles d'un seul tenant, qui peuvent contenir des trous ou être concaves
// à condition qu'ils soient infinis.
public interface Obstacle {



    enum ModeRecherche { PREMIERE, DERNIERE }

    // Récupération du logger
    Logger LOGGER = Logger.getLogger( "CrazyDiamond" );

    boolean aSurSaSurface(Point2D p) ;

    /**
     *
     * @param p : le point à tester
     * @return true si l'Obstacle contient le point p, false sinon.
     * Par convention un point à la surface de l'Obstacle est considéré comme faisant partie de l'Obstacle.
     *
     */
    boolean contient(Point2D p) ;

    default Commande commandeCreation(Environnement env) {  return new CommandeCreerObstacleSimple(env,this) ; }


    default boolean contient_strict(Point2D p) { return (contient(p) && !aSurSaSurface(p)) ; }

    default boolean comprend(Obstacle o) { return this.equals(o) ; }

    default Obstacle obstacle_avec_id(String obs_id) { return id().equals(obs_id)?this:null ; }

    default Composition composition_contenant(Obstacle o) { return null ; }

    Point2D normale(Point2D p) throws Exception ;

    Double courbureRencontreeAuSommet(Point2D pt_sur_surface, Point2D direction) throws Exception;

    default boolean aSymetrieDeRevolution()  { return false ; }

    /**
     *
     * @return un point situé sur l'axe de révolution, si l'Obstacle a une symétrie de révolution, sinon 'null'
     */
    default Point2D pointSurAxeRevolution()  { return null ;}

    default boolean estOrientable() { return false ; }

    default void definirOrientation(double orientation_deg)  {
//        throw new NoSuchMethodException("La méthode definirOrientation() n'est pas implémentée par l'Obstacle "+this) ;
    }

    default boolean aUneOrientation() { return false ; }

    default double orientation() {

        return 0 ;
//        throw new NoSuchMethodException("La méthode orientation() n'est pas implémentée par l'Obstacle "+this) ;

    }

    void tournerAutourDe(Point2D centre_rot,double angle_rot_deg) ;

    default void definirAppartenanceSystemeOptiqueCentre(boolean b) { }
    default boolean appartientASystemeOptiqueCentre() { return false; }

    void definirAppartenanceComposition(boolean b) ;
    boolean appartientAComposition() ;

    /**
     * Pour un obstacle avec symétrie de révolution, calcule les positions et retourne les propriétés (courbures,
     * indices avant/apres, valeurs de diaphragmes...) de tous les dioptres de l'obstacle qui coupent l'axe fourni en
     * paramètre. L'axe est supposé être l'axe de symétrie de révolution de l'Obstacle.
     * <p>
     * Le rayon de courbure ne peut pas être quasi égal à 0. Un dioptre avec ce rayon de courbure ne sera pas retourné.
     * <p>
     * Les rayons de courbure sont positifs si la surface est convexe dans le sens de l'axe fourni, négatifs sinon.
     * <p>
     * Les indices des milieux hors de l'obstacle sont mis à 0.0 par défaut. A charge pour l'appelant de les renseigner
     * ensuite correctement selon ce qu'il souhaite faire.
     * <p>
     * Pré-condition : l'axe fourni en paramètre est l'axe de révolution (choisi selon les mêmes conventions que l'axe d'un SOC)
     * @param axe : axe sur lequel on cherche la position des dioptres paraxiaux
     * @return la liste des dioptres sur l'axe de révolution fourni, classés par Z croissants, Rcourbure "croissants"
     */
    default List<DioptreParaxial> dioptresParaxiaux(PositionEtOrientation axe) {
        LOGGER.log(Level.SEVERE,"dioptresParaxiaux() pas implémenté par l'obstacle (l'obstacle doit avoir une symétrie de révolution)",this);
        return null ;
    }

    default Point2D premiere_intersection(Rayon r) {  return cherche_intersection(r, ModeRecherche.PREMIERE) ; }
    default Point2D derniere_intersection(Rayon r) {  return cherche_intersection(r, ModeRecherche.DERNIERE) ; }

    default boolean aUneProprieteDiaphragme() { return false ; }
    default Property<Double> diaphragmeProperty() { return null ; }
    default Double rayonDiaphragmeParDefaut() { return null ; }

    default double rayonDiaphragmeMaximumConseille() { return Double.MAX_VALUE ; }
    default void forcerRayonDiaphragmeMaximumConseille(Double diaph_max_conseille) {
        // Par défaut, ne rien faire
    }

    default boolean estReflechissant() {
        return (traitementSurface() == TraitementSurface.REFLECHISSANT
                || ((traitementSurface() == TraitementSurface.PARTIELLEMENT_REFLECHISSANT) && (tauxReflexionSurface() > 0.5d))) ;
    }

    /**
     * Recherche la première ou la dernière intersection du rayon r avec l'obstacle.
     * À noter que si le point de départ du rayon est sur la surface de l'obstacle, il ne sera pas retourné.
     * S'il n'y a qu'une intersection, elle est retournée aussi bien en tant que première qu'en tant que dernière intersection.
     * @param r : rayon dont on cherche l'intersection
     * @param mode (PREMIERE ou DERNIERE)
     * @return le point d'intersection trouvé, ou 'null' s'il n'y en a pas
     */
    Point2D cherche_intersection(Rayon r, ModeRecherche mode) ;

    /**
     * Cherche toutes les intersections d'un rayon avec la surface de l'obstacle, et les retourne classées de la plus
     * proche à la plus éloignée, dans le sens de la marche du rayon r. Le point de départ du rayon peut être à
     * l'intérieur comme à l'extérieur de l'obstacle.
     * <p>
     * L'implémentation par défaut ne convient que pour les obstacles qui ont au plus deux intersections avec tout
     * rayon incident. Si ce n'est pas le cas, une implémentation correcte doit impérativement être fournie.
     * Dans tous les cas, il est conseillé de fournir une implémentation spécifique à l'obstacle qui sera certainement
     * plus performante que l'implémentation par défaut.
     *
     * @param r : le rayon
     * @return la liste classée des intersections
     */
    default ArrayList<Point2D> cherche_toutes_intersections(Rayon r) {

        ArrayList<Point2D> resultats = new ArrayList<>(2) ;

        Point2D premiere_int = premiere_intersection(r) ;
        Point2D derniere_int = derniere_intersection(r) ;

        if (premiere_int!=null)  resultats.add(premiere_int) ;
        if (derniere_int!=null)  resultats.add(derniere_int) ;

        return resultats ;

    }

//    boolean estCouvertPar(BoiteLimiteGeometrique zone_rect) ;

    // TODO : Implémenter cette méthode pour implémenter les Compositions de Compositions les plus générales
   // ArrayList<Point2D> cherche_toutes_intersections(Rayon r) ;

    default void ajouterRappelSurChangementToutePropriete(RappelSurChangement rap) { }
    default void ajouterRappelSurChangementTouteProprieteModifiantChemin(RappelSurChangement rap) { }

    String id() ;
    StringProperty nomProperty() ;
    String nom() ;


    default Color couleurContour() {return null ;}
    default Color couleurMatiere() {return null ;}
    default NatureMilieu natureMilieu() { return null ; }

    default TraitementSurface traitementSurface() { return null; }
    default double tauxReflexionSurface() { return 0.0; }

    void definirOrientationAxePolariseur(double angle_pol) ;
    double orientationAxePolariseur() ;
    DoubleProperty orientationAxePolariseurProperty();


    default double indiceRefraction()  {
        throw new UnsupportedOperationException("L'obstacle "+this+" ne peut pas calculer son indice de réfraction") ;
    }

    void accepte(VisiteurEnvironnement v) ;

     default void accepte(VisiteurElementAvecMatiere v) {
         throw new UnsupportedOperationException("L'obstacle "+this+" ne peut pas accepter un VisiteurElementAvecMatiere") ;
     }

    void retaillerPourSourisEn(Point2D pos_souris) ;
    void retaillerParCommandePourSourisEn(Point2D pos_souris) ;
    default void retaillerSelectionPourSourisEn(Point2D pclic) {
        retaillerPourSourisEn(pclic);
    }
    default void retaillerSelectionParCommandePourSourisEn(Point2D pclic,Point2D p_depart_poignee) {
        new CommandeDefinirPositionPoigneeObstacleSelection(this,pclic,p_depart_poignee).executer();
//        retaillerParCommandePourSourisEn(pclic,p_depart_poignee);
    }
    default Contour positions_poignees() { return null ; }

    void translater(Point2D vecteur) ;
    void translaterParCommande(Point2D vecteur) ;
    default boolean est_tres_proche_de(Point2D pt,double tolerance) { return false ; }

    void convertirDistances(double facteur_conversion) ;

    static Point2D normaleAuPointIncidence(Obstacle o,Rayon r) throws Exception {

        Point2D inter = o.premiere_intersection(r) ;

        if (inter == null)
            throw new Exception("Pas d'intersection du rayon avec l'obstacle") ;

        return o.normale(inter) ;
    }

    /**
     * Construit et initialise le rayon réfléchi "total" d'un rayon incident r par un obstacle o, c'est-à-dire en supposant
     * que la puissance du rayon réfléchi est égale à celle du rayon incident (réflexion qui n'est pas accompagnée d'une réfraction)
     * @param o : l'Obstacle qui réfléchit le rayon incident
     * @param r : le Rayon incident
     * @return le Rayon réfléchi
     * @throws Exception
     */
     static Rayon rayonReflechiTotal(Obstacle o, Rayon r) throws Exception {

         // Si l'obstacle o rencontré est concave, il est possible que le rayon parte de la SURFACE de ce dernier, et
         // soit réfléchi par cet osbtacle lui-même. Mais si le point de départ est dans la masse de l'obstacle, et
         // pas dans sa surface, il n'y a pas de rayon réfléchi, sauf si le milieu de l'obstacle est transparent (si on
         // a atteint, ou dépassé, l'angle de la réflexion totale)
         if (o.contient(r.depart()))
             if (!o.aSurSaSurface(r.depart()) && (o.natureMilieu()!=NatureMilieu.TRANSPARENT))
                 return null ;

//         System.out.println("Angle rayon incident  : "+r.direction.angle(new Point2D(1,0))+"°");


//         Point2D inter = o.premiere_intersection(r) ;
         Point2D inter = (r.arrivee() !=null)? r.arrivee() :o.premiere_intersection(r) ;

         Point2D oppose_vecteur_incident = r.direction().multiply(-1.0) ;
         Point2D normale                 = o.normale(inter) ;

//         System.out.println("Angle normale : "+normale.angle(new Point2D(1,0))+"°");

//         System.out.println("Angle i1 : "+normale.angle(oppose_vecteur_incident)+"°");



         double sens_rotation ;

         if (oppose_vecteur_incident.crossProduct(normale).getZ() > 0 )
             sens_rotation = +1.0 ;
         else
             sens_rotation = -1.0 ;

         // TODO : Vérifier ces calculs en affichant les angles des vecteurs incidents, réfléchis et de la normale



//         Affine rotation = new Affine() ;
//         rotation.appendRotation(sens_rotation*2*oppose_vecteur_incident.angle(normale));
         Rotate rotation = new Rotate(sens_rotation*2*oppose_vecteur_incident.angle(normale)) ;

//         Rayon r_reflechi =  new Rayon(inter, rotation.transform(oppose_vecteur_incident), Rayon.PhenomeneOrigine.REFLEXION) ;


         Rayon r_reflechi = new Rayon(inter, rotation.transform(oppose_vecteur_incident), r.indice_milieu_traverse,
                     Rayon.PhenomeneOrigine.REFLEXION, r.ratio_puissance,
                     r.est_polarisee, r.angle_champ_electrique);

//         System.out.println("Angle rayon reflechi  : "+r_reflechi.direction.angle(new Point2D(1,0))+"°");
//         System.out.println("Angle i2 : "+normale.angle(r_reflechi.direction)+"°");

         // TODO : A mettre en assertion
         if(Math.abs(normale.angle(r_reflechi.direction())-normale.angle(oppose_vecteur_incident))>0.000001) {
             throw new Exception("L'angle i2 du rayon réfléchi est différent de l'angle i1 du rayon incident. (i1 = "+normale.angle(oppose_vecteur_incident)+"° et i2 ="+normale.angle(r_reflechi.direction())+"°)") ;
         }

        return  r_reflechi ;
     }

    /**
     * Construit et initialise le rayon réfracté d'un rayon incident r par un obstacle transparent o.
     * @param o : obsacle qui réfracte le rayon
     * @param r : rayon considéré
     * @param env : environnement
     * @param calcul_transmittance : indique si les facteurs de transmittance doivent être calculés
     * @return le rayon réfracté
     * @throws  Exception si le rayon réfracté n'a pas pu être calculé
     */
    static Rayon rayonRefracte(Obstacle o, Rayon r, Environnement env,boolean calcul_transmittance) throws Exception {
        Point2D inter = (r.arrivee() !=null)? r.arrivee() :o.premiere_intersection(r) ;

        Point2D oppose_vecteur_incident = r.direction().multiply(-1.0) ;
        Point2D normale                 = o.normale(inter) ;


        double i1 = normale.angle(oppose_vecteur_incident) ;

        double n1 = r.indice_milieu_traverse ;
        double n2 = o.indiceRefraction() ;

        // Pour la suite, il faut que la normale et l'opposé du vecteur incident soient orientés du même côté
        // (autrement dit rayon incident et normale vont à la rencontre l'un de l'autre,  ce qui signifie que le rayon entre
        // dans le milieu de l'obstacle rencontré)
        if (normale.dotProduct(oppose_vecteur_incident)<0) { // Le rayon sort de l'obstacle rencontré
            normale = normale.multiply(-1.0);
            Obstacle obs_emergence = env.autre_obstacle_contenant(inter,o) ;

            n2 = obs_emergence!=null?obs_emergence.indiceRefraction():env.indiceRefraction() ;
        }

        Point2D oppose_normale          = normale.multiply(-1.0) ;

        LOGGER.log(Level.FINER,"Angle normale : {0}°",normale.angle(new Point2D(1,0))) ;
        LOGGER.log(Level.FINER,"Angle opposé de la normale : {0}°",oppose_normale.angle(new Point2D(1,0)));

        LOGGER.log(Level.FINER,"Angle i1 : {0}°"+normale.angle(oppose_vecteur_incident));
        LOGGER.log(Level.FINER,"Angle i1 : {0}°"+oppose_vecteur_incident.angle(normale));

        double sens_rotation ;

        if (oppose_vecteur_incident.crossProduct(normale).getZ() > 0 )
            sens_rotation = -1.0 ;
        else
            sens_rotation = +1.0 ;

        // TODO : Vérifier ces calculs en affichant les angles des vecteurs incidents, réfléchis et de la normale

        LOGGER.log(Level.FINEST,"n1 : {0}"+n1);
        LOGGER.log(Level.FINEST,"n2 : {0}"+n2);

        double sinus_i2 = (n1/n2)*Math.sin(Math.toRadians(i1)) ;

        // A-t-on atteint l'angle de réflexion totale ?
        if (Math.abs(sinus_i2)>1.0) {
            LOGGER.log(Level.FINER,"Réflexion totale : pas de rayon transmis");
            return null ;
//            return rayonReflechi(o, r, Rayon.PhenomeneOrigine.REFLEXION);
        }

        double i2 = Math.toDegrees(Math.asin(sinus_i2)) ;

        // Affine rotation = new Affine() ;
        // rotation.appendRotation(sens_rotation*i2);
        Rotate rotation = new Rotate(sens_rotation*i2) ;

        Rayon r_refracte = null ;

        if (!calcul_transmittance) // Pas de calcul de transmittance : toute la puissance du rayon incident est transmise au rayon réfracté
              r_refracte =  new Rayon(inter, rotation.transform(oppose_normale), n2, Rayon.PhenomeneOrigine.TRANSMISSION,r.ratio_puissance) ;
        else {
            double cos_theta_i = Math.cos(Math.toRadians(i1)) ;
            double cos_theta_t = Math.cos(Math.toRadians(i2)) ;
            double coeff_reflexion_te = (n1*cos_theta_i-n2*cos_theta_t)/(n1*cos_theta_i+n2*cos_theta_t) ;
            double coeff_reflexion_tm = (n1*cos_theta_t-n2*cos_theta_i)/(n1*cos_theta_t+n2*cos_theta_i) ;
            double reflectance_te = coeff_reflexion_te*coeff_reflexion_te ;
            double reflectance_tm = coeff_reflexion_tm*coeff_reflexion_tm ;
            double transmittance_te = 1.0 - reflectance_te ;
            double transmittance_tm = 1.0 - reflectance_tm ;

            if (!r.est_polarisee) {
                // rayon incident non polarisé (= polarisation changeante à chaque instant) : la reflectance est la moyenne
                // des réflectances d'une lumière polarisée TE et d'une lumière polarisée TM

                double moyenne_reflectance = 0.5*(reflectance_te+reflectance_tm) ;
                double moyenne_transmittance = 1.0 - moyenne_reflectance ;

                r_refracte = new Rayon(inter, rotation.transform(oppose_normale), n2, Rayon.PhenomeneOrigine.TRANSMISSION, moyenne_transmittance*r.ratio_puissance,false,0.0);
                // NB : on vient de supposer que l'onde transmise n'est absolument pas polarisée alors qu'elle l'est
                // en partie à l'issue de la réfraction, de même que l'onde réflechie.
                // TODO : introduire (et calculer) un taux de polarisation après chaque réflexion/réfraction ??
            }
            else // Onde incidente polarisée rectilignement
            {

                if (r.angle_champ_electrique==0.0) // E_tm
                    r_refracte = new Rayon(inter, rotation.transform(oppose_normale), n2, Rayon.PhenomeneOrigine.TRANSMISSION, transmittance_tm*r.ratio_puissance,true,0.0);
                else if (r.angle_champ_electrique==90.0) // E_te
                    r_refracte = new Rayon(inter, rotation.transform(oppose_normale), n2, Rayon.PhenomeneOrigine.TRANSMISSION, transmittance_te*r.ratio_puissance,true,90.0);
                else {
                    // tan_phi = E_te_incident/E_tm_incident
                    double cos_phi_i = Math.cos(Math.toRadians(r.angle_champ_electrique)) ;
                    double sin_phi_i = Math.sin(Math.toRadians(r.angle_champ_electrique)) ;

                    double transmittance_resultante = cos_phi_i*cos_phi_i*transmittance_tm + sin_phi_i*sin_phi_i*transmittance_te ;

                    // Calcul de l'angle de polarisation du champ E du rayon transmis
                    double phi_t = Math.atan(Math.sqrt((transmittance_te/transmittance_tm))*Math.tan(Math.toRadians(r.angle_champ_electrique))) ;

                    r_refracte = new Rayon(inter, rotation.transform(oppose_normale), n2, Rayon.PhenomeneOrigine.TRANSMISSION, transmittance_resultante * r.ratio_puissance, true, phi_t);

                }

            }

        }


        LOGGER.log(Level.FINER,"Angle i1 : {0}°"+normale.angle(oppose_vecteur_incident));

        LOGGER.log(Level.FINER,"Angle rayon réfracté  : {0}°", r_refracte.direction().angle(new Point2D(1,0)));
        LOGGER.log(Level.FINER,"Angle i2 : {0}°",i2);

        LOGGER.log(Level.FINER,"Ratio puissance du rayon réfracté  : {0}",r_refracte.ratio_puissance);
        LOGGER.log(Level.FINER,"Rayon réfracté polarisé ? {0}",r_refracte.est_polarisee);

        LOGGER.log(Level.FINER,"Angle polarisation champ E du rayon réfracté : {0}",r_refracte.angle_champ_electrique);


        if(Math.abs(n1*Math.sin(Math.toRadians(i1)) - n2*Math.sin(Math.toRadians(i2)))>0.000001) {
            LOGGER.log(Level.SEVERE,"ALERTE n1*sin(i1) différent de n2*sin(i2)");
//            throw new Exception("L'angle i2 du rayon réfléchi est différent de l'angle i1 du rayon incident. (i1 = "+normale.angle(oppose_vecteur_incident)+"° et i2 ="+normale.angle(r_reflechi.direction)+"°)") ;
        }

        return  r_refracte ;

    }

    /**
     * Calcule les rayons réfractés et réfléchis produits par l'incidence d'un rayon incident sur un obstacle
     * @param o : l'obstacle rencontré par le rayon
     * @param r : le rayon incident (le milieu d'origine du rayon est déjà défini par l'appelant [dans r.indice_milieu_traverse], ainsi que son point
     *          d'intersection avec l'obstacle o [dans r.arrivee()})
     * @param env : l'environnement
     * @return Les rayons réfractés et réfléchis (l'un ou l'autre peuvent être à 'null' s'ils n'existent pas)
     * @throws Exception
     */
    static RayonsRefracteEtReflechi rayonsRefracteEtReflechi(Obstacle o, Rayon r, Environnement env) throws Exception {

        RayonsRefracteEtReflechi rayons_res = new RayonsRefracteEtReflechi() ;
        // NB : rayon_reflechi et rayon_refracte sont maintenant initialisés à 'null' par défaut

        // Pas de rayon réfléchi ni de rayon réfracté si la surface de l'obstacle o rencontré est absorbante
        if (o.traitementSurface()==TraitementSurface.ABSORBANT)
            return rayons_res ;

        // Pourrait se remplacer par Point2D inter = r.arrivee() ; car l'appelant a toujours fait d'avance le calcul de l'intersection
        Point2D inter = (r.arrivee() !=null)? r.arrivee() :o.premiere_intersection(r) ;

        boolean reflexion_totale = false ;

        Point2D oppose_vecteur_incident = r.direction().multiply(-1.0) ;
        Point2D normale                 = o.normale(inter) ;
        // NB : si o est un obstacle sans épaisseur (PAS_DE_MILIEU), le sens de la normale est indéterminé

        rayons_res.normale = normale ;

        double n1 = r.indice_milieu_traverse ;
        double i1 = normale.angle(oppose_vecteur_incident) ;

        if (i1>90.0)
            i1=180.0-i1 ;

        double sens_rotation_refraction ;
        double sens_rotation_reflexion ;

        // Le rayon incident et la normale vont-ils bien "à la rencontre" l'un de l'autre (si l'obstacle a une épaisseur
        // cela signifie que le rayon "émerge" de l'obstacle ; si l'obstacle n'a pas d'épaisseur cette notion n'a pas de sens)
        boolean rayon_sort = (normale.dotProduct(oppose_vecteur_incident)<0) ;

        // Si rayon incident et normale ne vont pas à la rencontre l'un de l'autre, on retourne la normale pour que ce soit le cas
        if (rayon_sort)
            normale = normale.multiply(-1.0);

        if (oppose_vecteur_incident.crossProduct(normale).getZ() > 0)
            sens_rotation_reflexion = +1.0;
        else
            sens_rotation_reflexion = -1.0;

        sens_rotation_refraction = - sens_rotation_reflexion ;

        // Recherche de l'obstacle d'émergence
        Obstacle obs_emergence = env.obstacle_emergence(r,inter,o) ;

        double n2 = (obs_emergence!=null? obs_emergence.indiceRefraction() : env.indiceRefraction()) ;
//        double n2 = (o.natureMilieu()!=NatureMilieu.PAS_DE_MILIEU)?o.indiceRefraction():r.indice_milieu_traverse ;

//        // Pour la suite, il faut que la normale et l'opposé du vecteur incident soient orientés du même côté
//        // (autrement dit rayon incident et normale aillent à la rencontre l'un de l'autre, ce qui signifie que le rayon entre
//        // dans le milieu de l'obstacle rencontré)
//        if (rayon_sort) { // Le rayon sort de l'obstacle rencontré
////            normale = normale.multiply(-1.0);
////            sens_rotation_refraction = -1.0*sens_rotation_refraction ;
//
////            if (o.natureMilieu()!=NatureMilieu.PAS_DE_MILIEU) { // Si on n'est pas dans le cas d'un obstacle sans épaisseur (segment ou autre...)
//                obs_emergence = env.autre_obstacle_contenant(inter, o);
//                // NB : obs_emergence ne peut pas être un élément sans épaisseur (sans milieu) car un tel élément ne contient jamais de point
//
////                 if (o.natureMilieu()!=NatureMilieu.PAS_DE_MILIEU)
////                n2 = (obs_emergence != null) ? obs_emergence.indiceRefraction() : env.indiceRefraction();
//
//                 if (obs_emergence!=null)
//                     n2 = obs_emergence.indiceRefraction() ;
//                 else if (o.natureMilieu()!=NatureMilieu.PAS_DE_MILIEU)
//                     n2 = env.indiceRefraction() ;
////            }
//        } else { // Le rayon n'émerge pas de l'obstacle o : il rentre donc dedans
//            if (o.natureMilieu()!=NatureMilieu.PAS_DE_MILIEU)
//                obs_emergence=o ;
//        }

        // Il faut s'assurer que l'obstacle d'émergence n'est pas absorbant : sinon il n'y a ni rayon réfracté, ni rayon réfléchi, à moins
        // que la surface de l'obstacle o que rencontre le rayon soit partiellement ou totalement réfléchissante
        if (obs_emergence!=null && obs_emergence.natureMilieu()==NatureMilieu.ABSORBANT
                && o.traitementSurface()!=TraitementSurface.REFLECHISSANT
                && o.traitementSurface()!=TraitementSurface.PARTIELLEMENT_REFLECHISSANT)
            return rayons_res ;

        Point2D oppose_normale          = normale.multiply(-1.0) ;

        double sinus_i2 = (n1/n2)*Math.sin(Math.toRadians(i1)) ;

        // A-t-on atteint l'angle de réflexion totale ?
        if (Math.abs(sinus_i2)>1.0)
            reflexion_totale = true ;

        // Comme on a écarté le cas du milieu ABSORBANT ou de la surface ABSORBANT plus haut, il faut calculer un rayon
        // réfléchi si :
        // réflexion totale
        // ou env avec reflexions de Fresnel qui accompagnent les réfractions
        // ou surface de l'obstacle partiellement ou totalement réfléchissante
        if ( (reflexion_totale || env.reflexionAvecRefraction()
                 || o.traitementSurface()==TraitementSurface.REFLECHISSANT
                 || o.traitementSurface()==TraitementSurface.PARTIELLEMENT_REFLECHISSANT)) {
//             Si l'obstacle o rencontré est concave, il est possible que le rayon parte de la SURFACE de ce dernier, et
//             soit réfléchi par cet obstacle lui-même. Mais si le point de départ est dans la masse de l'obstacle, et
//             pas à sa surface, il n'y a pas de rayon réfléchi, sauf peut-être si le milieu de l'obstacle est transparent (si on
//             a atteint, ou dépassé, l'angle de la réflexion totale)
//            if (o.contient(r.depart()) && !o.aSurSaSurface(r.depart()) && (o.natureMilieu()!=NatureMilieu.TRANSPARENT)) {
//                    rayons_res.rayon_reflechi = null ;
//            }
//            else
//            {

                // TODO : Vérifier ces calculs en affichant les angles des vecteurs incidents, réfléchis et de la normale

                Rotate rotation_reflexion = new Rotate(sens_rotation_reflexion * 2 * oppose_vecteur_incident.angle(normale)) ;

                // On calcule d'abord un rayon réfléchi total, on calculera plus loin sa réflectance et sa polarisation si besoin
                    rayons_res.rayon_reflechi = new Rayon(inter, rotation_reflexion.transform(oppose_vecteur_incident), r.indice_milieu_traverse,
                            Rayon.PhenomeneOrigine.REFLEXION,
                            ((reflexion_totale||o.traitementSurface()==TraitementSurface.REFLECHISSANT)?r.ratio_puissance:r.ratio_puissance*o.tauxReflexionSurface()),
                            r.est_polarisee, r.angle_champ_electrique);
                // NB : si env.reflexionAvecRefraction() et qu'on n'est pas dans un cas de réflexion totale, et que la surface est
                // partiellement réflechissante, il faudra ajouter à la puissance réfléchieune part de puissance issue de la réflexion
                // de Fresnel

                // TODO : A mettre en assertion
                if(Math.abs(normale.angle(rayons_res.rayon_reflechi.direction())-normale.angle(oppose_vecteur_incident))>0.000001) {
                    LOGGER.log(Level.SEVERE, "ALERTE : L'angle i1' du rayon réfléchi est différent de l'angle i1' du rayon incident. (i1 = {0} , i1' = {0})",new Object[] {i1,normale.angle(rayons_res.rayon_reflechi.direction())});

//                    throw new Exception("L'angle i2 du rayon réfléchi est différent de l'angle i1 du rayon incident. (i1 = "+i1+"° et i2 ="+normale.angle(rayons_res.rayon_reflechi.direction)+"°)") ;

//                }


            }
        } // Fin du blc de calcul du rayon réfléchi

        // Si l'obstacle rencontré est purement réfléchissant (à 100%), on s'arrête là (pas de rayon réfracté à calculer)
        if (o.traitementSurface()==TraitementSurface.REFLECHISSANT)
            return rayons_res ;

        // Idem si le rayon émerge dans un milieu absorbant
        if (obs_emergence!=null && obs_emergence.natureMilieu()==NatureMilieu.ABSORBANT)
            return rayons_res ;


        // Traitement du rayon réfracté (ou transmis sans réfraction dans le cas d'une surface PARTIELLEMENT_REFLECHISSANTE
        // d'un obstacle sans épaisseur)

        LOGGER.log(Level.FINER,"Angle normale : {0}°",normale.angle(new Point2D(1,0))) ;
        LOGGER.log(Level.FINER,"Angle opposé de la normale : {0}°",oppose_normale.angle(new Point2D(1,0)));

        LOGGER.log(Level.FINER,"Angle i1 : {0}°",normale.angle(oppose_vecteur_incident));
//      LOGGER.log(Level.FINER,"Angle i1 : {0}°",oppose_vecteur_incident.angle(normale));

        // TODO : Vérifier ces calculs en affichant les angles des vecteurs incidents, réfléchis et de la normale

        LOGGER.log(Level.FINER,"n1 : {0}",n1);
        LOGGER.log(Level.FINER,"n2 : {0}",n2);


        // A-t-on atteint l'angle de réflexion totale ?
        if (reflexion_totale) {
            LOGGER.log(Level.FINER,"Réflexion totale : pas de rayon transmis");
            rayons_res.rayon_refracte = null ; // Inutile : déjà initialisé à null...

            // On avait déjà calculé le rayon réfléchi total : on a donc terminé
            return rayons_res ;
        }

        // On n'est pas à la réflexion totale

        // Oublier le rayon réfléchi si on a juste une réfraction totale à calculer (normalement, on a déjà évité de calculer
        // un rayon réfléchi dans ce cas) et qu'il n'y a pas de réflexion partielle à la surface de l'obstacle (dachant que le
        // cas où la surface est totalement réfléchissante a déjà été écarté)
        if(!env.reflexionAvecRefraction() && o.traitementSurface()!=TraitementSurface.PARTIELLEMENT_REFLECHISSANT)
            rayons_res.rayon_reflechi = null ;

        double i2 = Math.toDegrees(Math.asin(sinus_i2)) ;

//        Affine rotation_refraction = new Affine() ;
//        rotation_refraction.appendRotation(sens_rotation_refraction*i2);

        Rotate rotation_refraction = new Rotate(sens_rotation_refraction*i2) ;

        // On commence par calculer le rayon réfracté "total", sans calcul de transmittance

//        Rayon r_refracte = null ;

        // Prise en compte des effets éventuels d'une surface polarisante
        boolean nouvelle_polarisation = o.traitementSurface() == TraitementSurface.POLARISANT || r.est_polarisee;
        double nouvel_angle_champ_electrique = (o.traitementSurface()==TraitementSurface.POLARISANT?o.orientationAxePolariseur():r.angle_champ_electrique) ;

        double coeff_dim_puissance_transmise_par_polariseur = 1.0 ;

        if (o.traitementSurface()==TraitementSurface.POLARISANT) {
            if (!r.est_polarisee) // Lumière non polarisée qui arrive sur une surface polarisante : il n'en reste alors que 50%
                coeff_dim_puissance_transmise_par_polariseur = 0.5 ;
            else // Lumière polarisée qui arrive sur une surface polarisante : Loi de Malus I=Io*Cos^2(Ecart angle polariseur/champ E)
                coeff_dim_puissance_transmise_par_polariseur = Math.pow(Math.cos(Math.toRadians(r.angle_champ_electrique-o.orientationAxePolariseur())),2) ;
        }


        if (!env.reflexionAvecRefraction()) {
            // Pas de calcul de transmittance : toute la puissance du rayon incident est transmise au rayon réfracté
//            if (o.traitementSurface()!=TraitementSurface.PARTIELLEMENT_REFLECHISSANT)
//                    rayons_res.rayon_refracte = new Rayon(inter, rotation_refraction.transform(oppose_normale), n2,
//                    Rayon.PhenomeneOrigine.TRANSMISSION,
//                    r.ratio_puissance);
//            else {
                rayons_res.rayon_refracte = new Rayon(inter, rotation_refraction.transform(oppose_normale), n2,
                        Rayon.PhenomeneOrigine.TRANSMISSION,
                        r.ratio_puissance*(1-o.tauxReflexionSurface())*coeff_dim_puissance_transmise_par_polariseur,
                        nouvelle_polarisation,
                        nouvel_angle_champ_electrique);

//            }

        }
        else {

            double cos_theta_i = Math.cos(Math.toRadians(i1)) ;
            double cos_theta_t = Math.cos(Math.toRadians(i2)) ;
            double coeff_reflexion_te = (n1*cos_theta_i-n2*cos_theta_t)/(n1*cos_theta_i+n2*cos_theta_t) ;
            double coeff_reflexion_tm = (n1*cos_theta_t-n2*cos_theta_i)/(n1*cos_theta_t+n2*cos_theta_i) ;

            double reflectance_te = coeff_reflexion_te*coeff_reflexion_te ;
            double reflectance_tm = coeff_reflexion_tm*coeff_reflexion_tm ;

            // TODO à transformer en assertion, ou déclencher une exception et l'attraper dans Souce.calculerCheminDuRayon
            if (reflectance_te>1.0 )
                LOGGER.log(Level.SEVERE,"ALERTE : La réflectance TE doit être inférieure à 1. Valeur : {0})",reflectance_te);
            if (reflectance_tm>1.0 )
                LOGGER.log(Level.SEVERE,"ALERTE : La réflectance TM doit être inférieure à 1. Valeur : {0})",reflectance_tm);

            double transmittance_te = 1.0 - reflectance_te ;
            double transmittance_tm = 1.0 - reflectance_tm ;


            if (!nouvelle_polarisation) {
                // Rayon émergent non polarisé (= polarisation changeante à chaque instant) : la reflectance est la moyenne
                // des réflectances d'une lumière polarisée TE et d'une lumière polarisée TM

                double moyenne_reflectance = 0.5*(reflectance_te+reflectance_tm) ;
                double moyenne_transmittance = 1.0 - moyenne_reflectance ;

                rayons_res.rayon_refracte = new Rayon(inter, rotation_refraction.transform(oppose_normale), n2,
                        Rayon.PhenomeneOrigine.TRANSMISSION,
                        moyenne_transmittance*r.ratio_puissance*(1-o.tauxReflexionSurface())*coeff_dim_puissance_transmise_par_polariseur,
                        nouvelle_polarisation,
                        nouvel_angle_champ_electrique);
                // NB : on vient de supposer que l'onde transmise n'est absolument pas polarisée alors qu'elle l'est
                // en partie à l'issue de la réfraction, de même que l'onde réflechie.
                // TODO : introduire (et calculer) un taux de polarisation après chaque réflexion/réfraction ??

                rayons_res.rayon_reflechi.ratio_puissance =
                        r.ratio_puissance*o.tauxReflexionSurface() + moyenne_reflectance * r.ratio_puissance*(1-o.tauxReflexionSurface()) ;
            }
            else // Onde incidente polarisée rectilignement
            {

                if (nouvel_angle_champ_electrique==0.0) // E_tm
                {
                    rayons_res.rayon_refracte = new Rayon(inter, rotation_refraction.transform(oppose_normale), n2,
                            Rayon.PhenomeneOrigine.TRANSMISSION,
                            transmittance_tm * r.ratio_puissance*(1-o.tauxReflexionSurface())*coeff_dim_puissance_transmise_par_polariseur,
                            true, 0.0) ;

                    rayons_res.rayon_reflechi.ratio_puissance = r.ratio_puissance*o.tauxReflexionSurface() + reflectance_tm  * r.ratio_puissance*(1-o.tauxReflexionSurface()) ;
                    rayons_res.rayon_reflechi.est_polarisee = true ;
                    rayons_res.rayon_reflechi.angle_champ_electrique = 0.0 ;
                }
                else if (nouvel_angle_champ_electrique==90.0) // E_te
                {
                    rayons_res.rayon_refracte = new Rayon(inter, rotation_refraction.transform(oppose_normale), n2,
                            Rayon.PhenomeneOrigine.TRANSMISSION,
                            transmittance_te * r.ratio_puissance*(1-o.tauxReflexionSurface())*coeff_dim_puissance_transmise_par_polariseur,
                            true, 90.0) ;

                    rayons_res.rayon_reflechi.ratio_puissance = r.ratio_puissance*o.tauxReflexionSurface() + reflectance_te  * r.ratio_puissance *(1-o.tauxReflexionSurface());
                    rayons_res.rayon_reflechi.est_polarisee = true ;
                    rayons_res.rayon_reflechi.angle_champ_electrique = 90.0 ;
                }
                else
                {
                    // tan_phi = E_te_incident/E_tm_incident
                    double cos_phi_i = Math.cos(Math.toRadians(nouvel_angle_champ_electrique)) ;
                    double sin_phi_i = Math.sin(Math.toRadians(nouvel_angle_champ_electrique)) ;

                    double transmittance_resultante = cos_phi_i*cos_phi_i*transmittance_tm + sin_phi_i*sin_phi_i*transmittance_te ;

                    // Calcul de l'angle de polarisation du champ E du rayon transmis
                    double phi_t = Math.atan(Math.sqrt(transmittance_te/transmittance_tm)*Math.tan(Math.toRadians(r.angle_champ_electrique))) ;

                    rayons_res.rayon_refracte = new Rayon(inter, rotation_refraction.transform(oppose_normale), n2,
                            Rayon.PhenomeneOrigine.TRANSMISSION,
                            transmittance_resultante * r.ratio_puissance*(1-o.tauxReflexionSurface())*coeff_dim_puissance_transmise_par_polariseur,
                            true, phi_t);

                    double reflectance_resultante = cos_phi_i*cos_phi_i*reflectance_tm + sin_phi_i*sin_phi_i*reflectance_te ;

                    // Calcul de l'angle de polarisation du champ E du rayon réfléchi
                    double phi_r = Math.atan(Math.sqrt((reflectance_te/reflectance_tm))*Math.tan(Math.toRadians(r.angle_champ_electrique))) ;

                    rayons_res.rayon_reflechi.ratio_puissance = r.ratio_puissance*o.tauxReflexionSurface() + reflectance_resultante * r.ratio_puissance * (1-o.tauxReflexionSurface()) ;
                    rayons_res.rayon_reflechi.est_polarisee = true ;
                    rayons_res.rayon_reflechi.angle_champ_electrique = phi_r ;

                }

            }

        }

        LOGGER.log(Level.FINER,"Angle i1 : {0}°",normale.angle(oppose_vecteur_incident));

        LOGGER.log(Level.FINER,"Ratio puissance du rayon réfléchi  : {0}",(rayons_res.rayon_reflechi!=null)?(rayons_res.rayon_reflechi.ratio_puissance):0.0);

        LOGGER.log(Level.FINER,"Angle rayon réfracté  : {0}°", rayons_res.rayon_refracte.direction().angle(new Point2D(1,0)));
        LOGGER.log(Level.FINER,"Angle i2 : {0}°",i2);

        LOGGER.log(Level.FINER,"Ratio puissance du rayon réfracté  : {0}",rayons_res.rayon_refracte.ratio_puissance);
        LOGGER.log(Level.FINER,"Rayon réfracté polarisé ? {0}",rayons_res.rayon_refracte.est_polarisee);

        LOGGER.log(Level.FINER,"Angle polarisation champ E du rayon réfracté : {0}",rayons_res.rayon_refracte.angle_champ_electrique);

        // TODO : Transformer en assertion
        if(Math.abs(n1*Math.sin(Math.toRadians(i1)) - n2*Math.sin(Math.toRadians(i2)))>0.000001) {
            LOGGER.log(Level.SEVERE,"ALERTE n1*sin(i1) différent de n2*sin(i2)");
//            throw new Exception("L'angle i2 du rayon réfléchi est différent de l'angle i1 du rayon incident. (i1 = "+normale.angle(oppose_vecteur_incident)+"° et i2 ="+normale.angle(r_reflechi.direction)+"°)") ;
        }

        return rayons_res ;
    }

}
