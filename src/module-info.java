module CrazyDiamond {
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.controls;
    requires java.logging;
    requires javafx.fxml;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires clipper2;
    opens CrazyDiamond.Controller ; // Requis pour que l'application CrazyDiamond trouve (lors de son éxécution) les fichiers .fxml des différents panneaux (sui sont dans Controller/View)
    exports CrazyDiamond.Model ; // Requis pour que les différents packages de CrazyDiamond se voient les uns les autres
    exports CrazyDiamond.Controller ; // Idem
    exports CrazyDiamond;
}