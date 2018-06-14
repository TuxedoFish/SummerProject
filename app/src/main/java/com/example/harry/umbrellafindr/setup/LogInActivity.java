package com.example.harry.umbrellafindr.setup;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import com.example.harry.umbrellafindr.app.ProfileActivity;
import com.example.harry.umbrellafindr.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

public class LogInActivity extends AppCompatActivity implements View.OnTouchListener{

    public Button mButtonStart;
    public RippleDrawable mRippleStart;

    public Button mButtonRegister;
    public RippleDrawable mRippleRegister;

    public EditText mEmail;
    public EditText mPassword;

    private FirebaseAuth mAuth;
//    private FirebaseFirestore db;

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

        mAuth = FirebaseAuth.getInstance();
//        db = FirebaseFirestore.getInstance();
//
//        DocumentReference docref = db.collection("users_info").document("test");
//
//        Map<String, Object> data  = new HashMap<>();
//        data.put("first_name", "Harry");
//        data.put("age", 19);
//        data.put("gender", "male");
//        data.put("bio", "unknown");
//        data.put("profile_picture", "some string");
//
//        docref.set(data);

        if(mAuth.getCurrentUser()!=null) {
            start(mButtonStart);
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

    private void checkIfEmailVerified()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user.isEmailVerified())
        {
            Intent toApp = new Intent(LogInActivity.this, ProfileActivity.class);
            startActivity(toApp);
            Toast.makeText(LogInActivity.this, "Successfully logged in", Toast.LENGTH_SHORT).show();
        }
        else
        {
            // email is not verified, so just prompt the message to the user and restart this activity.
            // NOTE: don't forget to log out the user.
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(LogInActivity.this, "Check your emails to verify your account", Toast.LENGTH_SHORT).show();
            //restart this activity
            finish();
            startActivity(getIntent());
        }
    }

    public void start(View v) {
        String email = mEmail.getText().toString();
        String password = mPassword.getText().toString();

        if(!email.equals("") && !password.equals("")) {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d("logged in", "log in : success");

                                checkIfEmailVerified();
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w("failed", "log in : failure", task.getException());
                                Toast.makeText(LogInActivity.this, "Log in failed." + task.getException(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            Log.e("failed", "log in : failure");
            Toast.makeText(LogInActivity.this, "Log in failed",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void register(View v) {
        Intent toApp = new Intent(LogInActivity.this, RegisterActivity.class);
        startActivity(toApp);
    }
}
