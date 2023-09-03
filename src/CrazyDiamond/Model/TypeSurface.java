package CrazyDiamond.Model;

public enum TypeSurface {
    CONCAVE("CONCAVE"),
    CONVEXE("CONVEXE");

    private final String value;

    TypeSurface(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static TypeSurface fromValue(String text) {
        for (TypeSurface t_surf : TypeSurface.values()) {
            if (String.valueOf(t_surf.value).equals(text)) {
                return t_surf;
            }
        }
        return null;
    }
}
