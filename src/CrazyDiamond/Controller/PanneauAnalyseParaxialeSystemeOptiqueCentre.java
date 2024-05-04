package CrazyDiamond.Controller;

import CrazyDiamond.Model.*;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.binding.ObjectExpression;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DoubleStringConverter;

import java.util.Objects;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PanneauAnalyseParaxialeSystemeOptiqueCentre {


    // Modèle
    SystemeOptiqueCentre soc ;

    CanvasAffichageEnvironnement canvas;

    // Récupération du logger
    private static final Logger LOGGER = Logger.getLogger( "CrazyDiamond" );
    private static final ResourceBundle rb = ResourceBundle.getBundle("CrazyDiamond") ;

    static class DoubleStringConverterSansException extends DoubleStringConverter {

        private static final String regExp = "[\\x00-\\x20]*[+-]?(((((\\d+)(\\.)?((\\p{Digit}+)?)([eE][+-]?(\\p{Digit}+))?)|(\\.((\\p{Digit}+))([eE][+-]?(\\p{Digit}+))?)|(((0[xX](\\p{XDigit}+)(\\.)?)|(0[xX](\\p{XDigit}+)?(\\.)(\\p{XDigit}+)))[pP][+-]?(\\p{Digit}+)))[fFdD]?))[\\x00-\\x20]*";

        private static final Pattern pattern = Pattern.compile(regExp);
        private static boolean isDouble(String s) {
            Matcher m = pattern.matcher(s);
            return m.matches();
        }

        @Override
        public Double fromString(String s) {

            if (s==null || !isDouble(s))
                return null ;

            return super.fromString(s);
        }
    }


    @FXML private Label label_nature_soc;

    @FXML private Label focale_objet;
    @FXML private Label focale_image;

    @FXML private Label z_pl_focal_objet;
    @FXML private Label z_pl_focal_image;
    @FXML private ToggleButton toggle_pl_focaux;
    @FXML private Label z_pl_principal_objet;
    @FXML private Label z_pl_principal_image;
    @FXML private ToggleButton toggle_pl_principaux;
    @FXML private Label z_pl_nodal_objet;
    @FXML private Label z_pl_nodal_image;
    @FXML private ToggleButton toggle_pl_nodaux;

    @FXML private Label label_vergence;
    @FXML private Label label_interstice;

    @FXML public Label label_z_objet;
    @FXML private Spinner<Double> spinner_z_objet;


    @FXML public Label label_h_objet;
    @FXML private Spinner<Double> spinner_h_objet;

    @FXML private Label z_image;
    @FXML private Label h_image;


    @FXML private ToggleButton toggle_montrer_objet;
    @FXML private ToggleButton toggle_montrer_image;

    @FXML private Label grandissement_transversal;
    @FXML private Label grandissement_angulaire;
    @FXML private Label grandissement_longitudinal;

    @FXML private ToggleButton toggle_montrer_dioptres;
    @FXML private TableView<RencontreDioptreParaxial> table_intersections;
    @FXML private  TableColumn<RencontreDioptreParaxial,Number> col_numero;
    @FXML private  TableColumn<RencontreDioptreParaxial,Double> col_z;
    @FXML private  TableColumn<RencontreDioptreParaxial,Double> col_r_courbure;
    @FXML private  TableColumn<RencontreDioptreParaxial,Double> col_r_diaphragme;
    @FXML private  TableColumn<RencontreDioptreParaxial,Double> col_n_avant;
    @FXML private  TableColumn<RencontreDioptreParaxial,Double> col_n_apres;
    @FXML private  TableColumn<RencontreDioptreParaxial,String> col_element;
    @FXML private  TableColumn<RencontreDioptreParaxial,Boolean> col_ignorer;
    @FXML private  TableColumn<RencontreDioptreParaxial,String> col_sens;
    @FXML private  TableColumn<RencontreDioptreParaxial,String> col_do;
    @FXML private  TableColumn<RencontreDioptreParaxial,String> col_dc;
    @FXML private  TableColumn<RencontreDioptreParaxial,String> col_dcpl;
    @FXML private  TableColumn<RencontreDioptreParaxial,String> col_dct;

    @FXML private Label label_n_entree;
    @FXML private Label label_n_sortie;

    @FXML private Label label_a;
    @FXML private Label label_b;
    @FXML private Label label_c;
    @FXML private Label label_d;


    class FormatageNombreAvecPrecisionAdaptee extends StringBinding {

        private final ObjectExpression<Double> obj_expr_double;
        private final DoubleExpression double_expr ;

        private final boolean est_une_distance ;

        public FormatageNombreAvecPrecisionAdaptee(ObjectExpression<Double> nb) {
            this(nb,false) ;
        }

        public FormatageNombreAvecPrecisionAdaptee(ObjectExpression<Double> nb, boolean est_une_distance) {
            super.bind(nb);
            this.obj_expr_double = nb ;
            this.double_expr = null ;
            this.est_une_distance = est_une_distance ;
        }

        public FormatageNombreAvecPrecisionAdaptee(DoubleExpression dp) {
            this(dp,false) ;
        }
        public FormatageNombreAvecPrecisionAdaptee(DoubleExpression dp,boolean est_une_distance) {
            super.bind(dp);
            this.obj_expr_double = null ;
            this.double_expr = dp ;
            this.est_une_distance = est_une_distance ;
        }

        @Override protected String computeValue() {
            String valeur_resultat = obj_expr_double!=null?canvas.convertisseurAffichageDistance().toString(obj_expr_double.get())
                        :canvas.convertisseurAffichageDistance().toString(double_expr.get());

            if (est_une_distance)
                valeur_resultat = valeur_resultat + canvas.environnement().suffixeUnite() ;

            return valeur_resultat ;
        }

    }


    public PanneauAnalyseParaxialeSystemeOptiqueCentre(SystemeOptiqueCentre soc, CanvasAffichageEnvironnement cnv) {
        LOGGER.log(Level.INFO,"Construction du SOC") ;

        if (soc==null)
            throw new IllegalArgumentException("L'objet SystemeOptiqueCentre attaché au PanneauAnalyseParaxialeSystemeOptiqueCentre ne peut pas être 'null'") ;

        this.soc = soc;
        this.canvas = cnv ;

    }

    public void initialize() {
        LOGGER.log(Level.INFO,"Initialisation du PanneauAnayseParaxialeSystemeOptiqueCentre et de ses liaisons") ;

        StringBinding affiche_nature_soc = new StringBinding() {
            { super.bind(soc.ZPlanFocal2Property(),soc.ZPlanPrincipal2Property(),soc.IntersectionsSurAxeProperty(),soc.MatriceTransfertESProperty()) ;}
            @Override protected String computeValue() {

                StringBuilder sb = new StringBuilder() ;
                sb.append("Système ") ;

                if (soc.MatriceTransfertES()!=null && Environnement.quasiEgal(soc.MatriceTransfertES().getMyx(),0d)) {
                    sb.append("afocal") ;
                    return sb.toString() ;
                }

                if (soc.ZPlanFocal2()==null || soc.ZPlanPrincipal2()==null || soc.NSortie()==0d
                        ||Objects.equals(soc.ZPlanFocal2(),soc.ZPlanPrincipal2()) || soc.InterSectionsSurAxe()==null)
                    return "-" ;


                for (RencontreDioptreParaxial itas : soc.dioptresRencontres())
                    if ((itas.obstacleSurface().traitementSurface()== TraitementSurface.REFLECHISSANT
                            || ( (itas.obstacleSurface().traitementSurface()==TraitementSurface.PARTIELLEMENT_REFLECHISSANT)
                            && (itas.obstacleSurface().tauxReflexionSurface()>50)))) {
                        sb.append("cata") ;
                        break ;
                    }

                sb.append("dioptrique ") ;

                double vergence = soc.NSortie()/(soc.ZPlanFocal2()-soc.ZPlanPrincipal2()) ;

                if (Environnement.quasiEgal(vergence,0d))
                    sb.append("afocal") ;
                else if (vergence>0)
                    sb.append("convergent") ;
                else
                    sb.append("divergent") ;

                return sb.toString();
            }
        };
        label_nature_soc.textProperty().bind(affiche_nature_soc);



        StringBinding calcul_focale_objet = new StringBinding() {
            { super.bind(soc.ZPlanFocal1Property(),soc.ZPlanPrincipal1Property()) ;}
            @Override protected String computeValue() {
                if (soc.ZPlanFocal1()==null || soc.ZPlanPrincipal1()==null)
                    return "" ;

                return canvas.convertisseurAffichageDistance().toString(soc.ZPlanFocal1() - soc.ZPlanPrincipal1())
                        + canvas.environnement().suffixeUnite() ;
            }
        };
        focale_objet.textProperty().bind(calcul_focale_objet);

        StringBinding calcul_focale_image = new StringBinding() {
            { super.bind(soc.ZPlanFocal2Property(),soc.ZPlanPrincipal2Property()) ;}
            @Override protected String computeValue() {
                if (soc.ZPlanFocal2()==null || soc.ZPlanPrincipal2()==null)
                    return "" ;

                return canvas.convertisseurAffichageDistance().toString(soc.ZPlanFocal2() - soc.ZPlanPrincipal2())
                        + canvas.environnement().suffixeUnite() ;
            }
        };
        focale_image.textProperty().bind(calcul_focale_image);

        z_pl_focal_objet.textProperty().bind(new FormatageNombreAvecPrecisionAdaptee(soc.ZPlanFocal1Property(),true));
        z_pl_focal_image.textProperty().bind(new FormatageNombreAvecPrecisionAdaptee(soc.ZPlanFocal2Property(),true));

        z_pl_principal_objet.textProperty().bind(new FormatageNombreAvecPrecisionAdaptee(soc.ZPlanPrincipal1Property(),true));
        z_pl_principal_image.textProperty().bind(new FormatageNombreAvecPrecisionAdaptee(soc.ZPlanPrincipal2Property(),true));

        z_pl_nodal_objet.textProperty().bind(new FormatageNombreAvecPrecisionAdaptee(soc.ZPlanNodal1Property(),true));
        z_pl_nodal_image.textProperty().bind(new FormatageNombreAvecPrecisionAdaptee(soc.ZPlanNodal2Property(),true));


        StringBinding affiche_label_z_objet = new StringBinding() {
            { super.bind(canvas.environnement().uniteProperty()) ;}
            @Override protected String computeValue() {return "Z ("+canvas.environnement().unite().symbole+") :";}
        };
        label_z_objet.textProperty().bind(affiche_label_z_objet);


        ObjectProperty<Double> z_objet_object_property = soc.ZObjetProperty();
        // Mise à jour de la valeur du spinner quand la valeur de la propriété change
        z_objet_object_property.addListener(new ChangeListenerAvecGarde<>(spinner_z_objet.getValueFactory().valueProperty()::set));
        // Mise à jour de la propriété ZObjet du SOC quand la valeur du spinner change
        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_z_objet, z_objet_object_property.get(), soc::definirZObjet);

        // Mise à jour de la valeur du spinner quand la valeur de la propriété change
//        spinner_z_objet.getValueFactory().valueProperty().bindBidirectional(z_objet_object_property);

        spinner_z_objet.getStyleClass().add(Spinner.STYLE_CLASS_ARROWS_ON_RIGHT_HORIZONTAL) ;
        canvas.ajustePasEtAffichageSpinnerValueFactoryDistance((SpinnerValueFactory.DoubleSpinnerValueFactory) spinner_z_objet.getValueFactory());

        StringBinding affiche_label_h_objet = new StringBinding() {
            { super.bind(canvas.environnement().uniteProperty()) ;}
            @Override protected String computeValue() {return "h ("+canvas.environnement().unite().symbole+") :";}
        };
        label_h_objet.textProperty().bind(affiche_label_h_objet);


        ObjectProperty<Double> h_objet_object_property = soc.HObjetProperty();
        // Mise à jour de la valeur du spinner quand la valeur de la propriété change
        h_objet_object_property.addListener(new ChangeListenerAvecGarde<>(spinner_h_objet.getValueFactory().valueProperty()::set));
        // Mise à jour de la propriété HObjet du SOC quand la valeur du spinner change
        OutilsControleur.integrerSpinnerDoubleValidantAdaptatifPourCanvas(canvas,spinner_h_objet, h_objet_object_property.get(), soc::definirHObjet);
//        spinner_h_objet.getValueFactory().valueProperty().bindBidirectional(h_objet_object_property);
        spinner_h_objet.getStyleClass().add(Spinner.STYLE_CLASS_ARROWS_ON_LEFT_VERTICAL) ;

        canvas.ajustePasEtAffichageSpinnerValueFactoryDistance((SpinnerValueFactory.DoubleSpinnerValueFactory) spinner_h_objet.getValueFactory());

        z_image.textProperty().bind(new FormatageNombreAvecPrecisionAdaptee(soc.ZImageProperty(),true));
        h_image.textProperty().bind(new FormatageNombreAvecPrecisionAdaptee(soc.HImageProperty(),true));

        grandissement_transversal.textProperty().bind(new FormatageNombreAvecPrecisionAdaptee(soc.GrandissementTransversalProperty()));

        grandissement_angulaire.textProperty().bind(new FormatageNombreAvecPrecisionAdaptee(soc.GrandissementAngulaireProperty()));

        grandissement_longitudinal.textProperty().bind(new FormatageNombreAvecPrecisionAdaptee(soc.GrandissementLongitudinalProperty()));

        toggle_pl_focaux.selectedProperty().bindBidirectional(soc.MontrerPlansFocauxProperty());
        toggle_pl_principaux.selectedProperty().bindBidirectional(soc.MontrerPlansPrincipauxProperty());
        toggle_pl_nodaux.selectedProperty().bindBidirectional(soc.MontrerPlansNodauxProperty());

        toggle_montrer_objet.selectedProperty().bindBidirectional(soc.MontrerObjetProperty());
        toggle_montrer_image.selectedProperty().bindBidirectional(soc.MontrerImageProperty());

        toggle_montrer_dioptres.selectedProperty().bindBidirectional(soc.MontrerDioptresProperty());

        label_n_entree.textProperty().bind(new FormatageNombreAvecPrecisionAdaptee(soc.NEntreeProperty()));
        label_n_sortie.textProperty().bind(new FormatageNombreAvecPrecisionAdaptee(soc.NSortieProperty()));

        StringBinding affiche_a = new StringBinding() {
            { super.bind(soc.MatriceTransfertESProperty()) ;}
            @Override protected String computeValue() {
                if (soc.MatriceTransfertES()==null)
                    return "" ;

                return canvas.convertisseurAffichageDistance().toString(soc.MatriceTransfertES().getMxx());
            }
        };
        label_a.textProperty().bind(affiche_a);

        StringBinding affiche_b = new StringBinding() {
            { super.bind(soc.MatriceTransfertESProperty()) ;}
            @Override protected String computeValue() {
                if (soc.MatriceTransfertES()==null)
                    return "" ;

                return canvas.convertisseurAffichageDistance().toString(soc.MatriceTransfertES().getMxy());
            }
        };
        label_b.textProperty().bind(affiche_b);

        StringBinding affiche_c = new StringBinding() {
            { super.bind(soc.MatriceTransfertESProperty()) ;}
            @Override protected String computeValue() {
                if (soc.MatriceTransfertES()==null)
                    return "" ;

                return canvas.convertisseurAffichageDistance().toString(soc.MatriceTransfertES().getMyx());
            }
        };
        label_c.textProperty().bind(affiche_c);

        StringBinding affiche_d = new StringBinding() {
            { super.bind(soc.MatriceTransfertESProperty()) ;}
            @Override protected String computeValue() {
                if (soc.MatriceTransfertES()==null)
                    return "" ;

                return canvas.convertisseurAffichageDistance().toString(soc.MatriceTransfertES().getMyy());
            }
        };
        label_d.textProperty().bind(affiche_d);

        StringBinding calcul_et_formatage_vergence = new StringBinding() {
            { super.bind(soc.MatriceTransfertESProperty()) ;}
            @Override protected String computeValue() {
                if (soc.MatriceTransfertES()==null)
                    return "" ;

                double c = soc.MatriceTransfertES().getMyx() ;

                // NB :  Attention le résultat n'est en dioptries (δ) que si les distances sont en mètres
                return canvas.convertisseurAffichageDistance().toString(-c)+" δ";
            }
        };
        label_vergence.textProperty().bind(calcul_et_formatage_vergence);

        StringBinding calcul_et_formatage_interstice = new StringBinding() {
            { super.bind(soc.ZPlanPrincipal1Property(),soc.ZPlanPrincipal2Property()) ;}
            @Override protected String computeValue() {
                if (soc.ZPlanPrincipal1()==null || soc.ZPlanPrincipal2()==null|| Objects.equals(soc.ZPlanPrincipal1(), soc.ZPlanPrincipal2()))
                    return "" ;

                return canvas.convertisseurAffichageDistance().toString(soc.ZPlanPrincipal2()-soc.ZPlanPrincipal1())
                        + suffixeUniteAUtiliser() ;
            }
        };
        label_interstice.textProperty().bind(calcul_et_formatage_interstice);

        // Faire les bindings du tableau des intersections
        table_intersections.setItems(soc.dioptresRencontres());


        // TODO : voir si vraiment utile
        soc.dioptresRencontresProperty().addListener(
                (ListChangeListener<? super RencontreDioptreParaxial>) (observable) -> table_intersections.refresh()
        );
        soc.matriceTransfertOptiqueProperty().addListener(
                (observableValue, affine, t1) -> table_intersections.refresh()
        );

        col_numero.setCellValueFactory(column-> new ReadOnlyObjectWrapper<>(table_intersections.getItems().indexOf(column.getValue()) + 1)) ;

        StringBinding affiche_label_z_dioptre = new StringBinding() {
            { super.bind(canvas.environnement().uniteProperty()) ;}
            @Override protected String computeValue() {return "Z ("+canvas.environnement().unite().symbole+")";}
        };
        col_z.textProperty().bind(affiche_label_z_dioptre);

        col_z.setCellValueFactory(p -> p.getValue().zProperty().asObject());
        col_z.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverterSansException()));

        col_z.setOnEditCommit(e-> {
            RencontreDioptreParaxial intersection = e.getTableView().getItems().get(e.getTablePosition().getRow());

            if (e.getNewValue()==null) {
                table_intersections.refresh();
                return; // Annule le commit
            }

            Point2D deplacement = soc.direction().multiply(e.getNewValue()-e.getOldValue()) ;
//            intersection.obstacleSurface().translaterParCommande(deplacement); // Déclenchera un recalcul de la matrice optique qui mettra à jour la valeur de Z affichée dans la table


            // Identification de l'obstacle réel de l'environnement qui contient l'obstacle dont on a changé le Z, ou cet obstacle
            // lui-même, s'il ne fait ni partie d'un groupe, ni d'une composante privée d'un obstacle de l'environnement
            Obstacle obs_reel_a_deplacer = canvas.environnement().obstacleContenant(intersection.obstacleSurface()) ;


//            // Composition interne privée qui n'est pas dans l'arbre des obstacles de l'environnement (ex : composition interne d'une lentlle)
//            Composition cmp_interne_a_deplacer = (obs_reel_a_deplacer.parent() instanceof Composition cmp ? (cmp.parent()==null?cmp:null) : null) ;
////            Lentille lentille_a_deplacer = (cmp_interne_a_deplacer!=null?(cmp_interne_a_deplacer.parent() instanceof Lentille lent?lent:null):null) ;
            Groupe grp_a_deplacer = canvas.environnement().plusGrandGroupeSolidaireContenant(obs_reel_a_deplacer) ;
            Composition cmp_a_deplacer = canvas.environnement().plusGrandeCompositionContenant(obs_reel_a_deplacer) ;
            if (grp_a_deplacer!=null)
                grp_a_deplacer.translaterParCommande(deplacement);
            else if (cmp_a_deplacer!=null)
                cmp_a_deplacer.translaterParCommande(deplacement);
//            else if (lentille_a_deplacer!=null)
//                lentille_a_deplacer.translaterParCommande(deplacement);
//            else if (cmp_interne_a_deplacer!=null)
//                cmp_interne_a_deplacer.translaterParCommande(deplacement);
            else
                obs_reel_a_deplacer.translaterParCommande(deplacement);

        });
        col_z.setEditable(true);

//        col_z.setStyle(".table-cell:filled {-fx-background-color: red}");
        col_z.setStyle("-fx-text-fill: blue;");

        StringBinding affiche_label_r_courbure = new StringBinding() {
            { super.bind(canvas.environnement().uniteProperty()) ;}
            @Override protected String computeValue() {return "R. Courbure ("+canvas.environnement().unite().symbole+")";}
        };
        col_r_courbure.textProperty().bind(affiche_label_r_courbure);

        col_r_courbure.setCellValueFactory(p -> p.getValue().rayonCourbureProperty());

        StringBinding affiche_label_r_diaphragme = new StringBinding() {
            { super.bind(canvas.environnement().uniteProperty()) ;}
            @Override protected String computeValue() {return "R. Diaphragme ("+canvas.environnement().unite().symbole+")";}
        };
        col_r_diaphragme.textProperty().bind(affiche_label_r_diaphragme);

        col_r_diaphragme.setCellValueFactory(p -> p.getValue().rayonDiaphragmeProperty());
        col_r_diaphragme.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverterSansException()));
        col_r_diaphragme.setOnEditCommit(e-> {
            RencontreDioptreParaxial intersection = e.getTableView().getItems().get(e.getTablePosition().getRow());
            double pup_max = intersection.obstacleSurface().rayonDiaphragmeMaximumConseille() ;

            if (e.getNewValue()==null || e.getNewValue()>pup_max) {
                intersection.obstacleSurface().forcerRayonDiaphragmeMaximumConseille(e.getNewValue());
            }

            if (intersection.obstacleSurface().aUneProprieteDiaphragme())
                intersection.obstacleSurface().diaphragmeProperty().setValue(e.getNewValue()); // Déclenchera un recalcul de la matrice optique qui mettre à jour la valeur de diaphragme affichée dans la table
            else
                intersection.rayonDiaphragmeProperty().set(e.getNewValue());
        });
        col_r_diaphragme.setEditable(true);
        col_r_diaphragme.setStyle("-fx-text-fill: blue;");

         col_n_avant.setCellValueFactory(p -> p.getValue().indiceAvantProperty().asObject());

         col_n_apres.setCellValueFactory(p -> p.getValue().indiceApresProperty().asObject());

         col_element.setCellValueFactory(p -> p.getValue().obstacleSurface().nomProperty());

         col_ignorer.setCellValueFactory(p -> p.getValue().ignorerProperty());
         col_ignorer.setCellFactory(CheckBoxTableCell.forTableColumn(col_ignorer));
//         col_ignorer.setOnEditCommit(e-> {
//             e.getTableView().getItems().get(e.getTablePosition().getRow()).ignorer.set(e.getNewValue()) ;
//
//             soc.calculeElementsCardinaux();
//
//         });
//        col_ignorer.setOnEditStart(e-> {
//
//            soc.calculeElementsCardinaux();
//
//        });
         col_ignorer.setEditable(true);
        col_ignorer.setStyle("-fx-text-fill: blue;");
//        col_ignorer.setStyle("-fx-background: blue");

        col_sens.setCellValueFactory(p -> p.getValue().sensProperty());

        col_do.setCellValueFactory(p -> p.getValue().estDiaphragmeOuvertureProperty());
        col_dc.setCellValueFactory(p -> p.getValue().estDiaphragmeChampProperty());
        col_dcpl.setCellValueFactory(p -> p.getValue().estDiaphragmeChampPleineLumiereProperty());
        col_dct.setCellValueFactory(p -> p.getValue().estDiaphragmeChampTotalProperty());

         table_intersections.getColumns().set(1,col_z);
         table_intersections.getColumns().set(2,col_r_courbure);
         table_intersections.getColumns().set(3,col_r_diaphragme);
         table_intersections.getColumns().set(4,col_n_avant);
         table_intersections.getColumns().set(5,col_n_apres);
         table_intersections.getColumns().set(6,col_element);
         table_intersections.getColumns().set(7,col_ignorer);
         table_intersections.getColumns().set(8,col_sens);
         table_intersections.getColumns().set(9,col_do);
         table_intersections.getColumns().set(10,col_dc);
         table_intersections.getColumns().set(11,col_dcpl);
         table_intersections.getColumns().set(12,col_dct);

         table_intersections.setEditable(true);

    }

    private String suffixeUniteAUtiliser() {

        return " "+canvas.environnement().unite().symbole ;

    }

    private void definirZObjetSOC(Double z_o) {soc.definirZObjet(z_o);}
    private void definirHObjetSOC(Double h_o) {soc.definirHObjet(h_o);}
}
