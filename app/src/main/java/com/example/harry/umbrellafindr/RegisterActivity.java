package com.example.harry.umbrellafindr;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity implements View.OnTouchListener, ProfilePictureDialogBox.DialogListener{

    public SwitchCompat mGenderSwitch;
    public ImageView mProfilePicture;

    public EditText mEmail;
    public EditText mPassword;
    public EditText mConfirmPassword;

    private FirebaseAuth mAuth;

    private static final int RESULT_LOAD_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mGenderSwitch = findViewById(R.id.switchGender);
        mGenderSwitch.setSwitchTextAppearance(getApplicationContext(), R.style.switchTextStyle);

        mProfilePicture = findViewById(R.id.imageView4);
        mProfilePicture.setOnTouchListener(this);

        mEmail = findViewById(R.id.editEmail);
        mPassword = findViewById(R.id.editPassword);
        mConfirmPassword = findViewById(R.id.editConfirmPassword);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.imageView4:
                DialogFragment profilePictureDialogBoxFragment = new ProfilePictureDialogBox();
                profilePictureDialogBoxFragment.show(getFragmentManager(), "profiles");
                break;
            case R.id.registerButton:
                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();
                String confirmPassword = mConfirmPassword.getText().toString();

                if(password.equals(confirmPassword)) {
//                    mAuth.createUserWithEmailAndPassword(email, password)
//                            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                                @Override
//                                public void onComplete(@NonNull Task<AuthResult> task) {
//                                    if (task.isSuccessful()) {
//                                        // Sign in success, update UI with the signed-in user's information
//                                        Log.d(TAG, "createUserWithEmail:success");
//                                        FirebaseUser user = mAuth.getCurrentUser();
//                                        updateUI(user);
//                                    } else {
//                                        // If sign in fails, display a message to the user.
//                                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
//                                        Toast.makeText(EmailPasswordActivity.this, "Authentication failed.",
//                                                Toast.LENGTH_SHORT).show();
//                                        updateUI(null);
//                                    }
//
//                                    // ...
//                                }
//                            });
                } else {
//                    AlertDialog failedToRegister = new AlertDialog();

                }
                break;
        }

        return super.onTouchEvent(event);
    }

    @Override
    public void onElementSelected(DialogFragment dialogFragment, int which) {
        switch (which) {
            case 0:

                break;

            case 1:
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);
                break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            mProfilePicture.setImageURI(selectedImage);
        }
    }
}
