package CrazyDiamond.Controller;

import CrazyDiamond.Model.Obstacle;
import CrazyDiamond.Model.Source;
import CrazyDiamond.Model.SystemeOptiqueCentre;
import CrazyDiamond.Model.Unite;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class ElementsSelectionnes {

    Unite unite ;
    ArrayList<Obstacle> obstacles ;
    ArrayList<Source> sources ;
    ArrayList<SystemeOptiqueCentre> socs ;

    public ElementsSelectionnes(Unite unite) {
        this.unite = unite ;
        obstacles = new ArrayList<>(1) ;
        sources = new ArrayList<>(1) ;
        socs = new ArrayList<>(1) ;
    }

    // Constructeur de copie
    public ElementsSelectionnes(ElementsSelectionnes es) {
        this.unite = es.unite() ;
        this.obstacles = (ArrayList<Obstacle>) es.obstacles.clone();
        this.sources = (ArrayList<Source>) es.sources.clone();
        this.socs = (ArrayList<SystemeOptiqueCentre>) es.socs.clone();
    }

    public void selectionnerUniquement(Obstacle o) {
        vider();
        obstacles.add(o) ;
    }

    /**
     * Ajoute un obstacle à la sélection. Si cet obstacle fait partie d'un SOC et que tous les autres obstacles du SOC
     * étaient déjà dans la sélection, on considère que c'est le SOC entier qui est sélectionné, et pas les obstacles
     * qui le composent.
     *
     * @param a_ajouter : l'obstacle à ajouter
     */
    public void ajouter(Obstacle a_ajouter) {
        if (!obstacles.contains(a_ajouter))
            obstacles.add(a_ajouter) ;

//        if (!a_ajouter.appartientASystemeOptiqueCentre())
//            obstacles.add(a_ajouter) ;
//        else {
//            SystemeOptiqueCentre soc = environnement.systemeOptiqueCentreContenant(a_ajouter);
//
//            if (!soc.stream_obstacles_centres().anyMatch(o -> !this.comprend(o))) {
//                soc.stream_obstacles_centres().forEach(o -> {obstacles.remove(o)});
//                socs.add(soc);
//            }
//            else
//                obstacles.add(a_ajouter) ;
//        }
    }

    public boolean comprend(Obstacle o) {return obstacles.contains(o) ;}
    public boolean comprend(Source s) {
        return sources.contains(s) ;
    }
    public boolean comprend(SystemeOptiqueCentre soc) {
        return socs.contains(soc) ;
    }

    public void selectionnerUniquement(Source s) {
        vider();
        sources.add(s) ;
    }

    public void ajouter(Source s) {
        if (!sources.contains(s))
            sources.add(s) ;
    }

    public void ajouterSources(List<Source> sources_a_ajouter) {
        sources_a_ajouter.forEach(this::ajouter);
    }

    public void ajouterObstacles(List<Obstacle> obstacles_a_ajouter) {
        obstacles_a_ajouter.forEach(this::ajouter);
    }

    public void ajouterSocs(List<SystemeOptiqueCentre> socs_a_ajouter) {
        socs_a_ajouter.forEach(this::ajouter);
    }

    public void selectionnerUniquement(SystemeOptiqueCentre sysoc) {
        vider();
        socs.add(sysoc) ;
    }

    public void ajouter(SystemeOptiqueCentre soc) {
        if (socs.contains(soc))
            return;

        // On supprime, puis on remet les obstacles afin qu'ils soient dans le même ordre dans les éléments sélectionnés et dans le SOC
        soc.obstacles_centres().forEach(obstacles::remove);
        ajouterObstacles(soc.obstacles_centres());

        socs.add(soc) ;
    }

    public void vider() {
        sources.clear();
        socs.clear();
        obstacles.clear();
    }

    public int nombreElements() {
        return sources.size() + obstacles.size() + socs.size() ;
    }

    public boolean estVide() {
        return nombreElements()==0 ;
    }

    public int nombreSources() { return sources.size() ;}
    public int nombreObstacles() { return obstacles.size() ;}
    public int nombreSystemesOptiquesCentres() { return socs.size() ;}
    public Source sourceUnique() {

        if (nombreElements()!=1 || sources.size()!=1)
            return null ;

        return sources.get(0) ;

    }

    public Obstacle obstacleUnique() {

        if (nombreElements()!=1 || obstacles.size()!=1)
            return null ;

        return obstacles.get(0) ;

    }

    public SystemeOptiqueCentre socUnique() {

        if (nombreElements()!=1 || socs.size()!=1)
            return null ;

        return socs.get(0) ;

    }

    public Stream<Obstacle> stream_obstacles() { return obstacles.stream() ; }
    public Stream<Source> stream_sources() { return sources.stream() ; }
    public Stream<SystemeOptiqueCentre> stream_socs() { return socs.stream() ; }

    public void remplaceParSources(List<Source> s_ajoutees) {
        vider();
        ajouterSources(s_ajoutees);
    }
    public void remplaceParObstacles(List<Obstacle> o_ajoutes) {
        vider();
        ajouterObstacles(o_ajoutes);
    }
    public void remplaceParSocs(List<SystemeOptiqueCentre> soc_ajoutes) {
        vider();
        ajouterSocs(soc_ajoutes);
    }

    public void retireObstacle(Obstacle remitem) { obstacles.remove(remitem) ;}
    public void retireSource(Source remitem) { sources.remove(remitem) ;}
    public void retireSoc(SystemeOptiqueCentre remitem) { socs.remove(remitem) ;}

    public void definirUnite(Unite unite) { this.unite = unite ;}
    public Unite unite() { return unite ; }

    public Iterator<Obstacle> iterateur_obstacles() {return obstacles.iterator(); }

    public Iterator<Source> iterateur_sources() { return sources.iterator() ; }

    public Iterator<SystemeOptiqueCentre> iterateur_systemesOptiquesCentres() { return socs.iterator() ; }
}
