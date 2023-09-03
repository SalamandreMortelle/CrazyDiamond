package CrazyDiamond.Serializer;

import CrazyDiamond.Model.DemiPlan;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class DemiPlanSerializer extends StdSerializer<DemiPlan> {

    public DemiPlanSerializer() {
        super(DemiPlan.class);
    }

    /**
     * @param demi_plan
     * @param jsonGenerator
     * @param serializerProvider
     * @throws IOException
     */
    @Override
    public void serialize(DemiPlan demi_plan, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringField("@type",DemiPlan.class.getSimpleName());

        demi_plan.appliquerSurIdentifiable(jsonGenerator::writeObject);
        demi_plan.appliquerSurNommable(jsonGenerator::writeObject);
        demi_plan.appliquerSurElementAvecContour(jsonGenerator::writeObject);
        demi_plan.appliquerSurElementAvecMatiere(jsonGenerator::writeObject);

        jsonGenerator.writeNumberField("x_origine",demi_plan.xOrigine());
        jsonGenerator.writeNumberField("y_origine",demi_plan.yOrigine());
        jsonGenerator.writeNumberField("orientation",demi_plan.orientation());

        jsonGenerator.writeEndObject();

    }
}
