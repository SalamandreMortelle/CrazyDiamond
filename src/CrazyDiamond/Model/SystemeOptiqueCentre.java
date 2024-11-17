package CrazyDiamond.Model;

import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class SystemeOptiqueCentre extends BaseElementNommable implements Nommable {
    private final Environnement environnement;
    private final ObjectProperty<PositionEtOrientation> axe;

    private static final Logger LOGGER = Logger.getLogger( "CrazyDiamond" );

    private final ObjectProperty<Color> couleur_axe;
    private static final Color couleur_axe_par_defaut = Color.WHITE ;

    // Liste des obstacles (obligatoirement des surfaces de révolution centrées sur l'axe du SOC)
    private final ListProperty<Obstacle> obstacles_centres ;

    /**
     * Matrice de transfert optique en optique paraxiale, entre les plans de référence d'abscisses z_plan_entree et
     * z_plan_sortie Seules les 4 composantes xx,xy,yx et yy de la matrice sont significatives.
     */
    private final ObjectProperty<Affine> matrice_transfert_es;

    private final BooleanProperty montrer_dioptres;
    private final BooleanProperty montrer_objet;
    private final BooleanProperty montrer_image;
    private final BooleanProperty montrer_plans_focaux;
    private final BooleanProperty montrer_plans_principaux;
    private final BooleanProperty montrer_plans_nodaux;

    // Propriétés calculées du système optique centré

    /**
     * Liste ordonnée dans le sens des Z croissants et des rayons de courbures "croissants" (de 0- à -infini et de +infini
     * à 0+) de tous les dioptres du système. Le signe des rayons de courbure est celui qui correspond à un rayon qui progresse
     * dans le sens des Z croissants. Les autres propriétés de chacun de ces dioptres sont renseignées également
     * (cf. attributs de la classe DioptreParaxial).
     */
    private final ListProperty<DioptreParaxial> dioptres;

    /**
     * Liste de tous les dioptres réellement rencontrés par un rayon arrivant sur l'axe, dans le sens des Z croissants mais
     * qui peut ensuite subir des réflexions et progresser dans le sens opposé, auquel cas les signes des rayons de courbure
     * et les indices avant/après la rencontre du dioptre sont adaptés en conséquence (indices avant/après permutés, et
     * rayons de courbure multipliés par -1).
     */
    private final ListProperty<RencontreDioptreParaxial> dioptres_rencontres;

    private boolean suspendre_repositionnement_obstacles = false;

    /**
     * Abscisse du plan de référence d'entrée du système optique, positionné sur le premier dioptre du système ayant la
     * plus petite abscisse.
     * Pour le plan d'entrée, cette abscisse est à la fois une abscisse géométrique et optique. Lorsque la lumière
     * rencontre le plan d'entrée du SOC, elle n'a subi aucune réflexion, susceptible d'avoir "replié" l'axe optique.
     */
    private final SimpleObjectProperty<Double> z_plan_entree;

    /**
     * Indice du milieu "avant" le SOC, abstraction faite des milieux des obstacles qui n'appartiennent pas au SOC
     */
    private final DoubleProperty n_entree ;

    /**
     * Abscisse géométrique du plan de référence de sortie du système optique, positionné sur le dernier dioptre
     * rencontré par un rayon confondu avec l'axe du système et émis depuis le plan d'entrée, dans le sens des Z
     * croissants.
     */
    private final SimpleObjectProperty<Double> z_geometrique_plan_sortie;
    /**
     * Abscisse optique du plan de référence de sortie du système optique, positionné sur le dernier dioptre
     * rencontré par un rayon confondu avec l'axe du système et émis depuis le plan d'entrée, dans le sens des Z
     * croissants.
     * Si la lumière subit des réflexions dans le SOC, l'axe optique "se replie" et le Z optique n'est plus forcément
     * identique au Z geometrique.
     */
    private final SimpleObjectProperty<Double> z_optique_plan_sortie ;
    /**
     * Indice du milieu "après" le SOC, dans le sens du rayon sortant du système, abstraction faite des milieux des
     * obstacles qui n'appartiennent pas au SOC
     */
    private final DoubleProperty n_sortie ;

    /**
     *  Booléen indiquant si le rayon qui sort du système est orienté dans le sens des X croissants. Si le système comporte une
     * surface totalement réfléchissante ou réfléchissante à plus de 50%, ce rayon sera orienté dans le sens des X décroissants
     */
    final BooleanProperty sens_plus_en_sortie ;


    /** Z geométrique du plan focal objet */
    private final SimpleObjectProperty<Double> z_geometrique_plan_focal_objet;
    /** Z optique du plan focal objet */
    private final SimpleObjectProperty<Double> z_optique_plan_focal_objet;
    /** Z geométrique du plan focal image */
    private final SimpleObjectProperty<Double> z_geometrique_plan_focal_image;
    /** Z optique du plan focal image */    
    private final SimpleObjectProperty<Double> z_optique_plan_focal_image;

    /** Z geométrique du plan principal objet */
    private final SimpleObjectProperty<Double> z_geometrique_plan_principal_objet;
    /** Z optique du plan principal objet */    
    private final SimpleObjectProperty<Double> z_optique_plan_principal_objet;

    /** Z geométrique du plan principal image */
    private final SimpleObjectProperty<Double> z_geometrique_plan_principal_image;

    /** Z optique du plan principal objet */    
    private final SimpleObjectProperty<Double> z_optique_plan_principal_image;

    /** Z geométrique du plan nodal objet */
    private final SimpleObjectProperty<Double> z_geometrique_plan_nodal_objet;
    /** Z optique du plan nodal objet */    
    private final SimpleObjectProperty<Double> z_optique_plan_nodal_objet;
    /** Z geométrique du plan nodal image */
    private final SimpleObjectProperty<Double> z_geometrique_plan_nodal_image;
    /** Z optique du plan nodal objet */    
    private final SimpleObjectProperty<Double> z_optique_plan_nodal_image;


//    /** Z optique de l'objet */
//    private final SimpleObjectProperty<Double> z_optique_objet;
    /** Z géométrique de l'objet */
    private final SimpleObjectProperty<Double> z_geometrique_objet;
    private final SimpleObjectProperty<Double> h_objet ;
    /** Z optique de l'image */
    private final SimpleObjectProperty<Double> z_optique_image;
    /** Z géométrique de l'image */
    private final SimpleObjectProperty<Double> z_geometrique_image;
    private final SimpleObjectProperty<Double> h_image ;

    private final SimpleObjectProperty<Double> grandissement_transversal ;
    private final SimpleObjectProperty<Double> grandissement_angulaire ;
    private final SimpleObjectProperty<Double> grandissement_longitudinal ;
    private final SimpleObjectProperty<Double> z_pupille_entree ;
    private final SimpleObjectProperty<Double> r_pupille_entree;

    // Demi-angle sous lequel on voit la Pupille d'entrée depuis le plan objet (en degrés)
    private final SimpleObjectProperty<Double> angle_ouverture ;

    private final SimpleObjectProperty<Double> r_champ_moyen_objet;
    private final SimpleObjectProperty<Double> r_champ_pleine_lumiere_objet;
    private final SimpleObjectProperty<Double> r_champ_total_objet;
    private final SimpleObjectProperty<Double> r_champ_moyen_image;
    private final SimpleObjectProperty<Double> r_champ_pleine_lumiere_image;
    private final SimpleObjectProperty<Double> r_champ_total_image;
    private final SimpleObjectProperty<Double> angle_champ_moyen_objet ;
    private final SimpleObjectProperty<Double> angle_champ_pleine_lumiere_objet ;
    private final SimpleObjectProperty<Double> angle_champ_total_objet ;
    private final SimpleObjectProperty<Double> angle_champ_moyen_image ;
    private final SimpleObjectProperty<Double> angle_champ_pleine_lumiere_image ;
    private final SimpleObjectProperty<Double> angle_champ_total_image ;
    private final SimpleObjectProperty<Double> z_pupille_sortie ;
    private final SimpleObjectProperty<Double> r_pupille_sortie;

    private final SimpleObjectProperty<Double> z_lucarne_entree ;
    private final SimpleObjectProperty<Double> r_lucarne_entree;
    private final SimpleObjectProperty<Double> z_lucarne_sortie ;
    private final SimpleObjectProperty<Double> r_lucarne_sortie;

    private boolean suspendre_calcul_elements_cardinaux = false;

    // Flag et valeur de z_image précalculés lors d'une conversion d'unités, car le re-calcul correct de z_image et h_image
    // n'est pas possible tant que l'unité de l'Environnement n'est pas mis à jour (elle ne l'est qu'après le changement
    // d'unité du SOC ; cf. Environnement::changerUnite()
    private boolean nouveau_z_optique_image_apres_conversion_a_prendre_compte = false;
    private Double nouveau_z_optique_image_apres_conversion = null ;

    private boolean nouveau_g_t_a_prendre_compte_apres_conversion = false;
    private Double nouveau_g_t_apres_conversion = null ;


    // Flag et valeur de h_image précalculés lors d'une conversion d'unités, car le re-calcul correct de z_image et h_image
    // n'est pas possible tant que l'unité de l'Environnement n'est pas mis à jour (elle ne l'est qu'après le changement
    // d'unité du SOC ; cf. Environnement::changerUnite()
    private boolean nouveau_h_image_apres_conversion_a_prendre_compte = false;
    private Double nouveau_h_image_apres_conversion = null ;

    public boolean reference(Obstacle obstacle) {
        return obstacles_centres.contains(obstacle);
    }


    private class IterateurObstaclesCentresReels implements Iterator<Obstacle> {
        private final Iterator<Obstacle> it_obstacles_reels ;
        public IterateurObstaclesCentresReels() {

            ArrayList<Obstacle> obstacles_centres_reels = new ArrayList<>(obstacles_centres.size()) ;

            for (Obstacle oc : obstacles_centres) {
                if (oc instanceof Groupe) {
                    for (Obstacle o_reel : ((Groupe) oc).iterableObstaclesReelsDepuisArrierePlan()) {
                        obstacles_centres_reels.add(o_reel) ;
                    }
                } else
                    obstacles_centres_reels.add(oc) ;
            }

            it_obstacles_reels = obstacles_centres_reels.iterator() ;
        }

        @Override public boolean hasNext() {return it_obstacles_reels.hasNext() ;}

        @Override public Obstacle next() {return it_obstacles_reels.next() ;
        }
    }


    public void definirPosition(Point2D pos) { axe.set(new PositionEtOrientation(pos,orientation()));}

    public void definirZObjet(double z) { z_geometrique_objet.set(z) ; }
    public void definirHObjet(double h) { h_objet.set(h) ; }

    public void definirMontrerObjet(boolean mo) {montrer_objet.set(mo);}

    public void definirMontrerImage(boolean mi) {montrer_image.set(mi);}

    public void definirMontrerPlansFocaux(boolean mpf) { montrer_plans_focaux.set(mpf);}

    public void definirMontrerPlansPrincipaux(boolean mpp) { montrer_plans_principaux.set(mpp); }

    public void definirMontrerPlansNodaux(boolean mpn) { montrer_plans_nodaux.set(mpn); }

    public void definirMontrerDioptres(boolean md) { montrer_dioptres.set(md); }

    public void deplacerObstacle(Obstacle o_a_deplacer, int i_pos_parmi_obstacles_reels_de_env) {
        obstacles_centres.remove(o_a_deplacer) ;

        int i_pos_cible_dans_soc = -1 ;

        for (Obstacle oc : obstacles_centres) { // Parcours des obstacles centrés
            if (environnement.indexParmiObstacles(oc)>i_pos_parmi_obstacles_reels_de_env) {
                // On s'arrête sur le premier qui a un index supérieur à la position de l'objet déplacé parmi les obstacles réels de l'env
                i_pos_cible_dans_soc = obstacles_centres.indexOf(oc) ; // On va insérer l'obstacle déplacé à sa place
                break ;
            }
        }

        if (i_pos_cible_dans_soc>=0)
            obstacles_centres.add(i_pos_cible_dans_soc,o_a_deplacer);
        else
            // L'obstacle va en dernière position
            obstacles_centres.add(o_a_deplacer) ;

        calculeElementsCardinaux();

    }

    public Environnement environnement() {
        return environnement ;
    }

    public Commande commandeCreation(Environnement env) {
        return new CommandeCreerSystemeOptiqueCentre(env,this) ;
    }

    public void ajouterObstaclesCentres(List<Obstacle> obstacles_a_ajouter) {
        obstacles_a_ajouter.forEach(this::ajouterObstacleCentre);
    }


    public record PositionElement(double z, double hauteur) { }

    /**
     * Classe utilitaire pour conserver les modalités de traversée de dioptre définies par l'utilisateur lorsque le
     * SOC change.
     */
    static class ModalitesTraverseeDioptre {

        // Champs servant à l'identification du dioptre dans la liste des DioptreParaxial réelles

        Obstacle obs_surface;
        double indice_avant;
        double indice_apres;

        // Champs à la main de l'utilisateur :
        Double r_diaphragme;
        boolean ignorer;

        /**
         * Construit les Modalités de traversée d'un dioptre par copie de certains attributs d'une Rencontre d'un dioptre paraxial
         * @param renc_dioptre_paraxial : la rencontre d'un dioptre paraxial dont on va garder les modalités
         */
        public ModalitesTraverseeDioptre(RencontreDioptreParaxial renc_dioptre_paraxial) {

            this.obs_surface = renc_dioptre_paraxial.obstacleSurface() ;
            this.indice_avant = renc_dioptre_paraxial.indiceAvant() ;
            this.indice_apres = renc_dioptre_paraxial.indiceApres() ;

            this.r_diaphragme = renc_dioptre_paraxial.rayonDiaphragme() ;
            this.ignorer = renc_dioptre_paraxial.ignorer() ;

        }
    }


    /**
     * Calcule la position (z_image optique + hauteur_image), en unités de l'environnement, de l'image d'un objet
     * (z_objet optique + hauteur_objet) par une matrice de transfert ES grâce à la relation homographique et à la formule du
     * grandissement transversal.
     * NB :
     * z_objet doit être fourni, en unités de l'environnement, comptées par rapport à la face d'entrée du système (positif
     * s'il est situé après la face d'entrée dans le sens de propagation de la lumière, négatif sinon)
     * z_image est retourné, en unités de l'environnement, comptées par rapport à la face de sortie (positif s'il est
     * après dans le sens de propagation de la lumière, négatif sinon)
     * A charge pour l'appelant d'ajouter ou de soustraire le z_image obtenu à la position z de la face de sortie selon
     * le sens de propagation de la lumière
     *
     * @param matrice_es : Matrice de transfert optique Entrée (1er dioptre rencontré) et la Sortie (dernier dioptre)  du système
     * @param position_obj : Position de l'objet (z sur l'axe, et hauteur)
     * @param n_objet : indice du milieu objet qui précède la face d'entrée (dans le sens de propagation de la lumière)
     * @param n_image : indice du milieu image qui suit la face de sortie (dans le sens de propagation de la lumière)
     * @return la position de l'image (z et hauteur)
     */
    private PositionElement positionImage(Affine matrice_es,PositionElement position_obj, double n_objet, double n_image) {
        double a = matrice_es.getMxx();
        double b = matrice_es.getMxy();
        double c = matrice_es.getMyx();
        double d = matrice_es.getMyy();

        double z_obj_sur_n_obj = position_obj.z * environnement.unite().valeur / n_objet ; // En mètres

        double z_image = n_image * ( a * z_obj_sur_n_obj -b ) / ( -c * z_obj_sur_n_obj + d ) ; // En mètres

        double g_transversal = a + c * z_image / n_image ;

        return new PositionElement(z_image / environnement.unite().valeur, position_obj.hauteur() * g_transversal ) ;
    }

    /**
     * Calcule la position (z_antecedent + hauteur_antecedent) de l'antécédent d'une image (z_image + hauteur_image) par
     * une matrice de transfert ES grâce à la relation homographique "inverse" et à la formule du grandissement transversal.
     *
     * <br>NB :
     * z_image doit être fourni en unités de l'environnement, comptées par rapport à la face de sortie du système (positif
     * s'il est situé après la face de sortie dans le sens de propagation de la lumière, négatif sinon)
     * z_antecedent est retourné en unités de l'environnement, comptées par rapport à la face d'entrée (positif s'il est
     * après dans le sens de propagation de la lumière, négatif sinon)
     * A charge pour l'appelant d'ajouter ou de soustraire le z_antecedent obtenu à la position z de la face d'entrée selon
     * le sens de propagation de la lumière
     *
     * @param matrice_es : Matrice de transfert optique Entrée (1er dioptre rencontré) et la Sortie (dernier dioptre)  du système
     * @param position_img : Position (z et hauteur) de l'image dont on cherche l'antécédent
     * @param n_objet : indice du milieu objet qui précède la face d'entrée (dans le sens de propagation de la lumière)
     * @param n_image : indice du milieu image qui suit la face de sortie (dans le sens de propagation de la lumière)
     * @return la position de l'antécédent (z et hauteur)
     */
    private PositionElement positionAntecedent(Affine matrice_es,PositionElement position_img, double n_objet, double n_image) {
        double a = matrice_es.getMxx();
        double b = matrice_es.getMxy();
        double c = matrice_es.getMyx();
        double d = matrice_es.getMyy();

        double z_image_sur_n_image = position_img.z * environnement.unite().valeur / n_image ; // En mètres

        double z_antecedent = n_objet * ( b + d * z_image_sur_n_image ) / ( a + c * z_image_sur_n_image) ; // En mètres

        double g_transversal_inverse = 1 / ( a + c * z_image_sur_n_image ) ;

        return new PositionElement(z_antecedent / environnement.unite().valeur , position_img.hauteur() * g_transversal_inverse ) ;
    }


    public void calculeElementsCardinaux() {

        if (suspendre_calcul_elements_cardinaux || environnement.calculsElementsCardinauxSocSuspendus() )
            return ;

        dioptres.clear();

        Affine nouvelle_matrice_transfert;

        try {
            // Calcul de tous les dioptres du SOC le sens des Z croissants et des Rcourbure "croissants" (progression
            // forcée dans le sens des Z croissants, en ignorant les dioptres réfléchissants) : on cherche à avoir la
            // liste exhaustive de tous les dioptres, sans chercher à savoir s'ils peuvent être rencontrés par le rayon
            // qui entre dans le SOC (les dioptres réellement rencontrés sont extraits dans la méthode
            // calculeMatriceTransfertOptique).
            dioptres.setAll(extraireDioptresParaxiaux());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,"Impossible de calculer les intersections du SOC avec l'axe",e);
            return ;
        }

        try {
            // On ne met pas directement à jour la property matrice_transfert car cela déclencherait immédiatement
            // un rafraichissement de l'affichage du SOC, alors qu'on n'a pas encore mis à jour les positions des éléments cardinaux
            nouvelle_matrice_transfert = calculeMatriceTransfertOptique() ;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,"Impossible de calculer la matrice de transfert",e);
            return ;
        }

        if (nouvelle_matrice_transfert==null) {
            supprimerAbscissesElementsCardinaux();

            matrice_transfert_es.set(null);

            return;
        }

        double a = nouvelle_matrice_transfert.getMxx();
        double b = nouvelle_matrice_transfert.getMxy();
        double c = nouvelle_matrice_transfert.getMyx();
        double d = nouvelle_matrice_transfert.getMyy();

        LOGGER.log(Level.FINE,"[ "+a+" "+b+" ]");
        LOGGER.log(Level.FINE,"[ "+c+" "+d+" ]");

        if (Environnement.quasiEgal(c,0d)) {

            LOGGER.log(Level.FINE,"Le système est afocal : pas d'éléments cardinaux") ;

            supprimerAbscissesElementsCardinaux();

            matrice_transfert_es.set(nouvelle_matrice_transfert);

            return ;
        }

        if (!Environnement.quasiEgal(a*d-b*c,1d)) {

            LOGGER.log(Level.SEVERE, "Le déterminant de la matrice de transfert du SOC n'est pas égal au rapport des indices avant/apres. Pas de calcul des éléments cardinaux.");

            supprimerAbscissesElementsCardinaux();

            matrice_transfert_es.set(nouvelle_matrice_transfert);

            return ;

        }

        double vergence = -c ;
        double focale_objet = (-n_entree.get()/vergence) ; // En mètres
        double focale_image = (n_sortie.get()/vergence) ;  // En mètres

        // Les Z des éléments cardinaux doivent être exprimés en unités de l'environnement : il faut donc faire des
        // conversions d'unité. De plus Les formules (cf. Perez pp. 53-54) donnent les Z *optiques* des éléments
        // cardinaux, qu'il faut convertir en Z géométriques (dont on a besoin ici, car ils serviront à positionner dans
        // le Canvas).
        z_optique_plan_focal_objet.set( (ZPlanEntree()*environnement.unite().valeur + focale_objet*d) / environnement.unite().valeur ) ;
        // Le Z géométrique du plan focal objet est identique à son Z optique : il est donné par EFo(alg)=fo.d et sur la face d'entrée E, la lumière progresse forcément dans le sens +
        z_geometrique_plan_focal_objet.set(z_optique_plan_focal_objet.get()) ;
//        z_geometrique_plan_focal_objet.set(convertirEnZGeometrique(z_optique_plan_focal_objet.get())) ;
        z_optique_plan_focal_image.set( (ZOptiquePlanSortie()*environnement.unite().valeur + focale_image*a) / environnement.unite().valeur ) ;
        // Le Z géométrique du plan focal image est donné par SFi(alg)=fi.a et sur la face de sortie S, la lumière progresse soit dans le sens +, doit dans le sens -
        z_geometrique_plan_focal_image.set( (ZGeometriquePlanSortie()*environnement.unite().valeur+(sens_plus_en_sortie.get()?1d:-1d)*focale_image*a)/environnement.unite().valeur);
//        z_geometrique_plan_focal_image.set(convertirEnZGeometrique(z_optique_plan_focal_image.get())) ;
        LOGGER.log(Level.FINE,"Z Plan Focal 1 : {0} , Z Plan Focal 2 : {1}",new Object[] {z_geometrique_plan_focal_objet, z_geometrique_plan_focal_image});

        z_optique_plan_principal_objet.set( (ZPlanEntree()*environnement.unite().valeur + focale_objet*(d-1d)) / environnement.unite().valeur );
        // Le Z géométrique du plan principal objet est identique à son Z optique : il est donné par EHo(alg)=fo.(d-1) et sur la face d'entrée E, la lumière progresse forcément dans le sens +
        z_geometrique_plan_principal_objet.set(z_optique_plan_principal_objet.get());
//        z_geometrique_plan_principal_objet.set(convertirEnZGeometrique(z_optique_plan_principal_objet.get()));
        z_optique_plan_principal_image.set( (ZOptiquePlanSortie()*environnement.unite().valeur + focale_image*(a-1d)) / environnement.unite().valeur ) ;
        // Le Z géométrique du plan principal image est donné par SHi(alg)=fi.(a-1) et sur la face de sortie S, la lumière progresse soit dans le sens +, doit dans le sens -
        z_geometrique_plan_principal_image.set((ZGeometriquePlanSortie()*environnement.unite().valeur+(sens_plus_en_sortie.get()?1d:-1d)*focale_image*(a-1d))/environnement.unite().valeur);
//        z_geometrique_plan_principal_image.set(convertirEnZGeometrique(z_optique_plan_principal_image.get())) ;
        LOGGER.log(Level.FINE,"Z Plan Principal 1 : {0} , Z Plan Principal 2 : {1}",new Object[] {z_geometrique_plan_principal_objet, z_geometrique_plan_principal_image});

        z_optique_plan_nodal_objet.set( (ZPlanEntree()*environnement.unite().valeur + focale_objet*(d-(sens_plus_en_sortie.get()?1d:-1d)*n_sortie.get()/n_entree.get())) / environnement.unite().valeur ) ;
        // Le Z géométrique du plan nodal objet est identique à son Z optique : il est donné par ENo(alg)=fo.(d-sens_plus_en_sortie*ni/no) et sur la face d'entrée E, la lumière progresse forcément dans le sens +
        z_geometrique_plan_nodal_objet.set(z_optique_plan_nodal_objet.get()) ;
//        z_geometrique_plan_nodal_objet.set(convertirEnZGeometrique(z_optique_plan_nodal_objet.get())) ;
        z_optique_plan_nodal_image.set( (ZOptiquePlanSortie()*environnement.unite().valeur + focale_image*(a-(sens_plus_en_sortie.get()?1d:-1d)*n_entree.get()/n_sortie.get())) / environnement.unite().valeur ) ;
        // Le Z géométrique du plan nodal image est donné par SNi(alg)=fi.(a-no/ni*sens_plus_en_sortie) et sur la face de sortie S, la lumière progresse soit dans le sens +, doit dans le sens -
        z_geometrique_plan_nodal_image.set((ZGeometriquePlanSortie()*environnement.unite().valeur + (sens_plus_en_sortie.get()?1d:-1d)*focale_image*(a-(sens_plus_en_sortie.get()?1d:-1d)*n_entree.get()/n_sortie.get())) / environnement.unite().valeur ) ;
//        z_geometrique_plan_nodal_image.set((z_optique_plan_sortie*environnement.unite().valeur + (sens_plus_en_sortie.get()?1d:-1d)*focale_image*(a-(sens_plus_en_sortie.get()?1d:-1d)*n_entree.get()/n_sortie.get())) / environnement.unite().valeur ) ;
//        z_geometrique_plan_nodal_image.set(convertirEnZGeometrique(z_optique_plan_nodal_image.get())) ;
        LOGGER.log(Level.FINE,"Z Plan Nodal 1 : {0} , Z Plan Nodal 2 : {1}",new Object[] {z_geometrique_plan_nodal_objet, z_geometrique_plan_nodal_image});

        matrice_transfert_es.set(nouvelle_matrice_transfert);

    }

    private void supprimerAbscissesElementsCardinaux() {

        z_optique_plan_focal_objet.set(null) ;
        z_geometrique_plan_focal_objet.set(null) ;
        
        z_optique_plan_focal_image.set(null) ;
        z_geometrique_plan_focal_image.set(null) ;
        
        z_optique_plan_principal_objet.set(null) ;
        z_geometrique_plan_principal_objet.set(null) ;
        
        z_optique_plan_principal_image.set(null) ;
        z_geometrique_plan_principal_image.set(null) ;
        
        z_optique_plan_nodal_objet.set(null) ;
        z_geometrique_plan_nodal_objet.set(null) ;
        
        z_optique_plan_nodal_image.set(null) ;
        z_geometrique_plan_nodal_image.set(null) ;

    }

    public Double ZPlanEntree() { return z_plan_entree.get(); }
    public ObjectProperty<Double> ZPlanEntreeProperty() { return z_plan_entree; }
    public Double ZGeometriquePlanSortie() { return z_geometrique_plan_sortie.get(); }
    public ObjectProperty<Double> ZGeometriquePlanSortieProperty() { return z_geometrique_plan_sortie; }
    public Double ZOptiquePlanSortie() { return z_optique_plan_sortie.get(); }
    public ObjectProperty<Double> ZOptiquePlanSortieProperty() { return z_optique_plan_sortie; }
    public ObjectProperty<Double> ZGeometriquePlanFocalObjetProperty() { return z_geometrique_plan_focal_objet;}
    public Double ZGeometriquePlanFocalObjet() { return z_geometrique_plan_focal_objet.get();}
    public ObjectProperty<Double> ZGeometriquePlanFocalImageProperty() { return z_geometrique_plan_focal_image;}
    public Double ZGeometriquePlanFocalImage() { return z_geometrique_plan_focal_image.get();}
    public ObjectProperty<Double> ZGeometriquePlanPrincipalObjetProperty() { return z_geometrique_plan_principal_objet;}
    public Double ZGeometriquePlanPrincipalObjet() { return z_geometrique_plan_principal_objet.get();}
    public ObjectProperty<Double> ZGeometriquePlanPrincipalImageProperty() { return z_geometrique_plan_principal_image;}
    public Double ZGeometriquePlanPrincipalImage() { return z_geometrique_plan_principal_image.get();}
    public ObjectProperty<Double> ZGeometriquePlanNodalObjetProperty() { return z_geometrique_plan_nodal_objet;}
    public Double ZGeometriquePlanNodalObjet() { return z_geometrique_plan_nodal_objet.get();}
    public ObjectProperty<Double> ZGeometriquePlanNodalImageProperty() { return z_geometrique_plan_nodal_image;}
    public Double ZGeometriquePlanNodalImage() { return z_geometrique_plan_nodal_image.get();}

    public ObjectProperty<Double> ZOptiquePlanFocalObjetProperty() { return z_optique_plan_focal_objet;}
    public Double ZOptiquePlanFocalObjet() { return z_optique_plan_focal_objet.get();}
    public ObjectProperty<Double> ZOptiquePlanFocalImageProperty() { return z_optique_plan_focal_image;}
    public Double ZOptiquePlanFocalImage() { return z_optique_plan_focal_image.get();}
    public ObjectProperty<Double> ZOptiquePlanPrincipalObjetProperty() { return z_optique_plan_principal_objet;}
    public Double ZOptiquePlanPrincipalObjet() { return z_optique_plan_principal_objet.get();}
    public ObjectProperty<Double> ZOptiquePlanPrincipalImageProperty() { return z_optique_plan_principal_image;}
    public Double ZOptiquePlanPrincipalImage() { return z_optique_plan_principal_image.get();}
    public ObjectProperty<Double> ZOptiquePlanNodalObjetProperty() { return z_optique_plan_nodal_objet;}
    public Double ZOptiquePlanNodalObjet() { return z_optique_plan_nodal_objet.get();}
    public ObjectProperty<Double> ZOptiquePlanNodalImageProperty() { return z_optique_plan_nodal_image;}
    public Double ZOptiquePlanNodalImage() { return z_optique_plan_nodal_image.get();}
    
    
    public ObjectProperty<Double> ZGeometriqueObjetProperty() { return z_geometrique_objet;}
    public Double ZGeometriqueObjet() { return z_geometrique_objet.get();}
    public ObjectProperty<Double> HObjetProperty() { return h_objet;}
    public Double HObjet() { return h_objet.get();}

    public ObjectProperty<Double> ZOptiqueImageProperty() { return z_optique_image;}
    public Double ZOptiqueImage() { return z_optique_image.get();}

    public ObjectProperty<Double> ZGeometriqueImageProperty() { return z_geometrique_image;}
    public Double ZGeometriqueImage() { return z_geometrique_image.get();}
    public ObjectProperty<Double> HImageProperty() { return h_image;}
    public Double HImage() { return h_image.get();}

    public ObjectProperty<Double> GrandissementTransversalProperty() { return grandissement_transversal;}
    public ObjectProperty<Double> GrandissementAngulaireProperty() { return grandissement_angulaire;}
    public ObjectProperty<Double> GrandissementLongitudinalProperty() { return grandissement_longitudinal;}


    public Double RChampMoyenObjet() { return r_champ_moyen_objet.get();}
    public Double RChampMoyenImage() { return r_champ_moyen_image.get();}
    public Double RChampPleineLumiereObjet() { return r_champ_pleine_lumiere_objet.get();}
    public Double RChampPleineLumiereImage() { return r_champ_pleine_lumiere_image.get();}
    public Double RChampTotalObjet() { return r_champ_total_objet.get();}
    public Double RChampTotalImage() { return r_champ_total_image.get();}

    public ObjectProperty<Affine> MatriceTransfertESProperty() { return matrice_transfert_es; }

    public Affine matriceTransfertES() { return matrice_transfert_es.get() ; }


    public BooleanProperty SensPlusEnSortieProperty() { return sens_plus_en_sortie; }
    public boolean SensPlusEnSortie() { return sens_plus_en_sortie.get(); }

    public DoubleProperty NEntreeProperty() {   return n_entree ;}
    public double NEntree() {   return n_entree.get() ;}
    public DoubleProperty NSortieProperty() {   return n_sortie ;}
    public double NSortie() {   return n_sortie.get() ;}

    public BooleanProperty MontrerObjetProperty() { return montrer_objet ; }
    public boolean MontrerObjet() { return montrer_objet.get() ; }
    public BooleanProperty MontrerImageProperty() { return montrer_image ; }
    public boolean MontrerImage() { return montrer_image.get() ; }

    public BooleanProperty MontrerDioptresProperty() { return montrer_dioptres ; }
    public boolean MontrerDioptres() { return montrer_dioptres.get() ; }

    public BooleanProperty MontrerPlansFocauxProperty() { return montrer_plans_focaux ; }
    public boolean MontrerPlansFocaux() { return montrer_plans_focaux.get() ; }

    public BooleanProperty MontrerPlansPrincipauxProperty() { return montrer_plans_principaux ; }
    public boolean MontrerPlansPrincipaux() { return montrer_plans_principaux.get() ; }

    public BooleanProperty MontrerPlansNodauxProperty() { return montrer_plans_nodaux ; }
    public boolean MontrerPlansNodaux() { return montrer_plans_nodaux.get() ; }


    public ListProperty<DioptreParaxial> IntersectionsSurAxeProperty() {
        return dioptres;
    }
    public ObservableList<DioptreParaxial> InterSectionsSurAxe() {
        return dioptres.get() ;
    }

    public ListProperty<RencontreDioptreParaxial> dioptresRencontresProperty() {
        return dioptres_rencontres;
    }
    public ObservableList<RencontreDioptreParaxial> dioptresRencontres() {
        return dioptres_rencontres.get() ;
    }

    public ObjectProperty<Affine> matriceTransfertESProperty() {
        return matrice_transfert_es ;
    }

    /**
     * <p>Calcule et retourne la Matrice de Transfert Optique du système, entre son plan d'entrée (plan de front du 1er
     * dioptre non ignoré rencontré par la lumière, qui progresse dans le sens de l'axe Z) et son plan de sortie (plan du dernier dioptre
     * rencontré non ignoré; la lumière pouvant sortir du système dans le sens de l'axe Z du système, ou dans le sens opposé ; toutefois
     * la lumière progresse toujours dans le sens des abscisses croissantes de l'axe optique. L'axe optique "se retourne" si la
     * lumière rencontre une surface réfléchissante)</p><br>
     * <p>
     *     Toutes les positions z sont exprimées en unité de l'environnement mais la matrice de transfert optique est
     *     calculée pour des vecteurs colonnes ( z , n*alpha ) dans lesquels z est en mètres. Ainsi la vergence (coefficient c
     *     de la matrice) est toujours en dioptries.
     * </p><br>
     * <p>
     * Pré-condition :
     * <ul><li>tous les dioptres (dans le sens des z croissants, et des RC "croissants") en supposant les dioptres tous transparents
     * sont déjà calculés et classés dans l'attribut dioptres (via la méthode extraireDioptresParaxiaux() appelée dans
     * calculeElementsCardinaux())
     * </ul>
     * </p><br>
     * <p>
     * Post-conditions :
     *  <ul>
     *  <li>z_plan_entree est calculé (en unités de l'environnement)
     *  <li>n_entree est calculé
     *  <li>z_plan_sortie est calculé (en unités de l'environnement)
     *  <li>n_sortie est calculé
     *  <li>sens_plus_en_sortie est calculé (true si la lumière progresse dans le sens de l'axe Z en sortie du système, false sinon)
     *  la liste (ordonnée dans l'ordre où elles ont lieu) des dioptres réellement rencontrés par un rayon paraxial colinéaire
     *  à l'axe du système, en tenant compte du caractère transparent ou réfléchissant des surfaces des dioptres, est calculée
     *  dans dioptres_rencontres avec pour chacun d'entre eux, le rayon de courbure algébrique, l'obstacle dont la surface
     *  a été rencontrée, les indices des milieux avant/après.
     *  </ul>
     *  </p>
     * @return la matrice de transfert optique du système Mt(ES)
     * @throws Exception si le calcul de la matrice de transfert échoue
     */
    private Affine calculeMatriceTransfertOptique() throws Exception {

        if (dioptres.size()==0)
            return null ;

        Affine resultat = new Affine(1d,0d,0d,
                0d,1d,0) ;

        // Mémorisons les modalités de traversée des dioptres (rayons des diaphragmes, dioptres à ignorer) qui étaient
        // précédemment définies par l'utilisateur (pour lui épargner de les re-saisir à chaque modification du SOC)
        ArrayList<ModalitesTraverseeDioptre> modalites_traversee_precedentes = new ArrayList<>(dioptres_rencontres.size())  ;

        if (dioptres_rencontres.size()>0) {

            for (RencontreDioptreParaxial its : dioptres_rencontres)
                modalites_traversee_precedentes.add(new ModalitesTraverseeDioptre(its)) ;
        }
        // Fin de la mémorisation

        dioptres_rencontres.clear();

        z_plan_entree.set(null);
//        z_plan_entree = dioptres.get(0).ZGeometrique();
        n_entree.set(dioptres.get(0).indiceAvant()) ;

        int i = 0;
        int pas = +1 ;
        int nb_reflexions = 0 ;
        int nb_dioptres_rencontres = 0 ; // Nombre de dioptres rencontrés (qu'ils soient ignorés ou non)

        // Variables pour recherche du diaphragme d'ouverture et de la pupille d'entrée
        double tan_demi_ouverture = Double.MAX_VALUE ;
        int index_diaphragme_ouverture = -1 ; // Position du diaphragme d'ouverture dans la liste des intersections réelles du SOC

        double ratio_h_emergent_max_depuis_objet = -1d ;
        int index_diaphragme_ouverture_bis = -1 ; // Position du diaphragme d'ouverture dans la liste des intersections réelles du SOC
        // Fin

        RencontreDioptreParaxial precedent_dioptre_rencontre_non_ignore = null ;
        RencontreDioptreParaxial dioptre_rencontre = null ;
        RencontreDioptreParaxial dioptre_sortie = null ;

        // Indice du milieu à appliquer, si le dioptre précédent est marqué "à ignorer"
        Double n_a_appliquer = null ;
        boolean ignorer_dioptre_courant;

        // NB : la condition sur nb_reflexions<3 est utile pour éviter une suite infinie d'allers-retours du rayon entre
        // deux surfaces réfléchissantes (situation qui peut se produire si la première surface "laisse entrer" le rayon
        // dans la cavité optique parce qu'elle est semi-réfléchissante)
        while (nb_reflexions<3 && i<dioptres.size() && i>=0) {

            // Instancions une rencontre de dioptre à partir du dioptre courant, en tenant compte du sens de propagation
            // de la lumière
            dioptre_rencontre = new RencontreDioptreParaxial(dioptres.get(i),pas>0,precedent_dioptre_rencontre_non_ignore) ;

            dioptres_rencontres.add(dioptre_rencontre) ;

            // On reprend les modalités de traversée précédentes, si elles sont applicables
            if (nb_dioptres_rencontres<modalites_traversee_precedentes.size())
                dioptre_rencontre.appliquerModalitesTraverseeDioptrePrecedentesSiApplicables(modalites_traversee_precedentes.get(nb_dioptres_rencontres)) ;

            dioptre_rencontre.activerDeclenchementCalculElementsCardinauxSiChangementModalitesTraversee(this) ;

            ignorer_dioptre_courant = dioptre_rencontre.ignorer() ;

            if (n_a_appliquer!=null)
                dioptre_rencontre.indice_avant.set(n_a_appliquer);

            if (ignorer_dioptre_courant) {

                if (n_a_appliquer==null) // Le milieu avant le dioptre devient aussi le milieu après le dioptre, vu qu'on ignore le dioptre
                    n_a_appliquer = dioptre_rencontre.indiceAvant();

                dioptre_rencontre.indice_apres.set(n_a_appliquer);

            } else { // Le dioptre courant n'est pas à ignorer

                if (z_plan_entree.get()==null)
                    z_plan_entree.set(dioptre_rencontre.ZGeometrique()) ; // Le plan d'entrée est celui du premier dioptre non ignoré rencontré

                if (n_a_appliquer!=null &&  ( ! dioptre_rencontre.obstacleSurface().estReflechissant() ) )
                    n_a_appliquer = null;

            }

            if (dioptre_rencontre.rayonCourbure()!=null && dioptre_rencontre.rayonCourbure()==0)
                throw new Exception("Rayon de courbure nul sur l'obstacle "+dioptre_rencontre.obstacleSurface()
                        +" : impossible de calculer une matrice de transfert (point anguleux sur l'axe)") ;

            // Si on arrive d'un dioptre_rencontre précédent non ignoré (on n'est pas sur le dioptre d'entrée du SOC)
            // et si le dioptre rencontré n'est pas à ignorer...
            if (precedent_dioptre_rencontre_non_ignore!=null && !ignorer_dioptre_courant) {
                // ...il y a une matrice de translation à ajouter

                // Conventions de sens et de signe ; cf. Optique, fondements et applications J-Ph. Pérez, Chap 13 (pp. 144/145)
                // L'axe optique change de sens après un miroir (le rayon progresse donc toujours dans le sens + de l'axe optique)
                double intervalle = (pas>0?1d:-1d)
                        * (dioptre_rencontre.ZGeometrique() - precedent_dioptre_rencontre_non_ignore.ZGeometrique())
                        / dioptre_rencontre.indiceAvant() ;

                if (intervalle!=0d)
                    resultat.prepend(new Affine(1d, intervalle*environnement.unite().valeur, 0d,
                            0d, 1d, 0d));

            }

            // Mémorisation de la matrice de transfert partielle (sera utilisée pour la recherche du Diaph. de Champ)
            dioptre_rencontre.matrice_transfert_partielle = resultat.clone() ;
            dioptre_rencontre.sens_plus_en_sortie_matrice_partielle = (pas>0) ;

            // Recherche du diaphragme d'ouverture et de l'angle d'ouverture
            // NB : on est obligé d'ignorer les diaphragmes des dioptres ignorés car le traitement requiert la matrice de
            // transfert depuis le plan d'entrée qui n'est peut-être pas encore déterminé...
            if (!ignorer_dioptre_courant && dioptre_rencontre.rayonDiaphragme()!=null) {

                // S'il y a un rayon diaphragme défini sur le dioptre rencontré, et que c'est le premier dans ce cas,
                // il constitue un premier diaphragme d'ouverture par défaut.
                if (index_diaphragme_ouverture == -1)
                    index_diaphragme_ouverture = i ;
                if (index_diaphragme_ouverture_bis == -1)
                    index_diaphragme_ouverture_bis = i ;

                if (ZGeometriqueObjet()!=null /* && (z_antecedent_diaphragme-ZObjet())>0 */) {
                    // La recherche du DO dépend de la position de l'objet z_objet, ce n'est pas une propriété intrinsèque du SOC

                    // Methode 1 pour trouver le DO : ratio hauteur d'émergence sur dioptre i / rayon diaphragme i maximal pour un rayon
                    // issu du dioptre_rencontre du plan objet et de l'axe, avec un angle non nul par rapport à l'axe (ici 1°)
                    // Cette méthode permet aussi de trouver les hauteurs limites du "cone d'ouverture" sur chaque dioptre
                    // et d'en faire une jolie, et parlante représentation graphique

                    Affine mat_transfert_depuis_objet = resultat.clone() ;

                    // Ajoutons la matrice de translation entre l'objet et le plan d'entrée (rappel : on multiplie les
                    // matrices en partant de la fin vers le début ; c'est donc bien un append, et non un prepend qu'il
                    // faut faire ici)
                    mat_transfert_depuis_objet.append(new Affine(  1d, (ZPlanEntree() - ZGeometriqueObjet())*environnement.unite().valeur/NEntree(), 0d,0d, 1d, 0d )) ;
                    // mat_transfert_depuis_objet.append(new Affine(  1d, (pas>0?1d:-1d)*(z_plan_entree-ZObjet())/NEntree(), 0d,0d, 1d, 0d )) ; // NON : sur le plan
                    // d'entree le rayon est toujours dans le sens de l'axe (et pas représente le sens du marche du rayon *au niveau du dioptre courant* et pas en entrée du SOC...)

                    // Calcul du rayon émergent (hauteur *en mètres* + angle) d'un rayon qui part d'un point situé sur l'axe et sur le plan objet,
                    // faisant un angle de 1° avec l'axe (l'indice avant la face d'entrée est supposé égal à 1 : on cherche le diaphragme
                    // d'ouverture du SOC "dans le vide")
                    Point2D r_emergent = mat_transfert_depuis_objet.transform(0,Math.toRadians(1d)) ;
                    r_emergent = new Point2D(r_emergent.getX()/environnement.unite().valeur , r_emergent.getY() ) ;

                    double ratio_h_emergent = Double.MAX_VALUE ;

                    if (dioptre_rencontre.rayonDiaphragme()!=0d)
                        ratio_h_emergent = Math.abs(r_emergent.getX() / dioptre_rencontre.rayonDiaphragme()) ;

                    // Enregistrement de la hauteur X (en mètres) du dioptre_rencontre avec le diaphragme
                    dioptre_rencontre.h_limite_ouverture.set(r_emergent.getX());

                    LOGGER.log(Level.FINE,"Ratio x/x_diaphragme du diaphragme {0} : {1}",
                            new Object[] {nb_dioptres_rencontres,ratio_h_emergent} ) ;

                    // NB : Toutes ces positions sont en mètres
                    // Il s'agit d'une position en Z optique
                    PositionElement antecedent_diaphragme_relatif_soc =
                            positionAntecedent(resultat,
                                    new PositionElement(0d, dioptre_rencontre.rayonDiaphragme()),
                                    NEntree(),
                                    dioptre_rencontre.indiceApres());

                    // On renseigne l'antécédent du diaphragme, avec son Z optique
                    dioptre_rencontre.antecedent_diaphragme.set(
                            new PositionElement(ZPlanEntree() + antecedent_diaphragme_relatif_soc.z(),
                                    antecedent_diaphragme_relatif_soc.hauteur()) ) ;

                    //  double z_antecedent_diaphragme = z_plan_entree + (pas>0?1d:-1d) * antecedent_diaphragme.z() ; // NON : sur le plan
                    // d'entree le rayon est toujours dans le sens de l'axe (et pas représente le sens de marche du rayon *au niveau du dioptre courant* et pas en entrée du SOC)
//                    double z_antecedent_diaphragme = z_plan_entree + antecedent_diaphragme_relatif_soc.z() ;
//                    double h_antecedent_diaphragme = antecedent_diaphragme_relatif_soc.hauteur();

                    // On cherche la hauteur d'incidence la plus grande par rapport à la hauteur du diaphragme
                    if (ratio_h_emergent>ratio_h_emergent_max_depuis_objet) {
                        ratio_h_emergent_max_depuis_objet = ratio_h_emergent ;
                        index_diaphragme_ouverture_bis = nb_dioptres_rencontres ;

//                        z_pupille_entree_potentielle = dioptre_rencontre.antecedentDiaphragme().z(); ;
//                        h_pupille_entree_potentielle = dioptre_rencontre.antecedentDiaphragme().hauteur() ;
                    }

                    // Méthode 2 pour trouver le DO : antecedent de diaphragme (par la partie du système en amont de
                    // celui-ci) que l'on voit sous le plus petit angle par rapport à la position de l'objet.
                    // TODO : a supprimer à terme
                    // Attention : en présence de surfaces réfléchissantes sur l'axe optique, celui-ci "se replie" et la distance
                    // (optique, càd la distance parcourue le long de l'axe) ne se calcule pas par la simple différence ci-dessou
                    double denom = Math.abs(dioptre_rencontre.antecedentDiaphragme().z() - ZGeometriqueObjet()) ;

                    Double tan_angle_antecedent_depuis_z_objet = Environnement.quasiEgal(denom,0d) ?
                            null : Math.abs(dioptre_rencontre.antecedentDiaphragme().hauteur()) / denom ;

                    if (tan_angle_antecedent_depuis_z_objet != null) {
                        LOGGER.log(Level.FINE, """
                                        Pupille entrée du diaphragme {0} en Z = {1} :
                                            hauteur : {2} m, angle vu de objet : {3}°
                                        """,
                                new Object[]{nb_dioptres_rencontres, dioptre_rencontre.antecedentDiaphragme().z() ,dioptre_rencontre.antecedentDiaphragme().hauteur(),
                                        Math.toDegrees(Math.atan(tan_angle_antecedent_depuis_z_objet))});

                        System.out.println("Antécédent du diaphragme "+nb_dioptres_rencontres+" en Z = "+dioptre_rencontre.antecedentDiaphragme().z()+" hauteur :"+dioptre_rencontre.antecedentDiaphragme().hauteur()) ;
                        System.out.println("    Angle vu de l'objet  :" +Math.toDegrees(Math.atan(tan_angle_antecedent_depuis_z_objet)));

                        // Si l'angle sous lequel on voit l'antécédent du diaphragme courant dans l'espace objet est plus
                        // petit que le plus petit des précédents, on a trouvé le nouveau DO
                        System.out.println("Comparaison avec angle sous lequel on voit la pupille d'entrée courante, qui vaut "+Math.toDegrees(Math.atan(tan_demi_ouverture))+" : ") ;
                        if (tan_angle_antecedent_depuis_z_objet < tan_demi_ouverture) {
                            System.out.println("PLUS PETIT : je prends le nouveau comme Pupille d'entrée");
                            tan_demi_ouverture = tan_angle_antecedent_depuis_z_objet;
                            index_diaphragme_ouverture = nb_dioptres_rencontres;
                        }
                    }

                    // Contrôle
                    if (index_diaphragme_ouverture!=index_diaphragme_ouverture_bis) {

                        RencontreDioptreParaxial d_ouv     = dioptres_rencontres.get(index_diaphragme_ouverture) ;
                        RencontreDioptreParaxial d_ouv_bis = dioptres_rencontres.get(index_diaphragme_ouverture_bis) ;
                        // Il peut arriver que les indexALaRacine trouvés ne soient pas les mêmes, mais qu'ils correspondent tous
                        // deux à des diaphragmes identiques, c'est à dire de même position et de même rayon suite à
                        // des erreurs d'arrondi. Dans ce cas pas d'alerte.
//                        if (!Environnement.quasiEgal(d_ouv.ZGeometrique(),d_ouv_bis.ZGeometrique())
                        if (!Environnement.quasiEgal(d_ouv.ZOptique(),d_ouv_bis.ZOptique())
                                || !Environnement.quasiEgal(d_ouv.rayonDiaphragme(),d_ouv_bis.rayonDiaphragme()))
                            LOGGER.log(Level.SEVERE, "DO n'est pas le même selon la méthode...");
                    }

                }
            } // Fin du bloc de recherche du DO

            // Prise en compte du dioptre courant dans la matrice de transfert ES
            if (!ignorer_dioptre_courant
                    && (! ( dioptre_rencontre.obstacleSurface().aUneProprieteDiaphragme() // Cas du Segment avec sa propriété diaphragme : s'il est ouvert, il n'agit pas, et de fait on l'ignore
                            && (dioptre_rencontre.rayonDiaphragme()!=null && dioptre_rencontre.rayonDiaphragme()>0d) ) ) )  {

                // Rencontre d'une surface absorbante ou d'un diaphragme totalement fermé ? => fin de la propagation
                if (dioptre_rencontre.obstacleSurface().traitementSurface() == TraitementSurface.ABSORBANT
                        || ( dioptre_rencontre.rayonDiaphragme()!=null
                             && !dioptre_rencontre.obstacleSurface().aUneProprieteDiaphragme() // Exclusion du cas du Segment, qui a une vraie propriété diaphragme
                             && dioptre_rencontre.rayonDiaphragme()==0d ) ) {

                    // Le milieu de sortie est le dernier milieu traversé, à savoir celui qui précède la surface
                    // absorbante (dans le sens de la marche du rayon)
                    dioptre_rencontre.propagerIndiceAvant();

                    dioptre_rencontre.sens.set((pas>0)?"⇥":"⇤");

                    ++nb_dioptres_rencontres ;

                    // Le dioptre absorbant (ou le diaphragme fermé) stoppe la propagation
                    break ;

                } else if (dioptre_rencontre.obstacleSurface().estReflechissant() ) {

                    // Rencontre d'une surface réfléchissante : propagation se poursuit, en renversant le sens

                    dioptre_rencontre.sens.set((pas>0)?"⮌":"⮎");

                    // Renversement du sens de marche du rayon
                    pas = -pas;
                    ++nb_reflexions;

                    // L'indice avant réflexion est aussi l'indice après réflexion (rappel : on a déjà mis les indices avant/apres dans le bon ordre)
                    dioptre_rencontre.propagerIndiceAvant();

                    if (dioptre_rencontre.rayonCourbure() == null) {
                        // Miroir plan : rien d'autre à faire (matrice transfert = matrice identité dans ce cas)
                    } else {
                        // Miroir localement sphérique, cf. Perez, Chap 13
                        resultat.prepend(new Affine(1d, 0d, 0d,
                                2d * dioptre_rencontre.indiceAvant()
                                        / (dioptre_rencontre.rayonCourbure()*environnement.unite().valeur) ,
                                1d, 0d));
                    }

                } else {

                        // Rencontre d'une surface majoritairement transparente

                        dioptre_rencontre.sens.set((pas>0)?"⟶":"⟵");
                        if (dioptre_rencontre.obstacleSurface().natureMilieu() != NatureMilieu.PAS_DE_MILIEU) // Il y a un "vrai" dioptre entre deux milieux

                            if (dioptre_rencontre.rayonCourbure() != null) {
                                // Coefficients a,b,c,d : cf. Optique, fondements et applications J-Ph. Pérez, Chap 4 (p. 45)
                                // (déterminant de la matrice de transfert vaut 1)
                                resultat.prepend(new Affine(1, 0, 0,
                                        -(dioptre_rencontre.indiceApres() - dioptre_rencontre.indiceAvant())
                                                / (dioptre_rencontre.rayonCourbure()*environnement.unite().valeur), 1, 0));
                            } else { // Dioptre plan (rayon de courbure infini)
                                // Matrice a b c d = matrice identité. Rien à faire
                            }

                    }

            } // if (!ignorer_dioptre_courant)

            if (!ignorer_dioptre_courant) {
                dioptre_sortie = dioptre_rencontre ;
                precedent_dioptre_rencontre_non_ignore = dioptre_rencontre;

            }
            ++nb_dioptres_rencontres ;

            i += pas ;
        }  // Fin de la construction de la liste des dioptres rencontrés et de la matrice de transfert

        z_geometrique_plan_sortie.set(dioptre_sortie.ZGeometrique()) ;
        z_optique_plan_sortie.set(dioptre_sortie.ZOptique()) ;
        sens_plus_en_sortie.set(pas>0) ;
        n_sortie.set(dioptre_sortie.indiceApres()) ;

//        z_geometrique_plan_sortie = dioptre_rencontre.ZGeometrique()  ;
//        z_optique_plan_sortie = dioptre_rencontre.ZOptique() ;
//        sens_plus_en_sortie.set(pas>0) ;
//        n_sortie.set(dioptre_rencontre.indiceApres()) ;

        // Mise à jour immédiate de la position de l'image (en mètres) : on en a besoin un peu plus loin dans cette méthode
        PositionElement position_image = positionImage(resultat,
                new PositionElement(convertirEnZOptique(z_geometrique_objet.get())-ZPlanEntree(), +0.1d),n_entree.get(),n_sortie.get()) ;
//        PositionElement position_image = positionImage(resultat,
//                new PositionElement(z_objet.get()-z_plan_entree, +0.1d),n_entree.get(),n_sortie.get()) ;
        double z_image_precalcule = ZOptiquePlanSortie() + position_image.z() ; // On calcule ici un Z optique
//        double z_image_precalcule = (z_geometrique_plan_sortie + (sens_plus_en_sortie.get()?1d:-1d) * position_image.z()) ;
        double h_image_precalcule = position_image.hauteur()  ;

        // Si on a trouvé un DO...
        if (index_diaphragme_ouverture>=0) {

            // ..on le marque ;
            dioptres_rencontres.get(index_diaphragme_ouverture).est_diaphragme_ouverture.set("✓");
            //...son antécédent est la pupille d'entrée du système
            z_pupille_entree.set(dioptres_rencontres.get(index_diaphragme_ouverture).antecedentDiaphragme().z()) ; // C'est le Z optique que l'on renseigne ici
            // Par convention, on choisit de prendre r_pupille entrée > 0
            r_pupille_entree.set(Math.abs(dioptres_rencontres.get(index_diaphragme_ouverture).antecedentDiaphragme().hauteur())) ;

            // ...et l'image de la pupille d'entrée est la pupille de sortie du système
            PositionElement image_pupille_entree =
                    positionImage(resultat, new PositionElement(z_pupille_entree.get()-ZPlanEntree(), r_pupille_entree.get()),
                            NEntree(), NSortie()); // Position calculée en Z optique

            z_pupille_sortie.set ( ZOptiquePlanSortie() + image_pupille_entree.z() );
//            z_pupille_sortie.set ( z_geometrique_plan_sortie + (sens_plus_en_sortie.get()?1d:-1d)*image_pupille_entree.z() );
            r_pupille_sortie.set ( Math.abs(image_pupille_entree.hauteur()) );

            // On peut alors définir l'angle d'ouverture du système
            angle_ouverture.set(Math.toDegrees(Math.abs(Math.atan(tan_demi_ouverture)))) ;
        }

        int index_diaphragme_champ = -1 ;
        double ratio_h_emergent_max_depuis_pupille_entree = 0d ;

        // Boucle utilisée pour :
        // 1) Recaler les hauteurs du cone d'ouverture limite (ayons marginaux) sur chacun des diaphragmes (pour visualisation de l'ouverture)
        // 2) Rechercher le Diaphragme de Champ et la Lucarne d'entrée
        for (int j = 0; j< dioptres_rencontres.size() ; j++) {

            RencontreDioptreParaxial its = dioptres_rencontres.get(j) ;

            if (its.ignorer())
                continue;

            // 1) Calage de la hauteur des rayons marginaux
            if (its.HLimiteOuverture()!=null && ratio_h_emergent_max_depuis_objet !=0d) {
                if (ratio_h_emergent_max_depuis_objet!=Double.MAX_VALUE)
                    its.h_limite_ouverture.set(its.HLimiteOuverture() / ratio_h_emergent_max_depuis_objet);
                else
                    its.h_limite_ouverture.set(0d) ;
            }


            // 2) Recherche du diaphragme de champ
            if (its.rayonDiaphragme()!=null) {
                // On lance un rayon depuis le centre de la pupille d'entrée (qui est sur l'axe) avec un angle non nul
                // (ici +1°) et on cherche le diaphragme pour lequel le ratio h_emergence_rayon / h_diaphragme est maximal.
                // Pour pouvoir positionner la lucarne d'entrée (et de sortie), il faut aussi avoir mémorisé les matrices
                // de transfert partielles au niveau de chacun des dioptres intermédiaires, ainsi que le sens de la
                // lumière au niveau de ces mêmes dioptres.
                Affine mat_transfert_depuis_pupille_entree = its.matriceTransfertPartielle().clone();

                // Ajoutons la matrice de translation entre la pupille d'entrée et le plan d'entrée
                mat_transfert_depuis_pupille_entree.append(new Affine(1d, (ZPlanEntree() - z_pupille_entree.get())*environnement.unite().valeur/NEntree(), 0d, 0d, 1d, 0d));

                // Rayon émergent au niveau de ce dioptre (h_objet=0 au niveau de la pupille d'entrée, angle objet = 1°)

                // Si l'objet est avant la pupille d'entrée, l'angle du rayon (issu d'une hauteur h>0, celle du champ_moyen_objet) sur la pupille d'entrée est négatif (on le prend à-1°)
                // Si l'objet est après la pupille d'entrée (objet virtuel), l'angle sur la pupille d'entrée est positif (+1°)
                Point2D r_emergent = mat_transfert_depuis_pupille_entree.transform(0, Math.toRadians((z_geometrique_objet.get()<z_pupille_entree.get())?-1d:1d));
                // Point2D r_emergent = mat_transfert_depuis_pupille_entree.transform(0, Math.toRadians(1d));

                double ratio_h_emergent = Double.MAX_VALUE ;
                if (its.rayonDiaphragme()!=0d)
                    ratio_h_emergent = Math.abs(r_emergent.getX() / its.rayonDiaphragme());

                // Enregistrement de la hauteur X du dioptre_rencontre avec le diaphragme
                its.h_limite_champ.set(r_emergent.getX());

                LOGGER.log(Level.FINE, "Ratio x/x_diaphragme du diaphragme {0} : {1}",
                        new Object[]{nb_dioptres_rencontres, ratio_h_emergent});

                if (ratio_h_emergent >= ratio_h_emergent_max_depuis_pupille_entree) {
                    ratio_h_emergent_max_depuis_pupille_entree = ratio_h_emergent;
                    index_diaphragme_champ = j ;

//                    // TODO : Optimisation possible car on a déjà calculé les antécédents des diaphragmes plus haut (mais sans les mémoriser)
//                    PositionElement antecedent_diaphragme =
//                            positionAntecedent(its.matriceTransfertPartielle(), new PositionElement(0d,its.rayonDiaphragme()),
//                                    NEntree(), its.indiceApres());
//
//                    // double z_antecedent_diaphragme = z_plan_entree + (pas>0?1d:-1d) * antecedent_diaphragme.z() ; // NON : sur le plan
//                    // d'entree le rayon est toujours dans le sens de l'axe (et pas représente le sens du marche du rayon *au niveau du dioptre courant* et pas en entrée du SOC)
//                    double z_antecedent_diaphragme = z_plan_entree + antecedent_diaphragme.z() ;
//                    double h_antecedent_diaphragme = antecedent_diaphragme.hauteur();
//
//                    z_lucarne_entree_potentielle = z_antecedent_diaphragme;
//                    h_lucarne_entree_potentielle = h_antecedent_diaphragme;
//
//                    z_lucarne_entree_potentielle = its.antecedentDiaphragme().z() ;
//                    h_lucarne_entree_potentielle = its.antecedentDiaphragme().hauteur();
                }
            }
        } // Fin for its

        if (index_diaphragme_champ>=0) {
            dioptres_rencontres.get(index_diaphragme_champ).est_diaphragme_champ.set("✓");

            // L'antécédent du diaphragme de champ par la partie de système qui le précède est la lucarne d'entrée du SOC...
            z_lucarne_entree.set(dioptres_rencontres.get(index_diaphragme_champ).antecedentDiaphragme().z()); // Il s'agit du Z optique de la lucarne d'entrée
            r_lucarne_entree.set(Math.abs(dioptres_rencontres.get(index_diaphragme_champ).antecedentDiaphragme().hauteur()));


            //        z_lucarne_entree.set(z_lucarne_entree_potentielle) ;
            //        h_lucarne_entree.set(h_lucarne_entree_potentielle) ;

            PositionElement image_lucarne_entree =
                    positionImage(resultat, new PositionElement(z_lucarne_entree.get() - ZPlanEntree(), r_lucarne_entree.get()),
                            NEntree(), NSortie());
            // ...et son image est la lucarne de sortie
            z_lucarne_sortie.set(ZOptiquePlanSortie() + image_lucarne_entree.z());
//            z_lucarne_sortie.set(z_geometrique_plan_sortie + (sens_plus_en_sortie.get() ? 1d : -1d) * image_lucarne_entree.z());
            r_lucarne_sortie.set(Math.abs(image_lucarne_entree.hauteur()));

            // Ce rayon a un signe :
            // si l'objet est avant la pupille d'entrée, il est négatif, car on a lancé un rayon d'angle +1° pour trouver les champs
            // si l'objet est après la pupille d'entrée, il est positif
            // r_champ_moyen_objet.set( r_lucarne_entree.get() * (z_objet.get()-z_pupille_entree.get()) / Math.abs((z_pupille_entree.get()-z_lucarne_entree.get())) ) ;

            // Comme on a pris soin de calculer l'angle sur la pupille d'entrée à +1° ou -1° selon les positions respectives (cf. plus haut)
            // du plan objet et du plan de la pupille d'entrée, on est sûr que le r_champ_moyen_objet doit être positif (comme les autres champs objets calculés plus loin)
            r_champ_moyen_objet.set(Math.abs(r_lucarne_entree.get() * (z_pupille_entree.get() - z_geometrique_objet.get()) / Math.abs((z_pupille_entree.get() - z_lucarne_entree.get()))));

            PositionElement position_image_cm = positionImage(resultat, new PositionElement(z_geometrique_objet.get() - ZPlanEntree(), r_champ_moyen_objet.get()), NEntree(), NSortie());
                r_champ_moyen_image.set(position_image_cm.hauteur());

        // Contrôle
        {

            // On avait calculé l'image d'un objet de hauteur positive ; si la hauteur du champ moyen objet est négative,
            // la hauteur algébrique de l'image sera l'opposée de celle qu'on avait trouvée.
            if (r_champ_moyen_objet.get() < 0)
                h_image_precalcule = -h_image_precalcule;

            double h_image_cm = (h_image_precalcule > 0 ? 1d : -1d) * Math.abs(r_lucarne_sortie.get() * (z_pupille_sortie.get() - z_image_precalcule) / Math.abs((z_pupille_sortie.get() - z_lucarne_sortie.get())));

            if (!Environnement.quasiEgal(Math.abs(h_image_cm),Math.abs(r_champ_moyen_image.get())))
                    LOGGER.log(Level.SEVERE,"La hauteur absolue du champ moyen n'est pas le même selon la méthode...");
        }


            angle_champ_moyen_objet.set(angleDeVuDe(z_lucarne_entree.get(), r_lucarne_entree.get(),z_pupille_entree.get()));
            angle_champ_moyen_image.set(angleDeVuDe(z_lucarne_sortie.get(), r_lucarne_sortie.get(),z_pupille_sortie.get()));

            Double r_ct_provisoire = null, r_cpl_provisoire=null ;
            Double h_incidence_entree_cpl_provisoire = null , h_incidence_entree_ct_provisoire = null ;
            Double coeff_dir_bord_cpl_provisoire = null , coeff_dir_bord_ct_provisoire = null ;

            int index_diaphragme_cpl =-1 , index_diaphragme_ct = -1 ;

            for (int k = 0; k< dioptres_rencontres.size(); k++) {

                RencontreDioptreParaxial its = dioptres_rencontres.get(k) ;

                // 1) Calage de la hauteur des rayons limites du champ moyen
                if (its.HLimiteChamp() != null /* && ratio_h_emergent_max_depuis_pupille_entree != 0d */ ) {
                    if (ratio_h_emergent_max_depuis_pupille_entree != Double.MAX_VALUE && ratio_h_emergent_max_depuis_pupille_entree != 0d)
                        its.h_limite_champ.set(its.HLimiteChamp() / ratio_h_emergent_max_depuis_pupille_entree);
                    else
                        its.h_limite_champ.set(0d);
                }


                if (its.antecedentDiaphragme()==null)
                    continue;

                double z_luc = its.antecedentDiaphragme().z() ;
                double r_luc = Math.abs(its.antecedentDiaphragme().hauteur()) ;

                if (!Environnement.quasiEgal(z_luc,z_pupille_entree.get())) {
//                if (z_luc!=z_pupille_entree.get()) {

                    double coeff_dir_1 = (r_luc - r_pupille_entree.get()) / (z_luc - z_pupille_entree.get());
                    double r_extr_1 = (z_luc - z_geometrique_objet.get()) * (-coeff_dir_1) + r_luc;
                    double coeff_dir_2 = (r_luc + r_pupille_entree.get()) / (z_luc - z_pupille_entree.get());
                    double r_extr_2 = (z_luc - z_geometrique_objet.get()) * (-coeff_dir_2) + r_luc;
                    // NB : r_extr_1 et r_extr_2 peuvent être tous deux négatifs, si le plan objet est après la lucarne et la pupille d'entrée


                    // 2) Recherche du Champ de pleine lumière (Cpl) et du Champ total (Ct)
                    double r_cpl_objet, r_ct_objet;
                    double coeff_dir_bord_cpl, coeff_dir_bord_ct;
                    double h_incidence_entree_cpl, h_incidence_entree_ct;

                    if (Math.abs(r_extr_1) <= Math.abs(r_extr_2)) {
                        r_cpl_objet = r_extr_1;
                        coeff_dir_bord_cpl = coeff_dir_1;
                        r_ct_objet = r_extr_2;
                        coeff_dir_bord_ct = coeff_dir_2;
                        h_incidence_entree_cpl = (z_luc - ZPlanEntree()) * (-coeff_dir_1) + r_luc;
                        h_incidence_entree_ct = (z_luc - ZPlanEntree()) * (-coeff_dir_2) + r_luc;
                    } else {
                        r_cpl_objet = r_extr_2;
                        coeff_dir_bord_cpl = coeff_dir_2;
                        r_ct_objet = r_extr_1;
                        coeff_dir_bord_ct = coeff_dir_1;
                        h_incidence_entree_cpl = (z_luc - ZPlanEntree()) * (-coeff_dir_2) + r_luc;
                        h_incidence_entree_ct = (z_luc - ZPlanEntree()) * (-coeff_dir_1) + r_luc;
                    }


////                double r_cpl_objet = Math.min(Math.abs(r_extr_1), Math.abs(r_extr_2));
//                double r_cpl_objet = Math.abs(r_extr_1)<=Math.abs(r_extr_2)?r_extr_1:r_extr_2;
//                double coeff_dir_r_bord_cpl = Math.abs(r_extr_1)<=Math.abs(r_extr_2)?coeff_dir_1:coeff_dir_2;
////                double r_ct_objet  = Math.max(Math.abs(r_extr_1), Math.abs(r_extr_2));
//                double r_ct_objet  = Math.abs(r_extr_1)>=Math.abs(r_extr_2)?r_extr_1:r_extr_2;
//                double coeff_dir_r_bord_ct = Math.abs(r_extr_1)>=Math.abs(r_extr_2)?coeff_dir_1:coeff_dir_2;
//
//                // On calcule au passage la hauteur d'incidence sur la face d'entée du système
//                double h_incidence_entree_cpl = Math.abs(r_extr_1)<=Math.abs(r_extr_2)?
//                         (z_luc-ZPlanEntree())*(r_luc-r_pupille_entree.get())/(z_pupille_entree.get()-z_luc)+r_luc
//                        :(z_luc-ZPlanEntree())*(r_luc+r_pupille_entree.get())/(z_pupille_entree.get()-z_luc)+r_luc  ;
//                double h_incidence_entree_ct = Math.abs(r_extr_1)>=Math.abs(r_extr_2)?
//                        (z_luc-ZPlanEntree())*(r_luc-r_pupille_entree.get())/(z_pupille_entree.get()-z_luc)+r_luc
//                        :(z_luc-ZPlanEntree())*(r_luc+r_pupille_entree.get())/(z_pupille_entree.get()-z_luc)+r_luc  ;


                    if (Double.isFinite(r_cpl_objet) && Double.isFinite(r_ct_objet) && (r_cpl_objet * r_ct_objet) < 0)
                        LOGGER.log(Level.SEVERE, "RCpl et Rct de signes opposés !?...");

//                double r_cpl_bis = Math.abs(r_pupille_entree.get() + (z_pupille_entree.get() - z_objet.get()) * (r_luc - r_pupille_entree.get())
//                            / (z_pupille_entree.get() - z_luc));

                    if ((r_cpl_provisoire == null) || Math.abs(r_cpl_objet) < Math.abs(r_cpl_provisoire)) {
                        r_cpl_provisoire = r_cpl_objet;
                        index_diaphragme_cpl = k;

                        h_incidence_entree_cpl_provisoire = h_incidence_entree_cpl;
                        coeff_dir_bord_cpl_provisoire = coeff_dir_bord_cpl;
                    }

                    // Recherche du Champ total (Ct)
//                double r_ct_bis = Math.abs((r_luc + r_pupille_entree.get()) * (z_objet.get() - z_pupille_entree.get())
//                        / (z_luc - z_pupille_entree.get()) - r_pupille_entree.get());

                    if ((r_ct_provisoire == null) || Math.abs(r_ct_objet) < Math.abs(r_ct_provisoire)) {
                        r_ct_provisoire = r_ct_objet;
                        index_diaphragme_ct = k;

                        h_incidence_entree_ct_provisoire = h_incidence_entree_ct;
                        coeff_dir_bord_ct_provisoire = coeff_dir_bord_ct;
                    }
                }
                    // Contrôle
//                    if (Double.isFinite(r_cpl) && r_cpl < 0)
//                        LOGGER.log(Level.SEVERE, "RCpl ne peut pas être négatif...");
//                    if (Double.isFinite(r_ct) && r_ct < 0)
//                        LOGGER.log(Level.SEVERE, "RCt ne peut pas être négatif...");
//                    if (Double.isFinite(r_cpl_bis) && Double.isFinite(r_ct_bis)) {
//                        if (!Environnement.quasiEgal(r_cpl, Math.min(r_cpl_bis, r_ct_bis)))
//                            LOGGER.log(Level.SEVERE, "RCpl n'est pas le même selon la méthode...");
//                        if (!Environnement.quasiEgal(r_ct, Math.max(r_cpl_bis, r_ct_bis)))
//                            LOGGER.log(Level.SEVERE, "RCt n'est pas le même selon la méthode...");
//                    }

            }

    //        // Contrôle : ce contrôle n'a pas de sens il compare la hauteur du champ moyen dans le plan objet et au niveau du DC...
    //        if (h_champ_moyen_objet.get()!=intersections_reelles_sur_axe.get(index_diaphragme_champ).h_limite_champ.get())
    //            LOGGER.log(Level.SEVERE,"L'angle de champ (moyen) n'est pas le même selon la méthode...");

            if (index_diaphragme_cpl>=0 && index_diaphragme_ct>=0) {

                dioptres_rencontres.get(index_diaphragme_cpl).est_diaphragme_champ_pleine_lumiere.set("✓");
                dioptres_rencontres.get(index_diaphragme_ct).est_diaphragme_champ_total.set("✓");

                // NB : ces rayons ne sont pas algébriques, ils sont toujours positifs (cf.le abs() plus haut)
                r_champ_pleine_lumiere_objet.set(r_cpl_provisoire);
                r_champ_total_objet.set(r_ct_provisoire);

                // TODO : vérifier le signe de ces angles dans différentes configurations : ils sont importants pour calculer
                //  les hauteurs d'incidence des rayons limites du Cpl et du Ct sur tous les dioptres
                angle_champ_pleine_lumiere_objet.set(Math.toDegrees(Math.atan(coeff_dir_bord_cpl_provisoire)));
                angle_champ_total_objet.set(Math.toDegrees(Math.atan(coeff_dir_bord_ct_provisoire)));

                PositionElement image_cpl = positionImage(resultat, new PositionElement(  convertirEnZOptique(z_geometrique_objet.get()) - ZPlanEntree(), r_cpl_provisoire), NEntree(), NSortie());
//                PositionElement image_cpl = positionImage(resultat, new PositionElement( z_objet.get() - z_plan_entree, r_cpl_provisoire), NEntree(), NSortie());
                PositionElement image_ct = positionImage(resultat, new PositionElement(convertirEnZOptique(z_geometrique_objet.get()) - ZPlanEntree(), r_ct_provisoire), NEntree(), NSortie());
//                PositionElement image_ct = positionImage(resultat, new PositionElement(z_objet.get() - z_plan_entree, r_ct_provisoire), NEntree(), NSortie());

                r_champ_pleine_lumiere_image.set(image_cpl.hauteur());
                r_champ_total_image.set(image_ct.hauteur());

                // TODO : vérifier le signe de ces angles dans différentes configurations : ils sont importants pour calculer
                //  les hauteurs d'incidence des rayons limites du Cpl et du Ct sur tous les dioptres
                angle_champ_pleine_lumiere_image.set(angleDeVuDe(ZOptiquePlanSortie() + image_cpl.z(), image_cpl.hauteur() - r_pupille_sortie.get(), z_pupille_sortie.get()));
//                angle_champ_pleine_lumiere_image.set(angleDeVuDe(z_geometrique_plan_sortie + image_cpl.z(), image_cpl.hauteur() - r_pupille_sortie.get(), z_pupille_sortie.get()));
                angle_champ_total_image.set(angleDeVuDe(ZOptiquePlanSortie() + image_ct.z(), image_ct.hauteur() + r_pupille_sortie.get(), z_pupille_sortie.get()));
//                angle_champ_total_image.set(angleDeVuDe(z_geometrique_plan_sortie + image_ct.z(), image_ct.hauteur() + r_pupille_sortie.get(), z_pupille_sortie.get()));


                for (RencontreDioptreParaxial its : dioptres_rencontres) {

                    // Calcul des h limite du champ de pleine lumiere et du champ total sur chaque dioptre

                    // Rayon émergent au niveau de ce dioptre
                    Point2D r_emergent_cpl = its.matriceTransfertPartielle().transform(h_incidence_entree_cpl_provisoire * environnement.unite().valeur, Math.toRadians(angle_champ_pleine_lumiere_objet.get()));
                    Point2D r_emergent_ct = its.matriceTransfertPartielle().transform(h_incidence_entree_ct_provisoire * environnement.unite().valeur, Math.toRadians(angle_champ_total_objet.get()));

                    // Enregistrement de la hauteur X du dioptre_rencontre avec le diaphragme/dioptre
                    its.h_limite_champ_pleine_lumiere.set(r_emergent_cpl.getX());
                    its.h_limite_champ_total.set(r_emergent_ct.getX());

                }

            }

        }

        return resultat ;

    }

    private static Double angleDeVuDe(double z, double h, double z_observateur) {
        if (z==z_observateur)
            return 90d ;
        
        return Math.toDegrees(Math.atan(h/(z-z_observateur))) ;
//        return Math.toDegrees(Math.atan2(h,(z-z_observateur))) ;
    }

    /**
     * Construit la liste de tous les dioptres au voisinage de l'axe du SOC, triée de l'abscisse z = -infini à
     * z = +infini et, à abscisse égale,  de Rc = 0- à -infini puis de +infini à 0+, sans tenir compte de la nature
     * (réfléchissante ou transparente) de ces dioptres (qui sont donc ici supposés, en quelque sorte, tous transparents).
     * @return la liste triée des dioptres
     */
    private ArrayList<DioptreParaxial> extraireDioptresParaxiaux() {

        ArrayList<DioptreParaxial> resultat = new ArrayList<>(2*obstacles_centres.size()) ;

//        Iterator<Obstacle> itoc = obstacles_centres.iterator() ;
        // Attention : si l'iterateur ci-dessous rencontre une composition, il en retourne un par un tous les obstacles
        Iterator<Obstacle> itoc = new IterateurObstaclesCentresReels() ;

        if (itoc.hasNext()) // Les dioptres de l'obstacle le plus en arrière sont supposés initialement tous visibles
            resultat.addAll(itoc.next().dioptresParaxiaux(axe())) ;

        // Itération sur les obstacles centrés suivants, qui sont classés de l'arrière-plan vers l'avant plan
        while (itoc.hasNext()) {

            Obstacle oc = itoc.next() ;

            Double z_deb_recouv = -Double.MAX_VALUE ;
            Double rc_deb_recouv = -Double.MIN_VALUE ;

            List<DioptreParaxial> dioptres_oc = oc.dioptresParaxiaux(axe()) ;

            // Itération sur les dioptres de l'obstacle qui sont classés par Z croissant, et Rc "croissants"
            for (DioptreParaxial d : dioptres_oc) {

                if (d.indiceApres() == 0 && d.indiceAvant() > 0) { // On SORT de l'obstacle oc

                    // Création d'un dioptre virtuel dont la position marque la zone de début de recouvrement
                    DioptreParaxial d_dep_recouv = new DioptreParaxial(z_deb_recouv, rc_deb_recouv);

                    double indice_apres = DioptreParaxial.captureIndiceApres(resultat, d);

                    DioptreParaxial.supprimeDioptresEntre(resultat, d_dep_recouv, d);

                    d.indice_apres.set(indice_apres);

                    // On n'est plus en train de recouvrir des obstacles en arrière-plan
                    z_deb_recouv = null;
                    rc_deb_recouv = null;

                } else if (d.indiceAvant() == 0 && d.indiceApres() > 0) { // On RENTRE dans l'obstacle oc

                    // On commence à recouvrir des obstacles en arrière-plan
                    z_deb_recouv = d.z();
                    rc_deb_recouv = d.rayonCourbure();

                    double indice_avant = DioptreParaxial.captureIndiceAvant(resultat, d);

                    d.indice_avant.set(indice_avant);

                } else if (d.indiceAvant() == 0 && d.indiceApres() == 0) { // Obstacle de type segment (qui, pour rappel, ne peut faire partie d'une composition)
                    d.indice_avant.set(DioptreParaxial.captureIndiceAvant(resultat, d));
                    d.indice_apres.set(DioptreParaxial.captureIndiceApres(resultat, d));
                } else { // Indice avant et indice après sont égaux et non-nuls ("faux dioptre" fusionné qui ne sert qu'à porter un R diaphragme)
                }
            }

            if (z_deb_recouv!=null && z_deb_recouv!=-Double.MAX_VALUE) { // On était en train de recouvrir des obstacles en arrière-plan
                // Création d'un dioptre virtuel dont la position marque la zone de début de recouvrement.
                DioptreParaxial d_dep_recouv = new DioptreParaxial(z_deb_recouv,rc_deb_recouv) ;

                DioptreParaxial.supprimeDioptresApres(resultat,d_dep_recouv);
            }

            // Tous les dioptres recouverts ont été supprimés de résultat. Il ne reste plus qu'à ajouter tous les dioptres
            // de l'obstacle courant qui est en avant ("au-dessus") de tous les précédents.

            resultat.addAll(dioptres_oc) ;

        }

        // Rappel : la méthode Obstacle::extraireDioptresParaxiaux() ne retourne normalement pas de dioptres avec un Rcourbure quasi
        // égal à 0 sinon il faudrait ici les supprimer :
        // resultat.removeIf(d -> Environnement.quasiEgal(d.rayonCourbure() , 0d )) ;

        // On trie le résultat (pour l'affichage à l'utilisateur). On peut ignorer le niveau de profondeur des obstacles dans ce tri,
        // car les recouvrements ont déjà été pris en compte : il ne reste que des dioptres réellement visibles.
        resultat.sort(DioptreParaxial.comparateur) ;

        // Renseigner les indices d'arrière-plan à la place des indices à 0
        for (DioptreParaxial d : resultat) {
            if (d.indiceAvant()==0) d.indice_avant.set(environnement.indiceRefraction());
            if (d.indiceApres()==0) d.indice_apres.set(environnement.indiceRefraction());
        }

        return resultat ;

    }


    private boolean aSurSaSurface(Point2D pt) {
        for (Obstacle o: obstacles_centres) {
            if (o.aSurSaSurface(pt))
                return true ;
        }

        return false ;
    }

    private static int compteur_soc = 0 ;

    protected static ArrayList<SystemeOptiqueCentre> tous_les_soc = new ArrayList<>() ;

    public SystemeOptiqueCentre(Environnement env, Point2D origine, double orientation_deg) {
        this(env,new Imp_Nommable("Syst. Opt. Centré  " + (++compteur_soc)),origine,orientation_deg) ;
    }

    public SystemeOptiqueCentre(Environnement env, Imp_Nommable iei , Point2D origine, double orientation_deg) {
        super(iei);

        tous_les_soc.add(this) ;

        this.environnement = env ;

        // Inutile : c'est l'Environnement qui se charge de déclencher les conversions de dimensions dans les SOCs lorsque
        // l'unité change.
//        this.environnement.uniteProperty().addListener( ( (observableValue, oldValue, newValue) -> {
//                    LOGGER.log(Level.FINER,"unite passe de {0} à {1}",new Object[] {oldValue,newValue});
//                    calculeElementsCardinaux();
//                } )
//        ) ;

        ObservableList<Obstacle> ols = FXCollections.observableArrayList() ;
        obstacles_centres   = new SimpleListProperty<>(ols);

        // A sa création, le SOC ne contient pas d'éléments : ses milieux d'entrée et de sortie sont donc identiques, et
        // sont le milieu de l'environnement général (qui peut changer, donc nécessité d'un binding)
        this.n_entree = new SimpleDoubleProperty(env.indiceRefraction()) ;
        this.n_sortie = new SimpleDoubleProperty(env.indiceRefraction()) ;

        this.sens_plus_en_sortie = new SimpleBooleanProperty(true) ;

        // Si on voulait gérer un éventuel changement de l'indice du milieu de l'environnement, il faudrait faire un
        // binding entre les milieux d'entrée/sortie du SOC et le milieu de l'environnement, en activant les deux lignes
        // ci-dessous. Mais il faudrait aussi faire les unbinds correspondants lorsqu'on ajoute dans le SOC un milieu
        // illimité qui devient le nouveau milieu d'entrée, ou de sortie (et les défaire lorsque cet obstacle illimité est
        // retiré du SOC). Il y aurait plusieurs choses à revoir dans la méthode old_chercheIntersectionSuivanteDepuis(),
        // pour garder une référence sur l'obstacle illimité du SOC qui constitue son milieu d'entrée (s'il y en a un)
        // et une autre sur celui qui constitue son milieu de sortie (s'il y en a un), et dans la methode
        // calculeMatriceTransfertOptique() pour faire/défaire les bindings lorsqu'on définit n_entree et n_sortie
        // L'utilité de faire tout ça n'est pas évidente...
//        this.n_entree.bind(env.indiceRefractionProperty());
//        this.n_sortie.bind(env.indiceRefractionProperty());

        this.axe = new SimpleObjectProperty<>(new PositionEtOrientation(origine,orientation_deg)) ;

        this.axe.addListener((observable, oldValue, newValue) -> {

            if (suspendre_repositionnement_obstacles)
                return ;

//            position_orientation.set(new PositionEtOrientation(nouveau_foyer,orientation()+angle_rot_deg));

            suspendre_calcul_elements_cardinaux = true ;

            Point2D delta_pos = newValue.position().subtract(oldValue.position()) ;
            double delta_angle_rot_deg = newValue.orientation_deg()- oldValue.orientation_deg() ;
            for (Obstacle o : obstacles_centres) {
                o.tournerAutourDe(this.origine(),delta_angle_rot_deg);
                o.translater(delta_pos);
            }
            suspendre_calcul_elements_cardinaux = false ;

        });

        this.couleur_axe = new SimpleObjectProperty<>(couleur_axe_par_defaut) ;

        this.matrice_transfert_es = new SimpleObjectProperty<>(null) ;

        this.montrer_dioptres = new SimpleBooleanProperty(false) ;
        this.montrer_objet = new SimpleBooleanProperty(false) ;
        this.montrer_image = new SimpleBooleanProperty(false) ;
        this.montrer_plans_focaux = new SimpleBooleanProperty(false) ;
        this.montrer_plans_principaux = new SimpleBooleanProperty(false) ;
        this.montrer_plans_nodaux = new SimpleBooleanProperty(false) ;

        this.z_plan_entree = new SimpleObjectProperty<>(null) ;
        this.z_geometrique_plan_sortie = new SimpleObjectProperty<>(null) ;
        this.z_optique_plan_sortie = new SimpleObjectProperty<>(null) ;

        this.z_geometrique_plan_focal_objet = new SimpleObjectProperty<>(null) ;
        this.z_geometrique_plan_focal_image = new SimpleObjectProperty<>(null) ;
        this.z_geometrique_plan_principal_objet = new SimpleObjectProperty<>(null) ;
        this.z_geometrique_plan_principal_image = new SimpleObjectProperty<>(null) ;
        this.z_geometrique_plan_nodal_objet = new SimpleObjectProperty<>(null) ;
        this.z_geometrique_plan_nodal_image = new SimpleObjectProperty<>(null) ;

        this.z_optique_plan_focal_objet = new SimpleObjectProperty<>(null) ;
        this.z_optique_plan_focal_image = new SimpleObjectProperty<>(null) ;
        this.z_optique_plan_principal_objet = new SimpleObjectProperty<>(null) ;
        this.z_optique_plan_principal_image = new SimpleObjectProperty<>(null) ;
        this.z_optique_plan_nodal_objet = new SimpleObjectProperty<>(null) ;
        this.z_optique_plan_nodal_image = new SimpleObjectProperty<>(null) ;
        
        this.z_geometrique_objet = new SimpleObjectProperty<>(0.0) ;
        this.h_objet = new SimpleObjectProperty<>(1.0) ;
        this.z_optique_image = new SimpleObjectProperty<>(null) ;
        this.z_geometrique_image = new SimpleObjectProperty<>(null) ;
        this.h_image = new SimpleObjectProperty<>(null) ;

        this.grandissement_transversal  = new SimpleObjectProperty<>(null) ;
        this.grandissement_angulaire    = new SimpleObjectProperty<>(null) ;
        this.grandissement_longitudinal = new SimpleObjectProperty<>(null) ;

        this.z_pupille_entree = new SimpleObjectProperty<>(null) ;
        this.r_pupille_entree = new SimpleObjectProperty<>(null) ;
        this.z_pupille_sortie = new SimpleObjectProperty<>(null) ;
        this.r_pupille_sortie = new SimpleObjectProperty<>(null) ;

        this.z_lucarne_entree = new SimpleObjectProperty<>(null) ;
        this.r_lucarne_entree = new SimpleObjectProperty<>(null) ;
        this.z_lucarne_sortie = new SimpleObjectProperty<>(null) ;
        this.r_lucarne_sortie = new SimpleObjectProperty<>(null) ;

        this.angle_ouverture = new SimpleObjectProperty<>(null) ;

        this.r_champ_moyen_objet = new SimpleObjectProperty<>(null) ;
        this.r_champ_pleine_lumiere_objet = new SimpleObjectProperty<>(null) ;
        this.r_champ_total_objet = new SimpleObjectProperty<>(null) ;
        this.r_champ_moyen_image = new SimpleObjectProperty<>(null) ;
        this.r_champ_pleine_lumiere_image = new SimpleObjectProperty<>(null) ;
        this.r_champ_total_image = new SimpleObjectProperty<>(null) ;
        
        this.angle_champ_moyen_objet = new SimpleObjectProperty<>(null) ;
        this.angle_champ_pleine_lumiere_objet = new SimpleObjectProperty<>(null) ;
        this.angle_champ_total_objet = new SimpleObjectProperty<>(null) ;
        this.angle_champ_moyen_image = new SimpleObjectProperty<>(null) ;
        this.angle_champ_pleine_lumiere_image = new SimpleObjectProperty<>(null) ;
        this.angle_champ_total_image = new SimpleObjectProperty<>(null) ;

        // Nécessaire pour déclencher le recalcul de la position des Diaph. d'Ouverture, de Champ, etc. qui dépendent
        // de la position du plan objet
        z_geometrique_objet.addListener((observable, oldValue, newValue) -> { calculeElementsCardinaux(); });

        // Calcul de la position optique de l'image grâce à la relation homographique, valable pour un système focal ou afocal
        ObjectBinding<Double> calcule_z_optique_image = new ObjectBinding<>() {

            // On ne met pas la dépendance à n_entree/n_sortie car ils sont forcément modifiés en même temps que la matrice de transfert
            {
                super.bind(matrice_transfert_es, z_geometrique_objet, n_entree, n_sortie);
            }

            @Override
            protected Double computeValue() {

//                if (suspendre_calcul_image)
//                    return null ; // Ne rien faire
                if (nouveau_z_optique_image_apres_conversion_a_prendre_compte) {
                    nouveau_z_optique_image_apres_conversion_a_prendre_compte = false;
                    return nouveau_z_optique_image_apres_conversion;
                }

                if (matrice_transfert_es.get() == null || z_geometrique_objet.get() == null || z_plan_entree.get()==null || z_optique_plan_sortie.get()==null)
                    return null;
                System.out.println("----");
                System.out.println("Z geometrique objet = "+z_geometrique_objet.get());

                double z_optique_objet = convertirEnZOptique(z_geometrique_objet.get()) ;
                System.out.println("Z optique objet = "+z_optique_objet);

                double z_optique_image = ZOptiquePlanSortie() + positionImage(matrice_transfert_es.get(), new PositionElement(z_optique_objet - ZPlanEntree(), 0d), n_entree.get(), n_sortie.get()).z();
//                double z_optique_image = z_optique_plan_sortie + positionImage(matrice_transfert_es.get(), new PositionElement(z_optique_objet - z_plan_entree, 0d), n_entree.get(), n_sortie.get()).z();
                System.out.println("Z optique image (méthode 1) = "+z_optique_image);

                // Code ci-dessous laissé provisoirement pour contrôle : TODO : à supprimer ;

                // Début code de contrôle
                double a = matrice_transfert_es.get().getMxx();
                double b = matrice_transfert_es.get().getMxy();
                double c = matrice_transfert_es.get().getMyx();
                double d = matrice_transfert_es.get().getMyy();

                // Relation homographique (Optique : Fondements et applications, J-Ph. Perez, chapitre 6)
                System.out.println("Z (optique) plan entrée = "+ZPlanEntree());
                System.out.println("Z optique plan sortie = "+ZOptiquePlanSortie());

                double z_optique_image_bis = (ZOptiquePlanSortie() * environnement.unite().valeur
                        + n_sortie.get()
                        * (a * (z_optique_objet - ZPlanEntree()) * environnement.unite().valeur / n_entree.get() - b)
                        / (-c * (z_optique_objet - ZPlanEntree()) * environnement.unite().valeur / n_entree.get() + d))
                        / environnement.unite().valeur ;

                System.out.println("Z optique image (méthode 2) = "+z_optique_image_bis);

                if (!Environnement.quasiEgal(z_optique_image, z_optique_image_bis))
                    LOGGER.log(Level.SEVERE, "Les Z optiques image ne sont pas les mêmes selon la méthode de calcul !");

                // Fin code de contrôle

                return z_optique_image;

            }
        };
        z_optique_image.bind(calcule_z_optique_image);


        // Calcul de la position géométrique de l'image grâce à sa position optique
        ObjectBinding<Double> calcule_z_geometrique_image = new ObjectBinding<>() {

            // On ne met pas la dépendance à n_entree/n_sortie car ils sont forcément modifiés en même temps que la matrice de transfert
            {
                super.bind(z_optique_image);
//                super.bind(matrice_transfert_es, z_geometrique_objet, n_entree, n_sortie);
            }

            @Override
            protected Double computeValue() {

                if (z_optique_image==null||z_optique_image.get()==null)
                    return null ;

                return ZGeometriquePlanSortie()+(sens_plus_en_sortie.get()?1d:-1d)*(z_optique_image.get()-ZOptiquePlanSortie()) ;
//                return z_optique_plan_sortie+(sens_plus_en_sortie.get()?1d:-1d)*(z_optique_image.get()-z_optique_plan_sortie) ;
//                return convertirEnZGeometrique(z_optique_image.get()) ;


            }
        };
        z_geometrique_image.bind(calcule_z_geometrique_image);

        ObjectBinding<Double> calcule_h_image = new ObjectBinding<>() {

            // On ne met pas la dépendance à n_entree/n_sortie car ils sont forcément modifiés en même temps que la matrice de transfert
            { super.bind(matrice_transfert_es, z_geometrique_objet,h_objet,n_entree,n_sortie) ;}
            @Override protected Double computeValue() {

//                if (suspendre_calcul_image)
//                    return null ; // Ne rien faire
                if (nouveau_h_image_apres_conversion_a_prendre_compte) {
                    nouveau_h_image_apres_conversion_a_prendre_compte = false ;
                    return nouveau_h_image_apres_conversion ;
                }

                if (matrice_transfert_es.get() == null || z_geometrique_objet.get() == null || h_objet.get() == null /*|| z_objet.get()>z_plan_entree*/)
                    return null ;

                double z_optique_objet = convertirEnZOptique(z_geometrique_objet.get()) ;

                double resultat = positionImage(matrice_transfert_es.get(), new PositionElement(z_optique_objet-ZPlanEntree(), h_objet.get()),n_entree.get(),n_sortie.get()).hauteur() ;
//                double resultat = positionImage(matrice_transfert_es.get(), new PositionElement(z_geometrique_objet.get()-z_plan_entree, h_objet.get()),n_entree.get(),n_sortie.get()).hauteur() ;

                // Code ci-dessous laissé provisoirement pour contrôle : TODO : à supprimer ;
//                double z_image = n_image * ( a * z_obj_sur_n_obj -b ) / ( -c * z_obj_sur_n_obj + d ) ;
//
//                double g_transversal = a + c * z_image / n_image ;

                // Début code de contrôle
                double a = matrice_transfert_es.get().getMxx();
                double b = matrice_transfert_es.get().getMxy();
                double c = matrice_transfert_es.get().getMyx();
                double d = matrice_transfert_es.get().getMyy();

                double resultat_bis =  h_objet.get()*(a+c*(a*(z_optique_objet-ZPlanEntree())*environnement.unite().valeur/n_entree.get() - b)
                        / (-c*(z_optique_objet-ZPlanEntree())*environnement.unite().valeur/n_entree.get()+d)) ;
//                double resultat_bis =  h_objet.get()*(a+c*(a*(z_geometrique_objet.get()-z_plan_entree)*environnement.unite().valeur/n_entree.get() - b)
//                        / (-c*(z_geometrique_objet.get()-z_plan_entree)*environnement.unite().valeur/n_entree.get()+d)) ;

                if (!Environnement.quasiEgal(resultat,resultat_bis))
                    LOGGER.log(Level.SEVERE,"Les H image ne sont pas les mêmes selon la méthode de calcul !") ;

                // Fin code de contrôle

                return resultat ;

            }
        };
        h_image.bind(calcule_h_image);

        ObjectBinding<Double> calcul_grandissement_transversal = new ObjectBinding<>() {
            { super.bind(matrice_transfert_es, z_optique_image,n_sortie) ;}
//            { super.bind(matrice_transfert_es, z_geometrique_image,n_sortie) ;}

            @Override protected Double computeValue() {

                if (nouveau_g_t_a_prendre_compte_apres_conversion) {
                    nouveau_g_t_a_prendre_compte_apres_conversion = false ;
                    return nouveau_g_t_apres_conversion;
                }

                if (matrice_transfert_es.get()==null)
                    return null ;

                double a = matrice_transfert_es.get().getMxx();
                double b = matrice_transfert_es.get().getMxy();
                double c = matrice_transfert_es.get().getMyx();
                double d = matrice_transfert_es.get().getMyy();


                if (Environnement.quasiEgal(c,0d)) // Système afocal : Gt constant, ne dépend pas de la position de l'objet
                    return a ;

                if (z_optique_image==null || z_optique_image.get()==null)
                    return null ;

//                double z_optique_image = convertirEnZOptique(z_geometrique_image.get()) ;
//                double z_optique_image = z_optique_image.get() ;

                return ( a+c*((z_optique_image.get() - ZOptiquePlanSortie())*environnement.unite().valeur / n_sortie.get() ) );
//                return ( a+c*((z_optique_image - z_optique_plan_sortie)*environnement.unite().valeur / ((sens_plus_en_sortie.get()?1d:-1d)*n_sortie.get()) ) );
//                return ( a+c*((z_geometrique_image.get() - z_geometrique_plan_sortie)*environnement.unite().valeur / n_sortie.get()) );
            }
        };
        grandissement_transversal.bind(calcul_grandissement_transversal);

        ObjectBinding<Double> calcul_grandissement_angulaire = new ObjectBinding<>() {
            { super.bind(matrice_transfert_es,sens_plus_en_sortie,n_entree,n_sortie, h_objet,h_image) ;}
            @Override protected Double computeValue() {

                if (matrice_transfert_es.get()==null)
                    return null ;

                double d = matrice_transfert_es.get().getMyy() ;

                // A-t-on un système afocal ?
                if (Environnement.quasiEgal(matrice_transfert_es.get().getMyx(),0))
                    return d*n_entree.get()/n_sortie.get() ;

                if (h_objet.get()==null || h_image.get()==null || h_objet.get()==0d)
                    return null ;

                return  (sens_plus_en_sortie.get()?1.0:-1.0) * (n_entree.get()/n_sortie.get()) * (h_objet.get()/h_image.get()) ;
            }
        };

        grandissement_angulaire.bind(calcul_grandissement_angulaire);

        ObjectBinding<Double> calcul_grandissement_longitudinal = new ObjectBinding<>() {
            {super.bind(calcul_grandissement_transversal,calcul_grandissement_angulaire) ;}
            @Override protected Double computeValue() {
                if (calcul_grandissement_transversal.get()==null || calcul_grandissement_angulaire.get()==null)
                    return null ;

                return calcul_grandissement_transversal.get()/calcul_grandissement_angulaire.get() ;
            }
        } ;

        grandissement_longitudinal.bind(calcul_grandissement_longitudinal);

        ObservableList<DioptreParaxial> oli_int = FXCollections.observableArrayList() ;
        this.dioptres = new SimpleListProperty<>(oli_int);

        ObservableList<RencontreDioptreParaxial> oli_int_r = FXCollections.observableArrayList() ;
        this.dioptres_rencontres = new SimpleListProperty<>(oli_int_r);

    }

    public Point2D Origine() { return axe.get().position() ;}
    public double XOrigine() { return axe.get().position().getX() ;}
    public double YOrigine() { return axe.get().position().getY() ;}

    public ObjectProperty<PositionEtOrientation> axeObjectProperty() { return axe;}
    public PositionEtOrientation axe() { return axe.get() ;}

    public double orientation() { return axe.get().orientation_deg(); }

    public PositionElement pupilleEntree() {return ((z_pupille_entree!=null&&z_pupille_entree.get()!=null&&r_pupille_entree!=null&&r_pupille_entree.get()!=null)?new PositionElement(z_pupille_entree.get(),r_pupille_entree.get()):null) ;}
    public PositionElement pupilleSortie() {return ((z_pupille_sortie!=null&&z_pupille_sortie.get()!=null&&r_pupille_sortie!=null&&r_pupille_sortie.get()!=null)?new PositionElement(z_pupille_sortie.get(),r_pupille_sortie.get()):null) ;}
    public PositionElement lucarneEntree() {return ((z_lucarne_entree!=null&&z_lucarne_entree.get()!=null&&r_lucarne_entree!=null&&r_lucarne_entree.get()!=null)?new PositionElement(z_lucarne_entree.get(),r_lucarne_entree.get()):null) ;}
    public PositionElement lucarneSortie() {return ((z_lucarne_sortie!=null&&z_lucarne_sortie.get()!=null&&r_lucarne_sortie!=null&&r_lucarne_sortie.get()!=null)?new PositionElement(z_lucarne_sortie.get(), r_lucarne_sortie.get()):null) ;}

    public Color couleurAxe() { return couleur_axe.get() ; }
    public void definirCouleurAxe(Color c) { couleur_axe.set(c); }

    public ObjectProperty<Color> couleurAxeProperty() { return couleur_axe ;}

    public ObservableList<Obstacle> obstacles_centres() {return obstacles_centres.get() ;}
    public Stream<Obstacle> stream_obstacles_centres() {return obstacles_centres.stream() ;}

    public void ajouterRappelSurChangementToutePropriete(RappelSurChangement rap) {
        axe.addListener((observable, oldValue, newValue) -> rap.rappel());
        couleur_axe.addListener((observable, oldValue, newValue) -> rap.rappel());

        montrer_dioptres.addListener((observable, oldValue, newValue) -> rap.rappel());

        z_geometrique_objet.addListener((observable, oldValue, newValue) -> rap.rappel());
        h_objet.addListener((observable, oldValue, newValue) -> rap.rappel());

        montrer_objet.addListener((observable, oldValue, newValue) -> rap.rappel());
        montrer_image.addListener((observable, oldValue, newValue) -> rap.rappel());

        montrer_plans_focaux.addListener((observable, oldValue, newValue) -> rap.rappel());
        montrer_plans_principaux.addListener((observable, oldValue, newValue) -> rap.rappel());
        montrer_plans_nodaux.addListener((observable, oldValue, newValue) -> rap.rappel());

        matrice_transfert_es.addListener((observable, oldValue, newValue) -> rap.rappel());
    }

    public void ajouterRappelSurChangementTouteProprieteModfiantElementsCardinaux(RappelSurChangement rap) {
        for (Obstacle o : obstacles_centres)
            o.ajouterRappelSurChangementToutePropriete(rap);
    }


    public void translater(Point2D tr) {
        axe.set(new PositionEtOrientation(origine().add(tr),orientation()));
    }

    public Point2D origine() {return axe.get().position();}

    public void definirOrigine(Point2D origine) { axe.set(new PositionEtOrientation(origine,orientation()));  }

    public void definirOrientation(double or_deg) {
        axe.set(new PositionEtOrientation(origine(),or_deg));
    }
    public void definirDirection(Point2D direction) {

        if (direction==null || direction.magnitude()==0d) {
            throw new IllegalArgumentException("La direction du SOC doit être définie et ne peut pas être un vecteur nul.") ;
        }

        double angle_deg = direction.angle(new Point2D(1,0)) ;

        if (direction.getY()>=0d)
            definirOrientation(angle_deg);
        else
            definirOrientation(360d-angle_deg);

    }

    public Point2D point(double z_geom, double h_geom) {
        return origine().add(direction().multiply(z_geom)).add(perpendiculaireDirection().multiply(h_geom)) ;
    }

    public Point2D intersectionDroiteSupportAvecPlan(DemiDroiteOuSegment dds,double z_plan) {
        return dds.intersectionDroiteSupportAvecDroiteSupportDe(new DemiDroiteOuSegment(point(z_plan, 0), perpendiculaireDirection())) ;
    }

    public void retaillerPourSourisEn(Point2D pos_souris) {
        // Si on est sur l'origine, ne rien faire
        if (pos_souris.equals(origine()))
            return ;

        // On oriente la direction du SOC sur la position courante de la souris
        definirDirection(pos_souris.subtract(origine()));

    }

    public Contour positions_poignees() {
        Contour c_poignees = new Contour(1) ;

        c_poignees.ajoutePoint(origine());

        return c_poignees ;
    }

    public boolean est_tres_proche_de(Point2D p, double tolerance_pointage) {
        return Environnement.quasiEgal(produit_vectoriel_simplifie(direction(),p.subtract(origine())),0d,tolerance_pointage) ;
    }

    private double produit_vectoriel_simplifie(Point2D v1, Point2D v2) {
        return (v1.getX()*v2.getY()-v1.getY()*v2.getX()) ;
    }

    public Point2D direction() {
        return new Point2D(Math.cos(Math.toRadians(orientation())),Math.sin(Math.toRadians(orientation()))) ;
    }

    public Point2D perpendiculaireDirection() {
        Point2D dir = direction() ;
        return new Point2D(-dir.getY(),dir.getX()) ;
    }

    public void accepte(VisiteurEnvironnement v) {
        v.visiteSystemeOptiqueCentre(this) ;
    }

    public Contour couper(BoiteLimiteGeometrique boite) {

        Contour contour = null ;

        DemiDroiteOuSegment s = new DemiDroiteOuSegment(origine(),direction()) ;

        Point2D p_inter1 = boite.premiere_intersection(s) ;
        Point2D p_inter2 = boite.derniere_intersection(s) ;

        if (p_inter1 != null && p_inter1.equals(p_inter2))
            p_inter2 = null ;

        DemiDroiteOuSegment s_opp = new DemiDroiteOuSegment(origine(),direction().multiply(-1.0)) ;

        Point2D p_inter_opp1 = boite.premiere_intersection(s_opp) ;
        Point2D p_inter_opp2 = boite.derniere_intersection(s_opp) ;

        if (p_inter_opp1 != null && p_inter_opp1.equals(p_inter_opp2))
            p_inter_opp2 = null ;

        ArrayList<Point2D> its = new ArrayList<>(2) ;

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

        if (its.size()==2) {
            contour = new Contour(2);
            contour.ajoutePoint(its.get(0));
            contour.ajoutePoint(its.get(1));
        }

        return contour ;

    }

    public boolean estEligible(Obstacle o) {
        return (o.aSymetrieDeRevolution() && !this.comprend(o)
                //&& environnement.systemeOptiqueCentreContenant(o) == null
                && !o.appartientASystemeOptiqueCentre()
                && (o.parent() instanceof Groupe grp && grp.parent()==null) // Obstacle doit être à la racine (pas un sous-groupe ni une sous-composition, ni un élément de ceux-ci)
                ) ;
    }

    public void ajouterObstacleCentre(Obstacle o) {

        if (!estEligible(o))
            return ;

        positionnerObstacle(o);

        // Insertion de l'objet à sa place compte tenu de son indexParmiObstacles dans l'environnement (après les objets de
        // indexParmiObstacles inférieur et avant ceux de indexParmiObstacles supérieur).
        if (obstacles_centres.size()==0 || environnement.indexParmiObstacles(o)>environnement.indexParmiObstacles(obstacles_centres.get(obstacles_centres.size()-1))) {
            obstacles_centres.add(o) ; // Ajout en dernière position
        } else {

            for (int i=0 ; i<obstacles_centres.size(); ++i) {
                if (environnement.indexParmiObstacles(o)<environnement.indexParmiObstacles(obstacles_centres.get(i))) {
                    obstacles_centres.add(i,o) ; // Insertion à la place du premier objet qui a un index supérieur (reste de la liste est déplacé à droite)
                    break;
                }
            }
        }

        o.definirAppartenanceSystemeOptiqueCentre(true) ;

        calculeElementsCardinaux();

        // Déclencher un recalcul des éléments cardinaux dès qu'un attribut ou un élément de l'obstacle change
        o.ajouterRappelSurChangementToutePropriete(this::calculeElementsCardinaux);

    }

    protected Point2D translationPourAjoutObstacle(Obstacle o) {
        Point2D axe_soc = direction() ;
        Point2D point_sur_axe_revolution = o.pointSurAxeRevolution().subtract(origine()) ;

        double distance_algebrique_point_sur_axe_revolution_axe_soc = (point_sur_axe_revolution.getX()*axe_soc.getY()-point_sur_axe_revolution.getY()*axe_soc.getX()) ;

        // Peut-être faut-il prendre l'opposé :  à tester...
        return perpendiculaireDirection().multiply(distance_algebrique_point_sur_axe_revolution_axe_soc) ;
    }

    protected double angleRotationPourAjoutObstacle(Obstacle o) {
        return (orientation() - o.orientation())%180d ;
    }

    private void positionnerObstacle(Obstacle o)  {

//        Point2D axe_soc = direction() ;
//        Point2D point_sur_axe_revolution = o.pointSurAxeRevolution().subtract(origine()) ;
//
//        double distance_algebrique_point_sur_axe_revolution_axe_soc = (point_sur_axe_revolution.getX()*axe_soc.getY()-point_sur_axe_revolution.getY()*axe_soc.getX()) ;
//
//        // Peut-être faut-il prendre l'opposé :  à tester...
//        Point2D translation = perpendiculaireDirection().multiply(distance_algebrique_point_sur_axe_revolution_axe_soc) ;

        // TODO : il faudrait désactiver les rappels avant de faire cette translation (déclenche d'inutiles recalculs de tous les rayons...)
        // et les réactiver juste après

        o.translater(translationPourAjoutObstacle(o));

//        if (!o.estOrientable())
//            return ;

        // Tourner autour du point sur axe translaté (pour gérer les Composition)

        // TODO : à revoir, pour une ellipse, on peut se retrouver avec le périhélie tantôt avant l'origine du SOC, et tantôt après (le long de l'axe Z)
        // or le calcul des z des dioptres (pour l'ellipse) fait l'hypothèse qu'il est toujours avant (cf. Ellipse::extraireDioptresParaxiaux : z_int_min = z_foyer - p/(1+e) )
//        o.tournerAutourDe(o.pointSurAxeRevolution(),(orientation() - o.orientation())%180d);
        o.tournerAutourDe(o.pointSurAxeRevolution(),angleRotationPourAjoutObstacle(o));
//        o.definirOrientation(orientation()) ;

//        throw new NoSuchMethodException("La méthode integrerDansSystemeOptiqueCentre() n'est pas implémentée par l'Obstacle "+this) ;
    }

    public void retirerObstacleCentre(Obstacle o) {

        if (!this.comprend(o))
            return ;

        obstacles_centres.remove(o) ;

        // TODO : il faudrait aussi retirer le rappel qui déclenche le recalcul des elements cardinaux
        // sinon, on déclenche des recalculs inutiles de ces éléments cardinaux
        //o.retirerRappelSurChangementToutePropriete(this::calculeElementsCardinaux);

        o.definirAppartenanceSystemeOptiqueCentre(false);

        if (obstacles_centres.size()==0)
            dioptres_rencontres.clear();

        calculeElementsCardinaux();

    }

    public boolean comprend(Obstacle o) {
        for (Obstacle obc : obstacles_centres) {
          if (obc.comprend(o))
              return true ;
        }
        return false ;
//        return obstacles_centres.contains(o) ;
    }

    public Point2D vecteurDirecteurAxe() {
        double theta = Math.toRadians(orientation()) ;
        return new Point2D(Math.cos(theta),Math.sin(theta)) ;
    }

    public void detacherObstacles() {
        for (Obstacle o : obstacles_centres) {
            o.definirAppartenanceSystemeOptiqueCentre(false);
        }

        obstacles_centres.clear();
    }

    public void convertirDistances(double facteur_conversion) {
        // Lors d'une conversion des distances de l'environnement, c'est l'environnement qui se charge de repositionner
        // les obstacles : inutile de le faire ici.
        suspendre_repositionnement_obstacles = true ;
        axe.set(new PositionEtOrientation(origine().multiply(facteur_conversion),orientation()));
        suspendre_repositionnement_obstacles = false ;

        z_plan_entree.set(ZPlanEntree()*facteur_conversion) ;
        z_geometrique_plan_sortie.set(ZGeometriquePlanSortie()*facteur_conversion) ;

//        z_plan_entree = z_plan_entree*facteur_conversion ;
//        z_geometrique_plan_sortie = z_geometrique_plan_sortie *facteur_conversion ;

        convertirObjectDoubleProperty(z_geometrique_plan_focal_objet,facteur_conversion);
        convertirObjectDoubleProperty(z_geometrique_plan_focal_image,facteur_conversion);

        convertirObjectDoubleProperty(z_geometrique_plan_principal_objet,facteur_conversion);
        convertirObjectDoubleProperty(z_geometrique_plan_principal_image,facteur_conversion);

        convertirObjectDoubleProperty(z_geometrique_plan_nodal_objet,facteur_conversion);
        convertirObjectDoubleProperty(z_geometrique_plan_nodal_image,facteur_conversion);

        convertirObjectDoubleProperty(z_optique_plan_focal_objet,facteur_conversion);
        convertirObjectDoubleProperty(z_optique_plan_focal_image,facteur_conversion);

        convertirObjectDoubleProperty(z_optique_plan_principal_objet,facteur_conversion);
        convertirObjectDoubleProperty(z_optique_plan_principal_image,facteur_conversion);

        convertirObjectDoubleProperty(z_optique_plan_nodal_objet,facteur_conversion);
        convertirObjectDoubleProperty(z_optique_plan_nodal_image,facteur_conversion);
        
        
        for (DioptreParaxial d : dioptres)
            d.convertirDistances(facteur_conversion) ;

        for (RencontreDioptreParaxial rd : dioptres_rencontres)
            rd.convertirDistances(facteur_conversion) ;

        // Impossible de déclencher le recalcul de z_image, h_image car il ne pourrait être juste tant qu'on n'a pas encore
        // défini la nouvelle unité de l'Environnement ce qui est fait après l'appel à cette méthode (cf. Environnement::changerUnite)
//        suspendre_calcul_image = true ;

//        z_image.unbind();
//        h_image.unbind();

        nouveau_z_optique_image_apres_conversion_a_prendre_compte = true ;
        nouveau_z_optique_image_apres_conversion = (z_optique_image.get() != null ? z_optique_image.get()*facteur_conversion : null) ;
        nouveau_h_image_apres_conversion = (h_image.get() != null ? h_image.get()*facteur_conversion : null) ;
        nouveau_g_t_apres_conversion = (grandissement_transversal.get() != null ? grandissement_transversal.get() : null) ;

        nouveau_h_image_apres_conversion_a_prendre_compte = true ;
        nouveau_g_t_a_prendre_compte_apres_conversion = true ;

        // Déclenche une màj de z_image et h_image (et le passage à false du flag nouveau_h_image), et un calcul du
        // grandissement transversal, puisque z_image va être mis à jour du fait de la mise à jour de z_objet.
        convertirObjectDoubleProperty(z_geometrique_objet,facteur_conversion);

        nouveau_h_image_apres_conversion_a_prendre_compte = true ; // On repasse le flag à true pour qu'il soit pris en compte

        convertirObjectDoubleProperty(h_objet,facteur_conversion); // Déclenche une 2ème màj de h_image

        convertirObjectDoubleProperty(r_champ_moyen_objet,facteur_conversion);
        convertirObjectDoubleProperty(r_champ_moyen_image,facteur_conversion);
        convertirObjectDoubleProperty(r_champ_pleine_lumiere_objet,facteur_conversion);
        convertirObjectDoubleProperty(r_champ_pleine_lumiere_image,facteur_conversion);
        convertirObjectDoubleProperty(r_champ_total_objet,facteur_conversion);
        convertirObjectDoubleProperty(r_champ_total_image,facteur_conversion);

        convertirObjectDoubleProperty(z_pupille_entree,facteur_conversion);
        convertirObjectDoubleProperty(r_pupille_entree,facteur_conversion);

        convertirObjectDoubleProperty(z_pupille_sortie,facteur_conversion);
        convertirObjectDoubleProperty(r_pupille_sortie,facteur_conversion);

        convertirObjectDoubleProperty(z_lucarne_entree,facteur_conversion);
        convertirObjectDoubleProperty(r_lucarne_entree,facteur_conversion);

        convertirObjectDoubleProperty(z_lucarne_sortie,facteur_conversion);
        convertirObjectDoubleProperty(r_lucarne_sortie,facteur_conversion);
    }

    private void convertirDoubleProperty(DoubleProperty dp, double facteur_conversion) {
        dp.set(dp.get()*facteur_conversion);
    }
    private void convertirObjectDoubleProperty(ObjectProperty<Double> opd, double facteur_conversion) {
         if (opd.get()!=null) opd.set(opd.get()*facteur_conversion);
    }

    /**
     * Convertit une coordonnée Z géométrique sur l'axe géométrique (à sens unique) du SOC, en coordonnée Z optique
     * minimale.
     * @param z_geometrique
     * @return
     */
    public double convertirEnZOptique(double z_geometrique) {

        if (dioptres_rencontres==null || dioptres_rencontres.get()==null || dioptres_rencontres.isEmpty())
            return z_geometrique ;

        double z_face_entree = dioptres_rencontres.get(0).ZOptique() ; // C'est à la fois le Z entrée géométrique et optique

        if (z_geometrique<=z_face_entree)
            return z_geometrique ;

        double z_optique_resultat = z_face_entree ;

        for (int i=1 ; i <dioptres_rencontres.size() ; i++) {

            if (z_geometrique <= dioptres_rencontres.get(i).ZGeometrique())
                return z_optique_resultat + Math.abs(z_geometrique - dioptres_rencontres.get(i-1).ZGeometrique()) ;

            z_optique_resultat = dioptres_rencontres.get(i).ZOptique() ;
        }

        return z_optique_resultat + Math.abs(z_geometrique - dioptres_rencontres.get(dioptres_rencontres.size()-1).ZGeometrique()) ;
    }

        /**
         * Convertit une coordonnée Z optique (c'est-à-dire une coordonnée le long de l'axe optique, sachant que celui-ci
         * peut se replier lorsqu'il rencontre une surface réfléchissante) en Z géométrique, c'est-à-dire en distance réelle
         * (sans "repli" d'axe). Le point de coordonnée z_optique=0 est l'origine du SOC
         * @param z_optique : Z optique
         * @return Z géométrique
         */
    public double convertirEnZGeometrique(double z_optique) {

        if (dioptres_rencontres==null || dioptres_rencontres.get()==null || dioptres_rencontres.isEmpty())
            return z_optique ;

        double z_face_entree = dioptres_rencontres.get(0).ZOptique() ;

        if (z_optique<=z_face_entree)
            return z_optique ;

        double z_geom_resultat = z_face_entree ;

        for (int i=1 ; i <dioptres_rencontres.size() ; i++) {

            if (z_optique <= dioptres_rencontres.get(i).ZOptique())
                return z_geom_resultat + (dioptres_rencontres.get(i).coefficientSensIncidence()) * (z_optique - dioptres_rencontres.get(i-1).ZOptique()) ;

            z_geom_resultat += (dioptres_rencontres.get(i).coefficientSensIncidence())*(dioptres_rencontres.get(i).ZOptique() - dioptres_rencontres.get(i-1).ZOptique()) ;

        }

        return z_geom_resultat + (dioptres_rencontres.get(dioptresRencontres().size()-1).coefficientSensEmergence()) * (z_optique - dioptres_rencontres.get(dioptresRencontres().size()-1).ZOptique()) ;
    }
}
