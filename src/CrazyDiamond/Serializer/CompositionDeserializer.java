package CrazyDiamond.Serializer;

import CrazyDiamond.Model.*;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class CompositionDeserializer extends StdDeserializer<Composition> {

    public CompositionDeserializer() {
        this(Composition.class);
    }

    public CompositionDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Composition deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {

        final ObjectMapper mapper = (ObjectMapper) jsonParser.getCodec();
        final JsonNode composition_node = mapper.readTree(jsonParser);

//        Environnement env = (Environnement) deserializationContext.getAttribute("environnement") ;

        Object facteur_conversion_obj = deserializationContext.getAttribute("facteur_conversion") ;

        Imp_Identifiable ii = mapper.treeToValue(composition_node, Imp_Identifiable.class) ;
        Imp_Nommable iei = mapper.treeToValue(composition_node, Imp_Nommable.class) ;
        Imp_ElementAvecContour iec = mapper.treeToValue(composition_node, Imp_ElementAvecContour.class) ;
        Imp_ElementAvecMatiere iem = mapper.treeToValue(composition_node, Imp_ElementAvecMatiere.class) ;

        String operateur = composition_node.get("operateur").asText() ;
        final Composition.Operateur op = Composition.Operateur.fromValue(operateur) ;

        Composition composition = new Composition(ii,iei,iec,iem,op) ;

        if (composition_node.has("composants")) {

            JsonNode liste_comp_node = composition_node.get("composants") ;

            int nb_comps = liste_comp_node.size();

            if (facteur_conversion_obj == null) {
                for (int i = 0; i < nb_comps; i++) {
                    JsonNode comp_node = liste_comp_node.get(i);
                    switch (comp_node.get("@type").asText()) {
                        case "Cercle" -> composition.ajouterObstacle(mapper.treeToValue(comp_node, Cercle.class));
                        case "Conique" -> composition.ajouterObstacle(mapper.treeToValue(comp_node, Conique.class));
                        case "DemiPlan" -> composition.ajouterObstacle(mapper.treeToValue(comp_node, DemiPlan.class));
                        case "Prisme" -> composition.ajouterObstacle(mapper.treeToValue(comp_node, Prisme.class));
                        case "Rectangle" -> composition.ajouterObstacle(mapper.treeToValue(comp_node, Rectangle.class));
                        case "Segment" -> composition.ajouterObstacle(mapper.treeToValue(comp_node, Segment.class));
                        case "Composition" -> composition.ajouterObstacle(mapper.treeToValue(comp_node, Composition.class));
                    }
                }
             } else {
                for (int i = 0; i < nb_comps; i++) {
                    JsonNode comp_node = liste_comp_node.get(i);
                    switch (comp_node.get("@type").asText()) {
                        case "Cercle" -> composition.ajouterObstacle(mapper.readerFor(Cercle.class).withAttribute("facteur_conversion", facteur_conversion_obj).treeToValue(comp_node,Cercle.class));
                        case "Conique" -> composition.ajouterObstacle(mapper.readerFor(Conique.class).withAttribute("facteur_conversion", facteur_conversion_obj).treeToValue(comp_node,Conique.class));
                        case "DemiPlan" -> composition.ajouterObstacle(mapper.readerFor(DemiPlan.class).withAttribute("facteur_conversion", facteur_conversion_obj).treeToValue(comp_node,DemiPlan.class));
                        case "Prisme" -> composition.ajouterObstacle(mapper.readerFor(Prisme.class).withAttribute("facteur_conversion", facteur_conversion_obj).treeToValue(comp_node,Prisme.class));
                        case "Rectangle" -> composition.ajouterObstacle(mapper.readerFor(Rectangle.class).withAttribute("facteur_conversion", facteur_conversion_obj).treeToValue(comp_node,Rectangle.class));
                        case "Segment" -> composition.ajouterObstacle(mapper.readerFor(Segment.class).withAttribute("facteur_conversion", facteur_conversion_obj).treeToValue(comp_node,Segment.class));
                        case "Composition" -> composition.ajouterObstacle(mapper.readerFor(Composition.class).withAttribute("facteur_conversion", facteur_conversion_obj).treeToValue(comp_node,Composition.class));
                    }
                }

            }

        }

        return composition;
    }
}
