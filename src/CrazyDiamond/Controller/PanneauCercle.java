package CrazyDiamond.Controller;

import CrazyDiamond.Model.Cercle;
import CrazyDiamond.Model.ChangeListenerAvecGarde;
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
    private boolean dans_composition;

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
    // La déclaration de cet attribut est requise pour faire un binding "persistant" entre la variable numérique du modèle
    // et l'ObjectProperty<Double> à l'intérieur du Spinner Value Factory qui encapsule la valueProperty du Spinner. Il
    // créé une StrongRef qui permet de s'assurer qu'il n'y aura pas de garbage collection intempestif de cet ObjectProperty.
    // Cette obligation vient de la Property du Spinner Value Factory qui est de type ObjectProperty<Double> (ou Integer...)
    // et non de type DoubleProperty comme la Property du modèle, qu'il faut donc convertir avec la méthode asObject et stocker
    // en tant que tel, pour pouvoir réaliser le binding.
    private ObjectProperty<Double> cercle_xcentre_object_property;
    private SpinnerValueFactory.DoubleSpinnerValueFactory svf_x ;
    @FXML
    private Spinner<Double> spinner_ycentre ;
    private ObjectProperty<Double> cercle_ycentre_object_property; // Attribut requis (cf. supra)

    @FXML
    private Spinner<Double> spinner_rayon ;
    private ObjectProperty<Double> cercle_rayon_object_property; // Attribut requis (cf. supra)

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

        cercle.centreProperty().addListener(new ChangeListenerAvecGarde<Point2D>(this::prendreEnComptePosition));

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
        cercle_rayon_object_property = cercle.rayonProperty().asObject() ;
        spinner_rayon.getValueFactory().valueProperty().bindBidirectional(cercle_rayon_object_property);

        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_rayon, cercle.rayon());

    }

    private void prendreEnComptePosition(Point2D nouveau_centre) {
                spinner_xcentre.getValueFactory().valueProperty().set(nouveau_centre.getX());
                spinner_ycentre.getValueFactory().valueProperty().set(nouveau_centre.getY());
    }
    private void definirXCentreCercle(Double x_c) { cercle.definirCentre(new Point2D(x_c,cercle.centre().getY())); }
    private void definirYCentreCercle(Double y_c) { cercle.definirCentre(new Point2D(cercle.centre().getX(),y_c)); }

}
