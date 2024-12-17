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

//    // Récupération du logger
//    private static final Logger LOGGER = Logger.getLogger( "CrazyDiamond" );

    private final Environnement environnement ;
    private TreeCell<Obstacle> drop_zone;
    private TreeItem<Obstacle> dragged_item;


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

        dragged_item = treeCell.getTreeItem();

        // Si on commence un glisser depuis une zone vide de l'arborescence, ne rien faire
        if (dragged_item==null) return ;

        // Seuls les obstacles "libres" (c'est-à-dire ceux qui sont au 1er niveau sous le noeud racine, et qui ne font
        // donc pas partie d'une Composition) peuvent être déplacés.
//        if (draggedItem.getParent()!=treeView.getRoot())
//            return ;

        Dragboard db = treeCell.startDragAndDrop(TransferMode.MOVE);

        ClipboardContent content = new ClipboardContent();
        content.put(CrazyDiamond.FORMAT_OBSTACLE_ID, dragged_item.getValue().id()) ;
        db.setContent(content);
        db.setDragView(treeCell.snapshot(null, null));
        event.consume();
    }

    private void dragOver(DragEvent event, TreeCell<Obstacle> treeCell, TreeView<Obstacle> treeView) {
        if (!event.getDragboard().hasContent(CrazyDiamond.FORMAT_OBSTACLE_ID)) return;

        TreeItem<Obstacle> item_survole = treeCell.getTreeItem();

        // can't drop on itself
        if (dragged_item == null || item_survole == dragged_item) return;

        Obstacle o_dragged = dragged_item.getValue() ;

        if (item_survole!=null) {
            Obstacle o_survole = item_survole.getValue();

            boolean o_dragged_est_a_la_racine    = (o_dragged.parent() != null && o_dragged.parent().parent()==null) ;
            boolean o_survole_est_a_la_racine    = (o_survole==null) || (o_survole.parent() != null && o_survole.parent().parent()==null) ;

            boolean o_survole_est_premier_niveau = (item_survole.getParent() == treeView.getRoot());
            boolean o_survole_est_composition = o_survole instanceof Composition;
            boolean o_survole_est_groupe = o_survole instanceof Groupe;

            // On ne peut pas déplacer une Composition dans une de ses sous-compositions
            if (deplacementInterdit(o_dragged, item_survole, o_survole, o_dragged_est_a_la_racine, o_survole_est_a_la_racine, o_survole_est_composition, o_survole_est_groupe))
                return;

//            // ignore if this is the root ; inutile car le treeitem racine existe mais n'est pas affiché
//            if (dragged_item.getParent() == null) {
//                clearDropLocation();
//                return;
//            }

            event.acceptTransferModes(TransferMode.MOVE);

            if (!Objects.equals(drop_zone, treeCell)) {
                clearDropLocation();
                this.drop_zone = treeCell;

                // Dépose dans une sous-composition ou dans une composition de 1er niveau qui est déployée ou qui est vide => on peut déposer dedans
                // De plus, on ne peut pas ajouter un Groupe dans une Composition
                if  (  ( o_survole_est_composition
                        && ( /*!o_survole_est_premier_niveau ||*/ item_survole.isExpanded() || (((Composition) o_survole).estVide()))
                        && (!(o_dragged instanceof ElementSansEpaisseur))
                        && (!(o_dragged instanceof Groupe) ) )
                    || ( o_survole_est_groupe
                        && ( item_survole.isExpanded() || (((Groupe) o_survole).estVide()))
                       )
                )
                    drop_zone.setStyle(DROP_HINT_STYLE_DANS);
                else
                    drop_zone.setStyle(DROP_HINT_STYLE_APRES);

            }
        } else { // Item survole est vide : cela veut dire qu'on déposera à la fin de la liste des obstacles de 1er niveau

            event.acceptTransferModes(TransferMode.MOVE);
        }
    }

    private void drop(DragEvent event, TreeCell<Obstacle> treeCell, TreeView<Obstacle> treeView) {
        Dragboard db = event.getDragboard();

        if (!db.hasContent(CrazyDiamond.FORMAT_OBSTACLE_ID))  {
            event.setDropCompleted(false);
            return;
        }

        Obstacle o_dragged = dragged_item.getValue() ;

        TreeItem<Obstacle> item_cible_depose = treeCell.getTreeItem(); // Item sur lequel on a déposé

        Obstacle o_cible_depose = (item_cible_depose!=null?item_cible_depose.getValue():null) ;

        boolean o_dragged_est_a_la_racine    = (o_dragged.parent() != null && o_dragged.parent().parent()==null) ;
        boolean o_cible_depose_est_a_la_racine    = (o_cible_depose==null) || (o_cible_depose.parent() != null && o_cible_depose.parent().parent()==null) ;

        boolean o_cible_depose_est_composition    = (item_cible_depose!=null) && (o_cible_depose instanceof Composition) ;
        boolean o_cible_depose_est_groupe = (item_cible_depose!=null) && (o_cible_depose instanceof Groupe) ;

        Obstacle o_racine = treeView.getRoot().getValue() ;

        if (deplacementInterdit(o_dragged, item_cible_depose, o_cible_depose, o_dragged_est_a_la_racine, o_cible_depose_est_a_la_racine, o_cible_depose_est_composition, o_cible_depose_est_groupe))
            return;

        effectuerDeplacementItem(treeView, o_dragged, item_cible_depose, o_cible_depose, o_cible_depose_est_composition, o_cible_depose_est_groupe, o_racine);

        event.setDropCompleted(true);
    }

    private static boolean deplacementInterdit(Obstacle o_dragged, TreeItem<Obstacle> item_cible_depose, Obstacle o_cible_depose, boolean o_dragged_est_a_la_racine, boolean o_cible_depose_est_a_la_racine, boolean o_cible_depose_est_composition, boolean o_cible_depose_est_groupe) {
        // On ne peut pas déplacer une Composition dans une de ses sous-compositions
        if ( (o_dragged instanceof Composition) /*&& o_cible_depose_est_composition*/ && o_dragged.comprend(o_cible_depose))
            return true;
        // On ne peut pas déplacer un Groupe dans un de ses sous-groupes
        if ((o_dragged instanceof Groupe) /* && o_cible_depose_est_groupe */ && o_dragged.comprend(o_cible_depose))
            return true;
        // On ne peut pas déplacer un Groupe dans une Composition
        if ((o_dragged instanceof Groupe)
                && ( ( o_cible_depose_est_composition && (item_cible_depose.isExpanded() || ((Composition) o_cible_depose).estVide()) )
                       || (o_cible_depose !=null && o_cible_depose.parent() instanceof Composition) ))
            return true;

        // On ne peut pas mettre un ElementSansEpaisseur sur une Composition ou après l'un des éléments qui en fait partie
        if ( (o_dragged instanceof ElementSansEpaisseur)
                && ( (o_cible_depose_est_composition && (item_cible_depose.isExpanded() || ((Composition) o_cible_depose).estVide()) ) // Dépose sur une Composition
                  || (o_cible_depose !=null && o_cible_depose.parent() instanceof Composition) ) // Ou dans une composition
                 )
            return true;

        if (o_dragged.appartientASystemeOptiqueCentre() && !o_dragged.parent().appartientASystemeOptiqueCentre() && !o_dragged_est_a_la_racine)
            throw new IllegalStateException("L'obstacle déplacé appartient au éléments racine d'un SOC mais n'est pas à la racine de l'environnement") ;


        // Un élément d'un SOC doit toujours rester un élément de 1er niveau : on ne peut le mettre dans un groupe ou dans une composition
        if ( (o_dragged.appartientASystemeOptiqueCentre() && !o_dragged.parent().appartientASystemeOptiqueCentre())
                && ( !o_cible_depose_est_a_la_racine
                     || ( o_cible_depose_est_groupe
                         && ( item_cible_depose.isExpanded() || (((Groupe) o_cible_depose).estVide()) ))
                     || (o_cible_depose_est_composition
                         && (item_cible_depose.isExpanded() || ( ((Composition) o_cible_depose).estVide() ) ) ) ) )
            return true;
        return false;
    }

    private void effectuerDeplacementItem(TreeView<Obstacle> treeView, Obstacle o_dragged, TreeItem<Obstacle> item_cible_depose, Obstacle o_cible_depose, boolean o_cible_depose_est_composition, boolean o_cible_depose_est_groupe, Obstacle o_racine) {
        // Dépose directe sur une composition ou sur un groupe qui est déployé ou qui est vide => ajout dans cette
        // composition. De plus, on ne peut pas ajouter un Groupe dans une Composition
        if ( (o_cible_depose_est_composition
                && (item_cible_depose.isExpanded() || ( ((Composition) o_cible_depose).estVide() ) ) )
        || ( o_cible_depose_est_groupe
                && ( item_cible_depose.isExpanded() || (((Groupe) o_cible_depose).estVide()) )) ) {

            drop_zone.setStyle(DROP_HINT_STYLE_DANS);

            // o_cible_depose sera le nouveau parent
            BaseObstacleComposite boc_cible = (BaseObstacleComposite) o_cible_depose;

            int indexSourceInParent = o_dragged.parent().indexALaRacine(o_dragged) ;

            // Pos Cible = dernière position
            int indexCibleInParent = ((BaseObstacleComposite) o_cible_depose).nombreObstaclesPremierNiveau()-1 ;

            if (o_cible_depose != o_dragged.parent()) // Déplacement vers un parent (composite) distinct
                new CommandeDeplacerObstacleDansComposite(environnement, o_dragged, o_cible_depose,indexCibleInParent+1).executer();
            else { // Déplacement à l'intérieur d'un même parent, au même niveau
                if (indexCibleInParent > indexSourceInParent) // Pos Cible est forcément >= pos source car on vise la dernière position (si = ne rien faire, l'obstacle est déjà à sa place cible)
                    new CommandeDeplacerObstacleDansComposite(environnement, o_dragged, o_cible_depose, indexCibleInParent).executer();
            }
        }
        else {// Dépose dans un groupe ou un sous-groupe, à une position précise càd qu'on ne dépose pas sur la racine
              // [o_cible_depose n'est ni un groupe expanded (ou vide) ni une composition expanded (ou vide)]
            if (drop_zone !=null)
                drop_zone.setStyle(DROP_HINT_STYLE_APRES);

            int indexSourceInParent = o_dragged.parent().indexALaRacine(o_dragged) ;

            int indexCibleInParent = (item_cible_depose !=null?
                    item_cible_depose.getParent().getChildren().indexOf(item_cible_depose)
                    : treeView.getRoot().getChildren().size()-1);

            Obstacle o_parent_cible = (item_cible_depose !=null? item_cible_depose.getParent().getValue(): o_racine) ;

            if ((o_cible_depose !=null && o_cible_depose.parent()!= o_dragged.parent())
                           || (o_cible_depose ==null && o_racine != o_dragged.parent() )) // L'obstacle déplacé n'a pas le même parent que l'obstacle cible après lequel on veut insérer l'objet
                new CommandeDeplacerObstacleDansComposite(environnement, o_dragged,o_parent_cible,indexCibleInParent+1).executer();
            else { // Déplacement à l'intérieur d'un même parent, au même niveau
                  if (indexCibleInParent > indexSourceInParent)
                    new CommandeDeplacerObstacleDansComposite(environnement, o_dragged,o_parent_cible,indexCibleInParent).executer();
                else if (indexCibleInParent < indexSourceInParent)
                    new CommandeDeplacerObstacleDansComposite(environnement, o_dragged,o_parent_cible,indexCibleInParent+1).executer();
            }

            treeView.getSelectionModel().select(dragged_item);
        }
    }

    private void clearDropLocation() {
        if (drop_zone != null) drop_zone.setStyle("");
    }    
}
