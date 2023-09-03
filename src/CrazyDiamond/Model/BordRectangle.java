package CrazyDiamond.Model;

public enum BordRectangle {
    HAUT(0), GAUCHE(1), BAS(2), DROIT(3) ;

    public final int index;

    BordRectangle(int index) {
        this.index = index;
    }

    public BordRectangle bord_suivant() {

        if (this==HAUT) return GAUCHE ;
        if (this==GAUCHE) return BAS ;
        if (this==BAS) return DROIT ;

        return HAUT ;

    }

    public Coin coin_suivant() {

        if (this==HAUT) return Coin.HG ;
        if (this==GAUCHE) return Coin.BG ;
        if (this==BAS) return Coin.BD ;

        return Coin.HD ;

    }
}
