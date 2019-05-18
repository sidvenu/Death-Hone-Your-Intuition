package io.github.sidvenu.death_honeyourintuition;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.ColorUtils;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    String matches;
    int colorSuccess, colorFailure;
    int age;
    int tries;
    boolean isMatchComplete = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        generateAge();
        resetTries();

        colorSuccess = getResources().getColor(R.color.colorSuccess);
        colorFailure = getResources().getColor(R.color.colorFailure);

        // set the FAB to the top of the keyboard (if opened)
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        final EditText ageGuessEditText = findViewById(R.id.guess_edit_text);
        ageGuessEditText.addTextChangedListener(new TextWatcher() {
            String lastText = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(s) &&
                        (
                                !MainActivity.isInteger(s.toString()) ||
                                        Integer.valueOf(s.toString()) > 100 ||
                                        Integer.valueOf(s.toString()) < 0
                        )
                ) {
                    Log.v("TAG", lastText);
                    ageGuessEditText.setText(lastText);
                    ageGuessEditText.setSelection(ageGuessEditText.getText().length());
                } else lastText = s.toString();
            }
        });

        final FloatingActionButton doneButton = findViewById(R.id.done_fab);

        doneButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                if (!MainActivity.isInteger(ageGuessEditText.getText().toString()))
                    return;
                if (!isMatchComplete) {
                    tries--;
                    if (tries == 0)
                        isMatchComplete = true;
                    int ageGuess = Integer.valueOf(ageGuessEditText.getText().toString());
                    float failureRatio = Math.abs(ageGuess - age) / 100.0f;
                    findViewById(R.id.root_view).setBackgroundColor(ColorUtils.blendARGB(colorSuccess, colorFailure, failureRatio));
                    int guessReply;
                    if (ageGuess - age > 0) {
                        guessReply = R.string.guess_reply_high;
                    } else if (ageGuess - age < 0) {
                        guessReply = R.string.guess_reply_low;
                    } else {
                        guessReply = R.string.guess_reply_correct;
                        isMatchComplete = true;
                    }
                    TextView guessReplyTextView = findViewById(R.id.guess_reply);
                    guessReplyTextView.setText(guessReply);

                    if (isMatchComplete) {
                        if (ageGuess == age) {
                            matches = matches.concat("W");
                        } else {
                            matches = matches.concat("L");
                        }
                        setPreviousMatches();
                        setWinsAndLosses();
                        doneButton.setImageResource(R.drawable.ic_baseline_refresh);
                        disableEditText(ageGuessEditText);
                    } else {
                        guessReplyTextView.setText(getString(guessReply) + " You have " + tries + " tries left");
                    }
                } else {
                    resetTries();
                    doneButton.setImageResource(R.drawable.ic_baseline_done);
                    enableEditText(ageGuessEditText);
                    ageGuessEditText.setText(null);
                    ageGuessEditText.requestFocus();
                    generateAge();
                    isMatchComplete = false;
                    ((TextView) findViewById(R.id.guess_reply)).setText("");
                }
            }
        });

        ageGuessEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    Log.v("TAG", "down pressed");
                    doneButton.performClick();
                    ageGuessEditText.clearFocus();
                    return false;
                }
                return false;
            }
        });

        findViewById(R.id.camera_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CameraActivity.class));
            }
        });
    }

    public static boolean isInteger(String s) {
        return isInteger(s, 10);
    }

    public static boolean isInteger(String s, int radix) {
        if (s.isEmpty()) return false;
        for (int i = 0; i < s.length(); i++) {
            if (i == 0 && s.charAt(i) == '-') {
                if (s.length() == 1) return false;
                else continue;
            }
            if (Character.digit(s.charAt(i), radix) < 0) return false;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        matches = preferences.getString(Keys.MATCHES, "");
        setPreviousMatches();
        setWinsAndLosses();
    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        preferences.edit()
                .putString(Keys.MATCHES, matches)
                .apply();
    }

    private void setPreviousMatches() {
        ((TextView) findViewById(R.id.previous_matches)).setText(
                String.format(getString(R.string.previous_matches_prefix), matches.replaceAll(".(?!$)", "$0 "))
        );
    }

    private void setWinsAndLosses() {
        ((TextView) findViewById(R.id.wins_and_losses_number)).setText(
                String.format(getString(R.string.wins_and_losses),
                        matches.replace("L", "").length(),
                        matches.replace("W", "").length()
                )
        );
    }

    private void resetTries() {
        tries = 10;
    }

    private void generateAge() {
        age = (int) (System.currentTimeMillis() % 101);
    }

    private void enableEditText(EditText editText) {
        editText.setFocusableInTouchMode(true);
        editText.setEnabled(true);
        editText.setCursorVisible(true);
        editText.setTextColor(getResources().getColor(android.R.color.white));
    }

    private void disableEditText(EditText editText) {
        editText.setFocusable(false);
        editText.setEnabled(false);
        editText.setCursorVisible(false);
        editText.setTextColor(getResources().getColor(android.R.color.darker_gray));
    }

    static class Keys {
        static String MATCHES = "MATCHES";
    }

}
