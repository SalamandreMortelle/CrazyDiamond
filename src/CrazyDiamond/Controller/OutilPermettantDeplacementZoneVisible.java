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
}
