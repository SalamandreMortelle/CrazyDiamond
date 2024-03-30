package CrazyDiamond.Serializer;

import CrazyDiamond.Model.Groupe;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class GroupeSerializer extends StdSerializer<Groupe> {

    public GroupeSerializer() {
        super(Groupe.class);
    }

    @Override
    public void serialize(Groupe groupe, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringField("@type",Groupe.class.getSimpleName());

        groupe.appliquerSurIdentifiable(jsonGenerator::writeObject);
        groupe.appliquerSurNommable(jsonGenerator::writeObject);

        jsonGenerator.writeBooleanField ("elements_solidaires",groupe.elementsSolidaires());

        groupe.appliquerSurElementComposite(jsonGenerator::writeObject);


        jsonGenerator.writeEndObject();

    }
}
