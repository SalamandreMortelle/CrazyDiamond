package CrazyDiamond.Controller;

import CrazyDiamond.Model.Obstacle;
import CrazyDiamond.Model.SystemeOptiqueCentre;
import javafx.beans.property.StringProperty;

public class ElementArbreSOC {

    protected final SystemeOptiqueCentre soc  ;
    protected final Obstacle obstacle ;

    public ElementArbreSOC() {
        this.soc = null ;
        this.obstacle = null ;
    }

    public ElementArbreSOC(SystemeOptiqueCentre soc) {
        this.soc = soc ;
        this.obstacle = null ;
    }

    public ElementArbreSOC(Obstacle obstacle) {
        this.soc = null ;
        this.obstacle = obstacle ;
    }

    public StringProperty nomProperty() {
        return (obstacle==null?soc.nomProperty():obstacle.nomProperty()) ;
    }

    public Object contenu() {
        return  (obstacle==null?soc:obstacle) ;
    }
}
