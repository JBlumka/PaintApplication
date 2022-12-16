package com.example.pain_t;

import javafx.scene.canvas.Canvas;

import java.io.File;
import java.util.List;
import java.util.Stack;

/**
 * Extension of Canvas class to attach additional attributes and methods.
 */
public class CustomCanvas extends Canvas {

    //Custom Attributes
    private File savePath;
    private double initHeight;
    private double initWidth;
    public Stack<List> undoStack = new Stack<>();
    public Stack<List> redoStack = new Stack<>();

    //Constructor
    public CustomCanvas(double width, double height) {
        this.setWidth(width);
        initWidth = width;
        this.setHeight(height);
        initHeight = height;
    }

    /**
     * Getter for initWidth
     */
    public double getInitWidth() {
        return initWidth;
    }

    /**
     * Getter for initHeight
     */
    public double getInitHeight() {
        return initHeight;
    }

    /**
     * Getter for savePath
     */
    public File getSavePath() {
        return savePath;
    }

    /**
     * Setter for savePath
     */
    public void setSavePath(File givenPath){
        savePath = givenPath;
    }

    /**
     * Method to add drawing action list to Canvas Undo stack
     */
    public void pushOneToUndoStack(List action) {
        try{
            undoStack.push(action);
            clearRedoStack();
        } catch(Exception e) { System.out.println("Error occurred while pushing to Undo stack");}
    }

    /**
     * Method for clearing the Redo stack
     */
    public void clearRedoStack() { redoStack.clear();}

    /**
     * Method for executing Undo() command.
     * Pops the top drawing action from Undo stack, and pushes it onto the Redo stack.
     */
    public Stack<List> Undo() {
            System.out.println("Pushed to redoStack: " + redoStack.push(undoStack.pop()));

        return undoStack;
    }

    /**
     * Method for executing Redo() command.
     * Pops the top drawing action from Redo stack, and pushes it onto the Undo stack.
     */
    public List Redo() {
        List returnAction = redoStack.pop();
        System.out.println("Pushed to undoStack: " + undoStack.push(returnAction));

        return returnAction;
    }
}