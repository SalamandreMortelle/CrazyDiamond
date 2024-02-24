package CrazyDiamond.Model;

public class CommandeDefinirLongueurRayonDiaphragmeEtOrientationSegment extends Commande {

    // Le récepteur de la commande
    Segment segment ;

    // Paramètre de la commande
    double longueur ;
    double rayon_diaphragme ;
    double orientation ;

    // Données en cas d'annulation
    double precedente_longeur ;
    double precedent_rayon_diaphragme ;
    double precedente_orientation ;

    public CommandeDefinirLongueurRayonDiaphragmeEtOrientationSegment(Segment s, double l, double r_d,double o) {
        this.segment = s ;
        this.longueur = l ;
        this.rayon_diaphragme = r_d ;
        this.orientation = o ;
    }

    @Override
    public void executer() {
        double nouveau_precedente_longueur = segment.longueur() ;
        double nouveau_precedent_rayon_diaphragme = segment.rayonDiaphragme() ;
        double nouveau_precedente_orientation = segment.orientation();

        if (longueur == nouveau_precedente_longueur && rayon_diaphragme == nouveau_precedent_rayon_diaphragme && orientation == nouveau_precedente_orientation)
            return;

        precedente_longeur = nouveau_precedente_longueur ;
        precedent_rayon_diaphragme = nouveau_precedent_rayon_diaphragme ;
        precedente_orientation = nouveau_precedente_orientation ;

        segment.definirLongueur(longueur);
        segment.definirRayonDiaphragme(rayon_diaphragme);
        segment.definirOrientation(orientation);

        enregistrer();
    }

    @Override
    public void annuler() {
        segment.definirLongueur(precedente_longeur);
        segment.definirRayonDiaphragme(precedent_rayon_diaphragme);
        segment.definirOrientation(precedente_orientation);
    }

    protected void convertirDistances(double facteur_conversion) {
        longueur = longueur * facteur_conversion ;
        rayon_diaphragme = rayon_diaphragme * facteur_conversion ;
        precedente_longeur = precedente_longeur * facteur_conversion ;
        precedent_rayon_diaphragme = precedent_rayon_diaphragme * facteur_conversion ;
    }

}
