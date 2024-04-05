package CrazyDiamond.Model;

import CrazyDiamond.Controller.ElementsSelectionnes;

import java.util.ArrayList;
import java.util.Iterator;

public class CommandeSupprimerElements extends Commande {

    // Le récepteur de la commande
    Environnement environnement ;

    // Parametres
    ElementsSelectionnes elements_a_supprimer ;

    // Etat initial
    ArrayList<ArrayList<Obstacle>> liste_obstacles_socs; // Liste des obstacles initialement présents dans chaque SOC

    /**
     * Supprime les éléments fournis de l'environnement
     * @param environnement : l'environnement dans lequel on supprime les éléments
     * @param es_a_supprimer : l'ensemble des éléments à supprimer. Attention : les obstacles à supprimer doivent être
     *                       ordonnés de la même manière que dans l'environnement. C'est actuellement garanti "par construction"
     *                       du fait de la manière dont on ajoute des obstacles dans la sélection :
     *                       - soit on ajoute un obstacle unique
     *                       - soit on ajoute, dans l'ordre tous les éléments de l'environnement dans l'ordre (via Ctrl+A)
     *                       - soit on en ajoute que certains via une sélection rectangulaire, mais on repère les obstacles
     *                       qui font partie de cette sélection grâce à un parcours ordonné des obstacles de l'environnement.
     */
    public CommandeSupprimerElements(Environnement environnement, ElementsSelectionnes es_a_supprimer) {
        this.environnement = environnement ;
        // Le retrait des obstacles, sources et socs de l'environnement altèrera (cf. callbacks ListChangeListener dans
        // l'Environnement) les éléments de la sélection courante que l'on est en train de parcourir, ce qui lèverait une exception.
        // Pour éviter cela, commençons par faire une copie (non profonde) de la sélection.
        this.elements_a_supprimer = new ElementsSelectionnes(es_a_supprimer) ;

        // Dé-commenter la ligne suivante si besoin d'enregistrer cette commande directement sans l'exécuter
        // memoriserEtatInitial();
    }

    @Override
    public void executer() {
        // NB : la suppression des SOCs (cf. ci-dessous) entraînera le détachement des obstacles qu'ils contenaient' :
        // ils ne seront plus marqués comme appartenant à un SOC et la liste des obstacles centrés du SOC aura été vidée.
        // L'état du SOC supprimé ainsi que celui des obstacles qu'il contenait auront donc été altérés.
        // De la même manière, la suppression d'un obstacle entraîne son détachement du SOC auquel il appartenait
        // éventuellement. Pour rétablir les éléments sélectionnés, il faudra donc non seulement les ajouter dans
        // l'environnement, mais aussi penser à dans chaque SOC les obstacles qu'il contenait, ce qui suppose de
        // mémoriser les listes d'objets de chaque SOC.
        memoriserEtatInitial();

        elements_a_supprimer.stream_obstacles().forEach(environnement::supprimerObstacleALaRacine);
        elements_a_supprimer.stream_sources().forEach(environnement::supprimerSource);
        elements_a_supprimer.stream_socs().forEach(environnement::supprimerSystemeOptiqueCentre);


        enregistrer();
    }

    private void memoriserEtatInitial() {
        liste_obstacles_socs = new ArrayList<>(elements_a_supprimer.nombreSystemesOptiquesCentres()) ;
        elements_a_supprimer.stream_socs().forEach(soc -> {
            ArrayList<Obstacle> liste_obstacles = new ArrayList<>(soc.obstacles_centres().size()) ;
            liste_obstacles.addAll(soc.obstacles_centres());
            liste_obstacles_socs.add(liste_obstacles) ;
        });
    }

    @Override
    public void annuler() {

        elements_a_supprimer.stream_obstacles().forEach(environnement::ajouterObstacleALaRacine);
        elements_a_supprimer.stream_sources().forEach(environnement::ajouterSource);

        Iterator<SystemeOptiqueCentre> it_soc = elements_a_supprimer.iterateur_systemesOptiquesCentres();
        Iterator<ArrayList<Obstacle>>  it_liste_obs = liste_obstacles_socs.iterator() ;
        while (it_soc.hasNext() && it_liste_obs.hasNext()) {
            SystemeOptiqueCentre soc = it_soc.next() ;
            soc.ajouterObstaclesCentres(it_liste_obs.next()) ;
            environnement.ajouterSystemeOptiqueCentre(soc);
        }


    }

    protected void convertirDistances(double facteur_conversion) {

        Iterator<Obstacle> it_obs = elements_a_supprimer.iterateur_obstacles();
        while (it_obs.hasNext()) {
            Obstacle obstacle = it_obs.next();
            if (!environnement.obstaclesComprennent(obstacle) && !obstacle.appartientAComposition())
                obstacle.convertirDistances(facteur_conversion);
        }

        Iterator<Source> it_src = elements_a_supprimer.iterateur_sources();
        while (it_src.hasNext()) {
            Source src = it_src.next();
            if (!environnement.sources().contains(src))
                src.convertirDistances(facteur_conversion);
        }

        Iterator<SystemeOptiqueCentre> it_soc = elements_a_supprimer.iterateur_systemesOptiquesCentres();
        while (it_soc.hasNext()) {
            SystemeOptiqueCentre soc = it_soc.next();
            if (!environnement.systemesOptiquesCentres().contains(soc))
                soc.convertirDistances(facteur_conversion);
        }
    }
}
