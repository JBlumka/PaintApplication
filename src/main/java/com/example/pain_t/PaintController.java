package com.example.pain_t;

import javafx.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.*;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.imageio.ImageIO;
import java.io.File;


public class PaintController {

    //ATTRIBUTE DECLARATIONS
    File savePath = null;
    @FXML
    private Canvas canvas;
    @FXML
    private ColorPicker cp;
    @FXML
    private Slider slider;

    @FXML
    private AnchorPane innerAnchorPane;

    Canvas tempCanvas;

    //Drawing Parameters
    double drawStartX;
    double drawStartY;

    public enum Mode {
        Paint, Eraser, ColorPicker, Cursor, Line, DashedLine, Square, Rectangle, Circle, Ellipse
    }
    private Mode status = Mode.Paint;

    //END OF ATTRIBUTE DECLARATIONS



    //TOP MENUBAR

    //Click event for Menu > File > New
    public void ClickedMenuBar_File_New() {
        System.out.println("File/New Clicked");

        clearCanvasMethod(canvas);
        canvas.setWidth(canvas.getScene().getWindow().getWidth());
        canvas.setHeight(canvas.getScene().getWindow().getHeight() - 96); //96 equals height of menu and toolbar
    }

    //Click event for Menu > File > Open
    public void ClickedMenuBar_File_Open() throws FileNotFoundException {
        System.out.println("File/Open Clicked");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open");
        Stage stage = (Stage) canvas.getScene().getWindow();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("IMAGE FILES", "*.jpg", "*.png", "*.gif"));
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            System.out.println(selectedFile);
            savePath = selectedFile;
            clearCanvasMethod(canvas);
            Image image = new Image(selectedFile.toURI().toString());
                canvas.setWidth(image.getWidth());
                canvas.setHeight(image.getHeight());
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.drawImage(image, 0, 0);
        }
    }

    //Click event for Menu > File > Save
    public void ClickedMenuBar_File_Save() {
        System.out.println("File/Save Clicked");

        if(savePath != null){
            System.out.print("Save As already occurred.");
            SaveMethod();
        } else {
            SaveAsMethod();
        }
    }

    //Click event for Menu > File > SaveAs
    public void ClickedMenuBar_File_SaveAs() {
        System.out.println("File/SaveAs Clicked");
        SaveAsMethod();
    }

    //Click event for Menu > File > Exit
    public void ClickedMenuBar_File_Exit() {
        System.out.println("File/Exit Clicked");
        Stage stage = (Stage) canvas.getScene().getWindow();
        stage.fireEvent(
                new WindowEvent(
                        stage,
                        WindowEvent.WINDOW_CLOSE_REQUEST
                )
        );
    }

    //Click event for Menu > Help > Help
    public void ClickedMenuBar_Help_Help() {
        System.out.println("Help/About Clicked");

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Helpful Information Dialog");
        alert.setHeaderText(null);
        alert.setContentText("It's a paint application. Figure it out.");
        alert.showAndWait();
    }

    //Click event for Menu > Help > About
    public void ClickedMenuBar_Help_About() {
        System.out.println("Help/About Clicked");

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information Dialog");
        alert.setHeaderText(null);
        alert.setContentText("Welcome to Pain(t) Version 3.14\n\nIcons sourced from https://icons8.com/");
        alert.showAndWait();
    }
    //END OF TOP MENUBAR



    //TOP TOOLBAR

    //Click event for ToolBar > PaintButton
    public void ClickedToolBarPaintButton(ActionEvent e) {
        System.out.println("ToolBar Paint Button Clicked!");
        status = Mode.Paint;
    }

    //Click event for ToolBar > EraserButton
    public void ClickedToolBarEraserButton(ActionEvent e) {
        System.out.println("ToolBar Eraser Button Clicked!");
        status = Mode.Eraser;
    }

    //Click event for ToolBar > ColorPickerButton
    public void ClickedToolBarColorPickerButton(ActionEvent e) {
        System.out.println("ToolBar ColorPicker Button Clicked!");
        status = Mode.ColorPicker;
    }

    //Click event for ToolBar > CursorButton
    public void ClickedToolBarCursorButton(ActionEvent e) {
        System.out.println("ToolBar Cursor Button Clicked!");
        status = Mode.Cursor;
    }



    //Click event for ToolBar > LineButton
    public void ClickedToolBarLineButton(ActionEvent e) {
        System.out.println("ToolBar Line Button Clicked!");
        status = Mode.Line;
    }

    //Click event for ToolBar > LineButton
    public void ClickedToolBarDashedLineButton(ActionEvent e) {
        System.out.println("ToolBar Dashed Line Button Clicked!");
        status = Mode.DashedLine;
    }

    //Click event for ToolBar > SquareButton
    public void ClickedToolBarSquareButton(ActionEvent e) {
        System.out.println("ToolBar Square Button Clicked!");
        status = Mode.Square;
    }

    //Click event for ToolBar > RectangleButton
    public void ClickedToolBarRectangleButton(ActionEvent e) {
        System.out.println("ToolBar Rectangle Button Clicked!");
        status = Mode.Rectangle;
    }

    //Click event for ToolBar > CircleButton
    public void ClickedToolBarCircleButton(ActionEvent e) {
        System.out.println("ToolBar Circle Button Clicked!");
        status = Mode.Circle;
    }

    //Click event for ToolBar > CircleButton
    public void ClickedToolBarEllipseButton(ActionEvent e) {
        System.out.println("ToolBar Ellipse Button Clicked!");
        status = Mode.Ellipse;
    }
    //END OF TOP TOOLBAR



    //CANVAS DRAW METHODS

    //Canvas Mouse Enter Event for changing cursor to Cross hair
    public void CanvasOnMouseEntered(MouseEvent mouseEvent) {
        System.out.println("Mouse Entered Canvas");

        switch (status) {
            case Cursor:
                canvas.getScene().setCursor(Cursor.DEFAULT);
                break;
            default:
                canvas.getScene().setCursor(Cursor.CROSSHAIR);
                break;
        }
    }

    //Canvas Mouse Enter Event for changing cursor to Default
    public void CanvasOnMouseExited(MouseEvent mouseEvent) {
        System.out.println("Mouse Exited Canvas");
        canvas.getScene().setCursor(Cursor.DEFAULT);
    }

    //Method called when mouse is pressed on canvas - Controls drawing modes
    public void PressedCanvas(javafx.scene.input.MouseEvent mouseEvent) {
        System.out.println("Pressed on Canvas");
        GraphicsContext gc = canvas.getGraphicsContext2D();
        tempCanvas = new Canvas(canvas.getWidth(), canvas.getHeight());
        GraphicsContext tempgc = tempCanvas.getGraphicsContext2D();

        switch (status) {
            case Paint:
                gc.setLineWidth(slider.getValue());
                gc.setStroke(cp.getValue());
                gc.beginPath();
                gc.moveTo(mouseEvent.getX(), mouseEvent.getY());
                gc.stroke();
                break;

            case ColorPicker:
                WritableImage snap = gc.getCanvas().snapshot(null, null);
                cp.setValue(snap.getPixelReader().getColor((int)mouseEvent.getX(), (int)mouseEvent.getY()));

            case Eraser:
                gc.setLineWidth(slider.getValue());
                gc.setStroke(Color.WHITE);
                gc.beginPath();
                gc.moveTo(mouseEvent.getX(), mouseEvent.getY());
                gc.stroke();
                break;

            case Line:
            case DashedLine:
            case Square:
            case Rectangle:
            case Circle:
            case Ellipse:
                tempgc.setFill(Color.TRANSPARENT);
                tempgc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
                tempCanvas.toFront();
                innerAnchorPane.getChildren().add(tempCanvas);
                drawStartX = mouseEvent.getX();
                drawStartY = mouseEvent.getY();
                tempgc.setLineWidth(slider.getValue());
                tempgc.setStroke(cp.getValue());
                break;


            default:
                break;
        }

    }

    //Method called when mouse is dragged on canvas - Controls drawing modes
    public void DraggedCanvas(javafx.scene.input.MouseEvent mouseEvent) {
        System.out.println("Dragged on Canvas");
        GraphicsContext tempgc = tempCanvas.getGraphicsContext2D();


        switch (status) {
            case Paint:
            case Eraser:
                GraphicsContext gc = canvas.getGraphicsContext2D();
                gc.lineTo(mouseEvent.getX(), mouseEvent.getY());
                gc.stroke();
                gc.closePath();
                gc.beginPath();
                gc.moveTo(mouseEvent.getX(), mouseEvent.getY());
                break;

            case Line:
                clearCanvasMethod(tempCanvas);
                tempgc.strokeLine(drawStartX, drawStartY, mouseEvent.getX(), mouseEvent.getY());
                break;

            case DashedLine:
                clearCanvasMethod(tempCanvas);
                tempgc.setLineDashes(5 * slider.getValue());
                tempgc.setLineDashOffset(5);
                tempgc.strokeLine(drawStartX, drawStartY, mouseEvent.getX(), mouseEvent.getY());
                break;

            case Square:
                clearCanvasMethod(tempCanvas);
                tempgc.strokeRect(clamp(Math.min(mouseEvent.getX(), drawStartX),(drawStartX - Math.min(Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY()-drawStartY))),(drawStartX + Math.min(Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY()-drawStartY)))),clamp(Math.min(mouseEvent.getY(), drawStartY),(drawStartY - Math.min(Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY()-drawStartY))),(drawStartY + Math.min(Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY()-drawStartY)))),Math.min(Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY()-drawStartY)),Math.min(Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY()-drawStartY)));
                break;

            case Rectangle:
                clearCanvasMethod(tempCanvas);
                tempgc.strokeRect(Math.min(drawStartX, mouseEvent.getX()), Math.min(drawStartY, mouseEvent.getY()),  Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY() - drawStartY));
                break;

            case Circle:
                clearCanvasMethod(tempCanvas);
                tempgc.strokeOval(clamp(Math.min(mouseEvent.getX(), drawStartX),(drawStartX - Math.min(Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY()-drawStartY))),(drawStartX + Math.min(Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY()-drawStartY)))),clamp(Math.min(mouseEvent.getY(), drawStartY),(drawStartY - Math.min(Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY()-drawStartY))),(drawStartY + Math.min(Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY()-drawStartY)))),Math.min(Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY()-drawStartY)),Math.min(Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY()-drawStartY)));
                break;

            case Ellipse:
                clearCanvasMethod(tempCanvas);
                tempgc.strokeOval(Math.min(drawStartX, mouseEvent.getX()), Math.min(drawStartY, mouseEvent.getY()),  Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY() - drawStartY));
                break;


            default:
                break;
        }
    }

    //Method called when mouse is released from canvas - Controls drawing modes
    public void ReleasedCanvas(javafx.scene.input.MouseEvent mouseEvent) {
        System.out.println("Released on Canvas");
        GraphicsContext gc = canvas.getGraphicsContext2D();

        switch (status) {
            case Paint:
            case Eraser:
                gc.lineTo(mouseEvent.getX(), mouseEvent.getY());
                gc.stroke();
                gc.closePath();
                break;

            case Line:
                innerAnchorPane.getChildren().remove(tempCanvas);
                tempCanvas = null;
                gc.setLineWidth(slider.getValue());
                gc.setStroke(cp.getValue());
                gc.strokeLine(drawStartX, drawStartY, mouseEvent.getX(), mouseEvent.getY());
                break;

            case DashedLine:
                innerAnchorPane.getChildren().remove(tempCanvas);
                tempCanvas = null;
                gc.setLineWidth(slider.getValue());
                gc.setStroke(cp.getValue());
                gc.setLineDashes(5 * slider.getValue());
                gc.setLineDashOffset(5);
                gc.strokeLine(drawStartX, drawStartY, mouseEvent.getX(), mouseEvent.getY());
                gc.setLineDashes(null);
                gc.setLineDashOffset(0.0);
                break;

            case Square:
                innerAnchorPane.getChildren().remove(tempCanvas);
                tempCanvas = null;
                gc.setLineWidth(slider.getValue());
                gc.setStroke(cp.getValue());
                gc.strokeRect(clamp(Math.min(mouseEvent.getX(), drawStartX),(drawStartX - Math.min(Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY()-drawStartY))),(drawStartX + Math.min(Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY()-drawStartY)))),clamp(Math.min(mouseEvent.getY(), drawStartY),(drawStartY - Math.min(Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY()-drawStartY))),(drawStartY + Math.min(Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY()-drawStartY)))),Math.min(Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY()-drawStartY)),Math.min(Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY()-drawStartY)));
                break;


            case Rectangle:
                innerAnchorPane.getChildren().remove(tempCanvas);
                tempCanvas = null;
                gc.setLineWidth(slider.getValue());
                gc.setStroke(cp.getValue());
                gc.strokeRect(Math.min(drawStartX, mouseEvent.getX()), Math.min(drawStartY, mouseEvent.getY()),  Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY() - drawStartY));
                break;

            case Circle:
                innerAnchorPane.getChildren().remove(tempCanvas);
                tempCanvas = null;
                gc.setLineWidth(slider.getValue());
                gc.setStroke(cp.getValue());
                gc.strokeOval(clamp(Math.min(mouseEvent.getX(), drawStartX),(drawStartX - Math.min(Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY()-drawStartY))),(drawStartX + Math.min(Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY()-drawStartY)))),clamp(Math.min(mouseEvent.getY(), drawStartY),(drawStartY - Math.min(Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY()-drawStartY))),(drawStartY + Math.min(Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY()-drawStartY)))),Math.min(Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY()-drawStartY)),Math.min(Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY()-drawStartY)));
                break;


            case Ellipse:
                innerAnchorPane.getChildren().remove(tempCanvas);
                tempCanvas = null;
                gc.setLineWidth(slider.getValue());
                gc.setStroke(cp.getValue());
                gc.strokeOval(Math.min(drawStartX, mouseEvent.getX()), Math.min(drawStartY, mouseEvent.getY()),  Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY() - drawStartY));
                break;

            default:
                break;
        }
    }
    //END OF CANVAS DRAW METHODS



    //HELPER METHODS

    public void SaveMethod() {
        try {
            WritableImage writableImage = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
            canvas.snapshot(null, writableImage);
            RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
            if (savePath.toString().endsWith(".png")) {
                ImageIO.write(renderedImage, "png", savePath);
            }
            if (savePath.toString().endsWith(".jpg")) {
                BufferedImage output = new BufferedImage((int) canvas.getWidth(), (int) canvas.getHeight(), BufferedImage.TYPE_3BYTE_BGR); //do all of this extra stuff to remove transparency
                int px[] = new int[(int) (canvas.getWidth() * canvas.getHeight())];
                ((BufferedImage) renderedImage).getRGB(0, 0, (int) canvas.getWidth(), (int) canvas.getHeight(), px, 0, (int) canvas.getWidth());
                output.setRGB(0, 0, (int) canvas.getWidth(), (int) canvas.getHeight(), px, 0, (int) canvas.getWidth());
                ImageIO.write(output, "jpg", savePath);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("Error!");
        }
    }



    //Method to invoke SaveAs
    public void SaveAsMethod() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save As");
        Stage stage = (Stage) canvas.getScene().getWindow();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PNG file", "*.png"));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JPEG file", "*.jpg"));
        File fileDest = fileChooser.showSaveDialog(stage);
        if (fileDest != null) {
            System.out.println(fileDest);
            try {
                WritableImage writableImage = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
                canvas.snapshot(null, writableImage);
                RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
                if(fileDest.toString().endsWith(".png")){
                    ImageIO.write(renderedImage, "png", fileDest);
                }
                if(fileDest.toString().endsWith(".jpg")){
                    BufferedImage output = new BufferedImage((int) canvas.getWidth(), (int) canvas.getHeight(), BufferedImage.TYPE_3BYTE_BGR); //do all of this extra stuff to remove transparency
                    int px[] = new int[(int) (canvas.getWidth() * canvas.getHeight())];
                    ((BufferedImage) renderedImage).getRGB(0, 0, (int) canvas.getWidth(), (int) canvas.getHeight(), px, 0, (int) canvas.getWidth());
                    output.setRGB(0, 0, (int) canvas.getWidth(), (int) canvas.getHeight(), px, 0, (int) canvas.getWidth());
                    ImageIO.write(output, "jpg", fileDest);
                }
                savePath = fileDest;
            } catch (IOException ex) {
                ex.printStackTrace();
                System.out.println("Error!");
            }
        }
    }

    //Method to clear canvas
    private void clearCanvasMethod(Canvas canvasToClear) {
        canvasToClear.getGraphicsContext2D().clearRect(0, 0, canvasToClear.getWidth(), canvasToClear.getHeight());
    }

    public static double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }
    //END OF HELPER METHODS
}