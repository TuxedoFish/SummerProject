package com.example.harry.umbrellafindr;

import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class RegisterActivity extends AppCompatActivity implements View.OnTouchListener, ProfilePictureDialogBox.DialogListener{

    public SwitchCompat mGenderSwitch;
    public ImageView mProfilePicture;

    private static final int RESULT_LOAD_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mGenderSwitch = findViewById(R.id.switchGender);
        mGenderSwitch.setSwitchTextAppearance(getApplicationContext(), R.style.switchTextStyle);

        mProfilePicture = findViewById(R.id.imageView4);
        mProfilePicture.setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.imageView4:
                DialogFragment profilePictureDialogBoxFragment = new ProfilePictureDialogBox();
                profilePictureDialogBoxFragment.show(getFragmentManager(), "profiles");
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
