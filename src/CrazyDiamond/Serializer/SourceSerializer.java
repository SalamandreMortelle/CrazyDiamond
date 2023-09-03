package CrazyDiamond.Serializer;

import CrazyDiamond.Model.Source;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import javafx.geometry.Point2D;

import java.io.IOException;

public class SourceSerializer extends StdSerializer<Source> {

    public SourceSerializer() {
        super(Source.class);
    }

    /**
     * @param source
     * @param jsonGenerator
     * @param serializerProvider
     * @throws IOException
     */
    @Override
    public void serialize(Source source, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

        jsonGenerator.writeStartObject();

        source.appliquerSurNommable(jsonGenerator::writeObject);

        Point2D position = source.position() ;
        jsonGenerator.writeNumberField("position_x",position.getX());
        jsonGenerator.writeNumberField("position_y",position.getY());
        jsonGenerator.writeNumberField("angle",source.orientation());
        jsonGenerator.writeNumberField("nombre_max_obstacles_rencontres",source.nombreMaximumRencontresObstacle());
        jsonGenerator.writeNumberField("nombre_rayons",source.nombreRayons());
        jsonGenerator.writeNumberField("ouverture_pinceau",source.ouverturePinceau());
        jsonGenerator.writeNumberField("largeur_projecteur",source.largeurProjecteur());
        jsonGenerator.writeObjectField("type",source.type());
        jsonGenerator.writeStringField("couleur",source.couleur().toString());
        jsonGenerator.writeBooleanField("lumiere_polarisee",source.lumierePolarisee());
        jsonGenerator.writeNumberField("angle_champ_electrique",source.angleChampElectrique());

        jsonGenerator.writeEndObject();

    }

}
