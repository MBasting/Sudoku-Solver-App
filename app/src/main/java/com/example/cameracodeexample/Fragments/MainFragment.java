package com.example.cameracodeexample.Fragments;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cameracodeexample.R;
import com.example.cameracodeexample.utils.DataBaseHandler;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Scanner;

public class MainFragment extends Fragment {
    private static final int CAMERA_REQUEST = 1888;
    Button text, text1;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private static final int MY_STORAGE_PERMISSION_CODE = 100;
    private static final String map = "/saved_images";
    //Bitmap photo;
    String photo;
    DataBaseHandler databaseHandler;
    private SQLiteDatabase db;
    Bitmap theImage;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       View view = inflater.inflate(R.layout.camera_fragment,container,false);
       // imageView =view. findViewById(R.id.imageView1);
        text = view.findViewById(R.id.my_rounded_button);
        text1 = view.findViewById(R.id.text1);
        databaseHandler = new DataBaseHandler(getContext());

        text.setOnClickListener(
                new View.OnClickListener() {
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
                    String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
                    File myDir = new File(root + "/saved_images");
                    if (!myDir.exists()) {
                        myDir.mkdirs();
                    }
                    SolveFragment solveFragment = new SolveFragment();
                    replaceFragment(solveFragment);
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                }
            }
        });

        text1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new LocalFragment());
            }
        });
       return view;
    }

    public void replaceFragment(Fragment otherFragment) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.mainframeLayout, otherFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void setDataToDataBase() {
        db = databaseHandler.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(databaseHandler.KEY_IMG_URL,getEncodedString(theImage));

        long id = db.insert(databaseHandler.TABLE_NAME, null, cv);
        if (id < 0) {
            Toast.makeText(getContext(), "Something went wrong. Please try again later...", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getContext(), "Add successful", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Reuqesting for premissons
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(getActivity(), "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
            else
            {
                Toast.makeText(getActivity(), "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
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
            theImage = (Bitmap) data.getExtras().get("data");
            // So return when the external storage is not mounted.
            if (!Environment.MEDIA_MOUNTED.equals(state)) {
               System.out.println("Storage not mounted");
               return;
            }
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + map, name);
            FileOutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(file, true);
                theImage.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
                outputStream.flush();
                outputStream.close();
                int number = 11;

                callPythonFunction("hello.py");
                // Code to set the content of a editText field.
                Class res = R.id.class;
                Field field = res.getField("number"+number);
                int drawableId = field.getInt(null);
                EditText editText = getActivity().findViewById(drawableId);
                editText.setText("1", TextView.BufferType.EDITABLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
            photo=getEncodedString(theImage);
            setDataToDataBase();
        }
    }

    private String getEncodedString(Bitmap bitmap) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
        byte[] imageArr = os.toByteArray();
        return Base64.encodeToString(imageArr, Base64.URL_SAFE);
    }

    private int[][] callPythonFunction(String fileName) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("python", resolvePythonScriptPath("com/example/cameracodeexample/hello.py"));
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            int[][] results = readProcessOutput(process.getInputStream());
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Something went wrong");
                return null;
            }
            else {
                return results;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String resolvePythonScriptPath(String filename) {
        File file = new File("src/main/java/com/example/cameracodeexample/" + filename);
        return file.getAbsolutePath();
    }

    public int[][] readProcessOutput(InputStream e) {
        int[][] sudoku = new int[9][9];
        Scanner sc = new Scanner(e);
        int indexRow = 0;
        int indexColumn = 0;
        while (sc.hasNext() && indexRow < 9) {
            if (indexColumn >= 9) {
                indexColumn = 0;
                indexRow++;
            }
            sudoku[indexRow][indexColumn] = sc.nextInt();
            indexColumn++;
        }
        return sudoku;
    }



    private String calculateSudoku(String fileName) {
        try {
            String prg = "import sys";
            BufferedWriter out = new BufferedWriter(new FileWriter("path/a.py"));
            out.write(prg);
            out.close();
            // Python script should write the sudoku result to a txt file.
            String result = "result.txt";
            Process p = Runtime.getRuntime().exec("python path/a.py " + fileName + result);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
