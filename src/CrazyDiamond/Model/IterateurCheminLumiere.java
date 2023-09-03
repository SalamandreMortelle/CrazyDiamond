package CrazyDiamond.Model;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

//public class IterateurCheminLumiere implements Iterator<Rayon> {
public class IterateurCheminLumiere implements Iterator<CheminLumiere> {

    CheminLumiere courant ;

    private static final Logger LOGGER = Logger.getLogger( "CrazyDiamond" );

    private Stack<CheminLumiere> traversee ;

    public IterateurCheminLumiere(CheminLumiere ch_racine) {
        if (ch_racine==null)
            throw new IllegalArgumentException("Un IterateurCheminLumiere ne peut être construit sur un CheminLumiere 'null'") ;

         traversee = new Stack<CheminLumiere>() ;

        traiteRayonsTransmis(ch_racine); ;
    }

    private void traiteRayonsTransmis(CheminLumiere ch_courant) {

        while (ch_courant != null) {

            traversee.push(ch_courant) ;

            ch_courant = ch_courant.chemin_rayon_transmis ;
        }

    }

    @Override
    public boolean hasNext() {

        return !traversee.isEmpty() ;

    }

    @Override
//    public Rayon next() {
    public CheminLumiere next() {

        LOGGER.log(Level.FINEST,"next() appelé sur IterateurCheminLumiere {0}",this);

        if (!hasNext())
            throw new NoSuchElementException();

        CheminLumiere ch_courant = traversee.pop() ;

        if (ch_courant.chemin_rayon_reflechi != null)
            traiteRayonsTransmis(ch_courant.chemin_rayon_reflechi);

//        return ch_courant.rayon ;
        return ch_courant ;

    }
}
