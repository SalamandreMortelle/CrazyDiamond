package CrazyDiamond.Controller;

import CrazyDiamond.Model.Source;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

public class OutilAjoutSource implements Outil {

    CanvasAffichageEnvironnement cae ;
    protected Source source_en_cours_ajout = null ;

    public OutilAjoutSource(CanvasAffichageEnvironnement cae) { this.cae = cae ; }

    public void traiterClicSourisCanvas(MouseEvent me) {

        Point2D pclic = cae.gc_vers_g(me.getX(),me.getY()) ;

        if (source_en_cours_ajout == null) { // On vient de commencer le tracé d'une nouvelle source

            // Création d'une nouvelle source
            source_en_cours_ajout = new Source(cae.environnement(), pclic, 0.0, Source.TypeSource.PINCEAU);

            return ;
        }

        // On est donc sur le 2ᵉ clic, qui fige la direction de la source

        Point2D direction = pclic.subtract(source_en_cours_ajout.position()) ;

        if (direction.magnitude()==0.0)
            return;

        source_en_cours_ajout.definirDirection(pclic.subtract(source_en_cours_ajout.position()));

        source_en_cours_ajout = null ;


    }

    public void traiterDeplacementSourisCanvas(MouseEvent me) {

        Point2D pos_souris = cae.gc_vers_g(me.getX(),me.getY()) ;

        if (source_en_cours_ajout !=null) {
            Point2D direction = pos_souris.subtract(source_en_cours_ajout.position());

            if (direction.magnitude() == 0.0)
                return;

            source_en_cours_ajout.definirDirection(pos_souris.subtract(source_en_cours_ajout.position()));

            cae.environnement().ajouterSource(source_en_cours_ajout); // Ne fait rien si source_courante est déjà dans l'environnement

            // TODO : à mettre dans PanneauPrincipal : quand une source est ajoutée dans l'environnement, on la sélectionne dans la liste
            // listview_sources.getSelectionModel().select(source_en_cours_ajout);
        }

    }

    public void traiterTouchePressee(KeyEvent keyEvent)
    {
        switch (keyEvent.getCode()) {
            case ESCAPE ->  { interrompre(); keyEvent.consume(); }
            }
    }

    public void interrompre() {
        if (source_en_cours_ajout != null) {
            // On retire la source courante, ce qui va rafraichir les chemins et le décor
            cae.environnement().retirerSource(source_en_cours_ajout);
            source_en_cours_ajout = null;
        }
    }

}
