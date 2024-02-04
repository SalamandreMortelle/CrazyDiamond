package CrazyDiamond.Controller;

import CrazyDiamond.Model.SystemeOptiqueCentre;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

public class OutilAjoutSystemeOptiqueCentre implements Outil {

    CanvasAffichageEnvironnement cae ;
    protected SystemeOptiqueCentre soc_en_cours_ajout = null ;

    public OutilAjoutSystemeOptiqueCentre(CanvasAffichageEnvironnement cae) { this.cae = cae ; }

    public void traiterClicSourisCanvas(MouseEvent me) {

        Point2D pclic = cae.gc_vers_g(me.getX(),me.getY()) ;

        if (soc_en_cours_ajout == null) {

            // Création d'un nouveau SOC
            soc_en_cours_ajout = new SystemeOptiqueCentre(cae.environnement(),pclic,0.0) ;

            return ;
        }

        // On est sur le 2ᵉ clic qui fige l'orientation du SOC en cours d'ajout
        Point2D direction = pclic.subtract(soc_en_cours_ajout.origine()) ;

        if (direction.magnitude()==0.0)
            return;

        soc_en_cours_ajout.definirDirection(pclic.subtract(soc_en_cours_ajout.origine()));

        soc_en_cours_ajout = null ;

    }

    public void traiterDeplacementSourisCanvas(MouseEvent me) {

        Point2D pos_souris = cae.gc_vers_g(me.getX(),me.getY()) ;

        if (soc_en_cours_ajout !=null) {
            Point2D direction = pos_souris.subtract(soc_en_cours_ajout.origine());

            if (direction.magnitude() == 0.0)
                return;

            soc_en_cours_ajout.definirDirection(pos_souris.subtract(soc_en_cours_ajout.origine()));

            cae.environnement().ajouterSystemeOptiqueCentre(soc_en_cours_ajout); // Ne fait rien si source_courante est déjà dans l'environnement

            // TODO : à mettre dans l'eventListener sur l'ajout d'obstacles au niveau de Panneau principal
//            TreeItem<ElementArbreSOC> ti_soc = chercheItemSOCDansArbreSOC(soc_en_cours_ajout,treeview_socs.getRoot()) ;
//            treeview_socs.getSelectionModel().select(chercheItemDansTreeItem(ti_soc.getValue(),treeview_socs.getRoot()));
        }

    }

    public void traiterTouchePressee(KeyEvent keyEvent)
    {
        switch (keyEvent.getCode()) {
            case ESCAPE ->  { interrompre(); keyEvent.consume(); }
            }
    }

    public void interrompre() {
        if (soc_en_cours_ajout != null) {
            // On retire le soc courant, [ce qui va rafraichir les chemins et le décor ?]
            cae.environnement().retirerSystemeOptiqueCentre(soc_en_cours_ajout);
            soc_en_cours_ajout = null;
        }
    }

}

