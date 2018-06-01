package com.example.harry.umbrellafindr;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

public class ProfileActivity extends AppCompatActivity {

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_profile:
                    ProfileFragment profile = new ProfileFragment();
                    openFragment(profile, R.id.contentPanel);
                    return true;
                case R.id.navigation_map:
                    MapFragment maps = new MapFragment();
                    openFragment(maps, R.id.contentPanel);
                    SearchOverlayFragment searchOverlayFragment = new SearchOverlayFragment();
                    openFragment(searchOverlayFragment, R.id.overlayPanel);
                    return true;
                case R.id.navigation_messages:
                    MessagesFragment messages = new MessagesFragment();
                    openFragment(messages, R.id.contentPanel);
                    return true;
            }
            return false;
        }
    };

    private void openFragment(Fragment fragment, int id) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        transaction.replace(id, fragment);
        transaction.addToBackStack(null);
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

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

}
