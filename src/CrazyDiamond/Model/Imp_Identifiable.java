package CrazyDiamond.Model;

import java.util.UUID;

// Classe de base pour tous les éléments constitutifs d'un environnement (sources, obstacles...)
public class Imp_Identifiable {

    protected final String id ;

    public String id() {
        return id ;
    }

    public Imp_Identifiable() {
        this.id = UUID.randomUUID().toString();
    }

    public Imp_Identifiable(String id) {this.id = id ;}
}
