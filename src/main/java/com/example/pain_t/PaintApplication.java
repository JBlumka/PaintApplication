package com.example.pain_t;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Optional;


public class PaintApplication extends Application {

    FXMLLoader fxmlLoader = new FXMLLoader(PaintApplication.class.getResource("paint-view.fxml"));

    //Override start method to create Pain(t) scene
    @Override
    public void start(Stage stage) throws IOException {
        Scene scene = new Scene(fxmlLoader.load());
        stage.setMaximized(true);
        stage.setTitle("Pain(t)");
        stage.setScene(scene);
        stage.show();


        PaintController controller = fxmlLoader.getController();
        try{
            controller.startUpMethod();

        } catch (Exception e) {
            System.out.println("Error executing controller.startUpMethod()");
        }

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
        Alert alert = new Alert(Alert.AlertType.NONE, "Do you want to save changes to all projects before closing?\n", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
        alert.setTitle("Pain(t)");
        Optional<ButtonType> optionClicked = alert.showAndWait();
        if(optionClicked.isPresent()){
            if(optionClicked.get().equals(ButtonType.YES)) {

                //iterate through every file and save them
                PaintController controller = fxmlLoader.getController();
                try{
                    controller.saveAllTabs();

                } catch (Exception e) {
                    System.out.println("Error executing controller.saveAllTabs()");
                }


                System.out.println("Clicked YES");
                mainWindow.close();
                System.exit(0);
            } else if (optionClicked.get().equals(ButtonType.NO)) {
                System.out.println("Clicked NO");
                mainWindow.close();
                System.exit(0);
            } else if (optionClicked.get().equals(ButtonType.CANCEL)) {
                System.out.println("Clicked Cancel");
            }
        }
    }

}
