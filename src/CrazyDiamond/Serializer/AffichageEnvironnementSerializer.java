package CrazyDiamond.Serializer;

import CrazyDiamond.Controller.CanvasAffichageEnvironnement;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.Iterator;

public class AffichageEnvironnementSerializer extends StdSerializer<CanvasAffichageEnvironnement> {

    public AffichageEnvironnementSerializer() {
        super(CanvasAffichageEnvironnement.class);
    }

    @Override
    public void serialize(CanvasAffichageEnvironnement cae, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

        // jsonGenerator.writeFieldName("affichage_environnement");

        jsonGenerator.writeStartObject();

        jsonGenerator.writeObjectField("environnement",cae.environnement());

        jsonGenerator.writeFieldName("parametres_affichage_environnement");

        jsonGenerator.writeStartObject();

        // Propriétés du canvas d'affichage de l'environnement
        jsonGenerator.writeNumberField("x_min", cae.xmin());
        jsonGenerator.writeNumberField("x_max", cae.xmax());
        jsonGenerator.writeNumberField("y_centre", cae.ycentre());
        jsonGenerator.writeBooleanField("normales_visibles", cae.normalesVisibles());
        jsonGenerator.writeStringField("couleur_normales", cae.couleurNormales().toString());
        jsonGenerator.writeBooleanField("prolongements_avant_visibles",cae.prolongementsAvantVisibles());
        jsonGenerator.writeBooleanField("prolongements_arriere_visibles",cae.prolongementsArriereVisibles());
        jsonGenerator.writeBooleanField("commentaire_visible",cae.commentaireVisible());

        jsonGenerator.writeEndObject(); // Fin de parametres_affichage_environnement

        jsonGenerator.writeEndObject();

    }
}
