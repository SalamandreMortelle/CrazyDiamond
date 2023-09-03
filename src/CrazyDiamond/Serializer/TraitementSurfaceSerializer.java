package CrazyDiamond.Serializer;

import CrazyDiamond.Model.TraitementSurface;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class TraitementSurfaceSerializer extends StdSerializer<TraitementSurface> {

    public TraitementSurfaceSerializer() { super(TraitementSurface.class); }

    /**
     * @param traitementSurface
     * @param jsonGenerator
     * @param serializerProvider
     * @throws IOException
     */
    @Override
    public void serialize(TraitementSurface traitementSurface, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStringField("traitement_surface",traitementSurface.toString());

    }
}
