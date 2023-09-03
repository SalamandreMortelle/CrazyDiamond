package CrazyDiamond.Serializer;

import CrazyDiamond.Model.Segment;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class SegmentSerializer extends StdSerializer<Segment> {

    public SegmentSerializer() {
        super(Segment.class);
    }

    /**
     * @param segment
     * @param jsonGenerator
     * @param serializerProvider
     * @throws IOException
     */
    @Override
    public void serialize(Segment segment, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringField("@type",Segment.class.getSimpleName());

        segment.appliquerSurIdentifiable(jsonGenerator::writeObject);
        segment.appliquerSurNommable(jsonGenerator::writeObject);
        segment.appliquerSurElementAvecContour(jsonGenerator::writeObject);
        segment.appliquerSurElementSansEpaisseur(jsonGenerator::writeObject);

        jsonGenerator.writeNumberField("x_centre",segment.xCentre());
        jsonGenerator.writeNumberField("y_centre",segment.yCentre());
        jsonGenerator.writeNumberField("longueur",segment.longueur());
        jsonGenerator.writeNumberField("orientation",segment.orientation());
        jsonGenerator.writeNumberField("rayon_diaphragme",segment.rayonDiaphragme());

        jsonGenerator.writeEndObject();

    }
}
