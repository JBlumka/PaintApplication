module com.example.pain_t_v1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.swing;
    requires javafx.media;


    opens com.example.pain_t to javafx.fxml;
    exports com.example.pain_t;
}