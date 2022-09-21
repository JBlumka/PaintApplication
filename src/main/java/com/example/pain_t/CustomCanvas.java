package com.example.pain_t;

import javafx.scene.canvas.Canvas;

import java.io.File;

public class CustomCanvas extends Canvas {
    private File savePath;

    public CustomCanvas(double width, double height) {
        this.setWidth(width);
        this.setHeight(height);
    }

    public File getSavePath() {
        return savePath;
    }

    public void setSavePath(File givenPath){
        savePath = givenPath;
    }
}