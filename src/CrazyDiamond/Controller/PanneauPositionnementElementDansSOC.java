package CrazyDiamond.Controller;

import CrazyDiamond.Model.ChangeListenerAvecGarde;
import CrazyDiamond.Model.CommandeDefinirUnParametrePoint;
import CrazyDiamond.Model.ElementDeSOC;
import CrazyDiamond.Model.PositionEtOrientation;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.Spinner;

public class PanneauPositionnementElementDansSOC {

    @FXML
    private Spinner<Double> spinner_position_dans_soc ;
    ElementDeSOC element_dans_soc;

    private CanvasAffichageEnvironnement canvas ;
    private boolean actualisation_position_relative_en_cours;

    public PanneauPositionnementElementDansSOC() { }

    public void initialize(CanvasAffichageEnvironnement cae,ElementDeSOC element_dans_soc) {

        this.canvas = cae ;
        this.element_dans_soc = element_dans_soc ;

        // Prise en compte automatique de la position et de l'orientation de l'ElementDeSoc
        if (element_dans_soc.positionEtOrientationProperty()!=null)
            element_dans_soc.positionEtOrientationProperty().addListener(new ChangeListenerAvecGarde<>(this::prendreEnComptePositionEtOrientation));
        else if (element_dans_soc.positionProperty()!=null)
            element_dans_soc.positionProperty().addListener(new ChangeListenerAvecGarde<>(this::prendreEnComptePosition));


        // Si un SOC Parent est affecté à l'élément, il faut assurer l'initialisation de la valeur du spinner : c'est
        // nécessaire car, quand un élément est ajouté à un SOC, il est positionné sur l'axe de ce SOC *avant* qu'on lui
        // affecte son SOC Parent. On ne peut donc pas se contenter des Listeners sur la position et l'orientation
        // (ci-dessus) pour assurer cette initialisation (le calcul de la position relative dans le SOC parent nécessite
        // de connaître ce SOC Parent).
        element_dans_soc.systemeOptiqueParentProperty().addListener((observableValue, oldValue, newValue) -> {
            if (oldValue == null && newValue != null) { // Ajout de ce SOC dans un SOC parent
                if (element_dans_soc.positionEtOrientationProperty()!=null)
                    prendreEnComptePositionEtOrientation(element_dans_soc.positionEtOrientationProperty().get());
                else if (element_dans_soc.positionProperty()!=null)
                    prendreEnComptePosition(element_dans_soc.positionProperty().get());
            } // Pas de 'else' car rien à faire si retrait de ce SOC d'un SOC Parent
        });

        // Position de l'élément sur l'axe du SOC parent
        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_position_dans_soc,
                element_dans_soc.positionDansSOCParent(), this::definirPositionDansSOCParent);

        spinner_position_dans_soc.getStyleClass().add(Spinner.STYLE_CLASS_ARROWS_ON_RIGHT_HORIZONTAL) ;
    }

    private void definirPositionDansSOCParent(double z) {

        if (actualisation_position_relative_en_cours)
            return;

        if (element_dans_soc.sansSOCParentActif())
            return;

        Point2D nouvelle_origine = element_dans_soc.SOCParent().origine().add(element_dans_soc.SOCParent().direction().multiply(z)) ;
        new CommandeDefinirUnParametrePoint<>(element_dans_soc,nouvelle_origine,
                element_dans_soc::pointDeReferencePourPositionnementDansSOCParent,
                element_dans_soc::definirPointDeReferencePourPositionnementDansSOCParent).executer();
    }

    private void prendreEnComptePositionEtOrientation(PositionEtOrientation nouvelle_pos_et_or) {
        if (element_dans_soc.sansSOCParentActif())
            return;
        actualisation_position_relative_en_cours = true ;
        spinner_position_dans_soc.getValueFactory().valueProperty().set(nouvelle_pos_et_or.position().subtract(element_dans_soc.SOCParent().origine()).dotProduct(element_dans_soc.SOCParent().direction()));
        actualisation_position_relative_en_cours = false ;
    }

    private void prendreEnComptePosition(Point2D nouvelle_pos) {
        if (element_dans_soc.sansSOCParentActif())
            return;
        actualisation_position_relative_en_cours = true ;
        spinner_position_dans_soc.getValueFactory().valueProperty().set(nouvelle_pos.subtract(element_dans_soc.SOCParent().origine()).dotProduct(element_dans_soc.SOCParent().direction()));
        actualisation_position_relative_en_cours = false ;
    }
}
