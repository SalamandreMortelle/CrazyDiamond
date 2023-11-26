package CrazyDiamond.Serializer;

import CrazyDiamond.Model.Environnement;
import CrazyDiamond.Model.Imp_Nommable;
import CrazyDiamond.Model.SystemeOptiqueCentre;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import javafx.geometry.Point2D;

import java.io.IOException;

public class SystemeOptiqueCentreDeserializer extends StdDeserializer<SystemeOptiqueCentre> {

    public SystemeOptiqueCentreDeserializer() {
        this(SystemeOptiqueCentre.class);
    }

    public SystemeOptiqueCentreDeserializer(Class<?> vc) {
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
    public SystemeOptiqueCentre deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {

        final ObjectCodec mapper = jsonParser.getCodec();
        final JsonNode soc_node = mapper.readTree(jsonParser);

        double facteur_conversion = 1d ;

        Object facteur_conversion_obj = deserializationContext.getAttribute("facteur_conversion") ;

        if (facteur_conversion_obj!=null)
            facteur_conversion = (Double) facteur_conversion_obj ;

        Environnement env = (Environnement) deserializationContext.getAttribute("environnement") ;

        Imp_Nommable iei = mapper.treeToValue(soc_node, Imp_Nommable.class) ;

        Point2D origine = new Point2D(
                soc_node.get("x_origine").asDouble()*facteur_conversion,
                soc_node.get("y_origine").asDouble()*facteur_conversion ) ;

        SystemeOptiqueCentre soc = new SystemeOptiqueCentre((Environnement) env,iei,origine,soc_node.get("orientation").asDouble()) ;

        if (soc_node.has("obstacles")) {

            int nb_obs_soc = soc_node.get("obstacles").size();

            for (int i = 0; i < nb_obs_soc; i++) {
                String obs_id = soc_node.get("obstacles").get(i).asText();

//                System.out.println("Obstacle "+obs_id+" à ajouter dans SOC "+soc.nom());
                soc.ajouterObstacle(env.obstacle(obs_id));
            }

        }

        return soc;
    }
}
