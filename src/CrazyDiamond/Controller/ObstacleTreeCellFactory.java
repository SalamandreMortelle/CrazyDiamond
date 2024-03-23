package CrazyDiamond.Controller;

import CrazyDiamond.CrazyDiamond;
import CrazyDiamond.Model.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.util.Callback;

import java.util.Objects;
import java.util.ResourceBundle;


// Credits : https://github.com/cerebrosoft/treeview-dnd-example/tree/master/treedrag

public class ObstacleTreeCellFactory implements Callback<TreeView<Obstacle>, TreeCell<Obstacle>>  {

    private static final ResourceBundle rb = ResourceBundle.getBundle("CrazyDiamond") ;
    private static final String DROP_HINT_STYLE_APRES = "-fx-border-color: #eea82f; -fx-border-width: 0 0 2 0; -fx-padding: 3 3 1 3";
    private static final String DROP_HINT_STYLE_DANS = "-fx-border-color: #eea82f; -fx-border-width: 1 1 1 1; -fx-padding: 3 3 1 3";

    private final Environnement environnement ;
    private TreeCell<Obstacle> dropZone;
    private TreeItem<Obstacle> draggedItem;


    public ObstacleTreeCellFactory(Environnement env) {
        this.environnement = env ;
    }

    @Override
    public TreeCell<Obstacle> call(TreeView<Obstacle> obstacleTreeView) {
        TreeCell<Obstacle> cell = new TreeCell<>() {

            Obstacle item_courant = null;

            @Override
            protected void updateItem(Obstacle item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    textProperty().unbind();
                    setText(null);
                    setGraphic(null);

                    item_courant = null;

                    return;
                }

//                ImageView iv1 = new ImageView();
//                if (item.getClass()== Cercle.class) {
//                    iv1.setImage(ICONE_CERCLE);
//                }
//                else {
////                    iv1.setImage(PIN_IMAGE);
//                }
//                setGraphic(iv1);

                if (item != item_courant) {
                    textProperty().bind(item.nomProperty());
                    item_courant = item;
                }

//                setText(item.nom());

            }
        };

        ContextMenu menuContextuelObstacles = new ContextMenu() ;
        MenuItem deleteItemObstacle = new MenuItem(rb.getString("supprimer.obstacle")) ;
        deleteItemObstacle.setOnAction(event -> new CommandeSupprimerObstacle(environnement,cell.getItem()).executer()) ;
        menuContextuelObstacles.getItems().add(deleteItemObstacle) ;
        cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
            if (isNowEmpty) {
                cell.setContextMenu(null);
            } else {
                cell.setContextMenu(menuContextuelObstacles);
            }
        });

        cell.setOnDragDetected((MouseEvent event) -> dragDetected(event, cell));
        cell.setOnDragOver((DragEvent event) -> dragOver(event, cell, obstacleTreeView));
        cell.setOnDragDropped((DragEvent event) -> drop(event, cell, obstacleTreeView));
        cell.setOnDragDone((DragEvent event) -> clearDropLocation());

        return cell;
    }

    private void dragDetected(MouseEvent event, TreeCell<Obstacle> treeCell) {
        draggedItem = treeCell.getTreeItem();

        // Seuls les obstacles "libres" (c'est-à-dire ceux qui sont au 1er niveau sous le noeud racine, et qui ne font
        // donc pas partie d'une Composition) peuvent être déplacés.
//        if (draggedItem.getParent()!=treeView.getRoot())
//            return ;

        Dragboard db = treeCell.startDragAndDrop(TransferMode.MOVE);

        ClipboardContent content = new ClipboardContent();
        content.put(CrazyDiamond.FORMAT_OBSTACLE_ID,draggedItem.getValue().id()) ;
        db.setContent(content);
        db.setDragView(treeCell.snapshot(null, null));
        event.consume();
    }

    private void dragOver(DragEvent event, TreeCell<Obstacle> treeCell, TreeView<Obstacle> treeView) {
        if (!event.getDragboard().hasContent(CrazyDiamond.FORMAT_OBSTACLE_ID)) return;

        TreeItem<Obstacle> item_survole = treeCell.getTreeItem();

        // can't drop on itself
        if (draggedItem == null || item_survole == draggedItem) return;
//        if (draggedItem == null || item_survole == null || item_survole == draggedItem) return;

        Obstacle o_dragged = draggedItem.getValue() ;

        if (item_survole!=null) {
            Obstacle o_survole = item_survole.getValue();
            boolean o_survole_est_premier_niveau = (item_survole.getParent() == treeView.getRoot());
            boolean o_survole_est_composition = o_survole instanceof Composition;
            boolean o_survole_est_groupe = o_survole instanceof Groupe;

//            if (!o_survole_est_premier_niveau && !o_survole_est_composition) return;
            // On peut déposer partout, sauf à une place précise dans une Composition (càd à sur un obstacle d'une composition)
            if (!o_survole_est_composition && o_survole.appartientAComposition())
                return ;

            // On ne peut pas déplacer une Composition dans une de ses sous-compositions
            if ((o_dragged instanceof Composition) && o_survole_est_composition && o_dragged.comprend(o_survole))
                return;
            // On ne peut pas déplacer un Groupe dans une de ses sous-groupes
            if ((o_dragged instanceof Groupe) && o_survole_est_groupe && o_dragged.comprend(o_survole))
                return;
            // On ne peut pas déplacer un Groupe dans une Composition
            if ((o_dragged instanceof Groupe) && (o_survole_est_composition || o_survole.appartientAComposition()))
                return;

            // On ne peut pas mettre un ElementSansEpaisseur dans une Composition
            if (o_survole_est_composition && item_survole.isExpanded() && (o_dragged instanceof ElementSansEpaisseur))
                return;

            // ignore if this is the root ; inutile car le treeitem racine existe mais n'est pas affiché
            if (draggedItem.getParent() == null) {
                clearDropLocation();
                return;
            }

            event.acceptTransferModes(TransferMode.MOVE);

            if (!Objects.equals(dropZone, treeCell)) {
                clearDropLocation();
                this.dropZone = treeCell;

                // Dépose dans une sous-composition ou dans une composition de 1er niveau qui est déployée ou qui est vide => on peut déposer dedans
                // De plus, on ne peut pas ajouter un Groupe dans une Composition
                if  (  ( o_survole_est_composition
                        && (!o_survole_est_premier_niveau || item_survole.isExpanded() || (((Composition) o_survole).estVide()))
                        && (!(o_dragged instanceof ElementSansEpaisseur))
                        && (!(o_dragged instanceof Groupe) ) )
                    || ( o_survole_est_groupe
                        && ( item_survole.isExpanded() || (((Groupe) o_survole).estVide()))
                       )
                )
                    dropZone.setStyle(DROP_HINT_STYLE_DANS);
                else
                    dropZone.setStyle(DROP_HINT_STYLE_APRES);

            }
        } else { // Item survole est vide : cela veut dire qu'on déposera à la fin de la liste des obstaces de 1er niveau

            event.acceptTransferModes(TransferMode.MOVE);
        }
    }

    private void drop(DragEvent event, TreeCell<Obstacle> treeCell, TreeView<Obstacle> treeView) {
        Dragboard db = event.getDragboard();

        if (!db.hasContent(CrazyDiamond.FORMAT_OBSTACLE_ID))  {
            event.setDropCompleted(false);
            return;
        }

        Obstacle o_dragged = draggedItem.getValue() ;

        TreeItem<Obstacle> item_cible_depose = treeCell.getTreeItem(); // Item sur lequel on a déposé

        Obstacle o_cible_depose = (item_cible_depose!=null?item_cible_depose.getValue():null) ;

        boolean o_cible_depose_est_premier_niveau = (item_cible_depose!=null) && (item_cible_depose.getParent()==treeView.getRoot()) ;
        boolean o_cible_depose_est_composition    = (item_cible_depose!=null) && (o_cible_depose instanceof Composition) ;
        boolean o_cible_depose_est_groupe = (item_cible_depose!=null) && (o_cible_depose instanceof Groupe) ;

        // On ne peut pas déplacer une Composition dans une de ses sous-compositions
        if ( (o_dragged instanceof Composition) && o_cible_depose_est_composition && o_dragged.comprend(o_cible_depose))
            return;
        // On ne peut pas déplacer un Groupe dans un de ses sous-groupes
        if ((o_dragged instanceof Groupe) && o_cible_depose_est_groupe && o_dragged.comprend(o_cible_depose))
            return;
        // On ne peut pas déplacer un Groupe dans une Composition
        if ((o_dragged instanceof Groupe) && (o_cible_depose_est_composition || o_cible_depose.appartientAComposition()))
            return;

        // Dépose dans une sous-composition ou dans une composition de 1er niveau qui est déployée ou qui est vide => ajout dans cette composition
        // De plus, on ne peut pas ajouter un Groupe dans une Composition
        if (o_cible_depose_est_composition
                && (!o_cible_depose_est_premier_niveau || item_cible_depose.isExpanded() || ( ((Composition)o_cible_depose).estVide() ) )
                && (!(o_dragged instanceof ElementSansEpaisseur))
                && (!(o_dragged instanceof Groupe)) ) {

            dropZone.setStyle(DROP_HINT_STYLE_DANS);

            Composition comp_cible = (Composition) o_cible_depose ;

            new CommandeAjouterObstacleDansComposition(environnement,comp_cible,o_dragged).executer() ;
        } else if ( o_cible_depose_est_groupe
                    && ( item_cible_depose.isExpanded() || (((Groupe) o_cible_depose).estVide()) )) {
            dropZone.setStyle(DROP_HINT_STYLE_DANS);

            Groupe grp_cible = (Groupe) o_cible_depose ;

            // Ajout en dernière position
            new CommandeAjouterObstacleDansGroupe(environnement,grp_cible,o_dragged).executer() ;

        }
        else if (o_cible_depose==null || environnement.estALaRacine(o_cible_depose)) { // Dépose sur un élément de 1er niveau => Element à positionner au 1er niveau de l'environnement
            if (dropZone!=null) dropZone.setStyle(DROP_HINT_STYLE_APRES);

            int indexCibleInParent = (item_cible_depose!=null?
                    item_cible_depose.getParent().getChildren().indexOf(item_cible_depose)
                    :treeView.getRoot().getChildren().size()-1);

//            if (!o_dragged.appartientAComposition() && !o_dragged.appartientAGroupe()) { // Déplacement d'un obstacle de premier niveau
            if (environnement.estALaRacine(o_dragged)) { // Déplacement d'un obstacle de premier niveau
                int indexSourceInParent = (item_cible_depose!=null?item_cible_depose.getParent().getChildren().indexOf(draggedItem):treeView.getRoot().getChildren().indexOf(draggedItem));

                if (indexCibleInParent > indexSourceInParent)
                    new CommandeDeplacerObstacleEnPosition(environnement,o_dragged,indexCibleInParent).executer();
                else if (indexCibleInParent < indexSourceInParent)
                    new CommandeDeplacerObstacleEnPosition(environnement,o_dragged,indexCibleInParent+1).executer();
            }
            else {
                if (o_dragged.appartientAComposition())// L'obstacle glissé est inclus dans une composition
                    new CommandeDeplacerObstacleDeCompositionDansEnvironnement(environnement, o_dragged, indexCibleInParent + 1).executer();
                else // L'obstacle glissé est dans un groupe
                    new CommandeDeplacerObstacleDeGroupeDansEnvironnement(environnement, o_dragged, indexCibleInParent + 1).executer();
            }
            treeView.getSelectionModel().select(draggedItem);
        } else { // Dépose à une position précise dans un sous-groupe

            // TODO : NYI

        }
        event.setDropCompleted(true);
    }

    private void clearDropLocation() {
        if (dropZone != null) dropZone.setStyle("");
    }    
}
