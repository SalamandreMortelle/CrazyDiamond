package CrazyDiamond.Serializer;

import CrazyDiamond.Model.Prisme;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class PrismeSerializer extends StdSerializer<Prisme> {

    public PrismeSerializer() {
        super(Prisme.class);
    }

    /**
     * @param prisme
     * @param jsonGenerator
     * @param serializerProvider
     * @throws IOException
     */
    @Override
    public void serialize(Prisme prisme, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringField("@type",Prisme.class.getSimpleName());

        prisme.appliquerSurIdentifiable(jsonGenerator::writeObject);
        prisme.appliquerSurNommable(jsonGenerator::writeObject);
        prisme.appliquerSurElementAvecContour(jsonGenerator::writeObject);
        prisme.appliquerSurElementAvecMatiere(jsonGenerator::writeObject);

        jsonGenerator.writeNumberField("x_centre",prisme.xCentre());
        jsonGenerator.writeNumberField("y_centre",prisme.yCentre());
        jsonGenerator.writeNumberField("angle_sommet",prisme.angleSommet());
        jsonGenerator.writeNumberField("largeur_base",prisme.largeurBase());
        jsonGenerator.writeNumberField("orientation",prisme.orientation());

        jsonGenerator.writeEndObject();

    }
}
