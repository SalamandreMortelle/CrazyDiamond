package CrazyDiamond.Serializer;

import CrazyDiamond.Model.Imp_Identifiable;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class Imp_IdentifiableSerializer extends StdSerializer<Imp_Identifiable> {

    public Imp_IdentifiableSerializer() {
        super(Imp_Identifiable.class);
    }

    /**
     * @param el_id
     * @param jsonGenerator
     * @param serializerProvider
     * @throws IOException
     */
    @Override
    public void serialize(Imp_Identifiable el_id, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

        jsonGenerator.writeStringField("id",el_id.id());

    }

}
