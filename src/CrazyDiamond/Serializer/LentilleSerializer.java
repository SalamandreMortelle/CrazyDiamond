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

        jsonGenerator.writeObjectField("forme_face_1",lentille.formeFace1());
        jsonGenerator.writeNumberField("rayon_1",lentille.rayon1());
        jsonGenerator.writeNumberField("parametre_1",lentille.parametre1());
        jsonGenerator.writeNumberField("excentricite_1",lentille.excentricite1());
        jsonGenerator.writeObjectField("convexite_face_1",lentille.convexiteFace1());

        jsonGenerator.writeObjectField("forme_face_2",lentille.formeFace2());
        jsonGenerator.writeNumberField("rayon_2",lentille.rayon2());
        jsonGenerator.writeNumberField("parametre_2",lentille.parametre2());
        jsonGenerator.writeNumberField("excentricite_2",lentille.excentricite2());
        jsonGenerator.writeObjectField("convexite_face_2",lentille.convexiteFace2());

        jsonGenerator.writeNumberField("diametre",lentille.diametre());
        jsonGenerator.writeNumberField("orientation",lentille.orientation());

        jsonGenerator.writeEndObject();

    }
}
