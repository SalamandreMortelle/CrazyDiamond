package CrazyDiamond.Model;

import CrazyDiamond.Controller.ElementsSelectionnes;

import java.util.ArrayList;
import java.util.Iterator;

public class CommandeImporterElements extends Commande {

    // Le récepteur de la commande
    Environnement environnement ;

    // Parametres
    ElementsSelectionnes elements_a_importer;

    // Etat initial
    ArrayList<ArrayList<Obstacle>> liste_obstacles_socs; // Liste des obstacles initialement présents dans chaque SOC

    /**
     * Supprime les éléments fournis de l'environnement
     * @param environnement : l'environnement dans lequel on supprime les éléments
     * @param elts_a_importer : l'ensemble des éléments à importer.
     */
    public CommandeImporterElements(Environnement environnement, ElementsSelectionnes elts_a_importer) {
        this.environnement = environnement ;
        // Si elts_a_importer référence directement la liste des éléments de la sélection courante, ils risquent d'être
        // modifiés intempestivement si l'utilisateur sélectionne d'autres éléments, ce qu'on ne veut pas (on veut prendre
        // une photo "figée" à un instant donné des éléments qui ont été importés)
        // Pour éviter cela, commençons par faire une copie (non profonde) de la sélection.
        this.elements_a_importer = new ElementsSelectionnes(elts_a_importer) ;

        // NB : la suppression des SOCs (cf. méthode annuler() ci-dessous) entraînera le détachement des obstacles qu'ils
        // contenaient' : ils ne seront plus marqués comme appartenant à un SOC et la liste des obstacles centrés du SOC
        // aura été vidée. L'état du SOC supprimé ainsi que celui des obstacles qu'il contenait auront donc été altérés.
        // De la même manière, la suppression d'un obstacle entraîne son détachement du SOC auquel il appartenait
        // éventuellement. Pour rétablir les éléments sélectionnés, il faudra donc non seulement les ajouter dans
        // l'environnement, mais aussi penser à dans chaque SOC les obstacles qu'il contenait, ce qui suppose de
        // mémoriser les listes d'objets de chaque SOC.
        memoriserEtatInitial();
    }

//    public CommandeImporterElements(Environnement environnement) {
//        this.environnement = environnement ;
//        this.elements_a_importer = new ElementsSelectionnes(environnement.unite()) ;
//
//        // Dé-commenter la ligne suivante si besoin d'enregistrer cette commande directement sans l'exécuter
//        memoriserEtatInitial();
//    }

    @Override
    public void executer() {
        // NB : la suppression des SOCs (cf. méthode annuler() ci-dessous) entraînera le détachement des obstacles qu'ils
        // contenaient' : ils ne seront plus marqués comme appartenant à un SOC et la liste des obstacles centrés du SOC
        // aura été vidée. L'état du SOC supprimé ainsi que celui des obstacles qu'il contenait auront donc été altérés.
        // De la même manière, la suppression d'un obstacle entraîne son détachement du SOC auquel il appartenait
        // éventuellement. Pour rétablir les éléments sélectionnés, il faudra donc non seulement les ajouter dans
        // l'environnement, mais aussi penser à dans chaque SOC les obstacles qu'il contenait, ce qui suppose de
        // mémoriser les listes d'objets de chaque SOC.
//        memoriserEtatInitial();

        elements_a_importer.stream_obstacles().forEach(environnement::ajouterObstacleALaRacine);

        elements_a_importer.stream_sources().forEach(environnement::ajouterSource);

        Iterator<SystemeOptiqueCentre> it_soc = elements_a_importer.iterateur_systemesOptiquesCentres();
        Iterator<ArrayList<Obstacle>>  it_liste_obs = liste_obstacles_socs.iterator() ;
        while (it_soc.hasNext() && it_liste_obs.hasNext()) {
            SystemeOptiqueCentre soc = it_soc.next() ;

            soc.ajouterObstaclesCentres(it_liste_obs.next()) ;
            environnement.ajouterSystemeOptiqueCentre(soc);
        }

        enregistrer();
    }

    private void memoriserEtatInitial() {
        liste_obstacles_socs = new ArrayList<>(elements_a_importer.nombreSystemesOptiquesCentres()) ;
        elements_a_importer.stream_socs().forEach(soc -> {
            ArrayList<Obstacle> liste_obstacles = new ArrayList<>(soc.obstacles_centres().size()) ;
            liste_obstacles.addAll(soc.obstacles_centres());
            liste_obstacles_socs.add(liste_obstacles) ;
        });
    }

    @Override
    public void annuler() {
        elements_a_importer.stream_obstacles().forEach(environnement::supprimerObstacleALaRacine); // Altère les socs qui contenaient les obstacles supprimés
        elements_a_importer.stream_sources().forEach(environnement::supprimerSource);
        elements_a_importer.stream_socs().forEach(environnement::supprimerSystemeOptiqueCentre);
    }

    protected void convertirDistances(double facteur_conversion) {

        Iterator<Obstacle> it_obs = elements_a_importer.iterateur_obstacles();
        while (it_obs.hasNext()) {
            Obstacle obstacle = it_obs.next();
            if (!environnement.obstaclesComprennent(obstacle) && !obstacle.appartientAComposition())
                obstacle.convertirDistances(facteur_conversion);
        }

        Iterator<Source> it_src = elements_a_importer.iterateur_sources();
        while (it_src.hasNext()) {
            Source src = it_src.next();
            if (!environnement.sources().contains(src))
                src.convertirDistances(facteur_conversion);
        }

        Iterator<SystemeOptiqueCentre> it_soc = elements_a_importer.iterateur_systemesOptiquesCentres();
        while (it_soc.hasNext()) {
            SystemeOptiqueCentre soc = it_soc.next();
            if (!environnement.systemesOptiquesCentres().contains(soc))
                soc.convertirDistances(facteur_conversion);
        }
    }
}
