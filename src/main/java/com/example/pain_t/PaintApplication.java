package com.example.pain_t;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import java.io.IOException;


public class PaintApplication extends Application {

    //Override start method to create Pain(t) scene
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(PaintApplication.class.getResource("paint-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setMaximized(true);
        stage.setTitle("Pain(t)");
        stage.setScene(scene);
        stage.show();

        //Override Close Request to launch custom close dialog window
        stage.setOnCloseRequest(evt -> {
            // prevent window from closing
            evt.consume();
            // execute own shutdown procedure
            shutdown(stage);
        });
    }

    public static void main(String[] args) {
        launch();
    }

    //Custom shutdown dialog message
    private void shutdown(Stage mainWindow) {
        Alert alert = new Alert(Alert.AlertType.NONE, "You may have unsaved changes.\nAre you sure you would like to close this application?", ButtonType.YES, ButtonType.NO);
        if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            // you may need to close other windows or replace this with Platform.exit();
            mainWindow.close();
            System.exit(0);
        }
    }


}
