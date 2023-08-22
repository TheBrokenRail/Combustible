package com.thebrokenrail.combustible.util;

import android.annotation.SuppressLint;
import android.graphics.RectF;
import android.text.Layout;
import android.text.Spannable;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class LinkOnTouchListener implements View.OnTouchListener {
    // https://github.com/saket/Better-Link-Movement-Method/blob/64be11b73db868a4fa615b53c0a76174227a7585/better-link-movement-method/src/main/java/me/saket/bettermovementmethod/BetterLinkMovementMethod.java#L294-L332
    private ClickableSpan findClickableSpanUnderTouch(TextView textView, Spannable text, MotionEvent event) {
        int touchX = (int) event.getX();
        int touchY = (int) event.getY();
        touchX -= textView.getTotalPaddingLeft();
        touchY -= textView.getTotalPaddingTop();

        final Layout layout = textView.getLayout();
        final int touchedLine = layout.getLineForVertical(touchY);
        final int touchOffset = layout.getOffsetForHorizontal(touchedLine, touchX);

        final RectF touchedLineBounds = new RectF();
        touchedLineBounds.left = layout.getLineLeft(touchedLine);
        touchedLineBounds.top = layout.getLineTop(touchedLine);
        touchedLineBounds.right = layout.getLineWidth(touchedLine) + touchedLineBounds.left;
        touchedLineBounds.bottom = layout.getLineBottom(touchedLine);

        if (touchedLineBounds.contains(touchX, touchY)) {
            final Object[] spans = text.getSpans(touchOffset, touchOffset, ClickableSpan.class);
            for (final Object span : spans) {
                if (span instanceof ClickableSpan) {
                    return (ClickableSpan) span;
                }
            }
        }
        return null;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        boolean ret = false;
        CharSequence text = ((TextView) v).getText();
        Spannable spannable = Spannable.Factory.getInstance().newSpannable(text);
        TextView widget = (TextView) v;
        int action = event.getAction();

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
            ClickableSpan link = findClickableSpanUnderTouch(widget, spannable, event);

            if (link != null) {
                if (action == MotionEvent.ACTION_UP) {
                    link.onClick(widget);
                }
                ret = true;
            }
        }
        return ret;
    }
}
