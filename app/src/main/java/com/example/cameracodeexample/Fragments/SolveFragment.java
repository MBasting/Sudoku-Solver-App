package com.example.cameracodeexample.Fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cameracodeexample.MainActivity;
import com.example.cameracodeexample.R;
import com.example.cameracodeexample.calc.Solver;
import com.example.cameracodeexample.utils.DataBaseHandler;

import java.io.File;
import java.nio.file.*;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.sql.SQLOutput;
import java.util.Arrays;

public class SolveFragment extends Fragment {
    private static final int CAMERA_REQUEST = 1888;
    private boolean confirmed = false;
    Button confirm;
    Button retry;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private static final int MY_STORAGE_PERMISSION_CODE = 100;
    private static final String map = "/saved_images";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_solve_activity, container, false);
        confirm = view.findViewById(R.id.confirm_button);
        View.OnClickListener confirmListiner = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (confirmed) {
                    replaceFragment(new MainFragment());
                } else {
                    TableLayout tl = getActivity().findViewById(R.id.sudoku);
                    int[][] sud = getElementsTable();
                    int[][] solved = Solver.solve(sud);
                    if (solved == null) {
                        System.out.println("Unsolvable");
                    } else {
                        MainFragment.setGrid(solved, tl);
                        confirm.setText("Return");
                        confirmed = true;
                    }
                }

            }};
        confirm.setOnClickListener( confirmListiner);
        retry = view.findViewById(R.id.try_again_button);
        retry.setOnClickListener(  new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if (getActivity().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                }
                if(getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_STORAGE_PERMISSION_CODE);
                }
                else
                {
                    SolveFragment solveFragment = new SolveFragment();
                    replaceFragment(solveFragment);
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                }
            }
        });
        return view;
    }

    public int[][] getElementsTable() {
        int[][] sud = new int[9][9];
        TableLayout tl = getActivity().findViewById(R.id.sudoku);
        for (int i = 0; i < tl.getChildCount(); i++) {
            View view = tl.getChildAt(i);
            if (view instanceof TableRow) {
                TableRow tableRow = (TableRow) view;
                for (int j = 0; j < tableRow.getChildCount(); j++) {
                    View int_view = tableRow.getChildAt(j);
                        if (int_view instanceof EditText) {
                            System.out.println("Set the value");
                            EditText editText = (EditText) int_view;
                            String val = editText.getText().toString();
                            if (!val.equals("") && val != null) {
                                sud[i][j] = Integer.parseInt(val);
                        }
                    }
                }
            }
        }
        return sud;
    }

    public void replaceFragment(Fragment otherFragment) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.mainframeLayout, otherFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    /**
     * Start an activity for result
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK)
        {
            String name = "test_image.jpg";
            String state = Environment.getExternalStorageState();
            Bitmap theImage = (Bitmap) data.getExtras().get("data");
            // So return when the external storage is not mounted.
            if (!Environment.MEDIA_MOUNTED.equals(state)) {
                System.out.println("Storage not mounted");
                return;
            }
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + map, name);
            FileOutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(file);
                theImage.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
                outputStream.flush();
                outputStream.close();
                int[][] sud = MainFragment.getSudoku(file, MainFragment.tNumbers);
                if (sud != null) {
                    TableLayout tl = getActivity().findViewById(R.id.sudoku);
                    MainFragment.setGrid(sud, tl);
                } else {
                    System.out.println("NO SUDOKU FOUND");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
