package CrazyDiamond.Serializer;

import CrazyDiamond.Model.*;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class ConiqueDeserializer extends StdDeserializer<Conique> {

    public ConiqueDeserializer() {
        this(Conique.class);
    }

    public ConiqueDeserializer(Class<?> vc) {
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
    public Conique deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {

        final ObjectCodec mapper = jsonParser.getCodec();
        final JsonNode conique_node = mapper.readTree(jsonParser);

        double facteur_conversion = 1d ;

        Object facteur_conversion_obj = deserializationContext.getAttribute("facteur_conversion") ;

        if (facteur_conversion_obj!=null)
            facteur_conversion = (Double) facteur_conversion_obj ;

        Imp_Identifiable ii = mapper.treeToValue(conique_node, Imp_Identifiable.class) ;
        Imp_Nommable iei = mapper.treeToValue(conique_node, Imp_Nommable.class) ;
        Imp_ElementAvecContour iec = mapper.treeToValue(conique_node, Imp_ElementAvecContour.class) ;
        Imp_ElementAvecMatiere iem = mapper.treeToValue(conique_node, Imp_ElementAvecMatiere.class) ;

        Conique conique = new Conique(ii,iei,iec,iem,
                conique_node.get("x_foyer").asDouble()*facteur_conversion,
                conique_node.get("y_foyer").asDouble()*facteur_conversion,
                conique_node.get("orientation").asDouble(),
                conique_node.get("parametre").asDouble()*facteur_conversion,
                conique_node.get("excentricite").asDouble()) ;

        return conique;
    }
}
