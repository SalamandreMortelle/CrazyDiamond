package CrazyDiamond.Model;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Iterator;
import java.util.Deque;

public class IterateurGroupePrefixe implements Iterator<Obstacle> {

    private final Deque<Iterator<Obstacle>> iterateurs = new ArrayDeque<>();
    private final boolean en_profondeur; // Parcours en profondeur si true, en largeur si false

    public IterateurGroupePrefixe(Obstacle node, boolean en_profondeur) {
        this.iterateurs.add(Collections.singleton(node).iterator());
        this.en_profondeur = en_profondeur;
    }

    @Override
    public boolean hasNext() {return ! this.iterateurs.isEmpty();}

    @Override
    public Obstacle next() {
        Iterator<Obstacle> iterator = this.iterateurs.peekFirst();

        Obstacle obs = iterator.next() ;
        if (!iterator.hasNext())
            this.iterateurs.removeFirst();
        if (obs.contientObstaclesFils()) { // équivalent à obs instanceof Groupe
            if (this.en_profondeur)
                this.iterateurs.addFirst(obs.obstaclesFils().iterator());
            else // Parcours en largeur
                this.iterateurs.addLast(obs.obstaclesFils().iterator());
        }
        return obs;
    }

}
