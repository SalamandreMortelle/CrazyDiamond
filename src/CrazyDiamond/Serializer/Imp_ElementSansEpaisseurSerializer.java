package CrazyDiamond.Serializer;

import CrazyDiamond.Model.Imp_ElementSansEpaisseur;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class Imp_ElementSansEpaisseurSerializer extends StdSerializer<Imp_ElementSansEpaisseur> {

    public Imp_ElementSansEpaisseurSerializer() {
        super(Imp_ElementSansEpaisseur.class);
    }

    /**
     * @param el_se
     * @param jsonGenerator
     * @param serializerProvider
     * @throws IOException
     */
    @Override
    public void serialize(Imp_ElementSansEpaisseur el_se, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

            jsonGenerator.writeObjectField("nature_milieu",el_se.natureMilieu());

    }

}
