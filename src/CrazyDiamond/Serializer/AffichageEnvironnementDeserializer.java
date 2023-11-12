package CrazyDiamond.Serializer;

import CrazyDiamond.Controller.CanvasAffichageEnvironnement;
import CrazyDiamond.Model.*;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import javafx.scene.paint.Color;

import java.io.IOException;

public class AffichageEnvironnementDeserializer extends StdDeserializer<CanvasAffichageEnvironnement> {

    public AffichageEnvironnementDeserializer() {
        this(CanvasAffichageEnvironnement.class) ;
    }
    public AffichageEnvironnementDeserializer(Class<?> vc) {
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
    public CanvasAffichageEnvironnement deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        final ObjectMapper mapper = (ObjectMapper) jsonParser.getCodec();
        final JsonNode ae_node = mapper.readTree(jsonParser);

        Environnement env = null ;

        if (ae_node.has("environnement"))
            env = mapper.treeToValue(ae_node.get("environnement"), Environnement.class) ;

        Double larg_g =  (Double) deserializationContext.getAttribute("largeur_graphique") ;
        Double haut_g =  (Double) deserializationContext.getAttribute("hauteur_graphique") ;

        if (!ae_node.has("parametres_affichage_environnement"))
            // Construction d'un Canvas Affichage Environnement avec les paramètres par défaut
            return new CanvasAffichageEnvironnement(env,larg_g, haut_g) ;

        final JsonNode pae_node = ae_node.get("parametres_affichage_environnement") ;

        double x_min = pae_node.get("x_min").asDouble() ;
        double y_min = pae_node.get("y_min").asDouble() ;
        double x_max = pae_node.get("x_max").asDouble() ;
        double y_max = pae_node.get("y_max").asDouble() ;

        boolean normales_visibles = pae_node.get("normales_visibles").asBoolean() ;
        String couleur_normales = pae_node.get("couleur_normales").asText() ;

        boolean prolongements_avant_visibles = pae_node.get("prolongements_avant_visibles").asBoolean() ;
        boolean prolongements_arriere_visibles = pae_node.get("prolongements_arriere_visibles").asBoolean() ;
        boolean commentaire_visible = pae_node.get("commentaire_visible").asBoolean() ;

        CanvasAffichageEnvironnement cae = new CanvasAffichageEnvironnement(env, larg_g, haut_g, x_min,y_min,x_max,y_max) ;
//        cae.definirDimensionsGraphiquesEtLimites(); ;

        cae.definirNormalesVisibles(normales_visibles) ;
        cae.definirProlongementsAvantVisibles(prolongements_avant_visibles) ;
        cae.definirProlongementsArriereVisibles(prolongements_arriere_visibles) ;
        cae.definirCommentaireVisible(commentaire_visible) ;
        cae.definirCouleurNormales(Color.valueOf(couleur_normales)) ;

        return cae;

    }
}
