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
        //  Si l'élément de SOC est un élément simple (ni une Composition, ni un Groupe) alors un changement de ses propriétés (rayon, largeur,
        // paramètre, excentricité, etc.) entraine toujours un recalage de la position de son centre/foyer donc de sa position : il suffit
        // donc "d'écouter" les changements de cette position pour pouvoir l'afficher dans le spinner "Pos dans SOC"
        // Pour les Compositions et les Groupes qui n'ont pas de centre etd onc , pas non plus de position, il faut écouter tous les changements
        // de propriété puis calculer la position du point de réf qui a pu être modifié du fait de ces changements de propriétés.
        if (element_dans_soc.positionEtOrientationProperty()!=null)
            element_dans_soc.positionEtOrientationProperty().addListener(new ChangeListenerAvecGarde<>( (po) -> {prendreEnComptePositionEtOrientation() ; }));
        else if (element_dans_soc.positionProperty()!=null)
            element_dans_soc.positionProperty().addListener(new ChangeListenerAvecGarde<>( (pos) -> { prendreEnComptePositionEtOrientation() ; } ));
        else { // Pas de proppriété Position ou PositionEtOrientation : cas des Composites (Groupe ou Composition)
            // Déclencher un re-calcul de la position dans le SOC Parent dès qu'un attribut ou un élément de l'obstacle (ou de ses
            // sous_obstacles) change. Si o est un Composite on surveille aussi les ajouts/retraits dans tous ses éventuels
            // sous-composites
            element_dans_soc.ajouterRappelSurChangementToutePropriete(this,this::prendreEnComptePositionEtOrientation);

        }


        // Si un SOC Parent est affecté à l'élément, il faut assurer l'initialisation de la valeur du spinner : c'est
        // nécessaire car, quand un élément est ajouté à un SOC, il est positionné sur l'axe de ce SOC *avant* qu'on lui
        // affecte son SOC Parent. On ne peut donc pas se contenter des Listeners sur la position et l'orientation
        // (ci-dessus) pour assurer cette initialisation (le calcul de la position relative dans le SOC parent nécessite
        // de connaître ce SOC Parent).
        element_dans_soc.systemeOptiqueParentProperty().addListener((observableValue, oldValue, newValue) -> {
            if (oldValue == null && newValue != null) { // Ajout de ce SOC dans un SOC parent
                prendreEnComptePositionEtOrientation() ;
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

    private void prendreEnComptePositionEtOrientation() {
        if (element_dans_soc.sansSOCParentActif())
            return;
        actualisation_position_relative_en_cours = true ;
        spinner_position_dans_soc.getValueFactory().valueProperty().set(element_dans_soc.pointDeReferencePourPositionnementDansSOCParent().subtract(element_dans_soc.SOCParent().origine()).dotProduct(element_dans_soc.SOCParent().direction()));
        actualisation_position_relative_en_cours = false ;
    }

    private void prendreEnComptePositionEtOrientation(PositionEtOrientation nouvelle_pos_et_or) {
        prendreEnComptePositionEtOrientation();
    }

    private void prendreEnComptePosition(Point2D nouvelle_pos) {
        prendreEnComptePositionEtOrientation();
    }
}
