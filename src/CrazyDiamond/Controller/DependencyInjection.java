package CrazyDiamond.Controller;

import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;

// Code from : https://edencoding.com/dependency-injection/

public class DependencyInjection {

    /**
     * A map of all Controllers that can be injected, and the methods responsible for doing so.
     */
    private static final Map<Class<?>, Callable<?>> injectionMethods = new HashMap<>();

    private static ResourceBundle bundle = null;

    public static void setBundle(ResourceBundle bundle) {
        DependencyInjection.bundle = bundle;
    }

    public static Parent load(String location) throws IOException {

        FXMLLoader loader = getLoader(location);

        Parent parent = loader.load() ;

        // IMPORTANT : Permet de garder une référence au controller pour qu'il ne soit pas supprimé par le Garbage Collector,
        // tant que la vue parent (contrôle graphique) existe. Sans cela, au bout de quelques secondes, il arrive qu'un Parent
        // ne soit plus mis à jour lorsque son Modèle est modifié.
        // Le loader maintient une référence (cf. code source de FXMLLoader/attribut controller) au contrôleur qu'il a créé
        // via l'injection de dépendance, mais le loader peut lui-même être supprimé à tout moment par le GC car il n'est
        // référencé que dans la variable locale "loader" de cette méthode.
        parent.setUserData(loader.getController());

        return parent ;
    }

    public static FXMLLoader getLoader(String location) {
        return new FXMLLoader(
                DependencyInjection.class.getResource(location),
                bundle,
                new JavaFXBuilderFactory(),
                controllerClass -> constructController(controllerClass));
    }

    /**
     * Determine whether a stored method is available
     * If one is, return the custom controller
     * If one is not, return the default controller
     * @param controllerClass the class of controller to be created
     * @return the controller created
     */
    private static Object constructController(Class<?> controllerClass) {
        if(injectionMethods.containsKey(controllerClass)) {
            return loadControllerWithSavedMethod(controllerClass);
        } else {
            return loadControllerWithDefaultConstructor(controllerClass);
        }
    }

    /**
     * Load a controller using the saved method
     * @param controller the class of the controller to be loaded
     * @return the loaded controller
     */
    private static Object loadControllerWithSavedMethod(Class<?> controller){
        try {
            return injectionMethods.get(controller).call();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static Object loadControllerWithDefaultConstructor(Class<?> controller){
        try {
            return controller.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void addInjectionMethod(Class<?> controller, Callable<?> method){
        injectionMethods.put(controller, method);
    }


    public static void removeInjectionMethod(Class<?> controller){
        injectionMethods.remove(controller);
    }

}