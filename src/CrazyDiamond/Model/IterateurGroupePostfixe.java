package CrazyDiamond.Model;

import java.util.*;

public class IterateurGroupePostfixe implements Iterator<Obstacle> {

    private final Deque<ListIterator<Obstacle>> iterateurs = new ArrayDeque<>();
    private final boolean en_profondeur; // breadthFirst si true, depthFirst si false

    public IterateurGroupePostfixe(Obstacle node, boolean en_profondeur) {
        this.iterateurs.add(Collections.singletonList(node).listIterator(1));
        this.en_profondeur = en_profondeur;
    }

    @Override
    public boolean hasNext() {
        return ! this.iterateurs.isEmpty();
    }

    @Override
    public Obstacle next() {
        ListIterator<Obstacle> iterator = this.iterateurs.peekFirst();

        Obstacle obs = iterator.previous();
        if (!iterator.hasPrevious())
            this.iterateurs.removeFirst();
        if (obs.contientObstaclesFils()) { // équivalent à obs instanceof Groupe
            List<Obstacle> obstacles_fils = obs.obstaclesFils() ;
            if (this.en_profondeur)
                this.iterateurs.addFirst(obstacles_fils.listIterator(obstacles_fils.size()));
            else
                this.iterateurs.addLast(obstacles_fils.listIterator(obstacles_fils.size()));
        }
        return obs;
    }

}
