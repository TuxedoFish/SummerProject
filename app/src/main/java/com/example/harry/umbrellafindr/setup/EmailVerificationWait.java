package com.example.harry.umbrellafindr.setup;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.harry.umbrellafindr.app.ProfileActivity;
import com.example.harry.umbrellafindr.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class EmailVerificationWait extends AppCompatActivity implements View.OnClickListener{

    //variables to log in when loaded
    private String mEmail;
    private String mPass;

    private FirebaseAuth mAuth;

    private Button mOpenMailBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification_wait);

        mEmail = getIntent().getExtras().getString("email");
        mPass = getIntent().getExtras().getString("password");

        mOpenMailBtn = (Button) findViewById(R.id.openMail);
        mOpenMailBtn.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
    }

    public void attemptLogin() {
        mAuth.signInWithEmailAndPassword(mEmail, mPass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("failed", "log in : success");
                            if (mAuth.getCurrentUser().isEmailVerified()) {
                                //User has verified
                                ImageView thumbs_down = (ImageView) findViewById(R.id.visualisation);
                                thumbs_down.setImageResource(R.drawable.ic_thumbs_up);

                                startActivity(new Intent(EmailVerificationWait.this, ProfileActivity.class));
                            } else {
                                //User has not verified
                                FirebaseAuth.getInstance().signOut();
                            }
                        } else {
                            Log.d("failed", "log in : failed");
                        }
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.openMail:
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                startActivity(intent);
                break;
        }
    }

    @Override
    protected void onResume() {
        attemptLogin();

        super.onResume();
    }
}
