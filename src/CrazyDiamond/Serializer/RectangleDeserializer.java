package CrazyDiamond.Serializer;

import CrazyDiamond.Model.*;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class RectangleDeserializer extends StdDeserializer<Rectangle> {

    public RectangleDeserializer() {
        this(Rectangle.class);
    }

    public RectangleDeserializer(Class<?> vc) {
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
    public Rectangle deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {

        final ObjectCodec mapper = jsonParser.getCodec();
        final JsonNode rectangle_node = mapper.readTree(jsonParser);

        Imp_Identifiable ii = mapper.treeToValue(rectangle_node, Imp_Identifiable.class) ;
        Imp_Nommable iei = mapper.treeToValue(rectangle_node, Imp_Nommable.class) ;
        Imp_ElementAvecContour iec = mapper.treeToValue(rectangle_node, Imp_ElementAvecContour.class) ;
        Imp_ElementAvecMatiere iem = mapper.treeToValue(rectangle_node, Imp_ElementAvecMatiere.class) ;

        Rectangle rectangle = new Rectangle(ii,iei,iec,iem,rectangle_node.get("x_centre").asDouble(),rectangle_node.get("y_centre").asDouble(),
                rectangle_node.get("largeur").asDouble(),
                rectangle_node.get("hauteur").asDouble(),
                rectangle_node.get("orientation").asDouble()) ;

        return rectangle;
    }
}
