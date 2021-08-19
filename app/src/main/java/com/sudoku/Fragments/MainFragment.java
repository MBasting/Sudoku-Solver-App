package com.sudoku.Fragments;

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
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.sudoku.R;
import com.sudoku.calc.Sudoku_Scanner;
import com.sudoku.calc.TNumber;
import com.sudoku.utils.DataBaseHandler;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainFragment extends Fragment {
    private static final int CAMERA_REQUEST = 1888;
    Button take_picture, fill_sudoku;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private static final int MY_STORAGE_PERMISSION_CODE = 101;
    private static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 102;
    private static final String map = "/Sudoku";
    //Bitmap photo;
    String photo;
    DataBaseHandler databaseHandler;
    private SQLiteDatabase db;
    Bitmap theImage;
    static TNumber[] tNumbers;
    EditText[] editTexts;


    /** Puts all reference images needed for later recognition in an array
     *  Every element consists of an image of the number, corresponding number and amount of pixels
     *
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
        take_picture = view.findViewById(R.id.take_picture);
        fill_sudoku = view.findViewById(R.id.fill_in_sudoku);
        databaseHandler = new DataBaseHandler(getContext());
        try {
            setNumbers();
        } catch (IOException e) {
            System.out.println("Something went wrong trying to load reference images");
            e.printStackTrace();
        }
        take_picture.setOnClickListener(
                new View.OnClickListener() {
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
                        if (storage_p && camera_p) {
                            switch_to_camera();
                        }

                    }
                });

        fill_sudoku.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new SolveFragment());
            }
        });
       return view;
    }

    public void switch_to_camera() {
        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        File myDir = new File(root + map);
        if (!myDir.exists()) {
            myDir.mkdirs();
            System.out.println("Created folder");
        }
        SolveFragment solveFragment = new SolveFragment();
        replaceFragment(solveFragment);
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
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
            Toast.makeText(getContext(), "Succesfully ", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode,
                permissions,
                grantResults);
        System.out.println("GOTTEN HERE");

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
                    switch_to_camera();
                }
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
                outputStream = new FileOutputStream(file, false);
                theImage.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
                outputStream.flush();
                outputStream.close();
                int[][] sud = getSudoku(file, tNumbers);
                if (sud != null) {
                    System.out.println(Arrays.deepToString(sud));
                    TableLayout tl = getActivity().findViewById(R.id.sudoku);
                    setGrid(sud, tl);
                } else {
                    System.out.println("NO SUDOKU FOUND");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            photo=getEncodedString(theImage);
        }
    }

    public static void setGrid(int[][] sudoku, TableLayout tl) {
        for (int i = 0; i < tl.getChildCount(); i++) {
            View view = tl.getChildAt(i);
            if (view instanceof TableRow) {
                TableRow tableRow = (TableRow) view;
                for (int j = 0; j < tableRow.getChildCount(); j++) {
                    if (sudoku[i][j] != 0) {
                        View int_view = tableRow.getChildAt(j);
                        if (int_view instanceof EditText) {
                            EditText editText = (EditText) int_view;
                            editText.setText(String.valueOf(sudoku[i][j]));
                        }
                    }
                }
            }
        }
    }


    public static int[][] getSudoku(File file, TNumber[] tNumbers) {
        int[][] sud = Sudoku_Scanner.scan(file.getAbsolutePath(), tNumbers);
        System.out.println(sud);
        System.out.println(Arrays.deepToString(sud));
        return sud;
    }

    private String getEncodedString(Bitmap bitmap) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
        byte[] imageArr = os.toByteArray();
        return Base64.encodeToString(imageArr, Base64.URL_SAFE);
    }
}
