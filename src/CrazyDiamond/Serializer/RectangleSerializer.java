package CrazyDiamond.Serializer;

import CrazyDiamond.Model.Rectangle;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class RectangleSerializer extends StdSerializer<Rectangle> {

    public RectangleSerializer() {
        super(Rectangle.class);
    }

    /**
     * @param rectangle
     * @param jsonGenerator
     * @param serializerProvider
     * @throws IOException
     */
    @Override
    public void serialize(Rectangle rectangle, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringField("@type",Rectangle.class.getSimpleName());

        rectangle.appliquerSurIdentifiable(jsonGenerator::writeObject);
        rectangle.appliquerSurNommable(jsonGenerator::writeObject);
        rectangle.appliquerSurElementAvecContour(jsonGenerator::writeObject);
        rectangle.appliquerSurElementAvecMatiere(jsonGenerator::writeObject);

        jsonGenerator.writeNumberField("x_centre",rectangle.xCentre());
        jsonGenerator.writeNumberField("y_centre",rectangle.yCentre());
        jsonGenerator.writeNumberField("largeur",rectangle.largeur());
        jsonGenerator.writeNumberField("hauteur",rectangle.hauteur());
        jsonGenerator.writeNumberField("orientation",rectangle.orientation());

        jsonGenerator.writeEndObject();

    }
}
