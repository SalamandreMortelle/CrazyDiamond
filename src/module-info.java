module CrazyDiamond {
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.controls;
    requires java.logging;
    requires javafx.fxml;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires clipper2;
    opens CrazyDiamond.Controller ;
    exports CrazyDiamond;
}