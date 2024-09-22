package CrazyDiamond.Model;

import javafx.beans.property.*;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

public class Composition extends BaseObstacleCompositeAvecContourEtMatiere implements Obstacle, Identifiable, Nommable, ElementAvecContour, ElementAvecMatiere {

    public enum Operateur {
        UNION("UNION"),
        INTERSECTION("INTERSECTION"),
        DIFFERENCE("DIFFERENCE"),
        DIFFERENCE_SYMETRIQUE("DIFFERENCE_SYMETRIQUE");
        private final String value;

        Operateur(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        public static Operateur fromValue(String text) {
            for (Operateur op : Operateur.values()) {
                if (String.valueOf(op.value).equals(text)) {
                    return op;
                }
            }
            return null;
        }

    }

    private final ObjectProperty<Operateur> operateur;

    private static int compteur_composition = 0;

    public Composition(Operateur op) throws IllegalArgumentException {
        this(null,
                op,null,null,1.0,null,null) ;
    }

    public Composition(String nom, Operateur op, TypeSurface type_surface,NatureMilieu nature_milieu, double indice_refraction, Color couleur_matiere, Color couleur_contour) throws IllegalArgumentException {
        super(nom!=null?nom:"Composition "+(++compteur_composition),
                type_surface,nature_milieu,indice_refraction,couleur_matiere,couleur_contour);

        operateur = new SimpleObjectProperty<>(op);
    }


    public Composition(Imp_Identifiable ii,Imp_Nommable ien,Imp_ElementComposite ic,Imp_ElementAvecContour iec, Imp_ElementAvecMatiere iem, Operateur op) throws IllegalArgumentException {
        super(ii,ien,ic,iec,iem) ;

        // Cette composition est le parent de tous ses éléments
        for (Obstacle o : ic.elements())
            o.definirParent(this);

        operateur = new SimpleObjectProperty<>(op);
    }

    @Override
    public Commande commandeCreation(Environnement env) {
        return new CommandeCreerComposition(env,this) ;
    }
    @Override
    public void retaillerPourSourisEn(Point2D pos_souris) {}


    @Override
    public void accepte(VisiteurEnvironnement v) {
        v.visiteComposition(this);
    }
    @Override
    public void accepte(VisiteurElementAvecMatiere v) {
        v.visiteComposition(this);
    }

    public Operateur operateur() {
        return operateur.get();
    }

    public ObjectProperty<Operateur> operateurProperty() {
        return operateur;
    }

    public void definirOperateur(Operateur op) {
        operateur.setValue(op);
    }

    /**
     * Ajoute un obstacle dans la Composition.
     * NB : Les utilisateurs de cette méthode doivent veiller à retirer l'obstacle de l'environnement avant d'appeler
     * cette méthode.
     * @param o : obstacle à ajouter, qui ne peut pas être un Groupe
     */
    public void ajouterObstacle(Obstacle o) {

        if (o instanceof Groupe)
            throw new IllegalCallerException("Un Groupe ne peut pas être ajouté dans une Composition.");

        if (this.elements().contains(o))
            return;

        // On définit l'appartenance à la composition avant de faire l'ajout, car les listeners du composite parent vont
        // se charger d'intégrer l'obstacle dans la vue (PanneauPrincipal) et de lui créer un panneau, qui n'est pas le
        // même selon que l'obstacle appartient à une composition ou non.
        o.definirAppartenanceComposition(true);

        super.ajouterObstacle(o);

        if (o instanceof ElementAvecContour eac) {
            eac.traitementSurfaceProperty().bind(traitementSurfaceProperty());
            eac.tauxReflexionSurfaceProperty().bind(tauxReflexionSurfaceProperty()) ;
            eac.orientationAxePolariseurProperty().bind(orientationAxePolariseurProperty());
        }
        if (o instanceof ElementAvecMatiere eam) {
            eam.natureMilieuProperty().bind(natureMilieuProperty());
            eam.indiceRefractionProperty().bind(indiceRefractionProperty());
            // NB : On ne fait pas de binding sur typeSurface (Convexe/Concave car c'est ue propriété "topologique"
            // intrinsèque de l'obstacle : l'inclusion de l'obstacle dans une Composition ne change rien à cette
            // topologie qu'il faut conserver pour que les calculs géométriques impliquant cet obstacle restent corrects.
        }

        // TODO : on pourrait aussi compléter le nom des obstacles avec le nom de leur composition d'appartenance

//        // TODO : il faudrait peut-être vérifier si l'obstacle appartient à l'environnement car sinon, il n'y aura pas de notification
//        // des rappels en cas de modification de ses propriétés (car ces rappels sont ajoutés lors de l'ajout de l'obstacle à l'environnement)


    }

    public void ajouterObstacleEnPosition(Obstacle o, int i_pos) {
        if (o instanceof Groupe)
            throw new IllegalCallerException("Un Groupe ne peut pas être ajouté dans une Composition.");

        if (this.elements().contains(o))
            return;

        // On définit l'appartenance à la composition avant de faire l'ajout, car les listeners du composite parent vont
        // se charger d'intégrer l'obstacle dans la vue (PanneauPrincipal) et de lui créer un panneau, qui n'est pas le
        // même selon que l'obstacle appartient à une composition ou non.
        o.definirAppartenanceComposition(true);

        super.ajouterObstacleEnPosition(o,i_pos);

        if (o instanceof ElementAvecContour eac) {
            eac.traitementSurfaceProperty().bind(traitementSurfaceProperty());
            eac.tauxReflexionSurfaceProperty().bind(tauxReflexionSurfaceProperty()) ;
            eac.orientationAxePolariseurProperty().bind(orientationAxePolariseurProperty());
        }
        if (o instanceof ElementAvecMatiere eam) {
            eam.natureMilieuProperty().bind(natureMilieuProperty());
            eam.indiceRefractionProperty().bind(indiceRefractionProperty());
            // NB : On ne fait pas de binding sur typeSurface (Convexe/Concave car c'est ue propriété "topologique"
            // intrinsèque de l'obstacle : l'inclusion de l'obstacle dans une Composition ne change rien à cette
            // topologie qu'il faut conserver pour que les calculs géométriques impliquant cet obstacle restent corrects.
        }

        // TODO : on pourrait aussi compléter le nom des obstacles avec le nom de leur composition d'appartenance

//        // TODO : il faudrait peut-être vérifier si l'obstacle appartient à l'environnement car sinon, il n'y aura pas de notification
//        // des rappels en cas de modification de ses propriétés (car ces rappels sont ajoutés lors de l'ajout de l'obstacle à l'environnement)



    }

    public void retirerObstacle(Obstacle o) {
        super.retirerObstacle(o);

        if (o instanceof ElementAvecContour eac) {
            eac.traitementSurfaceProperty().unbind();
            eac.tauxReflexionSurfaceProperty().unbind();
            eac.orientationAxePolariseurProperty().unbind();
        }
        if (o instanceof ElementAvecMatiere eam) {
            eam.natureMilieuProperty().unbind();
            eam.indiceRefractionProperty().unbind();
        }

        o.definirAppartenanceComposition(false);
    }

    public Composition composition_contenant(Obstacle o) {
        for (Obstacle ob : elements()) {
            if (ob.comprend(o)) { // Une composition ne peut pas comprendre de Groupes
                Composition c_cont = ob.composition_contenant(o);
                return (c_cont!=null?c_cont:this) ;
            }
        }
        return null ;
    }

    @Override
    public boolean contient(Point2D p) {

        boolean resultat_vrai = (typeSurface() == TypeSurface.CONVEXE);
        boolean resultat_faux = (typeSurface() != TypeSurface.CONVEXE);

        switch (operateur.get()) {
            case UNION -> {

                for (Obstacle o : elements()) {
                    if (o.contient(p)) return resultat_vrai;
                }

                return resultat_faux;

            }
            case INTERSECTION -> {

                if (estVide())
                    return resultat_faux;

                for (Obstacle o : elements()) {
                    if (!o.contient(p)) return resultat_faux;
                }

                return resultat_vrai;

            }
            case DIFFERENCE -> {

                if (estVide())
                    return resultat_faux;

                Iterator<Obstacle> ito = elements().iterator();

                Obstacle ob_principal = ito.next();

                if (!ob_principal.contient(p)) return resultat_faux;

                while (ito.hasNext()) {
                    Obstacle ob = ito.next();
                    // Rappel convention : si le point est à la surface de l'obstacle (= de la composition), il est
                    // contenu dedans
                    if (ob.contient_strict(p)) return resultat_faux;
                }

                return resultat_vrai;
            }
            case DIFFERENCE_SYMETRIQUE -> {
                if (estVide())
                    return resultat_faux;

                boolean est_dans_un_obstacle = false;

                for (Obstacle ob : elements()) {
                    if (ob.contient(p)) {
                        if (!est_dans_un_obstacle)
                            est_dans_un_obstacle = true;
                        else { // Le point est au moins dans deux obstacles : il n'est donc pas dans la différence
                            // symétrique, sauf si il est à la surface du deuxième

                            if (!ob.aSurSaSurface(p))
                                return resultat_faux; // Le point est strictement contenu dans le deuxième obstacle : on l'écarte
                        }
                    }
                }
                // Si le point n'est que dans un et un seul obstacle, il est contenu dans l'obstacle
                return (est_dans_un_obstacle ? resultat_vrai : resultat_faux);
            }

        }

        throw new IllegalStateException("Composition::contient : operateur inconnu");
    }

    @Override
    public boolean aSurSaSurface(Point2D p) {

        switch (operateur.get()) {
            case UNION -> {

                boolean est_sur_une_surface = false;
                boolean est_strictement_dans_un_obstacle = false;
                for (Obstacle o : elements()) {
                    if (!est_strictement_dans_un_obstacle && o.contient_strict(p))
                        est_strictement_dans_un_obstacle = true;
                    else if (o.aSurSaSurface(p))
                        est_sur_une_surface = true;

//                    if (o.aSurSaSurface(p))
//                        est_sur_une_surface = true ;
                }
                return (est_sur_une_surface && !est_strictement_dans_un_obstacle);
            }
            case INTERSECTION -> {
                boolean est_sur_une_surface = false;
                for (Obstacle o : elements()) {

                    // Le point doit être contenu dans tous les obstacles pour être dans leur INTERSECTION
                    if (!o.contient(p))
                        return false;

                    // Il doit aussi être au moins sur une surface
                    if (o.aSurSaSurface(p))
                        est_sur_une_surface = true;
                }
                return est_sur_une_surface;
            }
            case DIFFERENCE -> {

                if (estVide())
                    return false;

                Iterator<Obstacle> ito = elements().iterator();

                Obstacle ob_principal = ito.next(); // On sait déjà que la liste des obstacles n'est pas vide

                if (!ob_principal.contient(p)) return false;

                boolean est_sur_surface_ob_principal = ob_principal.aSurSaSurface(p);
                boolean est_sur_surface_autre_ob = false;

                while (ito.hasNext()) {
                    Obstacle ob = ito.next();

                    if (ob.contient_strict(p))
                        return false;

                    if (ob.aSurSaSurface(p))
                        est_sur_surface_autre_ob = true;
                }

                return (est_sur_surface_ob_principal || est_sur_surface_autre_ob);
            }
            case DIFFERENCE_SYMETRIQUE -> {
                boolean est_sur_une_surface = false;
                boolean est_contenu_strictement_dans_un_obstacle = false;

                for (Obstacle o : elements()) {

                    if (o.contient_strict(p)) {
                        if (est_contenu_strictement_dans_un_obstacle)
                            return false; // Le point est contenu strictement dans deux obstacles au moins : il ne peut
                            // pas être dans la DIFFERENCE_SYMETRIQUE
                        else
                            est_contenu_strictement_dans_un_obstacle = true;
                    }

                    if (o.aSurSaSurface(p))
                        est_sur_une_surface = true;

                }
                return est_sur_une_surface;
            }
        }

        throw new IllegalStateException("Composition::aSurSaSurface : operateur inconnu");
    }

    protected Obstacle estSurSurfaceDe(Point2D p) throws Exception {

        Obstacle obst = null;

        Exception ex = new IllegalStateException("Composition::estSurSurfaceDe : le point n'est pas à la surface de la Composition");

        switch (operateur.get()) {
            case UNION -> {

                boolean est_sur_une_surface = false;
                for (Obstacle o : elements()) {
                    if (est_sur_une_surface && o.contient_strict(p))
                        throw ex; // Point strictement inclus dans un des obstacles : ne peut pas être à la surface de leur UNION
                    if (o.aSurSaSurface(p)) {
                        est_sur_une_surface = true;
                        obst = o;
                    }
                }
                if (obst == null)
                    throw ex;

                return obst;
            }
            case INTERSECTION -> {
                for (Obstacle o : elements()) {
                    // Le point doit être contenu dans tous les obstacles pour être dans leur INTERSECTION
                    if (!o.contient(p))
                        throw ex;

                    // Il doit aussi être au moins sur une surface
                    if (o.aSurSaSurface(p))
                        obst = o;
                }
                if (obst == null)
                    throw ex;

                return obst;
            }
            case DIFFERENCE -> {

                if (estVide())
                    throw ex;

                Iterator<Obstacle> ito = elements().iterator();

                Obstacle ob_principal = ito.next(); // On sait déjà que la liste des obstacles n'est pas vide

                if (!ob_principal.contient(p)) throw ex;

                boolean est_sur_surface_ob_principal = ob_principal.aSurSaSurface(p);

                while (ito.hasNext()) {
                    Obstacle ob = ito.next();

                    if (ob.contient_strict(p))
                        throw ex;

                    if (ob.aSurSaSurface(p)) {
                        obst = ob;
                    }
                }

                if (est_sur_surface_ob_principal)
                    return ob_principal;

                if (obst == null)
                    throw ex;

                return obst;
            }
            case DIFFERENCE_SYMETRIQUE -> {
                boolean est_contenu_strictement_dans_un_obstacle = false;

                for (Obstacle ob : elements()) {

                    if (ob.contient_strict(p)) {
                        if (est_contenu_strictement_dans_un_obstacle)
                            throw ex; // Le point est contenu strictement dans deux obstacles au moins : il ne peut
                            // pas être dans la DIFFERENCE_SYMETRIQUE
                        else
                            est_contenu_strictement_dans_un_obstacle = true;
                    }

                    if (ob.aSurSaSurface(p)) {
                        obst = ob;
                    }

                }

                if (obst == null)
                    throw ex;

                return obst;

            }
        }

        throw new IllegalStateException("Composition::estSurSurfaceDe : le point n'est pas à la surface de la Composition");
    }

    @Override
    public Point2D normale(Point2D p) throws Exception {
        // Identification de l'obstacle sur la surface duquel se trouve le point
        Obstacle obst = estSurSurfaceDe(p);

        double coeff_renversement = (typeSurface() == TypeSurface.CONVEXE ? 1.0 : -1.0);

        switch (operateur.get()) {
            case UNION, INTERSECTION -> {
                return obst.normale(p).multiply(coeff_renversement);
            }
            case DIFFERENCE -> {
                if (/* obst== elements.get(0) &&*/ elements().indexOf(obst) == 0)
                    return obst.normale(p).multiply(coeff_renversement);

                return obst.normale(p).multiply(-1.0).multiply(coeff_renversement);
            }
            case DIFFERENCE_SYMETRIQUE -> {

                for (Obstacle ob : elements()) {
                    if (ob.contient_strict(p))
                        return obst.normale(p).multiply(-1.0).multiply(coeff_renversement);
                }

                return obst.normale(p).multiply(coeff_renversement);
            }
        }

        throw new Exception("Impossible de trouver la normale d'un point qui n'est pas sur la surface de la Composition.");

    }

    @Override
    public ArrayList<Point2D> cherche_toutes_intersections(Rayon r) {

        ArrayList<Point2D> resultats = new ArrayList<>(2 * elements().size());

        for (Obstacle o : elements()) {
            ArrayList<Point2D> intersections_o = o.cherche_toutes_intersections(r);

            LOGGER.log(Level.FINER, "{0} intersection(s) trouvée(s) avec l'obstacle {1} de la Composition : {2} ", new Object[]{intersections_o.size(), o, intersections_o});

            // On ne garde que les points qui sont sur la surface de la composition
            for (Point2D p : intersections_o) {

                if (aSurSaSurface(p)) {
                    resultats.add(p);
                    LOGGER.log(Level.FINER, "    L'intersection {0} est sur la surface de la Composition", p);
                }
            }

        }

        Comparator<Point2D> comparateur = (p1, p2) -> {

            double distance_p1_depart = p1.subtract(r.depart()).magnitude();
            double distance_p2_depart = p2.subtract(r.depart()).magnitude();

            return Double.compare(distance_p1_depart, distance_p2_depart);

        };

        resultats.sort(comparateur);

        return resultats;
    }

    @Override
    public Point2D cherche_intersection(Rayon r, ModeRecherche mode) {

        ArrayList<Point2D> intersections = cherche_toutes_intersections(r);

        LOGGER.log(Level.FINER, "{0} intersection(s) trouvée(s) avec la Composition {1} : {2}", new Object[]{intersections.size(), this, intersections});

        if (intersections.size() == 0)
            return null;

        if (mode == ModeRecherche.PREMIERE)
            return intersections.get(0);

        // mode == DERNIERE
        return intersections.get(intersections.size() - 1);

    }


    @Override
    public void ajouterRappelSurChangementToutePropriete(RappelSurChangement rap) {
        super.ajouterRappelSurChangementToutePropriete(rap);

        operateur.addListener((observable, oldValue, newValue) -> rap.rappel());
    }

    @Override
    public void ajouterRappelSurChangementTouteProprieteModifiantChemin(RappelSurChangement rap) {

        super.ajouterRappelSurChangementTouteProprieteModifiantChemin(rap);

        operateur.addListener((observable, oldValue, newValue) -> rap.rappel());
    }



    @Override
    public Double courbureRencontreeAuSommet(Point2D pt_sur_surface, Point2D direction) throws Exception {
        // Identification de l'obstacle sur la surface duquel se trouve le point
        Obstacle obst = estSurSurfaceDe(pt_sur_surface);

//        return (direction.dotProduct(normale(pt_sur_surface))<=0d?
//                obst.courbureRencontreeAuSommet(pt_sur_surface,direction):-obst.courbureRencontreeAuSommet(pt_sur_surface,direction)) ;

        return obst.courbureRencontreeAuSommet(pt_sur_surface, direction);
    }

    @Override
    public double rayonDiaphragmeMaximumConseille() {

        double res = Double.MAX_VALUE ;

        for (Obstacle o : elements()) {
            if (o.rayonDiaphragmeMaximumConseille()<res)
                res = o.rayonDiaphragmeMaximumConseille() ;
        }

        return res ;
    }

    @Override
    public List<DioptreParaxial> dioptresParaxiaux(PositionEtOrientation axe) {

        int nb_obs = nombreObstaclesPremierNiveau() ;
        ArrayList<DioptreParaxial> resultat = new ArrayList<>(2*nb_obs) ;
        ArrayList<DioptreParaxial> dioptres_composition = new ArrayList<>(2*nb_obs) ;

        // Pour les UNIONs et les INTERSECTIONs
        int nb_obs_contenant = 0 ;

        // Pour les DIFFERENCEs
        Obstacle obs_principal = ((!estVide())?obstacle(0) :null) ;
        int nb_obs_principal_contenant = 0 ;
        int nb_obs_secondaire_contenant = 0 ;

        // Pour les INTERSECTIONs
        int nb_obs_avec_matiere = 0 ;

        // Construction de la liste "brute" des dioptres de tous les obstacles de la composition. Comptage du nombre
        // total d'obstacles (hors obstacles sans matière comme un cercle de rayon nul) et du nombre d'obstacles qui
        // s'étendent sur z = -infini
        for (int i = 0 ; i<nb_obs ; i++) {

            Obstacle o = obstacle(i) ;

            List<DioptreParaxial> dioptres_o = o.dioptresParaxiaux(axe);

            if (dioptres_o.isEmpty()) // Pour écarter les Cercles (ou les ellipses...) de rayon (ou de paramètre) nul
                continue;

            if (dioptres_o.get(0).indiceAvant() > 0d ) {
                ++nb_obs_contenant;

                if (i==0)
                    ++nb_obs_principal_contenant ;
                else
                    ++nb_obs_secondaire_contenant ;
            }

            ++nb_obs_avec_matiere; // Rappel : une Composition ne peut contenir que des stream_obstacles avec matière (pas de segments)

            dioptres_composition.addAll(dioptres_o) ;
        }

        // Tri par Z croissant et Rc "croissant"
        dioptres_composition.sort(DioptreParaxial.comparateur) ;

        for (DioptreParaxial d_c : dioptres_composition) {

            // On remplace tous les indices non nuls des dioptres de la composition par l'indice de la Composition
            if (d_c.indiceAvant()>0d)
                d_c.indice_avant.set(indiceRefraction());
            if (d_c.indiceApres()>0d)
                d_c.indice_apres.set(indiceRefraction());

            switch (operateur()) {
                case UNION -> {

                    if (nb_obs_contenant>0) { // On est déjà dans un obstacle
                        if (d_c.indiceApres() > 0) { // Entrée dans un obstacle
                            ++nb_obs_contenant;
                        } else { // Sortie d'un obstacle
                            --nb_obs_contenant;
                            if (nb_obs_contenant == 0)
                                resultat.add(d_c);
                        }
                    } else { // On n'est pas dans un obstacle
                        if (d_c.indiceApres() > 0) { // Entrée dans un obstacle
                            ++nb_obs_contenant;
                            resultat.add(d_c) ;
                        } else { // Sortie d'un obstacle
                            LOGGER.log(Level.SEVERE,"Impossible de sortir d'un obstacle sans y être entré.") ;
                        }

                    }

                }
                case INTERSECTION -> {
                    if (nb_obs_contenant==nb_obs_avec_matiere) { // On est dans la Composition car on est dans tous ses obstacles
                        if (d_c.indiceApres()==0) { // Sortie d'un obstacle
                            resultat.add(d_c) ;
                            --nb_obs_contenant ;
                        } else { // Entrée dans un obstacle
                            LOGGER.log(Level.SEVERE,"Impossible de rentrer dans un obstacle si on est déjà dans tous.") ;
                        }
                    } else { // On n'est pas dans la Composition
                        if (d_c.indiceApres() > 0) { // Entrée dans un obstacle
                            ++nb_obs_contenant;
                            if (nb_obs_contenant == nb_obs_avec_matiere) // Est_on maintenant dans tous les obstacles ?
                                resultat.add(d_c) ;
                        } else { // Sortie d'un obstacle
                            --nb_obs_contenant ;
                        }

                    }
                }
                case DIFFERENCE -> {
                    if (nb_obs_principal_contenant>0) { // On est déjà dans l'obstacle principal de la DIFFERENCE
                        if (nb_obs_secondaire_contenant==0) { // On n'est pas encore dans un objet secondaire
                            if (d_c.indiceApres()>0) { // Entrée dans un obstacle (forcément secondaire)
                                d_c.permuterIndicesAvantApres(); // Cette entrée dans l'obstacle nous fait sortir de la compo : il faut permuter les indices
                                resultat.add(d_c);
                                ++nb_obs_secondaire_contenant ;
                            } else { // Sortie d'un obstacle
                                if (d_c.obstacleSurface()==obs_principal) { // Sortie de l'obstacle principal
                                    resultat.add(d_c) ;
                                    --nb_obs_principal_contenant ;
                                } else { // Sortie d'un objet (forcément secondaire)
                                    LOGGER.log(Level.SEVERE,"Impossible de sortir d'un obstacle secondaire sans y être entré.") ;
                                }
                            }
                        } else { // On est déjà dans un objet secondaire
                            if (d_c.indiceApres()>0) { // Entrée dans un obstacle (forcément secondaire)
                                ++nb_obs_secondaire_contenant ;
                            } else { // Sortie d'un obstacle
                                if (d_c.obstacleSurface()==obs_principal) { // Sortie de l'obstacle principal
                                    --nb_obs_principal_contenant ;
                                } else { // Sortie d'un objet secondaire
                                    --nb_obs_secondaire_contenant ;
                                    if (nb_obs_secondaire_contenant==0) {
                                        d_c.permuterIndicesAvantApres(); // Cette sortie dans un obstacle secondaire nous fait entrer dans la compo : il faut permuter les indices
                                        resultat.add(d_c);
                                    }
                                }
                            }
                        }
                    } else { // On n'est pas encore dans l'obstacle principal
                        if (nb_obs_secondaire_contenant==0) { // On n'est pas encore dans un objet secondaire
                            if (d_c.indiceApres()>0) { // Entrée dans un obstacle
                                if (d_c.obstacleSurface()==obs_principal) { // Entrée dans l'obstacle principal
                                    resultat.add(d_c) ;
                                    ++nb_obs_principal_contenant ;
                                } else { // Entrée dans un obstacle secondaire
                                    ++nb_obs_secondaire_contenant ;
                                }
                            } else { // Sortie d'un obstacle
                                LOGGER.log(Level.SEVERE,"Impossible de sortir d'un obstacle sans y être entré.") ;
                            }
                        } else { // On est déjà dans un ou plusieurs obstacles secondaires
                            if (d_c.indiceApres()>0) { // Entrée dans un obstacle
                                if (d_c.obstacleSurface()==obs_principal) { // Entrée dans l'obstacle principal
                                    ++nb_obs_principal_contenant ;
                                } else { // Entrée dans un obstacle secondaire
                                    ++nb_obs_secondaire_contenant ;
                                }
                            } else { // Sortie d'un obstacle (forcément secondaire)
                                --nb_obs_secondaire_contenant ;
                            }
                        }
                    } // Fin du cas "pas encore dans l'objet principal"
                }
                case DIFFERENCE_SYMETRIQUE -> {
                    if (nb_obs_contenant==0) { // On n'est pas encore dans un obstacle
                        if (d_c.indiceApres()>0) { // On entre dans un obstacle
                            resultat.add(d_c) ;
                            ++nb_obs_contenant ;
                        } else { // On sort d'un obstacle
                            LOGGER.log(Level.SEVERE,"Impossible de sortir d'un obstacle sans y être entré.") ;
                        }
                    } else { // On est déjà dans un ou plusieurs obstacles
                        if (d_c.indiceApres()>0) { // On entre dans un (autre) obstacle
                            ++nb_obs_contenant ;
                            if (nb_obs_contenant==2) {// Si on est maintenant dans deux obstacles, on vient de sortir de la composition -> dioptre à ajouter
                                d_c.permuterIndicesAvantApres(); // Cette entrée dans un obstacle nous fait sortir de la compo : il faut permuter les indices
                                resultat.add(d_c);
                            }
                        } else { // On sort d'un obstacle
                            --nb_obs_contenant ;
                            if (nb_obs_contenant<=1) {

                                if (nb_obs_contenant==1) // Si on n'est plus que dans un obstacle, c'est que cette sortie
                                    // nous a fait entrer dans la compo : il faut permuter les indices.
                                    d_c.permuterIndicesAvantApres();

                                resultat.add(d_c);
                            }
                        }
                    }
                }
            }
        }

        //  On peut se retrouver avec des dioptres qui sont "confondus" (même Z, même Rc algébrique) que l'on peut
        //  "fusionner" en mettant à jour de façon cohérente les indices avant/après (et en tenant compte de la présence
        //  éventuelle de diaphragmes, qu'il faut alors conserver)
        // NB : ce traitement de fusion n'est probablement pas strictement nécessaire, mais avoir des dioptres inutiles
        // complique inutilement les calculs de la matrice de transfert optique, et de toutes les propriétés optiques du SOC

        return fusionneDioptres(resultat) ;
    }

     private List<DioptreParaxial> fusionneDioptres(List<DioptreParaxial> liste_dioptres) {

        ArrayList<DioptreParaxial> resultat_fusionne = new ArrayList<>(liste_dioptres.size()) ;

        DioptreParaxial d_prec = null;

        for (DioptreParaxial d_courant : liste_dioptres) {

            if (d_prec != null) {
                if (d_prec.estConfonduAvec(d_courant)) {
                    d_prec.fusionneAvecDioptreConfondu(d_courant);
                    continue; // On saute le dioptre courant d_res, puisqu'il a été fusionné dans le précédent
                } else {
                    if (d_prec.estInutile())
                        resultat_fusionne.remove(resultat_fusionne.size()-1) ;
                }
            }

            resultat_fusionne.add(d_courant) ;

            d_prec = d_courant;
        }

        int dernier_index = resultat_fusionne.size()-1 ;
        if (dernier_index>=0 && resultat_fusionne.get(dernier_index).estInutile())
            resultat_fusionne.remove(dernier_index) ;

        return resultat_fusionne ;

    }

}