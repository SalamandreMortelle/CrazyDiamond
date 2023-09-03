package CrazyDiamond.Serializer;

import CrazyDiamond.Model.*;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class PrismeDeserializer extends StdDeserializer<Prisme> {

    public PrismeDeserializer() {
        this(Prisme.class);
    }

    public PrismeDeserializer(Class<?> vc) {
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
    public Prisme deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {

        final ObjectCodec mapper = jsonParser.getCodec();
        final JsonNode prisme_node = mapper.readTree(jsonParser);

        Imp_Identifiable ii = mapper.treeToValue(prisme_node, Imp_Identifiable.class) ;
        Imp_Nommable iei = mapper.treeToValue(prisme_node, Imp_Nommable.class) ;
        Imp_ElementAvecContour iec = mapper.treeToValue(prisme_node, Imp_ElementAvecContour.class) ;
        Imp_ElementAvecMatiere iem = mapper.treeToValue(prisme_node, Imp_ElementAvecMatiere.class) ;

        Prisme prisme = new Prisme(ii,iei,iec,iem,prisme_node.get("x_centre").asDouble(),prisme_node.get("y_centre").asDouble(),
                prisme_node.get("angle_sommet").asDouble(),
                prisme_node.get("largeur_base").asDouble(),
                prisme_node.get("orientation").asDouble()) ;

        return prisme;
    }
}
