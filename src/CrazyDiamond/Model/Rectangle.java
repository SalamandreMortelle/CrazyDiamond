package CrazyDiamond.Model;

import javafx.beans.property.*;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.util.Pair;

import java.io.IOException;
import java.util.ArrayList;

public class Rectangle implements Obstacle, Identifiable, Nommable,ElementAvecContour,ElementAvecMatiere {

    private final Imp_Identifiable imp_identifiable ;
    private final Imp_Nommable imp_nommable;
    private final Imp_ElementAvecContour imp_elementAvecContour ;
    private final Imp_ElementAvecMatiere imp_elementAvecMatiere ;

    private final ObjectProperty<PositionEtOrientation> position_orientation ;
//    protected final DoubleProperty x_centre ;
//    protected final DoubleProperty y_centre ;
//    protected final DoubleProperty orientation ;

    protected final DoubleProperty largeur ;
    protected final DoubleProperty hauteur ;



    private static int compteur_rectangle ;
    private final BooleanProperty appartenance_systeme_optique_centre;

    public Rectangle(TypeSurface type_surface, double  x_centre, double y_centre, double largeur, double hauteur, double orientation_deg) throws IllegalArgumentException {
        this(
            new Imp_Identifiable(),
            new Imp_Nommable( "Rectangle "+(++compteur_rectangle)),
            new Imp_ElementAvecContour(null),
            new Imp_ElementAvecMatiere(type_surface,null,1.0,null ),
            x_centre,y_centre,largeur,hauteur,orientation_deg
        ) ;
    }
    public Rectangle(Imp_Identifiable ii,Imp_Nommable in,Imp_ElementAvecContour iec, Imp_ElementAvecMatiere iem, double  x_centre, double y_centre, double largeur, double hauteur, double orientation_deg) throws IllegalArgumentException {

        if (largeur==0d || hauteur==0d)
            throw new IllegalArgumentException("Un rectangle doit avoir une largeur et une hauteur non nulles.") ;

        imp_identifiable = ii ;
        imp_nommable = in ;
        imp_elementAvecContour = iec;
        imp_elementAvecMatiere = iem;

        this.position_orientation = new SimpleObjectProperty<PositionEtOrientation>(new PositionEtOrientation(new Point2D(x_centre,y_centre),orientation_deg)) ;
//        this.x_centre = new SimpleDoubleProperty(x_centre);
//        this.y_centre = new SimpleDoubleProperty(y_centre);
//        this.orientation = new SimpleDoubleProperty(orientation_deg) ;

        this.largeur = new SimpleDoubleProperty(largeur);
        this.hauteur = new SimpleDoubleProperty(hauteur);


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

    @Override public void definirOrientationAxePolariseur(double angle_pol) {imp_elementAvecContour.definirOrientationAxePolariseur(angle_pol);}
    @Override public double orientationAxePolariseur() {return imp_elementAvecContour.orientationAxePolariseur() ;}
    @Override public DoubleProperty orientationAxePolariseurProperty() {return imp_elementAvecContour.orientationAxePolariseurProperty() ;}
    @Override
    public Double courbureRencontreeAuSommet(Point2D pt_sur_surface, Point2D direction) throws Exception {
        return null ;
    }
    @Override public void definirTauxReflexionSurface(double taux_refl) {imp_elementAvecContour.definirTauxReflexionSurface(taux_refl);}
    @Override public double tauxReflexionSurface() {return imp_elementAvecContour.tauxReflexionSurface();}

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

    public void definirCentre(Point2D centre) {position_orientation.set(new PositionEtOrientation(centre,orientation()));}

//    public void definirXcentre(double x_c) { this.x_centre.set(x_c); }
//    public void definirYcentre(double y_c) { this.y_centre.set(y_c); }

    public void definirLargeur(double larg) { this.largeur.set(larg); }
    public void definirHauteur(double haut) { this.hauteur.set(haut); }

//    public DoubleProperty xCentreProperty() { return x_centre; }
//    public DoubleProperty yCentreProperty() { return y_centre; }
    public DoubleProperty largeurProperty() { return largeur; }
    public double largeur() { return largeur.get(); }
    public DoubleProperty hauteurProperty() { return hauteur; }
    public double hauteur() { return hauteur.get(); }

//    public DoubleProperty orientationProperty() { return orientation ;}
    public ObjectProperty<PositionEtOrientation> positionEtOrientationObjectProperty() {
        return position_orientation ;
    }
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


    public void translater(Point2D vecteur) {
        position_orientation.set(new PositionEtOrientation(centre().add(vecteur),orientation()));
//        x_centre.set(vecteur.getX()+x_centre.get()) ;
//        y_centre.set(vecteur.getY()+y_centre.get()) ;
    }


    public void accepte(VisiteurEnvironnement v) {
        v.visiteRectangle(this);
    }

    @Override
    public void accepte(VisiteurElementAvecMatiere v) {
        v.visiteRectangle(this);
    }

    public ContoursObstacle couper(BoiteLimiteGeometrique boite) {
        return couper(boite,true) ;
    }

    DemiDroiteOuSegment cote(BordRectangle b) {

        if (b== BordRectangle.HAUT) return DemiDroiteOuSegment.construireSegment(coin(Coin.HD),coin(Coin.HG)) ;
        if (b== BordRectangle.GAUCHE) return  DemiDroiteOuSegment.construireSegment(coin(Coin.HG),coin(Coin.BG)) ;
        if (b== BordRectangle.BAS) return DemiDroiteOuSegment.construireSegment(coin(Coin.BG),coin(Coin.BD)) ;

        return DemiDroiteOuSegment.construireSegment(coin(Coin.BD),coin(Coin.HD)) ;

    }
    ContoursObstacle couper(BoiteLimiteGeometrique boite, boolean avec_contours_surface) {

        ContoursObstacle contours = new ContoursObstacle() ;

        Contour c_masse = new Contour() ;
        Contour c_surface = null ;

        BordRectangle bord_init = null ;
        BordRectangle bord_prec = null ;
        Point2D intersection = null ;

        boolean trace_surface = false ;

        // On part du Bord HAUT du Rectangle
        BordRectangle cote_courant = BordRectangle.HAUT ;
        DemiDroiteOuSegment seg_cote_courant = cote(cote_courant) ;

        if (boite.contains(coin(Coin.HD))) { // Le coin HD est visible
            // On l'ajoute au contour de masse et au contour de surface
            c_masse.ajoutePoint(coin(Coin.HD));
            c_surface = new Contour() ;
            c_surface.ajoutePoint(coin(Coin.HD));
            trace_surface = true ;
        }

        do { // Boucle sur les côtés du Rectangle this (cote_courant/seg_cote_courant) parcourus dans le sens trigo

            // On va stocker, pour chaque intersection (deux au plus) entre la zone visible et le cote sec_courant du
            // Rectangle : le Bord (de la zone visible "boite") + le point d'intersection.
            Pair<BordRectangle,Point2D>[] intersections_avec_bords  = new Pair[2];

//            // Recherche des intersections (de 0 à 2) de seg_cote avec boite limite
            for (BordRectangle b : BordRectangle.values()) {
                intersection =  seg_cote_courant.intersectionAvec(boite.bord(b)) ;
                if (intersection != null) {
                    if (intersections_avec_bords[0]==null) {
                        intersections_avec_bords[0] = new Pair<>(b,intersection) ;
                    } else {
                        intersections_avec_bords[1] = new Pair<>(b,intersection)  ;
                    }
                }
            }

            // Il faut que les interserctions soient classées de la plus proche du départ du seg_cote_courant à la plus éloignée
//            if (intersections_avec_bords[1]!=null && intersections_avec_bords[0].getValue().subtract(seg_cote_courant.depart()).magnitude()>intersections_avec_bords[1].getValue().subtract(seg_cote_courant.depart()).magnitude()) {
            if (intersections_avec_bords[1]!=null && intersections_avec_bords[0].getValue().distance(seg_cote_courant.depart())>intersections_avec_bords[1].getValue().distance(seg_cote_courant.depart())) {
                Pair<BordRectangle,Point2D> tampon = new Pair<>(intersections_avec_bords[0].getKey(),intersections_avec_bords[0].getValue()) ;
                intersections_avec_bords[0] = new Pair<>(intersections_avec_bords[1].getKey(),intersections_avec_bords[1].getValue()) ;
                intersections_avec_bords[1] = tampon ;
            }


            // Si seg_cote a une première intersection
            if (intersections_avec_bords[0]!=null) {

                if (bord_init==null)
                    bord_init = intersections_avec_bords[0].getKey() ;

                if (bord_prec!=null)
                    boite.completerContourAvecCoinsConsecutifsEntreBordsContenusDansObstacle(bord_prec,intersections_avec_bords[0].getKey(),this,c_masse);

                c_masse.ajoutePoint(intersections_avec_bords[0].getValue());

                // Initialiser un nouveau c_surface si nécessaire
                if (trace_surface==true) {
                    c_surface.ajoutePoint(intersections_avec_bords[0].getValue());
                    trace_surface = false ;
                }
                else { // Début du tracé d'un nouveau morceau de surface
                    if (c_surface!=null)
                        contours.ajouterContourSurface(c_surface);

                    c_surface = new Contour() ;
                    c_surface.ajoutePoint(intersections_avec_bords[0].getValue());
                    trace_surface = true ;
                }


                bord_prec = intersections_avec_bords[0].getKey() ;
            }

            // Si l'extrêmité suivante du côté courant du Rectangle est visible
            if (boite.contains(coin(cote_courant.coin_suivant()))) {

                // L'ajouter au contour de masse, et au contour de surface
                c_masse.ajoutePoint(coin(cote_courant.coin_suivant()));
                c_surface.ajoutePoint(coin(cote_courant.coin_suivant()));
                bord_prec = null ;
            } else {
                if (intersections_avec_bords[1]!=null) { // 2ème intersection => Le contour sort de la zone visible
                    c_masse.ajoutePoint(intersections_avec_bords[1].getValue());
                    c_surface.ajoutePoint(intersections_avec_bords[1].getValue());
                    // Si il y a une 2ème intersection, le tracé de la surface s'interrompt forcément (2ème intersection = sortie de la zone visible)
                    trace_surface = false ;
                    bord_prec = intersections_avec_bords[1].getKey() ;
                }
            }

            cote_courant = cote_courant.bord_suivant() ;
            seg_cote_courant = cote(cote_courant) ;
        } while (cote_courant != BordRectangle.HAUT) ;

        if (bord_prec!=null)
            boite.completerContourAvecCoinsConsecutifsEntreBordsContenusDansObstacle(bord_prec,bord_init,this,c_masse);

        // Aucune partie du contour du rectangle n'est visible, et le centre de la zone visible est dans le Rectangle
        if (c_masse.nombrePoints() == 0 && this.contient(boite.centre())) {

            contours.ajouterContourMasse(boite.construireContour());

            return contours ;

        }

        // A partir d'ici, on sait que c_masse n'est pas vide : une partie du contour du Rectangle est visible

        // Si le Rectangle est CONCAVE, c_masse est un "trou" dans la zone visible
        if (typeSurface() == TypeSurface.CONCAVE && c_masse.nombrePoints()>0)
            contours.ajouterContourMasse(boite.construireContourAntitrigo());

        contours.ajouterContourMasse(c_masse);

        if (c_surface!=null)
            contours.ajouterContourSurface(c_surface);

        return contours ;

    }

    ContoursObstacle couper_old(BoiteLimiteGeometrique boite, boolean avec_contours_surface) {

        ContoursObstacle contours = new ContoursObstacle() ;

        double xmin = boite.getMinX() ;
        double xmax = boite.getMaxX() ;
        double ymin = boite.getMinY() ;
        double ymax = boite.getMaxY() ;

//        BoiteLimiteGeometrique boite_rect = boite() ;
        BoiteLimiteGeometrique boite_rect = new BoiteLimiteGeometrique(0,0,1,2) ;

        // Boite totalement hors zone visible (et n'englobe pas la zone visible)
        if (  boite_rect.getMinY() > ymax || boite_rect.getMaxY() < ymin
                || boite_rect.getMinX() > xmax || boite_rect.getMaxX() < xmin ) {

            if (typeSurface() == TypeSurface.CONCAVE)
                contours.ajouterContourMasse(boite.construireContour());
//                gc.fillRect(cae.xmin(),cae.ymin(),cae.xmax()-cae.xmin(),cae.ymax()-cae.ymin());

//            gc.setFill(pf);
//            gc.setStroke(s);

            return contours ; // Rien à faire si le rectangle est convexe et hors zone

        }

        // Boite englobe totalement la zone visible
        if ( ( boite_rect.getMinY() < ymin && boite_rect.getMaxY() > ymax )
                && ( boite_rect.getMinX() < xmin &&  boite_rect.getMaxX() > xmax ) ) {

            if (typeSurface() == TypeSurface.CONVEXE)
                contours.ajouterContourMasse(boite.construireContour());
//                gc.fillRect(cae.xmin(),cae.ymin(),cae.xmax()-cae.xmin(),cae.ymax()-cae.ymin());

//            gc.setFill(pf);
//            gc.setStroke(s);

            return contours; // Rien à faire si le rectangle est concave et englobe la zone visible
        }



        // Construction du contour visible du rectangle, dans le sens trigo
        Contour contour_travail = new Contour(4) ;
        Contour contour_surface = new Contour(4) ;

        // Coin HD est-il visible
        if (boite.contains(boite_rect.coin(Coin.HD)))
            contour_travail.ajoutePoint(boite_rect.coin(Coin.HD));
        else { // Coin HD hors zone
            // Coin HD trop à droite mais pas trop haut (morceau de l'horizontale droite visible)
            if ( boite_rect.getMaxX()>xmax && boite_rect.getMaxY()<=ymax)
                contour_travail.ajoutePoint(xmax,boite_rect.getMaxY());
            // Sinon reste le cas trop haut mais rien à faire dans ce cas
        }

        // Coin HG est-il visible
        if (boite.contains(boite_rect.coin(Coin.HG)))
            contour_travail.ajoutePoint(boite_rect.coin(Coin.HG));
        else { // Coin HG hors zone
            // Coin HG trop à gauche mais pas trop haut (morceau horizontale haute visible)
            if (boite_rect.getMinX() < xmin && boite_rect.getMaxY() <= ymax) {
                contour_travail.ajoutePoint(xmin, boite_rect.getMaxY()); // Point de sortie
                if (avec_contours_surface && contour_travail.nombrePoints() > 1)
                    contour_surface.concatene(contour_travail);
//                    cae.tracerPolyligne(contour_masse.xpoints, contour_masse.ypoints);
                contour_travail.raz();
            } else if (boite_rect.getMinX()>=xmin && boite_rect.getMaxY() > ymax ) { // Point HG trop haut mais pas trop à gauche (morceau verticale gauche visible)
                contour_travail.ajoutePoint(boite_rect.getMinX(),ymax);
            } // Sinon reste le cas trop haut ET trop à gauche mais rien à faire dans ce cas
        }

        // Coin BG est-il visible
        if (boite.contains(boite_rect.coin(Coin.BG)))
            contour_travail.ajoutePoint(boite_rect.coin(Coin.BG));
        else { // Coin BG hors zone
            // Coin BG trop bas mais pas trop à gauche (morceau verticale gauche visible)
            if ( boite_rect.getMinX()>=xmin && boite_rect.getMinY() < ymin ) {
                contour_travail.ajoutePoint(boite_rect.getMinX(), ymin); // Point de sortie
                if (avec_contours_surface && contour_travail.nombrePoints() > 1)
                    contour_surface.concatene(contour_travail);
//                if (contour_masse.taille() > 1)
//                    cae.tracerPolyligne(contour_masse.xpoints, contour_masse.ypoints);
                contour_travail.raz();
            } else if (boite_rect.getMinY()>=ymin && boite_rect.getMinX()<xmin ) { // Point BG trop à gauche mais pas trop bas (morceau horizontale basse visible)
                contour_travail.ajoutePoint(xmin, boite_rect.getMinY()); // Nouveau pt de départ (entrée dans zone)
            } // Sinon reste le cas trop bas ET trop à gauche mais rien à faire dans ce cas

        }

        // Coin BD est-il visible
        if (boite.contains(boite_rect.coin(Coin.BD)))
            contour_travail.ajoutePoint(boite_rect.coin(Coin.BD));
        else { // Coin BD hors zone
            // Coin BD pas trop bas mais trop à droite (morceau horizontale basse visible)
            if ( boite_rect.getMinY()>=ymin && boite_rect.getMaxX() > xmax) {
                contour_travail.ajoutePoint(xmax, boite_rect.getMinY());
                if (avec_contours_surface && contour_travail.nombrePoints() > 1)
                    contour_surface.concatene(contour_travail);
//                if (contour_masse.taille() > 1)
//                    cae.tracerPolyligne(contour_masse.xpoints, contour_masse.ypoints);
                contour_travail.raz();
            } else if (boite_rect.getMaxX()<=xmax && boite_rect.getMinY()<ymin) {// Coin BD trop bas mais pas trop à droite (morceau verticale droite visible)
                contour_travail.ajoutePoint(boite_rect.getMaxX(),ymin); // Nouveau point de départ (entrée dans zone)
            }
        }

        // Retour sur HD pour bouclage éventuel du circuit :
        // Coin HD est-il visible
        if (boite.contains(boite_rect.coin(Coin.HD)))
            contour_travail.ajoutePoint(boite_rect.coin(Coin.HD));
        else { // Coin HD hors zone
            // Coin HD trop haut mais pas trop à droite (morceau verticale droite visible)
            if ( boite_rect.getMaxY()>ymax && boite_rect.getMaxX()<=xmax )
                contour_travail.ajoutePoint(boite_rect.getMaxX(),ymax);
            // Sinon reste le cas trop à droite mais  rien à faire dans ce cas
        }

        if (avec_contours_surface && contour_travail.nombrePoints() > 1)
            contour_surface.concatene(contour_travail);
//            cae.tracerPolyligne(contour_masse.xpoints, contour_masse.ypoints);

        if (avec_contours_surface && contour_surface.nombrePoints()>1)
            contours.ajouterContourSurface(contour_surface);

        BoiteLimiteGeometrique partie_visible = boite_rect.couper(boite) ;

        if (partie_visible==null) // Ne doit pas arriver : ce cas est déjà écarté (cf. returns en debut de fonction)
            throw new IllegalStateException("Problème dans le tracé du rectangle "+this) ;

        if (typeSurface()== TypeSurface.CONVEXE)
            contours.ajouterContourMasse(partie_visible.construireContour());
//            gc.fillRect(partie_visible.getMinX(),partie_visible.getMinY(),partie_visible.getWidth(),partie_visible.getHeight()) ;
        else { //CONCAVE
            contours.ajouterContourMasse(boite.construireContourAntitrigo());
            contours.ajouterContourMasse(partie_visible.construireContour());
        }

        return contours ;

//        if (rect.typeSurface()== Obstacle.TypeSurface.CONVEXE)
//            gc.fillRect(boite.getMinX(),boite.getMinY(),boite.getWidth(),boite.getHeight()) ;
//        else { // CONCAVE
//            gc.fillRect(cae.xmin(), cae.ymin(),boite.getMinX()- cae.xmin(), cae.ymax()- cae.ymin()); // Partie gauche
//            gc.fillRect(boite.getMaxX(), cae.ymin(), cae.xmax()-boite.getMaxX(), cae.ymax()- cae.ymin()); // Partie droite
//            gc.fillRect(boite.getMinX(), cae.ymin(),boite.getWidth(),boite.getMinY()- cae.ymin()) ; // Partie centrale basse
//            gc.fillRect(boite.getMinX(),boite.getMaxY(),boite.getWidth(), cae.ymax()-boite.getHeight()-boite.getMinY()); // Partie centrale haute
//        }

//        gc.strokeRect(boite.getMinX(),boite.getMinY(),boite.getWidth(),boite.getHeight());

//        gc.setFill(pf);
//        gc.setStroke(s);
        // Note : on pourrait aussi utiliser gc.save() au début de la méthode puis gc.restore() à la fin


    }

    @Override
    public void ajouterRappelSurChangementToutePropriete(RappelSurChangement rap) {
        imp_elementAvecContour.ajouterRappelSurChangementToutePropriete(rap);
        imp_elementAvecMatiere.ajouterRappelSurChangementToutePropriete(rap);

        position_orientation.addListener((observable, oldValue, newValue) -> { rap.rappel(); });
//        x_centre.addListener((observable, oldValue, newValue) -> { rap.rappel(); });
//        y_centre.addListener((observable, oldValue, newValue) -> { rap.rappel(); });
//        orientation.addListener((observable, oldValue, newValue) -> { rap.rappel(); });
        largeur.addListener((observable, oldValue, newValue) -> { rap.rappel(); });
        hauteur.addListener((observable, oldValue, newValue) -> { rap.rappel(); });
    }

    @Override
    public void ajouterRappelSurChangementTouteProprieteModifiantChemin(RappelSurChangement rap) {
        imp_elementAvecContour.ajouterRappelSurChangementTouteProprieteModifiantChemin(rap);
        imp_elementAvecMatiere.ajouterRappelSurChangementTouteProprieteModifiantChemin(rap);

        position_orientation.addListener((observable, oldValue, newValue) -> { rap.rappel(); });
//        x_centre.addListener((observable, oldValue, newValue) -> { rap.rappel(); });
//        y_centre.addListener((observable, oldValue, newValue) -> { rap.rappel(); });
//        orientation.addListener((observable, oldValue, newValue) -> { rap.rappel(); });
        largeur.addListener((observable, oldValue, newValue) -> { rap.rappel(); });
        hauteur.addListener((observable, oldValue, newValue) -> { rap.rappel(); });

    }

    public void retaillerPourSourisEn(Point2D pos_souris) {
        // Si on est sur le point de départ, ne rien faire
//        if (pos_souris.getX()==x_centre.get() && pos_souris.getY()==y_centre.get())
//            return ;
        if (pos_souris.equals(centre()))
            return ;

            largeur.set(2d * Math.abs(pos_souris.getX() - xCentre()));
            hauteur.set(2d * Math.abs(pos_souris.getY() - yCentre()));

    }

    @Override
    public void retaillerSelectionPourSourisEn(Point2D pos_souris) {
        // Si on est sur le point de départ, ne rien faire
        if (pos_souris.equals(centre()))
            return ;
//        if (pos_souris.getX()==x_centre.get() && pos_souris.getY()==y_centre.get())
//            return ;

        if (!appartientASystemeOptiqueCentre()) {
            // Calculer l'écart angulaire entre le Coin HD où se trouve la poignée et la position de la souris, par rapport
            // au centre du Rectangle
            Point2D vec_centre_hd = coin(Coin.HD).subtract(centre()) ;
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

            largeur.set(Math.abs(2d*longueur_demi_diagonale*Math.cos(alpha)));
            hauteur.set(Math.abs(2d*longueur_demi_diagonale*Math.sin(alpha)));

        }

    }


    @Override
    public Contour positions_poignees() {
        Contour c_poignees = new Contour(1);

//        double demi_largeur = largeur.get()/2d ;
//        double demi_hauteur = hauteur.get()/2d ;

        c_poignees.ajoutePoint(coin(Coin.HD));

//        c_poignees.ajoutePoint(x_centre.get()+demi_largeur , y_centre.get()+demi_hauteur);
//        c_poignees.ajoutePoint(x_centre.get()-demi_largeur , y_centre.get()+demi_hauteur);
//        c_poignees.ajoutePoint(x_centre.get()-demi_largeur , y_centre.get()-demi_hauteur);
//        c_poignees.ajoutePoint(x_centre.get()+demi_largeur , y_centre.get()-demi_hauteur);

        return c_poignees;
    }

    private Point2D vecteur_directeur() {
        double theta = Math.toRadians(orientation()) ;

        double cos_theta = Math.cos(theta) ;
        double sin_theta = Math.sin(theta) ;

       return new Point2D(cos_theta,sin_theta) ;
    }
    public Point2D centre() { return position_orientation.get().position() ; }
    public double xCentre() { return centre().getX() ; }
    public double yCentre() { return centre().getY() ; }

    public Point2D coin(Coin c) {
        double theta = Math.toRadians(orientation()) ;
        double cos_theta = Math.cos(theta) ;
        double sin_theta = Math.sin(theta) ;

        if (c==Coin.HD)
            return new Point2D(xCentre()+0.5d*largeur.get()*cos_theta-0.5d*hauteur.get()*sin_theta,
                    yCentre()+0.5d*largeur.get()*sin_theta+0.5d*hauteur.get()*cos_theta) ;

        if (c==Coin.HG)
            return new Point2D(xCentre()-0.5d*largeur.get()*cos_theta-0.5d*hauteur.get()*sin_theta,
                    yCentre()-0.5d*largeur.get()*sin_theta+0.5d*hauteur.get()*cos_theta) ;

        if (c==Coin.BG)
            return new Point2D(xCentre()-0.5d*largeur.get()*cos_theta+0.5d*hauteur.get()*sin_theta,
                    yCentre()-0.5d*largeur.get()*sin_theta-0.5d*hauteur.get()*cos_theta) ;

        return new Point2D(xCentre()+0.5d*largeur.get()*cos_theta+0.5d*hauteur.get()*sin_theta,
                yCentre()+0.5d*largeur.get()*sin_theta-0.5d*hauteur.get()*cos_theta) ;

    }

    private Point2D[] coins() {
        double theta = Math.toRadians(orientation()) ;
        double cos_theta = Math.cos(theta) ;
        double sin_theta = Math.sin(theta) ;

        Point2D coins[] = new Point2D[4] ;

        // On part du coin Haut Droit, abstraction faite de l'orientation du rectangle, et on tourne dans le sens trigo

        coins[0] = new Point2D(xCentre()+0.5d*largeur.get()*cos_theta-0.5d*hauteur.get()*sin_theta,
                yCentre()+0.5d*largeur.get()*sin_theta+0.5d*hauteur.get()*cos_theta) ;
        coins[1] = new Point2D(xCentre()-0.5d*largeur.get()*cos_theta-0.5d*hauteur.get()*sin_theta,
                yCentre()-0.5d*largeur.get()*sin_theta+0.5d*hauteur.get()*cos_theta) ;
        coins[2] = new Point2D(xCentre()-0.5d*largeur.get()*cos_theta+0.5d*hauteur.get()*sin_theta,
                yCentre()-0.5d*largeur.get()*sin_theta-0.5d*hauteur.get()*cos_theta) ;
        coins[3] = new Point2D(xCentre()+0.5d*largeur.get()*cos_theta+0.5d*hauteur.get()*sin_theta,
                yCentre()+0.5d*largeur.get()*sin_theta-0.5d*hauteur.get()*cos_theta) ;

        return coins ;

    }

    @Override
    public  boolean contient(Point2D p) {

        boolean dans_rect = false ;

        Point2D p_dans_ref_oriente = point_dans_ref_oriente(p) ;

        double x_or = p_dans_ref_oriente.getX() ;
        double y_or = p_dans_ref_oriente.getY() ;

        double larg = largeur.get() ;
        double haut = hauteur.get() ;

        if (-larg/2d <= x_or && x_or <= larg/2d && -haut/2d <= y_or && y_or <= haut/2d)
            dans_rect = true ;

        if (typeSurface()==TypeSurface.CONVEXE)
            return dans_rect || this.aSurSaSurface(p)  ;
        else
            return (!dans_rect) || this.aSurSaSurface(p) ;

    }

    @Override
    public  boolean aSurSaSurface(Point2D p) {

        Point2D p_dans_ref_oriente = point_dans_ref_oriente(p) ;

        double x_or = p_dans_ref_oriente.getX() ;
        double y_or = p_dans_ref_oriente.getY() ;

        double larg = largeur.get() ;
        double haut = hauteur.get() ;

        if ( ( Environnement.quasiEgal(-larg/2d,x_or)||Environnement.quasiEgal(larg/2d,x_or) )
            && (-haut/2d<=y_or && y_or<= haut/2d) )
                return true ;

        if ( ( Environnement.quasiEgal(-haut/2d,y_or)||Environnement.quasiEgal(haut/2d,y_or) )
            &&  (-larg/2d<=x_or && x_or<= larg/2d) )
                return true ;

        return false ;
    }

    private Point2D point_dans_ref_oriente(Point2D p) {
        Point2D vec_centre_p = p.subtract(centre()) ;

        double angle_avec_vec_dir = Math.atan2(vec_centre_p.getY(),vec_centre_p.getX()) - Math.atan2(vecteur_directeur().getY(),vecteur_directeur().getX()) ;

        double dist_centre_p = vec_centre_p.magnitude() ;

        double x_proj_sur_dir = dist_centre_p*Math.cos(angle_avec_vec_dir) ;
        double y_proj_sur_dir = dist_centre_p*Math.sin(angle_avec_vec_dir) ;

        return new Point2D(x_proj_sur_dir,y_proj_sur_dir) ;
    }

    private double produit_vectoriel_simplifie(Point2D v1, Point2D v2) {
        return (v1.getX()*v2.getY()-v1.getY()*v2.getX()) ;
    }

    @Override
    public Point2D normale(Point2D p) throws Exception {

        if (!this.aSurSaSurface(p))
            throw new Exception("Impossible de trouver la normale d'un point qui n'est pas sur la surface du rectangle.") ;

        Point2D p_dans_ref_oriente = point_dans_ref_oriente(p) ;

        double x_or = p_dans_ref_oriente.getX() ;
        double y_or = p_dans_ref_oriente.getY() ;

        double larg = largeur.get() ;
        double haut = hauteur.get() ;

        Point2D norm = null ;

        double cos_theta = Math.cos(Math.toRadians(orientation())) ;
        double sin_theta = Math.sin(Math.toRadians(orientation())) ;

        if (Environnement.quasiEgal(-larg/2d,x_or))
            norm = new Point2D(-cos_theta,-sin_theta) ;
        else if (Environnement.quasiEgal(larg/2d,x_or))
            norm = new Point2D(cos_theta,sin_theta) ;
        else if (Environnement.quasiEgal(-haut/2d , y_or))
            norm = new Point2D(sin_theta,-cos_theta) ;
        else if (Environnement.quasiEgal(haut/2d , y_or))
            norm = new Point2D(-sin_theta,cos_theta) ;

        if (norm == null)
            throw new Exception("Erreur lors du calcul de la normale.") ;

        if (typeSurface()==TypeSurface.CONCAVE)
            return norm.multiply(-1.0) ;

        return norm ;
    }

    @Override
    public Point2D cherche_intersection(Rayon r, ModeRecherche mode) {

        return cherche_intersection_avec_demidroite_ou_segment(r.supportGeometrique(),mode) ;

//        System.out.println("Intersection du rayon "+r+" avec rectangle "+this+" : "+inte);
    }

    public Point2D cherche_intersection_avec_demidroite_ou_segment(DemiDroiteOuSegment dd_ou_s, ModeRecherche mode) {

        Point2D[] intersections = cherche_toutes_intersections_avec_demidroite_ou_segment(dd_ou_s) ;

//        int n = intersections.length;
//        System.out.println(n+" intersections avec "+this+" : "+(n>0?intersections[0]:"-")+" , "+(n>1?intersections[1]:"-"));

        if (intersections.length == 0)
            return null ;

        if (intersections.length == 1) {
            if (!Environnement.quasiConfondus(intersections[0],dd_ou_s.depart()))
                return intersections[0];
            else
                return null;
        }

        // Il y a deux intersections

        if (mode==ModeRecherche.PREMIERE && !Environnement.quasiConfondus(intersections[0],dd_ou_s.depart()))
            return intersections[0] ;

        return intersections[1] ;
    }

    /**
     * @param dd_ou_s
     * @return toutes les intersections (0, 1 ou 2) du rectangle avec une demi-droite ou un segment, classées de la plus proche du
     * point de départ de la demi-droite ou du segment, à la plus éloignée.
     */
    public Point2D[] cherche_toutes_intersections_avec_demidroite_ou_segment(DemiDroiteOuSegment dd_ou_s) {

        Point2D[] intersections  ;

        Point2D coins[] = coins() ;

        Point2D p_inter = null ;
        Point2D p1 = null  ;
        Point2D p2 = null ;

        DemiDroiteOuSegment bord_haut = DemiDroiteOuSegment.construireSegment(coins[0],coins[1]) ;

        p_inter = dd_ou_s.intersectionAvec(bord_haut) ;
        if (p_inter!=null) p1=p_inter ;

        DemiDroiteOuSegment bord_gauche = DemiDroiteOuSegment.construireSegment(coins[1],coins[2]) ;
        p_inter = dd_ou_s.intersectionAvec(bord_gauche) ;
        if (p_inter!=null) {
            if (p1==null)
                p1 = p_inter ;
            else
                p2 = p_inter ;
        }

        if (p2==null) {
            DemiDroiteOuSegment bord_bas = DemiDroiteOuSegment.construireSegment(coins[2], coins[3]);
            p_inter = dd_ou_s.intersectionAvec(bord_bas);
            if (p_inter != null) {
                if (p1 == null)
                    p1 = p_inter ;
                else
                    p2 = p_inter;
            }
        }

        if (p2==null) {
            DemiDroiteOuSegment bord_droit = DemiDroiteOuSegment.construireSegment(coins[3], coins[0]);
            p_inter = dd_ou_s.intersectionAvec(bord_droit);
            if (p_inter != null) {
                if (p1 == null)
                    p1 = p_inter ;
                else
                    p2 = p_inter;
            }
        }

        if (p1==null && p2==null) {
            return new Point2D[0] ;
        }

        if (p2==null) {

            intersections = new Point2D[1] ;

            intersections[0]=p1 ;

            return intersections;
        }

        // Il y a deux intersections
        intersections = new Point2D[2] ;

        if (p2.subtract(dd_ou_s.depart()).magnitude()>p1.subtract(dd_ou_s.depart()).magnitude()) {
            intersections[0] = p1 ;
            intersections[1] = p2 ;
        } else {
            intersections[0] = p2 ;
            intersections[1] = p1 ;
        }

        return intersections ;

    }

    public boolean aSymetrieDeRevolution() {return true ;}

    @Override
    public Point2D pointSurAxeRevolution() {
        return centre();
    }

    @Override
    public boolean estOrientable() {
        return true ;
    }

    @Override
    public void tournerAutourDe(Point2D centre_rot, double angle_rot_deg) {
        Rotate r = new Rotate(angle_rot_deg,centre_rot.getX(),centre_rot.getY()) ;

        Point2D nouveau_centre = r.transform(centre()) ;

//        // Très important : mettre à jour l'orientation en premier
//        orientation.set(orientation.get()+angle_rot_deg);
//
//        x_centre.set(nouveau_centre.getX());
//        y_centre.set(nouveau_centre.getY());

        position_orientation.set(new PositionEtOrientation(nouveau_centre,orientation()+angle_rot_deg));

    }

    @Override
    public void definirOrientation(double orientation_deg)  {
        position_orientation.set(new PositionEtOrientation(centre(),orientation_deg));
//        this.orientation.set(orientation_deg);
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
    public Double rayonDiaphragmeParDefaut() {
        return hauteur.get()*0.5d;
    }

    @Override
    public double rayonDiaphragmeMaximumConseille() {
        return hauteur.get();
    }

    @Override
    public Double abscissePremiereIntersectionSurAxe(Point2D origine_axe, Point2D direction_axe, double z_depart,boolean sens_z_croissants, Double z_inter_prec) {

        double z_centre ;

        if (centre().subtract(origine_axe).dotProduct(direction_axe)>=0)
            z_centre = centre().distance(origine_axe) ;
        else
            z_centre = -centre().distance(origine_axe) ;

        double demi_largeur = largeur.get()*0.5d ;

        double z_int_min = z_centre - demi_largeur ;
        double z_int_max = z_centre + demi_largeur ;

        // S'assurer de ne pas retourner à nouveau l'intersection z_inter_prec
        if (z_inter_prec!=null) {
            if (z_int_min==z_inter_prec)
                return (sens_z_croissants?z_int_max:null) ;
            if (z_int_max==z_inter_prec)
                return (sens_z_croissants?null:z_int_min) ;
        }

//        // Cas particuliers où le point de départ est sur une des intersections
//        if (Environnement.quasiEgal(z_depart,z_int_min))
//            return (sens_z_croissants?z_int_max:null) ;
//        if (Environnement.quasiEgal(z_depart,z_int_max))
//            return (sens_z_croissants?null:z_int_min) ;

        if (z_depart==z_int_min) return z_int_min ;
        if (z_depart==z_int_max) return z_int_max ;

        // Cas général
        if (z_depart<z_int_min)
            return (sens_z_croissants?z_int_min:null) ;
        else if (z_int_min<z_depart && z_depart<z_int_max)
            return (sens_z_croissants?z_int_max:z_int_min) ;
        else // z_depart>z_int_max
            return (sens_z_croissants?null:z_int_max) ;

    }

    @Override
    public ArrayList<Double> abscissesToutesIntersectionsSurAxe(Point2D origine_axe, Point2D direction_axe, double z_depart, boolean sens_z_croissants, Double z_inter_prec) {

        ArrayList<Double> resultat = new ArrayList<>(2) ;

        double z_centre ;

        if (centre().subtract(origine_axe).dotProduct(direction_axe)>=0)
            z_centre = centre().distance(origine_axe) ;
        else
            z_centre = -centre().distance(origine_axe) ;

        double demi_largeur = largeur.get()*0.5d ;

        double z_int_min = z_centre - demi_largeur ;
        double z_int_max = z_centre + demi_largeur ;

        // S'assurer de ne pas retourner à nouveau l'intersection z_inter_prec
        if (z_inter_prec!=null) {
            if (z_int_min==z_inter_prec) {
                if (sens_z_croissants) resultat.add(z_int_max);
                return resultat ;
            }
            if (z_int_max==z_inter_prec) {
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
            if (demi_largeur!=0d) resultat.add(z_int_max) ;

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
            if (demi_largeur!=0d) resultat.add(z_int_min) ;

        }

        return resultat ;

    }
}
