package CrazyDiamond.Controller;

import CrazyDiamond.CrazyDiamond;
import CrazyDiamond.Model.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.util.Callback;

import java.util.Objects;
import java.util.ResourceBundle;


// Credits : https://github.com/cerebrosoft/treeview-dnd-example/tree/master/treedrag

public class ArbreSOCTreeCellFactory implements Callback<TreeView<ElementDeSOC>,/* ArbreSOCTreeCellFactory.TreeCellArbreSoc>*/ TreeCell<ElementDeSOC>>  {

    private static final ResourceBundle rb = ResourceBundle.getBundle("CrazyDiamond") ;
    private static final String DROP_HINT_STYLE = "-fx-border-color: #eea82f; -fx-border-width: 1 1 1 1; -fx-padding: 3 3 1 3";

    private final Environnement environnement ;
    private TreeCell<ElementDeSOC> dropZone;
    private TreeItem<ElementDeSOC> dragged_item;

    public ArbreSOCTreeCellFactory(Environnement env) {
        this.environnement = env ;
    }


    @Override
    public TreeCell<ElementDeSOC> call(TreeView<ElementDeSOC> socTreeView) {
        TreeCell<ElementDeSOC> cell = new TreeCell<>() {
            ElementDeSOC item_courant = null;

            @Override
            protected void updateItem(ElementDeSOC item, boolean empty) {
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
        deleteItemSoc.setOnAction(event ->
            new CommandeSupprimerSystemeOptiqueCentre(environnement,(SystemeOptiqueCentre) cell.getItem()).executer()
        );
        menuContextuelSocSupprimer.getItems().add(deleteItemSoc);

        ContextMenu menuContextuelSocRetirerObstacle = new ContextMenu();
        MenuItem retirerItemSoc = new MenuItem(rb.getString("retirer.obstacle.soc"));
        retirerItemSoc.setOnAction(event ->
            new CommandeRetirerElementsDeSystemeOptiqueCentre((SystemeOptiqueCentre) cell.getTreeItem().getParent().getValue(), (Obstacle) cell.getItem()).executer()
        );
        menuContextuelSocRetirerObstacle.getItems().add(retirerItemSoc);

        cell.itemProperty().addListener((obs,old_val,new_val) -> {
            if (new_val==null) {
                cell.setContextMenu(null);
            } else {
                if (new_val.estUnSOC())
                    cell.setContextMenu(menuContextuelSocSupprimer);
                else if (new_val.estUnObstacle())
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

    private void dragDetected(MouseEvent event, TreeCell<ElementDeSOC> treeCell, TreeView<ElementDeSOC> treeView) {
        dragged_item = treeCell.getTreeItem();

        // Si on commence un glisser depuis une zone vide de l'arborescence, ne rien faire
        if (dragged_item==null) return ;

        Dragboard db = treeView.startDragAndDrop(TransferMode.MOVE);
//        Dragboard db = treeCell.startDragAndDrop(TransferMode.MOVE);
        // NB : il est possible d'associer une image au dragboard pour que l'utilisateur visualise ce qu'il déplace

        ClipboardContent content = new ClipboardContent();
        //  On ne peut mettre que des ByteBuffer ou des objets sérialisables dans le dragboard : il faut peut-être
        //  rendre le SOC identifiable et passer son id (comme on le fait pour les obstacles dans ObstacleTreeCellFactory)
        // Contentons-nous d'utiliser son hashCode, c'est plus simple
//        content.put(CrazyDiamond.FORMAT_SOC, dragged_item.getValue()) ;
        if (dragged_item.getValue() instanceof SystemeOptiqueCentre s)
            content.put(CrazyDiamond.FORMAT_SOC, s.hashCode()) ;
        else if (dragged_item.getValue() instanceof Obstacle o)
            content.put(CrazyDiamond.FORMAT_OBSTACLE_ID, o.id()) ;
        db.setContent(content);
        db.setDragView(treeCell.snapshot(null, null));
        event.consume();

    }

    private void dragOver(DragEvent event, TreeCell<ElementDeSOC> treeCell, TreeView<ElementDeSOC> treeView) {

        Dragboard db = event.getDragboard();

        if (!deposerElementPossible(db,treeCell)) {
            clearDropLocation();
            this.dropZone = null ;
            return;
        }

        TreeItem<ElementDeSOC> item_survole = treeCell.getTreeItem();

        if (item_survole!=null) {
            SystemeOptiqueCentre soc_survole = (SystemeOptiqueCentre) item_survole.getValue() ;
            if (db.hasContent(CrazyDiamond.FORMAT_OBSTACLE_ID)) {
                Obstacle dragged_obs = environnement.obstacle((String) db.getContent(CrazyDiamond.FORMAT_OBSTACLE_ID));
                // Si l'obstacle déplacé ne provient pas de l'arbre des SOC (càd qu'il ne faisait pas partie d'un SOC),
                // il vient de l'arbre des obstacles et il faut s'assurer de son éligibilité dans le SOC cible
                if (event.getGestureSource()!=treeView && !soc_survole.estEligiblePourAjout(dragged_obs))
                    return;
            } else {// Si ce n'est un obstacle, ça ne peut plus être qu'un SOC
                SystemeOptiqueCentre dragged_soc = environnement.systemesOptiquesCentre((Integer) db.getContent(CrazyDiamond.FORMAT_SOC));
                if (!soc_survole.estEligiblePourAjout(dragged_soc))
                    return;
            }
        } else {
            if (db.hasContent(CrazyDiamond.FORMAT_OBSTACLE_ID))
                return; // On ne peut pas déplacer un obstacle sur une zone vide de l'arborecence (possible que pour les SOCs)
            else if (environnement.systemesOptiquesCentre((Integer) db.getContent(CrazyDiamond.FORMAT_SOC)).SOCParent()==null)
                return; // On ne peut pas déplacer à la racine un SOC qui s'y trouve déjà
        }

        // Le transfert ici est possible
        event.acceptTransferModes(TransferMode.MOVE);

        if (!Objects.equals(dropZone, treeCell)) { // Si on change de zone de dépose
            clearDropLocation(); // On ne marque plus la précédente zone

            this.dropZone = treeCell;

            if (item_survole!=null) // Si on ne survole pas une zone vide
                dropZone.setStyle(DROP_HINT_STYLE); // Marquer la nouvelle zone
        }
    }

    private void drop(DragEvent event, TreeCell<ElementDeSOC> treeCell, TreeView<ElementDeSOC> treeView) {
        Dragboard db = event.getDragboard();

//        if (!db.hasContent(CrazyDiamond.FORMAT_OBSTACLE_ID))
//            return;

        if ( !deposerElementPossible(db,treeCell)) {
            event.setDropCompleted(false);
            return;
        }

        TreeItem<ElementDeSOC> item_survole = treeCell.getTreeItem();

        if (item_survole!=null) {
            // A ce stade item_survole ne peut plus être qu'un SOC
            SystemeOptiqueCentre soc_survole = (SystemeOptiqueCentre) item_survole.getValue() ;
            if (db.hasContent(CrazyDiamond.FORMAT_OBSTACLE_ID)) {

                Obstacle dragged_obs = environnement.obstacle((String) db.getContent(CrazyDiamond.FORMAT_OBSTACLE_ID));
                if (event.getGestureSource()!=treeView && !soc_survole.estEligiblePourAjout(dragged_obs)) {
                    event.setDropCompleted(false);
                    return;
                }

                new CommandeAjouterElementsDansSystemeOptiqueCentre(environnement,soc_survole,dragged_obs).executer();

            } else { // Si ce n'est un obstacle, ça ne peut plus être qu'un SOC

                SystemeOptiqueCentre dragged_soc = environnement.systemesOptiquesCentre((Integer) db.getContent(CrazyDiamond.FORMAT_SOC));
                if (!soc_survole.estEligiblePourAjout(dragged_soc)) {
                    event.setDropCompleted(false);
                    return;
                }

                new CommandeAjouterElementsDansSystemeOptiqueCentre(environnement,soc_survole,dragged_soc).executer();
            }
        } else { // item_survole==null


            if (db.hasContent(CrazyDiamond.FORMAT_OBSTACLE_ID))
                return; // On ne peut pas déplacer un obstacle sur une zone vide de l'arborecence (possible que pour les SOCs)
            // On sait déjà que c'est un SOC qui est déplacé
            SystemeOptiqueCentre dragged_soc = environnement.systemesOptiquesCentre((Integer) db.getContent(CrazyDiamond.FORMAT_SOC));

            // Si dragged_soc est déjà à la racine, ou si c'est un obstacle qu'on met sur la zone vide, ne rien faire
            if (dragged_soc.SOCParent()==null || db.hasContent(CrazyDiamond.FORMAT_OBSTACLE_ID)) {
                event.setDropCompleted(false);
                return;
            }

            new CommandeDeplacerSystemeOptiqueCentreDansRacineEnvironnement(environnement,dragged_soc).executer();
        }

//        if (db.hasContent(CrazyDiamond.FORMAT_OBSTACLE_ID)) {
//            Obstacle dragged_obs = environnement.obstacle((String) db.getContent(CrazyDiamond.FORMAT_OBSTACLE_ID));
//            ElementDeSOC el_cible = treeCell.getItem();
//
//            if (!el_cible.estUnSOC() || dragged_obs == null) {
//                event.setDropCompleted(false);
//                return;
//            }
//        }


        event.setDropCompleted(true);

//        treeView.getSelectionModel().select(draggedItem);

        clearDropLocation();

        if (treeCell.getTreeItem()!=null)
            treeCell.getTreeItem().setExpanded(true);
    }

    private boolean deposerElementPossible(Dragboard db, TreeCell<ElementDeSOC> cell_cible) {

        Obstacle dragged_obs = null ;
        SystemeOptiqueCentre dragged_soc = null ;

        if (db.hasContent(CrazyDiamond.FORMAT_OBSTACLE_ID))
            dragged_obs = environnement.obstacle((String) db.getContent(CrazyDiamond.FORMAT_OBSTACLE_ID));
        if (db.hasContent(CrazyDiamond.FORMAT_SOC))
            dragged_soc = environnement.systemesOptiquesCentre((Integer) db.getContent(CrazyDiamond.FORMAT_SOC));

        return (db.hasContent(CrazyDiamond.FORMAT_OBSTACLE_ID) || db.hasContent(CrazyDiamond.FORMAT_SOC))
                // On ne peut déposer que sur un SOC, si on dépose sur un élement et il faut alors s'assurer de l'éligibilité
                && (cell_cible.getItem() == null
                    || (cell_cible.getItem().estUnSOC() && (cell_cible.getItem() instanceof SystemeOptiqueCentre soc)
                                                             && ( (dragged_soc!=null && soc.estEligiblePourAjout(dragged_soc))
                                                                  || (dragged_obs!=null&&soc.estEligiblePourAjout(dragged_obs))) ) )
                // Dans une zone vide, on ne peut déplacer qu'un SOC (pour en faire un SOC de 1er niveau)
                && (cell_cible.getItem() != null || db.hasContent(CrazyDiamond.FORMAT_SOC));
    }

    private void clearDropLocation() {
        if (dropZone != null) dropZone.setStyle("");
    }    
}
