package io.github.sidvenu.death_honeyourintuition;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;

import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.ibm.cloud.sdk.core.service.security.IamOptions;
import com.ibm.watson.visual_recognition.v3.VisualRecognition;
import com.ibm.watson.visual_recognition.v3.model.DetectFacesOptions;
import com.ibm.watson.visual_recognition.v3.model.DetectedFaces;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class CameraActivity extends AppCompatActivity {

    private IamOptions options;
    private VisualRecognition visualRecognition;
    private int REQUEST_IMAGE_CAPTURE = 1;
    private File photoFile;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        options = new IamOptions.Builder()
                .apiKey("UILcys4LBj7acKZ-PyQgjauaU0KnAh4d-IRMDwkVV5P4")
                .build();

        visualRecognition = new VisualRecognition("2018-03-19", options);
        dispatchTakePictureIntent();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            photoFile = new File(storageDir, "pic.jpg");
            // Continue only if the File was successfully created
            Uri photoURI = FileProvider.getUriForFile(this,
                    "com.example.android.fileprovider",
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            /*Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);*/
            try {
                final DetectFacesOptions detectFacesOptions = new DetectFacesOptions.Builder()
                        .imagesFile(photoFile)
                        .build();
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        DetectedFaces result = visualRecognition.detectFaces(detectFacesOptions).execute().getResult();
                        Log.v("TAG", String.valueOf(result));
                    }
                });
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }
    }
    /*
    final DetectFacesOptions detectFacesOptions = new DetectFacesOptions.Builder()
                        .imagesFile(outputFile)
                        .build();
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        DetectedFaces result = visualRecognition.detectFaces(detectFacesOptions).execute().getResult();
                        Log.v("TAG", String.valueOf(result));
                    }
                });
     */
}
