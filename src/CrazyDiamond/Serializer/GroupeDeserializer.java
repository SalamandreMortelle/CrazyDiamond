package CrazyDiamond.Serializer;

import CrazyDiamond.Model.*;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class GroupeDeserializer extends StdDeserializer<Groupe> {

    public GroupeDeserializer() {
        this(Groupe.class);
    }

    public GroupeDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Groupe deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {

        final ObjectMapper mapper = (ObjectMapper) jsonParser.getCodec();

        final JsonNode groupe_node = mapper.readTree(jsonParser);

        Imp_Identifiable ii = mapper.treeToValue(groupe_node, Imp_Identifiable.class) ;
        Imp_Nommable iei = mapper.treeToValue(groupe_node, Imp_Nommable.class) ;
        Imp_ElementComposite ic = mapper.treeToValue(groupe_node, Imp_ElementComposite.class) ;

        boolean elements_solidaires = groupe_node.get("elements_solidaires").asBoolean();

        return new Groupe(ii,iei,ic,elements_solidaires);
    }
}
