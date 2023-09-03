package CrazyDiamond.Serializer;

import CrazyDiamond.Model.Imp_Nommable;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class Imp_NommableSerializer extends StdSerializer<Imp_Nommable> {

    public Imp_NommableSerializer() {
        super(Imp_Nommable.class);
    }

    /**
     * @param el_id
     * @param jsonGenerator
     * @param serializerProvider
     * @throws IOException
     */
    @Override
    public void serialize(Imp_Nommable el_id, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

        jsonGenerator.writeStringField("nom",el_id.nom());

    }

}
