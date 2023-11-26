package CrazyDiamond.Serializer;

import CrazyDiamond.Model.*;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class SegmentDeserializer extends StdDeserializer<Segment> {

    public SegmentDeserializer() {
        this(Segment.class);
    }

    public SegmentDeserializer(Class<?> vc) {
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
    public Segment deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {

        final ObjectCodec mapper = jsonParser.getCodec();
        final JsonNode segment_node = mapper.readTree(jsonParser);

        double facteur_conversion = 1d ;

        Object facteur_conversion_obj = deserializationContext.getAttribute("facteur_conversion") ;

        if (facteur_conversion_obj!=null)
            facteur_conversion = (Double) facteur_conversion_obj ;

        Imp_Identifiable ii = mapper.treeToValue(segment_node, Imp_Identifiable.class) ;
        Imp_Nommable iei = mapper.treeToValue(segment_node, Imp_Nommable.class) ;
        Imp_ElementAvecContour iec = mapper.treeToValue(segment_node, Imp_ElementAvecContour.class) ;
        Imp_ElementSansEpaisseur ies = mapper.treeToValue(segment_node, Imp_ElementSansEpaisseur.class) ;

        Segment segment = new Segment(ii,iei,iec,ies,
                segment_node.get("x_centre").asDouble()*facteur_conversion,
                segment_node.get("y_centre").asDouble()*facteur_conversion,
                segment_node.get("longueur").asDouble()*facteur_conversion,
                segment_node.get("orientation").asDouble(),
                segment_node.get("rayon_diaphragme").asDouble()*facteur_conversion) ;

        return segment;
    }
}
