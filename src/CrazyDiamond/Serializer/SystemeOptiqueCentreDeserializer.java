package CrazyDiamond.Serializer;

import CrazyDiamond.Model.Environnement;
import CrazyDiamond.Model.Imp_Nommable;
import CrazyDiamond.Model.SystemeOptiqueCentre;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import javafx.geometry.Point2D;

import java.io.IOException;
import java.util.Map;

public class SystemeOptiqueCentreDeserializer extends StdDeserializer<SystemeOptiqueCentre> {

    public SystemeOptiqueCentreDeserializer() {
        this(SystemeOptiqueCentre.class);
    }

    public SystemeOptiqueCentreDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public SystemeOptiqueCentre deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {

        final ObjectCodec mapper = jsonParser.getCodec();
        final JsonNode soc_node = mapper.readTree(jsonParser);

        double facteur_conversion = 1d ;

        Object facteur_conversion_obj = deserializationContext.getAttribute("facteur_conversion") ;

        if (facteur_conversion_obj!=null)
            facteur_conversion = (Double) facteur_conversion_obj ;

        Environnement env = (Environnement) deserializationContext.getAttribute("environnement") ;

        Imp_Nommable ien = mapper.treeToValue(soc_node, Imp_Nommable.class) ;

        Point2D origine = new Point2D(
                soc_node.get("x_origine").asDouble()*facteur_conversion,
                soc_node.get("y_origine").asDouble()*facteur_conversion ) ;

        SystemeOptiqueCentre soc = new SystemeOptiqueCentre(env,ien,origine,soc_node.get("orientation").asDouble()) ;

        if (soc_node.has("z_objet")) soc.definirZObjet(soc_node.get("z_objet").asDouble()*facteur_conversion) ;
        if (soc_node.has("h_objet")) soc.definirHObjet(soc_node.get("h_objet").asDouble()*facteur_conversion) ;

        if (soc_node.has("montrer_objet")) soc.definirMontrerObjet(soc_node.get("montrer_objet").asBoolean()) ;
        if (soc_node.has("montrer_image")) soc.definirMontrerImage(soc_node.get("montrer_image").asBoolean()) ;
        if (soc_node.has("montrer_plans_focaux")) soc.definirMontrerPlansFocaux(soc_node.get("montrer_plans_focaux").asBoolean()) ;
        if (soc_node.has("montrer_plans_principaux")) soc.definirMontrerPlansPrincipaux(soc_node.get("montrer_plans_principaux").asBoolean()) ;
        if (soc_node.has("montrer_plans_nodaux")) soc.definirMontrerPlansNodaux(soc_node.get("montrer_plans_nodaux").asBoolean()) ;

        if (soc_node.has("montrer_dioptres")) soc.definirMontrerDioptres(soc_node.get("montrer_dioptres").asBoolean()) ;


        if (soc_node.has("obstacles")) {

            Map<String,String> id_remp = (Map <String,String>) deserializationContext.getAttribute("identifiants_remplaces") ;

            int nb_obs_soc = soc_node.get("obstacles").size();

            for (int i = 0; i < nb_obs_soc; i++) {
                String obs_id = soc_node.get("obstacles").get(i).asText();

                // Il est possible que l'identifiant originel ait été remplacé (pour éviter un doublon) lorsque l'obstacle a
                // été désérialisé ; dans ce cas, il faut utiliser l'identifiant de remplacement qui a été généré.
                if (id_remp.containsKey(obs_id))
                    obs_id = id_remp.get(obs_id) ;

                // NB : attention, ici on suppose que l'environnement contient déjà l'obstacle dont on a l'identifiant : c'est vrai
                // quand on désérialise tout un environnement depuis un fichier, mais pas quand on désérialise depuis le presse-papier car
                // les obstacles ne sont chargés que dans l'objet ElementsSelectionnes, et pas encore dans l'environnement hote.
                soc.ajouterObstacleCentre(env.obstacle(obs_id));
            }

        }

        if (soc_node.has("sous_systemes")) {

            int nb_sous_soc = soc_node.get("sous_systemes").size();

            for (int i = 0; i < nb_sous_soc; i++) {
                soc.ajouterSystemeOptiqueCentre(mapper.treeToValue(soc_node.get("sous_systemes").get(i), SystemeOptiqueCentre.class)) ;
            }
        }

        if (soc_node.has("modalites_traversee_dioptres")) {

            int nb_modalites = soc_node.get("modalites_traversee_dioptres").size();

            for (int i = 0; i < nb_modalites; i++) {
                JsonNode mod = soc_node.get("modalites_traversee_dioptres").get(i);

                if (mod.has("r_diaphragme"))
                    soc.dioptresRencontres().get(i).definirRayonDiaphragme(mod.get("r_diaphragme").asDouble()) ;
                if (mod.has("ignorer"))
                    soc.dioptresRencontres().get(i).definirIgnorer(mod.get("ignorer").asBoolean()) ;

            }

        }

        return soc;
    }
}
