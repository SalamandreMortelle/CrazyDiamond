package CrazyDiamond.Serializer;

import CrazyDiamond.Model.FormeFaceLentille;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class FormeFaceLentilleDeserializer extends StdDeserializer<FormeFaceLentille> {
    public FormeFaceLentilleDeserializer() {
        this(null);
    }
    public FormeFaceLentilleDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public FormeFaceLentille deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        String forme_face = node.get("forme_face_lentille").asText();

        for (FormeFaceLentille f_f : FormeFaceLentille.values()) {
            if (f_f.toString().equals(forme_face))
                return f_f;
        }

        return null;
    }
}
