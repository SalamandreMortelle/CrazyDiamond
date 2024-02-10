package CrazyDiamond.Controller;

import CrazyDiamond.CrazyDiamond;
import CrazyDiamond.Model.Composition;
import CrazyDiamond.Model.Environnement;
import CrazyDiamond.Model.Obstacle;
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
        deleteItemObstacle.setOnAction(event -> environnement.retirerObstacle(cell.getItem())) ;
        menuContextuelObstacles.getItems().add(deleteItemObstacle) ;
        cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
            if (isNowEmpty) {
                cell.setContextMenu(null);
            } else {
                cell.setContextMenu(menuContextuelObstacles);
            }
        });

        cell.setOnDragDetected((MouseEvent event) -> dragDetected(event, cell, obstacleTreeView));
        cell.setOnDragOver((DragEvent event) -> dragOver(event, cell, obstacleTreeView));
        cell.setOnDragDropped((DragEvent event) -> drop(event, cell, obstacleTreeView));
        cell.setOnDragDone((DragEvent event) -> clearDropLocation());

        return cell;
    }

    private void dragDetected(MouseEvent event, TreeCell<Obstacle> treeCell, TreeView<Obstacle> treeView) {
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
        if (draggedItem == null || item_survole == null || item_survole == draggedItem) return;

        Obstacle o_dragged = draggedItem.getValue() ;

        Obstacle o_survole = item_survole.getValue() ;
        boolean o_survole_est_premier_niveau = (item_survole.getParent()==treeView.getRoot()) ;
        boolean o_survole_est_composition    = o_survole instanceof Composition ;
        // On en peut déposer que sur un obstacle de 1er niveau, ou sur une Composition
        if (!o_survole_est_premier_niveau && !o_survole_est_composition ) return ;

        // On ne peut pas déplacer une Composition dans une de ses sous-compositions
        if ( (o_dragged instanceof Composition) && o_survole_est_composition && o_dragged.comprend(o_survole))
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

            if (o_survole_est_composition && (!o_survole_est_premier_niveau || item_survole.isExpanded()))
                dropZone.setStyle(DROP_HINT_STYLE_DANS);
            else
                dropZone.setStyle(DROP_HINT_STYLE_APRES);

        }
    }

    private void drop(DragEvent event, TreeCell<Obstacle> treeCell, TreeView<Obstacle> treeView) {
        Dragboard db = event.getDragboard();

        if (!db.hasContent(CrazyDiamond.FORMAT_OBSTACLE_ID))  {
            event.setDropCompleted(false);
            return;
        }

        Obstacle o_dragged = draggedItem.getValue() ;
        boolean o_dragged_est_inclus_dans_composition = o_dragged.appartientAComposition() ;

        TreeItem<Obstacle> item_cible_depose = treeCell.getTreeItem(); // Item sur lequel on a déposé
        Obstacle o_cible_depose = item_cible_depose.getValue() ;

        boolean o_cible_depose_est_premier_niveau = (item_cible_depose.getParent()==treeView.getRoot()) ;
        boolean o_cible_depose_est_composition    = o_cible_depose instanceof Composition ;

        // On ne peut pas déplacer une Composition dans une de ses sous-compositions
        if ( (o_dragged instanceof Composition) && o_cible_depose_est_composition && o_dragged.comprend(o_cible_depose))
            return;

        // Dépose dans une sous-composition ou dans une composition de 1er niveau qui est déployée => ajout dans cette composition
        if (o_cible_depose_est_composition && (!o_cible_depose_est_premier_niveau || item_cible_depose.isExpanded())) {
            dropZone.setStyle(DROP_HINT_STYLE_DANS);
            Composition comp_cible = (Composition) o_cible_depose ;

            if (!o_dragged_est_inclus_dans_composition) // Obstacle glissé non inclus dans une composition
                environnement.retirerObstacle(draggedItem.getValue()); // On le retire de l'environnement
            else
                // On le retire de la composition dont il fait partie
                environnement.compositionContenant(o_dragged).retirerObstacle(o_dragged);

            comp_cible.ajouterObstacle(draggedItem.getValue());
        }
        else { // Dépose sur un élément de 1er niveau => Element à positionner au 1er niveau de l'environnement
            dropZone.setStyle(DROP_HINT_STYLE_APRES);

            int indexCibleInParent = item_cible_depose.getParent().getChildren().indexOf(item_cible_depose);
            if (!o_dragged_est_inclus_dans_composition) {
                int indexSourceInParent = item_cible_depose.getParent().getChildren().indexOf(draggedItem);

                if (indexCibleInParent > indexSourceInParent)
                    environnement.deplacerObstacleEnPosition(draggedItem.getValue(), indexCibleInParent);
                else if (indexCibleInParent < indexSourceInParent)
                    environnement.deplacerObstacleEnPosition(draggedItem.getValue(), indexCibleInParent + 1);
            }
            else // L'obstacle glissé est inclus dans une composition
            {
                // On l'en retire
                environnement.compositionContenant(o_dragged).retirerObstacle(o_dragged);

                // On le positionne dans l'environnement
                environnement.insererObstacleEnPosition(draggedItem.getValue(),indexCibleInParent + 1);
            }

            treeView.getSelectionModel().select(draggedItem);
        }
        event.setDropCompleted(true);
    }

    private void clearDropLocation() {
        if (dropZone != null) dropZone.setStyle("");
    }    
}
