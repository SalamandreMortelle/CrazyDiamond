package CrazyDiamond.Model;

public enum FormeFaceLentille {

    SPHERIQUE("SPHERIQUE"),
    CONIQUE("CONIQUE");
    private final String value;

    FormeFaceLentille(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static FormeFaceLentille fromValue(String text) {
        for (FormeFaceLentille f_face : FormeFaceLentille.values()) {
            if (String.valueOf(f_face.value).equals(text)) {
                return f_face;
            }
        }
        return null;
    }
}
