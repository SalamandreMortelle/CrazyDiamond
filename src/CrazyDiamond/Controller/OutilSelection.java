package CrazyDiamond.Controller;

import CrazyDiamond.Model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.cfg.ContextAttributes;
import com.fasterxml.jackson.databind.json.JsonMapper;
import javafx.geometry.Point2D;
import javafx.scene.control.Alert;
import javafx.scene.input.*;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OutilSelection extends Outil {

    private static final Logger LOGGER = Logger.getLogger( "CrazyDiamond" );
    static protected final DataFormat format_crazy_diamond_elements = new DataFormat("application/crazy-diamond.elements");

    JsonMapper jsonMapper ;
    
    private boolean retaillage_selection_en_cours = false ;
    private boolean selection_rectangulaire_en_cours;
    private Point2D p_debut_glisser_selection_g;

    // Debut du dernier déplacement du glisser
    private Point2D p_debut_glisser_g;
    private Point2D position_depart_poignee;

    // Point de départ de tout le glisser
    private Point2D p_depart_glisser_g;

    public OutilSelection(CanvasAffichageEnvironnement cae, JsonMapper jsonMapper) {
        super(cae);
        this.jsonMapper = jsonMapper ;
    }

    @Override
    public void prendre() {
        super.prendre();
        cae.selection().vider();
    }

    @Override
    public void deposer() {
        super.deposer();
        if (cae.selection().nombreElements()>0)
            cae.selection().vider();
    }

    @Override
    public void traiterClicSourisCanvas(MouseEvent me) {
        Point2D pclic = cae.gc_vers_g(me.getX(),me.getY()) ;

        // On n'est pas au départ d'un glisser
        p_depart_glisser_g = null ;

        if (cae.selection().obstacleUnique() != null) {
            if (!retaillage_selection_en_cours ) {
                if (cae.poignee_obstacle_pointee_en(pclic)) { // On commence un re-taillage
                    retaillage_selection_en_cours = true;
                    position_depart_poignee = cae.poigneeSelectionObstacleUnique() ;
                    // TODO : mémoriser taille de départ
                }
            } else { // Re-taillage de sélection était en cours : on le termine
                cae.selection().obstacleUnique().retaillerSelectionParCommandePourSourisEn(pclic,position_depart_poignee);
                retaillage_selection_en_cours = false ;
                position_depart_poignee = null ;
            }
        }
        // Pas de retaillage possible pour les sources
        /* else if (cae.selection().sourceUnique() != null) {
            if (!retaillage_selection_en_cours) {
                if (cae.poignee_source_pointee_en(pclic)) { // On commence un re-taillage
                    retaillage_selection_en_cours = true;
                }
            } else { // Re-taillage de sélection était en cours : on le termine
                cae.selection().sourceUnique().retaillerSourceParCommandePourSourisEn(pclic);
                retaillage_selection_en_cours = false ;
            }
        } */

    }

    @Override
    public void traiterDeplacementSourisCanvas(MouseEvent me) {

        Point2D pos_souris = cae.gc_vers_g(me.getX(),me.getY()) ;

        if (cae.selection().obstacleUnique() !=null && retaillage_selection_en_cours) {
            cae.selection().obstacleUnique().retaillerSelectionPourSourisEn(pos_souris);
        }
        // Pas de retaillage possible pour les sources
        /*else if (cae.selection().sourceUnique() !=null && retaillage_selection_en_cours) {
            cae.selection().sourceUnique().retaillerPourSourisEn(pos_souris);
        }*/ else if (cae.selection().socUnique() !=null && retaillage_selection_en_cours) {
            cae.selection().socUnique().retaillerPourSourisEn(pos_souris);
        }

    }

    @Override
    public void traiterBoutonSourisPresse(MouseEvent mouseEvent) {

        p_debut_glisser_g = cae.gc_vers_g(mouseEvent.getX(), mouseEvent.getY());
        p_debut_glisser_selection_g = p_debut_glisser_g ;

//        Obstacle o_avant = cae.selection().obstacleUnique() ;
//        Source s_avant = cae.selection().sourceUnique();
//        SystemeOptiqueCentre soc_avant = cae.selection().socUnique() ;

        Obstacle o_pointe  = cae.obstacle_pointe_en(p_debut_glisser_g) ;
        Source   s_pointee = cae.source_pointee_en(p_debut_glisser_g) ;
        SystemeOptiqueCentre   soc_pointe = cae.soc_pointe_en(p_debut_glisser_g) ;

        if (!retaillage_selection_en_cours) {

            p_depart_glisser_g = p_debut_glisser_g ;

            if (s_pointee!=null && !cae.selection().comprend(s_pointee)) { // Début du déplacement d'une source
                cae.selection().definirUnite(cae.environnement().unite());
                cae.selection().selectionnerUniquement(s_pointee);
                selection_rectangulaire_en_cours = false ;
            } else if (o_pointe!=null && !cae.selection().comprend(o_pointe)) { // Début du déplacement d'un obstacle
                cae.selection().definirUnite(cae.environnement().unite());
                cae.selection().selectionnerUniquement(o_pointe);
                selection_rectangulaire_en_cours = false ;
            } else if (soc_pointe!=null && !cae.selection().comprend(soc_pointe)) { // Début du déplacement d'un SOC
                cae.selection().definirUnite(cae.environnement().unite());
                cae.selection().selectionnerUniquement(soc_pointe);
                selection_rectangulaire_en_cours = false ;
            } else {

                // On est soit au début d'une zone de sélection rectangulaire, soit au début du déplacement d'un (ou plusieurs
                // éléments qui étaient déjà sélectionnés)

                if (s_pointee == null && o_pointe == null && soc_pointe ==null) { // Début d'une zone de sélection rectangulaire
                    p_depart_glisser_g = null ;
                    cae.selection().vider();
                    selection_rectangulaire_en_cours = true ;
                }

            }
        }

        // TODO : à gérer plutôt avec un listener sur la sélection courante dans PanneauPrincipal
//            if (cae.selection().obstacleUnique()!=o_avant) {
//                treeview_obstacles.getSelectionModel().select(chercheItemDansTreeItem(cae.selection().obstacleUnique(), treeview_obstacles.getRoot()));
//            }
//            if (cae.selection().sourceUnique()!=s_avant) {
//                listview_sources.getSelectionModel().select(cae.selection().sourceUnique());
//            }
//            if (cae.selection().socUnique()!=soc_avant) {
//                treeview_socs.getSelectionModel().select(chercheItemSOCDansArbreSOC(cae.selection().socUnique(),treeview_socs.getRoot()));
//            }

    }

    @Override
    public void traiterGlisserSourisCanvas(MouseEvent mouseEvent) {

        if (p_debut_glisser_g==null)
            return;

        Point2D p_fin_glisser_g   = cae.gc_vers_g(mouseEvent.getX(),mouseEvent.getY());

        Point2D v_glisser_g = p_fin_glisser_g.subtract(p_debut_glisser_g) ;

        // La position actuelle de la souris devient le nouveau point de depart pour la suite du glisser
        this.p_debut_glisser_g = p_fin_glisser_g ;

        if (selection_rectangulaire_en_cours) {

            BoiteLimiteGeometrique zone_rect = new BoiteLimiteGeometrique(
                    Math.min(p_debut_glisser_selection_g.getX(),p_fin_glisser_g.getX()),
                    Math.min(p_debut_glisser_selection_g.getY(),p_fin_glisser_g.getY()),
                    Math.abs(p_debut_glisser_selection_g.getX()-p_fin_glisser_g.getX()),
                    Math.abs(p_debut_glisser_selection_g.getY()-p_fin_glisser_g.getY())
            ) ;

            cae.selectionnerParZoneRectangulaire(zone_rect);
        }
        else
            cae.translaterSelection(v_glisser_g); // Translation intermédiaire partielle, sans création de Commande

    }


    @Override
    public void traiterBoutonSourisRelacheFinGlisser(MouseEvent mouseEvent) {

        if (p_debut_glisser_g==null)
            return;


        // NB : Inutile d'essayer de capter un ultime déplacement, les coordonnées de la souris sont toujours les mêmes
        // que celles du dernier évènement GlisserSourisCanvas (les deux évènements sont déclenchés au même point)
//        Point2D p_fin_glisser_g   = cae.gc_vers_g(mouseEvent.getX(),mouseEvent.getY());
//        Point2D v_glisser_g = p_fin_glisser_g.subtract(p_debut_glisser_g) ;
//
//        if (cae.selection().nombreElements() >0 ) {
//            System.out.println("Translatiooooooooooooon de "+v_glisser_g);
//            cae.translaterSelection(v_glisser_g);
//        }

        if (!selection_rectangulaire_en_cours) { // Fin ("arrivée") du glisser
            Point2D p_arrivee_glisser_g = cae.gc_vers_g(mouseEvent.getX(),mouseEvent.getY());
            Point2D v_glisser_total_g   = p_arrivee_glisser_g.subtract(p_depart_glisser_g) ;
            // Enregistrement d'une seule commande correspondant au déplacement résultant global
            new CommandeTranslaterElements(cae.environnement(),v_glisser_total_g,cae.selection().sources, cae.selection().obstacles, cae.selection().socs).enregistrer();
        }

        cae.selectionnerParZoneRectangulaire(null);
        selection_rectangulaire_en_cours = false ;

        this.p_debut_glisser_g = null ;
        this.p_debut_glisser_selection_g = null ;

    }



    @Override
    public void interrompre() {
        reinitialiser();
    }

    private void reinitialiser() {
        cae.selectionnerParZoneRectangulaire(null);
        cae.selection().vider();
        selection_rectangulaire_en_cours = false ;
        this.p_debut_glisser_g = null ;
        this.p_debut_glisser_selection_g = null ;
        retaillage_selection_en_cours = false;
    }

    @Override
    public void traiterTouchePressee(KeyEvent keyEvent) {

        switch (keyEvent.getCode()) {
            case ESCAPE ->  { interrompre(); keyEvent.consume(); }
            case LEFT -> {
                if (cae.selection().estVide())
                    break; // Ne pas consommer l'évènement pour que les champs texte, spinners, etc. puissent le recevoir

                translaterSelectionParCommande(new Point2D(-cae.resolution(), 0.0)) ;

                keyEvent.consume();
            }
            case RIGHT ->  {
                if (cae.selection().estVide())
                    break; // Ne pas consommer l'évènement pour que les champs texte, spinners, etc. puissent le recevoir

                translaterSelectionParCommande(new Point2D(cae.resolution(),0.0)); ;

                keyEvent.consume();
            }
            case UP -> {
                if (cae.selection().estVide())
                    break; // Ne pas consommer l'évènement pour que les champs texte, spinners, etc. puissent le recevoir

                translaterSelectionParCommande(new Point2D(0.0, cae.resolution())); ;

                keyEvent.consume();
            }
            case DOWN ->  {
                if (cae.selection().estVide())
                    break; // Ne pas consommer l'évènement pour que les champs texte, spinners, etc. puissent le recevoir

                translaterSelectionParCommande(new Point2D(0.0,-cae.resolution())); ;

                keyEvent.consume();
            }
            case A -> {
                if (!keyEvent.isControlDown())
                    break ; // Ne pas consommer l'évènement pour que les champs texte, spinners, etc. puissent le recevoir

                selectionnerTout() ;

                keyEvent.consume();
            }
            case C -> {
                if (!keyEvent.isControlDown() || cae.selection().estVide())
                    break ; // Ne pas consommer l'évènement pour que les champs texte, spinners, etc. puissent le recevoir

                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();

                String json = serialiserElementsSelectionnes();

                if (json!=null) {
                    content.put(format_crazy_diamond_elements, json);
                    content.putString(json);
                    clipboard.setContent(content);
                }

                keyEvent.consume();
            }
            case X -> {
                if (!keyEvent.isControlDown())
                    break ; // Ne pas consommer l'évènement pour que les champs texte, spinners, etc. puissent le recevoir

                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();

                String json = serialiserElementsSelectionnes();

                if (json!=null) {
                    content.put(format_crazy_diamond_elements, json);
                    content.putString(json);
                    clipboard.setContent(content);

                    supprimerElementsSelectionnes() ;
                }


                keyEvent.consume();
            }
            case V -> {
                if (!keyEvent.isControlDown())
                    break ; // Ne pas consommer l'évènement pour que les champs texte, spinners, etc. puissent le recevoir

                Clipboard clipboard = Clipboard.getSystemClipboard();

                ElementsSelectionnes es = null ;

                try {
                    // Passage d'un environnement hôte dans lequel l'ObjectReader va ajouter les éléments importables du fichier
                    ContextAttributes ca = ContextAttributes.getEmpty().withSharedAttribute("environnement_hote", cae.environnement()) ;

                    ObjectReader or = jsonMapper.readerFor(ElementsSelectionnes.class).with(ca) ;
                    if (clipboard.hasContent(format_crazy_diamond_elements))
                        es = or.readValue(clipboard.getContent(format_crazy_diamond_elements).toString(),ElementsSelectionnes.class) ;
                    else if (clipboard.hasString()) // Si le clipboard contient une string, on tente de la parser comme du JSON CrazyDiamond
                        es = or.readValue(clipboard.getString(),ElementsSelectionnes.class) ;

                    if (es!=null)
                        cae.definirSelection(es) ;

                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE,"Exception lors de la lecture du presse-papier") ;

                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setHeaderText("Impossible d'instancier de nouveaux éléments à partir des éléments du presse-papier");
                    alert.setContentText(e.getMessage()+System.lineSeparator()+"in :"+System.lineSeparator()+e.getStackTrace()[0].toString());
                    alert.showAndWait();
                }

                keyEvent.consume();
            }


        }

    }

    private void translaterSelectionParCommande(Point2D vecteur) {
        new CommandeTranslaterElements(cae.environnement(),vecteur,cae.selection().sources, cae.selection().obstacles, cae.selection().socs).executer();
    }

    private void selectionnerTout() {

        cae.selection().vider();
        cae.selection().definirUnite(cae.environnement().unite()) ;

        Iterator<Obstacle> ito = cae.environnement().iterateur_obstacles() ;
        while (ito.hasNext())
            cae.selection().ajouter(ito.next());

        Iterator<Source> its = cae.environnement().iterateur_sources() ;
        while (its.hasNext())
            cae.selection().ajouter(its.next());

        Iterator<SystemeOptiqueCentre> itsoc = cae.environnement().iterateur_systemesOptiquesCentres() ;
        while (itsoc.hasNext())
            cae.selection().ajouter(itsoc.next());

    }

    private String serialiserElementsSelectionnes() {

        String json = null ;

        try {
            json = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(cae.selection());
        } catch (JsonProcessingException e) {
            LOGGER.log(Level.SEVERE,"Exception lors de la sérialisation en JSON des éléments sélectionnés ",e.getMessage());

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Impossible de sérialiser les éléments sélectionnés");
            alert.setContentText(e.getMessage()+System.lineSeparator()+e.getCause());
            alert.showAndWait();
        }

        return json ;

    }

    private void supprimerElementsSelectionnes() {
        ElementsSelectionnes es = cae.selection() ;

        // Le retrait des obstacles, sources et socs de l'environnement altère (cf. callbacks ListChangeListener dans
        // l'Environnement) les éléments sélectionnés que l'on est en train de parcourir, ce qui lèverait une exception.
        // Pour éviter cela, commençons par faire une copie (non profonde) de la sélection.
        ElementsSelectionnes es_copie = new ElementsSelectionnes(es) ;

        es_copie.stream_obstacles().forEach(cae.environnement()::retirerObstacle);
        es_copie.stream_sources().forEach(cae.environnement()::retirerSource);
        es_copie.stream_socs().forEach(cae.environnement()::retirerSystemeOptiqueCentre);

    }

}
