package io.github.sidvenu.death_honeyourintuition;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextPaint;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ibm.cloud.sdk.core.service.security.IamOptions;
import com.ibm.watson.visual_recognition.v3.VisualRecognition;
import com.ibm.watson.visual_recognition.v3.model.DetectFacesOptions;
import com.ibm.watson.visual_recognition.v3.model.DetectedFaces;
import com.ibm.watson.visual_recognition.v3.model.Face;
import com.ibm.watson.visual_recognition.v3.model.FaceLocation;
import com.ibm.watson.visual_recognition.v3.model.ImageWithFaces;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class CameraActivity extends AppCompatActivity {

    private VisualRecognition visualRecognition;
    private final int REQUEST_IMAGE_CAPTURE = 1, REQUEST_PERMISSIONS = 2;
    private File photoFile;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        ArrayList<String> list = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            list.add(Manifest.permission.CAMERA);
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            list.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        IamOptions options = new IamOptions.Builder()
                .apiKey(BuildConfig.ibmWatsonApiKey)
                .build();

        visualRecognition = new VisualRecognition("2018-03-19", options);
        if (list.size() == 0) {
            dispatchTakePictureIntent();
        }

        Object[] arr = list.toArray();
        if (arr != null && list.size() > 0)
            ActivityCompat.requestPermissions(this,
                    Arrays.copyOf(arr,
                            arr.length,
                            String[].class),
                    REQUEST_PERMISSIONS);
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
                        try {
                            Paint p = new Paint();
                            p.setStyle(Paint.Style.STROKE);
                            p.setStrokeWidth(5);
                            p.setAntiAlias(true);
                            p.setColor(Color.RED);

                            float textHeight = 50;
                            TextPaint textPaint = new TextPaint();
                            textPaint.setColor(Color.RED);
                            textPaint.setTextSize(textHeight);

                            BitmapFactory.Options opt = new BitmapFactory.Options();
                            opt.inMutable = true;
                            final Bitmap b = BitmapFactory.decodeStream(new FileInputStream(photoFile), null, opt);
                            Canvas canvas = new Canvas(b);
                            ImageWithFaces image = result.getImages().get(0);
                            for (Face f : image.getFaces()) {
                                long ageNum = Math.round((f.getAge().getMax() + f.getAge().getMin()) / 2.0);
                                FaceLocation location = f.getFaceLocation();
                                canvas.drawRect(location.getLeft().floatValue(),
                                        location.getTop().floatValue(),
                                        (float) (location.getLeft() + location.getWidth()),
                                        (float) (location.getTop() + location.getHeight()),
                                        p
                                );

                                String age = "Age: " + ageNum;
                                int worldLifeExpectancy = 72;
                                String deathYear = "Death Year: "
                                        + (Calendar.getInstance().get(Calendar.YEAR) + worldLifeExpectancy - ageNum);
                                String gender = "Gender: " + (("MALE".equals(f.getGender().getGender())) ? "Male" : "Female");
                                canvas.drawText(age + "  " + gender, location.getLeft().floatValue(), (float) (location.getTop() - textHeight / 2), textPaint);
                                canvas.drawText(deathYear, location.getLeft().floatValue(), (float) (location.getTop() + location.getHeight() + textHeight), textPaint);
                                //canvas.drawText(age+"  "+gender, 0, 20, textPaint);
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ImageView picture = findViewById(R.id.picture);
                                    picture.setImageBitmap(b);
                                }
                            });
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && (grantResults.length != 2 || grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                // permission was granted, yay! Do the
                // contacts-related task you need to do.
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(this, "Permission denied. Please grant permission and try again", Toast.LENGTH_LONG).show();
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
