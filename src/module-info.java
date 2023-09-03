module CrazyDiamond {
    requires Clipper;
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.controls;
    requires java.logging;
    requires javafx.fxml;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    opens CrazyDiamond.Controller ;
    exports CrazyDiamond;
}