package CrazyDiamond.Serializer;

import CrazyDiamond.Model.Obstacle;
import CrazyDiamond.Model.RencontreDioptreParaxial;
import CrazyDiamond.Model.SystemeOptiqueCentre;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class SystemeOptiqueCentreSerializer extends StdSerializer<SystemeOptiqueCentre> {

    public SystemeOptiqueCentreSerializer() {
        super(SystemeOptiqueCentre.class);
    }

    @Override
    public void serialize(SystemeOptiqueCentre soc, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

        jsonGenerator.writeStartObject();

//        jsonGenerator.writeStringField("@type",Cercle.class.getSimpleName());

        soc.appliquerSurNommable(jsonGenerator::writeObject);

        jsonGenerator.writeNumberField("x_origine",soc.XOrigine());
        jsonGenerator.writeNumberField("y_origine",soc.YOrigine());
        jsonGenerator.writeNumberField("orientation",soc.orientation());

        jsonGenerator.writeNumberField("z_objet",soc.ZObjet());
        jsonGenerator.writeNumberField("h_objet",soc.HObjet());

        jsonGenerator.writeBooleanField("montrer_objet",soc.MontrerObjet());
        jsonGenerator.writeBooleanField("montrer_image",soc.MontrerImage());

        jsonGenerator.writeBooleanField("montrer_plans_focaux",soc.MontrerPlansFocaux());
        jsonGenerator.writeBooleanField("montrer_plans_principaux",soc.MontrerPlansPrincipaux());
        jsonGenerator.writeBooleanField("montrer_plans_nodaux",soc.MontrerPlansNodaux());

        jsonGenerator.writeBooleanField("montrer_dioptres",soc.MontrerDioptres());

        jsonGenerator.writeArrayFieldStart("obstacles");

        for (Obstacle o : soc.obstacles_centres())
            jsonGenerator.writeString(o.id());

        jsonGenerator.writeEndArray();

        jsonGenerator.writeArrayFieldStart("modalites_traversee_dioptres");

        for (RencontreDioptreParaxial renc : soc.dioptresRencontres()) {
            jsonGenerator.writeStartObject();
            if (renc.rayonDiaphragme()!=null)
                jsonGenerator.writeNumberField("r_diaphragme",renc.rayonDiaphragme());
            jsonGenerator.writeBooleanField("ignorer",renc.ignorer());
            jsonGenerator.writeEndObject();
        }

        jsonGenerator.writeEndArray();


        jsonGenerator.writeEndObject();

    }
}
