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

    private void shutdown(Stage mainWindow) {
        // you could also use your logout window / whatever here instead
        Alert alert = new Alert(Alert.AlertType.NONE, "Really close the stage?", ButtonType.YES, ButtonType.NO);
        if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            // you may need to close other windows or replace this with Platform.exit();
            mainWindow.close();
        }
    }


}
