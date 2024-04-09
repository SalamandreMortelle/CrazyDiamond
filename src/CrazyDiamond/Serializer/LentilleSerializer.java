package CrazyDiamond.Serializer;

import CrazyDiamond.Model.Lentille;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class LentilleSerializer extends StdSerializer<Lentille> {

    public LentilleSerializer() {
        super(Lentille.class);
    }

    @Override
    public void serialize(Lentille lentille, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringField("@type",Lentille.class.getSimpleName());

        lentille.appliquerSurIdentifiable(jsonGenerator::writeObject);
        lentille.appliquerSurNommable(jsonGenerator::writeObject);
        lentille.appliquerSurElementAvecContour(jsonGenerator::writeObject);
        lentille.appliquerSurElementAvecMatiere(jsonGenerator::writeObject);

        jsonGenerator.writeNumberField("x_centre",lentille.xCentre());
        jsonGenerator.writeNumberField("y_centre",lentille.yCentre());
        jsonGenerator.writeNumberField("epaisseur",lentille.epaisseur());
        jsonGenerator.writeNumberField("r_courbure_1",lentille.rayonCourbure1());
        jsonGenerator.writeBooleanField("face_1_plane",lentille.face1Plane());
        jsonGenerator.writeNumberField("r_courbure_2",lentille.rayonCourbure2());
        jsonGenerator.writeBooleanField("face_2_plane",lentille.face2Plane());
        jsonGenerator.writeNumberField("diametre",lentille.diametre());
        jsonGenerator.writeNumberField("orientation",lentille.orientation());

        jsonGenerator.writeEndObject();

    }
}
