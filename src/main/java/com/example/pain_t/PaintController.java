package com.example.pain_t;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class PaintController {

    //ATTRIBUTE DECLARATIONS

    //FXML nodes
    @FXML
    private TextField WidthDimTextField;
    @FXML
    private TextField HeightDimTextField;

    @FXML
    private ColorPicker cp;
    @FXML
    private Slider slider;

    @FXML
    private TabPane tabPane;

    @FXML
    private TextField NumOfSidesTextField;
    public int numSides;

    Canvas tempCanvas;

    //Drawing Parameters
    double drawStartX;
    double drawStartY;

    //Tab parameters
    int tabIterator = 0;

    //Undo-Redo global variable
    List<Object> drawEventList;

    //Enumerated modes
    public enum Mode {
        Paint, Eraser, ColorPicker, Cursor, Line, DashedLine, Square, Rectangle, Circle, Ellipse, Triangle, NPolygon, Image, Clear, ResizeWidth, ResizeHeight
    }
    private Mode status = Mode.Cursor;

    //END OF ATTRIBUTE DECLARATIONS



    //TOP MENU BAR

    //Click event for Menu > File > New
    public void ClickedMenuBar_File_New() {
        System.out.println("File/New Clicked");
        createNewCanvasTab();
    }

    //Click event for Menu > File > Open
    public void ClickedMenuBar_File_Open() {
        System.out.println("File/Open Clicked");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open");
        Stage stage = (Stage) getCurrentCanvas().getScene().getWindow();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("IMAGE FILES", "*.jpg", "*.png", "*.bmp", "*.gif"));
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            System.out.println(selectedFile);
            openImageMethod(selectedFile);

            //UNDO REDO INFORMATION
            List<Object> drawEventList = new ArrayList<>();
            drawEventList.add(Mode.Image);
            drawEventList.add(selectedFile);
            System.out.println(drawEventList);
            getCurrentCanvas().pushOneToUndoStack(drawEventList);
        }
    }

    //Click event for Menu > File > Clear
    public void ClickedMenuBar_File_Clear() {
        System.out.println("File/Clear Clicked");
        clearCanvasMethod(getCurrentCanvas());


        //TODO UNDO/REDO
        List<Object> drawEventList = new ArrayList<>();
        drawEventList.add(Mode.Clear);
        System.out.println(drawEventList);
        getCurrentCanvas().pushOneToUndoStack(drawEventList);

    }

    //Click event for Menu > File > Undo
    public void ClickedMenuBar_File_Undo() {

        System.out.println("Clicked UNDO");
        if (getCurrentCanvas().undoStack.size() > 0) {
            Stack<List> actionStack = getCurrentCanvas().Undo();

            getCurrentCanvas().setWidth(getCurrentCanvas().getInitWidth());
            getCurrentCanvas().setHeight(getCurrentCanvas().getInitHeight());
            clearCanvasMethod(getCurrentCanvas());

            for (List<Object> listAction : actionStack) {
                System.out.println(listAction);
                performAction(listAction);
            }
        }
    }

    //Click event for Menu > File > Redo
    public void ClickedMenuBar_File_Redo() {
        System.out.println("Clicked REDO");
        if(getCurrentCanvas().redoStack.size()>0) {
            List action = getCurrentCanvas().Redo();
            performAction(action);
        }
    }

    //Click event for Menu > File > Save
    public void ClickedMenuBar_File_Save() {
        System.out.println("File/Save Clicked");

        if(getCurrentCanvas().getSavePath() != null){
            System.out.print("Save As already occurred.");
            SaveMethod(getCurrentCanvas());
        } else {
            SaveAsMethod(getCurrentCanvas());
        }
        Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
        currentTab.setText(getCurrentCanvas().getSavePath().getName());
    }

    //Click event for Menu > File > SaveAs
    public void ClickedMenuBar_File_SaveAs() {
        System.out.println("File/SaveAs Clicked");
        SaveAsMethod(getCurrentCanvas());
        Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
        currentTab.setText(getCurrentCanvas().getSavePath().getName());
    }

    public void ClickedMenuBar_File_SaveAll() {
        System.out.println("File/SaveAll Clicked");
        saveAllTabs();
    }

    //Click event for Menu > File > Exit
    public void ClickedMenuBar_File_Exit() {
        System.out.println("File/Exit Clicked");
        Stage stage = (Stage) getCurrentCanvas().getScene().getWindow();
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
        alert.setContentText("Welcome to Pain(t) Version 3.141\n\nIcons sourced from https://icons8.com/");
        alert.showAndWait();
    }
    //END OF TOP MENU BAR



    //TOP TOOLBAR

    //Click event for Canvas Dimension Width text field
    public void WidthDimInputChanged() {
        System.out.println("ToolBar CanvasDim Width Entered");
        double inputWidth = clamp(Double.parseDouble(WidthDimTextField.getCharacters().toString()),1,3000);
        if(inputWidth > 1.0){
            getCurrentCanvas().setWidth(inputWidth);

            //UNDO REDO INFORMATION
            List drawEventList = new ArrayList();
            drawEventList.add(Mode.ResizeWidth);
            drawEventList.add(inputWidth);
            System.out.println(drawEventList);
            getCurrentCanvas().pushOneToUndoStack(drawEventList);
        }
        WidthDimTextField.setText(Double.toString(getCurrentCanvas().getWidth()));
    }

    //Click event for Canvas Dimension Height text field
    public void HeightDimInputChanged() {
        System.out.println("ToolBar CanvasDim Height Entered");
        double inputHeight = clamp(Double.parseDouble(HeightDimTextField.getCharacters().toString()),1,2000);
        if(inputHeight > 1.0){
            getCurrentCanvas().setHeight(inputHeight);

            //UNDO REDO INFORMATION
            List drawEventList = new ArrayList();
            drawEventList.add(Mode.ResizeHeight);
            drawEventList.add(inputHeight);
            System.out.println(drawEventList);
            getCurrentCanvas().pushOneToUndoStack(drawEventList);
        }
        HeightDimTextField.setText(Double.toString(getCurrentCanvas().getHeight()));
    }

    public void ClickedToolBarEightButton() {
        System.out.println("EIGHT");
        Media media = new Media(new File("C:\\Users\\jeb\\Documents\\CS250Code\\Pain(t)\\Pain_t_V1\\src\\main\\resources\\com\\example\\pain_t\\eight.wav").toURI().toString()); //TODO MAKE INTO A RELATIVE PATH
        MediaPlayer player = new MediaPlayer(media);
        player.play();
    }


    //Click event for ToolBar > PaintButton
    public void ClickedToolBarPaintButton() {
        System.out.println("ToolBar Paint Button Clicked!");
        status = Mode.Paint;
    }

    //Click event for ToolBar > EraserButton
    public void ClickedToolBarEraserButton() {
        System.out.println("ToolBar Eraser Button Clicked!");
        status = Mode.Eraser;
    }

    //Click event for ToolBar > ColorPickerButton
    public void ClickedToolBarColorPickerButton() {
        System.out.println("ToolBar ColorPicker Button Clicked!");
        status = Mode.ColorPicker;
    }

    //Click event for ToolBar > CursorButton
    public void ClickedToolBarCursorButton() {
        System.out.println("ToolBar Cursor Button Clicked!");
        status = Mode.Cursor;
    }

    //Click event for ToolBar > LineButton
    public void ClickedToolBarLineButton() {
        System.out.println("ToolBar Line Button Clicked!");
        status = Mode.Line;
    }

    //Click event for ToolBar > LineButton
    public void ClickedToolBarDashedLineButton() {
        System.out.println("ToolBar Dashed Line Button Clicked!");
        status = Mode.DashedLine;
    }

    //Click event for ToolBar > SquareButton
    public void ClickedToolBarSquareButton() {
        System.out.println("ToolBar Square Button Clicked!");
        status = Mode.Square;
    }

    //Click event for ToolBar > RectangleButton
    public void ClickedToolBarRectangleButton() {
        System.out.println("ToolBar Rectangle Button Clicked!");
        status = Mode.Rectangle;
    }

    //Click event for ToolBar > CircleButton
    public void ClickedToolBarCircleButton() {
        System.out.println("ToolBar Circle Button Clicked!");
        status = Mode.Circle;
    }

    //Click event for ToolBar > EllipseButton
    public void ClickedToolBarEllipseButton() {
        System.out.println("ToolBar Ellipse Button Clicked!");
        status = Mode.Ellipse;
    }

    //Click event for ToolBar > TriangleButton
    public void ClickedToolBarTriangleButton() {
        System.out.println("ToolBar Triangle Button Clicked!");
        status = Mode.Triangle;
    }

    //Click event for ToolBar > NPolygonButton
    public void ClickedToolBarNPolygonButton() {
        System.out.println("ToolBar N-Polygon Button Clicked!");

        Label polyLabel = new Label("Number of sides (3 to 25 inclusive):");
        TextField newSceneTextField = new TextField();

        Button polyButton = new Button();
        String polyButtonTitle = "Submit";
        polyButton.setText(polyButtonTitle);

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(polyLabel, 0, 0);
        grid.add(newSceneTextField, 1, 0);
        grid.add(polyButton,0,1);

        int polySceneWidth = 400;
        int polySceneHeight = 200;
        Scene nPolyScene = new Scene(grid, polySceneWidth, polySceneHeight);

        String polyTitle = "N-Polygon Sides";
        Stage polyStage = new Stage();
        polyStage.setTitle(polyTitle);
        polyStage.setScene(nPolyScene);
        polyStage.show();

        polyButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    double input = Double.parseDouble(newSceneTextField.getText());
                        numSides = (int) Math.floor(clamp((Double) input, 3.0, 25.0));
                        System.out.println("numSides = " + numSides);
                }
                catch(Exception e) {
                    System.out.println("Invalid input");
                }
                System.out.println("Closed N-Polygon window");
                polyStage.close();
            }
        });

        status = Mode.NPolygon; //TODO USE numSides only, assuming that it is a valid number, remove all references to temporary textbox
    }
    //END OF TOP TOOLBAR



    //CANVAS DRAW METHODS

    //Canvas Mouse Enter Event for changing cursor to Cross-hair
    public void CanvasOnMouseEntered(MouseEvent mouseEvent) {
        System.out.println("Mouse Entered Canvas");

        if (status == Mode.Cursor) {
            getCurrentCanvas().getScene().setCursor(Cursor.DEFAULT);
        } else {
            getCurrentCanvas().getScene().setCursor(Cursor.CROSSHAIR);
        }
    }

    //Canvas Mouse Enter Event for changing cursor to Default
    public void CanvasOnMouseExited(MouseEvent mouseEvent) {
        System.out.println("Mouse Exited Canvas");
        getCurrentCanvas().getScene().setCursor(Cursor.DEFAULT);
    }

    //Method called when mouse is pressed on canvas - Controls drawing modes
    public void PressedCanvas(javafx.scene.input.MouseEvent mouseEvent) {
        System.out.println("Pressed on Canvas");

        //UNDO REDO INFORMATION
        if(status != Mode.Cursor && status != Mode.ColorPicker){
            //getCurrentCanvas().redoStack = null;
            drawEventList = new ArrayList();
            drawEventList.add(status);
            drawEventList.add(slider.getValue());
            drawEventList.add(cp.getValue());
            drawEventList.add(mouseEvent.getX());
            drawEventList.add(mouseEvent.getY());
        }

        GraphicsContext gc = getCurrentCanvas().getGraphicsContext2D();
        tempCanvas = new Canvas(getCurrentCanvas().getWidth(), getCurrentCanvas().getHeight());
        GraphicsContext temp_gc = tempCanvas.getGraphicsContext2D();

        switch (status) {
            case Paint:
                gc.setLineWidth(slider.getValue());
                gc.setStroke(cp.getValue());
                gc.beginPath();
                gc.moveTo(mouseEvent.getX(), mouseEvent.getY());
                //gc.stroke();
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
            case Triangle:
                temp_gc.setFill(Color.TRANSPARENT);
                temp_gc.fillRect(0, 0, getCurrentCanvas().getWidth(), getCurrentCanvas().getHeight());
                tempCanvas.toFront();
                ((AnchorPane)getCurrentCanvas().getParent()).getChildren().add(tempCanvas);
                drawStartX = mouseEvent.getX();
                drawStartY = mouseEvent.getY();
                temp_gc.setLineWidth(slider.getValue());
                temp_gc.setStroke(cp.getValue());
                break;

            case NPolygon:
                try {
                    numSides = Integer.parseInt(NumOfSidesTextField.getText());
                    if(numSides >=3) {

                        temp_gc.setFill(Color.TRANSPARENT);
                        temp_gc.fillRect(0, 0, getCurrentCanvas().getWidth(), getCurrentCanvas().getHeight());
                        tempCanvas.toFront();
                        ((AnchorPane)getCurrentCanvas().getParent()).getChildren().add(tempCanvas);
                        drawStartX = mouseEvent.getX();
                        drawStartY = mouseEvent.getY();
                        temp_gc.setLineWidth(slider.getValue());
                        temp_gc.setStroke(cp.getValue());
                    }
                }
                catch(Exception e) {
                    System.out.println("Invalid number of sides");
                }
                break;

            default:
                break;
        }
    }

    //Method called when mouse is dragged on canvas - Controls drawing modes
    public void DraggedCanvas(javafx.scene.input.MouseEvent mouseEvent) {
        System.out.println("Dragged on Canvas");
        GraphicsContext temp_gc = tempCanvas.getGraphicsContext2D();

        //UNDO REDO INFORMATION
        if(status == Mode.Paint || status == Mode.Eraser) {
            drawEventList.add(mouseEvent.getX());
            drawEventList.add(mouseEvent.getY());
        }


        switch (status) {
            case Paint:
            case Eraser:
                GraphicsContext gc = getCurrentCanvas().getGraphicsContext2D();
                gc.lineTo(mouseEvent.getX(), mouseEvent.getY());
                gc.stroke();
                gc.closePath();
                gc.beginPath();
                gc.moveTo(mouseEvent.getX(), mouseEvent.getY());
                break;

            case Line:
                clearCanvasMethod(tempCanvas);
                temp_gc.strokeLine(drawStartX, drawStartY, mouseEvent.getX(), mouseEvent.getY());
                break;

            case DashedLine:
                clearCanvasMethod(tempCanvas);
                temp_gc.setLineDashes(5 * slider.getValue());
                temp_gc.setLineDashOffset(5);
                temp_gc.strokeLine(drawStartX, drawStartY, mouseEvent.getX(), mouseEvent.getY());
                break;

            case Square:
                clearCanvasMethod(tempCanvas);
                temp_gc.strokeRect(clamp(Math.min(mouseEvent.getX(), drawStartX),(drawStartX - Math.min(Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY()-drawStartY))),(drawStartX + Math.min(Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY()-drawStartY)))),clamp(Math.min(mouseEvent.getY(), drawStartY),(drawStartY - Math.min(Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY()-drawStartY))),(drawStartY + Math.min(Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY()-drawStartY)))),Math.min(Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY()-drawStartY)),Math.min(Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY()-drawStartY)));
                break;

            case Rectangle:
                clearCanvasMethod(tempCanvas);
                temp_gc.strokeRect(Math.min(drawStartX, mouseEvent.getX()), Math.min(drawStartY, mouseEvent.getY()),  Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY() - drawStartY));
                break;

            case Circle:
                clearCanvasMethod(tempCanvas);
                temp_gc.strokeOval(clamp(Math.min(mouseEvent.getX(), drawStartX),(drawStartX - Math.min(Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY()-drawStartY))),(drawStartX + Math.min(Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY()-drawStartY)))),clamp(Math.min(mouseEvent.getY(), drawStartY),(drawStartY - Math.min(Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY()-drawStartY))),(drawStartY + Math.min(Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY()-drawStartY)))),Math.min(Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY()-drawStartY)),Math.min(Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY()-drawStartY)));
                break;

            case Ellipse:
                clearCanvasMethod(tempCanvas);
                temp_gc.strokeOval(Math.min(drawStartX, mouseEvent.getX()), Math.min(drawStartY, mouseEvent.getY()),  Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY() - drawStartY));
                break;

            case Triangle:
                clearCanvasMethod(tempCanvas);
                double[] xPoints = new double[]{drawStartX, mouseEvent.getX(), drawStartX + 0.5*(mouseEvent.getX() - drawStartX)};
                double[] yPoints = new double[]{drawStartY, drawStartY, drawStartY + 0.5*Math.sqrt(3)*(Math.abs(mouseEvent.getX() - drawStartX)) * (mouseEvent.getY()-drawStartY)/(Math.abs((mouseEvent.getY()-drawStartY)))};
                temp_gc.strokePolygon(xPoints, yPoints,3);
                break;

            case NPolygon:
                try {
                    double[] xPoint = new double[numSides];
                    double[] yPoint = new double[numSides];

                    clearCanvasMethod(tempCanvas);
                    double mag = Math.sqrt(Math.pow(mouseEvent.getX() -drawStartX, 2)+Math.pow(mouseEvent.getY()-drawStartY,2));
                    xPoint[0] = mouseEvent.getX();
                    yPoint[0] = mouseEvent.getY();

                    double angle = Math.atan((mouseEvent.getY()-drawStartY)/(mouseEvent.getX()-drawStartX));
                    if(mouseEvent.getX() < drawStartX) { angle += Math.PI; }
                    for (int i=1; i<numSides; i++){
                        angle+=2*Math.PI/numSides;
                        xPoint[i] = drawStartX + mag * Math.cos(angle);
                        yPoint[i] = drawStartY + mag * Math.sin(angle);
                    }
                    temp_gc.strokePolygon(xPoint, yPoint,numSides);
                }
                catch(Exception ignored) { }
                break;

            default:
                break;
        }
    }

    //Method called when mouse is released from canvas - Controls drawing modes
    public void ReleasedCanvas(javafx.scene.input.MouseEvent mouseEvent) {
        System.out.println("Released on Canvas");
        GraphicsContext gc = getCurrentCanvas().getGraphicsContext2D();

        //UNDO REDO INFORMATION
        if(status != Mode.Cursor && status != Mode.ColorPicker && status != Mode.NPolygon) {
            drawEventList.add(mouseEvent.getX());
            drawEventList.add(mouseEvent.getY());
            System.out.println(drawEventList);
            getCurrentCanvas().pushOneToUndoStack(drawEventList);
        }

        switch (status) {
            case Paint:
            case Eraser:
                gc.lineTo(mouseEvent.getX(), mouseEvent.getY());
                gc.stroke();
                gc.closePath();
                break;

            case Line:
                ((AnchorPane)getCurrentCanvas().getParent()).getChildren().remove(tempCanvas);
                tempCanvas = null;
                gc.setLineWidth(slider.getValue());
                gc.setStroke(cp.getValue());
                gc.strokeLine(drawStartX, drawStartY, mouseEvent.getX(), mouseEvent.getY());
                break;

            case DashedLine:
                ((AnchorPane)getCurrentCanvas().getParent()).getChildren().remove(tempCanvas);
                tempCanvas = null;
                gc.setLineWidth(slider.getValue());
                gc.setStroke(cp.getValue());
                gc.setLineDashes(5 * slider.getValue());
                gc.setLineDashOffset(5);
                gc.strokeLine(drawStartX, drawStartY, mouseEvent.getX(), mouseEvent.getY());
                gc.setLineDashes(0.0);
                gc.setLineDashOffset(0.0);
                break;

            case Square:
                ((AnchorPane)getCurrentCanvas().getParent()).getChildren().remove(tempCanvas);
                tempCanvas = null;
                gc.setLineWidth(slider.getValue());
                gc.setStroke(cp.getValue());
                //double length = define
                gc.strokeRect(clamp(Math.min(mouseEvent.getX(), drawStartX),(drawStartX - Math.min(Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY()-drawStartY))),(drawStartX + Math.min(Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY()-drawStartY)))),clamp(Math.min(mouseEvent.getY(), drawStartY),(drawStartY - Math.min(Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY()-drawStartY))),(drawStartY + Math.min(Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY()-drawStartY)))),Math.min(Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY()-drawStartY)),Math.min(Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY()-drawStartY)));
                break;


            case Rectangle:
                ((AnchorPane)getCurrentCanvas().getParent()).getChildren().remove(tempCanvas);
                tempCanvas = null;
                gc.setLineWidth(slider.getValue());
                gc.setStroke(cp.getValue());
                gc.strokeRect(Math.min(drawStartX, mouseEvent.getX()), Math.min(drawStartY, mouseEvent.getY()),  Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY() - drawStartY));
                break;

            case Circle:
                ((AnchorPane)getCurrentCanvas().getParent()).getChildren().remove(tempCanvas);
                tempCanvas = null;
                gc.setLineWidth(slider.getValue());
                gc.setStroke(cp.getValue());
                gc.strokeOval(clamp(Math.min(mouseEvent.getX(), drawStartX),(drawStartX - Math.min(Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY()-drawStartY))),(drawStartX + Math.min(Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY()-drawStartY)))),clamp(Math.min(mouseEvent.getY(), drawStartY),(drawStartY - Math.min(Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY()-drawStartY))),(drawStartY + Math.min(Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY()-drawStartY)))),Math.min(Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY()-drawStartY)),Math.min(Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY()-drawStartY)));
                break;


            case Ellipse:
                ((AnchorPane)getCurrentCanvas().getParent()).getChildren().remove(tempCanvas);
                tempCanvas = null;
                gc.setLineWidth(slider.getValue());
                gc.setStroke(cp.getValue());
                gc.strokeOval(Math.min(drawStartX, mouseEvent.getX()), Math.min(drawStartY, mouseEvent.getY()),  Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY() - drawStartY));
                break;

            case Triangle:
                ((AnchorPane)getCurrentCanvas().getParent()).getChildren().remove(tempCanvas);
                tempCanvas = null;
                gc.setLineWidth(slider.getValue());
                gc.setStroke(cp.getValue());
                double[] xPoints = new double[]{drawStartX, mouseEvent.getX(), drawStartX + 0.5*(mouseEvent.getX() - drawStartX)};
                double[] yPoints = new double[]{drawStartY, drawStartY, (Double) drawStartY + 0.5*Math.sqrt(3)*(Math.abs(mouseEvent.getX() - drawStartX)) * (mouseEvent.getY()-drawStartY)/(Math.abs((mouseEvent.getY()-drawStartY)))};
                gc.strokePolygon(xPoints, yPoints,3);
                break;

            case NPolygon:
                try {
                    double[] xPoint = new double[numSides];
                    double[] yPoint = new double[numSides];

                    ((AnchorPane)getCurrentCanvas().getParent()).getChildren().remove(tempCanvas);
                    tempCanvas = null;
                    gc.setLineWidth(slider.getValue());
                    gc.setStroke(cp.getValue());

                    double mag = Math.sqrt(Math.pow(mouseEvent.getX() -drawStartX, 2)+Math.pow(mouseEvent.getY()-drawStartY,2));
                    xPoint[0] = mouseEvent.getX();
                    yPoint[0] = mouseEvent.getY();

                    double angle = Math.atan((mouseEvent.getY()-drawStartY)/(mouseEvent.getX()-drawStartX));
                    if(mouseEvent.getX() < drawStartX) { angle += Math.PI; }
                    for (int i=1; i<numSides; i++){
                        angle+=2*Math.PI/numSides;
                        xPoint[i] = drawStartX + mag * Math.cos(angle);
                        yPoint[i] = drawStartY + mag * Math.sin(angle);
                    }
                    gc.strokePolygon(xPoint, yPoint,numSides);

                    //UNDO REDO INFORMATION
                    drawEventList.add(mouseEvent.getX());
                    drawEventList.add(mouseEvent.getY());
                    drawEventList.add(numSides);
                    System.out.println(drawEventList);
                    getCurrentCanvas().pushOneToUndoStack(drawEventList);
                }
                catch(Exception ignored) { }
                break;

            default:
                break;
        }
    }
    //END OF CANVAS DRAW METHODS



    //HELPER METHODS

    //Helper method for saving a canvas
    public void SaveMethod(CustomCanvas canvas) {
        try {
            WritableImage writableImage = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
            canvas.snapshot(null, writableImage);
            BufferedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
            if (canvas.getSavePath().toString().endsWith(".png")) {
                ImageIO.write(renderedImage, "png", canvas.getSavePath());
            }
            if (canvas.getSavePath().toString().endsWith(".bmp")) {
                BufferedImage output = new BufferedImage((int) canvas.getWidth(), (int) canvas.getHeight(), BufferedImage.TYPE_INT_RGB); //do all of this extra stuff to remove transparency
                int[] px = new int[(int) (canvas.getWidth() * canvas.getHeight())];
                renderedImage.getRGB(0, 0, (int) canvas.getWidth(), (int) canvas.getHeight(), px, 0, (int) canvas.getWidth());
                output.setRGB(0, 0, (int) canvas.getWidth(), (int) canvas.getHeight(), px, 0, (int) canvas.getWidth());
                ImageIO.write(output, "bmp", canvas.getSavePath());
            }
            if (canvas.getSavePath().toString().endsWith(".jpg")) {
                BufferedImage output = new BufferedImage((int) canvas.getWidth(), (int) canvas.getHeight(), BufferedImage.TYPE_3BYTE_BGR); //do all of this extra stuff to remove transparency
                int[] px = new int[(int) (canvas.getWidth() * canvas.getHeight())];
                renderedImage.getRGB(0, 0, (int) canvas.getWidth(), (int) canvas.getHeight(), px, 0, (int) canvas.getWidth());
                output.setRGB(0, 0, (int) canvas.getWidth(), (int) canvas.getHeight(), px, 0, (int) canvas.getWidth());
                ImageIO.write(output, "jpg", canvas.getSavePath());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("Error!");
        }
    }



    //Helper method to invoke SaveAs
    public void SaveAsMethod(CustomCanvas canvas) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save As");
        Stage stage = (Stage) canvas.getScene().getWindow();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PNG file", "*.png"));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JPEG file", "*.jpg"));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("BMP file", "*.bmp"));
        File fileDest = fileChooser.showSaveDialog(stage);
        if (fileDest != null) {
            System.out.println(fileDest);
            try {
                WritableImage writableImage = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
                canvas.snapshot(null, writableImage);
                BufferedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
                if(fileDest.toString().endsWith(".png")){
                    ImageIO.write(renderedImage, "png", fileDest);
                }
                if(fileDest.toString().endsWith(".bmp")){
                    BufferedImage output = new BufferedImage((int) canvas.getWidth(), (int) canvas.getHeight(), BufferedImage.TYPE_INT_RGB); //do all of this extra stuff to remove transparency
                    int[] px = new int[(int) (canvas.getWidth() * canvas.getHeight())];
                    renderedImage.getRGB(0, 0, (int) canvas.getWidth(), (int) canvas.getHeight(), px, 0, (int) canvas.getWidth());
                    output.setRGB(0, 0, (int) canvas.getWidth(), (int) canvas.getHeight(), px, 0, (int) canvas.getWidth());
                    ImageIO.write(output, "bmp", canvas.getSavePath());
                }
                if(fileDest.toString().endsWith(".jpg")){
                    BufferedImage output = new BufferedImage((int) canvas.getWidth(), (int) canvas.getHeight(), BufferedImage.TYPE_3BYTE_BGR); //do all of this extra stuff to remove transparency
                    int[] px = new int[(int) (canvas.getWidth() * canvas.getHeight())];
                    renderedImage.getRGB(0, 0, (int) canvas.getWidth(), (int) canvas.getHeight(), px, 0, (int) canvas.getWidth());
                    output.setRGB(0, 0, (int) canvas.getWidth(), (int) canvas.getHeight(), px, 0, (int) canvas.getWidth());
                    ImageIO.write(output, "jpg", fileDest);
                }
                canvas.setSavePath(fileDest);
            } catch (IOException ex) {
                ex.printStackTrace();
                System.out.println("Error!");
            }
        }
    }

    //Helper method to clear canvas
    private void clearCanvasMethod(Canvas canvasToClear) {
        canvasToClear.getGraphicsContext2D().clearRect(0, 0, canvasToClear.getWidth(), canvasToClear.getHeight());
    }

    private void openImageMethod(File selectedFile) {
        getCurrentCanvas().setSavePath(selectedFile);
        clearCanvasMethod(getCurrentCanvas());
        Image image = new Image(selectedFile.toURI().toString());
        getCurrentCanvas().setWidth(image.getWidth());
        getCurrentCanvas().setHeight(image.getHeight());
        setCanvasDimText();
        GraphicsContext gc = getCurrentCanvas().getGraphicsContext2D();
        gc.drawImage(image, 0, 0);
        Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
        currentTab.setText(getCurrentCanvas().getSavePath().getName());
    }

    //Helper method to clamp value between a minimum and maximum (for circle/square tools)
    public static double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }

    //Helper method to obtain the current tab's canvas
    private CustomCanvas getCurrentCanvas () {
        Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
        ScrollPane currentSP = (ScrollPane) currentTab.getContent();
        AnchorPane currentAP = (AnchorPane) currentSP.getContent();
        return (CustomCanvas) currentAP.getChildren().get(0);
    }

    //Helper method to create a new tab
    void createNewCanvasTab() {
        Tab newTab = new Tab();
        newTab.setText("Untitled " + tabIterator);
        tabIterator++;
        tabPane.getTabs().add(newTab);

        //Override Close Request to launch custom close dialog window
        newTab.setOnCloseRequest(e -> {
            newTab.getTabPane().setTabDragPolicy(TabPane.TabDragPolicy.FIXED);
            //prevent window from closing
            e.consume();
            // execute own shutdown procedure
            launchTabCloseConfirmDialog(newTab);
        });

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setPrefWidth(1);
        scrollPane.setPrefHeight(1);
        scrollPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("scrollpaneStyle.css")).toExternalForm());
        newTab.setContent(scrollPane);

        AnchorPane newInnerAnchorPane = new AnchorPane();
        newInnerAnchorPane.setPrefWidth(1);
        newInnerAnchorPane.setPrefHeight(1);
        newInnerAnchorPane.setStyle("-fx-background-color: white;");
        scrollPane.setContent(newInnerAnchorPane);


        CustomCanvas newCanvas = new CustomCanvas(Math.round(scrollPane.getScene().getWindow().getWidth()), Math.round(scrollPane.getScene().getWindow().getHeight() - 96));
        newCanvas.setOnMouseEntered(this::CanvasOnMouseEntered);
        newCanvas.setOnMouseExited(this::CanvasOnMouseExited);
        newCanvas.setOnMousePressed(this::PressedCanvas);
        newCanvas.setOnMouseDragged(this::DraggedCanvas);
        newCanvas.setOnMouseReleased(this::ReleasedCanvas);
        newCanvas.toFront();
        newInnerAnchorPane.getChildren().add(newCanvas);

        tabPane.getSelectionModel().select(newTab); //Shows the new tab as the current view

        setCanvasDimText();

    }

    //Helper method to launch closing dialog on closed tabs
    private void launchTabCloseConfirmDialog(Tab closingTab) {
        Alert alert = new Alert(Alert.AlertType.NONE, "Do you want to save changes to this tab before closing?\n", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
        alert.setTitle("Pain(t)");
        Optional<ButtonType> optionClicked = alert.showAndWait();
        if(optionClicked.isPresent()){
            if(optionClicked.get().equals(ButtonType.YES)) {
                CustomCanvas canvasToSave = ((CustomCanvas)((AnchorPane)((ScrollPane)closingTab.getContent()).getContent()).getChildren().get(0));
                if(canvasToSave.getSavePath() != null){
                    System.out.print("Save As already occurred.");
                    SaveMethod(canvasToSave);
                } else {
                    tabPane.getSelectionModel().select(closingTab);
                    SaveAsMethod(canvasToSave);
                }
                System.out.println("Clicked YES");
                tabPane.getTabs().remove(closingTab);

            } else if (optionClicked.get().equals(ButtonType.NO)) {
                System.out.println("Clicked NO");
                tabPane.getTabs().remove(closingTab);
            } else {
                System.out.println("Clicked Cancel");
            }
        }

    }

    //Helper method to set canvas dimension text fields
    void setCanvasDimText() {
        WidthDimTextField.setText(String.valueOf(getCurrentCanvas().getWidth()));
        HeightDimTextField.setText(String.valueOf(getCurrentCanvas().getHeight()));
    }

    //Helper method to save all tabs
    void saveAllTabs() {
        for(Tab i: tabPane.getTabs()){
            CustomCanvas canvasToSave = ((CustomCanvas)((AnchorPane)((ScrollPane)i.getContent()).getContent()).getChildren().get(0));
            if(canvasToSave.getSavePath() != null){
                System.out.print("Save As already occurred.");
                SaveMethod(canvasToSave);
            } else {
                tabPane.getSelectionModel().select(i);
                SaveAsMethod(canvasToSave);
            }
        }
    }

    //Helper method to interpret action lists for Undo/Redo

    void performAction(List list) {
        GraphicsContext gc = getCurrentCanvas().getGraphicsContext2D();
        Mode actionMode = (Mode) list.get(0);

        switch  (actionMode) {

            case Paint:
                gc.setLineWidth((Double) list.get(1));
                gc.setStroke((Paint) list.get(2));
                //Iterates through each point to freehand draw
                for(int i=3; i<list.size()-2; i+=2){
                    gc.beginPath();
                    gc.moveTo((Double) list.get(i), (Double) list.get(i+1));
                    gc.lineTo((Double) list.get(i+2), (Double) list.get(i+3));
                    gc.stroke();
                    gc.closePath();
                }
                break;

            case Eraser:
                gc.setLineWidth((Double) list.get(1));
                gc.setStroke(Color.WHITE);
                //Iterates through each point to freehand erase
                for(int i=3; i<list.size()-2; i+=2){
                    gc.beginPath();
                    gc.moveTo((Double) list.get(i), (Double) list.get(i+1));
                    gc.lineTo((Double) list.get(i+2), (Double) list.get(i+3));
                    gc.stroke();
                    gc.closePath();
                }
                break;

            case Line:
                gc.setLineWidth((Double) list.get(1));
                gc.setStroke((Paint) list.get(2));
                gc.strokeLine((Double) list.get(3), (Double) list.get(4), (Double) list.get(5), (Double) list.get(6));
                break;

            case DashedLine:
                gc.setLineWidth((Double) list.get(1));
                gc.setStroke((Paint) list.get(2));
                gc.setLineDashes(5 * (Double) list.get(1));
                gc.setLineDashOffset(5);
                gc.strokeLine((Double) list.get(3), (Double) list.get(4), (Double) list.get(5), (Double) list.get(6));
                gc.setLineDashes(0.0);
                gc.setLineDashOffset(0.0);
                break;

            case Square:
                gc.setLineWidth((Double) list.get(1));
                gc.setStroke((Paint) list.get(2));
                gc.strokeRect(clamp(Math.min((Double) list.get(5), (Double) list.get(3)),((Double) list.get(3) - Math.min(Math.abs((Double) list.get(5) - (Double) list.get(3)), Math.abs((Double) list.get(6)-(Double) list.get(4)))),((Double) list.get(3) + Math.min(Math.abs((Double) list.get(5) - (Double) list.get(3)), Math.abs((Double) list.get(6) - (Double) list.get(4))))),clamp(Math.min((Double) list.get(6), (Double) list.get(4)),((Double) list.get(4) - Math.min(Math.abs((Double) list.get(5) - (Double) list.get(3)), Math.abs((Double) list.get(6) - (Double) list.get(4)))),((Double) list.get(4) + Math.min(Math.abs((Double) list.get(5) - (Double) list.get(3)), Math.abs((Double) list.get(6) - (Double) list.get(4))))),Math.min(Math.abs((Double) list.get(5) - (Double) list.get(3)), Math.abs((Double) list.get(6) - (Double) list.get(4))),Math.min(Math.abs((Double) list.get(5) - (Double) list.get(3)), Math.abs((Double) list.get(6) - (Double) list.get(4))));
                break;

            case Rectangle:
                gc.setLineWidth((Double) list.get(1));
                gc.setStroke((Paint) list.get(2));
                gc.strokeRect(Math.min((Double) list.get(3), (Double) list.get(5)), Math.min((Double) list.get(4), (Double) list.get(6)),  Math.abs((Double) list.get(5) - (Double) list.get(3)), Math.abs((Double) list.get(6) - (Double) list.get(4)));
                break;

            case Circle:
                gc.setLineWidth((Double) list.get(1));
                gc.setStroke((Paint) list.get(2));
                gc.strokeOval(clamp(Math.min((Double) list.get(5), (Double) list.get(3)),((Double) list.get(3) - Math.min(Math.abs((Double) list.get(5) - (Double) list.get(3)), Math.abs((Double) list.get(6)-(Double) list.get(4)))),((Double) list.get(3) + Math.min(Math.abs((Double) list.get(5) - (Double) list.get(3)), Math.abs((Double) list.get(6) - (Double) list.get(4))))),clamp(Math.min((Double) list.get(6), (Double) list.get(4)),((Double) list.get(4) - Math.min(Math.abs((Double) list.get(5) - (Double) list.get(3)), Math.abs((Double) list.get(6) - (Double) list.get(4)))),((Double) list.get(4) + Math.min(Math.abs((Double) list.get(5) - (Double) list.get(3)), Math.abs((Double) list.get(6) - (Double) list.get(4))))),Math.min(Math.abs((Double) list.get(5) - (Double) list.get(3)), Math.abs((Double) list.get(6) - (Double) list.get(4))),Math.min(Math.abs((Double) list.get(5) - (Double) list.get(3)), Math.abs((Double) list.get(6) - (Double) list.get(4))));
                break;

            case Ellipse:
                gc.setLineWidth((Double) list.get(1));
                gc.setStroke((Paint) list.get(2));
                gc.strokeOval(Math.min((Double) list.get(3), (Double) list.get(5)), Math.min((Double) list.get(4), (Double) list.get(6)),  Math.abs((Double) list.get(5) - (Double) list.get(3)), Math.abs((Double) list.get(6) - (Double) list.get(4)));
                break;

            case Triangle:
                gc.setLineWidth((Double) list.get(1));
                gc.setStroke((Paint) list.get(2));
                double[] xPoints = new double[]{(Double) list.get(3), (Double) list.get(5), (Double) list.get(3) + 0.5*((Double) list.get(5) - (Double) list.get(3))};
                double[] yPoints = new double[]{(Double) list.get(4), (Double) list.get(4), (Double) list.get(4) + 0.5*Math.sqrt(3)*(Math.abs((Double) list.get(5) - (Double) list.get(3))) * ((Double) list.get(6)-(Double) list.get(4))/(Math.abs(((Double) list.get(6)-(Double) list.get(4))))};
                gc.strokePolygon(xPoints, yPoints,3);
                break;

            case NPolygon:
                gc.setLineWidth((Double) list.get(1));
                gc.setStroke((Paint) list.get(2));

                double mag = Math.sqrt(Math.pow((Double) list.get(5) - (Double) list.get(3), 2) + Math.pow((Double) list.get(6) - (Double) list.get(4),2));
                double[] xPoint = new double[(Integer) list.get(7)];
                double[] yPoint = new double[(Integer) list.get(7)];
                xPoint[0] = (Double) list.get(5);
                yPoint[0] = (Double) list.get(6);

                double angle = Math.atan(((Double) list.get(6)-(Double) list.get(4))/((Double) list.get(5)-(Double) list.get(3)));
                if((Double) list.get(5) < (Double) list.get(3)) { angle += Math.PI; }
                for (int i=1; i<(Integer) list.get(7); i++){
                    angle+=2*Math.PI/(Integer) list.get(7);
                    xPoint[i] = (Double) list.get(3) + mag * Math.cos(angle);
                    yPoint[i] = (Double) list.get(4) + mag * Math.sin(angle);
                }
                gc.strokePolygon(xPoint, yPoint,(Integer) list.get(7));
                break;

            case Image:
                openImageMethod((File) list.get(1));
                break;

            case Clear:
                clearCanvasMethod(getCurrentCanvas());
                break;

            case ResizeWidth:
                getCurrentCanvas().setWidth((Double) list.get(1));
                break;

            case ResizeHeight:
                getCurrentCanvas().setHeight((Double) list.get(1));
                break;

            default:
                break;
        }
    }



    //Helper method for initialization on start-up
    void startUpMethod() {
        System.out.println("Hello World!");

        createNewCanvasTab();
        setCanvasDimText();
    }
    //END OF HELPER METHODS
}
//TODO ADD CHECK THAT REDUNDANT ACTIONS ARE NOT PUT ON UNDO STACK (Like a rectangle with size of zero)