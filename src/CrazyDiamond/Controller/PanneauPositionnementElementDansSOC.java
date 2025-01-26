package CrazyDiamond.Controller;

import CrazyDiamond.Model.*;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.Spinner;

public class PanneauPositionnementElementDansSOC {

    @FXML
    private Spinner<Double> spinner_position_dans_soc ;
    ElementDeSOC element_dans_soc;

    private CanvasAffichageEnvironnement canvas ;

    public PanneauPositionnementElementDansSOC() { } ;

    public void initialize(CanvasAffichageEnvironnement cae,ElementDeSOC element_dans_soc) {

        this.canvas = cae ;
        this.element_dans_soc = element_dans_soc ;

        // Prise en compte automatique de la position et de l'orientation
        if (element_dans_soc.positionEtOrientationProperty()!=null)
            element_dans_soc.positionEtOrientationProperty().addListener(new ChangeListenerAvecGarde<>(this::prendreEnComptePositionEtOrientation));
        else if (element_dans_soc.positionProperty()!=null)
            element_dans_soc.positionProperty().addListener(new ChangeListenerAvecGarde<>(this::prendreEnComptePosition));

        // Position de l'élément sur l'axe du SOC parent
        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_position_dans_soc,
                element_dans_soc.positionDansSOCParent(), this::definirPositionDansSOCParent);

        spinner_position_dans_soc.getStyleClass().add(Spinner.STYLE_CLASS_ARROWS_ON_RIGHT_HORIZONTAL) ;
    }

    private void definirPositionDansSOCParent(double z) {
        Point2D nouvelle_origine = element_dans_soc.SOCParent().origine().add(element_dans_soc.SOCParent().direction().multiply(z)) ;
        new CommandeDefinirUnParametrePoint<>(element_dans_soc,nouvelle_origine,
                element_dans_soc::pointDeReferencePourPositionnementDansSOCParent,
                element_dans_soc::definirPointDeReferencePourPositionnementDansSOCParent).executer();
    }

    private void prendreEnComptePositionEtOrientation(PositionEtOrientation nouvelle_pos_et_or) {
        if (element_dans_soc.SOCParent()==null)
            return;
        spinner_position_dans_soc.getValueFactory().valueProperty().set(nouvelle_pos_et_or.position().subtract(element_dans_soc.SOCParent().origine()).dotProduct(element_dans_soc.SOCParent().direction()));
    }

    private void prendreEnComptePosition(Point2D nouvelle_pos) {
        if (element_dans_soc.SOCParent()==null)
            return;
        spinner_position_dans_soc.getValueFactory().valueProperty().set(nouvelle_pos.subtract(element_dans_soc.SOCParent().origine()).dotProduct(element_dans_soc.SOCParent().direction()));
    }
}
