package CrazyDiamond.Model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class Groupe extends BaseObstacle implements Obstacle, Identifiable, Nommable {

    private final ListProperty<Obstacle> elements;

    private static int compteur_groupe = 0;

    public Groupe() throws IllegalArgumentException {

        this(
                new Imp_Identifiable(),
                new Imp_Nommable("Groupe " + (++compteur_groupe))
        ) ;

    }

    public Groupe(Imp_Identifiable ii, Imp_Nommable in) throws IllegalArgumentException {
        super(ii,in);

        ObservableList<Obstacle> olo = FXCollections.observableArrayList();
        elements = new SimpleListProperty<>(olo);

    }

    public ObservableList<Obstacle> elements() {
        return elements.get();
    }

    public boolean estVide() {
        return elements().size()==0 ;
    }


    @Override
    public Commande commandeCreation(Environnement env) {
        return new CommandeCreerGroupe(env,this) ;
    }
    @Override
    public void retaillerPourSourisEn(Point2D pos_souris) {}

    @Override
    public void translater(Point2D vecteur) {
        for (Obstacle o : elements)
            o.translater(vecteur);
    }
    @Override
    public void translaterParCommande(Point2D vecteur) {
        new CommandeTranslaterObstacles(vecteur, elements()).executer();
    }
    @Override
    public void accepte(VisiteurEnvironnement v) {
        v.visiteGroupe(this);
    }

    /**
     * Ajoute un obstacle dans Groupe.
     * NB : Les utilisateurs de cette méthode doivent veiller à retirer l'obstacle de l'environnement avant d'appeler
     * cette méthode.
     * @param o : obstacle à ajouter
     */
    public void ajouterObstacle(Obstacle o) {

        if (this.elements.contains(o))
            return;

        // TODO : il faudrait peut-être vérifier si l'obstacle appartient à l'environnement car sinon, il n'y aura pas de notification
        // des rappels en cas de modification de ses propriétés (car ces rappels sont ajoutés lors de l'ajout de l'obstacle à l'environnement)

//        o.ajouterRappelSurChangementTouteProprieteModifiantChemin( this::illuminerToutesSources); ;

        this.elements.add(o);

        o.definirAppartenanceGroupe(true);

    }

    public void retirerObstacle(Obstacle o) {
        elements.remove(o);

        // TODO : ajouter un listener sur la liste des obstacles ?

        o.definirAppartenanceGroupe(false);

    }

    public void ajouterListenerListeObstacles(ListChangeListener<Obstacle> lcl_o) {
        elements.addListener(lcl_o);

        //Il faut aussi détecter les changements qui interviennent dans les sous-groupes
        for (Obstacle o : elements) {
            if (o.getClass() == Groupe.class) {
                Groupe grp = (Groupe) o ;
                grp.ajouterListenerListeObstacles(lcl_o) ;
            }

        }
    }
    @Override
    public boolean comprend(Obstacle o) {

        for (Obstacle ob : elements) {
            if (ob.comprend(o))
                return true ;
        }

        return Obstacle.super.comprend(o);
    }

    @Override
    public Obstacle obstacle_avec_id(String obs_id) {

        for (Obstacle ob : elements) {
            Obstacle o_trouve = ob.obstacle_avec_id(obs_id) ;
            if (o_trouve!=null)
                return o_trouve ;
        }

        return Obstacle.super.obstacle_avec_id(obs_id);
    }
    public Composition composition_contenant(Obstacle o) {
        throw new IllegalCallerException("Cette méthode ne devrait pas être appelée") ;
//        for (Obstacle ob : elements) {
//            if (ob.comprend(o))
//                return ob.composition_contenant(o);
//        }
//
//        return null ;
//
    }

    @Override
    public boolean contient(Point2D p) {
        throw new IllegalCallerException("Cette méthode ne devrait pas être appelée") ;

//        for (Obstacle o : elements) {
//            if (o.contient(p))
//                return true ;
//        }
//        return false ;
    }

    @Override
    public boolean aSurSaSurface(Point2D p) {
        throw new IllegalCallerException("Cette méthode ne devrait pas être appelée") ;

//        for (Obstacle o : elements) {
//            if (o.aSurSaSurface(p))
//                return true ;
//        }
//        return false ;
    }

    protected Obstacle estSurSurfaceDe(Point2D p) throws Exception {
        throw new IllegalCallerException("Cette méthode ne devrait pas être appelée") ;
//        for (Obstacle o : elements) {
//            if (o.aSurSaSurface(p))
//                return o ;
//        }
//        return null ;

    }
    @Override
    public Point2D normale(Point2D p) throws Exception {
        throw new IllegalCallerException("Cette méthode ne devrait pas être appelée") ;

//        // Identification de l'obstacle sur la surface duquel se trouve le point
//        Obstacle obst = estSurSurfaceDe(p);
//
//        return obst.normale(p) ;
    }


    @Override
    public ArrayList<Point2D> cherche_toutes_intersections(Rayon r) {
        // Un groupe d'obstacle n'est qu'un conteneur logique sans existence physique, il n'a pas d'intersections avec
        // les rayons, ce sont les obstacles qu'il contient qui en ont.
        throw new IllegalCallerException("Cette méthode ne devrait pas être appelée") ;

//        ArrayList<Point2D> resultats = new ArrayList<>(2 * elements.size());
//
//        for (Obstacle o : elements) {
//            ArrayList<Point2D> intersections_o = o.cherche_toutes_intersections(r);
//
//            LOGGER.log(Level.FINER, "{0} intersection(s) trouvée(s) avec l'obstacle {1} de la Composition : {2} ", new Object[]{intersections_o.size(), o, intersections_o});
//
//            // On ne garde que les points qui sont sur la surface de la composition
//            for (Point2D p : intersections_o) {
//
//                if (aSurSaSurface(p)) {
//                    resultats.add(p);
//                    LOGGER.log(Level.FINER, "    L'intersection {0} est sur la surface de la Composition", p);
//                }
//            }
//
//        }
//
//        Comparator<Point2D> comparateur = (p1, p2) -> {
//
//            double distance_p1_depart = p1.subtract(r.depart()).magnitude();
//            double distance_p2_depart = p2.subtract(r.depart()).magnitude();
//
//            return Double.compare(distance_p1_depart, distance_p2_depart);
//
//        };
//
//
//        resultats.sort(comparateur);
//
//
//        return resultats;

    }

    @Override
    public Point2D cherche_intersection(Rayon r, ModeRecherche mode) {
        // Un groupe d'obstacle n'est qu'un conteneur logique sans existence physique, il n'a pas d'intersections avec
        // les rayons, ce sont les obstacles qu'il contient qui en ont.
        throw new IllegalCallerException("Cette méthode ne devrait pas être appelée") ;


//        ArrayList<Point2D> intersections = cherche_toutes_intersections(r);
//
//        LOGGER.log(Level.FINER, "{0} intersection(s) trouvée(s) avec la Composition {1} : {2}", new Object[]{intersections.size(), this, intersections});
//
//        if (intersections.size() == 0)
//            return null;
//
//        if (mode == ModeRecherche.PREMIERE)
//            return intersections.get(0);
//
//        // mode == DERNIERE
//        return intersections.get(intersections.size() - 1);

    }


    @Override
    public void ajouterRappelSurChangementToutePropriete(RappelSurChangement rap) {
        // Factorisable dans une classe abstraite BaseObstacleComposite (code commun à Composition et à Groupe)

        for (Obstacle o : elements) {
            o.ajouterRappelSurChangementToutePropriete(rap);
        }

        // Dans un groupe, il faut aussi mettre en observation la liste de ses éléments pour réagir aux ajouts
        // et aux suppressions d'éléments
        ListChangeListener<Obstacle> lcl_elements = change -> {
            while (change.next()) {

                if (change.wasRemoved()) {
                    LOGGER.log(Level.FINER, "Obstacle supprimé du groupe");
                    rap.rappel();

                } else if (change.wasAdded()) {

                    for (Obstacle additem : change.getAddedSubList()) {
                        LOGGER.log(Level.FINER, "Obstacle ajouté dans le groupe : {0}", additem);
                        rap.rappel();

                    }
                }

            }
        };
        elements.addListener(lcl_elements);

    }

    @Override
    public void ajouterRappelSurChangementTouteProprieteModifiantChemin(RappelSurChangement rap) {
        // Factorisable dans une classe abstraite BaseObstacleComposite (code commun à Composition et à Groupe)
        for (Obstacle o : elements)
            o.ajouterRappelSurChangementTouteProprieteModifiantChemin(rap);

        // TODO : Voir s'il ne faudrait pas mettre en place un ListChangeListener sur les éléments comme dans
        //  ajouterRappelSurChangementToutePropriete ci-dessus.
    }

    @Override
    public void definirOrientationAxePolariseur(double angle_pol) {
        // TODO : pour un Groupe, je pense qu'il ne faut pas que cette méthode soit utilisée (contrairement aux Compositions)
        throw new IllegalCallerException("Cette méthode ne devrait pas être appelée") ;
    }

    @Override
    public double orientationAxePolariseur() {
        // TODO : pour un Groupe, je pense qu'il ne faut pas que cette méthode soit utilisée (contrairement aux Compositions)
        throw new IllegalCallerException("Cette méthode ne devrait pas être appelée") ;
    }

    @Override
    public DoubleProperty orientationAxePolariseurProperty() {
        // TODO : pour un Groupe, je pense qu'il ne faut pas que cette méthode soit utilisée (contrairement aux Compositions)
        throw new IllegalCallerException("Cette méthode ne devrait pas être appelée") ;
    }


    @Override
    public boolean aSymetrieDeRevolution() {
        // Factorisable dans une classe abstraite BaseObstacleComposite (code commun à Composition et à Groupe)

        Obstacle o_prec = null;
        Double direction_commune = null;

        for (Obstacle o : elements) {

            // Si un des éléments de la Composition n'est pas centrable, la Composition ne l'est pas.
            // Il pourrait théoriquement l'être, par exemple si l'obstacle non centrable est exclu de la Composition
            // par le jeu des intersections ou des différences avec les autres obstacles, mais nous ne voulons pas
            // rentrer ces cas particuliers qui n'ont que peu d'intérêt : ce genre de situation n'apparait ni dans les
            // lentilles ni dans les objectifs nous semble-t-il.
            if (!o.aSymetrieDeRevolution())
                return false;

            if (o_prec != null) {

                if (o_prec.aUneOrientation() && o.aUneOrientation()) {
                    if ((o_prec.orientation() % 180d) != (o.orientation() % 180d))
                        return false;
                }

                // Cas où o_prec.pointSurAxeRevolution() et o.pointSurAxeRevolution() sont confondus : rien à controler
                if (o_prec.pointSurAxeRevolution().equals(o.pointSurAxeRevolution()))
                    continue;

                DemiDroiteOuSegment seg = DemiDroiteOuSegment.construireSegment(o_prec.pointSurAxeRevolution(), o.pointSurAxeRevolution());

                double nouvelle_direction_commune = seg.angle() % 180d;

                if (o_prec.aUneOrientation()) {
                    if (!Environnement.quasiEgal((o_prec.orientation() % 180d), nouvelle_direction_commune))
                        //                    if ((o_prec.orientation() % 180d) != (seg.angle() % 180d))
                        return false;
                } else if (o.aUneOrientation()) {
                    if (!Environnement.quasiEgal((o.orientation() % 180d), nouvelle_direction_commune))
                        //                        if ((o.orientation() % 180d) != (seg.angle() % 180d))
                        return false;
                }

                //                if (direction_commune!=null && direction_commune!=nouvelle_direction_commune)
                if (direction_commune != null && !Environnement.quasiEgal(direction_commune, nouvelle_direction_commune))
                    return false;

                direction_commune = nouvelle_direction_commune;
            }
            o_prec = o;
        }
        return true;
    }

    @Override
    public Point2D pointSurAxeRevolution() {
        // Factorisable dans une classe abstraite BaseObstacleComposite (code commun à Composition et à Groupe)
        return ((elements.size() > 0 && aSymetrieDeRevolution()) ? elements.get(0).pointSurAxeRevolution() : null);
    }

    @Override
    public boolean estOrientable() {
        // Factorisable dans une classe abstraite BaseObstacleComposite (code commun à Composition et à Groupe)
        return true;
    }

    @Override
    public void definirOrientation(double orientation_deg) {
        // Factorisable dans une classe abstraite BaseObstacleComposite (code commun à Composition et à Groupe)
        if (elements.size() == 0)
            return;

        if (!aSymetrieDeRevolution())
            return;

        // On oriente le premier élément
        double delta_ori = orientation_deg - elements.get(0).orientation();
        elements.get(0).definirOrientation(orientation_deg);

        // Les éléments suivants doivent rester à même distance du premier, rotation par rapport au "centre" (=point sur axe révolution) du premier
        Point2D centre_rot = elements.get(0).pointSurAxeRevolution();

        for (int i = 1; i < elements.size(); i++) {
            Obstacle o = elements.get(i);
            o.tournerAutourDe(centre_rot, delta_ori);
        }

    }

    @Override
    public void tournerAutourDe(Point2D centre_rot, double angle_rot_deg) {
        // Factorisable dans une classe abstraite BaseObstacleComposite (code commun à Composition et à Groupe)
        for (Obstacle o : elements)
            o.tournerAutourDe(centre_rot, angle_rot_deg);

    }

    @Override
    public double orientation() {
        // Factorisable dans une classe abstraite BaseObstacleComposite (code commun à Composition et à Groupe)
        if (elements.size() == 0) return 0d;

        Obstacle o_prec = null;

        for (Obstacle o : elements) {

            if (o_prec != null) {

                // Cas où o_prec.pointSurAxeRevolution() et o.pointSurAxeRevolution() sont confondus : rien à controler
                if (o_prec.pointSurAxeRevolution().equals(o.pointSurAxeRevolution()))
                    continue;

                DemiDroiteOuSegment seg = DemiDroiteOuSegment.construireSegment(o_prec.pointSurAxeRevolution(), o.pointSurAxeRevolution());

//                double nouvelle_direction_commune = seg.angle() ;

                return seg.angle();
            }

            o_prec = o;
        }

        // Si tous les élements avaient même centre, ou si il n'y avait qu'un élement, on retourne l'orientation du premier
        return elements.get(0).orientation();
//        return Obstacle.super.orientation();
    }

    @Override
    public Double courbureRencontreeAuSommet(Point2D pt_sur_surface, Point2D direction) throws Exception {
        // TODO : pour un Groupe, je pense qu'il ne faut pas que cette méthode soit utilisée (contrairement aux Compositions)
        throw new IllegalCallerException("Cette méthode ne devrait pas être appelée") ;

//        // Identification de l'obstacle sur la surface duquel se trouve le point
//        Obstacle obst = estSurSurfaceDe(pt_sur_surface);
//
//        return obst.courbureRencontreeAuSommet(pt_sur_surface, direction);
    }

    @Override
    public void definirAppartenanceSystemeOptiqueCentre(boolean b) {
        // Factorisable dans une classe abstraite BaseObstacleComposite (code commun à Composition et à Groupe)

        super.definirAppartenanceSystemeOptiqueCentre(b);
//        this.appartenance_systeme_optique_centre.set(b);

        // Tous les obstacles de la Composition sont concernés aussi (permet de désactiver certains contrôles de ces obstacles
        // dans les vues des panneaux, comme le contrôle de l'orientation)
        for (Obstacle o : elements)
            o.definirAppartenanceSystemeOptiqueCentre(b);
    }


    @Override
    public void definirAppartenanceComposition(boolean b) {
        // Factorisable dans une classe abstraite BaseObstacleComposite (code commun à Composition et à Groupe)

        super.definirAppartenanceComposition(b);
//        this.appartenance_composition.set(b);

        for (Obstacle o : elements)
            o.definirAppartenanceComposition(b);

    }

    @Override
    public double rayonDiaphragmeMaximumConseille() {
        // TODO : pour un Groupe, je pense qu'il ne faut pas que cette méthode soit utilisée (contrairement aux Compositions)
        throw new IllegalCallerException("Cette méthode ne devrait pas être appelée") ;

//        double res = Double.MAX_VALUE ;
//
//        for (Obstacle o : elements) {
//            if (o.rayonDiaphragmeMaximumConseille()<res)
//                res = o.rayonDiaphragmeMaximumConseille() ;
//        }
//
//        return res ;
    }

    @Override
    public List<DioptreParaxial> dioptresParaxiaux(PositionEtOrientation axe) {
        // TODO : pour un Groupe, je pense qu'il ne faut pas que cette méthode soit utilisée (contrairement aux Compositions)
        throw new IllegalCallerException("Cette méthode ne devrait pas être appelée") ;

//        ArrayList<DioptreParaxial> resultat = new ArrayList<>(2*elements.size()) ;
//        ArrayList<DioptreParaxial> dioptres_composition = new ArrayList<>(2*elements.size()) ;
//
//        // Pour les UNIONs et les INTERSECTIONs
//        int nb_obs_contenant = 0 ;
//
//        // Pour les DIFFERENCEs
//        Obstacle obs_principal = (elements.size()>0?elements.get(0):null) ;
//        int nb_obs_principal_contenant = 0 ;
//        int nb_obs_secondaire_contenant = 0 ;
//
//        // Pour les INTERSECTIONs
//        int nb_obs_avec_matiere = 0 ;
//
//        // Construction de la liste "brute" des dioptres de tous les obstacles de la composition. Comptage du nombre
//        // total d'obstacles (hors obstacles sans matière comme un cercle de rayon nul) et du nombre d'obstacles qui
//        // s'étendent sur z = -infini
//        for (int i = 0 ; i<elements.size() ; i++) {
//
//            Obstacle o = elements.get(i) ;
//
//            List<DioptreParaxial> dioptres_o = o.dioptresParaxiaux(axe);
//
//            if (dioptres_o.isEmpty()) // Pour écarter les Cercles (ou les ellipses...) de rayon (ou de paramètre) nul
//                continue;
//
//            if (dioptres_o.get(0).indiceAvant() > 0d ) {
//                ++nb_obs_contenant;
//
//                if (i==0)
//                    ++nb_obs_principal_contenant ;
//                else
//                    ++nb_obs_secondaire_contenant ;
//            }
//
//            ++nb_obs_avec_matiere; // Rappel : une Composition ne peut contenir que des obstacles avec matière (pas de segments)
//
//            dioptres_composition.addAll(dioptres_o) ;
//        }
//
//        // Tri par Z croissant et Rc "croissant"
//        dioptres_composition.sort(DioptreParaxial.comparateur) ;
//
//        for (DioptreParaxial d_c : dioptres_composition) {
//
//            // On remplace tous les indices non nuls des dioptres de la composition par l'indice de la Composition
//            if (d_c.indiceAvant()>0d)
//                d_c.indice_avant.set(indiceRefraction());
//            if (d_c.indiceApres()>0d)
//                d_c.indice_apres.set(indiceRefraction());
//
//            switch (operateur()) {
//                case UNION -> {
//
//                    if (nb_obs_contenant>0) { // On est déjà dans un obstacle
//                        if (d_c.indiceApres() > 0) { // Entrée dans un obstacle
//                            ++nb_obs_contenant;
//                        } else { // Sortie d'un obstacle
//                            --nb_obs_contenant;
//                            if (nb_obs_contenant == 0)
//                                resultat.add(d_c);
//                        }
//                    } else { // On n'est pas dans un obstacle
//                        if (d_c.indiceApres() > 0) { // Entrée dans un obstacle
//                            ++nb_obs_contenant;
//                            resultat.add(d_c) ;
//                        } else { // Sortie d'un obstacle
//                            LOGGER.log(Level.SEVERE,"Impossible de sortir d'un obstacle sans y être entré.") ;
//                        }
//
//                    }
//
//                }
//                case INTERSECTION -> {
//                    if (nb_obs_contenant==nb_obs_avec_matiere) { // On est dans la Composition car on est dans tous ses obstacles
//                        if (d_c.indiceApres()==0) { // Sortie d'un obstacle
//                            resultat.add(d_c) ;
//                            --nb_obs_contenant ;
//                        } else { // Entrée dans un obstacle
//                            LOGGER.log(Level.SEVERE,"Impossible de rentrer dans un obstacle si on est déjà dans tous.") ;
//                        }
//                    } else { // On n'est pas dans la Composition
//                        if (d_c.indiceApres() > 0) { // Entrée dans un obstacle
//                            ++nb_obs_contenant;
//                            if (nb_obs_contenant == nb_obs_avec_matiere) // Est_on maintenant dans tous les obstacles ?
//                                resultat.add(d_c) ;
//                        } else { // Sortie d'un obstacle
//                            --nb_obs_contenant ;
//                        }
//
//                    }
//                }
//                case DIFFERENCE -> {
//                    if (nb_obs_principal_contenant>0) { // On est déjà dans l'obstacle principal de la DIFFERENCE
//                        if (nb_obs_secondaire_contenant==0) { // On n'est pas encore dans un objet secondaire
//                            if (d_c.indiceApres()>0) { // Entrée dans un obstacle (forcément secondaire)
//                                d_c.permuterIndicesAvantApres(); // Cette entrée dans l'obstacle nous fait sortir de la compo : il faut permuter les indices
//                                resultat.add(d_c);
//                                ++nb_obs_secondaire_contenant ;
//                            } else { // Sortie d'un obstacle
//                                if (d_c.obstacleSurface()==obs_principal) { // Sortie de l'obstacle principal
//                                    resultat.add(d_c) ;
//                                    --nb_obs_principal_contenant ;
//                                } else { // Sortie d'un objet (forcément secondaire)
//                                    LOGGER.log(Level.SEVERE,"Impossible de sortir d'un obstacle secondaire sans y être entré.") ;
//                                }
//                            }
//                        } else { // On est déjà dans un objet secondaire
//                            if (d_c.indiceApres()>0) { // Entrée dans un obstacle (forcément secondaire)
//                                ++nb_obs_secondaire_contenant ;
//                            } else { // Sortie d'un obstacle
//                                if (d_c.obstacleSurface()==obs_principal) { // Sortie de l'obstacle principal
//                                    --nb_obs_principal_contenant ;
//                                } else { // Sortie d'un objet secondaire
//                                    --nb_obs_secondaire_contenant ;
//                                    if (nb_obs_secondaire_contenant==0) {
//                                        d_c.permuterIndicesAvantApres(); // Cette sortie dans un obstacle secondaire nous fait entrer dans la compo : il faut permuter les indices
//                                        resultat.add(d_c);
//                                    }
//                                }
//                            }
//                        }
//                    } else { // On n'est pas encore dans l'obstacle principal
//                        if (nb_obs_secondaire_contenant==0) { // On n'est pas encore dans un objet secondaire
//                            if (d_c.indiceApres()>0) { // Entrée dans un obstacle
//                                if (d_c.obstacleSurface()==obs_principal) { // Entrée dans l'obstacle principal
//                                    resultat.add(d_c) ;
//                                    ++nb_obs_principal_contenant ;
//                                } else { // Entrée dans un obstacle secondaire
//                                    ++nb_obs_secondaire_contenant ;
//                                }
//                            } else { // Sortie d'un obstacle
//                                LOGGER.log(Level.SEVERE,"Impossible de sortir d'un obstacle sans y être entré.") ;
//                            }
//                        } else { // On est déjà dans un ou plusieurs obstacles secondaires
//                            if (d_c.indiceApres()>0) { // Entrée dans un obstacle
//                                if (d_c.obstacleSurface()==obs_principal) { // Entrée dans l'obstacle principal
//                                    ++nb_obs_principal_contenant ;
//                                } else { // Entrée dans un obstacle secondaire
//                                    ++nb_obs_secondaire_contenant ;
//                                }
//                            } else { // Sortie d'un obstacle (forcément secondaire)
//                                --nb_obs_secondaire_contenant ;
//                            }
//                        }
//                    } // Fin du cas "pas encore dans l'objet principal"
//                }
//                case DIFFERENCE_SYMETRIQUE -> {
//                    if (nb_obs_contenant==0) { // On n'est pas encore dans un obstacle
//                        if (d_c.indiceApres()>0) { // On entre dans un obstacle
//                            resultat.add(d_c) ;
//                            ++nb_obs_contenant ;
//                        } else { // On sort d'un obstacle
//                            LOGGER.log(Level.SEVERE,"Impossible de sortir d'un obstacle sans y être entré.") ;
//                        }
//                    } else { // On est déjà dans un ou plusieurs obstacles
//                        if (d_c.indiceApres()>0) { // On entre dans un (autre) obstacle
//                            ++nb_obs_contenant ;
//                            if (nb_obs_contenant==2) {// Si on est maintenant dans deux obstacles, on vient de sortir de la composition -> dioptre à ajouter
//                                d_c.permuterIndicesAvantApres(); // Cette entrée dans un obstacle nous fait sortir de la compo : il faut permuter les indices
//                                resultat.add(d_c);
//                            }
//                        } else { // On sort d'un obstacle
//                            --nb_obs_contenant ;
//                            if (nb_obs_contenant<=1) {
//
//                                if (nb_obs_contenant==1) // Si on n'est plus que dans un obstacle, c'est que cette sortie
//                                    // nous a fait entrer dans la compo : il faut permuter les indices.
//                                    d_c.permuterIndicesAvantApres();
//
//                                resultat.add(d_c);
//                            }
//                        }
//                    }
//                }
//            }
//
//
//        }
//
//
//        //  On peut se retrouver avec des dioptres qui sont "confondus" (même Z, même Rc algébrique) que l'on peut
//        //  "fusionner" en mettant à jour de façon cohérente les indices avant/après (et en tenant compte de la présence
//        //  éventuelle de diaphragmes, qu'il faut alors conserver)
//        // NB : ce traitement de fusion n'est probablement pas strictement nécessaire, mais avoir des dioptres inutiles
//        // complique inutilement les calculs de la matrice de transfert optique, et de toutes les propriétés optiques du SOC
//
//        return fusionneDioptres(resultat) ;
////        return resultat ;

    }

     private List<DioptreParaxial> fusionneDioptres(List<DioptreParaxial> liste_dioptres) {
         // TODO : pour un Groupe, je pense qu'il ne faut pas que cette méthode soit utilisée (contrairement aux Compositions)
         throw new IllegalCallerException("Cette méthode ne devrait pas être appelée") ;

//        ArrayList<DioptreParaxial> resultat_fusionne = new ArrayList<>(liste_dioptres.size()) ;
//
//        DioptreParaxial d_prec = null;
//
//        for (DioptreParaxial d_courant : liste_dioptres) {
//
//            if (d_prec != null) {
//                if (d_prec.estConfonduAvec(d_courant)) {
//                    d_prec.fusionneAvecDioptreConfondu(d_courant);
//                    continue; // On saute le dioptre courant d_res, puisqu'il a été fusionné dans le précédent
//                } else {
//                    if (d_prec.estInutile())
//                        resultat_fusionne.remove(resultat_fusionne.size()-1) ;
//                }
//            }
//
//            resultat_fusionne.add(d_courant) ;
//
//            d_prec = d_courant;
//        }
//
//        int dernier_index = resultat_fusionne.size()-1 ;
//        if (dernier_index>=0 && resultat_fusionne.get(dernier_index).estInutile())
//            resultat_fusionne.remove(dernier_index) ;
//
//        return resultat_fusionne ;

    }

    @Override
    public void convertirDistances(double facteur_conversion) {
        // Factorisable dans une classe abstraite BaseObstacleComposite (code commun à Composition et à Groupe)

        for (Obstacle o : elements)
            o.convertirDistances(facteur_conversion);

    }


}