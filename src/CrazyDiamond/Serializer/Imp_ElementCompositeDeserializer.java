package CrazyDiamond.Serializer;

import CrazyDiamond.Model.*;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class Imp_ElementCompositeDeserializer extends StdDeserializer<Imp_ElementComposite> {

    public Imp_ElementCompositeDeserializer() {
        this(Imp_ElementComposite.class);
    }
    public Imp_ElementCompositeDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Imp_ElementComposite deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {

//        final ObjectMapper mapper = (ObjectMapper) jsonParser.getCodec();
        final ObjectCodec mapper = jsonParser.getCodec();
        final JsonNode el_composite_node = mapper.readTree(jsonParser);

        String nom_tableau_elements = el_composite_node.has("elements")?
                "elements" :
                (el_composite_node.has("composants")?"composants":null) ;

        Imp_ElementComposite iec = new Imp_ElementComposite() ;

        if (nom_tableau_elements!=null) {

            JsonNode liste_elements_node = el_composite_node.get(nom_tableau_elements) ;

            int nb_comps = liste_elements_node.size();

            for (int i = 0; i < nb_comps; i++) {
                JsonNode comp_node = liste_elements_node.get(i);
                switch (comp_node.get("@type").asText()) {
                    case "Lentille" -> iec.ajouterObstacle(mapper.treeToValue(comp_node, Lentille.class));
                    case "Cercle" -> iec.ajouterObstacle(mapper.treeToValue(comp_node, Cercle.class));
                    case "Conique" -> iec.ajouterObstacle(mapper.treeToValue(comp_node, Conique.class));
                    case "DemiPlan" -> iec.ajouterObstacle(mapper.treeToValue(comp_node, DemiPlan.class));
                    case "Prisme" -> iec.ajouterObstacle(mapper.treeToValue(comp_node, Prisme.class));
                    case "Rectangle" -> iec.ajouterObstacle(mapper.treeToValue(comp_node, Rectangle.class));
                    case "Segment" -> iec.ajouterObstacle(mapper.treeToValue(comp_node, Segment.class));
                    case "Groupe" -> iec.ajouterObstacle(mapper.treeToValue(comp_node, Groupe.class));
                    case "Composition" -> iec.ajouterObstacle(mapper.treeToValue(comp_node, Composition.class));
                }
            }

        }


        return iec ;
    }

}
