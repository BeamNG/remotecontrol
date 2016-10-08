package com.beamng.remotecontrol;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ProgressBar;

public class InverseProgressBar extends ProgressBar {

    public InverseProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public InverseProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public InverseProgressBar(Context context) {
        super(context);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        canvas.scale(-1f, 1f, super.getWidth() * 0.5f, super.getHeight() * 0.5f);
        super.onDraw(canvas);
    }

}
