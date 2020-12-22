package com.example.cameracodeexample.Fragments;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cameracodeexample.MainActivity;
import com.example.cameracodeexample.R;
import com.example.cameracodeexample.calc.Scanner;
import com.example.cameracodeexample.calc.Solver;
import com.example.cameracodeexample.calc.TNumber;
import com.example.cameracodeexample.utils.DataBaseHandler;
import com.squareup.picasso.Picasso;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Arrays;

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
    TNumber[] tNumbers;

    /** Puts all reference images needed for later recognition in an array
     *  Every element consists of an image of the number, corresponding number and amount of pixels
     *
     * @return Array of Elements(image as Mat, number, amount of pixels)
     */
    public void setNumbers() throws IOException {
        AssetManager assetManager = getActivity().getAssets();
        tNumbers = new TNumber[9];
        for (int i = 0; i < 9; i++){
//                    File temp = files[i];
//                    String p = temp.getPath();
//                    int name = Integer.parseInt(String.valueOf(temp.getName().charAt(0)));
                Mat img = new Mat();
                String file = i + 1 + ".png";
                InputStream istr = assetManager.open(file);
                Bitmap bitmap = BitmapFactory.decodeStream(istr);
                Utils.bitmapToMat(bitmap, img);

                Imgproc.cvtColor(img, img, Imgproc.COLOR_BGR2GRAY);
                // To calculate the number of pixels active we need to count the white pixels
                // So the image needs to be inverted before calculating.
                Core.bitwise_not(img, img);
                Scalar sum = Core.sumElems(img);
                int s = (int) sum.val[0];
                tNumbers[i] = new TNumber(img, i+1, s);
        }
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       View view = inflater.inflate(R.layout.camera_fragment,container,false);
       // imageView =view. findViewById(R.id.imageView1);
        text = view.findViewById(R.id.my_rounded_button);
        text1 = view.findViewById(R.id.text1);
        databaseHandler = new DataBaseHandler(getContext());
        try {
            setNumbers();
        } catch (IOException e) {
            System.out.println("Something went wrong trying to load reference images");
            e.printStackTrace();
        }
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
                System.out.println();
                File file1 = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + map, "test_1.jpg");
                int[][] sud = getSudoku(file1, tNumbers);
                int[][] sol = Solver.solve(sud);
                System.out.println(Arrays.deepToString(sol));
                int number = 11;

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

    private static int[][] getSudoku(File file, TNumber[] tNumbers) {
        int[][] sud = Scanner.scan(file.getAbsolutePath(), tNumbers);
        return sud;
    }

    private String getEncodedString(Bitmap bitmap) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
        byte[] imageArr = os.toByteArray();
        return Base64.encodeToString(imageArr, Base64.URL_SAFE);
    }
}
