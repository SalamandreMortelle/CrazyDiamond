package CrazyDiamond.Model;

public enum TraitementSurface {

    AUCUN("AUCUN"),
    PARTIELLEMENT_REFLECHISSANT("PARTIELLEMENT_REFLECHISSANT"),
    REFLECHISSANT("REFLECHISSANT"),
    ABSORBANT("ABSORBANT"),
    POLARISANT("POLARISANT") ;

    private final String value;

    TraitementSurface(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static TraitementSurface fromValue(String text) {
        for (TraitementSurface t_surf : TraitementSurface.values()) {
            if (String.valueOf(t_surf.value).equals(text)) {
                return t_surf;
            }
        }
        return null;
    }

}
