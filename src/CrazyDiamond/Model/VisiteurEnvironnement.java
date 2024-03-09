package CrazyDiamond.Model;

public interface VisiteurEnvironnement {

    void avantVisiteEnvironnement(Environnement e) ;
    void apresVisiteEnvironnement(Environnement e) ;

    void visiteSource(Source s) ;

    void visiteSegment(Segment e) ;
    void visiteCercle(Cercle c) ;
    void visiteRectangle(Rectangle r) ;
    void visiteConique(Conique c) ;
    void visiteDemiPlan(DemiPlan dp) ;
    void visitePrisme(Prisme prisme);

    void visiteComposition(Composition c) ;
    void visiteCompositionDeuxObstacles(CompositionDeuxObstacles c) ;

    default void visiteGroupe(Groupe g) { }

    void visiteSystemeOptiqueCentre(SystemeOptiqueCentre soc) ;

    default void avantVisiteSystemesOptiquesCentres() { }
    default void apresVisiteSystemesOptiquesCentres() { }

    default void avantVisiteObstacles() { }
    default void apresVisiteObstacles() { }

    default void avantVisiteSources() { }
    default void apresVisiteSources() { }

}
