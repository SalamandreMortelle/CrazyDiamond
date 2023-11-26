package CrazyDiamond.Controller;

import CrazyDiamond.Model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PanneauParametresEnvironnement {

    public ColorPicker colorpicker_matiere_nouveaux_obstacles;
    public ColorPicker colorpicker_contour_nouveaux_obstacles;
    public ColorPicker colorpicker_couleur_nouveaux_rayons;
    public CheckBox checkbox_fresnel;
    public Button editer_texte_commentaire;

    @FXML
    private ToggleGroup choix_unite ;

    @FXML
    private RadioButton choix_unite_m;
    @FXML
    private RadioButton choix_unite_dm;
    @FXML
    private RadioButton choix_unite_cm;
    @FXML
    private RadioButton choix_unite_mm;


    // Modèle
    Environnement environnement ;

    private static final Logger LOGGER = Logger.getLogger( "CrazyDiamond" );
    private static final ResourceBundle rb = ResourceBundle.getBundle("CrazyDiamond") ;

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

        if (environnement.unite() == Unite.M)
            choix_unite_m.setSelected(true);
        else if (environnement.unite() == Unite.DM)
            choix_unite_dm.setSelected(true);
        else if (environnement.unite() == Unite.CM)
            choix_unite_cm.setSelected(true);
        else if (environnement.unite() == Unite.MM)
            choix_unite_mm.setSelected(true);

        // Ce listener est mono-directionnel Vue > Modèle (mais l'état initial du toggle choix_unite est déjà positionné)
        choix_unite.selectedToggleProperty().addListener((observable, oldValue,newValue) -> {
            LOGGER.log(Level.FINE,"Choix unité passe de {0} à {1}", new Object[] {oldValue,newValue}) ;

            if (newValue== choix_unite_m && environnement.unite()!= Unite.M)
                environnement.changerUnite(environnement.unite(),Unite.M);
            else if (newValue== choix_unite_dm && environnement.unite()!= Unite.DM)
                environnement.changerUnite(environnement.unite(),Unite.DM);
            else if (newValue== choix_unite_cm && environnement.unite()!= Unite.CM)
                environnement.changerUnite(environnement.unite(),Unite.CM);
            else if (newValue== choix_unite_mm && environnement.unite()!= Unite.MM)
                environnement.changerUnite(environnement.unite(),Unite.MM);

        });

        environnement.uniteProperty().addListener( (observableValue, oldValue, newValue) -> {
            LOGGER.log(Level.FINE,"Unité passe de {0} à {1}", new Object[] {oldValue,newValue}) ;

            if (newValue == Unite.M && choix_unite.getSelectedToggle()!= choix_unite_m)
                choix_unite.selectToggle(choix_unite_m);
            else if (newValue == Unite.DM && choix_unite.getSelectedToggle()!= choix_unite_dm)
                choix_unite.selectToggle(choix_unite_dm);
            else if (newValue == Unite.CM && choix_unite.getSelectedToggle()!= choix_unite_cm)
                choix_unite.selectToggle(choix_unite_cm);
            else if (newValue == Unite.MM && choix_unite.getSelectedToggle()!= choix_unite_mm)
                choix_unite.selectToggle(choix_unite_mm);

        } );


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

    public void traiterEditionCommentaire(ActionEvent actionEvent) {

        ButtonType okButtonType = new ButtonType(rb.getString("bouton.dialogue.edition_commentaire.ok"), ButtonBar.ButtonData.OK_DONE);
        ButtonType annulerButtonType = new ButtonType(rb.getString("bouton.dialogue.edition_commentaire.annuler"), ButtonBar.ButtonData.CANCEL_CLOSE);
        Dialog<String> boite_dialogue = new Dialog<>() ;

        boite_dialogue.setTitle(rb.getString("titre.dialogue.edition_commentaire"));
        boite_dialogue.setHeaderText(rb.getString("invite.dialogue.edition_commentaire"));

//        ObservableList<Obstacle> obstacles_a_proposer =  FXCollections.observableArrayList();
//
//        Iterator<Obstacle> ito =  environnement.iterateur_obstacles() ;
//        while (ito.hasNext()) {
//            Obstacle o = ito.next() ;
//            // Rechercher si l'obstacle o implemente l'interface ElementAvecMatiere car eux seuls peuvent faire partie d'une composition
//            if (o instanceof ElementAvecMatiere)
//                obstacles_a_proposer.add( o ) ;
//        }
//
//        ListView<Obstacle> lo = new ListView<Obstacle>(obstacles_a_proposer) ;
//
//        // TODO Limiter la composition à deux objets : proposer deux listview en sélection SINGLE côte à côte (mais
//        // interdire de choisir le même objet dans les deux listes... : retirer de la 2ème l'objet sélectionné dans
//        // la première, et le remettre si il n'est plus sélectionné dans la première... Mais quid si on sélectionne
//        // d'abord dans la 2eme liste, avant la première ??
//
//        ScrollPane sp = new ScrollPane(lo) ;
//        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
//
//        lo.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
//
//        boite_dialogue.getDialogPane().setContent(lo);

        TextArea zone_saisie = new TextArea(environnement.commentaire()) ;
        zone_saisie.setWrapText(true);

        boite_dialogue.getDialogPane().setContent(zone_saisie);

        boite_dialogue.setResultConverter( buttonType -> {
            if (buttonType == okButtonType)
                return zone_saisie.getText() ;

            return null ;
        });

        boite_dialogue.getDialogPane().getButtonTypes().add(okButtonType);
        boite_dialogue.getDialogPane().getButtonTypes().add(annulerButtonType);


        Optional<String> op_commentaire_saisi =  boite_dialogue.showAndWait() ;
        if (op_commentaire_saisi.isPresent()) {

            String commentaire_saisi = op_commentaire_saisi.get() ;

            environnement.definirCommentaire(commentaire_saisi) ;
        }



    }
}
