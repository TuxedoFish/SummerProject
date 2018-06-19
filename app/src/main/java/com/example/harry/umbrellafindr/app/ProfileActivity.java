package com.example.harry.umbrellafindr.app;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

import com.example.harry.umbrellafindr.R;
import com.example.harry.umbrellafindr.utils.User;

public class ProfileActivity extends AppCompatActivity {
    public ImageButton mSearchButton;
    public RippleDrawable mRippleRegister;

    private User mUser;
    private User[] fakeUsers;

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
        setContentView(R.layout.activity_profile);

        MapFragment maps = new MapFragment();
        openFragment(maps, R.id.contentPanel);
        SearchOverlayFragment searchOverlayFragment = new SearchOverlayFragment();
        openFragment(searchOverlayFragment, R.id.overlayPanel);

//        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
//        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        mSearchButton = (ImageButton)findViewById(R.id.searchButton);
        mRippleRegister = (RippleDrawable)mSearchButton.getBackground();

        mSearchButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mRippleRegister.setHotspot(event.getX(), event.getY());
                searchForUsers();
                return false;
            }
        });
    }

    public void searchForUsers() {

    }

}
