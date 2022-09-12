package com.example.pain_t;

import javafx.event.ActionEvent;
import javafx.event.Event;
import java.awt.image.RenderedImage;
import java.io.*;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import java.io.File;


public class PaintController {

    //ATTRIBUTE DECLARATIONS
    File savePath = null;
    @FXML
    private Canvas canvas;
    //END OF ATTRIBUTE DECLARATIONS

    //TOP MENUBAR

    //Click event for Menu > File > New
    public void ClickedMenuBar_File_New(ActionEvent e) {
        System.out.println("File/New Clicked");

        clearCanvasMethod();
    }

    //Click event for Menu > File > Open
    public void ClickedMenuBar_File_Open(ActionEvent e) throws FileNotFoundException {
        System.out.println("File/Open Clicked");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open");
        Stage stage = (Stage) canvas.getScene().getWindow();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("IMAGE FILES", "*.jpg", "*.png", "*.gif"));
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            System.out.println(selectedFile);
            clearCanvasMethod();
            Image image = new Image(selectedFile.toURI().toString());
                canvas.setWidth(image.getWidth());
                canvas.setHeight(image.getHeight());
            GraphicsContext gc1 = canvas.getGraphicsContext2D();
            gc1.drawImage(image, 0, 0);
        }
    }

    //Click event for Menu > File > Save
    public void ClickedMenuBar_File_Save(ActionEvent e) {
        System.out.println("File/Save Clicked");

        if(savePath != null){
            System.out.print("Save As already occurred.");
            try {
                WritableImage writableImage = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
                canvas.snapshot(null, writableImage);
                RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
                ImageIO.write(renderedImage, "png", savePath);
            } catch (IOException ex) {
                ex.printStackTrace();
                System.out.println("Error!");
            }
        } else {
            SaveAsMethod();
        }
    }

    //Click event for Menu > File > SaveAs
    public void ClickedMenuBar_File_SaveAs(ActionEvent e) {
        System.out.println("File/SaveAs Clicked");
        SaveAsMethod();
    }

    //Click event for Menu > File > Exit
    public void ClickedMenuBar_File_Exit(ActionEvent e) {
        System.out.println("File/Exit Clicked");
        System.exit(0);
    }

    //Click event for Menu > Toolbar > Home
    public void ClickedMenuBar_Home(Event e) { System.out.println("Home Clicked"); }

    //Click event for Menu > Toolbar > View
    public void ClickedMenuBar_View(ActionEvent e) {
        System.out.println("View Clicked");
    }

    //Click event for Menu > Help > About
    public void ClickedMenuBar_Help_About(ActionEvent e) {
        System.out.println("Help/About Clicked");

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information Dialog");
        alert.setHeaderText(null);
        alert.setContentText("Welcome to Pain(t) Version 3.1\n\nIcons sourced from https://icons8.com/");
        alert.showAndWait();
    }
    //END OF TOP MENUBAR


    //TOP TOOLBAR

    //Click event for ToolBar > Button
    public void ClickedToolBarButton(ActionEvent e) {
        System.out.println("ToolBar Button Clicked!");
    }

    //END OF TOP TOOLBAR


    //HELPER METHODS

    //Method to invoke SaveAs
    public void SaveAsMethod() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save As");
        Stage stage = (Stage) canvas.getScene().getWindow();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("IMAGE FILE", "*.png"));
        File fileDest = fileChooser.showSaveDialog(stage);
        if (fileDest != null) {
            System.out.println(fileDest);
            try {
                WritableImage writableImage = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
                canvas.snapshot(null, writableImage);
                RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
                ImageIO.write(renderedImage, "png", fileDest);
                savePath = fileDest;
            } catch (IOException ex) {
                ex.printStackTrace();
                System.out.println("Error!");
            }
        }
    }

    //Method to clear canvas
    private void clearCanvasMethod() {
        canvas.setWidth(canvas.getScene().getWindow().getWidth());
        canvas.setHeight(canvas.getScene().getWindow().getHeight() - 96); //96 equals height of menu and toolbar
        canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    //END OF HELPER METHODS

}

