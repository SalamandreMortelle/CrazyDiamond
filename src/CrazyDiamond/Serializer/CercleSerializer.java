package CrazyDiamond.Serializer;

import CrazyDiamond.Model.Cercle;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class CercleSerializer extends StdSerializer<Cercle> {

    public CercleSerializer() {
        super(Cercle.class);
    }

    /**
     * @param cercle
     * @param jsonGenerator
     * @param serializerProvider
     * @throws IOException
     */
    @Override
    public void serialize(Cercle cercle, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringField("@type",Cercle.class.getSimpleName());

        cercle.appliquerSurIdentifiable(jsonGenerator::writeObject);
        cercle.appliquerSurNommable(jsonGenerator::writeObject);
        cercle.appliquerSurElementAvecContour(jsonGenerator::writeObject);
        cercle.appliquerSurElementAvecMatiere(jsonGenerator::writeObject);

        jsonGenerator.writeNumberField("x_centre",cercle.xCentre());
        jsonGenerator.writeNumberField("y_centre",cercle.yCentre());
        jsonGenerator.writeNumberField("rayon",cercle.rayon());

        jsonGenerator.writeEndObject();

    }
}
