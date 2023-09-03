package CrazyDiamond.Model;

public enum Sommet {
    H(0),
    BG(1),
    BD(2);

    public final int index;

    Sommet(int index) {
        this.index = index;
    }

    public Sommet sommet_suivant() {

        if (this == H) return Sommet.BG ;
        if (this == BG) return Sommet.BD ;

        return Sommet.H ;
    }

    public BordPrisme bord_suivant() {

        if (this == H) return BordPrisme.GAUCHE;
        if (this == BG) return BordPrisme.BAS ;

        return BordPrisme.DROIT ;
    }

}
