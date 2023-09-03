package CrazyDiamond.Serializer;

import CrazyDiamond.Model.Imp_Nommable;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class Imp_NommableDeserializer extends StdDeserializer<Imp_Nommable> {

    public Imp_NommableDeserializer() {
        this(Imp_Nommable.class);
    }

    public Imp_NommableDeserializer(Class<?> vc) {
        super(vc);
    }


    /**
     * @param jsonParser
     * @param deserializationContext
     * @return
     * @throws IOException
     * @throws JacksonException
     */
    @Override
    public Imp_Nommable deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {

//        final ObjectMapper mapper = (ObjectMapper) jsonParser.getCodec();
        final ObjectCodec mapper = jsonParser.getCodec();
        final JsonNode iei_node = mapper.readTree(jsonParser);

        return new Imp_Nommable(iei_node.get("nom").asText()) ;
    }

}
