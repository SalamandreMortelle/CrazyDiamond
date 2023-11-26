package CrazyDiamond.Serializer;

import CrazyDiamond.Model.*;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import javafx.scene.paint.Color;

import java.io.IOException;

public class EnvironnementDeserializer extends StdDeserializer<Environnement> {

    public EnvironnementDeserializer() {
        this(Environnement.class) ;
    }
    public EnvironnementDeserializer(Class<?> vc) {
        super(vc);
    }

      @Override
    public Environnement deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
          final ObjectMapper mapper = (ObjectMapper) jsonParser.getCodec();
          final JsonNode node = mapper.readTree(jsonParser);
          JsonNode env_node;

          Environnement env_hote = (Environnement) deserializationContext.getAttribute("environnement_hote") ;
          Double facteur_conversion = 1d ;

          Environnement e;

          if (env_hote == null) { // Construction d'un nouvel environnement
              env_node = node ;
              String str_unite = (env_node.has("unite")?env_node.get("unite").asText():"m");
              String str_couleur_fond = env_node.get("couleur_fond").asText();
              boolean reflexion_avec_refraction = env_node.get("reflexion_avec_refraction").asBoolean();

              String str_commentaire = null;
              if (env_node.has("commentaire"))
                  str_commentaire = env_node.get("commentaire").asText();

              e = new Environnement(Unite.fromValue(str_unite), Color.valueOf(str_couleur_fond), reflexion_avec_refraction);

              if (str_commentaire != null)
                  e.definirCommentaire(str_commentaire);
          } else { // Importation des éléments dans env_hote (attention aux unités à convertir)

              if (node.has("environnement"))
                  // Descendre au niveau du sous-objet environnement
                  env_node = node.get("environnement") ;
              else // Si jamais on importe un fichier dont l'élément racine est directement un environnement
                  // (et pas un AffichageEnvironnement)
                  env_node = node ;

              Unite unite_importee = Unite.M ;

              if (env_node.has("unite"))
                  unite_importee = Unite.fromValue(env_node.get("unite").asText()) ;

              facteur_conversion = unite_importee.valeur / env_hote.unite().valeur  ;

              e = env_hote ;
          }

        if (env_node.has("obstacles")) {

            JsonNode liste_obs_node = env_node.get("obstacles") ;

            int nb_obs = liste_obs_node.size();


            if (env_hote==null) {
                for (int i = 0; i < nb_obs; i++) {
                    JsonNode obs_node = liste_obs_node.get(i);
                    switch (obs_node.get("@type").asText()) {
                        case "Cercle" -> e.ajouterObstacle(mapper.treeToValue(obs_node, Cercle.class));
                        case "Conique" -> e.ajouterObstacle(mapper.treeToValue(obs_node, Conique.class));
                        case "DemiPlan" -> e.ajouterObstacle(mapper.treeToValue(obs_node, DemiPlan.class));
                        case "Prisme" -> e.ajouterObstacle(mapper.treeToValue(obs_node, Prisme.class));
                        case "Rectangle" -> e.ajouterObstacle(mapper.treeToValue(obs_node, Rectangle.class));
                        case "Segment" -> e.ajouterObstacle(mapper.treeToValue(obs_node, Segment.class));
                        case "Composition" -> e.ajouterObstacle(mapper.treeToValue(obs_node, Composition.class));
                    }
                }
            } else {
                for (int i = 0; i < nb_obs; i++) {
                    JsonNode obs_node = liste_obs_node.get(i);
                    switch (obs_node.get("@type").asText()) {
                        case "Cercle" -> e.ajouterObstacle(mapper.readerFor(Cercle.class)
                                .withAttribute("facteur_conversion", facteur_conversion).treeToValue(obs_node,Cercle.class));
                        case "Conique" -> e.ajouterObstacle(mapper.readerFor(Conique.class)
                                .withAttribute("facteur_conversion", facteur_conversion).treeToValue(obs_node,Conique.class));
                        case "DemiPlan" -> e.ajouterObstacle(mapper.readerFor(DemiPlan.class)
                                .withAttribute("facteur_conversion", facteur_conversion).treeToValue(obs_node,DemiPlan.class));
                        case "Prisme" -> e.ajouterObstacle(mapper.readerFor(Prisme.class)
                                .withAttribute("facteur_conversion", facteur_conversion).treeToValue(obs_node,Prisme.class));
                        case "Rectangle" -> e.ajouterObstacle(mapper.readerFor(Rectangle.class)
                                .withAttribute("facteur_conversion", facteur_conversion).treeToValue(obs_node,Rectangle.class));
                        case "Segment" -> e.ajouterObstacle(mapper.readerFor(Segment.class)
                                .withAttribute("facteur_conversion", facteur_conversion).treeToValue(obs_node,Segment.class));
                        case "Composition" -> e.ajouterObstacle(mapper.readerFor(Composition.class)
                                .withAttribute("facteur_conversion", facteur_conversion).treeToValue(obs_node,Composition.class));
                    }
                }

            }
        }

        if (env_node.has("sources")) {

            int nb_src = env_node.get("sources").size();

            if (env_hote==null) {
                for (int j = 0; j < nb_src; j++) {
                    Source src = mapper.readerFor(Source.class)
                            .withAttribute("environnement", e)
                            .treeToValue(env_node.get("sources").get(j), Source.class);
                    e.ajouterSource(src);
                }
            } else {
                for (int j = 0; j < nb_src; j++) {
                    Source src = mapper.readerFor(Source.class)
                            .withAttribute("environnement", e)
                            .withAttribute("facteur_conversion",facteur_conversion)
                            .treeToValue(env_node.get("sources").get(j), Source.class);
                    e.ajouterSource(src);
                }

            }


        }

        if (env_node.has("systemes_optiques_centres")) {

            int nb_soc = env_node.get("systemes_optiques_centres").size();

            if (env_hote==null) {
                for (int k = 0; k < nb_soc; k++) {
                    SystemeOptiqueCentre soc = mapper.readerFor(SystemeOptiqueCentre.class)
                            .withAttribute("environnement", e)
                            .treeToValue(env_node.get("systemes_optiques_centres").get(k), SystemeOptiqueCentre.class);
                    e.ajouterSystemeOptiqueCentre(soc);
                }
            } else {
                for (int k = 0; k < nb_soc; k++) {
                    SystemeOptiqueCentre soc = mapper.readerFor(SystemeOptiqueCentre.class)
                            .withAttribute("environnement", e)
                            .withAttribute("facteur_conversion",facteur_conversion)
                            .treeToValue(env_node.get("systemes_optiques_centres").get(k), SystemeOptiqueCentre.class);
                    e.ajouterSystemeOptiqueCentre(soc);
                }
            }
        }

        return e;

    }
}
