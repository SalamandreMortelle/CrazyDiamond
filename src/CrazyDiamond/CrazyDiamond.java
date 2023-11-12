package CrazyDiamond;

import CrazyDiamond.Controller.CanvasAffichageEnvironnement;
import CrazyDiamond.Controller.DependencyInjection;
import CrazyDiamond.Controller.PanneauPrincipal;
import CrazyDiamond.Model.Environnement;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.Level;

public class CrazyDiamond extends Application {

    // Récupération du logger
    private static final Logger LOGGER = Logger.getLogger( "CrazyDiamond" );
    private static final ResourceBundle rb = ResourceBundle.getBundle("CrazyDiamond",CrazyDiamond.class.getModule()) ;

    // Bloc statique pour configurer le gestionnaire de logs, avant toute création d'objet
    static{
            try (InputStream is = CrazyDiamond.class.getClassLoader().getResourceAsStream("logging.properties")) {
                LogManager.getLogManager().readConfiguration(is);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE,"Impossible de charger le fichier logging.properties",e);
            }
    }

    protected CanvasAffichageEnvironnement affichage_environnement_initial_a_charger;

    @Override
    public void start(Stage primaryStage) /*throws Exception*/ {

        LOGGER.log(Level.FINE,"Instanciation de l'Application Crazy Diamond");

        this.setUpDependecyInjector();

        // Création d'un environnement vide
        affichage_environnement_initial_a_charger = new CanvasAffichageEnvironnement(new Environnement());

        Parent root = null ;

        try {
            root = DependencyInjection.load("View/PanneauPrincipal.fxml");
            LOGGER.log(Level.FINE,"Panneau principal créé");
        } catch (IOException e) {
            LOGGER.log( Level.SEVERE, "Exception lors de l'accès au fichier .fxml .",e);
            System.exit(1);
        }

        // Ne pas garder une référence de l'environnement initial, car il est possible qu'un autre environnement soit
        // ultérieurement chargé par l'utilisateur (et passé en paramètre du constructeur d'un nouveau PanneauPrincipal).
        // Ainsi, la mémoire allouée à cet environnement initial, devenue inaccessible sera libérée par le GarbageCollector.
        affichage_environnement_initial_a_charger = null ;

        primaryStage.setTitle(rb.getString("nom_application"));
//        primaryStage.setTitle("Crazy Diamond");

        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

        LOGGER.log(Level.FINE, "VisualBounds primary screen : "+primaryScreenBounds) ;

        // Taille initiale de la fenêtre (hors titre)
        // primaryStage.initStyle(StageStyle.UTILITY);
        // primaryStage.setScene(new Scene(root, 1024 , 768));

        // AVERTISSEMENT : cette façon de tailler la scène à partir de la taille du primaryStage PERTURBE (voire BLOQUE !) le
        // dimensionnement initial de la fenêtre et de ses composants : en fait la primaryStage est faite pour s'adapter
        // à la taille de la scène qu'elle contient (et de ses composants), pas l'inverse.
        //Scene scene = new Scene(root, primaryStage.getMaxWidth() , primaryStage.getMaxHeight()) ;

        // Par défaut, la scène occupera 80% de la largeur et 80% de la hauteur de l'écran principal
        Scene scene = new Scene(root,0.8*primaryScreenBounds.getWidth(),0.8*primaryScreenBounds.getHeight()) ;

        primaryStage.setScene(scene);

        primaryStage.show();

    }

    private void setUpDependecyInjector() {

//        DependencyInjection.setBundle(ResourceBundle.getBundle("CrazyDiamond",Locale.FRENCH));
        DependencyInjection.setBundle(rb);

        // Create factories
        Callable<?> controleurPanneauPrincipalFactory = () -> new PanneauPrincipal(affichage_environnement_initial_a_charger);

        // Save the factory in the injector
        DependencyInjection.addInjectionMethod(PanneauPrincipal.class, controleurPanneauPrincipalFactory);
    }

    public static void main(String[] args) {
        launch(args);
    }

}
