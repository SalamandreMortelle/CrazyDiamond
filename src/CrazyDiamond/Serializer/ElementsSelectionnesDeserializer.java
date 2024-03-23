package CrazyDiamond.Serializer;

import CrazyDiamond.Controller.ElementsSelectionnes;
import CrazyDiamond.Model.*;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.ContextAttributes;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.HashMap;

public class ElementsSelectionnesDeserializer extends StdDeserializer<ElementsSelectionnes> {

    public ElementsSelectionnesDeserializer() {
        this(ElementsSelectionnes.class) ;
    }
    public ElementsSelectionnesDeserializer(Class<?> vc) {
        super(vc);
    }

      @Override
    public ElementsSelectionnes deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
          final ObjectMapper mapper = (ObjectMapper) jsonParser.getCodec();
          final JsonNode node = mapper.readTree(jsonParser);

          if (!node.has("obstacles")&&!node.has("sources")&&!node.has("systemes_optiques_centres"))
              return null ;

          Environnement env_hote = (Environnement) deserializationContext.getAttribute("environnement_hote") ;

          if (env_hote==null)
              throw new IOException("Pas d'environnement hôte défini pour la désérialisation des ElementsSelectionnes") ;

          double facteur_conversion;

          Unite unite_importee = Unite.M ;

          if (node.has("unite"))
              unite_importee = Unite.fromValue(node.get("unite").asText()) ;

          ElementsSelectionnes es = new ElementsSelectionnes(unite_importee);

          facteur_conversion = (unite_importee != null ? unite_importee.valeur : 1d) / env_hote.unite().valeur  ;

          // Dictionnaire des identifiants qui auront dû être remplacés par de nouveaux lors de la désérialisation des
          // obstacles afin d'éviter d'avoir des doublons d'id dans l'environnement (cf. Imp_IdentifiableDeserializer
          // pour le renseignement de ce dictionnaire)
          HashMap<String,String> identifiants_remplaces = new HashMap<>(0) ;

          // NB : on ajoute l'attribut environnement une fois pour toute, même s'il ne sert que pour la construction des
          // sources et des SOCs
          ContextAttributes ctxa = ContextAttributes.getEmpty()
                  .withSharedAttribute("environnement",env_hote)
                  .withSharedAttribute("facteur_conversion",facteur_conversion)
                  .withSharedAttribute("identifiants_remplaces",identifiants_remplaces);
          mapper.setDefaultAttributes(ctxa) ;


          if (node.has("obstacles")) {

            JsonNode liste_obs_node = node.get("obstacles") ;

            int nb_obs = liste_obs_node.size();

            for (int i = 0; i < nb_obs; i++) {
                JsonNode obs_node = liste_obs_node.get(i);
                Obstacle o_a_ajouter = null;
                switch (obs_node.get("@type").asText()) {
                    case "Cercle" -> o_a_ajouter = mapper.treeToValue(obs_node, Cercle.class) ;
                    case "Conique" -> o_a_ajouter = mapper.treeToValue(obs_node, Conique.class) ;
                    case "DemiPlan" -> o_a_ajouter = mapper.treeToValue(obs_node, DemiPlan.class) ;
                    case "Prisme" -> o_a_ajouter = mapper.treeToValue(obs_node, Prisme.class);
                    case "Rectangle" -> o_a_ajouter = mapper.treeToValue(obs_node, Rectangle.class);
                    case "Segment" -> o_a_ajouter = mapper.treeToValue(obs_node, Segment.class);
                    case "Composition" -> o_a_ajouter = mapper.treeToValue(obs_node, Composition.class);
                }
                // Ajout de l'obstacle dans l'environnement, et dans les éléments sélectionnés
                env_hote.ajouterObstacleALaRacine(o_a_ajouter);
                es.ajouter(o_a_ajouter);
            }
        }

        if (node.has("sources")) {

            int nb_src = node.get("sources").size();

                for (int j = 0; j < nb_src; j++) {
                    Source s_a_jouter = mapper.treeToValue(node.get("sources").get(j), Source.class) ;
                    // Ajout de la source dans l'environnement, et dans les éléments sélectionnés
                    env_hote.ajouterSource(s_a_jouter);
                    es.ajouter(s_a_jouter);
                }

        }

        if (node.has("systemes_optiques_centres")) {

            int nb_soc = node.get("systemes_optiques_centres").size();

                for (int k = 0; k < nb_soc; k++) {
                    SystemeOptiqueCentre soc_a_ajouter = mapper.treeToValue(node.get("systemes_optiques_centres").get(k), SystemeOptiqueCentre.class) ;
                    // Ajout du soc dans l'environnement, et dans les éléments sélectionnés
                    env_hote.ajouterSystemeOptiqueCentre(soc_a_ajouter);
                    es.ajouter(soc_a_ajouter);
                }
        }

          return es;

    }
}
