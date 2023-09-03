package CrazyDiamond.Model;

public interface VisiteurElementAvecMatiere {

    void visiteCercle(Cercle c) ;
    void visiteRectangle(Rectangle r) ;
    void visiteConique(Conique c) ;
    void visiteParabole(Parabole p) ;
    void visiteDemiPlan(DemiPlan dp) ;
    void visitePrisme(Prisme prisme);

    void visiteComposition(Composition c) ;
    void visiteCompositionDeuxObstacles(CompositionDeuxObstacles c) ;


}
