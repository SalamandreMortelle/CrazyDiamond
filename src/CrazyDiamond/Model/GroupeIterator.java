package CrazyDiamond.Model;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Iterator;
import java.util.Deque;

public class GroupeIterator implements Iterator<Obstacle> {

    private final Deque<Iterator<Obstacle>> iterators = new ArrayDeque<>();
    private final boolean breadthFirst; // breadthFirst si vrai, depthFirst si false

    public GroupeIterator(Obstacle node, boolean breadthFirst) {
        this.iterators.add(Collections.singleton(node).iterator());
        this.breadthFirst = breadthFirst;
    }

    @Override
    public boolean hasNext() {
        return ! this.iterators.isEmpty();
    }

    @Override
    public Obstacle next() {
        Iterator<Obstacle> iterator = this.iterators.removeFirst();

        Obstacle obs = iterator.next();
        if (iterator.hasNext())
            this.iterators.addFirst(iterator);
        if (obs.contientObstaclesFils()) { // équivalent à obs instanceof Groupe
            if (this.breadthFirst)
                this.iterators.addLast(obs.obstaclesFils().iterator());
            else
                this.iterators.addFirst(obs.obstaclesFils().iterator());
        }
        return obs;
    }
}
