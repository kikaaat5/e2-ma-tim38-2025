package com.example.mobileapplication.ui;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.style.LineBackgroundSpan;

public class DotSpanEx implements LineBackgroundSpan {
    private final float radius;
    private final int color;
    private final int index;

    public DotSpanEx(float radius, int color, int index) {
        this.radius = radius;
        this.color = color;
        this.index = index;
    }

    @Override public void drawBackground(Canvas c, Paint p, int left, int right, int top,
                                         int baseline, int bottom, CharSequence text, int start, int end, int lnum) {
        int old = p.getColor();
        p.setColor(color);
        float cx = (left + right) / 2f + (index - 1) * (radius * 2.2f); // -1,0,1
        float cy = bottom + radius * 1.5f;
        c.drawCircle(cx, cy, radius, p);
        p.setColor(old);
    }
}
