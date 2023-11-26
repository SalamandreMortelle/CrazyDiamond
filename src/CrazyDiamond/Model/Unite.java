package CrazyDiamond.Model;

import java.util.ResourceBundle;

public enum Unite {

        M(ResourceBundle.getBundle("CrazyDiamond").getString("unite_m"),"m",1d),
        DM(ResourceBundle.getBundle("CrazyDiamond").getString("unite_dm"),"dm",0.1d),
        CM(ResourceBundle.getBundle("CrazyDiamond").getString("unite_cm"),"cm",0.01d),
        MM(ResourceBundle.getBundle("CrazyDiamond").getString("unite_mm"),"mm",0.001d);

        public final String label;
        public final String symbole;
        public final double valeur;

        Unite(String label,String symbole, double valeur) {
                this.label   = label;
                this.symbole = symbole;
                this.valeur  = valeur;
        }

        @Override
        public String toString() {
                return String.valueOf(symbole);
        }

        public static Unite fromValue(String text) {
                for (Unite u : Unite.values()) {
                        if (String.valueOf(u.symbole).equals(text)) {
                                return u;
                        }
                }
                return null;
        }

}
