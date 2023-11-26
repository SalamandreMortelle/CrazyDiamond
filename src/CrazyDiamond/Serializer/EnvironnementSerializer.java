package CrazyDiamond.Serializer;

import CrazyDiamond.Model.Environnement;
import CrazyDiamond.Model.Obstacle;
import CrazyDiamond.Model.Source;
import CrazyDiamond.Model.SystemeOptiqueCentre;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.Iterator;

public class EnvironnementSerializer extends StdSerializer<Environnement> {

    public EnvironnementSerializer() {
        super(Environnement.class);
    }

    /**
     * @param environnement
     * @param jsonGenerator
     * @param serializerProvider
     * @throws IOException
     */
    @Override
    public void serialize(Environnement environnement, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringField("unite",environnement.unite().toString());

        // Propriétés de l'environnement
        jsonGenerator.writeStringField("couleur_fond", environnement.couleurFond().toString());
        jsonGenerator.writeBooleanField("reflexion_avec_refraction", environnement.reflexionAvecRefraction());


        if (!environnement.commentaire().isEmpty())
          jsonGenerator.writeStringField("commentaire", environnement.commentaire());

        // Obstacles
        if (environnement.nombreObstacles()>0) {
            jsonGenerator.writeArrayFieldStart("obstacles");

            Iterator<Obstacle> ito = environnement.iterateur_obstacles();
            while (ito.hasNext()) {
                jsonGenerator.writeObject(ito.next());
            }

            jsonGenerator.writeEndArray();
        }

        // Sources
        if (environnement.nombreSources()>0) {
            jsonGenerator.writeArrayFieldStart("sources");

            Iterator<Source> its = environnement.iterateur_sources();
            while (its.hasNext()) {
                jsonGenerator.writeObject(its.next());
            }

            jsonGenerator.writeEndArray();
        }

        // Systemes Optiques Centres
        if (environnement.nombreSystemesOptiquesCentres()>0) {
            jsonGenerator.writeArrayFieldStart("systemes_optiques_centres");

            Iterator<SystemeOptiqueCentre> itsoc = environnement.iterateur_systemesOptiquesCentres();
            while (itsoc.hasNext()) {
                jsonGenerator.writeObject(itsoc.next());
            }

            jsonGenerator.writeEndArray();
        }

        jsonGenerator.writeEndObject();

    }
}
