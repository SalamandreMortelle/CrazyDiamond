package CrazyDiamond.Model;

import javafx.geometry.Point2D;

public class CommandeDefinirPositionPoigneeObstacle extends Commande {

    // Le récepteur de la commande
    Obstacle obstacle ;

    // Paramètre de la commande
    Point2D position_poignee ;

    // Données en cas d'annulation
    Point2D precedente_position_poignee ;

    public CommandeDefinirPositionPoigneeObstacle(Obstacle o, Point2D position_poignee, Point2D precedente_position_poignee) {
        this.obstacle = o ;
        this.position_poignee = position_poignee ;
        this.precedente_position_poignee = precedente_position_poignee ;
    }

    @Override
    public void executer() {

        if (position_poignee.equals(precedente_position_poignee))
            return;

        obstacle.retaillerPourSourisEn(position_poignee);

        enregistrer();
    }

    @Override
    public void annuler() {
        obstacle.retaillerPourSourisEn(precedente_position_poignee);
    }

    protected void convertirDistances(double facteur_conversion) {
        precedente_position_poignee = precedente_position_poignee.multiply(facteur_conversion) ;
        position_poignee = position_poignee.multiply(facteur_conversion) ;
    }

}
