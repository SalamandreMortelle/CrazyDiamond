package CrazyDiamond.Model;

import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.List;

/**
 * Cette commande permet d'ajouter une liste arbitraire d'éléments (Obstacles ou SOCs) dans un SOC parent.
 * Si un élément ajouté fait partie d'un SOC, il en est retiré avant d'être ajouté.
 */
public class CommandeAjouterElementsDansSystemeOptiqueCentre extends Commande {

    // Le récepteur de la commande
    SystemeOptiqueCentre soc;
    Environnement environnement ;
    List<ElementDeSOC> elements;


    // Informations pour l'annulation : état initial

//    ArrayList<ArrayList<ElementDeSOC>> listes_elements_initiaux_des_socs; // Liste des obstacles initialement présents dans chaque SOC ajouté
    ArrayList<SystemeOptiqueCentre> socs_contenant_elements_ajoutes;
    ArrayList<Point2D> translations;
    ArrayList<Double> rotations;

    public CommandeAjouterElementsDansSystemeOptiqueCentre(Environnement env, SystemeOptiqueCentre soc, ElementDeSOC el_a_ajouter) {
        this.environnement = env ;
        this.elements = new ArrayList<>(1) ;
        this.elements.add(el_a_ajouter) ;
        initialiser(soc);
    }
    public CommandeAjouterElementsDansSystemeOptiqueCentre(Environnement env, SystemeOptiqueCentre soc, List<ElementDeSOC> el_a_ajouter) {
        this.environnement = env ;
        this.elements = new ArrayList<>(el_a_ajouter.size()) ;
        this.elements.addAll(el_a_ajouter) ;
        initialiser(soc);
    }
    private void initialiser(SystemeOptiqueCentre soc) {
        this.soc = soc ;

        socs_contenant_elements_ajoutes = new ArrayList<>(elements.size()) ;
        translations = new ArrayList<>(elements.size()) ;
        rotations = new ArrayList<>(elements.size()) ;
//        listes_elements_initiaux_des_socs = new ArrayList<>(1);

        elements.forEach(el -> {
            socs_contenant_elements_ajoutes.add(el.SOCParentDirect()) ;
            translations.add(soc.translationPourAjoutElement(el));
            rotations.add(soc.angleRotationPourAjoutElement(el));

//            if (el instanceof SystemeOptiqueCentre soc_ajoute)
//                listes_elements_initiaux_des_socs.add(new ArrayList<>(soc_ajoute.elements_centres_premier_niveau())) ;

        });
    }

    @Override
    public void executer() {
        elements.forEach(el -> {

            if (el.SOCParent()!=null)
                el.SOCParent().retirer(el);
            else if (el instanceof SystemeOptiqueCentre el_soc)
                environnement.retirerSystemeOptiqueCentreALaRacine(el_soc);

            // L'élément est retiré de son SOC parent (et ses éventuels éléments [si c'est un SOC] ne le référencent plus comme SOC parent)

            soc.ajouter(el) ;
        }) ;
        enregistrer();
    }

    @Override
    public void annuler() {
        ElementDeSOC el ;
        for (int i = 0; i < elements.size() ; ++i) {
            el = elements.get(i);

            soc.retirer(el);

            el.tournerAutourDe(el.pointSurAxeRevolution(), -rotations.get(i));
            el.translater(translations.get(i).multiply(-1d));

            // On remet l'élément dans son SOC d'origine s'il en avait un
            SystemeOptiqueCentre soc_contenant = socs_contenant_elements_ajoutes.get(i) ;
            if (soc_contenant!=null)
                soc_contenant.ajouter(el);
            // ... s'il n'en avait pas et que c'est un SOC, on le remet dans l'environnement
            else if (el instanceof SystemeOptiqueCentre el_soc)
               environnement.ajouterSystemeOptiqueCentre(el_soc);


        }
    }

    protected void convertirDistances(double facteur_conversion) {
        translations.forEach(t -> t.multiply(facteur_conversion));
    }

}
