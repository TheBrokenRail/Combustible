package com.thebrokenrail.combustible.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.thebrokenrail.combustible.R;

public class DepthGauge extends LinearLayout {
    private int depth = 0;
    private final int width;
    private final int lineWidth;
    private final Paint linePaint;

    private final int topMargin;
    private int previousDepth = -1;

    public DepthGauge(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        // Enable Drawing
        setWillNotDraw(false);

        // Get Width
        width = context.getResources().getDimensionPixelSize(R.dimen.depth_gauge_width);
        lineWidth = context.getResources().getDimensionPixelSize(R.dimen.depth_gauge_line_width);

        // Color
        int[] colorAttrs = new int[]{com.google.android.material.R.attr.colorOnSurface};
        TypedArray typedArray = context.obtainStyledAttributes(colorAttrs);
        int color = typedArray.getResourceId(0, 0);
        typedArray.recycle();

        // Create Paint
        linePaint = new Paint();
        linePaint.setStyle(Paint.Style.FILL);
        linePaint.setColor(ContextCompat.getColor(context, color));

        // Get Top Margin
        topMargin = context.getResources().getDimensionPixelSize(R.dimen.feed_item_margin);
    }

    public void setDepth(int depth, int previousDepth) {
        // Find Original Padding
        int originalPadding = getPaddingStart() - (this.depth * width);
        // Set New Padding
        this.depth = depth;
        setPaddingRelative((depth * width) + originalPadding, getPaddingTop(), getPaddingEnd(), getPaddingBottom());
        // Top Margin
        this.previousDepth = previousDepth;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw Lines
        boolean isRtl = getLayoutDirection() == LAYOUT_DIRECTION_RTL;
        for (int i = 0; i < depth; i++) {
            boolean chopTopMargin = previousDepth < (i + 1);
            int top = chopTopMargin ? topMargin : 0;
            float alpha = ((float) (i + 1)) / Util.MAX_DEPTH;
            linePaint.setAlpha((int) (alpha * 255));
            if (isRtl) {
                int x = getWidth() - (width * i);
                canvas.drawRect(x - lineWidth, top, x, getHeight(), linePaint);
            } else {
                int x = width * i;
                canvas.drawRect(x, top, x + lineWidth, getHeight(), linePaint);
            }
        }
    }
}
