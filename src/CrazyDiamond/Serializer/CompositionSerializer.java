package CrazyDiamond.Serializer;

import CrazyDiamond.Model.Composition;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class CompositionSerializer extends StdSerializer<Composition> {

    public CompositionSerializer() {
        super(Composition.class);
    }

    @Override
    public void serialize(Composition composition, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringField("@type",Composition.class.getSimpleName());

        composition.appliquerSurIdentifiable(jsonGenerator::writeObject);
        composition.appliquerSurNommable(jsonGenerator::writeObject);
        composition.appliquerSurElementComposite(jsonGenerator::writeObject);
        composition.appliquerSurElementAvecContour(jsonGenerator::writeObject);
        composition.appliquerSurElementAvecMatiere(jsonGenerator::writeObject);

        jsonGenerator.writeObjectField("operateur",composition.operateur().toString());

        jsonGenerator.writeEndObject();

    }
}
