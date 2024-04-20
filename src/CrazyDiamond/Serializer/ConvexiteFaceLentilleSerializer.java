package CrazyDiamond.Serializer;

import CrazyDiamond.Model.ConvexiteFaceLentille;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class ConvexiteFaceLentilleSerializer extends StdSerializer<ConvexiteFaceLentille> {

    public ConvexiteFaceLentilleSerializer() { super(ConvexiteFaceLentille.class); }

    @Override
    public void serialize(ConvexiteFaceLentille convexiteFaceLentille, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStringField("convexite_face_lentille", convexiteFaceLentille.toString());
    }
}
