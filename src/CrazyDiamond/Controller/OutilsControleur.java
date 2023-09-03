package CrazyDiamond.Controller;

import CrazyDiamond.Model.ChangeListenerAvecGarde;
import javafx.application.Platform;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

import java.util.function.Consumer;

public class OutilsControleur {
    // Version sans callback de mise à jour du modèle (utile quand un bindBidirectional a déjà été fait)
    protected static void integrerSpinnerDoubleValidantAdaptatifPourCanvas(CanvasAffichageEnvironnement canvas, Spinner<Double> spinner, double val_init) {
        integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner,val_init,null);
    }
    protected static void integrerSpinnerDoubleValidantAdaptatifPourCanvas(CanvasAffichageEnvironnement canvas, Spinner<Double> spinner, double val_init, Consumer<Double> consumer) {

        ConvertisseurDoubleValidantAffichageDistance conv = new ConvertisseurDoubleValidantAffichageDistance(canvas, spinner.getValueFactory().valueProperty()) ;
        spinner.getValueFactory().setConverter(conv);

        spinner.getValueFactory().valueProperty().set(val_init);

        // Attention : ce callback ne se déclenche pas si la new_value est égale à la old_value, ce qui arrive
        // lorsqu'on tente de saisir une valeur interdite dans le Spinner (cf. méthode
        // ConvertisseurDoubleValidant::fromString() qui va alors retourner la valeur courante de la
        // valueProperty du spinner) => Dans ce cas, la value du Spinner est inchangée (ce qui est une bonne chose)
        // mais le champ texte du Spinner continue d'afficher la valeur invalide. Pour cette raison, il faut remettre
        // à jour le contenu du champ texte quand il n'a plus le focus
        if (consumer!=null)
            spinner.getValueFactory().valueProperty().addListener(new ChangeListenerAvecGarde<Double>(consumer)) ;

        spinner.getEditor().focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) // Perte du focus => on remet la valeur courante du spinner dans le textfield
                spinner.getEditor().setText(conv.toString(spinner.getValue()));
            else // Gain du focus : sélection du contenu
                Platform.runLater(spinner.getEditor()::selectAll);
        });

        canvas.ajustePasEtAffichageSpinnerValueFactoryDistance((SpinnerValueFactory.DoubleSpinnerValueFactory) spinner.getValueFactory());

    }
    protected static void integrerSpinnerDoubleValidant(Spinner<Double> spinner, double val_init) {
        integrerSpinnerDoubleValidant(spinner,val_init,null);
    }
    protected static void integrerSpinnerDoubleValidant(Spinner<Double> spinner, double val_init, Consumer<Double> consumer) {

        ConvertisseurDoubleValidant conv = new ConvertisseurDoubleValidant(spinner.getValueFactory().valueProperty()) ;
        spinner.getValueFactory().setConverter(conv);

        spinner.getValueFactory().valueProperty().set(val_init);

        // Attention : ce callback ne se déclenche pas si la new_value est égale à la old_value, ce qui arrive
        // lorsqu'on tente de saisir une valeur interdite dans le Spinner (cf. méthode
        // ConvertisseurDoubleValidant::fromString() qui va alors retourner la valeur courante de la
        // valueProperty du spinner) => Dans ce cas, la value du Spinner est inchangée (ce qui est une bonne chose)
        // mais le champ texte du Spinner continue d'afficher la valeur invalide. Pour cette raison, il faut remettre
        // à jour le contenu du champ texte quand il n'a plus le focus
        if (consumer!=null)
            spinner.getValueFactory().valueProperty().addListener(new ChangeListenerAvecGarde<Double>(consumer)) ;

        spinner.getEditor().focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) // Perte du focus => on remet la valeur courante du spinner dans le textfield
                spinner.getEditor().setText(conv.toString(spinner.getValue()));
            else // Gain du focus : sélection du contenu
                Platform.runLater(spinner.getEditor()::selectAll);
        });


    }

    protected static void integrerSpinnerEntierValidant(Spinner<Integer> spinner, Integer val_init/*, Consumer<Double> consumer*/) {

        ConvertisseurEntierValidant conv = new ConvertisseurEntierValidant(spinner.getValueFactory().valueProperty()) ;
        spinner.getValueFactory().setConverter(conv);

        spinner.getValueFactory().valueProperty().set(val_init);

        // Attention : ce callback ne se déclenche pas si la new_value est égale à la old_value, ce qui arrive
        // lorsqu'on tente de saisir une valeur interdite dans le Spinner (cf. méthode
        // ConvertisseurDoubleValidant::fromString() qui va alors retourner la valeur courante de la
        // valueProperty du spinner) => Dans ce cas, la value du Spinner est inchangée (ce qui est une bonne chose)
        // mais le champ texte du Spinner continue d'afficher la valeur invalide. Pour cette raison, il faut remettre
        // à jour le contenu du champ texte quand il n'a plus le focus
/*        if (consumer!=null)
            spinner.getValueFactory().valueProperty().addListener(new ChangeListenerAvecGarde<Double>(consumer)) ;
*/
        spinner.getEditor().focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) // Perte du focus => on remet la valeur courante du spinner dans le textfield
                spinner.getEditor().setText(conv.toString(spinner.getValue()));
            else // Gain du focus : sélection du contenu
                Platform.runLater(spinner.getEditor()::selectAll);
        });


    }


}
