package CrazyDiamond.Model;

public enum BordPrisme {

    GAUCHE(0), BAS(1), DROIT(2) ;

    public final int index;

    BordPrisme(int index) {
        this.index = index;
    }

    public BordPrisme bord_suivant() {

        if (this==GAUCHE) return BAS ;
        if (this==BAS) return DROIT ;
        return GAUCHE ;

    }

    public Sommet sommet_suivant() {

        if (this==GAUCHE) return Sommet.BG ;
        if (this==BAS) return Sommet.BD ;

        return Sommet.H ;

    }

}
