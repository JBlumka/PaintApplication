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

/**
 * Controller for managing all actions performed by user within PaintApplication
 */
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

    //Number of sides variable for N-polygon tool
    public int numSides;

    //Temporary canvas used for canvas drawing operations while dragging cursor
    Canvas tempCanvas;

    //Mouseevent initial positon parameters
    double drawStartX;
    double drawStartY;

    //Tab parameters
    int tabIterator = 0;

    //Event list for Undo-Redo implementation
    List<Object> drawEventList;

    //Declaration of enumerated modes
    public enum Mode {
        Paint, Eraser, ColorPicker, Cursor, Line, DashedLine, Square, Rectangle, Circle, Ellipse, Triangle, NPolygon, Image, Clear, ResizeWidth, ResizeHeight, Cut_Action
    }

    //Status variable for tracking draw functions
    private Mode status = Mode.Cursor;

    //END OF ATTRIBUTE DECLARATIONS



    //TOP MENU BAR METHODS

    /**
     * Handles the click event for the File > New MenuItem in the MenuBar.
     * <p>
     * This method always executes immediately, when the user clicks the specified MenuItem.
     * This method creates a new Canvas object.
     *
     * @see         Canvas New canvas object
     */
    public void ClickedMenuBar_File_New() {
        System.out.println("File/New Clicked");
        createNewCanvasTab();
    }


    /**
     * Handles the click event for the File > Open MenuItem in the MenuBar.
     * Returns an Image object to be painted on the Canvas.
     * Prompts user to specify image file location with a FileChooser.
     * Event is added to the Undo/Redo stack.
     * <p>
     * This method always returns immediately, regardless of whether the
     * image exists in a usable file location or file type.
     * The applet will then attempt to draw the image on the Canvas,
     * and handle errors appropriately.
     *
     * @see       Image  the image at the specified file location
     */
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

            //Add event to Undo/Redo Stack
            List<Object> drawEventList = new ArrayList<>();
            drawEventList.add(Mode.Image);
            drawEventList.add(selectedFile);
            System.out.println(drawEventList);
            getCurrentCanvas().pushOneToUndoStack(drawEventList);
        }
    }


    /**
     * Handles the click event for the File > Clear MenuItem in the MenuBar.
     * Event is added to the Undo/Redo Stack.
     * <p>
     * This method always executes immediately, clearing the current Canvas.
     *
     * @see         Canvas Cleared canvas object
     */
    public void ClickedMenuBar_File_Clear() {
        System.out.println("File/Clear Clicked");
        clearCanvasMethod(getCurrentCanvas());

        //Add event to Undo/Redo Stack
        drawEventList.add(Mode.Clear);
        System.out.println(drawEventList);
        getCurrentCanvas().pushOneToUndoStack(drawEventList);
    }


    /**
     * Handles the click event for the File > Undo MenuItem in the MenuBar.
     * Undoes the most recently occurred action on the currently selected Canvas object.
     * <p>
     * This method always executes immediately, regardless of whether there are any actions to undo.
     * Errors are handled appropriately.
     * The graphics that draw the Canvas will update to reflect the undo action.
     *
     * @see         Canvas Canvas will reflect change from undoed action
     */
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


    /**
     * Handles the click event for the File > Redo MenuItem in the MenuBar.
     * Redoes the most recent Undo action on the currently selected Canvas object.
     * <p>
     * This method always executes immediately, regardless of whether there are any actions to redo.
     * Errors are handled appropriately.
     * The graphics that draw the Canvas will update to reflect the redo action.
     *
     * @see         Canvas Canvas will reflect change from redoed action
     */
    public void ClickedMenuBar_File_Redo() {
        System.out.println("Clicked REDO");
        if(getCurrentCanvas().redoStack.size()>0) {
            List action = getCurrentCanvas().Redo();
            performAction(action);
        }
    }


    /**
     * Handles the click event for the File > Save MenuItem in the MenuBar.
     * Saves current Canvas object to previously specified file location.
     * <p>
     * This method always executes immediately, regardless of whether the
     * image has been previously saved. If the current Canvas has not been saved previously,
     * the SaveAs function is invoked to prompt user for save file location.
     */
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


    /**
     * Handles the click event for the File > SaveAs MenuItem in the MenuBar.
     * Saves current Canvas object to specified file location.
     * <p>
     * This method always executes immediately, prompting the user for a file location to save the current Canvas object to.
     */
    public void ClickedMenuBar_File_SaveAs() {
        System.out.println("File/SaveAs Clicked");
        SaveAsMethod(getCurrentCanvas());
        Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
        currentTab.setText(getCurrentCanvas().getSavePath().getName());
    }


    /**
     * Handles the click event for the File > SaveAll MenuItem in the MenuBar.
     * Saves all Canvas objects to previously specified file locations.
     * Each Canvas object has an individually assignable file location attribute.
     * <p>
     * This method always executes immediately, regardless of whether the Canvas objects has been previously saved.
     * This method increments through all Canvases, and if a Canvas has not been saved previously,
     * the Canvas to be saved is displayed and the SaveAs function is invoked to prompt user for save file location.
     */
    public void ClickedMenuBar_File_SaveAll() {
        System.out.println("File/SaveAll Clicked");
        saveAllTabs();
    }


    /**
     * Handles the click event for the File > Exit MenuItem in the MenuBar.
     * Prompts user to save all unsaved work before closing application.
     * This save prompt can be bypassed by the user.
     * <p>
     * This method always executes immediately, opening a window event to prompt user to save.
     *
     * @see         WindowEvent Window event prompt to save before closing
     */
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


    /**
     * Handles the click event for the Help > Help MenuItem in the MenuBar.
     * An alert is displayed showing "helpful" information about the application
     * <p>
     * This method always executes immediately, displaying an alert to the user.
     *
     * @see         Alert "helpful" information dialog alert
     */
    public void ClickedMenuBar_Help_Help() {
        System.out.println("Help/About Clicked");

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Helpful Information Dialog");
        alert.setHeaderText(null);
        alert.setContentText("It's a paint application. Figure it out.");
        alert.showAndWait();
    }


    /**
     * Handles the click event for the Help > About MenuItem in the MenuBar.
     * Displays version number and other application information to the user
     * <p>
     * This method always executes immediately, displaying an alert with application
     * information to the user.
     *
     * @see         Alert Application information alert
     */
    public void ClickedMenuBar_Help_About() {
        System.out.println("Help/About Clicked");

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information Dialog");
        alert.setHeaderText(null);
        alert.setContentText("Welcome to Pain(t) Release Version 1 \n\nIcons sourced from https://icons8.com/");
        alert.showAndWait();
    }
    //END OF TOP MENU BAR METHODS



    //TOOLBAR METHODS

    /**
     * Handles the Enter key event for the Width Canvas Dimension TextField in the toolbar.
     * The current canvases Width is changed to reflect the modified width dimension.
     * <p>
     * This method always executes immediately, regardless of whether the
     * width dimension has been changed.
     *
     * @see           Canvas Width dimension of current canvas
     */
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

    /**
     * Handles the Enter key event for the Height Canvas Dimension TextField in the toolbar.
     * The current canvases Height is changed to reflect the modified height dimension.
     * <p>
     * This method always executes immediately, regardless of whether the
     * height dimension has been changed.
     *
     * @see           Canvas Height dimension of current canvas
     */
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


    /**
     * Handles the click event for the Eight button in the Toolbar.
     * Audio of the word "Eight" is played.
     * <p>
     * This method always executes immediately.
     */
    public void ClickedToolBarEightButton() {
        System.out.println("EIGHT");
        Media media = new Media(new File("src/main/resources/com/example/pain_t/eight.wav").toURI().toString());
        MediaPlayer player = new MediaPlayer(media);
        player.play();
    }


    /**
     * Handles the click event for the Paint button in the Toolbar.
     * The painting mode is set to Paint.
     * <p>
     * This method always executes immediately.
     */
    public void ClickedToolBarPaintButton() {
        System.out.println("ToolBar Paint Button Clicked!");
        status = Mode.Paint;
    }


    /**
     * Handles the click event for the Eraser button in the Toolbar.
     * The painting mode is set to Eraser.
     * <p>
     * This method always executes immediately.
     */
    public void ClickedToolBarEraserButton() {
        System.out.println("ToolBar Eraser Button Clicked!");
        status = Mode.Eraser;
    }


    /**
     * Handles the click event for the ColorPicker button in the Toolbar.
     * The painting mode is set to ColorPicker.
     * <p>
     * This method always executes immediately.
     */
    public void ClickedToolBarColorPickerButton() {
        System.out.println("ToolBar ColorPicker Button Clicked!");
        status = Mode.ColorPicker;
    }


    /**
     * Handles the click event for the Cursor button in the Toolbar.
     * The painting mode is set to Cursor.
     * <p>
     * This method always executes immediately.
     */
    public void ClickedToolBarCursorButton() {
        System.out.println("ToolBar Cursor Button Clicked!");
        status = Mode.Cursor;
    }


    /**
     * Handles the click event for the Cut button in the Toolbar.
     * The painting mode is set to Cut_Action.
     * <p>
     * This method always executes immediately.
     */
    public void ClickedToolBarCutButton() {
        System.out.println("ToolBar Cut Button Clicked!");
        status = Mode.Cut_Action;
    }


    /**
     * Handles the click event for the Line button in the Toolbar.
     * The painting mode is set to Line.
     * <p>
     * This method always executes immediately.
     */
    public void ClickedToolBarLineButton() {
        System.out.println("ToolBar Line Button Clicked!");
        status = Mode.Line;
    }


    /**
     * Handles the click event for the Dashed Line button in the Toolbar.
     * The painting mode is set to DashedLine.
     * <p>
     * This method always executes immediately.
     */
    public void ClickedToolBarDashedLineButton() {
        System.out.println("ToolBar Dashed Line Button Clicked!");
        status = Mode.DashedLine;
    }


    /**
     * Handles the click event for the Square button in the Toolbar.
     * The painting mode is set to Square.
     * <p>
     * This method always executes immediately.
     */
    public void ClickedToolBarSquareButton() {
        System.out.println("ToolBar Square Button Clicked!");
        status = Mode.Square;
    }


    /**
     * Handles the click event for the Rectangle button in the Toolbar.
     * The painting mode is set to Rectangle.
     * <p>
     * This method always executes immediately.
     */
    public void ClickedToolBarRectangleButton() {
        System.out.println("ToolBar Rectangle Button Clicked!");
        status = Mode.Rectangle;
    }


    /**
     * Handles the click event for the Circle button in the Toolbar.
     * The painting mode is set to Circle.
     * <p>
     * This method always executes immediately.
     */
    public void ClickedToolBarCircleButton() {
        System.out.println("ToolBar Circle Button Clicked!");
        status = Mode.Circle;
    }


    /**
     * Handles the click event for the Ellipse button in the Toolbar.
     * The painting mode is set to Ellipse.
     * <p>
     * This method always executes immediately.
     */
    public void ClickedToolBarEllipseButton() {
        System.out.println("ToolBar Ellipse Button Clicked!");
        status = Mode.Ellipse;
    }


    /**
     * Handles the click event for the Triangle button in the Toolbar.
     * The painting mode is set to Triangle.
     * <p>
     * This method always executes immediately.
     */
    public void ClickedToolBarTriangleButton() {
        System.out.println("ToolBar Triangle Button Clicked!");
        status = Mode.Triangle;
    }


    /**
     * Handles the click event for the N-Polygon button in the Toolbar.
     * A stage is shown, prompting the user for the number of sides.
     * The number of sides given is clamped to a value between 3 and 25 inclusive.
     * The painting mode is set to NPolygon.
     * <p>
     * This method always executes immediately, displaying a stage to the user.
     *
     * @see         Scene Prompt for number of sides
     */
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
                        numSides = (int) Math.floor(clamp( input, 3.0, 25.0));
                        System.out.println("numSides = " + numSides);
                }
                catch(Exception e) {
                    System.out.println("Invalid input");
                }
                System.out.println("Closed N-Polygon window");
                polyStage.close();
            }
        });

        status = Mode.NPolygon;
    }
    //END OF TOOLBAR METHODS



    //CANVAS DRAW METHODS

    /**
     * Handles the Mouse Enter event within the displayed canvas.
     * Changes the mouse cursor into a Cross-hair while mouse is inside the Canvas.
     * <p>
     * This method always executes immediately.
     *
     * @see Cursor Cross-hair cursor
     */
    public void CanvasOnMouseEntered(MouseEvent mouseEvent) {
        System.out.println("Mouse Entered Canvas");
        if (status == Mode.Cursor) {
            getCurrentCanvas().getScene().setCursor(Cursor.DEFAULT);
        } else {
            getCurrentCanvas().getScene().setCursor(Cursor.CROSSHAIR);
        }
    }


    /**
     * Handles the Mouse Exit event for the displayed canvas.
     * Changes the mouse cursor into a default mouse cursor while mouse is outside the Canvas.
     * <p>
     * This method always executes immediately.
     *
     * @see Cursor Default mouse cursor
     */
    public void CanvasOnMouseExited(MouseEvent mouseEvent) {
        System.out.println("Mouse Exited Canvas");
        getCurrentCanvas().getScene().setCursor(Cursor.DEFAULT);
    }


    /**
     * Handles the Mouse Pressed event for the current canvas.
     * Begins execution of currently selected drawing mode.
     * Saves the starting mouse location and other needed attributes (differs based on drawing mode).
     * <p>
     * This method always executes immediately, regardless of whether
     *  the currently selected drawing mode will modify the current Canvas.
     */
    public void PressedCanvas(javafx.scene.input.MouseEvent mouseEvent) {
        System.out.println("Pressed on Canvas");

        //Add event to Undo/Redo stack
        if(status != Mode.Cursor && status != Mode.ColorPicker && status != Mode.Cut_Action){
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
                if(numSides >=3 && numSides <= 25) {
                    temp_gc.setFill(Color.TRANSPARENT);
                    temp_gc.fillRect(0, 0, getCurrentCanvas().getWidth(), getCurrentCanvas().getHeight());
                    tempCanvas.toFront();
                    ((AnchorPane)getCurrentCanvas().getParent()).getChildren().add(tempCanvas);
                    drawStartX = mouseEvent.getX();
                    drawStartY = mouseEvent.getY();
                    temp_gc.setLineWidth(slider.getValue());
                    temp_gc.setStroke(cp.getValue());
                }
                break;

            case Cut_Action:

                temp_gc.setFill(Color.TRANSPARENT);
                temp_gc.fillRect(0, 0, getCurrentCanvas().getWidth(), getCurrentCanvas().getHeight());
                tempCanvas.toFront();
                ((AnchorPane)getCurrentCanvas().getParent()).getChildren().add(tempCanvas);
                drawStartX = mouseEvent.getX();
                drawStartY = mouseEvent.getY();
                break;

            default:
                break;
        }
    }


    /**
     * Handles the Mouse dragged event for the displayed canvas.
     * Executes the currently selected drawing mode on a temporary canvas
     * in order to show the change dynamically
     * <p>
     * This method always executes immediately, dynamically showing drawing changes
     *
     * @see Canvas Shows drawing mode changes dynamically
     */
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

            case Cut_Action:
                clearCanvasMethod(tempCanvas);
                    temp_gc.setFill(Color.WHITE);
                    temp_gc.fillRect(Math.min(drawStartX, mouseEvent.getX()), Math.min(drawStartY, mouseEvent.getY()),  Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY() - drawStartY));

                break;

            default:
                break;
        }
    }

    /**
     * Handles the Mouse Release event for the displayed canvas.
     * Removes the temporary canvas created in the mouse pressed event,
     * executes the finalized drawing event on the current canvas,
     * and adds the executed drawing event to the Undo/Redo stack
     * <p>
     * This method always executes immediately.
     *
     * @see Canvas Drawing event on current canvas
     */
    public void ReleasedCanvas(MouseEvent mouseEvent) {
        System.out.println("Released on Canvas");
        GraphicsContext gc = getCurrentCanvas().getGraphicsContext2D();

        //UNDO REDO INFORMATION
        if(status != Mode.Cursor && status != Mode.ColorPicker && status != Mode.NPolygon && status != Mode.Cut_Action) {
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
                double[] yPoints = new double[]{drawStartY, drawStartY, drawStartY + 0.5*Math.sqrt(3)*(Math.abs(mouseEvent.getX() - drawStartX)) * (mouseEvent.getY()-drawStartY)/(Math.abs((mouseEvent.getY()-drawStartY)))};
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

            case Cut_Action:

                ((AnchorPane)getCurrentCanvas().getParent()).getChildren().remove(tempCanvas);
                tempCanvas = null;

                gc.setFill(Color.WHITE);
                gc.fillRect(Math.min(drawStartX, mouseEvent.getX()), Math.min(drawStartY, mouseEvent.getY()),  Math.abs(mouseEvent.getX() - drawStartX), Math.abs(mouseEvent.getY() - drawStartY));

                drawEventList = new ArrayList();
                drawEventList.add(status);
                drawEventList.add(Math.min(drawStartX, mouseEvent.getX())); //top left X
                drawEventList.add(Math.min(drawStartY, mouseEvent.getY())); //top left Y
                drawEventList.add(Math.abs(mouseEvent.getX() - drawStartX)); //X length
                drawEventList.add(Math.abs(mouseEvent.getY() - drawStartY)); //Y length
                System.out.println(drawEventList);
                getCurrentCanvas().pushOneToUndoStack(drawEventList);
                break;

            default:
                break;
        }
    }
    //END OF CANVAS DRAW METHODS



    //HELPER METHODS

    /**
     * Helper method for saving the current Canvas to a previously specified file location.
     * <p>
     * This method always executes immediately, regardless of whether the file path still exists.
     *
     * @param  canvas current Canvas object
     */
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


    /**
     * Helper method for executing SaveAs on the current Canvas.
     * Prompts user for save file location with a FileChooser.
     * <p>
     * This method always executes immediately, prompting the user for a save file location.
     *
     * @param  canvas current Canvas object
     * @see FileChooser  File chooser for selecting desired save file location
     */
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


    /**
     * Helper method for clearing the current Canvas.
     * Clears the current canvas.
     * <p>
     * This method always executes immediately, clearing the current canvas.
     */
    private void clearCanvasMethod(Canvas canvasToClear) {
        canvasToClear.getGraphicsContext2D().clearRect(0, 0, canvasToClear.getWidth(), canvasToClear.getHeight());
    }


    /**
     * Helper method for opening a file on a new Canvas object.
     * Executes the File > Open command
     * <p>
     * This method always executes immediately, creating a new Canvas,
     * setting the tab name to the selectedFile filePath name, and displaying the selectedFile on the Canvas.
     *
     * @param  selectedFile  an absolute URL giving the base location of the image
     * @see         Canvas   New canvas named with specified image filename and specified image displayed
     */
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


    /**
     * Helper method for clamping a double input value between a minimum and maximum value.
     * <p>
     * This method returns the result of clamping the double input value (val) between a minimum (min) and maximum (max) value.
     *
     * @param  val  input double value to be clamped
     * @param  min  minimum double value to constrain input value
     * @param  max  maximum double value to constrain input value
     * @return      Clamped value of type double
     */
    public static double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }


    /**
     * Helper method to return the current tab's Canvas.
     * <p>
     * This method returns the currently displayed Canvas object.
     *
     * @return      currently displayed Canvas object
     */
    private CustomCanvas getCurrentCanvas () {
        Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
        ScrollPane currentSP = (ScrollPane) currentTab.getContent();
        AnchorPane currentAP = (AnchorPane) currentSP.getContent();
        return (CustomCanvas) currentAP.getChildren().get(0);
    }

    /**
     * Helper method to create a new Tab with embedded custom Canvas Object.
     * <p>
     * This method creates a new instance of the following hierarchy
     * new Tab -> new ScrollPane -> new AnchorPane -> new custom Canvas
     */
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


    /**
     * Helper method to launch Alert prompting user to save changes to closing Tab object.
     * <p>
     * Displays Alert prompting user to save changes to the closing Tab and Canvas object.
     * Executes SaveAs / Save on closingTab's canvas object if chosen by user.
     *
     * @param  closingTab  Tab object being closed by the user
     * @see    Alert       prompt to save closing Tab/Canvas
     */
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


    /**
     * Helper method to set canvas dimension TextFields to current canvas dimensions
     * <p>
     * This method always modifies the canvas Width TextField and canvas Height TextField
     * to the dimensions of the currently displayed Canvas object.
     */
    void setCanvasDimText() {
        WidthDimTextField.setText(String.valueOf(getCurrentCanvas().getWidth()));
        HeightDimTextField.setText(String.valueOf(getCurrentCanvas().getHeight()));
    }


    /**
     * Helper method to implement File -> Save All
     * <p>
     * Iterates through all tabs and execute SaveAs/Save on each Canvas object.
     */
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

    /**
     * Helper method to interpret and execute action lists saved in the Undo/Redo stack.
     * <p>
     * Performs the action specified by a given drawing action list.
     * Action performed depends on Mode specified in first index of the list.
     *
     * @param  list  an absolute URL giving the base location of the image
     * @see         Canvas drawing action performed on current canvas
     */
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

            case Cut_Action:
                gc.setFill(Color.WHITE);
                gc.fillRect((Double) list.get(1), (Double) list.get(2), (Double) list.get(3), (Double) list.get(4));
                break;

            default:
                break;
        }
    }


    /**
     * Helper method for initializing first canvas tab on program start-up.
     * <p>
     * This method creates a new Canvas tab and sets the canvas dimension TextFields to initialization dimensions.
     *
     * @see         Canvas Initial tab and canvas shown on program start-up
     */
    //Helper method for initialization on start-up
    void startUpMethod() {
        System.out.println("Program Initialization");
        createNewCanvasTab();
        setCanvasDimText();
    }
    //END OF HELPER METHODS
}