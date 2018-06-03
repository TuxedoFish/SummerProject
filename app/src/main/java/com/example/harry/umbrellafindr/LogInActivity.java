package com.example.harry.umbrellafindr;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class LogInActivity extends AppCompatActivity {

    public Button mButtonStart;
    public RippleDrawable mRippleStart;

    public Button mButtonRegister;
    public RippleDrawable mRippleRegister;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        TextView textView = (TextView) findViewById(R.id.textView2);
        SpannableString content = new SpannableString("forgotten password?");
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        textView.setText(content);

        mButtonStart = (Button) findViewById(R.id.button);
        mRippleStart = (RippleDrawable) mButtonStart.getBackground();

        mButtonRegister = (Button) findViewById(R.id.button3);
        mRippleRegister = (RippleDrawable) mButtonRegister.getBackground();

        mButtonStart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_BUTTON_PRESS){
                    switch (v.getId()) {
                        case R.id.button:
                            mRippleStart.setHotspot(event.getX(), event.getY());
                            break;
                    }
                    return true;
                }
                return false;
            }
        });
        mButtonRegister.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_BUTTON_PRESS){
                    switch (v.getId()) {
                        case R.id.button3:
                            mRippleRegister.setHotspot(event.getX(), event.getY());
                            break;
                    }
                    return true;
                }
                return false;
            }
        });
    }

    public void start(View v) {
        Intent toApp = new Intent(LogInActivity.this, ProfileActivity.class);
        startActivity(toApp);
    }
    public void register(View v) {
        Intent toApp = new Intent(LogInActivity.this, RegisterActivity.class);
        startActivity(toApp);
    }
}
