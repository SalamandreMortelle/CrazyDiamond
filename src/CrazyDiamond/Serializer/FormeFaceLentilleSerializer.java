package CrazyDiamond.Serializer;

import CrazyDiamond.Model.FormeFaceLentille;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class FormeFaceLentilleSerializer extends StdSerializer<FormeFaceLentille> {

    public FormeFaceLentilleSerializer() { super(FormeFaceLentille.class); }

    @Override
    public void serialize(FormeFaceLentille formeFaceLentille, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStringField("forme_face_lentille", formeFaceLentille.toString());
    }
}
