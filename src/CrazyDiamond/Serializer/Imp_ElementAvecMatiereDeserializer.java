package CrazyDiamond.Serializer;

import CrazyDiamond.Model.Imp_ElementAvecMatiere;
import CrazyDiamond.Model.NatureMilieu;
import CrazyDiamond.Model.TypeSurface;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import javafx.scene.paint.Color;

import java.io.IOException;

public class Imp_ElementAvecMatiereDeserializer extends StdDeserializer<Imp_ElementAvecMatiere> {

    public Imp_ElementAvecMatiereDeserializer() {
        this(Imp_ElementAvecMatiere.class);
    }
    public Imp_ElementAvecMatiereDeserializer(Class<?> vc) {
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
    public Imp_ElementAvecMatiere deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {

        final ObjectCodec mapper = jsonParser.getCodec();
        final JsonNode iem_node = mapper.readTree(jsonParser);

        String couleur_matiere = iem_node.get("couleur_matiere").asText() ;

        String nature_milieu = iem_node.get("nature_milieu").asText() ;
        final NatureMilieu n_m = NatureMilieu.fromValue(nature_milieu) ;

        String type_surface = iem_node.get("type_surface").asText() ;
        final TypeSurface t_s = TypeSurface.fromValue(type_surface) ;

        double indice_refraction = iem_node.get("indice_refraction").asDouble() ;

        return new Imp_ElementAvecMatiere(t_s,n_m,indice_refraction,Color.valueOf(couleur_matiere)) ;
    }


}
