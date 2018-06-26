package com.example.harry.umbrellafindr.setup;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.harry.umbrellafindr.utils.DatabaseLogic;
import com.example.harry.umbrellafindr.app.HubActivity;
import com.example.harry.umbrellafindr.R;
import com.google.firebase.auth.FirebaseAuth;

public class LogInActivity extends AppCompatActivity implements View.OnTouchListener{

    public Button mButtonStart;
    public RippleDrawable mRippleStart;

    public Button mButtonRegister;
    public RippleDrawable mRippleRegister;

    public EditText mEmail;
    public EditText mPassword;

    private FirebaseAuth mAuth;
//    private FirebaseFirestore db;

    private DatabaseLogic databaseLogic;

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

        mEmail = (EditText) findViewById(R.id.editEmailLogIn);
        mPassword = (EditText) findViewById(R.id.editPasswordLogIn);

        databaseLogic = new DatabaseLogic();
        if(databaseLogic.isLoggedIn()) {
            completeLogIn();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.button:
                mRippleStart.setHotspot(event.getX(), event.getY());
                break;
            case R.id.button3:
                mRippleRegister.setHotspot(event.getX(), event.getY());
                break;
        }
        return true;
    }

    public void start(View v) {
        String email = mEmail.getText().toString();
        String password = mPassword.getText().toString();

        if(!email.equals("") && !password.equals("")) {
            databaseLogic.attemptLogIn(this, email, password);
        } else {
            Log.e("failed", "log in : failure");
            Toast.makeText(LogInActivity.this, "Log in failed",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void completeLogIn() {
        Intent toApp = new Intent(LogInActivity.this, HubActivity.class);
        startActivity(toApp);
        Toast.makeText(LogInActivity.this, "Successfully logged in", Toast.LENGTH_SHORT).show();
    }

    public void signOut() {
        databaseLogic.signOut();
        Toast.makeText(LogInActivity.this, "Check your emails to verify your account", Toast.LENGTH_SHORT).show();
        //restart this activity
        finish();
        startActivity(getIntent());
    }

    public void register(View v) {
        Intent toApp = new Intent(LogInActivity.this, RegisterActivity.class);
        startActivity(toApp);
    }
}
