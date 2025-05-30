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

    public static void remplacerOuAjouterNoeudFils(Pane pere, Node fils_a_remplacer, Node nouveau_fils,int pos_ajout) {
        int pos = pere.getChildren().indexOf(fils_a_remplacer) ;

        if (pos!= (-1)) {
            supprimerNoeudFils(pere, fils_a_remplacer);
//            if (pos == -1)
//                System.out.println("Oh la la");
            if (nouveau_fils!=null)
                pere.getChildren().add(pos, nouveau_fils);
        }
        else {
            if (nouveau_fils!=null)
                pere.getChildren().add(pos_ajout, nouveau_fils);
        }
    }

    public static void supprimerNoeudFils(Pane pere, Node fils_a_supprimer) {
        if (fils_a_supprimer!=null)
            pere.getChildren().remove(fils_a_supprimer);
    }

    public static void gererAppartenanceSOC(ElementDeSOC el,Pane pere,Node panneau_pos_abs, Node panneau_pos_rel_soc ) {

        int pos_pour_ajout = pere.getChildren().indexOf((panneau_pos_abs!=null)?panneau_pos_abs:panneau_pos_rel_soc) ;

        if (el.sansSOCParentActif())
            supprimerNoeudFils(pere,panneau_pos_rel_soc);
        else {
                supprimerNoeudFils(pere, panneau_pos_abs);
            if (el.SOCParent().referenceDirectement(el))
                supprimerNoeudFils(pere, panneau_pos_rel_soc);
        }

        el.systemeOptiqueParentProperty().addListener((observableValue, oldValue, newValue) -> {
            LOGGER.log(Level.FINE, "SOC Parent passe de {0} à {1}", new Object[]{oldValue, newValue});

            if (oldValue == null && newValue != null ) { // Ajout de cet élément dans un SOC parent
                if (newValue.referenceDirectement(el))
                    remplacerOuAjouterNoeudFils(pere, panneau_pos_abs, panneau_pos_rel_soc,pos_pour_ajout);
                else
                    supprimerNoeudFils(pere,panneau_pos_abs);
            } else if (oldValue != null && newValue == null) { // Retrait de cet élément d'un SOC Parent
                    remplacerOuAjouterNoeudFils(pere, panneau_pos_rel_soc, panneau_pos_abs,pos_pour_ajout);
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
