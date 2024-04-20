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


    private final ObjectProperty<FormeFaceLentille> forme_face_1;    
    protected final DoubleProperty rayon_1;

    protected final DoubleProperty parametre_1;
    protected final DoubleProperty excentricite_1;

    private final ObjectProperty<ConvexiteFaceLentille> convexite_face_1;


    private final ObjectProperty<FormeFaceLentille> forme_face_2;
    protected final DoubleProperty rayon_2;
    
    protected final DoubleProperty parametre_2;
    protected final DoubleProperty excentricite_2;
    
    private final ObjectProperty<ConvexiteFaceLentille> convexite_face_2;

    protected final DoubleProperty diametre ;

    private static int compteur_lentille ;
    private Cercle cercle_1;
    private Cercle cercle_2;
    
    private Conique conique_1;
    private Conique conique_2;
    
    private DemiPlan dp_haut;
    private DemiPlan dp_bas;
    private DemiPlan dp_face_1_plane;
    private DemiPlan dp_limite_face_1_concave;
    private DemiPlan dp_face_2_plane;
    private DemiPlan dp_limite_face_2_concave;

    // Constructeur simplifié pour lentille à faces sphériques
    public Lentille(TypeSurface type_surface, double  x_centre, double y_centre, double epaisseur, 
                    double rayon_1, ConvexiteFaceLentille convexite_face_1,
                    double rayon_2, ConvexiteFaceLentille convexite_face_2, 
                    double diametre, double orientation_deg) throws IllegalArgumentException {
        this(null,type_surface,null,x_centre,y_centre,epaisseur,
                FormeFaceLentille.SPHERIQUE , rayon_1, rayon_1, 0.0, convexite_face_1,
                FormeFaceLentille.SPHERIQUE , rayon_2, rayon_2, 0.0, convexite_face_2,
                diametre,orientation_deg,1.5,null,null) ;
    }


    Lentille(String nom, TypeSurface type_surface, NatureMilieu nature_milieu,
             double x_centre, double y_centre, double epaisseur, 
             FormeFaceLentille forme_1, double rayon_1, double parametre_1, double excentricite_1, ConvexiteFaceLentille convexite_face_1,
             FormeFaceLentille forme_2, double rayon_2, double parametre_2, double excentricite_2, ConvexiteFaceLentille convexite_face_2,
             double diametre, double orientation_deg, double indice_refraction,
             Color couleur_matiere, Color couleur_contour) {

        super(nom != null ? nom :"Lentille "+(++compteur_lentille), type_surface, nature_milieu, indice_refraction,
                couleur_matiere, couleur_contour);

        if (epaisseur<=0d)
            throw new IllegalArgumentException("L'épaisseur d'une lentille doit être strictement positive.") ;
        if (diametre<=0d)
            throw new IllegalArgumentException("Le diamètre d'une lentille doit être strictement positif.") ;
        if ( (rayon_1 ==0d && convexite_face_1 != ConvexiteFaceLentille.PLANE) || (rayon_2 ==0d && convexite_face_2 != ConvexiteFaceLentille.PLANE) )
            throw new IllegalArgumentException("Le rayon de courbure d'une face non plane d'une lentille ne peut être nul.") ;
        if ( (parametre_1 ==0d && convexite_face_1 != ConvexiteFaceLentille.PLANE) || (parametre_2==0d && convexite_face_2 != ConvexiteFaceLentille.PLANE) )
            throw new IllegalArgumentException("Le paramètre d'une face conique non plane d'une lentille ne peut être nul.") ;
        if (rayon_1 <0d || rayon_2 <0d)
            throw new IllegalArgumentException("Le rayon d'une face d'une lentille sphérique ne peut être négatif.") ;
        if (parametre_1 <0d || parametre_2 <0d)
            throw new IllegalArgumentException("Le paramètre d'une face de lentille conique ne peut être négatif.") ;
        if (excentricite_1 <0d || excentricite_2 <0d)
            throw new IllegalArgumentException("L'excentricité d'une face de lentille conique ne peut être négatif.") ;
        if (convexite_face_1 == ConvexiteFaceLentille.PLANE && convexite_face_2 == ConvexiteFaceLentille.PLANE)
            throw new IllegalArgumentException("Les deux faces d'une lentille ne peuvent pas être planes.") ;
        if (/*diametre!=Double.MAX_VALUE &&*/ 0.5d*diametre>Math.min(rayon_1, rayon_2)) // À affiner : si les deux cercles de courbure se coupent, le rayon de la lentille ne peut pas être supérieur à l'ordonnée de leur intersection
            throw new IllegalArgumentException("Le rayon (demi-diamètre) de la lentille ne peut pas être supérieur au rayon de courbure d'une des faces.") ;

        this.position_orientation = new SimpleObjectProperty<>(new PositionEtOrientation(new Point2D(x_centre,y_centre),orientation_deg)) ;

        this.epaisseur = new SimpleDoubleProperty(epaisseur);
        
        this.forme_face_1 = new SimpleObjectProperty<>(forme_1) ;
        this.rayon_1 = new SimpleDoubleProperty(rayon_1);
        this.parametre_1 = new SimpleDoubleProperty(parametre_1);
        this.excentricite_1 = new SimpleDoubleProperty(excentricite_1);
        this.convexite_face_1 = new SimpleObjectProperty<>(convexite_face_1) ;

        this.forme_face_2 = new SimpleObjectProperty<>(forme_2) ;
        this.rayon_2 = new SimpleDoubleProperty(rayon_2);
        this.parametre_2 = new SimpleDoubleProperty(parametre_2);
        this.excentricite_2 = new SimpleDoubleProperty(excentricite_2);
        this.convexite_face_2 = new SimpleObjectProperty<>(convexite_face_2) ;
        
        this.diametre = new SimpleDoubleProperty(diametre);

        this.composition = new Composition("Composition privée", Composition.Operateur.INTERSECTION,
                type_surface,nature_milieu,indice_refraction,couleur_matiere,couleur_contour) ;

        construireComposition(x_centre, y_centre, epaisseur, 
                forme_1, rayon_1, parametre_1, excentricite_1, convexite_face_1, 
                forme_2, rayon_2, parametre_2, excentricite_2, convexite_face_2,
                diametre, orientation_deg);


    }


    public Lentille(Imp_Identifiable ii, Imp_Nommable in, Imp_ElementAvecContour iac, Imp_ElementAvecMatiere iam,
                    double x_centre, double y_centre, double epaisseur,
                    FormeFaceLentille forme_1, double rayon_1, double parametre_1, double excentricite_1,ConvexiteFaceLentille convexite_face_1,
                    FormeFaceLentille forme_2, double rayon_2, double parametre_2, double excentricite_2,ConvexiteFaceLentille convexite_face_2, 
                    double diametre, double orientation_deg) 
    {
        super(ii, in, iac, iam);

        if (epaisseur<=0d)
            throw new IllegalArgumentException("L'épaisseur d'une lentille doit être strictement positive.") ;
        if (diametre<=0d)
            throw new IllegalArgumentException("Le diamètre d'une lentille doit être strictement positif.") ;
        if (rayon_1 <0d || rayon_2 <0d)
            throw new IllegalArgumentException("Le rayon de courbure d'une face d'une lentille ne peut être négatif.") ;
        if (parametre_1 <0d || parametre_2 <0d)
            throw new IllegalArgumentException("Le paramètre d'une face de lentille conique ne peut être négatif.") ;
        if (excentricite_1 <0d || excentricite_2 <0d)
            throw new IllegalArgumentException("L'excentricité d'une face de lentille conique ne peut être négatif.") ;
        if (convexite_face_1 == ConvexiteFaceLentille.PLANE && convexite_face_2 == ConvexiteFaceLentille.PLANE)
            throw new IllegalArgumentException("Les deux faces d'une lentille ne peuvent pas être planes.") ;
        if (diametre!=Double.MAX_VALUE && 0.5d*diametre>Math.min(rayon_1, rayon_2)) // À affiner : si les deux cercles de courbure se coupent, le rayon de la lentille ne peut pas être supérieur à l'ordonnée de leur intersection
            throw new IllegalArgumentException("Le rayon (demi-diamètre) de la lentille ne peut pas être supérieur au rayon de courbure d'une des faces.") ;

        this.position_orientation = new SimpleObjectProperty<>(new PositionEtOrientation(new Point2D(x_centre,y_centre),orientation_deg)) ;

        this.epaisseur = new SimpleDoubleProperty(epaisseur);

        this.forme_face_1 = new SimpleObjectProperty<>(forme_1) ;
        this.rayon_1 = new SimpleDoubleProperty(rayon_1);
        this.parametre_1 = new SimpleDoubleProperty(parametre_1);
        this.excentricite_1 = new SimpleDoubleProperty(excentricite_1);
        this.convexite_face_1 = new SimpleObjectProperty<>(convexite_face_1) ;

        this.forme_face_2 = new SimpleObjectProperty<>(forme_2) ;
        this.rayon_2 = new SimpleDoubleProperty(rayon_2);
        this.parametre_2 = new SimpleDoubleProperty(parametre_2);
        this.excentricite_2 = new SimpleDoubleProperty(excentricite_2);
        this.convexite_face_2 = new SimpleObjectProperty<>(convexite_face_2) ;
        
        this.diametre = new SimpleDoubleProperty(diametre);

        this.composition = new Composition("Composition privée", Composition.Operateur.INTERSECTION,
                iam.typeSurface(),iam.natureMilieu(),iam.indiceRefraction(),iam.couleurMatiere(),iac.couleurContour()) ;

        construireComposition(x_centre, y_centre, epaisseur,
                forme_1, rayon_1, parametre_1, excentricite_1, convexite_face_1,
                forme_2, rayon_2, parametre_2, excentricite_2, convexite_face_2,
                diametre, orientation_deg);


    }

    private void construireComposition(double x_centre, double y_centre, double epaisseur, 
                                       FormeFaceLentille forme_1, double rayon_1, double parametre_1, double excentricite_1, ConvexiteFaceLentille convexite_face_1,
                                       FormeFaceLentille forme_2, double rayon_2, double parametre_2, double excentricite_2, ConvexiteFaceLentille convexite_face_2,
                                       double diametre, double orientation_deg) {

        // TODO tester si convexiteFace1 est PLANE et créer un demi plan si c'est le cas, idem pour convexiteFace2

        // NB : Comme la quasi-totalité des paramètres de ces constructions vont êtres liés (par des bindings) aux
        // propriétés de la Lentille, le calcul de paramètres exacts n'a, je pense, aucune importance. En effet, dès
        // qu'il est défini, le binding écrase la valeur de la propriété cible par celle de la propriété source.
        dp_face_1_plane = new DemiPlan("Demi-plan privé face 1", -0.5*epaisseur, 0d, 180d, TypeSurface.CONVEXE);
        dp_face_2_plane = new DemiPlan("Demi-plan privé face 2", 0.5*epaisseur, 0d, 0d, TypeSurface.CONVEXE);


        cercle_1 = new Cercle("Cercle privé face 1",
                (convexite_face_1== ConvexiteFaceLentille.CONVEXE?(-0.5d*epaisseur + rayon_1):(-0.5d* epaisseur - rayon_1)), 0, rayon_1,
                (convexite_face_1== ConvexiteFaceLentille.CONVEXE?TypeSurface.CONVEXE:TypeSurface.CONCAVE));

        cercle_2 = new Cercle("Cercle privé face 2",
                (convexite_face_2== ConvexiteFaceLentille.CONVEXE?(0.5d*epaisseur - rayon_2):(0.5d* epaisseur + rayon_2)),0, rayon_1,
                (convexite_face_2== ConvexiteFaceLentille.CONVEXE?TypeSurface.CONVEXE:TypeSurface.CONCAVE));

        conique_1 = new Conique("Conique privée face 1",
                (convexite_face_1==ConvexiteFaceLentille.CONVEXE?(-0.5d*epaisseur + parametre_1/(1+excentricite_1)):(-0.5d* epaisseur - parametre_1/(1+excentricite_1))), 0,
                (convexite_face_1==ConvexiteFaceLentille.CONVEXE?180d:0d),parametre_1,excentricite_1,
                (convexite_face_1==ConvexiteFaceLentille.CONVEXE?TypeSurface.CONVEXE:TypeSurface.CONCAVE));

        conique_2 = new Conique("Conique privée face 2",
                (convexite_face_2== ConvexiteFaceLentille.CONVEXE?(0.5d*epaisseur - parametre_2/(1+excentricite_2)):(0.5d* epaisseur + parametre_2/(1+excentricite_2))), 0,
                (convexite_face_1==ConvexiteFaceLentille.CONVEXE?0d:180d),parametre_2,excentricite_2,
                (convexite_face_2== ConvexiteFaceLentille.CONVEXE?TypeSurface.CONVEXE:TypeSurface.CONCAVE));


        dp_limite_face_1_concave = new DemiPlan("Demi-plan privé limite face 1 concave", -0.5*epaisseur-rayon_1, 0d, 180d, TypeSurface.CONVEXE);
        dp_limite_face_2_concave = new DemiPlan("Demi-plan privé limite face 2 concave", 0.5*epaisseur+rayon_2, 0d, 0d, TypeSurface.CONVEXE);


        switch (convexite_face_1) {
            case CONVEXE,CONCAVE -> composition.ajouterObstacle(forme_1==FormeFaceLentille.SPHERIQUE?cercle_1:conique_1) ;
            case PLANE -> composition.ajouterObstacle(dp_face_1_plane) ;
        }
//        if (convexite_face_1== ConvexiteFaceLentille.PLANE)
//            composition.ajouterObstacle(dp_face_1_plane);
//        else
//            composition.ajouterObstacle(cercle_1);
        switch (convexite_face_2) {
            case CONVEXE,CONCAVE -> composition.ajouterObstacle(forme_2==FormeFaceLentille.SPHERIQUE?cercle_2:conique_2) ;
            case PLANE -> composition.ajouterObstacle(dp_face_2_plane) ;
        }
//        if (convexite_face_2== ConvexiteFaceLentille.PLANE)
//            composition.ajouterObstacle(dp_face_2_plane);
//        else
//            composition.ajouterObstacle(cercle_2);

        if (convexite_face_1== ConvexiteFaceLentille.CONCAVE)
            composition.ajouterObstacle(dp_limite_face_1_concave);
        if (convexite_face_2== ConvexiteFaceLentille.CONCAVE)
            composition.ajouterObstacle(dp_limite_face_2_concave);

        // Mieux vaut avoir en permanence les deux demi-plans qui délimitent le diamètre, même s'ils sont inutiles, que d'avoir à
        // les créer et à la détruire en fonction des aléas de paramétrage de la lentille. Attention aux performances cependant.
        dp_haut = new DemiPlan("Demi-plan privé limite demi-diamètre haut",
                0d, 0.5d * diametre, 90d, TypeSurface.CONVEXE);
        dp_bas = new DemiPlan("Demi-plan privé limite demi-diamètre bas",
                0d, -0.5d * diametre, 270d, TypeSurface.CONVEXE);

        // Les demi-plans haut bas sont présents en permanence dans la composition même s'ils ne servent à rien (trop
        // complexe de calculer en permanence et dans tous les cas possibles si ces demi-plans sont nécessaires=
//        if (0.5d*diametre<Math.max(rayon_1,rayon_2)) {
//        composition.ajouterObstacle(dp_haut);
//        composition.ajouterObstacle(dp_bas);
//        }

        // L'ajout de ces deux plans casse (du point de vue du programme) la symétrie de révolution et empêche une extraction correcte des dioptres paraxiaux
//        composition.ajouterObstacle(dp_haut);
//        composition.ajouterObstacle(dp_bas);

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

        cercle_1.rayonProperty().bind(this.rayon_1);
        conique_1.parametreProperty().bind(this.parametre_1) ;
        conique_1.excentriciteProperty().bind(this.excentricite_1) ;
        cercle_2.rayonProperty().bind(this.rayon_2);
        conique_2.parametreProperty().bind(this.parametre_2) ;
        conique_2.excentriciteProperty().bind(this.excentricite_2) ;
        composition.indiceRefractionProperty().bind(this.indiceRefractionProperty()) ;
        composition.typeSurfaceProperty().bind(this.typeSurfaceProperty()) ;
        composition.natureMilieuProperty().bind(this.natureMilieuProperty()) ;
        composition.couleurMatiereProperty().bind(this.couleurMatiereProperty()) ;
        composition.couleurContourProperty().bind(this.couleurContourProperty()) ;

        ObjectBinding<TypeSurface> calcul_convexite_cercle_ou_conique_1 = new ObjectBinding<>() {
            @Override
            protected TypeSurface computeValue() {
                { super.bind(convexiteFace1Property()) ; }
                return (convexiteFace1()==ConvexiteFaceLentille.CONCAVE?TypeSurface.CONCAVE:TypeSurface.CONVEXE) ;
            }
        } ;
        cercle_1.typeSurfaceProperty().bind(calcul_convexite_cercle_ou_conique_1);
        conique_1.typeSurfaceProperty().bind(calcul_convexite_cercle_ou_conique_1);

        ObjectBinding<TypeSurface> calcul_convexite_cercle_ou_conique_2 = new ObjectBinding<>() {
            @Override
            protected TypeSurface computeValue() {
                { super.bind(convexiteFace2Property()) ; }
                return (convexiteFace2()==ConvexiteFaceLentille.CONCAVE?TypeSurface.CONCAVE:TypeSurface.CONVEXE) ;
            }
        } ;
        cercle_2.typeSurfaceProperty().bind(calcul_convexite_cercle_ou_conique_2);
        conique_2.typeSurfaceProperty().bind(calcul_convexite_cercle_ou_conique_2);

        formeFace1Property().addListener( (observable, oldValue, newValue) -> {
            switch (newValue) {
                case SPHERIQUE -> {
                    if (convexiteFace1()==ConvexiteFaceLentille.PLANE)
                        return;
                    composition.retirerObstacle(conique_1);
                    composition.ajouterObstacle(cercle_1);
                }
                case CONIQUE -> {
                    if (convexiteFace1()==ConvexiteFaceLentille.PLANE)
                        return;
                    composition.retirerObstacle(cercle_1);
                    composition.ajouterObstacle(conique_1);
                }
            }
        } );
        formeFace2Property().addListener( (observable, oldValue, newValue) -> {
            switch (newValue) {
                case SPHERIQUE -> {
                    if (convexiteFace2()==ConvexiteFaceLentille.PLANE)
                        return;
                    composition.retirerObstacle(conique_2);
                    composition.ajouterObstacle(cercle_2);
                }
                case CONIQUE -> {
                    if (convexiteFace2()==ConvexiteFaceLentille.PLANE)
                        return;
                    composition.retirerObstacle(cercle_2);
                    composition.ajouterObstacle(conique_2);
                }
            }
        } );



        convexiteFace1Property().addListener( (observable, oldValue, newValue) -> {
            switch (newValue) {
                case PLANE -> {
                    composition.retirerObstacle(cercle_1);
                    composition.retirerObstacle(conique_1);
                    composition.retirerObstacle(dp_limite_face_1_concave);
                    composition.ajouterObstacle(dp_face_1_plane);
                }
                case CONVEXE -> {
                    composition.retirerObstacle(dp_face_1_plane);
                    composition.retirerObstacle(dp_limite_face_1_concave);
                    composition.ajouterObstacle(formeFace1()==FormeFaceLentille.SPHERIQUE?cercle_1:conique_1);
                }
                case CONCAVE -> {
                    composition.retirerObstacle(dp_face_1_plane);
                    composition.ajouterObstacle(formeFace1()==FormeFaceLentille.SPHERIQUE?cercle_1:conique_1);
                    composition.ajouterObstacle(dp_limite_face_1_concave);
                }
            }
        } );

        convexiteFace2Property().addListener( (observable, oldValue, newValue) -> {
            switch (newValue) {
                case PLANE -> {
                    composition.retirerObstacle(cercle_2);
                    composition.retirerObstacle(conique_2);
                    composition.retirerObstacle(dp_limite_face_2_concave);
                    composition.ajouterObstacle(dp_face_2_plane);
                }
                case CONVEXE -> {
                    composition.retirerObstacle(dp_face_2_plane);
                    composition.retirerObstacle(dp_limite_face_2_concave);
                    composition.ajouterObstacle(formeFace2()==FormeFaceLentille.SPHERIQUE?cercle_2:conique_2);
                }
                case CONCAVE -> {
                    composition.retirerObstacle(dp_face_2_plane);
                    composition.ajouterObstacle(formeFace2()==FormeFaceLentille.SPHERIQUE?cercle_2:conique_2);
                    composition.ajouterObstacle(dp_limite_face_2_concave);
                }
            }

        } );


        ObjectBinding<Point2D> calcul_position_centre_cercle_1 = new ObjectBinding<>() {
            @Override
            protected Point2D computeValue() {

                { super.bind(epaisseurProperty(),positionEtOrientationObjectProperty(), rayon1Property(), convexiteFace1Property()) ; }

                Point2D centre = positionEtOrientationObjectProperty().get().position();
                Point2D vecteur_dir = positionEtOrientationObjectProperty().get().direction() ;

                if (convexiteFace1()== ConvexiteFaceLentille.CONVEXE || convexiteFace1()== ConvexiteFaceLentille.PLANE)
                    return centre.add(vecteur_dir.multiply(-0.5d*epaisseur()+ rayon1()));

                // Face 1 CONCAVE : le centre du cercle 1 passe "à gauche" de la face
                return centre.add(vecteur_dir.multiply(-0.5d*epaisseur()- rayon1()));

            }
        } ;
        cercle_1.centreProperty().bind(calcul_position_centre_cercle_1);

        ObjectBinding<PositionEtOrientation> calcul_position_et_orientation_conique_1 = new ObjectBinding<>() {
            @Override
            protected PositionEtOrientation computeValue() {

                { super.bind(epaisseurProperty(),positionEtOrientationObjectProperty(), parametre1Property(),excentricite1Property(), convexiteFace1Property()) ; }

                Point2D position = positionEtOrientationObjectProperty().get().position();
                double orientation_deg = positionEtOrientationObjectProperty().get().orientation_deg();
                Point2D vecteur_dir = positionEtOrientationObjectProperty().get().direction() ;

//                double a = parametre1()/(1+excentricite1()) ;
                if (convexiteFace1()== ConvexiteFaceLentille.CONVEXE || convexiteFace1()== ConvexiteFaceLentille.PLANE)
                    return new PositionEtOrientation(position.add(vecteur_dir.multiply(-0.5d*epaisseur()+parametre1()/(1+excentricite1()))),orientation_deg+180d);
//                    return new PositionEtOrientation(position.add(vecteur_dir.multiply(-0.5d*epaisseur()+parametre1())),orientation_deg+180d);
                // Face 1 CONCAVE : le foyer de la conique 1 passe "à gauche" de la face
                return new PositionEtOrientation(position.add(vecteur_dir.multiply(-0.5d*epaisseur()-parametre1()/(1+excentricite1()))),orientation_deg);
//                return new PositionEtOrientation(position.add(vecteur_dir.multiply(-0.5d*epaisseur()-parametre1())),orientation_deg+180d);

            }
        } ;
        conique_1.positionEtOrientationObjectProperty().bind(calcul_position_et_orientation_conique_1);


        ObjectBinding<Point2D> calcul_position_centre_cercle_2 = new ObjectBinding<>() {
            @Override
            protected Point2D computeValue() {

                { super.bind(epaisseurProperty(),positionEtOrientationObjectProperty(), rayon2Property(), convexiteFace2Property()) ; }

                Point2D centre = positionEtOrientationObjectProperty().get().position();
                Point2D vecteur_dir = positionEtOrientationObjectProperty().get().direction() ;

                if (convexiteFace2()== ConvexiteFaceLentille.CONVEXE || convexiteFace2()== ConvexiteFaceLentille.PLANE)
                    return centre.add(vecteur_dir.multiply(0.5d*epaisseur()- rayon2()));

                // Face 2 CONCAVE : le centre du cercle 2 passe "à droite" de la face
                return centre.add(vecteur_dir.multiply(0.5d*epaisseur()+ rayon2()));
            }
        } ;
        cercle_2.centreProperty().bind(calcul_position_centre_cercle_2);

        ObjectBinding<PositionEtOrientation> calcul_position_et_orientation_conique_2 = new ObjectBinding<>() {
            @Override
            protected PositionEtOrientation computeValue() {

                { super.bind(epaisseurProperty(),positionEtOrientationObjectProperty(), parametre2Property(),excentricite2Property(),  convexiteFace2Property()) ; }

                Point2D position = positionEtOrientationObjectProperty().get().position();
                double orientation_deg = positionEtOrientationObjectProperty().get().orientation_deg();
                Point2D vecteur_dir = positionEtOrientationObjectProperty().get().direction() ;


                if (convexiteFace2()== ConvexiteFaceLentille.CONVEXE || convexiteFace2()== ConvexiteFaceLentille.PLANE)
                    return new PositionEtOrientation(position.add(vecteur_dir.multiply(0.5d*epaisseur()-parametre2()/(1+excentricite2()))),orientation_deg);
                // Face 1 CONCAVE : le foyer de la conique 2 passe "à gauche" de la face
                return new PositionEtOrientation(position.add(vecteur_dir.multiply(0.5d*epaisseur()+parametre2()/(1+excentricite2()))),orientation_deg+180d);

            }
        } ;
        conique_2.positionEtOrientationObjectProperty().bind(calcul_position_et_orientation_conique_2);


        ObjectBinding<PositionEtOrientation> calcul_position_dp_haut = new ObjectBinding<>() {
            @Override
            protected PositionEtOrientation computeValue() {

                { super.bind(diametreProperty(),positionEtOrientationObjectProperty()) ; }

                Point2D position = positionEtOrientationObjectProperty().get().position();
                double orientation_deg = positionEtOrientationObjectProperty().get().orientation_deg();
                Point2D vecteur_dir = positionEtOrientationObjectProperty().get().direction() ;
//                return new PositionEtOrientation(position.add(vecteur_dir.multiply(0.5d*diametre())) , orientation_deg+90d ) ;
                Point2D perp_vecteur_dir = new Point2D(-vecteur_dir.getY(), vecteur_dir.getX()) ;
                return new PositionEtOrientation(position.add(perp_vecteur_dir.multiply(0.5d*diametre())) , orientation_deg+90d ) ;

            }
        } ;
        dp_haut.positionEtOrientationObjectProperty().bind(calcul_position_dp_haut);

        ObjectBinding<PositionEtOrientation> calcul_position_dp_bas = new ObjectBinding<>() {
            @Override
            protected PositionEtOrientation computeValue() {

                { super.bind(diametreProperty(),positionEtOrientationObjectProperty()) ; }

                Point2D position = positionEtOrientationObjectProperty().get().position();
                double orientation_deg = positionEtOrientationObjectProperty().get().orientation_deg();
                Point2D vecteur_dir = positionEtOrientationObjectProperty().get().direction() ;
//                return new PositionEtOrientation(position.add(vecteur_dir.multiply(0.5d*diametre())) , orientation_deg+270d ) ;
                Point2D perp_vecteur_dir = new Point2D(vecteur_dir.getY(), -vecteur_dir.getX()) ;
                return new PositionEtOrientation(position.add(perp_vecteur_dir.multiply(0.5d*diametre())) , orientation_deg+270d ) ;

            }
        } ;
        dp_bas.positionEtOrientationObjectProperty().bind(calcul_position_dp_bas);

//        diametreProperty().addListener( (observable, oldValue,newValue) -> {
//            ajouterOuRetirerDpHautDpBas(newValue.doubleValue(), rayon1(), rayon2()) ;
//        } ) ;
//        rayon1Property().addListener( (observable, oldValue, newValue) -> {
//            ajouterOuRetirerDpHautDpBas(diametre(), newValue.doubleValue(), rayon2()) ;
//        } ) ;
//        rayon2Property().addListener( (observable, oldValue, newValue) -> {
//            ajouterOuRetirerDpHautDpBas(diametre(), rayon1(), newValue.doubleValue()) ;
//        } ) ;

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
        dp_face_1_plane.positionEtOrientationObjectProperty().bind(calcul_position_dp1);

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
        dp_face_2_plane.positionEtOrientationObjectProperty().bind(calcul_position_dp2);

        ObjectBinding<PositionEtOrientation> calcul_position_dp_limite_face_1_concave = new ObjectBinding<>() {
            @Override
            protected PositionEtOrientation computeValue() {

                { super.bind(formeFace1Property(),rayon1Property(),parametre1Property(),excentricite1Property(),epaisseurProperty(),positionEtOrientationObjectProperty()) ; }

                Point2D position = positionEtOrientationObjectProperty().get().position();
                double orientation_deg = positionEtOrientationObjectProperty().get().orientation_deg();
                Point2D vecteur_dir = positionEtOrientationObjectProperty().get().direction() ;

                double d_lim = -0.5d*epaisseur() - (formeFace1()==FormeFaceLentille.SPHERIQUE?rayon1():(excentricite1()<1d?parametre1()/(1-excentricite1()*excentricite1()):Double.MAX_VALUE)) ;
                return new PositionEtOrientation(position.add(vecteur_dir.multiply(d_lim)) , orientation_deg+180d ) ;
//                return new PositionEtOrientation(position.add(vecteur_dir.multiply(-0.5d*epaisseur()- rayon1())) , orientation_deg+180d ) ;

            }
        } ;
        dp_limite_face_1_concave.positionEtOrientationObjectProperty().bind(calcul_position_dp_limite_face_1_concave);

        ObjectBinding<PositionEtOrientation> calcul_position_dp_limite_face_2_concave = new ObjectBinding<>() {
            @Override
            protected PositionEtOrientation computeValue() {

                { super.bind(formeFace2Property(),rayon2Property(),parametre2Property(),excentricite2Property(),epaisseurProperty(),positionEtOrientationObjectProperty()) ; }

                Point2D position = positionEtOrientationObjectProperty().get().position();
                double orientation_deg = positionEtOrientationObjectProperty().get().orientation_deg();
                Point2D vecteur_dir = positionEtOrientationObjectProperty().get().direction() ;
                double d_lim = 0.5d*epaisseur() + (formeFace2()==FormeFaceLentille.SPHERIQUE?rayon2():(excentricite2()<1d?parametre2():Double.MAX_VALUE)) ;
                return new PositionEtOrientation(position.add(vecteur_dir.multiply(d_lim)) , orientation_deg+0d ) ;
//                return new PositionEtOrientation(position.add(vecteur_dir.multiply(0.5d*epaisseur()+ rayon2())) , orientation_deg+0d ) ;

            }
        } ;
        dp_limite_face_2_concave.positionEtOrientationObjectProperty().bind(calcul_position_dp_limite_face_2_concave);

        // TODO
    }

    private void ajouterOuRetirerDpHautDpBas(double diametre, double r_1, double r_2) {
        if (0.5d*diametre <Math.max(r_1,r_2) ) {
            if (!composition.comprend(dp_haut))
                composition.ajouterObstacle(dp_haut);
            if (!composition.comprend(dp_bas))
                composition.ajouterObstacle(dp_bas);
        } else {
            if (composition.comprend(dp_haut))
                composition.retirerObstacle(dp_haut);
            if (composition.comprend(dp_bas))
                composition.retirerObstacle(dp_bas);
        }
    }


    public ObjectProperty<PositionEtOrientation> positionEtOrientationObjectProperty() {return position_orientation ;}
    public Point2D centre() { return position_orientation.get().position() ; }
    public double xCentre() { return centre().getX() ; }
    public double yCentre() { return centre().getY() ; }

    public double epaisseur() {return epaisseur.get();}
    public DoubleProperty epaisseurProperty() {return epaisseur;}

    public FormeFaceLentille formeFace1() {return forme_face_1.get();}
    public ObjectProperty<FormeFaceLentille> formeFace1Property() {return forme_face_1;}
    public double rayon1() {return rayon_1.get();}
    public DoubleProperty rayon1Property() {return rayon_1;}
    public double parametre1() {return parametre_1.get();}
    public DoubleProperty parametre1Property() {return parametre_1;}
    public double excentricite1() {return excentricite_1.get();}
    public DoubleProperty excentricite1Property() {return excentricite_1;}
    public ConvexiteFaceLentille convexiteFace1() {return convexite_face_1.get();}
    public ObjectProperty<ConvexiteFaceLentille> convexiteFace1Property() {return convexite_face_1;}

    public FormeFaceLentille formeFace2() {return forme_face_2.get();}
    public ObjectProperty<FormeFaceLentille> formeFace2Property() {return forme_face_2;}

    public double rayon2() {return rayon_2.get();}
    public DoubleProperty rayon2Property() {return rayon_2;}
    public double parametre2() {return parametre_2.get();}
    public DoubleProperty parametre2Property() {return parametre_2;}
    public double excentricite2() {return excentricite_2.get();}
    public DoubleProperty excentricite2Property() {return excentricite_2;}
    
    public ConvexiteFaceLentille convexiteFace2() {return convexite_face_2.get();}
    public ObjectProperty<ConvexiteFaceLentille> convexiteFace2Property() {return convexite_face_2;}
    public double diametre() {return diametre.get();}
    public DoubleProperty diametreProperty() {return diametre;}

    public void definirCentre(Point2D centre) {position_orientation.set(new PositionEtOrientation(centre,orientation()));}
    public void definirEpaisseur(double epaisseur) {this.epaisseur.set(epaisseur);}
    
    public void definirFormeFace1(FormeFaceLentille forme_1) { this.forme_face_1.set(forme_1); }
    public void definirRayon1(double r_1) {this.rayon_1.set(r_1);}
    public void definirParametre1(double p_1) {this.parametre_1.set(p_1);}
    public void definirExcentricite1(double e_1) {this.excentricite_1.set(e_1);}
    public void definirConvexiteFace1(ConvexiteFaceLentille convexite_face_1) {this.convexite_face_1.set(convexite_face_1);}

    public void definirFormeFace2(FormeFaceLentille forme_2) { this.forme_face_2.set(forme_2); }
    public void definirRayon2(double r_2) {this.rayon_2.set(r_2);}
    public void definirParametre2(double p_2) {this.parametre_2.set(p_2);}
    public void definirExcentricite2(double e_2) {this.excentricite_2.set(e_2);}
    public void definirConvexiteFace2(ConvexiteFaceLentille convexite_face_2) {this.convexite_face_2.set(convexite_face_2);}

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

        forme_face_1.addListener((observable, oldValue, newValue) -> rap.rappel());
        rayon_1.addListener((observable, oldValue, newValue) -> rap.rappel());
        parametre_1.addListener((observable, oldValue, newValue) -> rap.rappel());
        excentricite_1.addListener((observable, oldValue, newValue) -> rap.rappel());
        convexite_face_1.addListener((observable, oldValue, newValue) -> rap.rappel());

        forme_face_2.addListener((observable, oldValue, newValue) -> rap.rappel());
        rayon_2.addListener((observable, oldValue, newValue) -> rap.rappel());
        parametre_2.addListener((observable, oldValue, newValue) -> rap.rappel());
        excentricite_2.addListener((observable, oldValue, newValue) -> rap.rappel());
        convexite_face_2.addListener((observable, oldValue, newValue) -> rap.rappel());

        diametre.addListener((observable, oldValue, newValue) -> rap.rappel());
    }

    @Override
    public void ajouterRappelSurChangementTouteProprieteModifiantChemin(RappelSurChangement rap) {
        super.ajouterRappelSurChangementTouteProprieteModifiantChemin(rap);

        position_orientation.addListener((observable, oldValue, newValue) -> rap.rappel());

        epaisseur.addListener((observable, oldValue, newValue) -> rap.rappel());

        forme_face_1.addListener((observable, oldValue, newValue) -> rap.rappel());
        rayon_1.addListener((observable, oldValue, newValue) -> rap.rappel());
        parametre_1.addListener((observable, oldValue, newValue) -> rap.rappel());
        excentricite_1.addListener((observable, oldValue, newValue) -> rap.rappel());
        convexite_face_1.addListener((observable, oldValue, newValue) -> rap.rappel());

        forme_face_2.addListener((observable, oldValue, newValue) -> rap.rappel());
        rayon_2.addListener((observable, oldValue, newValue) -> rap.rappel());
        parametre_2.addListener((observable, oldValue, newValue) -> rap.rappel());
        excentricite_2.addListener((observable, oldValue, newValue) -> rap.rappel());
        convexite_face_2.addListener((observable, oldValue, newValue) -> rap.rappel());

        diametre.addListener((observable, oldValue, newValue) -> rap.rappel());
    }


    @Override
    public void retaillerPourSourisEn(Point2D pos_souris) {
        // Si on est sur le point de départ, ne rien faire
        if (pos_souris.equals(centre()))
            return ;

        epaisseur.set(2d*Math.abs(pos_souris.getX() - xCentre()));
        diametre.set(2d * Math.abs(pos_souris.getY() - yCentre()));

        rayon_1.set(10*epaisseur());
        parametre_1.set(10*epaisseur());
        rayon_2.set(10*epaisseur());
        parametre_2.set(10*epaisseur());

    }

    @Override
    public void retaillerSelectionPourSourisEn(Point2D pos_souris) {
        // Si on est sur le point de départ, ne rien faire
        if (pos_souris.equals(centre()))
            return ;

        if (!appartientASystemeOptiqueCentre()) {
            // Calculer l'écart angulaire entre le Coin HD où se trouve la poignée et la position de la souris, par rapport
            // au centre du Rectangle
            Point2D vec_centre_hd = calcule_position_poignee().subtract(centre()) ;
//            Point2D vec_centre_hd = (new Point2D(xCentre()+0.5d*epaisseur(),yCentre()+0.5d*diametre())).subtract(centre()) ;
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
            rayon_1.set(10*epaisseur());
            parametre_1.set(10*epaisseur());
            rayon_2.set(10*epaisseur());
            parametre_2.set(10*epaisseur());
        }

    }

    private double produit_vectoriel_simplifie(Point2D v1, Point2D v2) {
        return (v1.getX()*v2.getY()-v1.getY()*v2.getX()) ;
    }

    @Override
    public Contour positions_poignees() {
        Contour c_poignees = new Contour(1);

        c_poignees.ajoutePoint(calcule_position_poignee()) ;

        return c_poignees;
    }

    private Point2D calcule_position_poignee() {
        double theta = Math.toRadians(orientation()) ;
        double cos_theta = Math.cos(theta) ;
        double sin_theta = Math.sin(theta) ;

        return new Point2D(xCentre()+0.5d*epaisseur()*cos_theta-0.5d*diametre()*sin_theta,
                yCentre()+0.5d*epaisseur()*sin_theta+0.5d*diametre()*cos_theta) ;

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
        rayon_1.set(rayon1()*facteur_conversion);
        rayon_2.set(rayon2()*facteur_conversion);
    }

    public Composition composition() {
        return composition;
    }
}
