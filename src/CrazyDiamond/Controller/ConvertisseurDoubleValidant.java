package CrazyDiamond.Controller;

import javafx.beans.property.ObjectProperty;
import javafx.util.StringConverter;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Cette classe permet de convertir une chaine en Double et inversement.
 * Si un ObjectProperty<Double> est passé en paramètre du constructeur, sa valeur courante sera retournée si la conversion
 * en Double de la chaîne passée à fromString() est impossible (maintien de la valeur courante de la Property).
 */
public class ConvertisseurDoubleValidant extends StringConverter<Double> {

    private static String format_par_defaut = "0.00" ;

    private static final String regExp = "[\\x00-\\x20]*[+-]?(((((\\d+)(\\.|,)?((\\d+)?)([eE][+-]?(\\d+))?)|(\\.((\\d+))([eE][+-]?(\\d+))?)|(((0[xX](\\p{XDigit}+)(\\.|,)?)|(0[xX](\\p{XDigit}+)?(\\.|,)(\\p{XDigit}+)))[pP][+-]?(\\d+)))[fFdD]?))[\\x00-\\x20]*" ;
    private static final Pattern pattern = Pattern.compile(regExp) ;

    protected final DecimalFormat decimal_format;

    // Object Property contenant la valeur actuelle (avant modification) de la distance, qu'il faudra retourner
    // si la conversion de la chaîne passée à la méthode fromString() est impossible
    ObjectProperty<Double> obj_prop_double;

    private static boolean isDouble(String s) {
        Matcher m = pattern.matcher(s);
        return m.matches();
    }

    public ConvertisseurDoubleValidant(ObjectProperty<Double> ob_p_d) {
        this(ob_p_d,format_par_defaut) ;
    }
    public ConvertisseurDoubleValidant(ObjectProperty<Double> ob_p_d, String format) {
        this.obj_prop_double = ob_p_d;

        decimal_format = new DecimalFormat(format) ;

        DecimalFormatSymbols dec_symbols = decimal_format.getDecimalFormatSymbols();
        dec_symbols.setDecimalSeparator('.');

        decimal_format.setDecimalFormatSymbols(dec_symbols);

    }

    @Override
    public String toString(Double value) {
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
     * @param value
     * @return
     */
    @Override
    public Double fromString(String value) {
        try {

            Double valeur_retour_par_defaut = (obj_prop_double != null ? obj_prop_double.getValue() : null);

            // If the specified value is null or zero-length, return null
            if (value == null) {
                return valeur_retour_par_defaut;
            }

            value = value.trim();

            if (value.length() < 1 || !isDouble(value)) {
                return valeur_retour_par_defaut;
            }

            // Perform the requested parsing
            return decimal_format.parse(value).doubleValue();
        } catch (ParseException ex) {
            throw new RuntimeException(ex);
        }
    }

}
