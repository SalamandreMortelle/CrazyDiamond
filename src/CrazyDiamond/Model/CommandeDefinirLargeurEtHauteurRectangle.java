package CrazyDiamond.Model;

public class CommandeDefinirLargeurEtHauteurRectangle extends Commande {

    // Le récepteur de la commande
    Rectangle rectangle ;

    // Paramètre de la commande
    double largeur ;
    double hauteur ;

    // Données en cas d'annulation
    double precedente_largeur ;
    double precedente_hauteur ;
    public CommandeDefinirLargeurEtHauteurRectangle(Rectangle r, double l, double h) {
        this.rectangle = r ;
        this.largeur = l ;
        this.hauteur = h ;
    }

    @Override
    public void executer() {
        double nouveau_precedente_largeur = rectangle.largeur() ;
        double nouveau_precedente_hauteur = rectangle.hauteur() ;

        if (largeur == nouveau_precedente_largeur && hauteur == nouveau_precedente_hauteur)
            return;

        precedente_largeur = nouveau_precedente_largeur ;
        precedente_hauteur = nouveau_precedente_hauteur ;

        rectangle.definirLargeur(largeur);
        rectangle.definirHauteur(hauteur);

        enregistrer();
    }

    @Override
    public void annuler() {
        rectangle.definirLargeur(precedente_largeur);
        rectangle.definirHauteur(precedente_hauteur);
    }

    protected void convertirDistances(double facteur_conversion) {
        largeur = largeur * facteur_conversion ;
        hauteur = hauteur * facteur_conversion ;
        precedente_largeur = precedente_largeur * facteur_conversion ;
        precedente_hauteur = precedente_hauteur * facteur_conversion ;
    }

}
