package CrazyDiamond.Serializer;

import CrazyDiamond.Model.Imp_ElementAvecContour;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class Imp_ElementAvecContourSerializer extends StdSerializer<Imp_ElementAvecContour> {

    public Imp_ElementAvecContourSerializer() {
        super(Imp_ElementAvecContour.class);
    }

    /**
     * @param el_ac
     * @param jsonGenerator
     * @param serializerProvider
     * @throws IOException
     */
    @Override
    public void serialize(Imp_ElementAvecContour el_ac, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

            jsonGenerator.writeStringField("couleur_contour",el_ac.couleurContour().toString());
            jsonGenerator.writeNumberField("taux_reflexion_surface",el_ac.tauxReflexionSurface());
            jsonGenerator.writeNumberField("orientation_axe_polariseur",el_ac.orientationAxePolariseur());
            jsonGenerator.writeObjectField ("traitement_surface",el_ac.traitementSurface());

    }

}
