package CrazyDiamond.Serializer;

import CrazyDiamond.Model.Environnement;
import CrazyDiamond.Model.Imp_Identifiable;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.Map;

public class Imp_IdentifiableDeserializer extends StdDeserializer<Imp_Identifiable> {

    public Imp_IdentifiableDeserializer() {
        this(Imp_Identifiable.class);
    }

    public Imp_IdentifiableDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Imp_Identifiable deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {

        final ObjectCodec mapper = jsonParser.getCodec();
        final JsonNode iei_node = mapper.readTree(jsonParser);

        Environnement env = (Environnement) deserializationContext.getAttribute("environnement") ;

        if (!iei_node.has("id"))
            return new Imp_Identifiable() ;

        String id = iei_node.get("id").asText() ;

        if (env.obstacle(id)==null) // Aucun des obstacles déjà présents dans l'environnement n'a le même id : on peut l'utiliser
            return new Imp_Identifiable(id) ;

        // L'identifiant à déserialiser existe déjà dans l'environnement : il faut en créer un nouveau et enregistrer
        // la correspondance avec l'ancien identifiant pour que les autres éléments à déserialiser qui y font référence
        // puissent le retrouver.
        Imp_Identifiable nouveau_ii = new Imp_Identifiable() ;

        Map<String,String> id_remp = (Map <String,String>) deserializationContext.getAttribute("identifiants_remplaces") ;

        id_remp.put(id, nouveau_ii.id()) ;

        return nouveau_ii ;
    }

}
