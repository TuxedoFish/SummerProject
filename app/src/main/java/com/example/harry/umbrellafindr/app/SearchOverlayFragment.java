package com.example.harry.umbrellafindr.app;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSeekBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.example.harry.umbrellafindr.R;

public class SearchOverlayFragment extends Fragment {

    public SeekBar mSeekBar;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View viewroot = inflater.inflate(R.layout.fragment_search_overlay, container, false);
        return viewroot;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mSeekBar = (AppCompatSeekBar)getView().findViewById(R.id.seekBar);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
}
