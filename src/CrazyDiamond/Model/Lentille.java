package CrazyDiamond.Model;

import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.*;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;

import java.util.ArrayList;
import java.util.List;

public class Lentille extends BaseObstacleAvecContourEtMatiere  implements Obstacle, Identifiable, Nommable, ElementAvecContour, ElementAvecMatiere {

    private final Composition composition ;

    private final ObjectProperty<PositionEtOrientation> position_orientation ;

    protected final DoubleProperty epaisseur ;
    protected final DoubleProperty r_courbure_1 ;
    protected final BooleanProperty face_1_plane ;
    protected final DoubleProperty r_courbure_2 ;
    protected final BooleanProperty face_2_plane ;
    protected final DoubleProperty diametre ;

    private static int compteur_lentille ;
    private Cercle cercle_1;
    private Cercle cercle_2;
    private DemiPlan dp_haut;
    private DemiPlan dp_bas;
    private DemiPlan dp1;
    private DemiPlan dp2;

    public Lentille(TypeSurface type_surface, double  x_centre, double y_centre, double epaisseur, double r_courbure_1, boolean face_1_plane,
                    double r_courbure_2, boolean face_2_plane, double diametre, double orientation_deg) throws IllegalArgumentException {
        this(null,type_surface,null,x_centre,y_centre,epaisseur,r_courbure_1,face_1_plane,
                r_courbure_2,face_2_plane,diametre,orientation_deg,1.5,null,null) ;
    }


    Lentille(String nom, TypeSurface type_surface, NatureMilieu nature_milieu,
             double x_centre, double y_centre, double epaisseur, double r_courbure_1, boolean face_1_plane,
             double r_courbure_2, boolean face_2_plane, double diametre, double orientation_deg, double indice_refraction,
             Color couleur_matiere, Color couleur_contour) {

        super(nom != null ? nom :"Lentille "+(++compteur_lentille), type_surface, nature_milieu, indice_refraction, couleur_matiere, couleur_contour);

        if (epaisseur<=0d)
            throw new IllegalArgumentException("L'épaisseur d'une lentille doit être strictement positive.") ;
        if (diametre<=0d)
            throw new IllegalArgumentException("Le diamètre d'une lentille doit être strictement positif.") ;
        if (r_courbure_1==0d || r_courbure_2==0d)
            throw new IllegalArgumentException("Un rayons de courbure d'une face d'une lentille ne peut être nul.") ;
        if (face_1_plane && face_2_plane)
            throw new IllegalArgumentException("Les deux faces d'une lentille ne peuvent pas être planes.") ;
        if (/*diametre!=Double.MAX_VALUE &&*/ 0.5d*diametre>Math.min(r_courbure_1,r_courbure_2)) // À affiner : si les deux cercles de courbure se coupent, le rayon de la lentille ne peut pas être supérieur à l'ordonnée de leur intersection
            throw new IllegalArgumentException("Le rayon (demi-diamètre) de la lentille ne peut pas être supérieur au rayon de courbure d'une des faces.") ;

        this.position_orientation = new SimpleObjectProperty<>(new PositionEtOrientation(new Point2D(x_centre,y_centre),orientation_deg)) ;

        this.epaisseur = new SimpleDoubleProperty(epaisseur);
        this.r_courbure_1 = new SimpleDoubleProperty(r_courbure_1);
        this.face_1_plane = new SimpleBooleanProperty(face_1_plane);
        this.r_courbure_2 = new SimpleDoubleProperty(r_courbure_2);
        this.face_2_plane = new SimpleBooleanProperty(face_2_plane);
        this.diametre = new SimpleDoubleProperty(diametre);

        this.composition = new Composition("Composition privée", Composition.Operateur.INTERSECTION,
                type_surface,nature_milieu,indice_refraction,couleur_matiere,couleur_contour) ;

        construireComposition(x_centre, y_centre, epaisseur, r_courbure_1, r_courbure_2, diametre, orientation_deg);


    }


    public Lentille(Imp_Identifiable ii, Imp_Nommable in, Imp_ElementAvecContour iac, Imp_ElementAvecMatiere iam,
                    double x_centre, double y_centre, double epaisseur, double r_courbure_1, boolean face_1_plane,
                    double r_courbure_2, boolean face_2_plane, double diametre, double orientation_deg) {
        super(ii, in, iac, iam);

        if (epaisseur<=0d)
            throw new IllegalArgumentException("L'épaisseur d'une lentille doit être strictement positive.") ;
        if (diametre<=0d)
            throw new IllegalArgumentException("Le diamètre d'une lentille doit être strictement positif.") ;
        if (r_courbure_1==0d || r_courbure_2==0d)
            throw new IllegalArgumentException("Un rayons de courbure d'une face d'une lentille ne peut être nul.") ;
        if (face_1_plane && face_2_plane)
            throw new IllegalArgumentException("Les deux faces d'une lentille ne peuvent pas être planes.") ;
        if (diametre!=Double.MAX_VALUE && 0.5d*diametre>Math.min(r_courbure_1,r_courbure_2)) // À affiner : si les deux cercles de courbure se coupent, le rayon de la lentille ne peut pas être supérieur à l'ordonnée de leur intersection
            throw new IllegalArgumentException("Le rayon (demi-diamètre) de la lentille ne peut pas être supérieur au rayon de courbure d'une des faces.") ;

        this.position_orientation = new SimpleObjectProperty<>(new PositionEtOrientation(new Point2D(x_centre,y_centre),orientation_deg)) ;

        this.epaisseur = new SimpleDoubleProperty(epaisseur);
        this.r_courbure_1 = new SimpleDoubleProperty(r_courbure_1);
        this.face_1_plane = new SimpleBooleanProperty(face_1_plane);
        this.r_courbure_2 = new SimpleDoubleProperty(r_courbure_2);
        this.face_2_plane = new SimpleBooleanProperty(face_2_plane);
        this.diametre = new SimpleDoubleProperty(diametre);

        this.composition = new Composition("Composition privée", Composition.Operateur.INTERSECTION,
                iam.typeSurface(),iam.natureMilieu(),iam.indiceRefraction(),iam.couleurMatiere(),iac.couleurContour()) ;

        construireComposition(x_centre, y_centre, epaisseur, r_courbure_1, r_courbure_2, diametre, orientation_deg);


    }

    private void construireComposition(double x_centre, double y_centre, double epaisseur, double r_courbure_1, double r_courbure_2, double diametre, double orientation_deg) {

        // TODO tester si face1Plane et créer un demi plan si c'est le cas, idem pour face2Plane

        // NB : Comme la quasi-totalité des paramètres de ces constructions vont êtres liés (par des bindings) aux
        // propriétés de la Lentille, le calcul de paramètres exacts n'a, je pense, aucune importance. En effet, dès
        // qu'il est défini, le binding écrase la valeur de la propriété cible par celle de la propriété source.
        dp1 = new DemiPlan("Demi-plan privé face 1", -0.5*epaisseur, 0d, 180d, TypeSurface.CONVEXE);
        dp2 = new DemiPlan("Demi-plan privé face 2", 0.5*epaisseur, 0d, 0d, TypeSurface.CONVEXE);

        cercle_1 = new Cercle("Cercle privé face 1", -0.5d* epaisseur + r_courbure_1, 0, r_courbure_1, TypeSurface.CONVEXE);
        composition.ajouterObstacle(cercle_1);
        cercle_2 = new Cercle("Cercle privé face 2", 0.5d* epaisseur - r_courbure_2,0, r_courbure_1,TypeSurface.CONVEXE);
        composition.ajouterObstacle(cercle_2);

//        if (diametre!=Double.MAX_VALUE) {
// Mieux vaut avoir en permanence les deux demi-plans qui délimitent le diamètre, même s'ils sont inutiles, que d'avoir à
// les créer et à la détruire en fonction des aléas de paramétrage de la lentille. Attention aux performances cependant.
        dp_haut = new DemiPlan("Demi-plan privé limite demi-diamètre haut",
                0d, 0.5d * diametre, 90d, TypeSurface.CONVEXE);
       // composition.ajouterObstacle(dp_haut);
        dp_bas = new DemiPlan("Demi-plan privé limite demi-diamètre bas",
                0d, -0.5d * diametre, 270d, TypeSurface.CONVEXE);
       // composition.ajouterObstacle(dp_bas);
//        }

        composition.tournerAutourDe(Point2D.ZERO, orientation_deg);
        composition.translater(new Point2D(x_centre, y_centre));

        // Liaisons (bindings) entre les propriétés de la lentille et celles de sa composition privée
        composition.natureMilieuProperty().bind(this.natureMilieuProperty());
        composition.traitementSurfaceProperty().bind(this.traitementSurfaceProperty());
        composition.couleurContourProperty().bind(this.couleurContourProperty());
        composition.couleurMatiereProperty().bind(this.couleurMatiereProperty());

        // TODO : Pas aussi simple, si face1 est plane, il faut remplacer le cercle 1 dans la composition par le dp1, et idem pour la face 2
        // On peut cependant laisser tous  les bindings actifs afin que les éléments de la Composition soient prêts à être ajoutés à tout
        // moment.

        cercle_1.rayonProperty().bind(this.r_courbure_1);
        cercle_2.rayonProperty().bind(this.r_courbure_2);
        composition.indiceRefractionProperty().bind(this.indiceRefractionProperty()) ;
        composition.typeSurfaceProperty().bind(this.typeSurfaceProperty()) ;
        composition.natureMilieuProperty().bind(this.natureMilieuProperty()) ;
        composition.couleurMatiereProperty().bind(this.couleurMatiereProperty()) ;
        composition.couleurContourProperty().bind(this.couleurContourProperty()) ;

        // TODO : il faut aussi gérer le changement de TypeSurface (CONVEXE/CONCAVE) selon le signe des rayons de courbure

        ObjectBinding<Point2D> calcul_position_centre_cercle_1 = new ObjectBinding<>() {
            @Override
            protected Point2D computeValue() {

                { super.bind(epaisseurProperty(),positionEtOrientationObjectProperty(),rayonCourbure1Property()) ; }

                Point2D centre = positionEtOrientationObjectProperty().get().position();
                Point2D vecteur_dir = positionEtOrientationObjectProperty().get().direction() ;

                return centre.add(vecteur_dir.multiply(-0.5d*epaisseur()+rayonCourbure1()));
            }
        } ;
        cercle_1.centreProperty().bind(calcul_position_centre_cercle_1);

        ObjectBinding<Point2D> calcul_position_centre_cercle_2 = new ObjectBinding<>() {
            @Override
            protected Point2D computeValue() {

                { super.bind(epaisseurProperty(),positionEtOrientationObjectProperty(),rayonCourbure2Property()) ; }

                Point2D centre = positionEtOrientationObjectProperty().get().position();
                Point2D vecteur_dir = positionEtOrientationObjectProperty().get().direction() ;

                return centre.add(vecteur_dir.multiply(0.5d*epaisseur()-rayonCourbure2()));
            }
        } ;
        cercle_2.centreProperty().bind(calcul_position_centre_cercle_2);

        ObjectBinding<PositionEtOrientation> calcul_position_dp1 = new ObjectBinding<>() {
            @Override
            protected PositionEtOrientation computeValue() {

                { super.bind(epaisseurProperty(),positionEtOrientationObjectProperty()) ; }

                Point2D position = positionEtOrientationObjectProperty().get().position();
                double orientation_deg = positionEtOrientationObjectProperty().get().orientation_deg();
                Point2D vecteur_dir = positionEtOrientationObjectProperty().get().direction() ;
                return new PositionEtOrientation(position.add(vecteur_dir.multiply(-0.5d*epaisseur())) , orientation_deg+180d ) ;

            }
        } ;
        dp1.positionEtOrientationObjectProperty().bind(calcul_position_dp1);

        ObjectBinding<PositionEtOrientation> calcul_position_dp2 = new ObjectBinding<>() {
            @Override
            protected PositionEtOrientation computeValue() {

                { super.bind(epaisseurProperty(),positionEtOrientationObjectProperty()) ; }

                Point2D position = positionEtOrientationObjectProperty().get().position();
                double orientation_deg = positionEtOrientationObjectProperty().get().orientation_deg();
                Point2D vecteur_dir = positionEtOrientationObjectProperty().get().direction() ;
                return new PositionEtOrientation(position.add(vecteur_dir.multiply(0.5d*epaisseur())) , orientation_deg+0d ) ;

            }
        } ;
        dp2.positionEtOrientationObjectProperty().bind(calcul_position_dp2);

        // TODO
    }


    public ObjectProperty<PositionEtOrientation> positionEtOrientationObjectProperty() {return position_orientation ;}
    public Point2D centre() { return position_orientation.get().position() ; }
    public double xCentre() { return centre().getX() ; }
    public double yCentre() { return centre().getY() ; }

    public double epaisseur() {return epaisseur.get();}
    public DoubleProperty epaisseurProperty() {return epaisseur;}
    public double rayonCourbure1() {return r_courbure_1.get();}
    public DoubleProperty rayonCourbure1Property() {return r_courbure_1;}
    public boolean face1Plane() {return face_1_plane.get();}
    public BooleanProperty face1PlaneProperty() {return face_1_plane;}
    public double rayonCourbure2() {return r_courbure_2.get();}
    public DoubleProperty rayonCourbure2Property() {return r_courbure_2;}
    public boolean face2Plane() {return face_2_plane.get();}
    public BooleanProperty face2PlaneProperty() {return face_2_plane;}
    public double diametre() {return diametre.get();}
    public DoubleProperty diametreProperty() {return diametre;}

    public void definirCentre(Point2D centre) {position_orientation.set(new PositionEtOrientation(centre,orientation()));}
    public void definirEpaisseur(double epaisseur) {this.epaisseur.set(epaisseur);}
    public void definirRayonCourbure1(double r_courbure_1) {this.r_courbure_1.set(r_courbure_1);}
    public void definirFace1Plane(boolean face_1_plane) {this.face_1_plane.set(face_1_plane);}
    public void definirRayonCourbure2(double r_courbure_2) {this.r_courbure_2.set(r_courbure_2);}
    public void definirFace2Plane(boolean face_2_plane) {this.face_2_plane.set(face_2_plane);}

    public void definirDiametre(double diametre) {this.diametre.set(diametre);}


    @Override
    public boolean aSurSaSurface(Point2D p) {
        return composition.aSurSaSurface(p);
    }

    @Override
    public boolean contient(Point2D p) {
        return composition.contient(p);
    }

    @Override
    public Point2D normale(Point2D p) throws Exception {
        return composition.normale(p) ;
    }

    @Override
    public Double courbureRencontreeAuSommet(Point2D pt_sur_surface, Point2D direction) throws Exception {
        return composition.courbureRencontreeAuSommet(pt_sur_surface,direction) ;
    }

    @Override
    public boolean aSymetrieDeRevolution() {
        return true;
    }

    @Override
    public Point2D pointSurAxeRevolution() {
        return centre();
    }

    @Override
    public boolean estOrientable() {
        return true;
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
    public double orientation() {
        return position_orientation.get().orientation_deg() ;
    }

    @Override
    public void tournerAutourDe(Point2D centre_rot, double angle_rot_deg) {
        Rotate r = new Rotate(angle_rot_deg,centre_rot.getX(),centre_rot.getY()) ;

        Point2D nouveau_centre = r.transform(centre()) ;

        // Déclenchera le recalcul des positions et orientations des éléments de la composition
        position_orientation.set(new PositionEtOrientation(nouveau_centre,orientation()+angle_rot_deg));

    }

    @Override
    public List<DioptreParaxial> dioptresParaxiaux(PositionEtOrientation axe) {
        return composition.dioptresParaxiaux(axe);
    }

    @Override
    public Point2D premiere_intersection(Rayon r) {
        return composition.premiere_intersection(r);
    }

    @Override
    public Point2D derniere_intersection(Rayon r) {
        return composition.derniere_intersection(r);
    }

    @Override
    public Double rayonDiaphragmeParDefaut() {
        return 0.5d*diametre.get();
    }

    @Override
    public double rayonDiaphragmeMaximumConseille() {
        return 0.5d*diametre.get();
    }

    @Override
    public boolean estReflechissant() {
        return composition.estReflechissant();
    }

    @Override
    public Point2D cherche_intersection(Rayon r, ModeRecherche mode) {
        return composition.cherche_intersection(r,mode);
    }

    @Override
    public ArrayList<Point2D> cherche_toutes_intersections(Rayon r) {
        return composition.cherche_toutes_intersections(r);
    }

    @Override
    public void accepte(VisiteurEnvironnement v) {
        v.visiteLentille(this);
    }

    @Override
    public void accepte(VisiteurElementAvecMatiere v) {
        v.visiteLentille(this);
    }

    @Override
    public void ajouterRappelSurChangementToutePropriete(RappelSurChangement rap) {
        super.ajouterRappelSurChangementToutePropriete(rap);

        position_orientation.addListener((observable, oldValue, newValue) -> rap.rappel());

        epaisseur.addListener((observable, oldValue, newValue) -> rap.rappel());

        r_courbure_1.addListener((observable, oldValue, newValue) -> rap.rappel());
        face_1_plane.addListener((observable, oldValue, newValue) -> rap.rappel());

        r_courbure_2.addListener((observable, oldValue, newValue) -> rap.rappel());
        face_2_plane.addListener((observable, oldValue, newValue) -> rap.rappel());

        diametre.addListener((observable, oldValue, newValue) -> rap.rappel());
    }

    @Override
    public void ajouterRappelSurChangementTouteProprieteModifiantChemin(RappelSurChangement rap) {
        super.ajouterRappelSurChangementTouteProprieteModifiantChemin(rap);

        position_orientation.addListener((observable, oldValue, newValue) -> rap.rappel());

        epaisseur.addListener((observable, oldValue, newValue) -> rap.rappel());

        r_courbure_1.addListener((observable, oldValue, newValue) -> rap.rappel());
        face_1_plane.addListener((observable, oldValue, newValue) -> rap.rappel());

        r_courbure_2.addListener((observable, oldValue, newValue) -> rap.rappel());
        face_2_plane.addListener((observable, oldValue, newValue) -> rap.rappel());

        diametre.addListener((observable, oldValue, newValue) -> rap.rappel());
    }


    @Override
    public void retaillerPourSourisEn(Point2D pos_souris) {
        // Si on est sur le point de départ, ne rien faire
        if (pos_souris.equals(centre()))
            return ;

        epaisseur.set(2d*Math.abs(pos_souris.getX() - xCentre()));
        diametre.set(2d * Math.abs(pos_souris.getY() - yCentre()));

        r_courbure_1.set(10*epaisseur());
        r_courbure_2.set(10*epaisseur());

    }

    @Override
    public void retaillerSelectionPourSourisEn(Point2D pos_souris) {
        // Si on est sur le point de départ, ne rien faire
        if (pos_souris.equals(centre()))
            return ;

        if (!appartientASystemeOptiqueCentre()) {
            // Calculer l'écart angulaire entre le Coin HD où se trouve la poignée et la position de la souris, par rapport
            // au centre du Rectangle
            Point2D vec_centre_hd = (new Point2D(xCentre()+0.5d*epaisseur(),yCentre()+0.5d*diametre())).subtract(centre()) ;
            Point2D vec_centre_pos = pos_souris.subtract(centre()) ;
            double delta_orientation= vec_centre_pos.angle(vec_centre_hd) ;

            if (produit_vectoriel_simplifie(vec_centre_pos,vec_centre_hd)>0)
                delta_orientation = -delta_orientation ;

            double nouvelle_orientation = (orientation() + delta_orientation)%360d ;

            if (nouvelle_orientation<0)
                nouvelle_orientation += 360d ;

            definirOrientation(nouvelle_orientation);
        } else {
            // Le rectangle appartient à un SOC : on ne peut donc pas en changer l'orientation (qui doit rester celle du SOC)
            // mais on peut en changer la largeur et la hauteur
            Point2D vec_centre_pos = pos_souris.subtract(centre()) ;

            double longueur_demi_diagonale = vec_centre_pos.magnitude() ;

            double angle = Math.atan2(vec_centre_pos.getY(),vec_centre_pos.getX()) ;

            if (angle<0)
                angle+= 2*Math.PI ;

            double alpha = angle - Math.toRadians(orientation()) ;

            epaisseur.set(Math.abs(longueur_demi_diagonale*Math.cos(alpha)));
            diametre.set(Math.abs(2d*longueur_demi_diagonale*Math.sin(alpha)));
            r_courbure_1.set(10*epaisseur());
            r_courbure_2.set(10*epaisseur());
        }

    }

    private double produit_vectoriel_simplifie(Point2D v1, Point2D v2) {
        return (v1.getX()*v2.getY()-v1.getY()*v2.getX()) ;
    }

    @Override
    public Contour positions_poignees() {
        Contour c_poignees = new Contour(1);

        c_poignees.ajoutePoint(new Point2D(xCentre()+0.5d*epaisseur(),yCentre()+0.5d*diametre()));

        return c_poignees;
    }

    @Override
    public void translater(Point2D vecteur) {
        position_orientation.set(new PositionEtOrientation(centre().add(vecteur),orientation()));
    }

    @Override
    public void translaterParCommande(Point2D vecteur) {
        new CommandeDefinirUnParametrePoint<>(this,centre().add(vecteur),this::centre,this::definirCentre).executer() ;
    }

    @Override
    public void convertirDistances(double facteur_conversion) {
        position_orientation.set(new PositionEtOrientation(centre().multiply(facteur_conversion),orientation()));
        epaisseur.set(epaisseur()*facteur_conversion);
        diametre.set(diametre()*facteur_conversion);
        r_courbure_1.set(rayonCourbure1()*facteur_conversion);
        r_courbure_2.set(rayonCourbure2()*facteur_conversion);
    }

    public Composition composition() {
        return composition;
    }
}
