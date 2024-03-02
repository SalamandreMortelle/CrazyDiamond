package CrazyDiamond.Controller;

import CrazyDiamond.Model.CommandeSupprimerSource;
import CrazyDiamond.Model.Environnement;
import CrazyDiamond.Model.Source;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.util.Callback;

import java.util.ResourceBundle;


// Credits : https://github.com/cerebrosoft/treeview-dnd-example/tree/master/treedrag

public class SourceListCellFactory implements Callback<ListView<Source>, ListCell<Source>>  {

    private static final ResourceBundle rb = ResourceBundle.getBundle("CrazyDiamond") ;

    private final Environnement environnement ;

    public SourceListCellFactory(Environnement env) {
        this.environnement = env ;
    }

    @Override
    public ListCell<Source> call(ListView<Source> sourceListView) {
        ListCell<Source> cell = new ListCell<>() {

            Source item_courant = null;

            @Override
            protected void updateItem(Source item, boolean empty) {
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
        ContextMenu menuContextuelSources = new ContextMenu() ;
        MenuItem deleteItemSource = new MenuItem(rb.getString("supprimer.source"));
        deleteItemSource.setOnAction( event -> new CommandeSupprimerSource(environnement,cell.getItem()).executer() );
        menuContextuelSources.getItems().add(deleteItemSource);

        cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
            if (isNowEmpty) {
                cell.setContextMenu(null);
            } else {
                cell.setContextMenu(menuContextuelSources);
            }
        });


        return cell;
    }

}
