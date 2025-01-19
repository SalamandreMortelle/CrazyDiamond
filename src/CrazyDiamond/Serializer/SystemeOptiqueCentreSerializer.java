package CrazyDiamond.Serializer;

import CrazyDiamond.Model.*;
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

        jsonGenerator.writeNumberField("z_objet",soc.ZGeometriqueObjet());
        jsonGenerator.writeNumberField("h_objet",soc.HObjet());

        jsonGenerator.writeBooleanField("montrer_objet",soc.MontrerObjet());
        jsonGenerator.writeBooleanField("montrer_image",soc.MontrerImage());

        jsonGenerator.writeBooleanField("montrer_plans_focaux",soc.MontrerPlansFocaux());
        jsonGenerator.writeBooleanField("montrer_plans_principaux",soc.MontrerPlansPrincipaux());
        jsonGenerator.writeBooleanField("montrer_plans_nodaux",soc.MontrerPlansNodaux());

        jsonGenerator.writeBooleanField("montrer_dioptres",soc.MontrerDioptres());

        // Sérialisation du tableau des obstacles dans un 1er tableau
        jsonGenerator.writeArrayFieldStart("obstacles");

        for (ElementDeSOC el : soc.elementsCentresRacine()) {
            if (el.estUnObstacle())
                jsonGenerator.writeString(((Obstacle) el).id());
        }

        jsonGenerator.writeEndArray(); // Fin du tableau des obstacles


        // Sérialisation des sous-SOCs dans un 2ème tableau
        boolean sous_soc_rencontre = false ;
        for (ElementDeSOC el : soc.elementsCentresRacine()) {
            if (el.estUnSOC()) {
                // Écriture des sous-SOC dans un tableau
                if (!sous_soc_rencontre) {
                    jsonGenerator.writeArrayFieldStart("sous_systemes");
                    sous_soc_rencontre=true ;
                }
                jsonGenerator.writeObject(el);
            }
        }
        if (sous_soc_rencontre) // Fin du tableau des sous-SOCs
            jsonGenerator.writeEndArray();


        // Sérialisation des modalités de traversée
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
