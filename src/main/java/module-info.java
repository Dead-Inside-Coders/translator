module translator {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;
    requires java.logging;
    requires log4j;

    opens com.tstu;
    opens com.tstu.controllers;
    opens com.tstu.fxml;
}