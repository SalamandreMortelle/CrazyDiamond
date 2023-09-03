package CrazyDiamond.Model;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;

import java.util.Iterator;

//public class CheminLumiere implements Iterable<Rayon> {
public class CheminLumiere implements Iterable<CheminLumiere> {

    
    //X Le rayon porté par ce noeud du CheminLumiere
    protected Rayon rayon = null ;
   
//X    protected ArrayList<Rayon> rayons ;

    public Color couleur ;
    
    CheminLumiere chemin_rayon_transmis = null ;
    CheminLumiere chemin_rayon_reflechi = null ;

    // Niveau du chemin dans l'arborescence (0 = rayon émis par la source)
    int niveau ;

    Point2D normale = null ;

    /**
     * Construit un nouveau CheminLumiere d'une couleur donnée
     * @param couleur
     */
    public CheminLumiere(Color couleur,int niveau) {

        if (couleur == null)
            throw new IllegalArgumentException("La couleur d'un CheminLumiere doit être définie");

       //X rayons = new ArrayList<Rayon>() ;
        
        this.couleur = couleur ;
        this.niveau  = niveau ;
    }

    public void ajouteRayon(Rayon r) {
        rayon = r ;
    }
//X    public void ajouteRayon(Rayon r) {
//X        rayons.add(r) ;
//X//        indices_milieux.add(indice) ;
//X    }

//    boolean estInfini() {
//
//        for(Rayon r : rayons) {
//            if (r.estInfini())
//                return true;
//        }
//
//        return false ;
//    }

    @Override
//    public Iterator<Rayon> iterator() {
    public Iterator<CheminLumiere> iterator() {
        return new IterateurCheminLumiere(this);
    }
    
    public CheminLumiere creerCheminRayonTransmisSuivant() {
        CheminLumiere nouveau_chemin_rayon_transmis = new CheminLumiere(couleur,niveau+1) ;
        chemin_rayon_transmis = nouveau_chemin_rayon_transmis ;
        return chemin_rayon_transmis ;
    }

    public CheminLumiere creerCheminRayonReflechiSuivant() {
        CheminLumiere nouveau_chemin_rayon_reflechi = new CheminLumiere(couleur,niveau+1) ;
        chemin_rayon_reflechi = nouveau_chemin_rayon_reflechi ;
        return chemin_rayon_reflechi ;
    }

    public Rayon rayon() { return rayon ;}
    public Point2D normale() { return normale ;}
}
