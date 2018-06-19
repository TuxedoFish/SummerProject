package com.example.harry.umbrellafindr.app;

import android.app.Fragment;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSeekBar;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;

import com.example.harry.umbrellafindr.R;

public class SearchOverlayFragment extends Fragment {
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search_overlay, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

    }
}
