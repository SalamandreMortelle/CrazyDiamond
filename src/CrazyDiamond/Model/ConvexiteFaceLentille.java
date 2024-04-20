package CrazyDiamond.Model;

public enum ConvexiteFaceLentille {

    CONVEXE("CONVEXE"),
    PLANE("PLANE"),
    CONCAVE("CONCAVE");
    private final String value;

    ConvexiteFaceLentille(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static ConvexiteFaceLentille fromValue(String text) {
        for (ConvexiteFaceLentille c_face : ConvexiteFaceLentille.values()) {
            if (String.valueOf(c_face.value).equals(text)) {
                return c_face;
            }
        }
        return null;
    }
}
