package CrazyDiamond.Model;

public enum Coin {
    HD(0),
    HG(1),
    BG(2),
    BD(3);

    public final int index;

    Coin(int index) {
        this.index = index;
    }

    public Coin coin_suivant() {

        if (this == HD) return Coin.HG ;
        if (this == HG) return Coin.BG ;
        if (this == BG) return Coin.BD ;

        return Coin.HD ;
    }

    public BordRectangle bord_suivant() {

        if (this == HD) return BordRectangle.HAUT ;
        if (this == HG) return BordRectangle.GAUCHE ;
        if (this == BG) return BordRectangle.BAS ;

        return BordRectangle.DROIT ;
    }
}
