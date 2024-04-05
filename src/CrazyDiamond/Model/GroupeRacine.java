package CrazyDiamond.Model;

public class GroupeRacine extends Groupe {

    public GroupeRacine(boolean solidaire) throws IllegalArgumentException {
        super(solidaire);
    }

    public GroupeRacine(String nom, boolean solidaire) throws IllegalArgumentException {
        super(nom, solidaire);
    }

    public GroupeRacine(Imp_Identifiable ii, Imp_Nommable in, Imp_ElementComposite ic, boolean solidaire) throws IllegalArgumentException {
        super(ii, in, ic, solidaire);
    }
}
