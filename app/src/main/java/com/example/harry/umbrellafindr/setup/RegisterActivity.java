package com.example.harry.umbrellafindr.setup;

import android.Manifest;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.RippleDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.harry.umbrellafindr.R;
import com.example.harry.umbrellafindr.utils.Utilities;
import com.example.harry.umbrellafindr.utils.Constants;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity implements View.OnTouchListener, ProfilePictureDialogBox.DialogListener{

    public SwitchCompat mGenderSwitch;
    public ImageView mProfilePicture;
    public Uri mProfilePictureURI = null;

    public EditText mEmail;
    public EditText mPassword;
    public EditText mConfirmPassword;
    public EditText mFirstName;

    public Button mButtonRegister;
    public RippleDrawable mRippleRegister;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private Utilities utils;

    private boolean clicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mGenderSwitch = findViewById(R.id.switchGender);
        mGenderSwitch.setSwitchTextAppearance(getApplicationContext(), R.style.switchTextStyle);

        mProfilePicture = findViewById(R.id.imageView4);
        mProfilePicture.setOnTouchListener(this);

        mButtonRegister = (Button) findViewById(R.id.registerButton);
        mRippleRegister = (RippleDrawable) mButtonRegister.getBackground();
        mButtonRegister.setOnTouchListener(this);

        mEmail = findViewById(R.id.editEmail);
        mPassword = findViewById(R.id.editPassword);
        mConfirmPassword = findViewById(R.id.editConfirmPassword);
        mFirstName = findViewById(R.id.editFirstName);

        utils = new Utilities();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);

        mAuth.signOut();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.imageView4:
                DialogFragment profilePictureDialogBoxFragment = new ProfilePictureDialogBox();
                profilePictureDialogBoxFragment.show(getFragmentManager(), "profiles");
                break;
            case R.id.registerButton:
                if(!clicked) {
                    clicked = true;

                    mRippleRegister.setHotspot(event.getX(), event.getY());

                    final String email = mEmail.getText().toString();
                    final String password = mPassword.getText().toString();
                    final String confirmPassword = mConfirmPassword.getText().toString();
                    final String firstname = mFirstName.getText().toString();

                    if (checkData(confirmPassword, password, email, firstname)) {
                        mAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            // Sign in success, update UI with the signed-in user's information
                                            Log.d("created", "createUserWithEmail:success");
                                            updateUserInfo(mFirstName.getEditableText().toString(), mProfilePictureURI, mGenderSwitch.getShowText(), -1, "please add a bio");
                                        } else {
                                            // If sign in fails, display a message to the user.
                                            Log.w("failed", "createUserWithEmail : failure", task.getException());
                                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                                    Toast.LENGTH_SHORT).show();
                                            clicked = false;
                                        }
                                    }
                                });
                    } else {
                        clicked = false;
                    }
                }
                break;
        }

        return super.onTouchEvent(event);
    }

    public void alert(String message) {
        Log.w("failed", "register : failure, \n" + message);
        Toast.makeText(RegisterActivity.this, "Registration failed, " + message,
                Toast.LENGTH_SHORT).show();
    }

    public boolean checkData(String confirmPassword, String password, String email, String name) {
        if(password.equals("")) {
            //Please fill in password
            //NEED A STRENGTH CHECKER
            alert("please fill in a password");
            return false;
        } else if (confirmPassword.equals("") || (!password.equals(confirmPassword))) {
            //Passwords do not match
            alert("passwords do not match");
            return false;
        } else if(email.equals("")) {
            //Please fill in an email
            alert("please fill in an email address");
            return false;
        } else if(mProfilePictureURI==null){
            //Please upload a profile picture
            alert("please upload a profile picture");
            return false;
        } else if(name.equals("")){
            //Please fill in name
            alert("please fill in your first name");
            return false;
        } else if(!email.endsWith("@ucl.ac.uk")) {
            //Has not entered a valid UCL email address
            alert("please enter a valid UCL email address");
            return false;
        }

        return true;
    }

    public void sendVerificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        Log.d("query", "user id - " + user.getUid());

        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // email sent
                            Log.d("created", "email sent:success");
                        }
                        else
                        {
                            // email not sent, so display message and restart the activity or do whatever you wish to do
                            //restart this activity
                            Log.d("created", "email sent:failure");
                            overridePendingTransition(0, 0);
                            finish();
                            overridePendingTransition(0, 0);
                            startActivity(getIntent());
                        }
                    }
                });
    }

    public void updateUserInfo(String mName, Uri mProfilePictureURI, boolean mGender, int mAge, String mBio) {
        FirebaseUser user = mAuth.getCurrentUser();

        String mGenderText = "unknown";
        if(mGender) { mGenderText = "male"; } else { mGenderText = "female"; }

        // Create a Map to store the data we want to set
        DocumentReference docRef = db.collection("users_info").document(user.getUid());
        Map<String, Object> data = new HashMap<>();
        data.put("first_name", mName);
        data.put("age", mAge);
        data.put("gender", mGenderText);
        data.put("bio", mBio);
        data.put("profile_picture", mProfilePictureURI.toString());

        // Add a new document (asynchronously) in collection "users" with id "uid"
        docRef.set(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()) {
                        Log.d("alert", "added data to database");

                        sendVerificationEmail();
                        Log.d("success", "log in after sign up : success");

                        // Log back out
                        FirebaseAuth.getInstance().signOut();
                        // Go to screen hinting to open emails
                        Intent mIntent = new Intent(RegisterActivity.this, EmailVerificationWait.class);
                        Bundle mBundle = new Bundle();
                        mBundle.putString("email", mEmail.getEditableText().toString());
                        mBundle.putString("password", mPassword.getEditableText().toString());
                        mIntent.putExtras(mBundle);

                        startActivity(mIntent);
                        finish();
                    } else {
                        Log.d("alert", "failed to add data to database" + task.getException());
                        alert("failed to add data ");
                    }
                }
            });
        }

    @Override
    public void onElementSelected(DialogFragment dialogFragment, int which) {
        switch (which) {
            case 0:
                if (ContextCompat.checkSelfPermission(RegisterActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

                    ActivityCompat.requestPermissions(RegisterActivity.this,
                            permissions, Constants.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                }
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, Constants.RESULT_LOAD_IMAGE_TAKEN);
                break;

            case 1:
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, Constants.RESULT_LOAD_IMAGE_GALLERY);
                break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri selectedImage = null;
        Bitmap bmp = null;

        if(requestCode == Constants.RESULT_LOAD_IMAGE_GALLERY && resultCode == RESULT_OK && data != null) {
            selectedImage = data.getData();
        }
        if(requestCode == Constants.RESULT_LOAD_IMAGE_TAKEN && resultCode == RESULT_OK && data != null) {
            bmp = (Bitmap) data.getExtras().get("data");

            if (bmp != null) {
                mProfilePictureURI = utils.getImageUri(getApplicationContext(), bmp);
                mProfilePicture.setImageBitmap(bmp);
                Log.d("log helper", "URI stored at : " + mProfilePictureURI);
            }
        }
        if (selectedImage != null) {
            mProfilePictureURI = selectedImage;
            mProfilePicture.setImageURI(selectedImage);
        }
    }
}
