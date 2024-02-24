package CrazyDiamond.Model;

import javafx.geometry.Point2D;

public class CommandeDefinirParametreEtAxeFocalConique extends Commande {

    // Le récepteur de la commande
    Conique conique ;

    // Paramètre de la commande
    double parametre ;
    Point2D axe_focal ;

    // Données en cas d'annulation
    double precedent_parametre ;
    Point2D precedent_axe_focal ;

    public CommandeDefinirParametreEtAxeFocalConique(Conique c, double p, Point2D axe_f) {
        this.conique = c ;
        this.parametre = p ;
        this.axe_focal = axe_f ;
    }

    @Override
    public void executer() {
        double nouveau_precedent_parametre = conique.parametre(); ;
        Point2D nouveau_precedent_axe_focal = conique.axe_focal(); ;

        if ( parametre == nouveau_precedent_parametre
             && axe_focal.getX() == nouveau_precedent_axe_focal.getX()
             && axe_focal.getY() == nouveau_precedent_axe_focal.getY() )
            return;

        precedent_parametre = nouveau_precedent_parametre ;
        precedent_axe_focal = nouveau_precedent_axe_focal ;

        conique.definirParametre(parametre);
        conique.definirAxeFocal(axe_focal);

        enregistrer();
    }

    @Override
    public void annuler() {
        conique.definirParametre(precedent_parametre);
        conique.definirAxeFocal(precedent_axe_focal);
    }

    protected void convertirDistances(double facteur_conversion) {
        precedent_parametre = precedent_parametre * facteur_conversion ;
        parametre = parametre * facteur_conversion ;

        // L'axe focal doit rester normalisé : on ne le convertit pas
//        precedent_axe_focal = new Point2D(precedent_axe_focal.getX()*facteur_conversion,precedent_axe_focal.getY()*facteur_conversion) ;
//        axe_focal = new Point2D(axe_focal.getX()*facteur_conversion, axe_focal.getY()*facteur_conversion) ;


    }

}
