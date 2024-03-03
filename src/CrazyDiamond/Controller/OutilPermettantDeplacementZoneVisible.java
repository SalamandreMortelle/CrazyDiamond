package CrazyDiamond.Controller;

import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;

//  Les classes qui héritent doivent appeler les méthodes implémentées ici (par ex : super.traiterBoutonSourisPresse(me);)
public class OutilPermettantDeplacementZoneVisible extends Outil {

    private Point2D p_debut_glisser;

    public OutilPermettantDeplacementZoneVisible(CanvasAffichageEnvironnement cae) {
        super(cae);
    }

    @Override
    public void prendre() {
        super.prendre();
    }

    @Override
    public void traiterClicSourisCanvas(MouseEvent me) {
    }

    @Override
    public void traiterBoutonSourisPresse(MouseEvent mouseEvent) {
        // C'est peut-être le début d'un glisser de souris : enregistrons la position de début de glisser
        p_debut_glisser = new Point2D(mouseEvent.getX(),mouseEvent.getY()) ;

    }

    @Override
    public void traiterGlisserSourisCanvas(MouseEvent mouseEvent) {

        cae.getScene().setCursor(Cursor.MOVE);

        Point2D p_fin_glisser = new Point2D(mouseEvent.getX(),mouseEvent.getY());

        Point2D p_debut_glisser_g = cae.gc_vers_g(p_debut_glisser.getX(),p_debut_glisser.getY());
        Point2D p_fin_glisser_g   = cae.gc_vers_g(p_fin_glisser.getX(),p_fin_glisser.getY());

        Point2D v_glisser_g = p_fin_glisser_g.subtract(p_debut_glisser_g) ;

        // La position actuelle de la souris devient le nouveau point de depart pour la suite du glisser
        p_debut_glisser = p_fin_glisser ;

        cae.translaterLimites(v_glisser_g.getX(),v_glisser_g.getY());

        cae.rafraichirAffichage();

    }

    @Override
    public void traiterBoutonSourisRelacheFinGlisser(MouseEvent mouseEvent) {

        cae.getScene().setCursor(curseur_souris);

//        Point2D p_fin_glisser = new Point2D(mouseEvent.getX(),mouseEvent.getY());

        Point2D p_debut_glisser_g = cae.gc_vers_g(p_debut_glisser.getX(),p_debut_glisser.getY());
//        Point2D p_fin_glisser_g   = cae.gc_vers_g(p_fin_glisser.getX(),p_fin_glisser.getY());
        Point2D p_fin_glisser_g   = cae.gc_vers_g(mouseEvent.getX(),mouseEvent.getY());

        Point2D v_glisser_g = p_fin_glisser_g.subtract(p_debut_glisser_g) ;

//        // Etait-on en train de déplacer un obstacle sélectionné ?
//        if (modeCourant() == selection && cae.selection().nombreElements() >0 ) {
//            cae.translaterSelection(v_glisser_g);
//        } else

//        if (modeCourant()!=selection || cae.selection().nombreElements() == 0)
//        { // Sinon, aucun élément n'était sélectionné : on était en train de déplacer la zone visible
        cae.translaterLimites(v_glisser_g.getX(), v_glisser_g.getY());
        cae.rafraichirAffichage();
//        }

    }
}
