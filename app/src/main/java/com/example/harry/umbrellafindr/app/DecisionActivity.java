package com.example.harry.umbrellafindr.app;

import android.graphics.Paint;
import android.graphics.drawable.RippleDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.harry.umbrellafindr.R;
import com.example.harry.umbrellafindr.utils.DatabaseLogic;
import com.example.harry.umbrellafindr.utils.Utilities;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;

import javax.annotation.Nullable;

public class DecisionActivity extends AppCompatActivity implements View.OnTouchListener{

    private DatabaseLogic databaseLogic;

    private ImageButton confirmButton;
    private TextView declineText;
    private RippleDrawable confirmButtonRipple;

    private TextView nameText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decision);

        databaseLogic = new DatabaseLogic();
        Utilities utils = new Utilities();

        nameText = findViewById(R.id.name);

        String id = getIntent().getStringExtra("request_user_id");

        utils.getUserInfo(databaseLogic.getDb(), id, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                nameText.setText((String)documentSnapshot.get("first_name"));
                nameText.setPaintFlags(nameText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            }
        });

        confirmButton = (ImageButton) findViewById(R.id.confirm_button);
        confirmButton.setOnTouchListener(this);
        confirmButtonRipple = (RippleDrawable) confirmButton.getBackground();

        declineText = findViewById(R.id.politely_decline_text);
        declineText.setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch(v.getId()) {
            case R.id.confirm_button:
                //Send back a positive request
                confirmButtonRipple.setHotspot(event.getX(), event.getY());
            break;

            case R.id.politely_decline_text:
                //Send back a negative response
            break;
        }

        return super.onTouchEvent(event);
    }
}
