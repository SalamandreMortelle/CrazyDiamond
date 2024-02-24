package CrazyDiamond.Controller;

import CrazyDiamond.CrazyDiamond;
import CrazyDiamond.Model.Environnement;
import CrazyDiamond.Model.Obstacle;
import CrazyDiamond.Model.SystemeOptiqueCentre;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.util.Callback;

import java.util.Objects;
import java.util.ResourceBundle;


// Credits : https://github.com/cerebrosoft/treeview-dnd-example/tree/master/treedrag

public class ArbreSOCTreeCellFactory implements Callback<TreeView<ElementArbreSOC>,/* ArbreSOCTreeCellFactory.TreeCellArbreSoc>*/ TreeCell<ElementArbreSOC>>  {

    private static final ResourceBundle rb = ResourceBundle.getBundle("CrazyDiamond") ;
    private static final String DROP_HINT_STYLE = "-fx-border-color: #eea82f; -fx-border-width: 1 1 1 1; -fx-padding: 3 3 1 3";

    private final Environnement environnement ;
    private TreeCell<ElementArbreSOC> dropZone;

    public ArbreSOCTreeCellFactory(Environnement env) {
        this.environnement = env ;
    }


    @Override
    public TreeCell<ElementArbreSOC> call(TreeView<ElementArbreSOC> socTreeView) {
        TreeCell<ElementArbreSOC> cell = new TreeCell<>() {
            //protected class TreeCellArbreSoc extends TreeCell<ElementArbreSOC> {

            ElementArbreSOC item_courant = null;

            @Override
            protected void updateItem(ElementArbreSOC item, boolean empty) {
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

            }

        } ;

        ContextMenu menuContextuelSocSupprimer = new ContextMenu();
        MenuItem deleteItemSoc = new MenuItem(rb.getString("supprimer.soc"));
        deleteItemSoc.setOnAction(event -> environnement.retirerSystemeOptiqueCentre(cell.getItem().soc));
        menuContextuelSocSupprimer.getItems().add(deleteItemSoc);

        ContextMenu menuContextuelSocRetirerObstacle = new ContextMenu();
        MenuItem retirerItemSoc = new MenuItem(rb.getString("retirer.obstacle.soc"));
        retirerItemSoc.setOnAction(event -> cell.getTreeItem().getParent().getValue().soc
                .retirerObstacleCentre(cell.getItem().obstacle));
        menuContextuelSocRetirerObstacle.getItems().add(retirerItemSoc);

        cell.itemProperty().addListener((obs,old_val,new_val) -> {
            if (new_val==null) {
                cell.setContextMenu(null);
            } else {
                if (new_val.soc!=null)
                    cell.setContextMenu(menuContextuelSocSupprimer);
                else if (new_val.obstacle!=null)
                    cell.setContextMenu(menuContextuelSocRetirerObstacle);
            }
        });

        cell.setOnDragDetected((MouseEvent event) -> dragDetected(event, cell, socTreeView));
        cell.setOnDragOver((DragEvent event) -> dragOver(event, cell, socTreeView));
        cell.setOnDragDropped((DragEvent event) -> drop(event, cell, socTreeView));
        cell.setOnDragDone((DragEvent event) -> clearDropLocation());
        cell.setOnDragExited((DragEvent event) -> clearDropLocation());

        return cell;
    }

    private void dragDetected(MouseEvent event, TreeCell<ElementArbreSOC> treeCell, TreeView<ElementArbreSOC> treeView) {
        treeCell.startDragAndDrop(TransferMode.MOVE);
    }

    private void dragOver(DragEvent event, TreeCell<ElementArbreSOC> treeCell, TreeView<ElementArbreSOC> treeView) {
        if (treeCell.getItem()==null || treeCell.getItem().soc==null || !event.getDragboard().hasContent(CrazyDiamond.FORMAT_OBSTACLE_ID)) {
            clearDropLocation();
            this.dropZone = null ;
            return;
        }

        Obstacle dragged_obs = environnement.obstacle((String)event.getDragboard().getContent(CrazyDiamond.FORMAT_OBSTACLE_ID)) ;

        if (!dragged_obs.aSymetrieDeRevolution() || dragged_obs.appartientASystemeOptiqueCentre() || dragged_obs.appartientAComposition())
            return;

        event.acceptTransferModes(TransferMode.MOVE);
        if (!Objects.equals(dropZone, treeCell)) {
            clearDropLocation();
            this.dropZone = treeCell;
            dropZone.setStyle(DROP_HINT_STYLE);
        }
    }

    private void drop(DragEvent event, TreeCell<ElementArbreSOC> treeCell, TreeView<ElementArbreSOC> treeView) {
        Dragboard db = event.getDragboard();

        if (!db.hasContent(CrazyDiamond.FORMAT_OBSTACLE_ID))
            return;

        Obstacle dragged_obs = environnement.obstacle((String)event.getDragboard().getContent(CrazyDiamond.FORMAT_OBSTACLE_ID)) ;
        ElementArbreSOC el_cible = treeCell.getItem() ;

        if (el_cible.soc==null || dragged_obs==null) {
            event.setDropCompleted(false);
            return;
        }

        SystemeOptiqueCentre soc_cible = el_cible.soc ; // Item sur lequel on a déposé

        // TODO : definir et appeler plutôt une méthode ajouterObstacleParCommande
        soc_cible.ajouterObstacle(dragged_obs);

        event.setDropCompleted(true);

//        treeView.getSelectionModel().select(draggedItem);

        clearDropLocation();

        treeCell.getTreeItem().setExpanded(true);
    }

    private void clearDropLocation() {
        if (dropZone != null) dropZone.setStyle("");
    }    
}
