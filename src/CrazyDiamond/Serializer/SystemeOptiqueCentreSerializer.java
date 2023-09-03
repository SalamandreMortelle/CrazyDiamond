package CrazyDiamond.Serializer;

import CrazyDiamond.Model.Obstacle;
import CrazyDiamond.Model.SystemeOptiqueCentre;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class SystemeOptiqueCentreSerializer extends StdSerializer<SystemeOptiqueCentre> {

    public SystemeOptiqueCentreSerializer() {
        super(SystemeOptiqueCentre.class);
    }

    /**
     * @param soc
     * @param jsonGenerator
     * @param serializerProvider
     * @throws IOException
     */
    @Override
    public void serialize(SystemeOptiqueCentre soc, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

        jsonGenerator.writeStartObject();

//        jsonGenerator.writeStringField("@type",Cercle.class.getSimpleName());

        soc.appliquerSurNommable(jsonGenerator::writeObject);

        jsonGenerator.writeNumberField("x_origine",soc.XOrigine());
        jsonGenerator.writeNumberField("y_origine",soc.YOrigine());
        jsonGenerator.writeNumberField("orientation",soc.orientation());

        jsonGenerator.writeArrayFieldStart("obstacles");

        for (Obstacle o : soc.obstacles_centres())
            jsonGenerator.writeString(o.id());

        jsonGenerator.writeEndArray();

        jsonGenerator.writeEndObject();

    }
}
