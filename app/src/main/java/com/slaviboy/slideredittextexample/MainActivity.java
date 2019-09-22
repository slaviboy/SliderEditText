package com.slaviboy.slideredittextexample;

import android.app.Activity;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.slaviboy.slideredittext.CornerRadius;
import com.slaviboy.slideredittext.Range;
import com.slaviboy.slideredittext.SliderEditText;

public class MainActivity extends AppCompatActivity implements SliderEditText.OnChangeListener {

    // sliders
    private SliderEditText fish;
    private SliderEditText steak;
    private SliderEditText milk;
    private SliderEditText hamburger;

    private TextView totalCalories;

    private double[] caloriesPerGram;
    private SliderEditText[] sliders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init sliders
        fish = findViewById(R.id.fish_slider);
        steak = findViewById(R.id.steak_slider);
        milk = findViewById(R.id.milk_slider);
        hamburger = findViewById(R.id.hamburger_slider);
        totalCalories = findViewById(R.id.total_calories);

        // set on change listener
        fish.setOnChangeListener(this);
        steak.setOnChangeListener(this);
        milk.setOnChangeListener(this);
        hamburger.setOnChangeListener(this);

        caloriesPerGram = new double[]{
                0.82, 1.43, 0.64, 2.95
        };

        sliders = new SliderEditText[]{
                fish, steak, milk, hamburger
        };

        // set font
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/twcen/TCM_____.TTF");
        TextView label = findViewById(R.id.calories_label);
        label.setTypeface(font);
    }

    @Override
    public void onChange(SliderEditText slider) {
        // calculate total calories
        int totalCaloriesCount = 0;
        for (int i = 0; i < sliders.length; i++) {
            totalCaloriesCount += caloriesPerGram[i] * (int) sliders[i].getValue();
        }
        totalCalories.setText(totalCaloriesCount + " calories");
    }

    //region Hide UI button
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI(this);
        }
    }

    public static void hideSystemUI(Activity activity) {
        View decorView = activity.getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    public static void showSystemUI(Activity activity) {
        View decorView = activity.getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }
}
