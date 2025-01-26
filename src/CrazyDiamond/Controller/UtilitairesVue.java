package CrazyDiamond.Controller;

import CrazyDiamond.Model.*;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class UtilitairesVue {
    private static final Logger LOGGER = Logger.getLogger( "CrazyDiamond" );

    private UtilitairesVue() { }

    public static void remplacerNoeudFils(Pane pere, Node fils_a_remplacer, Node nouveau_fils) {
        int pos = pere.getChildren().indexOf(fils_a_remplacer) ;
        supprimerNoeudFils(pere,fils_a_remplacer);
        pere.getChildren().add(pos, nouveau_fils);
    }

    public static void supprimerNoeudFils(Pane pere, Node fils_a_supprimer) {
        pere.getChildren().remove(fils_a_supprimer);
    }
    public static void gererAppartenanceSOC(ElementDeSOC el,Pane pere,Node panneau_pos_abs, Node panneau_pos_rel_soc ) {

        if (el.SOCParent() != null) {

            supprimerNoeudFils(pere, panneau_pos_abs);
//            int pos = vbox_panneau_racine.getChildren().indexOf(vbox_positionnement_absolu) ;
//            vbox_panneau_racine.getChildren().remove(vbox_positionnement_absolu);
//            vbox_panneau_racine.getChildren().add(pos, panneau_positionnement_element_dans_soc);

            // Force le spinner du panneau positionnement à s'initialiser
            // TODO : voir si c'est vraiment nécessaire
            el.definirPointDeReferencePourPositionnementDansSOCParent(el.pointDeReferencePourPositionnementDansSOCParent());
        } else
            supprimerNoeudFils(pere,panneau_pos_rel_soc);


        el.systemeOptiqueParentProperty().addListener((observableValue, oldValue, newValue) -> {
            LOGGER.log(Level.FINE, "SOC Parent passe de {0} à {1}", new Object[]{oldValue, newValue});

            if (oldValue == null && newValue != null) { // Ajout de ce SOC dans un SOC parent

                remplacerNoeudFils(pere, panneau_pos_abs, panneau_pos_rel_soc);

//                int pos = vbox_panneau_racine.getChildren().indexOf(vbox_positionnement_absolu) ;
//                vbox_panneau_racine.getChildren().remove(vbox_positionnement_absolu);
//                vbox_panneau_racine.getChildren().add(pos, panneau_positionnement_element_dans_soc);

            } else if (oldValue != null && newValue == null) { // Retrait de ce SOC d'un SOC Parent

                remplacerNoeudFils(pere, panneau_pos_rel_soc, panneau_pos_abs);

//                int pos = vbox_panneau_racine.getChildren().indexOf(panneau_positionnement_element_dans_soc) ;
//                vbox_panneau_racine.getChildren().remove(panneau_positionnement_element_dans_soc);
//                vbox_panneau_racine.getChildren().add(pos,vbox_positionnement_absolu);

            }

        });

    }

    public static void gererAppartenanceComposition(boolean dans_composition, Obstacle obs, Parent baseContour, PanneauElementAvecContour baseContourController, Parent baseMatiere, PanneauElementAvecMatiere baseMatiereController) {
        if (dans_composition) {
            baseContour.setVisible(false);
            if (baseMatiere!=null) // Certains obstacles n'ont pas de matière (ex : Segment)
                baseMatiere.setVisible(false);

        } else {
            baseContourController.initialize((ElementAvecContour) obs);
            if (baseMatiereController!=null) // Certains obstacles n'ont pas de matière (ex : Segment)
                baseMatiereController.initialize((ElementAvecMatiere) obs);
        }
    }

}
