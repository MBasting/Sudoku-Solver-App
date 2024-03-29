package com.example.sudoku;


import com.example.sudoku.calc.Solver;

import org.junit.Test;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;

import static com.example.sudoku.calc.Sudoku_Scanner.prepare;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class SolverTest {

    @Test
    public void solve0() {
        int[][] sudoku = new int[9][9];
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                sudoku[i][j]=0;
            }
        }
        sudoku[0][2] = 4;
        sudoku[0][7] = 5;
        sudoku[0][8] = 2;


        sudoku[1][2] = 7;
        sudoku[1][5] = 6;
        sudoku[1][7] = 9;
        sudoku[1][8] = 8;

        sudoku[2][0] = 8;
        sudoku[2][3] = 1;
        sudoku[2][4] = 3;
        sudoku[2][6] = 4;
        sudoku[2][8] = 6;
        sudoku[3][0] = 7;
        sudoku[3][3] = 5;
        sudoku[3][5] = 1;
        sudoku[4][1] = 6;
        sudoku[4][8] = 4;
        sudoku[5][0] = 9;
        sudoku[5][5] = 3;
        sudoku[5][6] = 2;
        sudoku[5][7] = 8;
        sudoku[6][6] = 5;
        sudoku[6][8] = 9;
        sudoku[7][4] = 5;
        sudoku[8][0] = 5;
        sudoku[8][1] = 8;
        sudoku[8][4] = 9;
        int[][] sud = Solver.solve(sudoku);
        String sol = "[[6, 3, 4, 8, 7, 9, 1, 5, 2], [1, 5, 7, 4, 2, 6, 3, 9, 8], [8, 9, 2, 1, 3, 5, 4, 7, 6], [7, 2, 8, 5, 4, 1, 9, 6, 3], [3, 6, 5, 9, 8, 2, 7, 1, 4], [9, 4, 1, 7, 6, 3, 2, 8, 5], [4, 7, 6, 3, 1, 8, 5, 2, 9], [2, 1, 9, 6, 5, 4, 8, 3, 7], [5, 8, 3, 2, 9, 7, 6, 4, 1]]";
        assertEquals(sol, Arrays.deepToString(sud));
    }

    @Test
    public void solv1() throws FileNotFoundException {
        File file = new File("src/test/java/com/example/cameracodeexample/sudoku1.txt");
        sudoku_pair sd = getSudokufromTXT(file);
        int[][] sud = Solver.solve(sd.tobe_solved);
        assertTrue(Arrays.deepEquals(sud, sd.solved));
    }

    @Test
    public void solv2() throws FileNotFoundException {
        File file = new File("src/test/java/com/example/cameracodeexample/sudoku2.txt");
        sudoku_pair sd = getSudokufromTXT(file);
        int[][] sud = Solver.solve(sd.tobe_solved);
        assertTrue(Arrays.deepEquals(sud, sd.solved));
    }

    @Test
    public void prepare1() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Mat img = Imgcodecs.imread("src\\test\\java\\com\\example\\cameracodeexample\\test_image.jpg");
        System.out.println("oka");
        prepare(img);
    }

    sudoku_pair getSudokufromTXT(File file) throws FileNotFoundException {
        int[][] sudoku = new int[9][9];
        Scanner sc = new Scanner(file);
        sc.next();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                sudoku[i][j] = Integer.parseInt(sc.next());
                sc.next();
            }
            sc.next();
        }
        int[][] solved = new int[9][9];
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                solved[i][j] = Integer.parseInt(sc.next());
                sc.next();
            }
            if (i != 8) {
                sc.next();
            }
        }
        System.out.println(solved.toString());
        sudoku_pair sd = new sudoku_pair(sudoku, solved);
        return sd;
    }

    //    /** Debug function
//     *
//     * @param img Mat to be displayed.
//     */
//    public static void showImage(Mat img) {
//        try {
//            MatOfByte mat = new MatOfByte();
//            Imgcodecs.imencode(".jpg", img, mat);
//            byte[] byteArray = mat.toArray();
//            InputStream in = new ByteArrayInputStream(byteArray);
//            BufferedImage buf = ImageIO.read(in);
//            JFrame fr = new JFrame();
//            fr.getContentPane().add(new JLabel(new ImageIcon(buf)));
//            fr.pack();
//            fr.setVisible(true);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}