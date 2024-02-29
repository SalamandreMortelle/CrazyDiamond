package CrazyDiamond.Controller;

import CrazyDiamond.Model.*;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.logging.Level;
import java.util.logging.Logger;

public class PanneauCercle  {

    // Modèle
    Cercle cercle ;
    private final boolean dans_composition;

    CanvasAffichageEnvironnement canvas;

    // Récupération du logger
    private static final Logger LOGGER = Logger.getLogger( "CrazyDiamond" );

    @FXML
    private VBox baseElementIdentifie;
    @FXML
    private PanneauElementIdentifie baseElementIdentifieController ;

    // Controleurs des sous-panneaux génériques pour les attributs de contour, et de matière
    @FXML
    private VBox baseContour;
    @FXML
    private PanneauElementAvecContour baseContourController;

    @FXML
    private VBox baseMatiere;
    @FXML
    private PanneauElementAvecMatiere baseMatiereController;


    @FXML
    private Spinner<Double> spinner_xcentre ;
    @FXML
    private Spinner<Double> spinner_ycentre ;

    @FXML
    private Spinner<Double> spinner_rayon ;

    public PanneauCercle(Cercle c, boolean dans_composition, CanvasAffichageEnvironnement cnv) {
        LOGGER.log(Level.INFO,"Construction du PanneauCercle") ;

        if (c==null)
            throw new IllegalArgumentException("L'objet Cercle attaché au PanneauCercle ne peut pas être 'null'") ;

        this.cercle = c;
        this.dans_composition = dans_composition ;

         canvas = cnv ;

    }

    public void initialize() {
        LOGGER.log(Level.INFO,"Initialisation du PanneauCercle et de ses liaisons") ;

        baseElementIdentifieController.initialize(cercle);

        if (!dans_composition) {
            baseContourController.initialize(cercle);
            baseMatiereController.initialize(cercle);
        }else {
            baseMatiere.setVisible(false);
            baseContour.setVisible(false);
        }

        cercle.centreProperty().addListener(new ChangeListenerAvecGarde<>(this::prendreEnComptePosition));

        // Position X
        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_xcentre, cercle.xCentre(), this::definirXCentreCercle);
        spinner_xcentre.getStyleClass().add(Spinner.STYLE_CLASS_ARROWS_ON_RIGHT_HORIZONTAL) ;
        spinner_xcentre.editableProperty().bind(cercle.appartenanceSystemeOptiqueProperty().not()) ;
        spinner_xcentre.disableProperty().bind(cercle.appartenanceSystemeOptiqueProperty()) ;

        // Position Y
        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_ycentre, cercle.yCentre(), this::definirYCentreCercle);
        spinner_ycentre.editableProperty().bind(cercle.appartenanceSystemeOptiqueProperty().not()) ;
        spinner_ycentre.disableProperty().bind(cercle.appartenanceSystemeOptiqueProperty()) ;

        // Rayon
        cercle.rayonProperty().addListener(new ChangeListenerAvecGarde<>(this::prendreEnCompteRayon));
        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_rayon, cercle.rayon(),this::definirRayon);

    }

    private void definirRayon(Double r) {
        new CommandeDefinirUnParametreDoubleDistance<>(cercle,r,cercle::rayon,cercle::definirRayon).executer() ;
    }

    private void prendreEnCompteRayon(Number nouveau_rayon) {
        spinner_rayon.getValueFactory().valueProperty().set(nouveau_rayon.doubleValue());
    }


    private void prendreEnComptePosition(Point2D nouveau_centre) {
        spinner_xcentre.getValueFactory().valueProperty().set(nouveau_centre.getX());
        spinner_ycentre.getValueFactory().valueProperty().set(nouveau_centre.getY());
    }
    private void definirXCentreCercle(Double x_c) {
        new CommandeDefinirUnParametrePoint<>(cercle,new Point2D(x_c,cercle.centre().getY()),cercle::centre,cercle::definirCentre).executer();
    }
    private void definirYCentreCercle(Double y_c) {
        new CommandeDefinirUnParametrePoint<>(cercle,new Point2D(cercle.centre().getX(),y_c),cercle::centre,cercle::definirCentre).executer();
    }

}
