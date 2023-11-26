package CrazyDiamond.Serializer;

import CrazyDiamond.Model.*;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class DemiPlanDeserializer extends StdDeserializer<DemiPlan> {

    public DemiPlanDeserializer() {
        this(DemiPlan.class);
    }

    public DemiPlanDeserializer(Class<?> vc) {
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
    public DemiPlan deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {

        final ObjectCodec mapper = jsonParser.getCodec();
        final JsonNode demi_plan_node = mapper.readTree(jsonParser);

        double facteur_conversion = 1d ;

        Object facteur_conversion_obj = deserializationContext.getAttribute("facteur_conversion") ;

        if (facteur_conversion_obj!=null)
            facteur_conversion = (Double) facteur_conversion_obj ;

        Imp_Identifiable ii = mapper.treeToValue(demi_plan_node, Imp_Identifiable.class) ;
        Imp_Nommable iei = mapper.treeToValue(demi_plan_node, Imp_Nommable.class) ;
        Imp_ElementAvecContour iec = mapper.treeToValue(demi_plan_node, Imp_ElementAvecContour.class) ;
        Imp_ElementAvecMatiere iem = mapper.treeToValue(demi_plan_node, Imp_ElementAvecMatiere.class) ;

        DemiPlan demi_plan = new DemiPlan(ii,iei,iec,iem,
                demi_plan_node.get("x_origine").asDouble()*facteur_conversion,
                demi_plan_node.get("y_origine").asDouble()*facteur_conversion,
                demi_plan_node.get("orientation").asDouble()) ;

        return demi_plan;
    }
}
