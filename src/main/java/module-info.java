module ru.ibrrus {
    requires javafx.controls;
    requires javafx.fxml;

    opens ru.ibrrus to javafx.fxml;
    exports ru.ibrrus;
}