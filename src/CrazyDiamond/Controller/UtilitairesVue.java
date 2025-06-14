package CrazyDiamond.Controller;

import CrazyDiamond.Model.ElementAvecContour;
import CrazyDiamond.Model.ElementAvecMatiere;
import CrazyDiamond.Model.ElementDeSOC;
import CrazyDiamond.Model.Obstacle;
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

    public static void gererAppartenanceSOC(ElementDeSOC el,PanneauElementAvecContour panneau_contour ,  Pane pere, Node panneau_pos_abs, Node panneau_pos_rel_soc ) {

        // Gestion de la bascule entre positionnement relatif et positionnement absolu, selon que l'élément est ou non
        // dans un SOC;

        int pos_pour_ajout = pere.getChildren().indexOf((panneau_pos_abs!=null)?panneau_pos_abs:panneau_pos_rel_soc) ;

        if (el.sansSOCParentActif())
            supprimerNoeudFils(pere,panneau_pos_rel_soc);
        else
            supprimerNoeudFils(pere, panneau_pos_abs);

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

        // Gestion de la possibilité de rendre l'élément réfléchissant : ce n'est pas possible si l'élément est dans un
        // sous-SOC, mais c'est possible s'il son SOC parent est un SOC racine, ou s'il n'appartient pas à un SOC

        if (panneau_contour!=null) {

            // Si on nous a passé un panneau_contour, el est forcément un Obstacle (car un SOC n'a pas de contour)

            if (el.SOCParent() != null) {

                // Il faut mettre sous-surveillance le grand pere
                el.SOCParent().systemeOptiqueParentProperty().addListener(panneau_contour.changeListenerGrandParent()) ;

                if (el.SOCParent().SOCParent() != null)  // Element fait partie d'un sous-SOC
                    panneau_contour.interdireChoixSurfaceReflechissante();
            }

            el.systemeOptiqueParentProperty().addListener((observableParentSOC, oldParentSOC, newParentSOC) -> {
                LOGGER.log(Level.FINE, "SOC Parent passe de {0} à {1}", new Object[]{oldParentSOC, newParentSOC});

                if (oldParentSOC == null && newParentSOC != null) { // Ajout de cet élément dans un SOC parent

                    if (newParentSOC.SOCParent() != null)
                        panneau_contour.interdireChoixSurfaceReflechissante();
                    else
                        panneau_contour.autoriserChoixSurfaceReflechissante();

                    // Il faut aussi mettre sous-surveillance le nouveau grand pere
                    newParentSOC.systemeOptiqueParentProperty().addListener(panneau_contour.changeListenerGrandParent()) ;

                } else if (oldParentSOC != null && newParentSOC == null) { // Retrait de cet élément d'un SOC Parent

                    panneau_contour.autoriserChoixSurfaceReflechissante();

                    oldParentSOC.systemeOptiqueParentProperty().removeListener(panneau_contour.changeListenerGrandParent());

                } else { // oldParentSOC != null && newParentSOC != null (ou alors ils sont null tous les deux) : on passe d'un SOC Parent à un autre
                    if (oldParentSOC!=null)
                        oldParentSOC.systemeOptiqueParentProperty().removeListener(panneau_contour.changeListenerGrandParent());
                    if (newParentSOC!=null)
                        newParentSOC.systemeOptiqueParentProperty().addListener(panneau_contour.changeListenerGrandParent());

                }
            });

        }


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
