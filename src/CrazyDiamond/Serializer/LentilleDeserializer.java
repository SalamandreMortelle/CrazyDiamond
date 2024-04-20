package CrazyDiamond.Serializer;

import CrazyDiamond.Model.*;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class LentilleDeserializer extends StdDeserializer<Lentille> {

    public LentilleDeserializer() {
        this(Lentille.class);
    }

    public LentilleDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Lentille deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {

        final ObjectCodec mapper = jsonParser.getCodec();
        final JsonNode lentille_node = mapper.readTree(jsonParser);

        double facteur_conversion = 1d ;

        Object facteur_conversion_obj = deserializationContext.getAttribute("facteur_conversion") ;

        if (facteur_conversion_obj!=null)
            facteur_conversion = (Double) facteur_conversion_obj ;

        Imp_Identifiable ii = mapper.treeToValue(lentille_node, Imp_Identifiable.class) ;
        Imp_Nommable in = mapper.treeToValue(lentille_node, Imp_Nommable.class) ;
        Imp_ElementAvecContour iec = mapper.treeToValue(lentille_node, Imp_ElementAvecContour.class) ;
        Imp_ElementAvecMatiere iam = mapper.treeToValue(lentille_node, Imp_ElementAvecMatiere.class) ;

        return new Lentille(ii,in,iec,iam,
                lentille_node.get("x_centre").asDouble()*facteur_conversion,
                lentille_node.get("y_centre").asDouble()*facteur_conversion,
                lentille_node.get("epaisseur").asDouble()*facteur_conversion,
                FormeFaceLentille.fromValue(lentille_node.get("forme_face_1").asText()),
                lentille_node.get("rayon_1").asDouble()*facteur_conversion,
                lentille_node.get("parametre_1").asDouble()*facteur_conversion,
                lentille_node.get("excentricite_1").asDouble()*facteur_conversion,
                ConvexiteFaceLentille.fromValue(lentille_node.get("convexite_face_1").asText()),
                FormeFaceLentille.fromValue(lentille_node.get("forme_face_2").asText()),
                lentille_node.get("rayon_2").asDouble()*facteur_conversion,
                lentille_node.get("parametre_2").asDouble()*facteur_conversion,
                lentille_node.get("excentricite_2").asDouble()*facteur_conversion,
                ConvexiteFaceLentille.fromValue(lentille_node.get("convexite_face_2").asText()),
                lentille_node.get("diametre").asDouble()*facteur_conversion,
                lentille_node.get("orientation").asDouble()) ;
    }

}
