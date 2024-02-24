package CrazyDiamond.Model;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.util.function.Consumer;

public class ChangeListenerAvecGarde<T> implements ChangeListener<T> {

    @Override
    public void changed(ObservableValue<? extends T> observableValue, T old_value, T new_value) {

        if (!changement_en_cours) {
            try {
                changement_en_cours = true;
                consumer.accept(new_value);
            } finally {
                changement_en_cours = false ;
            }
        }

    }

    private boolean changement_en_cours = false ;

    private Consumer<T> consumer ;

    public ChangeListenerAvecGarde(Consumer<T> cons) {
        this.consumer = cons ;
    }

//    /**
//     * @param observableValue
//     * @param old_value
//     * @param new_value
//     */
//    @Override
//    public void changed(ObservableValue<? extends Point2D> observableValue, Point2D old_value, Point2D new_value) {
//        if (!changement_en_cours) {
//            try {
//                changement_en_cours = true;
//
//                consumer.accept(new_value);
//                spinner_xcentre.getValueFactory().valueProperty().set(new_value.getX());
//                spinner_ycentre.getValueFactory().valueProperty().set(new_value.getY());
//            } finally {
//                changement_en_cours = false ;
//            }
//        }
//    }


}
