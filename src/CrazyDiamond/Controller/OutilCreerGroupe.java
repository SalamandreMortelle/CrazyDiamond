package CrazyDiamond.Controller;

import CrazyDiamond.Model.Groupe;
import CrazyDiamond.Model.Obstacle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OutilCreerGroupe extends OutilPermettantDeplacementZoneVisible {

    private static final Logger LOGGER = Logger.getLogger( "CrazyDiamond" );

    private static final ResourceBundle rb = ResourceBundle.getBundle("CrazyDiamond") ;
    public OutilCreerGroupe(CanvasAffichageEnvironnement cae) {
        super(cae);
    }

    public void prendre() {
        ButtonType okButtonType = new ButtonType(rb.getString("bouton.dialogue.groupe.ok"), ButtonBar.ButtonData.OK_DONE);
        ButtonType annulerButtonType = new ButtonType(rb.getString("bouton.dialogue.groupe.annuler"), ButtonBar.ButtonData.CANCEL_CLOSE);
        Dialog<ArrayList<Obstacle>> boite_dialogue = new Dialog<>() ;

        boite_dialogue.setTitle(rb.getString("titre.dialogue.groupe"));
        boite_dialogue.setHeaderText(rb.getString("invite.dialogue.groupe"));

        ObservableList<Obstacle> obstacles_a_proposer =  FXCollections.observableArrayList();

        // Pour éviter des complications, seuls les obstacles de premier niveau sont proposés pour ajout au Groupe
        // Exemple de complication : si l'utilisateur choisissait un Groupe et l'un de ses sous-groupes dans la liste, que
        // devons-nous faire ?? Idem s'il choisit une Composition et l'une de ses sous-compositions
        Iterator<Obstacle> ito =  cae.environnement().iterateur_obstacles_premier_niveau() ;
        ito.forEachRemaining(obstacles_a_proposer::add);
//        while (ito.hasNext())
//            obstacles_a_proposer.add( ito.next() ) ;


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

            Groupe nouveau_groupe = new Groupe() ;


            for(Obstacle o : obstacles_choisis) {
                cae.environnement().supprimerObstacleALaRacine(o);
                nouveau_groupe.ajouterObstacle(o);
            }

            cae.environnement().ajouterObstacleALaRacine(nouveau_groupe);

            nouveau_groupe.commandeCreation(cae.environnement()).enregistrer();
//            new CommandeCreerComposition(cae.environnement(),compo).enregistrer();
        }


    }
}

