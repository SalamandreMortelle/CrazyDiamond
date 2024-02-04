package CrazyDiamond.Controller;

import CrazyDiamond.Model.Obstacle;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

public class OutilAjoutObstacle implements Outil {

    CanvasAffichageEnvironnement cae ;
    protected Obstacle obstacle_en_cours_ajout = null ;

    public OutilAjoutObstacle(CanvasAffichageEnvironnement cae) { this.cae = cae ; }

    public void traiterClicSourisCanvas(MouseEvent me) {

        Point2D pclic = cae.gc_vers_g(me.getX(),me.getY()) ;

        if (obstacle_en_cours_ajout == null) { // On vient de commencer le tracé d'un nouveau cercle

            // Création d'un nouveau cercle
//            obstacle_en_cours_ajout = new Cercle(TypeSurface.CONVEXE, pclic.getX(), pclic.getY(), cae.resolution()) ;
            obstacle_en_cours_ajout = creerObstacle(pclic.getX(), pclic.getY());

            return ;
        }

        obstacle_en_cours_ajout.retaillerPourSourisEn(pclic);

        // Enregistrer l'obstacle courante dans l'environnement (si pas déjà fait suite à un mouvement de la souris :
        // cette méthode ne fait rien si la source est déjà ajoutée)
        cae.environnement().ajouterObstacle(obstacle_en_cours_ajout);

        obstacle_en_cours_ajout = null ;

    }

    // Méthode à surcharger dans les classes filles
    public Obstacle creerObstacle(double x, double y) { return null ;}

    public void traiterDeplacementSourisCanvas(MouseEvent me) {

        Point2D pos_souris = cae.gc_vers_g(me.getX(),me.getY()) ;

        if (obstacle_en_cours_ajout !=null) {
            obstacle_en_cours_ajout.retaillerPourSourisEn(pos_souris);

            // Ajouter l'obstacle dans l'environnement, si pas déjà fait (cette méthode ne fait rien si l'obstacle est déjà ajoutée)
            cae.environnement().ajouterObstacle(obstacle_en_cours_ajout);

            // TODO : à mettre dans l'eventListener sur l'ajout d'obstacles au niveau de Panneau principal
//            treeview_obstacles.getSelectionModel().select(chercheItemDansTreeItem(obstacle_en_cours_ajout,treeview_obstacles.getRoot()));
        }

    }

    public void traiterTouchePressee(KeyEvent keyEvent)
    {
        switch (keyEvent.getCode()) {
            case ESCAPE ->  { interrompre(); keyEvent.consume(); }
            }
    }

    public void interrompre() {
        if (obstacle_en_cours_ajout != null) {
            // On retire la source courante, ce qui va rafraichir les chemins et le décor
            cae.environnement().retirerObstacle(obstacle_en_cours_ajout);
            obstacle_en_cours_ajout = null;
        }
    }

}

