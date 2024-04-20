package CrazyDiamond.Serializer;

import CrazyDiamond.Model.ConvexiteFaceLentille;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class ConvexiteFaceLentilleDeserializer extends StdDeserializer<ConvexiteFaceLentille> {
    public ConvexiteFaceLentilleDeserializer() {
        this(null);
    }
    public ConvexiteFaceLentilleDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public ConvexiteFaceLentille deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        String type_face = node.get("convexite_face_lentille").asText();

        for (ConvexiteFaceLentille c_f : ConvexiteFaceLentille.values()) {
            if (c_f.toString().equals(type_face))
                return c_f;
        }

        return null;
    }
}
