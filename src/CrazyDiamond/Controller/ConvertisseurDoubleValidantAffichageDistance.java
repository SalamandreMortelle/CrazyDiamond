package CrazyDiamond.Controller;

import javafx.beans.property.ObjectProperty;

/**
 * Cette classe permet de convertir une chaine en Double et inversement, en affichant un nombre de décimales cohérent
 * avec la résolution courante du CanvasAffichageEnvironnement qui lui est passé (plus un pixel graphique de l'affichage
 * représente une petite zone de l'espace géométrique, plus le nombre de décimales est élevé).
 * Si un ObjectProperty<Double> est passé en paramètre du constructeur, sa valeur courante sera retournée si la conversion
 * en Double de la chaîne passée à fromString() est impossible (maintien de la valeur courante de la Property).
 */
public class ConvertisseurDoubleValidantAffichageDistance extends ConvertisseurDoubleValidant {

    private final CanvasAffichageEnvironnement cae;


    public ConvertisseurDoubleValidantAffichageDistance(CanvasAffichageEnvironnement cae, ObjectProperty<Double> ob_p_d) {
        super(ob_p_d);
        this.cae = cae;
        this.obj_prop_double = ob_p_d;

        // Initialisation du nombre de décimales à afficher
        caleSurResolution();

        // Recalage automatique du nombre de décimales si la résolution du canvas change
        cae.resolutionProperty().addListener( ( (observableValue, oldValue, newValue) -> {caleSurResolution();} ));

    }

    public ConvertisseurDoubleValidantAffichageDistance(CanvasAffichageEnvironnement cae) {
        this(cae,null);
    }

    public final void caleSurResolution() {
        int nb_decimales =(int) Math.ceil(Math.log10(1 / (cae.resolution()*cae.environnement().unite().valeur))) ; // Passage de la résolution en mètres/pixel

        if (nb_decimales > 1) {
            StringBuilder sb = new StringBuilder("0.0");
            for (int i = 1; i < nb_decimales; i++)
                sb.append('0');

            decimal_format.applyPattern(sb.toString());
        }

    }

}
