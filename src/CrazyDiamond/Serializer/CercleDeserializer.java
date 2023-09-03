package CrazyDiamond.Serializer;

import CrazyDiamond.Model.*;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class CercleDeserializer extends StdDeserializer<Cercle> {

    public CercleDeserializer() {
        this(Cercle.class);
    }

    public CercleDeserializer(Class<?> vc) {
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
    public Cercle deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {

        final ObjectCodec mapper = jsonParser.getCodec();
        final JsonNode cercle_node = mapper.readTree(jsonParser);

        Imp_Identifiable ii = mapper.treeToValue(cercle_node, Imp_Identifiable.class) ;
        Imp_Nommable iei = mapper.treeToValue(cercle_node, Imp_Nommable.class) ;
        Imp_ElementAvecContour iec = mapper.treeToValue(cercle_node, Imp_ElementAvecContour.class) ;
        Imp_ElementAvecMatiere iem = mapper.treeToValue(cercle_node, Imp_ElementAvecMatiere.class) ;

        Cercle cercle = new Cercle(ii,iei,iec,iem,cercle_node.get("x_centre").asDouble(),cercle_node.get("y_centre").asDouble(),cercle_node.get("rayon").asDouble()) ;

        return cercle;
    }
}
