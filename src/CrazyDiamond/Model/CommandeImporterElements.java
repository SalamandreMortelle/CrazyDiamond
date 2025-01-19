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
    ArrayList<ArrayList<ElementDeSOC>> listes_elements_initiaux_des_socs; // Liste des obstacles initialement présents dans chaque SOC

    /**
     * Importe les éléments fournis dans l'environnement
     * @param environnement : l'environnement dans lequel on importe les éléments
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
        // contenaient : ceux-ci ne seront plus marqués comme appartenant à un SOC et la liste des obstacles centrés du SOC
        // aura été vidée. L'état du SOC supprimé ainsi que celui des obstacles qu'il contenait auront donc été altérés.
        // De la même manière, la suppression d'un obstacle entraîne son détachement du SOC auquel il appartenait
        // éventuellement. Pour rétablir les éléments sélectionnés, il faudra donc non seulement les ajouter dans
        // l'environnement, mais aussi penser à ajouter dans chaque SOC les obstacles qu'il contenait, ce qui suppose de
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

    private void memoriserEtatInitial() {
        listes_elements_initiaux_des_socs = new ArrayList<>(elements_a_importer.nombreSystemesOptiquesCentres()) ;
        elements_a_importer.stream_socs().forEach(soc -> {
//            ArrayList<ElementDeSOC> liste_elements = new ArrayList<>(soc.elements_centres_premier_niveau().size()) ;
//            liste_elements.addAll(soc.elements_centres_premier_niveau());
//            listes_elements_initiaux_des_socs.add(liste_elements) ;
            listes_elements_initiaux_des_socs.add(new ArrayList<>(soc.elementsCentresRacine())) ;
        });
    }

    @Override
    public void executer() {
        // NB : la suppression des SOCs (cf. méthode annuler() ci-dessous) entraînera le détachement des obstacles qu'ils
        // contenaient' : ils ne seront plus marqués comme appartenant à un SOC et la liste des obstacles centrés du SOC
        // aura été vidée. L'état du SOC supprimé ainsi que celui des obstacles qu'il contenait auront donc été altérés.
        // De la même manière, la suppression d'un obstacle entraîne son détachement du SOC auquel il appartenait
        // éventuellement. Pour rétablir les éléments sélectionnés, il faudra ainsi non seulement les ajouter dans
        // l'environnement, mais aussi penser à ajouter dans chaque SOC les obstacles qu'il contenait, ce qui suppose de
        // mémoriser les listes d'objets de chaque SOC.
//        memoriserEtatInitial();

        elements_a_importer.stream_obstacles().forEach(environnement::ajouterObstacleALaRacine);

        elements_a_importer.stream_sources().forEach(environnement::ajouterSource);

        Iterator<SystemeOptiqueCentre> it_soc = elements_a_importer.iterateur_systemesOptiquesCentres();
//        Iterator<ArrayList<ElementDeSOC>>  it_listes_elts_initiaux = listes_elements_initiaux_des_socs.iterator() ;
        while (it_soc.hasNext() /* && it_listes_elts_initiaux.hasNext()*/) {
            SystemeOptiqueCentre soc = it_soc.next() ;

//             Cette condition (if) n'est pas nécessaire, on ne risque pas de remettre dans le SOC les éléments qui y sont déjà
//             car c'est contrôlé et bloqué par le test estEligible que fait le SystemeOptiqueCentre pour tout élément qu'on lui ajoute
//             (Seul cas où ils n'y sont plus : si on a annulé la commande d'import / en supprimant le SOC de l'environnement,
//             dans la méthode 'annuler' plus bas, on a entrainé le détachement de tous ses éléments)
//                        if (soc.elements_centres_premier_niveau().size()==0)
//                            soc.ajouter(it_listes_elts_initiaux.next()) ;
//
//             L'ajout de cette liste des éléments initiaux dans le SOC n'a aucun effet s'ils y sont déjà (cf.
//             SystemeOptiqueCentre::estEligible). Lors d'un import depuis un fichier, ou d'un "coller" (Ctrl+V), ils y
//             sont déjà, mais si on a annulé (Ctrl+Z) la commande d'import, ils n'y sont plus, car en supprimant le SOC
//             de l'environnement (dans la méthode annuler plus bas), on a entrainé le détachement de tous ses éléments
//             [c'est ainsi parce que les éléments du SOC ont la référence de leur SOC parent ; on ne peut pas
//             autoriser que cette référence soit celle d'un SOC qui n'est plus dans l'environnement].
//
//             Une alternative serait de créer (par clonage/copie) un nouveau SOC pour chaque SOC importé, lors de
//             l'instanciation de la commande, en faisant en sorte que les éléments que chaque SOC contient (qui sont
//             peut-être encore dans l'environnement) ne le référencent pas en tant que parent, jusqu'à ce que ce SOC
//             soit ajouté dans l'environnement. Quand l'ajout dans l'environnement a lieu, il faudrait alors mettre à
//             jour les parents de tous ses éléments (de 1er niveau) A voir... Pas sûr que ce soit vraiment plus simple
//             ou plus propre
            //
            // OU PLUS SIMPLEMENT : DOOOOONNNNEEEEE :
            // Quand on supprime un SOC de l'environnement, on ne devrait pas vider sa liste d'éléments centrés : on
            // devrait se contenter de mettre le SOC Parent de chacun de ses éléments à 'null' (et, bien sûr de le
            // retirer de l'environnement si c'est un SOC de 1er niveau ou de son SOC Parent si c'est un sous SOC).
            // Néanmoins, le SOC contient toujours une réf à l'env, même s'il n'en fait plus partie. Cependant, lorsque
            // le SOC est remis dans l'env, il faut penser à refaire de lui le SOC Parent de tous ses éléments de 1er
            // niveau => Cette approche permettrait de se passer de l'attribut listes_elements_initiaux_des_socs !

//            soc.ajouter(it_listes_elts_initiaux.next()) ;

            environnement.ajouterSystemeOptiqueCentre(soc);
        }

        enregistrer();
    }



    @Override
    public void annuler() {
        elements_a_importer.stream_obstacles().forEach(environnement::supprimerObstacleALaRacine); // Altère les socs qui contenaient les obstacles supprimés
        elements_a_importer.stream_sources().forEach(environnement::supprimerSource);
        elements_a_importer.stream_socs().forEach(environnement::retirerSystemeOptiqueCentre);
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
