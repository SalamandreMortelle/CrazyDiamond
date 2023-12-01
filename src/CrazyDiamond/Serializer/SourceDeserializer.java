package CrazyDiamond.Serializer;

import CrazyDiamond.Model.Environnement;
import CrazyDiamond.Model.Imp_Nommable;
import CrazyDiamond.Model.Source;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;

import java.io.IOException;

public class SourceDeserializer extends StdDeserializer<Source> {

    public SourceDeserializer() {
        this(Source.class);
    }

    public SourceDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Source deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {

        final ObjectCodec mapper =  jsonParser.getCodec();
        final JsonNode source_node = mapper.readTree(jsonParser);

//        final TypeSurface t_s = TypeSurface.fromValue(cercle_node.get("type_surface").asText()) ;
//        final TypeSurface t_s = mapper.treeToValue(cercle_node,TypeSurface.class) ;

        double facteur_conversion = 1d ;

        Object facteur_conversion_obj = deserializationContext.getAttribute("facteur_conversion") ;

        if (facteur_conversion_obj!=null)
            facteur_conversion = (Double) facteur_conversion_obj ;


        Object env = deserializationContext.getAttribute("environnement") ;

        Imp_Nommable iei = mapper.treeToValue(source_node, Imp_Nommable.class) ;

        Point2D position = new Point2D(
                source_node.get("position_x").asDouble()*facteur_conversion,
                source_node.get("position_y").asDouble()*facteur_conversion ) ;

        double angle = source_node.get("angle").asDouble() ;
        int nb_max_obst_renc = source_node.get("nombre_max_obstacles_rencontres").asInt() ;
        int nb_rayons = source_node.get("nombre_rayons").asInt() ;
        double ouverture_pinceau = source_node.get("ouverture_pinceau").asDouble() ;
        double largeur_projecteur = source_node.get("largeur_projecteur").asDouble()*facteur_conversion ;

        String type_source = source_node.get("type").asText() ;
        final Source.TypeSource t_s = Source.TypeSource.fromValue(type_source) ;

        String couleur = source_node.get("couleur").asText() ;
        Color col = Color.valueOf(couleur) ;

        boolean lumiere_polarisee = source_node.get("lumiere_polarisee").asBoolean() ;
        double angle_champ_electrique = source_node.get("angle_champ_electrique").asDouble() ;

        return new Source((Environnement) env,iei,position,angle,t_s,nb_rayons,ouverture_pinceau,largeur_projecteur, col,lumiere_polarisee,angle_champ_electrique,nb_max_obst_renc);
    }
}
