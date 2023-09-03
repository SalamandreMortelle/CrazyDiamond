package CrazyDiamond.Serializer;

import CrazyDiamond.Model.Imp_Identifiable;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class Imp_IdentifiableDeserializer extends StdDeserializer<Imp_Identifiable> {

    public Imp_IdentifiableDeserializer() {
        this(Imp_Identifiable.class);
    }

    public Imp_IdentifiableDeserializer(Class<?> vc) {
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
    public Imp_Identifiable deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {

        final ObjectCodec mapper = jsonParser.getCodec();
        final JsonNode iei_node = mapper.readTree(jsonParser);

        if (!iei_node.has("id"))
            return new Imp_Identifiable() ;

        return new Imp_Identifiable(iei_node.get("id").asText()) ;
    }

}
