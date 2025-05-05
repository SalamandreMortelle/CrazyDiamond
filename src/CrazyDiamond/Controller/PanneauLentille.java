package CrazyDiamond.Controller;

import CrazyDiamond.Model.*;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.logging.Level;
import java.util.logging.Logger;

import static CrazyDiamond.Model.ConvexiteFaceLentille.CONVEXE;

public class PanneauLentille {

    // Modèle
    Lentille lentille ;
    private final boolean dans_composition;

    CanvasAffichageEnvironnement canvas;

    // Récupération du logger
    private static final Logger LOGGER = Logger.getLogger( "CrazyDiamond" );

    @FXML
    private VBox baseElementIdentifie;
    @FXML
    private PanneauElementIdentifie baseElementIdentifieController ;

    // Contrôleurs des sous-panneaux génériques pour les attributs de contour, et de matière
    @FXML
    private VBox baseContour;
    @FXML
    private PanneauElementAvecContour baseContourController;

    @FXML
    private VBox baseMatiere;
    @FXML
    private PanneauElementAvecMatiere baseMatiereController;

    @FXML
    public VBox vbox_panneau_racine;
    @FXML
    public VBox vbox_positionnement_absolu;
    @FXML
    private HBox hbox_positionnement_relatif_dans_soc;
    @FXML
    private PanneauPositionnementElementDansSOC hbox_positionnement_relatif_dans_socController;

    @FXML
    private Spinner<Double> spinner_xcentre;
    @FXML
    private Spinner<Double> spinner_ycentre;

    @FXML
    private Spinner<Double> spinner_epaisseur;
    @FXML
    private Spinner<Double> spinner_diametre;

    @FXML
    private ToggleGroup choix_forme_face_1;
    @FXML
    public RadioButton choix_spherique_1;
    @FXML
    public RadioButton choix_conique_1;
    @FXML
    private Spinner<Double> spinner_rayon_1;
    @FXML
    private Spinner<Double> spinner_parametre_1;
    @FXML
    private Spinner<Double> spinner_excentricite_1;
    @FXML
    private ToggleGroup choix_convexite_face_1;
    @FXML
    private RadioButton choix_convexe_1 ;
    @FXML
    private RadioButton choix_plane_1;
    @FXML
    private RadioButton choix_concave_1;


    @FXML
    private CheckBox checkbox_faces_symetriques;


    @FXML
    private ToggleGroup choix_forme_face_2;
    @FXML
    public RadioButton choix_spherique_2;
    @FXML
    public RadioButton choix_conique_2;
    @FXML
    private Spinner<Double> spinner_rayon_2;
    @FXML
    private Spinner<Double> spinner_parametre_2;
    @FXML
    private Spinner<Double> spinner_excentricite_2;
    @FXML
    private ToggleGroup choix_convexite_face_2;
    @FXML
    private RadioButton choix_convexe_2 ;
    @FXML
    private RadioButton choix_plane_2 ;
    @FXML
    private RadioButton choix_concave_2 ;

    @FXML
    public Spinner<Double> spinner_orientation;
    @FXML
    public Slider slider_orientation;

    public VBox parent_parametres_face_1;
    public HBox parametres_forme_spherique_1;
    public VBox parametres_forme_conique_1;

    public VBox parent_parametres_face_2;
    public HBox parametres_forme_spherique_2;
    public VBox parametres_forme_conique_2;


    public PanneauLentille(Lentille l, boolean dans_composition, CanvasAffichageEnvironnement cnv) {

        if (l==null)
            throw new IllegalArgumentException("L'objet Lentille attaché au PanneauLentille ne peut pas être 'null'") ;

        this.lentille = l ;
        this.dans_composition=dans_composition;
        this.canvas = cnv ;

    }

    public void initialize() {
        LOGGER.log(Level.INFO,"Initialisation du PanneauLentille et de ses liaisons") ;

        baseElementIdentifieController.initialize(lentille);

        hbox_positionnement_relatif_dans_socController.initialize(canvas,lentille);

        UtilitairesVue.gererAppartenanceSOC(lentille,vbox_panneau_racine,vbox_positionnement_absolu, hbox_positionnement_relatif_dans_soc);

        UtilitairesVue.gererAppartenanceComposition(dans_composition,lentille,baseContour,baseContourController,baseMatiere,baseMatiereController) ;

        // Prise en compte automatique de la position et de l'orientation
        lentille.positionEtOrientationObjectProperty().addListener(new ChangeListenerAvecGarde<>(this::prendreEnComptePositionEtOrientation));

        // Position Xcentre
        spinner_xcentre.getStyleClass().add(Spinner.STYLE_CLASS_ARROWS_ON_RIGHT_HORIZONTAL) ;
        spinner_xcentre.editableProperty().bind(lentille.appartenanceSystemeOptiqueProperty().not()) ;
        spinner_xcentre.disableProperty().bind(lentille.appartenanceSystemeOptiqueProperty()) ;
        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_xcentre, lentille.xCentre(), this::definirXCentreLentille);

        // Position Ycentre
        spinner_ycentre.editableProperty().bind(lentille.appartenanceSystemeOptiqueProperty().not()) ;
        spinner_ycentre.disableProperty().bind(lentille.appartenanceSystemeOptiqueProperty()) ;
        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_ycentre, lentille.yCentre(), this::definirYCentreLentille);

        // Orientation
        spinner_orientation.getValueFactory().setWrapAround(true);
        spinner_orientation.editableProperty().bind(lentille.appartenanceSystemeOptiqueProperty().not()) ;
        spinner_orientation.disableProperty().bind(lentille.appartenanceSystemeOptiqueProperty()) ;
        OutilsControleur.integrerSpinnerDoubleValidant(spinner_orientation,lentille.orientation(),this::definirOrientation);

        slider_orientation.valueProperty().set(lentille.orientation());
        slider_orientation.valueProperty().addListener(new ChangeListenerAvecGarde<>(this::definirOrientation));
        slider_orientation.disableProperty().bind(lentille.appartenanceSystemeOptiqueProperty()) ;

        // Epaisseur
        lentille.epaisseurProperty().addListener(new ChangeListenerAvecGarde<>(this::prendreEnCompteEpaisseur));
        spinner_epaisseur.getStyleClass().add(Spinner.STYLE_CLASS_ARROWS_ON_RIGHT_HORIZONTAL) ;
        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_epaisseur, lentille.epaisseur(),this::definirEpaisseur);

        // Diamètre
        lentille.diametreProperty().addListener(new ChangeListenerAvecGarde<>(this::prendreEnCompteDiametre));
        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_diametre, lentille.diametre(),this::definirDiametre);

        // Forme Face 1
        prendreEnCompteFormeFace1(lentille.formeFace1());
        lentille.formeFace1Property().addListener(new ChangeListenerAvecGarde<>(this::prendreEnCompteFormeFace1));
        choix_forme_face_1.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            LOGGER.log(Level.FINE,"Choix forme face 1 passe de {0} à {1}", new Object[] {oldValue,newValue}) ;

            if (newValue==choix_spherique_1 && lentille.formeFace1()!= FormeFaceLentille.SPHERIQUE)
                new CommandeDefinirUnParametre<>(lentille, FormeFaceLentille.SPHERIQUE, lentille::formeFace1, lentille::definirFormeFace1).executer();
            if (newValue==choix_conique_1 && lentille.formeFace1()!= FormeFaceLentille.CONIQUE)
                new CommandeDefinirUnParametre<>(lentille, FormeFaceLentille.CONIQUE, lentille::formeFace1, lentille::definirFormeFace1).executer();
        });

//        parametres_forme_spherique_1.disableProperty().bind(lentille.formeFace1Property().isNotEqualTo(FormeFaceLentille.SPHERIQUE)) ;
//        parametres_forme_conique_1.disableProperty().bind(lentille.formeFace1Property().isNotEqualTo(FormeFaceLentille.CONIQUE)) ;

        if (lentille.formeFace1()==FormeFaceLentille.SPHERIQUE)
            parent_parametres_face_1.getChildren().remove(parametres_forme_conique_1);
        else
            parent_parametres_face_1.getChildren().remove(parametres_forme_spherique_1);

        lentille.formeFace1Property().addListener((observable, oldValue, newValue) -> {
            if (newValue==FormeFaceLentille.SPHERIQUE) {
                parent_parametres_face_1.getChildren().remove(parametres_forme_conique_1);
                parent_parametres_face_1.getChildren().add(3,parametres_forme_spherique_1);
            } else if (newValue==FormeFaceLentille.CONIQUE) {
                parent_parametres_face_1.getChildren().remove(parametres_forme_spherique_1);
                parent_parametres_face_1.getChildren().add(3,parametres_forme_conique_1);
            }
        });

        // Rayon 1
        prendreEnCompteRayon1(lentille.rayon1());
        lentille.rayon1Property().addListener(new ChangeListenerAvecGarde<>(this::prendreEnCompteRayon1));
        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas, spinner_rayon_1, lentille.rayon1(),this::definirRayon1);

//        ((SpinnerValueFactory.DoubleSpinnerValueFactory) spinner_rayon_1.getValueFactory()).minProperty().bind(lentille.epaisseurProperty().multiply(0.5d));

//        DoubleBinding calcul_rayon1_min = new DoubleBinding() {
//                { super.bind(lentille.epaisseurProperty(),lentille.convexiteFace1Property()); }
//            @Override
//            protected double computeValue() {
//
//                return 0d ;
////                return (lentille.convexiteFace1()==CONVEXE?0.5d*lentille.epaisseur()+1E-7d:1E-7d) ;
//
//            }
//        } ;
//        ((SpinnerValueFactory.DoubleSpinnerValueFactory) spinner_rayon_1.getValueFactory()).minProperty().bind(calcul_rayon1_min);

        // Parametre 1
        prendreEnCompteParametre1(lentille.parametre1());
        lentille.parametre1Property().addListener(new ChangeListenerAvecGarde<>(this::prendreEnCompteParametre1));
        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas, spinner_parametre_1, lentille.parametre1(),this::definirParametre1);

//        DoubleBinding calcul_parametre1_min = new DoubleBinding() {
//                { super.bind(lentille.epaisseurProperty(),lentille.excentricite1Property(),lentille.convexiteFace1Property()); ; }
//            @Override
//            protected double computeValue() {
//                return (lentille.convexiteFace1()==ConvexiteFaceLentille.CONVEXE? 0.5d*lentille.epaisseur()*(1+ lentille.excentricite1()):0d) ;
//            }
//        } ;
//        ((SpinnerValueFactory.DoubleSpinnerValueFactory) spinner_parametre_1.getValueFactory()).minProperty().bind(calcul_parametre1_min);


        // Excentricite 1
        prendreEnCompteExcentricite1(lentille.excentricite1());
        lentille.excentricite1Property().addListener(new ChangeListenerAvecGarde<>(this::prendreEnCompteExcentricite1));
        OutilsControleur.integrerSpinnerDoubleValidant(spinner_excentricite_1, lentille.excentricite1(),this::definirExcentricite1);

        // Convexite Face 1
        prendreEnCompteConvexiteFace1(lentille.convexiteFace1());
        lentille.convexiteFace1Property().addListener(new ChangeListenerAvecGarde<>(this::prendreEnCompteConvexiteFace1));
        choix_convexite_face_1.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            LOGGER.log(Level.FINE,"Choix convexite face 1 passe de {0} à {1}", new Object[] {oldValue,newValue}) ;

            if (newValue==choix_convexe_1 && lentille.convexiteFace1()!= CONVEXE)
                new CommandeDefinirUnParametre<>(lentille, CONVEXE, lentille::convexiteFace1, lentille::definirConvexiteFace1).executer();
            if (newValue==choix_plane_1 && lentille.convexiteFace1()!= ConvexiteFaceLentille.PLANE)
                new CommandeDefinirUnParametre<>(lentille, ConvexiteFaceLentille.PLANE, lentille::convexiteFace1, lentille::definirConvexiteFace1).executer();
            if (newValue==choix_concave_1 && lentille.convexiteFace1()!= ConvexiteFaceLentille.CONCAVE)
                new CommandeDefinirUnParametre<>(lentille, ConvexiteFaceLentille.CONCAVE, lentille::convexiteFace1, lentille::definirConvexiteFace1).executer();
        });

        choix_spherique_1.disableProperty().bind(choix_plane_1.selectedProperty());
        choix_conique_1.disableProperty().bind(choix_plane_1.selectedProperty());
        parametres_forme_spherique_1.disableProperty().bind(choix_plane_1.selectedProperty());
        parametres_forme_conique_1.disableProperty().bind(choix_plane_1.selectedProperty());

        // Forme Face 2
        prendreEnCompteFormeFace2(lentille.formeFace2());
        lentille.formeFace2Property().addListener(new ChangeListenerAvecGarde<>(this::prendreEnCompteFormeFace2));
        choix_forme_face_2.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            LOGGER.log(Level.FINE,"Choix forme face 2 passe de {0} à {1}", new Object[] {oldValue,newValue}) ;

            if (newValue==choix_spherique_2 && lentille.formeFace2()!= FormeFaceLentille.SPHERIQUE)
                new CommandeDefinirUnParametre<>(lentille, FormeFaceLentille.SPHERIQUE, lentille::formeFace2, lentille::definirFormeFace2).executer();
            if (newValue==choix_conique_2 && lentille.formeFace2()!= FormeFaceLentille.CONIQUE)
                new CommandeDefinirUnParametre<>(lentille, FormeFaceLentille.CONIQUE, lentille::formeFace2, lentille::definirFormeFace2).executer();
        });

//        parametres_forme_spherique_2.disableProperty().bind(lentille.formeFace2Property().isNotEqualTo(FormeFaceLentille.SPHERIQUE)) ;
//        parametres_forme_conique_2.disableProperty().bind(lentille.formeFace2Property().isNotEqualTo(FormeFaceLentille.CONIQUE)) ;

        if (lentille.formeFace2()==FormeFaceLentille.SPHERIQUE)
            parent_parametres_face_2.getChildren().remove(parametres_forme_conique_2);
        else
            parent_parametres_face_2.getChildren().remove(parametres_forme_spherique_2);

        lentille.formeFace2Property().addListener((observable, oldValue, newValue) -> {
            if (newValue==FormeFaceLentille.SPHERIQUE) {
                parent_parametres_face_2.getChildren().remove(parametres_forme_conique_2);
                parent_parametres_face_2.getChildren().add(3,parametres_forme_spherique_2);
            } else if (newValue==FormeFaceLentille.CONIQUE) {
                parent_parametres_face_2.getChildren().remove(parametres_forme_spherique_2);
                parent_parametres_face_2.getChildren().add(3,parametres_forme_conique_2);
            }
        });

        // Rayon 2
        prendreEnCompteRayon2(lentille.rayon2());
        lentille.rayon2Property().addListener(new ChangeListenerAvecGarde<>(this::prendreEnCompteRayon2));
        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas, spinner_rayon_2, lentille.rayon2(),this::definirRayon2);
        // Parametre 2
        prendreEnCompteParametre2(lentille.parametre2());
        lentille.parametre2Property().addListener(new ChangeListenerAvecGarde<>(this::prendreEnCompteParametre2));
        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas, spinner_parametre_2, lentille.parametre2(),this::definirParametre2);
        // Excentricite 2
        prendreEnCompteExcentricite2(lentille.excentricite2());
        lentille.excentricite2Property().addListener(new ChangeListenerAvecGarde<>(this::prendreEnCompteExcentricite2));
        OutilsControleur.integrerSpinnerDoubleValidant(spinner_excentricite_2, lentille.excentricite2(),this::definirExcentricite2);

        // Convexite Face 2
        prendreEnCompteConvexiteFace2(lentille.convexiteFace2());
        lentille.convexiteFace2Property().addListener(new ChangeListenerAvecGarde<>(this::prendreEnCompteConvexiteFace2));
        choix_convexite_face_2.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            LOGGER.log(Level.FINE,"Choix convexite face 2 passe de {0} à {1}", new Object[] {oldValue,newValue}) ;

            if (newValue==choix_convexe_2 && lentille.convexiteFace2()!= CONVEXE)
                new CommandeDefinirUnParametre<>(lentille, CONVEXE, lentille::convexiteFace2, lentille::definirConvexiteFace2).executer();
            if (newValue==choix_plane_2 && lentille.convexiteFace2()!= ConvexiteFaceLentille.PLANE)
                new CommandeDefinirUnParametre<>(lentille, ConvexiteFaceLentille.PLANE, lentille::convexiteFace2, lentille::definirConvexiteFace2).executer();
            if (newValue==choix_concave_2 && lentille.convexiteFace2()!= ConvexiteFaceLentille.CONCAVE)
                new CommandeDefinirUnParametre<>(lentille, ConvexiteFaceLentille.CONCAVE, lentille::convexiteFace2, lentille::definirConvexiteFace2).executer();
        });

        choix_spherique_2.disableProperty().bind(choix_plane_2.selectedProperty());
        choix_conique_2.disableProperty().bind(choix_plane_2.selectedProperty());
        parametres_forme_spherique_2.disableProperty().bind(choix_plane_2.selectedProperty());
        parametres_forme_conique_2.disableProperty().bind(choix_plane_2.selectedProperty());


        checkbox_faces_symetriques.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                choix_spherique_2.selectedProperty().bindBidirectional(choix_spherique_1.selectedProperty());
                choix_conique_2.selectedProperty().bindBidirectional(choix_conique_1.selectedProperty());

                choix_convexe_2.selectedProperty().bindBidirectional(choix_convexe_1.selectedProperty());
                choix_plane_2.selectedProperty().bindBidirectional(choix_plane_1.selectedProperty());
                choix_concave_2.selectedProperty().bindBidirectional(choix_concave_1.selectedProperty());

                spinner_rayon_2.getValueFactory().valueProperty().bindBidirectional(spinner_rayon_1.getValueFactory().valueProperty());
                spinner_parametre_2.getValueFactory().valueProperty().bindBidirectional(spinner_parametre_1.getValueFactory().valueProperty());
                spinner_excentricite_2.getValueFactory().valueProperty().bindBidirectional(spinner_excentricite_1.getValueFactory().valueProperty());
            } else {
                choix_spherique_2.selectedProperty().unbindBidirectional(choix_spherique_1.selectedProperty());
                choix_conique_2.selectedProperty().unbindBidirectional(choix_conique_1.selectedProperty());

                choix_convexe_2.selectedProperty().unbindBidirectional(choix_convexe_1.selectedProperty());
                choix_plane_2.selectedProperty().unbindBidirectional(choix_plane_1.selectedProperty());
                choix_concave_2.selectedProperty().unbindBidirectional(choix_concave_1.selectedProperty());

                spinner_rayon_2.getValueFactory().valueProperty().unbindBidirectional(spinner_rayon_1.getValueFactory().valueProperty());
                spinner_parametre_2.getValueFactory().valueProperty().unbindBidirectional(spinner_parametre_1.getValueFactory().valueProperty());
                spinner_excentricite_2.getValueFactory().valueProperty().unbindBidirectional(spinner_excentricite_1.getValueFactory().valueProperty());
            }
        });
        
    }

    private void definirOrientation(Number or) {
        new CommandeDefinirUnParametre<>(lentille,or.doubleValue(),lentille::orientation,lentille::definirOrientation).executer();
    }
    private void definirEpaisseur(Double l) {
        new CommandeDefinirUnParametreDoubleDistance<>(lentille,l,lentille::epaisseur,lentille::definirEpaisseur).executer() ;
    }
    private void definirDiametre(Double h) {
        new CommandeDefinirUnParametreDoubleDistance<>(lentille,h,lentille::diametre,lentille::definirDiametre).executer() ;
    }
    private void definirRayon1(Double l) {
        new CommandeDefinirUnParametreDoubleDistance<>(lentille,l,lentille::rayon1,lentille::definirRayon1).executer() ;
    }
    private void definirParametre1(Double l) {
        new CommandeDefinirUnParametreDoubleDistance<>(lentille,l,lentille::parametre1,lentille::definirParametre1).executer() ;
    }
    private void definirExcentricite1(Double l) {
        new CommandeDefinirUnParametreDoubleDistance<>(lentille,l,lentille::excentricite1,lentille::definirExcentricite1).executer() ;
    }
    
    private void definirRayon2(Double l) {
        new CommandeDefinirUnParametreDoubleDistance<>(lentille,l,lentille::rayon2,lentille::definirRayon2).executer() ;
    }
    private void definirParametre2(Double l) {
        new CommandeDefinirUnParametreDoubleDistance<>(lentille,l,lentille::parametre2,lentille::definirParametre2).executer() ;
    }
    private void definirExcentricite2(Double l) {
        new CommandeDefinirUnParametreDoubleDistance<>(lentille,l,lentille::excentricite2,lentille::definirExcentricite2).executer() ;
    }

    private void prendreEnCompteEpaisseur(Number l) {
        spinner_epaisseur.getValueFactory().valueProperty().set(l.doubleValue());
    }
    private void prendreEnCompteDiametre(Number l) {
        spinner_diametre.getValueFactory().valueProperty().set(l.doubleValue());
    }

    private void prendreEnCompteRayon1(Number l) {
        spinner_rayon_1.getValueFactory().valueProperty().set(l.doubleValue());
    }
    private void prendreEnCompteParametre1(Number l) {
        spinner_parametre_1.getValueFactory().valueProperty().set(l.doubleValue());
    }
    private void prendreEnCompteExcentricite1(Number l) {
        spinner_excentricite_1.getValueFactory().valueProperty().set(l.doubleValue());
    }
    private void prendreEnCompteParametre2(Number l) {
        spinner_parametre_2.getValueFactory().valueProperty().set(l.doubleValue());
    }
    private void prendreEnCompteExcentricite2(Number l) {
        spinner_excentricite_2.getValueFactory().valueProperty().set(l.doubleValue());
    }

    private void prendreEnCompteConvexiteFace1(ConvexiteFaceLentille c_f) {
        switch (c_f) {
            case CONVEXE -> choix_convexe_1.setSelected(true);
            case PLANE -> choix_plane_1.setSelected(true);
            case CONCAVE -> choix_concave_1.setSelected(true);
        }
    }

    private void prendreEnCompteConvexiteFace2(ConvexiteFaceLentille c_f) {
        switch (c_f) {
            case CONVEXE -> choix_convexe_2.setSelected(true);
            case PLANE -> choix_plane_2.setSelected(true);
            case CONCAVE -> choix_concave_2.setSelected(true);
        }
    }

    private void prendreEnCompteFormeFace1(FormeFaceLentille f_f) {
        switch (f_f) {
            case SPHERIQUE -> choix_spherique_1.setSelected(true);
            case CONIQUE -> choix_conique_1.setSelected(true);
        }
    }

    private void prendreEnCompteFormeFace2(FormeFaceLentille f_f) {
        switch (f_f) {
            case SPHERIQUE -> choix_spherique_1.setSelected(true);
            case CONIQUE -> choix_conique_2.setSelected(true);
        }
    }
    
    
    private void prendreEnCompteRayon2(Number l) {
        spinner_rayon_2.getValueFactory().valueProperty().set(l.doubleValue());
    }

    private void definirXCentreLentille(Double x_c) {
        new CommandeDefinirUnParametrePoint<>(lentille,new Point2D(x_c,lentille.yCentre()),lentille::centre,lentille::definirCentre).executer();
    }
    private void definirYCentreLentille(Double y_c) {
        new CommandeDefinirUnParametrePoint<>(lentille,new Point2D(lentille.xCentre(),y_c),lentille::centre,lentille::definirCentre).executer();        
    }

    private void prendreEnComptePositionEtOrientation(PositionEtOrientation nouvelle_pos_et_or) {
        spinner_xcentre.getValueFactory().valueProperty().set(nouvelle_pos_et_or.position().getX());
        spinner_ycentre.getValueFactory().valueProperty().set(nouvelle_pos_et_or.position().getY());
        spinner_orientation.getValueFactory().valueProperty().set(nouvelle_pos_et_or.orientation_deg());
        slider_orientation.valueProperty().set(nouvelle_pos_et_or.orientation_deg());
    }


}
