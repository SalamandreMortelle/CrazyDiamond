package CrazyDiamond.Serializer;

import CrazyDiamond.Model.Imp_ElementAvecMatiere;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class Imp_ElementAvecMatiereSerializer extends StdSerializer<Imp_ElementAvecMatiere> {

    public Imp_ElementAvecMatiereSerializer() {
        super(Imp_ElementAvecMatiere.class);
    }

    /**
     * @param el_am
     * @param jsonGenerator
     * @param serializerProvider
     * @throws IOException
     */
    @Override
    public void serialize(Imp_ElementAvecMatiere el_am, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

        jsonGenerator.writeStringField("couleur_matiere",el_am.couleurMatiere().toString());
        jsonGenerator.writeObjectField("nature_milieu",el_am.natureMilieu());
        jsonGenerator.writeObjectField("type_surface",el_am.typeSurface());
        jsonGenerator.writeNumberField("indice_refraction",el_am.indiceRefraction());

    }

}
