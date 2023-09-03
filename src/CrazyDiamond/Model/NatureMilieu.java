package CrazyDiamond.Model;

public enum NatureMilieu {
    ABSORBANT("ABSORBANT"), TRANSPARENT("TRANSPARENT"), PAS_DE_MILIEU("PAS_DE_MILIEU") ;

    private final String value;

    NatureMilieu(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static NatureMilieu fromValue(String text) {
        for (NatureMilieu n_milieu : NatureMilieu.values()) {
            if (String.valueOf(n_milieu.value).equals(text)) {
                return n_milieu;
            }
        }
        return null;
    }

}

