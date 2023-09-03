package CrazyDiamond.Serializer;

import CrazyDiamond.Model.Imp_ElementSansEpaisseur;
import CrazyDiamond.Model.NatureMilieu;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class Imp_ElementSansEpaisseurDeserializer extends StdDeserializer<Imp_ElementSansEpaisseur> {

    public Imp_ElementSansEpaisseurDeserializer() {
        this(Imp_ElementSansEpaisseur.class);
    }
    public Imp_ElementSansEpaisseurDeserializer(Class<?> vc) {
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
    public Imp_ElementSansEpaisseur deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {

        final ObjectCodec mapper = jsonParser.getCodec();
        final JsonNode iem_node = mapper.readTree(jsonParser);

        String nature_milieu = iem_node.get("nature_milieu").asText() ;
        final NatureMilieu n_m = NatureMilieu.fromValue(nature_milieu) ;

        return new Imp_ElementSansEpaisseur(n_m) ;

    }

}
