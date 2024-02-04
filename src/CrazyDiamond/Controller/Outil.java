package CrazyDiamond.Controller;

import javafx.scene.Cursor;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

public abstract class Outil {

    CanvasAffichageEnvironnement cae ;
    Cursor curseur_souris = Cursor.DEFAULT;

    public Outil(CanvasAffichageEnvironnement cae) {
        this.cae = cae;
    }

    public void prendre() { }
    public void deposer() { }

    void traiterClicSourisCanvas(MouseEvent me) { }  // Traitement des clics en dehors d'un glisser

    void traiterDeplacementSourisCanvas(MouseEvent me) { }

     void traiterBoutonSourisPresse(MouseEvent mouseEvent)  { }

    void traiterGlisserSourisCanvas(MouseEvent mouseEvent) { }
    void traiterBoutonSourisRelacheFinGlisser(MouseEvent mouseEvent) { }
    void interrompre() { }

    void traiterTouchePressee(KeyEvent keyEvent) {}

    public Cursor curseurSouris() {
        return curseur_souris;
    }
//    public Cursor definirCurseurSouris() {
//        return curseur_souris;
//    }

}
