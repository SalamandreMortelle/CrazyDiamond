package CrazyDiamond.Model;

import javafx.beans.property.*;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;

import java.io.IOException;

public class Parabole implements Obstacle, Identifiable, Nommable,ElementAvecContour,ElementAvecMatiere {

    private final Imp_Identifiable imp_identifiable ;
    private final Imp_Nommable imp_nommable;
    private final Imp_ElementAvecContour imp_elementAvecContour ;
    private final Imp_ElementAvecMatiere imp_elementAvecMatiere ;

    protected final DoubleProperty a ;
    protected final DoubleProperty b ;
    protected final DoubleProperty c ;

    private static int compteur_parabole ;
    private BooleanProperty appartenance_systeme_optique_centre;

    public Parabole(TypeSurface type_surface, double  coeff_a , Point2D pextremum) throws IllegalArgumentException {

        this(type_surface,coeff_a,-pextremum.getX()*2*coeff_a,pextremum.getY()-3*coeff_a*pextremum.getX()*pextremum.getX()) ;

//        if (coeff_a==0)
//            throw new IllegalArgumentException("Le coefficient a d'une parabole ne peut pas être nul.") ;
//
//        imp_identifiable = new Imp_Identifiable() ;
//        imp_nommable = new Imp_Nommable("Parabole "+(++compteur_parabole));
//        imp_elementAvecContour = new Imp_ElementAvecContour(null) ;
//        imp_elementAvecMatiere = new Imp_ElementAvecMatiere(type_surface,null,1.0,null   );
//
//
//        this.a = new SimpleDoubleProperty(coeff_a);
//        this.b = new SimpleDoubleProperty(-pextremum.getX()*2*coeff_a);
//        this.c = new SimpleDoubleProperty(pextremum.getY()-3*coeff_a*pextremum.getX()*pextremum.getX());
//
//        this.appartenance_systeme_optique_centre = new SimpleBooleanProperty(false) ;

    }

    public Parabole(TypeSurface type_surface, double coeff_a, double coeff_b, double coeff_c) throws IllegalArgumentException {

        this (
                new Imp_Identifiable(),
                new Imp_Nommable("Parabole "+(++compteur_parabole)),
                new Imp_ElementAvecContour(null),
                new Imp_ElementAvecMatiere(type_surface,null,1.0,null),
                coeff_a,coeff_b,coeff_c
        );
    }

    public Parabole(Imp_Identifiable ii,Imp_Nommable in,Imp_ElementAvecContour iec, Imp_ElementAvecMatiere iem, double coeff_a, double coeff_b, double coeff_c) throws IllegalArgumentException {

        if (coeff_a==0)
            throw new IllegalArgumentException("Le coefficient a d'une parabole ne peut pas être nul.") ;

        imp_identifiable = ii ;
        imp_nommable = in ;
        imp_elementAvecContour = iec;
        imp_elementAvecMatiere = iem;

        this.a = new SimpleDoubleProperty(coeff_a);
        this.b = new SimpleDoubleProperty(coeff_b);
        this.c = new SimpleDoubleProperty(coeff_c);

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

    @Override public void definirTauxReflexionSurface(double taux_refl) {imp_elementAvecContour.definirTauxReflexionSurface(taux_refl);}
    @Override public double tauxReflexionSurface() {return imp_elementAvecContour.tauxReflexionSurface();}

    @Override public void definirOrientationAxePolariseur(double angle_pol) {imp_elementAvecContour.definirOrientationAxePolariseur(angle_pol);}
    @Override public double orientationAxePolariseur() {return imp_elementAvecContour.orientationAxePolariseur() ;}
    @Override public DoubleProperty orientationAxePolariseurProperty() {return imp_elementAvecContour.orientationAxePolariseurProperty() ;}

    @Override
    public Double courbureRencontreeAuSommet(Point2D pt_sur_surface, Point2D direction) throws Exception {
        return (direction.dotProduct(normale(pt_sur_surface))<=0d?1/(2d*a.get()):-1/(2d*a.get())) ;
    }


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


    @Override public String toString() { return nom(); }

    @Override
    public void accepte(VisiteurEnvironnement v) {
        v.visiteParabole(this);
    }

    @Override
    public void accepte(VisiteurElementAvecMatiere v) {
        v.visiteParabole(this);
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
        double a_init = a.get() ;
        double b_init = b.get() ;
        b.set(b_init-2*a_init*vecteur.getX()) ;
        c.set(a_init*vecteur.getX()*vecteur.getX()-b_init*vecteur.getX()+c.get()+vecteur.getY());
    }


    @Override
    public void ajouterRappelSurChangementToutePropriete(RappelSurChangement rap) {
        imp_elementAvecContour.ajouterRappelSurChangementToutePropriete(rap);
        imp_elementAvecMatiere.ajouterRappelSurChangementToutePropriete(rap);

        a.addListener((observable, oldValue, newValue) -> { rap.rappel(); });
        b.addListener((observable, oldValue, newValue) -> { rap.rappel(); });
        c.addListener((observable, oldValue, newValue) -> { rap.rappel(); });
    }

    @Override
    public void ajouterRappelSurChangementTouteProprieteModifiantChemin(RappelSurChangement rap) {
        imp_elementAvecContour.ajouterRappelSurChangementTouteProprieteModifiantChemin(rap);
        imp_elementAvecMatiere.ajouterRappelSurChangementTouteProprieteModifiantChemin(rap);


        a.addListener((observable, oldValue, newValue) -> { rap.rappel(); });
        b.addListener((observable, oldValue, newValue) -> { rap.rappel(); });
        c.addListener((observable, oldValue, newValue) -> { rap.rappel(); });

    }

    @Override
    public void retaillerPourSourisEn(Point2D pos_souris)  {
        throw new UnsupportedOperationException("Parabole::retaillerPourSourisEn n'est pas implémenté.") ;
    }

    @Override
    public Contour positions_poignees() {
        Contour c_poignees = new Contour(4);

        // NYI

        return c_poignees;
    }


    protected Point2D extremum() {
        double a = this.a.get() ;
        double b = this.b.get() ;
        double c = this.c.get() ;
        return new Point2D(-b/(2*a),3*b*b/(4*a)+c) ;
    }

    @Override
    public boolean contient(Point2D p) {

        double a = this.a.get() ;
        double b = this.b.get() ;
        double c = this.c.get() ;

        double y = a*p.getX()*p.getX() + b*p.getX() + c ;

        if (typeSurface() == TypeSurface.CONCAVE) {
            if (a > 0)
                return (y >= p.getY()) || this.aSurSaSurface(p) ;
            else
                return (y < p.getY()) || this.aSurSaSurface(p) ;
        }
        else { // CONVEXE
            if (a > 0)
                return (y < p.getY()) || this.aSurSaSurface(p) ;
            else
                return (y >= p.getY()) || this.aSurSaSurface(p) ;
        }

    }

    @Override
    public boolean aSurSaSurface(Point2D p) {

        double a = this.a.get() ;
        double b = this.b.get() ;
        double c = this.c.get() ;

        double y = a*p.getX()*p.getX() + b*p.getX() + c ;

        return Environnement.quasiEgal(y,p.getY()) ;
    }

    @Override
    public Point2D normale(Point2D p) throws Exception {

        if (!this.aSurSaSurface(p))
                throw new Exception("Impossible de trouver la normale d'un point qui n'est pas sur la surface de la parabole.") ;

        double a = this.a.get() ;
        double b = this.b.get() ;
        double c = this.c.get() ;

        if (typeSurface() == TypeSurface.CONCAVE)
            if (a>0)
                return new Point2D(-2*a*p.getX()-b,1).normalize() ;
            else
                return new Point2D(2*a*p.getX()+b,-1).normalize() ;
        else
            if (a>0)
                return new Point2D(2*a*p.getX()+b,-1).normalize() ;
            else
                return new Point2D(-2*a*p.getX()-b,1).normalize() ;
    }

    @Override
    public void tournerAutourDe(Point2D centre_rot, double angle_rot_deg) {

    }

    @Override
    public void definirAppartenanceSystemeOptiqueCentre(boolean b) {this.appartenance_systeme_optique_centre.set(b);}
    @Override
    public boolean appartientASystemeOptiqueCentre() {return this.appartenance_systeme_optique_centre.get() ;}

//    @Override
//    public Double abscissePremiereIntersectionSurAxe(Point2D origine_axe, Point2D direction_axe, double z_depart,boolean sens_z_croissants) {
//        LOGGER.log(Level.SEVERE,"abscissePremiereIntersectionSurAxe pas implémenté dans la classe Parabole");
//        return null ;
//
//    }
    // TODO : Ecrire une implémentation spécifique  de la méthode cherche_toutes_intersections(Rayon r)
    // plus optimisée que l'implémentation par défaut
    // @Override
    // public ArrayList<Point2D> cherche_toutes_intersections(Rayon r)


    @Override
    public Point2D cherche_intersection(Rayon r, ModeRecherche mode) {
//    public Point2D premiere_intersection(Rayon r) {

        if (this.contient(r.depart()))
            if (!this.aSurSaSurface(r.depart()))
                return null ;

        double a = this.a.get() ;
        double b = this.b.get() ;
        double c = this.c.get() ;

        double xdep = r.depart().getX() ;
        double ydep = r.depart().getY() ;

        double xdir = r.direction().getX() ;
        double ydir = r.direction().getY() ;


        // Cas particulier du rayon vertical
        if (r.direction().getX()==0) {
            if ( typeSurface() == TypeSurface.CONCAVE && ( (a>0 && r.direction().getY()<0) || (a<0 && r.direction().getY()>0) ) )
                return new Point2D(xdep,a * xdep * xdep + b * xdep + c  );
            else if ( typeSurface() == TypeSurface.CONVEXE && ( (a>0 && r.direction().getY()>0) || (a<0 && r.direction().getY()<0) ) )
                return new Point2D(xdep,a * xdep * xdep + b * xdep + c  );
            else
                return null ;
        }

        // Cas général (rayon non vertical) : on se ramène à une equation du second degré dont on cherche les racines
        // éventuelles, et si il y en a, on cherche la bonne
        double aeq = a ;
        double beq = (b-ydir/xdir) ;
        double ceq = (c+(ydir/xdir)*xdep-ydep) ;

        double discr = beq*beq - 4*aeq*ceq ;

        if (discr<0)
            return null ;

        double xinter ;
        double yinter ;

        if (discr==0.0) {

            xinter = -beq/(2*aeq) ;

            if ( ( xdir > 0 && (xinter < xdep) ) || ( xdir < 0 && (xinter > xdep) ) )
                return null ;

        } else { // Discriminant positif : renvoyer la bonne racine

            double x1 = (-beq - Math.sqrt(discr)) / (2 * aeq);
            double x2 = (-beq + Math.sqrt(discr)) / (2 * aeq);

            if (typeSurface() ==TypeSurface.CONCAVE) {

                if (!this.contient(r.depart()) || (this.contient(r.depart()) && this.aSurSaSurface(r.depart()))) {
                    if (xdir > 0)
                        xinter = Math.max(x1, x2);
                    else
                        xinter = Math.min(x1, x2);
                } else {  // this.contient && ( !this.contient ||  depart pas sur surface )
                    // On ne passe ici que si le départ est dans l'obstacle (et pas sur sa surface)
                    if (xdir > 0)
                        xinter = (mode == ModeRecherche.PREMIERE) ? Math.min(x1, x2) : Math.max(x1, x2);
                    else
                        xinter = (mode == ModeRecherche.PREMIERE) ? Math.max(x1, x2) : Math.min(x1, x2) ;
                }

            } else { // CONVEXE
                if (!this.contient(r.depart()) || (this.contient(r.depart()) && this.aSurSaSurface(r.depart()))) {
                    if (xdir > 0)
                        xinter = (mode == ModeRecherche.PREMIERE) ? Math.min(x1, x2) : Math.max(x1, x2);
                    else
                        xinter = (mode == ModeRecherche.PREMIERE) ? Math.max(x1, x2) : Math.min(x1, x2);
                } else {  // this.contient && ( !this.contient ||  depart pas sur surface )
                    // On ne passe ici que si le départ est dans l'obstacle (et pas sur sa surface)
                    if (xdir > 0)
                        xinter = Math.max(x1, x2);
                    else
                        xinter = Math.min(x1, x2);
                }

                // Convexe et point de départ sur la surface : pas d'intersection possible
                if (this.aSurSaSurface(r.depart()))
                    return null ;

                if ( ( xdir > 0 && (xinter < xdep) ) || ( xdir < 0 && (xinter > xdep) ) )
                    return null ;
            }
        }

        yinter = a*xinter*xinter+b*xinter+c ;

        return new Point2D(xinter,yinter) ;

    }

    public double a() {return a.get() ;}
    public double b() {return b.get() ;}
    public double c() {return c.get() ;}
}
