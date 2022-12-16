package com.example.pain_t;

import javafx.scene.canvas.Canvas;

import java.io.File;
import java.util.List;
import java.util.Stack;

//Custom canvas class to attach attributes to canvas
public class CustomCanvas extends Canvas {
    private File savePath;

    private double initHeight;
    private double initWidth;
    public Stack<List> undoStack = new Stack<>();
    public Stack<List> redoStack = new Stack<>();

    public CustomCanvas(double width, double height) {
        this.setWidth(width);
        initWidth = width;
        this.setHeight(height);
        initHeight = height;
    }

    public double getInitWidth() {
        return initWidth;
    }

    public double getInitHeight() {
        return initHeight;
    }
    //TODO RETURN AN OBJECT THAT WILL SET INITIAL WIDTH AND HEIGHT OF CANVAS BEFORE DOING UNDO/STACK

    public File getSavePath() {
        return savePath;
    }

    public void setSavePath(File givenPath){
        savePath = givenPath;
    }

    public void pushOneToUndoStack(List action) {
        try{
            undoStack.push(action);
            clearRedoStack();
        } catch(Exception e) { System.out.println("Error occurred while pushing to Undo stack");}
    }

    public void clearRedoStack() { redoStack.clear();}

    public Stack<List> Undo() {
            System.out.println("Pushed to redoStack: " + redoStack.push(undoStack.pop()));

        return undoStack;
    }

    public List Redo() {
        List returnAction = redoStack.pop();
        System.out.println("Pushed to undoStack: " + undoStack.push(returnAction));

        return returnAction;
    }
}