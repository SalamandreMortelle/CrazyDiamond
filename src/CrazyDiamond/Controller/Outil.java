package CrazyDiamond.Controller;

import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

public interface Outil {

    default void traiterClicSourisCanvas(MouseEvent me) { } // Traitement des clics en dehors d'un glisser

    default void traiterDeplacementSourisCanvas(MouseEvent me) { }

    default void traiterBoutonSourisPresse(MouseEvent mouseEvent) { }

    default void traiterGlisserSourisCanvas(MouseEvent mouseEvent) { }
    default void traiterBoutonSourisRelacheFinGlisser(MouseEvent mouseEvent) { }
    default void interrompre() { }

    default void traiterTouchePressee(KeyEvent keyEvent) { }

}
