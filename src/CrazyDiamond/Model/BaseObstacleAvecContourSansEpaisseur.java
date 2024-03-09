package CrazyDiamond.Model;

import javafx.beans.property.ObjectProperty;

import java.io.IOException;

public abstract class BaseObstacleAvecContourSansEpaisseur extends BaseObstacleAvecContour {

    private final Imp_ElementSansEpaisseur imp_elementSansEpaisseur;


    BaseObstacleAvecContourSansEpaisseur(String nom) {
        super(nom);
        this.imp_elementSansEpaisseur = new Imp_ElementSansEpaisseur(null) ;
    }

    BaseObstacleAvecContourSansEpaisseur(Imp_Identifiable ii, Imp_Nommable in, Imp_ElementAvecContour iac,Imp_ElementSansEpaisseur ise) {
        super(ii,in,iac);
        this.imp_elementSansEpaisseur = ise ;
    }

    public void definirNatureMilieu(NatureMilieu nature_mil) { imp_elementSansEpaisseur.definirNatureMilieu(nature_mil); }
    public NatureMilieu natureMilieu() { return imp_elementSansEpaisseur.natureMilieu(); }
    public ObjectProperty<NatureMilieu> natureMilieuProperty() { return imp_elementSansEpaisseur.natureMilieuProperty(); }


    public void appliquerSurElementSansEpaisseur(ConsumerAvecException<Object,IOException> consumer) throws IOException {
        consumer.accept(imp_elementSansEpaisseur);
    }

    protected void ajouterRappelSurChangementToutePropriete(RappelSurChangement rap) {
        super.ajouterRappelSurChangementToutePropriete(rap);
        imp_elementSansEpaisseur.ajouterRappelSurChangementToutePropriete(rap);
    }

    protected void ajouterRappelSurChangementTouteProprieteModifiantChemin(RappelSurChangement rap) {
        super.ajouterRappelSurChangementTouteProprieteModifiantChemin(rap);
        imp_elementSansEpaisseur.ajouterRappelSurChangementTouteProprieteModifiantChemin(rap);
    }
}
