package com.example.cameracodeexample;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.cameracodeexample.Fragments.MainFragment;
import com.example.cameracodeexample.Fragments.SolveFragment;
import com.example.cameracodeexample.utils.DataBaseHandler;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST = 1888;
    Button camera, text1;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private static final int MY_STORAGE_PERMISSION_CODE = 100;
    private static final String map = "/saved_images";
    //Bitmap photo;
    String photo;
    DataBaseHandler databaseHandler;
    private SQLiteDatabase db;
    Bitmap theImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Fragment fragment = new MainFragment();
        loadFragment(fragment);
        OpenCVLoader.initDebug();
//        setContentView(R.layout.camera_fragment);
//        camera = this.findViewById(R.id.my_rounded_button);
//        text1 = this.findViewById(R.id.text1);
//        databaseHandler = new DataBaseHandler(this.getBaseContext());
//
//        camera.setOnClickListener(
//                new View.OnClickListener() {
//                    @RequiresApi(api = Build.VERSION_CODES.M)
//                    @Override
//                    public void onClick(View v) {
//                        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
//                        {
//                            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
//                        }
//                        if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_STORAGE_PERMISSION_CODE);
//                        }
//                        else
//                        {
//                            // Create root directory for saved images
//                            String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
//                            File myDir = new File(root + "/saved_images");
//                            if (!myDir.exists()) {
//                                myDir.mkdirs();
//                            }
//
//                            loadFragment(new SolveFragment());
//                        }
//                    }
//                });
    }

    public void loadFragment(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        //frame_container is your layout name in xml file
        transaction.replace(R.id.mainframeLayout, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

//    private void setDataToDataBase() {
//        db = databaseHandler.getWritableDatabase();
//        ContentValues cv = new ContentValues();
//        cv.put(databaseHandler.KEY_IMG_URL,getEncodedString(theImage));
//
//        long id = db.insert(databaseHandler.TABLE_NAME, null, cv);
//        if (id < 0) {
//            Toast.makeText(this.getBaseContext(), "Something went wrong. Please try again later...", Toast.LENGTH_LONG).show();
//        } else {
//            Toast.makeText(this.getBaseContext(), "Add successful", Toast.LENGTH_LONG).show();
//        }
//    }

//    /**
//     * Start an activity for result
//     * @param requestCode
//     * @param resultCode
//     * @param data
//     */
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK)
//        {
//            String name = "test_image.jpg";
//            String state = Environment.getExternalStorageState();
//            theImage = (Bitmap) data.getExtras().get("data");
//            // So return when the external storage is not mounted.
//            if (!Environment.MEDIA_MOUNTED.equals(state)) {
//                System.out.println("Storage not mounted");
//                return;
//            }
//            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + map, name);
//            FileOutputStream outputStream = null;
//            try {
//                outputStream = new FileOutputStream(file, true);
//                theImage.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
//                outputStream.flush();
//                outputStream.close();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            photo=getEncodedString(theImage);
//            setDataToDataBase();
//        }
//    }

//    private String getEncodedString(Bitmap bitmap) {
//        ByteArrayOutputStream os = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
//        byte[] imageArr = os.toByteArray();
//        return Base64.encodeToString(imageArr, Base64.URL_SAFE);
//    }



}
