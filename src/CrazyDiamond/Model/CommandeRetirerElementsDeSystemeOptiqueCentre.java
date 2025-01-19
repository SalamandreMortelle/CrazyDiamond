package CrazyDiamond.Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Retire un ou plusieurs éléments (Obstacles ou SOCs) se trouvant à la racine d'un SOC
 */
public class CommandeRetirerElementsDeSystemeOptiqueCentre extends Commande {

    // Le récepteur de la commande
    SystemeOptiqueCentre soc;

    // Parametre
    List<ElementDeSOC> elements_a_retirer;

    public CommandeRetirerElementsDeSystemeOptiqueCentre(SystemeOptiqueCentre soc, ElementDeSOC el_a_retirer) {
        this.soc = soc ;
        this.elements_a_retirer = new ArrayList<>(1) ;
        this.elements_a_retirer.add(el_a_retirer) ;

    }
    public CommandeRetirerElementsDeSystemeOptiqueCentre(SystemeOptiqueCentre soc, List<ElementDeSOC> elts_a_retirer) {
        this.soc = soc ;
        this.elements_a_retirer = new ArrayList<>(elts_a_retirer.size()) ;
        this.elements_a_retirer.addAll(elts_a_retirer) ;
    }

    @Override
    public void executer() {
        soc.retirer(elements_a_retirer);
//        elements_a_retirer.forEach(soc::retirer) ;

        enregistrer();
    }

    @Override
    public void annuler() {
        soc.ajouter(elements_a_retirer);
    }
//    public void annuler() {
//        elements_a_retirer.forEach(soc::ajouterObstacleCentre);
//    }

}
