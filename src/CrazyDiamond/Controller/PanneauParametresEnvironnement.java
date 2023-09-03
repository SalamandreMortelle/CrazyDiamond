package CrazyDiamond.Controller;

import CrazyDiamond.Model.ElementAvecContour;
import CrazyDiamond.Model.ElementAvecMatiere;
import CrazyDiamond.Model.Environnement;
import CrazyDiamond.Model.Source;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;

public class PanneauParametresEnvironnement {

    public ColorPicker colorpicker_matiere_nouveaux_obstacles;
    public ColorPicker colorpicker_contour_nouveaux_obstacles;
    public ColorPicker colorpicker_couleur_nouveaux_rayons;
    public CheckBox checkbox_fresnel;

    // Modèle
    Environnement environnement ;

    @FXML
    public ColorPicker colorpicker_fond;

//    public PanneauParametresEnvironnement(Environnement e) {
//        System.out.println("BUILDING  PanneauParametresEnvironnement");
//
//        if (e==null)
//            throw new IllegalArgumentException("L'objet Environnement attaché au PanneauParametresEnvironnement ne peut pas être 'null'") ;
//
//        this.environnement = e;
//
//    }

    public void initialize(Environnement environnement) {

        this.environnement = environnement ;

//        colorpicker_fond.valueProperty().bind( environnement.couleurFondProperty() );

        colorpicker_fond.setValue(environnement.couleurFond());

        colorpicker_contour_nouveaux_obstacles.valueProperty().bindBidirectional(ElementAvecContour.couleur_contour_par_defaut_property);
        colorpicker_matiere_nouveaux_obstacles.valueProperty().bindBidirectional(ElementAvecMatiere.couleur_matiere_par_defaut_property);
        colorpicker_couleur_nouveaux_rayons.valueProperty().bindBidirectional(Source.couleurParDefautProperty());

        checkbox_fresnel.selectedProperty().bindBidirectional(environnement.reflexionAvecRefractionProperty());


//        colorpicker_contour_nouveaux_obstacles.setValue(ElementAvecContour.couleur_contour_par_defaut_property.getValue());
//        colorpicker_matiere_nouveaux_obstacles.setValue(ElementAvecMatiere.couleur_matiere_par_defaut_property.getValue());

//        colorpicker_fond.setOnAction(new EventHandler() {
//            public void handle(Event t) {
//                text.setFill(colorPicker.getValue());
//            }
//        });


    }

    public void traiterChangementCouleurFond(ActionEvent actionEvent) {
        environnement.definirCouleurFond(colorpicker_fond.getValue());
    }

    public void traiterChangementCouleurMatiereObstacles(ActionEvent actionEvent) {
//        ElementAvecMatiere.couleur_matiere_par_defaut = colorpicker_matiere_nouveaux_obstacles.getValue() ;
//        ElementAvecMatiere.couleur_matiere_par_defaut.saturate() ;

//        ElementAvecMatiere.couleur_matiere_par_defaut_property.setValue(colorpicker_matiere_nouveaux_obstacles.getValue());
    }

    public void traiterChangementCouleurContourObstacles(ActionEvent actionEvent) {
//        ElementAvecContour.couleur_contour_par_defaut = colorpicker_contour_nouveaux_obstacles.getValue() ;

//        ElementAvecContour.couleur_contour_par_defaut.saturate() ;
    }

}
