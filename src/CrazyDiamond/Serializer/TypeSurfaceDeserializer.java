package CrazyDiamond.Serializer;

import CrazyDiamond.Model.TypeSurface;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class TypeSurfaceDeserializer extends StdDeserializer<TypeSurface> {
    public TypeSurfaceDeserializer() {
        this(null);
    }
    public TypeSurfaceDeserializer(Class<?> vc) {
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
    public TypeSurface deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        String type_surf = node.get("type_surface").asText();

        for (TypeSurface t_s : TypeSurface.values()) {
            if (t_s.toString().equals(type_surf))
                return t_s;
        }

        return null;
    }
}
