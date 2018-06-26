package com.example.harry.umbrellafindr.app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.harry.umbrellafindr.R;
import com.example.harry.umbrellafindr.utils.DatabaseLogic;

public class DecisionActivity extends AppCompatActivity implements View.OnClickListener{

    private DatabaseLogic databaseLogic;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decision);

        databaseLogic = new DatabaseLogic();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.confirm_button:
                //Send back a positive request
            break;

            case R.id.politely_decline_text:
                //Send back a negative response
            break;
        }
    }
}
