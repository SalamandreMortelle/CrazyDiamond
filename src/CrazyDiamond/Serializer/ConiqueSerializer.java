package CrazyDiamond.Serializer;

import CrazyDiamond.Model.Conique;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class ConiqueSerializer extends StdSerializer<Conique> {

    public ConiqueSerializer() {
        super(Conique.class);
    }

    /**
     * @param conique
     * @param jsonGenerator
     * @param serializerProvider
     * @throws IOException
     */
    @Override
    public void serialize(Conique conique, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringField("@type",Conique.class.getSimpleName());

        conique.appliquerSurIdentifiable(jsonGenerator::writeObject);
        conique.appliquerSurNommable(jsonGenerator::writeObject);
        conique.appliquerSurElementAvecContour(jsonGenerator::writeObject);
        conique.appliquerSurElementAvecMatiere(jsonGenerator::writeObject);

        jsonGenerator.writeNumberField("x_foyer",conique.xFoyer());
        jsonGenerator.writeNumberField("y_foyer",conique.yFoyer());
        jsonGenerator.writeNumberField("orientation",conique.orientation());
        jsonGenerator.writeNumberField("parametre",conique.parametre());
        jsonGenerator.writeNumberField("excentricite",conique.excentricite());

        jsonGenerator.writeEndObject();

    }
}
