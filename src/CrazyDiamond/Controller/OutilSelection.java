package CrazyDiamond.Controller;

import CrazyDiamond.Model.*;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

public class OutilSelection extends Outil {

    private boolean retaillage_selection_en_cours = false ;
    private boolean selection_rectangulaire_en_cours;
    private Point2D p_debut_glisser_selection_g;
    private Point2D p_debut_glisser_g;

    public OutilSelection(CanvasAffichageEnvironnement cae) {
        super(cae);
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

        if (cae.selection().obstacleUnique() != null) {
            if (!retaillage_selection_en_cours ) {
                if (cae.poignee_obstacle_pointee_en(pclic)) // On commence un re-taillage
                    retaillage_selection_en_cours = true;
            } else { // Re-taillage de sélection était en cours : on le termine
                cae.selection().obstacleUnique().retaillerSelectionPourSourisEn(pclic);
                retaillage_selection_en_cours = false ;
            }
        }  else if (cae.selection().sourceUnique() != null) {
            if (!retaillage_selection_en_cours) {
                if (cae.poignee_source_pointee_en(pclic)) { // On commence un re-taillage
                    retaillage_selection_en_cours = true;
                }
            } else { // Re-taillage de sélection était en cours : on le termine
                cae.selection().sourceUnique().retaillerPourSourisEn(pclic);
                retaillage_selection_en_cours = false ;
            }
        }

    }

    @Override
    public void traiterDeplacementSourisCanvas(MouseEvent me) {

        Point2D pos_souris = cae.gc_vers_g(me.getX(),me.getY()) ;

        if (cae.selection().obstacleUnique() !=null && retaillage_selection_en_cours) {
            cae.selection().obstacleUnique().retaillerSelectionPourSourisEn(pos_souris);
        } else if (cae.selection().sourceUnique() !=null && retaillage_selection_en_cours) {
            cae.selection().sourceUnique().retaillerPourSourisEn(pos_souris);
        } else if (cae.selection().socUnique() !=null && retaillage_selection_en_cours) {
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
            if (s_pointee!=null && !cae.selection().comprend(s_pointee)) {
                cae.selection().definirUnite(cae.environnement().unite());
                cae.selection().selectionnerUniquement(s_pointee);
                selection_rectangulaire_en_cours = false ;
            } else if (o_pointe!=null && !cae.selection().comprend(o_pointe)) {
                cae.selection().definirUnite(cae.environnement().unite());
                cae.selection().selectionnerUniquement(o_pointe);
                selection_rectangulaire_en_cours = false ;
            } else if (soc_pointe!=null && !cae.selection().comprend(soc_pointe)) {
                cae.selection().definirUnite(cae.environnement().unite());
                cae.selection().selectionnerUniquement(soc_pointe);
                selection_rectangulaire_en_cours = false ;
            } else {

                if (s_pointee == null && o_pointe == null && soc_pointe ==null) {
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
            cae.translaterSelection(v_glisser_g);

    }


    @Override
    public void traiterBoutonSourisRelacheFinGlisser(MouseEvent mouseEvent) {

        if (p_debut_glisser_g==null)
            return;

        Point2D p_fin_glisser_g   = cae.gc_vers_g(mouseEvent.getX(),mouseEvent.getY());

        Point2D v_glisser_g = p_fin_glisser_g.subtract(p_debut_glisser_g) ;

        if (cae.selection().nombreElements() >0 )
            cae.translaterSelection(v_glisser_g);

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

                cae.translaterSelection(new Point2D(-cae.resolution(), 0.0)) ;
                keyEvent.consume();
            }
            case RIGHT ->  {
                if (cae.selection().estVide())
                    break; // Ne pas consommer l'évènement pour que les champs texte, spinners, etc. puissent le recevoir

                cae.translaterSelection(new Point2D(cae.resolution(),0.0)) ;
                keyEvent.consume();
            }
            case UP -> {
                if (cae.selection().estVide())
                    break; // Ne pas consommer l'évènement pour que les champs texte, spinners, etc. puissent le recevoir

                cae.translaterSelection(new Point2D(0.0, cae.resolution())) ;
                keyEvent.consume();
            }
            case DOWN ->  {
                if (cae.selection().estVide())
                    break; // Ne pas consommer l'évènement pour que les champs texte, spinners, etc. puissent le recevoir

                cae.translaterSelection(new Point2D(0.0,-cae.resolution())) ;
                keyEvent.consume();
            }

        }

    }

}
