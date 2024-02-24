package CrazyDiamond.Controller;

import CrazyDiamond.Model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OutilCreerComposition extends OutilPermettantDeplacementZoneVisible {

    private static final Logger LOGGER = Logger.getLogger( "CrazyDiamond" );

    private static final ResourceBundle rb = ResourceBundle.getBundle("CrazyDiamond") ;
    public OutilCreerComposition(CanvasAffichageEnvironnement cae) {
        super(cae);
    }

    public void prendre() {
        ButtonType okButtonType = new ButtonType(rb.getString("bouton.dialogue.composition.ok"), ButtonBar.ButtonData.OK_DONE);
        ButtonType annulerButtonType = new ButtonType(rb.getString("bouton.dialogue.composition.annuler"), ButtonBar.ButtonData.CANCEL_CLOSE);
        Dialog<ArrayList<Obstacle>> boite_dialogue = new Dialog<>() ;

        boite_dialogue.setTitle(rb.getString("titre.dialogue.composition"));
        boite_dialogue.setHeaderText(rb.getString("invite.dialogue.composition"));

        ObservableList<Obstacle> obstacles_a_proposer =  FXCollections.observableArrayList();

        Iterator<Obstacle> ito =  cae.environnement().iterateur_obstacles() ;
        while (ito.hasNext()) {
            Obstacle o = ito.next() ;
            // Rechercher si l'obstacle o implémente l'interface ElementAvecMatiere car c'est requis pour faire partie d'une composition
            // S"assurer aussi qu'il ne fait pas partie d'un SOC
            if (o instanceof ElementAvecMatiere && !o.appartientASystemeOptiqueCentre())
                obstacles_a_proposer.add( o ) ;
        }

        ListView<Obstacle> lo = new ListView<>(obstacles_a_proposer) ;

        ScrollPane sp = new ScrollPane(lo) ;
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        lo.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        boite_dialogue.getDialogPane().setContent(lo);

        boite_dialogue.setResultConverter( buttonType -> {
            if (buttonType == okButtonType)
                return new ArrayList<>(lo.getSelectionModel().getSelectedItems()) ;

            return null ;
        });

        boite_dialogue.getDialogPane().getButtonTypes().add(okButtonType);
        boite_dialogue.getDialogPane().getButtonTypes().add(annulerButtonType);


        Optional<ArrayList<Obstacle>> op_obstacles_choisis =  boite_dialogue.showAndWait() ;
        if (op_obstacles_choisis.isPresent()) {

            ArrayList<Obstacle> obstacles_choisis = op_obstacles_choisis.get() ;

            LOGGER.log(Level.INFO,"Obstacles choisis pour composition : {0}",obstacles_choisis) ;

            Composition nouvelle_composition = new Composition(Composition.Operateur.UNION);

            for(Obstacle o : obstacles_choisis) {
                cae.environnement().retirerObstacle(o);
                nouvelle_composition.ajouterObstacle(o);
            }

            cae.environnement().ajouterObstacle(nouvelle_composition);

            nouvelle_composition.commandeCreation(cae.environnement()).enregistrer();
//            new CommandeCreerComposition(cae.environnement(),compo).enregistrer();
        }


    }
}

