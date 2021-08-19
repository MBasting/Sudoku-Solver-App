package com.sudoku.calc;

import org.opencv.core.Mat;

public class Isolated_Number {
    int x;
    int y;
    Mat roi;

    public Isolated_Number(int x, int y, Mat roi) {
        this.x = x;
        this.y = y;
        this.roi = roi;
    }
}
