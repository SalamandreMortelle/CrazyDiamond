package CrazyDiamond.Serializer;

import CrazyDiamond.Model.TraitementSurface;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class TraitementSurfaceDeserializer extends StdDeserializer<TraitementSurface>  {
    public TraitementSurfaceDeserializer() {
        this(null);
    }
    public TraitementSurfaceDeserializer(Class<?> vc) {
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
    public TraitementSurface deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        String trait_surf = node.get("traitement_surface").asText();

        for (TraitementSurface t_s : TraitementSurface.values()) {
            if (t_s.toString().equals(trait_surf))
                return t_s;
        }

        return null;
    }

}
