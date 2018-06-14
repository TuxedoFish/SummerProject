package com.example.harry.umbrellafindr.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatSeekBar;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.SeekBar;

import com.example.harry.umbrellafindr.R;

public class TextThumbBarSeek extends AppCompatSeekBar {

    private int mThumbSize;
    private TextPaint mTextPaint;

    public TextThumbBarSeek(Context context) {
        this(context, null);
    }

    public TextThumbBarSeek(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.seekBarStyle);
    }

    public TextThumbBarSeek(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mThumbSize = getResources().getDimensionPixelSize(R.dimen.thumb_size);

        mTextPaint = new TextPaint();
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.thumb_text_size));
        mTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    private String calculateText(int progress) {
        if(progress==0) {
            return "15m";
        } else if(progress<25) {
            return "30m";
        } else if (progress<50) {
            return "45m";
        } else if(progress<75) {
            return "1h";
        } else if (progress==100) {
            return ">2h";
        } else {
            return "2h";
        }
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        String progressText = calculateText(getProgress());
        Rect bounds = new Rect();
        mTextPaint.getTextBounds(progressText, 0, progressText.length(), bounds);

        int leftPadding = getPaddingLeft() - getThumbOffset();
        int rightPadding = getPaddingRight() - getThumbOffset();
        int width = getWidth() - leftPadding - rightPadding;
        float progressRatio = (float) getProgress() / getMax();
        float thumbOffset = mThumbSize * (.5f - progressRatio);
        float thumbX = progressRatio * width + leftPadding + thumbOffset;
        float thumbY = getHeight() / 2f + bounds.height() / 2f;
        canvas.drawText(progressText, thumbX, thumbY, mTextPaint);
    }
}

