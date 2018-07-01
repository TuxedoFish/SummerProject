package com.example.harry.umbrellafindr.app;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.harry.umbrellafindr.R;
import com.example.harry.umbrellafindr.utils.Constants;

public class HubActivity extends AppCompatActivity implements MapFragment.Communicator, SearchOverlayFragment.clickListener{
    private SearchOverlayFragment mSearchOverlayFragment;
    private MapFragment mMapFragment;

    @Override
    public void onSearch(View v) {
        mMapFragment.beginSearch();
    }

    @Override
    public void sendRequest(String user_id) {
        Intent toDecision = new Intent(HubActivity.this, DecisionActivity.class);
        toDecision.putExtra("request_user_id", user_id);

        startActivityForResult(toDecision, Constants.REQUEST_DECISION_FROM_USER);
    }

    private void closeFragment(int id) {
        FragmentManager fragmentManager = getFragmentManager();
        if(fragmentManager.findFragmentById(id) != null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.remove(fragmentManager.findFragmentById(id));
            transaction.commit();
        }
    }

    private void openFragment(Fragment fragment, int id) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        transaction.replace(id, fragment);
        transaction.disallowAddToBackStack();
        transaction.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hub);

        mMapFragment = new MapFragment();
        openFragment(mMapFragment, R.id.contentPanel);
        mSearchOverlayFragment = new SearchOverlayFragment();
        openFragment(mSearchOverlayFragment, R.id.overlayPanel);

//        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
//        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }
}
