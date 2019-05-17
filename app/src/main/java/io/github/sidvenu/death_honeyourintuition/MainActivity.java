package io.github.sidvenu.death_honeyourintuition;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.multidex.MultiDexApplication;

import com.ibm.cloud.sdk.core.service.security.IamOptions;
import com.ibm.watson.visual_recognition.v3.VisualRecognition;
import com.ibm.watson.visual_recognition.v3.model.ClassifiedImages;
import com.ibm.watson.visual_recognition.v3.model.ClassifyOptions;
import com.ibm.watson.visual_recognition.v3.model.DetectFacesOptions;
import com.ibm.watson.visual_recognition.v3.model.DetectedFaces;
import com.rengwuxian.materialedittext.MaterialEditText;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText ageGuess = findViewById(R.id.guess_edit_text);
        ageGuess.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        IamOptions options = new IamOptions.Builder()
                .apiKey("UILcys4LBj7acKZ-PyQgjauaU0KnAh4d-IRMDwkVV5P4")
                .build();

        final VisualRecognition visualRecognition = new VisualRecognition("2018-03-19", options);

        final DetectFacesOptions detectFacesOptions = new DetectFacesOptions.Builder()
                .url("https://qph.fs.quoracdn.net/main-raw-246505832-fiulqsjebqiqozmsewlykdsploxnitxt.jpeg")
                .build();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                DetectedFaces result = visualRecognition.detectFaces(detectFacesOptions).execute().getResult();
                Log.v("TAG", String.valueOf(result));
                startActivity(new Intent(MainActivity.this, CameraActivity.class));
            }
        });
    }

}
