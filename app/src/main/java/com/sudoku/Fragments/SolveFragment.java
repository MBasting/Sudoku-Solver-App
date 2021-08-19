package com.sudoku.Fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.sudoku.R;
import com.sudoku.calc.Solver;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class SolveFragment extends Fragment {
    private static final int CAMERA_REQUEST = 1888;
    private boolean confirmed = false;
    Button confirm;
    Button retry;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private static final int MY_STORAGE_PERMISSION_CODE = 101;
    private static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 102;
    private static final String map = "/Sudoku";


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
                        Toast.makeText(getActivity(), "Sudoku is unsolvable, check if all numbers are correctly detected", Toast.LENGTH_LONG).show();
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
                boolean camera_p = true;
                boolean storage_p = true;
                List<String> listPermissionsNeeded = new ArrayList<>();
                if (getActivity().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                {
                    camera_p = false;
                    System.out.println("ADD CAMERA PERMISSION REQUEST");
                    listPermissionsNeeded.add(Manifest.permission.CAMERA);
                }
                if(getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    storage_p = false;
                    System.out.println("ADD Storage PERMISSION REQUEST");
                    listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
                if (!camera_p || !storage_p) {
                    requestPermissions(listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
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

    @Override
    public void onPause() {
        System.out.println("APPLICATION PAUSED");
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode,
                permissions,
                grantResults);

        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this.getActivity(), "Camera Permission Granted", Toast.LENGTH_SHORT) .show();
            }
            else {
                Toast.makeText(this.getActivity(), "Camera Permission Denied", Toast.LENGTH_SHORT) .show();
            }
        }
        else if (requestCode == MY_STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this.getActivity(), "Storage Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this.getActivity(), "Storage Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == REQUEST_ID_MULTIPLE_PERMISSIONS) {
            boolean camera_p = true;
            boolean storage_p = true;
            if (grantResults.length > 0) {
                for (int i = 0; i < permissions.length; i++) {
                    if (permissions[i].equals(Manifest.permission.CAMERA)) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this.getActivity(), "Camera Permission Granted", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this.getActivity(), "Camera Permission Denied", Toast.LENGTH_SHORT) .show();
                            camera_p = false;
                        }
                    }
                    if (permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this.getActivity(), "Storage Permission Granted", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this.getActivity(), "Storage Permission Denied", Toast.LENGTH_SHORT) .show();
                            storage_p = false;
                        }
                    }
                }
                if (!camera_p || !storage_p) {
                    Toast.makeText(this.getActivity(), "Necessary permission denied", Toast.LENGTH_SHORT).show();
                    getActivity().finishAffinity();
                } else {
                    System.out.println("FINISHED ASKING FOR PERMISSION");
                    SolveFragment solveFragment = new SolveFragment();
                    replaceFragment(solveFragment);
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                }
            }
        }
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
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
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
