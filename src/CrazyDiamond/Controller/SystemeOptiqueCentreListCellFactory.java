package CrazyDiamond.Controller;

import CrazyDiamond.CrazyDiamond;
import CrazyDiamond.Model.Environnement;
import CrazyDiamond.Model.Obstacle;
import CrazyDiamond.Model.SystemeOptiqueCentre;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.util.Callback;

import java.util.Objects;


// Credits : https://github.com/cerebrosoft/treeview-dnd-example/tree/master/treedrag

public class SystemeOptiqueCentreListCellFactory implements Callback<ListView<SystemeOptiqueCentre>, ListCell<SystemeOptiqueCentre>>  {
    private static final String DROP_HINT_STYLE = "-fx-border-color: #eea82f; -fx-border-width: 1 1 1 1; -fx-padding: 3 3 1 3";

    private final Environnement environnement ;
    private ListCell<SystemeOptiqueCentre> dropZone;
   // private SystemeOptiqueCentre draggedSystemeOptiqueCentre;


    public SystemeOptiqueCentreListCellFactory(Environnement env) {
        this.environnement = env ;
    }

    @Override
    public ListCell<SystemeOptiqueCentre> call(ListView<SystemeOptiqueCentre> socListView) {
        ListCell<SystemeOptiqueCentre> cell = new ListCell<>() {

            SystemeOptiqueCentre item_courant = null ;
            @Override
            protected void updateItem(SystemeOptiqueCentre item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    textProperty().unbind();
                    setText(null);
                    setGraphic(null);

                    item_courant = null ;

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
                    item_courant = item ;
                }

            }
        };

//        cell.setOnDragDetected((MouseEvent event) -> dragDetected(event, cell, socListView));
        cell.setOnDragOver((DragEvent event) -> dragOver(event, cell, socListView));
        cell.setOnDragDropped((DragEvent event) -> drop(event, cell, socListView));
        cell.setOnDragDone((DragEvent event) -> clearDropLocation());

        return cell;
    }

    private void dragDetected(MouseEvent event, ListCell<SystemeOptiqueCentre> listCell, ListView<SystemeOptiqueCentre> listView) {
        listCell.startDragAndDrop(TransferMode.MOVE);
    }

    private void dragOver(DragEvent event, ListCell<SystemeOptiqueCentre> listCell, ListView<SystemeOptiqueCentre> listView) {
        if (listCell.getItem()==null || !event.getDragboard().hasContent(CrazyDiamond.FORMAT_OBSTACLE_ID)) return;

        Obstacle dragged_obs = environnement.obstacle((String)event.getDragboard().getContent(CrazyDiamond.FORMAT_OBSTACLE_ID)) ;

        if (!dragged_obs.aSymetrieDeRevolution() || dragged_obs.appartientASystemeOptiqueCentre())
            return;

        event.acceptTransferModes(TransferMode.MOVE);
        if (!Objects.equals(dropZone, listCell)) {
            clearDropLocation();
            this.dropZone = listCell;
            dropZone.setStyle(DROP_HINT_STYLE);
        }
    }

    private void drop(DragEvent event, ListCell<SystemeOptiqueCentre> listCell, ListView<SystemeOptiqueCentre> listView) {
        Dragboard db = event.getDragboard();

        if (!db.hasContent(CrazyDiamond.FORMAT_OBSTACLE_ID))
            return;

        Obstacle dragged_obs = environnement.obstacle((String)event.getDragboard().getContent(CrazyDiamond.FORMAT_OBSTACLE_ID)) ;
        SystemeOptiqueCentre soc_cible = listCell.getItem(); // Item sur lequel on a déposé

        if (soc_cible==null || dragged_obs==null) {
            event.setDropCompleted(false);
            return;
        }

        soc_cible.ajouterObstacle(dragged_obs);
        event.setDropCompleted(true);
    }

    private void clearDropLocation() {
        if (dropZone != null) dropZone.setStyle("");
    }    
}
