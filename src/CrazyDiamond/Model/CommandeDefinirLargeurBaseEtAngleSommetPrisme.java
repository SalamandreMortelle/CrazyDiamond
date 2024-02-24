package CrazyDiamond.Model;

public class CommandeDefinirLargeurBaseEtAngleSommetPrisme extends Commande {

    // Le récepteur de la commande
    Prisme prisme ;

    // Paramètre de la commande
    double largeur_base ;
    double angle_sommet ;

    // Données en cas d'annulation
    double precedente_largeur_base ;
    double precedent_angle_sommet ;
    public CommandeDefinirLargeurBaseEtAngleSommetPrisme(Prisme p, double l_b, double a_s) {
        this.prisme = p ;
        this.largeur_base = l_b ;
        this.angle_sommet = a_s ;
    }

    @Override
    public void executer() {
        double nouveau_precedente_largeur_base = prisme.largeurBase() ;
        double nouveau_precedent_angle_sommet = prisme.angleSommet() ;

        if (largeur_base == nouveau_precedente_largeur_base && angle_sommet==nouveau_precedent_angle_sommet)
            return;

        precedente_largeur_base = nouveau_precedente_largeur_base ;
        precedent_angle_sommet  = nouveau_precedent_angle_sommet ;

        prisme.definirLargeurBase(largeur_base);
        prisme.definirAngleSommet(angle_sommet);

        enregistrer();
    }

    @Override
    public void annuler() {
        prisme.definirLargeurBase(precedente_largeur_base);
        prisme.definirAngleSommet(precedent_angle_sommet);
    }

    protected void convertirDistances(double facteur_conversion) {
        largeur_base = largeur_base * facteur_conversion ;
        precedente_largeur_base = precedente_largeur_base * facteur_conversion ;
    }

}
