package CrazyDiamond.Serializer;

import CrazyDiamond.Model.TypeSurface;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class TypeSurfaceSerializer extends StdSerializer<TypeSurface> {

    public TypeSurfaceSerializer() { super(TypeSurface.class); }

    /**
     * @param typeSurface
     * @param jsonGenerator
     * @param serializerProvider
     * @throws IOException
     */
    @Override
    public void serialize(TypeSurface typeSurface, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStringField("type_surface",typeSurface.toString());
    }
}
