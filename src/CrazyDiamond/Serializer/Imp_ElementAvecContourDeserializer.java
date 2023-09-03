package CrazyDiamond.Serializer;

import CrazyDiamond.Model.Imp_ElementAvecContour;
import CrazyDiamond.Model.TraitementSurface;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import javafx.scene.paint.Color;

import java.io.IOException;

public class Imp_ElementAvecContourDeserializer extends StdDeserializer<Imp_ElementAvecContour> {

    public Imp_ElementAvecContourDeserializer() {
        this(Imp_ElementAvecContour.class);
    }
    public Imp_ElementAvecContourDeserializer(Class<?> vc) {
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
    public Imp_ElementAvecContour deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {

//        final ObjectMapper mapper = (ObjectMapper) jsonParser.getCodec();
        final ObjectCodec mapper = jsonParser.getCodec();
        final JsonNode iec_node = mapper.readTree(jsonParser);

        String couleur_contour = iec_node.get("couleur_contour").asText() ;
        double taux_reflexion_surface = iec_node.get("taux_reflexion_surface").asDouble() ;
        double orientation_axe_polariseur = iec_node.get("orientation_axe_polariseur").asDouble() ;
        String traitement_surface = iec_node.get("traitement_surface").asText() ;
        final TraitementSurface t_s = TraitementSurface.fromValue(traitement_surface) ;

        return new Imp_ElementAvecContour(Color.valueOf(couleur_contour),t_s,taux_reflexion_surface,orientation_axe_polariseur) ;
    }

}
