package CrazyDiamond.Serializer;

import CrazyDiamond.Controller.ElementsSelectionnes;
import CrazyDiamond.Model.Obstacle;
import CrazyDiamond.Model.Source;
import CrazyDiamond.Model.SystemeOptiqueCentre;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.Iterator;

public class ElementsSelectionnesSerializer extends StdSerializer<ElementsSelectionnes> {

    public ElementsSelectionnesSerializer() {
        super(ElementsSelectionnes.class);
    }

    @Override
    public void serialize(ElementsSelectionnes elements_selectionnes, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringField("unite",elements_selectionnes.unite().toString());

        // Obstacles
        if (elements_selectionnes.nombreObstacles()>0) {
            jsonGenerator.writeArrayFieldStart("obstacles");

            Iterator<Obstacle> ito = elements_selectionnes.iterateur_obstacles();
            while (ito.hasNext()) {
                jsonGenerator.writeObject(ito.next());
            }

            jsonGenerator.writeEndArray();
        }

        // Sources
        if (elements_selectionnes.nombreSources()>0) {
            jsonGenerator.writeArrayFieldStart("sources");

            Iterator<Source> its = elements_selectionnes.iterateur_sources();
            while (its.hasNext()) {
                jsonGenerator.writeObject(its.next());
            }

            jsonGenerator.writeEndArray();
        }

        // Systemes Optiques Centres
        if (elements_selectionnes.nombreSystemesOptiquesCentres()>0) {
            jsonGenerator.writeArrayFieldStart("systemes_optiques_centres");

            Iterator<SystemeOptiqueCentre> itsoc = elements_selectionnes.iterateur_systemesOptiquesCentres();
            while (itsoc.hasNext()) {
                jsonGenerator.writeObject(itsoc.next());
            }

            jsonGenerator.writeEndArray();
        }

        jsonGenerator.writeEndObject();

    }
}
