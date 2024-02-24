package CrazyDiamond.Controller;

import javafx.beans.property.ObjectProperty;
import javafx.util.StringConverter;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Cette classe permet de convertir une chaine en Double et inversement.
 * Si un ObjectProperty<Double> est passé en paramètre du constructeur, sa valeur courante sera retournée si la conversion
 * en Double de la chaîne passée à fromString() est impossible (maintien de la valeur courante de la Property).
 */
public class ConvertisseurEntierValidant extends StringConverter<Integer> {

    private static final String format_par_defaut = "0" ;

    private static final String regExp = "[\\x00-\\x20]*(\\d+)[\\x00-\\x20]*" ;
    private static final Pattern pattern = Pattern.compile(regExp) ;

    protected final DecimalFormat decimal_format;

    // Object Property contenant la valeur actuelle (avant modification) de la distance, qu'il faudra retourner
    // si la conversion de la chaîne passée à la méthode fromString() est impossible
    ObjectProperty<Integer> ob_prop_integer;

    private static boolean isInteger(String s) {
        Matcher m = pattern.matcher(s);
        return m.matches();
    }

    public ConvertisseurEntierValidant(ObjectProperty<Integer> ob_p_i) {
        this(ob_p_i,format_par_defaut) ;
    }
    public ConvertisseurEntierValidant(ObjectProperty<Integer> ob_p_i, String format) {
        this.ob_prop_integer = ob_p_i;

        decimal_format = new DecimalFormat(format) ;

//        DecimalFormatSymbols dec_symbols = decimal_format.getDecimalFormatSymbols();
//        dec_symbols.setDecimalSeparator('.');
//
//        decimal_format.setDecimalFormatSymbols(dec_symbols);

    }

    @Override
    public String toString(Integer value) {
        // If the specified value is null, return a zero-length String
        if (value == null) {
            return "";
        }

        return decimal_format.format(value);
    }

    /**
     * Lit la chaîne value et essaye de la convertir en Double. Si la conversion est impossible, renvoie la valeur courante
     * de obj_prop_double ou sinon, null.
     *
     * @param value : chaine à convertir en Integer
     * @return valur Integer
     */
    @Override
    public Integer fromString(String value) {
        try {

            Integer valeur_retour_par_defaut = (ob_prop_integer != null ? ob_prop_integer.get() : null);

            // If the specified value is null or zero-length, return null
            if (value == null) {
                return valeur_retour_par_defaut;
            }

            value = value.trim();

            if (value.length() < 1 || !isInteger(value)) {
                return valeur_retour_par_defaut;
            }

            // Si le formatage de la valeur courante aboutit à la même chaîne que la nouvelle valeur value, on retourne
            // aussi la valeur actuelle pour ne pas déclencher des mises à jour intempestives de la Value dans les spinners
            if (valeur_retour_par_defaut!=null && (decimal_format.format(valeur_retour_par_defaut).equals(value)) )
                return valeur_retour_par_defaut ;

            // Perform the requested parsing
            return decimal_format.parse(value).intValue();
        } catch (ParseException ex) {
            throw new RuntimeException(ex);
        }
    }

}
