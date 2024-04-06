package CrazyDiamond.Model;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class IterateurGroupePostfixeObstaclesReels implements Iterator<Obstacle> {

    private final IterateurGroupePostfixe iterateur_groupe_postfixe;

    private Obstacle obstacle_suivant;

    public IterateurGroupePostfixeObstaclesReels(Obstacle node, boolean en_profondeur) {
        iterateur_groupe_postfixe = new IterateurGroupePostfixe(node,en_profondeur) ;
        obstacle_suivant = obstacleReelSuivant() ;
    }

    @Override
    public boolean hasNext() {
        return (obstacle_suivant!=null) ;
    }

    @Override
    public Obstacle next() {

        if (obstacle_suivant==null)
            throw new NoSuchElementException() ;

        Obstacle resultat = obstacle_suivant ;
        obstacle_suivant = obstacleReelSuivant();

        return resultat;
    }

    /**
     * Retourne l'obstacle réel suivant du groupe ou null s'il n'y en a pas.
     * L'appelant ne doit pas appeler la méthode next() s'il utilise celle-ci.
     * @return l'obstacle réel suivant
     */
    private Obstacle obstacleReelSuivant() {

        Obstacle resultat = null ;

        if (iterateur_groupe_postfixe.hasNext())
            resultat = iterateur_groupe_postfixe.next();

        return (resultat!=null?(resultat.estReel()?resultat:obstacleReelSuivant()):null) ;
    }
}
