package CrazyDiamond.Controller;

import CrazyDiamond.Model.Obstacle;
import CrazyDiamond.Model.Source;
import CrazyDiamond.Model.SystemeOptiqueCentre;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class ElementsSelectionnes {

    ArrayList<Obstacle> obstacles ;
    ArrayList<Source> sources ;
    ArrayList<SystemeOptiqueCentre> socs ;

    public ElementsSelectionnes() {
        obstacles = new ArrayList<>(1) ;
        sources = new ArrayList<>(1) ;
        socs = new ArrayList<>(1) ;
    }

    public void selectionneUniquement(Obstacle o) {
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
    public void ajoute(Obstacle a_ajouter) {
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

    public void selectionneUniquement(Source s) {
        vider();
        sources.add(s) ;
    }

    public void ajoute(Source s) {
        if (!sources.contains(s))
            sources.add(s) ;
    }

    public void ajouteSources(List<Source> sources_a_ajouter) {
        sources_a_ajouter.forEach(this::ajoute);
    }

    public void ajouteObstacles(List<Obstacle> obstacles_a_ajouter) {
        obstacles_a_ajouter.forEach(this::ajoute);
    }

    public void ajouteSocs(List<SystemeOptiqueCentre> socs_a_ajouter) {
        socs_a_ajouter.forEach(this::ajoute);
    }

    public void selectionneUniquement(SystemeOptiqueCentre sysoc) {

        vider();
        socs.add(sysoc) ;
    }

    public void ajoute(SystemeOptiqueCentre soc) {
        if (!socs.contains(soc))
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

    public int nombreSources() { return sources.size() ;}
    public int nombreObstacles() { return obstacles.size() ;}
    public int nombreSocs() { return socs.size() ;}
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


    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    public Iterator<Source> iterateur_sources() {
        return sources.iterator();
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    public Iterator<Obstacle> iterateur_obstacles() {
        return obstacles.iterator();
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    public Iterator<SystemeOptiqueCentre> iterateur_socs() {
        return socs.iterator();
    }


    public Stream<Obstacle> stream_obstacles() { return obstacles.stream() ; }
    public Stream<Source> stream_sources() { return sources.stream() ; }
    public Stream<SystemeOptiqueCentre> stream_socs() { return socs.stream() ; }

    public void remplaceParSources(List<Source> s_ajoutees) {
        vider();
        ajouteSources(s_ajoutees);
    }
    public void remplaceParObstacles(List<Obstacle> o_ajoutes) {
        vider();
        ajouteObstacles(o_ajoutes);
    }
    public void remplaceParSocs(List<SystemeOptiqueCentre> soc_ajoutes) {
        vider();
        ajouteSocs(soc_ajoutes);
    }

    public void retireObstacle(Obstacle remitem) {obstacles.remove(remitem) ;}
    public void retireSource(Source remitem) {sources.remove(remitem) ;}
    public void retireSoc(SystemeOptiqueCentre remitem) {socs.remove(remitem) ;}
}
