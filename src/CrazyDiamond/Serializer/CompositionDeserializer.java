package CrazyDiamond.Serializer;

import CrazyDiamond.Model.*;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class CompositionDeserializer extends StdDeserializer<Composition> {

    public CompositionDeserializer() {
        this(Composition.class);
    }

    public CompositionDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Composition deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {

        final ObjectMapper mapper = (ObjectMapper) jsonParser.getCodec();

        final JsonNode composition_node = mapper.readTree(jsonParser);

        Imp_Identifiable ii = mapper.treeToValue(composition_node, Imp_Identifiable.class) ;
        Imp_Nommable iei = mapper.treeToValue(composition_node, Imp_Nommable.class) ;
        Imp_ElementComposite ic = mapper.treeToValue(composition_node, Imp_ElementComposite.class) ;
        Imp_ElementAvecContour iec = mapper.treeToValue(composition_node, Imp_ElementAvecContour.class) ;
        Imp_ElementAvecMatiere iem = mapper.treeToValue(composition_node, Imp_ElementAvecMatiere.class) ;

        String operateur = composition_node.get("operateur").asText() ;
        final Composition.Operateur op = Composition.Operateur.fromValue(operateur) ;

        return new Composition(ii,iei,ic,iec,iem,op);
    }
}
