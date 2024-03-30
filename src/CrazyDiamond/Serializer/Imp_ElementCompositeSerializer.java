package CrazyDiamond.Serializer;

import CrazyDiamond.Model.Imp_ElementComposite;
import CrazyDiamond.Model.Obstacle;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class Imp_ElementCompositeSerializer extends StdSerializer<Imp_ElementComposite> {

    public Imp_ElementCompositeSerializer() {
        super(Imp_ElementComposite.class);
    }

    @Override
    public void serialize(Imp_ElementComposite el_c, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        // Écriture des éléments dans un tableau
        jsonGenerator.writeArrayFieldStart("elements");

        for (Obstacle o : el_c.elements()) {
            jsonGenerator.writeObject (o);
        }

        jsonGenerator.writeEndArray();
    }

}
